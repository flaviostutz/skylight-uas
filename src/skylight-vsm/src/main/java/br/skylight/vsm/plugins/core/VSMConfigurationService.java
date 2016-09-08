package br.skylight.vsm.plugins.core;

import java.io.File;
import java.io.FileInputStream;
import java.net.NetworkInterface;
import java.util.Properties;

import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.RankedNetworkInterface;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.io.dataterminal.DataTerminal;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.services.StorageService;

@ServiceDefinition
@ServiceImplementation(serviceDefinition=VSMConfigurationService.class)
public class VSMConfigurationService extends Worker {

	private int vsmId = IOHelper.parseUnsignedHex("11111111");
	private int multicastSendUdpPort;// = DataTerminal.DEFAULT_MULTICAST_CUCS_VSM_PORT_RECEIVE;
	private int multicastReceiveUdpPort;// = DataTerminal.DEFAULT_MULTICAST_CUCS_VSM_PORT_SEND;
	private String multicastUdpAddress;// = DataTerminal.DEFAULT_MULTICAST_ADDRESS;
	private NetworkInterface multicastNetworkInterface;// = DataTerminal.DEFAULT_MULTICAST_NETWORK_INTERFACE;
	private boolean assertGrantsForRelayingMessages = true;
	
	private Properties configProperties;

	@ServiceInjection
	public StorageService storageService;
	
	@Override
	public void onActivate() throws Exception {
		//load config properties file
		configProperties = new Properties();
		File f = storageService.resolveFile("vsm-config.properties");
		if(f.exists()) {
			FileInputStream fis = new FileInputStream(f);
			configProperties.load(fis);
			fis.close();
		}

		multicastSendUdpPort = Integer.parseInt(configProperties.getProperty("network-multicast-send-port", ""+DataTerminal.DEFAULT_MULTICAST_VSM_TO_CUCS_PORT));
		multicastReceiveUdpPort = Integer.parseInt(configProperties.getProperty("network-multicast-receive-port", ""+DataTerminal.DEFAULT_MULTICAST_CUCS_TO_VSM_PORT));
		multicastUdpAddress = configProperties.getProperty("network-multicast-address", DataTerminal.DEFAULT_MULTICAST_ADDRESS);

		//match network interface for CUCS communications
		multicastNetworkInterface = DataTerminal.DEFAULT_MULTICAST_NETWORK_INTERFACE;
		String ni = configProperties.getProperty("network-interface-name");
		if(ni!=null) {
			for (RankedNetworkInterface n : IOHelper.getRankedNetworkInterfaces()) {
				if(n.getNetworkInterface().getDisplayName().toLowerCase().contains(ni.toLowerCase())) {
					multicastNetworkInterface = n.getNetworkInterface();
					break;
				}
			}
		}
	}
	
	public int getVsmId() {
		return vsmId;
	}

	public int getMulticastSendUdpPort() {
		return multicastSendUdpPort;
	}

	public int getMulticastReceiveUdpPort() {
		return multicastReceiveUdpPort;
	}

	public String getMulticastUdpAddress() {
		return multicastUdpAddress;
	}

	public NetworkInterface getMulticastNetworkInterface() {
		return multicastNetworkInterface;
	}

	public boolean isAssertGrantsForRelayingMessages() {
		return assertGrantsForRelayingMessages;
	}

	public Properties getConfigProperties() {
		return configProperties;
	}
	
}
