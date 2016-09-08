package br.skylight.vsm.plugins.dummyvehicle;

import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.dli.enums.PayloadType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.io.dataterminal.DataTerminal;
import br.skylight.commons.io.dataterminal.UDPUnicastDataTerminal;
import br.skylight.commons.plugins.watchdog.WatchDog;
import br.skylight.simulation.dummy.DummyMessagingService;
import br.skylight.simulation.dummy.DummyUAV;
import br.skylight.simulation.dummy.DummyVehicleIdService;
import br.skylight.vsm.VSMVehicle;

public class DummyVehicle extends VSMVehicle {

	public static final int VEHICLE_SEND_PORT = 11122;
	public static final int VEHICLE_RECEIVE_PORT = 12233;
	
	private WatchDog watchDog;
	
	public DummyVehicle() {
		watchDog = new WatchDog();
	}

	@Override
	protected void onConnect() {
		try {
			DummyUAV.startup();
//			watchDog.startup(DummyWatchDogKickerService.IDENTIFICATION, DummyUAV.class, 2000, 10000, 15000, 15000, "-XX:+UseConcMarkSweepGC", "-XX:+CMSIncrementalMode");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDisconnect() {
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
		return new UDPUnicastDataTerminal(DataTerminal.DEFAULT_MULTICAST_ADDRESS, VEHICLE_RECEIVE_PORT, VEHICLE_SEND_PORT, DataTerminalType.GDT, DummyVehicleIdService.VEHICLE_ID.getVehicleID());
//		return new AC4790DataTerminal(new SerialConnectionParams("COM4"), DataTerminalType.GDT, DummyServices.VEHICLE_ID.getVehicleID(), false, false);
//		return new SerialDataTerminal(new SerialConnectionParams("COM1"), DataTerminalType.GDT, DummyServices.VEHICLE_ID.getVehicleID(), false, false);
	}

	@Override
	public Vehicle createVehicle() {
		Vehicle vehicle = new Vehicle();
		vehicle.setAuthorizeAnyCUCS(true);
		vehicle.setAuthorizeOverrideAnyCUCS(true);
		vehicle.setVehicleID(DummyVehicleIdService.VEHICLE_ID);

		Payload p = new Payload();
		p.setAuthorizeAnyCUCS(true);
		p.setAuthorizeOverrideAnyCUCS(true);
		p.setPayloadType(PayloadType.EO);
		p.setUniqueStationNumber(1);
		p.setVehicleID(DummyVehicleIdService.VEHICLE_ID);
		vehicle.getPayloads().put(1, p);
		
		return vehicle;
	}

}
