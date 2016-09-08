package br.skylight.cucs.plugins.vehiclecontrol;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.enums.FlightTerminationState;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.vehicle.FlightTerminationCommand;
import br.skylight.commons.dli.vehicle.FlightTerminationModeReport;
import br.skylight.commons.plugin.annotations.ExtensionPointsInjection;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.plugins.subscriber.VehicleListener;
import br.skylight.cucs.widgets.RoundButton;
import br.skylight.cucs.widgets.VehicleMessageRefreshButton;
import br.skylight.cucs.widgets.VehicleView;

public class FlightTerminationView extends VehicleView implements MessageListener, VehicleListener {

	@ServiceInjection
	public SubscriberService subscriberService;
	
	@ServiceInjection
	public MessagingService messagingService;
	
	@ExtensionPointsInjection
	public List<FlightTerminationModeExtensionPoint> flightTerminationModeExtensionPoints;
	
	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="18,12"
	private RoundButton prepareTermination = null;
	private RoundButton execute = null;
	private JPanel jPanel = null;
	private VehicleMessageRefreshButton refresh = null;
	private JPanel jPanel1 = null;

	//used to control offline terminations (ucs is not receiving nothing from vehicle, but will try to send a command)
	private boolean pressedReset;
	private int lastSentMode;
	
