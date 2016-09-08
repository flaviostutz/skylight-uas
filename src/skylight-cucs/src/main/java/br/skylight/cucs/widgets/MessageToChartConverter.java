package br.skylight.cucs.widgets;

import org.jfree.data.xy.XYSeriesCollection;

import br.skylight.commons.dli.services.Message;

public interface MessageToChartConverter {

	public void addMessageDataToDataset(Message message, XYSeriesCollection series);
	
}
