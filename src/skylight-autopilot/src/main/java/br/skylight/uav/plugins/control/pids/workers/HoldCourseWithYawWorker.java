package br.skylight.uav.plugins.control.pids.workers;

import br.skylight.commons.dli.skylight.PIDConfiguration;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.infra.MathHelper;
import br.skylight.uav.plugins.control.pids.PIDController;
import br.skylight.uav.plugins.control.pids.PIDControllers;
import br.skylight.uav.plugins.control.pids.SkylightPIDWorker;

public class HoldCourseWithYawWorker extends SkylightPIDWorker {

	private static final float MAX_SIDESLIP_ANGLE = (float)Math.toRadians(30);
	
	public HoldCourseWithYawWorker() {
		super(createPIDController());
	}
	
	private static PIDController createPIDController() {
		final PIDController p = new PIDController(PIDControl.HOLD_COURSE_WITH_YAW);
		p.setRestrictSetpointFrom(0);
		p.setRestrictSetpointTo((float)MathHelper.TWO_PI);
//		p.setRestrictOutputFrom(-(float)Math.PI);
//		p.setRestrictOutputTo((float)Math.PI);
		p.setNormalizeErrorTo((float)MathHelper.TWO_PI);
		p.setProportionalGain(0.7F);
		p.setFeedbackToOutputScale(1, 1);
//		p.setIntegratorUsageErrorThreshold(MAX_SIDESLIP_ANGLE);
//		p.setPIDControllerListener(new PIDControllerListener() {
//			@Override
//			public float onSetpointSet(float targetSetPointValue) {
//				//restrict output min/max values to 90 degrees of target set point
//				float min = (float)MathHelper.normalizeAngle(targetSetPointValue-Math.toRadians(90));
//				float max = (float)MathHelper.normalizeAngle(targetSetPointValue+Math.toRadians(90));
//				p.setRestrictOutputFrom(min<max?min:max);
//				p.setRestrictOutputTo(min<max?max:min);
//				return targetSetPointValue;
//			}
//		});
		return p;
	}

	@Override
	public void onActivate() throws Exception {
//		pidControllers.getPIDWorker(PIDControl.HOLD_COURSE_WITH_ROLL).deactivate();
		pidControllers.getPIDWorker(PIDControl.HOLD_YAW_WITH_ELEV_RUDDER).deactivate();
	}
	
	@Override
	public float getFeedbackValue() {
		return (float)gpsService.getCourseHeading();
	}
	
	@Override
	public void step(float yawValue, PIDControllers pidControllers) {
//		pidControllers.holdSetpoint(PIDControl.HOLD_YAW_WITH_ELEV_RUDDER, (float)MathHelper.normalizeAngle(getPIDController().getSetpointValue() + yawValue));
//		pidControllers.holdSetpoint(PIDControl.HOLD_YAW_WITH_ELEV_RUDDER, (float)MathHelper.normalizeAngle(getPIDController().getSetpointValue()));
		//FIXME fix this control
		pidControllers.holdSetpoint(PIDControl.HOLD_YAW_WITH_ELEV_RUDDER, (float)MathHelper.normalizeAngle(getPIDController().getSetpointValue()));
	}
	
	@Override
	public void onDeactivate() throws Exception {
//		pidControllers.unholdSetpoint(PIDControl.HOLD_ROLL_WITH_AILERON);
	}

	@Override
	protected void setupPIDController(SkylightVehicleConfigurationMessage ac, PIDConfiguration pc) {
//		getPIDController().setIntegralGain(pc.getKi(), -(float)Math.PI, (float)Math.PI);
		getPIDController().setIntegralGain(pc.getKi(), -MAX_SIDESLIP_ANGLE, MAX_SIDESLIP_ANGLE);
	}

}
