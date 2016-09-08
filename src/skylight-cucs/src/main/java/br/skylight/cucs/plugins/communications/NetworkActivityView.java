package br.skylight.cucs.plugins.communications;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.Serializable;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;

import br.skylight.commons.StringHelper;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.View;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;

public class NetworkActivityView extends View {

	@ServiceInjection
	public MessagingService messagingService;
	
	private final NumberFormat nf = NumberFormat.getInstance();  //  @jve:decl-index=0:
	
	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="20,-18"
	
	private ThreadWorker guiRefresher;

	private JLabel jLabel = null;

	private JLabel jLabel1 = null;

	private JLabel receivedText = null;

	private JLabel sentText = null;

	private JLabel jLabel2 = null;

	private JLabel jLabel21 = null;

	private JLabel packetsReceivedText = null;

	private JLabel packetsSentText = null;

	private JLabel jLabel3 = null;

	private JLabel jLabel4 = null;

	private JLabel jLabel22 = null;

	private JLabel inputErrorsText = null;

	private JLabel jLabel221 = null;

	private JLabel inputIdleTimeText = null;

	private JLabel jLabel2211 = null;

	private JLabel outputIdleTimeText = null;

	private JLabel jLabel222 = null;

	private JLabel outputErrorsText = null;

	private JLabel jLabel5 = null;

	private JLabel vsmLatencyText = null;

