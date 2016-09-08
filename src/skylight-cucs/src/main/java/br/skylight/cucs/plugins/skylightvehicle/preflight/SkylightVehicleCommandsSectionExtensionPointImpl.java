package br.skylight.cucs.plugins.skylightvehicle.preflight;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import org.jdesktop.swingx.combobox.EnumComboBoxModel;

import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.enums.VehicleType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.skylight.CommandType;
import br.skylight.commons.dli.skylight.GenericSystemCommand;
import br.skylight.commons.dli.vehicle.VehicleSteeringCommand;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.vehicleconfiguration.VehicleCommandsSectionExtensionPoint;
import br.skylight.cucs.widgets.NumberInputDialog;

@ExtensionPointImplementation(extensionPointDefinition=VehicleCommandsSectionExtensionPoint.class)
public class SkylightVehicleCommandsSectionExtensionPointImpl implements VehicleCommandsSectionExtensionPoint {

	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="10,52"
	private Vehicle currentVehicle;
	
	@ServiceInjection
	public MessagingService messagingService;
	
	@ServiceInjection
	public VehicleControlService vehicleControlService;
	
	private JComboBox commandCombo = null;
	private JButton actionButton = null;
	private JTextPane instructions = null;
	private JLabel jLabel = null;
	
	@Override
	public JPanel getSectionComponent() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = GridBagConstraints.WEST;
			gridBagConstraints1.insets = new Insets(5, 5, 0, 0);
			gridBagConstraints1.gridy = 0;
			jLabel = new JLabel();
			jLabel.setText("Maintenance actions:");
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.gridy = 2;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.gridx = 0;
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.gridx = 0;
			gridBagConstraints31.weighty = 1.0;
			gridBagConstraints31.gridy = 3;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.fill = GridBagConstraints.BOTH;
			gridBagConstraints11.gridy = 1;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints11.gridx = 0;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(282, 132));
			contents.add(getCommandCombo(), gridBagConstraints11);
			contents.add(getActionButton(), gridBagConstraints31);
			contents.add(getInstructions(), gridBagConstraints);
			contents.add(jLabel, gridBagConstraints1);
		}
		return contents;
	}

	@Override
	public String getSectionName() {
		return "Skylight Vehicle Commands";
	}

	@Override
	public boolean updateVehicle(Vehicle vehicle) {
		this.currentVehicle = vehicle;
		return vehicle!=null && vehicle.getVehicleID()!=null && vehicle.getVehicleID().getVehicleType().equals(VehicleType.TYPE_60);
	}

	/**
	 * This method initializes commandCombo	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getCommandCombo() {
		if (commandCombo == null) {
			commandCombo = new JComboBox();
			commandCombo.setModel(new EnumComboBoxModel<CommandType>(CommandType.class));
			commandCombo.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					instructions.setText(getSelectedCommandType().getInstructions());
					getActionButton().setText(getSelectedCommandType().toString());
					getActionButton().setVisible(!getSelectedCommandType().equals(CommandType.NONE));
				}
			});
			commandCombo.setSelectedIndex(0);
		}
		return commandCombo;
	}

	private CommandType getSelectedCommandType() {
		return (CommandType)commandCombo.getSelectedItem();
	}
	
	/**
	 * This method initializes actionButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getActionButton() {
		if (actionButton == null) {
			actionButton = new JButton();
			actionButton.setText("Action");
			actionButton.setEnabled(true);
			actionButton.setVisible(false);
			actionButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(getSelectedCommandType().equals(CommandType.SET_ALTIMETER_SETTING)) {
						Double r = NumberInputDialog.showInputDialog(null, "Enter pressure at MSL in Pascals:", 101325, 0, 222222, 1, 0, 0);
						if(r!=null) {
							VehicleSteeringCommand vs = vehicleControlService.resolveVehicleSteeringCommandForSending(currentVehicle.getVehicleID().getVehicleID());
							vs.setAltimeterSetting(r.floatValue());
							vehicleControlService.sendVehicleSteeringCommand(vs);
						}
					} else if(!getSelectedCommandType().equals(CommandType.NONE)) {
						float value1 = 0;
						float value2 = 0;
						if(getSelectedCommandType().getInstructionsValue1()!=null) {
							Double r = NumberInputDialog.showInputDialog(getSelectedCommandType().getInstructionsValue1());
							if(r==null) {
								return;
							} else {
								value1 = r.floatValue();
							}
						}
						if(getSelectedCommandType().getInstructionsValue2()!=null) {
							Double r = NumberInputDialog.showInputDialog(getSelectedCommandType().getInstructionsValue2());
							if(r==null) {
								return;
							} else {
								value2 = r.floatValue();
							}
						}
						int r = JOptionPane.showConfirmDialog(null, "Confirm operation?");
						if(r==JOptionPane.OK_OPTION) {
							GenericSystemCommand m = messagingService.resolveMessageForSending(GenericSystemCommand.class);
							m.setCommandType(getSelectedCommandType());
							m.setVehicleID(currentVehicle.getVehicleID().getVehicleID());
							messagingService.sendMessage(m);
						}
					}
				}
			});
		}
		return actionButton;
	}

	/**
	 * This method initializes instructions	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextPane getInstructions() {
		if (instructions == null) {
			instructions = new JTextPane();
			instructions.setBackground(SystemColor.control);
			instructions.setBorder(null);
			instructions.setEditable(false);
		}
		return instructions;
	}

}
