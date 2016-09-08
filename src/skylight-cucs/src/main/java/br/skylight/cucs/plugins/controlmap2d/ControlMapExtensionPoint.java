package br.skylight.cucs.plugins.controlmap2d;

import java.awt.Component;
import java.util.List;

import javax.swing.JMenuItem;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.EventType;
import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.annotations.ExtensionPointDefinition;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.mapkit.MapKit;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.plugins.subscriber.VehicleListener;

@ExtensionPointDefinition
public abstract class ControlMapExtensionPoint extends Worker implements VehicleListener {

	private MapKit mapKit;

	@ServiceInjection
	public SubscriberService subscriberService;

	private Vehicle currentVehicle;

	@Override
	public void onActivate() throws Exception {
		subscriberService.addVehicleListener(this);
	}
	
	@Override
	public void onDeactivate() throws Exception {
		subscriberService.removeVehicleListener(this);
	}
	
	public MapKit getMapKit() {
		return mapKit;
	}
	
	public void setMapKit(MapKit mapKit) {
		this.mapKit = mapKit;
	}
	
	public void updateGUI() {
		mapKit.updateUI();
	}

	@Override
	public void onVehicleEvent(Vehicle av, EventType type) {
		if(type.equals(EventType.SELECTED)) {
			currentVehicle = av;
		}
	}

	@Override
	public void onPayloadEvent(Payload p, EventType type) {
	}

	public Vehicle getCurrentVehicle() {
		return currentVehicle;
	}

	public List<JMenuItem> prepareContextMenuItems(GeoPosition clickPosition) {
		return null;
	}
	
	public Component getToolComponent() {
		return null;
	}
	
	public abstract boolean isCompatibleWithVehicle(Vehicle vehicle);
	
}
