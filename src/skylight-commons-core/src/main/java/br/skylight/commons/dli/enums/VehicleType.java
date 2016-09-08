package br.skylight.commons.dli.enums;

public enum VehicleType {
	
	TYPE_0("Not Identified"),
	TYPE_1("BAMS UAV"), 
	TYPE_2("Crecerelle"),
	TYPE_3("Crecerelle GE"),
	TYPE_4("Eagle-1"),
	TYPE_5("RQ-8 Fire Scout"),
	TYPE_6("RQ-4A Global Hawk A"),
	TYPE_7("Grasshopper"),
	TYPE_8("Moyen Duc"),
	TYPE_9("Petit Duc"),
	TYPE_10("Phoenix"),
	TYPE_11("MQ-1 Predator A"),
	TYPE_12("MQ-9 Predator B"),
	TYPE_13("Ranger"),
	TYPE_14("RQ-7 Shadow 200"),
	TYPE_15("Sperwer"),
	TYPE_16("Sperwer LE"),
	TYPE_17("RQ-2B Pioneer"),
	TYPE_18("Eagle Eye"),
	TYPE_19("RQ-5 Hunter"),
	TYPE_20("GHMD (Navy)"),
	TYPE_21("Mucke"),
	TYPE_22("Luna"),
	TYPE_23("KZO"),
	TYPE_24("Taifun"),
	TYPE_25("Fledermaus"),
	TYPE_26("Falco"),
	TYPE_27("Nibbo"),
	TYPE_28("Hermes 180"),
	TYPE_29("Hermes 450"),
	TYPE_30("RQ-4B Global Hawk B"),
	TYPE_31("Warrior (Army)"),
	TYPE_32("ScanEagle A15"),
	TYPE_33("Vigilante 496"),
	TYPE_34("Vigilante 502"),
	TYPE_35(" CamCopter S100"),
	TYPE_36("Little Bird"),
	TYPE_37("Neuron"),
	TYPE_38("Tier II (USMC)"),
	TYPE_39("Dragon Eye"),
	TYPE_40("Silver Fox"),
	TYPE_41("SkyLark"),
	TYPE_42("Kestrel"),
	TYPE_43("Voyeur"),
	TYPE_44("Coyote"),
	TYPE_45("FCS Class I"),
	TYPE_46("FCS Class II"),
	TYPE_47("FCS Class III"),
	TYPE_48("Raven-B"),
	TYPE_49("Spyhawk"),
	TYPE_50("Wasp"),
	TYPE_51("Puma"),
	TYPE_52("Aerosonde"),
	TYPE_53("ScanEagle A20"),
	TYPE_54("Sky-X"),
	TYPE_55("Lince"),
	TYPE_56("Cobra"),
	TYPE_57("Reserved"),
	TYPE_58("Reserved"),
	TYPE_59("Reserved"),
	/** Skylight vehicle */
	TYPE_60("Skylight");
	
	private String name;
	
	private VehicleType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
