package br.skylight.commons.dli.services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;

import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.dli.messagetypes.GenericInformationRequestMessage;
import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.io.dataterminal.DataPacketListener;
import br.skylight.commons.io.dataterminal.DataTerminal;
import br.skylight.commons.io.dataterminal.UDPMulticastDataTerminal;
import br.skylight.commons.io.dataterminal.UDPUnicastDataTerminal;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceInjection;

@ServiceDefinition
public class MessagingService extends Worker implements DataPacketListener {

	private static final Logger logger = Logger.getLogger(MessagingService.class.getName());

	@ServiceInjection
	public PluginManager pluginManager;
	
	private String name;
	
	//OUTPUT MESSAGES
	private byte[] outIdd;//defined in constructor
	//message data output
	private DataOutputStream datados;
	private ByteArrayOutputStream2 databos;
	//wrapper output
	private Checksum outCheckSum;
	private ByteArrayOutputStream2 bos;
	private CheckedOutputStream cos;
	private DataOutputStream dos;
	private long outputErrors = 0;
	//next message instance ID for each type
	private long[] outMessageTypeID = new long[3000];
	protected MessageInstancesRepository messageInstancesRepository;
	
	//INPUT MESSAGES
	private static final byte[] inIdd = new byte[10];
	//wrapper input
	private Checksum inCheckSum;
	private ByteArrayInputStream2 bis;
	private CheckedInputStream cis;
	private DataInputStream dis;
	private double lastLatencyTime;
	private long inputErrors = 0;
	//last message instance ID for each type
	private long[] inMessageTypeID = new long[3000];
	
	private DataTerminal dataTerminal;
	private MessageListener messageListener = null;
	private MessageSentListener messageSentListener = null;
	private Map<MessageType,MessageListener> messageListeners = new HashMap<MessageType,MessageListener>();
	private Map<MessageType,MessageSentListener> messageSentListeners = new HashMap<MessageType,MessageSentListener>();

	private MessageResender messageResender;
	
	//synchronization mutex
	private ReentrantLock sendLock = new ReentrantLock();
	private ReentrantLock receiveLock = new ReentrantLock();

	private Message lastMessageReceived;
	private Message lastMessageSent;

