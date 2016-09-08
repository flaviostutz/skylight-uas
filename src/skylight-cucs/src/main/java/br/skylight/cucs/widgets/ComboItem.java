package br.skylight.cucs.widgets;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

public class ComboItem<T> {

	private T value;
	private String label;
	
	public ComboItem(T value, String label) {
		this.value = value;
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
	public T getValue() {
		return value;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public void setValue(T value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return label;
	}
	
	public static boolean isSelectedItem(JComboBox combo, Object itemValue) {
		return ((ComboItem)combo.getSelectedItem()).getValue().equals(itemValue);
	}

	public static void selectValue(JComboBox combo, Object itemValue) {
		ComboBoxModel m = combo.getModel();
		for(int i=0; i<m.getSize(); i++) {
			ComboItem ci = ((ComboItem)m.getElementAt(i));
			if(ci.getValue().equals(itemValue)) {
				combo.setSelectedItem(ci);
				return;
			}
		}
	}
	
}