	public FlightTerminationView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMessageListener(MessageType.M108, this);
	}

	@Override
	public void onMessageReceived(Message message) {
		//M108
		if(message instanceof FlightTerminationModeReport) {
			pressedReset = false;
			updateGUI();
			getPrepareTermination().notifyFeedback();
			getExecute().notifyFeedback();
			getRefresh().notifyFeedback();
		}
	}
	
	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.gridy = 2;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.gridwidth = 2;
			gridBagConstraints4.anchor = GridBagConstraints.CENTER;
			gridBagConstraints4.insets = new Insets(3, 5, 0, 5);
			gridBagConstraints4.fill = GridBagConstraints.BOTH;
			gridBagConstraints4.weighty = 1.0;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.gridy = 1;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(202, 139));
			contents.add(getJPanel(), gridBagConstraints4);
			updateGUI();
		}
		return contents;
	}

	/**
	 * This method initializes prepareTermination	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private RoundButton getPrepareTermination() {
		if (prepareTermination == null) {
			prepareTermination = new RoundButton();
			prepareTermination.setText("ARM");
			prepareTermination.setFont(new Font("Dialog", Font.BOLD, 12));
			prepareTermination.setEnabled(false);
			prepareTermination.setColorUnselected(Color.YELLOW);
			prepareTermination.setColorSelected(Color.YELLOW.darker());
			prepareTermination.setPreferredSize(new Dimension(119, 23));
			prepareTermination.setColorUnselected(Color.YELLOW);
			prepareTermination.setMargin(ViewHelper.getMinimalButtonMargin());
			prepareTermination.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					FlightTerminationCommand tc = messagingService.resolveMessageForSending(FlightTerminationCommand.class);
					tc.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
					FlightTerminationModeReport flightTerminationModeReport = (FlightTerminationModeReport)getLastReceivedMessageForCurrentVehicle(MessageType.M108);
					
					//SEND SYSTEM RESET MESSAGE
					if(flightTerminationModeReport!=null && !pressedReset &&
						(flightTerminationModeReport.getFlightTerminationState().equals(FlightTerminationState.ARM_FT_SYSTEM)
								|| flightTerminationModeReport.getFlightTerminationState().equals(FlightTerminationState.EXECUTE_FT_SYSTEM))) {
						tc.setFlightTerminationMode(flightTerminationModeReport.getFlightTerminationMode());
						tc.setFlightTerminationState(FlightTerminationState.RESET_FT_SYSTEM);
						messagingService.sendMessage(tc);
						
					//SEND SYSTEM ARM MESSAGE
					} else {
						Map<Integer,String> modes = new HashMap<Integer,String>();
						for (FlightTerminationModeExtensionPoint ep : flightTerminationModeExtensionPoints) {
							if(ep.getModeLabelItems()!=null) {
								modes.putAll(ep.getModeLabelItems());
							}
						}
						int mode = FlightTerminationModeDialog.showFlightTerminationModeInput(modes);
						if(mode!=-1) {
							tc.setFlightTerminationMode(mode);
							tc.setFlightTerminationState(FlightTerminationState.ARM_FT_SYSTEM);
							messagingService.sendMessage(tc);
							
							//This control will be enabled upon flight termination status message receipt too, 
							//but may be the vehicle isn't sending messages to ground station, but still receiving commands, 
							//so enabled this for blind flight termination execution (without receiving arm confirmation from vehicle)
							getExecute().setEnabled(true);
							getExecute().setText("TERMINATE");
							lastSentMode = mode;
						} else {
							getPrepareTermination().notifyFeedback();
						}
					}
					
					//keep this flag to indicate that a arm/reset was sent, but not yet answered.
					//in the meanwhile, the operator could send termination commands because
					//maybe the vehicle is not answering, but can still receive the termination commands
					pressedReset = true;
					
				}
			});
		}
		return prepareTermination;
	}

	/**
	 * This method initializes execute	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private RoundButton getExecute() {
		if (execute == null) {
			execute = new RoundButton();
			execute.setText("TERMINATE");
			execute.setMargin(new Insets(10, 10, 10, 10));
			execute.setFont(new Font("Dialog", Font.BOLD, 16));
			execute.setEnabled(false);
			execute.setRaiseLevel(4);
			execute.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					FlightTerminationModeReport flightTerminationModeReport = (FlightTerminationModeReport)getLastReceivedMessageForCurrentVehicle(MessageType.M108);
					if(pressedReset
						|| flightTerminationModeReport.getFlightTerminationState().equals(FlightTerminationState.ARM_FT_SYSTEM)
						|| flightTerminationModeReport.getFlightTerminationState().equals(FlightTerminationState.EXECUTE_FT_SYSTEM)) {
						FlightTerminationCommand tc = messagingService.resolveMessageForSending(FlightTerminationCommand.class);
						tc.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
						if(pressedReset) {
							tc.setFlightTerminationMode(lastSentMode);
						} else {
							tc.setFlightTerminationMode(flightTerminationModeReport.getFlightTerminationMode());
						}
						tc.setFlightTerminationState(FlightTerminationState.EXECUTE_FT_SYSTEM);
						messagingService.sendMessage(tc);
					}
				}
			});
		}
		return execute;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints5.gridwidth = 1;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.gridy = 2;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.insets = new Insets(0, 3, 0, 3);
			gridBagConstraints3.weighty = 1.0;
			gridBagConstraints3.fill = GridBagConstraints.NONE;
			gridBagConstraints3.anchor = GridBagConstraints.NORTH;
			gridBagConstraints3.gridy = 1;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.insets = new Insets(3, 3, 6, 3);
			gridBagConstraints2.weightx = 0.0;
			gridBagConstraints2.fill = GridBagConstraints.NONE;
			gridBagConstraints2.weighty = 1.0;
			gridBagConstraints2.anchor = GridBagConstraints.SOUTH;
			gridBagConstraints2.gridy = 0;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getPrepareTermination(), gridBagConstraints2);
			jPanel.add(getExecute(), gridBagConstraints3);
			jPanel.add(getJPanel1(), gridBagConstraints5);
		}
		return jPanel;
	}

	protected void updateGUI() {
		getRefresh().setVehicle(getCurrentVehicle());
		FlightTerminationModeReport flightTerminationModeReport = (FlightTerminationModeReport)getLastReceivedMessageForCurrentVehicle(MessageType.M108);
		boolean hasGrants = cucsHasGrantedLOI(MessageType.M46.getLOIs());
		getRefresh().setEnabled(hasGrants);
		
		if(!hasGrants || pressedReset) {
			getPrepareTermination().setText("ARM");
			getPrepareTermination().setEnabled(false);
			getExecute().setEnabled(false);
			getExecute().setBackground(SystemColor.control);
			getJPanel().setBackground(SystemColor.control);
		} else {
			getPrepareTermination().setEnabled(true);
			if(flightTerminationModeReport==null || flightTerminationModeReport.getFlightTerminationState().equals(FlightTerminationState.RESET_FT_SYSTEM)) {
				getPrepareTermination().setText("ARM");
				getPrepareTermination().setSelected(false);
				getExecute().setEnabled(false);
				getExecute().setSelected(false);
				getExecute().setBackground(SystemColor.control);
				getJPanel().setBackground(SystemColor.control);
			} else if(flightTerminationModeReport.getFlightTerminationState().equals(FlightTerminationState.ARM_FT_SYSTEM)) {
				getPrepareTermination().setText("RESET");
				getPrepareTermination().setSelected(true);
				getExecute().setEnabled(true);
				getExecute().setSelected(false);
				getExecute().setBackground(new Color(200,0,0));
				getJPanel().setBackground(Color.YELLOW);
				getExecute().setText(getFlightTerminationModeLabel(flightTerminationModeReport.getFlightTerminationMode()));
			} else if(flightTerminationModeReport.getFlightTerminationState().equals(FlightTerminationState.EXECUTE_FT_SYSTEM)) {
				getPrepareTermination().setText("RESET");
				getPrepareTermination().setSelected(true);
				getExecute().setEnabled(true);
				getExecute().setSelected(true);
				getExecute().setBackground(new Color(150,0,0));
				getJPanel().setBackground(Color.ORANGE);
			}
		}
	}

	private String getFlightTerminationModeLabel(int mode) {
		for (FlightTerminationModeExtensionPoint ep : flightTerminationModeExtensionPoints) {
			if(ep.getModeLabelItems().get(mode)!=null) {
				if(ep.getModeLabelItems()!=null) {
					return ep.getModeLabelItems().get(mode);
				}
			}
		}
		
		//specific mode name not found
		if(mode>0) {
			return "TERMINATE ("+ mode +")";
		} else {
			return "TERMINATE";
		}
	}

	@Override
	protected String getBaseTitle() {
		return "Flight Termination";
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
			refresh.setup(subscriberService, messagingService, MessageType.M108);
		}
		return refresh;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			gridBagConstraints1.insets = new Insets(3, 3, 3, 3);
			gridBagConstraints1.gridy = 1;
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.add(getRefresh(), gridBagConstraints1);
		}
		return jPanel1;
	}
	
}
