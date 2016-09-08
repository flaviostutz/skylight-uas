package br.skylight.commons.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;

import br.skylight.commons.infra.ByteSequenceComparator;

/**
 * Filter for sequence recognition.
 * If endSequence is null, a sequence is recognized only by the startSequence.
 * If endSequence is not null, the identified sequence is composed of [startSequence] + body + [endSequence].
 * @author Flávio Stutz
 */
public class SequenceStreamFilter extends StreamFilter {

	private byte[] startSequence = null;
	private int maxBodySize = Integer.MAX_VALUE;
	private byte[] endSequence = null;

	private boolean searchingStartMark = true;
	private ByteArrayOutputStream sequenceBuffer = new ByteArrayOutputStream();
	private ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream();
	
	private boolean removeSequenceFromStream = true;
	private SequenceListener listener = null;
	
	private Object sequenceWaiter = new Object();
	
//	private int bytesToFilterOut = 0;
	private ByteSequenceComparator sequenceComparator = new ByteSequenceComparator();

	public SequenceStreamFilter(byte[] sequence, boolean removeSequenceFromStream) {
		this(sequence, null, 0, removeSequenceFromStream);
	}
	
	public SequenceStreamFilter(byte[] sequence, int maxBodySize, boolean removeSequenceFromStream) {
		this(sequence, null, maxBodySize, removeSequenceFromStream);
	}
	
	public SequenceStreamFilter(byte[] startSequence, byte[] endSequence, int maxBodySize, boolean removeSequenceFromStream) {
		this.startSequence = startSequence;
		this.endSequence = endSequence;
		this.removeSequenceFromStream = removeSequenceFromStream;
		this.searchingStartMark = true;
		this.maxBodySize = maxBodySize;
	}

	public void setSequenceListener(SequenceListener listener) {
		this.listener = listener;
	}

	public void doFiltering(byte byteIn, OutputStream os, FilteredInputStream<? extends StreamFilter> is) throws IOException {
		if(startSequence==null) {
			os.write(byteIn);
			return;
		}
//		System.out.print("FI "+IOHelper.byteToHex(byteIn));
		//keep bytes in main buffer
		sequenceBuffer.write(byteIn);
		sequenceComparator.addByte(byteIn);

		//searching for start mark
		if(searchingStartMark) {
			
			//start sequence match
			if(sequenceComparator.isLastBytesPartial(startSequence)) {
				//match
				if(sequenceComparator.isLastBytesMatch(startSequence)) {
					//no need to look for end sequence
					if(endSequence==null && maxBodySize==0) {
						notifySequenceFound(os, sequenceBuffer.toByteArray());
						sequenceBuffer.reset();
						
					//look for end sequence
					} else {
						searchingStartMark = false;
						bodyBuffer.reset();
					}
				}
				
			//not even a partial match
			} else {
				//flush trial buffer contents to output because not event a partial start sequence was found
				os.write(sequenceBuffer.toByteArray());
				sequenceBuffer.reset();
			}

		//searching for end mark or maxBodySize
		} else {

			//end sequence match
			if(sequenceComparator.isLastBytesPartial(endSequence)) {
				//match
				if(sequenceComparator.isLastBytesMatch(endSequence)) {
					bodyBuffer.write(endSequence);
					notifySequenceFound(os, bodyBuffer.toByteArray());
					bodyBuffer.reset();
					searchingStartMark = true;
				}
				
			//not even a partial match
			} else {
				//flush trial buffer contents to output because not event a partial start sequence was found
				bodyBuffer.write(sequenceBuffer.toByteArray());
				sequenceBuffer.reset();
			}
			
			//max body size reached
			if(bodyBuffer.size()>=(maxBodySize+startSequence.length+endSequence.length)) {
				if(!removeSequenceFromStream) {
					//flush trial buffer contents to output because the full end sequence was not found
					os.write(bodyBuffer.toByteArray());
				}
				
				//if not looking for end mark, consider current body as a match
				if(endSequence==null) {
					notifySequenceFound(os, bodyBuffer.toByteArray());
				}
				
				//max body size reached, reinit pattern search
				bodyBuffer.reset();//reset trial buffer
				sequenceBuffer.reset();
				searchingStartMark = true;
			}
		}
	}

	private void notifySequenceFound(OutputStream os, byte[] bytes) throws IOException {
		if(!removeSequenceFromStream) {
			os.write(bytes);
		}
		if(listener!=null) {
			byte[] body = new byte[bytes.length-startSequence.length];
			if(endSequence!=null) {
				body = new byte[bytes.length-startSequence.length-endSequence.length];
			}
			System.arraycopy(bytes, startSequence.length, body, 0, body.length);
			listener.onSequenceFound(bytes, body, os);
		}
		synchronized (sequenceWaiter) {
//			System.out.println("FOUND SEQUENCE");
			sequenceWaiter.notifyAll();
		}
	}

	public void setStartSequence(byte[] startSequence) {
		this.startSequence = startSequence;
	}
	
	public void setEndSequence(byte[] endSequence) {
		this.endSequence = endSequence;
	}
	
	public void setMaxBodySize(int maxBodySize) {
		this.maxBodySize = maxBodySize;
	}
	public int getMaxBodySize() {
		return maxBodySize;
	}

	public void waitForSequence(long timeout) throws TimeoutException {
		checkMode();
		try {
			synchronized (sequenceWaiter) {
				long st = System.currentTimeMillis();
				sequenceWaiter.wait(timeout);
				if((System.currentTimeMillis()-st)>=timeout) {
					throw new TimeoutException();
				}
			}
		} catch (InterruptedException e1) {
			throw new RuntimeException(e1);
		}
	}

	private void checkMode() {
		if(!isNormalMode()) {
			throw new IllegalStateException("Filter must be in normal mode");
		}
	}

	/**
	 * Set number of bytes to be filtered out for
	 * sequences that don't match expected sequence
	 * @param bytesToFilterOut
	 */
//	public void setBytesToFilterOut(int bytesToFilterOut) {
//		this.bytesToFilterOut = bytesToFilterOut;
//	}
	
}
