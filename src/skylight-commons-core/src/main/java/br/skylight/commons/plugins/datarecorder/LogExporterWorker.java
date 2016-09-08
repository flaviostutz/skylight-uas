package br.skylight.commons.plugins.datarecorder;

import java.io.DataInputStream;
import java.io.IOException;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.io.dataterminal.DataPacketListener;

public class LogExporterWorker {

	private DataInputStream logStream;
	private Message lastMessage;
	MessagingService inMessaging;
	
	public LogExporterWorker(DataInputStream logStream) {
		this.logStream = logStream;
		this.inMessaging = new MessagingService();
		
		inMessaging.setMessageListener(new MessageListener() {
			@Override
			public void onMessageReceived(Message message) {
				lastMessage = message;
			}
		});
	}
	
	public Message fetchNextMessage() throws IOException {
		lastMessage = null;
		while(lastMessage==null && logStream.available()>0) {
			DataRecorderService.readNextPacket(logStream, new DataPacketListener() {
				@Override
				public void onPacketReceived(byte[] data, int len, double timestamp) throws IOException {
					inMessaging.onPacketReceived(data, len, timestamp);
				}
			});
		}
		return lastMessage;
	}
	
	public Message getLastMessage() {
		return lastMessage;
	}
}
