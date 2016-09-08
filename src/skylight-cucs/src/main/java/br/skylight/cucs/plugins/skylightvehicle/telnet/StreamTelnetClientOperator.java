package br.skylight.cucs.plugins.skylightvehicle.telnet;

import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.plugins.streamchannel.StreamChannelOperator;
import br.skylight.cucs.plugins.skylightvehicle.tcptunnel.TCPStreamClientOperator;

@ManagedMember
@ExtensionPointImplementation(extensionPointDefinition=StreamChannelOperator.class)
public class StreamTelnetClientOperator extends TCPStreamClientOperator {

	public StreamTelnetClientOperator() {
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
