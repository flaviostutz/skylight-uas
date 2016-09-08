package br.skylight.commons.dli.payload;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.BitmappedStation;
import br.skylight.commons.dli.enums.DoorState;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class PayloadBayCommand extends Message<PayloadBayCommand> implements MessageTargetedToStation {

	private BitmappedStation stationNumber = new BitmappedStation();//u4
	private DoorState payloadBayDoors;//u1
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M206;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		stationNumber.readState(in);
		payloadBayDoors = DoorState.values()[in.readUnsignedByte()];
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		stationNumber.writeState(out);
		out.writeByte(payloadBayDoors.ordinal());
	}

	@Override
	public void resetValues() {
		stationNumber.reset();
		payloadBayDoors = DoorState.values()[0];
	}

	public BitmappedStation getStationNumber() {
		return stationNumber;
	}

	public DoorState getPayloadBayDoors() {
		return payloadBayDoors;
	}

	public void setPayloadBayDoors(DoorState payloadBayDoors) {
		this.payloadBayDoors = payloadBayDoors;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((stationNumber == null) ? 0 : stationNumber.hashCode());
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
		PayloadBayCommand other = (PayloadBayCommand) obj;
		if (stationNumber == null) {
			if (other.stationNumber != null)
				return false;
		} else if (!stationNumber.equals(other.stationNumber))
			return false;
		return true;
	}

	@Override
	public BitmappedStation getTargetStations() {
		return stationNumber;
	}

}
