package br.skylight.cucs.plugins.sound;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import br.skylight.commons.infra.Worker;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;

@ServiceDefinition
@ServiceImplementation(serviceDefinition=SoundService.class)
public class SoundService extends Worker {

	private List<SoundWorker> playRequests = new CopyOnWriteArrayList<SoundWorker>();
	private ExecutorService soundPlayer = Executors.newFixedThreadPool(3);

	public void playSound(SoundDefinition sound) {
		//don't play twice if the same sound is already scheduled
		for (SoundWorker ps : playRequests) {
			//remove played sounds
			if(ps.getFuture().isDone()) {
				playRequests.remove(ps);
			//verify if desired sound is not being played
			} else {
				if(ps.getSoundDefinition().equals(sound)) {
					return;
				}
			}
		}
		//schedule desired sound
		SoundWorker s = new SoundWorker(sound, soundPlayer);
		Future f = soundPlayer.submit(s);
		s.setFuture(f);
		playRequests.add(s);
	}

	public void stopSound(SoundDefinition sound) {
		for (Runnable r : playRequests) {
			SoundWorker s = (SoundWorker)r;
			if(s.getSoundDefinition().equals(sound)) {
				s.stopPlaying();
			}
		}
	}

}
