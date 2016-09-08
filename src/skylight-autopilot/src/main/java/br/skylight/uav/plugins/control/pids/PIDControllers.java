package br.skylight.uav.plugins.control.pids;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.dli.vehicle.VehicleConfigurationMessage;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.plugins.control.Pilot;
import br.skylight.uav.plugins.control.instruments.AdvancedInstrumentsService;
import br.skylight.uav.plugins.control.pids.workers.HoldAirspeedWithPitchWorker;
import br.skylight.uav.plugins.control.pids.workers.HoldAirspeedWithThrottleWorker;
import br.skylight.uav.plugins.control.pids.workers.HoldAltitudeWithPitchWorker;
import br.skylight.uav.plugins.control.pids.workers.HoldAltitudeWithThrottleWorker;
import br.skylight.uav.plugins.control.pids.workers.HoldCourseWithRollWorker;
import br.skylight.uav.plugins.control.pids.workers.HoldCourseWithYawWorker;
import br.skylight.uav.plugins.control.pids.workers.HoldGroundspeedWithAirspeedWorker;
import br.skylight.uav.plugins.control.pids.workers.HoldPitchWithElevatorRudderWorker;
import br.skylight.uav.plugins.control.pids.workers.HoldRollWithAileronWorker;
import br.skylight.uav.plugins.control.pids.workers.HoldYawWithElevatorRudderWorker;
import br.skylight.uav.plugins.storage.RepositoryService;
import br.skylight.uav.services.ActuatorsService;
import br.skylight.uav.services.GPSService;
import br.skylight.uav.services.InstrumentsService;

@ManagedMember
public class PIDControllers extends Worker {

	private static final Logger logger = Logger.getLogger(PIDControllers.class.getName());
	private Map<PIDControl, SkylightPIDWorker> pidWorkers;
	
	private float aileron;
	private float rudder;
	private float elevator;
	private float throttle;

	//used to avoid changing throttle too fast (and thus killing engine)
//	private PIDController throttleChanger = new PIDController(null);
	
	@ServiceInjection
	public InstrumentsService instrumentsService;
	@ServiceInjection
	public ActuatorsService actuatorsService;
	@ServiceInjection
	public GPSService gpsService;
	@ServiceInjection
	public AdvancedInstrumentsService advancedInstrumentsService;
	@ServiceInjection
	public RepositoryService repositoryService;
	@MemberInjection
	public Pilot pilot;

	@Override
	public void onActivate() throws Exception {
		//initialize controls
		pidWorkers = new HashMap<PIDControl, SkylightPIDWorker>();
		for (PIDControl pc : PIDControl.values()) {
			SkylightPIDWorker w = createWorker(pc);
			w.setPidControllers(this);
			w.setPilot(pilot);
			w.setActuatorsService(actuatorsService);
			w.setAdvancedInstrumentsService(advancedInstrumentsService);
			w.setGpsService(gpsService);
			w.setInstrumentsService(instrumentsService);
			pidWorkers.put(pc, w);
		}

		//configure throttle smoother
		//FIXME put this configuratino in vehicle configuration message
//		throttleChanger.setIntegralGain(0.001F, 0, 127);
//		throttleChanger.setProportionalGain(0F);
//		throttleChanger.setRestrictOutputFrom(0);
//		throttleChanger.setRestrictOutputTo(127);
//		throttleChanger.setRestrictSetpointFrom(0);
//		throttleChanger.setRestrictSetpointTo(127);
//		throttleChanger.setProportionalGain(1);
	}
	
