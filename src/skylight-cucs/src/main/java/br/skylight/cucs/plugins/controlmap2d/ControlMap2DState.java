package br.skylight.cucs.plugins.controlmap2d;

import java.io.Serializable;

public class ControlMap2DState implements Serializable {

	private double lastLatitude = -15;
	private double lastLongitude = 45;
	private int lastZoomLevel = 10;
	private int lastReferenceAltitude = 4;
	
//	@Override
//	public void readState(DataInputStream in) throws IOException {
//		lastLatitude = in.readDouble();
//		lastLongitude = in.readDouble();
//		lastZoomLevel = in.readInt();
//		lastReferenceAltitude = in.readInt();
//	}
//
//	@Override
//	public void writeState(DataOutputStream out) throws IOException {
//		out.writeDouble(lastLatitude);
//		out.writeDouble(lastLongitude);
//		out.writeInt(lastZoomLevel);
//		out.writeInt(lastReferenceAltitude);
//	}

	public double getLastLatitude() {
		return lastLatitude;
	}

	public void setLastLatitude(double lastLatitude) {
		this.lastLatitude = lastLatitude;
	}

	public double getLastLongitude() {
		return lastLongitude;
	}

	public void setLastLongitude(double lastLongitude) {
		this.lastLongitude = lastLongitude;
	}

	public int getLastZoomLevel() {
		return lastZoomLevel;
	}

	public void setLastZoomLevel(int lastZoomLevel) {
		this.lastZoomLevel = lastZoomLevel;
	}

	public int getLastReferenceAltitude() {
		return lastReferenceAltitude;
	}

	public void setLastReferenceAltitude(int lastReferenceAltitude) {
		this.lastReferenceAltitude = lastReferenceAltitude;
	}
	
}
