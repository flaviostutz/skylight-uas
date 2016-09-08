package br.skylight.cucs.plugins.skylightvehicle;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import net.java.games.input.Component;

import org.jfree.data.xy.XYSeriesCollection;

import br.skylight.commons.Servo;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.skylight.ServoActuationCommand;
import br.skylight.commons.dli.skylight.ServosStateMessage;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.cucs.plugins.subscriber.SubscriberService;
import br.skylight.cucs.widgets.CUCSViewHelper;
import br.skylight.cucs.widgets.HoldControllerWidget;
import br.skylight.cucs.widgets.HoldControllerWidgetListener;
import br.skylight.cucs.widgets.MessageToChartConverter;
import br.skylight.cucs.widgets.TelemetryChartFrame;

public class ServoActuationWidget extends HoldControllerWidget implements HoldControllerWidgetListener {

	@ServiceInjection
	public MessagingService messagingService;
	
	@ServiceInjection
	public SubscriberService subscriberService;
	
	private Servo servo;
	private int vehicleID;
	private long graphStartTime;
	private static final int MAX_GRAPH_ITEMS = 50;

	public ServoActuationWidget() {
		setHoldControllerListener(this);
		getUnholdButton().setVisible(false);
		getGraphButton().setEnabled(true);
	}
	
	public void setFeedback(float value) {
		super.setFeedback((int)value+"");
	}

	@Override
	public void onGraphClicked() {
		graphStartTime = System.currentTimeMillis();
		final TelemetryChartFrame c = CUCSViewHelper.showMultiChart(getLabel(), "Tick", "Value", true, MAX_GRAPH_ITEMS, new MessageToChartConverter() {
			public void addMessageDataToDataset(Message message, XYSeriesCollection ds) {
				if(message instanceof ServosStateMessage) {
					ServosStateMessage m = (ServosStateMessage)message;
					long t = System.currentTimeMillis() - graphStartTime;
					if(getServo().equals(Servo.AILERON_LEFT)) {
						ds.getSeries(0).add(t, m.getAileronLeftState());
					} else if(getServo().equals(Servo.AILERON_RIGHT)) {
						ds.getSeries(0).add(t, m.getAileronRightState());
					} else if(getServo().equals(Servo.CAMERA_PAN)) {
						ds.getSeries(0).add(t, m.getCameraPanState());
					} else if(getServo().equals(Servo.CAMERA_TILT)) {
						ds.getSeries(0).add(t, m.getCameraTiltState());
					} else if(getServo().equals(Servo.ELEVATOR)) {
						ds.getSeries(0).add(t, m.getElevatorState());
					} else if(getServo().equals(Servo.RUDDER)) {
						ds.getSeries(0).add(t, m.getRudderState());
					} else if(getServo().equals(Servo.THROTTLE)) {
						ds.getSeries(0).add(t, m.getThrottleState());
					} else if(getServo().equals(Servo.GENERIC_SERVO)) {
						ds.getSeries(0).add(t, m.getGenericServoState());
					}
				}
			}
		}, 
		getLabel());
		
		subscriberService.addMessageListener(MessageType.M2012, c);
		c.addWindowListener(new WindowListener() {
			public void windowClosing(WindowEvent e) {
				subscriberService.removeMessageListener(MessageType.M2012, c);
			}
			public void windowOpened(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {}
			public void windowActivated(WindowEvent e) {}
		});
	}

	@Override
	public void onHoldClicked(double value) {
		ServoActuationCommand sa = messagingService.resolveMessageForSending(ServoActuationCommand.class);
		sa.setServo(servo);
		sa.setVehicleID(vehicleID);
		sa.setCommandedSetpoint((float)value);
		messagingService.sendMessage(sa);
	}

	@Override
	public void onUnholdClicked() {
	}
	
	@Override
	public float getControllerValueToHoldValue(float controllerComponentValue, Component component) {
		return 0;
	}

	public void setServo(Servo servo) {
		this.servo = servo;
	}
	public Servo getServo() {
		return servo;
	}
	
	public void setVehicleID(int vehicleID) {
		this.vehicleID = vehicleID;
	}
	public int getVehicleID() {
		return vehicleID;
	}
	
}
