package br.skylight.cucs.plugins.skylightvehicle;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.autocomplete.ComboBoxCellEditor;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

import br.skylight.commons.AGLAltitudeMode;
import br.skylight.commons.Vehicle;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.enums.VehicleType;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.dli.vehicle.VehicleConfigurationMessage;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.plugins.skylightvehicle.vehiclecontrol.SkylightVehicle;
import br.skylight.cucs.plugins.skylightvehicle.vehiclecontrol.SkylightVehicleControlService;
import br.skylight.cucs.plugins.vehicleconfiguration.VehicleConfigurationSectionExtensionPoint;
import br.skylight.cucs.widgets.FeedbackButton;
import br.skylight.cucs.widgets.tables.JXTreeTable2;
import br.skylight.cucs.widgets.tables.ObjectPerColumnTreeTableNode;

@ExtensionPointImplementation(extensionPointDefinition=VehicleConfigurationSectionExtensionPoint.class)
public class VehicleConfigurationMiscExtensionPointImpl implements VehicleConfigurationSectionExtensionPoint {

	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="10,52"
	private JScrollPane scroll = null;
	private FeedbackButton refresh = null;
	private JButton uploadButton = null;
	private Vehicle currentVehicle;  //  @jve:decl-index=0:
	private SkylightVehicle currentSkylightVehicle;  //  @jve:decl-index=0:
	private List<ObjectPerColumnTreeTableNode> nodes = new ArrayList<ObjectPerColumnTreeTableNode>();  //  @jve:decl-index=0:
	private JXTreeTable2 treeTable = null;
	
	@ServiceInjection
	public MessagingService messagingService;
	
	@ServiceInjection
	public SkylightVehicleControlService skylightVehicleControlService;
	
	@Override
	public JPanel getSectionComponent() {
		return getContents();
	}
	
