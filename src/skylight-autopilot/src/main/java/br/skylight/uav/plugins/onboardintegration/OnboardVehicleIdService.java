package br.skylight.uav.plugins.onboardintegration;

import br.skylight.commons.dli.enums.VehicleType;
import br.skylight.commons.dli.systemid.VehicleID;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.uav.services.VehicleIdService;

@ServiceImplementation(serviceDefinition=VehicleIdService.class)
public class OnboardVehicleIdService implements VehicleIdService {

	public static VehicleID VEHICLE_ID = new VehicleID();
	static {
		VEHICLE_ID.setVehicleID(Integer.parseInt("010101", 16));
		VEHICLE_ID.setVehicleIDUpdate(Integer.parseInt("010101", 16));
		VEHICLE_ID.setVehicleType(VehicleType.TYPE_60);
		VEHICLE_ID.setVehicleSubtype(3);
		VEHICLE_ID.setTailNumber("BR010101");
		VEHICLE_ID.setATCCallSign("BR010101");
	}
	
	@Override
	public VehicleID getInitialVehicleID() {
		return VEHICLE_ID;
	}

}
