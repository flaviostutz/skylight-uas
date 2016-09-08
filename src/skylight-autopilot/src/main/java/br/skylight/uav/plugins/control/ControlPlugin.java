package br.skylight.uav.plugins.control;

import br.skylight.commons.plugin.Plugin;
import br.skylight.commons.plugin.annotations.PluginDefinition;
import br.skylight.commons.plugins.logrecorder.AsyncFileLoggingHandler;
import br.skylight.uav.plugins.control.pids.PIDControllers;

@PluginDefinition(members={PIDControllers.class, AsyncFileLoggingHandler.class})
public class ControlPlugin extends Plugin {

}
