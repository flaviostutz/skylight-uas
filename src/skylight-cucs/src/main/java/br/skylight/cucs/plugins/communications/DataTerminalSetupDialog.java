package br.skylight.cucs.plugins.communications;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;

import br.skylight.commons.StringHelper;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.datalink.DataLinkSetupMessage;
import br.skylight.commons.dli.datalink.DataLinkStatusReport;
import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.widgets.FeedbackButton;
import br.skylight.cucs.widgets.NumberInputDialog;

public class DataTerminalSetupDialog extends JDialog implements MessageListener {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel4 = null;
	private JLabel jLabel6 = null;
	private JLabel jLabel7 = null;
	private JLabel jLabel8 = null;
	private JLabel jLabel10 = null;
	private JLabel type = null;
	private JLabel linkId = null;
	private JButton close = null;
	private JPanel jPanel = null;
	private FeedbackButton send = null;
	private JSpinner channelNumber = null;
	private JSpinner primaryHopPattern = null;
	private JSpinner forwardCarrierFreq = null;
	private JSpinner returnCarrierFreq = null;
	private JSpinner pnCode = null;
	private DataLinkStatusReport statusReport;  //  @jve:decl-index=0:
	
	@ServiceInjection
	public MessagingService messagingService;
	
	@ServiceInjection
	public SubscriberService subscriberService;
	
