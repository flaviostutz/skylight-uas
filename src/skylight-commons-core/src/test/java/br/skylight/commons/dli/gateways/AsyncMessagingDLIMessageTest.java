package br.skylight.commons.dli.gateways;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import br.skylight.commons.dli.enums.AltitudeCommandType;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.HeadingCommandType;
import br.skylight.commons.dli.enums.MissionPlanMode;
import br.skylight.commons.dli.enums.SpeedType;
import br.skylight.commons.dli.messagetypes.MessageAcknowledgeConfiguration;
import br.skylight.commons.dli.messagetypes.MessageAcknowledgement;
import br.skylight.commons.dli.mission.MissionUploadCommand;
import br.skylight.commons.dli.services.AsyncSenderMessagingService;
import br.skylight.commons.dli.services.MessageInstancesRepository;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.vehicle.VehicleSteeringCommand;

@Test(groups="dlimessages")
/**
 * White box tests on messaging gateways
 */
public class AsyncMessagingDLIMessageTest {

	private DummyDataTerminal dt;
	
	@BeforeTest
	public void init() throws Exception {
		dt = new DummyDataTerminal();
	}
	
	public void testSerializeDeserialize() throws Exception {
		DummyAsyncMessagingService g = new DummyAsyncMessagingService();
		g.bindTo(dt);
		g.activate();
		
		//validate read/write serialization
		for(int i=0; i<3; i++) {
			MissionUploadCommand mc = g.resolveMessageForSending(MissionUploadCommand.class);
			mc.setMissionID("OUTBACK-"+i);
			mc.setCucsID(234);
			mc.setVehicleID(890);
			mc.setWaypointNumber(3);
			mc.setMissionPlanMode(MissionPlanMode.LOAD_MISSION);
			g.sendMessage(mc);
			
			//wait for async send to be done
			Thread.sleep(100);

			//loopback
//			DummyDataTerminal dt = (DummyDataTerminal)Services.get(DataTerminal.class);
			dt.setReadBuffer(dt.getSendBuffer());
			dt.setReadBufferLen(dt.getSendBufferLen());
			
			//wait for async read to be done
			Thread.sleep(200);
			
			MissionUploadCommand muc = (MissionUploadCommand)g.getLastReceivedMessage();
			assertTrue(muc!=mc);
			assertEquals(muc.getMissionID(), "OUTBACK-"+i);
			assertEquals(muc.getMissionPlanMode(), mc.getMissionPlanMode());
			assertEquals(muc.getCucsID(), 234);
			assertFalse(muc.getTimeStamp()==123);
			assertEquals(muc.getVehicleID(), 890);
			assertEquals(muc.getWaypointNumber(), 3);
			Thread.sleep(100);
		}
		g.deactivate();
//		DummyDataTerminal dt = (DummyDataTerminal)Services.get(DataTerminal.class);
		dt.deactivate();
	}
	
	public void testQueueSameTypeDifferentKeys() throws Exception {
		DummyAsyncMessagingService g = new DummyAsyncMessagingService();
		g.bindTo(dt);
		
		MessageAcknowledgeConfiguration mac1 = g.resolveMessageForSending(MessageAcknowledgeConfiguration.class);
		mac1.setAcknowledgeMessageType(MessageType.M100);
		g.sendMessage(mac1);
		
		MessageAcknowledgeConfiguration mac2 = g.resolveMessageForSending(MessageAcknowledgeConfiguration.class);
		mac2.setAcknowledgeMessageType(MessageType.M101);
		g.sendMessage(mac2);
		
		MessageAcknowledgeConfiguration mac3 = g.resolveMessageForSending(MessageAcknowledgeConfiguration.class);
		mac3.setAcknowledgeMessageType(MessageType.M102);
		g.sendMessage(mac3);
		
		Thread.sleep(100);
		assertEquals(dt.getSentMessagesCount(),0);
		
		dt.activate();
		g.activate();
		Thread.sleep(100);
		assertEquals(dt.getSentMessagesCount(),3);
	}

	public void testQueueSameTypeSameKeys() throws Exception {
		DummyAsyncMessagingService g = new DummyAsyncMessagingService();
		g.bindTo(dt);
		g.activate();

		//TWO WITH SAME TYPE AND SAME KEY
		MessageAcknowledgeConfiguration mac1 = g.resolveMessageForSending(MessageAcknowledgeConfiguration.class);
		mac1.setAcknowledgeMessageType(MessageType.M100);
		g.sendMessage(mac1);
		MessageAcknowledgeConfiguration mac2 = g.resolveMessageForSending(MessageAcknowledgeConfiguration.class);
		mac2.setAcknowledgeMessageType(MessageType.M100);
		g.sendMessage(mac2);

		//ANOTHER WITH SAME TYPE BUT DIFFERENT KEY
		MessageAcknowledgeConfiguration mac3 = g.resolveMessageForSending(MessageAcknowledgeConfiguration.class);
		mac3.setAcknowledgeMessageType(MessageType.M101);
		g.sendMessage(mac3);
		
		Thread.sleep(100);
//		DummyDataTerminal dt = (DummyDataTerminal)Services.get(DataTerminal.class);
		assertEquals(dt.getSentMessagesCount(),2);
		assertEquals(g.getMessageInstancesRepository().getMessageForSendingPool().getNumIdle(MessageAcknowledgeConfiguration.class),2);
	}

