package br.skylight.cucs.plugins.skylightvehicle.preflight;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import br.skylight.commons.AlertWrapper;
import br.skylight.commons.Servo;
import br.skylight.commons.VerificationResult;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.BitmappedLOI;
import br.skylight.commons.dli.datalink.DataLinkStatusReport;
import br.skylight.commons.dli.enums.AlertPriority;
import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.skylight.MiscInfoMessage;
import br.skylight.commons.dli.skylight.ServoActuationCommand;
import br.skylight.commons.dli.skylight.SoftwarePartReport;
import br.skylight.commons.dli.skylight.SoftwareStatus;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusAlert;
import br.skylight.commons.dli.systemid.VSMAuthorisationResponse;
import br.skylight.commons.dli.vehicle.AirAndGroundRelativeStates;
import br.skylight.commons.dli.vehicle.InertialStates;
import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.commons.services.StorageService;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.skylightvehicle.vehiclecontrol.SkylightVehicleControlService;
import br.skylight.cucs.widgets.VehicleView;
import br.skylight.cucs.widgets.checklist.CheckItemResult;
import br.skylight.cucs.widgets.checklist.ChecklistItem;
import br.skylight.cucs.widgets.checklist.ChecklistItemListener;
import br.skylight.cucs.widgets.checklist.ChecklistItemState;
import br.skylight.cucs.widgets.checklist.ChecklistLogger;

public class AutoChecklistView extends VehicleView implements MessageListener, ChecklistLogger {

	private static final long serialVersionUID = 1L;

	private boolean testing = false;

	private JPanel jContentPane = null;
	private ChecklistItem comm1 = null;
	private ChecklistItem comm2 = null;
	private ChecklistItem comm3 = null;
	private JLabel jLabel1 = null;
	private ChecklistItem systems2 = null;
	private ChecklistItem systems3 = null;
	private ChecklistItem systems4 = null;
	private JLabel jLabel11 = null;
	private ChecklistItem servos1 = null;
	private ChecklistItem servos2 = null;
	private JPanel contents = null;  //  @jve:decl-index=0:visual-constraint="10,1"
	private JPanel jContentPane1 = null;
	private JLabel jLabel2 = null;
	private ChecklistItem sensors2 = null;
	private ChecklistItem sensors3 = null;
	private JLabel jLabel12 = null;
	private ChecklistItem camera2 = null;
	private ChecklistItem camera1 = null;
	private ChecklistItem camera3 = null;
	private ChecklistItem uavconfig1 = null;
	private ChecklistItem uavconfig2 = null;
	private JLabel jLabel1111 = null;
	private ChecklistItem plan1 = null;
	private ChecklistItem plan2 = null;
	private JButton checkAll = null;
	private JButton logButton = null;
	private ChecklistLogUI logUI = null; // @jve:decl-index=0:visual-constraint=
											// "602,321"

	private String assistedChecklistLog = ""; // @jve:decl-index=0:

	private JLabel jLabel13 = null;

	private JLabel jLabel14 = null;

	private JPanel jPanel = null;

	private JPanel jPanel2 = null;

	private List<ChecklistItem> allChecklists = new ArrayList<ChecklistItem>(); // @  //  @jve:decl-index=0:
																				// jve
	private CheckItemResult testsCancelledResult = new CheckItemResult(ChecklistItemState.TEST_WARNING, "Tests cancelled");  //  @jve:decl-index=0:
	
	private ChecklistItem comm4 = null;
	protected ThreadWorker sweeper;
	
	private Map<String,SoftwarePartReport> softwarePartReports = new HashMap<String,SoftwarePartReport>();
	private SoftwareStatus softwareStatus;
	private MiscInfoMessage miscInfoMessage;
	private InertialStates inertialStates;
	private AirAndGroundRelativeStates airAndGroundRelativeStates;
	
	@ServiceInjection
	public MessagingService messagingService;

	@ServiceInjection
	public VehicleControlService vehicleControlService;
	
	@ServiceInjection
	public SkylightVehicleControlService skylightVehicleControlService;
	
	@ServiceInjection
	public StorageService storageService;

	@ServiceInjection
	public PluginManager pluginManager;

