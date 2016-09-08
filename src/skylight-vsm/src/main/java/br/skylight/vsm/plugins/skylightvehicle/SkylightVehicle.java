package br.skylight.vsm.plugins.skylightvehicle;

import java.util.logging.Logger;


import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.io.dataterminal.DataTerminal;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.watchdog.WatchDog;
import br.skylight.uav.infra.UAVHelper;
import br.skylight.uav.plugins.messaging.OnboardMessagingService;
import br.skylight.uav.plugins.onboardintegration.OnboardVehicleIdService;
import br.skylight.uav.plugins.onboardpayloads.GenericDispenserPayloadOperator;
import br.skylight.uav.plugins.onboardpayloads.ViscaEOIRPayloadOperator;
import br.skylight.vsm.VSMVehicle;
import br.skylight.vsm.plugins.core.VSMConfigurationService;

public class SkylightVehicle extends VSMVehicle {

	private static final Logger logger = Logger.getLogger(SkylightVehicle.class.getName());
	private WatchDog watchDog;

	@ServiceInjection
	public VSMConfigurationService vsmConfigurationService;

	public SkylightVehicle() {
		watchDog = new WatchDog();
	}

	@Override
	public void onConnect() {
		logger.info("VSM is connected to Skylight Vehicle");
		try {
//			SkylightUAV.startup();
			// watchDog.startup(FlightGearWatchDogKickerService.IDENTIFICATION,
			// FlightGearUAV.class, 2000, 10000, 15000, 15000,
			// "-XX:+UseConcMarkSweepGC", "-XX:+CMSIncrementalMode");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisconnect() {
		logger.info("VSM is disconnected from Skylight Vehicle");
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
		return UAVHelper.loadDataTerminal(vsmConfigurationService.getConfigProperties(), "SkylightVSM", DataTerminalType.GDT, OnboardMessagingService.DATA_LINK_ID, OnboardMessagingService.RECEIVE_PORT, OnboardMessagingService.SEND_PORT);
	}

	@Override
	public Vehicle createVehicle() {
		Vehicle v = new Vehicle();
		v.setAuthorizeAnyCUCS(true);
		v.setVehicleID(OnboardVehicleIdService.VEHICLE_ID);

		// CAMERA PAYLOAD
		Payload p1 = new Payload();
		p1.setAuthorizeAnyCUCS(true);
		p1.setAuthorizeOverrideAnyCUCS(true);
		p1.setUniqueStationNumber(ViscaEOIRPayloadOperator.PAYLOAD.getUniqueStationNumber());
		p1.setPayloadType(ViscaEOIRPayloadOperator.PAYLOAD.getPayloadType());
		p1.setStationDoor(ViscaEOIRPayloadOperator.PAYLOAD.getStationDoor());
		p1.setNumberOfPayloadRecordingDevices(ViscaEOIRPayloadOperator.PAYLOAD.getNumberOfPayloadRecordingDevices());
		p1.setEoIrPayload(ViscaEOIRPayloadOperator.EOIR_PAYLOAD);
		p1.setVehicleID(OnboardVehicleIdService.VEHICLE_ID);
		v.getPayloads().put(p1.getUniqueStationNumber(), p1);

		// DISPENSER PAYLOAD
		Payload p2 = new Payload();
		p2.setAuthorizeAnyCUCS(true);
		p2.setAuthorizeOverrideAnyCUCS(true);
		p2.setUniqueStationNumber(GenericDispenserPayloadOperator.PAYLOAD.getUniqueStationNumber());
		p2.setPayloadType(GenericDispenserPayloadOperator.PAYLOAD.getPayloadType());
		p2.setStationDoor(GenericDispenserPayloadOperator.PAYLOAD.getStationDoor());
		p2.setNumberOfPayloadRecordingDevices(GenericDispenserPayloadOperator.PAYLOAD.getNumberOfPayloadRecordingDevices());
		p2.setVehicleID(OnboardVehicleIdService.VEHICLE_ID);
		v.getPayloads().put(p2.getUniqueStationNumber(), p2);

		return v;
	}

	@Override
	public boolean isSupportsDifferentialGPSSignal() {
		return false;
	}

}
