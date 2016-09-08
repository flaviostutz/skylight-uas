package br.skylight.commons.plugins.datarecorder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.io.dataterminal.DataPacketListener;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.services.StorageService;

@ServiceDefinition
@ServiceImplementation(serviceDefinition=DataRecorderService.class)
public class DataRecorderService extends Worker implements LoggerDataPacketListener {

	private static final Logger logger = Logger.getLogger(DataRecorderService.class.getName());
	protected static final byte[] PACKET_END_BYTES = new byte[] {(byte)0, (byte)200, (byte)100};
	
	@ServiceInjection
	public MessagingService messagingService;
	
	@ServiceInjection
	public StorageService storageService;

	private boolean recording;
	
	private DataOutputStream outRecorderStream;
	private DataOutputStream inRecorderStream;
	
	@Override
	public void onActivate() throws Exception {
		startRecording();
	}
	
	@Override
	public void onDeactivate() throws Exception {
		stopRecording();
	}

	
	public void startRecording() {
		if(!recording) {
			try {
				//prepare files to be used for logging
				File inLogFile = storageService.resolveTimestampFile("datalogger", ".in");
				inRecorderStream = new DataOutputStream(new FileOutputStream(inLogFile));
				
				File outLogFile = storageService.resolveTimestampFile("datalogger", ".out");
				outRecorderStream = new DataOutputStream(new FileOutputStream(outLogFile));
				
				//set itself as packet listener on current data terminal
				messagingService.getDataTerminal().setLoggerPacketListener(this);
				recording = true;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void stopRecording() {
		if(recording) {
			messagingService.getDataTerminal().setLoggerPacketListener(null);
			recording = false;
			try {
				inRecorderStream.close();
				inRecorderStream = null; 
				outRecorderStream.close();
				outRecorderStream = null;
			} catch (IOException e) {
				logger.throwing(null,null,e);
			}
		}
	}

	@Override
	public void onPacketSent(byte[] data, int len) {
		try {
			//record packet received from data terminal
			outRecorderStream.write(data, 0, len);
			outRecorderStream.write(PACKET_END_BYTES);
		} catch (IOException e) {
			e.printStackTrace();
			logger.throwing(null,null,e);
		}
	}

	@Override
	public void onPacketReceived(byte[] data, int len, double timestamp) throws IOException {
		try {
			//record packet received from data terminal
			inRecorderStream.writeDouble(timestamp);
			inRecorderStream.write(data, 0, len);
			inRecorderStream.write(PACKET_END_BYTES);
		} catch (IOException e) {
			e.printStackTrace();
			logger.throwing(null,null,e);
		}
	}

	public static void readNextPacket(DataInputStream is, DataPacketListener dataPacketListener) throws IOException {
		int nextMarkIndex = 0;
		int i = 0;
		byte[] packet = new byte[1024];
		double timestamp = -1;
		
		//or a packet is found or an EOF will occur at the end of the stream
		while(true) {
			if(i==0) {
				timestamp = is.readDouble();
			}
			//read packet
			byte b = (byte)is.read();
			packet[i++] = b;
			
			//verify if there was found a packet begin byte
			if(b==PACKET_END_BYTES[nextMarkIndex]) {
				nextMarkIndex++;
				//whole packet mark found
				if(PACKET_END_BYTES.length==nextMarkIndex) {
					nextMarkIndex = 0;
					//notify last packet read
					if(i>PACKET_END_BYTES.length) {
						dataPacketListener.onPacketReceived(packet, i-PACKET_END_BYTES.length, timestamp);
						return;
					}
					i = 0;
				}
			} else {
				nextMarkIndex = 0;
			}

		}
	}
	
	public static void readPackets(DataInputStream is, DataPacketListener dataPacketListener) throws IOException {
		while(is.available()>0) {
			readNextPacket(is, dataPacketListener);
		}
	}
	
	public boolean isRecording() {
		return recording;
	}
	
}
