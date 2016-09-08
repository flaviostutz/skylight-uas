package br.skylight.commons.dli.services;

import java.io.ByteArrayOutputStream;

public class ByteArrayOutputStream2 extends ByteArrayOutputStream {

	public ByteArrayOutputStream2() {
	}
	
	public ByteArrayOutputStream2(int size) {
		super(size);
	}
	
	public ByteArrayOutputStream2(byte[] buffer) {
		buf = buffer;
	}

	public byte[] getBuffer() {
		return buf;
	}
	
}
