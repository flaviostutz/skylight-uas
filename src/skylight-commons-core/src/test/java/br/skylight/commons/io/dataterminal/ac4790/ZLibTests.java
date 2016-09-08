package br.skylight.commons.io.dataterminal.ac4790;

/* -*-mode:java; c-basic-offset:2; -*- */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.concurrent.TimeoutException;

import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.io.ZChunkedInputStream;
import br.skylight.commons.io.ZChunkedOutputStream;
import br.skylight.commons.io.dataterminal.PipedInputStream2;

import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZInputStream;
import com.jcraft.jzlib.ZOutputStream;

// Test deflate() with full flush
class ZLibTests {

	public static void main(String[] args) throws IOException, ClassNotFoundException, TimeoutException, InterruptedException {
		//testNormalStream();
		testLossyStream();
	}

	static int totalXFer;
	private static void testLossyStream() throws IOException, TimeoutException, InterruptedException {
//		String s = "";
//		for(int i='a'; i<='z'; i++) s += (char)i;
//		String r = "";
//		for(int i=0; i<100; i++) r += s + " ";
//		String hello = "START "+ r +"END";
//		final byte[] hb = hello.getBytes();
		File f = new File("d:\\bedroom.mpg");
		FileInputStream fis = new FileInputStream(f);
		final byte[] hb = new byte[(int)f.length()];
		IOHelper.readFully(fis, hb, Integer.MAX_VALUE, false);
		
		final PipedInputStream2 pis = new PipedInputStream2(256);
		final PipedOutputStream pos = new PipedOutputStream(pis) {
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				totalXFer += len;
				//remove some bytes from stream
				if(totalXFer>=550 && totalXFer<570) {
//					System.out.println("REMOVING DATA FROM STREAM " + totalXFer);
//					return;
				}
				if(totalXFer>80000 && totalXFer<90000) {
//					return;
				}
				super.write(b, off, len);
//				System.out.println(total + "B");
			}
		};
		
		//break data integrity
//		ib[4]++;
//		ib[23]++;
		
