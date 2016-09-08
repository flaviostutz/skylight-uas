package br.skylight.uav.tests;

import br.skylight.uav.plugins.onboardintegration.NMEAGPSService;

public class GPSTest {

	public static void main(String[] args) throws Exception {
		NMEAGPSService gateway = new NMEAGPSService();
		gateway.activate();
		System.out.println("GPSService started.");

		long last = System.currentTimeMillis();
		while (true) {
			if (System.currentTimeMillis() - last > 500) {
				String tela = "====================================\n" +
						"Lat: " + gateway.getPosition().getLatitude() + "\n";
				tela += "Lon: " + gateway.getPosition().getLongitude() + "\n";
				tela += "MSL: " + gateway.getPosition().getAltitude() + "\n";
				tela += "Course: " + gateway.getCourseHeading() + "\n";
				tela += "GSpeed: " + gateway.getGroundSpeed() + "\n";
				last = System.currentTimeMillis();
			}
			Thread.sleep(50);
		}

	}
}
