package br.skylight.cucs.plugins.dataviewer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import br.skylight.commons.MessageFieldDef;
import br.skylight.commons.dli.services.Message;

public class MessageFieldTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private List<MessageFieldDef> shownMessageFields = new ArrayList<MessageFieldDef>();

	public void addMessageFieldDef(MessageFieldDef def) {
		shownMessageFields.add(def);
	}
	
	public void updateMessage(Message message) {
		for (MessageFieldDef def : shownMessageFields) {
			if(def.getMessageType().equals(message.getMessageType())) {
				def.updateMessage(message);
			}
		}
	}
	
	@Override
	public int getRowCount() {
		return shownMessageFields.size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		MessageFieldDef def = shownMessageFields.get(row-1);
		if(column==0) {
			return def.getLabel();
		} else if(column==1) {
			return def.getFormattedValue();
		} else if(column==2){
			return def;
		} else {
			return "";
		}
	}
	
	@Override
	public int getColumnCount() {
		return 3;
	}
	
	@Override
	public String getColumnName(int column) {
		if(column==0) {
			return "Name";
		} else if(column==1) {
			return "Value";
		} else if(column==2) {
			return "Graph";
		} else {
			return "";
		}
	}
	
}
