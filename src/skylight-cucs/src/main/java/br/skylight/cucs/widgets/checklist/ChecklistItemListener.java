package br.skylight.cucs.widgets.checklist;

public abstract class ChecklistItemListener {

	public boolean prepareItemCheck() {
		return true;
	}
	public abstract CheckItemResult checkItem();
	
}
