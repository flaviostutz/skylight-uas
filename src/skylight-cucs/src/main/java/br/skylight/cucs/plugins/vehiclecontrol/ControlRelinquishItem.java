package br.skylight.cucs.plugins.vehiclecontrol;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import br.skylight.commons.ControllableElement;
import br.skylight.commons.LOI;
import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;

public class ControlRelinquishItem extends JPanel {

	private static final long serialVersionUID = 1L;
	private JCheckBox checkbox = null;
	private ControllableElement controllableElement;
	private LOI loi;
	private JLabel jLabel = null;
	
	/**
	 * This is the default constructor
	 * @param loi 
	 */
	public ControlRelinquishItem(ControllableElement ce, LOI loi) {
		super();
		this.controllableElement = ce;
		this.loi = loi;
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
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridy = 0;
		this.setSize(295, 33);
		this.setLayout(new GridBagLayout());
		this.add(getCheckbox(), gridBagConstraints);
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
				checkbox.setText("Aerial Vehicle [" + loi + "]");
			} else if(controllableElement instanceof Payload) {
				checkbox.setText(((Payload)controllableElement).getLabel() + " [" + loi + "]");
			}
		}
		return checkbox;
	}

	public ControllableElement getControllableElement() {
		return controllableElement;
	}
	
	public LOI getLoi() {
		return loi;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
