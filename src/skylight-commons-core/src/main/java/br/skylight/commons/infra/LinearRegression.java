package br.skylight.commons.infra;

public class LinearRegression {

	private int n;
	private float[][] samples;
	private float lastx;
	private float[] lastResult = new float[]{0,0};

	//avoid recalculating results that are already known
	private boolean lastResultValid;
	
	public LinearRegression(int numberOfSamples) {
		samples = new float[numberOfSamples][2];
		lastResultValid = false;
	}

	/**
	 * Performs a regression on samples and returns an array containing the
	 * elements "a" and "b" for the form "y = a + bx".
	 * 
	 * @return
	 */
	public float[] regress() {
		if (n == 0) {
			throw new IllegalStateException("No sample was added to this calculator yet");
		}
		if(!lastResultValid) {
			double sumx=0, sumy=0, sumxx=0, sumxy=0;
			for (int i = 0; i < n; i++) {
				float x = samples[i][0];
				float y = samples[i][1];
				sumx += x;
				sumy += y;
				sumxx += x*x;
				sumxy += x*y;
			}
			double sxx = sumxx-(sumx*sumx/n);
			double sxy = sumxy-(sumx*sumy/n);
			if(sxx!=0) {
				lastResult[1] = (float)(sxy/sxx);//b
				lastResult[0] = (float)((sumy-(lastResult[1]*sumx))/n);//a
			}
			lastResultValid = true;
		}
		return lastResult;
	}

	public void addSample(float x, float y) {
		lastx = x;
		if (n < samples.length) {
			n++;
		} else {
			//put new sample in tail
			for (int i = 0; i < samples.length-1; i++) {
				samples[i][0] = samples[i+1][0];
				samples[i][1] = samples[i+1][1];
			}
		}
		samples[n-1][0] = x;
		samples[n-1][1] = y;
		lastResultValid = false;
	}
	
	public void addSample(float y) {
		addSample(lastx+1, y);
	}
	
	public int getCurrentNumberOfSamples() {
		return n;
	}
	
	public int getMaxNumberOfSamples() {
		return samples.length;
	}

	public void reset() {
		n = 0;
	}
	
	public float calculateBestYValue() {
		if (n > 0) {
			float[] r = regress();
			return r[0] + r[1]*(samples[0][0] + ((samples[n-1][0] - samples[0][0])/2F));
		} else {
			return 0;
		}
	}
	
}
