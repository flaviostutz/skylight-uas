package br.skylight.commons.dli.systemid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.VehicleType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;


public class VehicleID extends Message<VehicleID> implements MessageWithVsmID {

	private int vsmID;
	private int vehicleIDUpdate;
	private VehicleType vehicleType = VehicleType.TYPE_60;//u2
	private int vehicleSubtype;//u2
	private int owningCountryCode;//u1
	private String tailNumber = "0";//c16
	private String missionID = "";//c20
	private String atcCallSign = "";//c32
	
	public int getVsmID() {
		return vsmID;
	}
	public void setVsmID(int vsmID) {
		this.vsmID = vsmID;
	}
	public int getVehicleIDUpdate() {
		return vehicleIDUpdate;
	}
	public void setVehicleIDUpdate(int vehicleIDUpdate) {
		this.vehicleIDUpdate = vehicleIDUpdate;
	}

	public VehicleType getVehicleType() {
		return vehicleType;
	}
	public void setVehicleType(VehicleType vehicleType) {
		this.vehicleType = vehicleType;
	}
	public int getVehicleSubtype() {
		return vehicleSubtype;
	}
	public void setVehicleSubtype(int vehicleSubtype) {
		this.vehicleSubtype = vehicleSubtype;
	}
	public int getOwningCountryCode() {
		return owningCountryCode;
	}
	public void setOwningCountryCode(int owningCountryCode) {
		this.owningCountryCode = owningCountryCode;
	}
	public String getTailNumber() {
		return tailNumber;
	}
	public void setTailNumber(String tailNumber) {
		this.tailNumber = tailNumber;
	}
	public String getMissionID() {
		return missionID;
	}
	public void setMissionID(String missionID) {
		this.missionID = missionID;
	}
	public String getATCCallSign() {
		return atcCallSign;
	}
	public void setATCCallSign(String callSign) {
		atcCallSign = callSign;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		vsmID = in.readInt();
		vehicleIDUpdate = in.readInt();
		vehicleType = VehicleType.values()[in.readUnsignedShort()];//u2
		vehicleSubtype = in.readUnsignedShort();//u2
		owningCountryCode = in.readUnsignedByte();//u1
		tailNumber = readNullTerminatedString(in);//c16
		missionID = readNullTerminatedString(in);//c20
		atcCallSign = readNullTerminatedString(in);//c32
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt(vsmID);
		out.writeInt(vehicleIDUpdate);
		out.writeShort(vehicleType.ordinal());//u2
		out.writeShort(vehicleSubtype);//u2
		out.writeByte(owningCountryCode);//u1
		writeNullTerminatedString(out, tailNumber);//c16
		writeNullTerminatedString(out, missionID);//c20
		writeNullTerminatedString(out, atcCallSign);//c32
	}
	@Override
	public MessageType getMessageType() {
		return MessageType.M20;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + vehicleIDUpdate;
		result = prime * result + vehicleSubtype;
		result = prime * result + vehicleType.ordinal();
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
		VehicleID other = (VehicleID) obj;
		if (vehicleIDUpdate != other.vehicleIDUpdate)
			return false;
		if (vehicleSubtype != other.vehicleSubtype)
			return false;
		if (vehicleType != other.vehicleType)
			return false;
		if (vsmID != other.vsmID)
			return false;
		return true;
	}
	
	@Override
	public void resetValues() {
		vsmID = 0;
		vehicleIDUpdate = 0;
		vehicleType = VehicleType.values()[0];
		vehicleSubtype = 0;
		owningCountryCode = 0;
		tailNumber = "";
		missionID = "";
		atcCallSign = "";
	}
	public void copyFrom(VehicleID v) {
		vsmID = v.getVsmID();
		vehicleIDUpdate = v.getVehicleIDUpdate();
		vehicleType = v.getVehicleType();
		vehicleSubtype = v.getVehicleSubtype();
		owningCountryCode = v.getOwningCountryCode();
		tailNumber = v.getTailNumber();
		missionID = v.getMissionID();
		atcCallSign = v.getATCCallSign();
	}
	
}
