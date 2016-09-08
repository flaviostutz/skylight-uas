package br.skylight.cucs.plugins.systemstatus;

import br.skylight.commons.dli.enums.SubsystemState;
import br.skylight.cucs.widgets.RoundButton;

public class SubsystemStatusButton extends RoundButton {

	public void setSubsystemState(SubsystemState subsystemState) {
		setBackground(subsystemState.getColor());
		setForeground(subsystemState.getForeground());
		setToolTipText(subsystemState.getName());
		setWaitingFeedback(false);
		setVisible(!subsystemState.equals(SubsystemState.NO_STATUS));
		updateUI();
	}

}
