package br.skylight.uav.plugins.onboardpayloads;

import java.util.logging.Logger;

import br.skylight.commons.Payload;
import br.skylight.commons.dli.enums.DoorState;
import br.skylight.commons.dli.enums.PayloadType;
import br.skylight.commons.dli.enums.SensorMode;
import br.skylight.commons.dli.enums.StationDoor;
import br.skylight.commons.dli.mission.PayloadActionWaypoint;
import br.skylight.commons.dli.payload.PayloadBayCommand;
import br.skylight.commons.dli.payload.PayloadBayStatus;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.plugins.payload.PayloadOperatorExtensionPoint;
import br.skylight.uav.plugins.payload.PayloadService;
import br.skylight.uav.services.ActuatorsService;
import br.skylight.uav.services.InstrumentsService;

@ExtensionPointImplementation(extensionPointDefinition=PayloadOperatorExtensionPoint.class)
public class GenericDispenserPayloadOperator extends PayloadOperatorExtensionPoint {

	private static final Logger logger = Logger.getLogger(GenericDispenserPayloadOperator.class.getName());
	
	public static Payload PAYLOAD = new Payload();
	static {
		PAYLOAD.setPayloadType(PayloadType.DISPENSABLE_PAYLOAD);
		PAYLOAD.setAuthorizeAnyCUCS(true);
		PAYLOAD.setAuthorizeOverrideAnyCUCS(true);
		PAYLOAD.setUniqueStationNumber(2);
		PAYLOAD.setStationDoor(StationDoor.YES);
	}

	private boolean payloadActivated = false;
	
	@ServiceInjection
	public PayloadService payloadService;
	
	@ServiceInjection
	public InstrumentsService instrumentsService;
	
	@ServiceInjection
	public ActuatorsService actuatorsService;
	
	@MemberInjection
	public ViscaCameraGateway viscaCameraGateway;
	
	public GenericDispenserPayloadOperator() {
		super(PAYLOAD);
	}

	@Override
	public void onActivate() throws Exception {
		super.onActivate();
		deactivatePayload();
	}

	@Override
	public void onDeactivate() throws Exception {
		if(actuatorsService!=null) {
			actuatorsService.setVideoTransmitterPower(false);
		}
	}
	
	@Override
	public void onPayloadMessageReceived(Message message) {
		//M206
		if(message instanceof PayloadBayCommand) {
			PayloadBayCommand m = (PayloadBayCommand)message;
			if(m.getPayloadBayDoors().equals(DoorState.OPEN)) {
				activatePayload();
			} else {
				deactivatePayload();
			}
			
		//M804
		} else if(message instanceof PayloadActionWaypoint) {
			PayloadActionWaypoint m = (PayloadActionWaypoint)message;

			//payload system mode
			if(m.getSetSensor1Mode().equals(SensorMode.TURN_ON)) {
				activatePayload();
			} else if(m.getSetSensor1Mode().equals(SensorMode.TURN_OFF)) {
				deactivatePayload();
			}
		}
	}
	
	private void activatePayload() {
		payloadActivated = true;
		logger.info("Station #"+getPayload().getUniqueStationNumber()+": activated");
		//set generic servo to maximum position for 2 seconds
		logger.info("Station #"+getPayload().getUniqueStationNumber()+": commanding generic servo to 127 for 2 seconds");
		actuatorsService.setGenericServo(127);
		Thread t = new Thread() {
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				deactivatePayload();
			};
		};
		t.start();
	}

	private void deactivatePayload() {
		payloadActivated = false;
		logger.info("Station #"+getPayload().getUniqueStationNumber()+": deactivated");
		actuatorsService.setGenericServo(-127);
	}

	@Override
	public boolean prepareScheduledPayloadMessage(Message message) {
		if(super.prepareScheduledPayloadMessage(message)) {
			return true;
			
		//M308
		} else if(message instanceof PayloadBayStatus) {
			PayloadBayStatus m = (PayloadBayStatus)message;
			m.setPayloadBayDoorStatus(payloadActivated?DoorState.OPEN:DoorState.CLOSED);
			return true;
		}
		return false;
	}
	
	@Override
	public void step() throws Exception {
	}

}