	public MessagingService() {
		try {
			//idd contents
			outIdd = new byte[10];
			byte[] idd = "2.5".getBytes("US-ASCII");
			System.arraycopy(idd, 0, outIdd, 0, idd.length);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		//OUTPUT
		//TODO verify if this checksum is compliant to spec's checksum method
		outCheckSum = new Adler32();
		bos = new ByteArrayOutputStream2(600);
		cos = new CheckedOutputStream(bos, outCheckSum);
		dos = new DataOutputStream(cos);
		databos = new ByteArrayOutputStream2(600);
		datados = new DataOutputStream(databos);
		messageInstancesRepository = new MessageInstancesRepository();
		
		//INPUT
		inCheckSum = new Adler32();
		bis = new ByteArrayInputStream2(new byte[0]);
		cis = new CheckedInputStream(bis, inCheckSum);
		dis = new DataInputStream(cis);
	}

	@Override
	public void onActivate() throws Exception {
		messageResender = new MessageResender(this);
		pluginManager.manageObject(messageResender);
		super.onActivate();
	}
	
	//SEND OUTGOING MESSAGES NOW
	public void sendMessage(Message message, boolean resendUntilAcknowledgedByReceiver) {
		//activate mechanism that guarantees message delivery
		if(resendUntilAcknowledgedByReceiver) {
			messageResender.startToResendUntilAcknowlegedByReceiver(message);
		}
		
		sendMessage(message);
	}
	
	public void sendMessage(Message message) {
		if(message.getVehicleID()==0) throw new IllegalArgumentException("'vehicleId' cannot be zero");
		if(message.getCucsID()==0) throw new IllegalArgumentException("'cucsId' cannot be zero");
		if(message.getTimeStamp()==0) throw new IllegalArgumentException("'timeStamp' cannot be zero");
		try {
			sendLock.lock();
			bos.reset();
			outCheckSum.reset();
			
			//FIELDS FROM MESSAGE WRAPPER
			//idd field
			dos.write(outIdd);
			
			//msg instance id
			long type = message.getMessageType().getNumber();
			int mid = (int)outMessageTypeID[(int)type]++;
			dos.writeInt(mid);
			
			//msg type
			dos.writeInt((int)type);
			
			//msg data length
			databos.reset();
			message.writeState(datados);
			dos.writeInt(databos.size());
	
			//stream id
			dos.writeInt(0);//fixed
	
			//packet seq
			dos.writeInt(-1);//fixed
	
			//message data
			dos.write(databos.getBuffer(), 0, databos.size());
	
			//checksum
			dos.writeInt((int)outCheckSum.getValue());
	
			if(dataTerminal!=null) {
				dataTerminal.sendPacket(bos.getBuffer(), bos.size());
				logger.finer(name + ": SENT message " + message.getMessageType() + ". size=" + bos.size() + "; mid=" + mid);

				//return message instance to pool so it can be reused
				messageInstancesRepository.returnSendMessageToPool(message);
				
				//notify message sent listeners
				if(messageSentListener!=null) {
					messageSentListener.onMessageSent(message);
				}
				MessageSentListener ml = messageSentListeners.get(message.getMessageType());
				if(ml!=null) {
					ml.onMessageSent(message);
				}
				lastMessageSent = message;
				
			} else {
				logger.warning(name + ": message won't be sent. MessagingService not bound to a DataTerminal");
			}
		} catch (Exception e) {
			logger.throwing(null,null,e);
			logger.warning("Couldn't send message " + message.getMessageType() + ". Discarding it. " + e.toString());
//			e.printStackTrace();
			outputErrors++;
		} finally {
			sendLock.unlock();
		}
	}

	public void bindTo(DataTerminal dataTerminal) throws Exception {
		logger.info("Messaging " + name + " is being bound to " + dataTerminal.getInfo());
		this.dataTerminal = dataTerminal;
		dataTerminal.setDataPacketListener(this);
		
		//register dataTerminal as listener of all ADT/GDT data terminal related messages
		if(dataTerminal.getDataTerminalType()!=null) {
			setMessageListener(MessageType.M400, dataTerminal);
			setMessageListener(MessageType.M401, dataTerminal);
			setMessageListener(MessageType.M501, dataTerminal);
		}
		
		dataTerminal.setName(getName());
	}

	public void unbind() throws Exception {
		if(dataTerminal!=null) {
			messageListeners.remove(MessageType.M400);
			messageListeners.remove(MessageType.M401);
			messageListeners.remove(MessageType.M501);
			dataTerminal.forceDeactivation(3000);
		}
		this.dataTerminal = null;
	}
	
	public DataTerminal getDataTerminal() {
		return dataTerminal;
	}
	
	//INCOMING MESSAGES
	@Override
	public void onPacketReceived(byte[] data, int len, double timestamp) {
		int type = 0;
		try {
			receiveLock.lock();
			bis.setBuffer(data);
			inCheckSum.reset();
			
			//FIELDS FROM MESSAGE WRAPPER
			try {
				//doc version
				IOHelper.readFully(dis, inIdd, 200, true);
			} catch (TimeoutException e) {
				throw new IOException(e);
			}
			
			//msg instance id
			long mid = IOHelper.readUnsignedInt(dis);
			
			//msg type
			type = (int)IOHelper.readUnsignedInt(dis);
			//verify if it is different only if next expected message id is > 0
			if(mid!=inMessageTypeID[type] && inMessageTypeID[type]!=0) {
				inputErrors++;
				logger.info(name + ": next expected message instance id was "+ inMessageTypeID[type] +" for message type "+type+" but received " + mid + ". Dropped messages?");
			}
			inMessageTypeID[type] = mid+1;
			
			//msg data length
			dis.readInt();//ignore contents
			
			//stream id
			dis.readInt();//ignore contents
			
			//packet seq
			dis.readInt();//ignore contents
			
			//message data
			Message message = messageInstancesRepository.resolveMessageForReceiving(type);
			message.setMessageInstanceId(mid);
			message.setReceiveTimeStamp(timestamp);
			message.readState(dis);
			logger.finer(name + ": RECEIVED message " + message.getMessageType() + ". size=" + len + "; mid=" + message.getMessageInstanceId() + "; latency=" + (int)(message.getLatency()*1000) + "ms");
	
			//checksum verification
			long calculatedCheckSum = inCheckSum.getValue();
			long messageCheckSum = IOHelper.readUnsignedInt(dis);
			if(messageCheckSum!=calculatedCheckSum) {
				inputErrors++;
				logger.info(name + ": message checksum doesn't match. " + messageCheckSum + "!="+ calculatedCheckSum + "; type=" + type + "; mid=" + mid);
				
			} else {
				lastLatencyTime = message.getLatency();
				if(lastLatencyTime>(message.getMessageType().getMaxLatency()/1000.0)) {
					logger.info(name + ": latency too high for " + message.getMessageType() + " (>" + message.getMessageType().getMaxLatency() + "ms). latency=" + (int)(lastLatencyTime*1000.0) + " ms");
				}
				
				//notify message listeners
				if(messageListener!=null) {
					messageListener.onMessageReceived(message);
				}
				MessageListener ml = messageListeners.get(message.getMessageType());
				if(ml!=null) {
					ml.onMessageReceived(message);
				}
				lastMessageReceived = message;
				if(ml==null && messageListener==null) {
					inputErrors++;
					logger.info(name + ": message "+message.getMessageType()+" received but no listener defined. type=" + message.getMessageType());
				}
//				System.out.println("OK");
			}
		} catch (Exception e) {
			outputErrors++;
			logger.throwing(null,null,e);
//			logger.info("Couldn't process incoming message. errors=" + outputErrors + "; messageType="+ type +"; e=" + e.toString());
			System.out.println("Couldn't process incoming message. errors=" + outputErrors + "; messageType="+ type +"; e=" + e.toString());
			e.printStackTrace();
		} finally {
			receiveLock.unlock();
		}
	}
	
	public <T extends Message> T resolveMessageForSending(Class<? extends Message> implementation) {
		T message = (T)messageInstancesRepository.borrowToBeSentMessageFromPool(implementation);
		message.incrementReuseCounter();
		message.resetValues();
		message.setTimeStamp(System.currentTimeMillis()/1000.0);
		return message;
	}
	public void returnUnsentMessageToPool(Message message) {
		message.decrementReuseCounter();
		messageInstancesRepository.returnSendMessageToPool(message);
	}
	
	public double getLastLatencyTime() {
		return lastLatencyTime;
	}
	
	public long getInputErrors() {
		return inputErrors;
	}
	public long getOutputErrors() {
		return outputErrors;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}

	public void setMessageListener(MessageType messageType, MessageListener messageListener) {
		if(messageListeners.get(messageType)!=null) {
			logger.info("'messageListener' is already defined and will be replaced. type=" + this + "; existingMessageListener=" + messageListeners.get(messageType).getClass().getName() + "; newMessageListener=" + messageListener.getClass().getName());
		}
		logger.finest("Setting " + messageListener.getClass().getName() + " as listener for message " + this);
		messageListeners.put(messageType, messageListener);
	}

	public void setMessageListener(MessageListener messageListener) {
		if(this.messageListener!=null) {
			logger.info("There is already another message listener registered that will be replaced. existing=" + this.messageListener + "; new=" + messageListener);
		}
		this.messageListener = messageListener;
	}
	
	public void sendRequestGenericInformation(MessageType messageType, int vehicleId) {
		GenericInformationRequestMessage m = resolveMessageForSending(GenericInformationRequestMessage.class);
		m.setRequestedMessageType(messageType);
		m.setVehicleID(vehicleId);
		sendMessage(m);
	}
	
	public void useNextPacketForSyncronization() {
		for (int i=0; i<inMessageTypeID.length; i++) {
			inMessageTypeID[i] = 0;
		}
	}

	protected void bindToAndActivate(DataTerminal dataTerminal) throws Exception {
		bindTo(dataTerminal);
		pluginManager.manageObject(dataTerminal);
	}
	
	public void bindToUDPMulticastDataTerminalWithOptionalFallbackToUDPUnicastAndActivate(NetworkInterface multicastNetworkInterface, String multicastUdpAddress, int multicastUdpSendPort, int multicastUdpReceivePort, DataTerminalType dataTerminalType, int dataLinkId, String unicastRemoteHost) throws Exception {
		DataTerminal dt = new UDPMulticastDataTerminal(multicastNetworkInterface, multicastUdpAddress, multicastUdpSendPort, multicastUdpReceivePort, dataTerminalType, dataLinkId);
		bindTo(dt);
		
		try {
			pluginManager.manageObject(dt);
		} catch (Exception e) {
			logger.severe("Error initializing multicast udp data terminal. e=" + e.getMessage() + ". To fallback to unicast udp, set env property 'fallback.unicastudp.remotehost'");
			e.printStackTrace();
			unbind();

			//optional fallback for unicast communications (not supported by STANAG standards and must be avoided in production)
			if(unicastRemoteHost!=null) {
				logger.warning("Falling back to udp unicast communications using remote host=" + unicastRemoteHost + "; sendPort=" + multicastUdpSendPort + "; receivePort=" + multicastUdpReceivePort);
				dt = new UDPUnicastDataTerminal(unicastRemoteHost, multicastUdpSendPort, multicastUdpReceivePort, null, 1);
				bindTo(dt);
				pluginManager.manageObject(dt);
			}
		}
	}
	
	public void setMessageSentListener(MessageSentListener msl) {
		this.messageSentListener = msl;
	}
	
	public MessageInstancesRepository getMessageInstancesRepository() {
		return messageInstancesRepository;
	}

	public Message getLastMessageReceived() {
		return lastMessageReceived;
	}
	
	public Message getLastMessageSent() {
		return lastMessageSent;
	}
	
}
