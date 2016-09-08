package br.skylight.cucs.plugins.vehiclecontrol;

import br.skylight.commons.Vehicle;
import br.skylight.cucs.mapkit.MapElement;

public class VehicleMapElement extends MapElement {

	private Vehicle vehicle;
	
	public VehicleMapElement(Vehicle vehicle) {
		this.vehicle = vehicle;
	}
	
	public Vehicle getVehicle() {
		return vehicle;
	}
	
}
