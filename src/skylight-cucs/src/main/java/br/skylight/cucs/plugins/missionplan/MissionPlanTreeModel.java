package br.skylight.cucs.plugins.missionplan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jdesktop.swingx.treetable.TreeTableNode;

import br.skylight.commons.Mission;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.RouteType;
import br.skylight.commons.dli.enums.TurnType;
import br.skylight.commons.dli.enums.WaypointSpeedType;
import br.skylight.commons.dli.mission.AVPositionWaypoint;
import br.skylight.commons.dli.mission.AVRoute;
import br.skylight.cucs.widgets.tables.ObjectToColumnAdapter;
import br.skylight.cucs.widgets.tables.TypedTreeTableModel;
import br.skylight.cucs.widgets.tables.TypedTreeTableNode;

public class MissionPlanTreeModel extends TypedTreeTableModel {

	private ObjectToColumnAdapter<AVPositionWaypoint> positionConverter;
	private ObjectToColumnAdapter<AVRoute> routeConverter;
	private ObjectToColumnAdapter<String> stringConverter;
	private List<MissionPlanTreeModelListener> listeners = new ArrayList<MissionPlanTreeModelListener>();

	private Mission mission;
	
	public MissionPlanTreeModel() {
		positionConverter = new ObjectToColumnAdapter<AVPositionWaypoint>() {
			@Override
			public Object getValueAt(AVPositionWaypoint pw, int column) {
				if(column==0) {
					return pw.getWaypointNumber() + (mission.computeWaypointsMap().get(pw.getWaypointNumber()).isExtended()?"*":"");
				} else if(column==1) {
					return pw.getWaypointAltitudeType();
				} else if(column==2) {
					return pw.getWaypointToAltitude();
				} else if(column==3) {
					return pw.getWaypointSpeedType();
				} else if(column==4) {
					return pw.getWaypointToSpeed();
				} else if(column==5) {
					return pw.getArrivalTime();
				} else if(column==6) {
					return Math.toDegrees(pw.getWaypointToLatitudeOrRelativeY());
				} else if(column==7) {
					return Math.toDegrees(pw.getWaypointToLongitudeOrRelativeX());
				} else if(column==8) {
					return pw.getTurnType();
				} else if(column==9) {
					return pw.getContingencyWaypointA();
				} else if(column==10) {
					return pw.getContingencyWaypointB();
				} else if(column==11) {
					return pw.getNextWaypoint();
				} else if(column==12) {
					return "Edit";
				} else {
					return "";
				}
			}
			
			@Override
			public void setValueAt(AVPositionWaypoint pw, Object value, int column) {
				if(column==0) {
					//do nothing. numbers are defined by position in table
				} else if(column==1) {
					pw.setWaypointAltitudeType((AltitudeType)value);
				} else if(column==2) {
					pw.setWaypointToAltitude((Float)value);
				} else if(column==3) {
					pw.setWaypointSpeedType((WaypointSpeedType)value);
				} else if(column==4) {
					pw.setWaypointToSpeed((Float)value);
				} else if(column==5) {
					pw.setArrivalTime((Double)value);
				} else if(column==6) {
					pw.setWaypointToLatitudeOrRelativeY(Math.toRadians((Double)value));
				} else if(column==7) {
					pw.setWaypointToLongitudeOrRelativeX(Math.toRadians((Double)value));
				} else if(column==8) {
					pw.setTurnType((TurnType)value);
				} else if(column==9) {
					pw.setContingencyWaypointA((Integer)value);
				} else if(column==10) {
					pw.setContingencyWaypointB((Integer)value);
				} else if(column==11) {
					pw.setNextWaypoint((Integer)value);
				} else {
					//do nothing
				}
				for (MissionPlanTreeModelListener l : listeners) {
					l.onPositionWaypointUpdated(pw, value, column);
				}
			}
		};
		routeConverter = new ObjectToColumnAdapter<AVRoute>() {
			public Object getValueAt(AVRoute route, int columnIndex) {
				if(columnIndex==0) {
					return route.getRouteID();
				} else if(columnIndex==12) {
					return route.getRouteType();
				} else {
					return "";
				}
			}
			public void setValueAt(AVRoute route, Object value, int columnIndex) {
				if(columnIndex==0) {
					route.setRouteID(value.toString());
				} else if(columnIndex==12) {
					route.setRouteType((RouteType)value);
				}
				for (MissionPlanTreeModelListener l : listeners) {
					l.onRouteUpdated(route, value, columnIndex);
				}
			};
		};
		stringConverter = new ObjectToColumnAdapter<String>() {
			@Override
			public Object getValueAt(String object, int column) {
				if(column==0) {
					return object;
				} else {
					return "";
				}
			}
			@Override
			public void setValueAt(String object, Object value, int columnIndex) {
				throw new UnsupportedOperationException("Not implemented");
			}
		};
		setRoot(new TypedTreeTableNode<String>("No mission loaded", stringConverter));
	}

