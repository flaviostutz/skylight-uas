package br.skylight.cucs.widgets.tables;

public interface ObjectToColumnAdapter<T> {

	public Object getValueAt(T userObject, int columnIndex);
	public void setValueAt(T userObject, Object value, int columnIndex);
	
}
