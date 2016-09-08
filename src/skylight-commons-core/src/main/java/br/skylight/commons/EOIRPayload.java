package br.skylight.commons;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.payload.EOIRConfigurationState;
import br.skylight.commons.dli.payload.EOIRLaserOperatingState;
import br.skylight.commons.dli.payload.EOIRLaserPayloadCommand;
import br.skylight.commons.dli.payload.PayloadBayStatus;
import br.skylight.commons.infra.SerializableState;

public class EOIRPayload implements SerializableState {

	private EOIRConfigurationState eoIrConfiguration = new EOIRConfigurationState();
	private float horizontalFOVAt1X = (float)Math.toRadians(45);
	private float positionXRelativeToAV;
	private float positionYRelativeToAV;
	private float positionZRelativeToAV;
	
	//used by cucs
	private EOIRLaserPayloadCommand eoIrLaserPayloadCommand;
	
	//transient
	private PayloadBayStatus payloadBayStatus;
	private EOIRLaserOperatingState operatingState;

	public float getHorizontalFOVAt1X() {
		return horizontalFOVAt1X;
	}
	public void setHorizontalFOVAt1X(float horizontalFOVAt1X) {
		this.horizontalFOVAt1X = horizontalFOVAt1X;
	}
	
	public EOIRConfigurationState getEoIrConfiguration() {
		return eoIrConfiguration;
	}
	public void setEoIrConfiguration(EOIRConfigurationState eoIrConfiguration) {
		this.eoIrConfiguration = eoIrConfiguration;
	}
	public EOIRLaserOperatingState getOperatingState() {
		return operatingState;
	}
	public void setOperatingState(EOIRLaserOperatingState operatingState) {
		this.operatingState = operatingState;
	}
	public PayloadBayStatus getPayloadBayStatus() {
		return payloadBayStatus;
	}
	public void setPayloadBayStatus(PayloadBayStatus payloadBayStatus) {
		this.payloadBayStatus = payloadBayStatus;
	}

	public float getPositionXRelativeToAV() {
		return positionXRelativeToAV;
	}
	public void setPositionXRelativeToAV(float positionXRelativeToAV) {
		this.positionXRelativeToAV = positionXRelativeToAV;
	}
	public float getPositionYRelativeToAV() {
		return positionYRelativeToAV;
	}
	public void setPositionYRelativeToAV(float positionYRelativeToAV) {
		this.positionYRelativeToAV = positionYRelativeToAV;
	}
	public float getPositionZRelativeToAV() {
		return positionZRelativeToAV;
	}
	public void setPositionZRelativeToAV(float positionZRelativeToAV) {
		this.positionZRelativeToAV = positionZRelativeToAV;
	}
	
	public EOIRLaserPayloadCommand getEoIrLaserPayloadCommand() {
		return eoIrLaserPayloadCommand;
	}
	public void setEoIrLaserPayloadCommand(EOIRLaserPayloadCommand eoIrLaserPayloadCommand) {
		this.eoIrLaserPayloadCommand = eoIrLaserPayloadCommand;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		eoIrConfiguration.readState(in);
		horizontalFOVAt1X = in.readFloat();
		positionXRelativeToAV = in.readFloat();
		positionYRelativeToAV = in.readFloat();
		positionZRelativeToAV = in.readFloat();
		//EoIrLaserPayloadCommand
		if(in.readBoolean()) {
			eoIrLaserPayloadCommand = new EOIRLaserPayloadCommand();
			eoIrLaserPayloadCommand.readState(in);
		}
	}
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		eoIrConfiguration.writeState(out);
		out.writeFloat(horizontalFOVAt1X);
		out.writeFloat(positionXRelativeToAV);
		out.writeFloat(positionYRelativeToAV);
		out.writeFloat(positionZRelativeToAV);
		out.writeBoolean(eoIrLaserPayloadCommand!=null);
		if(eoIrLaserPayloadCommand!=null) {
			eoIrLaserPayloadCommand.writeState(out);
		}
	}
	
}
