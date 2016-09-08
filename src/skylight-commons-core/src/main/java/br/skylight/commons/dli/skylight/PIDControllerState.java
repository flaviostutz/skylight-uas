package br.skylight.commons.dli.skylight;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class PIDControllerState extends Message<PIDControllerState> {

	private PIDControl pidControl;

	private float setpointValue;
	private float feedbackValue;
	private float outputValue;
	
	private float proportionalValue;
	private float integralValue;
	private float diferentialValue;
	
	private boolean active;
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M2011;
	}

	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public PIDControl getPIDControl() {
		return pidControl;
	}

	public void setPIDControl(PIDControl pidControl) {
		this.pidControl = pidControl;
	}

	public float getSetpointValue() {
		return setpointValue;
	}

	public void setSetpointValue(float setpointValue) {
		this.setpointValue = setpointValue;
	}

	public float getFeedbackValue() {
		return feedbackValue;
	}

	public void setFeedbackValue(float feedbackValue) {
		this.feedbackValue = feedbackValue;
	}

	public float getOutputValue() {
		return outputValue;
	}

	public void setOutputValue(float outputValue) {
		this.outputValue = outputValue;
	}

	public float getProportionalValue() {
		return proportionalValue;
	}

	public void setProportionalValue(float proportionalValue) {
		this.proportionalValue = proportionalValue;
	}

	public float getIntegralValue() {
		return integralValue;
	}

	public void setIntegralValue(float integralValue) {
		this.integralValue = integralValue;
	}

	public float getDiferentialValue() {
		return diferentialValue;
	}

	public void setDiferentialValue(float diferentialValue) {
		this.diferentialValue = diferentialValue;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		pidControl = PIDControl.values()[in.readUnsignedByte()];

		setpointValue = in.readFloat();
		feedbackValue = in.readFloat();
		outputValue = in.readFloat();
		
		//TODO Minimize PIDs telemetry overhead by enabling the selection of individual PIDs that will be reported
//		proportionalValue = in.readFloat();
//		integralValue = in.readFloat();
//		diferentialValue = in.readFloat();
		
		active = in.readBoolean();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeByte(pidControl.ordinal());

		out.writeFloat(setpointValue);
		out.writeFloat(feedbackValue);
		out.writeFloat(outputValue);
		
//		out.writeFloat(proportionalValue);
//		out.writeFloat(integralValue);
//		out.writeFloat(diferentialValue);
		
		out.writeBoolean(active);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pidControl == null) ? 0 : pidControl.hashCode());
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
		PIDControllerState other = (PIDControllerState) obj;
		if (pidControl == null) {
			if (other.pidControl != null)
				return false;
		} else if (!pidControl.equals(other.pidControl))
			return false;
		return true;
	}

	@Override
	public void resetValues() {
		setpointValue = 0;
		feedbackValue = 0;
		outputValue = 0;
		
		proportionalValue = 0;
		integralValue = 0;
		diferentialValue = 0;
		
		active = false;
	}

}
