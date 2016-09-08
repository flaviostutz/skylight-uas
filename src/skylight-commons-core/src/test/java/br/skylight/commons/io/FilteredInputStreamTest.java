package br.skylight.commons.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import br.skylight.commons.infra.IOHelper;

import com.gc.iotools.stream.is.StatsInputStream;

public class FilteredInputStreamTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
//		testStreamMode();
		testNormalMode();
//		testNormalModeSimpleSequence();
	}
	
	public static void testNormalMode() throws Exception {
		final ByteArrayInputStream bis = new ByteArrayInputStream("abcd#0123456789$".getBytes());
		final ByteArrayInputStream tis = new ByteArrayInputStream("abcd".getBytes());

		SequenceStreamFilter ssf = new SequenceStreamFilter("#".getBytes(), "$".getBytes(), 10, true);
		final FilteredInputStream<SequenceStreamFilter> fis = new FilteredInputStream<SequenceStreamFilter>(bis, ssf);
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					StatsInputStream is = new StatsInputStream(fis);
					while(true) {
						int i = is.read();
						int t = tis.read();
						System.out.print(((char)i) + "==" + ((char)t) + " ");
						IOHelper.assertEquals(i, t);
//						Thread.sleep(100);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

		ssf.setSequenceListener(new SequenceListener() {
			public void onSequenceFound(byte[] fullSequence, byte[] body, OutputStream os) {
				if(!new String(body).equals("0123456789")) {
					throw new AssertionError("Expected sequence don't match");
				} else {
					System.out.println("TEST OK");
				}
			}
		});
		fis.startFilteredMode();
		System.out.println("Filtering removing some bytes...");
		t.start();
		
		Thread.sleep(70000);
//		ssf.waitForByteSequence(10000);

		System.out.println("Stopped filtering...");
		fis.stopFilteredMode();
	}

	public static void testNormalModeSimpleSequence() throws Exception {
		final ByteArrayInputStream bis = new ByteArrayInputStream("abcd01234efgh".getBytes());
		final ByteArrayInputStream tis = new ByteArrayInputStream("abcdefgh".getBytes());

		SequenceStreamFilter ssf = new SequenceStreamFilter("01234".getBytes(), true);
		final FilteredInputStream<SequenceStreamFilter> fis = new FilteredInputStream<SequenceStreamFilter>(bis, ssf);
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					StatsInputStream is = new StatsInputStream(fis);
					while(true) {
						int i = is.read();
						int t = tis.read();
						System.out.print(((char)i) + "" + ((char)t) + " ");
						IOHelper.assertEquals(i, t);
//						Thread.sleep(100);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

		ssf.setSequenceListener(new SequenceListener() {
			public void onSequenceFound(byte[] fullSequence, byte[] body, OutputStream os) {
				System.out.println("Sequence found " + new String(fullSequence));
			}
		});
		fis.startFilteredMode();
		System.out.println("Filtering removing some bytes...");
		t.start();
		
		Thread.sleep(70000);
//		ssf.waitForByteSequence(10000);

		System.out.println("Stopped filtering...");
		fis.stopFilteredMode();
	}
	
	
	public static void testStreamMode() throws IOException {
		final ByteArrayInputStream bis = new ByteArrayInputStream("abcdefghi#0123456789$".getBytes());
		final ByteArrayInputStream tis = new ByteArrayInputStream("abcdfghi#0123456789$".getBytes());

		final StreamFilter ssf = new StreamFilter();
		final FilteredInputStream<StreamFilter> fis = new FilteredInputStream<StreamFilter>(bis, ssf);
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					StatsInputStream is = new StatsInputStream(fis);
					while(true) {
						int i = is.read();
						int t = tis.read();
						System.out.print(((char)i) + "" + ((char)t) + " ");
						IOHelper.assertEquals(i, t);
//						Thread.sleep(100);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		ssf.useStreamMode();
		fis.startFilteredMode();
		t.start();

		Thread tr = new Thread(new Runnable() {
			public void run() {
				System.out.println("Filtering in stream mode...");
				try {
					while(true) {
						int i = ssf.getFilterIn().read();
						if((char)i!='e') {
							System.out.println("write " + (char)i);
							ssf.getFilterOut().write(i);
							ssf.getFilterOut().flush();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		tr.start();
	}

}
