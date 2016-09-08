package br.skylight.cucs.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class FeedbackButtonActionListener implements ActionListener {

	private FeedbackButton feedbackButton;
	private List<ActionListener> actionListeners = new ArrayList<ActionListener>();
	
	public FeedbackButtonActionListener(FeedbackButton feedbackButton) {
		this.feedbackButton = feedbackButton;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//process selection request
		if(feedbackButton.isWaitingFeedback()) {
			feedbackButton.setWaitingFeedback(false);
			feedbackButton.setRequestedSelection(feedbackButton.isSelected());
		} else {
			feedbackButton.setWaitingFeedback(true);
			if(feedbackButton.isToggleMode()) {
				feedbackButton.setRequestedSelection(!feedbackButton.isSelected());
			} else {
				feedbackButton.setRequestedSelection(feedbackButton.isSelected());
			}
		}
		
		//call all action listeners bound to this button
		for (ActionListener al : actionListeners) {
			al.actionPerformed(e);
		}
	}

	public void addActionListener(ActionListener l) {
		actionListeners.add(l);
	}

	public ActionListener[] getActionListeners() {
		return (ActionListener[])actionListeners.toArray();
	}

}
