package br.skylight.commons;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import br.skylight.commons.dli.enums.AltitudeType;
import br.skylight.commons.dli.enums.VehicleMode;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.skylight.SafetyActionForAlert;
import br.skylight.commons.dli.skylight.SkylightVehicleConfigurationMessage;
import br.skylight.commons.infra.IOHelper;

public class RulesOfSafety extends Message<RulesOfSafety> {

	//general
	private int maxFlightTimeMinutes;
	private SafetyAction actionOnGpsAndDataLinkLost;

	//manual recovery action configuration
	private Coordinates manualRecoveryLoiterLocation = new Coordinates();
	private AltitudeType manualRecoveryLoiterAltitudeType;
	private double manualRecoveryReachLoiterLocationTimeout;
	private double manualRecoveryLoiterTimeout;
	private SafetyAction manualRecoveryActionOnLoiterTimeout;
	
	//data link recovery
	private boolean dataLinkRecoveryEnabled;
	private double dataLinkTimeout;
	private byte dataLinkMinStrength;
	private int dataLinkMaxRecoveryRetries;
	private Coordinates dataLinkRecoveryLoiterLocation = new Coordinates();
	private AltitudeType dataLinkRecoveryLoiterAltitudeType;
	private double dataLinkTimeoutReachingLoiterLocation;
	private double dataLinkTimeoutLoiteringWaitingRecovery;
	private double dataLinkTimeStableLinkForSuccess;
	private SafetyAction dataLinkActionOnLinkRecoveryFailure;
	private VehicleMode dataLinkModeOnLinkRecoverySuccess;

	//GPS recovery
	private boolean gpsSignalRecoveryEnabled;
	private double gpsLinkTimeout;
	private int gpsMaxRecoveryRetries;
	private SafetyAction gpsLinkRecoveryAction;
	private double gpsTimeoutTryingRecoverLink;
	private double gpsTimeWithStableLinkForSuccess;
	private SafetyAction gpsActionOnRecoveryFailure;
	private VehicleMode gpsModeOnRecoverySuccess;
	
	//generic safety actions
	private ArrayList<SafetyActionForAlert> safetyActions = new ArrayList<SafetyActionForAlert>();

	//mission regions
	private AltitudeType minMaxAltitudeType;
	private float maxAltitude;
	private float minAltitude;
	private Region authorizedRegion = new Region();
	private ArrayList<Region> prohibitedRegions = new ArrayList<Region>();

	public RulesOfSafety() {
		resetValues();
	}
	
	public void setSafetyActionForAlert(Alert alert, SafetyAction safetyAction) {
		//update existing
		for (SafetyActionForAlert sa : safetyActions) {
			if(sa.getAlert().equals(alert)) {
				sa.setSafetyAction(safetyAction);
				return;
			}
		}
		//create new
		safetyActions.add(new SafetyActionForAlert(alert, safetyAction));
	}
	
	public SafetyAction getSafetyActionForAlert(Alert alert) {
		for (SafetyActionForAlert sa : safetyActions) {
			if(sa.getAlert().equals(alert)) {
				return sa.getSafetyAction();
			}
		}
		return SafetyAction.DO_NOTHING;
	}
	
