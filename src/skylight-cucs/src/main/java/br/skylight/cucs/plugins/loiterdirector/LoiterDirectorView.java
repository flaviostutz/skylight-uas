package br.skylight.cucs.plugins.loiterdirector;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import br.skylight.commons.EventType;
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
import br.skylight.commons.dli.vehicle.LoiterConfiguration;
import br.skylight.commons.dli.vehicle.VehicleOperatingModeCommand;
import br.skylight.commons.dli.vehicle.VehicleOperatingModeReport;
import br.skylight.commons.dli.vehicle.VehicleSteeringCommand;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.subscriber.LoiterListener;
import br.skylight.cucs.plugins.subscriber.PreferencesListener;
import br.skylight.cucs.widgets.CUCSViewHelper;
import br.skylight.cucs.widgets.RoundButton;
import br.skylight.cucs.widgets.VehicleMessageRefreshButton;
import br.skylight.cucs.widgets.VehicleView;
import br.skylight.cucs.widgets.artificialhorizon.ArtificialHorizon;
import br.skylight.cucs.widgets.artificialhorizon.ArtificialHorizon.HorizonControl;
import br.skylight.cucs.widgets.artificialhorizon.ArtificialHorizonListener;
import br.skylight.cucs.widgets.artificialhorizon.ClickableElement;

public class LoiterDirectorView extends VehicleView implements MessageListener, PreferencesListener, LoiterListener {

	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="11,14"
	private VehicleMessageRefreshButton refresh = null;
	private ArtificialHorizon artificialHorizon = null;
	private RoundButton activate = null;

	private JSpinner radius = null;
	private JLabel jLabel = null;

	@ServiceInjection
	public MessagingService messagingService;
	private JButton aroundPosition = null;
	
