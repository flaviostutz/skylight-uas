package br.skylight.uav.plugins.tcptunnel;

import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugins.streamchannel.StreamChannelOperator;

@ExtensionPointImplementation(extensionPointDefinition=StreamChannelOperator.class)
public class TelnetServerOperator extends TCPStreamServerOperator {

	public TelnetServerOperator() {
		super(23);
	}

	@Override
	protected int getMaxBytesPerSecond() {
		return 4000;
	}

	@Override
	protected void onCommandReceived(long commandNumber, String commandText) {
	}
	
}
