package br.skylight.cucs.widgets.tables;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class TypedTableModel<T> extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	
	private List<T> userObjects = new ArrayList<T>();
	private ObjectToColumnAdapter<T> adapter;
	private String[] columnNames;
	private boolean[] columnEditables;
	
	public TypedTableModel(ObjectToColumnAdapter<T> adapter, String ... columnNames) {
		this.adapter = adapter;
		this.columnNames = columnNames;
		columnEditables = new boolean[columnNames.length];
	}
	
	public void setColumnEditables(boolean ... columnEditables) {
		this.columnEditables = columnEditables;
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnEditables[columnIndex];
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if(getRowCount()>0) {
			if(getValueAt(0, columnIndex)==null) {
				return Object.class;
			} else {
				return getValueAt(0, columnIndex).getClass();
			}
		} else {
			return super.getColumnClass(columnIndex);
		}
	}
	
	public List<T> getUserObjects() {
		return userObjects;
	}
	
	public void setUserObjects(List<T> userObjects) {
		this.userObjects = userObjects;
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return adapter.getValueAt(userObjects.get(rowIndex), columnIndex);
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		adapter.setValueAt(userObjects.get(rowIndex), aValue, columnIndex);
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return userObjects.size();
	}
	
	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}
	
}
