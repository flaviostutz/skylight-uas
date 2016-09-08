package br.skylight.cucs.plugins.logplayer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.io.dataterminal.DataPacketListener;
import br.skylight.commons.io.dataterminal.DirectDataTerminal;
import br.skylight.commons.plugins.datarecorder.DataRecorderService;

public class LogPlayer extends ThreadWorker {

	private LogPlayerListener listener;
	private long currentTime;
	private float speed;
	private DataInputStream inLog;
	private boolean shouldRelocateStreams = true;

	private long beginPacketTime = -1;
	private long endPacketTime = -1;
	private long startRealTime;

	private long lastProcessedPacketTime = -1;
	
	public DirectDataTerminal directDataTerminal;
	
	public LogPlayer(File inLogFile, DirectDataTerminal directDataTerminal, LogPlayerListener listener) throws IOException {
		super(Float.MAX_VALUE);
		this.listener = listener;
		this.directDataTerminal = directDataTerminal;
		this.speed = 1;

		//received data log
		if(!inLogFile.exists()) throw new FileNotFoundException(inLogFile.getName());
		FileInputStream fis = new FileInputStream(inLogFile);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		IOHelper.readInputStreamIntoOutputStream(fis, bos);
		inLog = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));

		//get first packet time
		DataRecorderService.readPackets(inLog, new DataPacketListener() {
			@Override
			public void onPacketReceived(byte[] data, int len, double timestamp) throws IOException {
				if(beginPacketTime==-1) {
					beginPacketTime = (long)(timestamp*1000);
				}
				endPacketTime = (long)(timestamp*1000);
			}
		});
		currentTime = beginPacketTime;
		inLog.reset();
	}

	@Override
	public void onActivate() throws Exception {
		startRealTime = System.currentTimeMillis() - getTimeElapsed();
	}
	
	@Override
	public void step() throws Exception {
		try {
			//process next message
			DataRecorderService.readNextPacket(inLog, new DataPacketListener() {
				@Override
				public void onPacketReceived(byte[] data, int len, double timestamp) throws IOException {
					lastProcessedPacketTime = (long)(timestamp*1000);
					directDataTerminal.setNextPacketToBeRead(data, len, lastProcessedPacketTime/1000.0, true);
				}
			});
			
			//simulate timing
			long logElapTime = lastProcessedPacketTime - beginPacketTime;
			currentTime = lastProcessedPacketTime;
			
			long estimatedElapTime = getEstimatedElapsedTime();
			
			if(estimatedElapTime<logElapTime) {
				synchronized(this) {
					wait(logElapTime-estimatedElapTime);
				}
			}
			listener.onTimeElapsed(logElapTime);
		} catch (EOFException e) {
			stopCurrentPlay();
			listener.onEndReached();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setTime(long timeMillis) {
		shouldRelocateStreams = true;
		stopCurrentPlay();
		this.currentTime = timeMillis;
	}
	
	public void stopCurrentPlay() {
		forceDeactivation(1000);
	}
	public void pause() {
		forceDeactivation(1000);
	}

	public void start() throws Exception {
		if(isActive()) {
			stopCurrentPlay();
		}
		
		//position streams at desired time
		if(shouldRelocateStreams) {
			try {
				positionStreamsAtTime(currentTime);
			} catch (IOException e) {
				e.printStackTrace();
				stopCurrentPlay();
				return;
			}
		}
		activate();
	}
	
	public boolean isStarted() {
		return isActive();
	}
	
	public void setSpeed(float speed) {
		this.speed = speed;
	}

	private void positionStreamsAtTime(long time) throws IOException {
		//position streams at desired time
		inLog.reset();

		do {
			//process next message
			DataRecorderService.readNextPacket(inLog, new DataPacketListener() {
				@Override
				public void onPacketReceived(byte[] data, int len, double timestamp) throws IOException {
					lastProcessedPacketTime = (long)(timestamp*1000);
				}
			});
		} while(lastProcessedPacketTime<time);
		
		shouldRelocateStreams = false;
	}

	public long getTimeElapsed() {
		return currentTime - beginPacketTime;
	}
	
	public long getEstimatedElapsedTime() {
		return (long)((System.currentTimeMillis() - startRealTime) * speed);
	}

	public long getBeginPacketTime() {
		return beginPacketTime;
	}
	public long getEndPacketTime() {
		return endPacketTime;
	}
	public long getLastProcessedPacketTime() {
		return lastProcessedPacketTime;
	}
	
}
