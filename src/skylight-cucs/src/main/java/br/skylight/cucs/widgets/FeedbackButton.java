package br.skylight.cucs.widgets;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class FeedbackButton extends JButton {

	private static final long serialVersionUID = 1L;

	private boolean toggleMode = true;

	private boolean showFeedbackMark = true;
	protected boolean waitingFeedback = false;
	private boolean requestedSelection = false;
	
	private FeedbackButtonActionListener feedbackButtonListener = new FeedbackButtonActionListener(this);
	
	public FeedbackButton() {
		//only this action listener is directly added to component
		super.addActionListener(feedbackButtonListener);
	}

	@Override
	public void addActionListener(ActionListener l) {
		//guarantee that all listeners will be executed after main listener
		feedbackButtonListener.addActionListener(l);
	}
	
	@Override
	public ActionListener[] getActionListeners() {
		return feedbackButtonListener.getActionListeners();
	}
	
	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		notifyFeedback();
	}
	
	public void notifyFeedback() {
		waitingFeedback = false;
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(showFeedbackMark && waitingFeedback) {
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(getForeground());
			g2.drawString("...", 12, getHeight()-8);
		}
	}
	
	public boolean getRequestedSelection() {
		return requestedSelection;
	}
	
	public boolean isToggleMode() {
		return toggleMode;
	}
	public void setToggleMode(boolean toggleMode) {
		this.toggleMode = toggleMode;
	}

	public boolean isWaitingFeedback() {
		return waitingFeedback;
	}
	
	public void setWaitingFeedback(boolean waitingFeedback) {
		this.waitingFeedback = waitingFeedback;
		updateUI();
	}

	protected void setRequestedSelection(boolean requestedSelection) {
		this.requestedSelection = requestedSelection;
	}
	
	public boolean isShowFeedbackMark() {
		return showFeedbackMark;
	}
	public void setShowFeedbackMark(boolean showFeedbackMark) {
		this.showFeedbackMark = showFeedbackMark;
	}
	
}
