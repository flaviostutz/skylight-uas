package br.skylight.cucs.widgets;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import br.skylight.commons.MeasureType;
import br.skylight.commons.ViewHelper;

public class JMeasureSpinner<T extends Number> extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private MeasureType measureType;  //  @jve:decl-index=0:
	private T value;

	private JSpinner spinner = null;

	private JLabel unit = null;

	public JMeasureSpinner() {
		this(null, null, -999999, 999999, 1, 0, 4);
	}
	public JMeasureSpinner(MeasureType measureType, T initialValue, double minValue, double maxValue, double stepSize, int minFractionDigits, int maxFractionDigits) {
		initialize();
		setup(measureType, initialValue, minValue, maxValue, stepSize, minFractionDigits, maxFractionDigits);
	}
	
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.insets = new Insets(0, 3, 0, 0);
        gridBagConstraints1.gridy = 0;
        unit = new JLabel();
        unit.setText(" ");
        unit.setPreferredSize(new Dimension(30, 14));
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.gridy = 0;
        this.setLayout(new GridBagLayout());
        this.setSize(new Dimension(126, 35));
        this.add(getSpinner(), gridBagConstraints);
        this.add(unit, gridBagConstraints1);
	}
	
	public void setValue(T value) {
		this.value = value;
		if(measureType!=null) {
			spinner.setValue(measureType.convertToTargetUnit(Double.valueOf(value.toString())));
		} else {
			setValueWithoutConversion(value);
		}
	}
	public void setValueWithoutConversion(T value) {
		spinner.setValue(Double.valueOf(value.toString()));
	}
	public T getValue() {
		double v = 0;
		if(measureType!=null) {
			v = measureType.convertToSourceUnit((Double)spinner.getValue());
		} else {
			v = (Double)spinner.getValue();
		}
		if(value instanceof Float) {
			return (T)(Float.valueOf((float)v));
		} else if(value instanceof Double) {
			return (T)(Double.valueOf(v));
		} else if(value instanceof Integer) {
			return (T)(Integer.valueOf((int)v));
		} else if(value instanceof Long) {
			return (T)(Long.valueOf((long)v));
		} else {
			return null;
		}
	}
	
	public void setup(MeasureType measureType, T value, double minValue, double maxValue, double stepSize, int minFractionDigits, int maxFractionDigits) {
		setMeasureType(measureType);
		if(measureType!=null) {
			ViewHelper.setupSpinnerNumber(spinner, measureType.convertToTargetUnit(value!=null?value.doubleValue():0), measureType.convertToTargetUnit(minValue), measureType.convertToTargetUnit(maxValue), measureType.convertToTargetUnit(stepSize), minFractionDigits, maxFractionDigits);
		} else {
			ViewHelper.setupSpinnerNumber(spinner, value!=null?value.doubleValue():0, minValue, maxValue, stepSize, minFractionDigits, maxFractionDigits);
		}
		if(value!=null) {
			setValue(value);
		}
	}
	
	public void setMeasureType(MeasureType measureType) {
		this.measureType = measureType;
		refreshUnitName();
	}
	
	public void refreshUnitName() {
		if(measureType!=null) {
			unit.setText(measureType.getTargetUnit().toString());
		}
	}
	
	/**
	 * This method initializes jSpinner	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	public JSpinner getSpinner() {
		if (spinner == null) {
			spinner = new JSpinner();
			spinner.setModel(new SpinnerNumberModel());
		}
		return spinner;
	}
	
	public void setUnitName(String name) {
		unit.setText(name);
	}
	
	public void setShowUnit(boolean show) {
		unit.setVisible(show);
	}
	
	public boolean isShowUnit() {
		return unit.isVisible();
	}
	public void setupClickButtonOnDefaultAction(JButton button) {
		CUCSViewHelper.setDefaultActionClick(spinner, button);
	}
	
}  //  @jve:decl-index=0:visual-constraint="15,19"
