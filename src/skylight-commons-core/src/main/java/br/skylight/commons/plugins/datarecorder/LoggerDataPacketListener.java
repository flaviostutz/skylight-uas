package br.skylight.commons.plugins.datarecorder;

import br.skylight.commons.io.dataterminal.DataPacketListener;

public interface LoggerDataPacketListener extends DataPacketListener {

	public void onPacketSent(byte[] data, int len);
	
}
