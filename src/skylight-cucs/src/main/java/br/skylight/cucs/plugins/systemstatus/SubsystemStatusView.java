package br.skylight.cucs.plugins.systemstatus;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import br.skylight.commons.AlertWrapper;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.enums.AlertPriority;
import br.skylight.commons.dli.enums.Subsystem;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusReport;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusRequest;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.widgets.FeedbackButton;
import br.skylight.cucs.widgets.VehicleView;

public class SubsystemStatusView extends VehicleView implements MessageListener {

	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="11,14"
	private JPanel subsystemsPanel = null;
	private Map<Subsystem,SubsystemStatusButton> buttons;  //  @jve:decl-index=0:
	private FeedbackButton refresh = null;
	
	@ServiceInjection
	public MessagingService messagingService;
	
	public SubsystemStatusView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected void onActivate() throws Exception {
		super.onActivate();
		getButtons();
		subscriberService.addMessageListener(MessageType.M1101, this);
	}

	@Override
	public void onMessageReceived(Message message) {
		//M1101
		if(message instanceof SubsystemStatusReport) {
			updateGUI();
			getRefresh().notifyFeedback();
		}
	}
	
	@Override
	protected JPanel getContents() {
		if(contents==null) {
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
			contents.add(getSubsystemsPanel(), gridBagConstraints6);
		}
		return contents;
	}


	@Override
	protected String getBaseTitle() {
		return "Subsystem Status";
	}

	@Override
	protected void updateGUI() {
		getRefresh().setEnabled(getCurrentVehicle()!=null);
		if(getCurrentVehicle()!=null) {
			for (Entry<Subsystem,SubsystemStatusButton> b : getButtons().entrySet()) {
				b.getValue().setSubsystemState(getCurrentVehicle().getSubsystemStates().get(b.getKey()));
				//show related subsystem alerts
				String t = "<html>";
				for(AlertWrapper aw : getCurrentVehicle().getSubsystemStatusAlerts().values()) {
					if(aw.getSubsystemStatusAlert().getSubsystemID().equals(b.getKey()) && aw.getSubsystemStatusAlert().getPriority().ordinal()>1) {
						t += aw.getSubsystemStatusAlert().getText() + "<br/>";
					}
				}
				t += "</html>";
				b.getValue().setToolTipText(t);
			}
		}
	}

	/**
	 * This method initializes subsystemsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getSubsystemsPanel() {
		if (subsystemsPanel == null) {
			subsystemsPanel = new JPanel();
			subsystemsPanel.setLayout(new FlowLayout());
		}
		return subsystemsPanel;
	}
	
	private Map<Subsystem,SubsystemStatusButton> getButtons() {
		if(buttons==null) {
			buttons = new HashMap<Subsystem,SubsystemStatusButton>();
			for (final Subsystem s : Subsystem.values()) {
				SubsystemStatusButton b = new SubsystemStatusButton();
				b.setBackground(Color.LIGHT_GRAY);
				b.setText(s.getName());
				b.setPreferredSize(new Dimension(120,30));
				b.setRoundness(15);
				b.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						SubsystemStatusRequest m = messagingService.resolveMessageForSending(SubsystemStatusRequest.class);
						m.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
						m.setSubsystem(s);
						messagingService.sendMessage(m);
					}
				});
				buttons.put(s, b);
				getSubsystemsPanel().add(b, null);
			}
		}
		return buttons;
	}

	/**
	 * This method initializes refresh	
	 * 	
	 * @return br.skylight.cucs.widgets.FeedbackButton	
	 */
	private FeedbackButton getRefresh() {
		if (refresh == null) {
			refresh = new FeedbackButton();
			refresh.setToolTipText("Refresh data");
			refresh.setMargin(ViewHelper.getMinimalButtonMargin());
			refresh.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/refresh.gif")));
			refresh.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					for (Subsystem s : Subsystem.values()) {
						SubsystemStatusRequest m = messagingService.resolveMessageForSending(SubsystemStatusRequest.class);
						m.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
						m.setSubsystem(s);
						messagingService.sendMessage(m);
					}
				}
			});
		}
		return refresh;
	}

}