	public void validate(VerificationResult v, SkylightVehicleConfigurationMessage skylightVehicleConfiguration) {
		if(authorizedRegion==null || !authorizedRegion.isValidArea()) {
			v.addWarning("ROS: Activity region is not defined. All space will be authorized.");
		}
		
		for (SafetyActionForAlert sa : safetyActions) {
			if(!sa.getSafetyAction().equals(SafetyAction.DO_NOTHING) && !sa.getAlert().isSafetyActionEnabled()) {
				v.addError("ROS: Alert " + sa.getAlert() + " cannot have a safety action");
			}
		}
		
		v.assertRange(maxFlightTimeMinutes, 1, 999999, "ROS: Max flight time");
		v.assertNotNull(actionOnGpsAndDataLinkLost, "ROS: Action on GPS and Data Link lost cannot be null");
		
		if(dataLinkRecoveryEnabled) {
			v.assertRange(dataLinkTimeout, 0.001, 999, "ROS: Data link timeout");
			v.assertRange(dataLinkMinStrength, -1, 100, "ROS: Data link mininum signal strength");
			v.assertRange(dataLinkMaxRecoveryRetries, 0, 9999, "ROS: Data link: number of recovery retries");
			v.assertValidCoordinate(dataLinkRecoveryLoiterLocation, "ROS: Data link: recovery loiter location");
			if(!dataLinkRecoveryLoiterAltitudeType.equals(minMaxAltitudeType)) {
				v.addWarning("Data link recovery loiter altitude type is different from min/max permitted altitudes. It won't be validated");
			} else {
				v.assertRange(dataLinkRecoveryLoiterLocation.getAltitude(), minAltitude, maxAltitude, "ROS: Data link recovery loiter altitude");
			}
			v.assertRange(dataLinkTimeoutReachingLoiterLocation, 0, 9999, "ROS: Data link: timeout reaching recovery loiter location");
			v.assertRange(dataLinkTimeoutLoiteringWaitingRecovery, 0, 9999, "ROS: Data link: timeout recovering signal with loiter");
			v.assertRange(dataLinkTimeStableLinkForSuccess, 0, 999, "ROS: Data link: time receiving messages for recovery success");
			v.assertNotNull(dataLinkModeOnLinkRecoverySuccess, "ROS: Data link: next mode on recovery success");
			v.assertNotNull(dataLinkActionOnLinkRecoveryFailure, "ROS: Data link: Action on recovery failure");
		}
		
		if(gpsSignalRecoveryEnabled) {
			v.assertRange(gpsLinkTimeout, 0.001, 999, "ROS: GPS signal timeout");
			v.assertRange(gpsMaxRecoveryRetries, 0, 9999, "ROS: GPS: number of recovery retries");
			v.assertNotNull(gpsLinkRecoveryAction, "ROS: GPS: action for recovering");
			v.assertRange(gpsTimeoutTryingRecoverLink, 0, 9999, "ROS: GPS: timeout trying to recover signal");
			v.assertRange(gpsTimeWithStableLinkForSuccess, 0.001, 9999, "ROS: GPS: time receiving messages for recovery success");
			v.assertNotNull(gpsModeOnRecoverySuccess, "ROS: GPS: next mode on recovery success");
		}

		//manual recovery
		v.assertValidCoordinate(manualRecoveryLoiterLocation, "ROS: Manual recovery location");
		if(!dataLinkRecoveryLoiterAltitudeType.equals(minMaxAltitudeType)) {
			v.addWarning("Manual recovery loiter altitude type is different from min/max permitted altitudes. It won't be validated");
		} else {
			v.assertRange(manualRecoveryLoiterLocation.getAltitude(), minAltitude, maxAltitude, "ROS: Manual recovery loiter altitude");
		}
		v.assertRange(manualRecoveryReachLoiterLocationTimeout, 0, 9999, "ROS: Timeout reaching manual recovery location");
		v.assertRange(manualRecoveryLoiterTimeout, 0, 999, "ROS: Timeout loitering around manual recovery point");
		v.assertNotNull(manualRecoveryActionOnLoiterTimeout, "ROS: Action on manual recovery timeout");
		
		if(minMaxAltitudeType==AltitudeType.AGL) {
			if(maxAltitude>skylightVehicleConfiguration.getAltitudeMaxAGL()) {
				v.addError("ROS: Max altitude ("+ maxAltitude +"m) is greater than vehicle max altitude (" + skylightVehicleConfiguration.getAltitudeMaxAGL() + "m)");
			}
		} else {
			v.addWarning("ROS: Max AGL altitude was not validated because min/max vehicle altitude is not AGL");
		}
		if(minAltitude<1) {
			v.addError("ROS: Min altitude cannot be less than 1 m");
		}
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		maxFlightTimeMinutes = in.readInt();
		actionOnGpsAndDataLinkLost = SafetyAction.values()[in.readUnsignedByte()];
		
		dataLinkRecoveryEnabled = in.readBoolean();
		dataLinkTimeout = in.readDouble();
		dataLinkMinStrength = in.readByte();
		dataLinkMaxRecoveryRetries = in.readInt();
		
		dataLinkRecoveryLoiterLocation.readState(in);
		dataLinkRecoveryLoiterAltitudeType = AltitudeType.values()[in.readUnsignedByte()];
		dataLinkTimeoutReachingLoiterLocation = in.readDouble();
		dataLinkTimeoutLoiteringWaitingRecovery = in.readDouble();
		dataLinkTimeStableLinkForSuccess = in.readDouble();
		
		manualRecoveryLoiterLocation.readState(in);
		manualRecoveryLoiterAltitudeType = AltitudeType.values()[in.readUnsignedByte()];
		manualRecoveryReachLoiterLocationTimeout = in.readDouble();
		manualRecoveryLoiterTimeout = in.readDouble();
		manualRecoveryActionOnLoiterTimeout = SafetyAction.values()[in.readUnsignedByte()];
		dataLinkModeOnLinkRecoverySuccess = VehicleMode.values()[in.readUnsignedByte()];
		
		gpsSignalRecoveryEnabled = in.readBoolean();
		gpsLinkTimeout = in.readDouble();
		gpsMaxRecoveryRetries = in.readInt();
		gpsLinkRecoveryAction = SafetyAction.values()[in.readUnsignedByte()];
		gpsTimeoutTryingRecoverLink = in.readDouble();
		gpsTimeWithStableLinkForSuccess = in.readDouble();
		gpsActionOnRecoveryFailure = SafetyAction.values()[in.readUnsignedByte()];
		gpsModeOnRecoverySuccess = VehicleMode.values()[in.readUnsignedByte()];
		
		minMaxAltitudeType = AltitudeType.values()[in.readUnsignedByte()];
		maxAltitude = in.readFloat();
		minAltitude = in.readFloat();

		IOHelper.readArrayList(in, SafetyActionForAlert.class, safetyActions);
		
		authorizedRegion.readState(in);
		IOHelper.readArrayList(in, Region.class, prohibitedRegions);
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		out.writeInt(maxFlightTimeMinutes);
		out.writeByte(actionOnGpsAndDataLinkLost.ordinal());
		
		out.writeBoolean(dataLinkRecoveryEnabled);
		out.writeDouble(dataLinkTimeout);
		out.writeByte(dataLinkMinStrength);
		out.writeInt(dataLinkMaxRecoveryRetries);
		
		dataLinkRecoveryLoiterLocation.writeState(out);
		out.writeByte(dataLinkRecoveryLoiterAltitudeType.ordinal());
		out.writeDouble(dataLinkTimeoutReachingLoiterLocation);
		out.writeDouble(dataLinkTimeoutLoiteringWaitingRecovery);
		out.writeDouble(dataLinkTimeStableLinkForSuccess);
		
		manualRecoveryLoiterLocation.writeState(out);
		out.writeByte(manualRecoveryLoiterAltitudeType.ordinal());
		out.writeDouble(manualRecoveryReachLoiterLocationTimeout);
		out.writeDouble(manualRecoveryLoiterTimeout);
		out.writeByte(manualRecoveryActionOnLoiterTimeout.ordinal());
		out.writeByte(dataLinkModeOnLinkRecoverySuccess.ordinal());
		
		out.writeBoolean(gpsSignalRecoveryEnabled);
		out.writeDouble(gpsLinkTimeout);
		out.writeInt(gpsMaxRecoveryRetries);
		out.writeByte(gpsLinkRecoveryAction.ordinal());
		out.writeDouble(gpsTimeoutTryingRecoverLink);
		out.writeDouble(gpsTimeWithStableLinkForSuccess);
		out.writeByte(gpsActionOnRecoveryFailure.ordinal());
		out.writeByte(gpsModeOnRecoverySuccess.ordinal());
		
		out.writeByte(minMaxAltitudeType.ordinal());
		out.writeFloat(maxAltitude);
		out.writeFloat(minAltitude);

		IOHelper.writeArrayList(out, safetyActions);
		
		authorizedRegion.writeState(out);
		IOHelper.writeArrayList(out, prohibitedRegions);
	}
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M2007;
	}

	@Override
	public void resetValues() {
		//general
		maxFlightTimeMinutes = 120;
		actionOnGpsAndDataLinkLost = SafetyAction.LOITER_WITH_ROLL_DESCENDING;

		//manual recovery action configuration
		manualRecoveryLoiterLocation.reset();
		manualRecoveryLoiterLocation.setAltitude(121.92F);
		manualRecoveryLoiterAltitudeType = AltitudeType.AGL;
		manualRecoveryLoiterLocation.setAltitude(100);
		manualRecoveryReachLoiterLocationTimeout = 600;
		manualRecoveryLoiterTimeout = 600;
		manualRecoveryActionOnLoiterTimeout = SafetyAction.LOITER_WITH_ROLL_DESCENDING;
		
		//data link recovery
		dataLinkRecoveryEnabled = false;
		dataLinkTimeout = 10;
		dataLinkMinStrength = 10;
		dataLinkMaxRecoveryRetries = 10;
		dataLinkRecoveryLoiterLocation.reset();
		dataLinkRecoveryLoiterLocation.setAltitude(121.92F);
		dataLinkRecoveryLoiterAltitudeType = AltitudeType.AGL;
		dataLinkRecoveryLoiterLocation.setAltitude(100);
		dataLinkTimeoutReachingLoiterLocation = 600;
		dataLinkTimeoutLoiteringWaitingRecovery = 600;
		dataLinkTimeStableLinkForSuccess = 5;
		dataLinkActionOnLinkRecoveryFailure = SafetyAction.GO_FOR_MANUAL_RECOVERY;
		dataLinkModeOnLinkRecoverySuccess = VehicleMode.PREVIOUS_MODE;

		//GPS recovery
		gpsSignalRecoveryEnabled = false;
		gpsLinkTimeout = 5;
		gpsMaxRecoveryRetries = 10;
		gpsLinkRecoveryAction = SafetyAction.LOITER_WITH_ROLL;
		gpsTimeoutTryingRecoverLink = 300;
		gpsTimeWithStableLinkForSuccess = 5;
		gpsActionOnRecoveryFailure = SafetyAction.LOITER_WITH_ROLL_DESCENDING;
		gpsModeOnRecoverySuccess = VehicleMode.PREVIOUS_MODE;
		
		//generic safety actions
		safetyActions.clear();
		setSafetyActionForAlert(Alert.ZERO_RPM_DETECTED,			SafetyAction.LOITER_WITH_ROLL_DESCENDING);

		//mission regions
		minMaxAltitudeType = AltitudeType.AGL;
		maxAltitude = 300;
		minAltitude = 30;
		authorizedRegion.clear();
		prohibitedRegions.clear();
	}

	public int getMaxFlightTimeMinutes() {
		return maxFlightTimeMinutes;
	}

	public void setMaxFlightTimeMinutes(int maxFlightTimeMinutes) {
		this.maxFlightTimeMinutes = maxFlightTimeMinutes;
	}

	public SafetyAction getActionOnGpsAndDataLinkLost() {
		return actionOnGpsAndDataLinkLost;
	}

	public void setActionOnGpsAndDataLinkLost(SafetyAction actionOnGpsAndDataLinkLost) {
		this.actionOnGpsAndDataLinkLost = actionOnGpsAndDataLinkLost;
	}

	public boolean isDataLinkRecoveryEnabled() {
		return dataLinkRecoveryEnabled;
	}

	public void setDataLinkRecoveryEnabled(boolean dataLinkRecoveryEnabled) {
		this.dataLinkRecoveryEnabled = dataLinkRecoveryEnabled;
	}

	public double getDataLinkTimeout() {
		return dataLinkTimeout;
	}

	public void setDataLinkTimeout(double dataLinkTimeout) {
		this.dataLinkTimeout = dataLinkTimeout;
	}

	public int getDataLinkMaxRecoveryRetries() {
		return dataLinkMaxRecoveryRetries;
	}

	public void setDataLinkMaxRecoveryRetries(int dataLinkNumberOfRetries) {
		this.dataLinkMaxRecoveryRetries = dataLinkNumberOfRetries;
	}

	public Coordinates getDataLinkRecoveryLoiterLocation() {
		return dataLinkRecoveryLoiterLocation;
	}

	public void setDataLinkRecoveryLoiterLocation(Coordinates dataLinkLoiterLocation) {
		this.dataLinkRecoveryLoiterLocation = dataLinkLoiterLocation;
	}

	public double getDataLinkTimeoutReachingLoiterLocation() {
		return dataLinkTimeoutReachingLoiterLocation;
	}

	public void setDataLinkTimeoutReachingLoiterLocation(double dataLinkReachLoiterLocationTimeout) {
		this.dataLinkTimeoutReachingLoiterLocation = dataLinkReachLoiterLocationTimeout;
	}

	public double getDataLinkTimeoutLoiteringWaitingRecovery() {
		return dataLinkTimeoutLoiteringWaitingRecovery;
	}

	public void setDataLinkTimeoutLoiteringWaitingRecovery(double dataLinkRecoverSignalOnLoiterTimeout) {
		this.dataLinkTimeoutLoiteringWaitingRecovery = dataLinkRecoverSignalOnLoiterTimeout;
	}

	public double getDataLinkTimeStableLinkForSuccess() {
		return dataLinkTimeStableLinkForSuccess;
	}

	public void setDataLinkTimeStableLinkForSuccess(double dataLinkTimeReceivingMessagesForSuccess) {
		this.dataLinkTimeStableLinkForSuccess = dataLinkTimeReceivingMessagesForSuccess;
	}

	public Coordinates getManualRecoveryLoiterLocation() {
		return manualRecoveryLoiterLocation;
	}

	public void setManualRecoveryLoiterLocation(Coordinates dataLinkManualRecoveryLocation) {
		this.manualRecoveryLoiterLocation = dataLinkManualRecoveryLocation;
	}

	public double getManualRecoveryReachLoiterLocationTimeout() {
		return manualRecoveryReachLoiterLocationTimeout;
	}

	public void setManualRecoveryReachLoiterLocationTimeout(double dataLinkReachManualRecoveryLocationTimeout) {
		this.manualRecoveryReachLoiterLocationTimeout = dataLinkReachManualRecoveryLocationTimeout;
	}

	public double getManualRecoveryLoiterTimeout() {
		return manualRecoveryLoiterTimeout;
	}

	public void setManualRecoveryLoiterTimeout(double dataLinkManualRecoveryLoiterTimeout) {
		this.manualRecoveryLoiterTimeout = dataLinkManualRecoveryLoiterTimeout;
	}

	public SafetyAction getManualRecoveryActionOnLoiterTimeout() {
		return manualRecoveryActionOnLoiterTimeout;
	}

	public void setManualRecoveryActionOnLoiterTimeout(SafetyAction dataLinkActionOnManualRecoveryFailure) {
		this.manualRecoveryActionOnLoiterTimeout = dataLinkActionOnManualRecoveryFailure;
	}

	public VehicleMode getDataLinkModeOnLinkRecoverySuccess() {
		return dataLinkModeOnLinkRecoverySuccess;
	}

	public void setDataLinkModeOnLinkRecoverySuccess(VehicleMode dataLinkNextModeOnLinkRecoverySuccess) {
		this.dataLinkModeOnLinkRecoverySuccess = dataLinkNextModeOnLinkRecoverySuccess;
	}

	public boolean isGpsSignalRecoveryEnabled() {
		return gpsSignalRecoveryEnabled;
	}

	public void setGpsSignalRecoveryEnabled(boolean gpsSignalRecoveryEnabled) {
		this.gpsSignalRecoveryEnabled = gpsSignalRecoveryEnabled;
	}

	public double getGpsLinkTimeout() {
		return gpsLinkTimeout;
	}

	public void setGpsLinkTimeout(double gpsLinkTimeout) {
		this.gpsLinkTimeout = gpsLinkTimeout;
	}

	public int getGpsMaxRecoveryRetries() {
		return gpsMaxRecoveryRetries;
	}

	public void setGpsMaxRecoveryRetries(int gpsNumberOfRetries) {
		this.gpsMaxRecoveryRetries = gpsNumberOfRetries;
	}

	public SafetyAction getGpsLinkRecoveryAction() {
		return gpsLinkRecoveryAction;
	}

	public void setGpsLinkRecoveryAction(SafetyAction gpsActionForRecovering) {
		this.gpsLinkRecoveryAction = gpsActionForRecovering;
	}

	public double getGpsTimeoutTryingRecoverLink() {
		return gpsTimeoutTryingRecoverLink;
	}

	public void setGpsTimeoutTryingRecoverLink(double gpsRecoveryTimeout) {
		this.gpsTimeoutTryingRecoverLink = gpsRecoveryTimeout;
	}

	public double getGpsTimeWithStableLinkForSuccess() {
		return gpsTimeWithStableLinkForSuccess;
	}

	public void setGpsTimeWithStableLinkForSuccess(double gpsTimeReceivingMessagesForSuccess) {
		this.gpsTimeWithStableLinkForSuccess = gpsTimeReceivingMessagesForSuccess;
	}

	public SafetyAction getGpsActionOnRecoveryFailure() {
		return gpsActionOnRecoveryFailure;
	}

	public void setGpsActionOnRecoveryFailure(SafetyAction gpsActionOnRecoveryFailure) {
		this.gpsActionOnRecoveryFailure = gpsActionOnRecoveryFailure;
	}

	public VehicleMode getGpsModeOnRecoverySuccess() {
		return gpsModeOnRecoverySuccess;
	}

	public void setGpsModeOnRecoverySuccess(VehicleMode gpsNextModeOnRecoverySuccess) {
		this.gpsModeOnRecoverySuccess = gpsNextModeOnRecoverySuccess;
	}

	public AltitudeType getMinMaxAltitudeType() {
		return minMaxAltitudeType;
	}

	public void setMinMaxAltitudeType(AltitudeType minMaxAltitudeType) {
		this.minMaxAltitudeType = minMaxAltitudeType;
	}

	public float getMaxAltitude() {
		return maxAltitude;
	}

	public void setMaxAltitude(float maxAltitude) {
		this.maxAltitude = maxAltitude;
	}

	public float getMinAltitude() {
		return minAltitude;
	}

	public void setMinAltitude(float minAltitude) {
		this.minAltitude = minAltitude;
	}

	public Region getAuthorizedRegion() {
		return authorizedRegion;
	}

	public void setAuthorizedRegion(Region activityRegion) {
		this.authorizedRegion = activityRegion;
	}

	public ArrayList<Region> getProhibitedRegions() {
		return prohibitedRegions;
	}

	public void setProhibitedRegions(ArrayList<Region> prohibitedRegions) {
		this.prohibitedRegions = prohibitedRegions;
	}
	
	public byte getDataLinkMinStrength() {
		return dataLinkMinStrength;
	}
	
	public void setDataLinkMinStrength(byte dataLinkMinStrength) {
		this.dataLinkMinStrength = dataLinkMinStrength;
	}
	
	public SafetyAction getDataLinkActionOnLinkRecoveryFailure() {
		return dataLinkActionOnLinkRecoveryFailure;
	}
	public void setDataLinkActionOnLinkRecoveryFailure(SafetyAction dataLinkActionOnLinkRecoveryFailure) {
		this.dataLinkActionOnLinkRecoveryFailure = dataLinkActionOnLinkRecoveryFailure;
	}
	
	public ArrayList<SafetyActionForAlert> getSafetyActions() {
		return safetyActions;
	}
	
	public AltitudeType getManualRecoveryLoiterAltitudeType() {
		return manualRecoveryLoiterAltitudeType;
	}
	public void setManualRecoveryLoiterAltitudeType(AltitudeType manualRecoveryLoiterAltitudeType) {
		this.manualRecoveryLoiterAltitudeType = manualRecoveryLoiterAltitudeType;
	}
	
	public AltitudeType getDataLinkRecoveryLoiterAltitudeType() {
		return dataLinkRecoveryLoiterAltitudeType;
	}
	public void setDataLinkRecoveryLoiterAltitudeType(AltitudeType dataLinkRecoveryLoiterAltitudeType) {
		this.dataLinkRecoveryLoiterAltitudeType = dataLinkRecoveryLoiterAltitudeType;
	}
	
}