package br.skylight.cucs.plugins.skylightvehicle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.skylight.MiscInfoMessage;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.widgets.VehicleMessageRefreshButton;
import br.skylight.cucs.widgets.VehicleView;

public class MiscInfoView extends VehicleView implements MessageListener {

	@ServiceInjection
	public MessagingService messagingService;

	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="11,14"

	private VehicleMessageRefreshButton refresh = null;

	private JLabel jLabel = null;

	private JLabel cht = null;

	private JLabel jLabel2 = null;

	private JLabel jLabel21 = null;

	private JLabel jLabel211 = null;

	private JLabel jLabel2111 = null;

	private JLabel jLabel21111 = null;

	private JLabel onboard = null;

	private JLabel batt1 = null;

	private JLabel batt2 = null;

	private JLabel generator = null;

	private JLabel vehicleLatency = null;

	private JLabel jLabel1 = null;

	private JLabel controlSource = null;

	private JLabel jLabel211111 = null;

	private JLabel vehicleTxErrors = null;

	private JLabel jLabel211112 = null;

	private JLabel jLabel2111121 = null;

	private JLabel hardwareResetCounter = null;

	private JLabel skippedHwMessagesCounter = null;

	private JLabel jLabel21111211 = null;

	private JLabel jLabel211112111 = null;

	private JLabel adtSentPacketsCounter = null;

	private JLabel adtSentPacketsResultCounter = null;

