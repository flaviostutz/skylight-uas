package br.skylight.uav.plugins.control.pids.workers;

import br.skylight.commons.dli.skylight.PIDConfiguration;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.uav.plugins.control.pids.PIDController;
import br.skylight.uav.plugins.control.pids.PIDControllers;
import br.skylight.uav.plugins.control.pids.SkylightPIDWorker;

public class HoldAirspeedWithThrottleWorker extends SkylightPIDWorker {

	public HoldAirspeedWithThrottleWorker() {
		super(createPIDController());
	}
	
	private static PIDController createPIDController() {
		PIDController p = new PIDController(PIDControl.HOLD_IAS_WITH_THROTTLE);
		p.setRestrictOutputFrom(THROTTLE_VALUE_MIN);
		p.setRestrictOutputTo(THROTTLE_VALUE_MAX);
		p.setProportionalGain(0.1F);
		p.setInverseOutput(false);
		p.setIntegralGain(0.1F, THROTTLE_VALUE_MIN, THROTTLE_VALUE_MAX);
		p.setFeedbackToOutputScale(15, 50);
		return p;
	}

	@Override
	public void onActivate() throws Exception {
		pidControllers.getPIDWorker(PIDControl.HOLD_IAS_WITH_PITCH).deactivate();
		pidControllers.getPIDWorker(PIDControl.HOLD_ALTITUDE_WITH_THROTTLE).deactivate();
	}
	
	@Override
	public void onDeactivate() throws Exception {
		actuatorsService.setThrottle(70);
	}

	@Override
	public float getFeedbackValue() {
		return advancedInstrumentsService.getIAS();
	}
	
	@Override
	public void step(float throttleValue, PIDControllers pidControllers) {
		pidControllers.addThrottle(throttleValue);
	}

	@Override
	protected void setupPIDController(SkylightVehicleConfigurationMessage ac, PIDConfiguration pc) {
		getPIDController().setIntegralGain(pc.getKi(), THROTTLE_VALUE_MIN, THROTTLE_VALUE_MAX);
		getPIDController().setRestrictSetpointFrom(minKtias);
		getPIDController().setRestrictSetpointTo(maxKtias);
	}

}
