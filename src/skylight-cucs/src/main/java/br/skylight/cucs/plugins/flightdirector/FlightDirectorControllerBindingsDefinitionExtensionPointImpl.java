package br.skylight.cucs.plugins.flightdirector;

import java.util.ArrayList;
import java.util.List;

import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.enums.HeadingCommandType;
import br.skylight.commons.dli.vehicle.VehicleSteeringCommand;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.gamecontroller.BindingDefinitionsExtensionPoint;
import br.skylight.cucs.plugins.gamecontroller.ControllerBinding;
import br.skylight.cucs.plugins.gamecontroller.ControllerBindingDefinition;
import br.skylight.cucs.plugins.gamecontroller.GameControllerService;
import br.skylight.cucs.plugins.gamecontroller.ValueResolver;

@ExtensionPointImplementation(extensionPointDefinition=BindingDefinitionsExtensionPoint.class)
public class FlightDirectorControllerBindingsDefinitionExtensionPointImpl extends BindingDefinitionsExtensionPoint {

	private ValueResolver speedValueResolver;
	private ValueResolver altitudeValueResolver;
	private ValueResolver headingValueResolver;
	private ValueResolver rollValueResolver;
	
	@ServiceInjection
	public VehicleControlService vehicleControlService;
	
	@Override
	public List<ControllerBindingDefinition> getControllerBindingDefinitions() {
		List<ControllerBindingDefinition> r = new ArrayList<ControllerBindingDefinition>();
		r.add(new ControllerBindingDefinition(GameControllerService.SPEED_CONTROL_ID, "Manual speed control", getSpeedValueResolver()) {
			@Override
			public void onComponentValueChanged(ControllerBinding controllerBinding, double value, Vehicle selectedVehicle) {
				if(selectedVehicle!=null) {
					VehicleSteeringCommand vs = vehicleControlService.resolveVehicleSteeringCommandForSending(selectedVehicle.getVehicleID().getVehicleID());
					vs.setCommandedSpeed((float)value);
					vehicleControlService.sendVehicleSteeringCommand(vs);
				}
			}
		});
		r.add(new ControllerBindingDefinition(GameControllerService.ALTITUDE_CONTROL_ID, "Manual altitude control", getAltitudeValueResolver()) {
			@Override
			public void onComponentValueChanged(ControllerBinding controllerBinding, double value, Vehicle selectedVehicle) {
				if(selectedVehicle!=null) {
					VehicleSteeringCommand vs = vehicleControlService.resolveVehicleSteeringCommandForSending(selectedVehicle.getVehicleID().getVehicleID());
					vs.setCommandedAltitude((float)value);
					vehicleControlService.sendVehicleSteeringCommand(vs);
				}
			}
		});
		r.add(new ControllerBindingDefinition(GameControllerService.COURSE_HEADING_CONTROL_ID, "Manual course heading control", getHeadingValueResolver()) {
			@Override
			public void onComponentValueChanged(ControllerBinding controllerBinding, double value, Vehicle selectedVehicle) {
				if(selectedVehicle!=null) {
					VehicleSteeringCommand vs = vehicleControlService.resolveVehicleSteeringCommandForSending(selectedVehicle.getVehicleID().getVehicleID());
					vs.setHeadingCommandType(HeadingCommandType.COURSE);
					vs.setCommandedCourse((float)value);
					vehicleControlService.sendVehicleSteeringCommand(vs);
				}
			}
		});
		r.add(new ControllerBindingDefinition(GameControllerService.ROLL_HEADING_CONTROL_ID, "Manual roll control", getRollValueResolver()) {
			@Override
			public void onComponentValueChanged(ControllerBinding controllerBinding, double value, Vehicle selectedVehicle) {
				if(selectedVehicle!=null) {
					VehicleSteeringCommand vs = vehicleControlService.resolveVehicleSteeringCommandForSending(selectedVehicle.getVehicleID().getVehicleID());
					vs.setHeadingCommandType(HeadingCommandType.ROLL);
					vs.setCommandedRoll((float)value);
					vehicleControlService.sendVehicleSteeringCommand(vs);
				}
			}
		});
		return r;
	}

	private ValueResolver getSpeedValueResolver() {
		if(speedValueResolver==null) {
			speedValueResolver = new ValueResolver();
			//TODO use vehicle limiting to configure this
			speedValueResolver.setMinValueProportional(1);
			speedValueResolver.setMaxValueProportional(30);
			speedValueResolver.setMinValueIncremental(-10);
			speedValueResolver.setMaxValueIncremental(9999);
			speedValueResolver.setMaxIncrementRate(13);
		}
		return speedValueResolver;
	}
	private ValueResolver getAltitudeValueResolver() {
		if(altitudeValueResolver==null) {
			altitudeValueResolver = new ValueResolver();
			//TODO use vehicle limiting to configure this
			altitudeValueResolver.setMinValueProportional(100);
			altitudeValueResolver.setMaxValueProportional(500);
			altitudeValueResolver.setMinValueIncremental(-100);
			altitudeValueResolver.setMaxValueIncremental(999999);
			altitudeValueResolver.setMaxIncrementRate(80);
		}
		return altitudeValueResolver;
	}
	private ValueResolver getHeadingValueResolver() {
		if(headingValueResolver==null) {
			//TODO use vehicle limiting to configure this
			headingValueResolver = new ValueResolver() {
				@Override
				public double getResolvedValue() {
					return MathHelper.normalizeAngle2(super.getResolvedValue());
				}
			};
			headingValueResolver.setMinValueProportional(-Math.PI);
			headingValueResolver.setMaxValueProportional(Math.PI);
			headingValueResolver.setMinValueIncremental(-Double.MAX_VALUE);
			headingValueResolver.setMaxValueIncremental(Double.MAX_VALUE);
			headingValueResolver.setMaxIncrementRate(Math.PI/2);
		}
		return headingValueResolver;
	}
	private ValueResolver getRollValueResolver() {
		if(rollValueResolver==null) {
			//TODO use vehicle limiting to configure this
			rollValueResolver = new ValueResolver();
			rollValueResolver.setMinValueProportional(-Math.PI/5);
			rollValueResolver.setMaxValueProportional(Math.PI/5);
			rollValueResolver.setMinValueIncremental(-Double.MAX_VALUE);
			rollValueResolver.setMaxValueIncremental(Double.MAX_VALUE);
			rollValueResolver.setMaxIncrementRate(Math.PI/2);
		}
		return rollValueResolver;
	}
	
}
