package br.skylight.commons;

import java.util.logging.Logger;

import br.skylight.commons.dli.annotations.MessageField;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class MessageFieldDef {

	private static final Logger logger = Logger.getLogger(MessageFieldDef.class.getName());
	
	private MessageType messageType;
	private int fieldNumber;
	private String label;
	private Message message;
	
	public MessageFieldDef(MessageType messageType, int fieldNumber) {
		this.messageType = messageType;
		this.fieldNumber = fieldNumber;
		try {
			//set field label
			Message m = messageType.getImplementation().newInstance();
			MessageField mf = m.getMessageField(fieldNumber);
			if(mf!=null) {
				label = m.getField(fieldNumber).getName();
			}
		} catch (Exception e) {
			logger.throwing(null,null,e);
		}
	}

	public void updateMessage(Message message) {
		if(!message.getMessageType().equals(messageType)) {
			throw new IllegalArgumentException("Message type must be " + messageType);
		}
		this.message = message;
	}
	
	public String getFormattedValue() {
		try {
			return message.getFormattedValue(fieldNumber);
		} catch (Exception e) {
			logger.throwing(null,null,e);
			return "ERROR";
		}
	}
	
	public double getValue() {
		try {
			return message.getValue(fieldNumber);
		} catch (Exception e) {
			logger.throwing(null,null,e);
			return Double.NaN;
		}
	}
	
	public MessageType getMessageType() {
		return messageType;
	}
	public int getFieldNumber() {
		return fieldNumber;
	}
	
	public String getLabel() {
		return label;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + fieldNumber;
		result = prime * result
				+ ((messageType == null) ? 0 : messageType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MessageFieldDef other = (MessageFieldDef) obj;
		if (fieldNumber != other.fieldNumber)
			return false;
		if (messageType == null) {
			if (other.messageType != null)
				return false;
		} else if (!messageType.equals(other.messageType))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return messageType + "["+ fieldNumber +"]";
	}
	
}
