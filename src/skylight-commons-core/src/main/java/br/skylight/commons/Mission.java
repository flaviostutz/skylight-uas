package br.skylight.commons;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.skylight.commons.dli.WaypointDef;
import br.skylight.commons.dli.enums.WaypointSpeedType;
import br.skylight.commons.dli.mission.AVLoiterWaypoint;
import br.skylight.commons.dli.mission.AVPositionWaypoint;
import br.skylight.commons.dli.mission.AVRoute;
import br.skylight.commons.dli.mission.PayloadActionWaypoint;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.skylight.MissionAnnotationsMessage;
import br.skylight.commons.dli.vehicle.VehicleConfigurationMessage;
import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.SerializableState;

public class Mission implements SerializableState {

	private String missionID;
	private ArrayList<AVPositionWaypoint> positionWaypoints = new ArrayList<AVPositionWaypoint>();
	private ArrayList<AVLoiterWaypoint> loiterWaypoints = new ArrayList<AVLoiterWaypoint>();
	private ArrayList<PayloadActionWaypoint> payloadActionWaypoints = new ArrayList<PayloadActionWaypoint>();
	private ArrayList<AVRoute> routes = new ArrayList<AVRoute>();

	private MissionAnnotationsMessage missionAnnotations = new MissionAnnotationsMessage();

	//transient
	private Map<Integer, WaypointDef> computedWaypointsMap = null;
	private Map<Integer, WaypointDef> waypointDefPool = null;
	private List<Message> allMissionMessages = new ArrayList<Message>();
	private Vehicle vehicle;

	public Mission() {
		missionID = "New mission";
		waypointDefPool = new HashMap<Integer,WaypointDef>();
	}
	
	public VerificationResult validate(VehicleConfigurationMessage vehicleConfiguration) {
		VerificationResult v = new VerificationResult();

		// prepare waypoints map
		Map<Integer, WaypointDef> wp = computeWaypointsMap();
		
		// waypoint validations
		if (wp.size() == 0) {
			v.addWarning("No waypoints defined in mission");
		}

		for (WaypointDef wd : wp.values()) {
			int waypointNumber = wd.getWaypointNumber();
			if(wd.getPositionWaypoint()!=null) {
				AVPositionWaypoint pw = wd.getPositionWaypoint();
				// contingency waypoints
				if (pw.getContingencyWaypointA() != 0) {
					if (wp.get(pw.getContingencyWaypointA()) == null) {
						v.addError("Waypoint #" + waypointNumber + ": Target waypoint (#" + pw.getContingencyWaypointA() + ") for contingency waypoint A doesn't exist");
					}
				}
				if (pw.getContingencyWaypointB() != 0) {
					if (wp.get(pw.getContingencyWaypointB()) == null) {
						v.addError("Waypoint #" + waypointNumber + ": Target waypoint (#" + pw.getContingencyWaypointB() + ") for contingency waypoint B doesn't exist");
					}
				}
				
				// next waypoints
				if (pw.getNextWaypoint() != 0) {
					if (wp.get(pw.getNextWaypoint()) == null) {
						v.addError("Waypoint #" + waypointNumber + ": Next waypoint (#" + pw.getNextWaypoint() + ") doesn't exist");
					} else {
						// look if target waypoint has a position waypoint defined
						WaypointDef nw = wp.get(pw.getNextWaypoint());
						if(nw.getPositionWaypoint() == null) {
							v.addError("Waypoint #" + waypointNumber + ": Next waypoint (#" + pw.getNextWaypoint() + ") is not a valid position waypoint");
						}
					}
					if (waypointNumber == pw.getNextWaypoint()) {
						v.addWarning("Waypoint #" + waypointNumber + ": Next waypoint is itself. A loop will occur here.");
					}
				} else {
					v.addWarning("Waypoint #" + waypointNumber + ": No next waypoint defined. Navigation will end here.");
				}
				
				//speed type
				if(pw.getWaypointSpeedType().equals(WaypointSpeedType.ARRIVAL_TIME)) {
					v.assertRange((float)pw.getArrivalTime(), 0, 5, 3600, Float.MAX_VALUE, "Waypoint #" + waypointNumber + ": arrival time");
				} else {
					if(pw.getWaypointToSpeed()>vehicleConfiguration.getMaximumIndicatedAirspeed()) {
						v.addWarning("Waypoint #" + pw.getWaypointNumber() + ": speed is above max indicated airspeed");
					}
				}
				
				//waypoint coordinates
				if(!pw.getWaypointPosition().isValid()) {
					v.addError("Waypoint #" + waypointNumber + ": invalid coordinates");
				}
				
			} else {
				v.addError("Waypoint #" + waypointNumber + ": No position waypoint found and it is mandatory");
			}
		}

		return v;
	}

