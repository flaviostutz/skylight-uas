package br.skylight.uav.plugins.control.pathmode;

import br.skylight.commons.dli.mission.FromToNextWaypointStates;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.statemachine.StateAdapter;
import br.skylight.uav.plugins.control.Commander;
import br.skylight.uav.plugins.control.Pilot;
import br.skylight.uav.plugins.control.instruments.AdvancedInstrumentsService;
import br.skylight.uav.plugins.control.pids.PIDControllers;
import br.skylight.uav.plugins.payload.PayloadService;
import br.skylight.uav.plugins.storage.RepositoryService;
import br.skylight.uav.services.ActuatorsService;
import br.skylight.uav.services.GPSService;

public abstract class FlightPathMode extends StateAdapter {

//	private static final Logger logger = Logger.getLogger(FlightPathMode.class.getName());
	
	@ServiceInjection
	public ActuatorsService actuatorsService;
	@ServiceInjection
	public RepositoryService repositoryService;
	@ServiceInjection
	public AdvancedInstrumentsService advancedInstrumentsService;
	@ServiceInjection
	public GPSService gpsService;
	@ServiceInjection
	public PayloadService payloadService;
	@MemberInjection
	public PIDControllers pidControllers;

	protected Commander commander;
	protected Pilot pilot;
	
	public FlightPathMode(Commander commander, Pilot pilot) {
		this.commander = commander;
		this.pilot = pilot;
	}

	@Override
	public void onEntry() throws Exception {
		pilot.unholdAll();
	}

	/**
	 * Prepare from-to-next waypoint state message for reporting purposes.
	 * Return 'false' if this message is not supported by current mode
	 */
	public abstract boolean prepareFromToNextWaypointStates(FromToNextWaypointStates fromToNextWaypointStates);
	
}
