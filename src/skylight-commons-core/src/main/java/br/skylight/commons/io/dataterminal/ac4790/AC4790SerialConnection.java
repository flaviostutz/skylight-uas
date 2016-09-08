package br.skylight.commons.io.dataterminal.ac4790;

import java.io.IOException;
import java.io.InputStream;

import br.skylight.commons.io.SerialConnection;

/**
 * Wrapper for serial connection
 * Both input and output streams are replaced by mechanisms that make
 * input/output filtering of all data so that it is possible
 * to control modem in the same line as data is transferred 
 */
public class AC4790SerialConnection {

	private AC4790OutputStream aos;
	private SerialConnection serialConnection;
	
	public AC4790SerialConnection(SerialConnection serialConnection, AC4790StreamFilterListener listener) throws IOException {
		//FIXME: IMPLEMENT FILE WITH SENSITIVITY
		this.aos = new AC4790OutputStream(serialConnection.getOutputStream(), serialConnection.getInputStream(), 0xB7, listener);//0xB7 for ground station modem and 0xBC for vehicle modem
		this.serialConnection = serialConnection;
	}
	
	public InputStream getInputStream() {
		return aos.getFilteredInputStream();
	}
	
	public AC4790OutputStream getOutputStream() {
		return aos;
	}
	
	public void close() throws IOException {
		serialConnection.close();
	}

}
