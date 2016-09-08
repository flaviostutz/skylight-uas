package br.skylight.cucs.widgets.tables;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;

public class DefaultCellEditor2 extends DefaultCellEditor {

	public DefaultCellEditor2(JCheckBox checkBox) {
		super(checkBox);
	}

	public DefaultCellEditor2(JComboBox comboBox) {
		super(comboBox);
	}

	public DefaultCellEditor2(JTextField textField) {
		super(textField);
	}

	public DefaultCellEditor2(final JSpinner spinner) {
		super(new JTextField());// dummy
		super.editorComponent = spinner;
		super.clickCountToStart = 2;
		super.delegate = new EditorDelegate() {
			public void setValue(Object value) {
				spinner.setValue(value);
			}

			public Object getCellEditorValue() {
				return spinner.getValue();
			}
		};
		
		((JSpinner.NumberEditor) spinner.getEditor()).getTextField().addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent ke) {
				if (ke.getKeyChar() == '\n') {
					delegate.actionPerformed(new ActionEvent(spinner, 0, null));
				}
			}
			public void keyReleased(KeyEvent arg0) {}
			public void keyPressed(KeyEvent arg0) {}
		});
	}

}
