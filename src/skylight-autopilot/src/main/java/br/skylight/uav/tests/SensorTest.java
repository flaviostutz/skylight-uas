package br.skylight.uav.tests;

import java.io.File;
import java.io.FileOutputStream;

import br.skylight.commons.infra.IOHelper;
import br.skylight.uav.plugins.onboardintegration.OnboardInstrumentsService;

public class SensorTest {

	public static void main(String[] args) throws Exception {

		File f = IOHelper.resolveFile(new File("/tests"), "sensor-test.csv");

		FileOutputStream fos = new FileOutputStream(f);
		String output = "ROLL;PITCH;BARO;KTIAS;BAT1;ERR1;ERR2;ERR3;ERR4;TSU1;LAT;LON;ALT;FIX;SATS;TSU2\n";
		fos.write(output.getBytes());

		OnboardInstrumentsService gateway = new OnboardInstrumentsService();
		gateway.activate();
		gateway.waitForReadiness(3000);
		
//		OnboardGPSService gpsGateway = new OnboardGPSService();
//		gpsGateway.activate();
//		gpsGateway.waitForReadiness();

		System.out.println("InstrumentsService started.");

		// actuators.setThrottle(40);
		int actVal = 0;
		int loops = 0;
		long lastScreenUpdate = 0;

		while (true) {

			if (actVal == 127) {
				actVal = -127;
				loops++;
			}

			if (loops > 15) {
				actVal = 0;
			}

			// "ROLL;PITCH;BARO;KTIAS;ERR1;ERR2;ERR3;ERR4;TSU1;LAT;LON;ALT;FIX;SATS;TSU2\n"
			// ;
//			output = gateway.getRoll() + ";" + gateway.getPitch() + ";" + gateway.getAltitudeAGLBarometric();
//			output += ";" + gateway.getIndicatedAirspeed() + ";" + gateway.getBattery1() + ";" + gateway.getI2CADCInitErrorEvents() + ";" + gateway.getI2CADCCommErrorEvents() + ";" + gateway.getIRSaturationErrorEvents() + ";" + gateway.getSCIErrorEvents() + ";";
//			output += gpsGateway.getLatitude() + ";" + gpsGateway.getLongitude() + ";" + gpsGateway.getAltitudeAGLGps();
//			output += ";" + gpsGateway.getQuality() + ";" + gpsGateway.getSatCount() + ";" + gpsGateway.getTimeSinceLastUpdate() + "\n";
			fos.write(output.getBytes());

			if (System.currentTimeMillis() - lastScreenUpdate > 500) {
				// String tela = "RPM: " + gateway.getRpm() + "\n";
				String tela = "Roll: " + gateway.getRoll() + "\n";
				tela += "Pitch: " + gateway.getPitch() + "\n";
//				tela += "AGL: " + gateway.getAltitudeAGLBarometric() + " ft. (";
//				tela += gateway.getVerticalSpeed() + " ft./min)\n";
				// tela += "Sonar :" + gateway.getAltitudeAGLSonar() + " ft.\n";
//				tela += "KTIAS :" + gateway.getIndicatedAirspeed() + "\n";
//				tela += "IR cal.: " + (System.currentTimeMillis() - gateway.getLastIRCalibration()) + " ms (ready=" + gateway.isCalibrated() + ")" + "\n";
//				tela += "Battery: " + gateway.getBattery1() + "%\n";
//				tela += "Lat: " + gpsGateway.getLatitude() + "\n";
//				tela += "Lon: " + gpsGateway.getLongitude() + "\n";
//				tela += "Alt: " + gpsGateway.getAltitudeAGLGps() + "\n";
//				tela += "Fix/Sats: " + gpsGateway.getQuality() + "/" + gpsGateway.getSatCount();
//				tela += " Err: " + gateway.getI2CADCInitErrorEvents() + "/" + gateway.getI2CADCCommErrorEvents() + "/" + gateway.getIRSaturationErrorEvents() + "/" + gateway.getSCIErrorEvents() + "\n";
//				tela += "GPS upd: " + gpsGateway.getTimeSinceLastUpdate() + "\n";
//				tela += (gateway.isAutoMode() ? "AUTO MODE" : "MANUAL MODE");
				tela += " ACT: " + actVal;

				System.out.println(tela);
				lastScreenUpdate = System.currentTimeMillis();
			}

			Thread.sleep(10);
		}

	}

}
