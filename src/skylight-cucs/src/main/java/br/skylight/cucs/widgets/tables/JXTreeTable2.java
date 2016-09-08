package br.skylight.cucs.widgets.tables;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.autocomplete.ComboBoxCellEditor;
import org.jdesktop.swingx.combobox.EnumComboBoxModel;
import org.jdesktop.swingx.treetable.TreeTableModel;

public class JXTreeTable2 extends JXTreeTable {

	private Map<Class,TableCellEditor> editors = new HashMap<Class,TableCellEditor>();

	public JXTreeTable2() {
		super();
	}

	public JXTreeTable2(TreeTableModel treeModel) {
		super(treeModel);
	}

	public void setCellEditor(Class clazz, TableCellEditor editor) {
		editors.put(clazz, editor);
	}

	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		Object obj = getValueAt(row, column);
		TableCellEditor e = editors.get(obj.getClass());
		//custom cell editors
		if(e!=null) {
			return e;
		//integer
		} else if(obj.getClass().equals(Integer.class)) {
			SpinnerNumberModel m = new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
			JSpinner s = new JSpinner(m);
			return new DefaultCellEditor2(s) {
				public Object getCellEditorValue() {
					return new Double(super.getCellEditorValue().toString()).intValue();
				}
			};
		//long
		} else if(obj.getClass().equals(Long.class)) {
			SpinnerNumberModel m = new SpinnerNumberModel(0L, Long.MIN_VALUE, Long.MAX_VALUE, 1);
			JSpinner s = new JSpinner(m);
			return new DefaultCellEditor2(s) {
				public Object getCellEditorValue() {
					return new Double(super.getCellEditorValue().toString()).longValue();
				}
			};
		//float
		} else if(obj.getClass().equals(Float.class)) {
			SpinnerNumberModel m = new SpinnerNumberModel(0F, -Float.MAX_VALUE, Float.MAX_VALUE, 0.1F);
			JSpinner s = new JSpinner(m);
			return new DefaultCellEditor2(s) {
				public Object getCellEditorValue() {
					return new Double(super.getCellEditorValue().toString()).floatValue();
				}
			};
		//double
		} else if(obj.getClass().equals(Double.class)) {
			SpinnerNumberModel m = new SpinnerNumberModel(0.0, -Double.MAX_VALUE, Double.MAX_VALUE, 0.1);
			JSpinner s = new JSpinner(m);
			return new DefaultCellEditor2(s) {
				public Object getCellEditorValue() {
					return new Double(super.getCellEditorValue().toString()).doubleValue();
				}
			};
		//boolean
		} else if(obj.getClass().equals(Boolean.class)) {
			return new DefaultCellEditor2(new JCheckBox());
		//automatic enum cell editor
		} else if(obj.getClass().isEnum()) {
			EnumComboBoxModel cm = new EnumComboBoxModel(obj.getClass());
			JComboBox cb = new JComboBox();
			cb.setModel(cm);
			return new ComboBoxCellEditor(cb);
		//default cell editors
		} else {
			return super.getCellEditor(row, column);
		}
	}
	
}
