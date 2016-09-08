package br.skylight.commons.io.dataterminal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

import br.skylight.commons.dli.datalink.DataLinkControlCommand;
import br.skylight.commons.dli.datalink.DataLinkSetupMessage;
import br.skylight.commons.dli.enums.DataLinkState;
import br.skylight.commons.dli.enums.DataTerminalType;

/**
 * Data terminal used for UDP unicast based communications. Useful when multicast is hard to get working (Windows 7 has some issues with that).
 */
public class UDPUnicastDataTerminal extends DataTerminal {

	private static final Logger logger = Logger.getLogger(UDPUnicastDataTerminal.class.getName());
	
	private String sendToAddress;
	
	private int sendPort;
	private DatagramSocket sendSocket;
	private DatagramPacket outputPacket;

	private int receivePort;
	private DatagramSocket receiveSocket;
	private DatagramPacket inputPacket;
	
	private boolean txEnabled = true;
	private boolean rxEnabled = true;
	
	public UDPUnicastDataTerminal(String sendToAddress, int sendPort, int receivePort, DataTerminalType dataTerminalType, int dataLinkId) {
		super(dataTerminalType, dataLinkId);
		this.sendPort = sendPort;
		this.receivePort = receivePort;
		this.sendToAddress = sendToAddress;
	}

	@Override
	public void onActivate() throws Exception {
		logger.info("Preparing UDP Data Terminal. sendToAddress="+ sendToAddress +"sendPort="+ sendPort +"; receivePort="+receivePort);

		sendSocket = new DatagramSocket();

		InetAddress sendAddress = InetAddress.getByName(sendToAddress);
		outputPacket = new DatagramPacket(new byte[0], 0, sendAddress, sendPort);

		receiveSocket = new DatagramSocket(receivePort);
		
		inputPacket = new DatagramPacket(new byte[0], 0);
		
		getDataLinkStatusReport().setDataLinkState(DataLinkState.TX_AND_RX);
	}

	@Override
	public void onDeactivate() throws Exception {
		super.onDeactivate();
		sendSocket.close();
		receiveSocket.close();
	}

	@Override
	protected int readNextPacket(byte[] buffer) throws IOException {
		if(!rxEnabled) {
			//link is not supposed to receive anything
			return 0;
		}
		inputPacket.setData(buffer);
		receiveSocket.receive(inputPacket);
		return inputPacket.getLength();
	}

	@Override
	public void sendNextPacket(byte[] data, int len) throws IOException {
		if(!txEnabled) {
			//link is not supposed to send anything
			return;
		}
		outputPacket.setData(data, 0, len);
		sendSocket.send(outputPacket);
	}

	@Override
	public int getDownlinkStatus() {
		return (short)(rxEnabled?100:0);
	}
	@Override
	public int getUplinkStatus() {
		return (short)(txEnabled?100:0);
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
		return "UDPUnicastDataTerminal " + super.getInfo();
	}
	
}
