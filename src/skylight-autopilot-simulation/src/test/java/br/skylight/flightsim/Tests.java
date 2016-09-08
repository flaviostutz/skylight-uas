package br.skylight.flightsim;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import br.skylight.flightsim.flyablebody.Environment;
import br.skylight.uav.plugins.onboardintegration.NMEAGPSService;



public class Tests {

	public static void main(String[] args) throws InterruptedException, IOException {
		if(true) {
			NMEAGPSService gs = new NMEAGPSService();
			gs.reader = new BufferedReader(new InputStreamReader(new FileInputStream("g:\\gps2.log"), Charset.forName("US-ASCII")));
			while(true) {
				gs.step();
				System.out.println(gs.getPosition().getLatitude() + " " + gs.getCourseHeading() + " " + gs.getAltitudeMSL() + " " + gs.getSatCount() + " " + gs.getFixQuality());
			}
			
//			GPSUpdate gu = NMEAParser.parse("$GPGGA,,,,,,,,,,,,,,,*7A", new GPSUpdate());
//			System.out.println(gu.getCourseMadeGood());
//			System.out.println(gu.getCoordinates().getLatitude());
//			gu = NMEAParser.parse("$GPRMC,,,,,,,,,,,*67", new GPSUpdate());
//			System.out.println(gu.getSatCount());
//			System.out.println(gu.getCoordinates().getLongitude());
//			return;
		}
//		BasicAirplane b = new BasicAirplane();
//		System.out.println("ALL ZERO");
//		b.setAngles(0, 0, 0);
//		b.rotate(1);
//		System.out.println("ROLL 30");
//		b.setAngles(Math.toRadians(30), 0, 0);
//		b.rotate(1);
//		System.out.println("PITCH 20");
//		b.setAngles(Math.toRadians(30), 0, Math.toRadians(30));
//		b.rotate(1);
//		System.out.println("HEADING -90");
//		b.setAngles(Math.toRadians(30), Math.toRadians(-90), Math.toRadians(30));
//		b.rotate(1);
//		if(true) return;

		Environment e = new Environment();
		BasicAirplane ba = new BasicAirplane(e);
		ba.setThrottle(90);
		ba.move(10);
		ba.setAileron(55);
//		ba.setElevator(-7);
//		ba.setRudder(3);
//		ba.setAngles(Math.toRadians(0), Math.toRadians(0), Math.toRadians(0));
		ba.move(2);
		ba.setAileron(0);
		ba.setElevator(0);
//		ba.setRudder(0);
//		ba.setThrottle(0);
		while(true) {
			ba.move(0.1);
			//duas 3s fica com problema
//			System.out.println(J3DHelper.str(J3DHelper.toV3d(ba.getPosition())));
//			System.out.println(J3DHelper.str(ba.getRoll()) + " " + J3DHelper.str(ba.getPitch()) + " " + J3DHelper.str(ba.getHeading()));
			Thread.sleep(100);
		}
	}
	
}
