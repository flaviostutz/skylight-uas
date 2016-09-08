package br.skylight.commons.io;

import java.io.IOException;

public class ProcessSerialConnection extends SerialConnection {

	private Process process;
	
	public ProcessSerialConnection(Process process, SerialConnectionParams serialConnectionParams) throws IOException {
		super(process.getInputStream(), process.getOutputStream(), serialConnectionParams);
		this.process = process;
	}
	
	public Process getProcess() {
		return process;
	}
	public void setProcess(Process process) {
		this.process = process;
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		process.destroy();
	}
	
}