	public NetworkActivityView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
		nf.setMaximumFractionDigits(1);
		nf.setMinimumFractionDigits(1);
		guiRefresher = new ThreadWorker(2) {
			@Override
			public void onActivate() throws Exception {
				setName("NetworkActivityView.guiRefresher");
			}
			@Override
			public void step() throws Exception {
				updateGUI();
			}
		};
		setTitleText("Network Activity");
	}

	protected void updateGUI() {
		if(messagingService.getDataTerminal()!=null) {
			receivedText.setText(formatStats(messagingService.getDataTerminal().getInputRate(), messagingService.getDataTerminal().getTotalBytesReceived()));
			packetsReceivedText.setText(messagingService.getDataTerminal().getTotalPacketsReceived() + " (" + (int)messagingService.getDataTerminal().getPacketsReceivedRate() + " pkt/s)");
			inputErrorsText.setText(messagingService.getInputErrors() + "");
			inputIdleTimeText.setText(StringHelper.formatElapsedTime((long)messagingService.getDataTerminal().getTimeSinceLastPacketReceived()) + (messagingService.getLastMessageReceived()!=null?" Last: #" + messagingService.getLastMessageReceived().getMessageType().getNumber():""));
			vsmLatencyText.setText((int)(messagingService.getLastLatencyTime()*1000.0)+ " ms");
			
			sentText.setText(formatStats(messagingService.getDataTerminal().getOutputRate(), messagingService.getDataTerminal().getTotalBytesSent()));
			packetsSentText.setText(messagingService.getDataTerminal().getTotalPacketsSent() + " (" + (int)messagingService.getDataTerminal().getPacketsSentRate() + " pkt/s)");
			outputErrorsText.setText(messagingService.getOutputErrors() + "");
			outputIdleTimeText.setText(StringHelper.formatElapsedTime((long)messagingService.getDataTerminal().getTimeSinceLastPacketSent()) + (messagingService.getLastMessageSent()!=null?" Last: #" + messagingService.getLastMessageSent().getMessageType().getNumber():""));
		}
	}

	private String formatStats(float dataRate, long totalBytes) {
		String totalStr =  nf.format((float)totalBytes/1024F) + " KB";
		if(totalBytes>1048576) {
			totalStr =  nf.format((float)totalBytes/1048576F) + " MB";
		}
		String dataRateStr = (int)dataRate + " B/s";
		if(dataRate>1024) {
			dataRateStr =  nf.format(dataRate/1024) + " KB/s";
		}
		return totalStr + " (" + dataRateStr + ")";
	}

	@Override
	protected void onActivate() throws Exception {
//		messagingService.getDataTerminal().setStatisticsEnabled(true);
		guiRefresher.activate();
	}
	
	@Override
	protected void onDeactivate() throws Exception {
		guiRefresher.deactivate();
//		messagingService.getDataTerminal().setStatisticsEnabled(false);
	}

	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
			gridBagConstraints26.gridx = 1;
			gridBagConstraints26.anchor = GridBagConstraints.WEST;
			gridBagConstraints26.insets = new Insets(2, 4, 0, 0);
			gridBagConstraints26.gridy = 5;
			vsmLatencyText = new JLabel();
			vsmLatencyText.setText("0");
			GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
			gridBagConstraints25.gridx = 0;
			gridBagConstraints25.anchor = GridBagConstraints.EAST;
			gridBagConstraints25.insets = new Insets(2, 0, 0, 0);
			gridBagConstraints25.gridy = 3;
			jLabel5 = new JLabel();
			jLabel5.setText("Latency:");
			GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
			gridBagConstraints24.gridx = 1;
			gridBagConstraints24.anchor = GridBagConstraints.WEST;
			gridBagConstraints24.insets = new Insets(2, 4, 0, 0);
			gridBagConstraints24.gridy = 11;
			outputErrorsText = new JLabel();
			outputErrorsText.setText("0");
			GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
			gridBagConstraints23.gridx = 0;
			gridBagConstraints23.anchor = GridBagConstraints.EAST;
			gridBagConstraints23.insets = new Insets(2, 0, 0, 0);
			gridBagConstraints23.gridy = 11;
			jLabel222 = new JLabel();
			jLabel222.setText("Errors:");
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.gridx = 1;
			gridBagConstraints22.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints22.insets = new Insets(2, 4, 0, 0);
			gridBagConstraints22.gridy = 12;
			outputIdleTimeText = new JLabel();
			outputIdleTimeText.setText("0");
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 0;
			gridBagConstraints21.anchor = GridBagConstraints.NORTHEAST;
			gridBagConstraints21.insets = new Insets(2, 0, 0, 0);
			gridBagConstraints21.weighty = 1.0;
			gridBagConstraints21.gridy = 12;
			jLabel2211 = new JLabel();
			jLabel2211.setText("Idle time:");
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			gridBagConstraints20.gridx = 1;
			gridBagConstraints20.anchor = GridBagConstraints.WEST;
			gridBagConstraints20.insets = new Insets(2, 4, 0, 0);
			gridBagConstraints20.gridy = 3;
			inputIdleTimeText = new JLabel();
			inputIdleTimeText.setText("0");
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			gridBagConstraints19.gridx = 0;
			gridBagConstraints19.anchor = GridBagConstraints.EAST;
			gridBagConstraints19.insets = new Insets(2, 0, 0, 0);
			gridBagConstraints19.gridy = 5;
			jLabel221 = new JLabel();
			jLabel221.setText("Idle time:");
			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
			gridBagConstraints18.gridx = 1;
			gridBagConstraints18.anchor = GridBagConstraints.WEST;
			gridBagConstraints18.insets = new Insets(2, 4, 0, 0);
			gridBagConstraints18.gridy = 4;
			inputErrorsText = new JLabel();
			inputErrorsText.setText("0");
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.gridx = 0;
			gridBagConstraints17.anchor = GridBagConstraints.EAST;
			gridBagConstraints17.insets = new Insets(2, 10, 0, 0);
			gridBagConstraints17.gridy = 4;
			jLabel22 = new JLabel();
			jLabel22.setText("Errors:");
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.anchor = GridBagConstraints.WEST;
			gridBagConstraints12.insets = new Insets(5, 8, 0, 0);
			gridBagConstraints12.gridy = 8;
			jLabel4 = new JLabel();
			jLabel4.setFont(new Font("Arial", Font.BOLD, 13));
			jLabel4.setText("Output");
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.anchor = GridBagConstraints.WEST;
			gridBagConstraints11.insets = new Insets(8, 8, 0, 0);
			gridBagConstraints11.gridy = 0;
			jLabel3 = new JLabel();
			jLabel3.setFont(new Font("Arial", Font.BOLD, 13));
			jLabel3.setText("Input");
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 1;
			gridBagConstraints10.anchor = GridBagConstraints.WEST;
			gridBagConstraints10.insets = new Insets(2, 4, 0, 0);
			gridBagConstraints10.gridy = 10;
			packetsSentText = new JLabel();
			packetsSentText.setText("0");
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 1;
			gridBagConstraints9.anchor = GridBagConstraints.WEST;
			gridBagConstraints9.insets = new Insets(0, 4, 0, 0);
			gridBagConstraints9.gridy = 2;
			packetsReceivedText = new JLabel();
			packetsReceivedText.setText("0");
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.anchor = GridBagConstraints.EAST;
			gridBagConstraints8.insets = new Insets(2, 0, 0, 0);
			gridBagConstraints8.gridy = 10;
			jLabel21 = new JLabel();
			jLabel21.setText("Packets sent:");
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.anchor = GridBagConstraints.EAST;
			gridBagConstraints7.insets = new Insets(2, 10, 0, 0);
			gridBagConstraints7.gridy = 2;
			jLabel2 = new JLabel();
			jLabel2.setText("Packets received:");
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.insets = new Insets(3, 4, 0, 10);
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.anchor = GridBagConstraints.WEST;
			gridBagConstraints3.gridy = 9;
			sentText = new JLabel();
			sentText.setText("0");
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.insets = new Insets(0, 4, 0, 10);
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.anchor = GridBagConstraints.WEST;
			gridBagConstraints2.gridy = 1;
			receivedText = new JLabel();
			receivedText.setText("0");
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			gridBagConstraints1.insets = new Insets(2, 10, 0, 0);
			gridBagConstraints1.gridy = 9;
			jLabel1 = new JLabel();
			jLabel1.setText("Bytes sent:");
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.anchor = GridBagConstraints.EAST;
			gridBagConstraints.insets = new Insets(2, 10, 0, 0);
			gridBagConstraints.gridy = 1;
			jLabel = new JLabel();
			jLabel.setText("Bytes received:");
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(175, 187));
			contents.add(jLabel, gridBagConstraints);
			contents.add(jLabel1, gridBagConstraints1);
			contents.add(receivedText, gridBagConstraints2);
			contents.add(sentText, gridBagConstraints3);
			contents.add(jLabel2, gridBagConstraints7);
			contents.add(jLabel21, gridBagConstraints8);
			contents.add(packetsReceivedText, gridBagConstraints9);
			contents.add(packetsSentText, gridBagConstraints10);
			contents.add(jLabel3, gridBagConstraints11);
			contents.add(jLabel4, gridBagConstraints12);
			contents.add(jLabel22, gridBagConstraints17);
			contents.add(inputErrorsText, gridBagConstraints18);
			contents.add(jLabel221, gridBagConstraints19);
			contents.add(inputIdleTimeText, gridBagConstraints26);
			contents.add(jLabel2211, gridBagConstraints21);
			contents.add(outputIdleTimeText, gridBagConstraints22);
			contents.add(jLabel222, gridBagConstraints23);
			contents.add(outputErrorsText, gridBagConstraints24);
			contents.add(jLabel5, gridBagConstraints25);
			contents.add(vsmLatencyText, gridBagConstraints20);
		}
		return contents;
	}

	@Override
	protected Serializable instantiateState() {
		return null;
	}

	@Override
	protected void onStateUpdated() {
	}

	@Override
	protected void prepareState() {
	}

}
