package br.skylight.commons.dli.gateways;

import br.skylight.commons.dli.datalink.DataLinkControlCommand;
import br.skylight.commons.dli.datalink.DataLinkSetupMessage;
import br.skylight.commons.io.dataterminal.DataTerminal;

public class DummyDataTerminal extends DataTerminal {

	public DummyDataTerminal() {
		super();
	}

	private int sentMessagesCount = 0;
	private byte[] sendBuffer = new byte[1024];
	private int sendBufferLen;

	private byte[] readBuffer = new byte[1024];
	private int readBufferLen;

	@Override
	protected int readNextPacket(byte[] buffer) {
		if(readBuffer!=null) {
			try {
				System.arraycopy(readBuffer, 0, buffer, 0, readBufferLen);
				return readBufferLen;
			} finally {
				//clear buffer
				readBuffer = null;
				readBufferLen = 0;
			}
		} else {
			return 0;
		}
	}

	public int getSentMessagesCount() {
		return sentMessagesCount;
	}
	
	@Override
	public void sendNextPacket(byte[] data, int len) {
		System.arraycopy(data, 0, sendBuffer, 0, len);
		sendBufferLen = len;
		sentMessagesCount++;
	}

	public byte[] getSendBuffer() {
		return sendBuffer;
	}
	public int getSendBufferLen() {
		return sendBufferLen;
	}

	public void setReadBuffer(byte[] readBuffer) {
		this.readBuffer = readBuffer;
	}
	public void setReadBufferLen(int readBufferLen) {
		this.readBufferLen = readBufferLen;
	}
	public byte[] getReadBuffer() {
		return readBuffer;
	}
	public int getReadBufferLen() {
		return readBufferLen;
	}

	@Override
	public int getDownlinkStatus() {
		return (short)(75 + 15*Math.random());
	}
	@Override
	public int getUplinkStatus() {
		return (short)(75 + 15*Math.random());
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
