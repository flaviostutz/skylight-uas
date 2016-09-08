package br.skylight.commons.infra;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface SerializableState {

	public void readState(DataInputStream in) throws IOException;
	public void writeState(DataOutputStream out) throws IOException;
	
}
