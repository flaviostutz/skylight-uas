package br.skylight.cucs.plugins.timer;

public class Timer {

	private long startTime;
	private long countdownTime = -1;
	private boolean countdownEnabled;
	private long accumulatedTimeBeforePause;
	private boolean started = false;
	private TimerListener listener;

	public void startTimer() {
		startTime = System.currentTimeMillis();
		started = true;
		Thread t = new Thread() {
			public void run() {
				while(started) {
					try {
						notifyTimeElapsed();
						Thread.sleep(100);
						if(countdownEnabled) {
							if(getRemainingTime()<=0) {
								notifyCountdownFinished();
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
	}

	private void notifyCountdownFinished() {
		if(listener!=null) {
			listener.onCountdownFinished();
		}
	}
	
	void notifyTimeElapsed() {
		if(listener!=null) {
			listener.onTimeElapsed(getElapsedTime());
		}
	}

	public void pauseTimer() {
		accumulatedTimeBeforePause = getElapsedTime();
		started = false;
		if(listener!=null) {
			listener.onTimeElapsed(getElapsedTime());
		}
	}
	
	public void resetTimer() {
		startTime = -1;
		accumulatedTimeBeforePause = 0;
		started = false;
		if(listener!=null) {
			listener.onTimeElapsed(getElapsedTime());
		}
	}

	public void setCountdownTime(long countdownTime) {
		this.countdownTime = countdownTime;
		notifyTimeElapsed();
	}
	
	public long getElapsedTime() {
		if(started) {
			return accumulatedTimeBeforePause + (System.currentTimeMillis()-startTime);
		} else {
			return accumulatedTimeBeforePause;
		}
	}
	
	public long getCountdownTime() {
		return countdownTime;
	}
	
	public long getRemainingTime() {
		return countdownTime - getElapsedTime();
	}
	
	public void setListener(TimerListener listener) {
		this.listener = listener;
	}

	public boolean isStarted() {
		return started;
	}
	
	public void setCountdownEnabled(boolean countdownEnabled) {
		this.countdownEnabled = countdownEnabled;
	}
	
	public boolean isCountdownEnabled() {
		return countdownEnabled;
	}

}
