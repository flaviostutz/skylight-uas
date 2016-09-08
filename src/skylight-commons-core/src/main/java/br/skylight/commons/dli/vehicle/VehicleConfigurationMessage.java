package br.skylight.commons.dli.vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.VerificationResult;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class VehicleConfigurationMessage extends Message<VehicleConfigurationMessage> {

	private int vsmID;
	private long configurationID;//u4
	private float propulsionFuelCapacity = 1F;//1 kg
	private float propulsionBatteryCapacity = -1;//J
	private float maximumIndicatedAirspeed = 50;
	private float optimumCruiseIndicatedAirspeed = 24;
	private float optimumEnduranceIndicatedAirspeed = 22;
	private float maximumLoadFactor = 2;
	private float grossWeight = 10;//kg
	private float xCG = 0.8F;//center of gravity reward from the nose (m)
	private int numberOfEngines = 1;//u1

	public void readStateForStorage(DataInputStream in) throws IOException {
		readState(in);
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		vsmID = in.readInt();
		configurationID = readUnsignedInt(in);
		propulsionFuelCapacity = in.readFloat();
		propulsionBatteryCapacity = in.readFloat();
		maximumIndicatedAirspeed = in.readFloat();
		optimumCruiseIndicatedAirspeed = in.readFloat();
		optimumEnduranceIndicatedAirspeed = in.readFloat();
		maximumLoadFactor = in.readFloat();
		grossWeight = in.readFloat();
		xCG = in.readFloat();
		numberOfEngines = in.readUnsignedByte();
	}

	public void writeStateForStorage(DataOutputStream out) throws IOException {
		writeState(out);
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt(vsmID);
		out.writeInt((int)configurationID);
		out.writeFloat(propulsionFuelCapacity);
		out.writeFloat(propulsionBatteryCapacity);
		out.writeFloat(maximumIndicatedAirspeed);
		out.writeFloat(optimumCruiseIndicatedAirspeed);
		out.writeFloat(optimumEnduranceIndicatedAirspeed);
		out.writeFloat(maximumLoadFactor);
		out.writeFloat(grossWeight);
		out.writeFloat(xCG);
		out.writeByte(numberOfEngines);
	}

	@Override
	public void resetValues() {
		vsmID = 0;
		configurationID = 0;
		propulsionFuelCapacity = 0;
		propulsionBatteryCapacity = 0;
		maximumIndicatedAirspeed = 0;
		optimumCruiseIndicatedAirspeed = 0;
		optimumEnduranceIndicatedAirspeed = 0;
		maximumLoadFactor = 0;
		grossWeight = 0;
		xCG = 0;
		numberOfEngines = (byte)0;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M100;
	}

	public int getVsmID() {
		return vsmID;
	}

	public void setVsmID(int vsmID) {
		this.vsmID = vsmID;
	}

	public long getConfigurationID() {
		return configurationID;
	}

	public void setConfigurationID(long configurationID) {
		this.configurationID = configurationID;
	}

	public float getPropulsionFuelCapacity() {
		return propulsionFuelCapacity;
	}

	public void setPropulsionFuelCapacity(float propulsionFuelCapacity) {
		this.propulsionFuelCapacity = propulsionFuelCapacity;
	}

	public float getPropulsionBatteryCapacity() {
		return propulsionBatteryCapacity;
	}

	public void setPropulsionBatteryCapacity(float propulsionBatteryCapacity) {
		this.propulsionBatteryCapacity = propulsionBatteryCapacity;
	}

	public float getMaximumIndicatedAirspeed() {
		return maximumIndicatedAirspeed;
	}

	public void setMaximumIndicatedAirspeed(float maximumIndicatedAirspeed) {
		this.maximumIndicatedAirspeed = maximumIndicatedAirspeed;
	}

	public float getOptimumCruiseIndicatedAirspeed() {
		return optimumCruiseIndicatedAirspeed;
	}

	public void setOptimumCruiseIndicatedAirspeed(float optimumCruiseIndicatedAirspeed) {
		this.optimumCruiseIndicatedAirspeed = optimumCruiseIndicatedAirspeed;
	}

	public float getOptimumEnduranceIndicatedAirspeed() {
		return optimumEnduranceIndicatedAirspeed;
	}

	public void setOptimumEnduranceIndicatedAirspeed(float optimumEnduranceIndicatedAirspeed) {
		this.optimumEnduranceIndicatedAirspeed = optimumEnduranceIndicatedAirspeed;
	}

	public float getMaximumLoadFactor() {
		return maximumLoadFactor;
	}

	public void setMaximumLoadFactor(float maximumLoadFactor) {
		this.maximumLoadFactor = maximumLoadFactor;
	}

	public float getGrossWeight() {
		return grossWeight;
	}

	public void setGrossWeight(float grossWeight) {
		this.grossWeight = grossWeight;
	}

	public float getXCG() {
		return xCG;
	}

	public void setXCG(float xcg) {
		xCG = xcg;
	}

	public int getNumberOfEngines() {
		return numberOfEngines;
	}

	public void setNumberOfEngines(int numberOfEngines) {
		this.numberOfEngines = numberOfEngines;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (configurationID ^ (configurationID >>> 32));
		result = prime * result + vsmID;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		VehicleConfigurationMessage other = (VehicleConfigurationMessage) obj;
		if (configurationID != other.configurationID)
			return false;
		if (vsmID != other.vsmID)
			return false;
		return true;
	}

	public void copyFrom(VehicleConfigurationMessage vc) {
		vsmID = vc.getVsmID();
		configurationID = vc.getConfigurationID();
		propulsionFuelCapacity = vc.getPropulsionFuelCapacity();
		propulsionBatteryCapacity = vc.getPropulsionBatteryCapacity();
		maximumIndicatedAirspeed = vc.getMaximumIndicatedAirspeed();
		optimumCruiseIndicatedAirspeed = vc.getOptimumCruiseIndicatedAirspeed();
		optimumEnduranceIndicatedAirspeed = vc.getOptimumEnduranceIndicatedAirspeed();
		maximumLoadFactor = vc.getMaximumLoadFactor();
		grossWeight = vc.getGrossWeight();
		xCG = vc.getXCG();
		numberOfEngines = vc.getNumberOfEngines();
	}

	public VerificationResult validate() {
		VerificationResult vr = new VerificationResult();
		
		//general configuration validation
		vr.assertRange(getMaximumIndicatedAirspeed(), 4, 20, 100, 1000, "Maximum indicated airspeed (m/s)");
		vr.assertRange(getOptimumCruiseIndicatedAirspeed(), 4, 16, 300, getMaximumIndicatedAirspeed(), "Optimum cruise indicated airspeed (m/s)");
		vr.assertRange(getOptimumEnduranceIndicatedAirspeed(), 4, 16, 300, getMaximumIndicatedAirspeed(), "Optimum endurance indicated airspeed (m/s)");
//		vr.assertRange(getPropulsionBatteryCapacity(), 0, 10, 6000, 999999, "Propulsion battery capacity (J)");
		vr.assertRange(getPropulsionFuelCapacity(), 0, 0.2F, 100, 1000, "Propulsion fuel capacity (kg)");
		vr.assertRange(getXCG(), 0, 999, "XCG");
		vr.assertRange(getNumberOfEngines(), 0, 1, "Number of engines must be ONE");
		
		return vr;
	}
	
}
