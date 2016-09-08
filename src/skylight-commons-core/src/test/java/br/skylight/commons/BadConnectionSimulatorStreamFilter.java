package br.skylight.commons;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import br.skylight.commons.infra.TimedBoolean;
import br.skylight.commons.io.FilteredInputStream;
import br.skylight.commons.io.StreamFilter;

public class BadConnectionSimulatorStreamFilter extends StreamFilter {

	private Random r = new Random();

	private float byteLossRatio = 0;
	private long maxRandomLatencyMillis = 0;
	
	private TimedBoolean randomLoseTimer;
	private TimedBoolean blackoutTimer;

	private boolean filter = true;
	
	public BadConnectionSimulatorStreamFilter() {
		randomLoseTimer = new TimedBoolean(Long.MAX_VALUE-System.currentTimeMillis()-999999999L);
		blackoutTimer = new TimedBoolean(0);
	}
	
	@Override
	public void doFiltering(byte byteIn, OutputStream os, FilteredInputStream<? extends StreamFilter> is) throws IOException {
		try {
			if(maxRandomLatencyMillis>0) {
				Thread.sleep((long)(r.nextFloat()*maxRandomLatencyMillis));
			}
			
			if(blackoutTimer.isTimedOut()) {
				if(blackoutTimer.isFirstTestAfterTimeOut()) {
					randomLoseTimer.reset();
					filter = true;
					System.out.println("LOSING RANDOM BYTES");
				}
				if(randomLoseTimer.isTimedOut()) {
					blackoutTimer.reset();
					filter = false;
					System.out.println("LOSING ALL BYTES");
				}
			}
			
			if(filter) {
				if(r.nextFloat()>=byteLossRatio) {
					super.doFiltering(byteIn, os, is);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void setByteLossRatio(float byteLossRatio) {
		this.byteLossRatio = byteLossRatio;
	}
	public void setMaxRandomLatencyMillis(long maxRandomLatencyMillis) {
		this.maxRandomLatencyMillis = maxRandomLatencyMillis;
	}
	public void setBlackoutTime(long t) {
		blackoutTimer.setTime(t);
	}
	public void setTimeWithRandomLose(long t) {
		randomLoseTimer.setTime(t);
	}
	
}
