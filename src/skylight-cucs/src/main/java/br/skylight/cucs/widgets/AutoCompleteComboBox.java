package br.skylight.cucs.widgets;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;

public class AutoCompleteComboBox extends JComboBox {

	private static final long serialVersionUID = -2832293102799874785L;

	private AutoTextFieldEditor autoTextFieldEditor;

	private boolean isFired;

	static List<String> demoList = new ArrayList<String>();

	public AutoCompleteComboBox() {
		this(demoList);
		setStrict(false);
	}

	public AutoCompleteComboBox(java.util.List list) {
		isFired = false;
		autoTextFieldEditor = new AutoTextFieldEditor(list);
		setEditable(true);
		setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		setModel(new DefaultComboBoxModel(list.toArray()) {
			private static final long serialVersionUID = 6585879775048250591L;
			protected void fireContentsChanged(Object obj, int i, int j) {
				if (!isFired)
					super.fireContentsChanged(obj, i, j);
			}

		});
		setEditor(autoTextFieldEditor);
	}

	public void setText(String text) {
		autoTextFieldEditor.getAutoTextFieldEditor().setText(text);
	}
	
	public String getText() {
		return autoTextFieldEditor.getAutoTextFieldEditor().getText();
	}
	
	public boolean isCaseSensitive() {
		return autoTextFieldEditor.getAutoTextFieldEditor().isCaseSensitive();
	}

	public void setCaseSensitive(boolean flag) {
		autoTextFieldEditor.getAutoTextFieldEditor().setCaseSensitive(flag);
	}

	public boolean isStrict() {
		return autoTextFieldEditor.getAutoTextFieldEditor().isStrict();
	}

	public void setStrict(boolean flag) {
		autoTextFieldEditor.getAutoTextFieldEditor().setStrict(flag);
	}

	public java.util.List getDataList() {
		return autoTextFieldEditor.getAutoTextFieldEditor().getDataList();
	}

	public void setDataList(java.util.List list) {
		autoTextFieldEditor.getAutoTextFieldEditor().setDataList(list);
		setModel(new DefaultComboBoxModel(list.toArray()) {
			private static final long serialVersionUID = 6585879775048250591L;
			protected void fireContentsChanged(Object obj, int i, int j) {
				if (!isFired)
					super.fireContentsChanged(obj, i, j);
			}

		});
	}

	void setSelectedValue(Object obj) {
		if (isFired) {
			return;
		} else {
			isFired = true;
			setSelectedItem(obj);
			fireItemStateChanged(new ItemEvent(this, 701, selectedItemReminder,
					1));
			isFired = false;
			return;
		}
	}

	protected void fireActionEvent() {
		if (!isFired)
			super.fireActionEvent();
	}

	private class AutoTextFieldEditor extends BasicComboBoxEditor {

		private AutoCompleteTextField getAutoTextFieldEditor() {
			return (AutoCompleteTextField) editor;
		}

		AutoTextFieldEditor(java.util.List list) {
			editor = new AutoCompleteTextField(list, AutoCompleteComboBox.this);
			editor.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		}
	}

}
