package br.skylight.cucs.plugins.skylightvehicle.tcptunnel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.plugins.streamchannel.StreamChannelOperator;

public abstract class TCPStreamClientOperator extends StreamChannelOperator {

	private static final Logger logger = Logger.getLogger(TCPStreamClientOperator.class.getName());
	
	private Socket clientSocket;
	private int listenPort;
//	private boolean reopenServer = false;
	private ThreadWorker listenerWorker;
	
	public TCPStreamClientOperator(int channelNumber, int listenPort) {
		super(channelNumber);
		this.listenPort = listenPort;
	}

	@Override
	public void onActivate() throws Exception {
		listenerWorker = new ThreadWorker(10) {
			ServerSocket ss;
			@Override
			public void onActivate() throws Exception {
				ss = new ServerSocket(listenPort);
				ss.setSoTimeout(500);
				logger.info("Waiting for a connection on port " + listenPort + " for TCP tunneling");
				setName("TCPStreamClientOperator.listenerWorker");
			}
			@Override
			public void step() throws Exception {
				try {
					clientSocket = ss.accept();
					//create a new thread to avoid deadlock because we are deactivating ourself!
					Thread t = new Thread() {
						public void run() {
							deactivateListenerWorker();
						};
					};
					t.start();
				} catch (Exception e) {
				}
			}
			@Override
			public void onDeactivate() throws Exception {
				ss.close();
				logger.info("Stopped waiting for a connection on port " + listenPort + " for TCP tunneling");
			}
		};
		super.onActivate();
	}
	
	@Override
	public void onDeactivate() throws Exception {
		deactivateListenerWorker();
		super.onDeactivate();
	}
	
	@Override
	protected InputStream getInputStreamFromTarget() throws IOException {
		if(clientSocket==null) {
			throw new RuntimeException("No client has connected to this TCP tunnel");
		}
		return clientSocket.getInputStream();
	}

	@Override
	protected OutputStream getOutputStreamFromTarget() throws IOException {
		if(clientSocket==null) {
			throw new RuntimeException("No client has connected to this TCP tunnel");
		}
		return clientSocket.getOutputStream();
	}

	@Override
	protected void openStreamsFromTarget() throws IOException {
		if(listenerWorker.isActive()) {
			throw new RuntimeException("Cannot open this channel because it is already listening to connections");
		}
		try {
			listenerWorker.activate();
			listenerWorker.waitForDeactivation(Integer.MAX_VALUE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void closeStreamsFromTarget() throws IOException {
		clientSocket.close();
		clientSocket = null;
	}

	@Override
	public void closeChannel() throws IOException {
		deactivateListenerWorker();
		super.closeChannel();
	}
	@Override
	protected void closeChannelInternal() throws IOException {
		deactivateListenerWorker();
//		if(reopenServer) {
//			Thread t = new Thread() {
//				public void run() {
//					try {
//						openChannel(getCucsId(), getVehicleId());
//					} catch (IOException e) {
//						throw new RuntimeException(e);
//					}
//				}
//			};
//			t.start();
//		}
		super.closeChannelInternal();
	}
	
	/**
	 * Retry listening to other client connection after the first client connection is dropped.
	 * @param keepListening
	 */
//	public void setReopenServer(boolean reopenServer) {
//		this.reopenServer = reopenServer;
//	}

	private void deactivateListenerWorker() {
		//wait for deactivation
		try {
			listenerWorker.deactivate();
			listenerWorker.waitForDeactivation(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int getListenPort() {
		return listenPort;
	}

}
