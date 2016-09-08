package br.skylight.commons.dli.vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.annotations.MessageField;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class InertialStates extends Message<InertialStates> {

	@MessageField(number=4)
	public double latitude;
	@MessageField(number=5)
	public double longitude;
	@MessageField(number=6)
	public float altitude;
	@MessageField(number=7)
	public AltitudeType altitudeType;//u1
	@MessageField(number=8)
	public float uSpeed;
	@MessageField(number=9)
	public float vSpeed;
	@MessageField(number=10)
	public float wSpeed;
	@MessageField(number=11)
	public float uAccel;
	@MessageField(number=12)
	public float vAccel;
	@MessageField(number=13)
	public float wAccel;
	@MessageField(number=14)
	public float phi;
	@MessageField(number=15)
	public float theta;
	@MessageField(number=16)
	public float psi;
	@MessageField(number=17)
	public float phiDot;
	@MessageField(number=18)
	public float thetaDot;
	@MessageField(number=19)
	public float psiDot;
	@MessageField(number=20)
	public float magneticVariation;
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M101;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		latitude = in.readDouble();
		longitude = in.readDouble();
		altitude = in.readFloat();
		altitudeType = AltitudeType.values()[in.readUnsignedByte()];
		uSpeed = in.readFloat();
		vSpeed = in.readFloat();
		wSpeed = in.readFloat();
		uAccel = in.readFloat();
		vAccel = in.readFloat();
		wAccel = in.readFloat();
		phi = in.readFloat();
		theta = in.readFloat();
		psi = in.readFloat();
		phiDot = in.readFloat();
		thetaDot = in.readFloat();
		psiDot = in.readFloat();
		magneticVariation = in.readFloat();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeDouble(latitude);
		out.writeDouble(longitude);
		out.writeFloat(altitude);
		out.writeByte(altitudeType.ordinal());
		out.writeFloat(uSpeed);
		out.writeFloat(vSpeed);
		out.writeFloat(wSpeed);
		out.writeFloat(uAccel);
		out.writeFloat(vAccel);
		out.writeFloat(wAccel);
		out.writeFloat(phi);
		out.writeFloat(theta);
		out.writeFloat(psi);
		out.writeFloat(phiDot);
		out.writeFloat(thetaDot);
		out.writeFloat(psiDot);
		out.writeFloat(magneticVariation);
	}

	@Override
	public void resetValues() {
		latitude = 0;
		longitude = 0;
		altitude = 0;
		altitudeType = AltitudeType.values()[0];
		uSpeed = 0;
		vSpeed = 0;
		wSpeed = 0;
		uAccel = 0;
		vAccel = 0;
		wAccel = 0;
		phi = 0;
		theta = 0;
		psi = 0;
		phiDot = 0;
		thetaDot = 0;
		psiDot = 0;
		magneticVariation = 0;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public float getAltitude() {
		return altitude;
	}

	public void setAltitude(float altitude) {
		this.altitude = altitude;
	}

	public AltitudeType getAltitudeType() {
		return altitudeType;
	}

	public void setAltitudeType(AltitudeType altitudeType) {
		this.altitudeType = altitudeType;
	}

	public float getUSpeed() {
		return uSpeed;
	}

	public void setUSpeed(float speed) {
		uSpeed = speed;
	}

	public float getVSpeed() {
		return vSpeed;
	}

	public void setVSpeed(float speed) {
		vSpeed = speed;
	}

	public float getWSpeed() {
		return wSpeed;
	}

	public void setWSpeed(float speed) {
		wSpeed = speed;
	}

	public float getUAccel() {
		return uAccel;
	}

	public void setUAccel(float accel) {
		uAccel = accel;
	}

	public float getVAccel() {
		return vAccel;
	}

	public void setVAccel(float accel) {
		vAccel = accel;
	}

	public float getWAccel() {
		return wAccel;
	}

	public void setWAccel(float accel) {
		wAccel = accel;
	}

	/**
	 * Roll angle
	 * @return
	 */
	public float getPhi() {
		return phi;
	}

	public void setPhi(float phi) {
		this.phi = phi;
	}

	/**
	 * Pitch angle
	 * @return
	 */
	public float getTheta() {
		return theta;
	}

	public void setTheta(float theta) {
		this.theta = theta;
	}

	/**
	 * Yaw/heading angle
	 * @return
	 */
	public float getPsi() {
		return psi;
	}

	public void setPsi(float psi) {
		this.psi = psi;
	}

	public float getPhiDot() {
		return phiDot;
	}

	public void setPhiDot(float phiDot) {
		this.phiDot = phiDot;
	}

	public float getThetaDot() {
		return thetaDot;
	}

	public void setThetaDot(float thetaDot) {
		this.thetaDot = thetaDot;
	}

	public float getPsiDot() {
		return psiDot;
	}

	public void setPsiDot(float psiDot) {
		this.psiDot = psiDot;
	}

	public float getMagneticVariation() {
		return magneticVariation;
	}

	public void setMagneticVariation(float magneticVariation) {
		this.magneticVariation = magneticVariation;
	}
	
	public double getGroundSpeed() {
		return Math.sqrt((uSpeed*uSpeed) + (vSpeed*vSpeed));
	}

}
