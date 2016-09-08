package br.skylight.cucs.plugins.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import br.skylight.commons.VerificationResult;
import br.skylight.commons.dli.enums.VehicleType;
import br.skylight.commons.plugin.annotations.ExtensionPointDefinition;

@ExtensionPointDefinition
public interface VehicleControlExtensionPoint {

	public boolean isCompatibleWith(VehicleType vehicleType);
	public String getExtensionIdentification();

	public void validateMission(int vehicleId, VerificationResult vr);
	public void sendMissionToVehicle(int vehicleId);
	public void saveMission(int vehicleId, DataOutputStream dos);
	public void loadMission(int vehicleId, DataInputStream dis);
	public void createNewMission(int vehicleId);

}
