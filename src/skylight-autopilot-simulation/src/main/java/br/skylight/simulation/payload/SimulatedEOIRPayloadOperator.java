package br.skylight.simulation.payload;

import java.util.logging.Logger;

import br.skylight.commons.Coordinates;
import br.skylight.commons.EOIRPayload;
import br.skylight.commons.Payload;
import br.skylight.commons.dli.enums.DoorState;
import br.skylight.commons.dli.enums.ImageOutputState;
import br.skylight.commons.dli.enums.ImagePositionValidity;
import br.skylight.commons.dli.enums.PayloadType;
import br.skylight.commons.dli.enums.SensorMode;
import br.skylight.commons.dli.enums.SensorPointingMode;
import br.skylight.commons.dli.enums.SetEOIRPointingMode;
import br.skylight.commons.dli.enums.SetZoom;
import br.skylight.commons.dli.enums.SystemOperatingMode;
import br.skylight.commons.dli.enums.SystemOperatingModeState;
import br.skylight.commons.dli.mission.PayloadActionWaypoint;
import br.skylight.commons.dli.payload.EOIRLaserOperatingState;
import br.skylight.commons.dli.payload.EOIRLaserPayloadCommand;
import br.skylight.commons.dli.payload.PayloadBayCommand;
import br.skylight.commons.dli.payload.PayloadBayStatus;
import br.skylight.commons.dli.payload.PayloadSteeringCommand;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.infra.CoordinatesHelper;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.simulation.flightgear.FlightGearServices;
import br.skylight.simulation.xplane.XPlaneServices;
import br.skylight.uav.plugins.payload.EOIRPayloadOperator;
import br.skylight.uav.plugins.payload.PayloadOperatorExtensionPoint;
import br.skylight.uav.plugins.payload.PayloadService;
import br.skylight.uav.services.ActuatorsService;
import br.skylight.uav.services.InstrumentsService;
import br.skylight.uav.services.ActuatorsService.RotationReference;

@ExtensionPointImplementation(extensionPointDefinition=PayloadOperatorExtensionPoint.class)
public class SimulatedEOIRPayloadOperator extends EOIRPayloadOperator {

	private static final Logger logger = Logger.getLogger(SimulatedEOIRPayloadOperator.class.getName());
	
	public static Payload PAYLOAD = new Payload();
	public static EOIRPayload EOIR_PAYLOAD = new EOIRPayload();
	static {
		PAYLOAD.setPayloadType(PayloadType.EOIR);
		PAYLOAD.setAuthorizeAnyCUCS(true);
		PAYLOAD.setAuthorizeOverrideAnyCUCS(true);
		PAYLOAD.setUniqueStationNumber(7);
		EOIR_PAYLOAD.getEoIrConfiguration().setAzimuthMin((float)Math.toRadians(-90));
		EOIR_PAYLOAD.getEoIrConfiguration().setAzimuthMax((float)Math.toRadians(90));
		EOIR_PAYLOAD.getEoIrConfiguration().setElevationMin((float)Math.toRadians(-180));
		EOIR_PAYLOAD.getEoIrConfiguration().setElevationMax((float)Math.toRadians(0));
		EOIR_PAYLOAD.getEoIrConfiguration().setEoHorizontalImageDimension(480);
		EOIR_PAYLOAD.getEoIrConfiguration().setEoVerticalImageDimension(320);
		EOIR_PAYLOAD.getEoIrConfiguration().setEoIrType("Simulated");
		EOIR_PAYLOAD.getEoIrConfiguration().setEoIrTypeRevisionLevel(1);
		EOIR_PAYLOAD.setHorizontalFOVAt1X((float)Math.toRadians(55));
		EOIR_PAYLOAD.setPositionXRelativeToAV(0.1F);
		EOIR_PAYLOAD.setPositionYRelativeToAV(0);
		EOIR_PAYLOAD.setPositionZRelativeToAV(0.1F);
	}

	private float zoom = 1;
	private DoorState doorState = DoorState.CLOSED;
	private EOIRLaserPayloadCommand eoIrLaserPayloadCommand = new EOIRLaserPayloadCommand();
	private PayloadSteeringCommand payloadSteeringCommand = new PayloadSteeringCommand();
	private SystemOperatingModeState currentModeState = SystemOperatingModeState.OFF;
	private float[] azimuthElevationResult2 = new float[2];
	
	@ServiceInjection
	public PayloadService payloadService;
	
	@ServiceInjection
	public InstrumentsService instrumentsService;
	
	@ServiceInjection
	public ActuatorsService actuatorsService;
	
	public SimulatedEOIRPayloadOperator() {
		super(PAYLOAD, EOIR_PAYLOAD);
	}

	@Override
	public void onActivate() throws Exception {
		super.onActivate();
	}

	@Override
	public void onDeactivate() throws Exception {
		if(actuatorsService!=null) {
			actuatorsService.setVideoTransmitterPower(false);
		}
	}
	
