package br.skylight.cucs.plugins.systemstatus;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusAlert;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.widgets.VehicleMessageRefreshButton;
import br.skylight.cucs.widgets.VehicleView;

public class SubsystemAlertsConsoleView extends VehicleView implements MessageListener {

	private static final Logger logger = Logger.getLogger(VehicleView.class.getName());
	
	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="11,14"
	private VehicleMessageRefreshButton refresh = null;
	
	@ServiceInjection
	public MessagingService messagingService;

	@ServiceInjection
	public SubscriberService subscriberService;

	@ServiceInjection
	public VehicleControlService vehicleControlService;
	
	private JScrollPane consoleScroll = null;

	private JTextArea console = null;
	
	public SubsystemAlertsConsoleView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	public void onActivate() throws Exception {
		super.onActivate();
		subscriberService.addMessageListener(MessageType.M1100, this);
	}
	
	@Override
	public void onMessageReceived(Message message) {
		if(isMessageFromCurrentVehicle(message)) {
			//M1100
			if(message instanceof SubsystemStatusAlert) {
				updateGUI();
				getRefresh().notifyFeedback();
			}
		}
	}
	
	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.fill = GridBagConstraints.BOTH;
			gridBagConstraints7.gridy = 0;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.weighty = 1.0;
			gridBagConstraints7.gridx = 0;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.weighty = 1.0;
			gridBagConstraints6.fill = GridBagConstraints.BOTH;
			gridBagConstraints6.gridy = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.anchor = GridBagConstraints.SOUTHEAST;
			gridBagConstraints.insets = new Insets(3, 3, 3, 3);
			gridBagConstraints.gridy = 1;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 1;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(210, 149));
			contents.add(getRefresh(), gridBagConstraints);
			contents.add(getConsoleScroll(), gridBagConstraints7);
		}
		return contents;
	}

	@Override
	protected String getBaseTitle() {
		return "Alerts console";
	}

	@Override
	protected void updateGUI() {
		getRefresh().setVehicle(getCurrentVehicle());
		getConsole().setText(getCurrentVehicle().getAlertsConsole());
		getConsole().setCaretPosition(getConsole().getText().length());
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
			refresh.setup(subscriberService, messagingService, MessageType.M1100);
		}
		return refresh;
	}

	/**
	 * This method initializes consoleScroll	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getConsoleScroll() {
		if (consoleScroll == null) {
			consoleScroll = new JScrollPane();
			consoleScroll.setViewportView(getConsole());
		}
		return consoleScroll;
	}

	/**
	 * This method initializes console	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getConsole() {
		if (console == null) {
			console = new JTextArea();
		}
		return console;
	}

}
