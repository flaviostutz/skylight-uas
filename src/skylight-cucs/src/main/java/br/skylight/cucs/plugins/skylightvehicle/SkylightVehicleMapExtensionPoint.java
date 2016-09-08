package br.skylight.cucs.plugins.skylightvehicle;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.StringHelper;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.enums.VehicleType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.skylight.MiscInfoMessage;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.mapkit.MapElementBridge;
import br.skylight.cucs.mapkit.MapElementGroup;
import br.skylight.cucs.mapkit.MapKit;
import br.skylight.cucs.plugins.controlmap2d.ControlMapExtensionPoint;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.plugins.vehiclecontrol.VehicleMapElement;

@ExtensionPointImplementation(extensionPointDefinition=ControlMapExtensionPoint.class)
public class SkylightVehicleMapExtensionPoint extends ControlMapExtensionPoint implements MessageListener {

	public static final String PREFIX_REFERENCE_POINTS = "referencePointGroup-";

	@ServiceInjection
	public SubscriberService subscriberService;

	@ServiceInjection
	public VehicleControlService vehicleControlService;

	@Override
	public void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMessageListener(MessageType.M2005, this);
	}
	
	@Override
	public void onMessageReceived(Message message) {
		if(getMapKit()!=null) {
			//M2005
			if(message instanceof MiscInfoMessage) {
				MiscInfoMessage m = (MiscInfoMessage)message;
				Vehicle vehicle = vehicleControlService.resolveVehicle(m.getVehicleID());
				MapElementGroup<VehicleMapElement> g = resolveVehicleReferencePointGroup(getMapKit(), vehicle);
				g.getElement(0).setPosition(new GeoPosition(Math.toDegrees(m.getCurrentTargetLatitude()), Math.toDegrees(m.getCurrentTargetLongitude())));
				g.getElement(0).setAltitude(m.getCurrentTargetAltitude());
				getMapKit().updateUI();
			}
		}
	}
	
	public static MapElementGroup<VehicleMapElement> resolveVehicleReferencePointGroup(MapKit mapKit, final Vehicle vehicle) {
		String groupId = PREFIX_REFERENCE_POINTS + vehicle.getVehicleID().getVehicleID();
		MapElementGroup<VehicleMapElement> g = mapKit.getMapElementGroup(groupId, VehicleMapElement.class);
		if(g==null) {
			g = new MapElementGroup<VehicleMapElement>(mapKit,
					"Reference point for vehicle " + StringHelper.formatId(vehicle.getVehicleID().getVehicleID()), 0,
					new MapElementBridge<VehicleMapElement>() {
						@Override
						public VehicleMapElement createMapElement(GeoPosition position, float altitude, int elementIndex, MapElementGroup<VehicleMapElement> group) {
							return new VehicleMapElement(vehicle);
						};
					}, new ReferencePointPainter()
				);
			g.setMaxAllowedElements(1);
			mapKit.addMapElementGroup(g, groupId);
			g.createElement(new GeoPosition(0,0), 0, VehicleMapElement.class);
		}
		return g;
	}

	@Override
	public boolean isCompatibleWithVehicle(Vehicle vehicle) {
		return vehicle!=null && vehicle.getVehicleID().getVehicleType().equals(VehicleType.TYPE_60);
	}

}
