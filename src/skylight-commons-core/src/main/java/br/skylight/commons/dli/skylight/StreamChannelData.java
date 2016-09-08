package br.skylight.commons.dli.skylight;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class StreamChannelData extends Message<StreamChannelData> {
	
	//maintain this size as the size left for max body size
	public static final int DATA_LENGTH = 505;
	
	private int channelNumber;//u1
	private short dataLength;//u2
	private byte[] data = new byte[DATA_LENGTH];
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M2014;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		channelNumber = in.readUnsignedByte();
		dataLength = in.readShort();
		in.read(data, 0, dataLength);
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeByte(channelNumber);
		out.writeShort(dataLength);
		out.write(data, 0, dataLength);
	}

	@Override
	public void resetValues() {
		channelNumber = 0;
		dataLength = 0;
	}

	public int getChannelNumber() {
		return channelNumber;
	}

	public void setChannelNumber(int channelNumber) {
		this.channelNumber = channelNumber;
	}

	public short getDataLength() {
		return dataLength;
	}

	public void setDataLength(short dataLength) {
		this.dataLength = dataLength;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public void setData(byte[] dataToBeCopied, int dataLength) {
		System.arraycopy(dataToBeCopied, 0, data, 0, dataLength);
		this.dataLength = (short)dataLength;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + channelNumber;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		//no instance should be replaced by another on async sender
		return false;
	}

}
