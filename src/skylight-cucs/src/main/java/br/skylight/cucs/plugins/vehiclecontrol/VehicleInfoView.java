package br.skylight.cucs.plugins.vehiclecontrol;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.util.Arrays;
import java.util.Map.Entry;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import br.skylight.commons.CUCSControl;
import br.skylight.commons.Payload;
import br.skylight.commons.StringHelper;
import br.skylight.commons.Vehicle;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.vehicle.VehicleConfigurationMessage;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.widgets.VehicleMessageRefreshButton;
import br.skylight.cucs.widgets.VehicleView;

public class VehicleInfoView extends VehicleView implements MessageListener {

	private static final long serialVersionUID = 1L;
	private JPanel info;
	private JPanel basicInfo = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JLabel jLabel3 = null;
	private JLabel jLabel4 = null;
	private JLabel jLabel5 = null;
	private JLabel jLabel6 = null;
	private JLabel jLabel7 = null;
	private JLabel vehicleId = null;
	private JLabel vsmId = null;
	private JLabel vehicleType = null;
	private JLabel vehicleSubtype = null;
	private JLabel tailNumber = null;
	private JLabel atcCall = null;
	private JScrollPane jScrollPane = null;
	private JList payloads = null;
	private JScrollPane jScrollPane1 = null;
	private JList controlledBy = null;
	private JPanel contents = null;  //  @jve:decl-index=0:visual-constraint="41,60"
	private JTabbedPane jTabbedPane = null;
	private JPanel configurationInfo = null;
	private JLabel jLabel8 = null;
	private JLabel jLabel81 = null;
	private JLabel jLabel811 = null;
	private JLabel jLabel8111 = null;
	private JLabel jLabel81111 = null;
	private JLabel jLabel811111 = null;
	private JLabel jLabel8111111 = null;
	private JLabel jLabel81111111 = null;
	private JLabel jLabel811111111 = null;
	private JLabel jLabel8111111111 = null;
	private JLabel configurationId = null;
	private JLabel propulsionFuel = null;
	private JLabel propulsionBatery = null;
	private JLabel maximumIAS = null;
	private JLabel optimumCruiseIAS = null;
	private JLabel optimumEndurance = null;
	private JLabel maximumLoadFactor = null;
	private JLabel grossWeight = null;
	private JLabel centerOfGravity = null;
	private JLabel numberEngines = null;
	
	private JPanel configInfo;
	private VehicleMessageRefreshButton refresh = null;

	@ServiceInjection
	public MessagingService messagingService;
	
	/**
	 * @param owner
	 */
	public VehicleInfoView(ViewExtensionPoint extensionPoint) {
		super(extensionPoint);
	}
	
