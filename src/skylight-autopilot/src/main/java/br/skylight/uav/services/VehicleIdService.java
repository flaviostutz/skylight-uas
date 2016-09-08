package br.skylight.uav.services;

import br.skylight.commons.dli.systemid.VehicleID;
import br.skylight.commons.plugin.annotations.ServiceDefinition;

@ServiceDefinition
public interface VehicleIdService {

	public VehicleID getInitialVehicleID();
	
}