	@Override
	public void onPayloadMessageReceived(Message message) {
		//M200
		if(message instanceof PayloadSteeringCommand) {
			payloadSteeringCommand = (PayloadSteeringCommand)message;
			onMessageReceived(payloadSteeringCommand, eoIrLaserPayloadCommand);
			
		//M201
		} else if(message instanceof EOIRLaserPayloadCommand) {
			eoIrLaserPayloadCommand = (EOIRLaserPayloadCommand)message;

			//system operating states switch
			if(eoIrLaserPayloadCommand.getSystemOperatingMode().equals(SystemOperatingMode.ACTIVE)) {
				turnOn();
			} else if(eoIrLaserPayloadCommand.getSystemOperatingMode().equals(SystemOperatingMode.OFF)) {
				turnOff();
			} else if(eoIrLaserPayloadCommand.getSystemOperatingMode().equals(SystemOperatingMode.STANDBY)) {
				standBy();
			}
			
			//steering operations
			onMessageReceived(payloadSteeringCommand, eoIrLaserPayloadCommand);
			
		//M206
		} else if(message instanceof PayloadBayCommand) {
			PayloadBayCommand m = (PayloadBayCommand)message;
			doorState = m.getPayloadBayDoors();
			System.out.println("Station #" + getPayload().getUniqueStationNumber() + ": Door state=" + doorState);
			
		//M804
		} else if(message instanceof PayloadActionWaypoint) {
			PayloadActionWaypoint m = (PayloadActionWaypoint)message;

			//payload system mode
			if(m.getSetSensor1Mode().equals(SensorMode.TURN_ON)) {
				turnOn();
			} else if(m.getSetSensor1Mode().equals(SensorMode.TURN_OFF)) {
				turnOff();
			} else if(m.getSetSensor1Mode().equals(SensorMode.GOTO_STANDBY)) {
				standBy();
			}
			
			//payload orientation
			if(m.getSetSensorPointingMode().equals(SensorPointingMode.ANGLE_RELATIVE_TO_AV)) {
				eoIrLaserPayloadCommand.setSetEOIRPointingMode(SetEOIRPointingMode.ANGLE_RELATIVE_TO_UAV);
				actuatorsService.setCameraOrientation(m.getPayloadAz(), m.getPayloadEl(), RotationReference.VEHICLE);
			} else if(m.getSetSensorPointingMode().equals(SensorPointingMode.LAT_LONG_SLAVED)) {
				eoIrLaserPayloadCommand.setSetEOIRPointingMode(SetEOIRPointingMode.LAT_LONG_SLAVED);
				payloadSteeringCommand.setLatitude(m.getStarepointLatitude());
				payloadSteeringCommand.setLongitude(m.getStarepointLongitude());
				payloadSteeringCommand.setAltitude(m.getStarepointAltitude());
				payloadSteeringCommand.setAltitudeType(m.getStarepointAltitudeType());
			}
		}
	}
	
	private void turnOn() {
		actuatorsService.setVideoTransmitterPower(true);
		currentModeState = SystemOperatingModeState.ACTIVE;
		System.out.println("Station #" + getPayload().getUniqueStationNumber() + ": Activate");
	}

	private void turnOff() {
		actuatorsService.setVideoTransmitterPower(false);
		currentModeState = SystemOperatingModeState.OFF;
		System.out.println("Station #" + getPayload().getUniqueStationNumber() + ": Off");
	}

	private void standBy() {
		currentModeState = SystemOperatingModeState.STANDBY;
		System.out.println("Station #" + getPayload().getUniqueStationNumber() + ": Standby");
	}
	
	private void onMessageReceived(PayloadSteeringCommand payloadSteeringCommand, EOIRLaserPayloadCommand eoIrLaserPayloadCommand) {
		if(!eoIrLaserPayloadCommand.getSetEOIRPointingMode().equals(SetEOIRPointingMode.NO_VALUE)) {
			//point to an angle relative to vehicle
			if(eoIrLaserPayloadCommand.getSetEOIRPointingMode().equals(SetEOIRPointingMode.ANGLE_RELATIVE_TO_UAV)) {
				actuatorsService.setCameraOrientation(payloadSteeringCommand.getSetCentrelineAzimuthAngle(), payloadSteeringCommand.getSetCentrelineElevationAngle(), RotationReference.VEHICLE);
			}
			if(payloadSteeringCommand.getSetZoom().equals(SetZoom.ZOOM_IN)) {
				zoom = (float)MathHelper.clamp(zoom+1, 1, 36);
			} else if(payloadSteeringCommand.getSetZoom().equals(SetZoom.ZOOM_OUT)) {
				zoom = (float)MathHelper.clamp(zoom-1, 1, 36);
			} else if(payloadSteeringCommand.getSetZoom().equals(SetZoom.USE_FOV)) {
				zoom = MathHelper.getZoom(payloadSteeringCommand.getSetHorizontalFieldOfView(), getEoIrPayload().getHorizontalFOVAt1X());
			}
			if(!payloadSteeringCommand.getSetZoom().equals(SetZoom.NO_CHANGE)) {
				System.out.println("Station #"+getPayload().getUniqueStationNumber()+": zoom=" + zoom);
			}
			
			//simulate zoom in FlightGear
			if(actuatorsService instanceof FlightGearServices) {
				FlightGearServices fs = ((FlightGearServices)actuatorsService);
				fs.setCameraFOV(MathHelper.getHorizontalFOV(zoom, (float)Math.toRadians(55)));
			}
			
			//simulate zoom in XPlane
			if(actuatorsService instanceof XPlaneServices) {
				XPlaneServices fs = ((XPlaneServices)actuatorsService);
				fs.setCameraZoom(zoom);
			}
			
		} else {
			logger.info("Received EoIrLaserPayloadCommand but no PayloadSteering is present for this payload. No action will be taken. station=" + getPayload().getUniqueStationNumber() + " ("+ getPayload().getPayloadType() +")");
		}
	}

