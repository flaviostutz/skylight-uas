package br.skylight.cucs.plugins.vehiclecontrol;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import br.skylight.commons.CUCSControl;
import br.skylight.commons.ControllableElement;
import br.skylight.commons.LOI;
import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;
import br.skylight.cucs.plugins.core.UserService;

public class ControlRequestItem extends JPanel {

	private static final long serialVersionUID = 1L;
	private JCheckBox checkbox = null;
	private JComboBox loiCombo = null;
	private JCheckBox overrideMode = null;

	private ControllableElement controllableElement;
	private JLabel jLabel = null;

	private UserService userService;
	
	/**
	 * This is the default constructor
	 */
	public ControlRequestItem(ControllableElement ce, UserService userService) {
		super();
		this.controllableElement = ce;
		this.userService = userService;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
		gridBagConstraints8.gridx = 0;
		gridBagConstraints8.insets = new Insets(0, 0, 0, 0);
		gridBagConstraints8.gridy = 0;
		jLabel = new JLabel();
		jLabel.setText(">");
		jLabel.setFont(new Font("Dialog", Font.BOLD, 14));
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.gridx = 3;
		gridBagConstraints3.gridy = 0;
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.fill = GridBagConstraints.NONE;
		gridBagConstraints2.gridy = 0;
		gridBagConstraints2.weightx = 0.0;
		gridBagConstraints2.gridx = 2;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridy = 0;
		this.setSize(295, 33);
		this.setLayout(new GridBagLayout());
		this.add(getCheckbox(), gridBagConstraints);
		this.add(getLoiCombo(), gridBagConstraints2);
		this.add(getOverrideMode(), gridBagConstraints3);
		this.add(jLabel, gridBagConstraints8);
	}

	/**
	 * This method initializes checkbox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getCheckbox() {
		if (checkbox == null) {
			checkbox = new JCheckBox();
			if(controllableElement instanceof Vehicle) {
				checkbox.setText("Aerial Vehicle");
			} else if(controllableElement instanceof Payload) {
				checkbox.setText(((Payload)controllableElement).getLabel());
			}
			checkbox.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					getLoiCombo().setEnabled(checkbox.isSelected());
					getOverrideMode().setEnabled(checkbox.isSelected());
				}
			});
			CUCSControl cc = controllableElement.resolveCUCSControl(userService.getCurrentCucsId());
			
			//select best loi
			LOI hloi = null;
			for (LOI loi : cc.getAuthorizedLOIs().getLOIs()) {
				if(hloi==null || loi.ordinal()>hloi.ordinal()) {
					hloi = loi;
				}
			}
			if(hloi!=null) {
				getLoiCombo().setSelectedItem(hloi);
				checkbox.setSelected(true);
				checkbox.setEnabled(true);
			} else {
				checkbox.setSelected(false);
				checkbox.setEnabled(false);
			}
			
			//override mode
			getOverrideMode().setSelected(cc.isOverrideMode());
		}
		return checkbox;
	}

	/**
	 * This method initializes loiCombo	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	public JComboBox getLoiCombo() {
		if (loiCombo == null) {
			loiCombo = new JComboBox();
			loiCombo.setEnabled(false);
			DefaultComboBoxModel model = new DefaultComboBoxModel();
			CUCSControl cc = controllableElement.resolveCUCSControl(userService.getCurrentCucsId());
			for (LOI loi : cc.getAuthorizedLOIs().getLOIs()) {
				model.addElement(loi);
			}
			loiCombo.setModel(model);
		}
		return loiCombo;
	}

	/**
	 * This method initializes overrideMode	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	public JCheckBox getOverrideMode() {
		if (overrideMode == null) {
			overrideMode = new JCheckBox();
			overrideMode.setText("Override mode");
			overrideMode.setEnabled(false);
		}
		return overrideMode;
	}

	public ControllableElement getControllableElement() {
		return controllableElement;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
