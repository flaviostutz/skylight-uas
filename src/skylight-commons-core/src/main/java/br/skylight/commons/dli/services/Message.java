package br.skylight.commons.dli.services;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import br.skylight.commons.MeasureType;
import br.skylight.commons.dli.annotations.MessageField;
import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.SerializableState;

public abstract class Message<T extends Message<T>> implements SerializableState {

	public static int BROADCAST_ID = (int)Long.parseLong("FFFFFFFF", 16);
	public static int NULL_ID = Integer.parseInt("FF0000", 16);

	//common attributes
	@MessageField(number=1, measureType=MeasureType.TIMESTAMP)
	public double timeStamp;
	@MessageField(number=2)
	public int vehicleID;
	@MessageField(number=3)
	public int cucsID;

	//transient
	private long messageInstanceId;
	private double receiveTimeStamp;
	private long reuseCounter;
	
	public Field getField(int fieldNumber) {
		return getMessageType().getFields().get(fieldNumber);
	}
	
	public int getFieldCount() {
		return getMessageType().getFields().size();
	}
	
	public MessageField getMessageField(int fieldNumber) {
		return getField(fieldNumber).getAnnotation(MessageField.class);
	}

	/**
	 * Returns NaN if this is not a number field 
	 * @param fieldNumber
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public Double getValue(int fieldNumber) throws IllegalArgumentException, IllegalAccessException {
		Field f = getField(fieldNumber);
		MessageField mf = getMessageField(fieldNumber);
		if(mf.measureType()!=null) {
			double value = Double.NaN;
			if(f.getType().equals(Short.class)) {
				value = f.getShort(this);
			} else if(f.getType().equals(Integer.class)) {
				value = f.getInt(this);
			} else if(f.getType().equals(Long.class)) {
				value = f.getLong(this);
			} else if(f.getType().equals(Float.class)) {
				value = f.getFloat(this);
			} else if(f.getType().equals(Double.class)) {
				value = f.getDouble(this);
			}
			if(!Double.isNaN(value)) {
				return value;
			} else {
				return Double.NaN;
			}
		} else {
			return Double.NaN;
		}
	}
	
	public String getFormattedValue(int fieldNumber) throws IllegalArgumentException, IllegalAccessException {
		Field f = getField(fieldNumber);
		double value = getValue(fieldNumber);
		MessageField mf = getMessageField(fieldNumber);
		if(!Double.isNaN(value)) {
			return mf.measureType().convertToTargetUnitStr(value, true);
		} else {
			Object o = f.get(this);
			if(o!=null) {
				return o.toString();
			} else {
				return null;
			}
		}
	}
	
	public abstract MessageType getMessageType();
	
	public double getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(double timeStamp) {
		this.timeStamp = timeStamp;
	}
	public int getVehicleID() {
		return vehicleID;
	}
	public void setVehicleID(int vehicleID) {
		this.vehicleID = vehicleID;
	}
	public int getCucsID() {
		return cucsID;
	}
	public void setCucsID(int cucsID) {
		this.cucsID = cucsID;
	}
	
	public void setMessageInstanceId(long messageInstanceId) {
		this.messageInstanceId = messageInstanceId;
	}
	public long getMessageInstanceId() {
		return messageInstanceId;
	}
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		timeStamp = in.readDouble();
		vehicleID = in.readInt();
		cucsID = in.readInt();
	}
	
	@Override
	public void writeState(DataOutputStream out) throws IOException {
		out.writeDouble(timeStamp);
		out.writeInt(vehicleID);
		out.writeInt(cucsID);
	}
	
	protected final long readUnsignedInt(DataInput in) throws IOException {
		return IOHelper.readUnsignedInt(in);
	}
	protected final String readNullTerminatedString(DataInput in) throws IOException {
		byte[] buffer = new byte[1024];
		int c = 0;
		byte b;
		while((b=in.readByte())!=0x0) {
			buffer[c++] = b;
		}
		return new String(buffer, 0, c, "US-ASCII");
	}
	protected final void writeNullTerminatedString(DataOutput out, String value) throws IOException {
		out.write(value.getBytes("US-ASCII"));
		out.write(0x0);//null termination
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cucsID;
		result = prime * result + vehicleID;
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
		Message other = (Message) obj;
		if (cucsID != other.cucsID)
			return false;
		if (vehicleID != other.vehicleID)
			return false;
		return true;
	}

	public boolean isUseInstanceCacheOptimization() {
		return true;
	}
	
	public double getTimeElapsed() {
		return (System.currentTimeMillis()/1000.0) - timeStamp;
	}
	
	public double getReceiveTimeStamp() {
		return receiveTimeStamp;
	}
	public void setReceiveTimeStamp(double receiveTimeStamp) {
		this.receiveTimeStamp = receiveTimeStamp;
	}
	
	public double getLatency() {
		return receiveTimeStamp - timeStamp;
	}
	
	/**
	 * Called when this instance is reused
	 */
	public abstract void resetValues();
	
	protected void incrementReuseCounter() {
		reuseCounter++;
	}

	public void decrementReuseCounter() {
		reuseCounter--;
	}
	
	public long getReuseCounter() {
		return reuseCounter;
	}
	
	/**
	 * Creates a copy of this message.
	 * Important to be used when there is a need to store in memory messages that were received in MessagingService
	 * because those instance are reused over time, so you need to copy it to avoid problems.
	 * @return
	 */
	public Message<T> createCopy() {
		try {
			Message m = getMessageType().getImplementation().newInstance();
			IOHelper.copyState(m, this);
			return m;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}