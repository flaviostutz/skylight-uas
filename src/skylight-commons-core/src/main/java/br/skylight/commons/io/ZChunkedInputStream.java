package br.skylight.commons.io;

import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import br.skylight.commons.infra.SyncCondition;

import com.jcraft.jzlib.ZInputStream;
import com.jcraft.jzlib.ZStreamException;

public class ZChunkedInputStream extends ZInputStream {

	private static final Logger logger = Logger.getLogger(ZChunkedInputStream.class.getName());
	
	private int level;
	private boolean nowrap;
	private int reinitCompressionOn = -1;

	private InputStream originalSuperIn;
	private Lock reinitControlLock = new ReentrantLock();

	private SyncCondition pendingChunkInit = new SyncCondition("pending chunk init");
	private boolean bypass;
	
	private byte[] buf1 = new byte[1];
	
	public ZChunkedInputStream(InputStream in, boolean nowrap) throws IOException {
		super(in, nowrap);
		this.nowrap = nowrap;
		prepareSuperIn();
	}

	public ZChunkedInputStream(InputStream in, int level) throws IOException {
		super(in, level);
		this.level = level;
		prepareSuperIn();
	}

	public ZChunkedInputStream(InputStream in) throws IOException {
		super(in);
		prepareSuperIn();
	}

	private InputStream getSuperIn() {
		return super.in;
	}

	/**
	 * Exchange super.in with our piped stream implementation
	 * 
	 * @throws IOException
	 */
	private void prepareSuperIn() throws IOException {
		this.originalSuperIn = super.in;
		SequenceStreamFilter sf = new SequenceStreamFilter(ZChunkedOutputStream.END_MARK, true);
		sf.setSequenceListener(new SequenceListener() {
			@Override
			public void onSequenceFound(byte[] fullSequence, byte[] body, OutputStream os) {
				if(pendingChunkInit.isConditionMet()) {
					try {
						pendingChunkInit.waitForConditionNotMet(10000);
						logger.finest("Double chunk arrival resolved automatically");
					} catch (TimeoutException e) {
						logger.warning("Discarding unprocessed chunk because a newer chunk has arrived. At least 1024 bytes must be gotten in read() operations in this stream each 10s");
					}
				}
				
				reinitControlLock.lock();
				try {
					reinitCompressionOn = ((FilteredInputStream<StreamFilter>) getSuperIn()).available();
					pendingChunkInit.notifyConditionMet();
//					System.out.println("FOUND CHUNK " + reinitCompressionOn);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					reinitControlLock.unlock();
				}
			}
		});
		FilteredInputStream<StreamFilter> fis = new FilteredInputStream<StreamFilter>(super.in, sf, 15000) {
			public int read(byte[] b) throws IOException {
				return read(b, 0, b.length);
			}
			public int read(byte[] b, int off, int len) throws IOException {
				//avoid deadlocks. Only continue if there is available data
				while(super.available()==0) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				reinitControlLock.lock();
				try {
					checkCompressionReinit();
					if(reinitCompressionOn!=-1) {
						int r = super.read(b, off, Math.min(reinitCompressionOn, len));
						reinitCompressionOn -= r;
						return r;
					} else {
						return super.read(b, off, len);
					}
				} finally {
					reinitControlLock.unlock();
				}
			}
			public int read() throws IOException {
				//avoid deadlocks. Only continue if there is available data
				while(super.available()==0) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				reinitControlLock.lock();
				try {
					checkCompressionReinit();
					int rb = super.read();
					if(reinitCompressionOn!=-1) {
						reinitCompressionOn--;
					}
					return rb;
				} finally {
					reinitControlLock.unlock();
				}
			}
			@Override
			public long skip(long n) throws IOException {
				reinitControlLock.lock();
				try {
					reinitCompressionOn-=n;
					return super.skip(n);
				} finally {
					reinitControlLock.unlock();
				}
			}
		};
		super.in = fis;
		fis.startFilteredMode();
	}

	private void checkCompressionReinit() {
//		System.out.println("COMPRESSION REINIT CHECK "+reinitCompressionOn);
		if (reinitCompressionOn == 0) {
			if (compress) {
				z.deflateInit(level);
			} else {
				z.inflateInit(nowrap);
			}
			reinitCompressionOn = -1;
			pendingChunkInit.notifyConditionNotMet();
		}
	}

	@Override
	public int read() throws IOException {
		if(bypass) {
			return in.read();
		} else {
			if (read(buf1, 0, 1) == -1) {
				return -1;
			}
			return (buf1[0] & 0xFF);
		}
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		if(bypass) {
			return in.read(b);
		} else {
			return super.read(b, 0, b.length);
		}
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if(bypass) {
			return in.read(b, off, len);
		} else {
			int totalRead = 0;
	
			// read as many chunks as necessary to fill request
			while (totalRead < len) {
	
				// READ DATA FROM super.read(..)
				try {
					int r = super.read(b, off + totalRead, len - totalRead);
					if (r != -1) {
						totalRead += r;
					} else {
						if (totalRead == 0) {
							return -1;
						} else {
							return totalRead;
						}
					}
				} catch (ZStreamException e) {
					logger.info("Problem uncompressing stream. Ignoring chunk.");
					reinitControlLock.lock();
					if(reinitCompressionOn>0) {
						super.in.skip(reinitCompressionOn);
					}
					reinitControlLock.unlock();
				}
			}
			return totalRead;
		}
	}

	@Override
	public void close() throws IOException {
		super.close();
		originalSuperIn.close();
	}

	public void setCompressionBypass(boolean bypass) {
		this.bypass = bypass;
	}

}
