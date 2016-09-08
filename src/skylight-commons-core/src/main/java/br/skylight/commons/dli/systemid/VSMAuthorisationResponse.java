package br.skylight.commons.dli.systemid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.BitmappedLOI;
import br.skylight.commons.dli.BitmappedStation;
import br.skylight.commons.dli.enums.ControlledStationModeResponse;
import br.skylight.commons.dli.enums.VehicleType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;


public class VSMAuthorisationResponse extends Message<VSMAuthorisationResponse> implements MessageWithVsmID {

	private int vsmID;
	private int dataLinkID;
	private BitmappedLOI loiAuthorized = new BitmappedLOI();//u1
	private BitmappedLOI loiGranted = new BitmappedLOI();//u1
	private BitmappedStation controlledStation = new BitmappedStation();//u4
	private ControlledStationModeResponse controlledStationMode = ControlledStationModeResponse.NOT_IN_CONTROL;//u1
	private VehicleType vehicleType;//u2
	private int vehicleSubtype;//u2

	public int getVsmID() {
		return vsmID;
	}
	public void setVsmID(int vsmID) {
		this.vsmID = vsmID;
	}
	public int getDataLinkID() {
		return dataLinkID;
	}
	public void setDataLinkID(int dataLinkID) {
		this.dataLinkID = dataLinkID;
	}
	public BitmappedStation getControlledStation() {
		return controlledStation;
	}
	public void setControlledStation(BitmappedStation controlledStation) {
		this.controlledStation = controlledStation;
	}
	public ControlledStationModeResponse getControlledStationMode() {
		return controlledStationMode;
	}
	public void setControlledStationMode(ControlledStationModeResponse controlledStationMode) {
		this.controlledStationMode = controlledStationMode;
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
	
	public BitmappedLOI getLoiAuthorized() {
		return loiAuthorized;
	}
	public BitmappedLOI getLoiGranted() {
		return loiGranted;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		vsmID = in.readInt();
		dataLinkID = in.readInt();
		loiAuthorized.setData(in.readUnsignedByte());//bitmap
		loiGranted.setData(in.readUnsignedByte());//bitmap
		controlledStation.setData(readUnsignedInt(in));//u4
		controlledStationMode = ControlledStationModeResponse.values()[in.readUnsignedByte()];//u1
		vehicleType = VehicleType.values()[in.readUnsignedShort()];//u2
		vehicleSubtype = in.readUnsignedShort();//u2
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt(vsmID);
		out.writeInt(dataLinkID);
		out.writeByte((byte)loiAuthorized.getData());//bitmap
		out.writeByte((byte)loiGranted.getData());//bitmap
		out.writeInt((int) controlledStation.getData());//u4
		out.writeByte(controlledStationMode.ordinal());//u1
		out.writeShort(vehicleType.ordinal());//u2
		out.writeShort(vehicleSubtype);//u2
	}
	@Override
	public MessageType getMessageType() {
		return MessageType.M21;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dataLinkID;
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
		VSMAuthorisationResponse other = (VSMAuthorisationResponse) obj;
		if (dataLinkID != other.dataLinkID)
			return false;
		if (vsmID != other.vsmID)
			return false;
		return true;
	}
	@Override
	public void resetValues() {
		vsmID = 0;
		dataLinkID = 0;
		loiAuthorized.reset();
		loiGranted.reset();
		controlledStation.reset();
		controlledStationMode = ControlledStationModeResponse.NOT_IN_CONTROL;
		vehicleType = VehicleType.values()[0];
		vehicleSubtype = 0;
	}
	
}
