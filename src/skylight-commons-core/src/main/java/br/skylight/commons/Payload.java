package br.skylight.commons;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.skylight.commons.dli.enums.PayloadType;
import br.skylight.commons.dli.enums.StationDoor;
import br.skylight.commons.dli.payload.PayloadSteeringCommand;

public class Payload extends ControllableElement {

	private int uniqueStationNumber = 0;
	private PayloadType payloadType = PayloadType.NOT_SPECIFIED;
	private StationDoor stationDoor = StationDoor.NO;
	private int numberOfPayloadRecordingDevices = 0;
	private EOIRPayload eoIrPayload;

	//used by cucs
//	private Map<MessageType,Message> lastReceivedMessages = new HashMap<MessageType,Message>();
	private List<Target> targets = new ArrayList<Target>();
	private PayloadSteeringCommand payloadSteeringCommand;

//	public void setLastReceivedMessage(Message message) {
//		lastReceivedMessages.put(message.getMessageType(), message);
//	}
//	
//	public Message getLastReceivedMessage(MessageType messageType) {
//		return lastReceivedMessages.get(messageType);
//	}
	
	public int getUniqueStationNumber() {
		return uniqueStationNumber;
	}
	public void setUniqueStationNumber(int uniqueStationNumber) {
		this.uniqueStationNumber = uniqueStationNumber;
	}
	
	public int getNumberOfPayloadRecordingDevices() {
		return numberOfPayloadRecordingDevices;
	}
	public PayloadType getPayloadType() {
		return payloadType;
	}
	public StationDoor getStationDoor() {
		return stationDoor;
	}
	public void setNumberOfPayloadRecordingDevices(int numberOfPayloadRecordingDevices) {
		this.numberOfPayloadRecordingDevices = numberOfPayloadRecordingDevices;
	}
	public void setPayloadType(PayloadType payloadType) {
		this.payloadType = payloadType;
	}
	public void setStationDoor(StationDoor stationDoor) {
		this.stationDoor = stationDoor;
	}
	
	public List<Target> getTargets() {
		return targets;
	}
	public void setTargets(List<Target> targets) {
		this.targets = targets;
	}
	
	public String getLabel() {
		if(getName()!=null && getName().trim().length()>0) {
			return getName();
		} else {
			return getPayloadType().getName() + " (#" + getUniqueStationNumber() + ")";
		}
	}
	
	public EOIRPayload getEoIrPayload() {
		return eoIrPayload;
	}
	public void setEoIrPayload(EOIRPayload eoIrPayload) {
		this.eoIrPayload = eoIrPayload;
	}
	
	public EOIRPayload resolveEoIrPayload() {
		if(eoIrPayload==null) {
			eoIrPayload = new EOIRPayload();
		}
		return eoIrPayload;
	}
	
	public void setPayloadSteeringCommand(PayloadSteeringCommand payloadSteeringCommand) {
		this.payloadSteeringCommand = payloadSteeringCommand;
	}
	public PayloadSteeringCommand resolvePayloadSteeringCommand() {
		if(payloadSteeringCommand==null) {
			payloadSteeringCommand = new PayloadSteeringCommand();
		}
		return payloadSteeringCommand;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (uniqueStationNumber ^ (uniqueStationNumber >>> 32));
		result = prime * result + ((vehicleID == null) ? 0 : vehicleID.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Payload other = (Payload) obj;
		if (uniqueStationNumber != other.uniqueStationNumber)
			return false;
		if (vehicleID == null) {
			if (other.vehicleID != null)
				return false;
		} else if (!vehicleID.equals(other.vehicleID))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return getLabel();
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
//		uniqueStationNumber = in.readInt();
//		payloadType = PayloadType.values()[in.readUnsignedShort()];//u2
//		stationDoor = StationDoor.values()[in.readUnsignedByte()];//u1
//		numberOfPayloadRecordingDevices = in.readUnsignedByte();//u1
		//eo ir payload extension
		if(in.readBoolean()) {
			eoIrPayload = new EOIRPayload();
			eoIrPayload.readState(in);
		}
		//payload steering
		if(in.readBoolean()) {
			payloadSteeringCommand = new PayloadSteeringCommand();
			payloadSteeringCommand.readState(in);
		}
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
//		out.writeInt(uniqueStationNumber);
//		out.writeShort(payloadType.ordinal());//u2
//		out.writeByte(stationDoor.ordinal());//u1
//		out.writeByte(numberOfPayloadRecordingDevices);//u1
		out.writeBoolean(eoIrPayload!=null);
		if(eoIrPayload!=null) {
			eoIrPayload.writeState(out);
		}
		out.writeBoolean(payloadSteeringCommand!=null);
		if(payloadSteeringCommand!=null) {
			payloadSteeringCommand.writeState(out);
		}
	}
	
}
