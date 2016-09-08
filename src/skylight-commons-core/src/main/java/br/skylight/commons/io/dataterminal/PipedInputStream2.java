package br.skylight.commons.io.dataterminal;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class PipedInputStream2 extends PipedInputStream {

	public PipedInputStream2() {
		super();
	}

	public PipedInputStream2(int pipeSize) {
		super(pipeSize);
	}

	public PipedInputStream2(PipedOutputStream src, int pipeSize) throws IOException {
		super(src, pipeSize);
	}

	public PipedInputStream2(PipedOutputStream src) throws IOException {
		super(src);
	}

	@Override
	public synchronized int read() throws IOException {
		try {
			return super.read();
		} finally {
			synchronized(this) {
				notifyAll();
			}
		}
	}
	
}
