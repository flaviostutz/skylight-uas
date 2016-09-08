package br.skylight.commons.dli.enums;

public enum DataLinkState {

	OFF("OFF"),
	RX_ONLY("RX only"),
	TX_AND_RX("TX/RX"),
	TX_HIGH_POWER_AND_RX("TX (high power)/RX");

	private String name;
	
	private DataLinkState(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
