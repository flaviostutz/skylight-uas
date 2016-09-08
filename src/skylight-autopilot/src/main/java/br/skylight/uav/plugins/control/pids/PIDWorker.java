package br.skylight.uav.plugins.control.pids;

import java.util.logging.Logger;

import br.skylight.commons.infra.Worker;
import br.skylight.uav.infra.UAVHelper;

public abstract class PIDWorker extends Worker {

	private static final Logger logger = Logger.getLogger(PIDWorker.class.getName());
	private PIDController pidController;
	
	public PIDWorker(PIDController pidController) {
		this.pidController = pidController;
	}
	
	public void step(PIDControllers pidControllers) {
		if(isActive()) {
			step(pidController.step(getFeedbackValue()), pidControllers);
		}
	}
	
	public void setSetpoint(float setpointValue) {
		if(!Float.isNaN(setpointValue)) {
			pidController.setSetpointValue(setpointValue);
		} else {
			try {
				deactivate();
				UAVHelper.notifyStateFine(logger, "Invalid setpoint for controller " + pidController.getPIDControl() + ". setpoint=" + setpointValue, pidController.getPIDControl() + "-pidworker-invalid-setpoint");
			} catch (Exception e) {
				logger.throwing(null, null, e);
			}
		}
	}
	
	protected abstract float getFeedbackValue();
	protected abstract void step(float pidControllerOutputValue, PIDControllers pidControllers);

	protected float getErrorValue() {
		return pidController.getLastErrorValue();
	}
	
	public PIDController getPIDController() {
		return pidController;
	}

	public void reset() {
		if(pidController!=null) {
			pidController.reset();
			try {
				deactivate();
			} catch (Exception e) {
				logger.throwing(null,null,e);
				e.printStackTrace();
			}
		}
	}

}
