package br.skylight.commons.io.dataterminal;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import br.skylight.commons.dli.datalink.DataLinkControlCommand;
import br.skylight.commons.dli.datalink.DataLinkSetupMessage;
import br.skylight.commons.infra.SyncCondition;

public class DirectDataTerminal extends DataTerminal {

	private SyncCondition pendingPacketToBeRead;
	
	private byte[] nextPacketToBeReadBytes;
	private int nextPacketToBeReadSize;
	
	private byte[] lastPacketSentBytes;
	private int lastPacketSentSize;
	
	private double currentTime;
	
	public DirectDataTerminal() {
		super();
		pendingPacketToBeRead = new SyncCondition("pending packet to be read");
		pendingPacketToBeRead.notifyConditionNotMet();
	}
	
	public void setNextPacketToBeRead(byte[] nextPacketToBeReadBytes, int nextPacketToBeReadSize, double currentTimestamp, boolean waitForPacketToBeSent) {
		this.nextPacketToBeReadBytes = nextPacketToBeReadBytes;
		this.nextPacketToBeReadSize = nextPacketToBeReadSize;
		this.currentTime = currentTimestamp;
		pendingPacketToBeRead.notifyConditionMet();
		if(waitForPacketToBeSent) {
			try {
				pendingPacketToBeRead.waitForConditionNotMet(10000);
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected int readNextPacket(byte[] buffer) throws IOException {
		try {
			pendingPacketToBeRead.waitForCondition(999999);
			//copy bytes
			for (int i=0; i<nextPacketToBeReadSize; i++) {
				buffer[i] = nextPacketToBeReadBytes[i];
			}
			return nextPacketToBeReadSize;
		} catch (TimeoutException e) {
			e.printStackTrace();
			return 0;
		} finally {
			pendingPacketToBeRead.notifyConditionNotMet();
		}
	}

	@Override
	protected void sendNextPacket(byte[] data, int len) throws IOException {
		this.lastPacketSentBytes = data;
		this.lastPacketSentSize = len;
	}

	public byte[] getLastPacketSentBytes() {
		return lastPacketSentBytes;
	}
	
	public int getLastPacketSentSize() {
		return lastPacketSentSize;
	}
	
	@Override
	public double getCurrentTime() {
		return currentTime;
	}
	
	@Override
	public void controlDataLink(DataLinkControlCommand cm) {
		populateStatusReportWithControlCommand(cm);
	}

	@Override
	public void setupDataLink(DataLinkSetupMessage sm) {
		populateStatusReportWithSetupMessage(sm);
	}

}