	public void step() {
		//zero all actuations
		aileron = Float.NaN;
		rudder = Float.NaN;
		elevator = Float.NaN;
		throttle = Float.NaN;
		
		//step workers (during steps, the workers may increment surface actuations)
		for (PIDWorker pw : pidWorkers.values()) {
			pw.step(this);
		}

		//TODO verify if in XPlane this kind of gain is useful. In FlightGear they made it worse
		float surfacesGain = 1F;// + (20F/(float)Math.pow(advancedInstrumentsService.getIAS(), 2));
//		System.out.println(surfacesGain);
		
		//perform surface actuations after all PIDs has been processed (some surfaces may have been changed by more than one worker)
		//all those values were clamped on addAileron/Rudder/Elevator/Throttle methods
		if(!Float.isNaN(aileron)) {
			actuatorsService.setAileron(aileron*surfacesGain);
		}
		if(!Float.isNaN(rudder)) {
			actuatorsService.setRudder(rudder*surfacesGain);
		}
		if(!Float.isNaN(elevator)) {
			actuatorsService.setElevator(elevator*surfacesGain);
		}
		if(!Float.isNaN(throttle)) {
			//smooth actuation using an integral PID control
//			throttleChanger.setSetpointValue(throttle);
//			System.out.println("THROTTLE " + throttleChanger.step(actuatorsService.getThrottle()) + " " + actuatorsService.getThrottle() + " " + throttle);
//			actuatorsService.setThrottle(throttleChanger.step(actuatorsService.getThrottle()));
			actuatorsService.setThrottle(throttle);
		}
	}

	public void unholdAll() {
		for (PIDWorker pw : pidWorkers.values()) {
			try {
				pw.deactivate();
			} catch (Exception e) {
				logger.warning("Could not deactivate worker for controller " + pw.getPIDController().getPIDControl() + ". e=" + e.toString());
				logger.throwing(null, null, e);
			}
		}
	}

	public PIDWorker getPIDWorker(PIDControl pidControl) {
		return pidWorkers.get(pidControl);
	}

	public void reloadVehicleConfiguration() {
		VehicleConfigurationMessage ac = repositoryService.getVehicleConfiguration();
		SkylightVehicleConfigurationMessage sac = repositoryService.getSkylightVehicleConfiguration();
		for (SkylightPIDWorker pw : pidWorkers.values()) {
			pw.setupPIDController(ac, sac);
		}
	}

	private SkylightPIDWorker createWorker(PIDControl pc) {
		if (pc.equals(PIDControl.HOLD_ROLL_WITH_AILERON)) {
			return new HoldRollWithAileronWorker();
		} else if (pc.equals(PIDControl.HOLD_GROUNDSPEED_WITH_IAS)) {
			return new HoldGroundspeedWithAirspeedWorker();
		} else if (pc.equals(PIDControl.HOLD_PITCH_WITH_ELEV_RUDDER)) {
			return new HoldPitchWithElevatorRudderWorker();
		} else if (pc.equals(PIDControl.HOLD_IAS_WITH_PITCH)) {
			return new HoldAirspeedWithPitchWorker();
		} else if (pc.equals(PIDControl.HOLD_ALTITUDE_WITH_PITCH)) {
			return new HoldAltitudeWithPitchWorker();
		} else if (pc.equals(PIDControl.HOLD_COURSE_WITH_ROLL)) {
			return new HoldCourseWithRollWorker();
		} else if (pc.equals(PIDControl.HOLD_COURSE_WITH_YAW)) {
			return new HoldCourseWithYawWorker();
		} else if (pc.equals(PIDControl.HOLD_YAW_WITH_ELEV_RUDDER)) {
			return new HoldYawWithElevatorRudderWorker();
		} else if (pc.equals(PIDControl.HOLD_IAS_WITH_THROTTLE)) {
			return new HoldAirspeedWithThrottleWorker();
		} else if (pc.equals(PIDControl.HOLD_ALTITUDE_WITH_THROTTLE)) {
			return new HoldAltitudeWithThrottleWorker();
		} else {
			throw new IllegalArgumentException("Cannot instantiate a worker for " + pc);
		}
	}

	public void holdSetpoint(PIDControl pidControl, float setpointValue) {
		holdSetpoint(pidControl, setpointValue, true);
	}
	
	private void holdSetpoint(PIDControl pidControl, float setpointValue, boolean cancelUpperControllers) {
		PIDWorker pw = getPIDWorker(pidControl);
		pw.setSetpoint(setpointValue);
		try {
			pw.activate();
		} catch (Exception e) {
			logger.warning("Could not activate worker for controller " + pidControl + ". e=" + e.toString());
			logger.throwing(null, null, e);
		}
	}

	public void unholdSetpoint(PIDControl pidControl) {
		try {
			getPIDWorker(pidControl).deactivate();
		} catch (Exception e) {
			logger.warning("Could not deactivate (unhold) worker for pid controller " + pidControl + ". e=" + e.toString());
			logger.throwing(null, null, e);
		}
	}