	public void testQueueDifferentTypes() throws Exception {
		DummyAsyncMessagingService g = new DummyAsyncMessagingService();
		g.bindTo(dt);

		//TWO WITH SAME TYPE AND SAME KEY
		VehicleSteeringCommand mac1 = g.resolveMessageForSending(VehicleSteeringCommand.class);
		mac1.setAltimeterSetting(43.55F);//not a key
		mac1.setAltitudeCommandType(AltitudeCommandType.ALTITUDE);
		mac1.setAltitudeType(AltitudeType.AGL);
		mac1.setHeadingCommandType(HeadingCommandType.COURSE);
		mac1.setSpeedType(SpeedType.GROUND_SPEED);
		g.sendMessage(mac1);
		VehicleSteeringCommand mac2 = g.resolveMessageForSending(VehicleSteeringCommand.class);
		mac2.setAltimeterSetting(5543.34F);//not a key
		mac2.setAltitudeCommandType(AltitudeCommandType.ALTITUDE);
		mac2.setAltitudeType(AltitudeType.AGL);
		mac2.setHeadingCommandType(HeadingCommandType.COURSE);
		mac2.setSpeedType(SpeedType.GROUND_SPEED);
		g.sendMessage(mac2);

		//TWO WITH SAME TYPE AND DIFFERENT KEYS
		MessageAcknowledgement mac3 = g.resolveMessageForSending(MessageAcknowledgement.class);
		mac3.setDataLinkID(54);
		g.sendMessage(mac3);
		MessageAcknowledgement mac4 = g.resolveMessageForSending(MessageAcknowledgement.class);
		mac4.setDataLinkID(5);
		g.sendMessage(mac4);
		
//		DummyDataTerminal dt = (DummyDataTerminal)Services.get(DataTerminal.class);
		dt.activate();
		g.activate();
		Thread.sleep(1000);
		assertEquals(dt.getSentMessagesCount(),3);
		assertEquals(g.getMessageInstancesRepository().getMessageForSendingPool().getNumIdle(VehicleSteeringCommand.class),1);
		assertEquals(g.getMessageInstancesRepository().getMessageForSendingPool().getNumIdle(MessageAcknowledgement.class),2);
	}

	public void testMemoryLeakProtection() throws Exception {
		DummyAsyncMessagingService g = new DummyAsyncMessagingService();
		g.bindTo(dt);
		
		//try to send messages when not sending messages (queue will be full)
		for(int i=0; i<AsyncSenderMessagingService.MAX_SEND_QUEUE_SIZE*10; i++) {
			MessageAcknowledgement m = g.resolveMessageForSending(MessageAcknowledgement.class);
			m.setDataLinkID(i);
			g.sendMessage(m);
		}
//		DummyDataTerminal dt = (DummyDataTerminal)Services.get(DataTerminal.class);
		assertEquals(dt.getSentMessagesCount(),0);
		assertEquals(g.getInternalSendQueueSize(), AsyncSenderMessagingService.MAX_SEND_QUEUE_SIZE);
		
		//start to send messages
		dt.activate();
		g.activate();
		Thread.sleep(1000);
		assertEquals(dt.getSentMessagesCount(),AsyncSenderMessagingService.MAX_SEND_QUEUE_SIZE);
		
		//send a lot of messages and verify internal cache size
		for (MessageType mt : MessageType.values()) {
			MessageAcknowledgeConfiguration m = g.resolveMessageForSending(MessageAcknowledgeConfiguration.class);
			m.setAcknowledgeMessageType(mt);
			g.sendMessage(m);
			Thread.sleep(1);
		}
		Thread.sleep(300);
		assertEquals(dt.getSentMessagesCount(),AsyncSenderMessagingService.MAX_SEND_QUEUE_SIZE + 1000);
		assertTrue(g.getMessageInstancesRepository().getMessageForSendingPool().getNumActive(MessageAcknowledgeConfiguration.class)<=MessageInstancesRepository.MAX_INSTANCES_FOR_EACH_TYPE);
	}
	
}
