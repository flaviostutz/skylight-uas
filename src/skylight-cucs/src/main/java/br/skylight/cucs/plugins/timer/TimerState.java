package br.skylight.cucs.plugins.timer;

import java.io.Serializable;

public class TimerState implements Serializable {

	private boolean soundEnabled = true;
	private String label = "Type label here...";
	private boolean countDownEnabled = false;
	private long countDownTime = -1;
	
	public boolean isSoundEnabled() {
		return soundEnabled;
	}

	public void setSoundEnabled(boolean soundEnabled) {
		this.soundEnabled = soundEnabled;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isCountDownEnabled() {
		return countDownEnabled;
	}

	public void setCountDownEnabled(boolean countDownEnabled) {
		this.countDownEnabled = countDownEnabled;
	}

	public long getCountDownTime() {
		return countDownTime;
	}

	public void setCountDownTime(long countDownTime) {
		this.countDownTime = countDownTime;
	}

}
