package br.skylight.simulation.xplane;

import br.skylight.commons.dli.enums.VehicleType;
import br.skylight.commons.dli.systemid.VehicleID;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.uav.services.VehicleIdService;

@ServiceImplementation(serviceDefinition=VehicleIdService.class)
public class XPlaneVehicleIdService implements VehicleIdService {

	public static VehicleID VEHICLE_ID = new VehicleID();
	static {
		VEHICLE_ID.setVehicleID(Integer.parseInt("040404", 16));
		VEHICLE_ID.setVehicleIDUpdate(Integer.parseInt("040404", 16));
		VEHICLE_ID.setVehicleType(VehicleType.TYPE_60);
		VEHICLE_ID.setVehicleSubtype(3);
		VEHICLE_ID.setTailNumber("BR040404");
		VEHICLE_ID.setATCCallSign("BR040404");
	}
	
	@Override
	public VehicleID getInitialVehicleID() {
		return VEHICLE_ID;
	}

}
