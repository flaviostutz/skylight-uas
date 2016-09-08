package br.skylight.uav.plugins.control.pids;

import java.util.logging.Logger;

import br.skylight.commons.dli.skylight.PIDControl;
import br.skylight.commons.infra.LinearRegression;
import br.skylight.commons.infra.MathHelper;

public class PIDController {

	private static final Logger logger = Logger.getLogger(PIDController.class.getName());
	
	//misc variables
	private PIDControl pidControl;
	private float lastFeedbackValue = Float.NaN;
	private float lastOutputValue = Float.NaN;
	private float lastErrorValue = Float.NaN;
	
	private float lastProportionalValue = Float.NaN;
	private float lastIntegralValue = Float.NaN;
	private float lastDifferentialValue = Float.NaN;

	private float integralAcum;
	
	private boolean inverseOutput = false;
	private float setpointValue;
	private float forcedOutputValue=Float.NaN;
	private float normalizeErrorTo=Float.NaN;
	private float normalizationReference;
	
	//desaturators
	private float restrictOutputFrom = -Long.MAX_VALUE;
	private float restrictOutputTo = Long.MAX_VALUE;

	private float restrictSetpointFrom = -Long.MAX_VALUE;
	private float restrictSetpointTo = Long.MAX_VALUE;

	//integrator - accumulated value limits
	private float integratorMinValue = -Long.MAX_VALUE;// minimum value to integrator
	private float integratorMaxValue = Long.MAX_VALUE;// maximum value to integrator
	private float integratorUsageErrorThreshold = Float.NaN;

	//PID gains
	private float feedbackToOutputScale = 1;
	private float proportionalGain = 1;// proportional gain
	private float kp = 1;//internal scaled gain
	
	private float integralGain = 0;// integral gain
	private float ki = 0;//internal scaled gain
	
	private float differentialGain = 0;// differential gain
	private float kd = 0;//internal scaled gain
	private LinearRegression differentialTermRegression = new LinearRegression(20);

	private long lastStepTime = System.currentTimeMillis();
	private long startTime = System.currentTimeMillis();
	
	public PIDController(PIDControl pidControl) {
		this.pidControl = pidControl;
	}

	/**
	 * This will calculate the next step of the controller.
	 * If the minimum time specified between steps was not reached,
	 * this method will block the thread until minimum interval is reached.
	 * @param inputValue
	 * @param measureTime
	 * @return
	 */
	public float step(float feedbackValue) {
		float error = setpointValue - feedbackValue;
		
		//normalize error to a desired value
		if(!Float.isNaN(normalizeErrorTo)) {
			if(error>normalizationReference) {
				error = error - normalizeErrorTo;
			} else if(error<-normalizationReference) {
				error = normalizeErrorTo + error;
			}
		}
		
		// calculate the proportional term
		float proportionalValue = kp * error;
		//this condition may happen if feedback is NaN!
		if(Float.isNaN(proportionalValue)) {
			proportionalValue = 0;
		}
		
		//calculate the integral term
		float integralValue = 0;
		if(ki!=0) {
			//integral part impact (according to time elapsed)
			float integralImpact = (float)((System.currentTimeMillis()-lastStepTime)/66.0);
			
			//integral part impact (adjust it to be used only when error is around zero)
			if(!Float.isNaN(integratorUsageErrorThreshold)) {
				integralImpact *= 1-Math.min(Math.abs(error/integratorUsageErrorThreshold),1);
//				System.out.println(">>>"+error + " " + integratorUsageErrorThreshold);
//				System.out.println(1-Math.min(Math.abs(error/integratorUsageErrorThreshold),1));
			}
			if(integralImpact==0) {
				integralAcum = 0;
			}
//			System.out.println(integralImpact);
			
			//Be careful with this code because it is not intuitive
			//multiply error by a reference time elapsed
			integralAcum += error * integralImpact;
			integralAcum = MathHelper.clamp(integralAcum, integratorMinValue/ki, integratorMaxValue/ki);
			integralValue = ki * integralAcum;
			lastStepTime = System.currentTimeMillis();
		}

		//calculate the differential term
		float differentialValue = 0;
		if(kd!=0) {
			if(!Float.isNaN(error)) {
				differentialTermRegression.addSample((System.currentTimeMillis()-startTime), error);
				differentialValue = differentialTermRegression.calculateBestYValue();
				differentialValue *= kd;
				if(Float.isNaN(differentialValue)) {
					differentialValue = 0;
				}
			}
		
//			differentialValue = (kd * (error - lastErrorValue));//elapsedTime;
//			if(!Float.isNaN(differentialValue)) {
//				if(differentialTermRegression.getMaxNumberOfSamples()>1) {
//					differentialTermRegression.addSample(differentialValue);
//					differentialValue = differentialTermRegression.calculateBestYValue();
//				}
//			} else {
//				differentialValue = 0;
//			}
		}

		//CALCULATE OUTPUT VALUE
		//pure output
		float outputValue = proportionalValue + integralValue + differentialValue;
		
		//output clamp
		outputValue = MathHelper.clamp(outputValue, restrictOutputFrom, restrictOutputTo);

		//force to a specific value (for tuning composite controller purposes)
		if(!Float.isNaN(forcedOutputValue)) {
			outputValue = forcedOutputValue;
		}
		
		//output inversion
		if(inverseOutput) {
			outputValue = -outputValue;
		}
		if(Float.isNaN(outputValue)) {
			logger.warning("NaN: "+ pidControl +" outputValue="+ outputValue +"; setPoint=" + setpointValue + "; currentValue=" + feedbackValue + "; targetSetPointValue=" + setpointValue);
		}
		
		//remember last values
		lastFeedbackValue = feedbackValue;
		lastErrorValue = error;
		lastOutputValue = outputValue;
		
		lastProportionalValue = proportionalValue;
		lastIntegralValue = integralValue;
		lastDifferentialValue = differentialValue;
		
//		System.out.println(pidControl.getName() + ": S=" + setpointValue + "; O=" + lastOutputValue + "; F=" + lastFeedbackValue + "; P=" + lastProportionalValue + "; I=" + lastIntegralValue + "; D=" + lastDifferentialValue);
//		lastStepTime = measureTime;
		
		return outputValue;
	}