	public float calculatePathLength(int fromWaypointNumber, int maxWaypointNumber) {
		float totalPathLength = 0;
		AVPositionWaypoint lastPosition = null;
		int n = fromWaypointNumber;
		List<Integer> processedWaypoints = new ArrayList<Integer>(); 
		while (n != 0) {
			processedWaypoints.add(n);
			WaypointDef w = getComputedWaypointsMap().get(n);
			n = 0;
			if (w != null) {
				AVPositionWaypoint pw = w.getPositionWaypoint();
				if (pw != null) {
					if (lastPosition != null) {
						totalPathLength += lastPosition.getWaypointPosition().distance(pw.getWaypointPosition());
					}
					lastPosition = pw;
					n = pw.getNextWaypoint();
					if(processedWaypoints.contains(n)) {
						return Float.POSITIVE_INFINITY;
					}
				}
			}
		}
		return totalPathLength;
	}

	public Map<Integer, WaypointDef> getComputedWaypointsMap() {
		if(computedWaypointsMap==null) {
			computedWaypointsMap = computeWaypointsMap();
		}
		return computedWaypointsMap;
	}
	
	public List<WaypointDef> getOrderedWaypoints() {
		List<WaypointDef> orderedWaypoints = new ArrayList<WaypointDef>(getComputedWaypointsMap().values());
		Collections.sort(orderedWaypoints, new Comparator<WaypointDef>() {
			public int compare(WaypointDef o1, WaypointDef o2) {
				if(o1.getWaypointNumber()<o2.getWaypointNumber()) {
					return -1;
				} else {
					return 1;
				}
			}
		});
		return orderedWaypoints;
	}

	public Map<Integer, WaypointDef> computeWaypointsMap() {
		if(computedWaypointsMap==null) {
			computedWaypointsMap = new HashMap<Integer,WaypointDef>();
		} else {
			computedWaypointsMap.clear();
		}
		for (AVPositionWaypoint pw : positionWaypoints) {
			resolveWaypointDef(pw.getWaypointNumber()).setPositionWaypoint(pw);
		}
		for (AVLoiterWaypoint lw : loiterWaypoints) {
			resolveWaypointDef(lw.getWaypointNumber()).setLoiterWaypoint(lw);
		}
		for (PayloadActionWaypoint pw : payloadActionWaypoints) {
			resolveWaypointDef(pw.getWaypointNumber()).getPayloadActionWaypoints().add(pw);
		}
		return computedWaypointsMap;
	}

	private WaypointDef resolveWaypointDef(int waypointNumber) {
		synchronized(computedWaypointsMap) {
			WaypointDef g = computedWaypointsMap.get(waypointNumber);
			if (g == null) {
				//reuse waypointdef to keep some previous info (reach time, state etc)
				g = waypointDefPool.get(waypointNumber);
				if(g!=null) {
					//reset states before reuse
					g.setLoiterWaypoint(null);
					g.getPayloadActionWaypoints().clear();
					g.setPositionWaypoint(null);
				} else {
					g = new WaypointDef();
					waypointDefPool.put(waypointNumber, g);
				}
				g.setMission(this);
				g.setWaypointNumber(waypointNumber);
				computedWaypointsMap.put(waypointNumber, g);
			}
			return g;
		}
	}

	public String getMissionID() {
		return missionID;
	}

	public void setMissionID(String missionID) {
		this.missionID = missionID;
	}

	public ArrayList<AVRoute> getRoutes() {
		return routes;
	}
	public void setRoutes(ArrayList<AVRoute> routes) {
		this.routes = routes;
	}
	
	public ArrayList<AVLoiterWaypoint> getLoiterWaypoints() {
		return loiterWaypoints;
	}
	public ArrayList<PayloadActionWaypoint> getPayloadActionWaypoints() {
		return payloadActionWaypoints;
	}
	public ArrayList<AVPositionWaypoint> getPositionWaypoints() {
		return positionWaypoints;
	}
	public void setComputedWaypointsMap(Map<Integer, WaypointDef> computedWaypointsMap) {
		this.computedWaypointsMap = computedWaypointsMap;
	}
	public void setLoiterWaypoints(ArrayList<AVLoiterWaypoint> loiterWaypoints) {
		this.loiterWaypoints = loiterWaypoints;
	}
	public void setPayloadActionWaypoints(ArrayList<PayloadActionWaypoint> payloadActionWaypoints) {
		this.payloadActionWaypoints = payloadActionWaypoints;
	}
	public void setPositionWaypoints(ArrayList<AVPositionWaypoint> positionWaypoints) {
		this.positionWaypoints = positionWaypoints;
	}

	public MissionAnnotationsMessage getMissionAnnotations() {
		return missionAnnotations;
	}
	
