package br.skylight.commons.io.dataterminal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.logging.Logger;

import br.skylight.commons.dli.datalink.DataLinkControlCommand;
import br.skylight.commons.dli.datalink.DataLinkSetupMessage;
import br.skylight.commons.dli.enums.DataLinkState;
import br.skylight.commons.dli.enums.DataTerminalType;

/**
 * In some cases this data terminal won't work if multicast support is not 
 * explicitly enabled on network device.
 * In Windows 7 sometimes the interface only will work after disabling and enabling again the interface
 */
public class UDPMulticastDataTerminal extends DataTerminal {

	private static final Logger logger = Logger.getLogger(UDPMulticastDataTerminal.class.getName());
	
	private String multicastAddress;
	private NetworkInterface networkInterface;
	
	private int multicastSendPort;
	private MulticastSocket multicastSendSocket;
	private DatagramPacket outputPacket;

	private int multicastReceivePort;
	private MulticastSocket multicastReceiveSocket;
	private DatagramPacket inputPacket;
	
	private boolean txEnabled = true;
	private boolean rxEnabled = true;
	
	public UDPMulticastDataTerminal(NetworkInterface networkInterface, String multicastAddress, int multicastSendPort, int multicastReceivePort, DataTerminalType dataTerminalType, int dataLinkId) {
		super(dataTerminalType, dataLinkId);
		this.multicastSendPort = multicastSendPort;
		this.multicastReceivePort = multicastReceivePort;
		this.multicastAddress = multicastAddress;
		this.networkInterface = networkInterface;
		
		if(networkInterface!=null) {
			if(!networkInterface.getInetAddresses().hasMoreElements()) {
				throw new RuntimeException("Network interface '" + networkInterface.getDisplayName() + "' has no IP address bound to it");
			}
			try {
				if(!networkInterface.supportsMulticast()) {
					throw new RuntimeException("Network interface '" + networkInterface.getDisplayName() + "' doesn't support multicast packets");
				}
			} catch (SocketException e) {
				throw new RuntimeException("Could not test if network interface supports multicast", e);
			}
		}
	}

	@Override
	public void onActivate() throws Exception {
		multicastSendSocket = new MulticastSocket();
		
		if(networkInterface!=null) {
			logger.info("Activating UDP Data Terminal for interface '" + networkInterface.getDisplayName() + "'; sendPort="+ multicastSendPort +"; receivePort="+multicastReceivePort);
			multicastSendSocket.setNetworkInterface(networkInterface);
		} else {
			logger.info("Activating UDP Data Terminal. sendPort="+ multicastSendPort +"; receivePort="+multicastReceivePort);
		}

		InetAddress group = InetAddress.getByName(multicastAddress);
		multicastReceiveSocket = new MulticastSocket(multicastReceivePort);
		
		//THIS WILL FAIL IF OS IS NOT PREPARED FOR MULTICAST SUPPORT. In Windows, click on the network device and install "Reliable Multicast Protocol"
		//http://support.microsoft.com/kb/239924 can fix this too
		multicastReceiveSocket.joinGroup(group);

		outputPacket = new DatagramPacket(new byte[0], 0, group, multicastSendPort);
		outputPacket.setAddress(group);
		
		inputPacket = new DatagramPacket(new byte[0], 0);
		
		getDataLinkStatusReport().setDataLinkState(DataLinkState.TX_AND_RX);
	}

	@Override
	public void onDeactivate() throws Exception {
		super.onDeactivate();
		multicastSendSocket.close();
		multicastReceiveSocket.close();
	}

	@Override
	protected int readNextPacket(byte[] buffer) throws IOException {
		if(!rxEnabled) {
			//link is not supposed to receive anything
			return 0;
		}
		inputPacket.setData(buffer);
		multicastReceiveSocket.receive(inputPacket);
		return inputPacket.getLength();
	}

	@Override
	public void sendNextPacket(byte[] data, int len) throws IOException {
		if(!txEnabled) {
			//link is not supposed to send anything
			return;
		}
		outputPacket.setData(data, 0, len);
		multicastSendSocket.send(outputPacket);
	}

	@Override
	public int getDownlinkStatus() {
		return (short)(75 + 15*Math.random());
	}
	@Override
	public int getUplinkStatus() {
		return (short)(75 + 15*Math.random());
	}

	@Override
	public void controlDataLink(DataLinkControlCommand cm) {
		rxEnabled = !cm.getSetDataLinkState().equals(DataLinkState.OFF);
		txEnabled = cm.getSetDataLinkState().equals(DataLinkState.TX_AND_RX) || cm.getSetDataLinkState().equals(DataLinkState.TX_HIGH_POWER_AND_RX);
		populateStatusReportWithControlCommand(cm);
	}

	@Override
	public void setupDataLink(DataLinkSetupMessage sm) {
		populateStatusReportWithSetupMessage(sm);
	}
	
	@Override
	public String getInfo() {
		return "UDPMulticastDataTerminal " + super.getInfo();
	}
	
}
