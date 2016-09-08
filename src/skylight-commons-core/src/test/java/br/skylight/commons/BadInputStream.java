package br.skylight.commons;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BadInputStream extends FilterInputStream {

	private float corruptRate;
	private float lossRate;
	private int maxSequenceBytesLoss;
	private float shortStopRate;
	private long maxShortStopTimeMillis;
	private float longStopRate;
	private long maxLongStopTimeMillis;
	
	private byte[] buf1 = new byte[1];
	
	public BadInputStream(InputStream in) {
		super(in);
	}
	
	@Override
	public int read() throws IOException {
		if (read(buf1, 0, 1) == -1)
			return (-1);
		return (buf1[0] & 0xFF);
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		generateRandomProblems();
		int r = super.read(b, off, len);
		int n = 0;
		for(int i=0; i<r; i++) {
			if(Math.random()<=corruptRate) {
				b[i]+=127;
				n++;
			}
		}
		if(n>0) {
			System.out.println("BAD IS: Corrupted "+ n +" bytes");
		}
		return r;
	}

	private void generateRandomProblems() throws IOException {
		if(Math.random()<=lossRate) {
			int s = (int)Math.floor(1+Math.random()*maxSequenceBytesLoss);
			System.out.println("BAD IS: Losing " + s + " bytes");
			in.skip(s);
		}
		if(Math.random()<=shortStopRate) {
			try {
				int s = (int)Math.floor(1+Math.random()*maxShortStopTimeMillis);
				System.out.println("BAD IS: Short pausing " + s + " millis");
				Thread.sleep(s);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else if(Math.random()<=longStopRate) {
			try {
				int s = (int)Math.floor(maxShortStopTimeMillis+Math.random()*(maxLongStopTimeMillis-maxShortStopTimeMillis));
				System.out.println("BAD IS: Long pausing " + s + " millis");
				Thread.sleep(s);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setCorruptRate(float corruptRate) {
		this.corruptRate = corruptRate;
	}
	public float getCorruptRate() {
		return corruptRate;
	}
	public void setLossRate(float lossRate) {
		this.lossRate = lossRate;
	}
	public float getLossRate() {
		return lossRate;
	}
	public void setMaxSequenceBytesLoss(int maxSequenceBytesLoss) {
		this.maxSequenceBytesLoss = maxSequenceBytesLoss;
	}
	public int getMaxSequenceBytesLoss() {
		return maxSequenceBytesLoss;
	}
	public void setLongStopRate(float longStopRate) {
		this.longStopRate = longStopRate;
	}
	public float getLongStopRate() {
		return longStopRate;
	}
	public void setMaxLongStopTimeMillis(long maxLongStopTimeMillis) {
		this.maxLongStopTimeMillis = maxLongStopTimeMillis;
	}
	public long getMaxLongStopTimeMillis() {
		return maxLongStopTimeMillis;
	}
	public void setMaxShortStopTimeMillis(long maxShortStopTimeMillis) {
		this.maxShortStopTimeMillis = maxShortStopTimeMillis;
	}
	public long getMaxShortStopTimeMillis() {
		return maxShortStopTimeMillis;
	}
	public void setShortStopRate(float shortStopRate) {
		this.shortStopRate = shortStopRate;
	}
	public float getShortStopRate() {
		return shortStopRate;
	}

}
