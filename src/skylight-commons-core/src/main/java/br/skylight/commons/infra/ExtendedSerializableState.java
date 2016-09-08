package br.skylight.commons.infra;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface ExtendedSerializableState extends SerializableState {

	public void readStateExtended(DataInputStream in) throws IOException;
	public void writeStateExtended(DataOutputStream out) throws IOException;
	
}
