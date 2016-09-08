package br.skylight.uav.plugins.control.pids.workers;

import br.skylight.commons.dli.skylight.PIDConfiguration;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.infra.MathHelper;
import br.skylight.uav.plugins.control.pids.PIDController;
import br.skylight.uav.plugins.control.pids.PIDControllers;
import br.skylight.uav.plugins.control.pids.SkylightPIDWorker;

public class HoldRollWithAileronWorker extends SkylightPIDWorker {

	public HoldRollWithAileronWorker() {
		super(createPIDController());
	}

	private static PIDController createPIDController() {
		// controller definition
		PIDController p = new PIDController(PIDControl.HOLD_ROLL_WITH_AILERON);
		p.setRestrictOutputFrom(ACTUATORS_RANGE_MIN);
		p.setRestrictOutputTo(ACTUATORS_RANGE_MAX);
		p.setNormalizeErrorTo((float)MathHelper.TWO_PI);
		p.setProportionalGain(1F);
		p.setIntegralGain(0.02F, ACTUATORS_INTEGRATOR_MIN, ACTUATORS_INTEGRATOR_MAX);
		p.setInverseOutput(true);
		p.setFeedbackToOutputScale((float)Math.toRadians(1), 1);
//		p.setIntegratorUsageErrorThreshold((float)Math.toRadians(10));
		return p;
	}

//	@Override
//	public void setSetpoint(float setpointValue) {
//		super.setSetpoint(setpointValue);
//	}
	
	@Override
	public void onDeactivate() throws Exception {
		actuatorsService.setAileron(0);
	}
	
	@Override
	public float getFeedbackValue() {
		return instrumentsService.getRoll();
	}

	@Override
	public void step(float aileronValue, PIDControllers pidControllers) {
		//roll rate damp
		aileronValue += MathHelper.clamp(instrumentsService.getRollRate()*rollRateDamp, DAMP_RANGE_MIN, DAMP_RANGE_MAX);

		//keep actuation proportional (inverse) to airspeed (1/as^2)
//		aileronValue *= 625.0/(Math.pow(advancedInstrumentsService.getIAS(),2F));
		
		//actuate
		pidControllers.addAileron(aileronValue);
	}

	@Override
	protected void setupPIDController(SkylightVehicleConfigurationMessage ac, PIDConfiguration pc) {
		getPIDController().setIntegralGain(pc.getKi(), ACTUATORS_INTEGRATOR_MIN, ACTUATORS_INTEGRATOR_MAX);
		getPIDController().setRestrictSetpointFrom(minRoll);
		getPIDController().setRestrictSetpointTo(maxRoll);
	}

}
