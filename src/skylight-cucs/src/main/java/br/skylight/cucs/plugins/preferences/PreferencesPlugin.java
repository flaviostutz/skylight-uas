package br.skylight.cucs.plugins.preferences;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import br.skylight.commons.MeasureType;
import br.skylight.commons.infra.SerializableState;
import br.skylight.commons.plugin.Plugin;
import br.skylight.commons.plugin.annotations.PluginDefinition;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.services.StorageService;

@PluginDefinition
public class PreferencesPlugin extends Plugin implements SerializableState {

	private static final Logger logger = Logger.getLogger(PreferencesPlugin.class.getName());
	
	@ServiceInjection
	public StorageService storageService;
	
	@Override
	public void onActivate() throws Exception {
		//load measure type preferences
		try {
			storageService.loadState("preferences", "preferences-units.dat", PreferencesPlugin.class);
		} catch (Exception e) {
			logger.throwing(null,null,e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDeactivate() throws Exception {
		storageService.saveState(this, "preferences", "preferences-units.dat");
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		MeasureType.readState(in);
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		MeasureType.writeState(out);
	}
	
}
