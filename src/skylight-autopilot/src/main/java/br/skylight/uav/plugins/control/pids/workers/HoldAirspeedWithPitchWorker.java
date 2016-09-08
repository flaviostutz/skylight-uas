package br.skylight.uav.plugins.control.pids.workers;

import br.skylight.commons.dli.skylight.PIDConfiguration;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.uav.plugins.control.pids.PIDController;
import br.skylight.uav.plugins.control.pids.PIDControllers;
import br.skylight.uav.plugins.control.pids.SkylightPIDWorker;

public class HoldAirspeedWithPitchWorker extends SkylightPIDWorker {

	public HoldAirspeedWithPitchWorker() {
		super(createPIDController());
	}
	
	private static PIDController createPIDController() {
		PIDController p = new PIDController(PIDControl.HOLD_IAS_WITH_PITCH);
		p.setRestrictOutputFrom(-(float)Math.toRadians(15));
		p.setRestrictOutputTo((float)Math.toRadians(15));
		p.setProportionalGain(1F);
		p.setInverseOutput(true);
		p.setFeedbackToOutputScale(1, (float)Math.toRadians(1));
		return p;
	}

	@Override
	public void onActivate() throws Exception {
		pidControllers.getPIDWorker(PIDControl.HOLD_IAS_WITH_THROTTLE).deactivate();
		pidControllers.getPIDWorker(PIDControl.HOLD_ALTITUDE_WITH_PITCH).deactivate();
	}
	
	@Override
	public float getFeedbackValue() {
		return advancedInstrumentsService.getIAS();
	}
	
	@Override
	public void step(float pitchValue, PIDControllers pidControllers) {
		pidControllers.holdSetpoint(PIDControl.HOLD_PITCH_WITH_ELEV_RUDDER, pitchValue);
	}
	
	@Override
	public void onDeactivate() throws Exception {
//		pidControllers.unholdSetpoint(PIDControl.HOLD_PITCH_WITH_ELEVATOR);
	}

	@Override
	protected void setupPIDController(SkylightVehicleConfigurationMessage ac, PIDConfiguration pc) {
		getPIDController().setIntegralGain(pc.getKi(), minPitch, maxPitch);
		getPIDController().setRestrictOutputFrom(minPitch);
		getPIDController().setRestrictOutputTo(maxPitch);
		getPIDController().setRestrictSetpointFrom(minKtias);
		getPIDController().setRestrictSetpointTo(maxKtias);
	}


}
