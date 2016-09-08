package br.skylight.commons.dli.skylight;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.Coordinates;
import br.skylight.commons.VerificationResult;
import br.skylight.commons.dli.enums.RunwayDirection;
import br.skylight.commons.dli.enums.Side;
import br.skylight.commons.infra.SerializableState;

public class Runway implements SerializableState {

	private Coordinates point1 = new Coordinates();
	private Coordinates point2 = new Coordinates();
	private float runwayWidth = 2;
	private RunwayDirection direction = RunwayDirection.RUNWAY12;//u1
	private Side maneuversSide = Side.LEFT;//u1
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		point1.readState(in);
		point2.readState(in);
		runwayWidth = in.readFloat();
		direction = RunwayDirection.values()[in.readUnsignedByte()];
		maneuversSide = Side.values()[in.readUnsignedByte()];
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		point1.writeState(out);
		point2.writeState(out);
		out.writeFloat(runwayWidth);
		out.writeByte(direction.ordinal());
		out.writeByte(maneuversSide.ordinal());
	}

	public void validate(VerificationResult r) {
		r.assertValidCoordinate(point1, "Runway point 1");
		r.assertValidCoordinate(point2, "Runway point 2");
		r.assertRange(runwayWidth, 0.1F, 200, "Runway width");
		r.assertNotNull(direction, "Runway direction");
		r.assertNotNull(maneuversSide, "Maneuvers side");
	}
	
	public Coordinates getPoint1() {
		return point1;
	}
	public void setPoint1(Coordinates point1) {
		this.point1 = point1;
	}
	public Coordinates getPoint2() {
		return point2;
	}
	public void setPoint2(Coordinates point2) {
		this.point2 = point2;
	}
	public float getRunwayWidth() {
		return runwayWidth;
	}
	public void setRunwayWidth(float runwayWidth) {
		this.runwayWidth = runwayWidth;
	}
	public RunwayDirection getDirection() {
		return direction;
	}
	public void setDirection(RunwayDirection direction) {
		this.direction = direction;
	}
	public Side getManeuversSide() {
		return maneuversSide;
	}
	public void setManeuversSide(Side maneuversSide) {
		this.maneuversSide = maneuversSide;
	}
	
	public void reset() {
		point1.reset();
		point2.reset();
		runwayWidth = 0;
		direction = RunwayDirection.values()[0];
		maneuversSide = Side.values()[0];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + ((maneuversSide == null) ? 0 : maneuversSide.hashCode());
		result = prime * result + ((point1 == null) ? 0 : point1.hashCode());
		result = prime * result + ((point2 == null) ? 0 : point2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Runway other = (Runway) obj;
		if (direction == null) {
			if (other.direction != null)
				return false;
		} else if (!direction.equals(other.direction))
			return false;
		if (maneuversSide == null) {
			if (other.maneuversSide != null)
				return false;
		} else if (!maneuversSide.equals(other.maneuversSide))
			return false;
		if (point1 == null) {
			if (other.point1 != null)
				return false;
		} else if (!point1.equals(other.point1))
			return false;
		if (point2 == null) {
			if (other.point2 != null)
				return false;
		} else if (!point2.equals(other.point2))
			return false;
		return true;
	}

}
