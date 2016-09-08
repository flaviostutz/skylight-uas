package br.skylight.commons;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.SerializableState;

public class MessagingConfiguration implements SerializableState {

	//message type/frequency
	private CopyOnWriteArrayList<ScheduledMessage> scheduledMessages = new CopyOnWriteArrayList<ScheduledMessage>();
	
	//message type
	private ArrayList<Long> messagesForAcknowledgement = new ArrayList<Long>();

	@Override
	public void readState(DataInputStream in) throws IOException {
		IOHelper.readArrayList(in, ScheduledMessage.class, scheduledMessages);
		
		//read messages for acknowledgment
		messagesForAcknowledgement.clear();
		int size = in.readInt();
		for(int i=0; i<size; i++) {
			messagesForAcknowledgement.add(in.readLong());
		}
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		IOHelper.writeArrayList(out, scheduledMessages);
		
		//write messages for acknowledgment
		out.writeInt(messagesForAcknowledgement.size());
		for (Long l : messagesForAcknowledgement) {
			out.writeLong(l);
		}
	}
	
	public ArrayList<Long> getMessagesForAcknowledgement() {
		return messagesForAcknowledgement;
	}
	
	public CopyOnWriteArrayList<ScheduledMessage> getScheduledMessages() {
		return scheduledMessages;
	}

	public MessagingConfiguration createCopy() {
		try {
			MessagingConfiguration m = new MessagingConfiguration();
			IOHelper.copyState(m, this);
			return m;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
