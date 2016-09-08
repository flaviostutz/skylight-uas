package br.skylight.cucs.plugins.skylightvehicle;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import br.skylight.commons.Vehicle;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.enums.VehicleType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.dli.skylight.PIDControllerState;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.plugins.skylightvehicle.vehiclecontrol.SkylightVehicle;
import br.skylight.cucs.plugins.skylightvehicle.vehiclecontrol.SkylightVehicleControlService;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.plugins.vehicleconfiguration.VehicleConfigurationSectionExtensionPoint;
import br.skylight.cucs.widgets.FeedbackButton;
import br.skylight.cucs.widgets.VehicleMessageRefreshButton;

@ExtensionPointImplementation(extensionPointDefinition=VehicleConfigurationSectionExtensionPoint.class)
public class VehicleConfigurationPIDExtensionPointImpl extends Worker implements VehicleConfigurationSectionExtensionPoint, MessageListener {

	private JPanel sectionComponent;  //  @jve:decl-index=0:visual-constraint="10,52"
	private Vehicle currentVehicle;  //  @jve:decl-index=0:
	private SkylightVehicle currentSkylightVehicle;  //  @jve:decl-index=0:
	
	private JScrollPane scrollPanel = null;
	private JPanel widgetsPanel = null;
	private VehicleMessageRefreshButton refresh = null;
	private List<PIDHoldControllerWidget> widgets = new ArrayList<PIDHoldControllerWidget>();  //  @jve:decl-index=0:

	@ServiceInjection
	public MessagingService messagingService;
	
	@ServiceInjection
	public SubscriberService subscriberService;

	@ServiceInjection
	public SkylightVehicleControlService skylightVehicleControlService;

	@ServiceInjection
	public PluginManager pluginManager;
	private FeedbackButton uploadAll = null;
	
	@Override
	public JPanel getSectionComponent() {
		if(sectionComponent==null) {
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.anchor = GridBagConstraints.WEST;
			gridBagConstraints11.gridy = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.BOTH;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.weighty = 1.0;
			gridBagConstraints1.gridwidth = 2;
			gridBagConstraints1.gridx = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.anchor = GridBagConstraints.EAST;
			gridBagConstraints.gridy = 1;
			sectionComponent = new JPanel();
			sectionComponent.setLayout(new GridBagLayout());
			sectionComponent.setSize(new Dimension(172, 109));
			sectionComponent.add(getRefresh(), gridBagConstraints);
			sectionComponent.add(getScrollPanel(), gridBagConstraints1);
			sectionComponent.add(getUploadAll(), gridBagConstraints11);
		}
		return sectionComponent;
	}

	@Override
	public void onActivate() throws Exception {
		subscriberService.addMessageListener(MessageType.M2011, this);
	}
	
	@Override
	public void onDeactivate() throws Exception {
		for (Component c : getWidgetsPanel().getComponents()) {
			if(c instanceof PIDHoldControllerWidget) {
				PIDHoldControllerWidget pc = (PIDHoldControllerWidget)c;
				pluginManager.unmanageObject(pc);
			}
		}
	}

	@Override
	public void onMessageReceived(Message message) {
		if(currentVehicle!=null 
			&& message.getVehicleID()==currentVehicle.getVehicleID().getVehicleID()) {
			//M2011
			if(message instanceof PIDControllerState) {
//				PIDControllerState m = (PIDControllerState)message;
				for (Component c : getWidgetsPanel().getComponents()) {
					if(c instanceof PIDHoldControllerWidget) {
						PIDHoldControllerWidget pc = (PIDHoldControllerWidget)c;
						pc.onMessageReceived(message);
					}
				}
				getRefresh().notifyFeedback();
			}
		}
	}
	
	@Override
	public String getSectionName() {
		return "PIDs";
	}

	@Override
	public boolean updateVehicle(Vehicle vehicle) {
		currentVehicle = vehicle;
		currentSkylightVehicle = skylightVehicleControlService.resolveSkylightVehicle(vehicle.getVehicleID().getVehicleID());
		for (Component c : getWidgetsPanel().getComponents()) {
			if(c instanceof PIDHoldControllerWidget) {
				PIDHoldControllerWidget pc = (PIDHoldControllerWidget)c;
				pc.setSkylightVehicle(currentSkylightVehicle);
				pc.updateGUI();
			}
		}
		getRefresh().setVehicle(vehicle);
		return vehicle!=null && vehicle.getVehicleID()!=null && vehicle.getVehicleID().getVehicleType().equals(VehicleType.TYPE_60);
	}

	/**
	 * This method initializes scrollPanel	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getScrollPanel() {
		if (scrollPanel == null) {
			scrollPanel = new JScrollPane();
			scrollPanel.setViewportView(getWidgetsPanel());
		}
		return scrollPanel;
	}

	/**
	 * This method initializes widgetsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getWidgetsPanel() {
		if (widgetsPanel == null) {
			widgetsPanel = new JPanel();
			widgetsPanel.setLayout(new BoxLayout(widgetsPanel, BoxLayout.Y_AXIS));
			//create PID panel elements
			for (PIDControl pc : PIDControl.values()) {
				PIDHoldControllerWidget w = new PIDHoldControllerWidget();
				w.getHoldControllerWidget().setMeasureType(pc.getSetpointMeasureType());
				pluginManager.manageObject(w);
				w.setPidControl(pc);
				widgetsPanel.add(w);
				widgets.add(w);
			}
		}
		return widgetsPanel;
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
			refresh.setup(subscriberService, messagingService, MessageType.M2011, MessageType.M2010);
		}
		return refresh;
	}

	/**
	 * This method initializes uploadAll	
	 * 	
	 * @return br.skylight.cucs.widgets.FeedbackButton	
	 */
	private FeedbackButton getUploadAll() {
		if (uploadAll == null) {
			uploadAll = new FeedbackButton();
			uploadAll.setToolTipText("Upload all PID configurations");
			uploadAll.setMargin(ViewHelper.getMinimalButtonMargin());
			uploadAll.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/upload.gif")));
			uploadAll.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					for(PIDHoldControllerWidget w : widgets) {
						w.getSetButton().doClick();
					}
				}
			});
		}
		return uploadAll;
	}

}
