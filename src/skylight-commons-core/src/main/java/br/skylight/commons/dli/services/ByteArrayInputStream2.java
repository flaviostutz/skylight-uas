package br.skylight.commons.dli.services;

import java.io.ByteArrayInputStream;

public class ByteArrayInputStream2 extends ByteArrayInputStream {

	public ByteArrayInputStream2(byte[] buf, int offset, int length) {
		super(buf, offset, length);
	}

	public ByteArrayInputStream2(byte[] buf) {
		super(buf);
	}
	
	public void setBuffer(byte[] buffer) {
		buf = buffer;
		count = buf.length;
		reset();
	}
	
}
