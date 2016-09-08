package br.skylight.commons;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.SerializableState;


public class Placemark implements SerializableState {

	private String label = "";
	private Coordinates point = new Coordinates();
	
	public String getLabel() {
		return label;
	}
	public Coordinates getPoint() {
		return point;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public void setPoint(Coordinates point) {
		this.point = point;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		label = in.readUTF();
		point = IOHelper.readState(Coordinates.class, in);
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		out.writeUTF(label);
		IOHelper.writeState(point, out);
	}
	
}
