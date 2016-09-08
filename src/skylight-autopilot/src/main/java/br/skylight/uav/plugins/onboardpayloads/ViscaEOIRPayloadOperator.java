package br.skylight.uav.plugins.onboardpayloads;

import java.util.logging.Logger;

import br.skylight.commons.Coordinates;
import br.skylight.commons.EOIRPayload;
import br.skylight.commons.Payload;
import br.skylight.commons.dli.enums.ImageOutputState;
import br.skylight.commons.dli.enums.ImagePositionValidity;
import br.skylight.commons.dli.enums.PayloadType;
import br.skylight.commons.dli.enums.SensorMode;
import br.skylight.commons.dli.enums.SensorPointingMode;
import br.skylight.commons.dli.enums.SetEOIRPointingMode;
import br.skylight.commons.dli.enums.SetZoom;
import br.skylight.commons.dli.enums.StationDoor;
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
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.uav.plugins.control.Commander;
import br.skylight.uav.plugins.payload.EOIRPayloadOperator;
import br.skylight.uav.plugins.payload.PayloadOperatorExtensionPoint;
import br.skylight.uav.plugins.payload.PayloadService;
import br.skylight.uav.services.ActuatorsService;
import br.skylight.uav.services.InstrumentsService;
import br.skylight.uav.services.ActuatorsService.RotationReference;

@ExtensionPointImplementation(extensionPointDefinition=PayloadOperatorExtensionPoint.class)
public class ViscaEOIRPayloadOperator extends EOIRPayloadOperator {

	//min distance from home for turning transmitter on
	private static final int DIST_HOME_TURN_ON = 200;
	
	private static final Logger logger = Logger.getLogger(ViscaEOIRPayloadOperator.class.getName());
	
	public static Payload PAYLOAD = new Payload();
	public static EOIRPayload EOIR_PAYLOAD = new EOIRPayload();
	static {
		PAYLOAD.setPayloadType(PayloadType.EOIR);
		PAYLOAD.setAuthorizeAnyCUCS(true);
		PAYLOAD.setAuthorizeOverrideAnyCUCS(true);
		PAYLOAD.setUniqueStationNumber(1);
		PAYLOAD.setStationDoor(StationDoor.NO);
		EOIR_PAYLOAD.getEoIrConfiguration().setAzimuthMin((float)Math.toRadians(-180));
		EOIR_PAYLOAD.getEoIrConfiguration().setAzimuthMax((float)Math.toRadians(180));
		EOIR_PAYLOAD.getEoIrConfiguration().setElevationMin((float)Math.toRadians(-180));
		EOIR_PAYLOAD.getEoIrConfiguration().setElevationMax((float)Math.toRadians(0));
		EOIR_PAYLOAD.getEoIrConfiguration().setEoHorizontalImageDimension(480);
		EOIR_PAYLOAD.getEoIrConfiguration().setEoVerticalImageDimension(320);
		EOIR_PAYLOAD.getEoIrConfiguration().setEoIrType("Sony Block Camera");
		EOIR_PAYLOAD.getEoIrConfiguration().setEoIrTypeRevisionLevel(1);
		EOIR_PAYLOAD.setHorizontalFOVAt1X((float)Math.toRadians(55));
		EOIR_PAYLOAD.setPositionXRelativeToAV(0F);
		EOIR_PAYLOAD.setPositionYRelativeToAV(0);
		EOIR_PAYLOAD.setPositionZRelativeToAV(0.15F);
	}

	private int zoom = 1;
	private EOIRLaserPayloadCommand eoIrLaserPayloadCommand = new EOIRLaserPayloadCommand();
	private PayloadSteeringCommand payloadSteeringCommand = new PayloadSteeringCommand();
	private SystemOperatingModeState currentModeState = SystemOperatingModeState.OFF;
	private float[] azimuthElevationResult2 = new float[2];
	private boolean poweredOn = false;
	private boolean imageInverted = false;
	private int lastAddressedSensor = 1;
	
	@ServiceInjection
	public PayloadService payloadService;
	
