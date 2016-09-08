package br.skylight.cucs.plugins.skylightvehicle.missionplan;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.Coordinates;
import br.skylight.commons.EventType;
import br.skylight.commons.Region;
import br.skylight.commons.SkylightMission;
import br.skylight.commons.Vehicle;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.enums.VehicleType;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.mapkit.MapElementGroup;
import br.skylight.cucs.plugins.controlmap2d.ControlMapExtensionPoint;
import br.skylight.cucs.plugins.skylightvehicle.vehiclecontrol.SkylightVehicleControlService;
import br.skylight.cucs.plugins.subscriber.SubscriberService;

@ExtensionPointImplementation(extensionPointDefinition=ControlMapExtensionPoint.class)
public class RulesOfSafetyMapExtensionPointImpl extends ControlMapExtensionPoint {

	private JPanel toolComponent;  //  @jve:decl-index=0:visual-constraint="36,22"
	private JButton addAuthorizedRegion = null;
	private JButton addProhibitedRegion = null;
	
	@ServiceInjection
	public SkylightVehicleControlService skylightVehicleControlService;
	
	@ServiceInjection
	public SubscriberService subscriberService;
	private JButton addManualRecoveryLocation = null;
	private JButton addDataLinkRecoveryLocation = null;
	
	@Override
	public Component getToolComponent() {
		if(toolComponent==null) {
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setHgap(2);
			flowLayout.setVgap(2);
			toolComponent = new JPanel();
			toolComponent.setLayout(flowLayout);
			toolComponent.setSize(new Dimension(127, 30));
			toolComponent.add(getAddAuthorizedRegion(), null);
			toolComponent.add(getAddProhibitedRegion(), null);
			toolComponent.add(getAddManualRecoveryLocation(), null);
			toolComponent.add(getAddDataLinkRecoveryLocation(), null);
		}
		return toolComponent;
	}
	/**
	 * This method initializes addAuthorizedRegion	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAddAuthorizedRegion() {
		if (addAuthorizedRegion == null) {
			addAuthorizedRegion = new JButton();
			addAuthorizedRegion.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/open.gif")));
			addAuthorizedRegion.setMargin(ViewHelper.getDefaultButtonMargin());
			addAuthorizedRegion.setText("");
			addAuthorizedRegion.setToolTipText("Add/replace authorized operations region");
			addAuthorizedRegion.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SkylightMission sm = skylightVehicleControlService.resolveSkylightMission(getCurrentVehicle().getVehicleID().getVehicleID());

					if(sm.getRulesOfSafety().getAuthorizedRegion().isValidArea()) {
						if(JOptionPane.OK_OPTION!=JOptionPane.showConfirmDialog(null, "There already exists an authorization region for this mission.\nDo you want to replace it by a new region?")) {
							return;
						}
					}
					
					//draw an initial sample authorized region based on current coordinates in map
					Region r = new Region();
					GeoPosition gp = getMapKit().getCenterPosition();
					double dlat = Math.toDegrees(CoordinatesHelper.metersToLatitudeLength(2000, Math.toRadians(gp.getLatitude())));
					double dlon = Math.toDegrees(CoordinatesHelper.metersToLongitudeLength(2000, Math.toRadians(gp.getLatitude())));
					r.addPoint(new Coordinates(gp.getLatitude()-dlat, gp.getLongitude()-dlon, 0));
					r.addPoint(new Coordinates(gp.getLatitude()+dlat, gp.getLongitude()-dlon, 0));
					r.addPoint(new Coordinates(gp.getLatitude()+dlat, gp.getLongitude()+dlon, 0));
					r.addPoint(new Coordinates(gp.getLatitude()-dlat, gp.getLongitude()+dlon, 0));
					sm.getRulesOfSafety().setAuthorizedRegion(r);
					
					//repaint map
					subscriberService.notifyMissionEvent(getCurrentVehicle().getMission(), EventType.UPDATED, null);
				}
			});
		}
		return addAuthorizedRegion;
	}
	
	/**
	 * This method initializes addProhibitedRegion	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAddProhibitedRegion() {
		if (addProhibitedRegion == null) {
			addProhibitedRegion = new JButton();
			addProhibitedRegion.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/open.gif")));
			addProhibitedRegion.setMargin(ViewHelper.getDefaultButtonMargin());
			addProhibitedRegion.setText("");
			addProhibitedRegion.setToolTipText("Add a new prohibited region");
			addProhibitedRegion.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SkylightMission sm = skylightVehicleControlService.resolveSkylightMission(getCurrentVehicle().getVehicleID().getVehicleID());

					//draw an sample prohibited region based on current coordinates in map
					Region r = new Region();
					GeoPosition gp = getMapKit().getCenterPosition();
					double dlat = Math.toDegrees(CoordinatesHelper.metersToLatitudeLength(500, Math.toRadians(gp.getLatitude())));
					double dlon = Math.toDegrees(CoordinatesHelper.metersToLongitudeLength(500, Math.toRadians(gp.getLatitude())));
					r.addPoint(new Coordinates(gp.getLatitude()-dlat, gp.getLongitude()-dlon, 0));
					r.addPoint(new Coordinates(gp.getLatitude()+dlat, gp.getLongitude()-dlon, 0));
					r.addPoint(new Coordinates(gp.getLatitude()+dlat, gp.getLongitude()+dlon, 0));
					r.addPoint(new Coordinates(gp.getLatitude()-dlat, gp.getLongitude()+dlon, 0));
					sm.getRulesOfSafety().getProhibitedRegions().add(r);
					
					//repaint map
					subscriberService.notifyMissionEvent(getCurrentVehicle().getMission(), EventType.UPDATED, null);
				}
			});
		}
		return addProhibitedRegion;
	}
	
	@Override
	public boolean isCompatibleWithVehicle(Vehicle vehicle) {
		return vehicle!=null && vehicle.getVehicleID().getVehicleType().equals(VehicleType.TYPE_60);
	}
	/**
	 * This method initializes addManualRecoveryLocation	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAddManualRecoveryLocation() {
		if (addManualRecoveryLocation == null) {
			addManualRecoveryLocation = new JButton();
			addManualRecoveryLocation.setToolTipText("Add/replace location for manual recovery");
			addManualRecoveryLocation.setMargin(ViewHelper.getDefaultButtonMargin());
			addManualRecoveryLocation.setText("");
			addManualRecoveryLocation.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/open.gif")));
			addManualRecoveryLocation.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SkylightMission sm = skylightVehicleControlService.resolveSkylightMission(getCurrentVehicle().getVehicleID().getVehicleID());
					if(sm.getRulesOfSafety().getManualRecoveryLoiterLocation().getLatitude()!=0 && sm.getRulesOfSafety().getManualRecoveryLoiterLocation().getLongitude()!=0) {
						if(JOptionPane.OK_OPTION!=JOptionPane.showConfirmDialog(null, "Manual recovery location is already defined.\nDo you want to replace it?")) {
							return;
						}
					}

					MapElementGroup<ManualRecoveryLoiterMapElement> g = SkylightMissionMapExtensionPointImpl.resolveManualRecoveryLoiterGroup(getMapKit(), getCurrentVehicle().getMission(), sm);
					getMapKit().addNewElementOnClick(g.getId());
				}
			});
		}
		return addManualRecoveryLocation;
	}

	/**
	 * This method initializes addDataLinkRecoveryLocation	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAddDataLinkRecoveryLocation() {
		if (addDataLinkRecoveryLocation == null) {
			addDataLinkRecoveryLocation = new JButton();
			addDataLinkRecoveryLocation.setToolTipText("Add/replace location for data link recovery");
			addDataLinkRecoveryLocation.setMargin(ViewHelper.getDefaultButtonMargin());
			addDataLinkRecoveryLocation.setText("");
			addDataLinkRecoveryLocation.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/open.gif")));
			addDataLinkRecoveryLocation.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SkylightMission sm = skylightVehicleControlService.resolveSkylightMission(getCurrentVehicle().getVehicleID().getVehicleID());
					if(sm.getRulesOfSafety().getDataLinkRecoveryLoiterLocation().getLatitude()!=0 && sm.getRulesOfSafety().getDataLinkRecoveryLoiterLocation().getLongitude()!=0) {
						if(JOptionPane.OK_OPTION!=JOptionPane.showConfirmDialog(null, "Data link recovery location is already defined.\nDo you want to replace it?")) {
							return;
						}
					}
					MapElementGroup<DataLinkRecoveryLoiterMapElement> g = SkylightMissionMapExtensionPointImpl.resolveDataLinkRecoveryLoiterGroup(getMapKit(), getCurrentVehicle().getMission(), sm);
					getMapKit().addNewElementOnClick(g.getId());
				}
			});
		}
		return addDataLinkRecoveryLocation;
	}
	
}