	public void setMissionAnnotations(MissionAnnotationsMessage missionAnnotations) {
		this.missionAnnotations = missionAnnotations;
	}

	@Override
	public void readState(DataInputStream in) throws IOException {
		missionID = in.readUTF();
		IOHelper.readArrayList(in, AVPositionWaypoint.class, positionWaypoints);
		IOHelper.readArrayList(in, AVLoiterWaypoint.class, loiterWaypoints);
		IOHelper.readArrayList(in, PayloadActionWaypoint.class, payloadActionWaypoints);
		IOHelper.readArrayList(in, AVRoute.class, routes);
		missionAnnotations.readState(in);
		computedWaypointsMap = null;
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		out.writeUTF(missionID);
		IOHelper.writeArrayList(out, positionWaypoints);
		IOHelper.writeArrayList(out, loiterWaypoints);
		IOHelper.writeArrayList(out, payloadActionWaypoints);
		IOHelper.writeArrayList(out, routes);
		missionAnnotations.writeState(out);
	}
	
	public void clear() {
		missionID = "";
		positionWaypoints.clear();
		loiterWaypoints.clear();
		payloadActionWaypoints.clear();
		routes.clear();
		missionAnnotations.resetValues();
		computedWaypointsMap = null;
	}
	
	public void setVehicle(Vehicle currentVehicle) {
		this.vehicle = currentVehicle;
	}
	public Vehicle getVehicle() {
		return vehicle;
	}

	public List<Message> getAllMissionMessages() {
		allMissionMessages.clear();
		for (AVPositionWaypoint m : positionWaypoints) {
			allMissionMessages.add(m);
		}
		for (AVLoiterWaypoint m : loiterWaypoints) {
			allMissionMessages.add(m);
		}
		for (PayloadActionWaypoint m : payloadActionWaypoints) {
			allMissionMessages.add(m);
		}
		for (AVRoute m : routes) {
			allMissionMessages.add(m);
		}
		allMissionMessages.add(missionAnnotations);
		return allMissionMessages;
	}

