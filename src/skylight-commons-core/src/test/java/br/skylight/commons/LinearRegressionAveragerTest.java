package br.skylight.commons;

import br.skylight.commons.infra.LinearRegression;
import br.skylight.commons.infra.MovingAverage;

public class LinearRegressionAveragerTest {

	public static void main(String[] args) {
		LinearRegression r = new LinearRegression(5);
		r.addSample(5);
		r.addSample(5);
		r.addSample(5);
		r.addSample(5);
		r.addSample(5);
		float[] results = r.regress();
		if(results[0]!=5 || results[1]!=0) {
			throw new AssertionError("Wrong results " + results[0] + " " + results[1]);
		}
		if(r.calculateBestYValue()!=5) {
			throw new AssertionError("Best Y was wrong: " + r.calculateBestYValue());
		}

		r.addSample(10);
		r.addSample(10);
		r.addSample(10);
		r.addSample(10);
		r.addSample(10);
		results = r.regress();
		if(results[0]!=10 || results[1]!=0) {
			throw new AssertionError("Wrong results " + results[0] + " " + results[1]);
		}
		if(r.calculateBestYValue()!=10) {
			throw new AssertionError("Best Y was wrong: " + r.calculateBestYValue());
		}

		r.addSample(5);
		r.addSample(0);
		r.addSample(0);
		results = r.regress();
		if(results[0]!=38 || results[1]!=-3) {
			throw new AssertionError("Wrong results " + results[0] + " " + results[1]);
		}

		MovingAverage ma = new MovingAverage(5);
		ma.addSample(10);
		ma.addSample(10);
		ma.addSample(0);
		ma.addSample(5);
		ma.addSample(5);
		if(ma.getAverage()!=6) {
			throw new AssertionError("Wrong results " + ma.getAverage());
		}

		ma.addSample(30);
		ma.addSample(30);
		ma.addSample(50);
		if(ma.getAverage()!=24) {
			throw new AssertionError("Wrong results " + ma.getAverage());
		}
		
		System.out.println("Everything OK");
	}
	
	
}
