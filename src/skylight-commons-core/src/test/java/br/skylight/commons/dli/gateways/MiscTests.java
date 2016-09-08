package br.skylight.commons.dli.gateways;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import br.skylight.commons.dli.configuration.FieldConfigurationIntegerResponse;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageFactory;

@Test
public class MiscTests {

	public void testMessageFactory() {
		Message m = MessageFactory.newInstance(1300);
		assertTrue(m instanceof FieldConfigurationIntegerResponse);
		assertEquals(m.getMessageType().getNumber(), 1300);
	}
	
}
