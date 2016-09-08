package br.skylight.cucs.plugins.core;

import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;

@ServiceDefinition
@ServiceImplementation(serviceDefinition=UserService.class)
public class UserService extends Worker {

	private int currentCucsId = IOHelper.parseUnsignedHex("11111111");
	
	@Override
	public void onActivate() throws Exception {
	}
	
	public int getCurrentCucsId() {
		return currentCucsId;
	}
	
}
