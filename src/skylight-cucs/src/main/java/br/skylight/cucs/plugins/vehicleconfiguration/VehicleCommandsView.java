package br.skylight.cucs.plugins.vehicleconfiguration;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.vehicle.VehicleConfigurationCommand;
import br.skylight.commons.plugin.PluginManager;
import br.skylight.commons.plugin.annotations.ExtensionPointsInjection;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.gamecontroller.GameControllerService;
import br.skylight.cucs.widgets.JMeasureSpinner;
import br.skylight.cucs.widgets.VehicleView;

public class VehicleCommandsView extends VehicleView implements MessageListener {

	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="11,14"
	
	@ServiceInjection
	public MessagingService messagingService;

	@ServiceInjection
	public GameControllerService gameControllerService;

	@ServiceInjection
	public PluginManager pluginManager;

	@ExtensionPointsInjection
	public List<VehicleCommandsSectionExtensionPoint> sectionImpls;

	private List<JPanel> visibleExtTabs = new CopyOnWriteArrayList<JPanel>();  //  @jve:decl-index=0:
	
	private JPanel fuel = null;

	private JLabel jLabel = null;

	private JMeasureSpinner<Integer> propulsionEnergy = null;

	private JButton uploadButton = null;
	
	public VehicleCommandsView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected void onActivate() throws Exception {
		super.onActivate();
	}

	@Override
	public void onMessageReceived(Message message) {
	}

	@Override
	protected void updateGUI() {
		getPropulsionEnergy().setEnabled(getCurrentVehicle()!=null);
		getUploadButton().setEnabled(getCurrentVehicle()!=null);
		for (VehicleCommandsSectionExtensionPoint ep : sectionImpls) {
			if(ep.updateVehicle(getCurrentVehicle())) {
				if(!visibleExtTabs.contains(ep.getSectionComponent())) {
					getContents().add(ep.getSectionName(), ep.getSectionComponent());
					visibleExtTabs.add(ep.getSectionComponent());
				}
			} else {
				getContents().remove(ep.getSectionComponent());
			}
		}
	}
	
	@Override
	protected JPanel getContents() {
		if(contents==null) {
			contents = new JPanel();
			contents.setLayout(new BoxLayout(getContents(), BoxLayout.Y_AXIS));
			contents.setSize(new Dimension(251, 139));
			contents.add(getFuel(), null);
		}
		return contents;
	}

	@Override
	protected String getBaseTitle() {
		return "Vehicle Commands";
	}

	protected VehicleCommandsView getThis() {
		return this;
	}

	/**
	 * This method initializes fuel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getFuel() {
		if (fuel == null) {
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.gridx = 2;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.insets = new Insets(0, 3, 0, 0);
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.gridx = 1;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			jLabel = new JLabel();
			jLabel.setText("Initial propulsion energy (%):");
			fuel = new JPanel();
			fuel.setLayout(new GridBagLayout());
			fuel.add(jLabel, gridBagConstraints1);
			fuel.add(getPropulsionEnergy(), gridBagConstraints2);
			fuel.add(getUploadButton(), gridBagConstraints3);
		}
		return fuel;
	}

	/**
	 * This method initializes propulsionEnergy	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JMeasureSpinner<Integer> getPropulsionEnergy() {
		if (propulsionEnergy == null) {
			propulsionEnergy = new JMeasureSpinner<Integer>(null, 50, 0, 100, 1, 0, 0);
			propulsionEnergy.setPreferredSize(new Dimension(40, 20));
		}
		return propulsionEnergy;
	}

	/**
	 * This method initializes uploadButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getUploadButton() {
		if (uploadButton == null) {
			uploadButton = new JButton();
			uploadButton.setToolTipText("Set initial propulsion energy");
			uploadButton.setIcon(new ImageIcon(getClass().getResource("/br/skylight/cucs/images/upload.gif")));
			uploadButton.setMargin(ViewHelper.getMinimalButtonMargin());
			uploadButton.setPreferredSize(new Dimension(20, 20));
			uploadButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					VehicleConfigurationCommand m = messagingService.resolveMessageForSending(VehicleConfigurationCommand.class);
					m.setInitialPropulsionEnergy(getPropulsionEnergy().getValue());
					m.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
					messagingService.sendMessage(m);
				}
			});
		}
		return uploadButton;
	}

}
