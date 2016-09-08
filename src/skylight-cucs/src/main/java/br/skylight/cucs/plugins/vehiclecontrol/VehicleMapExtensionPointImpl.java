package br.skylight.cucs.plugins.vehiclecontrol;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.EventType;
import br.skylight.commons.StringHelper;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.mission.FromToNextWaypointStates;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.vehicle.AirAndGroundRelativeStates;
import br.skylight.commons.dli.vehicle.InertialStates;
import br.skylight.commons.infra.TimedBoolean;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.mapkit.DefaultMapElement;
import br.skylight.cucs.mapkit.MapActionListenerAdapter;
import br.skylight.cucs.mapkit.MapElement;
import br.skylight.cucs.mapkit.MapElementBridge;
import br.skylight.cucs.mapkit.MapElementGroup;
import br.skylight.cucs.mapkit.MapKit;
import br.skylight.cucs.mapkit.painters.PathPainter;
import br.skylight.cucs.plugins.controlmap2d.ControlMapExtensionPoint;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.subscriber.SubscriberService;

@ExtensionPointImplementation(extensionPointDefinition=ControlMapExtensionPoint.class)
public class VehicleMapExtensionPointImpl extends ControlMapExtensionPoint implements MessageListener {

	//prefixes for vehicleId
	private static final String PREFIX_PATH = "path-";
	private static final String PREFIX_VEHICLE = "vehicle-";
	private static final String PREFIX_FROM_TO_NEXT_LOCATION = "fromToNextLocationGroup-";

//	private JSpinner referenceAltitude = null;
	private JButton clearPath = null;
	private JCheckBox centerOnUpdate = null;
	private JPanel toolComponent;  //  @jve:decl-index=0:visual-constraint="11,11"
	private TimedBoolean pathTimeout = new TimedBoolean(500);
	private int maxPathElements = 1000;
	private WindDirectionPainter windDirectionPainter;

	@ServiceInjection
	public SubscriberService subscriberService;

	@ServiceInjection
	public VehicleControlService vehicleControlService;

	@ServiceInjection
	public PluginManager pluginManager;
	
