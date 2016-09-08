package br.skylight.commons.infra;

import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public abstract class ThreadWorker extends Worker implements Runnable {

	private static final Logger logger = Logger.getLogger(ThreadWorker.class.getName());

	private SyncCondition shutdownCondition;
	private SyncCondition readinessCondition;
	private boolean ready;
	private Thread workerThread;
	
	private long realStepTime;
	private long lastStepStartTime;
	private long stepCounter = 0;
	private long stepTimeAveragingStartTime;
	private TimedBoolean stepTimeAveraging;
	private float stepTimeAverage = 0;
	
	private int exceptionCount = 0;
	private Throwable lastException;
	private boolean deactivationRequested = false;

	private long maxTimeBetweenStepsForAlert = -1;
	private long maxTimeBetweenStepsForTimeout = -1;
	private long minStepTime = 0;
	
	private boolean restartOnException = false;
	private long lastWholeStepTime;
	protected String name;
	
	private boolean daemonThread = false;

	public ThreadWorker(float maxStepsFrequency) {
		this(maxStepsFrequency, -1, -1);
	}

	public ThreadWorker(float maxStepsFrequency, long maxTimeBetweenStepsForAlert, long maxTimeBetweenStepsForTimeout) {
		if(maxStepsFrequency==0) throw new IllegalStateException("'maxStepsFrequency' cannot be 0");
		this.minStepTime = (long)(1000.0/maxStepsFrequency);
		this.maxTimeBetweenStepsForAlert = maxTimeBetweenStepsForAlert;
		this.maxTimeBetweenStepsForTimeout = maxTimeBetweenStepsForTimeout;
		this.name = "Worker " + getClass().getSimpleName();
		this.shutdownCondition = new SyncCondition(name + " shutdown");
		this.readinessCondition = new SyncCondition(name + " worker readiness");
		this.stepTimeAveraging = new TimedBoolean(3000);
		stepTimeAveraging.reset();
	}
	
	final public void run() {
		lastStepStartTime = System.currentTimeMillis();
		stepTimeAveragingStartTime = System.currentTimeMillis();
		boolean exceptionOccurred = false;
		try {
			// main loop
			while (isActive()) {

				// ALERTS FROM LAST REAL STEP TIME
				realStepTime = (System.currentTimeMillis() - lastStepStartTime);
				
				//calculate step time average
				if(stepTimeAveraging.checkTrue() && stepCounter>0) {
					stepTimeAverage = (float)(System.currentTimeMillis()-stepTimeAveragingStartTime)/(float)stepCounter;
					stepTimeAveragingStartTime = System.currentTimeMillis();
					stepCounter = 0;
					if (isTimeAlert()) {
						logger.info(toString() + ": step average time alert. actual=" + stepTimeAverage + "ms; alert=" + maxTimeBetweenStepsForAlert + "ms; timeout=" + maxTimeBetweenStepsForTimeout + "ms");
					}
				}
				stepCounter++;

				//hold for minimum time between steps
				if(realStepTime<minStepTime) {
					sleep((int)(minStepTime-realStepTime));
				}
				
				// DO WORK
				lastWholeStepTime = System.currentTimeMillis() - lastStepStartTime;
				lastStepStartTime = System.currentTimeMillis();
				step();
			}

		} catch (Exception e) {
			logger.throwing(null,null,e);
			e.printStackTrace();
			if(exceptionCount<Integer.MAX_VALUE) {
				exceptionCount++;
			}
			exceptionOccurred = true;
			lastException = e;

		} catch (Error e) {
			logger.throwing(null,null,e);
			e.printStackTrace();
			if(exceptionCount<Integer.MAX_VALUE) {
				exceptionCount++;
			}
			exceptionOccurred = true;
			lastException = e;

		} finally {
			try {
				workerThread = null;
				super.setActive(false);
				logger.info(this + ": thread deactivated");
				shutdownCondition.notifyConditionMet();
				
				//verify the need for restarting after an exception has occurred
				if(exceptionOccurred && restartOnException && !deactivationRequested) {
					logger.info(this + ": worker is being restarted because an exception has occurred");
					restart(1000);
				}
			} catch (Exception e) {
				logger.throwing(null,null,e);
			}
		}
	}

	public boolean isTimeAlert() {
		return maxTimeBetweenStepsForAlert != -1 && stepTimeAverage > maxTimeBetweenStepsForAlert;
	}

	@Override
	public final void activate() throws Exception {
		if (!isActive()) {
			// call initialization
			shutdownCondition.notifyConditionNotMet();
			deactivationRequested = false;
			ready = false;
			super.activate();

			if (workerThread == null) {
				workerThread = new Thread(this, getName());
				workerThread.setDaemon(daemonThread);
			}

			workerThread.start();

			logger.info(toString() + ": thread activated");
		} else {
			logger.info(toString() + ": thread worker already activated");
		}

	}

	@Override
	public final void deactivate() throws Exception {
		deactivationRequested = true;
		if (isActive()) {
			super.deactivate();
			ready = false;
		} else {
			logger.info(this + ": thread worker already inactive");
		}
	}

	final public void forceDeactivation(int timeoutForNormalDeactivation) {
		// try to end thread normally
		try {
			//execute in another thread to avoid being locked by deactivation method
			executeDeactivateInAnotherThread();
			waitForDeactivation(timeoutForNormalDeactivation);
		} catch (Exception e) {
			logger.throwing(null,null,e);
			e.printStackTrace();
		}

		// thread didn't exit normally. Forcing thread interruption
		if (workerThread != null && workerThread.isAlive()) {
			logger.warning(this + ": Normal deactivation failed. Calling Thread.interrupt()...");
			workerThread.interrupt();
			executeDeactivateInAnotherThread();
		}
		workerThread = null;
	}

	private void executeDeactivateInAnotherThread() {
		Thread t = new Thread() {
			public void run() {
				try {
					deactivate();
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		};
		t.start();
	}

	public long getTimeSinceLastStep() {
		return System.currentTimeMillis() - lastStepStartTime;
	}

	public boolean isTimeout() {
		if(maxTimeBetweenStepsForTimeout==-1) {
			return false;
		}
		return getTimeSinceLastStep() > maxTimeBetweenStepsForTimeout;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
		readinessCondition.notifyConditionMet();
	}

	final public long getLastStepTime() {
		return realStepTime;
	}

	final public long getStepTimeAverage() {
		return (long)stepTimeAverage;
	}

	public String getStatusInfo() {
		return toString() + ": " + (isReady() ? "ready" : (isActive() ? "active" : "inactive")) + "/" + getTimeSinceLastStep() + "ms/" + getStepFrequencyAverage() + "Hz";
	}

	public void sleep(int timeout) {
		synchronized(this) {
			try {
//				Thread.sleep(timeout);//in J2ME this was more sucetible to thread starvation
				wait(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public float getStepFrequencyAverage() {
		return 1000F/stepTimeAverage;
	}

	public void waitForReadiness(int timeout) {
		try {
			readinessCondition.waitForCondition(timeout);
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		}
	}

	public void waitForDeactivation(int timeout) {
		try {
			if(workerThread!=null && workerThread.isAlive()) {
				shutdownCondition.waitForCondition(timeout);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setMaxTimeBetweenStepsForAlert(long maxTimeBetweenStepsForAlert) {
		this.maxTimeBetweenStepsForAlert = maxTimeBetweenStepsForAlert;
	}
	public void setMaxTimeBetweenStepsForTimeout(long maxTimeBetweenStepsForTimeout) {
		this.maxTimeBetweenStepsForTimeout = maxTimeBetweenStepsForTimeout;
	}
	
	public void restart(int timeoutForDeactivation) {
		try {
			forceDeactivation(timeoutForDeactivation);
			waitForDeactivation(timeoutForDeactivation);
			activate();
		} catch (Exception e) {
			logger.throwing(null,null,e);
			throw new RuntimeException("Couldn't restart thread worker " + toString() + ". e=" + e);
		}
	}
	
	public int getExceptionCount() {
		return exceptionCount;
	}
	
	public void setRestartOnException(boolean restartOnException) {
		this.restartOnException = restartOnException;
	}

	public void setName(String name) {
		this.name = name;
	}
	private String getName() {
		return name;
	}
	
	public Thread getWorkerThread() {
		return workerThread;
	}
	
	public long getLastWholeStepTime() {
		return lastWholeStepTime;
	}
	
	public Throwable getLastException() {
		return lastException;
	}
	
	public String getStackTrace(int maxStacks, boolean simple) {
		if(workerThread!=null) {
			try {
				return formatSimpleStack(workerThread.getStackTrace(), maxStacks, simple);
			} catch (Exception e) {
				return e.toString();
			}
		} else {
			return "No thread is active for this ThreadWorker";
		}
	}

	private String formatSimpleStack(StackTraceElement[] stackTrace, int maxStacks, boolean simple) {
		String s = "";
		for(int i=0; i<Math.min(stackTrace.length, maxStacks); i++) {
			StackTraceElement e = stackTrace[i];
			if(!simple) {
				s += e.toString() + "\n";
			} else {
				String c = e.getClassName();
				int p = c.lastIndexOf(".");
				if(p!=-1) {
					c = c.substring(p+1);
				}
				s += c + "." + e.getMethodName() +"("+ e.getLineNumber() +")" + "\n";
			}
		}
		return s;
	}

	public String getLastExceptionStackTrace(int maxStacks, boolean simple) {
		if(lastException!=null) {
			return formatSimpleStack(lastException.getStackTrace(), maxStacks, simple);
		} else {
			return "No exceptions found in current Thread Worker";
		}
	}
	
}
