package br.skylight.uav.plugins.control.pids.workers;

import br.skylight.commons.dli.skylight.PIDConfiguration;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.infra.MathHelper;
import br.skylight.uav.plugins.control.pids.PIDController;
import br.skylight.uav.plugins.control.pids.PIDControllers;
import br.skylight.uav.plugins.control.pids.SkylightPIDWorker;

public class HoldPitchWithElevatorRudderWorker extends SkylightPIDWorker {

	public HoldPitchWithElevatorRudderWorker() {
		super(createPIDController());
	}
	
	private static PIDController createPIDController() {
		PIDController p = new PIDController(PIDControl.HOLD_PITCH_WITH_ELEV_RUDDER);
		p.setRestrictOutputFrom(ACTUATORS_RANGE_MIN);
		p.setRestrictOutputTo(ACTUATORS_RANGE_MAX);
		p.setNormalizeErrorTo((float)MathHelper.TWO_PI);
		p.setInverseOutput(true);
		p.setProportionalGain(1F);
		p.setIntegralGain(0.02F, ACTUATORS_INTEGRATOR_MIN, ACTUATORS_INTEGRATOR_MAX);
		p.setFeedbackToOutputScale((float)Math.toRadians(1), 1);
//		p.setIntegratorUsageErrorThreshold((float)Math.toRadians(10));
		return p;
	}

	@Override
	public float getFeedbackValue() {
		return instrumentsService.getPitch();
	}

	@Override
	public void onActivate() throws Exception {
	}
	
	@Override
	public void onDeactivate() throws Exception {
		//avoid leaving actuators in a dangerous position when releasing it
		actuatorsService.setRudder(0);
		actuatorsService.setElevator(0);
	}
	
	@Override
	public void step(float elevatorRudderValue, PIDControllers pidControllers) {
		//pitch rate damp
		elevatorRudderValue += MathHelper.clamp(instrumentsService.getPitchRate()*pitchRateDamp, DAMP_RANGE_MIN, DAMP_RANGE_MAX);

		//keep actuation proportional (inverse) to airspeed (1/as^2)
//		elevatorRudderValue *= 625.0/(Math.pow(advancedInstrumentsService.getIAS(),2));
		
		//distribute actuation among elevator/rudder according to roll and surface gain
		pidControllers.addElevator(elevatorRudderValue*(float)Math.cos(instrumentsService.getRoll()) * elevatorSurfaceGain);
		pidControllers.addRudder(-(elevatorRudderValue*(float)Math.sin(instrumentsService.getRoll()) * rudderSurfaceGain));
	}

	@Override
	protected void setupPIDController(SkylightVehicleConfigurationMessage ac, PIDConfiguration pc) {
		getPIDController().setIntegralGain(pc.getKi(), ACTUATORS_INTEGRATOR_MIN*1.3F, ACTUATORS_INTEGRATOR_MAX*1.3F);
		getPIDController().setRestrictSetpointFrom(minPitch);//avoid using angles more than +- 90
		getPIDController().setRestrictSetpointTo(maxPitch);
	}

}