	public void insertWaypoint(AVPositionWaypoint pw, int afterWaypointNumber) {
		//preparation
		normalizeWaypointNumbers();
		ArrayList<WaypointDef> ws = new ArrayList<WaypointDef>(computedWaypointsMap.values());
		Collections.sort(ws, new Comparator<WaypointDef>() {
			@Override
			public int compare(WaypointDef o1, WaypointDef o2) {
				if(o1.getWaypointNumber()<o2.getWaypointNumber()) {
					return -1;
				} else if(o1.getWaypointNumber()>o2.getWaypointNumber()) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		
		//shift waypoint numbers if there already exists another waypoint in desired slot
		int newWaypointNumber = afterWaypointNumber + 1;
		if(computedWaypointsMap.get(newWaypointNumber)!=null) {
			for(WaypointDef wd : ws) {
				if(wd.getWaypointNumber()>afterWaypointNumber) {
					wd.changeWaypointNumber(wd.getWaypointNumber()+1, this);
				}
			}
		}
		
		pw.setWaypointNumber(newWaypointNumber);
		
		//insert this waypoint in the middle of a path
		WaypointDef previousWaypoint = computedWaypointsMap.get(afterWaypointNumber);
		if(previousWaypoint!=null) {
			pw.setNextWaypoint(previousWaypoint.getPositionWaypoint().getNextWaypoint());
			previousWaypoint.getPositionWaypoint().setNextWaypoint(pw.getWaypointNumber());
		}
		positionWaypoints.add(pw);
		
		//recompute internal state
		computeWaypointsMap();
	}
	
	/** 
	 * Normalize numbers so that connected waypoints will have contiguous numbers
	 */
	public void normalizeWaypointNumbers() {
		ArrayList<Integer> waypoints = new ArrayList<Integer>(computedWaypointsMap.keySet());
		Collections.sort(waypoints, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				if(o1<o2) {
					return -1;
				} else if(o1>o2) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		
		//normalize numbers
		int nextExpectedNumber = 1;
		for(Integer originalNumber : waypoints) {
			WaypointDef ow = computedWaypointsMap.get(originalNumber);
			if(ow.getWaypointNumber()!=nextExpectedNumber) {
				ow.changeWaypointNumber(nextExpectedNumber, this);
			}
			nextExpectedNumber++;
		}
		computeWaypointsMap();
	}
	
//	public void insertPositionWaypointAt(int insertAt, AVPositionWaypoint pw) {
//		//increment waypoint numbers that are after this waypoint
//		for (AVPositionWaypoint opw : positionWaypoints) {
//			if(opw.getWaypointNumber()>=insertAt) {
//				opw.setWaypointNumber(opw.getWaypointNumber()+1);
//				opw.setNextWaypoint(opw.getNextWaypoint()+1);
//			}
//		}
//		for (AVLoiterWaypoint lw : loiterWaypoints) {
//			if(lw.getWaypointNumber()>=insertAt) {
//				lw.setWaypointNumber(lw.getWaypointNumber()+1);
//			}
//		}
//		for (PayloadActionWaypoint paw : payloadActionWaypoints) {
//			if(paw.getWaypointNumber()>=insertAt) {
//				paw.setWaypointNumber(paw.getWaypointNumber()+1);
//			}
//		}
//
//		//increment route numbers that are after this waypoint
//		for (AVRoute r : routes) {
//			if(r.getInitialWaypointNumber()>=insertAt) {
//				r.setInitialWaypointNumber(r.getInitialWaypointNumber()+1);
//			}
//		}
//		
//		//add waypoint
//		pw.setWaypointNumber(insertAt);
//		pw.setNextWaypoint(insertAt+1);
//		positionWaypoints.add(pw);
//		
//		//recompute internal state
//		computeWaypointsMap();
//	}

	public void removeWaypointAt(int waypointNumber, boolean connectPreviousToNext) {
		AVPositionWaypoint rp = null;
		for (AVPositionWaypoint opw : positionWaypoints) {
			if(opw.getWaypointNumber()==waypointNumber) {
				rp = opw;
			}
		}
		if(rp!=null) {
			positionWaypoints.remove(rp);
			
			//connect previous waypoint to the next because this waypoint was deleted
			if(connectPreviousToNext) {
				AVPositionWaypoint previousWaypoint = null;
				for (AVPositionWaypoint pw : positionWaypoints) {
					if(pw.getNextWaypoint()==rp.getWaypointNumber()) {
						previousWaypoint = pw;
						break;
					}
				}
				WaypointDef nextWaypoint = computeWaypointsMap().get(rp.getNextWaypoint());
				if(previousWaypoint!=null && nextWaypoint!=null) {
					previousWaypoint.setNextWaypoint(nextWaypoint.getWaypointNumber());
				}
			}
		}
		
		//remove loiter extension if the deleted waypoint has one
		AVLoiterWaypoint rl = null;
		for (AVLoiterWaypoint lw : loiterWaypoints) {
			if(lw.getWaypointNumber()==waypointNumber) {
				rl = lw;
			}
		}
		if(rl!=null) {
			loiterWaypoints.remove(rl);
		}
		
		//remove payload action extension if the deleted waypoint has one
		List<PayloadActionWaypoint> remove = new ArrayList<PayloadActionWaypoint>();
		for (PayloadActionWaypoint paw : payloadActionWaypoints) {
			if(paw.getWaypointNumber()==waypointNumber) {
				remove.add(paw);
			}
		}
		for (PayloadActionWaypoint pw : remove) {
			payloadActionWaypoints.remove(pw);
		}

		//recompute internal state
		computeWaypointsMap();
	}

	public void deleteRoute(AVRoute route, boolean removeWaypoints) {
		//determine position what waypoints will be deleted
		if(removeWaypoints) {
			List<Integer> removeWp = new ArrayList<Integer>();
			for (AVPositionWaypoint pw : positionWaypoints) {
				if(isWaypointInsideRoute(pw.getWaypointNumber(), route.getInitialWaypointNumber())) {
					removeWp.add(pw.getWaypointNumber());
				}
			}
			//remove waypoints
			for (Integer wpn : removeWp) {
				removeWaypointAt(wpn, true);
			}
		}
		//remove route
		routes.remove(route);
	}
	
	public boolean isWaypointInsideRoute(int waypointNumber, int initialRouteWaypointNumber) {
		//order routes
		Collections.sort(routes, new Comparator<AVRoute>() {
			public int compare(AVRoute o1, AVRoute o2) {
				if(o1.getInitialWaypointNumber()<o2.getInitialWaypointNumber()) {
					return -1;
				} else if(o1.getInitialWaypointNumber()>o2.getInitialWaypointNumber()) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		
		AVRoute insideRoute = null;
		for (AVRoute route : routes) {
			if(waypointNumber>=route.getInitialWaypointNumber()) {
				insideRoute = route;
			}
		}
		
		if(insideRoute==null) {
			return false;
		} else {
			return insideRoute.getInitialWaypointNumber()==initialRouteWaypointNumber;
		}
	}

	public Mission createCopy() {
		try {
			Mission m = new Mission();
			IOHelper.copyState(m, this);
			return m;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public int getHighestWaypointNumber() {
		int n = 0;
		for (AVPositionWaypoint pw : positionWaypoints) {
			if(pw.getWaypointNumber()>n) {
				n = pw.getWaypointNumber(); 
			}
		}
		return n;
	}
	
}
