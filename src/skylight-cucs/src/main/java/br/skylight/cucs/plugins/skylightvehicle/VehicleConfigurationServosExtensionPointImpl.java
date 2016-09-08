package br.skylight.cucs.plugins.skylightvehicle;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import br.skylight.commons.Servo;
import br.skylight.commons.ServoConfiguration;
import br.skylight.commons.Vehicle;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.enums.VehicleType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.skylight.ServosStateMessage;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.plugins.gamecontroller.GameControllerService;
import br.skylight.cucs.plugins.skylightvehicle.vehiclecontrol.SkylightVehicle;
import br.skylight.cucs.plugins.skylightvehicle.vehiclecontrol.SkylightVehicleControlService;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.plugins.vehicleconfiguration.VehicleConfigurationSectionExtensionPoint;
import br.skylight.cucs.widgets.JPopupMenuMouseListener;
import br.skylight.cucs.widgets.VehicleMessageRefreshButton;
import br.skylight.cucs.widgets.tables.ObjectToColumnAdapter;
import br.skylight.cucs.widgets.tables.TypedTableModel;

@ExtensionPointImplementation(extensionPointDefinition=VehicleConfigurationSectionExtensionPoint.class)
public class VehicleConfigurationServosExtensionPointImpl extends Worker implements VehicleConfigurationSectionExtensionPoint, MessageListener {

	private JPanel sectionComponent;  //  @jve:decl-index=0:visual-constraint="10,52"
	private Vehicle currentVehicle;  //  @jve:decl-index=0:
	private SkylightVehicle currentSkylightVehicle;  //  @jve:decl-index=0:

	private JScrollPane scrollPanel = null;
	private JTable servoTable = null;
	private VehicleMessageRefreshButton refresh = null;
	private JButton uploadButton = null;

	@ServiceInjection
	public MessagingService messagingService;
	
	@ServiceInjection
	public SubscriberService subscriberService;

	@ServiceInjection
	public SkylightVehicleControlService skylightVehicleControlService;

	@ServiceInjection
	public GameControllerService gameControllerService;
	
	private JButton joystick = null;
	
	@Override
	public JPanel getSectionComponent() {
		if(sectionComponent==null) {
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.anchor = GridBagConstraints.EAST;
			gridBagConstraints5.insets = new Insets(0, 0, 0, 5);
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.gridy = 1;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.gridwidth = 3;
			gridBagConstraints1.gridx = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 2;
			gridBagConstraints.anchor = GridBagConstraints.EAST;
			gridBagConstraints.gridy = 1;
			sectionComponent = new JPanel();
			sectionComponent.setLayout(new GridBagLayout());
			sectionComponent.setSize(new Dimension(200, 125));
			sectionComponent.add(getRefresh(), gridBagConstraints);
			sectionComponent.add(getScrollPanel(), gridBagConstraints1);
			sectionComponent.add(getUploadButton(), gridBagConstraints11);
			sectionComponent.add(getJoystick(), gridBagConstraints5);
		}
		return sectionComponent;
	}

	@Override
	public void onActivate() throws Exception {
		subscriberService.addMessageListener(MessageType.M2012, this);
	}
	
	@Override
	public void onDeactivate() throws Exception {
	}


	@Override
	public void onMessageReceived(Message message) {
		//m2012
		if(message instanceof ServosStateMessage) {
			ServosStateMessage m = (ServosStateMessage)message;
			TypedTableModel<ServoConfiguration> model = (TypedTableModel<ServoConfiguration>)getServoTable().getModel();
			for (ServoConfiguration sc : model.getUserObjects()) {
				if(sc.getServo().equals(Servo.AILERON_LEFT)) {
					sc.setLastValue(m.getAileronLeftState());
				} else if(sc.getServo().equals(Servo.AILERON_RIGHT)) {
					sc.setLastValue(m.getAileronRightState());
				} else if(sc.getServo().equals(Servo.CAMERA_PAN)) {
					sc.setLastValue(m.getCameraPanState());
				} else if(sc.getServo().equals(Servo.CAMERA_TILT)) {
					sc.setLastValue(m.getCameraTiltState());
				} else if(sc.getServo().equals(Servo.ELEVATOR)) {
					sc.setLastValue(m.getElevatorState());
				} else if(sc.getServo().equals(Servo.RUDDER)) {
					sc.setLastValue(m.getRudderState());
				} else if(sc.getServo().equals(Servo.THROTTLE)) {
					sc.setLastValue(m.getThrottleState());
				} else if(sc.getServo().equals(Servo.GENERIC_SERVO)) {
					sc.setLastValue(m.getGenericServoState());
				}
			}
			getServoTable().updateUI();
			getRefresh().notifyFeedback();
		}
	}
	
	@Override
	public String getSectionName() {
		return "Servos";
	}

