package br.skylight.cucs.widgets;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import br.skylight.commons.Coordinates;
import br.skylight.commons.MeasureType;
import br.skylight.commons.ViewHelper;

public class LocationInput extends JPanel {

	private static final long serialVersionUID = 1L;
	private JMeasureSpinner<Double> decimalCoordinates = null;

	public enum LocationType {
		LATITUDE, LONGITUDE;
	}
	
	private LocationType type = LocationType.LATITUDE;  //  @jve:decl-index=0:
	private JPanel dmsPanel = null;
	private JLabel d = null;
	private JMeasureSpinner<Integer> dSpinner = null;
	private JLabel m = null;
	private JMeasureSpinner<Integer> mSpinner = null;
	private JLabel s = null;
	private JMeasureSpinner<Float> sSpinner = null;
	private JComboBox direction = null;
	private JToggleButton changeInput = null;
	
	/**
	 * This is the default constructor
	 */
	public LocationInput() {
		super();
		initialize();
		setup(LocationType.LATITUDE);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
		gridBagConstraints61.gridx = 2;
		gridBagConstraints61.insets = new Insets(0, 3, 0, 0);
		gridBagConstraints61.anchor = GridBagConstraints.EAST;
		gridBagConstraints61.gridy = 0;
		GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
		gridBagConstraints51.gridx = 1;
		gridBagConstraints51.weightx = 1.0;
		gridBagConstraints51.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints51.gridy = 0;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.gridy = 0;
		this.setSize(479, 35);
		this.setLayout(new GridBagLayout());
		this.add(getDecimalSpinner(), gridBagConstraints);
		this.add(getDmsPanel(), gridBagConstraints51);
		this.add(getChangeInput(), gridBagConstraints61);
	}

	public void setup(LocationType locationType) {
		this.type = locationType;
		double maxValue = (type.equals(LocationType.LATITUDE)?Math.PI/2:Math.PI);
		getDecimalSpinner().setup(MeasureType.GEO_POSITION, 0.0, -maxValue, maxValue, Math.toRadians(1), 0, 9);
		getDSpinner().setup(null, 0, 0, Math.toDegrees(maxValue), 1, 0, 0);
		getMSpinner().setup(null, 0, 0, 59, 1, 0, 0);
		getSSpinner().setup(null, 0F, 0, 59.9999999, 1, 0, 6);
		if(locationType.equals(LocationType.LATITUDE)) {
			getDirection().removeAllItems();
			getDirection().addItem("N");
			getDirection().addItem("S");
		} else {
			getDirection().removeAllItems();
			getDirection().addItem("W");
			getDirection().addItem("E");
		}
	}
	
