package br.skylight.cucs.widgets.tables;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableNode;

public class TypedTreeTableModel extends DefaultTreeTableModel {
	
	@Override
	public Object getValueAt(Object node, int column) {
		if(node instanceof TreeTableNode) {
			TreeTableNode t = (TreeTableNode)node;
			return t.getValueAt(column);
		} else {
			return super.getValueAt(node, column);
		}
	}
	@Override
	public void setValueAt(Object value, Object node, int column) {
		if(node instanceof TreeTableNode) {
			TreeTableNode t = (TreeTableNode)node;
			if(value==null) {
				t.setValueAt(null, column);
			} else {
//				t.setValueAt(value.toString(), column);
				t.setValueAt(value, column);
			}
		} else {
			super.setValueAt(value, node, column);
		}
	}
	
}
