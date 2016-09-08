package br.skylight.uav.plugins.control.maneuvers;

import java.util.logging.Logger;

import br.skylight.commons.Coordinates;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.plugins.control.Commander;
import br.skylight.uav.plugins.control.FlightEngineer;
import br.skylight.uav.plugins.control.Pilot;
import br.skylight.uav.plugins.control.instruments.AdvancedInstrumentsService;
import br.skylight.uav.plugins.control.pids.PIDControllers;
import br.skylight.uav.plugins.storage.RepositoryService;
import br.skylight.uav.services.ActuatorsService;
import br.skylight.uav.services.GPSService;
import br.skylight.uav.services.InstrumentsService;

@ManagedMember
public abstract class Maneuver extends Worker {

	private static final Logger logger = Logger.getLogger(Maneuver.class.getName());
	
	private boolean running;
	private long startTime;
//	private boolean aborted;
	
	private Coordinates referencePosition = new Coordinates(0,0,0);
	private float referenceTurnRadius;

	private ManeuverListener maneuverListener;

	@MemberInjection
	public Pilot pilot;
	@MemberInjection
	public FlightEngineer flightEngineer;
	@ServiceInjection
	public Commander commander;
	@MemberInjection
	public PIDControllers pidControllers;
	@ServiceInjection
	public PluginManager pluginManager;
	@ServiceInjection
	public RepositoryService repositoryService;
	@ServiceInjection
	public AdvancedInstrumentsService advancedInstrumentsService;
	@ServiceInjection
	public GPSService gpsService;
	@ServiceInjection
	public InstrumentsService instrumentsService;
	@ServiceInjection
	public ActuatorsService actuatorsService;
	@ServiceInjection
	public MessagingService messagingService;
	
	public void doStep() {
		if(running) {
			try {
				step();
			} catch (Exception e) {
				logger.throwing(null,null,e);
				throw new RuntimeException(e);
			}
		}
	}
	
	public void onStart() throws Exception {};
	public void onStop(boolean aborted) throws Exception {};
	
	public boolean isRunning() {
		return running;
	}

	public void start() throws Exception {
		if(running) {
			stop(true);
		}
		onStart();
		startTime = System.currentTimeMillis();
		running = true;
	}

	public void stop(boolean aborted) throws Exception {
		onStop(aborted);
		running = false;
		if(maneuverListener==null) {
			logger.info("Maneuver "+ this.getName() +" finished but there is no listener for this event");
		} else {
			maneuverListener.maneuverFinished(this, aborted);
		}
	}
	
	public double getElapsedTime() {
		return (System.currentTimeMillis() - startTime)/1000.0;
	}

	public void setManeuverListener(ManeuverListener maneuverListener) {
		this.maneuverListener = maneuverListener;
	}
	
	public String getName() {
		int i = getClass().getName().lastIndexOf('.');
		if(i!=-1) {
			return getClass().getName().substring(i+1);
		} else {
			return getClass().getName();
		}
	}

	public void setReferencePosition(Coordinates referencePosition) {
		this.referencePosition = referencePosition;
	}
	
	public Coordinates getReferencePosition() {
		return referencePosition;
	}

	protected float calculateCurrentMinTurnRadius() {
		referenceTurnRadius = pilot.calculateMinTurnRadius(gpsService.getGroundSpeed());
		return referenceTurnRadius;
	}

	public float getReferenceTurnRadius() {
		return referenceTurnRadius;
	}
	public void setReferenceTurnRadius(int referenceTurnRadius) {
		this.referenceTurnRadius = referenceTurnRadius;
	}

	public void reset() {
		this.running = false;
//		this.aborted = false;
	}
	
	@Override
	public void deactivate() throws Exception {
		super.deactivate();
		stop(false);
	}
	
}
