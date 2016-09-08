package br.skylight.commons;

public class Target {

	private Coordinates coordinates;
	private long timestamp;

	private Coordinates pointOfDetection;
	
	public Coordinates getPointOfDetection() {
		return pointOfDetection;
	}
	public void setPointOfDetection(Coordinates pointOfDetection) {
		this.pointOfDetection = pointOfDetection;
	}
	public Coordinates getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(Coordinates coordinates) {
		this.coordinates = coordinates;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