	public MiscInfoView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMessageListener(MessageType.M2005, this);
	}
	
	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints42 = new GridBagConstraints();
			gridBagConstraints42.gridx = 1;
			gridBagConstraints42.anchor = GridBagConstraints.WEST;
			gridBagConstraints42.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints42.gridy = 11;
			adtSentPacketsResultCounter = new JLabel();
			adtSentPacketsResultCounter.setText("-");
			GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
			gridBagConstraints32.gridx = 1;
			gridBagConstraints32.anchor = GridBagConstraints.WEST;
			gridBagConstraints32.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints32.gridy = 10;
			adtSentPacketsCounter = new JLabel();
			adtSentPacketsCounter.setText("-");
			GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
			gridBagConstraints25.gridx = 0;
			gridBagConstraints25.anchor = GridBagConstraints.EAST;
			gridBagConstraints25.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints25.gridy = 11;
			jLabel211112111 = new JLabel();
			jLabel211112111.setText("ADT sent packets counter (modem):");
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.anchor = GridBagConstraints.EAST;
			gridBagConstraints15.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints15.gridy = 10;
			jLabel21111211 = new JLabel();
			jLabel21111211.setText("ADT sent packets counter (AP):");
			GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
			gridBagConstraints41.gridx = 1;
			gridBagConstraints41.anchor = GridBagConstraints.WEST;
			gridBagConstraints41.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints41.gridy = 9;
			skippedHwMessagesCounter = new JLabel();
			skippedHwMessagesCounter.setText("-");
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.gridx = 1;
			gridBagConstraints31.anchor = GridBagConstraints.WEST;
			gridBagConstraints31.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints31.gridy = 8;
			hardwareResetCounter = new JLabel();
			hardwareResetCounter.setText("-");
			GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
			gridBagConstraints24.gridx = 0;
			gridBagConstraints24.anchor = GridBagConstraints.EAST;
			gridBagConstraints24.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints24.gridy = 9;
			jLabel2111121 = new JLabel();
			jLabel2111121.setText("Skipped hw messages counter:");
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 0;
			gridBagConstraints14.anchor = GridBagConstraints.EAST;
			gridBagConstraints14.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints14.gridy = 8;
			jLabel211112 = new JLabel();
			jLabel211112.setText("Hw reset counter:");
			GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
			gridBagConstraints23.gridx = 1;
			gridBagConstraints23.anchor = GridBagConstraints.WEST;
			gridBagConstraints23.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints23.gridy = 7;
			vehicleTxErrors = new JLabel();
			vehicleTxErrors.setText("-");
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 0;
			gridBagConstraints13.anchor = GridBagConstraints.EAST;
			gridBagConstraints13.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints13.gridy = 7;
			jLabel211111 = new JLabel();
			jLabel211111.setText("Vehicle to VSM transmit errors:");
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.gridx = 1;
			gridBagConstraints22.anchor = GridBagConstraints.WEST;
			gridBagConstraints22.insets = new Insets(8, 5, 5, 0);
			gridBagConstraints22.gridy = 0;
			controlSource = new JLabel();
			controlSource.setText("-");
			controlSource.setFont(new Font("Tahoma", Font.PLAIN, 13));
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.anchor = GridBagConstraints.EAST;
			gridBagConstraints12.insets = new Insets(8, 0, 5, 0);
			gridBagConstraints12.gridy = 0;
			jLabel1 = new JLabel();
			jLabel1.setText("Control source:");
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 1;
			gridBagConstraints11.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints11.anchor = GridBagConstraints.WEST;
			gridBagConstraints11.gridy = 6;
			vehicleLatency = new JLabel();
			vehicleLatency.setText("-");
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 1;
			gridBagConstraints10.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints10.anchor = GridBagConstraints.WEST;
			gridBagConstraints10.gridy = 5;
			generator = new JLabel();
			generator.setText("-");
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 1;
			gridBagConstraints9.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints9.anchor = GridBagConstraints.WEST;
			gridBagConstraints9.gridy = 4;
			batt2 = new JLabel();
			batt2.setText("-");
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 1;
			gridBagConstraints8.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints8.anchor = GridBagConstraints.WEST;
			gridBagConstraints8.gridy = 3;
			batt1 = new JLabel();
			batt1.setText("-");
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 1;
			gridBagConstraints7.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints7.anchor = GridBagConstraints.WEST;
			gridBagConstraints7.gridy = 2;
			onboard = new JLabel();
			onboard.setText("-");
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.anchor = GridBagConstraints.EAST;
			gridBagConstraints6.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints6.gridy = 6;
			jLabel21111 = new JLabel();
			jLabel21111.setText("VSM to Vehicle latency (ms):");
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.anchor = GridBagConstraints.EAST;
			gridBagConstraints5.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints5.gridy = 5;
			jLabel2111 = new JLabel();
			jLabel2111.setText("Generator (V):");
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.anchor = GridBagConstraints.EAST;
			gridBagConstraints4.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints4.gridy = 4;
			jLabel211 = new JLabel();
			jLabel211.setText("Battery 2 (V):");
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.anchor = GridBagConstraints.EAST;
			gridBagConstraints3.insets = new Insets(0, 0, 5, 0);
			gridBagConstraints3.gridy = 3;
			jLabel21 = new JLabel();
			jLabel21.setText("Battery 1 (V):");
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 0;
			gridBagConstraints21.anchor = GridBagConstraints.EAST;
			gridBagConstraints21.insets = new Insets(0, 0, 3, 0);
			gridBagConstraints21.gridy = 2;
			jLabel2 = new JLabel();
			jLabel2.setText("Onboard Temp (celsius):");
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.insets = new Insets(0, 5, 5, 0);
			gridBagConstraints2.anchor = GridBagConstraints.WEST;
			gridBagConstraints2.gridy = 1;
			cht = new JLabel();
			cht.setText("-");
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.insets = new Insets(0, 8, 0, 0);
			gridBagConstraints.anchor = GridBagConstraints.EAST;
			gridBagConstraints.gridy = 1;
			jLabel = new JLabel();
			jLabel.setText("Cilinder Head Temp (celsius):");
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = GridBagConstraints.SOUTHEAST;
			gridBagConstraints1.insets = new Insets(3, 3, 3, 3);
			gridBagConstraints1.gridwidth = 2;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.gridy = 12;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(241, 291));
			contents.add(getRefresh(), gridBagConstraints1);
			contents.add(jLabel, gridBagConstraints);
			contents.add(cht, gridBagConstraints2);
			contents.add(jLabel2, gridBagConstraints21);
			contents.add(jLabel21, gridBagConstraints3);
			contents.add(jLabel211, gridBagConstraints4);
			contents.add(jLabel2111, gridBagConstraints5);
			contents.add(jLabel21111, gridBagConstraints6);
			contents.add(onboard, gridBagConstraints7);
			contents.add(batt1, gridBagConstraints8);
			contents.add(batt2, gridBagConstraints9);
			contents.add(generator, gridBagConstraints10);
			contents.add(vehicleLatency, gridBagConstraints11);
			contents.add(jLabel1, gridBagConstraints12);
			contents.add(controlSource, gridBagConstraints22);
			contents.add(jLabel211111, gridBagConstraints13);
			contents.add(vehicleTxErrors, gridBagConstraints23);
			contents.add(jLabel211112, gridBagConstraints14);
			contents.add(jLabel2111121, gridBagConstraints24);
			contents.add(hardwareResetCounter, gridBagConstraints31);
			contents.add(skippedHwMessagesCounter, gridBagConstraints41);
			contents.add(jLabel21111211, gridBagConstraints15);
			contents.add(jLabel211112111, gridBagConstraints25);
			contents.add(adtSentPacketsCounter, gridBagConstraints32);
			contents.add(adtSentPacketsResultCounter, gridBagConstraints42);
		}
		return contents;
	}

	@Override
	public void onMessageReceived(Message message) {
		//M2005
		if(message instanceof MiscInfoMessage) {
			MiscInfoMessage m = (MiscInfoMessage)message;
			if(isMessageFromCurrentVehicle(m)) {
				cht.setText(m.getChtTemperature()+"");
				onboard.setText(m.getOnboardTemperature()+"");
				batt1.setText(m.getBattery1Voltage()+"");
				batt2.setText(m.getBattery2Voltage()+"");
				generator.setText(m.getGeneratorVoltage()+"");
				vehicleLatency.setText((int)(m.getLinkLatencyTime()*1000)+"");
				vehicleTxErrors.setText(m.getDataTerminalTransmitErrors()+"");
				Color c = SystemColor.control;
				if(m.isManualRCControl()) {
					c = Color.RED;
				}
				controlSource.setBackground(c);
				controlSource.setText(m.isManualRCControl()?"RC Pilot":"Autopilot");
				hardwareResetCounter.setText(m.getNumberOfHardwareResets()+"");
				skippedHwMessagesCounter.setText(m.getNumberOfSkippedHardwareMessages()+"");
				adtSentPacketsCounter.setText(m.getAdtPacketsSentAPCounter() + "");
				adtSentPacketsResultCounter.setText(m.getAdtPacketsSentModemCounter() + "");
				updateGUI();
				getRefresh().notifyFeedback();
			}
		}
	}
	
	@Override
	protected String getBaseTitle() {
		return "Misc Infos";
	}

	@Override
	protected void updateGUI() {
		getRefresh().setVehicle(getCurrentVehicle());
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
			refresh.setup(subscriberService, messagingService, MessageType.M2005);
		}
		return refresh;
	}

}
