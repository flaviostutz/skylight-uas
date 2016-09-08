package br.skylight.cucs.plugins.flightdirector;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import br.skylight.commons.Vehicle;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.FlightPathControlMode;
import br.skylight.commons.dli.enums.ModeState;
import br.skylight.commons.dli.enums.SpeedType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.vehicle.AirAndGroundRelativeStates;
import br.skylight.commons.dli.vehicle.InertialStates;
import br.skylight.commons.dli.vehicle.ModePreferenceCommand;
import br.skylight.commons.dli.vehicle.ModePreferenceReport;
import br.skylight.commons.dli.vehicle.VehicleOperatingModeCommand;
import br.skylight.commons.dli.vehicle.VehicleOperatingModeReport;
import br.skylight.commons.dli.vehicle.VehicleOperatingStates;
import br.skylight.commons.dli.vehicle.VehicleSteeringCommand;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.gamecontroller.GameControllerService;
import br.skylight.cucs.plugins.gamecontroller.GameControllerServiceListener;
import br.skylight.cucs.plugins.subscriber.PreferencesListener;
import br.skylight.cucs.plugins.subscriber.VehicleSteeringListener;
import br.skylight.cucs.widgets.CUCSViewHelper;
import br.skylight.cucs.widgets.FeedbackButton;
import br.skylight.cucs.widgets.JPopupMenuMouseListener;
import br.skylight.cucs.widgets.RoundButton;
import br.skylight.cucs.widgets.VehicleMessageRefreshButton;
import br.skylight.cucs.widgets.VehicleView;
import br.skylight.cucs.widgets.artificialhorizon.ArtificialHorizon;
import br.skylight.cucs.widgets.artificialhorizon.ArtificialHorizonListener;

public class FlightDirectorView extends VehicleView implements MessageListener, PreferencesListener, VehicleSteeringListener, GameControllerServiceListener {

	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="11,14"
	private VehicleMessageRefreshButton refresh = null;
	private ArtificialHorizon artificialHorizon = null;
	private RoundButton activate = null;

	private FeedbackButton manualAltitudeButton = null;
	private FeedbackButton manualSpeedButton = null;
	private FeedbackButton manualHeadingButton = null;
	private JButton joystick = null;

	@ServiceInjection
	public MessagingService messagingService;
	
	@ServiceInjection
	public GameControllerService gameControllerService;
	
	@ServiceInjection
	public PluginManager pluginManager;
	
