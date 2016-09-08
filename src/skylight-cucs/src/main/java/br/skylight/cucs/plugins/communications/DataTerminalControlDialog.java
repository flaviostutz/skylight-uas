package br.skylight.cucs.plugins.communications;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.combobox.EnumComboBoxModel;

import br.skylight.commons.StringHelper;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.datalink.DataLinkControlCommand;
import br.skylight.commons.dli.datalink.DataLinkStatusReport;
import br.skylight.commons.dli.enums.AntennaMode;
import br.skylight.commons.dli.enums.CommunicationSecurityMode;
import br.skylight.commons.dli.enums.CommunicationSecurityState;
import br.skylight.commons.dli.enums.DataLinkState;
import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.dli.enums.LinkChannelPriorityState;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.widgets.FeedbackButton;
import br.skylight.cucs.widgets.NumberInputDialog;

public class DataTerminalControlDialog extends JDialog implements MessageListener {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel6 = null;
	private JLabel jLabel7 = null;
	private JLabel jLabel8 = null;
	private JLabel jLabel10 = null;
	private JLabel type = null;
	private JLabel linkId = null;
	private JButton close = null;
	private JPanel jPanel = null;
	private FeedbackButton send = null;
	private DataLinkStatusReport statusReport;  //  @jve:decl-index=0:
	
	@ServiceInjection
	public MessagingService messagingService;
	
	@ServiceInjection
	public SubscriberService subscriberService;
	private JComboBox dataLinkState = null;
	private JComboBox antennaMode = null;
	private JComboBox securityMode = null;
	private JComboBox channelPriority = null;
	
