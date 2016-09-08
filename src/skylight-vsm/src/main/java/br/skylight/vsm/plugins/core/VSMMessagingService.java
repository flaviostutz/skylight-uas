package br.skylight.vsm.plugins.core;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.systemid.MessageWithVsmID;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;

@ServiceImplementation(serviceDefinition=MessagingService.class)
public class VSMMessagingService extends MessagingService {

	@ServiceInjection
	public VSMConfigurationService cs;
	
	@ServiceInjection
	public PluginManager pluginManager;
	
	public VSMMessagingService() {
		super.setName("VSM<->CUCS");
	}
	
	@Override
	public void sendMessage(Message message) {
		if(message instanceof MessageWithVsmID) {
			((MessageWithVsmID)message).setVsmID(cs.getVsmId());
			System.out.println("SENT MESSAGE TO CUCS");
		}
		super.sendMessage(message);
	}
	
	@Override
	public void onActivate() throws Exception {
		bindToUDPMulticastDataTerminalWithOptionalFallbackToUDPUnicastAndActivate(cs.getMulticastNetworkInterface(), cs.getMulticastUdpAddress(), cs.getMulticastSendUdpPort(), cs.getMulticastReceiveUdpPort(), null, 2, System.getProperty("fallback.unicastudp.remotehost"));
	}
	
	@Override
	public void onDeactivate() throws Exception {
		unbind();
	}

}