	@ServiceInjection
	public InstrumentsService instrumentsService;
	
	@ServiceInjection
	public ActuatorsService actuatorsService;
	
	@MemberInjection
	public ViscaCameraGateway viscaCameraGateway;
	
	@ServiceInjection
	public Commander commander;
	
	public ViscaEOIRPayloadOperator() {
		super(PAYLOAD, EOIR_PAYLOAD);
	}

	@Override
	public void onActivate() throws Exception {
		super.onActivate();
		deactivatePayload();
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
			if(!currentModeState.equals(SystemOperatingModeState.ACTIVE) && eoIrLaserPayloadCommand.getSystemOperatingMode().equals(SystemOperatingMode.ACTIVE)) {
				activatePayload();
			} else if(!currentModeState.equals(SystemOperatingModeState.OFF) && eoIrLaserPayloadCommand.getSystemOperatingMode().equals(SystemOperatingMode.OFF)) {
				deactivatePayload();
			}
			
			//command profile change in visca
			if(lastAddressedSensor!=eoIrLaserPayloadCommand.getAddressedSensor().getData()) {
				commandProfileChange();
			}
			
			//steering operations
			onMessageReceived(payloadSteeringCommand, eoIrLaserPayloadCommand);
			
		//M206
		} else if(message instanceof PayloadBayCommand) {
			PayloadBayCommand m = (PayloadBayCommand)message;
			//no door
			
		//M804
		} else if(message instanceof PayloadActionWaypoint) {
			PayloadActionWaypoint m = (PayloadActionWaypoint)message;

			//payload system mode
			if(m.getSetSensor1Mode().equals(SensorMode.TURN_ON)) {
				activatePayload();
			} else if(m.getSetSensor1Mode().equals(SensorMode.TURN_OFF)) {
				deactivatePayload();
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
	
	//switch camera profile according to addressed sensor
	private void commandProfileChange() {
		lastAddressedSensor = (int)eoIrLaserPayloadCommand.getAddressedSensor().getData();
		logger.info("Station #"+getPayload().getUniqueStationNumber()+": recalling camera profile=" + lastAddressedSensor);
		if(lastAddressedSensor>=0 && lastAddressedSensor<=5) {
			viscaCameraGateway.recallMemoryPreset(lastAddressedSensor);
		}
	}

	private void activatePayload() {
		//From now the video transmitter will be switch on/off automatically. The transmitter will be turn on
		//only when it is at least Xm far from manual recovery position
		currentModeState = SystemOperatingModeState.ACTIVE;
//		powerOnDevices();//this will be called in step()
		logger.info("Station #"+getPayload().getUniqueStationNumber()+": activated");
		powerOnDevices();
	}

	private void deactivatePayload() {
		currentModeState = SystemOperatingModeState.OFF;
		powerOffDevices();
		logger.info("Station #"+getPayload().getUniqueStationNumber()+": deactivated");
		powerOffDevices();
	}

	private void onMessageReceived(PayloadSteeringCommand payloadSteeringCommand, EOIRLaserPayloadCommand eoIrLaserPayloadCommand) {
		if(!eoIrLaserPayloadCommand.getSetEOIRPointingMode().equals(SetEOIRPointingMode.NO_VALUE)) {
			//point to an angle relative to vehicle
			if(eoIrLaserPayloadCommand.getSetEOIRPointingMode().equals(SetEOIRPointingMode.ANGLE_RELATIVE_TO_UAV)) {
				actuatorsService.setCameraOrientation(payloadSteeringCommand.getSetCentrelineAzimuthAngle(), payloadSteeringCommand.getSetCentrelineElevationAngle(), RotationReference.VEHICLE);
			}
			if(payloadSteeringCommand.getSetZoom().equals(SetZoom.ZOOM_IN)) {
				zoom = (int)MathHelper.clamp(zoom+1, 1, 36);
			} else if(payloadSteeringCommand.getSetZoom().equals(SetZoom.ZOOM_OUT)) {
				zoom = (int)MathHelper.clamp(zoom-1, 1, 36);
			} else if(payloadSteeringCommand.getSetZoom().equals(SetZoom.USE_FOV)) {
				zoom = (int)MathHelper.getZoom(payloadSteeringCommand.getSetHorizontalFieldOfView(), getEoIrPayload().getHorizontalFOVAt1X());
			}
			if(!payloadSteeringCommand.getSetZoom().equals(SetZoom.NO_CHANGE)) {
				logger.info("Station #"+getPayload().getUniqueStationNumber()+": zoom=" + zoom);
				if(zoom>=1 && zoom<=25) {
					viscaCameraGateway.setZoomRatio(zoom);
				}
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
			m.getAddressedSensor().setData(lastAddressedSensor);
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
			return false;//not supported
		}
		return false;
	}
	
	@Override
	public void step() throws Exception {
		//automatically switch off transmitter if too near manual recovery location
		//this is done because the video transmitter is near RC control spectrum (2.4GHz)
		if(currentModeState.equals(SystemOperatingModeState.ACTIVE)) {
			Coordinates home = commander.getHomePosition();
			if(gpsService.getPosition().distance(home)<DIST_HOME_TURN_ON && poweredOn) {
				logger.info("Turning camera off. Too near home position. d=" + gpsService.getPosition().distance(home) + " m");
				powerOffDevices();
			} else if(gpsService.getPosition().distance(home)>=DIST_HOME_TURN_ON && !poweredOn) {
				logger.info("Turning camera on. Far from home position. d=" + gpsService.getPosition().distance(home) + " m");
				powerOnDevices();
			}
		}
		
		//verify if there is a need to reverse camera image because of gimbal positioning
		//change to inverted picture
		if(instrumentsService.getInstrumentsInfos().isGimbalInverted() && !imageInverted) {
			viscaCameraGateway.setPictureFlip(true);
			viscaCameraGateway.setPictureReverse(true);
			imageInverted = true;
			logger.info("Camera picture is inverted");
		//change to normal picture
		} else if(!instrumentsService.getInstrumentsInfos().isGimbalInverted() && imageInverted) {
			viscaCameraGateway.setPictureFlip(false);
			viscaCameraGateway.setPictureReverse(false);
			imageInverted = false;
			logger.info("Camera picture is normal");
		}
		
		//keep a lat/long location pointed by camera even when the vehicle is moving/changing attitude
		if(eoIrLaserPayloadCommand.getSetEOIRPointingMode().equals(SetEOIRPointingMode.LAT_LONG_SLAVED) && payloadSteeringCommand.getLatitude()!=0 && payloadSteeringCommand.getLongitude()!=0) {
			float[] azimuthElevation = calculatePayloadAzimuthElevationForStaringPosition(payloadSteeringCommand.getLatitude(), payloadSteeringCommand.getLongitude());
			if(!Float.isNaN(azimuthElevation[0])) {
				actuatorsService.setCameraOrientation(azimuthElevation[0], azimuthElevation[1], RotationReference.EARTH);
			} else {
				logger.info("Couldn't calculate azimuth/elevation for payload. targetLat=" + Math.toDegrees(payloadSteeringCommand.getLatitude()) + "," + Math.toDegrees(payloadSteeringCommand.getLongitude()) + "; vehicleLat=" + gpsService.getPosition().getLatitude() + "; vehicleLong=" + gpsService.getPosition().getLongitude());
			}
		}
	}

	private void powerOnDevices() {
		if(!poweredOn) {
//			viscaCameraGateway.setCameraPower(true);
			commandProfileChange();
			actuatorsService.setVideoTransmitterPower(true);//will turn camera on too
			logger.info("Camera devices powered on");
			poweredOn = true;
		}
	}

	private void powerOffDevices() {
		if(poweredOn) {
//			viscaCameraGateway.setCameraPower(false);
			actuatorsService.setVideoTransmitterPower(false);//this will turn camera off
			actuatorsService.setCameraOrientation(0, -180, RotationReference.VEHICLE);//park position
			logger.info("Camera devices powered off. Gimbal parked.");
			poweredOn = false;
		}
	}
	
	
}
