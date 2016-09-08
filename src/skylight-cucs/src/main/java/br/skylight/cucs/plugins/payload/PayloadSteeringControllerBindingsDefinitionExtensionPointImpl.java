package br.skylight.cucs.plugins.payload;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import br.skylight.commons.EventType;
import br.skylight.commons.Payload;
import br.skylight.commons.StringHelper;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.enums.SetZoom;
import br.skylight.commons.dli.payload.EOIRConfigurationState;
import br.skylight.commons.dli.payload.PayloadSteeringCommand;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.gamecontroller.BindingDefinitionsExtensionPoint;
import br.skylight.cucs.plugins.gamecontroller.ControllerBinding;
import br.skylight.cucs.plugins.gamecontroller.ControllerBindingDefinition;
import br.skylight.cucs.plugins.gamecontroller.GameControllerService;
import br.skylight.cucs.plugins.gamecontroller.ValueResolver;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.plugins.subscriber.VehicleListener;

@ExtensionPointImplementation(extensionPointDefinition=BindingDefinitionsExtensionPoint.class)
public class PayloadSteeringControllerBindingsDefinitionExtensionPointImpl extends BindingDefinitionsExtensionPoint implements VehicleListener {

	private static final Logger logger = Logger.getLogger(PayloadSteeringControllerBindingsDefinitionExtensionPointImpl.class.getName());
	
	private ValueResolver azimuthValueResolver;
	private ValueResolver elevationValueResolver;
	private ValueResolver fovValueResolver;
	
	private Payload lastSelectedPayload;
	private float dimensionRatio = 1;
	
	@ServiceInjection
	public VehicleControlService vehicleControlService;
	
	@ServiceInjection
	public SubscriberService subscriberService;

	@Override
	public void onActivate() throws Exception {
		subscriberService.addVehicleListener(this);
	}
	
	@Override
	public List<ControllerBindingDefinition> getControllerBindingDefinitions() {
		List<ControllerBindingDefinition> r = new ArrayList<ControllerBindingDefinition>();
		r.add(new ControllerBindingDefinition(GameControllerService.PAYLOAD_AZIMUTH_CONTROL_ID, "Payload azimuth control", getAzimuthValueResolver()) {
			@Override
			public void onComponentValueChanged(ControllerBinding controllerBinding, double value, Vehicle selectedVehicle) {
				if(selectedVehicle!=null && lastSelectedPayload!=null) {
					PayloadSteeringCommand m1 = vehicleControlService.resolvePayloadSteeringCommandForSending(selectedVehicle.getVehicleID().getVehicleID(), lastSelectedPayload.getUniqueStationNumber());
					m1.setSetCentrelineAzimuthAngle((float)value);
					vehicleControlService.sendPayloadSteeringCommand(m1);
				}
			}
		});
		r.add(new ControllerBindingDefinition(GameControllerService.PAYLOAD_ELEVATION_CONTROL_ID, "Payload elevation control", getElevationValueResolver()) {
			@Override
			public void onComponentValueChanged(ControllerBinding controllerBinding, double value, Vehicle selectedVehicle) {
				if(selectedVehicle!=null && lastSelectedPayload!=null) {
					PayloadSteeringCommand m1 = vehicleControlService.resolvePayloadSteeringCommandForSending(selectedVehicle.getVehicleID().getVehicleID(), lastSelectedPayload.getUniqueStationNumber());
					m1.setSetCentrelineElevationAngle((float)value);
					vehicleControlService.sendPayloadSteeringCommand(m1);
				}
			}
		});
		r.add(new ControllerBindingDefinition(GameControllerService.PAYLOAD_FOV_CONTROL_ID, "Payload zoom control", getFovValueResolver()) {
			@Override
			public void onComponentValueChanged(ControllerBinding controllerBinding, double value, Vehicle selectedVehicle) {
				if(selectedVehicle!=null && lastSelectedPayload!=null) {
					PayloadSteeringCommand m1 = vehicleControlService.resolvePayloadSteeringCommandForSending(selectedVehicle.getVehicleID().getVehicleID(), lastSelectedPayload.getUniqueStationNumber());
					m1.setSetZoom(SetZoom.USE_FOV);
					m1.setSetHorizontalFieldOfView((float)value);
					m1.setSetVerticalFieldOfView((float)value*dimensionRatio);
					vehicleControlService.sendPayloadSteeringCommand(m1);
				}
			}
		});
		return r;
	}

