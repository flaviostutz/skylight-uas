package br.skylight.commons;

import java.io.DataInputStream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.text.NumberFormat;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

public enum MeasureType {

	UNDEFINED("Undefined", null),
	AIR_SPEED("Air speed", SI.METERS_PER_SECOND),
	WIND_SPEED("Wind speed", SI.METERS_PER_SECOND),
	ENGINE_SPEED("Engine speed", SI.HERTZ),
	GROUND_SPEED("Ground speed", SI.METERS_PER_SECOND),
	GROUND_ACCEL("Ground acceleration", SI.METERS_PER_SQUARE_SECOND),
	ALTITUDE("Altitude", SI.METER),
	DISTANCE("Distance", SI.METER),
	ATTITUDE_ANGLES("Attitude angles", SI.RADIAN),
	ATTITUDE_ANGLE_ACCEL("Ground acceleration", SI.RADIAN.divide(SI.SECOND)),
	HEADING("Heading", SI.RADIAN),
	PRESSURE("Pressure", SI.PASCAL),
	TEMPERATURE("Temperature", SI.KELVIN),
	TIMESTAMP("Timestamp", SI.SECOND),
	GEO_POSITION("Geo position angles", SI.RADIAN); 
	
	private String name;
	private Unit sourceUnit;
	private Unit targetUnit;
	private UnitConverter unitConverter = UnitConverter.IDENTITY;
	
	private NumberFormat nf = NumberFormat.getInstance();

	private MeasureType(String name, Unit sourceUnit) {
		this.name = name;
		this.sourceUnit = sourceUnit;
		this.targetUnit = sourceUnit;
		
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		nf.setGroupingUsed(false);
	}
	
	public String getName() {
		return name;
	}
	
	public double convertToTargetUnit(double siValue) {
		return unitConverter.convert(siValue);
	}

	public String convertToTargetUnitStr(double siValue, boolean showUnit) {
		return nf.format(convertToTargetUnit(siValue)) + (showUnit?" " + targetUnit.toString():"");
	}

	public double convertToSourceUnit(double value) {
		return unitConverter.inverse().convert(value);
	}
	
	public void setTargetUnit(Unit targetUnit) {
		if(sourceUnit!=null) {
			if(!sourceUnit.isCompatible(targetUnit)) {
				throw new IllegalArgumentException("'targetUnit' must be compatible with 'sourceUnit'. target=" + targetUnit + "; source=" + sourceUnit);
			} else {
				this.targetUnit = targetUnit;
				this.unitConverter = sourceUnit.getConverterTo(targetUnit);
			}
		}
	}
	public Unit getTargetUnit() {
		return targetUnit;
	}
	
	public Unit getSourceUnit() {
		return sourceUnit;
	}
	
	public static void readState(DataInputStream in) throws IOException {
		//unit preferences
		for (MeasureType mt : MeasureType.values()) {
			if(mt.getSourceUnit()!=null) {
				mt.setTargetUnit(Unit.valueOf(in.readUTF()));
			}
		}
	}
	
	public static void writeState(DataOutputStream out) throws IOException {
		//unit preferences
		for (MeasureType mt : MeasureType.values()) {
			if(mt.getSourceUnit()!=null) {
				out.writeUTF(mt.getTargetUnit().toString());
			}
		}
	}

}