	public AutoChecklistView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMessageListener(MessageType.M21, this);
		subscriberService.addMessageListener(MessageType.M101, this);
		subscriberService.addMessageListener(MessageType.M102, this);
		subscriberService.addMessageListener(MessageType.M501, this);
		subscriberService.addMessageListener(MessageType.M1100, this);
		subscriberService.addMessageListener(MessageType.M2005, this);
		subscriberService.addMessageListener(MessageType.M2013, this);
		subscriberService.addMessageListener(MessageType.M2018, this);
	}

	@Override
	public void onMessageReceived(Message message) {
		//M21
		if(message instanceof VSMAuthorisationResponse) {
			getComm1().checkTest();
		
		//M101
		} else if(message instanceof InertialStates) {
			inertialStates = (InertialStates)message;
			getComm2().checkTest();
			getSensors2().checkTest();

		//M102
		} else if(message instanceof AirAndGroundRelativeStates) {
			airAndGroundRelativeStates = (AirAndGroundRelativeStates)message;
			getSensors2().checkTest();

		//M501
		} else if(message instanceof DataLinkStatusReport) {
			DataLinkStatusReport m = (DataLinkStatusReport)message;
			if(m.getAddressedTerminal().equals(DataTerminalType.GDT)) {
				getComm3().checkTest();
			} else if(m.getAddressedTerminal().equals(DataTerminalType.ADT)) {
				getComm4().checkTest();
			}

		//M1110
		} else if(message instanceof SubsystemStatusAlert) {
			SubsystemStatusAlert m = (SubsystemStatusAlert)message;
			getSystems4().checkTest();
			
		//M2005
		} else if(message instanceof MiscInfoMessage) {
			miscInfoMessage = (MiscInfoMessage)message;
			getSensors2().checkTest();
			getSensors3().checkTest();
			
		//M2013
		} else if(message instanceof SoftwareStatus) {
			softwareStatus = (SoftwareStatus)message;
			getUavconfig1().checkTest();
			getPlan1().checkTest();
			
		//M2018
		} else if(message instanceof SoftwarePartReport) {
			SoftwarePartReport sr = (SoftwarePartReport)message;
			softwarePartReports.put(sr.getName(),sr);
			getSystems2().checkTest();
		}
	}
	
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints52 = new GridBagConstraints();
			gridBagConstraints52.gridx = 0;
			gridBagConstraints52.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints52.insets = new Insets(0, 7, 0, 3);
			gridBagConstraints52.gridy = 6;
			GridBagConstraints gridBagConstraints42 = new GridBagConstraints();
			gridBagConstraints42.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints42.gridy = 23;
			gridBagConstraints42.insets = new Insets(0, 7, 3, 3);
			gridBagConstraints42.weighty = 1.0;
			gridBagConstraints42.anchor = GridBagConstraints.NORTH;
			gridBagConstraints42.gridx = 0;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.anchor = GridBagConstraints.WEST;
			gridBagConstraints2.insets = new Insets(2, 0, 0, 0);
			gridBagConstraints2.gridy = 14;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.insets = new Insets(0, 0, 0, 0);
			gridBagConstraints1.gridy = 2;
			jLabel13 = new JLabel();
			jLabel13.setText("Communications");
			jLabel13.setFont(new Font("Dialog", Font.BOLD, 12));
			GridBagConstraints gridBagConstraints191 = new GridBagConstraints();
			gridBagConstraints191.gridx = 0;
			gridBagConstraints191.insets = new Insets(0, 0, 7, 0);
			gridBagConstraints191.weighty = 1.0;
			gridBagConstraints191.anchor = GridBagConstraints.WEST;
			gridBagConstraints191.gridy = 0;
			GridBagConstraints gridBagConstraints181 = new GridBagConstraints();
			gridBagConstraints181.gridx = 0;
			gridBagConstraints181.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints181.insets = new Insets(0, 7, 0, 3);
			gridBagConstraints181.weighty = 0.0;
			gridBagConstraints181.anchor = GridBagConstraints.NORTH;
			gridBagConstraints181.gridy = 22;
			GridBagConstraints gridBagConstraints171 = new GridBagConstraints();
			gridBagConstraints171.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints171.gridx = 0;
			gridBagConstraints171.gridy = 21;
			gridBagConstraints171.insets = new Insets(0, 7, 0, 3);
			GridBagConstraints gridBagConstraints161 = new GridBagConstraints();
			gridBagConstraints161.anchor = GridBagConstraints.WEST;
			gridBagConstraints161.insets = new Insets(0, 7, 0, 3);
			gridBagConstraints161.gridx = 0;
			gridBagConstraints161.gridy = 20;
			gridBagConstraints161.fill = GridBagConstraints.HORIZONTAL;
			GridBagConstraints gridBagConstraints151 = new GridBagConstraints();
			gridBagConstraints151.anchor = GridBagConstraints.WEST;
			gridBagConstraints151.gridx = 0;
			gridBagConstraints151.gridy = 15;
			gridBagConstraints151.weightx = 0.0;
			gridBagConstraints151.insets = new Insets(2, 0, 0, 0);
			gridBagConstraints151.fill = GridBagConstraints.NONE;
			jLabel11 = new JLabel();
			jLabel11.setText("Manual verifications");
			jLabel11.setFont(new Font("Dialog", Font.BOLD, 12));
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints7.insets = new Insets(0, 7, 0, 3);
			gridBagConstraints7.gridy = 13;
			GridBagConstraints gridBagConstraints77 = new GridBagConstraints();
			gridBagConstraints77.gridx = 0;
			gridBagConstraints77.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints77.insets = new Insets(0, 7, 0, 3);
			gridBagConstraints77.gridy = 13;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints6.insets = new Insets(0, 7, 0, 3);
			gridBagConstraints6.gridy = 11;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.anchor = GridBagConstraints.WEST;
			gridBagConstraints4.insets = new Insets(2, 0, 0, 0);
			gridBagConstraints4.gridy = 7;
			jLabel1 = new JLabel();
			jLabel1.setText("Systems");
			jLabel1.setFont(new Font("Dialog", Font.BOLD, 12));
			jLabel14 = new JLabel();
			jLabel14.setText("Vehicle configuration");
			jLabel14.setFont(new Font("Dialog", Font.BOLD, 12));
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.insets = new Insets(0, 7, 0, 3);
			gridBagConstraints3.gridy = 5;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 0;
			gridBagConstraints21.weighty = 0.0;
			gridBagConstraints21.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints21.anchor = GridBagConstraints.NORTH;
			gridBagConstraints21.insets = new Insets(0, 7, 0, 3);
			gridBagConstraints21.gridy = 4;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 0.0;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.insets = new Insets(0, 7, 0, 3);
			gridBagConstraints.gridy = 3;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getComm1(), gridBagConstraints);
			jContentPane.add(getComm2(), gridBagConstraints21);
			jContentPane.add(getComm3(), gridBagConstraints3);
			jContentPane.add(getComm4(), gridBagConstraints52);
			jContentPane.add(jLabel1, gridBagConstraints4);
			jContentPane.add(getSystems2(), gridBagConstraints6);
			jContentPane.add(getSystems3(), gridBagConstraints7);
			jContentPane.add(getSystems4(), gridBagConstraints77);
			jContentPane.add(getUavconfig1(), gridBagConstraints161);
			jContentPane.add(getUavconfig2(), gridBagConstraints171);
			jContentPane.add(getCheckAll(), gridBagConstraints191);
			jContentPane.add(jLabel13, gridBagConstraints1);
			jContentPane.add(jLabel14, gridBagConstraints2);
		}
		return jContentPane;
	}

	/**
	 * This method initializes comm1
	 * 
	 * @return br.skylight.groundstation.widgets.checklist.ChecklistItem
	 */
	private ChecklistItem getComm1() {
		if (comm1 == null) {
			comm1 = new ChecklistItem();
			comm1.setTitle("Control grants stabilished");
			comm1.setChecklistItemListener(new ChecklistItemListener() {
				@Override
				public boolean prepareItemCheck() {
					vehicleControlService.requestVehicleInfos();
					return false;
				}
				@Override
				public CheckItemResult checkItem() {
					if (cucsHasGrantedLOI(new BitmappedLOI(5))) {
						return new CheckItemResult(ChecklistItemState.TEST_OK, null);
					} else if (cucsHasGrantedLOI(new BitmappedLOI(4))) {
						return new CheckItemResult(ChecklistItemState.TEST_WARNING, "This CUCS haven't grants for take-off and landing operations");
					} else if (cucsHasGrantedLOI(new BitmappedLOI(3))) {
						return new CheckItemResult(ChecklistItemState.TEST_WARNING, "This CUCS haven't grants for vehicle control. It will control only vehicle's payloads");
					} else if (cucsHasGrantedLOI(new BitmappedLOI(2))) {
						return new CheckItemResult(ChecklistItemState.TEST_WARNING, "This CUCS has grants only for receiving data from vehicle. No control will be allowed.");
					} else {
						return new CheckItemResult(ChecklistItemState.TEST_WARNING, "This CUCS has grants only for receiving indirect data from vehicle. No control will be allowed.");
					}
				}
			});
			prepareCheckItem(comm1);
		}
		return comm1;
	}

	/**
	 * This method initializes comm2
	 * 
	 * @return br.skylight.groundstation.widgets.checklist.ChecklistItem
	 */
	private ChecklistItem getComm2() {
		if (comm2 == null) {
			comm2 = new ChecklistItem();
			comm2.setTitle("Low latency with vehicle");
			comm2.setChecklistItemListener(new ChecklistItemListener() {
				@Override
				public boolean prepareItemCheck() {
					messagingService.sendRequestGenericInformation(MessageType.M101, getCurrentVehicle().getVehicleID().getVehicleID());
					return false;
				}
				public CheckItemResult checkItem() {
					if (inertialStates.getLatency() > 7) {
						return new CheckItemResult(ChecklistItemState.TEST_ERROR, "Latency too bad: " + (int)(inertialStates.getLatency()*1000) + " ms");
					} else if (inertialStates.getLatency() > 1) {
						return new CheckItemResult(ChecklistItemState.TEST_WARNING, "Latency warning: " + (int)(inertialStates.getLatency()*1000) + " ms");
					} else {
						return new CheckItemResult(ChecklistItemState.TEST_OK, "Latency: " + (int)(inertialStates.getLatency()*1000) + " ms");
					}
				}
			});
			prepareCheckItem(comm2);
		}
		return comm2;
	}

	/**
	 * This method initializes comm3
	 * 
	 * @return br.skylight.groundstation.widgets.checklist.ChecklistItem
	 */
	private ChecklistItem getComm3() {
		if (comm3 == null) {
			comm3 = new ChecklistItem();
			comm3.setTitle("Vehicle signal is good");
			comm3.setChecklistItemListener(new ChecklistItemListener() {
				@Override
				public boolean prepareItemCheck() {
					DataLinkStatusReport mg = getCurrentVehicle().getGdtDataLinkStatusReport();
					if(mg==null) {
						messagingService.sendRequestGenericInformation(MessageType.M501, getCurrentVehicle().getVehicleID().getVehicleID());
						return false;
					} else {
						return true;
					}
				}
				public CheckItemResult checkItem() {
					// verify GDT signal level
					DataLinkStatusReport mg = getCurrentVehicle().getGdtDataLinkStatusReport();
					if (mg.getDownlinkStatus() < 20) {
						return new CheckItemResult(ChecklistItemState.TEST_ERROR, "Vehicle signal level (from ADT) is too weak on ground terminal (GDT). Level=" + mg.getDownlinkStatus() + "%");
					} else if (mg.getDownlinkStatus() < 60) {
						return new CheckItemResult(ChecklistItemState.TEST_ERROR, "Vehicle signal level (from ADT) is not strong on ground terminal (GDT). Level=" + mg.getDownlinkStatus() + "%");
					} else {
						return new CheckItemResult(ChecklistItemState.TEST_OK, "GDT downlink strength: GDT=" + mg.getDownlinkStatus() + "%");
					}
				}
			});
			prepareCheckItem(comm3);
		}
		return comm3;
	}

	/**
	 * This method initializes systems2
	 * 
	 * @return br.skylight.groundstation.widgets.checklist.ChecklistItem
	 */
	private ChecklistItem getSystems2() {
		if (systems2 == null) {
			systems2 = new ChecklistItem();
			systems2.setTitle("Internal systems operational");
			systems2.setChecklistItemListener(new ChecklistItemListener() {
				@Override
				public boolean prepareItemCheck() {
					messagingService.sendRequestGenericInformation(MessageType.M2018, getCurrentVehicle().getVehicleID().getVehicleID());
					return false;
				}
				@Override
				public CheckItemResult checkItem() {
					boolean warning = false;
					boolean failed = false;
					String str = "";
					for(SoftwarePartReport ps : softwarePartReports.values()) {
						if(ps.isAlert()) {
							warning = true;
							str += ps.getName() + " last freq=" + ps.getAverageFrequency() + "Hz; time idle="+ ps.getTimeSinceLastStepMillis() +"ms (alert); ";
						}
						if(ps.isTimeout()) {
							failed = true;
							str += ps.getName() + " last freq=" + ps.getAverageFrequency() + "Hz; time idle="+ ps.getTimeSinceLastStepMillis() +"ms (timeout); ";
						}
					}
					if(failed) {
						return new CheckItemResult(ChecklistItemState.TEST_ERROR, str);
					} else if(warning) {
						return new CheckItemResult(ChecklistItemState.TEST_WARNING, str);
					} else {
						return new CheckItemResult(ChecklistItemState.TEST_OK, str);
					}
				}
			});
			prepareCheckItem(systems2);
		}
		return systems2;
	}

	/**
	 * This method initializes systems3
	 * 
	 * @return br.skylight.groundstation.widgets.checklist.ChecklistItem
	 */
	private ChecklistItem getSystems3() {
		if (systems3 == null) {
			systems3 = new ChecklistItem();
			systems3.setTitle("Enough disk space on station");
			systems3.setChecklistItemListener(new ChecklistItemListener() {
				public CheckItemResult checkItem() {
					long free = storageService.getBaseDir().getFreeSpace()/(1024*1024);
					if(free<100) {//100MB
						return new CheckItemResult(ChecklistItemState.TEST_ERROR, "Free disk space too low and may cause operational errors on ground station during flights. Free space: " + free + " MB");
					} else if(free<20000) {//20GB
						return new CheckItemResult(ChecklistItemState.TEST_WARNING, "Free disk space may be insufficient for video recording. Free space: " + free + " MB");
					} else {
						return new CheckItemResult(ChecklistItemState.TEST_OK, "Free space: " + free + " MB");
					}
				}
			});
			prepareCheckItem(systems3);
		}
		return systems3;
	}

	/**
	 * This method initializes systems3
	 * 
	 * @return br.skylight.groundstation.widgets.checklist.ChecklistItem
	 */
	private ChecklistItem getSystems4() {
		if (systems4 == null) {
			systems4 = new ChecklistItem();
			systems4.setTitle("No system alerts active");
			systems4.setChecklistItemListener(new ChecklistItemListener() {
				@Override
				public boolean prepareItemCheck() {
					messagingService.sendRequestGenericInformation(MessageType.M1100, getCurrentVehicle().getVehicleID().getVehicleID());
					return false;
				}
				@Override
				public CheckItemResult checkItem() {
					for (AlertWrapper aw : getCurrentVehicle().getSubsystemStatusAlerts().values()) {
						if(aw.getSubsystemStatusAlert().getPriority().ordinal()>=AlertPriority.EMERGENCY.ordinal()) {
							return new CheckItemResult(ChecklistItemState.TEST_ERROR, "Found dangerous items in active alerts: " + aw.getSubsystemStatusAlert().getText());
						} else if(aw.getSubsystemStatusAlert().getPriority().ordinal()>=AlertPriority.CAUTION.ordinal()) {
							return new CheckItemResult(ChecklistItemState.TEST_WARNING, "Found warning items in active alerts: " + aw.getSubsystemStatusAlert().getText());
						}
					}
					return new CheckItemResult(ChecklistItemState.TEST_OK, "Found "+ getCurrentVehicle().getSubsystemStatusAlerts().size() +" nominal/cleared alerts");
				}
			});
			prepareCheckItem(systems4);
		}
		return systems4;
	}

	/**
	 * This method initializes servos1
	 * 
	 * @return br.skylight.groundstation.widgets.checklist.ChecklistItem
	 */
	private ChecklistItem getServos1() {
		if (servos1 == null) {
			servos1 = new ChecklistItem();
			servos1.setTitle("Surface actuators verification");
			servos1.setChecklistItemListener(new ChecklistItemListener() {
				public CheckItemResult checkItem() {
					//instruct operator about tests
					int r = JOptionPane.showConfirmDialog(null, 
						"All vehicle control surfaces will be swept.\n" +
						"Pay attention to actuation and verify full range and well trimmed movements.\n" +
						"Ready to start?", "Actuators test", JOptionPane.OK_CANCEL_OPTION);

					//start tests
					try {
						if(r==JOptionPane.OK_OPTION) {
							//reset surface states
							resetActuators();
							String errorMsg = "";
							
							//THROTTLE
							int answer = testActuator(Servo.THROTTLE);
							if(answer==JOptionPane.NO_OPTION) {
								errorMsg += "THROTTLE control is not behaving as expected; ";
							} if(answer==JOptionPane.CANCEL_OPTION) {
								return cancelTests();
							}

							//AILERON
							answer = testActuator(Servo.AILERON_RIGHT);
							if(answer==JOptionPane.NO_OPTION) {
								errorMsg += "AILERON control is not behaving as expected; ";
							} if(answer==JOptionPane.CANCEL_OPTION) {
								return cancelTests();
							}

							//ELEVATOR
							answer = testActuator(Servo.ELEVATOR);
							if(answer==JOptionPane.NO_OPTION) {
								errorMsg += "ELEVATOR control is not behaving as expected; ";
							} if(answer==JOptionPane.CANCEL_OPTION) {
								return cancelTests();
							}

							//RUDDER
							answer = testActuator(Servo.RUDDER);
							if(answer==JOptionPane.NO_OPTION) {
								errorMsg += "RUDDER control is not behaving as expected; ";
							} if(answer==JOptionPane.CANCEL_OPTION) {
								return cancelTests();
							}
							
							if(errorMsg.length()>0) {
								return new CheckItemResult(ChecklistItemState.TEST_ERROR, errorMsg);
							} else { 
								return new CheckItemResult(ChecklistItemState.TEST_OK, null);
							}
							
						} else {
							return cancelTests();
						}
					} catch (Exception e) {
						return new CheckItemResult(ChecklistItemState.TEST_ERROR, e.toString());
					}
				}
			});
			servos1.setChecklistLogger(this);
		}
		return servos1;
	}

	private int testActuator(final Servo servo) throws IOException, Exception {
		try {
			startSweeping(servo, 3000);
			int re = JOptionPane.showConfirmDialog(null, "Is " + servo.getName() + " behaving as expected?\n"+ servo.getInstructions(), "Test confirmation", JOptionPane.YES_NO_CANCEL_OPTION);
			stopSweeping();
			return re;
			
		} catch (IllegalStateException e) {
			JOptionPane.showMessageDialog(null, e.toString());
			return JOptionPane.CANCEL_OPTION;
		}
		
	}
	
	private void stopSweeping() {
		if(sweeper!=null) {
			try {
				sweeper.deactivate();
			} catch (Exception e) {
				e.printStackTrace();
			}
			sweeper.waitForDeactivation(5000);
		}
	}
	
	private void startSweeping(final Servo servo, final int time) throws Exception {
		sweeper = new ThreadWorker(1) {
			public void step() throws Exception {
				//full up
				setServo(127);
				Thread.sleep(time);
				if(!isActive()) {
					return;
				}

				//neutral
				setServo(0);
				Thread.sleep(time);
				if(!isActive()) {
					return;
				}
				
				//full down
				if(!servo.equals(Servo.THROTTLE)) {
					setServo(-127);
					Thread.sleep(time);
					if(!isActive()) {
						return;
					}

					//neutral
					setServo(0);
					Thread.sleep(time);
					if(!isActive()) {
						return;
					}
				}

			}
			private void setServo(int setpoint) {
				ServoActuationCommand a = messagingService.resolveMessageForSending(ServoActuationCommand.class);
				a.setServo(servo);
				a.setCommandedSetpoint(setpoint);
				messagingService.sendMessage(a);
			}
			@Override
			public void onDeactivate() throws Exception {
				//neutral
				setServo(0);
			}
		};
		sweeper.activate();
	}

	private CheckItemResult cancelTests() {
		stopSweeping();
		testing = false;
		addAssistedChecklistLog("===TESTS CANCELLED BY OPERATOR===");
		return testsCancelledResult;
	}
	
	private AutoChecklistView getThis() {
		return this;
	}
	
	/**
	 * This method initializes servos2
	 * 
	 * @return br.skylight.groundstation.widgets.checklist.ChecklistItem
	 */
	private ChecklistItem getServos2() {
		if (servos2 == null) {
			servos2 = new ChecklistItem();
			servos2.setTitle("Manual/auto mode switching properly");
			servos2.setChecklistItemListener(new ChecklistItemListener() {
				public CheckItemResult checkItem() {
					//start sweeping for tests
					try {
						resetActuators();
					} catch (Exception e1) {
						e1.printStackTrace();
						return new CheckItemResult(ChecklistItemState.TEST_ERROR, "ERROR: "+e1.getMessage());
					}
					
					try {
						startSweeping(Servo.AILERON_RIGHT, 1000);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, e.toString());
						return cancelTests();
					}
					
					//instruct operator about tests
					int answer = JOptionPane.showConfirmDialog(null, 
						"Automatic to manual mode switching will be tested now:\n" +
						"  1. Switch RC controller from/to auto mode a few times;\n" +
						"  2. When in auto mode, aileron will be swept;\n" +
						"  3. When in manual mode, verify if your RC controller is trimmed and responsive.\n" +
						"Did the tests succeed?", "Auto/manual mode test", JOptionPane.OK_CANCEL_OPTION);

					stopSweeping();
					
					if(answer==JOptionPane.OK_OPTION) {
						return new CheckItemResult(ChecklistItemState.TEST_OK, null);
					} else if(answer==JOptionPane.NO_OPTION) {
						return new CheckItemResult(ChecklistItemState.TEST_ERROR, "Manual/Auto mode switching not working as expected");
					} else {
						return cancelTests();
					}
				}
			});
			servos2.setChecklistLogger(this);
		}
		return servos2;
	}

	private void resetActuators() throws IOException, Exception {
		sendServoCommand(Servo.AILERON_RIGHT, 0);
		sendServoCommand(Servo.AILERON_LEFT, 0);
		sendServoCommand(Servo.ELEVATOR, 0);
		sendServoCommand(Servo.RUDDER, 0);
		sendServoCommand(Servo.THROTTLE, 0);
	}
	
	private void sendServoCommand(Servo servo, float value) {
		ServoActuationCommand a = messagingService.resolveMessageForSending(ServoActuationCommand.class);
		a.setServo(servo);
		a.setCommandedSetpoint(value);
		a.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
		messagingService.sendMessage(a);
	}
	
	/**
	 * This method initializes contents0
	 * 
	 * @return javax.swing.JPanel
	 */
	protected JPanel getContents() {
		if (contents == null) {
			GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
			gridBagConstraints29.gridx = 0;
			gridBagConstraints29.gridwidth = 2;
			gridBagConstraints29.insets = new Insets(2, 2, 2, 2);
			gridBagConstraints29.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints29.gridy = 0;
			GridBagConstraints gridBagConstraints30 = new GridBagConstraints();
			gridBagConstraints30.gridx = 1;
			gridBagConstraints30.gridy = 1;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 3;
			gridBagConstraints11.gridy = 1;
			GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
			gridBagConstraints23.gridx = 0;
			gridBagConstraints23.insets = new Insets(3, 3, 0, 0);
			gridBagConstraints23.anchor = GridBagConstraints.WEST;
			gridBagConstraints23.gridy = 0;
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.gridx = 2;
			gridBagConstraints22.gridy = 0;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(523, 348));
			contents.add(getJPanel(), gridBagConstraints11);
			contents.add(getJPanel2(), gridBagConstraints29);
		}
		return contents;
	}

	/**
	 * This method initializes jContentPane1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane1() {
		if (jContentPane1 == null) {
			GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
			gridBagConstraints24.anchor = GridBagConstraints.EAST;
			gridBagConstraints24.gridx = 0;
			gridBagConstraints24.gridy = 0;
			gridBagConstraints24.insets = new Insets(0, 0, 15, 3);
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			gridBagConstraints20.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints20.gridx = 0;
			gridBagConstraints20.gridy = 4;
			gridBagConstraints20.insets = new Insets(0, 7, 0, 3);
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints17.gridx = 0;
			gridBagConstraints17.gridy = 21;
			gridBagConstraints17.weighty = 1.0;
			gridBagConstraints17.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints17.insets = new Insets(0, 7, 0, 3);
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			gridBagConstraints19.anchor = GridBagConstraints.NORTH;
			gridBagConstraints19.insets = new Insets(0, 7, 0, 3);
			gridBagConstraints19.gridx = 0;
			gridBagConstraints19.gridy = 2;
			gridBagConstraints19.weighty = 0.0;
			gridBagConstraints19.fill = GridBagConstraints.HORIZONTAL;
			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
			gridBagConstraints18.anchor = GridBagConstraints.WEST;
			gridBagConstraints18.gridx = 0;
			gridBagConstraints18.gridy = 1;
			gridBagConstraints18.insets = new Insets(0, 0, 0, 0);
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints16.gridx = 0;
			gridBagConstraints16.gridy = 20;
			gridBagConstraints16.insets = new Insets(0, 7, 0, 3);
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.anchor = GridBagConstraints.WEST;
			gridBagConstraints15.gridy = 19;
			gridBagConstraints15.insets = new Insets(2, 0, 0, 0);
			gridBagConstraints15.gridx = 0;
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints10.gridx = 0;
			gridBagConstraints10.gridy = 7;
			gridBagConstraints10.insets = new Insets(0, 7, 0, 3);
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.anchor = GridBagConstraints.WEST;
			gridBagConstraints9.insets = new Insets(0, 7, 0, 3);
			gridBagConstraints9.gridx = 0;
			gridBagConstraints9.gridy = 6;
			gridBagConstraints9.fill = GridBagConstraints.HORIZONTAL;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.anchor = GridBagConstraints.WEST;
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.gridy = 5;
			gridBagConstraints8.weightx = 0.0;
			gridBagConstraints8.insets = new Insets(2, 0, 0, 0);
			gridBagConstraints8.fill = GridBagConstraints.NONE;
			GridBagConstraints gridBagConstraints71 = new GridBagConstraints();
			gridBagConstraints71.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints71.gridx = 0;
			gridBagConstraints71.gridy = 18;
			gridBagConstraints71.insets = new Insets(0, 7, 0, 3);
			GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
			gridBagConstraints61.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints61.gridx = 0;
			gridBagConstraints61.gridy = 16;
			gridBagConstraints61.insets = new Insets(0, 7, 0, 3);
			GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
			gridBagConstraints51.anchor = GridBagConstraints.WEST;
			gridBagConstraints51.insets = new Insets(0, 7, 0, 3);
			gridBagConstraints51.gridx = 0;
			gridBagConstraints51.gridy = 17;
			gridBagConstraints51.fill = GridBagConstraints.HORIZONTAL;
			GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
			gridBagConstraints41.anchor = GridBagConstraints.WEST;
			gridBagConstraints41.gridx = 0;
			gridBagConstraints41.gridy = 12;
			gridBagConstraints41.insets = new Insets(2, 0, 0, 0);
			jLabel12 = new JLabel();
			jLabel12.setText("Camera");
			jLabel12.setFont(new Font("Dialog", Font.BOLD, 12));
			jLabel2 = new JLabel();
			jLabel2.setText("Sensors");
			jLabel2.setFont(new Font("Dialog", Font.BOLD, 12));
			jLabel1111 = new JLabel();
			jLabel1111.setText("Mission plan");
			jLabel1111.setFont(new Font("Dialog", Font.BOLD, 12));
			jContentPane1 = new JPanel();
			jContentPane1.setLayout(new GridBagLayout());
			jContentPane1.add(jLabel11, gridBagConstraints8);
			jContentPane1.add(getSensors2(), gridBagConstraints19);
			jContentPane1.add(getSensors3(), gridBagConstraints20);
			jContentPane1.add(getServos1(), gridBagConstraints9);
			jContentPane1.add(getServos2(), gridBagConstraints10);
//			jContentPane1.add(jLabel12, gridBagConstraints41);
//			jContentPane1.add(getCamera1(), gridBagConstraints61);
//			jContentPane1.add(getCamera2(), gridBagConstraints51);
//			jContentPane1.add(getCamera3(), gridBagConstraints71);
			jContentPane1.add(jLabel1111, gridBagConstraints15);
			jContentPane1.add(getPlan1(), gridBagConstraints16);
			jContentPane1.add(getPlan2(), gridBagConstraints17);
			jContentPane1.add(jLabel2, gridBagConstraints18);
			jContentPane1.add(getLogButton(), gridBagConstraints24);
		}
		return jContentPane1;
	}

	/**
	 * This method initializes sensors2
	 * 
	 * @return br.skylight.groundstation.widgets.checklist.ChecklistItem
	 */
	private ChecklistItem getSensors2() {
		if (sensors2 == null) {
			sensors2 = new ChecklistItem();
			sensors2.setTitle("Good sensors reading");
			sensors2.setChecklistItemListener(new ChecklistItemListener() {
				@Override
				public boolean prepareItemCheck() {
					messagingService.sendRequestGenericInformation(MessageType.M101, getCurrentVehicle().getVehicleID().getVehicleID());
					messagingService.sendRequestGenericInformation(MessageType.M102, getCurrentVehicle().getVehicleID().getVehicleID());
					messagingService.sendRequestGenericInformation(MessageType.M2005, getCurrentVehicle().getVehicleID().getVehicleID());
					return false;
				}
				@Override
				public CheckItemResult checkItem() {
					if(inertialStates==null || airAndGroundRelativeStates==null || miscInfoMessage==null) {
						return null;
					}
					
					VerificationResult r = new VerificationResult();
					r.assertRange((int)Math.toDegrees(inertialStates.getTheta()), -2, -1, 1, 2, "Pitch");
					r.assertRange((int)Math.toDegrees(inertialStates.getPhi()), -2, -1, 1, 2, "Roll");
					r.assertRange((float)inertialStates.getGroundSpeed(), -1F, -0.1F, 0.1F, 1F, "Ground speed");
					r.assertRange(inertialStates.getUAccel()+inertialStates.getVAccel()+inertialStates.getWAccel(), -1F, -0.1F, 0.1F, 1F, "Body accelerations");
					r.assertRange(inertialStates.getWSpeed(), -1F, -0.2F, 0.2F, 1F, "Vertical speed");
					r.assertRange(airAndGroundRelativeStates.getAglAltitude(), -5F, -0.2F, 0.2F, 5F, "AGL altitude");
					r.assertRange(airAndGroundRelativeStates.getIndicatedAirspeed(), -3F, -0.5F, 0.5F, 3F, "IAS");
					r.assertRange(miscInfoMessage.getOnboardTemperature(), -5, 10, 40, 70, "Onboard temperature");
					r.assertRange(miscInfoMessage.getChtTemperature(), -5, 20, 70, 130, "Cilinder head temperature");
					
					return generateResult(r);
				}
			});
			prepareCheckItem(sensors2);
		}
		return sensors2;
	}

	/**
	 * This method initializes sensors3
	 * 
	 * @return br.skylight.groundstation.widgets.checklist.ChecklistItem
	 */
	private ChecklistItem getSensors3() {
		if (sensors3 == null) {
			sensors3 = new ChecklistItem();
			sensors3.setTitle("Batteries charged");
			sensors3.setChecklistItemListener(new ChecklistItemListener() {
				@Override
				public boolean prepareItemCheck() {
					messagingService.sendRequestGenericInformation(MessageType.M2005, getCurrentVehicle().getVehicleID().getVehicleID());
					return false;
				}
				public CheckItemResult checkItem() {
					if(miscInfoMessage==null) {
						return null;
					}
					VerificationResult r = new VerificationResult();
					r.assertNotLowValue(miscInfoMessage.getBattery1Voltage(), 11000, 15000, "Battery 1");
					r.assertNotLowValue(miscInfoMessage.getBattery2Voltage(), 11000, 15000, "Battery 2");
					return generateResult(r);
				}
			});
			prepareCheckItem(sensors3);
		}
		return sensors3;
	}

	/**
	 * This method initializes camera2
	 * 
	 * @return br.skylight.groundstation.widgets.checklist.ChecklistItem
	 */
	private ChecklistItem getCamera2() {
		if (camera2 == null) {
			camera2 = new ChecklistItem();
			camera2.setTitle("Receiving image from UAV");
			prepareCheckItem(camera2);
		}
		return camera2;
	}

	/**
	 * This method initializes camera1
	 * 
	 * @return br.skylight.groundstation.widgets.checklist.ChecklistItem
	 */
	private ChecklistItem getCamera1() {
		if (camera1 == null) {
			camera1 = new ChecklistItem();
			camera1.setTitle("Transmitter on/off switching properly");
			prepareCheckItem(camera1);
		}
		return camera1;
	}

	/**
	 * This method initializes camera3
	 * 
	 * @return br.skylight.groundstation.widgets.checklist.ChecklistItem
	 */
	private ChecklistItem getCamera3() {
		if (camera3 == null) {
			camera3 = new ChecklistItem();
			camera3.setTitle("Camera PTZ working properly");
			prepareCheckItem(camera3);
		}
		return camera3;
	}

	/**
	 * This method initializes uavconfig1
	 * 
	 * @return br.skylight.groundstation.widgets.checklist.ChecklistItem
	 */
	private ChecklistItem getUavconfig1() {
		if (uavconfig1 == null) {
			uavconfig1 = new ChecklistItem();
			uavconfig1.setTitle("Vehicle configuration uploaded");
			uavconfig1.setChecklistItemListener(new ChecklistItemListener() {
				@Override
				public boolean prepareItemCheck() {
					messagingService.sendRequestGenericInformation(MessageType.M2013, getCurrentVehicle().getVehicleID().getVehicleID());
					return false;
				}
				public CheckItemResult checkItem() {
					try {
						long localConfigChecksum = IOHelper.calculateCRC(getCurrentVehicle().getVehicleConfiguration());
						if(softwareStatus.getVehicleConfigurationCRC()!=localConfigChecksum) {
							return new CheckItemResult(ChecklistItemState.TEST_WARNING, "Vehicle configuration on vehicle is different from UCS. Upload it.");
						}
						
						long localSkylightConfigChecksum = IOHelper.calculateCRC(skylightVehicleControlService.resolveSkylightVehicleConfiguration(getCurrentVehicle().getVehicleID().getVehicleID()));
						if(softwareStatus.getSkylightVehicleConfigurationCRC()!=localSkylightConfigChecksum) {
							return new CheckItemResult(ChecklistItemState.TEST_WARNING, "Vehicle configuration on vehicle is different from UCS. Upload it.");
						}
						
						return new CheckItemResult(ChecklistItemState.TEST_OK, "Vehicle/ground station vehicle configurations are the same");
						
					} catch (Exception e) {
						e.printStackTrace();
						return new CheckItemResult(ChecklistItemState.TEST_ERROR, "ERROR: "+e.getMessage());
					}
				}
			});
			prepareCheckItem(uavconfig1);
		}
		return uavconfig1;
	}

	/**
	 * This method initializes uavconfig2
	 * 
	 * @return br.skylight.groundstation.widgets.checklist.ChecklistItem
	 */
	private ChecklistItem getUavconfig2() {
		if (uavconfig2 == null) {
			uavconfig2 = new ChecklistItem();
			uavconfig2.setTitle("Local vehicle configuration validation");
			uavconfig2.setChecklistItemListener(new ChecklistItemListener() {
				public CheckItemResult checkItem() {
					VerificationResult r = getCurrentVehicle().getVehicleConfiguration().validate();
					skylightVehicleControlService.resolveSkylightVehicle(getCurrentVehicle().getVehicleID().getVehicleID()).getSkylightVehicleConfiguration().validate(r, getCurrentVehicle().getVehicleConfiguration());
					return generateResult(r);
				}
			});
			prepareCheckItem(uavconfig2);
		}
		return uavconfig2;
	}

	/**
	 * This method initializes plan1
	 * 
	 * @return br.skylight.groundstation.widgets.checklist.ChecklistItem
	 */
	private ChecklistItem getPlan1() {
		if (plan1 == null) {
			plan1 = new ChecklistItem();
			plan1.setTitle("Mission plan uploaded");
			plan1.setChecklistItemListener(new ChecklistItemListener() {
				@Override
				public boolean prepareItemCheck() {
					messagingService.sendRequestGenericInformation(MessageType.M2013, getCurrentVehicle().getVehicleID().getVehicleID());
					return false;
				}
				public CheckItemResult checkItem() {
					try {
						if(getCurrentVehicle().getMission()!=null) {
							long localMissionChecksum = IOHelper.calculateCRC(getCurrentVehicle().getMission());
							if(softwareStatus.getMissionCRC()!=localMissionChecksum) {
								return new CheckItemResult(ChecklistItemState.TEST_WARNING, "Mission plan is different from ground station. Upload it.");
							} else {
								return new CheckItemResult(ChecklistItemState.TEST_OK, "Vehicle mission plan is the same as UCS");
							}
						} else {
							//mission not found in vehicle
							if(softwareStatus.getMissionCRC()==-1) {
								return new CheckItemResult(ChecklistItemState.TEST_OK, "No mission plan found both on vehicle and ground station");
							} else {
								return new CheckItemResult(ChecklistItemState.TEST_WARNING, "No mission plan found in ground station but there is a mission in vehicle");
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						return new CheckItemResult(ChecklistItemState.TEST_ERROR, "ERROR: "+e.getMessage());
					}
				}
			});
			prepareCheckItem(plan1);
		}
		return plan1;
	}

	/**
	 * This method initializes plan2
	 * 
	 * @return br.skylight.groundstation.widgets.checklist.ChecklistItem
	 */
	private ChecklistItem getPlan2() {
		if (plan2 == null) {
			plan2 = new ChecklistItem();
			plan2.setTitle("Local mission validated");
			plan2.setChecklistItemListener(new ChecklistItemListener() {
				public CheckItemResult checkItem() {
					if(getCurrentVehicle().getMission()!=null) {
						VerificationResult r = getCurrentVehicle().getMission().validate(getCurrentVehicle().getVehicleConfiguration());
						return generateResult(r);
					} else {
						return new CheckItemResult(ChecklistItemState.TEST_WARNING, "No mission plan found both on vehicle and ground station");
					}
				}
			});
			prepareCheckItem(plan2);
		}
		return plan2;
	}

	/**
	 * This method initializes checkAll
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getCheckAll() {
		if (checkAll == null) {
			checkAll = new JButton();
			checkAll.setText("Execute all tests");
			checkAll.setFont(new Font("Dialog", Font.BOLD, 14));
			checkAll.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// start tests
					if (!testing) {
						addAssistedChecklistLog("==STARTING ALL CHECKLIST TESTS==");
						checkAll.setText("Cancel tests");
						testing = true;
						// use a new thread for the screen to get updated
						Thread t = new Thread(new Runnable() {
							public void run() {
								try {
									for (final ChecklistItem item : allChecklists) {
										item.performTest();
										if (!testing)
											break;
									}
								} finally {
									testing = false;
									checkAll.setText("Execute all tests");
									addAssistedChecklistLog("==FINISHED CHECKLIST TESTS==");
								}
							}
						});
						t.start();
					} else {
						testing = false;
					}
				}
			});
		}
		return checkAll;
	}

	/**
	 * This method initializes logButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getLogButton() {
		if (logButton == null) {
			logButton = new JButton();
			logButton.setText("See test logs");
			logButton.setFont(new Font("Dialog", Font.PLAIN, 9));
			logButton.setMargin(ViewHelper.getMinimalButtonMargin());
			logButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getLogUI().showContents(assistedChecklistLog);
				}
			});
		}
		return logButton;
	}

	/**
	 * This method initializes logUI
	 * 
	 * @return br.skylight.groundstation.tabs.ChecklistLogUI
	 */
	private ChecklistLogUI getLogUI() {
		if (logUI == null) {
			logUI = new ChecklistLogUI(null);
		}
		return logUI;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel2
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.fill = GridBagConstraints.BOTH;
			gridBagConstraints14.gridx = 1;
			gridBagConstraints14.gridy = 0;
			gridBagConstraints14.weightx = 1.0;
			gridBagConstraints14.weighty = 0.0;
			gridBagConstraints14.insets = new Insets(0, 15, 3, 5);
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.fill = GridBagConstraints.BOTH;
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.gridy = 0;
			gridBagConstraints12.weightx = 1.0;
			gridBagConstraints12.weighty = 0.0;
			gridBagConstraints12.insets = new Insets(0, 7, 3, 0);
			jPanel2 = new JPanel();
			jPanel2.setLayout(new GridBagLayout());
			jPanel2.setBorder(BorderFactory.createTitledBorder(null, "Assisted checklist", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 14), new Color(51, 51, 51)));
			jPanel2.add(getJContentPane(), gridBagConstraints12);
			jPanel2.add(getJContentPane1(), gridBagConstraints14);
		}
		return jPanel2;
	}

	/**
	 * This method initializes comm4
	 * 
	 * @return br.skylight.groundstation.widgets.checklist.ChecklistItem
	 */
	private ChecklistItem getComm4() {
		if (comm4 == null) {
			comm4 = new ChecklistItem();
			comm4.setTitle("Ground signal is good");
			comm4.setChecklistItemListener(new ChecklistItemListener() {
				@Override
				public boolean prepareItemCheck() {
					DataLinkStatusReport ma = getCurrentVehicle().getAdtDataLinkStatusReport();
					if(ma==null) {
						messagingService.sendRequestGenericInformation(MessageType.M501, getCurrentVehicle().getVehicleID().getVehicleID());
						return false;
					} else {
						return true;
					}
				}
				public CheckItemResult checkItem() {
					//verify ADT signal level
					DataLinkStatusReport ma = getCurrentVehicle().getAdtDataLinkStatusReport();
					if (ma.getDownlinkStatus() < 20) {
						return new CheckItemResult(ChecklistItemState.TEST_ERROR, "Ground signal level (from GDT) is too weak on vehicle (ADT). Level=" + ma.getDownlinkStatus() + "%");
					} else if (ma.getDownlinkStatus() < 60) {
						return new CheckItemResult(ChecklistItemState.TEST_ERROR, "Ground signal level (from GDT) is not strong on vehicle (ADT). Level=" + ma.getDownlinkStatus() + "%");
					} else {
						return new CheckItemResult(ChecklistItemState.TEST_OK, "ADT downlink strength=" + ma.getDownlinkStatus() + "%");
					}
				}
			});
			prepareCheckItem(comm4);
		}
		return comm4;
	}

	private void prepareCheckItem(ChecklistItem item) {
		allChecklists.add(item);
		item.setChecklistLogger(this);
	}

	private CheckItemResult generateResult(VerificationResult r) {
		if(r.getErrors().size()>0) {
			return new CheckItemResult(ChecklistItemState.TEST_ERROR, r.toString());
		} else if(r.getWarnings().size()>0) {
			return new CheckItemResult(ChecklistItemState.TEST_WARNING, r.toString());
		} else {
			return new CheckItemResult(ChecklistItemState.TEST_OK, r.toString());
		}
	}

	@Override
	protected void updateGUI() {
	}
	
	@Override
	protected String getBaseTitle() {
		return "Pre-flight Checklist (auto)";
	}

	private void addAssistedChecklistLog(String contents) {
		assistedChecklistLog += contents + "\r\n";
		getLogUI().updateContents(assistedChecklistLog);
	}

	@Override
	public void logTestResult(String title, CheckItemResult result) {
		addAssistedChecklistLog(result.getMessage());
	}
	
}