	private ValueResolver getElevationValueResolver() {
		if(elevationValueResolver==null) {
			elevationValueResolver = new ValueResolver();
			//TODO use payload limiting to configure this
			elevationValueResolver.setMinValueProportional(Math.toRadians(-90));
			elevationValueResolver.setMaxValueProportional(Math.toRadians(90));
			elevationValueResolver.setMinValueIncremental(Math.toRadians(-90));
			elevationValueResolver.setMaxValueIncremental(Math.toRadians(90));
			elevationValueResolver.setMaxIncrementRate(Math.toRadians(60));
		}
		return elevationValueResolver;
	}
	private ValueResolver getAzimuthValueResolver() {
		if(azimuthValueResolver==null) {
			azimuthValueResolver = new ValueResolver();
			//TODO use payload limiting to configure this
			azimuthValueResolver.setMinValueProportional(Math.toRadians(-90));
			azimuthValueResolver.setMaxValueProportional(Math.toRadians(90));
			azimuthValueResolver.setMinValueIncremental(Math.toRadians(-90));
			azimuthValueResolver.setMaxValueIncremental(Math.toRadians(90));
			azimuthValueResolver.setMaxIncrementRate(Math.toRadians(60));
		}
		return azimuthValueResolver;
	}
	private ValueResolver getFovValueResolver() {
		if(fovValueResolver==null) {
			fovValueResolver = new ValueResolver();
			fovValueResolver.setMinValueProportional(Math.toRadians(18));
			fovValueResolver.setMaxValueProportional(Math.toRadians(35));
			fovValueResolver.setMinValueIncremental(Math.toRadians(1));
			fovValueResolver.setMaxValueIncremental(Math.toRadians(45));
			fovValueResolver.setMaxIncrementRate(Math.toRadians(15));
		}
		return fovValueResolver;
	}

	@Override
	public void onPayloadEvent(Payload p, EventType type) {
		if(type.equals(EventType.SELECTED) || (lastSelectedPayload==null || (type.equals(EventType.UPDATED) && lastSelectedPayload.getUniqueStationNumber()==p.getUniqueStationNumber()))) {
			lastSelectedPayload = p;

			//default values for payload control limits
			getElevationValueResolver().setMinValueProportional(Math.toRadians(-90));
			elevationValueResolver.setMaxValueProportional(Math.toRadians(90));
			getAzimuthValueResolver().setMinValueProportional(Math.toRadians(-90));
			azimuthValueResolver.setMaxValueProportional(Math.toRadians(90));
			dimensionRatio = 0.75F;

			//setup min/max values
			if(lastSelectedPayload.getEoIrPayload()!=null) {
				EOIRConfigurationState cs = (EOIRConfigurationState)lastSelectedPayload.getEoIrPayload().getEoIrConfiguration();
				//current values for current payload
				if(cs!=null) {
					if(cs.getElevationMax()<cs.getElevationMin()) {
						logger.warning("Max elevation is less than min elevation. Using defaults. payload=" + cs.getStationNumber().getStations() + "; vehicle=" + StringHelper.formatId(cs.getVehicleID()));
					} else {
						getElevationValueResolver().setMinValueProportional(cs.getElevationMin());
						elevationValueResolver.setMaxValueProportional(cs.getElevationMax());
					}
					if(cs.getAzimuthMax()<cs.getAzimuthMin()) {
						logger.warning("Max azimuth is less than min azimuth. Using defaults. payload=" + cs.getStationNumber().getStations() + "; vehicle=" + StringHelper.formatId(cs.getVehicleID()));
					} else {
						getAzimuthValueResolver().setMinValueProportional(cs.getAzimuthMin());
						azimuthValueResolver.setMaxValueProportional(cs.getAzimuthMax());
					}
					
					dimensionRatio = ((float)cs.getEoVerticalImageDimension()/(float)cs.getEoHorizontalImageDimension());
				}
	
				elevationValueResolver.setMinValueIncremental(elevationValueResolver.getMinValueProportional());
				elevationValueResolver.setMaxValueIncremental(elevationValueResolver.getMaxValueProportional());
				elevationValueResolver.setMaxIncrementRate(Math.toRadians(60));
	
				azimuthValueResolver.setMinValueIncremental(azimuthValueResolver.getMinValueProportional());
				azimuthValueResolver.setMaxValueIncremental(azimuthValueResolver.getMaxValueProportional());
				azimuthValueResolver.setMaxIncrementRate(Math.toRadians(60));
			}
		}
	}

	@Override
	public void onVehicleEvent(Vehicle av, EventType type) {
	}

}
