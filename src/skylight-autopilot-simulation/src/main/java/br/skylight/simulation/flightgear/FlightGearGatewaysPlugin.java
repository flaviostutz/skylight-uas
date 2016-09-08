package br.skylight.simulation.flightgear;

import br.skylight.commons.plugin.Plugin;
import br.skylight.commons.plugin.annotations.PluginDefinition;
import br.skylight.commons.plugins.logrecorder.AsyncFileLoggingHandler;
import br.skylight.uav.plugins.messaging.MessageScheduler;

@PluginDefinition(members={MessageScheduler.class, AsyncFileLoggingHandler.class})
public class FlightGearGatewaysPlugin extends Plugin {

}
