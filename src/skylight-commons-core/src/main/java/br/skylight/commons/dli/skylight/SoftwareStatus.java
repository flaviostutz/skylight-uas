package br.skylight.commons.dli.skylight;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class SoftwareStatus extends Message<SoftwareStatus> {

	private long missionCRC;
	private long vehicleConfigurationCRC;
	private long skylightVehicleConfigurationCRC;
	private String softwareVersion = "";
	
//	private ArrayList<SoftwarePartState> softwarePartStates = new ArrayList<SoftwarePartState>();
//	private int next = 0;//used to avoid unnecessary instantiations (just for gc optimization)

	@Override
	public MessageType getMessageType() {
		return MessageType.M2013;
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeLong(missionCRC);
		out.writeLong(vehicleConfigurationCRC);
		out.writeLong(skylightVehicleConfigurationCRC);
//		IOHelper.writeArrayList(out, softwarePartStates);
		out.writeUTF(softwareVersion);
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		missionCRC = in.readLong();
		vehicleConfigurationCRC = in.readLong();
		skylightVehicleConfigurationCRC = in.readLong();
//		IOHelper.readArrayList(in, SoftwarePartState.class, softwarePartStates);
		softwareVersion = in.readUTF();
	}
	
	@Override
	public void resetValues() {
//		next = 0;
	}

	/**
	 * Used just to avoid object creation
	 */
//	public SoftwarePartState resolveNextSoftwarePartState() {
//		synchronized(softwarePartStates) {
//			//no instance available. Create a new one
//			if(softwarePartStates.size()<=next) {
//				SoftwarePartState s = new SoftwarePartState();
//				softwarePartStates.add(s);
//				next++;
//				return s;
//			//return an existing instance
//			} else {
//				return softwarePartStates.get(next++);
//			}
//		}
//	}
	
//	public ArrayList<SoftwarePartState> getSoftwarePartStates() {
//		return softwarePartStates;
//	}
	
	public long getMissionCRC() {
		return missionCRC;
	}
	public void setMissionCRC(long missionCRC) {
		this.missionCRC = missionCRC;
	}
	public long getVehicleConfigurationCRC() {
		return vehicleConfigurationCRC;
	}
	public void setVehicleConfigurationCRC(long vehicleConfigurationCRC) {
		this.vehicleConfigurationCRC = vehicleConfigurationCRC;
	}
	
	public long getSkylightVehicleConfigurationCRC() {
		return skylightVehicleConfigurationCRC;
	}
	public void setSkylightVehicleConfigurationCRC(long skylightVehicleConfigurationCRC) {
		this.skylightVehicleConfigurationCRC = skylightVehicleConfigurationCRC;
	}
	
	public String getSoftwareVersion() {
		return softwareVersion;
	}
	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	
}