	@Override
	public boolean updateVehicle(Vehicle vehicle) {
		currentVehicle = vehicle;
		currentSkylightVehicle = skylightVehicleControlService.resolveSkylightVehicle(vehicle.getVehicleID().getVehicleID());
		
		TypedTableModel<ServoConfiguration> model = (TypedTableModel<ServoConfiguration>)getServoTable().getModel();
		model.getUserObjects().clear();
		SkylightVehicleConfigurationMessage vc = currentSkylightVehicle.getSkylightVehicleConfiguration();
		for (ServoConfiguration sc : vc.getServoConfigurations().values()) {
			model.getUserObjects().add(sc);
		}
		getServoTable().updateUI();
		getRefresh().setVehicle(vehicle);
		return vehicle.getVehicleID()!=null && vehicle.getVehicleID().getVehicleType().equals(VehicleType.TYPE_60);
	}

	/**
	 * This method initializes scrollPanel	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getScrollPanel() {
		if (scrollPanel == null) {
			scrollPanel = new JScrollPane();
			scrollPanel.setViewportView(getServoTable());
		}
		return scrollPanel;
	}

	/**
	 * This method initializes servoTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getServoTable() {
		if (servoTable == null) {
			servoTable = new JTable();
			TypedTableModel<ServoConfiguration> model = new TypedTableModel<ServoConfiguration>(new ObjectToColumnAdapter<ServoConfiguration>() {
				@Override
				public Object getValueAt(ServoConfiguration servoConfiguration, int columnIndex) {
					if(columnIndex==0) {
						return servoConfiguration.getServo().getName();
					} else if(columnIndex==1) {
						return servoConfiguration.getMinUs();
					} else if(columnIndex==2) {
						return servoConfiguration.getMaxUs();
					} else if(columnIndex==3) {
						return servoConfiguration.getTrimUs();
					} else if(columnIndex==4) {
						return servoConfiguration.isInverse();
					} else if(columnIndex==5) {
						return Math.toDegrees(servoConfiguration.getRangeAngle());
					} else if(columnIndex==6) {
						return servoConfiguration.getLastValue();
					} else {
						return null;
					}
				}
				@Override
				public void setValueAt(ServoConfiguration servoConfiguration, Object value, int columnIndex) {
					if(columnIndex==1) {
						servoConfiguration.setMinUs((Integer)value);
					} else if(columnIndex==2) {
						servoConfiguration.setMaxUs((Integer)value);
					} else if(columnIndex==3) {
						servoConfiguration.setTrimUs((Integer)value);
					} else if(columnIndex==4) {
						servoConfiguration.setInverse((Boolean)value);
					} else if(columnIndex==5) {
						servoConfiguration.setRangeAngle((float)Math.toRadians((Double)value));
					}
				}
			}, "Servo name", "Min position µs", "Max position µs", "Trim µs", "Inverse", "Range angle (°)", "Current value");
			model.setColumnEditables(false, true, true, true, true, true, false);
			servoTable.setModel(model);
		}
		return servoTable;
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
			refresh.setup(subscriberService, messagingService, MessageType.M2012, MessageType.M2009);
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
			uploadButton.setToolTipText("Activate hold");
			uploadButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/upload.gif")));
			uploadButton.setMargin(ViewHelper.getMinimalButtonMargin());
			uploadButton.setPreferredSize(new Dimension(20, 20));
			uploadButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					TypedTableModel<ServoConfiguration> sc = (TypedTableModel<ServoConfiguration>)getServoTable().getModel();
					for (ServoConfiguration s : sc.getUserObjects()) {
						ServoConfiguration sa = messagingService.resolveMessageForSending(ServoConfiguration.class);
						sa.setVehicleID(currentVehicle.getVehicleID().getVehicleID());
						sa.copyFrom(s);
						messagingService.sendMessage(sa);
					}
				}
			});
		}
		return uploadButton;
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
			joystick.addMouseListener(
				new JPopupMenuMouseListener(
						gameControllerService.createSetupControlsMenu(
							ServoControllerBindingsDefinitionExtensionPointImpl.AILERON_L_CONTROL_ID,
							ServoControllerBindingsDefinitionExtensionPointImpl.AILERON_R_CONTROL_ID,
							ServoControllerBindingsDefinitionExtensionPointImpl.RUDDER_CONTROL_ID,
							ServoControllerBindingsDefinitionExtensionPointImpl.ELEVATOR_CONTROL_ID,
							ServoControllerBindingsDefinitionExtensionPointImpl.THROTTLE_CONTROL_ID,
							ServoControllerBindingsDefinitionExtensionPointImpl.CAMERA_PAN_CONTROL_ID,
							ServoControllerBindingsDefinitionExtensionPointImpl.CAMERA_TILT_CONTROL_ID), 
						true));
		}
		return joystick;
	}

}