	/**
	 * @param owner
	 */
	public DataTerminalControlDialog(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(252, 200);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setTitle("Data terminal setup");
		this.setContentPane(getJContentPane());
		setModal(true);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints30 = new GridBagConstraints();
			gridBagConstraints30.fill = GridBagConstraints.BOTH;
			gridBagConstraints30.gridy = 10;
			gridBagConstraints30.weightx = 1.0;
			gridBagConstraints30.insets = new Insets(3, 5, 0, 5);
			gridBagConstraints30.gridx = 1;
			GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
			gridBagConstraints29.fill = GridBagConstraints.BOTH;
			gridBagConstraints29.gridy = 8;
			gridBagConstraints29.weightx = 1.0;
			gridBagConstraints29.insets = new Insets(3, 5, 0, 5);
			gridBagConstraints29.gridx = 1;
			GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
			gridBagConstraints28.fill = GridBagConstraints.BOTH;
			gridBagConstraints28.gridy = 7;
			gridBagConstraints28.weightx = 1.0;
			gridBagConstraints28.insets = new Insets(3, 5, 0, 5);
			gridBagConstraints28.gridx = 1;
			GridBagConstraints gridBagConstraints27 = new GridBagConstraints();
			gridBagConstraints27.fill = GridBagConstraints.BOTH;
			gridBagConstraints27.gridy = 6;
			gridBagConstraints27.weightx = 1.0;
			gridBagConstraints27.insets = new Insets(3, 5, 0, 5);
			gridBagConstraints27.gridx = 1;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 0;
			gridBagConstraints21.gridwidth = 2;
			gridBagConstraints21.weightx = 0.0;
			gridBagConstraints21.weighty = 1.0;
			gridBagConstraints21.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints21.gridy = 11;
			GridBagConstraints gridBagConstraints91 = new GridBagConstraints();
			gridBagConstraints91.gridx = 1;
			gridBagConstraints91.insets = new Insets(5, 5, 0, 0);
			gridBagConstraints91.weightx = 0.0;
			gridBagConstraints91.anchor = GridBagConstraints.WEST;
			gridBagConstraints91.gridy = 0;
			linkId = new JLabel();
			linkId.setText("-");
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 1;
			gridBagConstraints12.insets = new Insets(3, 5, 0, 0);
			gridBagConstraints12.weightx = 0.0;
			gridBagConstraints12.anchor = GridBagConstraints.WEST;
			gridBagConstraints12.gridy = 1;
			type = new JLabel();
			type.setText("-");
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 0;
			gridBagConstraints10.anchor = GridBagConstraints.EAST;
			gridBagConstraints10.insets = new Insets(3, 5, 5, 0);
			gridBagConstraints10.weightx = 0.0;
			gridBagConstraints10.gridy = 10;
			jLabel10 = new JLabel();
			jLabel10.setText("Channel priority:");
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.anchor = GridBagConstraints.EAST;
			gridBagConstraints8.insets = new Insets(3, 0, 0, 0);
			gridBagConstraints8.weightx = 0.0;
			gridBagConstraints8.gridy = 8;
			jLabel8 = new JLabel();
			jLabel8.setText("Security mode:");
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.anchor = GridBagConstraints.EAST;
			gridBagConstraints7.insets = new Insets(3, 5, 0, 0);
			gridBagConstraints7.weightx = 0.0;
			gridBagConstraints7.gridy = 7;
			jLabel7 = new JLabel();
			jLabel7.setText("Antenna mode:");
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.anchor = GridBagConstraints.EAST;
			gridBagConstraints6.insets = new Insets(3, 0, 0, 0);
			gridBagConstraints6.weightx = 0.0;
			gridBagConstraints6.gridy = 6;
			jLabel6 = new JLabel();
			jLabel6.setText("Data link state:");
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			gridBagConstraints1.insets = new Insets(3, 0, 0, 0);
			gridBagConstraints1.weightx = 0.0;
			gridBagConstraints1.gridy = 1;
			jLabel1 = new JLabel();
			jLabel1.setText("Type:");
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.anchor = GridBagConstraints.EAST;
			gridBagConstraints.insets = new Insets(5, 0, 0, 0);
			gridBagConstraints.weightx = 0.0;
			gridBagConstraints.gridy = 0;
			jLabel = new JLabel();
			jLabel.setText("Data link Id:");
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(jLabel, gridBagConstraints);
			jContentPane.add(jLabel1, gridBagConstraints1);
			jContentPane.add(jLabel6, gridBagConstraints6);
			jContentPane.add(jLabel7, gridBagConstraints7);
			jContentPane.add(jLabel8, gridBagConstraints8);
			jContentPane.add(jLabel10, gridBagConstraints10);
			jContentPane.add(type, gridBagConstraints12);
			jContentPane.add(linkId, gridBagConstraints91);
			jContentPane.add(getJPanel(), gridBagConstraints21);
			jContentPane.add(getDataLinkState(), gridBagConstraints27);
			jContentPane.add(getAntennaMode(), gridBagConstraints28);
			jContentPane.add(getSecurityMode(), gridBagConstraints29);
			jContentPane.add(getChannelPriority(), gridBagConstraints30);
		}
		return jContentPane;
	}

	/**
	 * This method initializes close	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getClose() {
		if (close == null) {
			close = new JButton();
			close.setText("Close");
			close.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
				}
			});
		}
		return close;
	}
	
	public void showDialog(DataLinkStatusReport sr, int vehicleId, DataTerminalType type) {
		this.statusReport = sr;

		//this is used because maybe ADT is in RXOnly mode and the ground
		//station don't have last reported ADT id for commanding it
		if(statusReport==null) {
			statusReport = new DataLinkStatusReport();
			Double r = NumberInputDialog.showInputDialog(getThis(), "Please enter Data Terminal ID:", 0, 0, Double.MAX_VALUE, 1, 0, 0);
			if(r!=null) {
				statusReport.setDataLinkId(r.intValue());
				statusReport.setAddressedTerminal(type);
				statusReport.setVehicleID(vehicleId);
			} else {
				return;
			}
		}
		
		populateFields(statusReport);
		ViewHelper.centerWindow(this);
		getSend().notifyFeedback();
		setVisible(true);
	}

	private void populateFields(DataLinkStatusReport sr) {
		linkId.setText(StringHelper.formatId(sr.getDataLinkId()));
		type.setText(sr.getAddressedTerminal().name());
		getDataLinkState().setSelectedItem(sr.getDataLinkState());
		getAntennaMode().setSelectedItem(AntennaMode.values()[sr.getAntennaState().ordinal()]);
		if(sr.getCommunicationSecurityState().equals(CommunicationSecurityState.KEYED)) {
			getSecurityMode().setSelectedItem(CommunicationSecurityMode.NORMAL);
		} else {
			getSecurityMode().setSelectedItem(CommunicationSecurityMode.ZEROIZED);
		}
		getChannelPriority().setSelectedItem(sr.getLinkChannelPriorityState());
	}
	
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			gridBagConstraints20.gridx = 0;
			gridBagConstraints20.insets = new Insets(0, 0, 0, 2);
			gridBagConstraints20.gridy = 0;
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			gridBagConstraints19.gridwidth = 1;
			gridBagConstraints19.gridy = 0;
			gridBagConstraints19.insets = new Insets(0, 2, 0, 0);
			gridBagConstraints19.gridx = 1;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getClose(), gridBagConstraints19);
			jPanel.add(getSend(), gridBagConstraints20);
		}
		return jPanel;
	}

	/**
	 * This method initializes send	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private FeedbackButton getSend() {
		if (send == null) {
			send = new FeedbackButton();
			send.setText("Send");
			send.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(send.isWaitingFeedback()) {
						if(JOptionPane.showConfirmDialog(getThis(), "An incorrect configuration can result in loss of vehicle communication.\nConfirm operation?")==JOptionPane.OK_OPTION) {
							DataLinkControlCommand m = messagingService.resolveMessageForSending(DataLinkControlCommand.class);
							m.setVehicleID(statusReport.getVehicleID());
							m.setAddressedTerminal(statusReport.getAddressedTerminal());
							m.setDataLinkId(statusReport.getDataLinkId());
							m.setCommunicationSecurityMode((CommunicationSecurityMode)getSecurityMode().getSelectedItem());
							m.setLinkChannelPriority((LinkChannelPriorityState)getChannelPriority().getSelectedItem());
							m.setSetAntennaMode((AntennaMode)getAntennaMode().getSelectedItem());
							m.setSetDataLinkState((DataLinkState)getDataLinkState().getSelectedItem());
							messagingService.sendMessage(m);
							
							//request message for control confirmation
							subscriberService.addMessageListener(MessageType.M501, getThis());
							messagingService.sendRequestGenericInformation(MessageType.M501, statusReport.getVehicleID());
						} else {
							send.notifyFeedback();
						}
					} else {
						subscriberService.removeMessageListener(MessageType.M501, getThis());
					}
				}
			});
		}
		return send;
	}

	@Override
	public void onMessageReceived(Message message) {
		if(message instanceof DataLinkStatusReport) {
			DataLinkStatusReport m = (DataLinkStatusReport)message;
			if(m.getAddressedTerminal().equals(statusReport.getAddressedTerminal()) && m.getDataLinkId()==statusReport.getDataLinkId() && m.getVehicleID()==statusReport.getVehicleID()) {
				statusReport = (DataLinkStatusReport) m.createCopy();
				populateFields(statusReport);
				getSend().notifyFeedback();
				subscriberService.removeMessageListener(MessageType.M501, getThis());
			}
		}
	}
	
	/**
	 * This method initializes dataLinkState	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getDataLinkState() {
		if (dataLinkState == null) {
			dataLinkState = new JComboBox();
			dataLinkState.setModel(new EnumComboBoxModel(DataLinkState.class));
		}
		return dataLinkState;
	}

	/**
	 * This method initializes antennaMode	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getAntennaMode() {
		if (antennaMode == null) {
			antennaMode = new JComboBox();
			antennaMode.setModel(new EnumComboBoxModel(AntennaMode.class));
		}
		return antennaMode;
	}

	/**
	 * This method initializes securityMode	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getSecurityMode() {
		if (securityMode == null) {
			securityMode = new JComboBox();
			securityMode.setModel(new EnumComboBoxModel(CommunicationSecurityMode.class));
		}
		return securityMode;
	}

	/**
	 * This method initializes channelPriority	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getChannelPriority() {
		if (channelPriority == null) {
			channelPriority = new JComboBox();
			channelPriority.setModel(new EnumComboBoxModel(LinkChannelPriorityState.class));
		}
		return channelPriority;
	}

	private DataTerminalControlDialog getThis() {
		return this;
	}
	
}  //  @jve:decl-index=0:visual-constraint="14,12"
