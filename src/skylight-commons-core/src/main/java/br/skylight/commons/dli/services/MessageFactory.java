package br.skylight.commons.dli.services;

import java.util.HashMap;
import java.util.Map;

public class MessageFactory {

	private static Map<Long, Class<? extends Message>> classes = new HashMap<Long, Class<? extends Message>>();
	static {
		// prepare factories
		for (MessageType mt : MessageType.values()) {
			classes.put(mt.getNumber(), mt.getImplementation());
		}
	}

	public static Message newInstance(long messageType) {
		Class<? extends Message> clazz = classes.get(messageType);
		if (clazz == null) {
			throw new RuntimeException("Unrecognized message type requested. type=" + messageType);
		} else {
			try {
				return clazz.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
}
