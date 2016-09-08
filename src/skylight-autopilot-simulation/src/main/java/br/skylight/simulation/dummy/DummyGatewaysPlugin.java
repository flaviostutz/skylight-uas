package br.skylight.simulation.dummy;

import br.skylight.commons.plugin.Plugin;
import br.skylight.commons.plugin.annotations.PluginDefinition;
import br.skylight.uav.plugins.messaging.MessageScheduler;

@PluginDefinition(members={MessageScheduler.class})
public class DummyGatewaysPlugin extends Plugin {

}