	@Override
	public void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMessageListener(MessageType.M101, this);
		subscriberService.addMessageListener(MessageType.M102, this);
		subscriberService.addMessageListener(MessageType.M110, this);
		pathTimeout.reset();
	}

	@Override
	public void onDeactivate() throws Exception {
		super.onDeactivate();
		subscriberService.removeMessageListener(MessageType.M101, this);
	}
	
	@Override
	public void onMessageReceived(Message message) {
		if(getMapKit()==null) {
			return;
		}
		
		//M101
		if(message instanceof InertialStates) {
			//update inertial states on vehicle map element
			InertialStates m = (InertialStates)message;
			GeoPosition pos = new GeoPosition(Math.toDegrees(m.getLatitude()), Math.toDegrees(m.getLongitude()));

			//vehicle element
			VehicleMapElement vme = resolveVehicleMapElement(m.getVehicleID());
			vme.setPosition(pos);

			//create path line element
			if(pathTimeout.checkTrue()) {
				MapElementGroup<DefaultMapElement> path = resolveVehiclePathGroup(m.getVehicleID());
				getMapKit().createMapElement(path.getId(), pos, m.getAltitude(), DefaultMapElement.class, 0);
				int excessQtty = path.getElements().size()-maxPathElements;
				if(excessQtty>3) {
					for (int i=0; i<excessQtty; i++) {
						path.getElements().remove(path.getElements().size()-1);
					}
				}
			}

			//center map on currently selected vehicle
			if(getCenterOnUpdate().isSelected() && getCurrentVehicle()!=null && message.getVehicleID()==getCurrentVehicle().getVehicleID().getVehicleID()) {
				getMapKit().setAddressLocation(pos);
			}
			updateGUI();

		//M102
		} else if(message instanceof AirAndGroundRelativeStates) {
			AirAndGroundRelativeStates m = (AirAndGroundRelativeStates)message;
			windDirectionPainter.setAirAndGroundRelativeStates(m);
			
		//M110
		} else if(message instanceof FromToNextWaypointStates) {
			FromToNextWaypointStates m = (FromToNextWaypointStates)message;
			MapElementGroup<VehicleMapElement> g = resolveFromToNextLocationGroup(getMapKit(), vehicleControlService.resolveVehicle(m.getVehicleID()));
			GeoPosition pos = new GeoPosition(Math.toDegrees(m.getToWaypointLatitude()), Math.toDegrees(m.getToWaypointLongitude()));
			if(g.getElements().size()==0) {
				g.createElement(pos, m.getToWaypointAltitude(), VehicleMapElement.class);
			} else {
				g.getElement(0).setAltitude(m.getToWaypointAltitude());
				g.getElement(0).setPosition(pos);
			}
			updateGUI();
		}
	}

	private VehicleMapElement resolveVehicleMapElement(int vehicleID) {
		String groupId = PREFIX_VEHICLE+vehicleID;
		MapElementGroup<VehicleMapElement> g = getMapKit().getMapElementGroup(groupId, VehicleMapElement.class);
		if(g==null) {
			final Vehicle v = vehicleControlService.resolveVehicle(vehicleID);
			g = new MapElementGroup<VehicleMapElement>(getMapKit(),
					"Vehicle " + v.getLabel(), 0,
					new MapElementBridge<VehicleMapElement>() {
						@Override
						public VehicleMapElement createMapElement(GeoPosition position, float altitude, int elementIndex, MapElementGroup<VehicleMapElement> group) {
							return new VehicleMapElement(v);
						};
					}, new VehicleMapElementPainter()
				);
			getMapKit().addMapElementGroup(g, groupId);
		}
		synchronized(g.getElements()) {
			if(g.getElements().size()==0) {
				getMapKit().createMapElement(g.getId(), new GeoPosition(0,0), 0, VehicleMapElement.class).setEditable(false);
			}
		}
		return g.getElements().get(0);
	}

	private MapElementGroup<DefaultMapElement> resolveVehiclePathGroup(int vehicleID) {
		String groupId = PREFIX_PATH+vehicleID;
		MapElementGroup<DefaultMapElement> g = getMapKit().getMapElementGroup(groupId, DefaultMapElement.class);
		if(g==null) {
			PathPainter<DefaultMapElement> pp = new PathPainter<DefaultMapElement>();
			pp.setLineColor(Color.YELLOW);
			pp.setShowVertex(false);
			
			final Vehicle v = vehicleControlService.resolveVehicle(vehicleID);
			g = new MapElementGroup<DefaultMapElement>(getMapKit(),
					"Vehicle path for " + v.getLabel(), 0,
					new MapElementBridge<DefaultMapElement>() {
						@Override
						public DefaultMapElement createMapElement(GeoPosition position, float altitude, int elementIndex, MapElementGroup<DefaultMapElement> group) {
							return new DefaultMapElement();
						};
					}, pp
				);
			getMapKit().addMapElementGroup(g, groupId);
		}
		return g;
	}
	
	public void setMaxPathElements(int maxPathElements) {
		this.maxPathElements = maxPathElements;
	}

	/**
	 * This method initializes clearPath	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getClearPath() {
		if (clearPath == null) {
			clearPath = new JButton();
			clearPath.setText("Clear path");
			clearPath.setFont(new Font("Dialog", Font.PLAIN, 10));
			clearPath.setMargin(new Insets(0,0,0,0));
			clearPath.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					for (Entry<Object,MapElementGroup<? extends MapElement>> ge : getMapKit().getMapElementGroups().entrySet()) {
						if(ge.getKey().toString().startsWith(PREFIX_PATH)) {
							ge.getValue().clearElements();
						}
					}
					getMapKit().updateUI();
				}
			});
		}
		return clearPath;
	}

	/**
	 * This method initializes centerOnUpdate	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getCenterOnUpdate() {
		if (centerOnUpdate == null) {
			centerOnUpdate = new JCheckBox();
			centerOnUpdate.setText("center on plane");
			centerOnUpdate.setEnabled(true);
			centerOnUpdate.setSelected(true);
			centerOnUpdate.setFont(new Font("Dialog", Font.PLAIN, 10));
		}
		return centerOnUpdate;
	}
	
	@Override
	public Component getToolComponent() {
		if(toolComponent==null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 3;
			gridBagConstraints3.insets = new Insets(3, 3, 3, 5);
			gridBagConstraints3.gridy = 0;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 2;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.anchor = GridBagConstraints.EAST;
			gridBagConstraints2.insets = new Insets(3, 0, 3, 0);
			gridBagConstraints2.gridy = 0;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.insets = new Insets(3, 3, 3, 0);
			gridBagConstraints1.gridy = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.insets = new Insets(3, 5, 3, 0);
			gridBagConstraints.gridy = 0;
			toolComponent = new JPanel();
			toolComponent.setLayout(new GridBagLayout());
			toolComponent.setSize(new Dimension(383, 30));
			toolComponent.add(getClearPath(), gridBagConstraints);
			toolComponent.add(getCenterOnUpdate(), gridBagConstraints1);
		}
		return toolComponent;
	}
	
	@Override
	public void setMapKit(MapKit mapKit) {
		super.setMapKit(mapKit);
        getMapKit().addMapActionListener(new MapActionListenerAdapter<MapElement>() {
			@Override
			public void onElementEvent(MapElement mapElement, EventType eventType) {
				if(mapElement instanceof VehicleMapElement) {
					subscriberService.notifyVehicleEvent(((VehicleMapElement)mapElement).getVehicle(), EventType.SELECTED, null);
				}
			}
		});
        windDirectionPainter = new WindDirectionPainter();
		getMapKit().addCustomPainter(windDirectionPainter);
	}

	@Override
	public boolean isCompatibleWithVehicle(Vehicle vehicle) {
		return true;
	}

	public static MapElementGroup<VehicleMapElement> resolveFromToNextLocationGroup(MapKit mapKit, final Vehicle vehicle) {
		String groupId = PREFIX_FROM_TO_NEXT_LOCATION + vehicle.getVehicleID().getVehicleID();
		MapElementGroup<VehicleMapElement> g = mapKit.getMapElementGroup(groupId, VehicleMapElement.class);
		if(g==null) {
			g = new MapElementGroup<VehicleMapElement>(mapKit,
					"From-to-next locations for vehicle " + StringHelper.formatId(vehicle.getVehicleID().getVehicleID()), 0,
					new MapElementBridge<VehicleMapElement>() {
						@Override
						public VehicleMapElement createMapElement(GeoPosition position, float altitude, int elementIndex, MapElementGroup<VehicleMapElement> group) {
							return new VehicleMapElement(vehicle);
						};
					}, new FromToNextLocationPainter()
				);
			g.setMaxAllowedElements(1);
			mapKit.addMapElementGroup(g, groupId);
			g.createElement(new GeoPosition(0,0), 0, VehicleMapElement.class);
		}
		return g;
	}
	
}
