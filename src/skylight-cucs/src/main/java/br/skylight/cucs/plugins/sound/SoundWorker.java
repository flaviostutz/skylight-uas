package br.skylight.cucs.plugins.sound;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import br.skylight.commons.ViewHelper;

public class SoundWorker implements Runnable {

	private SoundDefinition soundDefinition;
	private boolean active = true;
	private Future future;
	private ExecutorService soundPlayer;
	
	public SoundWorker(SoundDefinition soundDefinition, ExecutorService soundPlayer) {
		this.soundDefinition = soundDefinition;
		this.soundPlayer = soundPlayer;
		this.active = true;
	}
	
	public void run() {
		if(active) {
			playSound(this);
			if(soundDefinition.isLoop()) {
				soundPlayer.execute(this);
			}
			if(!soundDefinition.isEnabled()) {
				try {
					//avoid cpu shortcircuit loops
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean isActive() {
		return active;
	}

	public void stopPlaying() {
		active = false;
	}

	protected void playSound(SoundWorker sound) {
		if(!sound.getSoundDefinition().isEnabled()) {
			return;
		}
		try {
			InputStream is = soundDefinition.getSoundURL().openStream();
			final AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(is);
			final AudioFormat audioFormat = audioInputStream.getFormat();

			final DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
			final SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);

			sourceDataLine.open(audioFormat);
			sourceDataLine.start();

			int cnt;
			byte[] tempBuffer = new byte[256];
			// Keep looping until the input read method returns -1 for empty stream or the
			// user clicks the Stop button causing stopPlayback to switch from false to true.
			while ((cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1 && sound.isActive()) {
				if (cnt > 0) {
					// Write data to the internal buffer of the data line where it will be delivered to the speaker.
					sourceDataLine.write(tempBuffer, 0, cnt);
				}
			}

			// Block and wait for internal buffer of the data line to empty (when stopped).
			sourceDataLine.drain();
			sourceDataLine.close();

		} catch (Exception e) {
			ViewHelper.showException(e);
		}
	}
	
	public void setFuture(Future future) {
		this.future = future;
	}
	public Future getFuture() {
		return future;
	}

	public SoundDefinition getSoundDefinition() {
		return soundDefinition;
	}

}
