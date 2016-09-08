package br.skylight.uav.plugins.control.pids;

import br.skylight.commons.dli.skylight.PIDConfiguration;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.dli.vehicle.VehicleConfigurationMessage;
import br.skylight.uav.plugins.control.Pilot;
import br.skylight.uav.plugins.control.instruments.AdvancedInstrumentsService;
import br.skylight.uav.services.ActuatorsService;
import br.skylight.uav.services.GPSService;
import br.skylight.uav.services.InstrumentsService;

public abstract class SkylightPIDWorker extends PIDWorker {

	//limit 20% more than specified to avoid reaching those limits due to dynamic characteristics of flight
	public static final float LIMITS_MAX_FACTOR = 0.75F;
	public static final float LIMITS_MIN_FACTOR = 1.25F;
	
	public static final float ACTUATORS_RANGE_MIN = -127;
	public static final float ACTUATORS_RANGE_MAX = 127;
	public static final float ACTUATORS_INTEGRATOR_MIN = -70;
	public static final float ACTUATORS_INTEGRATOR_MAX = 70;

	public static final float THROTTLE_VALUE_MIN = 0;
	public static final float THROTTLE_VALUE_MAX = 127;

	public static final float DAMP_RANGE_MIN = -127;
	public static final float DAMP_RANGE_MAX = 127;

	protected float elevatorSurfaceGain = 0;
	protected float rudderSurfaceGain = 0;
	
	protected float pitchRateDamp = 0;
	protected float rollRateDamp = 0;
	protected float yawRateDamp = 0;
	
	protected float maxPitch = (float)Math.toRadians(12);
	protected float minPitch = (float)Math.toRadians(-12);
	protected float maxRoll = (float)Math.toRadians(30);
	protected float minRoll = (float)Math.toRadians(-30);
	protected float maxKtias = 150;
	protected float minKtias = 50;
	
	protected InstrumentsService instrumentsService;
	protected ActuatorsService actuatorsService;
	protected GPSService gpsService;
	protected Pilot pilot;
	protected AdvancedInstrumentsService advancedInstrumentsService;
	protected PIDControllers pidControllers;
	
	public SkylightPIDWorker(PIDController pidController) {
		super(pidController);
	}

	public final void setupPIDController(VehicleConfigurationMessage ac, SkylightVehicleConfigurationMessage sac) {
		rudderSurfaceGain = sac.getRudderSurfaceGain();
		elevatorSurfaceGain = sac.getElevatorSurfaceGain();

		pitchRateDamp = sac.getPitchRateDamp();
		rollRateDamp = sac.getRollRateDamp();
		yawRateDamp = sac.getYawRateDamp();

		maxPitch = (sac.getPitchMax()>Math.PI)?sac.getPitchMax():(sac.getPitchMax() * LIMITS_MAX_FACTOR);
		minPitch = (sac.getPitchMin()<-Math.PI)?sac.getPitchMin():(sac.getPitchMin() * LIMITS_MAX_FACTOR);
		maxRoll = (sac.getRollMax()>Math.PI)?sac.getRollMax():(sac.getRollMax() * LIMITS_MAX_FACTOR);
		minRoll = (sac.getRollMin()<-Math.PI)?sac.getRollMin():(sac.getRollMin() * LIMITS_MAX_FACTOR);
		minKtias = sac.getStallIndicatedAirspeed() * LIMITS_MIN_FACTOR;
		maxKtias = ac.getMaximumIndicatedAirspeed() * LIMITS_MAX_FACTOR;

		//common configuration for PID controllers
		PIDConfiguration pc = sac.getPIDConfiguration(getPIDController().getPIDControl());
		PIDController pcc = getPIDController();
		pcc.setProportionalGain(pc.getKp());
		pcc.setDifferentialGain(pc.getKd());
		pcc.setIntegralGain(pc.getKi(), -Float.MAX_VALUE, Float.MAX_VALUE);
		
		//specific configuration for each PID controller
		setupPIDController(sac, pc);
	}
	
	public void setPidControllers(PIDControllers pidControllers) {
		this.pidControllers = pidControllers;
	}
	public void setAdvancedInstrumentsService(AdvancedInstrumentsService advancedInstrumentsService) {
		this.advancedInstrumentsService = advancedInstrumentsService;
	}
	public void setInstrumentsService(InstrumentsService instrumentsService) {
		this.instrumentsService = instrumentsService;
	}
	public void setGpsService(GPSService gpsService) {
		this.gpsService = gpsService;
	}
	public void setActuatorsService(ActuatorsService actuatorsService) {
		this.actuatorsService = actuatorsService;
	}
	public void setPilot(Pilot pilot) {
		this.pilot = pilot;
	}
	
	protected abstract void setupPIDController(SkylightVehicleConfigurationMessage ac, PIDConfiguration pc);
	
	public float getMinKtias() {
		return minKtias;
	}

}
