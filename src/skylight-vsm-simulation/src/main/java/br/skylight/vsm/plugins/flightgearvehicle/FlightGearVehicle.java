package br.skylight.vsm.plugins.flightgearvehicle;

import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.io.dataterminal.DataTerminal;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.watchdog.WatchDog;
import br.skylight.simulation.flightgear.FlightGearMessagingService;
import br.skylight.simulation.flightgear.FlightGearUAV;
import br.skylight.simulation.flightgear.FlightGearVehicleIdService;
import br.skylight.simulation.payload.SimulatedEOIRPayloadOperator;
import br.skylight.uav.infra.UAVHelper;
import br.skylight.uav.plugins.onboardpayloads.GenericDispenserPayloadOperator;
import br.skylight.vsm.VSMVehicle;
import br.skylight.vsm.plugins.core.VSMConfigurationService;

public class FlightGearVehicle extends VSMVehicle {

	private WatchDog watchDog;
	
	@ServiceInjection
	public VSMConfigurationService vsmConfigurationService;

	public FlightGearVehicle() {
		watchDog = new WatchDog();
	}

	@Override
	public void onConnect() {
		try {
			FlightGearUAV.startup();
//			watchDog.startup(FlightGearWatchDogKickerService.IDENTIFICATION, FlightGearUAV.class, 2000, 10000, 15000, 15000, "-XX:+UseConcMarkSweepGC", "-XX:+CMSIncrementalMode");
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
	public boolean isSupportsDifferentialGPSSignal() {
		return false;
	}

	@Override
	public MessagingService createMessagingService() {
		MessagingService ms = new MessagingService();
		ms.setName("VSM<->UAV");
		return ms;
	}

	@Override
	public DataTerminal createDataTerminal() {
		return UAVHelper.loadDataTerminal(vsmConfigurationService.getConfigProperties(), "FlightGearVSM", DataTerminalType.GDT, FlightGearMessagingService.DATA_LINK_ID, FlightGearMessagingService.RECEIVE_PORT, FlightGearMessagingService.SEND_PORT);
	}

	@Override
	public Vehicle createVehicle() {
		Vehicle v = new Vehicle();
		v.setAuthorizeAnyCUCS(true);
		v.setVehicleID(FlightGearVehicleIdService.VEHICLE_ID);

		//CAMERA PAYLOAD
		Payload p = new Payload();
		p.setAuthorizeAnyCUCS(true);
		p.setAuthorizeOverrideAnyCUCS(true);
		p.setUniqueStationNumber(SimulatedEOIRPayloadOperator.PAYLOAD.getUniqueStationNumber());
		p.setPayloadType(SimulatedEOIRPayloadOperator.PAYLOAD.getPayloadType());
		p.setStationDoor(SimulatedEOIRPayloadOperator.PAYLOAD.getStationDoor());
		p.setNumberOfPayloadRecordingDevices(SimulatedEOIRPayloadOperator.PAYLOAD.getNumberOfPayloadRecordingDevices());
		p.setEoIrPayload(SimulatedEOIRPayloadOperator.EOIR_PAYLOAD);
		p.setVehicleID(FlightGearVehicleIdService.VEHICLE_ID);
		v.getPayloads().put(p.getUniqueStationNumber(), p);

		//DISPENSER PAYLOAD
		Payload p2 = new Payload();
		p2.setAuthorizeAnyCUCS(true);
		p2.setAuthorizeOverrideAnyCUCS(true);
		p2.setUniqueStationNumber(GenericDispenserPayloadOperator.PAYLOAD.getUniqueStationNumber());
		p2.setPayloadType(GenericDispenserPayloadOperator.PAYLOAD.getPayloadType());
		p2.setStationDoor(GenericDispenserPayloadOperator.PAYLOAD.getStationDoor());
		p2.setNumberOfPayloadRecordingDevices(GenericDispenserPayloadOperator.PAYLOAD.getNumberOfPayloadRecordingDevices());
		p2.setVehicleID(FlightGearVehicleIdService.VEHICLE_ID);
		v.getPayloads().put(p2.getUniqueStationNumber(), p2);
		
		return v;
	}

}
