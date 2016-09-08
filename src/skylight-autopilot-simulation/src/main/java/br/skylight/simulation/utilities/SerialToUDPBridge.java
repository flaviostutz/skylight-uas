package br.skylight.simulation.utilities;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.io.SerialConnectionParams;
import br.skylight.commons.io.dataterminal.DataPacketListener;
import br.skylight.commons.io.dataterminal.SerialDataTerminal;

/**
 * This class implements a two way bridge between a serial connection and UDP packets
 * When this is started, it starts to listen to incoming packets over serial line. When it gets
 * any packet, it sends to the target host as a UDP packet.
 * In the meanwhile it still listens to packets on UDP side, when it gets any packet, send it over
 * the serial line too.
 * The data transmitted over serial line has marks to indicate packet delimitation.
 * @author Flavio Stutz
 */
public class SerialToUDPBridge extends SerialDataTerminal implements DataPacketListener {

	private static DatagramSocket udpReceiver;
	
	private String sendToUdpHost;
	private int sendToUdpPort;
	private DatagramSocket udpSender;
	
	private SerialToUDPBridge(SerialConnectionParams serialParams, String sendToUdpHost, int sendToUdpPort) {
		super(serialParams, DataTerminalType.ADT, 1111, false, false);
		this.sendToUdpHost = sendToUdpHost;
		this.sendToUdpPort = sendToUdpPort;
	}
	
	@Override
	public void onPacketReceived(byte[] data, int len, double timestamp) throws IOException {
		if(udpSender==null) {
			udpSender = new DatagramSocket();
		}
		DatagramPacket p = new DatagramPacket(data, len);
		p.setSocketAddress(new InetSocketAddress(sendToUdpHost, sendToUdpPort));
		udpSender.send(p);
	}

	public static void main(String[] args) throws Exception {
		String serialPort = "COM1";
		int receiveFromUdpPort = 7700;//8800
		String sendToUdpHost = "localhost";
		int sendToUdpPort = 7800;//8900
		if(args.length>=4) {
			serialPort = args[0];
			sendToUdpHost = args[1];
			sendToUdpPort = Integer.parseInt(args[2]);
		}
		startBridge(serialPort, receiveFromUdpPort, sendToUdpHost, sendToUdpPort);
	}
	
	public static void startBridge(String serialPort, final int receiveFromUdpPort, String sendToUdpHost, int sendToUdpPort) throws Exception {
		//start SERIAL receiver
		final SerialToUDPBridge s = new SerialToUDPBridge(new SerialConnectionParams(serialPort), sendToUdpHost, sendToUdpPort);
		s.setDataPacketListener(s);
		s.activate();
		System.out.println("Receiving/sending data from/to serial port '"+ serialPort +"'");
		System.out.println("Sending UDP packets to '"+ sendToUdpHost+":"+sendToUdpPort +"'");
		
		//start UDP receiver
		ThreadWorker t = new ThreadWorker(50) {
			@Override
			public void onActivate() throws Exception {
				udpReceiver = new DatagramSocket(receiveFromUdpPort);
			}
			@Override
			public void step() throws Exception {
				byte[] buffer = new byte[1024];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				udpReceiver.receive(packet);
				s.sendNextPacket(packet.getData(), packet.getLength());
			};
		};
		t.activate();
		System.out.println("Receiving packets from UDP port '"+ receiveFromUdpPort +"'");
		System.out.println(">> Serial to UDP bridge started");
		s.waitForDeactivation(Integer.MAX_VALUE);
	}
	
}
