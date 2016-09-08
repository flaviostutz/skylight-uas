package br.skylight.flightsim;

import javax.vecmath.Point3d;

import br.skylight.flightsim.flyablebody.DragCoefficients;

public class BasicAirplaneInfo {

	//mass
	private Point3d centerOfMass;
	private double mass = 10;
	
	//wings
	private double wingSpan = 2.794;
	private double wingChordLength = 0.35;
	private double wingHeight = 0.03;
	private double wingBodyPosition = 1.1;//1.25;
	private double wingDihedral = Math.toRadians(10);
	private double wingLiftCoefficient = 1;
	private double wingDragCoefficient = DragCoefficients.AIRFOIL;
	
	//body
	private double bodyWidth = 0.1;
	private double bodyHeight = 0.12;
	private double bodyLength = 1.918;
	private double bodyFrontalDragCoefficient = DragCoefficients.SPHERE;
	private double bodyLateralDragCoefficient = DragCoefficients.LONG_CYLINDER;
	private double stabilizatorsFrontalDragCoefficient = DragCoefficients.LONG_CYLINDER;
	
	//horizontal stabilizator
	private double horizontalStabilizatorWidth = 0.8;
	private double horizontalStabilizatorLength = 0.2;
	private double horizontalStabilizatorHeight = 0.02;
	
	//vertical stabilizator
	private double verticalStabilizatorHeight = 0.25;
	private double verticalStabilizatorLength = 0.2;
	private double verticalStabilizatorWidth = 0.02;

	//aileron
	private double aileronLength = 0.1;
	private double aileronAngleRange = Math.toRadians(45);
	
	//elevator
	private double elevatorLength = 0.1;
	private double elevatorAngleRange = Math.toRadians(45);
	
	//rudder
	private double rudderLength = 0.1;
	private double rudderAngleRange = Math.toRadians(45);
	
	//engine
	private double engineMinForce = 30;
	private double engineMaxForce = 400;

	public BasicAirplaneInfo() {
		//lift position on wing must be slightly to the front of center of mass to provide torque on overall body
		centerOfMass = new Point3d(wingBodyPosition+(wingChordLength/2.0), bodyHeight/2.0, wingSpan/2.0);
	}
	
