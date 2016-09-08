package br.skylight.simulation.flightgear;

import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.dli.services.AsyncSenderMessagingService;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.io.dataterminal.DataTerminal;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.infra.UAVHelper;
import br.skylight.uav.plugins.storage.RepositoryService;
import br.skylight.uav.services.VehicleIdService;

@ServiceImplementation(serviceDefinition=MessagingService.class)
public class FlightGearMessagingService extends AsyncSenderMessagingService {

	public static final int SEND_PORT = 3344;
	public static final int RECEIVE_PORT = 4433;
	public static final int DATA_LINK_ID = 3333;

	@ServiceInjection
	public VehicleIdService vehicleIdService;

	@ServiceInjection
	public RepositoryService repositoryService;

	@ServiceInjection
	public PluginManager pluginManager;

	public FlightGearMessagingService() {
		super.setName("UAV<->VSM");
	}
	
	@Override
	public void sendMessage(Message message) {
		message.setVehicleID(vehicleIdService.getInitialVehicleID().getVehicleID());
		if(message.getCucsID()==0) {
			message.setCucsID(Message.BROADCAST_ID);//vsm will send this message to all granted cucs
		}
		super.sendMessage(message);
	}
	
	@Override
	public void onActivate() throws Exception {
		DataTerminal dt = UAVHelper.loadDataTerminal(repositoryService.getConfigProperties(), "FlightGearUAV", DataTerminalType.ADT, DATA_LINK_ID, SEND_PORT, RECEIVE_PORT);
		bindTo(dt);
		pluginManager.manageObject(dt);
		super.onActivate();
	}

	@Override
	public void onDeactivate() throws Exception {
		unbind();
		super.onDeactivate();
	}
	
}
