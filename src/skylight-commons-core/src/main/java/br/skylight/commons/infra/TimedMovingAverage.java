package br.skylight.commons.infra;

public class TimedMovingAverage {

	private TimedBoolean timer;
	private MovingAverage movingAverage;
	
	public TimedMovingAverage(int numberOfSamples, long minTimeBetweenSamplesMillis) {
		timer = new TimedBoolean(minTimeBetweenSamplesMillis);
		movingAverage = new MovingAverage(numberOfSamples);
	}
	
	public void addSample(float value) {
		if(timer.checkTrue()) {
			movingAverage.addSample(value);
		}
	}
	
	public float getAverage() {
		return movingAverage.getAverage();
	}
	
}
