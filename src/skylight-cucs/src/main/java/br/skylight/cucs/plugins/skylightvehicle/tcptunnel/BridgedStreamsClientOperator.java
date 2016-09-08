package br.skylight.cucs.plugins.skylightvehicle.tcptunnel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;

import br.skylight.commons.io.dataterminal.PipedInputStream2;
import br.skylight.commons.plugins.streamchannel.StreamChannelOperator;

public abstract class BridgedStreamsClientOperator extends StreamChannelOperator {

	private PipedInputStream2 pis1;
	private PipedOutputStream pos1;
	private PipedInputStream2 pis2;
	private PipedOutputStream pos2;
	
	public BridgedStreamsClientOperator(int channelNumber) {
		super(channelNumber);
	}

	@Override
	protected InputStream getInputStreamFromTarget() throws IOException {
		return pis1;
	}

	@Override
	protected OutputStream getOutputStreamFromTarget() throws IOException {
		return pos2;
	}

	@Override
	protected void openStreamsFromTarget() throws IOException {
		pis1 = new PipedInputStream2();
		pos1 = new PipedOutputStream(pis1);
		pis2 = new PipedInputStream2();
		pos2 = new PipedOutputStream(pis2);
	}

	@Override
	protected void closeStreamsFromTarget() throws IOException {
		pis1.close();
		pos1.close();
		pis2.close();
		pos2.close();
	}
	
	public InputStream getInputStream() {
		return pis2;
	}
	
	public OutputStream getOutputStream() {
		return pos1;
	}
	
}