	public void holdSetpoint(PIDControl pidControl, float altitude, AltitudeType altitudeType) {
		holdSetpoint(pidControl, altitude, altitudeType, true);
	}
	
	private void holdSetpoint(PIDControl pidControl, float altitude, AltitudeType altitudeType, boolean cancelUpperControllers) {
		PIDWorker w = getPIDWorker(pidControl);
		try {
			if (w instanceof HoldAltitudeWithThrottleWorker) {
				((HoldAltitudeWithThrottleWorker) w).setSetpoint(altitude, altitudeType);
				w.activate();
			} else if (w instanceof HoldAltitudeWithPitchWorker) {
				((HoldAltitudeWithPitchWorker) w).setSetpoint(altitude, altitudeType);
				w.activate();
			}
		} catch (Exception e) {
			logger.warning("Could not activate (hold) worker for pid controller " + pidControl + ". e=" + e.toString());
			logger.throwing(null, null, e);
		}
	}

	public float getCurrentAltitudeError() {
		if(getPIDWorker(PIDControl.HOLD_ALTITUDE_WITH_PITCH).isActive()) {
			return getPIDWorker(PIDControl.HOLD_ALTITUDE_WITH_PITCH).getErrorValue();
		} else if(getPIDWorker(PIDControl.HOLD_ALTITUDE_WITH_THROTTLE).isActive()) {
			return getPIDWorker(PIDControl.HOLD_ALTITUDE_WITH_THROTTLE).getErrorValue();
		} else {
			return 0;
		}
	}
	
	public float getCurrentCourseError() {
		if(getPIDWorker(PIDControl.HOLD_COURSE_WITH_YAW).isActive()) {
			return getPIDWorker(PIDControl.HOLD_COURSE_WITH_YAW).getErrorValue();
		} else if(getPIDWorker(PIDControl.HOLD_COURSE_WITH_ROLL).isActive()) {
			return getPIDWorker(PIDControl.HOLD_COURSE_WITH_ROLL).getErrorValue();
		} else {
			return 0;
		}
	}

	public float getCurrentSpeedError() {
		if(getPIDWorker(PIDControl.HOLD_GROUNDSPEED_WITH_IAS).isActive()) {
			return getPIDWorker(PIDControl.HOLD_GROUNDSPEED_WITH_IAS).getErrorValue();
		} else if(getPIDWorker(PIDControl.HOLD_IAS_WITH_PITCH).isActive()) {
			return getPIDWorker(PIDControl.HOLD_IAS_WITH_PITCH).getErrorValue();
		} else if(getPIDWorker(PIDControl.HOLD_IAS_WITH_THROTTLE).isActive()) {
			return getPIDWorker(PIDControl.HOLD_IAS_WITH_THROTTLE).getErrorValue();
		} else {
			return 0;
		}
	}
	
	public void addThrottle(float value) {
		if(Float.isNaN(throttle)) {
			throttle = 0;
		}
		throttle += value;
		throttle = MathHelper.clamp(throttle, SkylightPIDWorker.THROTTLE_VALUE_MIN, SkylightPIDWorker.THROTTLE_VALUE_MAX);
	}
	public void addAileron(float value) {
		if(Float.isNaN(aileron)) {
			aileron = 0;
		}
		aileron += value;
		aileron = MathHelper.clamp(aileron, SkylightPIDWorker.ACTUATORS_RANGE_MIN, SkylightPIDWorker.ACTUATORS_RANGE_MAX);
	}
	public void addElevator(float value) {
		if(Float.isNaN(elevator)) {
			elevator = 0;
		}
		elevator += value;
		elevator = MathHelper.clamp(elevator, SkylightPIDWorker.ACTUATORS_RANGE_MIN, SkylightPIDWorker.ACTUATORS_RANGE_MAX);
	}
	public void addRudder(float value) {
		if(Float.isNaN(rudder)) {
			rudder = 0;
		}
		rudder += value;
		rudder = MathHelper.clamp(rudder, SkylightPIDWorker.ACTUATORS_RANGE_MIN, SkylightPIDWorker.ACTUATORS_RANGE_MAX);
	}

}