	/**
	 * @param owner
	 */
	public DataTerminalSetupDialog(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(252, 216);
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
			GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
			gridBagConstraints26.gridx = 1;
			gridBagConstraints26.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints26.insets = new Insets(2, 5, 0, 5);
			gridBagConstraints26.gridy = 10;
			GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
			gridBagConstraints25.gridx = 1;
			gridBagConstraints25.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints25.insets = new Insets(2, 5, 0, 5);
			gridBagConstraints25.gridy = 8;
			GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
			gridBagConstraints24.gridx = 1;
			gridBagConstraints24.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints24.insets = new Insets(2, 5, 0, 5);
			gridBagConstraints24.gridy = 7;
			GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
			gridBagConstraints23.gridx = 1;
			gridBagConstraints23.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints23.insets = new Insets(2, 5, 0, 5);
			gridBagConstraints23.gridy = 6;
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.gridx = 1;
			gridBagConstraints22.anchor = GridBagConstraints.WEST;
			gridBagConstraints22.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints22.insets = new Insets(2, 5, 0, 5);
			gridBagConstraints22.weightx = 1.0;
			gridBagConstraints22.gridy = 4;
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
			gridBagConstraints10.insets = new Insets(3, 0, 5, 0);
			gridBagConstraints10.weightx = 0.0;
			gridBagConstraints10.gridy = 10;
			jLabel10 = new JLabel();
			jLabel10.setText("PN code:");
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.anchor = GridBagConstraints.EAST;
			gridBagConstraints8.insets = new Insets(3, 0, 0, 0);
			gridBagConstraints8.weightx = 0.0;
			gridBagConstraints8.gridy = 8;
			jLabel8 = new JLabel();
			jLabel8.setText("Return carrier freq (Hz):");
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.anchor = GridBagConstraints.EAST;
			gridBagConstraints7.insets = new Insets(3, 5, 0, 0);
			gridBagConstraints7.weightx = 0.0;
			gridBagConstraints7.gridy = 7;
			jLabel7 = new JLabel();
			jLabel7.setText("Forward carrier freq (Hz):");
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.anchor = GridBagConstraints.EAST;
			gridBagConstraints6.insets = new Insets(3, 0, 0, 0);
			gridBagConstraints6.weightx = 0.0;
			gridBagConstraints6.gridy = 6;
			jLabel6 = new JLabel();
			jLabel6.setText("Prim. hop pattern:");
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.anchor = GridBagConstraints.EAST;
			gridBagConstraints4.insets = new Insets(3, 0, 0, 0);
			gridBagConstraints4.weightx = 0.0;
			gridBagConstraints4.gridy = 4;
			jLabel4 = new JLabel();
			jLabel4.setText("Channel number:");
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
			jContentPane.add(jLabel4, gridBagConstraints4);
			jContentPane.add(jLabel6, gridBagConstraints6);
			jContentPane.add(jLabel7, gridBagConstraints7);
			jContentPane.add(jLabel8, gridBagConstraints8);
			jContentPane.add(jLabel10, gridBagConstraints10);
			jContentPane.add(type, gridBagConstraints12);
			jContentPane.add(linkId, gridBagConstraints91);
			jContentPane.add(getJPanel(), gridBagConstraints21);
			jContentPane.add(getChannelNumber(), gridBagConstraints22);
			jContentPane.add(getPrimaryHopPattern(), gridBagConstraints23);
			jContentPane.add(getForwardCarrierFreq(), gridBagConstraints24);
			jContentPane.add(getReturnCarrierFreq(), gridBagConstraints25);
			jContentPane.add(getPnCode(), gridBagConstraints26);
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
		getChannelNumber().setValue(sr.getReportedChannel());
		getPrimaryHopPattern().setValue(sr.getReportedPrimaryHopPattern());
		getForwardCarrierFreq().setValue(sr.getReportedForwardLinkCarrierFreq());
		getReturnCarrierFreq().setValue(sr.getReportedReturnLinkCarrierFreq());
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
							DataLinkSetupMessage m = messagingService.resolveMessageForSending(DataLinkSetupMessage.class);
							m.setAddressedTerminal(statusReport.getAddressedTerminal());
							m.setDataLinkId(statusReport.getDataLinkId());
							m.setSelectChannel((Integer)getChannelNumber().getValue());
							m.setSelectForwardLinkCarrierFreq((Float)getForwardCarrierFreq().getValue());
							m.setSelectReturnLinkCarrierFreq((Float)getReturnCarrierFreq().getValue());
							m.setSelectPrimaryHopPattern((Integer)getPrimaryHopPattern().getValue());
							m.setSetPnCode((Integer)getPnCode().getValue());
							m.setVehicleID(statusReport.getVehicleID());
							messagingService.sendMessage(m);
		
							//request message for setup confirmation
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

	/**
	 * This method initializes channelNumber	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getChannelNumber() {
		if (channelNumber == null) {
			channelNumber = new JSpinner(new SpinnerNumberModel(0, 0, Short.MAX_VALUE*2, 1));
		}
		return channelNumber;
	}

	/**
	 * This method initializes primaryHopPattern	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getPrimaryHopPattern() {
		if (primaryHopPattern == null) {
			primaryHopPattern = new JSpinner(new SpinnerNumberModel(0, 0, 254, 1));
		}
		return primaryHopPattern;
	}

	/**
	 * This method initializes forwardCarrierFreq	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getForwardCarrierFreq() {
		if (forwardCarrierFreq == null) {
			forwardCarrierFreq = new JSpinner(new SpinnerNumberModel(0.0, 0, Float.MAX_VALUE, 0.1));
			ViewHelper.setupSpinnerNumber(forwardCarrierFreq, 0, 0, Integer.MAX_VALUE, 1, 0, 5);
		}
		return forwardCarrierFreq;
	}

	/**
	 * This method initializes returnCarrierFreq	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getReturnCarrierFreq() {
		if (returnCarrierFreq == null) {
			returnCarrierFreq = new JSpinner(new SpinnerNumberModel(0.0, 0, Float.MAX_VALUE, 0.1));
			ViewHelper.setupSpinnerNumber(forwardCarrierFreq, 0, 0, Integer.MAX_VALUE, 1, 0, 5);
		}
		return returnCarrierFreq;
	}

	/**
	 * This method initializes pnCode	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getPnCode() {
		if (pnCode == null) {
			pnCode = new JSpinner(new SpinnerNumberModel(0, 0, Short.MAX_VALUE * 2, 1));
		}
		return pnCode;
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
	
	private DataTerminalSetupDialog getThis() {
		return this;
	}
	
}  //  @jve:decl-index=0:visual-constraint="14,12"
