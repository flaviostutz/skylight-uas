package br.skylight.commons.dli.services;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.infra.SerializableState;

public class MessageWrapper implements SerializableState {

	//wrapper message
	private int idd;
	private int msgInstance;
	private int messageType;
	private int messageLength;
	private int streamID;
	private int packetSeq;
		
	public int getIdd() {
		return idd;
	}
	public void setIdd(int idd) {
		this.idd = idd;
	}
	public int getMsgInstance() {
		return msgInstance;
	}
	public void setMsgInstance(int msgInstance) {
		this.msgInstance = msgInstance;
	}
	public int getMessageType() {
		return messageType;
	}
	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}
	public int getMessageLength() {
		return messageLength;
	}
	public void setMessageLength(int messageLength) {
		this.messageLength = messageLength;
	}
	public int getStreamID() {
		return streamID;
	}
	public void setStreamID(int streamID) {
		this.streamID = streamID;
	}
	public int getPacketSeq() {
		return packetSeq;
	}
	public void setPacketSeq(int packetSeq) {
		this.packetSeq = packetSeq;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		idd = in.readInt();
		msgInstance = in.readInt();
		messageType = in.readInt();
		messageLength = in.readInt();
		streamID = in.readInt();
		packetSeq = in.readInt();
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		out.writeInt(idd);
		out.writeInt(msgInstance);
		out.writeInt(messageType);
		out.writeInt(messageLength);
		out.writeInt(streamID);
		out.writeInt(packetSeq);
	}
	
	protected final long readUnsignedInt(DataInput in) throws IOException {
		return ((long)in.readInt()) & 0xFFFFFFFFL;
	}
	protected final void writeUnsignedInt(DataOutput out, long value) throws IOException {
		out.writeInt((int)(value & 0xFFFFFFFFL));
	}
	protected final String readNullTerminatedString(DataInput in) throws IOException {
		String result = "";
		byte b;
		while((b=in.readByte())!=0x0) {
			result += b;
		}
		return result;
	}
	protected final void writeNullTerminatedString(DataOutput out, String value) throws IOException {
		out.write(value.getBytes());
		out.write(0x0);//null termination
	}
	
	
}
