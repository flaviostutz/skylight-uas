package br.skylight.cucs.plugins.systemstatus;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import br.skylight.commons.ViewHelper;
import br.skylight.commons.dli.enums.AlertPriority;
import br.skylight.commons.dli.enums.AlertType;
import br.skylight.commons.dli.enums.Subsystem;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusAlert;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.plugins.workbench.ViewExtensionPoint;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.widgets.VehicleView;

public class OperatorAnnotationView extends VehicleView {

	@ServiceInjection
	public MessagingService messagingService;

	@ServiceInjection
	public SubscriberService subscriberService;

	@ServiceInjection
	public VehicleControlService vehicleControlService;
	
	private JPanel contents;  //  @jve:decl-index=0:visual-constraint="11,14"

	private JButton jButton = null;

	public OperatorAnnotationView(ViewExtensionPoint viewExtensionPoint) {
		super(viewExtensionPoint);
	}

	@Override
	protected JPanel getContents() {
		if(contents==null) {
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.gridy = 0;
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
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 1;
			contents = new JPanel();
			contents.setLayout(new GridBagLayout());
			contents.setSize(new Dimension(210, 149));
			contents.add(getJButton(), gridBagConstraints4);
		}
		return contents;
	}

	@Override
	protected String getBaseTitle() {
		return "Operator Annotation";
	}

	@Override
	protected void updateGUI() {
		getJButton().setEnabled(getCurrentVehicle()!=null);
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("Register annotation");
			jButton.setPreferredSize(new Dimension(130, 35));
			jButton.setMargin(ViewHelper.getDefaultButtonMargin());
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SubsystemStatusAlert ssa = messagingService.resolveMessageForSending(SubsystemStatusAlert.class);
					String result = JOptionPane.showInputDialog("Annotation:", "Type some text here...");
					if(result!=null) {
						ssa.setVehicleID(getCurrentVehicle().getVehicleID().getVehicleID());
						ssa.setPriority(AlertPriority.NOMINAL);
						ssa.setSubsystemID(Subsystem.VSM_STATUS);
						ssa.setType(AlertType.CLEARABLE_BY_OPERATOR);
						ssa.setText(result);
						messagingService.sendMessage(ssa);
					}
				}
			});
		}
		return jButton;
	}
	
}
