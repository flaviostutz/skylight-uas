package br.skylight.commons;

import java.io.IOException;

import br.skylight.commons.io.dataterminal.DataPacketListener;
import br.skylight.commons.io.dataterminal.MemoryDataTerminal;

public class MemoryDataTerminalTest {

	public static void main(String[] args) throws Exception {
		MemoryDataTerminal m1 = new MemoryDataTerminal();
		m1.setDataPacketListener(new DataPacketListener() {
			public void onPacketReceived(byte[] data, int len, double timestamp) throws IOException {
				if(!"TEST2".equals(new String(data, 0, len, "UTF-8"))) {
					throw new RuntimeException("Wrong received packet data: " + data);
				} else {
					System.out.println("Terminal 1 recv packet: OK");
				}
			}
		});
		
		MemoryDataTerminal m2 = new MemoryDataTerminal();
		m2.setDataPacketListener(new DataPacketListener() {
			public void onPacketReceived(byte[] data, int len, double timestamp) throws IOException {
				if(!"TEST1".equals(new String(data, 0, len, "UTF-8"))) {
					throw new RuntimeException("Wrong received packet data: " + data);
				} else {
					System.out.println("Terminal 2 recv packet: OK");
				}
			}
		});
		m2.setRestartOnException(false);
		m1.connectTo(m2);
		
		m1.activate();
		m2.activate();
		
		m1.sendPacket("TEST1".getBytes("UTF-8"), 5);
		m2.sendPacket("TEST2".getBytes("UTF-8"), 5);

		Thread.currentThread().join();
	}
	
}
