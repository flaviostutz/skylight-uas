package br.skylight.commons.infra;

import java.util.concurrent.TimeoutException;

public class SyncCondition {

	private String name;
	private boolean conditionMet = false;
	private Object metMonitor = new Object();
	private Object notMetMonitor = new Object();

	public SyncCondition(String name) {
		this.name = name;
	}
	
	public void notifyConditionNotMet() {
		conditionMet = false;
		synchronized(notMetMonitor) {
			notMetMonitor.notifyAll();
		}
	}
	
	public void notifyConditionMet() {
		conditionMet = true;
		synchronized(metMonitor) {
			metMonitor.notifyAll();
		}
//		System.out.println("CONDITION MET NOTIFICATION");
	}

	public void waitForConditionMet() throws TimeoutException {
		waitForCondition(Long.MAX_VALUE);
	}
	
	public void waitForCondition(long timeout) throws TimeoutException {
		long st = System.currentTimeMillis();
		if(!conditionMet) {
			try {
				synchronized(metMonitor) {
					metMonitor.wait(timeout);
					if((System.currentTimeMillis()-st)>=timeout) {
						throw new TimeoutException("Timeout waiting for condition met for '" + name + "'");
					}
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
//		System.out.println("FINISHED WAITING FOR CONDITION");
	}

	public void waitForConditionNotMet(int timeout) throws TimeoutException {
		long st = System.currentTimeMillis();
		if(conditionMet) {
			try {
				synchronized(notMetMonitor) {
					notMetMonitor.wait(timeout);
					if((System.currentTimeMillis()-st)>=timeout) {
						throw new TimeoutException("Timeout waiting for condition not met for '" + name + "'");
					}
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public boolean isConditionMet() {
		return conditionMet;
	}
	
}
