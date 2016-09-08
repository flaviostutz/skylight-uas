package br.skylight.cucs.plugins.payload;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.combobox.EnumComboBoxModel;

import br.skylight.commons.MeasureType;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.enums.DoorState;
import br.skylight.commons.dli.enums.SetEOIRPointingMode;
import br.skylight.commons.dli.enums.SetZoom;
import br.skylight.commons.dli.enums.StationDoor;
import br.skylight.commons.dli.enums.SystemOperatingMode;
import br.skylight.commons.dli.payload.EOIRLaserOperatingState;
import br.skylight.commons.dli.payload.EOIRLaserPayloadCommand;
import br.skylight.commons.dli.payload.PayloadBayCommand;
import br.skylight.commons.dli.payload.PayloadBayStatus;
import br.skylight.commons.dli.payload.PayloadConfigurationMessage;
import br.skylight.commons.dli.payload.PayloadSteeringCommand;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.gamecontroller.GameControllerService;
import br.skylight.cucs.plugins.gamecontroller.GameControllerServiceListener;
import br.skylight.cucs.plugins.subscriber.PreferencesListener;
import br.skylight.cucs.widgets.JMeasureSpinner;
import br.skylight.cucs.widgets.JPopupMenuMouseListener;
import br.skylight.cucs.widgets.PayloadView;
import br.skylight.cucs.widgets.VehicleMessageRefreshButton;

public class PayloadControlView extends PayloadView implements MessageListener, PreferencesListener, GameControllerServiceListener {

	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="11,14"
	private VehicleMessageRefreshButton refresh = null;
	private JLabel jLabel = null;
	private JLabel doorStateLabel = null;
	private JLabel jLabel2 = null;
	private JLabel jLabel3 = null;
	private JLabel jLabel4 = null;
	private JPanel eoIrPanel = null;
	private JLabel jLabel5 = null;
	private JLabel jLabel6 = null;
	private JComboBox operatingMode = null;
	private JMeasureSpinner<Float> azimuth = null;
	private JMeasureSpinner<Float> elevation = null;
	private JMeasureSpinner<Float> horizontalFOV = null;
	private JButton zoomPlus = null;
	private JButton zoomMinus = null;
	private JButton operatingModeButton = null;
	private JButton azElButton = null;
	private JButton fovButton = null;
	private JComboBox doorState = null;
	private JButton doorStateButton = null;
	private JLabel doorStateStr = null;
	private JLabel operatingModeStr = null;
	private JLabel azimuthStr = null;
	private JLabel elevationStr = null;
	private JLabel fovStr = null;
	private JLabel jLabel1 = null;
	private JButton joystick = null;

	@ServiceInjection
	public MessagingService messagingService;

	@ServiceInjection
	public GameControllerService gameControllerService;

	@ServiceInjection
	public PluginManager pluginManager;
	private JLabel jLabel31 = null;
	private JMeasureSpinner<Integer> sensorNumber = null;
	private JButton azElButton1 = null;
	private JButton sensorNumberButton = null;
	private JLabel sensorNumberStr = null;
	
