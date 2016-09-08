package br.skylight.cucs.plugins.payload;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.Coordinates;
import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.enums.PayloadType;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.plugins.controlmap2d.ControlMapExtensionPoint;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.widgets.CUCSViewHelper;

@ExtensionPointImplementation(extensionPointDefinition=ControlMapExtensionPoint.class)
public class ActionPlanningMapExtensionPointImpl extends ControlMapExtensionPoint {

	@ServiceInjection
	public SubscriberService subscriberService;
	
	@Override
	public List<JMenuItem> prepareContextMenuItems(final GeoPosition clickPosition) {
		boolean ok = false;
		if(getCurrentVehicle()!=null) {
			for (Payload p : getCurrentVehicle().getPayloads().values()) {
				if(p.getPayloadType().equals(PayloadType.DISPENSABLE_PAYLOAD)) {
					ok = true;
				}
			}
		}
		if(ok) {
			List<JMenuItem> items = new ArrayList<JMenuItem>();
			if(getCurrentVehicle()!=null && getCurrentVehicle().getMission()!=null) {
				if(getMapKit().getLastSelectedButton3()==null) {
					//DROP OBJECT
					JMenu mi1 = new JMenu("Plan action");
					JMenuItem mi11 = new JMenuItem("Drop object here...");
					mi11.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							ObjectDropPlanningDialog d = new ObjectDropPlanningDialog(null);
							Coordinates c = new Coordinates();
							CUCSViewHelper.copyCoordinates(c, clickPosition);
							d.showDialog(getCurrentVehicle(), subscriberService, c);
						}
					});
					mi1.setToolTipText("<html>This utility will create some waypoints that will be used for <br/>releasing an object that will hit the ground in a desired position.<br/>After creation, upload the mission to vehicle and activate the newly created route.");
					mi1.add(mi11);
					items.add(mi1);
				}
			}
			return items;
		} else {
			//no dispensable payloads found. skip
			return null;
		}
	}

	@Override
	public boolean isCompatibleWithVehicle(Vehicle vehicle) {
		return true;
	}
	
}
