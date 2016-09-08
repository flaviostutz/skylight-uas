package br.skylight.commons.dli.enums;

public enum PayloadType {
	NOT_SPECIFIED("Not specified"),
	EO("EO device"), 
	IR("IR device"),
	EOIR("EO/IR device"),
	SAR("SAR device"), 
	FIXED_CAMERA("Fixed camera"),
	COMMS_RELAY("Communications relay"),
	DISPENSABLE_PAYLOAD("Dispensable payload");
	
	private String name;
	
	private PayloadType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
