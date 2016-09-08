package br.skylight.simulation.dummy;

import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.dli.services.AsyncSenderMessagingService;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.io.dataterminal.DataTerminal;
import br.skylight.commons.io.dataterminal.UDPMulticastDataTerminal;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.services.VehicleIdService;

@ServiceImplementation(serviceDefinition=MessagingService.class)
public class DummyMessagingService extends AsyncSenderMessagingService {

	public static final int UAV_SEND_PORT = 1122;
	public static final int UAV_RECEIVE_PORT = 2233;

	@ServiceInjection
	public PluginManager pluginManager;
	
	@ServiceInjection
	public VehicleIdService vehicleIdService;
	
	public DummyMessagingService() {
		super.setName("UAV<->VSM");
	}

	@Override
	public void sendMessage(Message message) {
		message.setVehicleID(vehicleIdService.getInitialVehicleID().getVehicleID());
		if(message.getCucsID()==0) {
			message.setCucsID(Message.BROADCAST_ID);//vsm will send this message to all granted cucs
		}
//		if(Math.random()<0.05) {
//			throw new RuntimeException("FORCED ERROR!");
//		}
		super.sendMessage(message);
	}
	
	@Override
	public void onActivate() throws Exception {
		bindToUDPMulticastDataTerminalWithOptionalFallbackToUDPUnicastAndActivate(DataTerminal.DEFAULT_MULTICAST_NETWORK_INTERFACE, DataTerminal.DEFAULT_MULTICAST_ADDRESS, UAV_SEND_PORT, UAV_RECEIVE_PORT, DataTerminalType.ADT, vehicleIdService.getInitialVehicleID().getVehicleID(), System.getProperty("fallback.unicastudp.remotehost"));
//		DataTerminal dt = new UDPMulticastDataTerminal(DataTerminal.DEFAULT_MULTICAST_NETWORK_INTERFACE, DataTerminal.DEFAULT_MULTICAST_ADDRESS, UAV_SEND_PORT, UAV_RECEIVE_PORT, );
//		DataTerminal dt = new AC4790DataTerminal(new SerialConnectionParams("COM4"), DataTerminalType.ADT, DummyServices.VEHICLE_ID.getVehicleID(), false, false);
//		DataTerminal dt = new SerialDataTerminal(new SerialConnectionParams("COM2"), DataTerminalType.ADT, DummyServices.VEHICLE_ID.getVehicleID(), false, false);
//		bindTo(dt);
//		pluginManager.manageObject(dt);
		super.onActivate();
	}

	@Override
	public void onDeactivate() throws Exception {
		unbind();
		super.onDeactivate();
	}
	
}
