package br.skylight.uav.plugins.control.maneuvers.safety;

import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.statemachine.StateAdapter;
import br.skylight.commons.statemachine.StateMachine;
import br.skylight.uav.plugins.control.maneuvers.StateBasedManeuver;

@ManagedMember
public class StabilizationManeuver extends StateBasedManeuver {

//	private static final Logger logger = Logger.getLogger(StabilizationManeuver.class.getName());
	
	private static final String STEP_STABILIZATION = "stabilization";
	private double timeInStableAttitude = 5;
	
	@Override
	public void onStart() {
		pilot.setEnableFlightHolds(false);
		pilot.unholdAll();
		getStateMachine().enterState(STEP_STABILIZATION);
	}

	@Override
	protected StateMachine<String, Object> setupStateMachine() {
		StateMachine<String,Object> sm = new StateMachine<String,Object>();
		
		sm.addState(STEP_STABILIZATION, new StateAdapter() {
			public void onEntry() throws Exception {
				//hold attitude
				pidControllers.holdSetpoint(PIDControl.HOLD_ROLL_WITH_AILERON, 0);
				pidControllers.holdSetpoint(PIDControl.HOLD_ALTITUDE_WITH_PITCH, 
											Math.max(advancedInstrumentsService.getAltitude(AltitudeType.AGL), 100),
											AltitudeType.AGL);
				float avgIas = (repositoryService.getSkylightVehicleConfiguration().getStallIndicatedAirspeed()+repositoryService.getVehicleConfiguration().getMaximumIndicatedAirspeed())/2F;
				pidControllers.holdSetpoint(PIDControl.HOLD_IAS_WITH_THROTTLE, avgIas);
			}
			
			public void onStep() throws Exception {
				if(getStateMachine().getTimeInCurrentState()>timeInStableAttitude) {
					stop(true);
				}
			}
		});
		return sm;
	}

	public void setTimeInStableAttitude(double timeInStableAttitude) {
		this.timeInStableAttitude = timeInStableAttitude;
	}
	
	public double getTimeInStableAttitude() {
		return timeInStableAttitude;
	}
	
	@Override
	public void onStop(boolean aborted) {
		getStateMachine().end();
	}
	
}
