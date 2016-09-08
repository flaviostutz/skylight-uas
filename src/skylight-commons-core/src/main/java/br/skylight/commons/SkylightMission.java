package br.skylight.commons;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.skylight.commons.dli.WaypointDef;
import br.skylight.commons.dli.enums.WaypointSpeedType;
import br.skylight.commons.dli.mission.AVPositionWaypoint;
import br.skylight.commons.dli.mission.AVRoute;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.dli.skylight.TakeoffLandingConfiguration;
import br.skylight.commons.dli.vehicle.VehicleConfigurationMessage;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.infra.SerializableState;

public class SkylightMission implements SerializableState {

	private TakeoffLandingConfiguration takeoffLandingConfiguration = new TakeoffLandingConfiguration();
	private RulesOfSafety rulesOfSafety = new RulesOfSafety();

	public void validate(VerificationResult v, Mission mission, VehicleConfigurationMessage vehicleConfiguration, SkylightVehicleConfigurationMessage skylightVehicleConfiguration) {
		mission.computeWaypointsMap();
		
		//validate position waypoints
		for (AVPositionWaypoint pw : mission.getPositionWaypoints()) {
			validatePositionWaypoint(v, pw, skylightVehicleConfiguration);
		}
		
		//validate from-to waypoint relations
		for (AVPositionWaypoint pw : mission.getPositionWaypoints()) {
			WaypointDef nw = mission.getComputedWaypointsMap().get(pw.getNextWaypoint());
			if(nw!=null && nw.getPositionWaypoint()!=null) {
				double distance = CoordinatesHelper.calculateDistance(pw.getWaypointToLatitudeOrRelativeY(), pw.getWaypointToLongitudeOrRelativeX(), nw.getPositionWaypoint().getWaypointToLatitudeOrRelativeY(), nw.getPositionWaypoint().getWaypointToLongitudeOrRelativeX());
				if(distance<calculateMinTurnRadius(vehicleConfiguration, skylightVehicleConfiguration)) {
					v.addWarning("Waypoint #"+nw.getWaypointNumber() + ": Too near #"+ pw.getWaypointNumber() +". Vehicle may be unable to perform a clean navigation");
				}
			}
		}
		
		// total mission path length
		if(vehicleConfiguration!=null) {
			if(skylightVehicleConfiguration!=null) {
				long maxFlightTime = skylightVehicleConfiguration.getMaxFlightTimeMinutes() * 60;
				long maxLength = (long) (vehicleConfiguration.getOptimumCruiseIndicatedAirspeed() * maxFlightTime);
				int rn = 1;
				if(mission.getRoutes().size()>0) {
					for (AVRoute r : mission.getRoutes()) {
						float totalPathLength = mission.calculatePathLength(r.getInitialWaypointNumber(), Integer.MAX_VALUE);
						if(!Float.isInfinite(totalPathLength)) {
							v.assertRange(totalPathLength, -1, 0, maxLength * 0.7F, maxLength * 1.2F, "Route #"+ rn +" path length (due to max flight time)");
						} else {
							v.addWarning("Mission length is infinite because of a closed loop in path. Verify route '" + r.getRouteID() + "'");
						}
						rn++;
					}
				} else {
					float totalPathLength = mission.calculatePathLength(1, Integer.MAX_VALUE);
					if(!Float.isInfinite(totalPathLength)) {
						v.assertRange(totalPathLength, -1, 0, maxLength * 0.7F, maxLength * 1.2F, "Total mission path length (due to max flight time)");
					} else {
						v.addWarning("Mission length is infinite because of a closed loop in path");
					}
				}
			} else {
				v.addWarning("Mission length was not validated because skylight vehicle configuration was not found");
			}
		} else {
			v.addWarning("Mission length was not validated because vehicle configuration was not found");
		}

		rulesOfSafety.validate(v, skylightVehicleConfiguration);
		takeoffLandingConfiguration.validate(v);
	}
	
	protected void validatePositionWaypoint(VerificationResult v, AVPositionWaypoint pw, SkylightVehicleConfigurationMessage svc) {
		// waypoint altitude
		// TODO add validation to rules of safety for different
		// altitude types (calculate AGL at current position
		// etc)
		if (rulesOfSafety.getMinMaxAltitudeType().equals(pw.getWaypointAltitudeType())) {
			v.assertRange(pw.getWaypointToAltitude(), rulesOfSafety.getMinAltitude(), rulesOfSafety.getMaxAltitude(), "Waypoint #"+ pw.getWaypointNumber() +": altitude");
		} else {
			v.addWarning("Waypoint #"+ pw.getWaypointNumber() +": altitude was not validated. Altitude type is different from min/max ROS configuration");
		}

		// waypoint coordinates inside activity region
		if (rulesOfSafety.getAuthorizedRegion()!=null && rulesOfSafety.getAuthorizedRegion().isValidArea()) {

			if (!rulesOfSafety.getAuthorizedRegion().isPointInside(pw.getWaypointPosition())) {
				v.addError("Waypoint #" + pw.getWaypointNumber() + ": Waypoint position is not inside the authorized region");
			}
		}

		// waypoint coordinates outside prohibited regions
		for (Region prohibitedRegion : rulesOfSafety.getProhibitedRegions()) {
			if (prohibitedRegion.isPointInside(pw.getWaypointPosition())) {
				v.addError("Waypoint #" + pw.getWaypointNumber() + ": Waypoint position is inside a prohibited region");
			}
		}
		
		//speed type
		if(!pw.getWaypointSpeedType().equals(WaypointSpeedType.ARRIVAL_TIME)) {
			if(pw.getWaypointToSpeed()<svc.getStallIndicatedAirspeed()) {
				v.addWarning("Waypoint #" + pw.getWaypointNumber() + ": speed is below stall airspeed");
			}
		}
	}

	public RulesOfSafety getRulesOfSafety() {
		return rulesOfSafety;
	}

	public void setRulesOfSafety(RulesOfSafety rulesOfSafety) {
		this.rulesOfSafety = rulesOfSafety;
	}

	public TakeoffLandingConfiguration getTakeoffLandingConfiguration() {
		return takeoffLandingConfiguration;
	}
	public void setTakeoffLandingConfiguration(TakeoffLandingConfiguration takeoffLandingConfiguration) {
		this.takeoffLandingConfiguration = takeoffLandingConfiguration;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		rulesOfSafety.readState(in);
		takeoffLandingConfiguration.readState(in);
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		rulesOfSafety.writeState(out);
		takeoffLandingConfiguration.writeState(out);
	}
	
//	public void resetValues() {
//		rulesOfSafety.resetValues();
//		takeoffLandingConfiguration.resetValues();
//	}
	
	public List<Message> getAllMissionMessages() {
		List<Message> mm = new ArrayList<Message>();
		mm.add(takeoffLandingConfiguration);
		mm.add(rulesOfSafety);
		return mm;
	}

	public void clear() {
		takeoffLandingConfiguration.resetValues();
		rulesOfSafety.resetValues();
	}

	public SkylightMission createCopy() {
		try {
			SkylightMission m = new SkylightMission();
			IOHelper.copyState(m, this);
			return m;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private double calculateMinTurnRadius(VehicleConfigurationMessage vc, SkylightVehicleConfigurationMessage svc) {
		float r = MathHelper.getTurnRadius(svc.getStallIndicatedAirspeed(), svc.getRollMax()*0.75F);
		r *= svc.getCalculatedVersusRealTurnFactor();
		return MathHelper.clamp(r, 20, 10000);
	}
	
}
