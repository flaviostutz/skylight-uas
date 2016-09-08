package br.skylight.commons.infra;

public class ByteSequenceComparator {

	//circular buffer
	protected byte buffer[];
	//current position in circular buffer
	protected int pos = 0;
	
	private int i;
	private int j;
	private int t;
	
	public ByteSequenceComparator() {
		this(128);
	}
	
	public ByteSequenceComparator(int size) {
		buffer = new byte[size];
		pos = 0;
	}

	public synchronized void addByte(int b) {
		addByte((byte)(b & 0xFF));
	}
	
	public synchronized void addByte(byte b) {
		buffer[pos++] = b;
		if (pos >= buffer.length) {
			pos = 0;
		}
	}
	
	/**
	 * Look for those bytes as the last bytes added to circular buffer
	 * @param reference
	 * @return
	 */
	public synchronized boolean isLastBytesMatch(byte[] reference) {
		return isLastBytesMatch(reference, 0, reference.length);
	}
	public synchronized boolean isLastBytesMatch(byte[] reference, int offset, int len) {
		for(i=offset;i<offset+len; i++) {
			t = pos-len+i;//(pos-1) + 1 = pos
			if(t<0) {
				t += buffer.length;
			}
			if(reference[i]!=buffer[t]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Looks for a partial match of last bytes from reference.
	 * If the last byte is equal to the first reference byte array, it will return true.
	 * If the last byte and the before last byte are equals to the first and second bytes in reference, it returns true. And so on.
	 * @param reference
	 * @return
	 */
	public synchronized boolean isLastBytesPartial(byte[] reference) {
		for(j=reference.length;j>0; j--) {
			if(isLastBytesMatch(reference, 0, j)) {
				return true;
			}
		}
		return false;
	}
}
