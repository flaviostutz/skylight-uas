package br.skylight.commons.dli.skylight;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.VerificationResult;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class TakeoffLandingConfiguration extends Message<TakeoffLandingConfiguration> {

	private boolean validTakeOffRunway;
	private Runway takeoffRunway = new Runway();

	private boolean validLandingRunway;
	private Runway landingRunway = new Runway();
	
	public void validate(VerificationResult r) {
		if(validTakeOffRunway) {
			takeoffRunway.validate(r);
		}
		if(validLandingRunway) {
			landingRunway.validate(r);
		}
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		validTakeOffRunway = in.readBoolean();
		takeoffRunway.readState(in);
		validLandingRunway = in.readBoolean();
		landingRunway.readState(in);
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeBoolean(validTakeOffRunway);
		takeoffRunway.writeState(out);
		out.writeBoolean(validLandingRunway);
		landingRunway.writeState(out);
	}
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M2006;
	}
	
	public Runway getTakeoffRunway() {
		return takeoffRunway;
	}
	public void setTakeoffRunway(Runway takeoffRunway) {
		this.takeoffRunway = takeoffRunway;
	}
	public Runway getLandingRunway() {
		return landingRunway;
	}
	public void setLandingRunway(Runway landingRunway) {
		this.landingRunway = landingRunway;
	}
	
	public void setValidLandingRunway(boolean validLandingRunway) {
		this.validLandingRunway = validLandingRunway;
	}
	public void setValidTakeOffRunway(boolean validTakeOffRunway) {
		this.validTakeOffRunway = validTakeOffRunway;
	}
	public boolean isValidLandingRunway() {
		return validLandingRunway;
	}
	public boolean isValidTakeOffRunway() {
		return validTakeOffRunway;
	}

	@Override
	public void resetValues() {
		validTakeOffRunway = false;
		takeoffRunway.reset();
		validLandingRunway = false;
		landingRunway.reset();
	}
	
}
