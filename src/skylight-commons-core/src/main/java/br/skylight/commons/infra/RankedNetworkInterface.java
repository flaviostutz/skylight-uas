package br.skylight.commons.infra;

import java.net.NetworkInterface;

public class RankedNetworkInterface implements Comparable<RankedNetworkInterface> {

	private int score;
	private NetworkInterface networkInterface;
	
	public RankedNetworkInterface(int score, NetworkInterface networkInterface) {
		this.score = score;
		this.networkInterface = networkInterface;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public int getScore() {
		return score;
	}
	public void setNetworkInterface(NetworkInterface networkInterface) {
		this.networkInterface = networkInterface;
	}
	public NetworkInterface getNetworkInterface() {
		return networkInterface;
	}
	
	@Override
	public int compareTo(RankedNetworkInterface o) {
		if(score>o.score) {
			return -1;
		} else if(score<o.score) {
			return 1;
		} else {
			return 0;
		}
	}
	
}
