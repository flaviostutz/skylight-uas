package br.skylight.uav.plugins.payload;

import br.skylight.commons.Coordinates;
import br.skylight.commons.EOIRPayload;
import br.skylight.commons.Payload;
import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.payload.EOIRConfigurationState;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.plugins.control.instruments.AdvancedInstrumentsService;
import br.skylight.uav.plugins.storage.RepositoryService;
import br.skylight.uav.services.GPSService;
import br.skylight.uav.services.InstrumentsService;

public abstract class EOIRPayloadOperator extends PayloadOperatorExtensionPoint {

	private EOIRPayload eoIrPayload;
	private float[] azimuthElevationResult = new float[2];
	
	//reused instance
	private Coordinates cameraImageCentre = new Coordinates();

	@ServiceInjection
	public GPSService gpsService;
	
	@ServiceInjection
	public InstrumentsService instrumentsService;
	
	@ServiceInjection
	public RepositoryService repositoryService;
	
	@ServiceInjection
	public AdvancedInstrumentsService advancedInstrumentsService;
	
	private Coordinates accuratePosition = new Coordinates(0,0,0);
	
	public EOIRPayloadOperator(Payload payload, EOIRPayload eoIrPayload) {
		super(payload);
		this.eoIrPayload = eoIrPayload;
	}

	public EOIRPayload getEoIrPayload() {
		return eoIrPayload;
	}

	@Override
	public boolean prepareScheduledPayloadMessage(Message message) {
		if(super.prepareScheduledPayloadMessage(message)) {
			return true;
		//M301
		} else if(message instanceof EOIRConfigurationState) { 		
			EOIRConfigurationState m = (EOIRConfigurationState)message;
			m.copyFrom(getEoIrPayload().getEoIrConfiguration());
			return true;
		}
		return false;
	}
	
	/**
	 * Calculates the payload image centre position
	 * See 'picture 2' at http://www.gisdevelopment.net/technology/rs/me05_078a.htm for an ideia
	 * of what will be done here
	 * @return Image centre position with altitude in WGS84
	 */
	protected Coordinates calculatePayloadImageCentrePosition(float payloadCentrelineAzimuthAngle, float payloadCentrelineElevationAngle) {
		Coordinates raySource = new Coordinates();
		raySource.set(gpsService.getPosition());
		raySource.setAltitude(advancedInstrumentsService.getAltitude(AltitudeType.AGL) - getEoIrPayload().getPositionZRelativeToAV());
		if(CoordinatesHelper.calculateRayGroundIntersectionLocation(cameraImageCentre, raySource, payloadCentrelineAzimuthAngle, payloadCentrelineElevationAngle, instrumentsService.getRoll(), instrumentsService.getPitch(), instrumentsService.getYaw())) {
			//ground level altitude
			if(repositoryService.getGroundLevelAltitudes()!=null) {
				cameraImageCentre.setAltitude(repositoryService.getGroundLevelAltitudes().getAltitudeGpsWGS84());
			}
			return cameraImageCentre;
		} else {
			//image centre is not on ground
			return null;
		}
	}

	/**
	 * Calculates the azimuth/elevation of the payload so that it points to a desired
	 * position/altitude. The azimuth/elevation returned is in Earth frame reference so that 
	 * zero azimuth means payload aways pointing to north (despite of vehicle heading).
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	double dx;
	double dy;
	double a;
	double az;
	double el;
	/**
	 * Calculates the payload orientation to stare a subject (lat/long) in Earth reference
	 */
	public float[] calculatePayloadAzimuthElevationForStaringPosition(double subjectLatitude, double subjectLongitude) {
		//absolute distance from camera to stared point
		Coordinates pp = getAccuratePayloadPosition();
		//a = distance from camera to subject being stared
//		double a = CoordinatesHelper.calculateDistance(pp.getLatitudeRadians(), pp.getLongitudeRadians(), subjectLatitude, subjectLongitude);
		dx = CoordinatesHelper.longitudeLengthToMeters(subjectLongitude-pp.getLongitudeRadians(), subjectLatitude);
		dy = CoordinatesHelper.latitudeLengthToMeters(subjectLatitude-pp.getLatitudeRadians(), subjectLatitude);
		a = Math.sqrt(dx*dx+dy*dy);

		//normal azimuth/elevation
		az = CoordinatesHelper.mathToHeadingReference(Math.atan2(dy,dx));
		el = (float)(-Math.atan2(pp.getAltitude(), a));
		
		azimuthElevationResult[0] = (float)MathHelper.normalizeAngle(az);
		azimuthElevationResult[1] = (float)MathHelper.normalizeAngle(el);
		return azimuthElevationResult;
	}

	/**
	 * Calculates payload position considering GPS antenna and camera positioning in vehicle
	 * Altitude comes in AGL reference
	 * @return
	 */
	public Coordinates getAccuratePayloadPosition() {
		SkylightVehicleConfigurationMessage svc = repositoryService.getSkylightVehicleConfiguration();
		//rotate relative camera position 
		double rx = getEoIrPayload().getPositionXRelativeToAV() - svc.getGpsAntennaPositionX();
		double ry = getEoIrPayload().getPositionYRelativeToAV() - svc.getGpsAntennaPositionY();
		double mr = Math.sqrt(rx*rx + ry*ry);
		double yaw = CoordinatesHelper.headingToMathReference(instrumentsService.getYaw());
		double rz = getEoIrPayload().getPositionZRelativeToAV() - svc.getGpsAntennaPositionZ();
		//calculate corrected position
		CoordinatesHelper.calculateCoordinatesFromRelativePosition(accuratePosition, gpsService.getPosition(), mr*Math.cos(yaw), mr*Math.sin(yaw));
//		accuratePosition.setAltitude(advancedInstrumentsService.getAltitude(AltitudeType.AGL)-(float)rz);//RIGHT VERSION
		accuratePosition.setAltitude(advancedInstrumentsService.getAltitude(AltitudeType.WGS84)-(float)rz);//TESTS
		return accuratePosition;
	}
	
}
