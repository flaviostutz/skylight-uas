package br.skylight.commons.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedOutputStream;

import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.io.dataterminal.PipedInputStream2;

public class FilteredInputStream<T extends StreamFilter> extends FilterInputStream {

	private static final int IDLE_SLEEP_TIME = 10;//sleep time to avoid CPU overuse
	
	private boolean filteredMode = false;
	private int filterBufferSize = 1024;
	
	private PipedInputStream2 normalInPipe;
	private PipedOutputStream normalOutPipe;
	
	private ThreadWorker pipeWorker;
	private T streamFilter;
	
	private boolean highThoughputMode = true;
	
	//mode synchronization
//	private Semaphore modeLock = new Semaphore(1);

	public FilteredInputStream(InputStream inputStream, T streamFilter) throws IOException {
		this(inputStream, streamFilter, 1024);
	}
	
	public FilteredInputStream(InputStream inputStream, T streamFilter, int filterBufferSize) throws IOException {
		super(inputStream);
		this.filterBufferSize = filterBufferSize;
		normalInPipe = new PipedInputStream2(filterBufferSize);
		normalOutPipe = new PipedOutputStream(normalInPipe);
		if(streamFilter==null) throw new IllegalArgumentException("'streamFilter' cannot be null");
		this.streamFilter = streamFilter;
	}

	public T getStreamFilter() {
		return streamFilter;
	}
	
	@Override
	public int read() throws IOException {
		while(true) {
			modeLockAcquire();
			try {
				if(!filteredMode) {
					if(highThoughputMode || super.in.available()>0) {
						return super.in.read();
					} else {
						sleep(IDLE_SLEEP_TIME);
					}
				} else {
					if(highThoughputMode || normalInPipe.available()>0) {
						return normalInPipe.read();
					} else {
						sleep(IDLE_SLEEP_TIME);
					}
				}
			} finally {
				modeLockRelease();
			}
		}
	}

	@Override
	public int read(byte[] b) throws IOException {
		while(true) {
			modeLockAcquire();
			try {
				if(!filteredMode) {
					if(highThoughputMode || super.in.available()>0) {
//						System.out.println("NORMAL READ");
						return super.in.read(b);
					}
				} else {
					if(highThoughputMode || normalInPipe.available()>0) {
//						System.out.println("FILTERED READ");
						return normalInPipe.read(b);
					}
				}
			} finally {
				modeLockRelease();
			}
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		while(true) {
			modeLockAcquire();
			try {
				if(!filteredMode) {
					if(highThoughputMode || super.in.available()>0) {
//						System.out.println("NORMAL READ");
						return super.in.read(b, off, len);
					}
				} else {
					if(highThoughputMode || normalInPipe.available()>0) {
//						System.out.println("FILTERED READ");
						return normalInPipe.read(b, off, len);
					}
				}
			} finally {
				modeLockRelease();
			}
		}
	}

	private void modeLockAcquire() {
//		try {
//			modeLock.acquire();
//		} catch (InterruptedException e) {
//			throw new RuntimeException(e);
//		}
	}

	private void modeLockRelease() {
//		modeLock.release();
	}
	
	public ThreadWorker getPipeWorker() {
		if(pipeWorker==null) {
			pipeWorker = new ThreadWorker(500) {
				byte[] buffer = new byte[1024];
				@Override
				public void onActivate() throws Exception {
					setName("FilteredInputStream.pipeWorker");
				}
				public void step() throws Exception {
					if(filteredMode) {
						//use available so that it will be possible to exit filtered mode when no data is being received
//						System.out.println("AVAILABLE?");
						if(highThoughputMode || getSuperIn().available()>0) {
//						System.out.println("READ SUPER");
//						System.out.println("RF " + IOHelper.byteToHex((byte)buffer[a]));
						int b = getSuperIn().read();
						if(b!=-1) {
							streamFilter.filter((byte)b, normalOutPipe, getThis());
							normalOutPipe.flush();//needed for correct pipe functioning
						}
							
						} else {
							//needed so that this thread won't gather all resources from CPU when waiting for data
							sleep(IDLE_SLEEP_TIME);
						}
					} else {
						deactivate();
					}
				}
			};
//			pipeWorker.setLogMessages(false);
		}
		return pipeWorker;
	}

	private FilteredInputStream<T> getThis() {
		return this;
	}

	public InputStream getSuperIn() throws IOException {
		return super.in;
	}
	
	public void startFilteredMode() throws IOException {
		modeLockAcquire();
		try {
			filteredMode = true;
	//		System.out.println("START FILTERED MODE");
			//transfer data from super.read(..) to inputInputStream
			try {
				getPipeWorker().activate();
			} catch (Exception e) {
				throw new IOException(e);
			}
		} finally {
			modeLockRelease();
//			System.out.println(">>START FILTER");
		}
	}
	public void stopFilteredMode() throws Exception {
		modeLockAcquire();
		try {
			filteredMode = false;
			getPipeWorker().deactivate();
			getPipeWorker().waitForDeactivation(1000);
		} finally {
			modeLockRelease();
//			System.out.println(">>STOP FILTER");
		}
	}
	
	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean isInFilteredMode() {
		return filteredMode;
	}
	
	@Override
	public void reset() throws IOException {
		synchronized(super.in) {
			super.in.reset();
			normalInPipe = new PipedInputStream2(filterBufferSize);
			normalOutPipe = new PipedOutputStream(normalInPipe);
		}
	}
	
	@Override
	public int available() throws IOException {
		if(filteredMode) {
			return normalInPipe.available();
		} else {
			return super.available();
		}
	}
	
	@Override
	public long skip(long n) throws IOException {
		if(filteredMode) {
			return normalInPipe.skip(n);
		} else {
			return super.in.skip(n);
		}
	}
	
	@Override
	public void close() throws IOException {
		try {
			stopFilteredMode();
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			super.close();
		}
	}
	
	@Override
	public void mark(int readlimit) {
		if(filteredMode) {
			synchronized(normalInPipe) {
				normalInPipe.mark(readlimit);
			}
		} else {
			synchronized(super.in) { 
				super.in.mark(readlimit);
			}
		}
	}
	
	@Override
	public boolean markSupported() {
		if(filteredMode) {
			return normalInPipe.markSupported();
		} else {
			return super.in.markSupported();
		}
	}

	/**
	 * If on high throughput mode, the stream cannot be switch from filtered to not filtered
	 * when the read operation is blocked waiting data.
	 */
	public void setHighThoughputMode(boolean highThoughputMode) {
		this.highThoughputMode = highThoughputMode;
	}
	public boolean isHighThoughputMode() {
		return highThoughputMode;
	}
	
	public void setStreamFilter(T streamFilter) {
		this.streamFilter = streamFilter;
	}
	
}
