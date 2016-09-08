package br.skylight.commons.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class StreamFilter {

	private PipedInputStream pis;
	private PipedOutputStream pos;

	private PipedInputStream tis;
	private PipedOutputStream tos;

	private boolean normalMode = true;
	
//	private ThreadWorker pipeWorker;
	
	public final void filter(byte byteIn, OutputStream os, FilteredInputStream<? extends StreamFilter> is) throws IOException {
		//stream mode
		if(!normalMode) {
//			System.out.println("FILTER STREAM MODE " + IOHelper.byteToHex(byteIn));
			
			//send data to filter input
			pos.write(byteIn);
			pos.flush();
			
			//get data from filter output
			if(tis.available()>0) {
				byte[] b = new byte[128];
				int i = tis.read(b);
				if(i!=-1) {
					os.write(b, 0, i);
				}
			}

		//normal mode
		} else {
//			System.out.println("FILTER NORMAL MODE "+IOHelper.byteToHex(byteIn));
			doFiltering(byteIn, os, is);
		}
	}
	
	public void doFiltering(byte byteIn, OutputStream os, FilteredInputStream<? extends StreamFilter> is) throws IOException {
		os.write(byteIn);
	}
	
	public void useStreamMode() throws IOException {
//		System.out.println("USE STREAM MODE");
		if(normalMode) {
			pis = new PipedInputStream(1024);
			pos = new PipedOutputStream(pis);
	
			tis = new PipedInputStream(1024);
			tos = new PipedOutputStream(tis);
			
			normalMode = false;
		}
//		try {
//			getPipeWorker().activate();
//		} catch (Exception e) {
//			throw new IOException(e);
//		}
	}
	
	public void useNormalMode() throws IOException {
//		System.out.println("USE NORMAL MODE");
//		try {
//			getPipeWorker().deactivate();
//		} catch (Exception e) {
//			throw new IOException(e);
//		}
		if(!normalMode) {
			pis.close();
			pos.close();
			pis = null;
			pos = null;
	
			tis.close();
			tos.close();
			tis = null;
			tos = null;
			normalMode = true;
		}
	}
	
	public InputStream getFilterIn() {
		if(pis==null) {
			throw new IllegalStateException("Call useStreamMode() before calling getFilterIn()");
		}
		return pis;
	}

	public OutputStream getFilterOut() {
		if(tos==null) {
			throw new IllegalStateException("Call useStreamMode() before calling getFilterOut()");
		}
		return tos;
	}
	
	public boolean isNormalMode() {
		return normalMode;
	}

//	private ThreadWorker getPipeWorker() {
//		if(pipeWorker!=null) {
//			pipeWorker = new ThreadWorker() {
//				protected void step() throws Exception {
//					if(pis!=null && tos!=null) {
//						tos.write(pis.read());
//					}
//				}
//			};
//		}
//		return pipeWorker;
//	}
//	
}