	public LoiterDirectorView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMessageListener(MessageType.M101, this);
		subscriberService.addMessageListener(MessageType.M102, this);
		subscriberService.addMessageListener(MessageType.M106, this);//just to force updateGUI()
		subscriberService.addPreferencesListener(this);
		subscriberService.addLoiterListener(this);
		onPreferencesUpdated();
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
			}
			
			updateGUI();
		}
	}

	@Override
	protected void updateGUI() {
		getActivate().setEnabled(getCurrentVehicle()!=null);
		getRadius().setEnabled(getCurrentVehicle()!=null);
		getArtificialHorizon().setEnabled(getCurrentVehicle()!=null);
		getRefresh().setVehicle(getCurrentVehicle());
		if(getCurrentVehicle()!=null) {
			//operating mode report
			VehicleOperatingModeReport m = vehicleControlService.getLastReceivedMessage(getCurrentVehicle().getVehicleID().getVehicleID(), MessageType.M106);
			boolean flightDirectorMode = false;
			if(m!=null) {
				if(m.getSelectFlightPathControlMode().equals(FlightPathControlMode.LOITER)) {
					flightDirectorMode = true;
				}
			}
			getActivate().setSelected(flightDirectorMode);
			
			//show greyed setpoint control when it is being overriden in manual control
			Vehicle vehicle = getCurrentVehicle();
			Color colorWhenModeIsConfiguration = Color.ORANGE;
			Color colorWhenModeIsNotConfiguration = Color.GRAY;

			//altitude
			ClickableElement altitudeControl = getArtificialHorizon().getElement(HorizonControl.ALTITUDE);
			if(vehicle.isCurrentMode(FlightPathControlMode.LOITER) && vehicle.getModePreferenceReport().getAltitudeModeState().equals(ModeState.CONFIGURATION)) {
				altitudeControl.setColors(colorWhenModeIsConfiguration, colorWhenModeIsConfiguration.brighter(), colorWhenModeIsConfiguration.darker());
			} else {
				altitudeControl.setColors(colorWhenModeIsNotConfiguration, colorWhenModeIsNotConfiguration.brighter(), colorWhenModeIsNotConfiguration.darker());
			}

			//heading
			ClickableElement headingControl = getArtificialHorizon().getElement(HorizonControl.HEADING);
			if(vehicle.isCurrentMode(FlightPathControlMode.LOITER) && vehicle.getModePreferenceReport().getCourseHeadingModeState().equals(ModeState.CONFIGURATION)) {
				headingControl.setColors(colorWhenModeIsConfiguration, colorWhenModeIsConfiguration.brighter(), colorWhenModeIsConfiguration.darker());
			} else {
				headingControl.setColors(colorWhenModeIsNotConfiguration, colorWhenModeIsNotConfiguration.brighter(), colorWhenModeIsNotConfiguration.darker());
			}

			//heading
			ClickableElement speedControl = getArtificialHorizon().getElement(HorizonControl.SPEED);
			if(vehicle.isCurrentMode(FlightPathControlMode.LOITER) && vehicle.getModePreferenceReport().getSpeedModeState().equals(ModeState.CONFIGURATION)) {
				speedControl.setColors(colorWhenModeIsConfiguration, colorWhenModeIsConfiguration.brighter(), colorWhenModeIsConfiguration.darker());
			} else {
				speedControl.setColors(colorWhenModeIsNotConfiguration, colorWhenModeIsNotConfiguration.brighter(), colorWhenModeIsNotConfiguration.darker());
			}

			getArtificialHorizon().repaint();
		}
	}

	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 1;
			gridBagConstraints13.gridy = 1;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 2;
			gridBagConstraints3.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints3.gridy = 1;
			jLabel = new JLabel();
			jLabel.setText("Radius:");
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 3;
			gridBagConstraints1.insets = new Insets(0, 1, 0, 0);
			gridBagConstraints1.gridy = 1;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.anchor = GridBagConstraints.WEST;
			gridBagConstraints2.gridy = 1;
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.fill = GridBagConstraints.BOTH;
			gridBagConstraints12.weightx = 1.0;
			gridBagConstraints12.weighty = 1.0;
			gridBagConstraints12.gridwidth = 6;
			gridBagConstraints12.gridy = 0;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 3;
			gridBagConstraints8.gridy = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 5;
			gridBagConstraints.weightx = 1.0;
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
			contents.add(getRadius(), gridBagConstraints1);
			contents.add(jLabel, gridBagConstraints3);
			contents.add(getAroundPosition(), gridBagConstraints13);
		}
		return contents;
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
			refresh.setup(subscriberService, messagingService, MessageType.M101, MessageType.M102, MessageType.M106);
		}
		return refresh;
	}

	@Override
	protected String getBaseTitle() {
		return "Loiter Director";
	}

	/**
	 * This method initializes artificialHorizon	
	 * 	
	 * @return br.skylight.cucs.widgets.ArtificialHorizon	
	 */
	private ArtificialHorizon getArtificialHorizon() {
		if (artificialHorizon == null) {
			artificialHorizon = new ArtificialHorizon();
			for (SpeedType st : SpeedType.values()) {
				artificialHorizon.getSpeed(st).setTargetValue(30);
				artificialHorizon.getSpeed(st).setTargetValueVisible(true);
			}
			for (AltitudeType at : AltitudeType.values()) {
				artificialHorizon.getAltitude(at).setTargetValue(300);
				artificialHorizon.getAltitude(at).setTargetValueVisible(true);
			}
			getArtificialHorizon().getHeading().setTargetValueVisible(false);
			artificialHorizon.addArtificialHorizonListener(new ArtificialHorizonListener() {
				@Override
				public void onTargetAltitudeSet(AltitudeType altitudeType, float value) {
					LoiterConfiguration m = getCurrentVehicle().resolveLoiterConfiguration();
					if(m!=null) {
						m.setAltitudeType(altitudeType);
						m.setLoiterAltitude(value);
					}
					loiterUpdated(m);
				}
				@Override
				public void onTargetSpeedSet(SpeedType speedType, float value) {
					LoiterConfiguration m = getCurrentVehicle().resolveLoiterConfiguration();
					if(m!=null) {
						m.setLoiterSpeed(value);
						m.setSpeedType(speedType);
					}
					loiterUpdated(m);
				}
				@Override
				public void onTargetHeadingSet(float value) {
				}
			});
		}
		return artificialHorizon;
	}

	protected void loiterUpdated(LoiterConfiguration m) {
		LoiterConfiguration m2 = messagingService.resolveMessageForSending(LoiterConfiguration.class);
		m2.copyParametersFrom(m);
		m2.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
		messagingService.sendMessage(m2);
		
		//update artificial horizon targets according to current loiter configuration
		getArtificialHorizon().getSpeed(m.getSpeedType()).setTargetValue(m.getLoiterSpeed());
		getArtificialHorizon().setSelectedSpeedType(m.getSpeedType());
		
		getArtificialHorizon().getAltitude(m.getAltitudeType()).setTargetValue(m.getLoiterAltitude());
		getArtificialHorizon().setSelectedAltitudeType(m.getAltitudeType());
		
		getRadius().setValue((double)m.getLoiterRadius());
		getArtificialHorizon().updateUI();
		
		subscriberService.notifyLoiterEvent(m, EventType.UPDATED, this);
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
			activate.setToolTipText("Start loitering now around current coordinates");
			activate.setMargin(ViewHelper.getDefaultButtonMargin());
			activate.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(!getCurrentVehicle().isCurrentMode(FlightPathControlMode.LOITER)) {
						LoiterConfiguration lc = getCurrentVehicle().resolveLoiterConfiguration();

						//loiter around current position if position is not defined yet
						if(lc.getLatitude()==0 || lc.getLongitude()==0) {
							sendVehicleSteeringForLoiteringCurrentPosition(lc);
						}

						//send loiter configuration
						CUCSViewHelper.copySetpointsFromArtificialHorizon(getArtificialHorizon(), lc);//set speed and altitude
						lc.setLoiterRadius(((Double)getRadius().getValue()).floatValue());
						lc.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
						loiterUpdated(lc);

						//change operating mode
						VehicleOperatingModeCommand m = messagingService.resolveMessageForSending(VehicleOperatingModeCommand.class);
						m.setSelectFlightPathControlMode(FlightPathControlMode.LOITER);
						m.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
						messagingService.sendMessage(m);
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

	protected void sendVehicleSteeringForLoiteringCurrentPosition(LoiterConfiguration lc) {
		//send vehicle steering for defining loiter position coordinates
		VehicleSteeringCommand vs = vehicleControlService.resolveVehicleSteeringCommandForSending(getCurrentVehicle().getVehicleID().getVehicleID());
		InertialStates is = vehicleControlService.getLastReceivedMessage(getCurrentVehicle().getVehicleID().getVehicleID(), MessageType.M101);
		if(is!=null) {
			CUCSViewHelper.updateArtificialHorizonTargets(is, artificialHorizon);
			vs.setLoiterPositionLatitude(is.getLatitude());
			vs.setLoiterPositionLongitude(is.getLongitude());
			vs.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
		}
		lc.setLatitude(vs.getLoiterPositionLatitude());
		lc.setLongitude(vs.getLoiterPositionLongitude());
		vs.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
		vehicleControlService.sendVehicleSteeringCommand(vs);
	}

	@Override
	public void onPreferencesUpdated() {
		CUCSViewHelper.updateDisplayUnits(getArtificialHorizon());
		updateGUI();
	}

	/**
	 * This method initializes radius	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getRadius() {
		if (radius == null) {
			radius = new JSpinner(new SpinnerNumberModel(300, 0.3, 999999, 1));
			radius.setPreferredSize(new Dimension(40, 20));
			((JSpinner.NumberEditor) radius.getEditor()).getTextField().addKeyListener(new KeyListener() {
				public void keyTyped(KeyEvent ke) {
					if (ke.getKeyChar() == '\n') {
						LoiterConfiguration lc = getCurrentVehicle().resolveLoiterConfiguration();
						lc.setLoiterRadius(((Double)getRadius().getValue()).floatValue());
						loiterUpdated(lc);
					}
				}
				public void keyReleased(KeyEvent arg0) {}
				public void keyPressed(KeyEvent arg0) {}
			});
		}
		return radius;
	}

	@Override
	public void onLoiterEvent(LoiterConfiguration lc, EventType type) {
//		if(!type.equals(EventType.DELETED)) {
//			loiterConfigurations.put(lc.getVehicleID(), lc);
//		} else {
//			loiterConfigurations.remove(lc.getVehicleID());
//		}
	}

	/**
	 * This method initializes aroundPosition	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAroundPosition() {
		if (aroundPosition == null) {
			aroundPosition = new JButton();
			aroundPosition.setMargin(ViewHelper.getDefaultButtonMargin());
			aroundPosition.setText("L");
			aroundPosition.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					LoiterConfiguration lc = vehicleControlService.resolveVehicle(getCurrentVehicle().getVehicleID().getVehicleID()).resolveLoiterConfiguration();
					sendVehicleSteeringForLoiteringCurrentPosition(lc);
					subscriberService.notifyLoiterEvent(lc, EventType.UPDATED, getThis());
				}
			});
		}
		return aroundPosition;
	}

	protected LoiterDirectorView getThis() {
		return this;
	}

}
