package br.skylight.cucs.widgets.tables;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ButtonCellEditorRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
	
	private static final long serialVersionUID = 1L;

	private JButton button;
	
	public ButtonCellEditorRenderer(JButton button) {
		super();
		this.button = button;
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireEditingStopped();
			}
		});
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		return button;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		return button;
	}

	public Object getCellEditorValue() {
		return button.getText();
	}

}