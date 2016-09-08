package br.skylight.commons.io.dataterminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;

import br.skylight.commons.dli.datalink.DataLinkControlCommand;
import br.skylight.commons.dli.datalink.DataLinkSetupMessage;
import br.skylight.commons.dli.enums.DataTerminalType;

public class MemoryDataTerminal extends StreamDataTerminal {

	private MemoryDataTerminal pair;

	private PipedInputStream2 pis = new PipedInputStream2(4096);
	private PipedOutputStream pos;

	public MemoryDataTerminal() {
		super();
	}

	public MemoryDataTerminal(DataTerminalType dataTerminalType, int dataLinkId) {
		super(dataTerminalType, dataLinkId, false, false);
		try {
			pos = new PipedOutputStream(pis);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getDownlinkStatus() {
		return 100;
	}
	
	@Override
	public int getUplinkStatus() {
		return 100;
	}
	
	@Override
	protected InputStream getInputStream() {
		return pis;
	}
	
	@Override
	protected OutputStream getOutputStream() {
		return pair.pos;
	}
	
	public void connectTo(MemoryDataTerminal pair) {
		this.pair = pair;
		pair.pair = this;
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
