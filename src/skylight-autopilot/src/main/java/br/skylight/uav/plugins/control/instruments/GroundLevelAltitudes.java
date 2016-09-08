package br.skylight.uav.plugins.control.instruments;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.Coordinates;
import br.skylight.commons.infra.SerializableState;

public class GroundLevelAltitudes implements SerializableState {

	private float altitudeGpsWGS84;
	private float altitudeGpsMSL;
	private float altitudeBarometric;//barometric pressure calculated with measured at ground level reference
	private float altitudePressure;//barometric pressure calculated with standard 101325 Pa reference
	private Coordinates groundLevelSetupPosition = new Coordinates(0,0,0);

	public float getAltitudeGpsWGS84() {
		return altitudeGpsWGS84;
	}

	public void setAltitudeGpsWGS84(float altitudeGpsWGS84) {
		this.altitudeGpsWGS84 = altitudeGpsWGS84;
	}

	public float getAltitudeGpsMSL() {
		return altitudeGpsMSL;
	}

	public void setAltitudeGpsMSL(float altitudeGpsMSL) {
		this.altitudeGpsMSL = altitudeGpsMSL;
	}

	public float getAltitudeBarometric() {
		return altitudeBarometric;
	}

	public void setAltitudeBarometric(float altitudeBarometric) {
		this.altitudeBarometric = altitudeBarometric;
	}

	public float getAltitudePressure() {
		return altitudePressure;
	}

	public void setAltitudePressure(float altitudePressure) {
		this.altitudePressure = altitudePressure;
	}
	
	public Coordinates getGroundLevelSetupPosition() {
		return groundLevelSetupPosition;
	}
	public void setGroundLevelSetupPosition(Coordinates setupPosition) {
		this.groundLevelSetupPosition = setupPosition;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		altitudeGpsWGS84 = in.readFloat();
		altitudeGpsMSL = in.readFloat();
		altitudeBarometric = in.readFloat();
		altitudePressure = in.readFloat();
		groundLevelSetupPosition.readState(in);
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		out.writeFloat(altitudeGpsWGS84);
		out.writeFloat(altitudeGpsMSL);
		out.writeFloat(altitudeBarometric);
		out.writeFloat(altitudePressure);
		groundLevelSetupPosition.writeState(out);
	}

}
