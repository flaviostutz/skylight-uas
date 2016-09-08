package br.skylight.cucs.plugins.skylightvehicle;

import java.util.ArrayList;
import java.util.List;

import br.skylight.commons.Servo;
import br.skylight.commons.Vehicle;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.skylight.ServoActuationCommand;
import br.skylight.commons.plugin.annotations.ExtensionPointImplementation;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.plugins.core.VehicleControlService;
import br.skylight.cucs.plugins.gamecontroller.BindingDefinitionsExtensionPoint;
import br.skylight.cucs.plugins.gamecontroller.ControllerBinding;
import br.skylight.cucs.plugins.gamecontroller.ControllerBindingDefinition;
import br.skylight.cucs.plugins.gamecontroller.ValueResolver;
import br.skylight.cucs.plugins.subscriber.SubscriberService;

@ExtensionPointImplementation(extensionPointDefinition=BindingDefinitionsExtensionPoint.class)
public class ServoControllerBindingsDefinitionExtensionPointImpl extends BindingDefinitionsExtensionPoint {

	public static final int CAMERA_TILT_CONTROL_ID = 207;
	public static final int CAMERA_PAN_CONTROL_ID = 206;
	public static final int THROTTLE_CONTROL_ID = 205;
	public static final int ELEVATOR_CONTROL_ID = 204;
	public static final int RUDDER_CONTROL_ID = 203;
	public static final int AILERON_R_CONTROL_ID = 202;
	public static final int AILERON_L_CONTROL_ID = 201;
	
	private ValueResolver aileronLeftValueResolver;
	private ValueResolver aileronRightValueResolver;
	private ValueResolver rudderValueResolver;
	private ValueResolver elevatorValueResolver;
	private ValueResolver throttleValueResolver;
	private ValueResolver cameraPanValueResolver;
	private ValueResolver cameraTiltValueResolver;
	
	@ServiceInjection
	public VehicleControlService vehicleControlService;
	
	@ServiceInjection
	public MessagingService messagingService;
	
	@ServiceInjection
	public SubscriberService subscriberService;
	
	@Override
	public List<ControllerBindingDefinition> getControllerBindingDefinitions() {
		List<ControllerBindingDefinition> r = new ArrayList<ControllerBindingDefinition>();
		r.add(new ControllerBindingDefinition(ServoControllerBindingsDefinitionExtensionPointImpl.AILERON_L_CONTROL_ID, "Aileron left servo", getAileronLeftValueResolver()) {
			@Override
			public void onComponentValueChanged(ControllerBinding controllerBinding, double value, Vehicle selectedVehicle) {
				if(selectedVehicle!=null) {
					ServoActuationCommand m = messagingService.resolveMessageForSending(ServoActuationCommand.class);
					m.setServo(Servo.AILERON_LEFT);
					m.setCommandedSetpoint((float)value);
					m.setVehicleID(subscriberService.getLastSelectedVehicle().getVehicleID().getVehicleID());
					messagingService.sendMessage(m);
				}
			}
		});
		r.add(new ControllerBindingDefinition(ServoControllerBindingsDefinitionExtensionPointImpl.AILERON_R_CONTROL_ID, "Aileron right servo", getAileronRightValueResolver()) {
			@Override
			public void onComponentValueChanged(ControllerBinding controllerBinding, double value, Vehicle selectedVehicle) {
				if(selectedVehicle!=null) {
					ServoActuationCommand m = messagingService.resolveMessageForSending(ServoActuationCommand.class);
					m.setServo(Servo.AILERON_RIGHT);
					m.setCommandedSetpoint((float)value);
					m.setVehicleID(subscriberService.getLastSelectedVehicle().getVehicleID().getVehicleID());
					messagingService.sendMessage(m);
				}
			}
		});
		r.add(new ControllerBindingDefinition(ServoControllerBindingsDefinitionExtensionPointImpl.RUDDER_CONTROL_ID, "Rudder servo", getRudderValueResolver()) {
			@Override
			public void onComponentValueChanged(ControllerBinding controllerBinding, double value, Vehicle selectedVehicle) {
				if(selectedVehicle!=null) {
					ServoActuationCommand m = messagingService.resolveMessageForSending(ServoActuationCommand.class);
					m.setServo(Servo.RUDDER);
					m.setCommandedSetpoint((float)value);
					m.setVehicleID(subscriberService.getLastSelectedVehicle().getVehicleID().getVehicleID());
					messagingService.sendMessage(m);
				}
			}
		});
		r.add(new ControllerBindingDefinition(ServoControllerBindingsDefinitionExtensionPointImpl.ELEVATOR_CONTROL_ID, "Elevator servo", getElevatorValueResolver()) {
			@Override
			public void onComponentValueChanged(ControllerBinding controllerBinding, double value, Vehicle selectedVehicle) {
				if(selectedVehicle!=null) {
					if(selectedVehicle!=null) {
						ServoActuationCommand m = messagingService.resolveMessageForSending(ServoActuationCommand.class);
						m.setServo(Servo.ELEVATOR);
						m.setCommandedSetpoint((float)value);
						m.setVehicleID(subscriberService.getLastSelectedVehicle().getVehicleID().getVehicleID());
						messagingService.sendMessage(m);
					}
				}
			}
		});
		r.add(new ControllerBindingDefinition(ServoControllerBindingsDefinitionExtensionPointImpl.THROTTLE_CONTROL_ID, "Throttle servo", getThrottleValueResolver()) {
			@Override
			public void onComponentValueChanged(ControllerBinding controllerBinding, double value, Vehicle selectedVehicle) {
				if(selectedVehicle!=null) {
					ServoActuationCommand m = messagingService.resolveMessageForSending(ServoActuationCommand.class);
					m.setServo(Servo.THROTTLE);
					m.setCommandedSetpoint((float)value);
					m.setVehicleID(subscriberService.getLastSelectedVehicle().getVehicleID().getVehicleID());
					messagingService.sendMessage(m);
				}
			}
		});
		r.add(new ControllerBindingDefinition(ServoControllerBindingsDefinitionExtensionPointImpl.CAMERA_PAN_CONTROL_ID, "Camera pan servo", getCameraPanValueResolver()) {
			@Override
			public void onComponentValueChanged(ControllerBinding controllerBinding, double value, Vehicle selectedVehicle) {
				if(selectedVehicle!=null) {
					ServoActuationCommand m = messagingService.resolveMessageForSending(ServoActuationCommand.class);
					m.setServo(Servo.CAMERA_PAN);
					m.setCommandedSetpoint((float)value);
					m.setVehicleID(subscriberService.getLastSelectedVehicle().getVehicleID().getVehicleID());
					messagingService.sendMessage(m);
				}
			}
		});
		r.add(new ControllerBindingDefinition(ServoControllerBindingsDefinitionExtensionPointImpl.CAMERA_TILT_CONTROL_ID, "Camera tilt servo", getCameraTiltValueResolver()) {
			@Override
			public void onComponentValueChanged(ControllerBinding controllerBinding, double value, Vehicle selectedVehicle) {
				if(selectedVehicle!=null) {
					ServoActuationCommand m = messagingService.resolveMessageForSending(ServoActuationCommand.class);
					m.setServo(Servo.CAMERA_TILT);
					m.setCommandedSetpoint((float)value);
					m.setVehicleID(subscriberService.getLastSelectedVehicle().getVehicleID().getVehicleID());
					messagingService.sendMessage(m);
				}
			}
		});
		return r;
	}

