package br.skylight.commons.dli.skylight;

import br.skylight.commons.StringHelper;


public enum CommandType {

	NONE("", null, null),
	/** Send calibration command to Hardware sensors */
	SET_ALTIMETER_SETTING("Setup altimeter setting. Click below and set current pressure at sea level for barometric altitude calculation:", null, null),
	/** Send calibration command to Hardware sensors */
	CALIBRATE_ONBOARD_SYSTEMS("Keep vehicle hold still with pitot covered and click the button below to calibrate onboard avionics:", null, null),
	/** Read current pressure altitude and set it as ground level */
	SET_GROUND_LEVEL("Place vehicle at runway touchdown position and click the button below to set ground level references for AGL estimation purposes:", null, null),
	/** Read current pressure altitude and set it as ground level */
	SYNCHRONIZE_VEHICLE_CLOCK("Synchronize vehicle clock with ground station. After setting clock the onboard computer will be REBOOTED. Send this command twice for confirmation (arm/execute)", null, null),
	/** Arm all failsafes available in software/hardware */
	ARM_FAILSAFES("Arms all hardware and software autopilot failsafes, including RoS actions. All systems will be monitored and if any component seems to be in trouble, it will be reseted. If recovery is not sensed in less than 5s, Flight Termination will take place.", null, null),
	/** Disarm all failsafes available in software/hardware */
	DISARM_FAILSAFES("Disarms all hardware and software autopilot failsafes, including RoS actions. System health will not be monitored, disabling automatic Flight Termination in case of autopilot problems.", null, null),
	/** Replace datalink by PPP */
	//REPLACE_DATALINK_BY_PPP("Close autopilot datalink and replace by a PPP connection using modem port. After this command you will have to reset the onboard computer. SEND THIS COMMAND TWICE for confirmation (arm/execute)"),
	/** Start recording instruments data to a file at maximum speed */
	START_HIGH_FREQUENCY_RECORDING("Start recording instruments data to '/Skylight/uav/instruments-data.csv' at high frequency. This file will be replaced at each recording session.", "Enter recording frequency (Hz) or '0' for the highest possible frequency:", null),
	/** Stop recording instruments data to file */
	STOP_HIGH_FREQUENCY_RECORDING("Stop recording instruments. Download file '/Skylight/uav/instruments-data.csv'. This file will be replaced at each recording session.", null, null),
	/** Disarm all failsafes and reboot onboard computer */
	REBOOT_SYSTEMS("Restart all systems in a safe manner. During shutdown, all failsafes will be disarmed. SEND THIS COMMAND TWICE for confirmation (arm/execute)", null, null),
	/** Disarm all failsafes and shutdown/poweroff all autopilot systems */
	SHUTDOWN_SYSTEMS("Shuts down and poweroff all systems in a safe manner. During shutdown, all failsafes will be disarmed and all systems powered off. SEND THIS COMMAND TWICE for confirmation (arm/execute)", null, null);

	private String instructions;
	private String instructionsValue1;
	private String instructionsValue2;
	
	private CommandType(String instructions, String instructionsValue1, String instructionsValue2) {
		this.instructions = instructions;
		this.instructionsValue1 = instructionsValue1;
		this.instructionsValue2 = instructionsValue2;
	}
	
	@Override
	public String toString() {
		return StringHelper.decapitalize(name(), true);
	}
	
	public String getInstructions() {
		return instructions;
	}
	
	public String getInstructionsValue1() {
		return instructionsValue1;
	}
	public String getInstructionsValue2() {
		return instructionsValue2;
	}
	
}
