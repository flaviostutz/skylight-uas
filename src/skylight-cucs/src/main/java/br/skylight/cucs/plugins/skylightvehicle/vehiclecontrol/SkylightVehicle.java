package br.skylight.cucs.plugins.skylightvehicle.vehiclecontrol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.SkylightMission;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.infra.SerializableState;

public class SkylightVehicle implements SerializableState {

	private int vehicleID;
	private SkylightMission skylightMission;
	private SkylightVehicleConfigurationMessage skylightVehicleConfiguration;
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		out.writeInt(vehicleID);
		out.writeBoolean(skylightVehicleConfiguration!=null);
		if(skylightVehicleConfiguration!=null) {
			skylightVehicleConfiguration.writeStateExtended(out);
		}
		
		out.writeBoolean(skylightMission!=null);
		if(skylightMission!=null) {
			skylightMission.writeState(out);
		}
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		vehicleID = in.readInt();
		boolean hasConfiguration = in.readBoolean();
		if(hasConfiguration) {
			skylightVehicleConfiguration = new SkylightVehicleConfigurationMessage();
			skylightVehicleConfiguration.readStateExtended(in);
		} else {
			skylightVehicleConfiguration = null;
		}
		
		boolean hasMission = in.readBoolean();
		if(hasMission) {
			skylightMission = new SkylightMission();
			skylightMission.readState(in);
		} else {
			skylightMission = null;
		}
	}

	public int getVehicleID() {
		return vehicleID;
	}
	
	public void setVehicleID(int vehicleID) {
		this.vehicleID = vehicleID;
	}
	
	public void setSkylightMission(SkylightMission skylightMission) {
		this.skylightMission = skylightMission;
	}
	public void setSkylightVehicleConfiguration(SkylightVehicleConfigurationMessage skylightVehicleConfiguration) {
		this.skylightVehicleConfiguration = skylightVehicleConfiguration;
	}
	public SkylightMission getSkylightMission() {
		return skylightMission;
	}
	public SkylightVehicleConfigurationMessage getSkylightVehicleConfiguration() {
		return skylightVehicleConfiguration;
	}
	
}
