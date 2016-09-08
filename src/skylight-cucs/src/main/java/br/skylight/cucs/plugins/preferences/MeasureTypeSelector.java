package br.skylight.cucs.plugins.preferences;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import javax.swing.SwingConstants;

import br.skylight.commons.MeasureType;

public class MeasureTypeSelector extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel label = null;
	private JComboBox selectedUnit = null;
	private MeasureType measureType;  //  @jve:decl-index=0:

	/**
	 * This is the default constructor
	 */
	public MeasureTypeSelector() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.fill = GridBagConstraints.NONE;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.anchor = GridBagConstraints.WEST;
		gridBagConstraints1.insets = new Insets(0, 5, 4, 5);
		gridBagConstraints1.gridx = 1;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.insets = new Insets(0, 5, 4, 0);
		gridBagConstraints.weightx = 0.0;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.gridy = 0;
		label = new JLabel();
		label.setText("label:");
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setHorizontalTextPosition(SwingConstants.RIGHT);
		label.setSize(new Dimension(100,22));
		this.setLayout(new GridBagLayout());
		this.setSize(new Dimension(250, 22));
		this.add(label, gridBagConstraints);
		this.add(getSelectedUnit(), gridBagConstraints1);
	}

	public void setMeasureType(MeasureType measureType) {
		this.measureType = measureType;
		getLabel().setText(measureType.getName()+":");
		DefaultComboBoxModel m = new DefaultComboBoxModel();
		for (Unit unit : SI.getInstance().getUnits()) {
			if(measureType.getSourceUnit()!=null) {
				if(unit.getDimension().equals(measureType.getSourceUnit().getDimension())) {
					m.addElement(unit);
				}
			}
		}
		for (Unit unit : NonSI.getInstance().getUnits()) {
			if(measureType.getSourceUnit()!=null) {
				if(unit.getDimension().equals(measureType.getSourceUnit().getDimension())) {
					m.addElement(unit);
				}
			}
		}
		getSelectedUnit().setModel(m);
		getSelectedUnit().setSelectedItem(measureType.getTargetUnit());
	}
	public MeasureType getMeasureType() {
		measureType.setTargetUnit((Unit)getSelectedUnit().getSelectedItem());
		return measureType;
	}
	
	/**
	 * This method initializes selectedUnit	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	public JComboBox getSelectedUnit() {
		if (selectedUnit == null) {
			selectedUnit = new JComboBox();
		}
		return selectedUnit;
	}
	
	public JLabel getLabel() {
		return label;
	}
	public void setLabel(JLabel label) {
		this.label = label;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
