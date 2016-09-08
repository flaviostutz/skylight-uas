package br.skylight.uav.plugins.tcptunnel;

import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugins.streamchannel.StreamChannelOperator;

@ExtensionPointImplementation(extensionPointDefinition=StreamChannelOperator.class)
public class JMXServerOperator extends TCPStreamServerOperator {

	public JMXServerOperator() {
		super(1098);
	}

	@Override
	protected int getMaxBytesPerSecond() {
		return 6000;
	}

	@Override
	protected void onCommandReceived(long commandNumber, String commandText) {
	}
	
}
