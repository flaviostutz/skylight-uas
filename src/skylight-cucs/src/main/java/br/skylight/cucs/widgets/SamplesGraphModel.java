package br.skylight.cucs.widgets;

import java.awt.Color;

public class SamplesGraphModel {

	private int n = 0;
	private int maxSamples = 20;
	private double[] samples = new double[maxSamples];
	private Color color = new Color(0,1,0,0.7F);
	private double minValue = 0;
	private double maxValue = 100;
	
	private SamplesGraph samplesGraph;

	public void addSample(double value) {
 		if (n < samples.length) {
			n++;
		} else {
			//put new sample in tail
			for (int i = 0; i < samples.length-1; i++) {
				samples[i] = samples[i+1];
			}
		}
		samples[n-1] = value;
		if(samplesGraph!=null) {
			samplesGraph.repaint();
		}
	}

	public void setMaxSamples(int maxSamples) {
		this.maxSamples = maxSamples;
		this.samples = new double[maxSamples];
		this.n = 0;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	public Color getColor() {
		return color;
	}
	
	public double[] getSamples() {
		return samples;
	}
	
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}
	public double getMaxValue() {
		return maxValue;
	}
	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}
	public double getMinValue() {
		return minValue;
	}
	
	public void setSamplesGraph(SamplesGraph samplesGraph) {
		this.samplesGraph = samplesGraph;
	}

	public void clear() {
		setMaxSamples(maxSamples);
	}
	
}
