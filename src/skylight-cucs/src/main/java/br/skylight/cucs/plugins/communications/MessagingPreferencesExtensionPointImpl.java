package br.skylight.cucs.plugins.communications;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.RankedNetworkInterface;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.services.StorageService;
import br.skylight.cucs.plugins.preferences.PreferencesSectionExtensionPoint;

@ExtensionPointImplementation(extensionPointDefinition=PreferencesSectionExtensionPoint.class)
public class MessagingPreferencesExtensionPointImpl extends Worker implements PreferencesSectionExtensionPoint {

	private MessagingPreferencesState preferences;  //  @jve:decl-index=0:
	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="10,52"
	
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JLabel jLabel3 = null;
	private JComboBox networkInterface = null;
	private JTextField multicastAddress = null;
	private JSpinner outputPort = null;
	private JSpinner inputPort = null;

	@ServiceInjection
	public StorageService storageService;

	@ServiceInjection
	public MessagingService messagingService;
	
	@Override
	public JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 1;
			gridBagConstraints21.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints21.insets = new Insets(0, 0, 3, 5);
			gridBagConstraints21.anchor = GridBagConstraints.WEST;
			gridBagConstraints21.gridy = 3;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 1;
			gridBagConstraints11.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints11.insets = new Insets(0, 0, 3, 5);
			gridBagConstraints11.anchor = GridBagConstraints.WEST;
			gridBagConstraints11.gridy = 2;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.fill = GridBagConstraints.BOTH;
			gridBagConstraints5.gridy = 1;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.insets = new Insets(0, 0, 3, 5);
			gridBagConstraints5.anchor = GridBagConstraints.WEST;
			gridBagConstraints5.gridx = 1;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.gridy = 0;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.insets = new Insets(0, 0, 3, 5);
			gridBagConstraints4.gridx = 1;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.anchor = GridBagConstraints.EAST;
			gridBagConstraints3.insets = new Insets(0, 5, 3, 5);
			gridBagConstraints3.gridy = 3;
			jLabel3 = new JLabel();
			jLabel3.setText("Input port:");
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.anchor = GridBagConstraints.EAST;
			gridBagConstraints2.insets = new Insets(0, 5, 3, 5);
			gridBagConstraints2.gridy = 2;
			jLabel2 = new JLabel();
			jLabel2.setText("Output port:");
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			gridBagConstraints1.insets = new Insets(0, 5, 3, 5);
			gridBagConstraints1.gridy = 1;
			jLabel1 = new JLabel();
			jLabel1.setText("Multicast address:");
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.anchor = GridBagConstraints.EAST;
			gridBagConstraints.insets = new Insets(0, 5, 3, 5);
			gridBagConstraints.gridy = 0;
			jLabel = new JLabel();
			jLabel.setText("Network interface:");
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(213, 112));
			contents.add(jLabel, gridBagConstraints);
			contents.add(jLabel1, gridBagConstraints1);
			contents.add(jLabel2, gridBagConstraints2);
			contents.add(jLabel3, gridBagConstraints3);
			contents.add(getNetworkInterface(), gridBagConstraints4);
			contents.add(getMulticastAddress(), gridBagConstraints5);
			contents.add(getOutputPort(), gridBagConstraints11);
			contents.add(getInputPort(), gridBagConstraints21);
		}
		load();
		return contents;
	}

	@Override
	public String getName() {
		return "Network";
	}

	@Override
	public int getOrder() {
		return 0;
	}

	/**
	 * This method initializes networkInterface	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getNetworkInterface() {
		if (networkInterface == null) {
			networkInterface = new JComboBox();
			DefaultComboBoxModel model = new DefaultComboBoxModel();
			for(RankedNetworkInterface ni : IOHelper.getRankedNetworkInterfaces()) {
				if(ni.getScore()>0) {
					model.addElement(new NetworkInterfaceWrapper(ni.getNetworkInterface()));
				}
			}
			networkInterface.setModel(model);
		}
		return networkInterface;
	}

	/**
	 * This method initializes multicastAddress	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMulticastAddress() {
		if (multicastAddress == null) {
			multicastAddress = new JTextField();
			multicastAddress.setPreferredSize(new Dimension(75, 20));
		}
		return multicastAddress;
	}

	/**
	 * This method initializes outputPort	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getOutputPort() {
		if (outputPort == null) {
			outputPort = new JSpinner(new SpinnerNumberModel());
			outputPort.setPreferredSize(new Dimension(75, 20));
		}
		return outputPort;
	}

	/**
	 * This method initializes inputPort	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getInputPort() {
		if (inputPort == null) {
			inputPort = new JSpinner(new SpinnerNumberModel());
			inputPort.setPreferredSize(new Dimension(75, 20));
		}
		return inputPort;
	}

	@Override
	public void load() {
		preferences = MessagingPreferencesState.load(storageService);
		getNetworkInterface().setSelectedItem(new NetworkInterfaceWrapper(preferences.getMulticastNetworkInterface()));
		getMulticastAddress().setText(preferences.getMulticastUdpAddress());
		getOutputPort().setValue(preferences.getMulticastUdpSendPort());
		getInputPort().setValue(preferences.getMulticastUdpReceivePort());
	}

	@Override
	public void save() {
		try {
			preferences.setMulticastNetworkInterface(((NetworkInterfaceWrapper)getNetworkInterface().getSelectedItem()).getNetworkInterface());
			preferences.setMulticastUdpAddress(getMulticastAddress().getText());
			preferences.setMulticastUdpSendPort((Integer)getOutputPort().getValue());
			preferences.setMulticastUdpReceivePort((Integer)getInputPort().getValue());
			preferences.save(storageService);
//			boolean useStats = messagingService.getDataTerminal().isStatisticsEnabled();
			messagingService.deactivate();
			messagingService.activate();
//			messagingService.getDataTerminal().setStatisticsEnabled(useStats);
		} catch (Exception e) {
			ViewHelper.showException(e);
		}
	}
	
}