	public ValueResolver getAileronLeftValueResolver() {
		if(aileronLeftValueResolver==null) {
			aileronLeftValueResolver = new ValueResolver();
			aileronLeftValueResolver.setMinValueProportional(-127);
			aileronLeftValueResolver.setMaxValueProportional(127);
			aileronLeftValueResolver.setMinValueIncremental(-127);
			aileronLeftValueResolver.setMaxValueIncremental(127);
			aileronLeftValueResolver.setMaxIncrementRate(20);
		}
		return aileronLeftValueResolver;
	}
	public ValueResolver getAileronRightValueResolver() {
		if(aileronRightValueResolver==null) {
			aileronRightValueResolver = new ValueResolver();
			 aileronRightValueResolver.setMinValueProportional(-127);
			aileronRightValueResolver.setMaxValueProportional(127);
			aileronRightValueResolver.setMinValueIncremental(-127);
			aileronRightValueResolver.setMaxValueIncremental(127);
			aileronRightValueResolver.setMaxIncrementRate(20);
		}
		return aileronRightValueResolver;
	}
	public ValueResolver getRudderValueResolver() {
		if(rudderValueResolver==null) {
			rudderValueResolver = new ValueResolver();
			rudderValueResolver.setMinValueProportional(-127);
			rudderValueResolver.setMaxValueProportional(127);
			rudderValueResolver.setMinValueIncremental(-127);
			rudderValueResolver.setMaxValueIncremental(127);
			rudderValueResolver.setMaxIncrementRate(20);
		}
		return rudderValueResolver;
	}
	public ValueResolver getElevatorValueResolver() {
		if(elevatorValueResolver==null) {
			elevatorValueResolver = new ValueResolver();
			elevatorValueResolver.setMinValueProportional(-127);
			elevatorValueResolver.setMaxValueProportional(127);
			elevatorValueResolver.setMinValueIncremental(-127);
			elevatorValueResolver.setMaxValueIncremental(127);
			elevatorValueResolver.setMaxIncrementRate(20);
		}
		return elevatorValueResolver;
	}
	public ValueResolver getThrottleValueResolver() {
		if(throttleValueResolver==null) {
			throttleValueResolver = new ValueResolver();
			throttleValueResolver.setMinValueProportional(-127);
			throttleValueResolver.setMaxValueProportional(127);
			throttleValueResolver.setMinValueIncremental(-127);
			throttleValueResolver.setMaxValueIncremental(127);
			throttleValueResolver.setMaxIncrementRate(20);
		}
		return throttleValueResolver;
	}
	public ValueResolver getCameraPanValueResolver() {
		if(cameraPanValueResolver==null) {
			cameraPanValueResolver = new ValueResolver();
			cameraPanValueResolver.setMinValueProportional(-127);
			cameraPanValueResolver.setMaxValueProportional(127);
			cameraPanValueResolver.setMinValueIncremental(-127);
			cameraPanValueResolver.setMaxValueIncremental(127);
			cameraPanValueResolver.setMaxIncrementRate(20);
		}
		return cameraPanValueResolver;
	}
	public ValueResolver getCameraTiltValueResolver() {
		if(cameraTiltValueResolver==null) {
			cameraTiltValueResolver = new ValueResolver();
			cameraTiltValueResolver.setMinValueProportional(-127);
			cameraTiltValueResolver.setMaxValueProportional(127);
			cameraTiltValueResolver.setMinValueIncremental(-127);
			cameraTiltValueResolver.setMaxValueIncremental(127);
			cameraTiltValueResolver.setMaxIncrementRate(20);
		}
		return cameraTiltValueResolver;
	}
	
}
