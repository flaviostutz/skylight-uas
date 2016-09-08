package br.skylight.commons.infra;

public class MovingAverage {

	private int n;
	private float[] samples;
	
	//avoid recalculating results that are already known
	private boolean lastResultValid;
	private float lastResult = 0;

	public MovingAverage(int numberOfSamples) {
		samples = new float[numberOfSamples];
		lastResultValid = false;
	}

	public float getAverage() {
		if(n==0) return 0;
		if(!lastResultValid) {
			float sum = 0;
			for (int i = 0; i < n; i++) {
				sum += samples[i];
			}
			lastResult = sum/n;
			lastResultValid = true;
		}
		return lastResult;
	}
	
	public void addSample(float value) {
		if (n < samples.length) {
			n++;
		} else {
			//put new sample in tail
			for (int i = 0; i < samples.length-1; i++) {
				samples[i] = samples[i+1];
			}
		}
		samples[n-1] = value;
		lastResultValid = false;
	}
	
	public int getNumberOfSamples() {
		return n;
	}

	public void reset() {
		samples = new float[samples.length];
	}
	
}