	@Override
	public String getColumnName(int column) {
		if(column==0) {
			return "Name";
		} else if(column==1) {
			return "Alt. type";
		} else if(column==2) {
			return "Altitude";
		} else if(column==3) {
			return "Speed type";
		} else if(column==4) {
			return "Speed";
		} else if(column==5) {
			return "Arrival time";
		} else if(column==6) {
			return "Latitude";
		} else if(column==7) {
			return "Longitude";
		} else if(column==8) {
			return "Turn type";
		} else if(column==9) {
			return "CWA";
		} else if(column==10) {
			return "CWB";
		} else if(column==11) {
			return "Next #";
		} else if(column==12) {
			return "Edit";
		} else {
			return "";
		}
	}
	
	public void updateMission(Mission mission) {
		this.mission = mission;
		if(mission!=null) {
			setRoot(new TypedTreeTableNode<String>("Mission " + mission.getMissionID(), stringConverter));
			mission.computeWaypointsMap();
			TypedTreeTableNode r = (TypedTreeTableNode)getRoot();
			
			//sort position waypoints
			Collections.sort(mission.getPositionWaypoints(), new Comparator<AVPositionWaypoint>() {
				@Override
				public int compare(AVPositionWaypoint o1, AVPositionWaypoint o2) {
					if(o1.getWaypointNumber()<o2.getWaypointNumber()) {
						return -1;
					} else if(o1.getWaypointNumber()>o2.getWaypointNumber()) {
						return 1;
					} else {
						return 0;
					}
				}
			});

			//sort routes
			Collections.sort(mission.getRoutes(), new Comparator<AVRoute>() {
				@Override
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

			//create waypoints
			for(AVPositionWaypoint pw : mission.getPositionWaypoints()) {
				TypedTreeTableNode nr = r;
				//look for waypoint route
				for(AVRoute route : mission.getRoutes()) {
					if(pw.getWaypointNumber()>=route.getInitialWaypointNumber()) {
						nr = resolveNodeForRoute(route);
					}
				}
				nr.add(createWaypointNode(pw));
			}
			
		} else {
			setRoot(new TypedTreeTableNode<String>("No mission loaded", stringConverter));
		}
		for (MissionPlanTreeModelListener l : listeners) {
			l.onMissionUpdated(mission);
		}
	}
	
	private TypedTreeTableNode<AVPositionWaypoint> createWaypointNode(AVPositionWaypoint pw) {
		//create waypoint node
		TypedTreeTableNode<AVPositionWaypoint> pn = new TypedTreeTableNode<AVPositionWaypoint>(pw, positionConverter);
		pn.setEditable(1, true);
		pn.setEditable(2, true);
		pn.setEditable(3, true);
		pn.setEditable(4, true);
		pn.setEditable(5, true);
		pn.setEditable(6, true);
		pn.setEditable(7, true);
		pn.setEditable(8, true);
		pn.setEditable(9, true);
		pn.setEditable(10, true);
		pn.setEditable(11, true);
		pn.setEditable(12, true);
		return pn;
	}

	public TypedTreeTableNode resolveNodeForRoute(AVRoute ar) {
		//look for existing node in tree
		TypedTreeTableNode r = (TypedTreeTableNode)getRoot();
		for(int i=0; i<r.getChildCount(); i++) {
			TreeTableNode tn = r.getChildAt(i);
			if(tn instanceof TypedTreeTableNode) {
				TypedTreeTableNode tnn = (TypedTreeTableNode)tn;
				if(tnn.getUserObject() instanceof AVRoute) {
					AVRoute fr = (AVRoute)tnn.getUserObject();
					if(fr.equals(ar)) {
						return tnn;
					}
				}
			}
		}

		//create route node
		TypedTreeTableNode<AVRoute> nr = new TypedTreeTableNode<AVRoute>(ar, routeConverter);
		nr.setEditable(0, true);
		nr.setEditable(12, true);
		r.add(nr);
		return nr;
	}

	@Override
	public int getColumnCount() {
		return 13;
	}
	
	public void addMissionPlanTreeModelListener(MissionPlanTreeModelListener listener) {
		listeners.add(listener);
	}

}
