package br.skylight.uav.plugins.control.pids.workers;

import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.skylight.PIDConfiguration;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.plugins.control.pids.PIDController;
import br.skylight.uav.plugins.control.pids.PIDControllers;
import br.skylight.uav.plugins.control.pids.SkylightPIDWorker;
import br.skylight.uav.plugins.storage.RepositoryService;

public class HoldAltitudeWithPitchWorker extends SkylightPIDWorker {

	@ServiceInjection
	public RepositoryService repositoryService;
	
	private AltitudeType altitudeType = AltitudeType.AGL;
	
	public HoldAltitudeWithPitchWorker() {
		super(createPIDController());
	}
	
	private static PIDController createPIDController() {
		PIDController p = new PIDController(PIDControl.HOLD_ALTITUDE_WITH_PITCH);
		p.setRestrictOutputFrom((float)Math.toRadians(-15));
		p.setRestrictOutputTo((float)Math.toRadians(15));
		p.setProportionalGain(0.3F);
		p.setFeedbackToOutputScale(1, (float)Math.toRadians(1)*10);
		return p;
	}

	@Override
	public void onActivate() throws Exception {
		pidControllers.getPIDWorker(PIDControl.HOLD_PITCH_WITH_ELEV_RUDDER).deactivate();
		pidControllers.getPIDWorker(PIDControl.HOLD_ALTITUDE_WITH_THROTTLE).deactivate();
		pidControllers.getPIDWorker(PIDControl.HOLD_IAS_WITH_PITCH).deactivate();
	}
	
	@Override
	public float getFeedbackValue() {
		return advancedInstrumentsService.getAltitude(altitudeType);
	}
	
	@Override
	public void step(float pitchValue, PIDControllers pidControllers) {
//		pitchValue += MathHelper.clamp(advancedInstrumentsService.getVerticalSpeed() * vspeedDamp, DAMP_RANGE_MIN, DAMP_RANGE_MAX);
		pitchValue = MathHelper.clamp(pitchValue, getPIDController().getRestrictOutputFrom(), getPIDController().getRestrictOutputTo());
		//continue to go up when inverted 
		if(instrumentsService.getRoll()<-Math.PI || instrumentsService.getRoll()>Math.PI) {
			pitchValue = -pitchValue;
		}
		pidControllers.holdSetpoint(PIDControl.HOLD_PITCH_WITH_ELEV_RUDDER, pitchValue);
	}
	
	@Override
	public void onDeactivate() throws Exception {
//		pidControllers.unholdSetpoint(PIDControl.HOLD_PITCH_WITH_ELEVATOR);
	}

	@Override
	protected void setupPIDController(SkylightVehicleConfigurationMessage ac, PIDConfiguration pc) {
		getPIDController().setIntegralGain(pc.getKi(), minPitch / 3F, maxPitch / 3F);
		getPIDController().setRestrictOutputFrom((float)Math.max(minPitch, Math.toDegrees(-90)));
		getPIDController().setRestrictOutputTo((float)Math.min(maxPitch, Math.toDegrees(90)));
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
