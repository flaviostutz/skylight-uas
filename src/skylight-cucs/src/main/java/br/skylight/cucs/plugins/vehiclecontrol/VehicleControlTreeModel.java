package br.skylight.cucs.plugins.vehiclecontrol;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import br.skylight.commons.CUCSControl;
import br.skylight.commons.ControllableElement;
import br.skylight.commons.Payload;
import br.skylight.commons.StringHelper;
import br.skylight.commons.Vehicle;
import br.skylight.cucs.plugins.core.UserService;
import br.skylight.cucs.widgets.tables.ObjectToColumnAdapter;
import br.skylight.cucs.widgets.tables.TypedTreeTableModel;
import br.skylight.cucs.widgets.tables.TypedTreeTableNode;

public class VehicleControlTreeModel extends TypedTreeTableModel {

	private ObjectToColumnAdapter<Vehicle> vehicleConverter;
	private ObjectToColumnAdapter<Payload> payloadConverter;
	private ObjectToColumnAdapter<CUCS> cucsConverter;
	private ObjectToColumnAdapter<String> stringConverter;

	private Map<Integer, Vehicle> knownVehicles = new HashMap<Integer, Vehicle>();
	private Map<Integer,CUCS> knownCUCS = new HashMap<Integer, CUCS>();

	private UserService userService;
	
	public VehicleControlTreeModel(UserService userService) {
		super();
		this.userService = userService;
		vehicleConverter = new ObjectToColumnAdapter<Vehicle>() {
			@Override
			public Object getValueAt(Vehicle value, int column) {
				//name
				if(column==0) {
					return value.getLabel();
				//info
				} else if(column==1) {
					return value.getVehicleID().getVehicleType().getName();
				//controlled by
				} else if(column==2) {
					return getControlInfo(value);
				} else {
					return "";
				}
			}
			@Override
			public void setValueAt(Vehicle object, Object value, int columnIndex) {
				object.setName(value.toString());
			}
		};
		payloadConverter = new ObjectToColumnAdapter<Payload>() {
			@Override
			public Object getValueAt(Payload object, int column) {
				//name
				if(column==0) {
					return object.getLabel();
				//info
				} else if(column==1) {
					return object.getPayloadType().getName();
				//controlled by
				} else if(column==2) {
					return getControlInfo(object);
				} else {
					return "";
				}
			}
			@Override
			public void setValueAt(Payload object, Object value, int columnIndex) {
				object.setName(value.toString());
			}
		};
		cucsConverter = new ObjectToColumnAdapter<CUCS>() {
			@Override
			public Object getValueAt(CUCS object, int column) {
				if(column==0) {
					return object.getLabel();

				//controlled vehicles
				} else if(column==2) {
					for (Vehicle v : knownVehicles.values()) {
						CUCSControl cc = v.getCucsControls().get(object.getCucsId());
						if(cc!=null && cc.getGrantedLOIs().getLOIs().size()>0) {
							return v.getLabel() + " " + Arrays.deepToString(cc.getGrantedLOIs().getLOIs().toArray()) + "";
						}
					}
				}
				return "";
			}
			@Override
			public void setValueAt(CUCS object, Object value, int columnIndex) {
				object.setName(value.toString());
			}
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

		setRoot(new TypedTreeTableNode<String>("", stringConverter));
	}
	
	public void updateVehiclesAndOperators(Map<Integer, Vehicle> knownVehicles, Map<Integer,CUCS> knownCUCS) {
		this.knownVehicles = knownVehicles;
		this.knownCUCS = knownCUCS;
		TypedTreeTableNode<String> r = (TypedTreeTableNode<String>)getRoot();

		//KNOWN VEHICLES
		TypedTreeTableNode<String> vr = new TypedTreeTableNode<String>("Vehicles", stringConverter);
		addOrUpdate(r, vr);
		for (int vehicleId : knownVehicles.keySet()) {
			//vehicle
			Vehicle v = knownVehicles.get(vehicleId);
			TypedTreeTableNode<Vehicle> vn = new TypedTreeTableNode<Vehicle>(v, vehicleConverter);
			addOrUpdate(vr, vn);
			
			//payloads
			for (Payload p : v.getPayloads().values()) {
				TypedTreeTableNode<Payload> pn = new TypedTreeTableNode<Payload>(p, payloadConverter);
				addOrUpdate(vn, pn);
			}
		}
		
		//KNOWN STATIONS/OPERATORS
		TypedTreeTableNode<String> cr = new TypedTreeTableNode<String>("Operators", stringConverter);
		r.add(cr);
		for (int cucsId : knownCUCS.keySet()) {
			CUCS c = knownCUCS.get(cucsId);
			TypedTreeTableNode<CUCS> cn = new TypedTreeTableNode<CUCS>(c, cucsConverter);
			addOrUpdate(cr, cn);
		}
	}

	private void addOrUpdate(TypedTreeTableNode parent, TypedTreeTableNode node) {
		parent.add(node);
	}

	@Override
	public String getColumnName(int column) {
		if(column==0) {
			return "Name";
		} else if(column==1) {
			return "Info";
		} else if(column==2) {
			return "Control [LOI]";
		} else {
			return "";
		}
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	private String getControlInfo(ControllableElement ce) {
		int selfCucsId = userService.getCurrentCucsId();
		String info = getCucsControlInfo(selfCucsId, ce);
		if(info!=null) {
			return info;
		} else {
			//verify if another station controls this element
			for (Entry<Integer,CUCSControl> c0 : ce.getCucsControls().entrySet()) {
				return getCucsControlInfo(c0.getKey(), ce);
			}
		}
		return "";
	}

	private String getCucsControlInfo(int cucsId, ControllableElement ce) {
		String cucsName = StringHelper.formatId(cucsId);
		CUCSControl cc = ce.getCucsControls().get(cucsId);
		if(cc!=null && !cc.getGrantedLOIs().getLOIs().isEmpty()) {
			CUCS c = knownCUCS.get(cucsId);
			if(c!=null && c.getName()!=null) {
				cucsName = c.getName();
			}
			if(cucsId==userService.getCurrentCucsId()) {
				cucsName = "In Control";
			}
			return cucsName + " " + Arrays.deepToString(cc.getGrantedLOIs().getLOIs().toArray()) + "" + (ce.getCucsControls().size()>1?"...":"");
		} else {
			return null;
		}
	}

}
