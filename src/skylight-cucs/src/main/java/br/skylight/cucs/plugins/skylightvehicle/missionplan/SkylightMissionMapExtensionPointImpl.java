package br.skylight.cucs.plugins.skylightvehicle.missionplan;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.concurrent.locks.ReentrantLock;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.Coordinates;
import br.skylight.commons.EventType;
import br.skylight.commons.MeasureType;
import br.skylight.commons.Mission;
import br.skylight.commons.Region;
import br.skylight.commons.SkylightMission;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.enums.VehicleType;
import br.skylight.commons.dli.mission.AVPositionWaypoint;
import br.skylight.commons.dli.skylight.Runway;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.mapkit.MapActionListenerAdapter;
import br.skylight.cucs.mapkit.MapElement;
import br.skylight.cucs.mapkit.MapElementBridge;
import br.skylight.cucs.mapkit.MapElementGroup;
import br.skylight.cucs.mapkit.MapKit;
import br.skylight.cucs.mapkit.painters.PolygonPainter;
import br.skylight.cucs.plugins.controlmap2d.ControlMapExtensionPoint;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.loiterdirector.LoiterPainter;
import br.skylight.cucs.plugins.skylightvehicle.missionplan.RunwayMapElement.RunwayPoint;
import br.skylight.cucs.plugins.skylightvehicle.missionplan.RunwayMapElement.RunwayType;
import br.skylight.cucs.plugins.skylightvehicle.vehiclecontrol.SkylightVehicle;
import br.skylight.cucs.plugins.skylightvehicle.vehiclecontrol.SkylightVehicleControlService;
import br.skylight.cucs.plugins.subscriber.MissionListener;
import br.skylight.cucs.plugins.subscriber.RunwayListener;
import br.skylight.cucs.widgets.CUCSViewHelper;
import br.skylight.cucs.widgets.NumberInputDialog;

@ExtensionPointImplementation(extensionPointDefinition=ControlMapExtensionPoint.class)
public class SkylightMissionMapExtensionPointImpl extends ControlMapExtensionPoint implements MissionListener, RunwayListener {

	public static final String PREFIX_MISSION = "skylightMission-";
	private static final String PREFIX_TAKEOFF = PREFIX_MISSION + "takeoff-";
	private static final String PREFIX_LANDING = PREFIX_MISSION + "landing-";
	private static final String PREFIX_AUTHORIZED_REGION = PREFIX_MISSION + "authorizedRegion-";
	private static final String PREFIX_PROHIBITED_REGION = PREFIX_MISSION + "prohibitedRegion-";
	private static final String ROS_MANUAL_RECOVERY = PREFIX_MISSION + "manualRecovery";  //  @jve:decl-index=0:
	private static final String ROS_DATA_LINK_RECOVERY = PREFIX_MISSION + "dataLinkRecovery";  //  @jve:decl-index=0:
	
	private ReentrantLock reloadLock = new ReentrantLock();

	@ServiceInjection
	public SkylightVehicleControlService skylightVehicleControlService;
	
	@ServiceInjection
	public VehicleControlService vehicleControlService;
	
