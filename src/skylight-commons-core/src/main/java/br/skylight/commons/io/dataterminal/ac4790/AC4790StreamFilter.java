package br.skylight.commons.io.dataterminal.ac4790;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import br.skylight.commons.io.FilteredInputStream;
import br.skylight.commons.io.SequenceStreamFilter;
import br.skylight.commons.io.StreamFilter;

public class AC4790StreamFilter extends SequenceStreamFilter {

	private static Logger logger = Logger.getLogger(AC4790StreamFilter.class.getName());
	
	public static final byte API_RECEIVE_PACKET = (byte)0x81;
	public static final byte API_SEND_DATA_COMPLETE = (byte)0x82;
	
	private boolean enableSequenceFiltering = true;

	private InputStream sis;
	private AC4790StreamFilterListener listener;
	private boolean payloadFound;
	
	//memory optimization (could be put inside method)
	private int payloadLen;
	private byte[] b = new byte[256];
	private int readLeft;
	private int r;
	
	public AC4790StreamFilter(InputStream superIn, AC4790StreamFilterListener listener) throws IOException {
		super(null, false);
		this.listener = listener;
		this.sis = superIn;

		//used for optimization (do nothing)
		if(this.listener==null) {
			this.listener = new AC4790StreamFilterListener() {
				public void notifyTransmittedRFPacketResult(int rssiLocalHeardRemote, int rssiRemoteHeardLocal, boolean successfulDelivery) {}
				public void notifyReceivedRFPacketResult(int rssiLocalHeardRemote, int rssiRemoteHeardLocal, int payloadLen) {}
			};
		}
	}

	@Override
	public void doFiltering(byte byteIn, OutputStream os, FilteredInputStream<? extends StreamFilter> is) throws IOException {
		//behave just like a common SequenceFilter
		if(enableSequenceFiltering) {
			super.doFiltering(byteIn, os, is);

		//each call to doFiltering(..) will process a whole message from modem
		} else {
			payloadFound = false;
			while(!payloadFound) {
//				System.out.println("TRY");
				//process received packet and forward payload data to output stream
				if(byteIn==API_RECEIVE_PACKET) {
//					System.out.println("RECEIVE PACKET");
					payloadLen = sis.read();
//					System.out.println("pl "+payloadLen);
					if(payloadLen>=0 && payloadLen<=129) {
						readLeft = payloadLen+5;
						//read entire modem api message
						while(readLeft>0) {
							r = sis.read(b, payloadLen+5-readLeft, readLeft);
							if(r!=-1) {
								readLeft -= r;
							} else {
//								System.out.println("EITA -1");
							}
						}
						//process modem api data
						listener.notifyReceivedRFPacketResult(
							b[0],//rssi remote heard local
							b[1],//rssi local heard remote
							payloadLen
						);
	//					b[2];//mac 2
	//					b[3];//mac 1
	//					b[4];//mac 0
						//process payload data
						os.write(b, 5, payloadLen);
//						ByteArrayOutputStream ba = new ByteArrayOutputStream();
//						ba.write(b, 5, payloadLen);
//						System.out.println("data=" + IOHelper.bytesToHexString(ba.toByteArray()));
//						os.flush();
						payloadFound = true;
//						System.out.println("FOUND");
					} else {
						logger.info("Invalid payload length found. Skipping. len=" + payloadLen);
//						System.out.println("Invalid payload length found. Skipping. len=" + payloadLen);
					}
					
				//process packet confirmation
				} else if(byteIn==API_SEND_DATA_COMPLETE) {
//					System.out.println("SEND DATA COMPLETE");
					listener.notifyTransmittedRFPacketResult(
							sis.read(),//rssi remote heard local 
							sis.read(),//rssi local heard remote
							sis.read()==1);//successful delivery
					payloadFound = true;
				}
				
				if(!payloadFound) {
					//invalid message type found. maybe there was a problem in stream
					//consume stream until finding a message type indication
					logger.info("Corrupted stream found. Unrecognized message type. Skipping stream.");
//					System.out.println("Corrupted stream. Skipping to next api packet.");
					byte b = (byte)sis.read();
//					System.out.print(IOHelper.byteToHex(b) + " ");
					while(b!=API_SEND_DATA_COMPLETE && b!=API_RECEIVE_PACKET) {
						b = (byte)sis.read();//drain stream
//						System.out.print(IOHelper.byteToHex(b) + " ");
					}
					//retry reading with next known message type byte
					byteIn = b;
//					System.out.println("TRY AGAIN");
				}
			}
		}
	}
	
	/**
	 * Enable this filter to behave just like an ordinal SequenceStreamFilter
	 * @throws IOException 
	 */
	public void setEnableSequenceFiltering(boolean enableSequenceFiltering) throws IOException {
		this.enableSequenceFiltering = enableSequenceFiltering;
		if(!enableSequenceFiltering) {
			useNormalMode();
		}
	}
	
}
