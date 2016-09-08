package br.skylight.uav.plugins.control.instruments;

import java.util.logging.Logger;

import traer.physics.Vector3D;
import br.skylight.commons.AGLAltitudeMode;
import br.skylight.commons.Coordinates;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.dli.vehicle.VehicleSteeringCommand;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.infra.CounterStats;
import br.skylight.commons.infra.LinearRegression;
import br.skylight.commons.infra.Worker;
import br.skylight.commons.infra.dted.DTEDLoader;
import br.skylight.commons.plugin.annotations.ServiceDefinition;
import br.skylight.commons.plugin.annotations.ServiceImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.services.StorageService;
import br.skylight.uav.plugins.storage.RepositoryService;
import br.skylight.uav.services.GPSService;
import br.skylight.uav.services.InstrumentsService;

@ServiceDefinition
@ServiceImplementation(serviceDefinition=AdvancedInstrumentsService.class)
public class AdvancedInstrumentsService extends Worker {

	private static final Logger logger = Logger.getLogger(AdvancedInstrumentsService.class.getName());
	
	private Vector3D groundSpeed = new Vector3D();
	private Vector3D windSpeed = new Vector3D();
	private DTEDLoader dtedLoader;
	
	private SkylightVehicleConfigurationMessage skylightVehicleConfiguration;
	
	private CounterStats verticalSpeedCalc = new CounterStats(1000);
//	private TimedMovingAverage staticPressureAverager = new TimedMovingAverage(20, 30);
//	private MovingAverage staticPressureAverager = new MovingAverage(20);
	private LinearRegression pitotPressureRegression = new LinearRegression(20);
	
	@ServiceInjection
	public InstrumentsService instrumentsService;
	@ServiceInjection
	public GPSService gpsService;
	@ServiceInjection
	public RepositoryService repositoryService;
	@ServiceInjection
	public StorageService storageService;

	@Override
	public void onActivate() throws Exception {
		reloadVehicleConfiguration();
		dtedLoader = new DTEDLoader(storageService.resolveDir("dted"), 10);
	}

	@Override
	public void step() throws Exception {
		//used to avoid too much flutuations in sensor readings
//		staticPressureAverager.addSample(instrumentsService.getStaticPressure());
		pitotPressureRegression.addSample(instrumentsService.getPitotPressure());
		verticalSpeedCalc.setValue(getAltitude(AltitudeType.PRESSURE));
	}
	
	public float getAltitude(AltitudeType altitudeType) {
		if(altitudeType.equals(AltitudeType.AGL)) {
			return getAltitudeAGL();
			
		} else if(altitudeType.equals(AltitudeType.BARO)) {
//			return calculateBarometricAltitude(staticPressureAverager.getAverage(), getPressureAtSeaLevel());
//			return calculateBarometricAltitude(staticPressureAverager.getAverage(), getPressureAtSeaLevel());
			return calculateBarometricAltitude(instrumentsService.getStaticPressure(), getPressureAtSeaLevel());
			
		} else if(altitudeType.equals(AltitudeType.PRESSURE)) {
			return calculateBarometricAltitude(instrumentsService.getStaticPressure(), VehicleSteeringCommand.STANDARD_MSL_PRESSURE);
			
		} else if(altitudeType.equals(AltitudeType.WGS84)) {
			return gpsService.getPosition().getAltitude();
			
		} else {
			logger.warning("Unrecognized altitude type. Using 'WGS84' instead. altitudeType=" + altitudeType);
			return gpsService.getPosition().getAltitude();
		}
	}
	
//	public boolean isAltitudeTypeAvailable(AltitudeType altitudeType) {
//		if(altitudeType.equals(AltitudeType.AGL)) {
//			if(skylightVehicleConfiguration.getAglAltitudeMode().equals(AGLAltitudeMode.GPS_AND_TERRAIN_DATA)) {
//				return false;
//			} else {
//				return repositoryService.getGroundLevelAltitudes()!=null;
//			}
//		}
//		return false;
//	}

	protected float getAltitudeAGL() {
		if(repositoryService.getGroundLevelAltitudes()!=null) {
			if(skylightVehicleConfiguration.getAglAltitudeMode().equals(AGLAltitudeMode.PRESSURE_AT_AGL_SETUP)) {
				return getAltitude(AltitudeType.PRESSURE) - repositoryService.getGroundLevelAltitudes().getAltitudePressure();

			} else if(skylightVehicleConfiguration.getAglAltitudeMode().equals(AGLAltitudeMode.BAROMETRIC_AT_AGL_SETUP)) {
				return getAltitude(AltitudeType.BARO) - repositoryService.getGroundLevelAltitudes().getAltitudeBarometric();

			} else if(skylightVehicleConfiguration.getAglAltitudeMode().equals(AGLAltitudeMode.GPS_AT_AGL_SETUP)) {
				return gpsService.getPosition().getAltitude() - repositoryService.getGroundLevelAltitudes().getAltitudeGpsWGS84();

			} else if(skylightVehicleConfiguration.getAglAltitudeMode().equals(AGLAltitudeMode.GPS_AND_TERRAIN_DATA)) {
				return getAGLAltitudeUsingTerrainData(gpsService.getPosition());
				
			} else {
				logger.warning("Unrecognized AGL altitude mode found. Using 'GPS_FROM_FIRST_POSITION'. mode=" + skylightVehicleConfiguration.getAglAltitudeMode());
				skylightVehicleConfiguration.setAglAltitudeMode(AGLAltitudeMode.GPS_AT_AGL_SETUP);
				return getAltitudeAGL();
			}
		} else {
//			logger.fine("Cannot calculate AGL because ground level is unknown. Using terrain data.");
			return getAGLAltitudeUsingTerrainData(gpsService.getPosition());
		}
	}
	
