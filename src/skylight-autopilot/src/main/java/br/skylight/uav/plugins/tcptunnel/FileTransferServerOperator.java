package br.skylight.uav.plugins.tcptunnel;

import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.plugins.streamchannel.FileTransferOperator;
import br.skylight.commons.plugins.streamchannel.StreamChannelOperator;

@ManagedMember
@ExtensionPointImplementation(extensionPointDefinition=StreamChannelOperator.class)
public class FileTransferServerOperator extends FileTransferOperator {

	public FileTransferServerOperator() {
		super(21);
	}

	@Override
	protected int getMaxBytesPerSecond() {
		return 250000;
	}

	
	
}
