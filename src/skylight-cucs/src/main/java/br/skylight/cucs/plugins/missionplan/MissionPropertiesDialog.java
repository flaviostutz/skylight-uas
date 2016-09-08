package br.skylight.cucs.plugins.missionplan;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import br.skylight.commons.StringHelper;
import br.skylight.commons.Vehicle;
import br.skylight.commons.ViewHelper;
import br.skylight.commons.plugin.annotations.ExtensionPointsInjection;
import br.skylight.cucs.widgets.CUCSViewHelper;

public class MissionPropertiesDialog extends Dialog {

	private static final long serialVersionUID = 1L;
	
	@ExtensionPointsInjection
	public List<MissionPropertiesTabExtensionPoint> tabs;
	
	private JTabbedPane tabbed = null;
	private JPanel basic = null;

	private JLabel jLabel = null;

	private JLabel jLabel1 = null;

	private JLabel vehicleType = null;

	private JTextField missionId = null;

	private JLabel jLabel3 = null;

	private JLabel vehicleId = null;

	private JButton ok = null;

	private JButton cancel = null;
	
	private List<MissionPropertiesTabExtensionPoint> compatibleTabs = new ArrayList<MissionPropertiesTabExtensionPoint>();  //  @jve:decl-index=0:
	private Vehicle vehicle;  //  @jve:decl-index=0:

	/**
	 * @param owner
	 */
	public MissionPropertiesDialog(Frame owner) {
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
		gridBagConstraints41.gridx = 1;
		gridBagConstraints41.weightx = 0.8;
		gridBagConstraints41.anchor = GridBagConstraints.WEST;
		gridBagConstraints41.insets = new Insets(2, 5, 3, 0);
		gridBagConstraints41.gridy = 1;
		GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
		gridBagConstraints31.gridx = 0;
		gridBagConstraints31.weightx = 1.0;
		gridBagConstraints31.anchor = GridBagConstraints.EAST;
		gridBagConstraints31.insets = new Insets(2, 0, 3, 0);
		gridBagConstraints31.gridy = 1;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.gridx = 0;
		this.setLayout(new GridBagLayout());
		this.setSize(253, 198);
		this.setPreferredSize(new Dimension(253, 198));
		this.setTitle("Mission properties");
		this.setModal(true);
		this.add(getTabbed(), gridBagConstraints);
		this.add(getOk(), gridBagConstraints31);
		this.add(getCancel(), gridBagConstraints41);
		CUCSViewHelper.setDefaultIcon(this);
	}

	/**
	 * This method initializes tabbed	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getTabbed() {
		if (tabbed == null) {
			tabbed = new JTabbedPane();
			tabbed.addTab("Identification", null, getBasic(), null);
		}
		return tabbed;
	}

	/**
	 * This method initializes basic	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getBasic() {
		if (basic == null) {
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 1;
			gridBagConstraints6.insets = new Insets(0, 3, 10, 0);
			gridBagConstraints6.anchor = GridBagConstraints.WEST;
			gridBagConstraints6.gridy = 1;
			vehicleId = new JLabel();
			vehicleId.setText(" ");
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.anchor = GridBagConstraints.EAST;
			gridBagConstraints5.insets = new Insets(0, 10, 10, 0);
			gridBagConstraints5.gridy = 1;
			jLabel3 = new JLabel();
			jLabel3.setText("Vehicle Id:");
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.gridy = 2;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.insets = new Insets(0, 3, 0, 15);
			gridBagConstraints4.anchor = GridBagConstraints.WEST;
			gridBagConstraints4.gridx = 1;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.insets = new Insets(0, 3, 10, 0);
			gridBagConstraints3.anchor = GridBagConstraints.WEST;
			gridBagConstraints3.gridy = 0;
			vehicleType = new JLabel();
			vehicleType.setText(" ");
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.anchor = GridBagConstraints.EAST;
			gridBagConstraints2.insets = new Insets(0, 10, 0, 0);
			gridBagConstraints2.weightx = 0.3;
			gridBagConstraints2.gridy = 2;
			jLabel1 = new JLabel();
			jLabel1.setText("Mission Id:");
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			gridBagConstraints1.insets = new Insets(0, 10, 10, 0);
			gridBagConstraints1.gridy = 0;
			jLabel = new JLabel();
			jLabel.setText("Vehicle type:");
			basic = new JPanel();
			basic.setLayout(new GridBagLayout());
			basic.add(jLabel, gridBagConstraints1);
			basic.add(jLabel1, gridBagConstraints2);
			basic.add(vehicleType, gridBagConstraints3);
			basic.add(getMissionId(), gridBagConstraints4);
			basic.add(jLabel3, gridBagConstraints5);
			basic.add(vehicleId, gridBagConstraints6);
		}
		return basic;
	}

	/**
	 * This method initializes missionId	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getMissionId() {
		if (missionId == null) {
			missionId = new JTextField();
		}
		return missionId;
	}

	/**
	 * This method initializes ok	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOk() {
		if (ok == null) {
			ok = new JButton();
			ok.setText("OK");
			ok.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					boolean allOk = true;
					for (MissionPropertiesTabExtensionPoint t : compatibleTabs) {
						if(!t.onOkPressed()) {
							allOk = false;
						}
					}
					if(allOk) {
						vehicle.getMission().setMissionID(getMissionId().getText());
						setVisible(false);
						dispose();
					}
				}
			});
		}
		return ok;
	}

	/**
	 * This method initializes cancel	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancel() {
		if (cancel == null) {
			cancel = new JButton();
			cancel.setText("Cancel");
			cancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					for (MissionPropertiesTabExtensionPoint t : compatibleTabs) {
						t.onCancelPressed();
					}
					setVisible(false);
					dispose();
				}
			});
		}
		return cancel;
	}
	
	public void showDialog(Vehicle vehicle) {
		this.vehicle = vehicle;
		getMissionId().setText(vehicle.getMission().getMissionID());
		vehicleType.setText(vehicle.getVehicleID().getVehicleType().getName());
		vehicleId.setText(StringHelper.formatId(vehicle.getVehicleID().getVehicleID()));
		
		//select compatible tabs
		for (MissionPropertiesTabExtensionPoint t : tabs) {
			if(t.isCompatibleWith(vehicle)) {
				compatibleTabs.add(t);
			}			
		}
		
		//calculate best dialog size
		double w = getPreferredSize().getWidth();
		double h = getPreferredSize().getHeight();
		for (MissionPropertiesTabExtensionPoint t : compatibleTabs) {
			JPanel tabPanel = t.createTabPanel(vehicle);
			if(tabPanel.getPreferredSize().getWidth()>w) {
				w = tabPanel.getPreferredSize().getWidth();
			}
			if(tabPanel.getPreferredSize().getHeight()>h) {
				h = tabPanel.getPreferredSize().getHeight();
			}
			//add tab to panel
			tabbed.addTab(t.getTabTitle(), null, tabPanel, null);
		}
		setSize((int)w, (int)h);
		ViewHelper.centerWindow(this);
		setVisible(true);
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
