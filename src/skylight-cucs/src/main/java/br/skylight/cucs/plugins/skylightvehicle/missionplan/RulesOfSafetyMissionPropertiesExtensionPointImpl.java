package br.skylight.cucs.plugins.skylightvehicle.missionplan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.autocomplete.ComboBoxCellEditor;
import org.jdesktop.swingx.combobox.EnumComboBoxModel;

import br.skylight.commons.Alert;
import br.skylight.commons.MeasureType;
import br.skylight.commons.Region;
import br.skylight.commons.RulesOfSafety;
import br.skylight.commons.SafetyAction;
import br.skylight.commons.SkylightMission;
import br.skylight.commons.Vehicle;
import br.skylight.commons.VerificationResult;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.VehicleMode;
import br.skylight.commons.dli.enums.VehicleType;
import br.skylight.commons.dli.skylight.SafetyActionForAlert;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.plugins.missionplan.MissionPropertiesTabExtensionPoint;
import br.skylight.cucs.plugins.skylightvehicle.vehiclecontrol.SkylightVehicleControlService;
import br.skylight.cucs.widgets.JMeasureSpinner;
import br.skylight.cucs.widgets.tables.ObjectToColumnAdapter;
import br.skylight.cucs.widgets.tables.TypedTableModel;

@ExtensionPointImplementation(extensionPointDefinition=MissionPropertiesTabExtensionPoint.class)
public class RulesOfSafetyMissionPropertiesExtensionPointImpl implements MissionPropertiesTabExtensionPoint {

	private JTabbedPane pane = null;
	private JPanel general = null;
	private JPanel gpsLoss = null;
	private JPanel dataLinkLoss = null;  //  @jve:decl-index=0:visual-constraint="539,120"
	private JPanel limits = null;
	private JCheckBox enableGpsRecovery = null;
	private JCheckBox enableDataLinkRecovery = null;
	private JLabel jLabel = null;
	private JComboBox bothLostAction = null;
	private JPanel jPanel = null;
	private JPanel jPanel1 = null;
	private JScrollPane jScrollPane = null;
	private JXTable alertActions = null;
	private Vehicle vehicle;
	private SkylightMission skylightMission;  //  @jve:decl-index=0:
	
	@ServiceInjection
	public SkylightVehicleControlService skylightVehicleControlService;
	private JPanel jPanel2 = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JMeasureSpinner<Double> gpsLinkTimeout = null;
	private JMeasureSpinner<Integer> gpsMaxRecoveryRetries = null;
	private JPanel jPanel3 = null;
	private JLabel jLabel3 = null;
	private JLabel jLabel4 = null;
	private JLabel jLabel5 = null;
	private JMeasureSpinner<Double> gpsTimeStableSuccess = null;
	private JMeasureSpinner<Double> gpsRecoveryTimeout = null;
	private JComboBox gpsRecoveryAction = null;
	private JPanel jPanel4 = null;
	private JLabel jLabel6 = null;
	private JLabel jLabel7 = null;
	private JComboBox gpsRecoveryFailureAction = null;
	private JComboBox gpsRecoverySuccessMode = null;
	private JPanel jPanel5 = null;
	private JLabel jLabel11 = null;
	private JLabel jLabel21 = null;
	private JMeasureSpinner<Double> dataLinkTimeout = null;
	private JMeasureSpinner<Integer> dataLinkMaxRecoveryRetries = null;
	private JPanel jPanel6 = null;
	private JLabel jLabel9 = null;
	private JLabel jLabel91 = null;
	private JLabel jLabel10 = null;
	private JLabel jLabel12 = null;
	private JLabel jLabel13 = null;
	private JMeasureSpinner<Double> dataLinkRecoveryLatitude = null;
	private JMeasureSpinner<Double> dataLinkRecoveryLongitude = null;
	private JMeasureSpinner<Double> dataLinkReachRecoveryLoiterTimeout = null;
	private JMeasureSpinner<Double> dataLinkMaxTimeLoiteringRecovery = null;
	private JMeasureSpinner<Double> dataLinkStableLinkSuccess = null;
	private JPanel jPanel7 = null;
	private JLabel jLabel92 = null;
	private JLabel jLabel911 = null;
	private JLabel jLabel101 = null;
	private JLabel jLabel121 = null;
	private JLabel jLabel131 = null;
	private JMeasureSpinner<Double> manualRecoveryLoiterLatitude = null;
	private JMeasureSpinner<Double> manualRecoveryLoiterLongitude = null;
	private JMeasureSpinner<Double> manualRecoveryReachLocationTimeout = null;
	private JMeasureSpinner<Double> manualRecoveryTimeoutLoiteringLocation = null;
	private JComboBox manualRecoveryActionOnTimeout = null;
	private JPanel jPanel8 = null;
	private JLabel jLabel8 = null;
	private JComboBox dataLinkRecoverySuccessMode = null;
	private JScrollPane dataLinkScroll = null;
	private JPanel jPanel9 = null;
	private JLabel jLabel14 = null;
	private JLabel jLabel15 = null;
	private JLabel jLabel16 = null;
	private JLabel jLabel17 = null;
	private JMeasureSpinner<Float> regionMinAltitude = null;
	private JMeasureSpinner<Float> regionMaxAltitude = null;
	private JComboBox regionAltitudeType = null;
	private JTextArea authorizedRegion = null;
	private JScrollPane authorizedRegionScroll = null;
	private JPanel jPanel10 = null;
	private JPanel regionEditProhibitedRegions = null;
	private JLabel jLabel141 = null;
	private JScrollPane prohibitedRegionsScroll = null;
	private JTextArea prohibitedRegions = null;
	private JPanel jPanel11 = null;
	private JLabel jLabel18 = null;
	private JMeasureSpinner<Integer> maxFlightTime = null;
	private JLabel jLabel71 = null;
	private JComboBox dataLinkActionOnRecoveryFailure = null;
	private JLabel jLabel912 = null;
	private JMeasureSpinner<Float> dataLinkRecoveryAltitude = null;
	private JLabel jLabel9121 = null;
	private JComboBox dataLinkRecoveryAltitudeType = null;
	private JLabel jLabel9111 = null;
	private JMeasureSpinner<Float> manualRecoveryLoiterAltitude = null;
	private JLabel jLabel91111 = null;
	private JComboBox manualRecoveryAltitudeType = null;
	private JLabel jLabel111 = null;
	private JMeasureSpinner<Integer> dataLinkMinStrength = null;
	
	@Override
	public JPanel createTabPanel(Vehicle vehicle) {
		this.vehicle = vehicle;
		this.skylightMission = skylightVehicleControlService.resolveSkylightMission(vehicle.getVehicleID().getVehicleID());
		
		JPanel contents = new JPanel();
		contents.setLayout(new BorderLayout());
		contents.setSize(new Dimension(394, 469));
		contents.add(getPane(), BorderLayout.CENTER);
		loadGUI();
		updateGUI();
		return contents;
	}

	@Override
	public String getTabTitle() {
		return "Rules of Safety";
	}

	@Override
	public boolean isCompatibleWith(Vehicle vehicle) {
		return vehicle.getVehicleID().getVehicleType().equals(VehicleType.TYPE_60);
	}

	@Override
	public void onCancelPressed() {
	}

