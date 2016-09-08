package br.skylight.commons.dli.skylight;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class ServosStateMessage extends Message<ServosStateMessage> {

	private float aileronLeftState;
	private float aileronRightState;
	private float rudderState;
	private float elevatorState;
	private float throttleState;
	private float cameraPanState;
	private float cameraTiltState;
	private float genericServoState;

	@Override
	public MessageType getMessageType() {
		return MessageType.M2012;
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeFloat(aileronLeftState);
		out.writeFloat(aileronRightState);
		out.writeFloat(rudderState);
		out.writeFloat(elevatorState);
		out.writeFloat(throttleState);
		out.writeFloat(cameraPanState);
		out.writeFloat(cameraTiltState);
		out.writeFloat(genericServoState);
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		aileronLeftState = in.readFloat();
		aileronRightState = in.readFloat();
		rudderState = in.readFloat();
		elevatorState = in.readFloat();
		throttleState = in.readFloat();
		cameraPanState = in.readFloat();
		cameraTiltState = in.readFloat();
		genericServoState = in.readFloat();
	}
	
	@Override
	public void resetValues() {
		aileronLeftState = 0;
		aileronRightState = 0;
		rudderState = 0;
		elevatorState = 0;
		throttleState = 0;
		cameraPanState = 0;
		cameraTiltState = 0;
		genericServoState = 0;
	}
	
	public float getGenericServoState() {
		return genericServoState;
	}
	
	public void setGenericServoState(float genericServoState) {
		this.genericServoState = genericServoState;
	}
	
	public float getThrottleState() {
		return throttleState;
	}

	public void setThrottleState(float throttleState) {
		this.throttleState = throttleState;
	}

	public float getAileronLeftState() {
		return aileronLeftState;
	}
	public void setAileronLeftState(float aileronLeftState) {
		this.aileronLeftState = aileronLeftState;
	}
	public float getAileronRightState() {
		return aileronRightState;
	}
	public void setAileronRightState(float aileronRightState) {
		this.aileronRightState = aileronRightState;
	}

	public float getElevatorState() {
		return elevatorState;
	}

	public void setElevatorState(float elevatorState) {
		this.elevatorState = elevatorState;
	}

	public float getRudderState() {
		return rudderState;
	}

	public void setRudderState(float rudderState) {
		this.rudderState = rudderState;
	}
	
	public float getCameraPanState() {
		return cameraPanState;
	}
	public float getCameraTiltState() {
		return cameraTiltState;
	}
	public void setCameraPanState(float cameraPanState) {
		this.cameraPanState = cameraPanState;
	}
	public void setCameraTiltState(float cameraTiltState) {
		this.cameraTiltState = cameraTiltState;
	}

}
