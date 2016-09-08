package br.skylight.cucs.plugins.skylightvehicle.tcptunnel;

import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.plugins.streamchannel.StreamChannelOperator;

@ManagedMember
@ExtensionPointImplementation(extensionPointDefinition=StreamChannelOperator.class)
public class TelnetClientOperator extends TCPStreamClientOperator {

	public TelnetClientOperator() {
		super(23, 2323);
	}

	@Override
	protected int getMaxBytesPerSecond() {
		return 4000;
	}

	@Override
	protected void onCommandReceived(long commandNumber, String commandText) {
	}

}
