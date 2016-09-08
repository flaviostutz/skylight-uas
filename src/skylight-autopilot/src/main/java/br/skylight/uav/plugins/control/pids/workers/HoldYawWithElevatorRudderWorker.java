package br.skylight.uav.plugins.control.pids.workers;

import br.skylight.commons.dli.skylight.PIDConfiguration;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.infra.MathHelper;
import br.skylight.uav.plugins.control.pids.PIDController;
import br.skylight.uav.plugins.control.pids.PIDControllers;
import br.skylight.uav.plugins.control.pids.SkylightPIDWorker;

public class HoldYawWithElevatorRudderWorker extends SkylightPIDWorker {

	public HoldYawWithElevatorRudderWorker() {
		super(createPIDController());
	}
	
	private static PIDController createPIDController() {
		PIDController p = new PIDController(PIDControl.HOLD_YAW_WITH_ELEV_RUDDER);
		p.setRestrictSetpointFrom((float)-Math.PI);
		p.setRestrictSetpointTo((float)Math.PI);
		p.setRestrictOutputFrom(ACTUATORS_RANGE_MIN);
		p.setRestrictOutputTo(ACTUATORS_RANGE_MAX);
		p.setNormalizeErrorTo((float)MathHelper.TWO_PI);
		p.setProportionalGain(1F);
		p.setIntegralGain(0.02F, ACTUATORS_INTEGRATOR_MIN, ACTUATORS_INTEGRATOR_MAX);
		p.setInverseOutput(true);
		p.setFeedbackToOutputScale(1, 100);
//		p.setIntegratorUsageErrorThreshold((float)Math.toRadians(20));
		return p;
	}

	@Override
	public void onActivate() throws Exception {
		pidControllers.getPIDWorker(PIDControl.HOLD_COURSE_WITH_ROLL).deactivate();
	}
	
	@Override
	public void onDeactivate() throws Exception {
		//avoid leaving actuators in a dangerous position when releasing it
		actuatorsService.setRudder(0);
		actuatorsService.setElevator(0);
	}
	
	@Override
	public float getFeedbackValue() {
		//FIXME fix HoldCourseWithYaw controller and change this
//		return instrumentsService.getYaw();
		return (float)MathHelper.normalizeAngle(gpsService.getCourseHeading());
	}
	
	@Override
	public void step(float elevatorRudderValue, PIDControllers pidControllers) {
		//yaw rate damp
		elevatorRudderValue += MathHelper.clamp(instrumentsService.getYawRate()*yawRateDamp, DAMP_RANGE_MIN, DAMP_RANGE_MAX);

		//keep actuation proportional (inverse) to airspeed (1/as^2)
//		elevatorRudderValue *= 625.0/(Math.pow(advancedInstrumentsService.getIAS(),2));
		
		//distribute actuation among elevator/rudder according to roll and surface gain
		pidControllers.addRudder(elevatorRudderValue*(float)Math.cos(instrumentsService.getRoll()) * rudderSurfaceGain);
		pidControllers.addElevator(elevatorRudderValue*(float)Math.sin(instrumentsService.getRoll()) * elevatorSurfaceGain);
	}

	@Override
	protected void setupPIDController(SkylightVehicleConfigurationMessage ac, PIDConfiguration pc) {
		getPIDController().setIntegralGain(pc.getKi(), ACTUATORS_INTEGRATOR_MIN/3F, ACTUATORS_INTEGRATOR_MAX/3F);
	}

}