	public double getAileronAngleRange() {
		return aileronAngleRange;
	}
	public void setAileronAngleRange(double aileronAngleRange) {
		this.aileronAngleRange = aileronAngleRange;
	}
	public double getAileronLength() {
		return aileronLength;
	}
	public double getElevatorLength() {
		return elevatorLength;
	}
	public void setAileronLength(double aileronLength) {
		this.aileronLength = aileronLength;
	}
	public void setElevatorLength(double elevatorLength) {
		this.elevatorLength = elevatorLength;
	}
	public double getBodyLength() {
		return bodyLength;
	}
	public void setBodyLength(double bodyLength) {
		this.bodyLength = bodyLength;
	}
	public double getElevatorAngleRange() {
		return elevatorAngleRange;
	}
	public void setElevatorAngleRange(double elevatorAngleRange) {
		this.elevatorAngleRange = elevatorAngleRange;
	}
	public double getEngineMaxForce() {
		return engineMaxForce;
	}
	public void setEngineMaxForce(double engineMaxForce) {
		this.engineMaxForce = engineMaxForce;
	}
	public double getEngineMinForce() {
		return engineMinForce;
	}
	public void setEngineMinForce(double engineMinForce) {
		this.engineMinForce = engineMinForce;
	}
	public double getHorizontalStabilizatorLength() {
		return horizontalStabilizatorLength;
	}
	public void setHorizontalStabilizatorLength(double horizontalStabilizatorLength) {
		this.horizontalStabilizatorLength = horizontalStabilizatorLength;
	}
	public double getHorizontalStabilizatorWidth() {
		return horizontalStabilizatorWidth;
	}
	public void setHorizontalStabilizatorWidth(double horizontalStabilizatorWidth) {
		this.horizontalStabilizatorWidth = horizontalStabilizatorWidth;
	}
	public double getRudderAngleRange() {
		return rudderAngleRange;
	}
	public void setRudderAngleRange(double rudderAngleRange) {
		this.rudderAngleRange = rudderAngleRange;
	}
	public double getRudderLength() {
		return rudderLength;
	}
	public void setRudderLength(double rudderLength) {
		this.rudderLength = rudderLength;
	}
	public double getVerticalStabilizatorHeight() {
		return verticalStabilizatorHeight;
	}
	public void setVerticalStabilizatorHeight(double verticalStabilizatorHeight) {
		this.verticalStabilizatorHeight = verticalStabilizatorHeight;
	}
	public double getVerticalStabilizatorLength() {
		return verticalStabilizatorLength;
	}
	public void setVerticalStabilizatorLength(double verticalStabilizatorLength) {
		this.verticalStabilizatorLength = verticalStabilizatorLength;
	}
	public double getBodyFrontalDragCoefficient() {
		return bodyFrontalDragCoefficient;
	}
	public void setBodyFrontalDragCoefficient(double bodyFrontalDragCoefficient) {
		this.bodyFrontalDragCoefficient = bodyFrontalDragCoefficient;
	}
	public double getBodyLateralDragCoefficient() {
		return bodyLateralDragCoefficient;
	}
	public void setBodyLateralDragCoefficient(double bodyLateralDragCoefficient) {
		this.bodyLateralDragCoefficient = bodyLateralDragCoefficient;
	}
	public double getWingChordLength() {
		return wingChordLength;
	}
	public void setWingChordLength(double wingChordLength) {
		this.wingChordLength = wingChordLength;
	}
	public double getWingDihedral() {
		return wingDihedral;
	}
	public void setWingDihedral(double wingDihedral) {
		this.wingDihedral = wingDihedral;
	}
	public double getWingDragCoefficient() {
		return wingDragCoefficient;
	}
	public void setWingDragCoefficient(double wingDragCoefficient) {
		this.wingDragCoefficient = wingDragCoefficient;
	}
	public double getWingLiftCoefficient() {
		return wingLiftCoefficient;
	}
	public void setWingLiftCoefficient(double wingLiftCoefficient) {
		this.wingLiftCoefficient = wingLiftCoefficient;
	}
	public double getWingSpan() {
		return wingSpan;
	}
	public void setWingSpan(double wingSpan) {
		this.wingSpan = wingSpan;
	}

	public Point3d getCenterOfMass() {
		return centerOfMass;
	}
	public void setCenterOfMass(Point3d centerOfMass) {
		this.centerOfMass = centerOfMass;
	}
	public double getWingBodyPosition() {
		return wingBodyPosition;
	}
	public void setWingBodyPosition(double wingBodyPosition) {
		this.wingBodyPosition = wingBodyPosition;
	}
	
	public double getStabilizatorsFrontalDragCoefficient() {
		return stabilizatorsFrontalDragCoefficient;
	}
	public void setStabilizatorsFrontalDragCoefficient(double stabilizatorsFrontalDragCoefficient) {
		this.stabilizatorsFrontalDragCoefficient = stabilizatorsFrontalDragCoefficient;
	}
	
	public double getMass() {
		return mass;
	}
	public void setMass(double mass) {
		this.mass = mass;
	}
	
	public double getWingHeight() {
		return wingHeight;
	}
	public void setWingHeight(double wingHeight) {
		this.wingHeight = wingHeight;
	}

	public double getHorizontalStabilizatorHeight() {
		return horizontalStabilizatorHeight;
	}
	public void setHorizontalStabilizatorHeight(double horizontalStabilizatorHeight) {
		this.horizontalStabilizatorHeight = horizontalStabilizatorHeight;
	}
	public double getVerticalStabilizatorWidth() {
		return verticalStabilizatorWidth;
	}
	public void setVerticalStabilizatorWidth(double verticalStabilizatorWidth) {
		this.verticalStabilizatorWidth = verticalStabilizatorWidth;
	}
	
	public double getBodyHeight() {
		return bodyHeight;
	}
	public double getBodyWidth() {
		return bodyWidth;
	}
	public void setBodyHeight(double bodyHeight) {
		this.bodyHeight = bodyHeight;
	}
	public void setBodyWidth(double bodyWidth) {
		this.bodyWidth = bodyWidth;
	}
	
}