	/**
	 * This method initializes decimalSpinner	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Double> getDecimalSpinner() {
		if (decimalCoordinates == null) {
			decimalCoordinates = new JMeasureSpinner<Double>();
		}
		return decimalCoordinates;
	}

	/**
	 * This method initializes dmsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getDmsPanel() {
		if (dmsPanel == null) {
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints7.gridy = 0;
			gridBagConstraints7.weightx = 0.0;
			gridBagConstraints7.insets = new Insets(0, 0, 0, 0);
			gridBagConstraints7.gridx = 9;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 7;
			gridBagConstraints6.fill = GridBagConstraints.BOTH;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.gridy = 0;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 8;
			gridBagConstraints5.insets = new Insets(0, 0, 0, 2);
			gridBagConstraints5.gridy = 0;
			s = new JLabel();
			s.setText("''");
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 4;
			gridBagConstraints4.fill = GridBagConstraints.BOTH;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.gridy = 0;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 5;
			gridBagConstraints3.insets = new Insets(0, 0, 0, 2);
			gridBagConstraints3.gridy = 0;
			m = new JLabel();
			m.setText("'");
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.fill = GridBagConstraints.BOTH;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.gridy = 0;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 2;
			gridBagConstraints1.insets = new Insets(0, 0, 0, 2);
			gridBagConstraints1.gridy = 0;
			d = new JLabel();
			d.setText("°");
			dmsPanel = new JPanel();
			dmsPanel.setLayout(new GridBagLayout());
			dmsPanel.setVisible(false);
			dmsPanel.add(d, gridBagConstraints1);
			dmsPanel.add(getDSpinner(), gridBagConstraints2);
			dmsPanel.add(m, gridBagConstraints3);
			dmsPanel.add(getMSpinner(), gridBagConstraints4);
			dmsPanel.add(s, gridBagConstraints5);
			dmsPanel.add(getSSpinner(), gridBagConstraints6);
			dmsPanel.add(getDirection(), gridBagConstraints7);
		}
		return dmsPanel;
	}

	/**
	 * This method initializes dSpinner	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Integer> getDSpinner() {
		if (dSpinner == null) {
			dSpinner = new JMeasureSpinner<Integer>();
			dSpinner.setShowUnit(false);
			dSpinner.setMinimumSize(new Dimension(50,10));
		}
		return dSpinner;
	}

	/**
	 * This method initializes mSpinner	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Integer> getMSpinner() {
		if (mSpinner == null) {
			mSpinner = new JMeasureSpinner<Integer>();
			mSpinner.setShowUnit(false);
			mSpinner.setMinimumSize(new Dimension(50,10));
		}
		return mSpinner;
	}

	/**
	 * This method initializes sSpinner	
	 * 	
	 * @return br.skylight.cucs.widgets.JMeasureSpinner	
	 */
	private JMeasureSpinner<Float> getSSpinner() {
		if (sSpinner == null) {
			sSpinner = new JMeasureSpinner<Float>();
			sSpinner.setShowUnit(false);
			sSpinner.setMinimumSize(new Dimension(50,10));
		}
		return sSpinner;
	}

	/**
	 * This method initializes direction	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getDirection() {
		if (direction == null) {
			direction = new JComboBox();
			direction.setMinimumSize(new Dimension(30,20));
		}
		return direction;
	}

	/**
	 * This method initializes changeInput	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JToggleButton getChangeInput() {
		if (changeInput == null) {
			changeInput = new JToggleButton();
			changeInput.setText("...");
			changeInput.setToolTipText("Change coordinates input format");
			changeInput.setMargin(ViewHelper.getMinimalButtonMargin());
			changeInput.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if(changeInput.isSelected()) {
						setValue(getDecimalSpinner().getValue());
					} else {
						setValue(getValueFromDMSPanel());
					}
					getDecimalSpinner().setVisible(!getChangeInput().isSelected());
					getDmsPanel().setVisible(getChangeInput().isSelected());
				}
			});
		}
		return changeInput;
	}
	
	public void setValue(double value) {
		String sv = Coordinates.convert(Math.toDegrees(Math.abs(value)), Coordinates.DD_MM_SS);
		String[] dms = (sv+":0").split("\\:");
		getDSpinner().setValue(Integer.parseInt(dms[0]));
		getMSpinner().setValue(Integer.parseInt(dms[1]));
		getSSpinner().setValue(Float.parseFloat(dms[2]));
		if(value>=0) {
			getDirection().setSelectedIndex(0);
		} else {
			getDirection().setSelectedIndex(1);
		}
		getDecimalSpinner().setValue(value);
	}
	
	public double getValue() {
		if(getChangeInput().isSelected()) {
			return getValueFromDMSPanel();
		} else {
			return getDecimalSpinner().getValue();
		}
	}

	private double getValueFromDMSPanel() {
		return Math.toRadians((getDirection().getSelectedItem().equals("S")|getDirection().getSelectedItem().equals("W")?-1:1)*Coordinates.convert(getDSpinner().getValue() + ":" + getMSpinner().getValue() + ":" + getSSpinner().getValue()));
	}	

}  //  @jve:decl-index=0:visual-constraint="10,10"
