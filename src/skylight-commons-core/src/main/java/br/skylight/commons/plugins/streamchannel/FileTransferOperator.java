package br.skylight.commons.plugins.streamchannel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;

import br.skylight.commons.infra.SyncCondition;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.io.dataterminal.PipedInputStream2;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.services.StorageService;

public abstract class FileTransferOperator extends StreamChannelOperator {

	private static final Logger logger = Logger.getLogger(FileTransferOperator.class.getName());
	public enum Mode {IDLE, RECEIVING_FILE, SENDING_FILE} 

	private static final int TRANSFER_BYTES_WINDOW = 4000;
	
	private static final int COMMAND_START_RECEIVING = 1;
	private static final int COMMAND_START_SENDING = 2;
	private static final int COMMAND_TRANSFER_CHECK = 3;
	private static final int COMMAND_TRANSFER_CANCEL = 5;
	private static final int COMMAND_EXPECTED_RECEIVED_BYTES = 6;
	private static final int STATUS_TRANSFER_SUCCESSFULL = 7;
	private static final int STATUS_TRANSFER_MESSAGE = 8;
	private static final int STATUS_RECEIVE_PROGRESS = 9;
	
	private File file;
	private Mode mode = Mode.IDLE;
	private ThreadWorker transferWorker;
	private CheckedInputStream sourceStream;
	private CheckedOutputStream targetStream;
	
	private PipedInputStream2 pis1;
	private PipedInputStream2 pis2;
	private PipedOutputStream pos1;
	private PipedOutputStream pos2;
	private long expectedReceivedBytes;
	private long actualReceivedBytes;
	
	private long totalSentBytes;
	private long lastStatusSendLength;
	
	private FileTransferOperatorListener listener;
	private SyncCondition tooMuchPendingBytes = new SyncCondition("Too much pending bytes");
	
	@ServiceInjection
	public StorageService storageService;
	
	public FileTransferOperator(int channelNumber) {
		super(channelNumber);
	}

	@Override
	public void onActivate() throws Exception {
		transferWorker = new ThreadWorker(400) {
			private byte[] buffer = new byte[1024];
			@Override
			public void onActivate() throws Exception {
				setName("FileTransferOperator.transferWorker");
			}
			@Override
			public void step() throws Exception {
				try {
					//SEND DATA
					if(mode.equals(Mode.SENDING_FILE)) {
						int i = sourceStream.read(buffer);
						if(i!=-1) {
							pos1.write(buffer, 0, i);
							totalSentBytes += i;
							//implement window transfer to avoid network congestion/timeouts when dealing with slow lines
							verifyReceivedBytesWindow();
							try {
								tooMuchPendingBytes.waitForConditionNotMet(20000);
							} catch (TimeoutException e) {
								throw new TimeoutException("Timeout receiving transfer confirmation");
							}
						} else {
							//SEND CRC FOR FILE INTEGRITY CHECK
							waitUntilAllPendingBytesWereSent();
							Thread.sleep(100);
							sendChannelCommand(COMMAND_TRANSFER_CHECK, sourceStream.getChecksum().getValue()+"");
							finishTransfer();
						}
					//RECEIVE DATA
					} else if(mode.equals(Mode.RECEIVING_FILE)) {
						if(pis2.available()>0) {
							int i = pis2.read(buffer);
							targetStream.write(buffer, 0, i);
							actualReceivedBytes += i;
							if(listener!=null) {
								listener.onDataTransfer(getPercentReceived());
							}
							//send status each 3k that is received
							verifyReceivedBytesWindow();
							if((actualReceivedBytes-lastStatusSendLength)>(TRANSFER_BYTES_WINDOW/2)) {
								sendChannelCommand(STATUS_RECEIVE_PROGRESS, actualReceivedBytes+"");
								lastStatusSendLength = actualReceivedBytes;
								tooMuchPendingBytes.notifyConditionNotMet();
							}
						}
					} else {
						Thread.sleep(500);
					}
				} catch (Exception e) {
					e.printStackTrace();
					failTransfer("Problem on file transfer. e=" + e.toString());
				}
			}
		};
		pluginManager.manageObject(transferWorker);
	}

	private void verifyReceivedBytesWindow() {
		if((totalSentBytes-actualReceivedBytes)>TRANSFER_BYTES_WINDOW) {
			tooMuchPendingBytes.notifyConditionMet();
		} else {
			tooMuchPendingBytes.notifyConditionNotMet();
		}
	}
	
	public void finishTransfer() throws Exception {
		mode = Mode.IDLE;
		if(sourceStream!=null) {
			sourceStream.close();
		}
		if(targetStream!=null) {
			targetStream.flush();
			targetStream.close();
		}
		pos1.flush();
	}
	
