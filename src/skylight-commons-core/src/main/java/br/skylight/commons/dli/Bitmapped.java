package br.skylight.commons.dli;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.SerializableState;

public class Bitmapped implements SerializableState {

	private long data;//u4
	
	@Override
	public void readState(DataInputStream in) throws IOException {
		data = IOHelper.readUnsignedInt(in);
	}

	@Override
	public void writeState(DataOutputStream out) throws IOException {
		out.writeInt((int)data);
	}
	
	public void setBit(int pos, boolean value) {
		long mask = (long) (1 << pos);
		if (value) {
			data = (long) (data | mask);
		} else {
			data = (long) (data & ~mask);
		}
	}
	
	public boolean isBit(int pos) {
		pos = (int)Math.min(pos, 63);
		return ((data & (long) (1L << pos)) != 0);
	}
	
	public boolean[] getBits() {
		boolean[] bits = new boolean[(int)data];
		for (int i=0; i<(int)data; i++) {
			if(isBit(i)) {
				bits[i] = true;
			}
		}
		return bits;
	}

	public void reset() {
		data = 0;
	}
	
	public void setData(long data) {
		this.data = data;
	}
	
	public long getData() {
		return data;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (data ^ (data >>> 32));
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
		Bitmapped other = (Bitmapped) obj;
		if (data != other.data)
			return false;
		return true;
	}
	
}
