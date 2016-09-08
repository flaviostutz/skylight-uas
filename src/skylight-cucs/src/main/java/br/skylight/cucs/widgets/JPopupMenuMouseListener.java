package br.skylight.cucs.widgets;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

public class JPopupMenuMouseListener extends MouseAdapter {

	private JPopupMenu jPopupMenu;
	private boolean useButton1Click;

	public JPopupMenuMouseListener(JPopupMenu jPopupMenu) {
		this(jPopupMenu, false);
	}

	public JPopupMenuMouseListener(JPopupMenu jPopupMenu, boolean useButton1) {
		this.jPopupMenu = jPopupMenu;
		this.useButton1Click = useButton1;
	}
	
	public void setUseButton1Click(boolean useButton1Click) {
		this.useButton1Click = useButton1Click;
	}
	
	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	private void maybeShowPopup(MouseEvent e) {
		if ((!useButton1Click && e.isPopupTrigger())
			  || (useButton1Click && e.getButton()==MouseEvent.BUTTON1)) {
			jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

}
