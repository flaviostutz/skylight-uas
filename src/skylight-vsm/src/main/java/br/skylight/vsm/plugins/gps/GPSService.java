package br.skylight.vsm.plugins.gps;

import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;

@ServiceDefinition
@ServiceImplementation(serviceDefinition=GPSService.class)
public class GPSService extends ThreadWorker {

	public GPSService() {
		super(10);
	}
	
	@Override
	public void step() throws Exception {
		//implement
	}

}
