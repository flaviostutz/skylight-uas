package br.skylight.commons.dli.skylight;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.VerificationResult;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class PIDConfiguration extends Message<PIDConfiguration> {

	private PIDControl pidControl;
	private float kp = 1;
	private float ki = 0;
	private float kd = 0;

	@Override
	public MessageType getMessageType() {
		return MessageType.M2010;
	}
	
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		pidControl = PIDControl.values()[in.readUnsignedByte()];
		kp = in.readFloat();
		ki = in.readFloat();
		kd = in.readFloat();
	}

	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeByte(pidControl.ordinal());
		out.writeFloat(kp);
		out.writeFloat(ki);
		out.writeFloat(kd);
	}

	public float getKd() {
		return kd;
	}

	public void setKd(float kd) {
		this.kd = kd;
	}

	public float getKi() {
		return ki;
	}

	public void setKi(float ki) {
		this.ki = ki;
	}

	public float getKp() {
		return kp;
	}

	public void setKp(float kp) {
		this.kp = kp;
	}

	public void setPIDControl(PIDControl pidControl) {
		this.pidControl = pidControl;
	}
	public PIDControl getPIDControl() {
		return pidControl;
	}
	
	public void validate(VerificationResult vr) {
		if(kp==0 && ki==0 && kd==0) {
			vr.addError("PID " + pidControl.getName() + ": All parameters are zero");
		}
		if(kp>1000 || ki>1000 || kd>1000) {
			vr.addWarning("PID " + pidControl.getName() + ": Some parameters are too high");
		}
	}

	@Override
	public void resetValues() {
		pidControl = null;
		kp = 0;
		ki = 0;
		kd = 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((pidControl == null) ? 0 : pidControl.hashCode());
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
		PIDConfiguration other = (PIDConfiguration) obj;
		if (pidControl == null) {
			if (other.pidControl != null)
				return false;
		} else if (!pidControl.equals(other.pidControl))
			return false;
		return true;
	}

	public void copyFrom(PIDConfiguration pc) {
		pidControl = pc.getPIDControl();
		kp = pc.getKp();
		ki = pc.getKi();
		kd = pc.getKd();
	}

	public void setup(float kp, float ki, float kd) {
		this.kp = kp;
		this.ki = ki;
		this.kd = kd;
	}

}
