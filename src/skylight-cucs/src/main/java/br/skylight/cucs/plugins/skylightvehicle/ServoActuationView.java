package br.skylight.cucs.plugins.skylightvehicle;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import br.skylight.commons.Servo;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.skylight.ServosStateMessage;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.gamecontroller.GameControllerService;
import br.skylight.cucs.plugins.gamecontroller.GameControllerServiceListener;
import br.skylight.cucs.widgets.JPopupMenuMouseListener;
import br.skylight.cucs.widgets.VehicleMessageRefreshButton;
import br.skylight.cucs.widgets.VehicleView;

public class ServoActuationView extends VehicleView implements MessageListener, GameControllerServiceListener {

	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="10,52"

	@ServiceInjection
	public PluginManager pluginManager;

	@ServiceInjection
	public MessagingService messagingService;

	@ServiceInjection
	public GameControllerService gameControllerService;
	
	private JScrollPane scroll = null;

	private JPanel controls = null;

	private VehicleMessageRefreshButton refresh = null;

	private JButton joystick = null;
	
	public ServoActuationView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected String getBaseTitle() {
		return "Servo Actuation";
	}

	@Override
	protected void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMessageListener(MessageType.M2012, this);
		gameControllerService.addGameControllerServiceListener(this);
		
		//create servo controllers
		for (Servo s : Servo.values()) {
			ServoActuationWidget w = new ServoActuationWidget();
			pluginManager.manageObject(w);
			w.setServo(s);
			w.setLabel(s.getName());
			getControls().add(w);
		}
		updateGUI();
		
		if(pluginManager.isPluginsStarted()) {
			onGameControllerServiceStartup();
		}
	}
	
	@Override
	protected void updateGUI() {
		getRefresh().setVehicle(getCurrentVehicle());
		if(getCurrentVehicle()!=null) {
			for (Component c : getControls().getComponents()) {
				if(c instanceof ServoActuationWidget) {
					((ServoActuationWidget) c).setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
				}
			}
		}
	}

	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.anchor = GridBagConstraints.WEST;
			gridBagConstraints11.gridy = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.gridwidth = 2;
			gridBagConstraints1.gridx = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.weightx = 0.0;
			gridBagConstraints.anchor = GridBagConstraints.EAST;
			gridBagConstraints.gridy = 1;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(205, 106));
			contents.add(getRefresh(), gridBagConstraints);
			contents.add(getScroll(), gridBagConstraints1);
			contents.add(getJoystick(), gridBagConstraints11);
		}
		return contents;
	}

	@Override
	public void onMessageReceived(Message message) {
		//m2012
		if(message instanceof ServosStateMessage) {
			ServosStateMessage m = (ServosStateMessage)message;
			for (Component c : getControls().getComponents()) {
				if(c instanceof ServoActuationWidget) {
					ServoActuationWidget pc = (ServoActuationWidget)c;
					if(pc.getServo().equals(Servo.AILERON_LEFT)) {
						pc.setFeedback(m.getAileronLeftState());
					} else if(pc.getServo().equals(Servo.AILERON_RIGHT)) {
						pc.setFeedback(m.getAileronRightState());
					} else if(pc.getServo().equals(Servo.CAMERA_PAN)) {
						pc.setFeedback(m.getCameraPanState());
					} else if(pc.getServo().equals(Servo.CAMERA_TILT)) {
						pc.setFeedback(m.getCameraTiltState());
					} else if(pc.getServo().equals(Servo.ELEVATOR)) {
						pc.setFeedback(m.getElevatorState());
					} else if(pc.getServo().equals(Servo.RUDDER)) {
						pc.setFeedback(m.getRudderState());
					} else if(pc.getServo().equals(Servo.THROTTLE)) {
						pc.setFeedback(m.getThrottleState());
					} else if(pc.getServo().equals(Servo.GENERIC_SERVO)) {
						pc.setFeedback(m.getGenericServoState());
					}
				}
			}
			getRefresh().notifyFeedback();
		}
	}

	/**
	 * This method initializes scroll	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getScroll() {
		if (scroll == null) {
			scroll = new JScrollPane();
			scroll.setViewportView(getControls());
		}
		return scroll;
	}

	/**
	 * This method initializes controls	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getControls() {
		if (controls == null) {
			controls = new JPanel();
			controls.setLayout(new BoxLayout(getControls(), BoxLayout.Y_AXIS));
		}
		return controls;
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
			refresh.setup(subscriberService, messagingService, MessageType.M2012);
		}
		return refresh;
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
		getJoystick().addMouseListener(
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

}
