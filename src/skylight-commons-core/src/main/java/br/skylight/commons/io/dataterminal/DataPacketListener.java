package br.skylight.commons.io.dataterminal;

import java.io.IOException;

public interface DataPacketListener {

	public void onPacketReceived(byte[] data, int len, double timestamp) throws IOException;
	
}
