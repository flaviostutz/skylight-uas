package br.skylight.commons;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.infra.SerializableState;

public class Region implements SerializableState {

	private ArrayList<Coordinates> points = new ArrayList<Coordinates>();

	//transient
	private double[] insideTestX = new double[0];
	private double[] insideTestY = new double[0];
	
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

	public boolean isPointInside(Coordinates waypointPosition) {
		if(!isValidArea()) {
			return false;
		}
		// resize array only if size has changed
		if (points.size() != insideTestX.length) {
			insideTestX = new double[points.size()];
			insideTestY = new double[points.size()];
		}

		//prepare array for test
		int c = 0;
		for (Coordinates gp : points) {
			insideTestX[c] = gp.getLatitude();
			insideTestY[c] = gp.getLongitude();
			c++;
		}
		
		return MathHelper.isPointInsidePolygon(
				insideTestX, insideTestY, 
				waypointPosition.getLatitude(), 
				waypointPosition.getLongitude());
	}

	public boolean isValidArea() {
		return points.size()>=3;
	}
	
	public void addPoint(Coordinates coordinates) {
		points.add(coordinates);
	}

	public void addPoint(Coordinates coordinates, int pointIndex) {
		points.add(pointIndex, coordinates);
	}
	
	public void clear() {
		points.clear();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((points == null) ? 0 : points.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Region other = (Region) obj;
		if (points == null) {
			if (other.points != null)
				return false;
		} else {
			if(points.size()!=other.points.size()) {
				return false;
			}
			for(int i=0; i<points.size(); i++) {
				if(!points.get(i).equals(other.points.get(i)))
					return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		String r = "";
		for (Coordinates p : points) {
			r += p.getLatitude() + ";" + p.getLongitude() + "\n";
		}
		return r;
	}
	
	public void parseString(String string) {
		points.clear();
		string = string.replaceAll(" ", "");
		if(string.length()>0) {
			String[] sl = string.split("\\n");
			for (String l : sl) {
				String[] cs = l.split(";");
				points.add(new Coordinates(Double.parseDouble(cs[0]), Double.parseDouble(cs[1]), 0));
			}
		}
	}

}