	private JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.insets = new Insets(3, 3, 3, 3);
			gridBagConstraints3.gridy = 1;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.BOTH;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.weighty = 1.0;
			gridBagConstraints2.gridwidth = 2;
			gridBagConstraints2.gridx = 0;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.insets = new Insets(3, 3, 3, 3);
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			gridBagConstraints1.gridy = 1;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(202, 119));
			contents.add(getRefresh(), gridBagConstraints1);
			contents.add(getScroll(), gridBagConstraints2);
			contents.add(getUploadButton(), gridBagConstraints3);
		}
		return contents;
	}

	@Override
	public String getSectionName() {
		return "Misc";
	}

	@Override
	public boolean updateVehicle(Vehicle vehicle) {
		this.currentVehicle = vehicle;
		this.currentSkylightVehicle = skylightVehicleControlService.resolveSkylightVehicle(vehicle.getVehicleID().getVehicleID());
		updateGUI();
		return vehicle!=null && vehicle.getVehicleID()!=null && vehicle.getVehicleID().getVehicleType().equals(VehicleType.TYPE_60);
	}

	private void updateGUI() {
		if(currentVehicle!=null) {
//			SkylightVehicleConfigurationMessage m = currentVehicle.getLastReceivedMessage(MessageType.M2000);
//			if(m!=null) {
			if(currentSkylightVehicle!=null && currentSkylightVehicle.getSkylightVehicleConfiguration()!=null) {
				VehicleConfigurationMessage m = currentVehicle.getVehicleConfiguration();
				SkylightVehicleConfigurationMessage sm = currentSkylightVehicle.getSkylightVehicleConfiguration();
				
				//put configuration to tree table
				DefaultTreeTableModel model = (DefaultTreeTableModel)getTreeTable().getTreeTableModel();

				nodes.clear();
				ObjectPerColumnTreeTableNode root = new ObjectPerColumnTreeTableNode("Configurations");
				ObjectPerColumnTreeTableNode n1 = createNode(root, "General", "", "");
				createNode(n1, "Configuration ID:", (Long)m.getConfigurationID(), "");
				createNode(n1, "Logging level:", sm.getOperatorLoggingLevel(), "");
				
				ObjectPerColumnTreeTableNode n2 = createNode(root, "Vehicle", "", "");
				createNode(n2, "Tail number:", sm.getVehicleIdentification().getTailNumber(), "");
				createNode(n2, "ATC call sign:", sm.getVehicleIdentification().getATCCallSign(), "");
				createNode(n2, "Max load factor:", m.getMaximumLoadFactor(), "m/s2");
				createNode(n2, "Gross weight:", m.getGrossWeight(), "kg");
				createNode(n2, "CG from nose:", m.getXCG(), "m");
				createNode(n2, "Gps antenna position X:", sm.getGpsAntennaPositionX(), "m");
				createNode(n2, "Gps antenna position Y:", sm.getGpsAntennaPositionX(), "m");
				createNode(n2, "Gps antenna position Z:", sm.getGpsAntennaPositionX(), "m");
				createNode(n2, "Ground touch position Z:", sm.getGpsAntennaPositionX(), "m");
				createNode(n2, "Parachute enabled:", sm.isParachuteEnabled(), "");
				createNode(n2, "Kill engine enabled:", sm.isKillEngineEnabled(), "");
				createNode(n2, "Safety procedures enabled:", sm.isSafetyProceduresEnabled(), "");
				createNode(n2, "Keep stable flight on 'No Mode':", sm.isKeepStableOnNoMode(), "");
				
				ObjectPerColumnTreeTableNode n3 = createNode(root, "Propulsion", "", "");
				createNode(n3, "Fuel capacity mass:", m.getPropulsionFuelCapacity(), "kg");
				createNode(n3, "Fuel capacity volume:", sm.getFuelCapacityVolume(), "m3");
				createNode(n3, "Number of engines:", m.getNumberOfEngines(), "");
				createNode(n3, "Fuel cons. throttle 0%:", sm.getFuelConsumptionForThrottle()[0], "mL/min");
				createNode(n3, "Fuel cons. throttle 10%:", sm.getFuelConsumptionForThrottle()[1], "mL/min");
				createNode(n3, "Fuel cons. throttle 20%:", sm.getFuelConsumptionForThrottle()[2], "mL/min");
				createNode(n3, "Fuel cons. throttle 30%:", sm.getFuelConsumptionForThrottle()[3], "mL/min");
				createNode(n3, "Fuel cons. throttle 40%:", sm.getFuelConsumptionForThrottle()[4], "mL/min");
				createNode(n3, "Fuel cons. throttle 50%:", sm.getFuelConsumptionForThrottle()[5], "mL/min");
				createNode(n3, "Fuel cons. throttle 60%:", sm.getFuelConsumptionForThrottle()[6], "mL/min");
				createNode(n3, "Fuel cons. throttle 70%:", sm.getFuelConsumptionForThrottle()[7], "mL/min");
				createNode(n3, "Fuel cons. throttle 80%:", sm.getFuelConsumptionForThrottle()[8], "mL/min");
				createNode(n3, "Fuel cons. throttle 90%:", sm.getFuelConsumptionForThrottle()[9], "mL/min");
				createNode(n3, "Fuel cons. throttle 100%:", sm.getFuelConsumptionForThrottle()[10], "mL/min");
				
				ObjectPerColumnTreeTableNode n4 = createNode(root, "Navigation", "", "");
				createNode(n4, "Calculated versus real turn ratio:", sm.getCalculatedVersusRealTurnFactor(), "fraction");
				createNode(n4, "Optimum cruise IAS:", m.getOptimumCruiseIndicatedAirspeed(), "m/s");
				createNode(n4, "Optimum endurance IAS:", m.getOptimumEnduranceIndicatedAirspeed(), "m/s");
				createNode(n4, "AGL altitude mode:", sm.getAglAltitudeMode(), "");
				
				ObjectPerColumnTreeTableNode n5 = createNode(root, "Operational limits", "", "");
				createNode(n5, "Min roll:", (float)Math.toDegrees(sm.getRollMin()), "degrees");
				createNode(n5, "Max roll:", (float)Math.toDegrees(sm.getRollMax()), "degrees");
				createNode(n5, "Min pitch:", (float)Math.toDegrees(sm.getPitchMin()), "degrees");
				createNode(n5, "Max pitch:", (float)Math.toDegrees(sm.getPitchMax()), "degrees");
				createNode(n5, "Max IAS:", m.getMaximumIndicatedAirspeed(), "m/s");
				createNode(n5, "Min stall IAS:", sm.getStallIndicatedAirspeed(), "m/s");
				createNode(n5, "Max altitude AGL:", sm.getAltitudeMaxAGL(), "m");
				createNode(n5, "Max flight time:", sm.getMaxFlightTimeMinutes(), "min");
				
				ObjectPerColumnTreeTableNode n6 = createNode(root, "Take-off/landing", "", "");
				createNode(n6, "Take-off/Lift-off IAS:", sm.getTakeOffLiftOffIndicatedAirspeed(), "m/s");
				createNode(n6, "Landing steady flight IAS:", sm.getLandingSteadyFlightIndicatedAirspeed(), "m/s");
				createNode(n6, "Landing min ground speed:", sm.getLandingMinGroundSpeed(), "m/s");
				createNode(n6, "Landing max ground speed:", sm.getLandingMaxGroundSpeed(), "m/s");
				createNode(n6, "Landing approach scale:", sm.getLandingApproachScale(), "fraction");
				
				ObjectPerColumnTreeTableNode n7 = createNode(root, "Sensors/controls", "", "");
				createNode(n7, "Sensor pitch trim:", sm.getSensorPitchTrim(), "fraction");
				createNode(n7, "Sensor roll trim:", sm.getSensorRollTrim(), "fraction");
				createNode(n7, "Sensor yaw trim:", sm.getSensorYawTrim(), "fraction");
				createNode(n7, "Pitch rate damp:", sm.getPitchRateDamp(), "fraction");
				createNode(n7, "Roll rate damp:", sm.getRollRateDamp(), "fraction");
				createNode(n7, "Yaw rate damp:", sm.getYawRateDamp(), "fraction");
				createNode(n7, "Rudder surface gain:", sm.getRudderSurfaceGain(), "fraction");
				createNode(n7, "Elevator surface gain:", sm.getElevatorSurfaceGain(), "fraction");
				
				model.setRoot(root);
				getTreeTable().updateUI();
				getRefresh().notifyFeedback();
			}
		}
	}
	
	private ObjectPerColumnTreeTableNode createNode(ObjectPerColumnTreeTableNode parent, String label, Object value, String unit) {
		ObjectPerColumnTreeTableNode n = new ObjectPerColumnTreeTableNode(label, value, unit);
		n.setEditable(1, true);
		parent.insert(n, parent.getChildCount());
		nodes.add(n);
		return n;
	}

	/**
	 * This method initializes scroll	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getScroll() {
		if (scroll == null) {
			scroll = new JScrollPane();
			scroll.setViewportView(getTreeTable());
		}
		return scroll;
	}
	
	/**
	 * This method initializes refresh	
	 * 	
	 * @return br.skylight.cucs.widgets.FeedbackButton	
	 */
	private FeedbackButton getRefresh() {
		if (refresh == null) {
			refresh = new FeedbackButton();
			refresh.setToolTipText("Refresh data");
			refresh.setMargin(ViewHelper.getMinimalButtonMargin());
			refresh.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/refresh.gif")));
			refresh.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					messagingService.sendRequestGenericInformation(MessageType.M100, currentVehicle.getVehicleID().getVehicleID());
					messagingService.sendRequestGenericInformation(MessageType.M2000, currentVehicle.getVehicleID().getVehicleID());
				}
			});
		}
		return refresh;
	}

	/**
	 * This method initializes uploadButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getUploadButton() {
		if (uploadButton == null) {
			uploadButton = new JButton();
			uploadButton.setToolTipText("Upload configuration");
			uploadButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/upload.gif")));
			uploadButton.setMargin(ViewHelper.getMinimalButtonMargin());
			uploadButton.setPreferredSize(new Dimension(20, 20));
			uploadButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					VehicleConfigurationMessage m = messagingService.resolveMessageForSending(VehicleConfigurationMessage.class);
					SkylightVehicleConfigurationMessage sm = messagingService.resolveMessageForSending(SkylightVehicleConfigurationMessage.class);
//					DefaultTreeTableModel model = (DefaultTreeTableModel)getTreeTable().getTreeTableModel();
					
					m.setConfigurationID((Long)nodes.get(1).getValueAt(1));
					sm.setOperatorLoggingLevel((Level)nodes.get(2).getValueAt(1));
					
					sm.getVehicleIdentification().setTailNumber((String)nodes.get(4).getValueAt(1));
					sm.getVehicleIdentification().setATCCallSign((String)nodes.get(5).getValueAt(1));
					m.setMaximumLoadFactor((Float)nodes.get(6).getValueAt(1));
					m.setGrossWeight((Float)nodes.get(7).getValueAt(1));
					m.setXCG((Float)nodes.get(8).getValueAt(1));
					sm.setGpsAntennaPositionX((Float)nodes.get(9).getValueAt(1));
					sm.setGpsAntennaPositionY((Float)nodes.get(10).getValueAt(1));
					sm.setGpsAntennaPositionZ((Float)nodes.get(11).getValueAt(1));
					sm.setGroundTouchPositionZ((Float)nodes.get(12).getValueAt(1));
					sm.setParachuteEnabled((Boolean)nodes.get(13).getValueAt(1));
					sm.setKillEngineEnabled((Boolean)nodes.get(14).getValueAt(1));
					sm.setSafetyProceduresEnabled((Boolean)nodes.get(15).getValueAt(1));
					sm.setKeepStableOnNoMode((Boolean)nodes.get(16).getValueAt(1));
					
					m.setPropulsionFuelCapacity((Float)nodes.get(18).getValueAt(1));
					sm.setFuelCapacityVolume((Float)nodes.get(19).getValueAt(1));
					m.setNumberOfEngines((Integer)nodes.get(20).getValueAt(1));
					sm.getFuelConsumptionForThrottle()[0] = ((Float)nodes.get(21).getValueAt(1));
					sm.getFuelConsumptionForThrottle()[1] = ((Float)nodes.get(22).getValueAt(1));
					sm.getFuelConsumptionForThrottle()[2] = ((Float)nodes.get(23).getValueAt(1));
					sm.getFuelConsumptionForThrottle()[3] = ((Float)nodes.get(24).getValueAt(1));
					sm.getFuelConsumptionForThrottle()[4] = ((Float)nodes.get(25).getValueAt(1));
					sm.getFuelConsumptionForThrottle()[5] = ((Float)nodes.get(26).getValueAt(1));
					sm.getFuelConsumptionForThrottle()[6] = ((Float)nodes.get(27).getValueAt(1));
					sm.getFuelConsumptionForThrottle()[7] = ((Float)nodes.get(28).getValueAt(1));
					sm.getFuelConsumptionForThrottle()[8] = ((Float)nodes.get(29).getValueAt(1));
					sm.getFuelConsumptionForThrottle()[9] = ((Float)nodes.get(30).getValueAt(1));
					sm.getFuelConsumptionForThrottle()[10] = ((Float)nodes.get(31).getValueAt(1));

					sm.setCalculatedVersusRealTurnFactor((Float)nodes.get(33).getValueAt(1));
					m.setOptimumCruiseIndicatedAirspeed((Float)nodes.get(34).getValueAt(1));
					m.setOptimumEnduranceIndicatedAirspeed((Float)nodes.get(35).getValueAt(1));
					sm.setAglAltitudeMode((AGLAltitudeMode)nodes.get(36).getValueAt(1));

					sm.setRollMin((float)Math.toRadians((Float)nodes.get(38).getValueAt(1)));
					sm.setRollMax((float)Math.toRadians((Float)nodes.get(39).getValueAt(1)));
					sm.setPitchMin((float)Math.toRadians((Float)nodes.get(40).getValueAt(1)));
					sm.setPitchMax((float)Math.toRadians((Float)nodes.get(41).getValueAt(1)));
					m.setMaximumIndicatedAirspeed((Float)nodes.get(42).getValueAt(1));
					sm.setStallIndicatedAirspeed((Float)nodes.get(43).getValueAt(1));
					sm.setAltitudeMaxAGL((Float)nodes.get(44).getValueAt(1));
					sm.setMaxFlightTimeMinutes((Integer)nodes.get(45).getValueAt(1));

					sm.setTakeOffLiftOffIndicatedAirspeed((Float)nodes.get(47).getValueAt(1));
					sm.setLandingSteadyFlightIndicatedAirspeed((Float)nodes.get(48).getValueAt(1));
					sm.setLandingMinGroundSpeed((Float)nodes.get(49).getValueAt(1));
					sm.setLandingMaxGroundSpeed((Float)nodes.get(50).getValueAt(1));
					sm.setLandingApproachScale((Float)nodes.get(51).getValueAt(1));

					sm.setSensorPitchTrim((Float)nodes.get(53).getValueAt(1));
					sm.setSensorRollTrim((Float)nodes.get(54).getValueAt(1));
					sm.setSensorYawTrim((Float)nodes.get(55).getValueAt(1));
					sm.setPitchRateDamp((Float)nodes.get(56).getValueAt(1));
					sm.setRollRateDamp((Float)nodes.get(57).getValueAt(1));
					sm.setYawRateDamp((Float)nodes.get(58).getValueAt(1));
					sm.setRudderSurfaceGain((Float)nodes.get(59).getValueAt(1));
					sm.setElevatorSurfaceGain((Float)nodes.get(60).getValueAt(1));
					
					m.setVehicleID(currentVehicle.getVehicleID().getVehicleID());
					messagingService.sendMessage(m);

					sm.setVehicleID(currentVehicle.getVehicleID().getVehicleID());
					messagingService.sendMessage(sm);
				}
			});
		}
		return uploadButton;
	}

	/**
	 * This method initializes treeTable	
	 * 	
	 * @return org.jdesktop.swingx.treeTable	
	 */
	private JXTreeTable getTreeTable() {
		if (treeTable == null) {
			treeTable = new JXTreeTable2();
			treeTable.setExpandsSelectedPaths(true);
			treeTable.setRootVisible(false);
			treeTable.setAutoCreateRowSorter(true);
			treeTable.addHighlighter(HighlighterFactory.createSimpleStriping(HighlighterFactory.GENERIC_GRAY));
			DefaultTreeTableModel model = new DefaultTreeTableModel();
			List<String> cn = new ArrayList<String>();
			cn.add("Name");
			cn.add("Value");
			cn.add("Unit");
			model.setColumnIdentifiers(cn);
			treeTable.setTreeTableModel(model);

			//set editor for logging Level
			DefaultComboBoxModel cm = new DefaultComboBoxModel();
			cm.addElement(Level.OFF);
			cm.addElement(Level.SEVERE);
			cm.addElement(Level.WARNING);
			cm.addElement(Level.INFO);
			cm.addElement(Level.CONFIG);
			cm.addElement(Level.FINE);
			cm.addElement(Level.FINER);
			cm.addElement(Level.FINEST);
			cm.addElement(Level.ALL);
			JComboBox cb = new JComboBox();
			cb.setModel(cm);
			treeTable.setCellEditor(Level.class, new ComboBoxCellEditor(cb));
		}
		return treeTable;
	}

}
