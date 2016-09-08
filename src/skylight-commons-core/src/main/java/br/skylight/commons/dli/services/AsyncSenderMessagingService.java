package br.skylight.commons.dli.services;

import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import br.skylight.commons.infra.SyncCondition;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.io.dataterminal.DataTerminal;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceInjection;

public class AsyncSenderMessagingService extends MessagingService {

	private static final Logger logger = Logger.getLogger(AsyncSenderMessagingService.class.getName());
	public static final int MAX_SEND_QUEUE_SIZE = 30;
	
//	private List<Message> toBeSent = new CopyOnWriteArrayList<Message>();
	private Queue<Message> toBeSentQueue;
	private ThreadWorker senderWorker;
	
	private ReentrantLock sendLock = new ReentrantLock();
	private SyncCondition messageAvailableCondition = new SyncCondition("Message available to be sent");
	
	@ServiceInjection
	public PluginManager pluginManager;

	public AsyncSenderMessagingService() {
		toBeSentQueue = new PriorityQueue<Message>(20, new Comparator<Message>() {
			public int compare(Message o1, Message o2) {
				//higher priority
				if(o1.getMessageType().getPriority()<o2.getMessageType().getPriority()) {
					return -1;
				//same priority
				} else if(o1.getMessageType().getPriority()==o2.getMessageType().getPriority()) {
					if(o1.getTimeStamp()==o2.getTimeStamp()) {
						if(o1.getReuseCounter()<o2.getReuseCounter()) {
							return -1;
						} else if(o1.getReuseCounter()>o2.getReuseCounter()) {
							return 1;
						} else {
							return 0;
						}
					} else if(o1.getTimeStamp()<o2.getTimeStamp()) {
						return -1;
					} else {
						return 1;
					}
				//lower priority
				} else {
					return 1;
				}
			}
		});

		senderWorker = new ThreadWorker(DataTerminal.MAX_PACKETS_PER_SECOND) {
			Message m = null;
			@Override
			public void onActivate() throws Exception {
				setName("AsyncSenderMessagingService.senderWorker");
			}
			@Override
			public void step() throws Exception {
				synchronized(toBeSentQueue) {
					m = toBeSentQueue.poll();
				}
				if(m!=null) {
					sendMessageSynchronously(m);
				} else {
					messageAvailableCondition.notifyConditionNotMet();
					messageAvailableCondition.waitForConditionMet();
				}
			}
		};
	}

	@Override
	public void onActivate() throws Exception {
		pluginManager.manageObject(senderWorker);
		super.onActivate();
	}
	
	@Override
	public void onDeactivate() throws Exception {
		pluginManager.unmanageObject(senderWorker);
		super.onDeactivate();
	}
	
	public void sendMessageSynchronously(Message message) throws IOException {
		super.sendMessage(message);
	}
	
	@Override
	public void sendMessage(Message message) {
		try {
			sendLock.lock();
	//		System.out.println("SCHLE1 " + message.getTimeElapsed());
			//AVOID TOO MANY MESSAGES IN SEND QUEUE (may be the link is down)
			if(toBeSentQueue.size()>=MAX_SEND_QUEUE_SIZE) {
				logger.finest("Won't send more messages. Send queue is full. messageType=" + message.getMessageType() + "; queueSize=" + toBeSentQueue.size());
				return;
			}

			//remove any previously added message that was not sent to force message update (to avoid sending 'old' message when a newer is available - for example for updating unsent state reports)
			//the 'equals()' method of each Message determines if a message contains the same sort of information as another and could be replaced by a newer version of the message
			Message remove = null;
			
			//Not that the following statement will be synchronized with the operation of
			//pooling a message from queue to send it (as in step() from senderThread) because
			//both are modifying structurally the same queue
			synchronized(toBeSentQueue) {
				for (Message em : toBeSentQueue) {
					if(em.equals(message)) {
						remove = em;
						break;
					}
				}
				if(remove!=null) {
					if(toBeSentQueue.remove(message)) {
						logger.fine("An older version of '"+ remove.getMessageType() +"' was replaced before being sent");
					}
				}
				if(!toBeSentQueue.offer(message)) {
					logger.finest("Couldn't offer message to send queue. Discarding it.");
				} else {
					messageAvailableCondition.notifyConditionMet();
				}
			}
	//		System.out.println("SCHLE2 " + message.getTimeElapsed());
		} finally {
			sendLock.unlock();
		}
	}

	public int getInternalSendQueueSize() {
		return toBeSentQueue.size();
	}

}