		//READ COMPRESSED STREAM CONTENTS AND UNCOMPRESS IT
		Thread t = new Thread() {
			public void run() {
				int i = 0;
				long last=System.currentTimeMillis();
				File f = new File("d:\\out.data");
				if(f.exists()) f.delete();
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(f);
					ZChunkedInputStream zIn = new ZChunkedInputStream(pis);
					try {
						int t;
						long start = System.currentTimeMillis();
						while((t=zIn.read())!=-1) {
	//						System.out.print(new String(new byte[]{(byte)t})+"(" + i + ") ");
							fos.write(t);
							fos.flush();
//							System.out.print(hb[i]);
							if((byte)t!=hb[i++]) {
//								System.out.println("Found: " + t + " expected: " + hb[--i]);
								System.out.print("E");
	//							break;
							}
							if((System.currentTimeMillis()-last)>100) {
								if(System.currentTimeMillis()-start>0) {
									System.out.println("T: " + i + "; success: "+100F*(float)i/(float)hb.length + "%; rate: " + (float)i/((float)(System.currentTimeMillis()-start)/1000F) + "B/s");
									last = System.currentTimeMillis();
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						try {
							zIn.close();
							fos.flush();
							fos.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				System.out.println("\nSuccess: "+100F*(float)i/(float)hb.length + "%");
				System.out.println("Ratio: "+100F*(float)totalXFer/(float)hb.length + "% " + totalXFer + "/" + hb.length);
			};
		};
//		Thread.sleep(2000);
		t.setName("ZLibTest thread");
		t.start();

		
		//WRITE CONTENTS TO STREAM SO IT WILL BE COMPRESSED
		ZChunkedOutputStream zOut = new ZChunkedOutputStream(pos, JZlib.Z_BEST_COMPRESSION);
		zOut.setFlushMode(JZlib.Z_SYNC_FLUSH);
		
		//split file writes
		int s = 20;
		int l = hb.length/s;
		for(int i=0; i<s; i++) {
			zOut.write(hb, i*l, l+(i==(s-1)?1:0));
			zOut.flush();
//			Thread.sleep(10);
			zOut.startNewChunk();
		}
//		zOut.write(hb, 0, hb.length);
//		int l = hb.length/4;
//		zOut.write(hb, 0, l);
//		zOut.write(hb, l, l);
//		zOut.write(hb, l*2, l);
//		zOut.write(hb, l*3, l+1);
//		for(int i=0; i<hb.length; i++) {
//			zOut.write(hb[i]);
//		}
		Thread.sleep(12000);//wait for reader thread to read all
		pis.close();
		zOut.close();
	}

	private static void testNormalStream() throws IOException, ClassNotFoundException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ZOutputStream zOut = new ZOutputStream(out, JZlib.Z_BEST_COMPRESSION);
		String hello = "Hello World! asldkfjhalk dhfjalhdflajkh dfljkah fljka hsdfljka hsdfljkha sdfjlkhadsjlkfh ajlkdshf ajlksdhf ajlkshdf ajlkshdf ajlkhd fajlkshd fajklshd falkjshd falkjshd falkjsdh falkjdhf alkj hdfalk djhfalkj hdfalkhj dfalkjhdf alkjhfd alksdjhf alksdjhf alskdjhf alskdjhf asdflkjh asdflkjh adsflkjha sdflkjha dsflkjah sdflkjha sdflkjah sdflkjha sdflkjah sdflakjshd falksjdhf asdlfkjh adsflkjh asdflkjh asdflkjh asdflkjh asdflkjh asdflkjh asdflkjha sdflkjah sdflkjahs dflkjahsd f asldkfjhalk dhfjalhdflajkh dfljkah fljka hsdfljka hsdfljkha sdfjlkhadsjlkfh ajlkdshf ajlksdhf ajlkshdf ajlkshdf ajlkhd fajlkshd fajklshd falkjshd falkjshd falkjsdh falkjdhf alkj hdfalk djhfalkj hdfalkhj dfalkjhdf alkjhfd alksdjhf alksdjhf alskdjhf alskdjhf asdflkjh asdflkjh adsflkjha sdflkjha dsflkjah sdflkjha sdflkjah sdflkjha sdflkjah sdflakjshd falksjdhf asdlfkjh adsflkjh asdflkjh asdflkjh asdflkjh asdflkjh asdflkjh asdflkjha sdflkjah sdflkjahs dflkjahsd fasldkfjhalk dhfjalhdflajkh dfljkah fljka hsdfljka hsdfljkha sdfjlkhadsjlkfh ajlkdshf ajlksdhf ajlkshdf ajlkshdf ajlkhd fajlkshd fajklshd falkjshd falkjshd falkjsdh falkjdhf alkj hdfalk djhfalkj hdfalkhj dfalkjhdf alkjhfd alksdjhf alksdjhf alskdjhf alskdjhf asdflkjh asdflkjh adsflkjha sdflkjha dsflkjah sdflkjha sdflkjah sdflkjha sdflkjah sdflakjshd falksjdhf asdlfkjh adsflkjh asdflkjh asdflkjh asdflkjh asdflkjh asdflkjh asdflkjha sdflkjah sdflkjahs dflkjahsd fasldkfjhalk dhfjalhdflajkh dfljkah fljka hsdfljka hsdfljkha sdfjlkhadsjlkfh ajlkdshf ajlksdhf ajlkshdf ajlkshdf ajlkhd fajlkshd fajklshd falkjshd falkjshd falkjsdh falkjdhf alkj hdfalk djhfalkj hdfalkhj dfalkjhdf alkjhfd alksdjhf alksdjhf alskdjhf alskdjhf asdflkjh asdflkjh adsflkjha sdflkjha dsflkjah sdflkjha sdflkjah sdflkjha sdflkjah sdflakjshd falksjdhf asdlfkjh adsflkjh asdflkjh asdflkjh asdflkjh asdflkjh asdflkjh asdflkjha sdflkjah sdflkjahs dflkjahsd fasldkfjhalk dhfjalhdflajkh dfljkah fljka hsdfljka hsdfljkha sdfjlkhadsjlkfh ajlkdshf ajlksdhf ajlkshdf ajlkshdf ajlkhd fajlkshd fajklshd falkjshd falkjshd falkjsdh falkjdhf alkj hdfalk djhfalkj hdfalkhj dfalkjhdf alkjhfd alksdjhf alksdjhf alskdjhf alskdjhf asdflkjh asdflkjh adsflkjha sdflkjha dsflkjah sdflkjha sdflkjah sdflkjha sdflkjah sdflakjshd falksjdhf asdlfkjh adsflkjh asdflkjh asdflkjh asdflkjh asdflkjh asdflkjh asdflkjha sdflkjah sdflkjahs dflkjahsd fasldkfjhalk dhfjalhdflajkh dfljkah fljka hsdfljka hsdfljkha sdfjlkhadsjlkfh ajlkdshf ajlksdhf ajlkshdf ajlkshdf ajlkhd fajlkshd fajklshd falkjshd falkjshd falkjsdh falkjdhf alkj hdfalk djhfalkj hdfalkhj dfalkjhdf alkjhfd alksdjhf alksdjhf alskdjhf alskdjhf asdflkjh asdflkjh adsflkjha sdflkjha dsflkjah sdflkjha sdflkjah sdflkjha sdflkjah sdflakjshd falksjdhf asdlfkjh adsflkjh asdflkjh asdflkjh asdflkjh asdflkjh asdflkjh asdflkjha sdflkjah sdflkjahs dflkjahsd fasldkfjhalk dhfjalhdflajkh dfljkah fljka hsdfljka hsdfljkha sdfjlkhadsjlkfh ajlkdshf ajlksdhf ajlkshdf ajlkshdf ajlkhd fajlkshd fajklshd falkjshd falkjshd falkjsdh falkjdhf alkj hdfalk djhfalkj hdfalkhj dfalkjhdf alkjhfd alksdjhf alksdjhf alskdjhf alskdjhf asdflkjh asdflkjh adsflkjha sdflkjha dsflkjah sdflkjha sdflkjah sdflkjha sdflkjah sdflakjshd falksjdhf asdlfkjh adsflkjh asdflkjh asdflkjh asdflkjh asdflkjh asdflkjh asdflkjha sdflkjah sdflkjahs dflkjahsd f";
		zOut.write(hello.getBytes());
		zOut.close();
		byte[] ib = out.toByteArray();

		ByteArrayInputStream in = new ByteArrayInputStream(ib);
		ZInputStream zIn = new ZInputStream(in);
		byte[] rb = new byte[ib.length];
		zIn.read(rb);
		String rs = new String(rb);
		System.out.println(rs);
		
		System.out.println("Ratio: "+100F*(float)ib.length/(float)hello.getBytes().length + "%");
	}

}
