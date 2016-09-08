package br.skylight.uav.plugins.tcptunnel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.streamchannel.StreamChannelOperator;
import br.skylight.commons.plugins.streamchannel.StreamChannelService;

@ServiceImplementation(serviceDefinition=StreamChannelOperator.class)
public abstract class TCPStreamServerOperator extends StreamChannelOperator {

	private Socket socket;

	@ServiceInjection
	public StreamChannelService streamChannelService;
	
	@ServiceInjection
	public PluginManager pluginManager;
	
	public TCPStreamServerOperator(int channelNumber) {
		super(channelNumber);
	}

	@Override
	public void onActivate() throws Exception {
		pluginManager.executeAfterStartup(new Runnable() {
			@Override
			public void run() {
				streamChannelService.registerStreamChannelOperator(getThis());
			}
		});
	}
	
	private TCPStreamServerOperator getThis() {
		return this;
	}

	@Override
	protected InputStream getInputStreamFromTarget() throws IOException {
		return socket.getInputStream();
	}

	@Override
	protected OutputStream getOutputStreamFromTarget() throws IOException {
		return socket.getOutputStream();
	}

	@Override
	protected void openStreamsFromTarget() throws IOException {
		if(socket==null) {
			socket = new Socket("127.0.0.1", getChannelNumber());
		}
	}

	@Override
	protected void closeStreamsFromTarget() throws IOException {
		if(socket!=null) {
			socket.close();
			socket = null;
		}
	}
	
}
