package br.skylight.commons;

import br.skylight.commons.dli.enums.AlertPriority;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.subsystemstatus.SubsystemStatusAlert;
import br.skylight.commons.infra.TimedBoolean;

public class AlertWrapper {

	private SubsystemStatusAlert subsystemStatusAlert;
	private boolean handled;
	private boolean messageSent;
	private double activationStartTime;
	private double priorityStartTime;
	private TimedBoolean activeTimer;
	
	public AlertWrapper(SubsystemStatusAlert subsystemStatusAlert, long timeOnSituationForConsideringActive) {
		this.subsystemStatusAlert = subsystemStatusAlert;
		this.activeTimer = new TimedBoolean(timeOnSituationForConsideringActive);
		activeTimer.setEnabled(false);
	}
	
	public SubsystemStatusAlert getSubsystemStatusAlert() {
		return subsystemStatusAlert;
	}
	
	public void notifyActivationCondition(String text) {
		if(!isActive()) {
			handled = false;
		}
		if(!isActive() 
			|| (subsystemStatusAlert.getAlert().getMinTimeForReactivationMillis()!=-1 
				&& getActiveTime()>subsystemStatusAlert.getAlert().getMinTimeForReactivationMillis()/1000.0)) {
			//activate timer so that in n seconds this alert will be considered active (if it is not deactivated before this time)
			if(!activeTimer.isEnabled()) {
				activeTimer.reset();
			}
			activeTimer.setEnabled(true);
			messageSent = false;
			subsystemStatusAlert.setPriority(subsystemStatusAlert.getAlert().getPriorityOnActivation());
			subsystemStatusAlert.setText(text);
			subsystemStatusAlert.setTimeStamp(System.currentTimeMillis()/1000.0);
			activationStartTime = System.currentTimeMillis()/1000.0;
			//don't use logger.INFO because INFO will be sent as alert and may cause a recursive loop
//			System.out.println("Alert: '" + text + "' activated");
		}
	}
	
	public boolean isMessageSent() {
		return messageSent;
	}
	
	public void sendAlertMessage(MessagingService messagingService) {
		subsystemStatusAlert.setTimeStamp(System.currentTimeMillis()/1000.0);
		SubsystemStatusAlert sa = messagingService.resolveMessageForSending(SubsystemStatusAlert.class);
		sa.copyFrom(subsystemStatusAlert);
		messagingService.sendMessage(sa);
		//don't use logger.INFO because INFO will be sent as alert and may cause a recursive loop
//		System.out.println("Alert: '" + sa.getText() + "' is being sent (" + sa.getPriority()+ ")");
		messageSent = true;
	}

	public void notifyDeactivationCondition(String text, MessagingService messagingService) {
		//send a message that this alert was deactivated if it was active
		if(isActive()) {
			subsystemStatusAlert.setPriority(AlertPriority.CLEARED);
			subsystemStatusAlert.setText(text);
			subsystemStatusAlert.setTimeStamp(System.currentTimeMillis()/1000.0);
			activationStartTime = System.currentTimeMillis()/1000.0;
			sendAlertMessage(messagingService);
		}
		//TESTS
		if(!isActive() && activeTimer.isEnabled()) {
//			System.out.println("Alert: '" + text + "' was deactivated before being sent");
		}
		activeTimer.setEnabled(false);
	}
	
	public double getActiveTime() {
		if(!isActive()) {
			return -1;
		} else {
			return System.currentTimeMillis()/1000.0 - activationStartTime;
		}
	}
	
	public double getPriorityTime() {
		return System.currentTimeMillis()/1000.0 - priorityStartTime;
	}
	public void setPriorityStartTime(double priorityStartTime) {
		this.priorityStartTime = priorityStartTime;
	}

	public boolean isActive() {
		return activeTimer.isTimedOut();
	}
	public void setHandled(boolean handled) {
		this.handled = handled;
	}
	public boolean isHandled() {
		return handled;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((subsystemStatusAlert == null) ? 0 : subsystemStatusAlert.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AlertWrapper other = (AlertWrapper) obj;
		if (subsystemStatusAlert == null) {
			if (other.subsystemStatusAlert != null)
				return false;
		} else if (!subsystemStatusAlert.equals(other.subsystemStatusAlert))
			return false;
		return true;
	}
	
}
