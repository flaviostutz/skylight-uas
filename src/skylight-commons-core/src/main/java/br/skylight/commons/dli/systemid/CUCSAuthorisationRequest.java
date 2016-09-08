package br.skylight.commons.dli.systemid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.BitmappedLOI;
import br.skylight.commons.dli.BitmappedStation;
import br.skylight.commons.dli.enums.ControlledStationMode;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class CUCSAuthorisationRequest extends Message<CUCSAuthorisationRequest> implements MessageWithVsmID {

	private int vsmID;
	private int dataLinkID;
	private int vehicleType;//u2
	private int vehicleSubtype;//u2
	private BitmappedLOI requestedHandoverLOI = new BitmappedLOI();//u1
	private BitmappedStation controlledStation = new BitmappedStation();//u4
	private ControlledStationMode controlledStationMode = ControlledStationMode.REQUEST_CONTROL;//u1
	private int waitForVehicleCoordinationMessage;//u1
	
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

	public int getVehicleType() {
		return vehicleType;
	}

	public void setVehicleType(int vehicleType) {
		this.vehicleType = vehicleType;
	}

	public int getVehicleSubtype() {
		return vehicleSubtype;
	}

	public void setVehicleSubtype(int vehicleSubtype) {
		this.vehicleSubtype = vehicleSubtype;
	}

	public BitmappedStation getControlledStation() {
		return controlledStation;
	}
	public void setControlledStation(BitmappedStation controlledStation) {
		this.controlledStation = controlledStation;
	}
	
	public BitmappedLOI getRequestedHandoverLOI() {
		return requestedHandoverLOI;
	}
	public void setRequestedHandoverLOI(BitmappedLOI requestedHandoverLOI) {
		this.requestedHandoverLOI = requestedHandoverLOI;
	}

	public ControlledStationMode getControlledStationMode() {
		return controlledStationMode;
	}
	public void setControlledStationMode(ControlledStationMode controlledStationMode) {
		this.controlledStationMode = controlledStationMode;
	}

	public int getWaitForVehicleCoordinationMessage() {
		return waitForVehicleCoordinationMessage;
	}

	public void setWaitForVehicleCoordinationMessage(int waitForVehicleCoordinationMessage) {
		this.waitForVehicleCoordinationMessage = waitForVehicleCoordinationMessage;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		vsmID = in.readInt();
		dataLinkID = in.readInt();
		vehicleType = in.readUnsignedShort();//u2
		vehicleSubtype = in.readUnsignedShort();//u2
		requestedHandoverLOI.setData(in.readUnsignedByte());//u1
		controlledStation.setData(readUnsignedInt(in));//u4
		controlledStationMode = ControlledStationMode.values()[in.readUnsignedByte()];//u1
		waitForVehicleCoordinationMessage = in.readUnsignedByte();//u1
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt(vsmID);
		out.writeInt(dataLinkID);
		out.writeShort(vehicleType);//u2
		out.writeShort(vehicleSubtype);//u2
		out.writeByte((byte)requestedHandoverLOI.getData());//u1
		out.writeInt((int) controlledStation.getData());//u4
		out.writeByte(controlledStationMode.ordinal());//u1
		out.writeByte(waitForVehicleCoordinationMessage);//u1
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (controlledStation.getData() ^ (controlledStation.getData() >>> 32));
		result = prime * result + dataLinkID;
		result = prime * result + (int)requestedHandoverLOI.getData();
		result = prime * result + vehicleSubtype;
		result = prime * result + vehicleType;
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
		CUCSAuthorisationRequest other = (CUCSAuthorisationRequest) obj;
		if (controlledStation != other.controlledStation)
			return false;
		if (dataLinkID != other.dataLinkID)
			return false;
		if (requestedHandoverLOI != other.requestedHandoverLOI)
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
		dataLinkID = 0;
		vehicleType = 0;
		vehicleSubtype = 0;
		requestedHandoverLOI.reset();
		controlledStation.reset();
		controlledStationMode = ControlledStationMode.values()[0];
		waitForVehicleCoordinationMessage = 0;
	}

}