	public PIDControl getPIDControl() {
		return pidControl;
	}
	
	public void setProportionalGain(float proportionalGain) {
		this.proportionalGain = proportionalGain;
		updateGainsScale();
	}

	public void setIntegralGain(float integralGain, float integralTermMin, float integralTermMax) {
		this.integralGain = integralGain;
		this.integratorMinValue = integralTermMin;
		this.integratorMaxValue = integralTermMax;
//		if(integralGain!=0) {
//			this.integratorMinValue = integralTermMin/integralGain;
//			this.integratorMaxValue = integralTermMax/integralGain;
//		} else {
//			this.integratorMinValue = 0;
//			this.integratorMaxValue = 0;
//		}
		updateGainsScale();
	}

	public void setDifferentialGain(float differentialGain) {
		this.differentialGain = differentialGain;
		updateGainsScale();
	}

	public float getIntegratorMinValue() {
		return integratorMinValue;
	}

	public float getIntegratorMaxValue() {
		return integratorMaxValue;
	}

	public float getProportionalGain() {
		return proportionalGain;
	}

	public float getIntegralGain() {
		return integralGain;
	}

	public float getDifferentialGain() {
		return differentialGain;
	}

	public void setRestrictOutputFrom(float restrictOutputFrom) {
		this.restrictOutputFrom = restrictOutputFrom;
	}

	public void setRestrictOutputTo(float restrictOutputTo) {
		this.restrictOutputTo = restrictOutputTo;
	}

	public float getSetpointValue() {
		return setpointValue;
	}

	/**
	 * Sets the target setPoint to be used during steps.
	 * @param targetSetPointValue
	 */
	public void setSetpointValue(float targetSetPointValue) {
		//setpoint clamp
		targetSetPointValue = MathHelper.clamp(targetSetPointValue, restrictSetpointFrom, restrictSetpointTo);

		this.setpointValue = targetSetPointValue;
	}

	/**
	 * Sometimes the output has an inverse sign from feedback, but are proportional.
	 * This inverses the output signal.
	 * @param inverseOutput
	 */
	public void setInverseOutput(boolean inverseOutput) {
		this.inverseOutput = inverseOutput;
	}

	/**
	 * The differential part of PID may be implemented based on error or the input, depending
	 * on specific needs. If this is false, it will be based on error, else will be based on
	 * input. Defaults to false.
	 * @param derivativeBasedOnInput
	 */
//	public void setUseFeedbackValueForDifferentialTerm(boolean useFeedbackValueForDifferentialTerm) {
//		this.useFeedbackValueForDifferentialTerm = useFeedbackValueForDifferentialTerm;
//	}