	@Override
	protected void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMessageListener(MessageType.M100, this);
	}

	@Override
	public void onMessageReceived(Message message) {
		//M100
		if(message instanceof VehicleConfigurationMessage) {
			VehicleConfigurationMessage m = (VehicleConfigurationMessage)message;
			Vehicle v = vehicleControlService.resolveVehicle(m.getVehicleID());
			v.setVehicleConfiguration(m);
			updateGUI();
			getRefresh().notifyFeedback();
		}
	}
	
	@Override
	protected void updateGUI() {
		getRefresh().setVehicle(getCurrentVehicle());
		if(getCurrentVehicle()!=null) {
			Vehicle vehicle = getCurrentVehicle();
			
			//BASIC INFO
			vehicleId.setText(StringHelper.formatId(vehicle.getVehicleID().getVehicleID()));
			vsmId.setText(StringHelper.formatId(vehicle.getVehicleID().getVsmID()));
			vehicleType.setText(vehicle.getVehicleID().getVehicleType().getName());
			vehicleSubtype.setText(vehicle.getVehicleID().getVehicleSubtype()+"");
			tailNumber.setText(vehicle.getVehicleID().getTailNumber());
			atcCall.setText(vehicle.getVehicleID().getATCCallSign());
			
			DefaultListModel m1 = new DefaultListModel();
			getPayloads().setModel(m1);
			for (Payload p : vehicle.getPayloads().values()) {
				m1.addElement("> "+p.getLabel());
			}
	
			DefaultListModel m2 = new DefaultListModel();
			getControlledBy().setModel(m2);
			for (Entry<Integer,CUCSControl> ce : vehicle.getCucsControls().entrySet()) {
				String cucsName = StringHelper.formatId(ce.getKey());
				CUCS c = vehicleControlService.getKnownCUCS().get(ce.getKey());
				if(c!=null) {
					cucsName = c.getLabel();
				}
				m2.addElement("> " + cucsName + " " + Arrays.deepToString(ce.getValue().getGrantedLOIs().getLOIs().toArray()));
			}
			
			//CONFIGURATION INFO
			VehicleConfigurationMessage cm = getCurrentVehicle().getLastReceivedMessage(MessageType.M100);
//			getJTabbedPane().setEnabled(cm!=null);
			if(cm!=null) {
				configurationId.setText(StringHelper.formatId((int)cm.getConfigurationID()));
				propulsionFuel.setText((cm.getPropulsionFuelCapacity()!=-1?cm.getPropulsionFuelCapacity():"-") + " kg");
				propulsionBatery.setText((cm.getPropulsionBatteryCapacity()!=-1?cm.getPropulsionBatteryCapacity():"-") + " J");
				maximumIAS.setText(cm.getMaximumIndicatedAirspeed() + " m/s");
				optimumCruiseIAS.setText(cm.getOptimumCruiseIndicatedAirspeed() + " m/s");
				optimumEndurance.setText(cm.getOptimumEnduranceIndicatedAirspeed() + " m/s");
				maximumLoadFactor.setText(cm.getMaximumLoadFactor() + " m/s2");
				grossWeight.setText(cm.getGrossWeight() + " kg");
				centerOfGravity.setText(cm.getXCG() + " m");
				numberEngines.setText(cm.getNumberOfEngines()+"");
			}
		}
	}

	@Override
	protected String getBaseTitle() {
		return "Vehicle Info";
	}

	/**
	 * This method initializes configInfo	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getConfigInfo() {
		if (configInfo == null) {
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.fill = GridBagConstraints.BOTH;
			gridBagConstraints14.gridy = 0;
			gridBagConstraints14.weightx = 1.0;
			gridBagConstraints14.weighty = 1.0;
			gridBagConstraints14.gridx = 0;
		}
		return configInfo;
	}

	/**
	 * This method initializes basicInfo	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getBasicInfo() {
		if (basicInfo == null) {
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.fill = GridBagConstraints.BOTH;
			gridBagConstraints31.gridwidth = 4;
			gridBagConstraints31.gridx = 0;
			gridBagConstraints31.gridy = 7;
			gridBagConstraints31.weightx = 1.0;
			gridBagConstraints31.weighty = 1.0;
			gridBagConstraints31.insets = new Insets(0, 15, 5, 15);
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.fill = GridBagConstraints.BOTH;
			gridBagConstraints21.gridwidth = 4;
			gridBagConstraints21.gridx = 0;
			gridBagConstraints21.gridy = 5;
			gridBagConstraints21.weightx = 1.0;
			gridBagConstraints21.weighty = 1.0;
			gridBagConstraints21.insets = new Insets(0, 15, 5, 15);
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints13.gridx = 1;
			gridBagConstraints13.gridy = 3;
			gridBagConstraints13.insets = new Insets(0, 3, 1, 0);
			atcCall = new JLabel();
			atcCall.setText("-");
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints12.gridx = 1;
			gridBagConstraints12.gridy = 2;
			gridBagConstraints12.insets = new Insets(0, 3, 1, 0);
			tailNumber = new JLabel();
			tailNumber.setText("-");
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints11.gridx = 3;
			gridBagConstraints11.gridy = 1;
			gridBagConstraints11.insets = new Insets(0, 3, 1, 10);
			vehicleSubtype = new JLabel();
			vehicleSubtype.setText("-");
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints10.gridx = 1;
			gridBagConstraints10.gridy = 1;
			gridBagConstraints10.insets = new Insets(0, 3, 1, 0);
			vehicleType = new JLabel();
			vehicleType.setText("-");
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints9.gridx = 3;
			gridBagConstraints9.gridy = 0;
			gridBagConstraints9.insets = new Insets(10, 3, 1, 10);
			vsmId = new JLabel();
			vsmId.setText("-");
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints8.gridx = 1;
			gridBagConstraints8.gridy = 0;
			gridBagConstraints8.weightx = 1.0;
			gridBagConstraints8.insets = new Insets(10, 3, 1, 0);
			vehicleId = new JLabel();
			vehicleId.setText("-");
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.anchor = GridBagConstraints.WEST;
			gridBagConstraints7.gridwidth = 2;
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.gridy = 4;
			gridBagConstraints7.insets = new Insets(2, 15, 4, 0);
			jLabel7 = new JLabel();
			jLabel7.setText("Payloads:");
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.anchor = GridBagConstraints.WEST;
			gridBagConstraints6.gridwidth = 2;
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.gridy = 6;
			gridBagConstraints6.insets = new Insets(2, 15, 4, 0);
			jLabel6 = new JLabel();
			jLabel6.setText("Controlled by:");
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.anchor = GridBagConstraints.EAST;
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.gridy = 3;
			gridBagConstraints5.insets = new Insets(0, 10, 4, 0);
			jLabel5 = new JLabel();
			jLabel5.setText("ATC call sign:");
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.anchor = GridBagConstraints.EAST;
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.gridy = 2;
			gridBagConstraints4.insets = new Insets(0, 10, 4, 0);
			jLabel4 = new JLabel();
			jLabel4.setText("Tail number:");
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.anchor = GridBagConstraints.EAST;
			gridBagConstraints3.gridx = 2;
			gridBagConstraints3.gridy = 1;
			gridBagConstraints3.insets = new Insets(0, 5, 4, 0);
			jLabel3 = new JLabel();
			jLabel3.setText("Subtype:");
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.anchor = GridBagConstraints.EAST;
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.insets = new Insets(0, 10, 4, 0);
			jLabel2 = new JLabel();
			jLabel2.setText("Vehicle type:");
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			gridBagConstraints1.gridx = 2;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.insets = new Insets(10, 5, 4, 0);
			jLabel1 = new JLabel();
			jLabel1.setText("VSM ID:");
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.anchor = GridBagConstraints.EAST;
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.insets = new Insets(10, 10, 4, 0);
			jLabel = new JLabel();
			jLabel.setText("Vehicle ID:");
			basicInfo = new JPanel();
			basicInfo.setLayout(new GridBagLayout());
			basicInfo.add(jLabel, gridBagConstraints);
			basicInfo.add(jLabel1, gridBagConstraints1);
			basicInfo.add(jLabel2, gridBagConstraints2);
			basicInfo.add(jLabel3, gridBagConstraints3);
			basicInfo.add(jLabel4, gridBagConstraints4);
			basicInfo.add(jLabel5, gridBagConstraints5);
			basicInfo.add(jLabel6, gridBagConstraints6);
			basicInfo.add(jLabel7, gridBagConstraints7);
			basicInfo.add(vehicleId, gridBagConstraints8);
			basicInfo.add(vsmId, gridBagConstraints9);
			basicInfo.add(vehicleType, gridBagConstraints10);
			basicInfo.add(vehicleSubtype, gridBagConstraints11);
			basicInfo.add(tailNumber, gridBagConstraints12);
			basicInfo.add(atcCall, gridBagConstraints13);
			basicInfo.add(getJScrollPane(), gridBagConstraints21);
			basicInfo.add(getJScrollPane1(), gridBagConstraints31);
		}
		return basicInfo;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setPreferredSize(new Dimension(259, 60));
			jScrollPane.setViewportView(getPayloads());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes payloads	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getPayloads() {
		if (payloads == null) {
			payloads = new JList();
			payloads.setBackground(SystemColor.control);
		}
		return payloads;
	}

	/**
	 * This method initializes jScrollPane1	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setPreferredSize(new Dimension(259, 60));
			jScrollPane1.setViewportView(getControlledBy());
		}
		return jScrollPane1;
	}

	/**
	 * This method initializes controlledBy	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getControlledBy() {
		if (controlledBy == null) {
			controlledBy = new JList();
			controlledBy.setBackground(SystemColor.control);
		}
		return controlledBy;
	}

	/**
	 * This method initializes contents	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	protected JPanel getContents() {
		if (contents == null) {
			GridBagConstraints gridBagConstraints38 = new GridBagConstraints();
			gridBagConstraints38.fill = GridBagConstraints.BOTH;
			gridBagConstraints38.gridy = 0;
			gridBagConstraints38.weightx = 1.0;
			gridBagConstraints38.weighty = 1.0;
			gridBagConstraints38.gridx = 0;
			GridBagConstraints gridBagConstraints37 = new GridBagConstraints();
			gridBagConstraints37.gridx = 0;
			gridBagConstraints37.insets = new Insets(3, 3, 3, 3);
			gridBagConstraints37.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints37.weightx = 1.0;
			gridBagConstraints37.anchor = GridBagConstraints.EAST;
			gridBagConstraints37.gridy = 1;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(317, 263));
			contents.add(getRefresh(), gridBagConstraints37);
			contents.add(getJTabbedPane(), gridBagConstraints38);
		}
		return contents;
	}

	/**
	 * This method initializes jTabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
			jTabbedPane.addTab("Basic Info", null, getBasicInfo(), null);
			jTabbedPane.addTab("Configuration Info", null, getConfigurationInfo(), null);
		}
		return jTabbedPane;
	}

	/**
	 * This method initializes configurationInfo	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getConfigurationInfo() {
		if (configurationInfo == null) {
			GridBagConstraints gridBagConstraints36 = new GridBagConstraints();
			gridBagConstraints36.gridx = 1;
			gridBagConstraints36.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints36.anchor = GridBagConstraints.WEST;
			gridBagConstraints36.weightx = 0.0;
			gridBagConstraints36.insets = new Insets(6, 5, 0, 5);
			gridBagConstraints36.gridy = 5;
			numberEngines = new JLabel();
			numberEngines.setText("-");
			GridBagConstraints gridBagConstraints35 = new GridBagConstraints();
			gridBagConstraints35.gridx = 1;
			gridBagConstraints35.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints35.anchor = GridBagConstraints.WEST;
			gridBagConstraints35.weightx = 0.0;
			gridBagConstraints35.insets = new Insets(6, 5, 0, 5);
			gridBagConstraints35.gridy = 4;
			centerOfGravity = new JLabel();
			centerOfGravity.setText("-");
			GridBagConstraints gridBagConstraints34 = new GridBagConstraints();
			gridBagConstraints34.gridx = 1;
			gridBagConstraints34.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints34.anchor = GridBagConstraints.WEST;
			gridBagConstraints34.weightx = 0.0;
			gridBagConstraints34.insets = new Insets(6, 5, 0, 5);
			gridBagConstraints34.gridy = 3;
			grossWeight = new JLabel();
			grossWeight.setText("-");
			GridBagConstraints gridBagConstraints33 = new GridBagConstraints();
			gridBagConstraints33.gridx = 1;
			gridBagConstraints33.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints33.anchor = GridBagConstraints.WEST;
			gridBagConstraints33.weightx = 0.0;
			gridBagConstraints33.insets = new Insets(6, 5, 0, 5);
			gridBagConstraints33.gridy = 2;
			maximumLoadFactor = new JLabel();
			maximumLoadFactor.setText("-");
			GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
			gridBagConstraints32.gridx = 3;
			gridBagConstraints32.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints32.anchor = GridBagConstraints.WEST;
			gridBagConstraints32.weightx = 0.0;
			gridBagConstraints32.insets = new Insets(6, 5, 0, 5);
			gridBagConstraints32.gridy = 5;
			optimumEndurance = new JLabel();
			optimumEndurance.setText("-");
			GridBagConstraints gridBagConstraints30 = new GridBagConstraints();
			gridBagConstraints30.gridx = 3;
			gridBagConstraints30.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints30.anchor = GridBagConstraints.WEST;
			gridBagConstraints30.weightx = 0.0;
			gridBagConstraints30.insets = new Insets(6, 5, 0, 5);
			gridBagConstraints30.gridy = 4;
			optimumCruiseIAS = new JLabel();
			optimumCruiseIAS.setText("-");
			GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
			gridBagConstraints29.gridx = 3;
			gridBagConstraints29.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints29.anchor = GridBagConstraints.WEST;
			gridBagConstraints29.weightx = 0.0;
			gridBagConstraints29.insets = new Insets(6, 5, 0, 5);
			gridBagConstraints29.gridy = 3;
			maximumIAS = new JLabel();
			maximumIAS.setText("-");
			GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
			gridBagConstraints28.gridx = 3;
			gridBagConstraints28.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints28.anchor = GridBagConstraints.WEST;
			gridBagConstraints28.weightx = 0.0;
			gridBagConstraints28.insets = new Insets(6, 5, 0, 5);
			gridBagConstraints28.gridy = 2;
			propulsionBatery = new JLabel();
			propulsionBatery.setText("-");
			GridBagConstraints gridBagConstraints27 = new GridBagConstraints();
			gridBagConstraints27.gridx = 3;
			gridBagConstraints27.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints27.anchor = GridBagConstraints.WEST;
			gridBagConstraints27.weightx = 0.0;
			gridBagConstraints27.insets = new Insets(6, 5, 0, 5);
			gridBagConstraints27.gridy = 0;
			propulsionFuel = new JLabel();
			propulsionFuel.setText("-");
			GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
			gridBagConstraints26.gridx = 1;
			gridBagConstraints26.insets = new Insets(6, 5, 0, 5);
			gridBagConstraints26.weightx = 5.0;
			gridBagConstraints26.anchor = GridBagConstraints.WEST;
			gridBagConstraints26.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints26.gridy = 0;
			configurationId = new JLabel();
			configurationId.setText("-");
			GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
			gridBagConstraints25.gridx = 0;
			gridBagConstraints25.weightx = 0.0;
			gridBagConstraints25.anchor = GridBagConstraints.EAST;
			gridBagConstraints25.insets = new Insets(6, 5, 0, 0);
			gridBagConstraints25.gridy = 5;
			jLabel8111111111 = new JLabel();
			jLabel8111111111.setText("Number of engines:");
			GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
			gridBagConstraints24.gridx = 0;
			gridBagConstraints24.weightx = 0.0;
			gridBagConstraints24.anchor = GridBagConstraints.EAST;
			gridBagConstraints24.insets = new Insets(6, 5, 0, 0);
			gridBagConstraints24.gridy = 4;
			jLabel811111111 = new JLabel();
			jLabel811111111.setText("Center of gravity from nose:");
			GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
			gridBagConstraints23.gridx = 0;
			gridBagConstraints23.weightx = 0.0;
			gridBagConstraints23.anchor = GridBagConstraints.EAST;
			gridBagConstraints23.insets = new Insets(6, 5, 0, 0);
			gridBagConstraints23.gridy = 3;
			jLabel81111111 = new JLabel();
			jLabel81111111.setText("Gross weight:");
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.gridx = 0;
			gridBagConstraints22.weightx = 0.0;
			gridBagConstraints22.anchor = GridBagConstraints.EAST;
			gridBagConstraints22.insets = new Insets(6, 5, 0, 0);
			gridBagConstraints22.gridy = 2;
			jLabel8111111 = new JLabel();
			jLabel8111111.setText("Maximum load factor:");
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			gridBagConstraints20.gridx = 2;
			gridBagConstraints20.weightx = 0.0;
			gridBagConstraints20.anchor = GridBagConstraints.EAST;
			gridBagConstraints20.insets = new Insets(6, 5, 0, 0);
			gridBagConstraints20.gridy = 5;
			jLabel811111 = new JLabel();
			jLabel811111.setText("Optimum endurance IAS:");
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			gridBagConstraints19.gridx = 2;
			gridBagConstraints19.weightx = 0.0;
			gridBagConstraints19.anchor = GridBagConstraints.EAST;
			gridBagConstraints19.insets = new Insets(6, 5, 0, 0);
			gridBagConstraints19.gridy = 4;
			jLabel81111 = new JLabel();
			jLabel81111.setText("Optimum cruise IAS::");
			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
			gridBagConstraints18.gridx = 2;
			gridBagConstraints18.weightx = 0.0;
			gridBagConstraints18.anchor = GridBagConstraints.EAST;
			gridBagConstraints18.insets = new Insets(6, 5, 0, 0);
			gridBagConstraints18.gridy = 3;
			jLabel8111 = new JLabel();
			jLabel8111.setText("Maximum IAS:");
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.gridx = 2;
			gridBagConstraints17.weightx = 0.0;
			gridBagConstraints17.anchor = GridBagConstraints.EAST;
			gridBagConstraints17.insets = new Insets(6, 5, 0, 0);
			gridBagConstraints17.gridy = 2;
			jLabel811 = new JLabel();
			jLabel811.setText("Propulsion battery capacity:");
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.gridx = 2;
			gridBagConstraints16.weightx = 0.0;
			gridBagConstraints16.anchor = GridBagConstraints.EAST;
			gridBagConstraints16.insets = new Insets(6, 5, 0, 0);
			gridBagConstraints16.gridy = 0;
			jLabel81 = new JLabel();
			jLabel81.setText("Propulsion fuel capacity:");
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.weightx = 1.0;
			gridBagConstraints15.anchor = GridBagConstraints.EAST;
			gridBagConstraints15.insets = new Insets(6, 5, 0, 0);
			gridBagConstraints15.fill = GridBagConstraints.NONE;
			gridBagConstraints15.gridy = 0;
			jLabel8 = new JLabel();
			jLabel8.setText("Configuration ID:");
			configurationInfo = new JPanel();
			configurationInfo.setLayout(new GridBagLayout());
			configurationInfo.add(jLabel8, gridBagConstraints15);
			configurationInfo.add(jLabel81, gridBagConstraints16);
			configurationInfo.add(jLabel811, gridBagConstraints17);
			configurationInfo.add(jLabel8111, gridBagConstraints18);
			configurationInfo.add(jLabel81111, gridBagConstraints19);
			configurationInfo.add(jLabel811111, gridBagConstraints20);
			configurationInfo.add(jLabel8111111, gridBagConstraints22);
			configurationInfo.add(jLabel81111111, gridBagConstraints23);
			configurationInfo.add(jLabel811111111, gridBagConstraints24);
			configurationInfo.add(jLabel8111111111, gridBagConstraints25);
			configurationInfo.add(configurationId, gridBagConstraints26);
			configurationInfo.add(propulsionFuel, gridBagConstraints27);
			configurationInfo.add(propulsionBatery, gridBagConstraints28);
			configurationInfo.add(maximumIAS, gridBagConstraints29);
			configurationInfo.add(optimumCruiseIAS, gridBagConstraints30);
			configurationInfo.add(optimumEndurance, gridBagConstraints32);
			configurationInfo.add(maximumLoadFactor, gridBagConstraints33);
			configurationInfo.add(grossWeight, gridBagConstraints34);
			configurationInfo.add(centerOfGravity, gridBagConstraints35);
			configurationInfo.add(numberEngines, gridBagConstraints36);
		}
		return configurationInfo;
	}

	/**
	 * This method initializes refresh	
	 * 	
	 * @return br.skylight.cucs.widgets.VehicleMessageRefreshButton	
	 */
	private VehicleMessageRefreshButton getRefresh() {
		if (refresh == null) {
			refresh = new VehicleMessageRefreshButton();
			refresh.setToolTipText("Refresh data");
			refresh.setMargin(ViewHelper.getMinimalButtonMargin());
			refresh.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/refresh.gif")));
			refresh.setup(subscriberService, messagingService, MessageType.M100);
		}
		return refresh;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
