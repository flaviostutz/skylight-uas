package br.skylight.cucs.plugins.payload.eoir;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Point2D;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import br.skylight.commons.Coordinates;
import br.skylight.commons.Payload;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.ImagePositionValidity;
import br.skylight.commons.dli.enums.SystemOperatingModeState;
import br.skylight.commons.dli.payload.EOIRLaserOperatingState;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.vehicle.AirAndGroundRelativeStates;
import br.skylight.commons.dli.vehicle.InertialStates;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.infra.MathHelper;
import br.skylight.cucs.mapkit.MapElement;
import br.skylight.cucs.mapkit.painters.MapElementPainter;
import br.skylight.cucs.widgets.CUCSViewHelper;

public class EOIRPayloadFOVPainter<T extends EOIRPayloadFOVMapElement> extends MapElementPainter<T> {

	@Override
	protected Polygon paintElement(Graphics2D go, EOIRPayloadFOVMapElement elem) {
		return paintElement(go, elem, elem.getPayload(), elem.getVehicle());
	}
	
	public Polygon paintElement(Graphics2D g, MapElement elem, Payload payload, Vehicle vehicle) {
//		Polygon poly = new Polygon();
//		//CAMERA ATTITUDE/CONFIGURATION DEFINITIONS
//		//default values
//		float roll = 0;
//		float pitch = 0;
//		double yaw = 0;
//		float alfa = (float)Math.toRadians(30);
//		float beta = (float)Math.toRadians(19.8F);
//		double altitude = 100;
//		GeoPosition cameraPosition = zeroPosition;
//
//		//attitude/position/altitude definition
//		InertialStates is = (InertialStates)vehicle.getLastReceivedMessage(MessageType.M101);
//		if(is!=null) {
//			cameraPosition = new GeoPosition(Math.toDegrees(is.getLatitude()), Math.toDegrees(is.getLongitude()));
//			if(is.getAltitudeType().equals(AltitudeType.AGL)) {
//				altitude = is.getAltitude();
//			}
//			roll = is.getPhi();
//			pitch = is.getTheta();
//			yaw = is.getPsi();
//		}
//		AirAndGroundRelativeStates gs = (AirAndGroundRelativeStates)vehicle.getLastReceivedMessage(MessageType.M102);
//		if(gs!=null) {
//			altitude = gs.getAglAltitude();
//		}
//		
//		//camera FOV configuration
//		EOIRLaserOperatingState os = payload.getEoIrPayload().getOperatingState();
//		if(os!=null) {
//			alfa = os.getActualVerticalFieldOfView();
//			beta = os.getActualHorizontalFieldOfView();
//			yaw = os.getActualCentrelineAzimuthAngle();
//			pitch = os.getActualCentrelineElevationAngle()+((float)Math.PI/2);
//			System.out.println(">" + os.getActualCentrelineAzimuthAngle() + " " + os.getActualCentrelineElevationAngle());
//		}
//		
//		//POINT A
//		float xangle = beta-roll;
//		float yangle = alfa+pitch;
//		double x = altitude*((Math.tan(xangle))/(Math.cos(yangle)));
//		double y = altitude*((Math.tan(yangle))/(Math.cos(xangle)));
//		int xm = (int)CUCSViewHelper.metersToPixels((float)x, cameraPosition, getMap())[0];
//		int ym = (int)CUCSViewHelper.metersToPixels((float)y, cameraPosition, getMap())[1];
//		poly.addPoint(xm, ym);
////		System.out.println("A:"+xm+","+ym);
//		
//		//POINT B
//		xangle = -beta-roll;
//		yangle = alfa+pitch;
//		x = altitude*((Math.tan(xangle))/(Math.cos(yangle)));
//		y = altitude*((Math.tan(yangle))/(Math.cos(xangle)));
//		xm = (int)CUCSViewHelper.metersToPixels((float)x, cameraPosition, getMap())[0];
//		ym = (int)CUCSViewHelper.metersToPixels((float)y, cameraPosition, getMap())[1];
//		poly.addPoint(xm, ym);
////		System.out.println("B:"+xm+","+ym);
//		
//		//POINT C
//		xangle = -beta-roll;
//		yangle = -alfa+pitch;
//		x = altitude*((Math.tan(xangle))/(Math.cos(yangle)));
//		y = altitude*((Math.tan(yangle))/(Math.cos(xangle)));
//		xm = (int)CUCSViewHelper.metersToPixels((float)x, cameraPosition, getMap())[0];
//		ym = (int)CUCSViewHelper.metersToPixels((float)y, cameraPosition, getMap())[1];
//		poly.addPoint(xm, ym);
////		System.out.println("C:"+xm+","+ym);
//
//		//POINT D
//		xangle = beta-roll;
//		yangle = -alfa+pitch;
//		x = altitude*((Math.tan(xangle))/(Math.cos(yangle)));
//		y = altitude*((Math.tan(yangle))/(Math.cos(xangle)));
//		xm = (int)CUCSViewHelper.metersToPixels((float)x, cameraPosition, getMap())[0];
//		ym = (int)CUCSViewHelper.metersToPixels((float)y, cameraPosition, getMap())[1];
//		poly.addPoint(xm, ym);
////		System.out.println("D:"+xm+","+ym);
//
//		//DRAW SHAPE
//		g.rotate(-MathHelper.HALF_PI+yaw);
//		g.setColor(new Color(0,120,0,50));
//		g.fill(poly);
//		g.setColor(new Color(0,80,0,150));
//		g.draw(poly);
		
		InertialStates is = (InertialStates)vehicle.getLastReceivedMessage(MessageType.M101);
		if(is!=null) {
			//get attitude/position/altitude definitions
			Coordinates cameraPosition = new Coordinates();
			cameraPosition.setLatitudeRadians(is.getLatitude());
			cameraPosition.setLongitudeRadians(is.getLongitude());
			if(is.getAltitudeType().equals(AltitudeType.AGL)) {
				cameraPosition.setAltitude(is.getAltitude());
			} else {
				AirAndGroundRelativeStates gs = (AirAndGroundRelativeStates)vehicle.getLastReceivedMessage(MessageType.M102);
				if(gs!=null) {
					cameraPosition.setAltitude(gs.getAglAltitude());
				}
			}
			//found AGL altitude
			if(cameraPosition.getAltitude()>0) {
				//camera FOV configuration
				EOIRLaserOperatingState os = payload.getEoIrPayload().getOperatingState();
//				System.out.println(os.getImagePosition().equals(ImagePositionValidity.VALID)); 
				if(os!=null && os.getSystemOperatingModeState().equals(SystemOperatingModeState.ACTIVE) && os.getImagePosition().equals(ImagePositionValidity.VALID)) {
					//draw camera FOV
//					System.out.println("FH: " + Math.toDegrees(os.getActualHorizontalFieldOfView()) + "; FV: " + Math.toDegrees(os.getActualVerticalFieldOfView()));
//					os.setActualCentrelineAzimuthAngle(0);
//					os.setActualCentrelineElevationAngle((float)Math.toRadians(-90));
					float fh2 = os.getActualHorizontalFieldOfView()/2;
					float fv2 = os.getActualVerticalFieldOfView()/2;
					float vehicleRoll = is.getPhi();
					float vehiclePitch = is.getTheta();
					float vehicleYaw = is.getPsi();
//					System.out.println("Centreline Az: " + Math.toDegrees(os.getActualCentrelineAzimuthAngle()) + "; El: " + Math.toDegrees(os.getActualCentrelineElevationAngle()));
//					float[] azimuthElevationEarth = new float[2];
//					CoordinatesHelper.transformVehicleToEarthReference(azimuthElevationEarth, os.getActualCentrelineAzimuthAngle(), os.getActualCentrelineElevationAngle(), is.getPsi(), is.getTheta(), is.getPhi());
					Coordinates p = new Coordinates();
					Polygon poly = new Polygon();
//					System.out.println("Centreline world Az: " + Math.toDegrees(azimuthElevationEarth[0]) + "; El: " + Math.toDegrees(azimuthElevationEarth[1]));
					
					//translate graphics back to world reference
					Point2D pos = elem.getPixelPosition();
					g.translate(-pos.getX(), -pos.getY());
					
					//point 1
					if(CoordinatesHelper.calculateRayGroundIntersectionLocation(p, cameraPosition, calculateRayAzimuth(os.getActualCentrelineAzimuthAngle(), os.getActualCentrelineElevationAngle(), fh2, fv2), os.getActualCentrelineElevationAngle()+fv2, vehicleRoll, vehiclePitch, vehicleYaw)) {
						addPoint(poly, CUCSViewHelper.toGeoPosition(p));
					}

					//TODO IMPLEMENT EXCEPTION WHEN LOOKING TO SKY

					//point 3
					if(CoordinatesHelper.calculateRayGroundIntersectionLocation(p, cameraPosition, calculateRayAzimuth(os.getActualCentrelineAzimuthAngle(), os.getActualCentrelineElevationAngle(), fh2, -fv2), os.getActualCentrelineElevationAngle()-fv2, vehicleRoll, vehiclePitch, vehicleYaw)) {
						addPoint(poly, CUCSViewHelper.toGeoPosition(p));
					}

					//point 2
					if(CoordinatesHelper.calculateRayGroundIntersectionLocation(p, cameraPosition, calculateRayAzimuth(os.getActualCentrelineAzimuthAngle(), os.getActualCentrelineElevationAngle(), -fh2, -fv2), os.getActualCentrelineElevationAngle()-fv2, vehicleRoll, vehiclePitch, vehicleYaw)) {
						addPoint(poly, CUCSViewHelper.toGeoPosition(p));
					}

					//point 4
					if(CoordinatesHelper.calculateRayGroundIntersectionLocation(p, cameraPosition, calculateRayAzimuth(os.getActualCentrelineAzimuthAngle(), os.getActualCentrelineElevationAngle(), -fh2, fv2), os.getActualCentrelineElevationAngle()+fv2, vehicleRoll, vehiclePitch, vehicleYaw)) {
						addPoint(poly, CUCSViewHelper.toGeoPosition(p));
					}

					//draw centreline location
					Point2D a = getMap().getTileFactory().geoToPixel(new GeoPosition(Math.toDegrees(os.getLatitudeOfImageCentre()), Math.toDegrees(os.getLongitudeOfImageCentre())), getMap().getZoom());
					g.setColor(new Color(255,255,255,150));
					g.drawLine((int)a.getX(), (int)a.getY()-2, (int)a.getX(), (int)a.getY()+2);
					g.drawLine((int)a.getX()-2, (int)a.getY(), (int)a.getX()+2, (int)a.getY());
					
					//draw FOV
					g.setColor(new Color(0,120,0,50));
					g.fill(poly);
					g.setColor(new Color(0,80,0,150));
					g.draw(poly);
				}		
			}
		}
		
		drawDefaultLabel(g, elem);
		return null;
	}

