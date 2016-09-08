package br.skylight.commons.dli.skylight;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class SoftwarePartReport extends Message<SoftwareStatus> {

	private String name = "";
	private float averageFrequency;
	private long timeSinceLastStepMillis;
	private boolean active;
	private boolean alert;
	private boolean timeout;
	private int exceptionCount;
	private String stackFragment = "";
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M2018;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		name = in.readUTF();
		averageFrequency = in.readFloat();
		timeSinceLastStepMillis = in.readLong();
		active = in.readBoolean();
		alert = in.readBoolean();
		timeout = in.readBoolean();
		exceptionCount = in.readInt();
		stackFragment = in.readUTF();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeUTF(name);
		out.writeFloat(averageFrequency);
		out.writeLong(timeSinceLastStepMillis);
		out.writeBoolean(active);
		out.writeBoolean(alert);
		out.writeBoolean(timeout);
		out.writeInt(exceptionCount);
		out.writeUTF(stackFragment);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getAverageFrequency() {
		return averageFrequency;
	}

	public void setAverageFrequency(float averageFrequency) {
		this.averageFrequency = averageFrequency;
	}

	public long getTimeSinceLastStepMillis() {
		return timeSinceLastStepMillis;
	}

	public void setTimeSinceLastStepMillis(long timeSinceLastStepMillis) {
		this.timeSinceLastStepMillis = timeSinceLastStepMillis;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public void setAlert(boolean alert) {
		this.alert = alert;
	}
	public int getExceptionCount() {
		return exceptionCount;
	}
	public void setTimeout(boolean timeout) {
		this.timeout = timeout;
	}
	public boolean isAlert() {
		return alert;
	}
	public boolean isTimeout() {
		return timeout;
	}

	public void setExceptionCount(int exceptionCount) {
		this.exceptionCount = exceptionCount;
	}
	
	public String getStackFragment() {
		return stackFragment;
	}
	public void setStackFragment(String stackFragment) {
		this.stackFragment = stackFragment;
	}

	@Override
	public void resetValues() {
		name = "";
		averageFrequency = 0;
		timeSinceLastStepMillis = 0;
		active = false;
		alert = false;
		timeout = false;
		exceptionCount = 0;
		stackFragment = "";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		SoftwarePartReport other = (SoftwarePartReport) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
