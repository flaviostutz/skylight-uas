package br.skylight.cucs.plugins.sound;

import java.net.URL;

public class SoundDefinition {

	private URL soundURL;
	private boolean loop;
	private boolean enabled;

	public SoundDefinition(URL soundURL, boolean loop) {
		this.soundURL = soundURL;
		this.loop = loop;
		this.enabled = true;
	}
	
	public boolean isLoop() {
		return loop;
	}
	public URL getSoundURL() {
		return soundURL;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
}
