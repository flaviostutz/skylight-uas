package br.skylight.commons.infra;

public class CounterStats {

	private double total;
	private TimedBoolean rateTimer;
	
	private long lastRateTime;
	private double lastRateTotal;
	private double rate;
	
	private double lastValue;
	
	public CounterStats() {
		this(1000);
	}
	
	public CounterStats(long rateTime) {
		rateTimer = new TimedBoolean(rateTime);
		rateTimer.reset();
		lastRateTime = System.currentTimeMillis();
	}
	
	public void addValue(double value) {
		if((Double.MAX_VALUE-total)<1000) {
			total = 0;
		}
		total += value;
		updateStats();
	}
	
	public void setValue(double value) {
		total += (value - lastValue);
		lastValue = value;
		updateStats();
	}
	
	public double getRate() {
		updateStats();
		return rate;
	}
	
	public double getTotal() {
		return total;
	}

	private void updateStats() {
		if(rateTimer.checkTrue()) {
			rate = ((total-lastRateTotal)/(System.currentTimeMillis()-lastRateTime))*1000.0;
			lastRateTotal = total;
			lastRateTime = System.currentTimeMillis();
		}
	}
	
}
