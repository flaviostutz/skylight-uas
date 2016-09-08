package br.skylight.vsm.plugins.xplanevehicle;

import java.util.logging.Logger;

import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.io.dataterminal.DataTerminal;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.watchdog.WatchDog;
import br.skylight.simulation.payload.SimulatedEOIRPayloadOperator;
import br.skylight.simulation.xplane.XPlaneMessagingService;
import br.skylight.simulation.xplane.XPlaneUAV;
import br.skylight.simulation.xplane.XPlaneVehicleIdService;
import br.skylight.uav.infra.UAVHelper;
import br.skylight.uav.plugins.onboardpayloads.GenericDispenserPayloadOperator;
import br.skylight.vsm.VSMVehicle;
import br.skylight.vsm.plugins.core.VSMConfigurationService;

public class XPlaneVehicle extends VSMVehicle {

	private static final Logger logger = Logger.getLogger(XPlaneVehicle.class.getName());
	
	private WatchDog watchDog;
	
	@ServiceInjection
	public VSMConfigurationService vsmConfigurationService;

	public XPlaneVehicle() {
		watchDog = new WatchDog();
	}
	
	@Override
	public void onConnect() {
		try {
			XPlaneUAV.startup();
//			watchDog.startup(XPlaneWatchDogKickerService.IDENTIFICATION, XPlaneUAV.class, 2000, 10000, 15000, 15000, "-XX:+UseConcMarkSweepGC", "-XX:+CMSIncrementalMode");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisconnect() {
		try {
			watchDog.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public MessagingService createMessagingService() {
		MessagingService ms = new MessagingService();
		ms.setName("VSM<->UAV");
		return ms;
	}

	@Override
	public DataTerminal createDataTerminal() {
		return UAVHelper.loadDataTerminal(vsmConfigurationService.getConfigProperties(), "XPlaneVSM", DataTerminalType.GDT, XPlaneMessagingService.DATA_LINK_ID, XPlaneMessagingService.RECEIVE_PORT, XPlaneMessagingService.SEND_PORT);
	}

	@Override
	public Vehicle createVehicle() {
		Vehicle v = new Vehicle();
		v.setAuthorizeAnyCUCS(true);
		v.setVehicleID(XPlaneVehicleIdService.VEHICLE_ID);

		//CAMERA PAYLOAD
		Payload p1 = new Payload();
		p1.setAuthorizeAnyCUCS(true);
		p1.setAuthorizeOverrideAnyCUCS(true);
		p1.setUniqueStationNumber(SimulatedEOIRPayloadOperator.PAYLOAD.getUniqueStationNumber());
		p1.setPayloadType(SimulatedEOIRPayloadOperator.PAYLOAD.getPayloadType());
		p1.setStationDoor(SimulatedEOIRPayloadOperator.PAYLOAD.getStationDoor());
		p1.setNumberOfPayloadRecordingDevices(SimulatedEOIRPayloadOperator.PAYLOAD.getNumberOfPayloadRecordingDevices());
		p1.setEoIrPayload(SimulatedEOIRPayloadOperator.EOIR_PAYLOAD);
		p1.setVehicleID(XPlaneVehicleIdService.VEHICLE_ID);
		v.getPayloads().put(p1.getUniqueStationNumber(), p1);

		//DISPENSER PAYLOAD
		Payload p2 = new Payload();
		p2.setAuthorizeAnyCUCS(true);
		p2.setAuthorizeOverrideAnyCUCS(true);
		p2.setUniqueStationNumber(GenericDispenserPayloadOperator.PAYLOAD.getUniqueStationNumber());
		p2.setPayloadType(GenericDispenserPayloadOperator.PAYLOAD.getPayloadType());
		p2.setStationDoor(GenericDispenserPayloadOperator.PAYLOAD.getStationDoor());
		p2.setNumberOfPayloadRecordingDevices(GenericDispenserPayloadOperator.PAYLOAD.getNumberOfPayloadRecordingDevices());
		p2.setVehicleID(XPlaneVehicleIdService.VEHICLE_ID);
		v.getPayloads().put(p2.getUniqueStationNumber(), p2);

		return v;
	}

	@Override
	public boolean isSupportsDifferentialGPSSignal() {
		return false;
	}

}
