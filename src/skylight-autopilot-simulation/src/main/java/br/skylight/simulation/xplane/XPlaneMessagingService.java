package br.skylight.simulation.xplane;


import java.util.logging.Logger;

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
public class XPlaneMessagingService extends AsyncSenderMessagingService {

	private static final Logger logger = Logger.getLogger(XPlaneMessagingService.class.getName());
	
	public static final int SEND_PORT = 2233;
	public static final int RECEIVE_PORT = 3322;
	public static final int DATA_LINK_ID = 2222;

	@ServiceInjection
	public PluginManager pluginManager;

	@ServiceInjection
	public VehicleIdService vehicleIdService;
	
	@ServiceInjection
	public RepositoryService repositoryService;
	
	public XPlaneMessagingService() {
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
		DataTerminal dt = UAVHelper.loadDataTerminal(repositoryService.getConfigProperties(), "XPlaneUAV", DataTerminalType.ADT, DATA_LINK_ID, SEND_PORT, RECEIVE_PORT);
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
