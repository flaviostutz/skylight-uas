package br.skylight.commons.dli.payload;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.BitmappedStation;
import br.skylight.commons.dli.enums.DoorState;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class PayloadBayStatus extends Message<PayloadBayStatus> implements MessageTargetedToStation {

	private BitmappedStation stationNumber = new BitmappedStation();//u4
	private DoorState payloadBayDoorStatus;//u1

	public PayloadBayStatus() {
		resetValues();
	}
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M308;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		stationNumber.setData(readUnsignedInt(in));
		payloadBayDoorStatus = DoorState.values()[in.readUnsignedByte()];
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		stationNumber.writeState(out);
		out.writeByte(payloadBayDoorStatus.ordinal());
	}

	@Override
	public void resetValues() {
		stationNumber.setData(0);
		payloadBayDoorStatus = DoorState.CLOSED;
	}

	public BitmappedStation getStationNumber() {
		return stationNumber;
	}

	public DoorState getPayloadBayDoorStatus() {
		return payloadBayDoorStatus;
	}

	public void setPayloadBayDoorStatus(DoorState payloadBayDoorStatus) {
		this.payloadBayDoorStatus = payloadBayDoorStatus;
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
		PayloadBayStatus other = (PayloadBayStatus) obj;
		if (stationNumber != other.stationNumber)
			return false;
		return true;
	}

	@Override
	public BitmappedStation getTargetStations() {
		return stationNumber;
	}
	
}