	protected float getAGLAltitudeUsingTerrainData(Coordinates position) {
		int groundFromWgs = dtedLoader.getElevation(position.getLatitude(), position.getLongitude());
		//terrain data not available
		if(groundFromWgs==-500) {
			logger.fine("DTED data not found for location. lat=" + position.getLatitude() + "; long=" + position.getLongitude());
		}
		return position.getAltitude() - groundFromWgs;
	}

	public void reloadVehicleConfiguration() {
		skylightVehicleConfiguration = repositoryService.getSkylightVehicleConfiguration();
	}

	public float getVerticalSpeed() {
		return -(float)verticalSpeedCalc.getRate();
	}

	/**
	 * http://electronicdesign.com/article/test-and-measurement/linear-pitot-tube-air-speed-indicator6396.aspx
	 */
	public float getIAS() {
		//1.225 kg/m3, the air density at sea level and 15 degrees Celsius
		//v = sqrt(2P/D); - P-pitot pressure; D-air density; v-airspeed
//		System.out.println("PS " + instrumentsService.getPitotPressure() + " " + ((float)Math.sqrt((2*instrumentsService.getPitotPressure())/1.225)));
		if(instrumentsService.getPitotPressure()>0) {
			return (float)Math.sqrt(2*pitotPressureRegression.calculateBestYValue()/1.225);//abs is used to avoid NaN number in case of negative pitot readings
		} else {
			return 0;
		}
	}
	
	/**
	 * See http://wahiduddin.net/calc/density_altitude.htm too
	 */
	private float calculateBarometricAltitude(float staticPressure, float altimeterSetting) {
//		http://www.challengers101.com/AltSettings.html
//		return MeasureHelper.feetToMeters((float)MeasureHelper.convertPascalsToInHg(altimeterSetting-staticPressure)*1000F);
		//convert from geopotential altitude to geometric altitude
//		geometricAltitude = (6356F*geopotentialAltitude)/(6356F-geopotentialAltitude)
		return (float)(44330.0 * (1-Math.pow((staticPressure/altimeterSetting),0.19)));
	}

	public Vector3D getSpeed() {
		groundSpeed.setX(CoordinatesHelper.getUComponent(gpsService.getCourseHeading(), gpsService.getGroundSpeed()));
		groundSpeed.setY(CoordinatesHelper.getVComponent(gpsService.getCourseHeading(), gpsService.getGroundSpeed()));
		groundSpeed.setZ(getVerticalSpeed());
		return groundSpeed;
	}

	public Vector3D getWindSpeed() {
//		windSpeed.setX(getSpeed().x() - (CoordinatesHelper.getUComponent(instrumentsService.getYaw(), getTAS()*(float)Math.cos(instrumentsService.getPitch()))));
//		windSpeed.setY(getSpeed().y() - (CoordinatesHelper.getVComponent(instrumentsService.getYaw(), getTAS()*(float)Math.cos(instrumentsService.getPitch()))));
		windSpeed.setX(getSpeed().x() - (float)(Math.cos(instrumentsService.getYaw()) * getTAS() * Math.cos(instrumentsService.getPitch())));
		windSpeed.setY(getSpeed().y() - (float)(Math.sin(instrumentsService.getYaw()) * getTAS() * Math.cos(instrumentsService.getPitch())));
//		windSpeed.setY(getSpeed().y() - (CoordinatesHelper.getVComponent(instrumentsService.getYaw(), getTAS()*(float)Math.cos(instrumentsService.getPitch()))));
//		windSpeed.setX(windSpeedVAvg.getAverage());
//		windSpeed.setY(windSpeedUAvg.getAverage());
//		windSpeed.setZ(groundSpeed.z() - (float)(Math.sin(instrumentsService.getPitch())*getIAS()));
//		System.out.println(">x"+ getGroundSpeed().x() + "   " + (CoordinatesHelper.getUComponent(instrumentsService.getYaw(), getTAS()*(float)Math.cos(instrumentsService.getPitch()))));
//		System.out.println(">y"+ getGroundSpeed().y() + "   " + (CoordinatesHelper.getVComponent(instrumentsService.getYaw(), getTAS()*(float)Math.cos(instrumentsService.getPitch()))));
		return windSpeed;
	}

	/**
	 * Based on http://www.csgnetwork.com/tasinfocalc.html
	 * This is not an absolute calculation because it uses an estimated temperature variation (2% OAT)
	 */
	public float getTAS() {
		return getIAS() * (1+(getAltitude(AltitudeType.BARO)/304.8F)*0.02F);
	}

	public float getPressureAtSeaLevel() {
		VehicleSteeringCommand vc = repositoryService.getVehicleSteeringCommand();
		if(vc!=null && vc.getAltimeterSetting()>10) {//simple validation
			return vc.getAltimeterSetting();
		} else {
			return VehicleSteeringCommand.STANDARD_MSL_PRESSURE;
		}
	}

	/**
	 * Get magnetic declination for current lat/long/alt according to WMM model.
	 * WMM.COF data file is valid from 2010-2025.
	 * @return Magnetic declination in radians
	 */
	public float getMagneticDeclination() {
		return (float)Math.toRadians(GeoMagJ.getCurrentDeclination(	gpsService.getPosition().getLatitude(), 
																	gpsService.getPosition().getLongitude(), 
																	gpsService.getPosition().getAltitude()));
	}

}
