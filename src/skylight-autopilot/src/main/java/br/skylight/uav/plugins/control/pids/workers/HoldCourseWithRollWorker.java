package br.skylight.uav.plugins.control.pids.workers;

import br.skylight.commons.dli.skylight.PIDConfiguration;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.infra.MathHelper;
import br.skylight.uav.plugins.control.pids.PIDController;
import br.skylight.uav.plugins.control.pids.PIDControllers;
import br.skylight.uav.plugins.control.pids.SkylightPIDWorker;

public class HoldCourseWithRollWorker extends SkylightPIDWorker {

	public HoldCourseWithRollWorker() {
		super(createPIDController());
	}
	
	private static PIDController createPIDController() {
		PIDController p = new PIDController(PIDControl.HOLD_COURSE_WITH_ROLL);
		p.setRestrictSetpointFrom(0);
		p.setRestrictSetpointTo((float)MathHelper.TWO_PI);
		p.setRestrictOutputFrom(-(float)Math.toRadians(20));
		p.setRestrictOutputTo((float)Math.toRadians(20));
		p.setNormalizeErrorTo((float)MathHelper.TWO_PI);
		p.setProportionalGain(0.7F);
		p.setFeedbackToOutputScale(1, 1);
		return p;
	}

	@Override
	public void onActivate() throws Exception {
		pidControllers.getPIDWorker(PIDControl.HOLD_ROLL_WITH_AILERON).deactivate();
		pidControllers.holdSetpoint(PIDControl.HOLD_COURSE_WITH_YAW, getPIDController().getSetpointValue());
	}
	
	@Override
	public float getFeedbackValue() {
		return (float)MathHelper.normalizeAngle(gpsService.getCourseHeading());
	}
	
	@Override
	public void step(float rollValue, PIDControllers pidControllers) {
		pidControllers.holdSetpoint(PIDControl.HOLD_ROLL_WITH_AILERON, rollValue);
	}
	
	@Override
	public void onDeactivate() throws Exception {
//		pidControllers.unholdSetpoint(PIDControl.HOLD_ROLL_WITH_AILERON);
	}

	@Override
	protected void setupPIDController(SkylightVehicleConfigurationMessage ac, PIDConfiguration pc) {
		getPIDController().setIntegralGain(pc.getKi(), minRoll / 3F, maxRoll / 3F);
		getPIDController().setRestrictOutputFrom((float)Math.max(-Math.PI/2, minRoll));//avoid circular angles for course control
		getPIDController().setRestrictOutputTo((float)Math.min(Math.PI/2, maxRoll));
	}

}
