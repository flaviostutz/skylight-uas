package br.skylight.uav.plugins.storage;

import java.io.File;

import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.services.StorageService;

@ServiceDefinition
@ServiceImplementation(serviceDefinition=StorageService.class)
public class UAVStorageService extends StorageService {

	@Override
	public File getBaseDir() {
		return new File(File.separator + "Skylight" + File.separator + "uav");
	}

}
