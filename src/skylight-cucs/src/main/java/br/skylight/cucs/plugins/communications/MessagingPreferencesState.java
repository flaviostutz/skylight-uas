package br.skylight.cucs.plugins.communications;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.logging.Logger;

import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.SerializableState;
import br.skylight.commons.io.dataterminal.DataTerminal;
import br.skylight.commons.services.StorageService;

public class MessagingPreferencesState implements SerializableState {

	private static final Logger logger = Logger.getLogger(MessagingPreferencesState.class.getName());
	
	private NetworkInterface multicastNetworkInterface = DataTerminal.DEFAULT_MULTICAST_NETWORK_INTERFACE;
	private String multicastUdpAddress = DataTerminal.DEFAULT_MULTICAST_ADDRESS;
	private int multicastUdpSendPort = DataTerminal.DEFAULT_MULTICAST_CUCS_TO_VSM_PORT;
	private int multicastUdpReceivePort = DataTerminal.DEFAULT_MULTICAST_VSM_TO_CUCS_PORT;
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		multicastUdpAddress = in.readUTF();
		multicastUdpSendPort = in.readInt();
		multicastUdpReceivePort = in.readInt();

		//get network interface
		String networkHardwareName = in.readUTF();
		multicastNetworkInterface = IOHelper.getNetworkInterfaceByName(networkHardwareName);
		if(multicastNetworkInterface==null) {
			//if network interface address is not found, keep a random interface
			multicastNetworkInterface = IOHelper.getDefaultNetworkInterface();
		}
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		out.writeUTF(multicastUdpAddress);
		out.writeInt(multicastUdpSendPort);
		out.writeInt(multicastUdpReceivePort);
		out.writeUTF(multicastNetworkInterface.getName());
	}

	public NetworkInterface getMulticastNetworkInterface() {
		return multicastNetworkInterface;
	}

	public void setMulticastNetworkInterface(NetworkInterface multicastNetworkInterface) {
		this.multicastNetworkInterface = multicastNetworkInterface;
	}

	public String getMulticastUdpAddress() {
		return multicastUdpAddress;
	}

	public void setMulticastUdpAddress(String multicastUdpAddress) {
		this.multicastUdpAddress = multicastUdpAddress;
	}

	public int getMulticastUdpSendPort() {
		return multicastUdpSendPort;
	}

	public void setMulticastUdpSendPort(int multicastUdpSendPort) {
		this.multicastUdpSendPort = multicastUdpSendPort;
	}

	public int getMulticastUdpReceivePort() {
		return multicastUdpReceivePort;
	}

	public void setMulticastUdpReceivePort(int multicastUdpReceivePort) {
		this.multicastUdpReceivePort = multicastUdpReceivePort;
	}

	public static MessagingPreferencesState load(StorageService storageService) {
		boolean networkInterfaceOk = false;
		try {
			MessagingPreferencesState p = storageService.loadState("preferences", "preferences-messaging.dat", MessagingPreferencesState.class);
			if(p!=null) {
				if(p.getMulticastNetworkInterface().getInetAddresses().hasMoreElements()
				   && p.getMulticastNetworkInterface().supportsMulticast()) {
					networkInterfaceOk = true;
				}
				if(!networkInterfaceOk) {
					p.setMulticastNetworkInterface(IOHelper.getDefaultNetworkInterface());
					p.save(storageService);
				}
				return p;
			}
		} catch (Exception e) {
			logger.throwing(null,null,e);
			e.printStackTrace();
		}
		return new MessagingPreferencesState();
	}

	public void save(StorageService storageService) throws IOException {
		storageService.saveState(this, "preferences", "preferences-messaging.dat");
	}

}