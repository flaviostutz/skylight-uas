package br.skylight.cucs.plugins.core;

import java.io.File;

import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.services.StorageService;

@ServiceDefinition
@ServiceImplementation(serviceDefinition=StorageService.class)
public class CUCSStorageService extends StorageService {

	@Override
	public File getBaseDir() {
		return new File(File.separator + "Skylight" + File.separator + "cucs");
	}

}
