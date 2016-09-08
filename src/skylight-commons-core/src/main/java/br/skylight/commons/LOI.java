package br.skylight.commons;

public enum LOI {

	LOI2(2), LOI3(3), LOI4(4), LOI5(5);
	
	private int number;
	
	private LOI(int number) {
		this.number = number;
	}
	
	public int getNumber() {
		return number;
	}
	
}
