package br.skylight.uav.plugins.control.pids.workers;

import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.skylight.PIDConfiguration;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.uav.plugins.control.pids.PIDController;
import br.skylight.uav.plugins.control.pids.PIDControllers;
import br.skylight.uav.plugins.control.pids.SkylightPIDWorker;

public class HoldAltitudeWithThrottleWorker extends SkylightPIDWorker {

	private AltitudeType altitudeType = AltitudeType.AGL;
	
	public HoldAltitudeWithThrottleWorker() {
		super(createPIDController());
	}
	
	private static PIDController createPIDController() {
		PIDController p = new PIDController(PIDControl.HOLD_ALTITUDE_WITH_THROTTLE);
		p.setRestrictOutputFrom(THROTTLE_VALUE_MIN);
		p.setRestrictOutputTo(THROTTLE_VALUE_MAX);
		p.setProportionalGain(1F);
		p.setInverseOutput(false);
		p.setDifferentialTermRegressionSamples(25);
		p.setIntegralGain(1F, THROTTLE_VALUE_MIN, THROTTLE_VALUE_MAX);
		p.setFeedbackToOutputScale(1, 10);
		return p;
	}

	@Override
	public void onActivate() throws Exception {
		pidControllers.getPIDWorker(PIDControl.HOLD_IAS_WITH_THROTTLE).deactivate();
		pidControllers.getPIDWorker(PIDControl.HOLD_ALTITUDE_WITH_PITCH).deactivate();
	}
	
	@Override
	public void onDeactivate() throws Exception {
		actuatorsService.setThrottle(70);
	}
	
	@Override
	public float getFeedbackValue() {
		return advancedInstrumentsService.getAltitude(altitudeType);
	}
	
	@Override
	public void step(float throttleValue, PIDControllers pidControllers) {
		pidControllers.addThrottle(throttleValue);
	}

	@Override
	protected void setupPIDController(SkylightVehicleConfigurationMessage ac, PIDConfiguration pc) {
		getPIDController().setIntegralGain(pc.getKi(), THROTTLE_VALUE_MIN, THROTTLE_VALUE_MAX);
		getPIDController().setRestrictSetpointFrom(0);
		getPIDController().setRestrictSetpointFrom(40);//avoid crashes to ground. should be turn off during takeoff/landing
	}
	
	public void setSetpoint(float altitudeValue, AltitudeType altitudeType) {
		super.setSetpoint(altitudeValue);
		
		//avoid overshooting when changing altitude types because of large differences in setpoint values
		if(this.altitudeType!=altitudeType) {
			getPIDController().reset();
		}
		this.altitudeType = altitudeType;
	}

	public AltitudeType getAltitudeType() {
		return altitudeType;
	}
}
