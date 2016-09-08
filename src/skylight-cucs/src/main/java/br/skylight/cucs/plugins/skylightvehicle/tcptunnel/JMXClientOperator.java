package br.skylight.cucs.plugins.skylightvehicle.tcptunnel;

import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.plugins.streamchannel.StreamChannelOperator;

@ManagedMember
@ExtensionPointImplementation(extensionPointDefinition=StreamChannelOperator.class)
public class JMXClientOperator extends TCPStreamClientOperator {

	public JMXClientOperator() {
		super(1099, 2099);
	}

	@Override
	protected int getMaxBytesPerSecond() {
		return 6000;
	}

	@Override
	protected void onCommandReceived(long commandNumber, String commandText) {
	}

}
