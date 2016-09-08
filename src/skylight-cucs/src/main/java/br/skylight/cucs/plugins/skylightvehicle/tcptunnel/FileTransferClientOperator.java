package br.skylight.cucs.plugins.skylightvehicle.tcptunnel;

import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.plugins.streamchannel.FileTransferOperator;
import br.skylight.commons.plugins.streamchannel.StreamChannelOperator;

@ManagedMember
@ExtensionPointImplementation(extensionPointDefinition=StreamChannelOperator.class)
public class FileTransferClientOperator extends FileTransferOperator {

	public FileTransferClientOperator() {
		super(21);
	}

	@Override
	protected int getMaxBytesPerSecond() {
		return 250000;
	}

	
	
}
