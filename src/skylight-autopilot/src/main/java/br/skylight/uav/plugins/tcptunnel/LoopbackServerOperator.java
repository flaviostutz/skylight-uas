package br.skylight.uav.plugins.tcptunnel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;

import br.skylight.commons.io.FilteredInputStream;
import br.skylight.commons.io.StreamFilter;
import br.skylight.commons.io.dataterminal.PipedInputStream2;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.streamchannel.StreamChannelOperator;
import br.skylight.commons.plugins.streamchannel.StreamChannelService;

@ExtensionPointImplementation(extensionPointDefinition=StreamChannelOperator.class)
public class LoopbackServerOperator extends StreamChannelOperator {

	@ServiceInjection
	public StreamChannelService streamChannelService;
	
	private PipedInputStream2 pis1;
	private PipedOutputStream pos1;
	
	private FilteredInputStream is;
	private FileOutputStream fos;
	
	public LoopbackServerOperator() {
		super(127);
		try {
			pis1 = new PipedInputStream2(4096);
			pos1 = new PipedOutputStream(pis1);
			
			is = new FilteredInputStream<StreamFilter>(pis1, new StreamFilter() {
				@Override
				public void doFiltering(byte byteIn, OutputStream os, FilteredInputStream<? extends StreamFilter> is) throws IOException {
					fos.write(byteIn);
					super.doFiltering(byteIn, os, is);
				}				
			});
			is.setHighThoughputMode(true);
//			is.startFilteredMode();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
	
	private LoopbackServerOperator getThis() {
		return this;
	}

	@Override
	protected InputStream getInputStreamFromTarget() {
		return is;
//		return new ByteArrayInputStream(new byte[9999999]);
	}

	@Override
	protected OutputStream getOutputStreamFromTarget() {
		return pos1;
//		return new ByteArrayOutputStream(9999999);
	}

	@Override
	protected void openStreamsFromTarget() throws FileNotFoundException {
		File f = new File("D:\\loopback.data");
		f.delete();
		fos = new FileOutputStream(f);
	}

	@Override
	protected void closeStreamsFromTarget() throws IOException {
		fos.close();
	}
	
	@Override
	protected int getMaxBytesPerSecond() {
		return 500000;
	}

	@Override
	protected void onCommandReceived(long commandNumber, String commandText) {
	}
}
