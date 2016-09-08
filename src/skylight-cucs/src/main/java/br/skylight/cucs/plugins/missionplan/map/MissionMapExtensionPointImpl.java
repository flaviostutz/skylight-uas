package br.skylight.cucs.plugins.missionplan.map;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.EventType;
import br.skylight.commons.Mission;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.WaypointDef;
import br.skylight.commons.dli.enums.FlightPathControlMode;
import br.skylight.commons.dli.enums.MissionPlanMode;
import br.skylight.commons.dli.enums.RouteType;
import br.skylight.commons.dli.enums.WaypointSpeedType;
import br.skylight.commons.dli.mission.AVPositionWaypoint;
import br.skylight.commons.dli.mission.AVRoute;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.vehicle.VehicleConfigurationMessage;
import br.skylight.commons.dli.vehicle.VehicleOperatingModeCommand;
import br.skylight.commons.dli.vehicle.VehicleSteeringCommand;
import br.skylight.commons.infra.TimedBoolean;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.mapkit.MapActionListenerAdapter;
import br.skylight.cucs.mapkit.MapElement;
import br.skylight.cucs.mapkit.MapElementBridge;
import br.skylight.cucs.mapkit.MapElementGroup;
import br.skylight.cucs.mapkit.MapKit;
import br.skylight.cucs.mapkit.painters.LayerPainter;
import br.skylight.cucs.plugins.controlmap2d.ControlMapExtensionPoint;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.missionplan.MissionPlanWaypointDialog;
import br.skylight.cucs.plugins.subscriber.MissionListener;

@ExtensionPointImplementation(extensionPointDefinition=ControlMapExtensionPoint.class)
public class MissionMapExtensionPointImpl extends ControlMapExtensionPoint implements MissionListener {

	//prefixes for missionId
	public static final String PREFIX_MISSION = "mission-";

	private List<MissionPlanPainter> missionPlanPainters = new ArrayList<MissionPlanPainter>();
	private TimedBoolean dragTimer = new TimedBoolean(100);

	private int routeCounter = 1;
	
	@ServiceInjection
	public VehicleControlService vehicleControlService;
	
	@ServiceInjection
	public MessagingService messagingService;
	
	@ServiceInjection
	public PluginManager pluginManager;
	