	private float calculateRayAzimuth(float centrelineAz, float centrelineEl, float fovH, float fovV) {
		//FIXME still having problems on azimuth
		return centrelineAz + (float)Math.atan(Math.tan(fovH)/Math.tan((MathHelper.HALF_PI-centrelineEl)-fovV));
	}

	private void addPoint(Polygon poly, GeoPosition point) {
		Point2D p = getMap().getTileFactory().geoToPixel(point, getMap().getZoom());
		poly.addPoint((int)p.getX(), (int)p.getY());
	}

//	private int[] calculateRelativePosition(GeoPosition cameraPos, Coordinates point) {
//		float longDiff = (float)Math.toRadians(point.getLongitude()-cameraPos.getLongitude());
//		float latDiff = (float)Math.toRadians(point.getLatitude()-cameraPos.getLatitude());
//		float dx = CoordinatesHelper.longitudeLengthToMeters(longDiff, cameraPos.getLatitude());
//		float dy = CoordinatesHelper.latitudeLengthToMeters(latDiff, cameraPos.getLatitude());
//		int xDistPixels = (int)ViewHelper.metersToPixels(dx, cameraPos, getMap())[0];
//		int yDistPixels = (int)ViewHelper.metersToPixels(dy, cameraPos, getMap())[1];
//		return new int[] {xDistPixels*(int)Math.signum(dx), yDistPixels*(int)Math.signum(dy)};
//	}
	
	@Override
	public int getLayerNumber() {
		return 2;
	}

}
