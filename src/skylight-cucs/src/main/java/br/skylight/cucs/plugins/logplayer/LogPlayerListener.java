package br.skylight.cucs.plugins.logplayer;


public interface LogPlayerListener {

	public void onTimeElapsed(long timeElapsed);
	public void onEndReached();
	
}
