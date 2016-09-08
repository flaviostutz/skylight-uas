package br.skylight.commons.dli.payload;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.dli.BitmappedStation;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageType;

public class EOIRConfigurationState extends Message<EOIRConfigurationState> implements MessageTargetedToStation {

	private BitmappedStation stationNumber = new BitmappedStation();//u4
	private String eoIrType;//14 chars
	private int eoIrTypeRevisionLevel;//u1
	private int eoVerticalImageDimension;
	private int eoHorizontalImageDimension;
	private int irVerticalImageDimension;
	private int irHorizontalImageDimension;
	private float elevationMin;
	private float elevationMax;
	private float azimuthMin;
	private float azimuthMax;
	
	@Override
	public MessageType getMessageType() {
		return MessageType.M301;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		super.readState(in);
		stationNumber.setData(readUnsignedInt(in));
		byte[] b = new byte[14];
		in.read(b, 0, b.length);
		eoIrType = new String(b).trim();
		eoIrTypeRevisionLevel = in.readUnsignedByte();
		eoVerticalImageDimension = in.readShort();
		eoHorizontalImageDimension = in.readShort();
		irVerticalImageDimension = in.readShort();
		irHorizontalImageDimension = in.readShort();
		elevationMin = in.readFloat();
		elevationMax = in.readFloat();
		azimuthMin = in.readFloat();
		azimuthMax = in.readFloat();
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		super.writeState(out);
		stationNumber.writeState(out);
		eoIrType = eoIrType + "              ";
		out.writeBytes(eoIrType.substring(0,14));
		out.writeByte(eoIrTypeRevisionLevel);
		out.writeShort(eoVerticalImageDimension);
		out.writeShort(eoHorizontalImageDimension);
		out.writeShort(irVerticalImageDimension);
		out.writeShort(irHorizontalImageDimension);
		out.writeFloat(elevationMin);
		out.writeFloat(elevationMax);
		out.writeFloat(azimuthMin);
		out.writeFloat(azimuthMax);
	}

	@Override
	public void resetValues() {
		stationNumber.setData(0);
		eoIrType = "Type name";
		eoIrTypeRevisionLevel = 0;
		eoVerticalImageDimension = 0;
		eoHorizontalImageDimension = 0;
		irVerticalImageDimension = 0;
		irHorizontalImageDimension = 0;
		elevationMin = 0;
		elevationMax = 0;
		azimuthMin = 0;
		azimuthMax = 0;
	}

	public BitmappedStation getStationNumber() {
		return stationNumber;
	}

	public String getEoIrType() {
		return eoIrType;
	}

	public void setEoIrType(String eoIrType) {
		this.eoIrType = eoIrType;
	}

	public int getEoIrTypeRevisionLevel() {
		return eoIrTypeRevisionLevel;
	}

	public void setEoIrTypeRevisionLevel(int eoIrTypeRevisionLevel) {
		this.eoIrTypeRevisionLevel = eoIrTypeRevisionLevel;
	}

	public int getEoVerticalImageDimension() {
		return eoVerticalImageDimension;
	}

	public void setEoVerticalImageDimension(int eoVerticalImageDimension) {
		this.eoVerticalImageDimension = eoVerticalImageDimension;
	}

	public int getEoHorizontalImageDimension() {
		return eoHorizontalImageDimension;
	}

	public void setEoHorizontalImageDimension(int eoHorizontalImageDimension) {
		this.eoHorizontalImageDimension = eoHorizontalImageDimension;
	}

	public int getIrVerticalImageDimension() {
		return irVerticalImageDimension;
	}

	public void setIrVerticalImageDimension(int irVerticalImageDimension) {
		this.irVerticalImageDimension = irVerticalImageDimension;
	}

	public int getIrHorizontalImageDimension() {
		return irHorizontalImageDimension;
	}

	public void setIrHorizontalImageDimension(int irHorizontalImageDimension) {
		this.irHorizontalImageDimension = irHorizontalImageDimension;
	}

	public float getElevationMin() {
		return elevationMin;
	}

	public void setElevationMin(float elevationMin) {
		this.elevationMin = elevationMin;
	}

	public float getElevationMax() {
		return elevationMax;
	}

	public void setElevationMax(float elevationMax) {
		this.elevationMax = elevationMax;
	}

	public float getAzimuthMin() {
		return azimuthMin;
	}

	public void setAzimuthMin(float azimuthMin) {
		this.azimuthMin = azimuthMin;
	}

	public float getAzimuthMax() {
		return azimuthMax;
	}

	public void setAzimuthMax(float azimuthMax) {
		this.azimuthMax = azimuthMax;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (stationNumber.getData() ^ (stationNumber.getData() >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EOIRConfigurationState other = (EOIRConfigurationState) obj;
		if (stationNumber != other.stationNumber)
			return false;
		return true;
	}

	public void copyFrom(EOIRConfigurationState c) {
		stationNumber.setData(c.getStationNumber().getData());
		eoIrType = c.eoIrType;
		eoIrTypeRevisionLevel = c.eoIrTypeRevisionLevel;
		eoVerticalImageDimension = c.eoVerticalImageDimension;
		eoHorizontalImageDimension = c.eoHorizontalImageDimension;
		irVerticalImageDimension = c.irVerticalImageDimension;
		irHorizontalImageDimension = c.irHorizontalImageDimension;
		elevationMin = c.elevationMin;
		elevationMax = c.elevationMax;
		azimuthMin = c.azimuthMin;
		azimuthMax = c.azimuthMax;
	}

	public float getImageWidthToHeightRatio() {
		return (float)eoHorizontalImageDimension/(float)eoVerticalImageDimension;
	}

	@Override
	public BitmappedStation getTargetStations() {
		return stationNumber;
	}
	
}