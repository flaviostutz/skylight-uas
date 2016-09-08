package br.skylight.cucs.widgets.checklist;

public class CheckItemResult {

	private ChecklistItemState state;
	private String message;

	public CheckItemResult(ChecklistItemState state, String message) {
		this.state = state;
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public ChecklistItemState getState() {
		return state;
	}
	public void setState(ChecklistItemState state) {
		this.state = state;
	}
	
}
