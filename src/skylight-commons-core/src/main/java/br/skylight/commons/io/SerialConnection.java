package br.skylight.commons.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialConnection {

	private InputStream inputStream;
	private OutputStream outputStream;
	private boolean connected;
	private SerialConnectionParams serialConnectionParams;
	
	public SerialConnection(InputStream is, OutputStream os, SerialConnectionParams serialConnectionParams) throws IOException {
		inputStream = new BufferedInputStream(is);
		outputStream = new BufferedOutputStream(os);
//		inputStream = is;
//		outputStream = os;
		this.serialConnectionParams = serialConnectionParams;
		this.connected = true;
	}

	public void close() throws IOException {
		inputStream.close();
		outputStream.close();
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public InputStream getInputStream() {
		return inputStream;
	}
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	public OutputStream getOutputStream() {
		return outputStream;
	}
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	public SerialConnectionParams getSerialConnectionParams() {
		return serialConnectionParams;
	}
	
	@Override
	public String toString() {
		return serialConnectionParams.toString();
	}
}
