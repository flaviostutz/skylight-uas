package br.skylight.cucs.widgets.tables;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

public abstract class EditableMutableTreeTableNode extends AbstractMutableTreeTableNode {

	private boolean[] editables;

	public EditableMutableTreeTableNode(Object userObject) {
		this(userObject, 30);
	}
	
	public EditableMutableTreeTableNode(Object userObject, int numberOfColumns) {
		super(userObject);
		editables = new boolean[numberOfColumns];
	}

	@Override
	public int getColumnCount() {
		return editables.length;
	}

	public void setEditable(int columnIndex, boolean editable) {
		editables[columnIndex] = editable;
	}
	
	@Override
	public boolean isEditable(int column) {
		if(column>=editables.length) {
			return false;
		}
		return editables[column];
	}
	
}