	@Override
	public boolean prepareScheduledPayloadMessage(Message message) {
		if(super.prepareScheduledPayloadMessage(message)) {
			return true;
			
		//M302
		} else if(message instanceof EOIRLaserOperatingState) {
			EOIRLaserOperatingState m = (EOIRLaserOperatingState)message;
			if(payloadSteeringCommand!=null && eoIrLaserPayloadCommand!=null) {
				if(actuatorsService.getCameraOrientationReference().equals(RotationReference.VEHICLE)) {
					m.setActualCentrelineAzimuthAngle(actuatorsService.getCameraAzimuth());
					m.setActualCentrelineElevationAngle(actuatorsService.getCameraElevation());
				} else if(actuatorsService.getCameraOrientationReference().equals(RotationReference.EARTH)) {
					azimuthElevationResult2[0] = actuatorsService.getCameraAzimuth();
					azimuthElevationResult2[1] = actuatorsService.getCameraElevation();
					CoordinatesHelper.transformEarthToVehicleReference(azimuthElevationResult2, instrumentsService.getRoll(), instrumentsService.getPitch(), instrumentsService.getYaw());
					m.setActualCentrelineAzimuthAngle(azimuthElevationResult2[0]);
					m.setActualCentrelineElevationAngle(azimuthElevationResult2[1]);
				}
				m.setActualHorizontalFieldOfView(MathHelper.getHorizontalFOV(zoom, getEoIrPayload().getHorizontalFOVAt1X()));
				m.setActualVerticalFieldOfView(MathHelper.getVerticalFOV(zoom, getEoIrPayload().getHorizontalFOVAt1X(), getEoIrPayload().getEoIrConfiguration().getImageWidthToHeightRatio()));
				m.setActualSensorRotationAngle(0);
				m.setImageOutputState(ImageOutputState.EO);
				m.setPointModeState(eoIrLaserPayloadCommand.getSetEOIRPointingMode().getPointingModeState());
				m.setSystemOperatingModeState(currentModeState);
				m.setEoCameraStatus(eoIrLaserPayloadCommand.getSetEOSensorMode());

				//calculate camera image centre position
				Coordinates c = calculatePayloadImageCentrePosition(m.getActualCentrelineAzimuthAngle(), m.getActualCentrelineElevationAngle());
				if(c!=null) {
					m.setImagePosition(ImagePositionValidity.VALID);
					m.setLatitudeOfImageCentre(c.getLatitudeRadians());
					m.setLongitudeOfImageCentre(c.getLongitudeRadians());
					m.setAltitudeWGS84(c.getAltitude());
				} else {
					m.setImagePosition(ImagePositionValidity.NOT_VALID);
				}
			}
			return true;

		//M308
		} else if(message instanceof PayloadBayStatus) { 		
			PayloadBayStatus m = (PayloadBayStatus)message;
			m.setPayloadBayDoorStatus(doorState);
			return true;
		}
		return false;
	}
	
	@Override
	public void step() throws Exception {
		if(eoIrLaserPayloadCommand.getSetEOIRPointingMode().equals(SetEOIRPointingMode.LAT_LONG_SLAVED) && payloadSteeringCommand.getLatitude()!=0 && payloadSteeringCommand.getLongitude()!=0) {
			float[] azimuthElevation = calculatePayloadAzimuthElevationForStaringPosition(payloadSteeringCommand.getLatitude(), payloadSteeringCommand.getLongitude());
			if(!Float.isNaN(azimuthElevation[0])) {
				actuatorsService.setCameraOrientation(azimuthElevation[0], azimuthElevation[1], RotationReference.EARTH);
			} else {
				logger.warning("Couldn't calculate azimuth/elevation for payload. targetLat=" + Math.toDegrees(payloadSteeringCommand.getLatitude()) + "," + Math.toDegrees(payloadSteeringCommand.getLongitude()) + "; vehicleLat=" + gpsService.getPosition().getLatitude() + "; vehicleLong=" + gpsService.getPosition().getLongitude());
			}
		}
	}

}