	public PayloadControlView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMessageListener(MessageType.M300, this);
		subscriberService.addMessageListener(MessageType.M301, this);
		subscriberService.addMessageListener(MessageType.M302, this);
		subscriberService.addMessageListener(MessageType.M308, this);
		subscriberService.addPreferencesListener(this);
		gameControllerService.addGameControllerServiceListener(this);
		onPreferencesUpdated();
		if(pluginManager.isPluginsStarted()) {
			onGameControllerServiceStartup();
		}
	}

	@Override
	public void onMessageReceived(Message message) {
		if(isMessageFromCurrentPayload(message)) {
			//M300
			if(message instanceof PayloadConfigurationMessage) {
				PayloadConfigurationMessage m = (PayloadConfigurationMessage)message;
				getRefresh().notifyFeedback();
				updateGUI();

			//M301
			} else if(message instanceof EOIRLaserOperatingState) {
				//just keep message in payload for future use
				
			//M302
			} else if(message instanceof EOIRLaserOperatingState) {
				EOIRLaserOperatingState m = (EOIRLaserOperatingState)message;
				getRefresh().notifyFeedback();
				updateGUI();

			//M308
			} else if(message instanceof PayloadBayStatus) {
				PayloadBayStatus m = (PayloadBayStatus)message;
				getRefresh().notifyFeedback();
				updateGUI();
			}
		}
	}

	@Override
	protected void updateGUI() {
		if(getCurrentPayload()!=null) {
			getRefresh().setVehicle(getCurrentVehicle());
		} else {
			getRefresh().setVehicle(null);
		}
		
		//door state controls
		boolean doorStateVisible = getCurrentPayload()!=null && getCurrentPayload().getStationDoor().equals(StationDoor.YES);
		doorStateLabel.setVisible(doorStateVisible);
		getDoorState().setVisible(doorStateVisible);
		doorStateStr.setVisible(doorStateVisible);
		getDoorStateButton().setVisible(doorStateVisible);
		if(doorStateVisible) {
			if(getCurrentPayload().getEoIrPayload()!=null) {
				PayloadBayStatus bs = getCurrentPayload().getEoIrPayload().getPayloadBayStatus();
				doorStateStr.setText(bs.getPayloadBayDoorStatus().toString());
			}
		}
		
		boolean showEoIrPanel = getCurrentPayload()!=null && getCurrentPayload().getEoIrPayload()!=null;
		getEoIrPanel().setVisible(showEoIrPanel);
		if(showEoIrPanel) {
			if(getCurrentPayload().getEoIrPayload()!=null) {
				EOIRLaserOperatingState bs = getCurrentPayload().getEoIrPayload().getOperatingState();
				if(bs!=null) {
					sensorNumberStr.setText(bs.getAddressedSensor().getData() + "");
					operatingModeStr.setText(bs.getSystemOperatingModeState().toString());
					azimuthStr.setText(""+(int)MeasureType.ATTITUDE_ANGLES.convertToTargetUnit(bs.getActualCentrelineAzimuthAngle()));
					elevationStr.setText(""+(int)MeasureType.ATTITUDE_ANGLES.convertToTargetUnit(bs.getActualCentrelineElevationAngle()));
					fovStr.setText(""+(int)MeasureType.ATTITUDE_ANGLES.convertToTargetUnit(bs.getActualHorizontalFieldOfView()));
				}
			}
		}
	}
	
	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints181 = new GridBagConstraints();
			gridBagConstraints181.gridx = 1;
			gridBagConstraints181.anchor = GridBagConstraints.EAST;
			gridBagConstraints181.gridy = 5;
			GridBagConstraints gridBagConstraints171 = new GridBagConstraints();
			gridBagConstraints171.gridx = 0;
			gridBagConstraints171.weighty = 1.0;
			gridBagConstraints171.gridy = 3;
			jLabel1 = new JLabel();
			jLabel1.setText(" ");
			GridBagConstraints gridBagConstraints141 = new GridBagConstraints();
			gridBagConstraints141.fill = GridBagConstraints.BOTH;
			gridBagConstraints141.gridy = 0;
			gridBagConstraints141.weightx = 1.0;
			gridBagConstraints141.gridwidth = 3;
			gridBagConstraints141.insets = new Insets(5, 3, 5, 5);
			gridBagConstraints141.gridx = 1;
			GridBagConstraints gridBagConstraints131 = new GridBagConstraints();
			gridBagConstraints131.gridx = 1;
			gridBagConstraints131.gridwidth = 3;
			gridBagConstraints131.gridy = 0;
			GridBagConstraints gridBagConstraints121 = new GridBagConstraints();
			gridBagConstraints121.gridx = 0;
			gridBagConstraints121.gridwidth = 4;
			gridBagConstraints121.insets = new Insets(2, 0, 0, 0);
			gridBagConstraints121.gridy = 2;
			GridBagConstraints gridBagConstraints111 = new GridBagConstraints();
			gridBagConstraints111.gridx = 2;
			gridBagConstraints111.insets = new Insets(0, 0, 3, 3);
			gridBagConstraints111.gridy = 1;
			doorStateStr = new JLabel();
			doorStateStr.setText("-");
			GridBagConstraints gridBagConstraints101 = new GridBagConstraints();
			gridBagConstraints101.gridx = 0;
			gridBagConstraints101.gridwidth = 4;
			gridBagConstraints101.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints101.insets = new Insets(2, 0, 0, 0);
			gridBagConstraints101.gridy = 2;
			GridBagConstraints gridBagConstraints91 = new GridBagConstraints();
			gridBagConstraints91.gridx = 3;
			gridBagConstraints91.insets = new Insets(0, 0, 3, 5);
			gridBagConstraints91.gridy = 1;
			GridBagConstraints gridBagConstraints81 = new GridBagConstraints();
			gridBagConstraints81.fill = GridBagConstraints.BOTH;
			gridBagConstraints81.gridy = 1;
			gridBagConstraints81.weightx = 1.0;
			gridBagConstraints81.insets = new Insets(0, 3, 3, 3);
			gridBagConstraints81.gridx = 1;
			jLabel4 = new JLabel();
			jLabel4.setText("Elevation:");
			jLabel3 = new JLabel();
			jLabel3.setText("Azimuth:");
			jLabel2 = new JLabel();
			jLabel2.setText("Operating mode:");
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints3.gridy = 1;
			doorStateLabel = new JLabel();
			doorStateLabel.setText("Door state:");
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.BOTH;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.weightx = 0.0;
			gridBagConstraints2.ipadx = 0;
			gridBagConstraints2.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints2.gridwidth = 3;
			gridBagConstraints2.gridx = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			gridBagConstraints1.insets = new Insets(5, 5, 5, 0);
			gridBagConstraints1.gridy = 0;
			jLabel = new JLabel();
			jLabel.setText("Payload:");
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 3;
			gridBagConstraints8.gridy = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 3;
			gridBagConstraints.weightx = 0.0;
			gridBagConstraints.anchor = GridBagConstraints.SOUTHEAST;
			gridBagConstraints.insets = new Insets(3, 3, 3, 3);
			gridBagConstraints.gridy = 5;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 1;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(251, 213));
			contents.add(getRefresh(), gridBagConstraints);
			contents.add(jLabel, gridBagConstraints1);
			contents.add(doorStateLabel, gridBagConstraints3);
			contents.add(getDoorState(), gridBagConstraints81);
			contents.add(getDoorStateButton(), gridBagConstraints91);
			contents.add(doorStateStr, gridBagConstraints111);
			contents.add(getEoIrPanel(), gridBagConstraints121);
			contents.add(jLabel1, gridBagConstraints171);
			contents.add(getJoystick(), gridBagConstraints181);
			contents.add(getPayloadComboBox(), gridBagConstraints141);
		}
		return contents;
	}

	/**
	 * This method initializes refresh	
	 * 	
	 * @return br.skylight.cucs.widgets.FeedbackButton	
	 */
	private VehicleMessageRefreshButton getRefresh() {
		if (refresh == null) {
			refresh = new VehicleMessageRefreshButton();
			refresh.setToolTipText("Refresh data");
			refresh.setMargin(ViewHelper.getMinimalButtonMargin());
			refresh.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/refresh.gif")));
			refresh.setup(subscriberService, messagingService, MessageType.M300, MessageType.M301, MessageType.M302, MessageType.M308);
		}
		return refresh;
	}

	@Override
	protected String getBaseTitle() {
		return "Payload Control";
	}

	@Override
	public void onPreferencesUpdated() {
		updateGUI();
	}

	protected PayloadControlView getThis() {
		return this;
	}

	/**
	 * This method initializes eoIrPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getEoIrPanel() {
		if (eoIrPanel == null) {
			GridBagConstraints gridBagConstraints27 = new GridBagConstraints();
			gridBagConstraints27.gridx = 3;
			gridBagConstraints27.insets = new Insets(0, 0, 3, 3);
			gridBagConstraints27.gridy = 0;
			sensorNumberStr = new JLabel();
			sensorNumberStr.setText("-");
			GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
			gridBagConstraints26.gridx = 12;
			gridBagConstraints26.insets = new Insets(0, 0, 5, 3);
			gridBagConstraints26.gridy = 0;
			GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
			gridBagConstraints25.gridx = 1;
			gridBagConstraints25.gridwidth = 2;
			gridBagConstraints25.insets = new Insets(0, 3, 3, 3);
			gridBagConstraints25.gridy = 0;
			GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
			gridBagConstraints24.gridx = 0;
			gridBagConstraints24.anchor = GridBagConstraints.EAST;
			gridBagConstraints24.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints24.gridy = 0;
			jLabel31 = new JLabel();
			jLabel31.setText("Sensor number:");
			GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
			gridBagConstraints23.gridx = 3;
			gridBagConstraints23.insets = new Insets(0, 0, 3, 3);
			gridBagConstraints23.gridy = 4;
			fovStr = new JLabel();
			fovStr.setText("-");
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.gridx = 3;
			gridBagConstraints22.insets = new Insets(0, 0, 3, 3);
			gridBagConstraints22.gridy = 3;
			elevationStr = new JLabel();
			elevationStr.setText("-");
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 3;
			gridBagConstraints21.insets = new Insets(0, 0, 3, 3);
			gridBagConstraints21.gridy = 2;
			azimuthStr = new JLabel();
			azimuthStr.setText("-");
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			gridBagConstraints19.gridx = 3;
			gridBagConstraints19.insets = new Insets(0, 0, 3, 3);
			gridBagConstraints19.gridy = 1;
			operatingModeStr = new JLabel();
			operatingModeStr.setText("-");
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			gridBagConstraints20.gridx = 12;
			gridBagConstraints20.insets = new Insets(0, 0, 3, 3);
			gridBagConstraints20.gridy = 4;
			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
			gridBagConstraints18.gridx = 12;
			gridBagConstraints18.gridheight = 2;
			gridBagConstraints18.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints18.insets = new Insets(0, 0, 5, 3);
			gridBagConstraints18.gridy = 2;
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.gridx = 12;
			gridBagConstraints17.insets = new Insets(0, 0, 5, 3);
			gridBagConstraints17.fill = GridBagConstraints.NONE;
			gridBagConstraints17.gridy = 1;
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.gridx = 2;
			gridBagConstraints16.anchor = GridBagConstraints.WEST;
			gridBagConstraints16.insets = new Insets(0, 0, 3, 0);
			gridBagConstraints16.weightx = 1.0;
			gridBagConstraints16.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints16.gridy = 5;
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 1;
			gridBagConstraints15.insets = new Insets(0, 3, 3, 3);
			gridBagConstraints15.weightx = 1.0;
			gridBagConstraints15.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints15.gridy = 5;
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 1;
			gridBagConstraints14.anchor = GridBagConstraints.WEST;
			gridBagConstraints14.gridwidth = 2;
			gridBagConstraints14.insets = new Insets(0, 3, 3, 3);
			gridBagConstraints14.gridy = 4;
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 1;
			gridBagConstraints13.anchor = GridBagConstraints.WEST;
			gridBagConstraints13.gridwidth = 2;
			gridBagConstraints13.insets = new Insets(0, 3, 5, 3);
			gridBagConstraints13.gridy = 3;
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 1;
			gridBagConstraints12.anchor = GridBagConstraints.WEST;
			gridBagConstraints12.gridwidth = 2;
			gridBagConstraints12.insets = new Insets(0, 3, 3, 3);
			gridBagConstraints12.gridy = 2;
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.fill = GridBagConstraints.BOTH;
			gridBagConstraints10.gridy = 1;
			gridBagConstraints10.weightx = 1.0;
			gridBagConstraints10.anchor = GridBagConstraints.WEST;
			gridBagConstraints10.gridwidth = 2;
			gridBagConstraints10.insets = new Insets(0, 3, 5, 3);
			gridBagConstraints10.gridx = 1;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 0;
			gridBagConstraints9.anchor = GridBagConstraints.EAST;
			gridBagConstraints9.insets = new Insets(0, 5, 3, 0);
			gridBagConstraints9.gridy = 4;
			jLabel6 = new JLabel();
			jLabel6.setText("Horizontal FOV:");
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.anchor = GridBagConstraints.EAST;
			gridBagConstraints7.insets = new Insets(0, 5, 3, 0);
			gridBagConstraints7.gridy = 5;
			jLabel5 = new JLabel();
			jLabel5.setText("Zoom level:");
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.anchor = GridBagConstraints.EAST;
			gridBagConstraints6.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints6.gridy = 3;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.anchor = GridBagConstraints.EAST;
			gridBagConstraints5.insets = new Insets(0, 5, 3, 0);
			gridBagConstraints5.gridy = 2;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.anchor = GridBagConstraints.EAST;
			gridBagConstraints4.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints4.gridy = 1;
			eoIrPanel = new JPanel();
			eoIrPanel.setLayout(new GridBagLayout());
			eoIrPanel.add(jLabel2, gridBagConstraints4);
			eoIrPanel.add(jLabel3, gridBagConstraints5);
			eoIrPanel.add(jLabel4, gridBagConstraints6);
			eoIrPanel.add(jLabel5, gridBagConstraints7);
			eoIrPanel.add(jLabel6, gridBagConstraints9);
			eoIrPanel.add(getOperatingMode(), gridBagConstraints10);
			eoIrPanel.add(getAzimuth(), gridBagConstraints12);
			eoIrPanel.add(getElevation(), gridBagConstraints13);
			eoIrPanel.add(getHorizontalFOV(), gridBagConstraints14);
			eoIrPanel.add(getZoomPlus(), gridBagConstraints15);
			eoIrPanel.add(getZoomMinus(), gridBagConstraints16);
			eoIrPanel.add(getOperatingModeButton(), gridBagConstraints17);
			eoIrPanel.add(getAzElButton(), gridBagConstraints18);
			eoIrPanel.add(getFovButton(), gridBagConstraints20);
			eoIrPanel.add(operatingModeStr, gridBagConstraints19);
			eoIrPanel.add(azimuthStr, gridBagConstraints21);
			eoIrPanel.add(elevationStr, gridBagConstraints22);
			eoIrPanel.add(fovStr, gridBagConstraints23);
			eoIrPanel.add(jLabel31, gridBagConstraints24);
			eoIrPanel.add(getSensorNumber(), gridBagConstraints25);
			eoIrPanel.add(getSensorNumberButton(), gridBagConstraints26);
			eoIrPanel.add(sensorNumberStr, gridBagConstraints27);
		}
		return eoIrPanel;
	}

	/**
	 * This method initializes operatingMode	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getOperatingMode() {
		if (operatingMode == null) {
			operatingMode = new JComboBox();
			operatingMode.setModel(new EnumComboBoxModel(SystemOperatingMode.class));
		}
		return operatingMode;
	}

	/**
	 * This method initializes azimuth	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getAzimuth() {
		if (azimuth == null) {
			azimuth = new JMeasureSpinner<Float>();
			azimuth.setup(MeasureType.ATTITUDE_ANGLES, 0F, -Math.PI, Math.PI, Math.toRadians(1), 0, 1);
			azimuth.setupClickButtonOnDefaultAction(getAzElButton());
		}
		return azimuth;
	}

	/**
	 * This method initializes elevation	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getElevation() {
		if (elevation == null) {
			elevation = new JMeasureSpinner<Float>();
			elevation.setup(MeasureType.ATTITUDE_ANGLES, 0F, -Math.PI, Math.PI, Math.toRadians(1), 0, 1);
			elevation.setupClickButtonOnDefaultAction(getAzElButton());
		}
		return elevation;
	}

	/**
	 * This method initializes horizontalFOV	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getHorizontalFOV() {
		if (horizontalFOV == null) {
			horizontalFOV = new JMeasureSpinner<Float>();
			horizontalFOV.setup(MeasureType.ATTITUDE_ANGLES, (float)Math.toRadians(55), 0, 2*Math.PI, Math.toRadians(1), 0, 1);
			horizontalFOV.setupClickButtonOnDefaultAction(getFovButton());
		}
		return horizontalFOV;
	}

	/**
	 * This method initializes zoomPlus	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getZoomPlus() {
		if (zoomPlus == null) {
			zoomPlus = new JButton();
			zoomPlus.setText("+");
			zoomPlus.setMargin(ViewHelper.getDefaultButtonMargin());
			zoomPlus.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					PayloadSteeringCommand m1 = vehicleControlService.resolvePayloadSteeringCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID(), getCurrentPayload().getUniqueStationNumber());
					m1.setSetZoom(SetZoom.ZOOM_IN);
					vehicleControlService.sendPayloadSteeringCommand(m1);
					m1.setSetZoom(SetZoom.NO_CHANGE);
				}
			});
		}
		return zoomPlus;
	}

	/**
	 * This method initializes zoomMinus	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getZoomMinus() {
		if (zoomMinus == null) {
			zoomMinus = new JButton();
			zoomMinus.setText("-");
			zoomMinus.setMargin(ViewHelper.getDefaultButtonMargin());
			zoomMinus.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					PayloadSteeringCommand m1 = vehicleControlService.resolvePayloadSteeringCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID(), getCurrentPayload().getUniqueStationNumber());
					m1.setSetZoom(SetZoom.ZOOM_OUT);
					vehicleControlService.sendPayloadSteeringCommand(m1);
					m1.setSetZoom(SetZoom.NO_CHANGE);
				}
			});
		}
		return zoomMinus;
	}

	/**
	 * This method initializes operatingModeButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOperatingModeButton() {
		if (operatingModeButton == null) {
			operatingModeButton = new JButton();
			operatingModeButton.setText("Set");
			operatingModeButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					EOIRLaserPayloadCommand m = vehicleControlService.resolveEOIRLaserPayloadCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID(), getCurrentPayload().getUniqueStationNumber());
					m.setSystemOperatingMode((SystemOperatingMode)getOperatingMode().getSelectedItem());
					vehicleControlService.sendEOIRLaserPayloadCommand(m);
				}
			});
		}
		return operatingModeButton;
	}

	/**
	 * This method initializes azElButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAzElButton() {
		if (azElButton == null) {
			azElButton = new JButton();
			azElButton.setText("Set");
			azElButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					EOIRLaserPayloadCommand m = vehicleControlService.resolveEOIRLaserPayloadCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID(), getCurrentPayload().getUniqueStationNumber());
					m.setSetEOIRPointingMode(SetEOIRPointingMode.ANGLE_RELATIVE_TO_UAV);
					vehicleControlService.sendEOIRLaserPayloadCommand(m);

					PayloadSteeringCommand m1 = vehicleControlService.resolvePayloadSteeringCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID(), getCurrentPayload().getUniqueStationNumber());
					m1.setSetCentrelineAzimuthAngle(getAzimuth().getValue());
					m1.setSetCentrelineElevationAngle(getElevation().getValue());
					vehicleControlService.sendPayloadSteeringCommand(m1);
				}
			});
		}
		return azElButton;
	}

	/**
	 * This method initializes fovButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getFovButton() {
		if (fovButton == null) {
			fovButton = new JButton();
			fovButton.setText("Set");
			fovButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					PayloadSteeringCommand m1 = vehicleControlService.resolvePayloadSteeringCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID(), getCurrentPayload().getUniqueStationNumber());
					m1.setSetZoom(SetZoom.USE_FOV);
					m1.setSetHorizontalFieldOfView(getHorizontalFOV().getValue());
					vehicleControlService.sendPayloadSteeringCommand(m1);
					m1.setSetZoom(SetZoom.NO_CHANGE);
				}
			});
		}
		return fovButton;
	}

	/**
	 * This method initializes doorState	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getDoorState() {
		if (doorState == null) {
			doorState = new JComboBox();
			doorState.setModel(new EnumComboBoxModel(DoorState.class));
		}
		return doorState;
	}

	/**
	 * This method initializes doorStateButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getDoorStateButton() {
		if (doorStateButton == null) {
			doorStateButton = new JButton();
			doorStateButton.setText("Set");
			doorStateButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					PayloadBayCommand m = messagingService.resolveMessageForSending(PayloadBayCommand.class);
					m.getTargetStations().addStation(getCurrentPayload().getUniqueStationNumber());
					m.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
					m.setPayloadBayDoors((DoorState)getDoorState().getSelectedItem());
					messagingService.sendMessage(m);
				}
			});
		}
		return doorStateButton;
	}

	/**
	 * This method initializes joystick	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJoystick() {
		if (joystick == null) {
			joystick = new JButton();
			joystick.setToolTipText("Setup game controller");
			joystick.setMargin(ViewHelper.getMinimalButtonMargin());
			joystick.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/joystick.gif")));
		}
		return joystick;
	}

	@Override
	public void onGameControllerServiceStartup() {
		for (MouseListener ml : getJoystick().getMouseListeners()) {
			getJoystick().removeMouseListener(ml);
		}
		getJoystick().addMouseListener(new JPopupMenuMouseListener(
				gameControllerService.createSetupControlsMenu(
						GameControllerService.PAYLOAD_AZIMUTH_CONTROL_ID, 
						GameControllerService.PAYLOAD_ELEVATION_CONTROL_ID, 
						GameControllerService.PAYLOAD_FOV_CONTROL_ID), 
				true));
	}

	/**
	 * This method initializes sensorNumber	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Integer> getSensorNumber() {
		if (sensorNumber == null) {
			sensorNumber = new JMeasureSpinner<Integer>();
			sensorNumber.setupClickButtonOnDefaultAction(getSensorNumberButton());
			sensorNumber.setup(null, 0, 0, 254, 1, 0, 0);
		}
		return sensorNumber;
	}

	/**
	 * This method initializes azElButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAzElButton1() {
		if (azElButton1 == null) {
			azElButton1 = new JButton();
			azElButton1.setText("Set");
		}
		return azElButton1;
	}

	/**
	 * This method initializes sensorNumberButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSensorNumberButton() {
		if (sensorNumberButton == null) {
			sensorNumberButton = new JButton();
			sensorNumberButton.setText("Set");
			sensorNumberButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					EOIRLaserPayloadCommand m = vehicleControlService.resolveEOIRLaserPayloadCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID(), getCurrentPayload().getUniqueStationNumber());
					m.getAddressedSensor().setData(getSensorNumber().getValue());
					vehicleControlService.sendEOIRLaserPayloadCommand(m);
				}
			});
		}
		return sensorNumberButton;
	}
	
}