	@Override
	protected void onCommandReceived(long commandNumber, String commandText) {
		try {
			if(commandNumber==COMMAND_START_SENDING) {
				String[] c = commandText.split("#");
				if(c[0].equals("(BASE)")) {
					file = new File(storageService.resolveDir("file-transfer"), c[1]);
				} else {
					file = new File(c[0], c[1]);
				}
				if(!file.exists()) {
					failTransfer("Aborting upload. File " + file.toString() + " doesn't exist");
					return;
				}
				expectedReceivedBytes = file.length();
				totalSentBytes = 0;
				sendChannelCommand(COMMAND_EXPECTED_RECEIVED_BYTES, expectedReceivedBytes+"");
				sourceStream = new CheckedInputStream(new FileInputStream(file), new CRC32());
				mode = Mode.SENDING_FILE;
				logger.info("Sending file " + file.getName());
				
			} else if(commandNumber==COMMAND_START_RECEIVING) {
				String[] c = commandText.split("#");
				//redirect file to be saved in SkylightUAV-updater dir if file is named 'updater.zip'
				//so that when the autopilot is restarted, the update will be applied
				if(c[1].equalsIgnoreCase("uav-updater.zip")) {
					logger.info("Saving received file to auto-updater directory");
					file = storageService.resolveFile("SkylightUAV-updater", c[1]);
				} else {
					file = storageService.resolveFile("file-transfer", c[1]);
				}
				file.delete();
				targetStream = new CheckedOutputStream(new FileOutputStream(file), new CRC32());
				actualReceivedBytes = 0;
				lastStatusSendLength = 0;
				mode = Mode.RECEIVING_FILE;
				logger.info("Receiving file " + file.getName());
				sendChannelCommand(COMMAND_START_SENDING, c[0] + "#" + c[1]);

			} else if(commandNumber==COMMAND_TRANSFER_CHECK) {
				if(actualReceivedBytes==expectedReceivedBytes) {
					//TEST CRC
					targetStream.flush();
					long crc = Long.parseLong(commandText);
					if(crc==targetStream.getChecksum().getValue()) {
						sendChannelCommand(STATUS_TRANSFER_SUCCESSFULL, "");
						logger.info("File transfer was successfull");
						finishTransfer();
						if(listener!=null) {
							listener.onTransferCheckedAndComplete();
						}
					} else {
						failTransfer("Sent/received file checksums don't match. expected=" + crc + "; actual=" + targetStream.getChecksum().getValue());
					}
					closeChannel();//only receiver will close channel to avoid loop
				} else {
					failTransfer("Expected received file length doesn't match. expected=" + expectedReceivedBytes + "; received=" + actualReceivedBytes);
				}

			} else if(commandNumber==COMMAND_TRANSFER_CANCEL) {
				if(commandText.length()==0) {//send cancel only by one side to avoid loop
					sendChannelCommand(COMMAND_TRANSFER_CANCEL, "1");
				}
				failTransfer("Transfer cancelled");
			} else if(commandNumber==STATUS_TRANSFER_SUCCESSFULL) {
				finishTransfer();
				closeChannel();
				if(listener!=null) {
					listener.onTransferCheckedAndComplete();
				}
				logger.info("File transfer was successfull");
			} else if(commandNumber==STATUS_TRANSFER_MESSAGE) {
				if(listener!=null) {
					listener.onTransferMessage(commandText);
				}
				logger.info(commandText);
			} else if(commandNumber==STATUS_RECEIVE_PROGRESS) {
				actualReceivedBytes = Long.parseLong(commandText);
				if(listener!=null) {
					listener.onDataTransfer(getPercentReceived());
				}
				verifyReceivedBytesWindow();
			} else if(commandNumber==COMMAND_EXPECTED_RECEIVED_BYTES) {
				expectedReceivedBytes = Long.parseLong(commandText);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.throwing(null, null, e);
			e.printStackTrace();
		}
	}
	
	private void failTransfer(String msg) {
		try {
			logger.info(msg);
			sendChannelCommand(STATUS_TRANSFER_MESSAGE, msg);
			finishTransfer();
			closeChannel();
			if(listener!=null) {
				listener.onTransferFailed(msg);
			}
		} catch (Exception e) {
			logger.throwing(null, null, e);
			e.printStackTrace();
		}
	}

	public void setFileTransferOperatorListener(FileTransferOperatorListener listener) {
		this.listener = listener;
	}

	public void cancelCurrentTransfer() {
		sendChannelCommand(COMMAND_TRANSFER_CANCEL, "");
	}
	
	public void sendFile(File file, int cucsId, int vehicleId) throws IOException {
		if(!isChannelOpen()) {
			openChannel(cucsId, vehicleId);
		} else {
			reopenChannel();
		}
		this.file = file;
		try {
			sendChannelCommand(COMMAND_START_RECEIVING, file.getParentFile().toString() + "#" + file.getName());
//			sendChannelCommand(COMMAND_START_RECEIVING, "(BASE)#" + file.getName());
		} catch (Exception e) {
			closeChannel();
		}
	}
	
	public void requestFile(String fileName, int cucsId, int vehicleId) throws IOException {
		if(!isChannelOpen()) {
			openChannel(cucsId, vehicleId);
		} else {
			reopenChannel();
		}
		try {
			onCommandReceived(COMMAND_START_RECEIVING, "(BASE)#" + fileName);
		} catch (Exception e) {
			closeChannel();
		}
	}
	
	@Override
	protected void openStreamsFromTarget() throws IOException {
		mode = Mode.IDLE;
		pis1 = new PipedInputStream2();
		pis2 = new PipedInputStream2();
		pos1 = new PipedOutputStream(pis1);
		pos2 = new PipedOutputStream(pis2);
	}

	@Override
	protected void closeStreamsFromTarget() throws IOException {
		mode = Mode.IDLE;
		pos1.close();
		pos2.close();
		pis1.close();
		pis2.close();
	}

	@Override
	protected InputStream getInputStreamFromTarget() throws IOException {
		return pis1;
	}

	@Override
	protected OutputStream getOutputStreamFromTarget() throws IOException {
		return pos2;
	}
	
	public Mode getMode() {
		return mode;
	}

	public int getPercentReceived() {
		return (int)(((float)actualReceivedBytes/(float)expectedReceivedBytes)*100);
	}
	
}
