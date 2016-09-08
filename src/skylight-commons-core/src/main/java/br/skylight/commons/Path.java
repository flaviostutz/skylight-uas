package br.skylight.commons;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.SerializableState;

public class Path implements SerializableState {

	private ArrayList<Coordinates> points = new ArrayList<Coordinates>();

	public ArrayList<Coordinates> getPoints() {
		return points;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		IOHelper.readArrayList(in, Coordinates.class, points);
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		IOHelper.writeArrayList(out, points);
	}
	
	public void addPoint(int index, Coordinates coordinates) {
		points.add(index, coordinates);
	}
	
}