	public FlightDirectorView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMessageListener(MessageType.M101, this);
		subscriberService.addMessageListener(MessageType.M102, this);
		subscriberService.addMessageListener(MessageType.M104, this);
		subscriberService.addMessageListener(MessageType.M109, this);
		subscriberService.addPreferencesListener(this);
		subscriberService.addVehicleSteeringListener(this);
		gameControllerService.addGameControllerServiceListener(this);
		onPreferencesUpdated();
		if(pluginManager.isPluginsStarted()) {
			onGameControllerServiceStartup();
		}
	}

	@Override
	public void onMessageReceived(Message message) {
		if(isMessageFromCurrentVehicle(message)) {
			//M101
			if(message instanceof InertialStates) {
				InertialStates m = (InertialStates)message;
				CUCSViewHelper.updateArtificialHorizonValues(m, getArtificialHorizon());
				getRefresh().notifyFeedback();

			//M102
			} else if(message instanceof AirAndGroundRelativeStates) {
				AirAndGroundRelativeStates m = (AirAndGroundRelativeStates)message;
				CUCSViewHelper.updateArtificialHorizonValues(m, getArtificialHorizon());
				getRefresh().notifyFeedback();

			//M104
			} else if(message instanceof VehicleOperatingStates) {
				VehicleOperatingStates m = (VehicleOperatingStates)message;
				getArtificialHorizon().setCommandedHeading(m.getCommandedHeading());
				getArtificialHorizon().updateUI();
				getRefresh().notifyFeedback();
				
			//M109
			} else if(message instanceof ModePreferenceReport) {
				ModePreferenceReport m = (ModePreferenceReport)message;
				getRefresh().notifyFeedback();
			}
			
			updateGUI();
		}
	}

	private boolean isFlightDirectorMode() {
		VehicleOperatingModeReport m = getCurrentVehicle().getLastReceivedMessage(MessageType.M106);
		return m!=null && m.getSelectFlightPathControlMode().equals(FlightPathControlMode.FLIGHT_DIRECTOR);
	}

	@Override
	protected void updateGUI() {
		getRefresh().setVehicle(getCurrentVehicle());
		getActivate().setEnabled(getCurrentVehicle()!=null);
		getArtificialHorizon().setEnabled(getCurrentVehicle()!=null);
		getManualAltitudeButton().setEnabled(getCurrentVehicle()!=null);
		getManualSpeedButton().setEnabled(getCurrentVehicle()!=null);
		getManualHeadingButton().setEnabled(getCurrentVehicle()!=null);
		
		if(getCurrentVehicle()!=null) {
			getActivate().setSelected(isFlightDirectorMode());
			getManualSpeedButton().setEnabled(!isFlightDirectorMode());
			
			//select preference mode buttons
			ModePreferenceReport m = getCurrentVehicle().getModePreferenceReport();
			
			boolean altitudeOverride = (m!=null && !m.getAltitudeModeState().equals(ModeState.CONFIGURATION));
			getManualAltitudeButton().setSelected(altitudeOverride);
			for (AltitudeType at : AltitudeType.values()) {
				getArtificialHorizon().getAltitude(at).setTargetValueVisible(altitudeOverride);
			}
			
			boolean headingOverride = (m!=null && !m.getCourseHeadingModeState().equals(ModeState.CONFIGURATION));
			getManualHeadingButton().setSelected(headingOverride);
			getArtificialHorizon().getHeading().setTargetValueVisible(m==null || !m.getCourseHeadingModeState().equals(ModeState.CONFIGURATION));
			
			boolean speedOverride = (isFlightDirectorMode() || (m!=null && !m.getSpeedModeState().equals(ModeState.CONFIGURATION)));
			getManualSpeedButton().setSelected(speedOverride);
			for (SpeedType st : SpeedType.values()) {
				getArtificialHorizon().getSpeed(st).setTargetValueVisible(speedOverride);
			}

			//update values
//			onSteeringEvent(getCurrentVehicle());
			
			getArtificialHorizon().repaint();
		}
	}
	
	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.gridx = 4;
			gridBagConstraints31.weightx = 1.0;
			gridBagConstraints31.gridy = 1;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 2;
			gridBagConstraints3.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints3.gridy = 1;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 1;
			gridBagConstraints21.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints21.gridy = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 3;
			gridBagConstraints1.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints1.gridy = 1;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints2.gridy = 1;
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.fill = GridBagConstraints.BOTH;
			gridBagConstraints12.weightx = 1.0;
			gridBagConstraints12.weighty = 1.0;
			gridBagConstraints12.gridwidth = 8;
			gridBagConstraints12.gridy = 0;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 3;
			gridBagConstraints8.gridy = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 7;
			gridBagConstraints.weightx = 0.0;
			gridBagConstraints.anchor = GridBagConstraints.SOUTHEAST;
			gridBagConstraints.insets = new Insets(3, 3, 3, 3);
			gridBagConstraints.gridy = 1;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 1;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(218, 186));
			contents.add(getRefresh(), gridBagConstraints);
			contents.add(getArtificialHorizon(), gridBagConstraints12);
			contents.add(getActivate(), gridBagConstraints2);
			contents.add(getManualAltitudeButton(), gridBagConstraints1);
			contents.add(getManualSpeedButton(), gridBagConstraints21);
			contents.add(getManualHeadingButton(), gridBagConstraints3);
			contents.add(getJoystick(), gridBagConstraints31);
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
			refresh.setup(subscriberService, messagingService, MessageType.M101, MessageType.M102, MessageType.M106, MessageType.M109);
		}
		return refresh;
	}

	@Override
	protected String getBaseTitle() {
		return "Flight Director/Manual Override";
	}

	/**
	 * This method initializes artificialHorizon	
	 * 	
	 * @return br.skylight.cucs.widgets.ArtificialHorizon	
	 */
	private ArtificialHorizon getArtificialHorizon() {
		if (artificialHorizon == null) {
			artificialHorizon = new ArtificialHorizon();
			
			artificialHorizon.addArtificialHorizonListener(new ArtificialHorizonListener() {
				@Override
				public void onTargetAltitudeSet(AltitudeType altitudeType, float value) {
					VehicleSteeringCommand m = vehicleControlService.resolveVehicleSteeringCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID());
					CUCSViewHelper.copySetpointsFromArtificialHorizon(getArtificialHorizon(), m);
					vehicleControlService.sendVehicleSteeringCommand(m);
				}
				@Override
				public void onTargetSpeedSet(SpeedType speedType, float value) {
					VehicleSteeringCommand m = vehicleControlService.resolveVehicleSteeringCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID());
					CUCSViewHelper.copySetpointsFromArtificialHorizon(getArtificialHorizon(), m);
					vehicleControlService.sendVehicleSteeringCommand(m);
				}
				@Override
				public void onTargetHeadingSet(float value) {
					VehicleSteeringCommand m = vehicleControlService.resolveVehicleSteeringCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID());
					CUCSViewHelper.copySetpointsFromArtificialHorizon(getArtificialHorizon(), m);
					vehicleControlService.sendVehicleSteeringCommand(m);
				}
			});
		}
		return artificialHorizon;
	}

	/**
	 * This method initializes activate	
	 * 	
	 * @return br.skylight.cucs.widgets.RoundButton	
	 */
	private RoundButton getActivate() {
		if (activate == null) {
			activate = new RoundButton();
			activate.setRoundness(10);
			activate.setText("Activate");
			activate.setColorUnselected(Color.LIGHT_GRAY);
			activate.setColorSelected(Color.ORANGE);
			activate.setToolTipText("Activate Flight Director mode");
			activate.setMargin(ViewHelper.getDefaultButtonMargin());
			activate.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//CHANGE TO FLIGHT DIRECTOR MODE
					if(!isFlightDirectorMode()) {
						//update setpoints as current inertial values
						InertialStates is = getCurrentVehicle().getLastReceivedMessage(MessageType.M101);
						if(is!=null) {
							CUCSViewHelper.updateArtificialHorizonTargets(is, getArtificialHorizon());
						}
						AirAndGroundRelativeStates rs = getCurrentVehicle().getLastReceivedMessage(MessageType.M102);
						if(rs!=null) {
							CUCSViewHelper.updateArtificialHorizonTargets(rs, getArtificialHorizon());
						}

						//send vehicle steering command
						sendVehicleSteeringAccordingToArtificialHorizon();

						//change to flight director mode
						VehicleOperatingModeCommand m = messagingService.resolveMessageForSending(VehicleOperatingModeCommand.class);
						m.setSelectFlightPathControlMode(FlightPathControlMode.FLIGHT_DIRECTOR);
						m.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
						messagingService.sendMessage(m);
						
					//CHANGE TO NOMODE
					} else {
						VehicleOperatingModeCommand m = messagingService.resolveMessageForSending(VehicleOperatingModeCommand.class);
						m.setSelectFlightPathControlMode(FlightPathControlMode.NO_MODE);
						m.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
						messagingService.sendMessage(m);
					}
				}
			});
		}
		return activate;
	}

	protected void sendVehicleSteeringAccordingToArtificialHorizon() {
		VehicleSteeringCommand mc = vehicleControlService.resolveVehicleSteeringCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID());
		CUCSViewHelper.copySetpointsFromArtificialHorizon(getArtificialHorizon(), mc);
		vehicleControlService.sendVehicleSteeringCommand(mc);
	}

	@Override
	public void onPreferencesUpdated() {
		CUCSViewHelper.updateDisplayUnits(getArtificialHorizon());
		updateGUI();
	}

	/**
	 * This method initializes manualAltitudeButton	
	 * 	
	 * @return br.skylight.cucs.widgets.FeedbackButton	
	 */
	private FeedbackButton getManualAltitudeButton() {
		if (manualAltitudeButton == null) {
			manualAltitudeButton = new FeedbackButton();
			manualAltitudeButton.setText("A");
			manualAltitudeButton.setToolTipText("Manual altitude mode");
			manualAltitudeButton.setMargin(ViewHelper.getDefaultButtonMargin());
			manualAltitudeButton.setToggleMode(true);
			manualAltitudeButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//change vehicle steering
					if(!manualAltitudeButton.isSelected()) {
						//set altitude setpoint as current inertial value
						for (AltitudeType at : AltitudeType.values()) {
							getArtificialHorizon().getAltitude(at).setTargetValue(getCurrentVehicle().getCurrentAltitude(at));
							getArtificialHorizon().getAltitude(at).setTargetValueVisible(true);
						}
					} else {
						for (AltitudeType at : AltitudeType.values()) {
							getArtificialHorizon().getAltitude(at).setTargetValueVisible(false);
						}
					}
					sendVehicleSteeringAccordingToArtificialHorizon();
					
					//change preference mode
					ModePreferenceCommand m = messagingService.resolveMessageForSending(ModePreferenceCommand.class);
					m.setAltitudeMode(!getManualAltitudeButton().isSelected()?ModeState.MANUAL_OVERRIDE:ModeState.CONFIGURATION);
					m.setSpeedMode(getManualSpeedButton().isSelected()?ModeState.MANUAL_OVERRIDE:ModeState.CONFIGURATION);
					m.setCourseHeadingMode(getManualHeadingButton().isSelected()?ModeState.MANUAL_OVERRIDE:ModeState.CONFIGURATION);
					m.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
					messagingService.sendMessage(m);

					updateGUI();
				}
			});
		}
		return manualAltitudeButton;
	}

	/**
	 * This method initializes manualSpeedButton	
	 * 	
	 * @return br.skylight.cucs.widgets.FeedbackButton	
	 */
	private FeedbackButton getManualSpeedButton() {
		if (manualSpeedButton == null) {
			manualSpeedButton = new FeedbackButton();
			manualSpeedButton.setMargin(ViewHelper.getDefaultButtonMargin());
			manualSpeedButton.setToolTipText("Manual speed mode");
			manualSpeedButton.setText("S");
			manualSpeedButton.setToggleMode(true);
			manualSpeedButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//change vehicle steering
					if(!manualSpeedButton.isSelected()) {
						//set speed setpoint as current inertial value
						for (SpeedType st : SpeedType.values()) {
							getArtificialHorizon().getSpeed(st).setTargetValue(getCurrentVehicle().getCurrentSpeed(st));
							getArtificialHorizon().getSpeed(st).setTargetValueVisible(true);
						}
					} else {
						for (SpeedType st : SpeedType.values()) {
							getArtificialHorizon().getSpeed(st).setTargetValueVisible(false);
						}
					}
					sendVehicleSteeringAccordingToArtificialHorizon();
					
					//change preference mode
					ModePreferenceCommand m = messagingService.resolveMessageForSending(ModePreferenceCommand.class);
					m.setAltitudeMode(getManualAltitudeButton().isSelected()?ModeState.MANUAL_OVERRIDE:ModeState.CONFIGURATION);
					m.setSpeedMode(!getManualSpeedButton().isSelected()?ModeState.MANUAL_OVERRIDE:ModeState.CONFIGURATION);
					m.setCourseHeadingMode(getManualHeadingButton().isSelected()?ModeState.MANUAL_OVERRIDE:ModeState.CONFIGURATION);
					m.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
					messagingService.sendMessage(m);

					updateGUI();
				}
			});
		}
		return manualSpeedButton;
	}

	/**
	 * This method initializes manualHeadingButton	
	 * 	
	 * @return br.skylight.cucs.widgets.FeedbackButton	
	 */
	private FeedbackButton getManualHeadingButton() {
		if (manualHeadingButton == null) {
			manualHeadingButton = new FeedbackButton();
			manualHeadingButton.setMargin(ViewHelper.getDefaultButtonMargin());
			manualHeadingButton.setToolTipText("Manual heading mode");
			manualHeadingButton.setText("H");
			manualHeadingButton.setToggleMode(true);
			manualHeadingButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//change vehicle steering
					if(!manualHeadingButton.isSelected()) {
						//set heading setpoint as current inertial value
						getArtificialHorizon().getHeading().setTargetValue(getCurrentVehicle().getCurrentCourseHeading());
						getArtificialHorizon().getHeading().setTargetValueVisible(true);
					} else {
						getArtificialHorizon().getHeading().setTargetValueVisible(false);
					}
					sendVehicleSteeringAccordingToArtificialHorizon();

					//change preference mode
					ModePreferenceCommand m = messagingService.resolveMessageForSending(ModePreferenceCommand.class);
					m.setAltitudeMode(getManualAltitudeButton().isSelected()?ModeState.MANUAL_OVERRIDE:ModeState.CONFIGURATION);
					m.setSpeedMode(getManualSpeedButton().isSelected()?ModeState.MANUAL_OVERRIDE:ModeState.CONFIGURATION);
					m.setCourseHeadingMode(!getManualHeadingButton().isSelected()?ModeState.MANUAL_OVERRIDE:ModeState.CONFIGURATION);
					m.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
					messagingService.sendMessage(m);

					updateGUI();
				}
			});
		}
		return manualHeadingButton;
	}

	@Override
	public void onSteeringEvent(Vehicle vehicle) {
		VehicleSteeringCommand vs = vehicle.getVehicleSteeringCommand();
		ArtificialHorizon ah = getArtificialHorizon();
		ah.getSpeed(vs.getSpeedType()).setTargetValue(vs.getCommandedSpeed());
//		ah.setSelectedSpeedType(vs.getSpeedType());
		ah.getAltitude(vs.getAltitudeType()).setTargetValue(vs.getCommandedAltitude());
//		ah.setSelectedAltitudeType(vs.getAltitudeType());
		ah.getHeading().setTargetValue(vs.getCommandedCourse());
		ah.repaint();
	}

	/**
	 * This method initializes joystick	
	 * 	
	 * @return br.skylight.cucs.widgets.FeedbackButton	
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
						GameControllerService.SPEED_CONTROL_ID, 
						GameControllerService.COURSE_HEADING_CONTROL_ID, 
						GameControllerService.ROLL_HEADING_CONTROL_ID, 
						GameControllerService.ALTITUDE_CONTROL_ID), 
				true));
	}
	
}
