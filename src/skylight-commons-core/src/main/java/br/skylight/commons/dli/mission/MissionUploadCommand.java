package br.skylight.commons.dli.mission;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.MissionPlanMode;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class MissionUploadCommand extends Message<MissionUploadCommand> {

	private String missionID;//c20
	private MissionPlanMode missionPlanMode;//u1
	private int waypointNumber;//u2
	
	public String getMissionID() {
		return missionID;
	}

	public void setMissionID(String missionID) {
		this.missionID = missionID;
	}

	public MissionPlanMode getMissionPlanMode() {
		return missionPlanMode;
	}

	public void setMissionPlanMode(MissionPlanMode missionPlanMode) {
		this.missionPlanMode = missionPlanMode;
	}

	public int getWaypointNumber() {
		return waypointNumber;
	}

	public void setWaypointNumber(int waypointNumber) {
		this.waypointNumber = waypointNumber;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.M800;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		missionID = readNullTerminatedString(in);//c20
		missionPlanMode = MissionPlanMode.values()[in.readUnsignedByte()];//u1
		waypointNumber = in.readUnsignedShort();//u2
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		writeNullTerminatedString(out, missionID);//c20
		out.writeByte(missionPlanMode.ordinal());//u1
		out.writeShort(waypointNumber);//u2
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((missionID == null) ? 0 : missionID.hashCode());
		result = prime * result + ((missionPlanMode == null) ? 0 : missionPlanMode.hashCode());
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
		MissionUploadCommand other = (MissionUploadCommand) obj;
		if (missionID == null) {
			if (other.missionID != null)
				return false;
		} else if (!missionID.equals(other.missionID))
			return false;
		if (missionPlanMode == null) {
			if (other.missionPlanMode != null)
				return false;
		} else if (!missionPlanMode.equals(other.missionPlanMode))
			return false;
		return true;
	}

	@Override
	public void resetValues() {
		missionID = null;
		missionPlanMode = null;
		waypointNumber = 0;
	}
	
}
