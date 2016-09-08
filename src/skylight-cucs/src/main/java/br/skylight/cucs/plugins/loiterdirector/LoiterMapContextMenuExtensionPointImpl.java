package br.skylight.cucs.plugins.loiterdirector;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.EventType;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.FlightPathControlMode;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.vehicle.LoiterConfiguration;
import br.skylight.commons.dli.vehicle.VehicleOperatingModeCommand;
import br.skylight.commons.dli.vehicle.VehicleSteeringCommand;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.mapkit.MapActionListenerAdapter;
import br.skylight.cucs.mapkit.MapElement;
import br.skylight.cucs.mapkit.MapElementBridge;
import br.skylight.cucs.mapkit.MapElementGroup;
import br.skylight.cucs.mapkit.MapKit;
import br.skylight.cucs.plugins.controlmap2d.ControlMapExtensionPoint;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.subscriber.LoiterListener;

@ExtensionPointImplementation(extensionPointDefinition=ControlMapExtensionPoint.class)
public class LoiterMapContextMenuExtensionPointImpl extends ControlMapExtensionPoint implements LoiterListener {

	private static final String PREFIX_LOITER = "loiter-";
	
	@ServiceInjection
	public VehicleControlService vehicleControlService;
	
	@ServiceInjection
	public MessagingService messagingService;
	
	@Override
	public void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addLoiterListener(this);
	}

	@Override
	public void setMapKit(MapKit mapKit) {
		super.setMapKit(mapKit);
		getMapKit().addMapActionListener(new MapActionListenerAdapter<MapElement>() {
			@Override
			public void onElementEvent(MapElement mapElement, EventType eventType) {
				if(mapElement instanceof LoiterDirectorMapElement) {
					LoiterDirectorMapElement me = (LoiterDirectorMapElement)mapElement;
					LoiterConfiguration lc = me.getLoiterConfiguration();
					if(eventType.equals(EventType.UPDATED)) {
						//send steering for defining new loiter coordinates
						VehicleSteeringCommand vs = vehicleControlService.resolveVehicleSteeringCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID());
						vs.setLoiterPositionLatitude(lc.getLatitude());
						vs.setLoiterPositionLongitude(lc.getLongitude());
						vs.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
						vehicleControlService.sendVehicleSteeringCommand(vs);
					}
					subscriberService.notifyLoiterEvent(lc, eventType, getThis());
					getMapKit().updateUI();
				}
			}
        });
	}

	private LoiterMapContextMenuExtensionPointImpl getThis() {
		return this;
	}

	@Override
	public void onLoiterEvent(LoiterConfiguration lc, EventType type) {
		if(getMapKit()==null) return;
		MapElementGroup<LoiterMapElement> g = resolveLoiterGroup(lc.getVehicleID());
		
		if(type.equals(EventType.SELECTED) 
				|| type.equals(EventType.CREATED)
				|| type.equals(EventType.UPDATED)) {
			//create loiter map element
			if(g.getElements().size()==0) {
				LoiterMapElement me = new LoiterDirectorMapElement();
				me.setLoiterConfiguration(lc);
				g.addElement(me, -1, LoiterMapElement.class);
			} else {
				g.getElement(0).setLoiterConfiguration(lc);
			}

		} else if(type.equals(EventType.DESELECTED)) {
			getMapKit().clearSelection();

		} else if(type.equals(EventType.DELETED)) {
			getMapKit().clearSelection();
			getMapKit().removeMapElementGroup(g.getId());
		}
		
		getMapKit().updateUI();
	}

	@Override
	public List<JMenuItem> prepareContextMenuItems(final GeoPosition clickPosition) {
		List<JMenuItem> items = new ArrayList<JMenuItem>();
		if(getCurrentVehicle()!=null) {
			if(getMapKit().getLastSelectedButton3()==null) {
				JMenuItem mi1 = new JMenuItem("Loiter here now");
				mi1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						LoiterConfiguration lc = getCurrentVehicle().resolveLoiterConfiguration();
						lc.setLatitude(Math.toRadians(clickPosition.getLatitude()));
						lc.setLongitude(Math.toRadians(clickPosition.getLongitude()));
						lc.setAltitudeType(AltitudeType.AGL);
						lc.setLoiterAltitude(getCurrentVehicle().getCurrentAltitude(AltitudeType.AGL));
						
						//send steering for defining new loiter coordinates
						VehicleSteeringCommand vs = vehicleControlService.resolveVehicleSteeringCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID());
						vs.setLoiterPositionLatitude(lc.getLatitude());
						vs.setLoiterPositionLongitude(lc.getLongitude());
						vs.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
						vehicleControlService.sendVehicleSteeringCommand(vs);
						
						//send loiter configuration
						LoiterConfiguration slc = messagingService.resolveMessageForSending(LoiterConfiguration.class);
						slc.copyParametersFrom(lc);
						slc.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
						messagingService.sendMessage(slc);

						if(!getCurrentVehicle().isCurrentMode(FlightPathControlMode.LOITER)) {
							VehicleOperatingModeCommand m = messagingService.resolveMessageForSending(VehicleOperatingModeCommand.class);
							m.setSelectFlightPathControlMode(FlightPathControlMode.LOITER);
							m.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
							messagingService.sendMessage(m);
						}
						
						subscriberService.notifyLoiterEvent(lc, EventType.UPDATED, null);
					}
				});
				items.add(mi1);
			}
		}
		
		return items;
	}

	@Override
	public boolean isCompatibleWithVehicle(Vehicle vehicle) {
		return true;
	}

	private MapElementGroup<LoiterMapElement> resolveLoiterGroup(int vehicleID) {
		String groupId = PREFIX_LOITER+vehicleID;
		MapElementGroup<LoiterMapElement> g = getMapKit().getMapElementGroup(groupId, LoiterMapElement.class);
		if(g==null) {
			final Vehicle v = vehicleControlService.resolveVehicle(vehicleID);
			g = new MapElementGroup<LoiterMapElement>(getMapKit(),
					"Loiter for " + v.getLabel(), 0,
					new MapElementBridge<LoiterMapElement>() {
						@Override
						public LoiterMapElement createMapElement(GeoPosition position, float altitude, int elementIndex, MapElementGroup<LoiterMapElement> group) {
							return new LoiterDirectorMapElement();
						};
					}, new LoiterPainter<LoiterMapElement>()
				);
			g.setMaxAllowedElements(1);
			getMapKit().addMapElementGroup(g, groupId);
		}
		return g;
	}
	
}
