package br.skylight.cucs.plugins.communications;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import br.skylight.commons.StringHelper;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.datalink.DataLinkStatusReport;
import javax.swing.WindowConstants;

public class DataTerminalInfoDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JLabel jLabel3 = null;
	private JLabel jLabel4 = null;
	private JLabel jLabel5 = null;
	private JLabel jLabel6 = null;
	private JLabel jLabel7 = null;
	private JLabel jLabel8 = null;
	private JLabel jLabel9 = null;
	private JLabel jLabel10 = null;
	private JLabel type = null;
	private JLabel linkId = null;
	private JLabel linkState = null;
	private JLabel channelNumber = null;
	private JLabel antennaState = null;
	private JLabel channelPriority = null;
	private JLabel primaryHopPattern = null;
	private JLabel forwardCarrierFreq = null;
	private JLabel returnCarrierFreq = null;
	private JLabel securityState = null;
	private JLabel downlinkStatus = null;
	private JButton jButton = null;
	/**
	 * @param owner
	 */
	public DataTerminalInfoDialog(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(231, 262);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setTitle("Data terminal Info");
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			gridBagConstraints19.gridx = 0;
			gridBagConstraints19.gridwidth = 2;
			gridBagConstraints19.gridy = 11;
			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
			gridBagConstraints18.gridx = 1;
			gridBagConstraints18.insets = new Insets(3, 5, 5, 0);
			gridBagConstraints18.weightx = 1.0;
			gridBagConstraints18.anchor = GridBagConstraints.WEST;
			gridBagConstraints18.gridy = 10;
			downlinkStatus = new JLabel();
			downlinkStatus.setText("-");
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.gridx = 1;
			gridBagConstraints17.insets = new Insets(3, 5, 0, 0);
			gridBagConstraints17.weightx = 1.0;
			gridBagConstraints17.anchor = GridBagConstraints.WEST;
			gridBagConstraints17.gridy = 9;
			securityState = new JLabel();
			securityState.setText("-");
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.gridx = 1;
			gridBagConstraints16.insets = new Insets(3, 5, 0, 0);
			gridBagConstraints16.weightx = 1.0;
			gridBagConstraints16.anchor = GridBagConstraints.WEST;
			gridBagConstraints16.gridy = 8;
			returnCarrierFreq = new JLabel();
			returnCarrierFreq.setText("-");
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 1;
			gridBagConstraints15.insets = new Insets(3, 5, 0, 0);
			gridBagConstraints15.weightx = 1.0;
			gridBagConstraints15.anchor = GridBagConstraints.WEST;
			gridBagConstraints15.gridy = 7;
			forwardCarrierFreq = new JLabel();
			forwardCarrierFreq.setText("-");
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 1;
			gridBagConstraints14.insets = new Insets(3, 5, 0, 0);
			gridBagConstraints14.weightx = 1.0;
			gridBagConstraints14.anchor = GridBagConstraints.WEST;
			gridBagConstraints14.gridy = 6;
			primaryHopPattern = new JLabel();
			primaryHopPattern.setText("-");
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 1;
			gridBagConstraints13.insets = new Insets(3, 5, 0, 0);
			gridBagConstraints13.weightx = 1.0;
			gridBagConstraints13.anchor = GridBagConstraints.WEST;
			gridBagConstraints13.gridy = 5;
			channelPriority = new JLabel();
			channelPriority.setText("-");
			GridBagConstraints gridBagConstraints121 = new GridBagConstraints();
			gridBagConstraints121.gridx = 1;
			gridBagConstraints121.insets = new Insets(3, 5, 0, 0);
			gridBagConstraints121.weightx = 1.0;
			gridBagConstraints121.anchor = GridBagConstraints.WEST;
			gridBagConstraints121.gridy = 3;
			antennaState = new JLabel();
			antennaState.setText("-");
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 1;
			gridBagConstraints11.insets = new Insets(3, 5, 0, 0);
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.anchor = GridBagConstraints.WEST;
			gridBagConstraints11.gridy = 4;
			channelNumber = new JLabel();
			channelNumber.setText("-");
			GridBagConstraints gridBagConstraints101 = new GridBagConstraints();
			gridBagConstraints101.gridx = 1;
			gridBagConstraints101.insets = new Insets(3, 5, 0, 0);
			gridBagConstraints101.weightx = 1.0;
			gridBagConstraints101.anchor = GridBagConstraints.WEST;
			gridBagConstraints101.gridy = 2;
			linkState = new JLabel();
			linkState.setText("-");
			GridBagConstraints gridBagConstraints91 = new GridBagConstraints();
			gridBagConstraints91.gridx = 1;
			gridBagConstraints91.insets = new Insets(5, 5, 0, 0);
			gridBagConstraints91.weightx = 1.0;
			gridBagConstraints91.anchor = GridBagConstraints.WEST;
			gridBagConstraints91.gridy = 0;
			linkId = new JLabel();
			linkId.setText("-");
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 1;
			gridBagConstraints12.insets = new Insets(3, 5, 0, 0);
			gridBagConstraints12.weightx = 1.0;
			gridBagConstraints12.anchor = GridBagConstraints.WEST;
			gridBagConstraints12.gridy = 1;
			type = new JLabel();
			type.setText("-");
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 0;
			gridBagConstraints10.anchor = GridBagConstraints.EAST;
			gridBagConstraints10.insets = new Insets(3, 0, 5, 0);
			gridBagConstraints10.weightx = 1.0;
			gridBagConstraints10.gridy = 10;
			jLabel10 = new JLabel();
			jLabel10.setText("Downlink status:");
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 0;
			gridBagConstraints9.anchor = GridBagConstraints.EAST;
			gridBagConstraints9.insets = new Insets(3, 0, 0, 0);
			gridBagConstraints9.weightx = 1.0;
			gridBagConstraints9.gridy = 9;
			jLabel9 = new JLabel();
			jLabel9.setText("Security state:");
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.anchor = GridBagConstraints.EAST;
			gridBagConstraints8.insets = new Insets(3, 0, 0, 0);
			gridBagConstraints8.weightx = 1.0;
			gridBagConstraints8.gridy = 8;
			jLabel8 = new JLabel();
			jLabel8.setText("Return carrier freq:");
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.anchor = GridBagConstraints.EAST;
			gridBagConstraints7.insets = new Insets(3, 0, 0, 0);
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.gridy = 7;
			jLabel7 = new JLabel();
			jLabel7.setText("Forward carrier freq:");
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.anchor = GridBagConstraints.EAST;
			gridBagConstraints6.insets = new Insets(3, 0, 0, 0);
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.gridy = 6;
			jLabel6 = new JLabel();
			jLabel6.setText("Prim. hop pattern:");
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.anchor = GridBagConstraints.EAST;
			gridBagConstraints5.insets = new Insets(3, 0, 0, 0);
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.gridy = 5;
			jLabel5 = new JLabel();
			jLabel5.setText("Channel priority:");
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.anchor = GridBagConstraints.EAST;
			gridBagConstraints4.insets = new Insets(3, 0, 0, 0);
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.gridy = 4;
			jLabel4 = new JLabel();
			jLabel4.setText("Channel number:");
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.anchor = GridBagConstraints.EAST;
			gridBagConstraints3.insets = new Insets(3, 0, 0, 0);
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.gridy = 3;
			jLabel3 = new JLabel();
			jLabel3.setText("Antenna state:");
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.anchor = GridBagConstraints.EAST;
			gridBagConstraints2.insets = new Insets(3, 0, 0, 0);
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.gridy = 2;
			jLabel2 = new JLabel();
			jLabel2.setText("Link state:");
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			gridBagConstraints1.insets = new Insets(3, 0, 0, 0);
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.gridy = 1;
			jLabel1 = new JLabel();
			jLabel1.setText("Type:");
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.anchor = GridBagConstraints.EAST;
			gridBagConstraints.insets = new Insets(5, 0, 0, 0);
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.gridy = 0;
			jLabel = new JLabel();
			jLabel.setText("Data link Id:");
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(jLabel, gridBagConstraints);
			jContentPane.add(jLabel1, gridBagConstraints1);
			jContentPane.add(jLabel2, gridBagConstraints2);
			jContentPane.add(jLabel3, gridBagConstraints3);
			jContentPane.add(jLabel4, gridBagConstraints4);
			jContentPane.add(jLabel5, gridBagConstraints5);
			jContentPane.add(jLabel6, gridBagConstraints6);
			jContentPane.add(jLabel7, gridBagConstraints7);
			jContentPane.add(jLabel8, gridBagConstraints8);
			jContentPane.add(jLabel9, gridBagConstraints9);
			jContentPane.add(jLabel10, gridBagConstraints10);
			jContentPane.add(type, gridBagConstraints12);
			jContentPane.add(linkId, gridBagConstraints91);
			jContentPane.add(linkState, gridBagConstraints101);
			jContentPane.add(channelNumber, gridBagConstraints11);
			jContentPane.add(antennaState, gridBagConstraints121);
			jContentPane.add(channelPriority, gridBagConstraints13);
			jContentPane.add(primaryHopPattern, gridBagConstraints14);
			jContentPane.add(forwardCarrierFreq, gridBagConstraints15);
			jContentPane.add(returnCarrierFreq, gridBagConstraints16);
			jContentPane.add(securityState, gridBagConstraints17);
			jContentPane.add(downlinkStatus, gridBagConstraints18);
			jContentPane.add(getJButton(), gridBagConstraints19);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("Close");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
				}
			});
		}
		return jButton;
	}
	
	public void showInfo(DataLinkStatusReport sr) {
		linkId.setText(StringHelper.formatId(sr.getDataLinkId()));
		type.setText(sr.getAddressedTerminal().name());
		linkState.setText(sr.getDataLinkState().getName());
		antennaState.setText(sr.getAntennaState().name());
		channelNumber.setText(sr.getReportedChannel()+"");
		channelPriority.setText(sr.getLinkChannelPriorityState().name());
		primaryHopPattern.setText(sr.getReportedPrimaryHopPattern()+"");
		forwardCarrierFreq.setText(StringHelper.formatNumber((double)sr.getReportedForwardLinkCarrierFreq(),4));
		returnCarrierFreq.setText(StringHelper.formatNumber((double)sr.getReportedReturnLinkCarrierFreq(),4));
		securityState.setText(sr.getCommunicationSecurityState().name());
		downlinkStatus.setText(sr.getDownlinkStatus() + "%");
		ViewHelper.centerWindow(this);
		setVisible(true);
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
