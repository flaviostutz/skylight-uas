package br.skylight.commons;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.infra.MathHelper;

public class ServoConfiguration extends Message<ServoConfiguration> {

	private Servo servo;
	private int minUs;
	private int maxUs;
	private int trimUs;
	private boolean inverse;
	private float rangeAngle;

	//transient
	private float lastValue;
	
	public ServoConfiguration() {
		resetValues();
	}
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M2009;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		servo = Servo.values()[in.readUnsignedByte()];
		minUs = in.readInt();
		maxUs = in.readInt();
		trimUs = in.readInt();
		inverse = in.readBoolean();
		rangeAngle = in.readFloat();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeByte(servo.ordinal());
		out.writeInt(minUs);
		out.writeInt(maxUs);
		out.writeInt(trimUs);
		out.writeBoolean(inverse);
		out.writeFloat(rangeAngle);
	}

	public Servo getServo() {
		return servo;
	}
	
	public void setServo(Servo servo) {
		this.servo = servo;
	}
	
	public float getLastValue() {
		return lastValue;
	}
	public void setLastValue(float lastValue) {
		this.lastValue = lastValue;
	}

	/**
	 * Gets a servo time (us) from an internal setpoint representation.
	 * -127 will be the min position, 127 will be the max position, added by trim
	 * @param setpoint A value between -127 and 127
	 */
	public int getServoTimeForZeroCenteredSetpoint(float setpoint) {
		return getServoTimeForSetpoint(setpoint+127F);
	}
	/**
	 * Gets a servo time (us) from an internal setpoint representation.
	 * 0 will be the min position, 254 will be the max position, added by trim
	 * @param setpoint A value between 0 and 254
	 */
	public int getServoTimeForSetpoint(float setpoint) {
		if(inverse) {
			setpoint = 254F-setpoint;
		}
//		if(servo.equals(Servo.THROTTLE)) {
//			return (int)MathHelper.clamp(minUs + trimUs + ((maxUs-minUs-trimUs)*(setpoint/254.0)), minUs, maxUs);
//		} else {
		return (int)MathHelper.clamp(minUs + (trimUs>0?trimUs:0) + ((maxUs-minUs-Math.abs(trimUs))*(setpoint/254.0)), minUs, maxUs);
//		}
	}
	
	public void validate(VerificationResult vr) {
		vr.assertRange(minUs, 0, 1000, 3000, 99999, "us for min position");
		vr.assertRange(maxUs, 0, 1000, 3000, 99999, "us for max position");
		if(minUs>=maxUs) {
			vr.addError("Max �s must be greater than min");
		}
		vr.assertRange(trimUs, -9999, -500, 500, 9999, "Trim �s");
		vr.assertRange((float)rangeAngle, 0F, (float)Math.toRadians(45), (float)Math.toRadians(180), (float)Math.toRadians(99999), "Range angle");
	}

	@Override
	public void resetValues() {
		servo = null;
		minUs = 1300;
		maxUs = 1700;
		trimUs = 0;
		rangeAngle = (float)Math.toRadians(90);
		inverse = false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((servo == null) ? 0 : servo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServoConfiguration other = (ServoConfiguration) obj;
		if (servo == null) {
			if (other.servo != null)
				return false;
		} else if (!servo.equals(other.servo))
			return false;
		return true;
	}

	public void copyFrom(ServoConfiguration sc) {
		servo = sc.getServo();
		minUs = sc.minUs;
		maxUs = sc.maxUs;
		trimUs = sc.trimUs;
		rangeAngle = sc.rangeAngle;
		inverse = sc.inverse;
	}

	public int getMinUs() {
		return minUs;
	}

	public void setMinUs(int minUs) {
		this.minUs = minUs;
	}

	public int getMaxUs() {
		return maxUs;
	}

	public void setMaxUs(int maxUs) {
		this.maxUs = maxUs;
	}

	public int getTrimUs() {
		return trimUs;
	}

	public void setTrimUs(int trimUs) {
		this.trimUs = trimUs;
	}

	public float getRangeAngle() {
		return rangeAngle;
	}

	public void setRangeAngle(float rangeAngle) {
		this.rangeAngle = rangeAngle;
	}
	
	public boolean isInverse() {
		return inverse;
	}
	public void setInverse(boolean inverse) {
		this.inverse = inverse;
	}
	
}
