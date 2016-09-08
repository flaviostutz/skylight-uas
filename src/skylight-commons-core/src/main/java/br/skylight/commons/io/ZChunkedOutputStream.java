package br.skylight.commons.io;

import java.io.IOException;
import java.io.OutputStream;

import com.jcraft.jzlib.ZOutputStream;

public class ZChunkedOutputStream extends ZOutputStream {

	protected static final byte[] END_MARK = new byte[] {0x0, 0x21, 0x51, -0x7F};

	private int level;
	private boolean nowrap;
	private boolean bypass;
	
	private int maxChunkSize = 3000;
	private long maxChunkTimeMillis = 1000;
	
	private int currentChunkSize = 0;
	private long currentChunkStartTimeMillis = 0;

	public ZChunkedOutputStream(OutputStream out, int level, boolean nowrap) throws IOException {
		super(out, level, nowrap);
		this.level = level;
		this.nowrap = nowrap;
	}

	public ZChunkedOutputStream(OutputStream out, int level) {
		super(out, level);
		this.level = level;
	}

	public ZChunkedOutputStream(OutputStream out) {
		super(out);
	}

	@Override
	public void write(int b) throws IOException {
		if(bypass) {
			out.write(b);
		} else {
			synchronized(out) {
				super.write(b);
				currentChunkSize++;
				checkCurrentChunk();
			}
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		super.write(b, 0, b.length);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if(bypass) {
			out.write(b, off, len);
		} else {
			synchronized(out) {
				super.write(b, off, len);
				currentChunkSize+=len;
				checkCurrentChunk();
			}
		}
	}
	
	private void checkCurrentChunk() throws IOException {
		if((System.currentTimeMillis()-currentChunkStartTimeMillis)>maxChunkTimeMillis
			|| currentChunkSize>maxChunkSize) {
			startNewChunk();
		}
	}

	public void startNewChunk() throws IOException {
		synchronized(out) {
			//send end chunk mark
			out.write(END_MARK);
//			for(int i=0; i<END_MARK.length; i++) {
//				out.write(END_MARK[i]);
//			}
//			System.out.println("\n\n>>> WRITE CHUNK "+(++chunkCount));						
			out.flush();
			
			//prepare compressor for next chunk
			if(compress) {
				z.deflateInit(level, nowrap);
			} else {
				z.inflateInit();
			}
			currentChunkStartTimeMillis = System.currentTimeMillis();
			currentChunkSize = 0;
		}
	}

	public void setCompressionBypass(boolean bypass) {
		this.bypass = bypass;
	}
	
}