	/**
	 * This method initializes pane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getPane() {
		if (pane == null) {
			pane = new JTabbedPane();
			pane.addTab("General", null, getGeneral(), null);
			pane.addTab("GPS loss", null, getGpsLoss(), null);
			pane.addTab("Data link loss", null, getDataLinkLoss(), null);
			pane.addTab("Limits", null, getLimits(), null);
		}
		return pane;
	}
	
	private void updateGUI() {
		getPane().removeAll();
		getPane().addTab("General", null, getGeneral(), null);
		if(getEnableGpsRecovery().isSelected()) {
			getPane().addTab("GPS loss", null, getGpsLoss(), null);
		}
		if(getEnableDataLinkRecovery().isSelected()) {
			getPane().addTab("Data link loss", null, getDataLinkLoss(), null);
		}
		getPane().addTab("Regions", null, getLimits(), null);
		getBothLostAction().setEnabled(getEnableDataLinkRecovery().isSelected() && getEnableGpsRecovery().isSelected());
	}

	/**
	 * This method initializes general	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getGeneral() {
		if (general == null) {
			GridBagConstraints gridBagConstraints53 = new GridBagConstraints();
			gridBagConstraints53.gridx = 0;
			gridBagConstraints53.weightx = 1.0;
			gridBagConstraints53.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints53.insets = new Insets(2, 2, 0, 2);
			gridBagConstraints53.gridy = 1;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.fill = GridBagConstraints.BOTH;
			gridBagConstraints5.weighty = 1.0;
			gridBagConstraints5.insets = new Insets(0, 3, 3, 3);
			gridBagConstraints5.gridy = 2;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.insets = new Insets(0, 3, 0, 3);
			gridBagConstraints4.gridy = 0;
			jLabel = new JLabel();
			jLabel.setText("Action if both GPS and Data links are lost simultaneously:");
			general = new JPanel();
			general.setLayout(new GridBagLayout());
			general.add(getJPanel(), gridBagConstraints4);
			general.add(getJPanel1(), gridBagConstraints5);
			general.add(getJPanel7(), gridBagConstraints53);
		}
		return general;
	}

	/**
	 * This method initializes gpsLoss	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getGpsLoss() {
		if (gpsLoss == null) {
			GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
			gridBagConstraints23.gridx = 0;
			gridBagConstraints23.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints23.weightx = 1.0;
			gridBagConstraints23.weighty = 1.0;
			gridBagConstraints23.anchor = GridBagConstraints.NORTH;
			gridBagConstraints23.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints23.gridy = 2;
			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
			gridBagConstraints18.gridx = 0;
			gridBagConstraints18.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints18.weightx = 1.0;
			gridBagConstraints18.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints18.gridy = 1;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints11.insets = new Insets(0, 2, 2, 2);
			gridBagConstraints11.gridy = 0;
			gpsLoss = new JPanel();
			gpsLoss.setLayout(new GridBagLayout());
			gpsLoss.add(getJPanel2(), gridBagConstraints11);
			gpsLoss.add(getJPanel3(), gridBagConstraints18);
			gpsLoss.add(getJPanel4(), gridBagConstraints23);
		}
		return gpsLoss;
	}

	/**
	 * This method initializes dataLinkLoss	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getDataLinkLoss() {
		if (dataLinkLoss == null) {
			GridBagConstraints gridBagConstraints50 = new GridBagConstraints();
			gridBagConstraints50.fill = GridBagConstraints.BOTH;
			gridBagConstraints50.gridy = 0;
			gridBagConstraints50.weightx = 1.0;
			gridBagConstraints50.weighty = 1.0;
			gridBagConstraints50.gridx = 0;
			dataLinkLoss = new JPanel();
			dataLinkLoss.setLayout(new GridBagLayout());
			dataLinkLoss.add(getDataLinkScroll(), gridBagConstraints50);
		}
		return dataLinkLoss;
	}

	/**
	 * This method initializes limits	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getLimits() {
		if (limits == null) {
			GridBagConstraints gridBagConstraints69 = new GridBagConstraints();
			gridBagConstraints69.gridx = 0;
			gridBagConstraints69.weightx = 1.0;
			gridBagConstraints69.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints69.gridy = 1;
			GridBagConstraints gridBagConstraints66 = new GridBagConstraints();
			gridBagConstraints66.gridx = 0;
			gridBagConstraints66.weightx = 1.0;
			gridBagConstraints66.fill = GridBagConstraints.BOTH;
			gridBagConstraints66.insets = new Insets(0, 2, 2, 2);
			gridBagConstraints66.weighty = 1.0;
			gridBagConstraints66.gridy = 3;
			GridBagConstraints gridBagConstraints65 = new GridBagConstraints();
			gridBagConstraints65.gridx = 0;
			gridBagConstraints65.weightx = 1.0;
			gridBagConstraints65.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints65.insets = new Insets(0, 2, 2, 2);
			gridBagConstraints65.gridy = 2;
			jLabel17 = new JLabel();
			jLabel17.setText("Altitude type:");
			jLabel16 = new JLabel();
			jLabel16.setText("Max altitude:");
			jLabel15 = new JLabel();
			jLabel15.setText("Min altitude:");
			jLabel14 = new JLabel();
			jLabel14.setText("Authorized region coordinates:");
			limits = new JPanel();
			limits.setLayout(new GridBagLayout());
			limits.add(getJPanel10(), gridBagConstraints65);
			limits.add(getRegionEditProhibitedRegions(), gridBagConstraints66);
			limits.add(getJPanel11(), gridBagConstraints69);
		}
		return limits;
	}

	/**
	 * This method initializes enableGpsRecovery	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getEnableGpsRecovery() {
		if (enableGpsRecovery == null) {
			enableGpsRecovery = new JCheckBox();
			enableGpsRecovery.setText("Enable GPS loss recovery procedures");
			enableGpsRecovery.setEnabled(true);
			enableGpsRecovery.setSelected(false);
			enableGpsRecovery.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					updateGUI();
				}
			});
		}
		return enableGpsRecovery;
	}

	/**
	 * This method initializes enableDataLinkRecovery	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getEnableDataLinkRecovery() {
		if (enableDataLinkRecovery == null) {
			enableDataLinkRecovery = new JCheckBox();
			enableDataLinkRecovery.setText("Enable data link loss recovery procedures");
			enableDataLinkRecovery.setEnabled(true);
			enableDataLinkRecovery.setSelected(false);
			enableDataLinkRecovery.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					updateGUI();
				}
			});
		}
		return enableDataLinkRecovery;
	}

	/**
	 * This method initializes bothLostAction	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getBothLostAction() {
		if (bothLostAction == null) {
			bothLostAction = new JComboBox();
//			bothLostAction.setModel(new EnumComboBoxModel<SafetyAction>(SafetyAction.class));
			bothLostAction.setModel(getGpsLossActions());
		}
		return bothLostAction;
	}

	private ComboBoxModel getGpsLossActions() {
		DefaultComboBoxModel m = new DefaultComboBoxModel();
		m.addElement(SafetyAction.DO_NOTHING);
		m.addElement(SafetyAction.DEPLOY_PARACHUTE);
		m.addElement(SafetyAction.HARD_SPIN_TO_GROUND);
		m.addElement(SafetyAction.LOITER_WITH_ROLL);
		m.addElement(SafetyAction.LOITER_WITH_ROLL_DESCENDING);
		return m;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.anchor = GridBagConstraints.WEST;
			gridBagConstraints3.insets = new Insets(0, 10, 0, 10);
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 3;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.fill = GridBagConstraints.NONE;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.insets = new Insets(3, 5, 0, 0);
			gridBagConstraints2.gridy = 2;
			gridBagConstraints2.anchor = GridBagConstraints.WEST;
			gridBagConstraints2.gridx = 0;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 1;
			gridBagConstraints1.insets = new Insets(0, 0, 0, 0);
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.insets = new Insets(0, 0, 0, 0);
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setBorder(BorderFactory.createTitledBorder(null, "GPS and Data Link", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanel.add(getEnableGpsRecovery(), gridBagConstraints);
			jPanel.add(getEnableDataLinkRecovery(), gridBagConstraints1);
			jPanel.add(jLabel, gridBagConstraints2);
			jPanel.add(getBothLostAction(), gridBagConstraints3);
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.fill = GridBagConstraints.BOTH;
			gridBagConstraints6.gridy = 0;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.weighty = 1.0;
			gridBagConstraints6.gridx = 0;
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.setBorder(BorderFactory.createTitledBorder(null, "Safety actions for Alerts", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanel1.add(getJScrollPane(), gridBagConstraints6);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getAlertActions());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes alertActions	
	 * 	
	 * @return org.jdesktop.swingx.alertActions	
	 */
	private JXTable getAlertActions() {
		if (alertActions == null) {
			alertActions = new JXTable();
			TypedTableModel<SafetyActionForAlert> m = new TypedTableModel<SafetyActionForAlert>(new ObjectToColumnAdapter<SafetyActionForAlert>() {
				@Override
				public Object getValueAt(SafetyActionForAlert sa, int columnIndex) {
					if(columnIndex==0) {
						return sa.getAlert();
					} else if(columnIndex==1) {
						return sa.getSafetyAction();
					} else {
						return null;
					}
				};
				@Override
				public void setValueAt(SafetyActionForAlert sa, Object value, int columnIndex) {
					if(columnIndex==0) {
						sa.setAlert((Alert)value);
					} else if(columnIndex==1) {
						sa.setSafetyAction((SafetyAction)value);
					}
				}
			}, "Alert", "Safety action");
			m.setColumnEditables(false, true);
			alertActions.setEditable(true);
			alertActions.setModel(m);
			alertActions.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			alertActions.getColumnExt(1).setCellEditor(new ComboBoxCellEditor(new JComboBox(new EnumComboBoxModel(SafetyAction.class))));
		}
		return alertActions;
	}

