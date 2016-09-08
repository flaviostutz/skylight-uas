package br.skylight.commons.io.dataterminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.dli.services.ByteArrayOutputStream2;
import br.skylight.commons.infra.ByteSequenceComparator;
import br.skylight.commons.io.ZChunkedInputStream;
import br.skylight.commons.io.ZChunkedOutputStream;

import com.jcraft.jzlib.JZlib;

public abstract class StreamDataTerminal extends DataTerminal {

	//although this is an int array, each element represents a single byte in stream
	private static final byte[] END_MARK = new byte[] {0x23, -0x50, 0x76};
	private static final Logger logger = Logger.getLogger(StreamDataTerminal.class.getName());
	private ByteArrayOutputStream2 tempPacket = new ByteArrayOutputStream2(new byte[1024]);
	private ByteSequenceComparator sequenceComparator = new ByteSequenceComparator();
	private InputStream is;
	private OutputStream os;
	private int nb;
	private boolean compressOutputData;
	private boolean uncompressInputData;
	
	public StreamDataTerminal() {
		this(null,0,false,false);
	}

	public StreamDataTerminal(DataTerminalType dataTerminalType, int dataLinkId, boolean compressOutputData, boolean uncompressInputData) {
		super(dataTerminalType, dataLinkId);
		this.compressOutputData = compressOutputData;
		this.uncompressInputData = uncompressInputData;
	}

	@Override
	public void onActivate() throws Exception {
		//prepare inputstream
		if(uncompressInputData) {
			try {
				is = new ZChunkedInputStream(getInputStream());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			is = getInputStream();
		}
		
		//prepare outputstream
		if(compressOutputData) {
			os = new ZChunkedOutputStream(getOutputStream(), JZlib.Z_BEST_COMPRESSION);
		} else {
			os = getOutputStream();
//			os = new FilterOutputStream(getOutputStream()) {
//				@Override
//				public void write(int b) throws IOException {
//					System.out.print(IOHelper.byteToHex((byte)b) + " ");
//					super.write(b);
//				}
//			};
		}
		
		super.onActivate();
	}
	
	protected int readNextPacket(byte[] buffer) throws IOException {
		synchronized(tempPacket) {
			tempPacket.reset();
			while(true) {
				
				//avoid being blocked and/or getting "dead end" exceptions on piped streams
				//TODO verify if this can be removed after correcting deadlocks
	//			if(is.available()>0) {
	//				System.out.println(name + " about to read... ");
					nb = is.read();
	//				System.out.println(name + " read " + nb);
	//				try {
	//					Thread.sleep(1);
	//				} catch (InterruptedException e) {
	//					e.printStackTrace();
	//				}
	//				System.out.println(getName() + ": READ " + nb);
	//			} else {
	//				try {
	//					Thread.sleep(10);
	//					continue;
	//				} catch (InterruptedException e) {
	//					e.printStackTrace();
	//				}
	//			}

				//FOR TESTING
//				if(tempPacket.size()>200) {
//					System.out.println("PACKET SIZE: "+tempPacket.size());
//					for (int i=0; i<tempPacket.size(); i++) {
//						System.out.print(IOHelper.byteToHex(tempPacket.getBuffer()[i]) + " ");
//					}
//					System.out.print(IOHelper.byteToHex((byte)nb) + " ");
//					System.out.println("");
//				}
				
				if(tempPacket.size()<1024) {
					
					//WRITE BYTE TO TEMP PACKET
					tempPacket.write(nb);
					
					//LOOK FOR END MARK
					sequenceComparator.addByte(nb);
					if(sequenceComparator.isLastBytesMatch(END_MARK)) {
						System.arraycopy(tempPacket.getBuffer(), 0, buffer, 0, tempPacket.size());
						return tempPacket.size()-END_MARK.length+1;
					}
					
				} else {
					tempPacket.reset();
					logger.info("Corrupted packet found (too large). Ignoring it.");
				}
				
			}
		}
	}

	@Override
	protected void sendNextPacket(byte[] data, int len) throws IOException {
//		System.out.println(name + " sending " + (len+END_MARK.length));
		synchronized(os) {
			os.write(data, 0, len);
			for(int i=0; i<END_MARK.length; i++) {
				os.write(END_MARK[i]);
			}
			os.flush();
		}
	}
	
	protected abstract InputStream getInputStream();
	protected abstract OutputStream getOutputStream();
	
}
