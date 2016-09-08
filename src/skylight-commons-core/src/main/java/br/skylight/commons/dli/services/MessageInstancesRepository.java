package br.skylight.commons.dli.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.StackKeyedObjectPool;

public class MessageInstancesRepository {

	private static final Logger logger = Logger.getLogger(MessageInstancesRepository.class.getName());
	public static final int MAX_INSTANCES_FOR_EACH_TYPE = 50;
	
	//message instances for reading packets
	private Map<Long,Message> messagesForReceiving = Collections.synchronizedMap(new HashMap<Long,Message>());

	private StackKeyedObjectPool messageForSendingPool;

	public MessageInstancesRepository() {
		messageForSendingPool = new StackKeyedObjectPool(new KeyedPoolableObjectFactory() {
			public Object makeObject(Object key) throws Exception {
				return ((Class)key).newInstance();
			}
			public boolean validateObject(Object key, Object obj) {
				return true;
			}
			public void passivateObject(Object key, Object obj) throws Exception {}
			public void destroyObject(Object key, Object obj) throws Exception {}
			public void activateObject(Object key, Object obj) throws Exception {}
		}, MAX_INSTANCES_FOR_EACH_TYPE);
	}

	/**
	 * Only one instance of a type is created for receiving operations.
	 * All receiving message listeners must ensure to make a copy of the
	 * received message if it needs to store it or use it later because
	 * subsequent messages received for the same type will be read
	 * using the same message instance.
	 * @param type
	 * @return
	 */
	public Message resolveMessageForReceiving(long type) {
		synchronized(messagesForReceiving) {
			Message m = messagesForReceiving.get(type);
			if(m==null) {
				m = MessageFactory.newInstance(type);
				messagesForReceiving.put(type, m);
			}
//			System.out.println("INSTANCES " + messagesForReceiving.size());
			return m;
		}
	}

	/**
	 * Get an available message instance that can be used for sending.
	 * After MessagingService sends the message, it will return this instance to the pool.
	 * @param implementation
	 * @return
	 */
	public Message borrowToBeSentMessageFromPool(Class<? extends Message> implementation) {
		try {
			return (Message) messageForSendingPool.borrowObject(implementation);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * You should only return messages that were gotten by resolveMessageForSending for
	 * this to work properly
	 * @param message
	 */
	public void returnSendMessageToPool(Message message) {
		try {
			messageForSendingPool.returnObject(message.getClass(), message);
		} catch (Exception e) {
			logger.warning("Couldn't return message for sending to pool. e=" + e.toString());
			logger.throwing(null, null, e);
			try {
				messageForSendingPool.invalidateObject(message.getClass(), message);
			} catch (Exception e1) {
				logger.throwing(null, null, e);
			}
		}
	}
	
	public StackKeyedObjectPool getMessageForSendingPool() {
		return messageForSendingPool;
	}

}
