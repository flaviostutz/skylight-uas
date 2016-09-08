package br.skylight.cucs.plugins.core;

import java.util.logging.Logger;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.io.dataterminal.DataTerminal;
import br.skylight.commons.io.dataterminal.UDPMulticastDataTerminal;
import br.skylight.commons.io.dataterminal.UDPUnicastDataTerminal;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.services.StorageService;
import br.skylight.cucs.plugins.communications.MessagingPreferencesState;

@ServiceImplementation(serviceDefinition=MessagingService.class)
public class CUCSMessagingService extends MessagingService {

	@ServiceInjection
	public PluginManager pluginManager;
	@ServiceInjection
	public UserService userService;
	@ServiceInjection
	public StorageService storageService;

	private Logger logger = Logger.getLogger(CUCSMessagingService.class.getName());
	
	public CUCSMessagingService() {
		super.setName("CUCS<->VSM");
	}

	@Override
	public void onActivate() throws Exception {
		MessagingPreferencesState p = MessagingPreferencesState.load(storageService);
		bindToUDPMulticastDataTerminalWithOptionalFallbackToUDPUnicastAndActivate(p.getMulticastNetworkInterface(), p.getMulticastUdpAddress(), p.getMulticastUdpSendPort(), p.getMulticastUdpReceivePort(), null, 1, System.getProperty("fallback.unicastudp.remotehost"));
	}
	
	@Override
	public void onDeactivate() throws Exception {
		unbind();
	}
	
	@Override
	public void sendMessage(Message message) {
		message.setCucsID(userService.getCurrentCucsId());
		super.sendMessage(message);
	}
	
}
