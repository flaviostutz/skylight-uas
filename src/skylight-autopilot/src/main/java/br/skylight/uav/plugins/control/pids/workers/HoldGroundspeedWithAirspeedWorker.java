package br.skylight.uav.plugins.control.pids.workers;

import br.skylight.commons.dli.skylight.PIDConfiguration;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.uav.plugins.control.pids.PIDController;
import br.skylight.uav.plugins.control.pids.PIDControllers;
import br.skylight.uav.plugins.control.pids.SkylightPIDWorker;

public class HoldGroundspeedWithAirspeedWorker extends SkylightPIDWorker {

	public HoldGroundspeedWithAirspeedWorker() {
		super(createPIDController());
	}
	
	private static PIDController createPIDController() {
		PIDController p = new PIDController(PIDControl.HOLD_GROUNDSPEED_WITH_IAS);
		p.setProportionalGain(1F);
		p.setFeedbackToOutputScale(1, 1);
		return p;
	}

	@Override
	public void onActivate() throws Exception {
		pidControllers.unholdSetpoint(PIDControl.HOLD_IAS_WITH_THROTTLE);
		pidControllers.unholdSetpoint(PIDControl.HOLD_IAS_WITH_PITCH);
	}
	
	@Override
	public float getFeedbackValue() {
		return gpsService.getGroundSpeed();
	}
	
	@Override
	public void step(float airspeedValue, PIDControllers pidControllers) {
		if(pilot.isUseReverseVerticalControl()) {
			pidControllers.holdSetpoint(PIDControl.HOLD_IAS_WITH_PITCH, airspeedValue);
		} else {
			pidControllers.holdSetpoint(PIDControl.HOLD_IAS_WITH_THROTTLE, airspeedValue);
		}
	}
	
	@Override
	public void onDeactivate() throws Exception {
//		pidControllers.unholdSetpoint(PIDControl.HOLD_IAS_WITH_THROTTLE);
//		pidControllers.unholdSetpoint(PIDControl.HOLD_IAS_WITH_PITCH);
	}

	@Override
	protected void setupPIDController(SkylightVehicleConfigurationMessage ac, PIDConfiguration pc) {
		getPIDController().setIntegralGain(pc.getKi(), 0, maxKtias);
		//there is no limit to ground speed itself, only airspeed (stall purposes)
	}

}
