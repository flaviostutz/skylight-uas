package br.skylight.commons.dli.vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.SpeedType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class AirAndGroundRelativeStates extends Message<AirAndGroundRelativeStates> {

	private float angleOfAttack;
	private float angleOfSideslip;
	private float trueAirspeed;
	private float indicatedAirspeed;
	private float outsideAirTemp;
	private float uWind;
	private float vWind;
	private float altimeterSetting;
	private float barometricAltitude;
	private float barometricAltitudeRate;
	private float pressureAltitude;
	private float aglAltitude;
	private float wgs84Altitude;
	private float uGround;
	private float vGround;
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M102;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		angleOfAttack = in.readFloat();
		angleOfSideslip = in.readFloat();
		trueAirspeed = in.readFloat();
		indicatedAirspeed = in.readFloat();
		outsideAirTemp = in.readFloat();
		uWind = in.readFloat();
		vWind = in.readFloat();
		altimeterSetting = in.readFloat();
		barometricAltitude = in.readFloat();
		barometricAltitudeRate = in.readFloat();
		pressureAltitude = in.readFloat();
		aglAltitude = in.readFloat();
		wgs84Altitude = in.readFloat();
		uGround = in.readFloat();
		vGround = in.readFloat();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeFloat(angleOfAttack);
		out.writeFloat(angleOfSideslip);
		out.writeFloat(trueAirspeed);
		out.writeFloat(indicatedAirspeed);
		out.writeFloat(outsideAirTemp);
		out.writeFloat(uWind);
		out.writeFloat(vWind);
		out.writeFloat(altimeterSetting);
		out.writeFloat(barometricAltitude);
		out.writeFloat(barometricAltitudeRate);
		out.writeFloat(pressureAltitude);
		out.writeFloat(aglAltitude);
		out.writeFloat(wgs84Altitude);
		out.writeFloat(uGround);
		out.writeFloat(vGround);
	}

	@Override
	public void resetValues() {
		angleOfAttack = 0;
		angleOfSideslip = 0;
		trueAirspeed = 0;
		indicatedAirspeed = 0;
		outsideAirTemp = 0;
		uWind = 0;
		vWind = 0;
		altimeterSetting = 0;
		barometricAltitude = 0;
		barometricAltitudeRate = 0;
		pressureAltitude = 0;
		aglAltitude = 0;
		wgs84Altitude = 0;
		uGround = 0;
		vGround = 0;
	}

	public float getAngleOfAttack() {
		return angleOfAttack;
	}

	public void setAngleOfAttack(float angleOfAttack) {
		this.angleOfAttack = angleOfAttack;
	}

	public float getAngleOfSideslip() {
		return angleOfSideslip;
	}

	public void setAngleOfSideslip(float angleOfSideslip) {
		this.angleOfSideslip = angleOfSideslip;
	}

	public float getTrueAirspeed() {
		return trueAirspeed;
	}

	public void setTrueAirspeed(float trueAirspeed) {
		this.trueAirspeed = trueAirspeed;
	}

	public float getIndicatedAirspeed() {
		return indicatedAirspeed;
	}

	public void setIndicatedAirspeed(float indicatedAirspeed) {
		this.indicatedAirspeed = indicatedAirspeed;
	}

	public float getOutsideAirTemp() {
		return outsideAirTemp;
	}

	public void setOutsideAirTemp(float outsideAirTemp) {
		this.outsideAirTemp = outsideAirTemp;
	}

	public float getUWind() {
		return uWind;
	}

	public void setUWind(float wind) {
		uWind = wind;
	}

	public float getVWind() {
		return vWind;
	}

	public void setVWind(float wind) {
		vWind = wind;
	}

	public float getAltimeterSetting() {
		return altimeterSetting;
	}

	public void setAltimeterSetting(float altimeterSetting) {
		this.altimeterSetting = altimeterSetting;
	}

	public float getBarometricAltitude() {
		return barometricAltitude;
	}

	public void setBarometricAltitude(float barometricAltitude) {
		this.barometricAltitude = barometricAltitude;
	}

	public float getBarometricAltitudeRate() {
		return barometricAltitudeRate;
	}

	public void setBarometricAltitudeRate(float barometricAltitudeRate) {
		this.barometricAltitudeRate = barometricAltitudeRate;
	}

	public float getPressureAltitude() {
		return pressureAltitude;
	}

	public void setPressureAltitude(float pressureAltitude) {
		this.pressureAltitude = pressureAltitude;
	}

	public float getAglAltitude() {
		return aglAltitude;
	}

	public void setAglAltitude(float aglAltitude) {
		this.aglAltitude = aglAltitude;
	}

	public float getWgs84Altitude() {
		return wgs84Altitude;
	}

	public void setWgs84Altitude(float wgs84Altitude) {
		this.wgs84Altitude = wgs84Altitude;
	}

	public float getUGround() {
		return uGround;
	}

	public void setUGround(float ground) {
		uGround = ground;
	}

	public float getVGround() {
		return vGround;
	}

	public void setVGround(float ground) {
		vGround = ground;
	}

	public float getAltitude(AltitudeType altitudeType) {
		if(altitudeType.equals(AltitudeType.AGL)) {
			return getAglAltitude();
		} else if(altitudeType.equals(AltitudeType.BARO)) {
			return getBarometricAltitude();
		} else if(altitudeType.equals(AltitudeType.WGS84)) {
			return getWgs84Altitude();
		} else if(altitudeType.equals(AltitudeType.PRESSURE)) {
			return getPressureAltitude();
		} else {
			System.out.println("getAltitude(..) will return NaN");
			return Float.NaN;
		}
	}

	public float getSpeed(SpeedType speedType) {
		if(speedType.equals(SpeedType.INDICATED_AIRSPEED)) {
			return getIndicatedAirspeed();
		} else if(speedType.equals(SpeedType.TRUE_AIRSPEED)) {
			return getTrueAirspeed();
		} else {
			System.out.println("getSpeed(..) will return NaN");
			return Float.NaN;
		}
	}
	
}
