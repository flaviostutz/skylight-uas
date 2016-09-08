package br.skylight.commons.dli.mission;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.TransferStatus;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class MissionUploadDownloadStatus extends Message<MissionUploadDownloadStatus> {

	private TransferStatus status;//u1
	private int percentComplete;//u1

	@Override
	public MessageType getMessageType() {
		return MessageType.M900;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		status = TransferStatus.values()[in.readUnsignedByte()];
		percentComplete = in.readUnsignedByte();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeByte(status.ordinal());
		out.writeByte(percentComplete);
	}

	@Override
	public void resetValues() {
		status = TransferStatus.values()[0];
		percentComplete = (byte)0;
	}

	public int getPercentComplete() {
		return percentComplete;
	}
	public void setPercentComplete(int percentComplete) {
		this.percentComplete = percentComplete;
	}
	public TransferStatus getStatus() {
		return status;
	}
	public void setStatus(TransferStatus status) {
		this.status = status;
	}
	
	

}