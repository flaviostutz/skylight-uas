package br.skylight.cucs.widgets.checklist;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;

public enum ChecklistItemState {

	IDLE(getImage("checkbox.gif"), 					new Font("Dialog", Font.PLAIN, 12), Color.BLACK),
	TESTING(getImage("checkbox-testing.gif"), 		new Font("Dialog", Font.ITALIC, 12), Color.BLACK), 
	TEST_OK(getImage("tick.gif"), 					new Font("Dialog", Font.PLAIN, 12), Color.BLACK), 
	TEST_ERROR(getImage("checkbox-error.gif"), 		new Font("Dialog", Font.BOLD, 12), Color.RED),
	TEST_WARNING(getImage("checkbox-warning.gif"), 	new Font("Dialog", Font.BOLD, 12), Color.DARK_GRAY);
	
	private Image icon;
	private Font font;
	private Color foregroundColor;
	
	private ChecklistItemState(Image icon, Font font, Color foregroundColor) {
		this.icon = icon;
		this.font = font;
		this.foregroundColor = foregroundColor;
	}
	
	public Image getIcon() {
		return icon;
	}
	public static Image getImage(String name) {
		return Toolkit.getDefaultToolkit().getImage(ChecklistItemState.class.getResource("/br/skylight/cucs/images/" + name));
	}
	public Font getFont() {
		return font;
	}
	public Color getForegroundColor() {
		return foregroundColor;
	}
	
}
