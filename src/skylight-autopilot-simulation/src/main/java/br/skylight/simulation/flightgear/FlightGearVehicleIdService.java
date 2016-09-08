package br.skylight.simulation.flightgear;

import br.skylight.commons.dli.enums.VehicleType;
import br.skylight.commons.dli.systemid.VehicleID;
import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.uav.services.VehicleIdService;

@ServiceImplementation(serviceDefinition=VehicleIdService.class)
public class FlightGearVehicleIdService implements VehicleIdService {

	public static VehicleID VEHICLE_ID = new VehicleID();
	static {
		VEHICLE_ID.setVehicleID(IOHelper.parseUnsignedHex("02020202"));
		VEHICLE_ID.setVehicleIDUpdate(IOHelper.parseUnsignedHex("02020202"));
		VEHICLE_ID.setVehicleType(VehicleType.TYPE_60);
		VEHICLE_ID.setVehicleSubtype(3);
		VEHICLE_ID.setTailNumber("BR02020202");
		VEHICLE_ID.setATCCallSign("BR02020202");
	}
	
	@Override
	public VehicleID getInitialVehicleID() {
		return VEHICLE_ID;
	}

}
