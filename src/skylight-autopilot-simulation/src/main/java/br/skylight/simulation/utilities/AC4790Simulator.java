package br.skylight.simulation.utilities;

import java.io.IOException;
import java.io.OutputStream;

import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.io.SerialConnection;
import br.skylight.commons.io.SerialConnectionParams;
import br.skylight.commons.io.SerialConnector;
import br.skylight.commons.io.dataterminal.ac4790.AC4790StreamFilter;

public class AC4790Simulator {

	public static void main(String[] args) throws Exception {
		SerialConnectionParams p1 = new SerialConnectionParams("COM1");
		SerialConnectionParams p2 = new SerialConnectionParams("COM2");
		
		final SerialConnection c1 = SerialConnector.openPort(p1);
		final SerialConnection c2 = SerialConnector.openPort(p2);

		final byte[] packet1 = new byte[128];
		ThreadWorker t1 = new ThreadWorker(999999) {
			public void step() throws Exception {
				int i = c1.getInputStream().read(packet1);
//				System.out.println("1: " + i + " " + getStepFrequencyAverage());
				if(i!=-1) {
					writePacketAPI(c2.getOutputStream(), c1.getOutputStream(), packet1, i);
				}
			};
		};
		t1.activate();

		final byte[] packet2 = new byte[128];
		ThreadWorker t2 = new ThreadWorker(999999) {
			public void step() throws Exception {
				int i = c2.getInputStream().read(packet2);
//				System.out.println("2: " + i);
				if(i!=-1) {
					writePacketAPI(c1.getOutputStream(), c2.getOutputStream(), packet2, i);
				}
			};
		};
		t2.activate();
	}

	protected static void writePacketAPI(OutputStream targetOS, OutputStream sourceOS, byte[] packet, int len) throws IOException {
		//send to target host using RECEIVE API
		synchronized(targetOS) {
			targetOS.write(AC4790StreamFilter.API_RECEIVE_PACKET);
			targetOS.write(len);
			targetOS.write(20);
			targetOS.write(20);
			targetOS.write(1);
			targetOS.write(2);
			targetOS.write(3);
	//		System.out.println(packet.length + " " + len);
			targetOS.write(packet, 0, len);
			targetOS.flush();
		}

		//send to source host using SEND DATA COMPLETE API
		synchronized(sourceOS) {
			sourceOS.write(AC4790StreamFilter.API_SEND_DATA_COMPLETE);
			sourceOS.write(15);
			sourceOS.write(15);
			sourceOS.write(1);
			sourceOS.flush();
		}
	}
	
}