	/**
	 * Force this controller's output to an specified value.
	 * This is mainly used in a chain of controllers during tuning process.
	 * Image the following chain of controllers: A -> B
	 * You can fix the output of A (that will be used as input for B) and adjust
	 * the gains for B without changing significantly the chain.
	 * @param forcedOutputValue
	 */
	public void setForcedOutputValue(float forcedOutputValue) {
		this.forcedOutputValue = forcedOutputValue;
	}

	public float getLastFeedbackValue() {
		return lastFeedbackValue;
	}

	public float getLastOutputValue() {
		return lastOutputValue;
	}

	public void setRestrictSetpointFrom(float restrictSetpointFrom) {
		this.restrictSetpointFrom = restrictSetpointFrom;
	}

	public void setRestrictSetpointTo(float restrictSetpointTo) {
		this.restrictSetpointTo = restrictSetpointTo;
	}

	/**
	 * Normalize the error in cases where the range is circular.
	 * For example, set this to 360 to control heading, roll or pitch, because
	 * they have a range of -180 to 180. We want to go from one point to
	 * another by the shortest path. So, if you want to go from -170 to 170,
	 * you don't have to go from -170 to 170 passing by zero (a path of 340 degrees), 
	 * but you want to pass by the position 180 (a path of just 20 degrees). 
	 * @param normalizeErrorTo in radians
	 */
	public void setNormalizeErrorTo(float normalizeErrorTo) {
		this.normalizeErrorTo = normalizeErrorTo;
		this.normalizationReference = normalizeErrorTo/2;
	}
	
	public float getLastDifferentialValue() {
		return lastDifferentialValue;
	}
	public float getLastProportionalValue() {
		return lastProportionalValue;
	}
	public float getLastIntegralValue() {
		return lastIntegralValue;
	}
	public float getLastErrorValue() {
		return lastErrorValue;
	}

	public void reset() {
		integralAcum = 0;
		lastErrorValue = Float.NaN;
		differentialTermRegression.reset();
	}

	/**
	 * Give a sense of scale between feedback values (related to errors) and
	 * the output generated by this controller.
	 * This is used to avoid PID gains with too high or too low values.
	 * For example, if an error (related to setpoint-feedback) is '0.1' and you know a output of around '10000' is needed
	 * in your system, you should set this value to around '100000', so that the PID gains could remain with values around 0.1 and 10.
	 * This is only a pratical feature with no great mathematical implications to the PID control.
	 * @param feedbackToOutputScale value that will be multiplied by current pid gains. Default value is '1'.
	 */
	public void setFeedbackToOutputScale(float feedbackToOutputScale) {
		this.feedbackToOutputScale = feedbackToOutputScale;
		updateGainsScale();
	}

	public float getFeedbackToOutputScale() {
		return feedbackToOutputScale;
	}

	private void updateGainsScale() {
		kp = proportionalGain * feedbackToOutputScale;
		ki = integralGain * feedbackToOutputScale;
		kd = differentialGain * feedbackToOutputScale;
	}
	
	/**
	 * Setup feedback to output scale using an estimated sample variation in feedback and output value
	 * @see setFeedbackToOutputScale(float feedbackToOutputScale)
	 */
	public void setFeedbackToOutputScale(float sampleFeedbackVariation, float sampleOutputVariation) {
		setFeedbackToOutputScale(sampleOutputVariation/sampleFeedbackVariation);
	}
	
	public void setIntegratorMinValue(float integratorMinValue) {
		this.integratorMinValue = integratorMinValue;
	}
	
	public void setIntegratorMaxValue(float integratorMaxValue) {
		this.integratorMaxValue = integratorMaxValue;
	}
	
	public void setDifferentialTermRegressionSamples(int numberOfSamples) {
		differentialTermRegression = new LinearRegression(numberOfSamples);
	}
	
	public float getRestrictOutputFrom() {
		return restrictOutputFrom;
	}
	public float getRestrictOutputTo() {
		return restrictOutputTo;
	}
	
	public float getRestrictSetpointFrom() {
		return restrictSetpointFrom;
	}
	public float getRestrictSetpointTo() {
		return restrictSetpointTo;
	}

	public void setIntegratorUsageErrorThreshold(float integratorUsageErrorThreshold) {
		this.integratorUsageErrorThreshold = integratorUsageErrorThreshold;
	}
	public float getIntegratorUsageErrorThreshold() {
		return integratorUsageErrorThreshold;
	}
	
}
