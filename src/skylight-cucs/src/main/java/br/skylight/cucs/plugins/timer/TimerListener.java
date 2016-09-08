package br.skylight.cucs.plugins.timer;

public interface TimerListener {

	public void onTimeElapsed(long elapsedTime);
	public void onCountdownFinished();

}
