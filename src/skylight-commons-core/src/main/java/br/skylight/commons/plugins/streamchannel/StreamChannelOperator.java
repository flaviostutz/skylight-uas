package br.skylight.commons.plugins.streamchannel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.skylight.StreamChannelCommand;
import br.skylight.commons.dli.skylight.StreamChannelData;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointDefinition;
import br.skylight.commons.plugin.annotations.ServiceInjection;

@ExtensionPointDefinition
public abstract class StreamChannelOperator extends Worker {

	private static final Logger logger = Logger.getLogger(StreamChannelOperator.class.getName());
	
	public static final int OPEN_CHANNEL = 111111;
	public static final int CLOSE_CHANNEL = 222222;
	
	private int channelNumber;
	private boolean channelOpen;
	private int cucsId;
	private int vehicleId;
	private InputToOutputWorker inputToOutputWorker;
	
	private List<StreamListener> listeners = new ArrayList<StreamListener>();
	
	@ServiceInjection
	public PluginManager pluginManager;
	
	@ServiceInjection
	public MessagingService messagingService;
	
	public StreamChannelOperator(int channelNumber) {
		this.channelNumber = channelNumber;
	}
	
	public int getChannelNumber() {
		return channelNumber;
	}

	public void onMessageReceived(Message message) {
		try {
			if(message instanceof StreamChannelCommand) {
				StreamChannelCommand m = (StreamChannelCommand)message;
				if(m.getCommandNumber()==OPEN_CHANNEL) {
					logger.info("Received request to open stream channel " + getChannelNumber());
					if(channelOpen) {
						if(m.getVehicleID()==vehicleId && m.getCucsID()==cucsId) {
							reopenChannel();
						} else {
							logger.info("This channel is already open with another cucs. Ignoring request.");
						}
					} else {
						openChannelInternal(m.getCucsID(), m.getVehicleID());
					}
				} else if(m.getCommandNumber()==CLOSE_CHANNEL) {
					logger.info("Received request to close stream channel " + getChannelNumber());
					closeChannelInternal();
				} else {
					onCommandReceived(m.getCommandNumber(), m.getCommandText());
				}
			
			} else if(message instanceof StreamChannelData) {
				StreamChannelData m = (StreamChannelData)message;
				if(channelOpen) {
					if(m.getVehicleID()!=vehicleId || m.getCucsID()!=cucsId) {
						logger.info("Ignoring stream message because this channel was stablished between other vehicleId/cucsId elements");
					} else {
						//TODO TEST IS OS CAN RECEIVE MORE BYTES AND DISCARD DATA IF IT IS FULL TO AVOID BLOCKING MessagingService receiver thread (will lock everything!)
						getOutputStreamFromTarget().write(m.getData(), 0, m.getDataLength());
					}
				} else {
					logger.info("Stream channel " + getChannelNumber() + " is not open thus cannot receive data");
				}
			}
		} catch (Exception e) {
			try {
				logger.fine("Cannot write to target stream. Closing channel.");
				closeChannel();
			} catch (Exception e1) {
				logger.throwing(null, null, e1);
			}
		}
	}

	protected abstract void onCommandReceived(long commandNumber, String commandText);

	public void reopenChannel() throws IOException {
		if(channelOpen) {
			closeChannelInternal();
		}
		openChannelInternal(cucsId, vehicleId);
	}

	public void openChannel(int cucsId, int vehicleId) throws IOException {
		if(!channelOpen) {
			openChannelInternal(cucsId, vehicleId);
			sendChannelCommand(OPEN_CHANNEL, "");
		} else {
			logger.info("This channel is already open. Ignoring request.");
		}
	}
	
	protected void openChannelInternal(int cucsId, int vehicleId) throws IOException {
		if(!channelOpen) {
			this.cucsId = cucsId;
			this.vehicleId = vehicleId;
			try {
				openStreamsFromTarget();
				
				//prepare inputstream listener for generating output messages
				inputToOutputWorker = new InputToOutputWorker(this, getInputStreamFromTarget(), channelNumber, getMaxBytesPerSecond());
				pluginManager.manageObject(inputToOutputWorker);
				channelOpen = true;
				
				for (StreamListener sl : listeners) {
					sl.onChannelOpened();
				}
				
			} catch (IOException e) {
				logger.warning("There was a problem opening channel " + getChannelNumber() + " e=" + e.toString());
				logger.throwing(null, null, e);
				sendChannelCommand(CLOSE_CHANNEL, "");
				channelOpen = false;
				throw e;
			}
			
		} else {
			logger.info("This channel is already open. Ignoring request.");
		}
	}

	public void closeChannel() throws IOException {
		if(channelOpen) {
			sendChannelCommand(CLOSE_CHANNEL, "");
			closeChannelInternal();
		} else {
			logger.info("This channel is already closed. Ignoring request.");
		}
	}
	protected void closeChannelInternal() throws IOException {
		if(channelOpen) {
			if(inputToOutputWorker!=null) {
				pluginManager.unmanageObject(inputToOutputWorker);
				inputToOutputWorker = null;
			}
			closeStreamsFromTarget();
			channelOpen = false;
			for (StreamListener sl : listeners) {
				sl.onChannelClosed();
			}
		} else {
			logger.info("This channel is already closed. Ignoring request.");
		}
	}

	public int getVehicleId() {
		return vehicleId;
	}
	public int getCucsId() {
		return cucsId;
	}

	public void sendChannelCommand(long commandNumber, String commandText) {
		StreamChannelCommand m = messagingService.resolveMessageForSending(StreamChannelCommand.class);
		m.setChannelNumber(channelNumber);
		m.setCommandNumber(commandNumber);
		m.setCommandText(commandText);
		m.setCucsID(cucsId);
		m.setVehicleID(vehicleId);
		messagingService.sendMessage(m);
	}
	
	public void addStreamListener(StreamListener streamListener) {
		listeners.add(streamListener);
	}
	
	public boolean isChannelOpen() {
		return channelOpen;
	}

	public void waitUntilAllPendingBytesWereSent() throws TimeoutException {
		inputToOutputWorker.waitUntilAllPendingBytesWereSent();
	}
	
	protected abstract void openStreamsFromTarget() throws IOException;
	protected abstract void closeStreamsFromTarget() throws IOException;
	protected abstract InputStream getInputStreamFromTarget() throws IOException;
	protected abstract OutputStream getOutputStreamFromTarget() throws IOException;
	protected abstract int getMaxBytesPerSecond();
	
}
