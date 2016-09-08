package br.skylight.cucs.widgets;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;

public class TelemetryChartFrame extends ChartFrame implements MessageListener {

	private static final long serialVersionUID = 6362147060040325712L;
	
	private MessageToChartConverter converter;
	private XYSeriesCollection ds;
	
	private TelemetryChartFrame(String title, JFreeChart chart, MessageToChartConverter converter, XYSeriesCollection ds) {
		super(title, chart);
		this.converter = converter;
		this.ds = ds;
	}
	
	public static TelemetryChartFrame createMultiChart(String title, String xtitle, String ytitle, boolean legend, int maxItemsInSeries, MessageToChartConverter converter, String ... seriesNames) {
		XYSeriesCollection ds = new XYSeriesCollection();
		for (String seriesName : seriesNames) {
			XYSeries series = new XYSeries(seriesName, false, true);
			series.setMaximumItemCount(maxItemsInSeries);
			ds.addSeries(series);
		}
		JFreeChart chart = ChartFactory.createXYLineChart(title, xtitle, ytitle, ds, PlotOrientation.VERTICAL, legend, true, true);
		return new TelemetryChartFrame(title, chart, converter, ds);
	}

	public void onMessageReceived(Message message) {
		converter.addMessageDataToDataset(message, ds);
	}

}
