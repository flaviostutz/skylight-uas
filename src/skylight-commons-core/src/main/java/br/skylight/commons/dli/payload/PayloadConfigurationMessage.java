package br.skylight.commons.dli.payload;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.BitmappedStation;
import br.skylight.commons.dli.enums.PayloadType;
import br.skylight.commons.dli.enums.StationDoor;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class PayloadConfigurationMessage extends Message<PayloadConfigurationMessage> implements MessageTargetedToStation {

	private int vsmID;
	private BitmappedStation payloadStationsAvailable = new BitmappedStation();//u4
	private BitmappedStation stationNumber = new BitmappedStation();//u4
	private PayloadType payloadType;//u2
	private StationDoor stationDoor;//u1
	private int numberOfPayloadRecordingDevices;//u1
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M300;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		vsmID = in.readInt();
		payloadStationsAvailable.setData(readUnsignedInt(in));
		stationNumber.setData(readUnsignedInt(in));
		payloadType = PayloadType.values()[in.readUnsignedByte()];
		stationDoor = StationDoor.values()[in.readUnsignedByte()];
		numberOfPayloadRecordingDevices = in.readUnsignedByte();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt(vsmID);
		out.writeInt((int)payloadStationsAvailable.getData());
		out.writeInt((int)stationNumber.getData());
		out.writeByte(payloadType.ordinal());
		out.writeByte(stationDoor.ordinal());
		out.writeByte(numberOfPayloadRecordingDevices);
	}

	@Override
	public void resetValues() {
		vsmID = 0;
		payloadStationsAvailable.reset();
		stationNumber.reset();
		payloadType = PayloadType.values()[0];
		stationDoor = StationDoor.values()[0];
		numberOfPayloadRecordingDevices = (byte)0;
	}

	public int getVsmID() {
		return vsmID;
	}

	public void setVsmID(int vsmID) {
		this.vsmID = vsmID;
	}

	public BitmappedStation getPayloadStationsAvailable() {
		return payloadStationsAvailable;
	}
	public void setPayloadStationsAvailable(BitmappedStation payloadStationsAvailable) {
		this.payloadStationsAvailable = payloadStationsAvailable;
	}
	public BitmappedStation getStationNumber() {
		return stationNumber;
	}
	public void setStationNumber(BitmappedStation stationNumber) {
		this.stationNumber = stationNumber;
	}
	
	public PayloadType getPayloadType() {
		return payloadType;
	}

	public void setPayloadType(PayloadType payloadType) {
		this.payloadType = payloadType;
	}

	public StationDoor getStationDoor() {
		return stationDoor;
	}

	public void setStationDoor(StationDoor stationDoor) {
		this.stationDoor = stationDoor;
	}

	public int getNumberOfPayloadRecordingDevices() {
		return numberOfPayloadRecordingDevices;
	}

	public void setNumberOfPayloadRecordingDevices(int numberOfPayloadRecordingDevices) {
		this.numberOfPayloadRecordingDevices = numberOfPayloadRecordingDevices;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (stationNumber.getData() ^ (stationNumber.getData() >>> 32));
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
		PayloadConfigurationMessage other = (PayloadConfigurationMessage) obj;
		if (stationNumber != other.stationNumber)
			return false;
		return true;
	}

	@Override
	public BitmappedStation getTargetStations() {
		return stationNumber;
	}
	
}