	/**
	 * This method initializes jPanel2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 1;
			gridBagConstraints10.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints10.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints10.gridy = 1;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 1;
			gridBagConstraints9.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints9.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints9.gridy = 0;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.anchor = GridBagConstraints.EAST;
			gridBagConstraints8.insets = new Insets(0, 5, 2, 0);
			gridBagConstraints8.gridy = 1;
			jLabel2 = new JLabel();
			jLabel2.setText("Max recovery retries:");
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.anchor = GridBagConstraints.EAST;
			gridBagConstraints7.insets = new Insets(0, 5, 2, 0);
			gridBagConstraints7.gridy = 0;
			jLabel1 = new JLabel();
			jLabel1.setText("GPS link timeout (s):");
			jPanel2 = new JPanel();
			jPanel2.setLayout(new GridBagLayout());
			jPanel2.setBorder(BorderFactory.createTitledBorder(null, "Problem determination", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanel2.add(jLabel1, gridBagConstraints7);
			jPanel2.add(jLabel2, gridBagConstraints8);
			jPanel2.add(getGpsLinkTimeout(), gridBagConstraints9);
			jPanel2.add(getGpsMaxRecoveryRetries(), gridBagConstraints10);
		}
		return jPanel2;
	}

	/**
	 * This method initializes gpsLinkTimeout	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getGpsLinkTimeout() {
		if (gpsLinkTimeout == null) {
			gpsLinkTimeout = new JMeasureSpinner<Double>();
			gpsLinkTimeout.setup(null, 5.0, 0.1, 99999, 1, 0, 3);
		}
		return gpsLinkTimeout;
	}

	/**
	 * This method initializes gpsMaxRecoveryRetries	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Integer> getGpsMaxRecoveryRetries() {
		if (gpsMaxRecoveryRetries == null) {
			gpsMaxRecoveryRetries = new JMeasureSpinner<Integer>();
			gpsMaxRecoveryRetries.setup(null, 30, 0, 9999999, 1, 0, 0);
		}
		return gpsMaxRecoveryRetries;
	}

	/**
	 * This method initializes jPanel3	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.fill = GridBagConstraints.BOTH;
			gridBagConstraints17.gridy = 0;
			gridBagConstraints17.weightx = 1.0;
			gridBagConstraints17.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints17.gridx = 1;
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.gridx = 1;
			gridBagConstraints16.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints16.anchor = GridBagConstraints.WEST;
			gridBagConstraints16.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints16.gridy = 3;
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 1;
			gridBagConstraints15.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints15.anchor = GridBagConstraints.WEST;
			gridBagConstraints15.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints15.gridy = 2;
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 0;
			gridBagConstraints14.anchor = GridBagConstraints.EAST;
			gridBagConstraints14.insets = new Insets(0, 5, 2, 0);
			gridBagConstraints14.gridy = 2;
			jLabel5 = new JLabel();
			jLabel5.setText("Time with stable link for success (s):");
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 0;
			gridBagConstraints13.anchor = GridBagConstraints.EAST;
			gridBagConstraints13.insets = new Insets(0, 5, 2, 0);
			gridBagConstraints13.gridy = 3;
			jLabel4 = new JLabel();
			jLabel4.setText("Timeout trying to recover link (s):");
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.anchor = GridBagConstraints.EAST;
			gridBagConstraints12.insets = new Insets(0, 5, 2, 0);
			gridBagConstraints12.gridy = 0;
			jLabel3 = new JLabel();
			jLabel3.setText("Gps link recovery action:");
			jPanel3 = new JPanel();
			jPanel3.setLayout(new GridBagLayout());
			jPanel3.setBorder(BorderFactory.createTitledBorder(null, "Recovery effort", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanel3.add(jLabel3, gridBagConstraints12);
			jPanel3.add(jLabel4, gridBagConstraints13);
			jPanel3.add(jLabel5, gridBagConstraints14);
			jPanel3.add(getGpsTimeStableSuccess(), gridBagConstraints15);
			jPanel3.add(getGpsRecoveryTimeout(), gridBagConstraints16);
			jPanel3.add(getGpsRecoveryAction(), gridBagConstraints17);
		}
		return jPanel3;
	}

	/**
	 * This method initializes gpsTimeStableSuccess	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getGpsTimeStableSuccess() {
		if (gpsTimeStableSuccess == null) {
			gpsTimeStableSuccess = new JMeasureSpinner<Double>();
			gpsTimeStableSuccess.setup(null, 2.0, 0.1, 9999, 1, 0, 3);
		}
		return gpsTimeStableSuccess;
	}

	/**
	 * This method initializes gpsRecoveryTimeout	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getGpsRecoveryTimeout() {
		if (gpsRecoveryTimeout == null) {
			gpsRecoveryTimeout = new JMeasureSpinner<Double>();
			gpsRecoveryTimeout.setup(null, 120.0, 0, 999999, 1, 0, 3);
		}
		return gpsRecoveryTimeout;
	}

	/**
	 * This method initializes gpsRecoveryAction	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getGpsRecoveryAction() {
		if (gpsRecoveryAction == null) {
			gpsRecoveryAction = new JComboBox();
			gpsRecoveryAction.setModel(getGpsLossActions());
		}
		return gpsRecoveryAction;
	}

	/**
	 * This method initializes jPanel4	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel4() {
		if (jPanel4 == null) {
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.fill = GridBagConstraints.BOTH;
			gridBagConstraints22.gridy = 0;
			gridBagConstraints22.weightx = 1.0;
			gridBagConstraints22.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints22.gridx = 1;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.fill = GridBagConstraints.BOTH;
			gridBagConstraints21.gridy = 1;
			gridBagConstraints21.weightx = 1.0;
			gridBagConstraints21.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints21.gridx = 1;
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			gridBagConstraints20.gridx = 0;
			gridBagConstraints20.insets = new Insets(0, 5, 2, 0);
			gridBagConstraints20.gridy = 1;
			jLabel7 = new JLabel();
			jLabel7.setText("Action on recovery FAILURE:");
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			gridBagConstraints19.gridx = 0;
			gridBagConstraints19.insets = new Insets(0, 5, 2, 0);
			gridBagConstraints19.gridy = 0;
			jLabel6 = new JLabel();
			jLabel6.setText("Mode on recovery SUCCESS:");
			jPanel4 = new JPanel();
			jPanel4.setLayout(new GridBagLayout());
			jPanel4.setBorder(BorderFactory.createTitledBorder(null, "Failure/success actions", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanel4.add(jLabel6, gridBagConstraints19);
			jPanel4.add(jLabel7, gridBagConstraints20);
			jPanel4.add(getGpsRecoveryFailureAction(), gridBagConstraints21);
			jPanel4.add(getGpsRecoverySuccessMode(), gridBagConstraints22);
		}
		return jPanel4;
	}

	/**
	 * This method initializes gpsRecoveryFailureAction	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getGpsRecoveryFailureAction() {
		if (gpsRecoveryFailureAction == null) {
			gpsRecoveryFailureAction = new JComboBox();
			gpsRecoveryFailureAction.setModel(getGpsLossActions());
		}
		return gpsRecoveryFailureAction;
	}

	/**
	 * This method initializes gpsRecoverySuccessMode	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getGpsRecoverySuccessMode() {
		if (gpsRecoverySuccessMode == null) {
			gpsRecoverySuccessMode = new JComboBox();
			gpsRecoverySuccessMode.setModel(getModeComboBox());
		}
		return gpsRecoverySuccessMode;
	}

	private ComboBoxModel getModeComboBox() {
		DefaultComboBoxModel m = new DefaultComboBoxModel();
		m.addElement(VehicleMode.NO_MODE);
		m.addElement(VehicleMode.WAYPOINT_MODE);
		m.addElement(VehicleMode.AUTOLAND_ENGAGE);
		m.addElement(VehicleMode.LOITER_AROUND_POSITION_MODE);
		m.addElement(VehicleMode.PREVIOUS_MODE);
		m.setSelectedItem(VehicleMode.WAYPOINT_MODE);
		return m;
	}

	/**
	 * This method initializes jPanel5	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel5() {
		if (jPanel5 == null) {
			GridBagConstraints gridBagConstraints81 = new GridBagConstraints();
			gridBagConstraints81.gridx = 1;
			gridBagConstraints81.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints81.gridy = 1;
			GridBagConstraints gridBagConstraints80 = new GridBagConstraints();
			gridBagConstraints80.gridx = 0;
			gridBagConstraints80.fill = GridBagConstraints.NONE;
			gridBagConstraints80.anchor = GridBagConstraints.EAST;
			gridBagConstraints80.gridy = 1;
			jLabel111 = new JLabel();
			jLabel111.setText("Min link strength (%):");
			GridBagConstraints gridBagConstraints27 = new GridBagConstraints();
			gridBagConstraints27.gridx = 1;
			gridBagConstraints27.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints27.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints27.gridy = 2;
			GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
			gridBagConstraints26.gridx = 1;
			gridBagConstraints26.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints26.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints26.gridy = 0;
			GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
			gridBagConstraints25.gridx = 0;
			gridBagConstraints25.insets = new Insets(0, 5, 2, 0);
			gridBagConstraints25.anchor = GridBagConstraints.EAST;
			gridBagConstraints25.gridy = 2;
			jLabel21 = new JLabel();
			jLabel21.setText("Max recovery retries:");
			GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
			gridBagConstraints24.gridx = 0;
			gridBagConstraints24.insets = new Insets(0, 5, 2, 0);
			gridBagConstraints24.anchor = GridBagConstraints.EAST;
			gridBagConstraints24.gridy = 0;
			jLabel11 = new JLabel();
			jLabel11.setText("Data link timeout (s):");
			jPanel5 = new JPanel();
			jPanel5.setLayout(new GridBagLayout());
			jPanel5.setBorder(BorderFactory.createTitledBorder(null, "Problem determination", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanel5.add(jLabel11, gridBagConstraints24);
			jPanel5.add(jLabel21, gridBagConstraints25);
			jPanel5.add(getDataLinkTimeout(), gridBagConstraints26);
			jPanel5.add(getDataLinkMaxRecoveryRetries(), gridBagConstraints27);
			jPanel5.add(jLabel111, gridBagConstraints80);
			jPanel5.add(getDataLinkMinStrength(), gridBagConstraints81);
		}
		return jPanel5;
	}

	/**
	 * This method initializes dataLinkTimeout	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getDataLinkTimeout() {
		if (dataLinkTimeout == null) {
			dataLinkTimeout = new JMeasureSpinner<Double>();
			dataLinkTimeout.setup(null, 10.0, 0.1, 99999, 1, 0, 3);
		}
		return dataLinkTimeout;
	}

	/**
	 * This method initializes dataLinkMaxRecoveryRetries	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Integer> getDataLinkMaxRecoveryRetries() {
		if (dataLinkMaxRecoveryRetries == null) {
			dataLinkMaxRecoveryRetries = new JMeasureSpinner<Integer>();
			dataLinkMaxRecoveryRetries.setup(null, 30, 0, 99999, 1, 0, 0);
		}
		return dataLinkMaxRecoveryRetries;
	}

	/**
	 * This method initializes jPanel6	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel6() {
		if (jPanel6 == null) {
			GridBagConstraints gridBagConstraints75 = new GridBagConstraints();
			gridBagConstraints75.fill = GridBagConstraints.BOTH;
			gridBagConstraints75.gridy = 4;
			gridBagConstraints75.weightx = 1.0;
			gridBagConstraints75.anchor = GridBagConstraints.WEST;
			gridBagConstraints75.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints75.gridx = 1;
			GridBagConstraints gridBagConstraints74 = new GridBagConstraints();
			gridBagConstraints74.gridx = 0;
			gridBagConstraints74.anchor = GridBagConstraints.EAST;
			gridBagConstraints74.gridy = 4;
			jLabel9121 = new JLabel();
			jLabel9121.setText("Recovery loiter altitude type:");
			GridBagConstraints gridBagConstraints73 = new GridBagConstraints();
			gridBagConstraints73.gridx = 1;
			gridBagConstraints73.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints73.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints73.gridy = 3;
			GridBagConstraints gridBagConstraints72 = new GridBagConstraints();
			gridBagConstraints72.gridx = 0;
			gridBagConstraints72.anchor = GridBagConstraints.EAST;
			gridBagConstraints72.gridy = 3;
			jLabel912 = new JLabel();
			jLabel912.setText("Recovery loiter altitude:");
			GridBagConstraints gridBagConstraints38 = new GridBagConstraints();
			gridBagConstraints38.gridx = 1;
			gridBagConstraints38.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints38.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints38.weightx = 0.0;
			gridBagConstraints38.gridy = 6;
			GridBagConstraints gridBagConstraints37 = new GridBagConstraints();
			gridBagConstraints37.gridx = 1;
			gridBagConstraints37.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints37.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints37.weightx = 0.0;
			gridBagConstraints37.gridy = 7;
			GridBagConstraints gridBagConstraints36 = new GridBagConstraints();
			gridBagConstraints36.gridx = 1;
			gridBagConstraints36.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints36.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints36.weightx = 0.0;
			gridBagConstraints36.gridy = 5;
			GridBagConstraints gridBagConstraints35 = new GridBagConstraints();
			gridBagConstraints35.gridx = 1;
			gridBagConstraints35.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints35.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints35.weightx = 0.0;
			gridBagConstraints35.gridy = 2;
			GridBagConstraints gridBagConstraints34 = new GridBagConstraints();
			gridBagConstraints34.gridx = 1;
			gridBagConstraints34.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints34.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints34.weightx = 0.0;
			gridBagConstraints34.gridy = 1;
			GridBagConstraints gridBagConstraints33 = new GridBagConstraints();
			gridBagConstraints33.gridx = 0;
			gridBagConstraints33.anchor = GridBagConstraints.EAST;
			gridBagConstraints33.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints33.gridy = 6;
			jLabel13 = new JLabel();
			jLabel13.setText("Time with stable link for success (s):");
			GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
			gridBagConstraints32.gridx = 0;
			gridBagConstraints32.anchor = GridBagConstraints.EAST;
			gridBagConstraints32.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints32.gridy = 7;
			jLabel12 = new JLabel();
			jLabel12.setText("Timeout loitering wating for link recovery (s):");
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.gridx = 0;
			gridBagConstraints31.anchor = GridBagConstraints.EAST;
			gridBagConstraints31.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints31.gridy = 5;
			jLabel10 = new JLabel();
			jLabel10.setText("Timeout reaching loiter location (s):");
			GridBagConstraints gridBagConstraints30 = new GridBagConstraints();
			gridBagConstraints30.gridx = 0;
			gridBagConstraints30.anchor = GridBagConstraints.EAST;
			gridBagConstraints30.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints30.gridy = 2;
			jLabel91 = new JLabel();
			jLabel91.setText("Recovery loiter longitude:");
			GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
			gridBagConstraints29.gridx = 0;
			gridBagConstraints29.anchor = GridBagConstraints.EAST;
			gridBagConstraints29.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints29.gridy = 1;
			jLabel9 = new JLabel();
			jLabel9.setText("Recovery loiter latitude:");
			jPanel6 = new JPanel();
			jPanel6.setLayout(new GridBagLayout());
			jPanel6.setBorder(BorderFactory.createTitledBorder(null, "Recovery effort", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanel6.add(jLabel9, gridBagConstraints29);
			jPanel6.add(jLabel91, gridBagConstraints30);
			jPanel6.add(jLabel10, gridBagConstraints31);
			jPanel6.add(jLabel12, gridBagConstraints32);
			jPanel6.add(jLabel13, gridBagConstraints33);
			jPanel6.add(getDataLinkRecoveryLatitude(), gridBagConstraints34);
			jPanel6.add(getDataLinkRecoveryLongitude(), gridBagConstraints35);
			jPanel6.add(getDataLinkReachRecoveryLoiterTimeout(), gridBagConstraints36);
			jPanel6.add(getDataLinkMaxTimeLoiteringRecovery(), gridBagConstraints37);
			jPanel6.add(getDataLinkStableLinkSuccess(), gridBagConstraints38);
			jPanel6.add(jLabel912, gridBagConstraints72);
			jPanel6.add(getDataLinkRecoveryAltitude(), gridBagConstraints73);
			jPanel6.add(jLabel9121, gridBagConstraints74);
			jPanel6.add(getDataLinkRecoveryAltitudeType(), gridBagConstraints75);
		}
		return jPanel6;
	}

	/**
	 * This method initializes dataLinkRecoveryLatitude	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getDataLinkRecoveryLatitude() {
		if (dataLinkRecoveryLatitude == null) {
			dataLinkRecoveryLatitude = new JMeasureSpinner<Double>();
			dataLinkRecoveryLatitude.setup(MeasureType.GEO_POSITION, 0.0, Math.toRadians(-90), Math.toRadians(90), Math.toRadians(0.1), 0, 9);
		}
		return dataLinkRecoveryLatitude;
	}

	/**
	 * This method initializes dataLinkRecoveryLongitude	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getDataLinkRecoveryLongitude() {
		if (dataLinkRecoveryLongitude == null) {
			dataLinkRecoveryLongitude = new JMeasureSpinner<Double>();
			dataLinkRecoveryLongitude.setup(MeasureType.GEO_POSITION, 0.0, Math.toRadians(-180), Math.toRadians(180), Math.toRadians(0.1), 0, 9);
		}
		return dataLinkRecoveryLongitude;
	}

	/**
	 * This method initializes dataLinkReachRecoveryLoiterTimeout	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getDataLinkReachRecoveryLoiterTimeout() {
		if (dataLinkReachRecoveryLoiterTimeout == null) {
			dataLinkReachRecoveryLoiterTimeout = new JMeasureSpinner<Double>();
			dataLinkReachRecoveryLoiterTimeout.setup(null, 300.0, 0, 999999, 10, 0, 0);
		}
		return dataLinkReachRecoveryLoiterTimeout;
	}

	/**
	 * This method initializes dataLinkMaxTimeLoiteringRecovery	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getDataLinkMaxTimeLoiteringRecovery() {
		if (dataLinkMaxTimeLoiteringRecovery == null) {
			dataLinkMaxTimeLoiteringRecovery = new JMeasureSpinner<Double>();
			dataLinkMaxTimeLoiteringRecovery.setup(null, 600.0, 0, 999999, 10, 0, 0);
		}
		return dataLinkMaxTimeLoiteringRecovery;
	}

	/**
	 * This method initializes dataLinkStableLinkSuccess	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getDataLinkStableLinkSuccess() {
		if (dataLinkStableLinkSuccess == null) {
			dataLinkStableLinkSuccess = new JMeasureSpinner<Double>();
			dataLinkStableLinkSuccess.setup(null, 5.0, 0.1, 9999, 1, 0, 3);
		}
		return dataLinkStableLinkSuccess;
	}

	/**
	 * This method initializes jPanel7	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel7() {
		if (jPanel7 == null) {
			GridBagConstraints gridBagConstraints79 = new GridBagConstraints();
			gridBagConstraints79.fill = GridBagConstraints.BOTH;
			gridBagConstraints79.gridy = 3;
			gridBagConstraints79.weightx = 1.0;
			gridBagConstraints79.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints79.gridx = 1;
			GridBagConstraints gridBagConstraints78 = new GridBagConstraints();
			gridBagConstraints78.gridx = 0;
			gridBagConstraints78.anchor = GridBagConstraints.EAST;
			gridBagConstraints78.gridy = 3;
			jLabel91111 = new JLabel();
			jLabel91111.setText("Manual recovery loiter altitude type:");
			GridBagConstraints gridBagConstraints77 = new GridBagConstraints();
			gridBagConstraints77.gridx = 1;
			gridBagConstraints77.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints77.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints77.gridy = 2;
			GridBagConstraints gridBagConstraints76 = new GridBagConstraints();
			gridBagConstraints76.gridx = 0;
			gridBagConstraints76.anchor = GridBagConstraints.EAST;
			gridBagConstraints76.gridy = 2;
			jLabel9111 = new JLabel();
			jLabel9111.setText("Manual recovery loiter altitude:");
			GridBagConstraints gridBagConstraints47 = new GridBagConstraints();
			gridBagConstraints47.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints47.gridy = 6;
			gridBagConstraints47.weightx = 1.0;
			gridBagConstraints47.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints47.gridx = 1;
			GridBagConstraints gridBagConstraints46 = new GridBagConstraints();
			gridBagConstraints46.gridx = 1;
			gridBagConstraints46.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints46.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints46.anchor = GridBagConstraints.WEST;
			gridBagConstraints46.gridy = 5;
			GridBagConstraints gridBagConstraints45 = new GridBagConstraints();
			gridBagConstraints45.gridx = 1;
			gridBagConstraints45.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints45.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints45.anchor = GridBagConstraints.WEST;
			gridBagConstraints45.gridy = 4;
			GridBagConstraints gridBagConstraints44 = new GridBagConstraints();
			gridBagConstraints44.gridx = 1;
			gridBagConstraints44.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints44.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints44.anchor = GridBagConstraints.WEST;
			gridBagConstraints44.gridy = 1;
			GridBagConstraints gridBagConstraints43 = new GridBagConstraints();
			gridBagConstraints43.gridx = 1;
			gridBagConstraints43.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints43.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints43.anchor = GridBagConstraints.WEST;
			gridBagConstraints43.gridy = 0;
			GridBagConstraints gridBagConstraints42 = new GridBagConstraints();
			gridBagConstraints42.gridx = 0;
			gridBagConstraints42.anchor = GridBagConstraints.EAST;
			gridBagConstraints42.insets = new Insets(0, 5, 2, 0);
			gridBagConstraints42.gridy = 6;
			jLabel131 = new JLabel();
			jLabel131.setText("Action on manual recovery FAILURE:");
			GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
			gridBagConstraints41.gridx = 0;
			gridBagConstraints41.anchor = GridBagConstraints.EAST;
			gridBagConstraints41.insets = new Insets(0, 5, 2, 0);
			gridBagConstraints41.gridy = 5;
			jLabel121 = new JLabel();
			jLabel121.setText("Timeout loitering waiting manual recovery (s):");
			GridBagConstraints gridBagConstraints40 = new GridBagConstraints();
			gridBagConstraints40.gridx = 0;
			gridBagConstraints40.anchor = GridBagConstraints.EAST;
			gridBagConstraints40.insets = new Insets(0, 5, 2, 0);
			gridBagConstraints40.gridy = 4;
			jLabel101 = new JLabel();
			jLabel101.setText("Timeout reaching loiter location (s):");
			GridBagConstraints gridBagConstraints39 = new GridBagConstraints();
			gridBagConstraints39.gridx = 0;
			gridBagConstraints39.anchor = GridBagConstraints.EAST;
			gridBagConstraints39.insets = new Insets(0, 5, 2, 0);
			gridBagConstraints39.gridy = 1;
			jLabel911 = new JLabel();
			jLabel911.setText("Manual recovery loiter longitude:");
			GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
			gridBagConstraints28.gridx = 0;
			gridBagConstraints28.anchor = GridBagConstraints.EAST;
			gridBagConstraints28.insets = new Insets(0, 5, 2, 0);
			gridBagConstraints28.gridy = 0;
			jLabel92 = new JLabel();
			jLabel92.setText("Manual recovery loiter latitude:");
			jPanel7 = new JPanel();
			jPanel7.setLayout(new GridBagLayout());
			jPanel7.setBorder(BorderFactory.createTitledBorder(null, "'Manual recovery' action configuration", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanel7.add(jLabel92, gridBagConstraints28);
			jPanel7.add(jLabel911, gridBagConstraints39);
			jPanel7.add(jLabel101, gridBagConstraints40);
			jPanel7.add(jLabel121, gridBagConstraints41);
			jPanel7.add(jLabel131, gridBagConstraints42);
			jPanel7.add(getManualRecoveryLoiterLatitude(), gridBagConstraints43);
			jPanel7.add(getManualRecoveryLoiterLongitude(), gridBagConstraints44);
			jPanel7.add(getManualRecoveryReachLocationTimeout(), gridBagConstraints45);
			jPanel7.add(getManualRecoveryTimeoutLoiteringLocation(), gridBagConstraints46);
			jPanel7.add(getManualRecoveryActionOnTimeout(), gridBagConstraints47);
			jPanel7.add(jLabel9111, gridBagConstraints76);
			jPanel7.add(getManualRecoveryLoiterAltitude(), gridBagConstraints77);
			jPanel7.add(jLabel91111, gridBagConstraints78);
			jPanel7.add(getManualRecoveryAltitudeType(), gridBagConstraints79);
		}
		return jPanel7;
	}

	/**
	 * This method initializes dataLinkManualRecoveryLoiterLatitude	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getManualRecoveryLoiterLatitude() {
		if (manualRecoveryLoiterLatitude == null) {
			manualRecoveryLoiterLatitude = new JMeasureSpinner<Double>();
			manualRecoveryLoiterLatitude.setup(MeasureType.GEO_POSITION, 0.0, Math.toRadians(-90), Math.toRadians(90), Math.toRadians(0.1), 0, 9);
		}
		return manualRecoveryLoiterLatitude;
	}

	/**
	 * This method initializes dataLinkManualRecoveryLoiterLongitude	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getManualRecoveryLoiterLongitude() {
		if (manualRecoveryLoiterLongitude == null) {
			manualRecoveryLoiterLongitude = new JMeasureSpinner<Double>();
			manualRecoveryLoiterLongitude.setup(MeasureType.GEO_POSITION, 0.0, Math.toRadians(-180), Math.toRadians(180), Math.toRadians(0.1), 0, 9);
		}
		return manualRecoveryLoiterLongitude;
	}

	/**
	 * This method initializes dataLinkReachManualRecoveryTimeout	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getManualRecoveryReachLocationTimeout() {
		if (manualRecoveryReachLocationTimeout == null) {
			manualRecoveryReachLocationTimeout = new JMeasureSpinner<Double>();
			manualRecoveryReachLocationTimeout.setup(null, 12000.0, 1, 9999999, 10, 0, 0);
		}
		return manualRecoveryReachLocationTimeout;
	}

	/**
	 * This method initializes dataLinkMaxTimeLoiteringManualRecovery	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getManualRecoveryTimeoutLoiteringLocation() {
		if (manualRecoveryTimeoutLoiteringLocation == null) {
			manualRecoveryTimeoutLoiteringLocation = new JMeasureSpinner<Double>();
			manualRecoveryTimeoutLoiteringLocation.setup(null, 600.0, 1, 9999999, 10, 0, 0);
		}
		return manualRecoveryTimeoutLoiteringLocation;
	}

	/**
	 * This method initializes manualRecoveryActionOnTimeout	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getManualRecoveryActionOnTimeout() {
		if (manualRecoveryActionOnTimeout == null) {
			manualRecoveryActionOnTimeout = new JComboBox();
			manualRecoveryActionOnTimeout.setModel(new EnumComboBoxModel<SafetyAction>(SafetyAction.class));
		}
		return manualRecoveryActionOnTimeout;
	}

	/**
	 * This method initializes jPanel8	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel8() {
		if (jPanel8 == null) {
			GridBagConstraints gridBagConstraints71 = new GridBagConstraints();
			gridBagConstraints71.fill = GridBagConstraints.BOTH;
			gridBagConstraints71.gridy = 1;
			gridBagConstraints71.weightx = 1.0;
			gridBagConstraints71.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints71.gridx = 1;
			GridBagConstraints gridBagConstraints70 = new GridBagConstraints();
			gridBagConstraints70.gridx = 0;
			gridBagConstraints70.gridy = 1;
			jLabel71 = new JLabel();
			jLabel71.setText("Action on recovery FAILURE:");
			GridBagConstraints gridBagConstraints49 = new GridBagConstraints();
			gridBagConstraints49.fill = GridBagConstraints.BOTH;
			gridBagConstraints49.gridy = 0;
			gridBagConstraints49.weightx = 1.0;
			gridBagConstraints49.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints49.gridx = 1;
			GridBagConstraints gridBagConstraints48 = new GridBagConstraints();
			gridBagConstraints48.gridx = 0;
			gridBagConstraints48.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints48.gridy = 0;
			jLabel8 = new JLabel();
			jLabel8.setText("Mode on recovery SUCCESS:");
			jPanel8 = new JPanel();
			jPanel8.setLayout(new GridBagLayout());
			jPanel8.setBorder(BorderFactory.createTitledBorder(null, "Failure/success actions", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanel8.add(jLabel8, gridBagConstraints48);
			jPanel8.add(getDataLinkRecoverySuccessMode(), gridBagConstraints49);
			jPanel8.add(jLabel71, gridBagConstraints70);
			jPanel8.add(getDataLinkActionOnRecoveryFailure(), gridBagConstraints71);
		}
		return jPanel8;
	}

	/**
	 * This method initializes dataLinkRecoverySuccessMode	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getDataLinkRecoverySuccessMode() {
		if (dataLinkRecoverySuccessMode == null) {
			dataLinkRecoverySuccessMode = new JComboBox();
			dataLinkRecoverySuccessMode.setModel(getModeComboBox());
		}
		return dataLinkRecoverySuccessMode;
	}

	/**
	 * This method initializes dataLinkScroll	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getDataLinkScroll() {
		if (dataLinkScroll == null) {
			dataLinkScroll = new JScrollPane();
			dataLinkScroll.setViewportView(getJPanel9());
		}
		return dataLinkScroll;
	}

	/**
	 * This method initializes jPanel9	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel9() {
		if (jPanel9 == null) {
			GridBagConstraints gridBagConstraints54 = new GridBagConstraints();
			gridBagConstraints54.gridx = 0;
			gridBagConstraints54.weightx = 1.0;
			gridBagConstraints54.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints54.insets = new Insets(0, 2, 2, 2);
			gridBagConstraints54.weighty = 1.0;
			gridBagConstraints54.anchor = GridBagConstraints.NORTH;
			gridBagConstraints54.gridy = 3;
			GridBagConstraints gridBagConstraints52 = new GridBagConstraints();
			gridBagConstraints52.gridx = 0;
			gridBagConstraints52.weightx = 1.0;
			gridBagConstraints52.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints52.insets = new Insets(0, 2, 2, 2);
			gridBagConstraints52.gridy = 1;
			GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
			gridBagConstraints51.gridx = 0;
			gridBagConstraints51.weightx = 1.0;
			gridBagConstraints51.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints51.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints51.gridy = 0;
			jPanel9 = new JPanel();
			jPanel9.setLayout(new GridBagLayout());
			jPanel9.add(getJPanel5(), gridBagConstraints51);
			jPanel9.add(getJPanel6(), gridBagConstraints52);
			jPanel9.add(getJPanel8(), gridBagConstraints54);
		}
		return jPanel9;
	}

	/**
	 * This method initializes regionMinAltitude	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getRegionMinAltitude() {
		if (regionMinAltitude == null) {
			regionMinAltitude = new JMeasureSpinner<Float>();
			regionMinAltitude.setup(MeasureType.ALTITUDE, 50F, 1, 99999999, 10, 0, 3);
		}
		return regionMinAltitude;
	}

	/**
	 * This method initializes regionMaxAltitude	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getRegionMaxAltitude() {
		if (regionMaxAltitude == null) {
			regionMaxAltitude = new JMeasureSpinner<Float>();
			regionMaxAltitude.setup(MeasureType.ALTITUDE, 50F, 1, 99999999, 10, 0, 3);
		}
		return regionMaxAltitude;
	}

	/**
	 * This method initializes regionAltitudeType	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getRegionAltitudeType() {
		if (regionAltitudeType == null) {
			regionAltitudeType = new JComboBox();
			regionAltitudeType.setModel(new EnumComboBoxModel<AltitudeType>(AltitudeType.class));
		}
		return regionAltitudeType;
	}

	/**
	 * This method initializes authorizedRegion	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getAuthorizedRegion() {
		if (authorizedRegion == null) {
			authorizedRegion = new JTextArea();
			authorizedRegion.setRows(3);
			authorizedRegion.setToolTipText("-Each line contains one coordinate;\n-Separate lat/long with comma;\n-Separate multiple regions with an additional line;\n\n-Ex.: \n-45.423;-15.498\n-45.422;-15.499\n-45.423;-15.498\n-45.422;-15.499");
		}
		return authorizedRegion;
	}

	/**
	 * This method initializes authorizedRegionScroll	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getAuthorizedRegionScroll() {
		if (authorizedRegionScroll == null) {
			authorizedRegionScroll = new JScrollPane();
			authorizedRegionScroll.setViewportView(getAuthorizedRegion());
		}
		return authorizedRegionScroll;
	}

	/**
	 * This method initializes jPanel10	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel10() {
		if (jPanel10 == null) {
			GridBagConstraints gridBagConstraints62 = new GridBagConstraints();
			gridBagConstraints62.anchor = GridBagConstraints.WEST;
			gridBagConstraints62.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints62.gridx = 1;
			gridBagConstraints62.gridy = 2;
			gridBagConstraints62.weightx = 1.0;
			gridBagConstraints62.fill = GridBagConstraints.VERTICAL;
			GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
			gridBagConstraints61.anchor = GridBagConstraints.WEST;
			gridBagConstraints61.gridx = 1;
			gridBagConstraints61.gridy = 1;
			gridBagConstraints61.insets = new Insets(0, 5, 2, 5);
			GridBagConstraints gridBagConstraints60 = new GridBagConstraints();
			gridBagConstraints60.anchor = GridBagConstraints.WEST;
			gridBagConstraints60.gridx = 1;
			gridBagConstraints60.gridy = 0;
			gridBagConstraints60.insets = new Insets(0, 5, 2, 5);
			GridBagConstraints gridBagConstraints59 = new GridBagConstraints();
			gridBagConstraints59.anchor = GridBagConstraints.EAST;
			gridBagConstraints59.gridx = 0;
			gridBagConstraints59.gridy = 2;
			gridBagConstraints59.insets = new Insets(0, 5, 2, 0);
			GridBagConstraints gridBagConstraints58 = new GridBagConstraints();
			gridBagConstraints58.anchor = GridBagConstraints.EAST;
			gridBagConstraints58.gridx = 0;
			gridBagConstraints58.gridy = 1;
			gridBagConstraints58.insets = new Insets(0, 5, 2, 0);
			GridBagConstraints gridBagConstraints57 = new GridBagConstraints();
			gridBagConstraints57.anchor = GridBagConstraints.EAST;
			gridBagConstraints57.gridx = 0;
			gridBagConstraints57.gridy = 0;
			gridBagConstraints57.insets = new Insets(0, 5, 2, 0);
			jPanel10 = new JPanel();
			jPanel10.setLayout(new GridBagLayout());
			jPanel10.setBorder(BorderFactory.createTitledBorder(null, "Authorized altitudes", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanel10.add(jLabel15, gridBagConstraints57);
			jPanel10.add(jLabel16, gridBagConstraints58);
			jPanel10.add(jLabel17, gridBagConstraints59);
			jPanel10.add(getRegionMinAltitude(), gridBagConstraints60);
			jPanel10.add(getRegionMaxAltitude(), gridBagConstraints61);
			jPanel10.add(getRegionAltitudeType(), gridBagConstraints62);
		}
		return jPanel10;
	}

	/**
	 * This method initializes regionEditProhibitedRegions	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getRegionEditProhibitedRegions() {
		if (regionEditProhibitedRegions == null) {
			GridBagConstraints gridBagConstraints64 = new GridBagConstraints();
			gridBagConstraints64.fill = GridBagConstraints.BOTH;
			gridBagConstraints64.gridy = 2;
			gridBagConstraints64.weightx = 1.0;
			gridBagConstraints64.weighty = 1.0;
			gridBagConstraints64.insets = new Insets(0, 5, 2, 5);
			gridBagConstraints64.gridheight = 2;
			gridBagConstraints64.gridx = 1;
			GridBagConstraints gridBagConstraints56 = new GridBagConstraints();
			gridBagConstraints56.gridx = 0;
			gridBagConstraints56.anchor = GridBagConstraints.NORTHEAST;
			gridBagConstraints56.gridy = 2;
			jLabel141 = new JLabel();
			jLabel141.setText("Prohibited regions coordinates:");
			GridBagConstraints gridBagConstraints63 = new GridBagConstraints();
			gridBagConstraints63.fill = GridBagConstraints.BOTH;
			gridBagConstraints63.gridx = 1;
			gridBagConstraints63.gridy = 0;
			gridBagConstraints63.weightx = 1.0;
			gridBagConstraints63.weighty = 0.5;
			gridBagConstraints63.gridheight = 2;
			gridBagConstraints63.insets = new Insets(0, 5, 2, 5);
			GridBagConstraints gridBagConstraints55 = new GridBagConstraints();
			gridBagConstraints55.anchor = GridBagConstraints.NORTHEAST;
			gridBagConstraints55.gridx = 0;
			gridBagConstraints55.gridy = 0;
			gridBagConstraints55.insets = new Insets(0, 5, 2, 0);
			regionEditProhibitedRegions = new JPanel();
			regionEditProhibitedRegions.setLayout(new GridBagLayout());
			regionEditProhibitedRegions.setBorder(BorderFactory.createTitledBorder(null, "Authorized operation region", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			regionEditProhibitedRegions.add(jLabel14, gridBagConstraints55);
			regionEditProhibitedRegions.add(getAuthorizedRegionScroll(), gridBagConstraints63);
			regionEditProhibitedRegions.add(jLabel141, gridBagConstraints56);
			regionEditProhibitedRegions.add(getProhibitedRegionsScroll(), gridBagConstraints64);
		}
		return regionEditProhibitedRegions;
	}

	/**
	 * This method initializes prohibitedRegionsScroll	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getProhibitedRegionsScroll() {
		if (prohibitedRegionsScroll == null) {
			prohibitedRegionsScroll = new JScrollPane();
			prohibitedRegionsScroll.setViewportView(getProhibitedRegions());
		}
		return prohibitedRegionsScroll;
	}

	/**
	 * This method initializes prohibitedRegions	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getProhibitedRegions() {
		if (prohibitedRegions == null) {
			prohibitedRegions = new JTextArea();
			prohibitedRegions.setRows(3);
			prohibitedRegions.setToolTipText("-Each line contains one coordinate;\n-Separate lat/long with comma;\n-Separate multiple regions with an additional line;\n\n-Ex.: \n-45.423;-15.498\n-45.422;-15.499\n-45.423;-15.498\n-45.422;-15.499\n\n-45.422;-15.499\n-45.423;-15.498\n-45.422;-15.499\n-45.423;-15.498");

		}
		return prohibitedRegions;
	}

	/**
	 * This method initializes jPanel11	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel11() {
		if (jPanel11 == null) {
			GridBagConstraints gridBagConstraints68 = new GridBagConstraints();
			gridBagConstraints68.gridx = 1;
			gridBagConstraints68.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints68.gridy = 0;
			GridBagConstraints gridBagConstraints67 = new GridBagConstraints();
			gridBagConstraints67.gridx = 0;
			gridBagConstraints67.gridy = 0;
			jLabel18 = new JLabel();
			jLabel18.setText("Max flight time (min):");
			jPanel11 = new JPanel();
			jPanel11.setLayout(new GridBagLayout());
			jPanel11.setBorder(BorderFactory.createTitledBorder(null, "Flight time", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.PLAIN, 11), new Color(0, 70, 213)));
			jPanel11.add(jLabel18, gridBagConstraints67);
			jPanel11.add(getMaxFlightTime(), gridBagConstraints68);
		}
		return jPanel11;
	}

	/**
	 * This method initializes maxFlightTime	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Integer> getMaxFlightTime() {
		if (maxFlightTime == null) {
			maxFlightTime = new JMeasureSpinner<Integer>();
			maxFlightTime.setup(null, 30, 1, 99999999, 10, 0, 0);
		}
		return maxFlightTime;
	}

	/**
	 * This method initializes dataLinkActionOnRecoveryFailure	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getDataLinkActionOnRecoveryFailure() {
		if (dataLinkActionOnRecoveryFailure == null) {
			dataLinkActionOnRecoveryFailure = new JComboBox();
			dataLinkActionOnRecoveryFailure.setModel(new EnumComboBoxModel<SafetyAction>(SafetyAction.class));
		}
		return dataLinkActionOnRecoveryFailure;
	}


	private void loadGUI() {
		RulesOfSafety ros = skylightMission.getRulesOfSafety();
		
		//GENERAL TAB
		//enable gps/data link recovery
		getEnableGpsRecovery().setSelected(ros.isGpsSignalRecoveryEnabled());
		getEnableDataLinkRecovery().setSelected(ros.isDataLinkRecoveryEnabled());
		getBothLostAction().setSelectedItem(ros.getActionOnGpsAndDataLinkLost());

		//manual recovery action configuration
		getManualRecoveryLoiterLatitude().setValue(ros.getManualRecoveryLoiterLocation().getLatitudeRadians());
		getManualRecoveryLoiterLongitude().setValue(ros.getManualRecoveryLoiterLocation().getLongitudeRadians());
		getManualRecoveryLoiterAltitude().setValue(ros.getManualRecoveryLoiterLocation().getAltitude());
		getManualRecoveryAltitudeType().setSelectedItem(ros.getManualRecoveryLoiterAltitudeType());
		getManualRecoveryReachLocationTimeout().setValue(ros.getManualRecoveryReachLoiterLocationTimeout());
		getManualRecoveryTimeoutLoiteringLocation().setValue(ros.getManualRecoveryLoiterTimeout());
		getManualRecoveryActionOnTimeout().setSelectedItem(ros.getManualRecoveryActionOnLoiterTimeout());
		
		//alert safety actions table
		TypedTableModel<SafetyActionForAlert> m = (TypedTableModel<SafetyActionForAlert>)getAlertActions().getModel();
		List<SafetyActionForAlert> l = new ArrayList<SafetyActionForAlert>();
		for (Alert a : Alert.values()) {
			if(a.isSafetyActionEnabled()) {
				l.add(new SafetyActionForAlert(a, ros.getSafetyActionForAlert(a)));
			}
		}
		Collections.sort(l, new Comparator<SafetyActionForAlert>() {
			public int compare(SafetyActionForAlert o1, SafetyActionForAlert o2) {
				return o1.getAlert().name().compareTo(o2.getAlert().name());
			}
		});
		m.setUserObjects(l);
		getAlertActions().updateUI();

		//GPS LOSS TAB
		getGpsLinkTimeout().setValue(ros.getGpsLinkTimeout());
		getGpsMaxRecoveryRetries().setValue(ros.getGpsMaxRecoveryRetries());
		getGpsRecoveryAction().setSelectedItem(ros.getGpsLinkRecoveryAction());
		getGpsTimeStableSuccess().setValue(ros.getGpsTimeWithStableLinkForSuccess());
		getGpsRecoveryTimeout().setValue(ros.getGpsTimeoutTryingRecoverLink());
		getGpsRecoverySuccessMode().setSelectedItem((VehicleMode)ros.getGpsModeOnRecoverySuccess());
		getGpsRecoveryFailureAction().setSelectedItem((SafetyAction)ros.getGpsActionOnRecoveryFailure());

		//DATA LINK LOSS TAB
		getDataLinkTimeout().setValue(ros.getDataLinkTimeout());
		getDataLinkMinStrength().setValue(Integer.parseInt(ros.getDataLinkMinStrength()+""));
		getDataLinkMaxRecoveryRetries().setValue(ros.getDataLinkMaxRecoveryRetries());
		getDataLinkRecoveryLatitude().setValue(ros.getDataLinkRecoveryLoiterLocation().getLatitudeRadians());
		getDataLinkRecoveryLongitude().setValue(ros.getDataLinkRecoveryLoiterLocation().getLongitudeRadians());
		getDataLinkRecoveryAltitude().setValue(ros.getDataLinkRecoveryLoiterLocation().getAltitude());
		getDataLinkRecoveryAltitudeType().setSelectedItem(ros.getDataLinkRecoveryLoiterAltitudeType());
		getDataLinkReachRecoveryLoiterTimeout().setValue(ros.getDataLinkTimeoutReachingLoiterLocation());
		getDataLinkStableLinkSuccess().setValue(ros.getDataLinkTimeStableLinkForSuccess());
		getDataLinkMaxTimeLoiteringRecovery().setValue(ros.getDataLinkTimeoutLoiteringWaitingRecovery());
		getDataLinkRecoverySuccessMode().setSelectedItem(ros.getDataLinkModeOnLinkRecoverySuccess());
		getDataLinkActionOnRecoveryFailure().setSelectedItem(ros.getDataLinkActionOnLinkRecoveryFailure());

		//LIMITS TAB
		getMaxFlightTime().setValue(ros.getMaxFlightTimeMinutes());
		getRegionMinAltitude().setValue(ros.getMinAltitude());
		getRegionMaxAltitude().setValue(ros.getMaxAltitude());
		getRegionAltitudeType().setSelectedItem(ros.getMinMaxAltitudeType());
		getAuthorizedRegion().setText(ros.getAuthorizedRegion().toString());
		
		//authorized region
		getAuthorizedRegion().setText(ros.getAuthorizedRegion().toString());
		
		//prohibited regions
		String s = "";
		for (Region pr : ros.getProhibitedRegions()) {
			s += pr.toString() + "\n";
		}
		getProhibitedRegions().setText(s);
	}
	
	@Override
	public boolean onOkPressed() {
		RulesOfSafety ros = skylightMission.getRulesOfSafety();
		
		//GENERAL TAB
		//enable gps/data link recovery
		ros.setGpsSignalRecoveryEnabled(getEnableGpsRecovery().isSelected());
		ros.setDataLinkRecoveryEnabled(getEnableDataLinkRecovery().isSelected());
		ros.setActionOnGpsAndDataLinkLost((SafetyAction)getBothLostAction().getSelectedItem());

		//manual recovery action configuration
		ros.getManualRecoveryLoiterLocation().setLatitudeRadians(getManualRecoveryLoiterLatitude().getValue());
		ros.getManualRecoveryLoiterLocation().setLongitudeRadians(getManualRecoveryLoiterLongitude().getValue());
		ros.getManualRecoveryLoiterLocation().setAltitude(getManualRecoveryLoiterAltitude().getValue());
		ros.setManualRecoveryLoiterAltitudeType((AltitudeType)getManualRecoveryAltitudeType().getSelectedItem());
		ros.setManualRecoveryReachLoiterLocationTimeout(getManualRecoveryReachLocationTimeout().getValue());
		ros.setManualRecoveryLoiterTimeout(getManualRecoveryTimeoutLoiteringLocation().getValue());
		ros.setManualRecoveryActionOnLoiterTimeout((SafetyAction)getManualRecoveryActionOnTimeout().getSelectedItem());
		
		//alert safety actions table
		TypedTableModel<SafetyActionForAlert> m = (TypedTableModel<SafetyActionForAlert>)getAlertActions().getModel();
		ros.getSafetyActions().clear();
		for (SafetyActionForAlert sa : m.getUserObjects()) {
			if(!sa.getSafetyAction().equals(SafetyAction.DO_NOTHING)) {
				ros.getSafetyActions().add(sa);
			}
		}

		//GPS LOSS TAB
		ros.setGpsLinkTimeout(getGpsLinkTimeout().getValue());
		ros.setGpsMaxRecoveryRetries(getGpsMaxRecoveryRetries().getValue());
		ros.setGpsLinkRecoveryAction((SafetyAction)getGpsRecoveryAction().getSelectedItem());
		ros.setGpsTimeWithStableLinkForSuccess(getGpsTimeStableSuccess().getValue());
		ros.setGpsTimeoutTryingRecoverLink(getGpsRecoveryTimeout().getValue());
		ros.setGpsModeOnRecoverySuccess((VehicleMode)getGpsRecoverySuccessMode().getSelectedItem());
		ros.setGpsActionOnRecoveryFailure((SafetyAction)getGpsRecoveryFailureAction().getSelectedItem());

		//DATA LINK LOSS TAB
		ros.setDataLinkTimeout(getDataLinkTimeout().getValue());
		ros.setDataLinkMinStrength(getDataLinkMinStrength().getValue().byteValue());
		ros.setDataLinkMaxRecoveryRetries(getDataLinkMaxRecoveryRetries().getValue());
		ros.getDataLinkRecoveryLoiterLocation().setLatitudeRadians(getDataLinkRecoveryLatitude().getValue());
		ros.getDataLinkRecoveryLoiterLocation().setLongitudeRadians(getDataLinkRecoveryLongitude().getValue());
		ros.getDataLinkRecoveryLoiterLocation().setAltitude(getDataLinkRecoveryAltitude().getValue());
		ros.setDataLinkRecoveryLoiterAltitudeType((AltitudeType)getDataLinkRecoveryAltitudeType().getSelectedItem());
		ros.setDataLinkTimeoutReachingLoiterLocation(getDataLinkReachRecoveryLoiterTimeout().getValue());
		ros.setDataLinkTimeStableLinkForSuccess(getDataLinkStableLinkSuccess().getValue());
		ros.setDataLinkTimeoutLoiteringWaitingRecovery(getDataLinkMaxTimeLoiteringRecovery().getValue());
		ros.setDataLinkModeOnLinkRecoverySuccess((VehicleMode)getDataLinkRecoverySuccessMode().getSelectedItem());
		ros.setDataLinkActionOnLinkRecoveryFailure((SafetyAction)getDataLinkActionOnRecoveryFailure().getSelectedItem());

		//LIMITS TAB
		ros.setMaxFlightTimeMinutes(getMaxFlightTime().getValue());
		ros.setMinAltitude(getRegionMinAltitude().getValue());
		ros.setMaxAltitude(getRegionMaxAltitude().getValue());
		ros.setMinMaxAltitudeType((AltitudeType)getRegionAltitudeType().getSelectedItem());

		//authorized region
		try {
			ros.getAuthorizedRegion().parseString(getAuthorizedRegion().getText());
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "There was an error parsing string '" + getAuthorizedRegion().getText() + "' as the coordinates for 'authorized region'");
			return false;
		}
		
		//prohibited regions
		ros.getProhibitedRegions().clear();
		String s = getProhibitedRegions().getText().trim().replaceAll("\\n\\n\\n", "\\n\\n");
		if(s.length()>0) {
			String[] sp = s.split("\\n\\n");
			for (String l : sp) {
				Region r = new Region();
				try {
					r.parseString(l);
					ros.getProhibitedRegions().add(r);
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "There was an error parsing string '" + l + "' as coordinates for 'prohibited regions'");
					return false;
				}
			}
		}
		
		//VALIDATION
		VerificationResult v = new VerificationResult();
		ros.validate(v, skylightVehicleControlService.resolveSkylightVehicleConfiguration(vehicle.getVehicleID().getVehicleID()));
		if(v.getErrors().size()>0 || v.getWarnings().size()>0) {
			if(JOptionPane.OK_OPTION!=JOptionPane.showConfirmDialog(null, v.toString() + "\nDo you want to continue?", "Rules of safety validation results", v.getOptionPaneResultLevel())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * This method initializes dataLinkRecoveryAltitude	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getDataLinkRecoveryAltitude() {
		if (dataLinkRecoveryAltitude == null) {
			dataLinkRecoveryAltitude = new JMeasureSpinner<Float>();
			dataLinkRecoveryAltitude.setup(MeasureType.ALTITUDE, 200F, 0, 9999999, 1, 0, 1);
		}
		return dataLinkRecoveryAltitude;
	}

	/**
	 * This method initializes dataLinkRecoveryAltitudeType	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getDataLinkRecoveryAltitudeType() {
		if (dataLinkRecoveryAltitudeType == null) {
			dataLinkRecoveryAltitudeType = new JComboBox();
			dataLinkRecoveryAltitudeType.setModel(new EnumComboBoxModel<AltitudeType>(AltitudeType.class));
		}
		return dataLinkRecoveryAltitudeType;
	}

	/**
	 * This method initializes manualRecoveryLoiterAltitude	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getManualRecoveryLoiterAltitude() {
		if (manualRecoveryLoiterAltitude == null) {
			manualRecoveryLoiterAltitude = new JMeasureSpinner<Float>();
			manualRecoveryLoiterAltitude.setup(MeasureType.ALTITUDE, 200F, 0, 9999999, 1, 0, 1);
		}
		return manualRecoveryLoiterAltitude;
	}

	/**
	 * This method initializes manualRecoveryAltitudeType	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getManualRecoveryAltitudeType() {
		if (manualRecoveryAltitudeType == null) {
			manualRecoveryAltitudeType = new JComboBox();
			manualRecoveryAltitudeType.setModel(new EnumComboBoxModel(AltitudeType.class));
		}
		return manualRecoveryAltitudeType;
	}

	/**
	 * This method initializes dataLinkMinStrength	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Integer> getDataLinkMinStrength() {
		if (dataLinkMinStrength == null) {
			dataLinkMinStrength = new JMeasureSpinner<Integer>();
			dataLinkMinStrength.setToolTipText("Use -1 if you don't want to trigger data link loss based on link strength");
		}
		return dataLinkMinStrength;
	}
	
}