	@Override
	public void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMissionListener(this);
 		reloadAllMissions();
	}

	private void handleMissionEvent(Mission mission, Object subject, EventType type) {
		if(getMapKit()!=null) {
			if(subject==null) {
				if(type.equals(EventType.SELECTED) 
					|| type.equals(EventType.CREATED)
					|| type.equals(EventType.UPDATED)) {
					reloadAllMissions();
		
				} else if(type.equals(EventType.DESELECTED)) {
		//			getMapKit().clearSelection();
					//TODO DESELECT ITEM AVOIDING INFINITE LOOPS (AS OCCURS WITH COMMENTED CODE)
		
				} else if(type.equals(EventType.DELETED)) {
					getMapKit().clearSelection();
					reloadAllMissions();
				}
				getMapKit().updateUI();
			}
		}
	}

	@Override
	public void onMissionEvent(Mission mission, EventType type) {
		handleMissionEvent(mission, null, type);
	}

	@Override
	public void onWaypointEvent(Mission mission, AVPositionWaypoint waypoint, EventType type) {
		handleMissionEvent(mission, waypoint, type);
	}
	
	@Override
	public void onRunwayEvent(Runway runway, EventType type) {
	}

	private void reloadAllMissions() {
//		System.out.println("SKYLIGHT RELOAD ALL");
		if(getMapKit()!=null) {
			reloadLock.lock();
			try {
				//remove all previous elements
				getMapKit().removeMapElementGroupsByIdPrefix(PREFIX_MISSION);
				
				for (Vehicle v : vehicleControlService.getKnownVehicles().values()) {
					SkylightVehicle sv = skylightVehicleControlService.resolveSkylightVehicle(v.getVehicleID().getVehicleID());
					
					if(sv.getSkylightMission()!=null && v.getMission()!=null) {
						//create authorized region elements
						MapElementGroup<RegionMapElement> ag = resolveAuthorizedRegionGroup(v.getMission(), sv.getSkylightMission().getRulesOfSafety().getAuthorizedRegion());
						ag.setEnabledToAddElements(true);
						ag.clearElements();
						createMapElementsForRegion(v.getMission(), ag, sv.getSkylightMission().getRulesOfSafety().getAuthorizedRegion());
	
						//create prohibited regions elements
						int n = 0;
						for (Region r : sv.getSkylightMission().getRulesOfSafety().getProhibitedRegions()) {
							MapElementGroup<RegionMapElement> pg = resolveProhibitedRegionGroup(v.getMission(), r, n++);
							pg.setEnabledToAddElements(true);
							pg.getElements().clear();
							createMapElementsForRegion(v.getMission(), pg, r);
						}
						
						//create manual recovery elements
						if(sv.getSkylightMission().getRulesOfSafety().getManualRecoveryLoiterLocation().isValid()) {
							MapElementGroup<ManualRecoveryLoiterMapElement> g = resolveManualRecoveryLoiterGroup(getMapKit(), v.getMission(), sv.getSkylightMission());
							g.createElement(CUCSViewHelper.toGeoPosition(sv.getSkylightMission().getRulesOfSafety().getManualRecoveryLoiterLocation()), sv.getSkylightMission().getRulesOfSafety().getManualRecoveryLoiterLocation().getAltitude(), ManualRecoveryLoiterMapElement.class);
						}
						
						//create data link recovery elements
						if(sv.getSkylightMission().getRulesOfSafety().getDataLinkRecoveryLoiterLocation().isValid()) {
							MapElementGroup<DataLinkRecoveryLoiterMapElement> g = resolveDataLinkRecoveryLoiterGroup(getMapKit(), v.getMission(), sv.getSkylightMission());
							g.createElement(CUCSViewHelper.toGeoPosition(sv.getSkylightMission().getRulesOfSafety().getDataLinkRecoveryLoiterLocation()), sv.getSkylightMission().getRulesOfSafety().getDataLinkRecoveryLoiterLocation().getAltitude(), DataLinkRecoveryLoiterMapElement.class);
						}
						
						//create takeoff elements
						//TODO implement
						
						//crate landing elements
						//TODO implement
					}
				}
			} finally {
				reloadLock.unlock();
			}
			getMapKit().updateUI();
		}
	}
	
	private void createMapElementsForRegion(Mission mission, MapElementGroup<RegionMapElement> group, Region region) {
		//create elements
		for (Coordinates p : region.getPoints()) {
			RegionMapElement r = new RegionMapElement(mission, region);
			r.setPoint(p);
			group.addElement(r, -1, RegionMapElement.class);
		}
	}	

	@Override
	public void setMapKit(MapKit mapKit) {
		super.setMapKit(mapKit);
        mapKit.addMapActionListener(new MapActionListenerAdapter<MapElement>() {
			@Override
			public void onElementEvent(MapElement mapElement, EventType eventType) {
				if(mapElement instanceof RunwayMapElement) {
					RunwayMapElement me = (RunwayMapElement)mapElement;
					subscriberService.notifyRunwayEvent(me.getRunway(), eventType, getThis());
				} else if(mapElement instanceof RegionMapElement) {
					RegionMapElement me = (RegionMapElement)mapElement;
					subscriberService.notifyMissionEvent(me.getMission(), EventType.UPDATED, getThis());
				} else if(mapElement instanceof ManualRecoveryLoiterMapElement) {
					ManualRecoveryLoiterMapElement me = (ManualRecoveryLoiterMapElement)mapElement;
					subscriberService.notifyMissionEvent(me.getMission(), EventType.UPDATED, getThis());
				} else if(mapElement instanceof DataLinkRecoveryLoiterMapElement) {
					DataLinkRecoveryLoiterMapElement me = (DataLinkRecoveryLoiterMapElement)mapElement;
					subscriberService.notifyMissionEvent(me.getMission(), EventType.UPDATED, getThis());
				}
				getMapKit().updateUI();
			}
			@Override
			public void onElementDoubleClicked(MapElement me) {
				if(me instanceof ManualRecoveryLoiterMapElement) {
					ManualRecoveryLoiterMapElement m = (ManualRecoveryLoiterMapElement)me;
					Double a = NumberInputDialog.showInputDialog(null, "Enter manual recovery altitude ("+ MeasureType.ALTITUDE.getTargetUnit().toString() +"):", MeasureType.ALTITUDE.convertToTargetUnit(m.getLoiterConfiguration().getLoiterAltitude()), 0, 99999999, 1, 0, 0);
					if(a!=null) {
						m.getLoiterConfiguration().setLoiterAltitude((float)MeasureType.ALTITUDE.convertToSourceUnit(a));
					}
				} else if(me instanceof DataLinkRecoveryLoiterMapElement) {
					DataLinkRecoveryLoiterMapElement m = (DataLinkRecoveryLoiterMapElement)me;
					Double a = NumberInputDialog.showInputDialog(null, "Enter data link recovery altitude ("+ MeasureType.ALTITUDE.getTargetUnit().toString() +"):", MeasureType.ALTITUDE.convertToTargetUnit(m.getLoiterConfiguration().getLoiterAltitude()), 0, 99999999, 1, 0, 0);
					if(a!=null) {
						m.getLoiterConfiguration().setLoiterAltitude((float)MeasureType.ALTITUDE.convertToSourceUnit(a));
					}
				}
				getMapKit().updateUI();
			}
        });
	}

	private void createMapElementsForTakeoffRunway(Mission mission, SkylightVehicle skylightVehicle, Runway runway) {
		//landing elements
		String groupId = PREFIX_TAKEOFF+mission.getMissionID();
		
		MapElementGroup<RunwayMapElement> group = new RunwayMapElementGroup("Takeoff runway for mission " + mission.getMissionID(), 5, RunwayType.TAKE_OFF, mission, skylightVehicle, runway, new LandingPainter(), getMapKit(), groupId);
		group.setEnabledToAddElements(false);
		group.clearElements();
		getMapKit().addMapElementGroup(group, groupId);
		
		RunwayMapElement me1 = new RunwayMapElement(mission, runway, RunwayType.TAKE_OFF, RunwayPoint.POINT1, skylightVehicle);
		getMapKit().addMapElement(group.getId(), me1, -1, RunwayMapElement.class);
		
		RunwayMapElement me2 = new RunwayMapElement(mission, runway, RunwayType.TAKE_OFF, RunwayPoint.POINT2, skylightVehicle);
		getMapKit().addMapElement(group.getId(), me2, -1, RunwayMapElement.class);
	}


	private void createMapElementsForLandingRunway(Mission mission, SkylightVehicle skylightVehicle, Runway runway) {
		//landing elements
		String groupId = PREFIX_LANDING+mission.getMissionID();
		
		MapElementGroup<RunwayMapElement> group = new RunwayMapElementGroup("Landing runway for mission " + mission.getMissionID(), 5, RunwayType.LANDING, mission, skylightVehicle, runway, new LandingPainter(), getMapKit(), groupId);
		group.setEnabledToAddElements(false);
		group.clearElements();
		getMapKit().addMapElementGroup(group, groupId);
		
		RunwayMapElement me1 = new RunwayMapElement(mission, runway, RunwayType.LANDING, RunwayPoint.POINT1, skylightVehicle);
		getMapKit().addMapElement(group.getId(), me1, -1, RunwayMapElement.class);
		
		RunwayMapElement me2 = new RunwayMapElement(mission, runway, RunwayType.LANDING, RunwayPoint.POINT2, skylightVehicle);
		getMapKit().addMapElement(group.getId(), me2, -1, RunwayMapElement.class);
	}

	private SkylightMissionMapExtensionPointImpl getThis() {
		return this;
	}

	@Override
	public boolean isCompatibleWithVehicle(Vehicle vehicle) {
		return vehicle!=null && vehicle.getVehicleID().getVehicleType().equals(VehicleType.TYPE_60);
	}

	private MapElementGroup<RegionMapElement> resolveAuthorizedRegionGroup(final Mission mission, final Region region) {
		String groupId = PREFIX_AUTHORIZED_REGION+mission.getMissionID();
		MapElementGroup<RegionMapElement> g = getMapKit().getMapElementGroup(groupId, RegionMapElement.class);
		if(g==null) {
			PolygonPainter<RegionMapElement> pp = new PolygonPainter<RegionMapElement>();
			pp.setPolygonContourColor(new Color(0, 225, 0, 190));
			pp.setPolygonFillColor(null);
			pp.setPolygonStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g = new RegionMapElementGroup(getMapKit(),"Authorized region for mission " + mission.getMissionID(), 20, mission, region, pp);
			getMapKit().addMapElementGroup(g, groupId);
		}
		return g;
	}
	
	private MapElementGroup<RegionMapElement> resolveProhibitedRegionGroup(final Mission mission, final Region region, int regionId) {
		String groupId = PREFIX_PROHIBITED_REGION+mission.getMissionID()+regionId;
		MapElementGroup<RegionMapElement> g = getMapKit().getMapElementGroup(groupId, RegionMapElement.class);
		if(g==null) {
			PolygonPainter<RegionMapElement> pp = new PolygonPainter<RegionMapElement>();
			pp.setPolygonContourColor(new Color(225, 0, 0, 190));
			pp.setPolygonFillColor(new Color(255, 0, 0, 50));
			pp.setPolygonStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g = new RegionMapElementGroup(getMapKit(),"Prohibited region #"+ regionId +" for mission " + mission.getMissionID(), 5, mission, region, pp);
			getMapKit().addMapElementGroup(g, groupId);
		}
		return g;
	}

	public static MapElementGroup<ManualRecoveryLoiterMapElement> resolveManualRecoveryLoiterGroup(MapKit mapKit, final Mission mission, final SkylightMission sm) {
		String groupId = ROS_MANUAL_RECOVERY+mission.getMissionID();
		MapElementGroup<ManualRecoveryLoiterMapElement> g = mapKit.getMapElementGroup(groupId, ManualRecoveryLoiterMapElement.class);
		if(g==null) {
			g = new MapElementGroup<ManualRecoveryLoiterMapElement>(mapKit,
					"Manual recovery loiter for mission " + mission.getMissionID(), 0,
					new MapElementBridge<ManualRecoveryLoiterMapElement>() {
						@Override
						public ManualRecoveryLoiterMapElement createMapElement(GeoPosition position, float altitude, int elementIndex, MapElementGroup<ManualRecoveryLoiterMapElement> group) {
							group.clearElements();
							ManualRecoveryLoiterMapElement t = new ManualRecoveryLoiterMapElement();
							t.setLabel("Manual recovery");
							t.setMission(mission);
							t.setRulesOfSafety(sm.getRulesOfSafety());
							return t;
						};
						@Override
						public void onElementDeleted(ManualRecoveryLoiterMapElement element, MapElementGroup<ManualRecoveryLoiterMapElement> group) {
							sm.getRulesOfSafety().getManualRecoveryLoiterLocation().reset();
						}
					}, new LoiterPainter<ManualRecoveryLoiterMapElement>()
				);
			g.setMaxAllowedElements(1);
			mapKit.addMapElementGroup(g, groupId);
		}
		return g;
	}
	
	public static MapElementGroup<DataLinkRecoveryLoiterMapElement> resolveDataLinkRecoveryLoiterGroup(MapKit mapKit, final Mission mission, final SkylightMission sm) {
		String groupId = ROS_DATA_LINK_RECOVERY+mission.getMissionID();
		MapElementGroup<DataLinkRecoveryLoiterMapElement> g = mapKit.getMapElementGroup(groupId, DataLinkRecoveryLoiterMapElement.class);
		if(g==null) {
			g = new MapElementGroup<DataLinkRecoveryLoiterMapElement>(mapKit,
					"Data link recovery loiter for mission " + mission.getMissionID(), 0,
					new MapElementBridge<DataLinkRecoveryLoiterMapElement>() {
						@Override
						public DataLinkRecoveryLoiterMapElement createMapElement(GeoPosition position, float altitude, int elementIndex, MapElementGroup<DataLinkRecoveryLoiterMapElement> group) {
							group.clearElements();
							DataLinkRecoveryLoiterMapElement t = new DataLinkRecoveryLoiterMapElement();
							t.setLabel("Data link recovery");
							t.setMission(mission);
							t.setRulesOfSafety(sm.getRulesOfSafety());
							sm.getRulesOfSafety().setDataLinkRecoveryEnabled(true);
							return t;
						};
						@Override
						public void onElementDeleted(DataLinkRecoveryLoiterMapElement element, MapElementGroup<DataLinkRecoveryLoiterMapElement> group) {
							sm.getRulesOfSafety().getDataLinkRecoveryLoiterLocation().reset();
							sm.getRulesOfSafety().setDataLinkRecoveryEnabled(false);
						}
					}, new LoiterPainter<DataLinkRecoveryLoiterMapElement>()
				);
			g.setMaxAllowedElements(1);
			mapKit.addMapElementGroup(g, groupId);
		}
		return g;
	}
	
}