	@Override
	public void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMissionListener(this);
		dragTimer.reset();
		reloadAllMissions();
	}
	
	private void removeAllMissions() {
		if(getMapKit()!=null) {
//			getMapKit().clearSelection();
			getMapKit().removeMapElementGroupsByIdPrefix(PREFIX_MISSION);
			
			//remove all mission painters
			synchronized(missionPlanPainters) {
				for (LayerPainter p : missionPlanPainters) {
					getMapKit().removeCustomPainter(p);
				}
				missionPlanPainters.clear();
			}
			
			getMapKit().updateUI();
		}
	}

	private void reloadAllMissions() {
//		System.out.println("MISSION RELOAD ALL");
		if(getMapKit()!=null) {
			//rebuild mission plan elements in control map
			removeAllMissions();
			
			//waypoint defs
			for (Vehicle v : vehicleControlService.getKnownVehicles().values()) {
				if(v.getMission()!=null) {
					v.getMission().computeWaypointsMap();
					
					//create mission painters
					MissionPlanPainter mp = new MissionPlanPainter(getMapKit());
					mp.setMission(v.getMission());
					getMapKit().addCustomPainter(mp);
					missionPlanPainters.add(mp);
					
					//create waypoint elements
					for (WaypointDef wd : v.getMission().getOrderedWaypoints()) {
						createMapElementsForWaypoint(wd);
					}
					
					routeCounter = v.getMission().getRoutes().size() + 1;
				}
			}
			getMapKit().updateUI();
		}

//		System.out.println("=========");
//		for (Entry<Object,MapElementGroup<? extends MapElement>> g : getMapKit().getMapElementGroups().entrySet()) {
//			System.out.println(g.getKey() + "=" + g.getValue());
//		};
//		System.out.println("---------");
//		for (Entry<Object,MapElementGroup<? extends MapElement>> g : getMapKit().getElementGroups().entrySet()) {
//			for (MapElement me : g.getValue().getElements()) {
//				System.out.println(me.getPosition().getLatitude() + ", " + me.getPosition().getLongitude());
//			}
//		};
	}

	private void createMapElementsForWaypoint(WaypointDef wd) {
		if(wd.getPositionWaypoint()!=null) {
			MapElementGroup<WaypointMapElement> group = resolveMissionWaypointGroup(wd.getMission());
			group.setEnabledToAddElements(false);
			WaypointMapElement me = new WaypointMapElement(wd);
			me.setPosition(new GeoPosition(Math.toDegrees(wd.getPositionWaypoint().getWaypointToLatitudeOrRelativeY()),
										   Math.toDegrees(wd.getPositionWaypoint().getWaypointToLongitudeOrRelativeX())));
			me.setAltitude(wd.getPositionWaypoint().getWaypointToAltitude());
			group.addElement(me, -1, WaypointMapElement.class);
		}
	}

	//subject may be WaypointDef, Region
	private void handleMissionEvent(Mission mission, Object subject, EventType type) {
		if(getMapKit()!=null) {
			if(type.equals(EventType.SELECTED) 
				|| type.equals(EventType.CREATED)
				|| type.equals(EventType.UPDATED)) {
				reloadAllMissions();
				
				if(subject instanceof AVPositionWaypoint) {
					selectWaypoint((AVPositionWaypoint)subject);
				}
	
			} else if(type.equals(EventType.DESELECTED)) {
				getMapKit().clearSelection();
			}
			
			getMapKit().updateUI();
		}
	}

	private void selectWaypoint(AVPositionWaypoint subject) {
		for(MapElementGroup g: new ArrayList<MapElementGroup>(getMapKit().getMapElementGroups().values())) {
			for(Object o: g.getElements()) {
				if(o instanceof WaypointMapElement) {
					WaypointMapElement w = (WaypointMapElement)o;
					if(w.getWaypointDef().getPositionWaypoint().equals(subject)) {
						//select waypoint
						getMapKit().selectElement(w);
						break;
					}
				}
			}
		}
	}

	@Override
	public void onVehicleEvent(Vehicle av, EventType type) {
		super.onVehicleEvent(av, type);
		reloadAllMissions();
	}
	
	@Override
	public void onMissionEvent(Mission mission, EventType type) {
		handleMissionEvent(mission, null, type);
	}

	@Override
	public void onWaypointEvent(Mission mission, AVPositionWaypoint pw, EventType type) {
		handleMissionEvent(mission, pw, type);
	}
	
	@Override
	public void setMapKit(MapKit mapKit) {
		super.setMapKit(mapKit);
        mapKit.addMapActionListener(new MapActionListenerAdapter<MapElement>() {
			@Override
			public void onElementEvent(MapElement mapElement, EventType eventType) {
				if(mapElement instanceof WaypointMapElement) {
					WaypointMapElement me = (WaypointMapElement)mapElement;
					AVPositionWaypoint pw = me.getWaypointDef().getPositionWaypoint();
					if(eventType.equals(EventType.SELECTED)) {
						if(!me.getWaypointDef().getMission().getVehicle().equals(getCurrentVehicle())) {
							subscriberService.notifyVehicleEvent(me.getWaypointDef().getMission().getVehicle(), EventType.SELECTED, null);
							selectWaypoint(pw);
						}
					}
					if(!eventType.equals(EventType.SELECTED) && !eventType.equals(EventType.DESELECTED)) {
						subscriberService.notifyMissionWaypointEvent(me.getWaypointDef().getMission(), pw, eventType, getThis());
					}
//					System.out.println("NOTIFY");
					notifyMissionEvent(me.getWaypointDef().getMission(), eventType);
				}
//				System.out.println("ELEMENT EVENT " + mapElement);
				getMapKit().updateUI();
			}
			@Override
			public void onElementDragged(MapElement me) {
				if(dragTimer.checkTrue()) {
					onElementEvent(me, EventType.UPDATED);
				}
			}
			@Override
			public void onElementDoubleClicked(MapElement me) {
				if(getSelectedWaypointDef()!=null) {
					showWaypointPropertiesDialog();
				}
			}
		});
	}

	protected void notifyMissionEvent(Mission mission, EventType eventType) {
		if(eventType.equals(EventType.CREATED) || eventType.equals(EventType.DELETED)) {
			subscriberService.notifyMissionEvent(mission, eventType, getThis());
//			reloadAllMissions();
		} else if(eventType.equals(EventType.UPDATED)) {
			subscriberService.notifyMissionEvent(mission, eventType, getThis());
		}
	}

	protected void showWaypointPropertiesDialog() {
		MissionPlanWaypointDialog d = new MissionPlanWaypointDialog(null);
		WaypointDef sw = getSelectedWaypointDef();
		Vehicle v = vehicleControlService.getKnownVehicles().get(sw.getPositionWaypoint().getVehicleID());
		d.showDialog(v, sw.getWaypointNumber());
		reloadAllMissions();
		selectWaypoint(sw.getPositionWaypoint());
	}

	private WaypointDef getSelectedWaypointDef() {
		MapElement me = getMapKit().getSelectedElement();
		if(me instanceof WaypointMapElement) {
			return ((WaypointMapElement) me).getWaypointDef();
		} else {
			return null;
		}
	}

	private WaypointDef getSelectedWaypointDefButton3() {
		MapElement me = getMapKit().getLastSelectedButton3();
		if(me instanceof WaypointMapElement) {
			return ((WaypointMapElement) me).getWaypointDef();
		} else {
			return null;
		}
	}
	
	private MissionMapExtensionPointImpl getThis() {
		return this;
	}
	
	@Override
	public List<JMenuItem> prepareContextMenuItems(final GeoPosition clickPosition) {
		List<JMenuItem> items = new ArrayList<JMenuItem>();

		if(getCurrentVehicle()!=null && getCurrentVehicle().getMission()!=null) {
			
			if(getSelectedWaypointDefButton3()==null) {
				//NEW WAYPOINT
				JMenuItem mi1 = new JMenuItem("Add new waypoint here");
				mi1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						MapElementGroup<WaypointMapElement> g = resolveMissionWaypointGroup(getCurrentVehicle().getMission());
						WaypointMapElement wme = g.createElement(clickPosition, Float.NaN, WaypointMapElement.class);
						reloadAllMissions();
						subscriberService.notifyMissionWaypointEvent(getCurrentVehicle().getMission(), wme.getWaypointDef().getPositionWaypoint(), EventType.CREATED, getThis());
						handleMissionEvent(getCurrentVehicle().getMission(), wme.getWaypointDef().getPositionWaypoint(), EventType.CREATED);
					}
				});
				items.add(mi1);
			}
			
			if(getSelectedWaypointDef()!=null) {
				//DEFINE RIGHT CLICKED WAYPOINT TO BE THE NEXT WAYPOINT OF THE SELECTED WAYPOINT
				if(getSelectedWaypointDefButton3()!=null) {
					//a different item (not the selected one) is being right clicked
					if(!getSelectedWaypointDef().equals(getSelectedWaypointDefButton3())) {
						//'SET THIS WAYPOINT AS NEXT'
						JMenuItem mi3 = new JMenuItem("Set this waypoint as next waypoint");
						mi3.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								AVPositionWaypoint sw = getSelectedWaypointDef().getPositionWaypoint();
								sw.setNextWaypoint(getSelectedWaypointDefButton3().getWaypointNumber());
								reloadAllMissions();
								selectWaypoint(sw);
							}
						});
						items.add(mi3);
						
					//the item selected with right button is the same as the currently selected
					} else {
						//UPLOAD SELECTED WAYPOINT TO VEHICLE
						JMenuItem mi4 = new JMenuItem(new AbstractAction() {
							public void actionPerformed(ActionEvent ae) {
								//cancel any transfer in progress
								vehicleControlService.sendMissionUploadCommand(getCurrentVehicle().getVehicleID().getVehicleID(), "", MissionPlanMode.CANCEL_UPLOAD_OR_DOWNLOAD);
								
								//upload SINGLE WAYPOINT
								vehicleControlService.sendWaypointDefToVehicle(getSelectedWaypointDef(), getCurrentVehicle().getVehicleID().getVehicleID());

								//load waypoints to vehicle
								vehicleControlService.sendMissionUploadCommand(getCurrentVehicle().getVehicleID().getVehicleID(), "", MissionPlanMode.LOAD_MISSION);
							}
						});
						mi4.setText("Upload waypoint");
						items.add(mi4);

						//WAYPOINT PROPERTIES
						JMenuItem mi2 = new JMenuItem("Waypoint properties");
						mi2.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								showWaypointPropertiesDialog();
							}
						});
						items.add(mi2);

						//ACTIVATE WAYPOINT MODE FROM HERE
						JMenuItem mi3 = new JMenuItem("Goto waypoint now");
						mi3.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								//send steering command to indicate next waypoint
								VehicleSteeringCommand ms = vehicleControlService.resolveVehicleSteeringCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID());
								ms.setCommandedWaypointNumber(getSelectedWaypointDef().getWaypointNumber());
								vehicleControlService.sendVehicleSteeringCommand(ms);
								
								//activate waypoint mode
								VehicleOperatingModeCommand m = messagingService.resolveMessageForSending(VehicleOperatingModeCommand.class);
								m.setSelectFlightPathControlMode(FlightPathControlMode.WAYPOINT);
								m.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
								messagingService.sendMessage(m);
							}
						});
						items.add(mi3);
					}
				}
			}
			
		}
		
		return items;
	}

	private MapElementGroup<WaypointMapElement> resolveMissionWaypointGroup(final Mission mission) {
		String groupId = PREFIX_MISSION+mission.getMissionID();
		MapElementGroup<WaypointMapElement> g = getMapKit().getMapElementGroup(groupId, WaypointMapElement.class);
		if(g==null) {
			g = new MapElementGroup<WaypointMapElement>(getMapKit(),
					"Waypoint for mission " + mission.getMissionID(), 0,
					new MapElementBridge<WaypointMapElement>() {
						@Override
						public WaypointMapElement createMapElement(GeoPosition position, float altitude, int elementIndex, MapElementGroup<WaypointMapElement> group) {
							AVPositionWaypoint pw = new AVPositionWaypoint();

							//a waypoint is selected. use its information as basis
							int insertAfter = 0;
							if(getSelectedWaypointDef()!=null) {
								pw = (AVPositionWaypoint)getSelectedWaypointDef().getPositionWaypoint().createCopy();
								insertAfter = getSelectedWaypointDef().getPositionWaypoint().getWaypointNumber();
								
							//no waypoint is selected. create with default values
							} else {
								VehicleConfigurationMessage vc = mission.getVehicle().getVehicleConfiguration();
								if(vc!=null) {
									pw.setWaypointToSpeed(vc.getOptimumCruiseIndicatedAirspeed());
									pw.setWaypointSpeedType(WaypointSpeedType.INDICATED_AIRSPEED);
								}
							}
							
							if(insertAfter==0) {
								if(JOptionPane.OK_OPTION==JOptionPane.showConfirmDialog(null, "Create a new route from here?")) {
									//create a new route because this waypoint is starting a new path
									AVRoute route = new AVRoute();
									insertAfter = mission.getHighestWaypointNumber()+1;
									route.setInitialWaypointNumber(insertAfter);
									route.setRouteID("Route " + routeCounter++);
									route.setRouteType(RouteType.FLIGHT);
									route.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
									mission.getRoutes().add(route);
								}
							}
							mission.insertWaypoint(pw, insertAfter);
							mission.normalizeWaypointNumbers();
							
							WaypointDef wd = new WaypointDef();
							wd.setMission(mission);
							wd.setWaypointNumber(pw.getWaypointNumber());
							wd.setPositionWaypoint(pw);
							pw.setVehicleID(mission.getVehicle().getVehicleID().getVehicleID());
							
							return new WaypointMapElement(wd);
						};
						@Override
						public void onElementDeleted(WaypointMapElement element, MapElementGroup<WaypointMapElement> group) {
							mission.removeWaypointAt(element.getWaypointDef().getWaypointNumber(), true);
							mission.normalizeWaypointNumbers();
						}
					}, new WaypointPainter()
				);
			getMapKit().addMapElementGroup(g, groupId);
		}
		return g;
	}
	
	@Override
	public boolean isCompatibleWithVehicle(Vehicle vehicle) {
		return true;
	}

}
