package br.skylight.cucs.plugins.vehiclecontrol;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import br.skylight.commons.ControllableElement;
import br.skylight.commons.LOI;
import br.skylight.commons.Payload;
import br.skylight.commons.StringHelper;
import br.skylight.commons.Vehicle;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.BitmappedLOI;
import br.skylight.commons.dli.BitmappedStation;
import br.skylight.commons.dli.enums.ControlledStationMode;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.systemid.CUCSAuthorisationRequest;
import br.skylight.cucs.plugins.core.UserService;
import br.skylight.cucs.widgets.CUCSViewHelper;

public class ControlRequestDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JLabel jLabel4 = null;
	private JLabel vehicleId = null;
	private JLabel vsmId = null;
	private JButton requestButton = null;
	private JButton cancelRequest = null;
	private JPanel requestedElements = null;
	
	private ControllableElement element;
	private List<ControlRequestItem> requestItems = new ArrayList<ControlRequestItem>();  //  @jve:decl-index=0:
	private JPanel jPanel1 = null;
	private JLabel jLabel = null;

	private MessagingService messagingService;
	private UserService userService;
	
	/**
	 * @param owner
	 */
	public ControlRequestDialog(Frame owner, ControllableElement element, MessagingService messagingService, UserService userService) {
		super(owner);
		this.element = element;
		this.messagingService = messagingService;
		this.userService = userService;
		initialize();
		ViewHelper.centerWindow(this);
		CUCSViewHelper.setDefaultIcon(this);
		ViewHelper.setPrimaryFocus(this, requestButton);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(381, 196);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setTitle("Control Request");
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.insets = new Insets(3, 55, 5, 10);
			gridBagConstraints.gridwidth = 4;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.gridy = 2;
			GridBagConstraints gridBagConstraints71 = new GridBagConstraints();
			gridBagConstraints71.gridx = 0;
			gridBagConstraints71.gridwidth = 3;
			gridBagConstraints71.insets = new Insets(0, 20, 10, 0);
			gridBagConstraints71.fill = GridBagConstraints.NONE;
			gridBagConstraints71.weightx = 1.0;
			gridBagConstraints71.gridy = 3;
			GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
			gridBagConstraints61.gridx = 7;
			gridBagConstraints61.gridy = 3;
			GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
			gridBagConstraints51.gridx = 1;
			gridBagConstraints51.gridy = 1;
			gridBagConstraints51.weightx = 1;
			gridBagConstraints51.weighty = 1;
			gridBagConstraints51.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints51.anchor = GridBagConstraints.NORTHWEST;
			GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
			gridBagConstraints41.gridx = 1;
			gridBagConstraints41.gridy = 3;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 3;
			gridBagConstraints7.insets = new Insets(10, 0, 4, 10);
			gridBagConstraints7.gridy = 0;
			vsmId = new JLabel();
			vsmId.setText("-");
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 1;
			gridBagConstraints6.insets = new Insets(10, 0, 4, 4);
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.anchor = GridBagConstraints.WEST;
			gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints6.gridy = 0;
			vehicleId = new JLabel();
			vehicleId.setText("-");
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.weightx = 0.0;
			gridBagConstraints4.anchor = GridBagConstraints.WEST;
			gridBagConstraints4.insets = new Insets(3, 10, 0, 4);
			gridBagConstraints4.gridwidth = 2;
			gridBagConstraints4.gridy = 1;
			jLabel4 = new JLabel();
			jLabel4.setText("Request the following controls:");
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 2;
			gridBagConstraints2.weightx = 0.0;
			gridBagConstraints2.anchor = GridBagConstraints.EAST;
			gridBagConstraints2.insets = new Insets(10, 0, 4, 4);
			gridBagConstraints2.gridy = 0;
			jLabel2 = new JLabel();
			jLabel2.setText("VSM ID:");
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.weightx = 0.0;
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			gridBagConstraints1.insets = new Insets(10, 10, 4, 4);
			gridBagConstraints1.gridy = 0;
			jLabel1 = new JLabel();
			jLabel1.setText("Vehicle ID:");
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(jLabel1, gridBagConstraints1);
			jContentPane.add(jLabel2, gridBagConstraints2);
			jContentPane.add(jLabel4, gridBagConstraints4);
			jContentPane.add(vehicleId, gridBagConstraints6);
			jContentPane.add(vsmId, gridBagConstraints7);
			jContentPane.add(getJPanel1(), gridBagConstraints71);
			jContentPane.add(getRequestedElements(), gridBagConstraints);
			
			//add actions
			requestedElements.removeAll();
			if(element instanceof Vehicle) {
				Vehicle v = (Vehicle)element;
				vehicleId.setText(v.getLabel());
				vsmId.setText(StringHelper.formatId(v.getVehicleID().getVsmID()));
				
				ControlRequestItem ci = new ControlRequestItem(v, userService);
				requestedElements.add(ci);
				requestItems.add(ci);
				for (Payload p : v.getPayloads().values()) {
					ci = new ControlRequestItem(p, userService);
					requestedElements.add(ci);
					requestItems.add(ci);
				}
			} else if(element instanceof Payload) {
				Payload p = (Payload)element;
				vehicleId.setText(StringHelper.formatId(p.getVehicleID().getVehicleID()));
				vsmId.setText(StringHelper.formatId(p.getVehicleID().getVsmID()));
				
				ControlRequestItem ci = new ControlRequestItem(p, userService);
				requestedElements.add(ci);
				requestItems.add(ci);
			}
		}
		return jContentPane;
	}

	/**
	 * This method initializes requestButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getRequestButton() {
		if (requestButton == null) {
			requestButton = new JButton();
			requestButton.setText("Request control");
			requestButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					for (ControlRequestItem ri : requestItems) {
						if(ri.getCheckbox().isSelected()) {
							CUCSAuthorisationRequest m = messagingService.resolveMessageForSending(CUCSAuthorisationRequest.class);
							m.setVehicleID(ri.getControllableElement().getVehicleID().getVehicleID());
							m.setVehicleType(ri.getControllableElement().getVehicleID().getVehicleType().ordinal());
							m.setVehicleSubtype(ri.getControllableElement().getVehicleID().getVehicleSubtype());
							m.setVsmID(ri.getControllableElement().getVehicleID().getVsmID());
							
							//LOI
							m.setRequestedHandoverLOI(BitmappedLOI.valueOf(((LOI)ri.getLoiCombo().getSelectedItem())));
							
							//STATION NUMBER FOR PAYLOADS
							if(ri.getControllableElement() instanceof Payload) {
								m.setControlledStation(new BitmappedStation(((Payload)ri.getControllableElement()).getUniqueStationNumber()));
							} else {
								m.getControlledStation().setData(0);
							}
							
							//OVERRIDE MODE
							if(ri.getOverrideMode().isSelected()) {
								m.setControlledStationMode(ControlledStationMode.OVERRIDE_CONTROL);
							} else {
								m.setControlledStationMode(ControlledStationMode.REQUEST_CONTROL);
							}
							
							//SEND MESSAGE
							messagingService.sendMessage(m);
						}
					}
					setVisible(false);
				}
			});
		}
		return requestButton;
	}

	private Component getThis() {
		return this;
	}
	
	/**
	 * This method initializes cancelRequest	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelRequest() {
		if (cancelRequest == null) {
			cancelRequest = new JButton();
			cancelRequest.setText("Cancel");
			cancelRequest.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
				}
			});
		}
		return cancelRequest;
	}

	/**
	 * This method initializes requestedElements	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getRequestedElements() {
		if (requestedElements == null) {
			jLabel = new JLabel();
			jLabel.setText("JLabel");
			requestedElements = new JPanel();
			requestedElements.setLayout(new BoxLayout(getRequestedElements(), BoxLayout.Y_AXIS));
			requestedElements.add(jLabel, null);
		}
		return requestedElements;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints8.gridy = 0;
			gridBagConstraints8.gridx = 1;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 0;
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
			jPanel1.add(getRequestButton(), gridBagConstraints3);
			jPanel1.add(getCancelRequest(), gridBagConstraints8);
		}
		return jPanel1;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
