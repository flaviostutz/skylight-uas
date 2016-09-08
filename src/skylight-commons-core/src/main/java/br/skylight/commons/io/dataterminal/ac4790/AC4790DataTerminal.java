package br.skylight.commons.io.dataterminal.ac4790;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import br.skylight.commons.dli.datalink.DataLinkControlCommand;
import br.skylight.commons.dli.datalink.DataLinkSetupMessage;
import br.skylight.commons.dli.datalink.DataLinkStatusReport;
import br.skylight.commons.dli.enums.AntennaState;
import br.skylight.commons.dli.enums.CommunicationSecurityMode;
import br.skylight.commons.dli.enums.CommunicationSecurityState;
import br.skylight.commons.dli.enums.DataLinkState;
import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.dli.enums.LinkChannelPriorityState;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.io.SerialConnectionParams;
import br.skylight.commons.io.dataterminal.StreamDataTerminal;
import br.skylight.commons.io.dataterminal.ac4790.AC4790OutputStream.TransmitterPower;

public class AC4790DataTerminal extends StreamDataTerminal implements AC4790StreamFilterListener {

	private static final Logger logger = Logger.getLogger(AC4790OutputStream.class.getName());
	
	private SerialConnectionParams serialConnectionParams;
	protected AC4790SerialConnection connection;
	
	private int rssiLocalHeardRemote;
	private int rssiRemoteHeardLocal;
	private int txErrors;
	private int packetsResultCounter = 0;//number of packet result messages returned by modem
	
	//this is a subjective modeling due to dificulties in upper range of RSSI to dBm in ac4790 manual
//	private static Map<Integer,Integer> rssiToPercentTable = new HashMap<Integer,Integer>();
//	static {
//		rssiToPercentTable.put(11, 100);
//		rssiToPercentTable.put(12, 81);
//		rssiToPercentTable.put(13, 73);
//		rssiToPercentTable.put(14, 68);
//		rssiToPercentTable.put(17, 63);
//		rssiToPercentTable.put(23, 57);
//		rssiToPercentTable.put(28, 52);
//		rssiToPercentTable.put(43, 48);
//		rssiToPercentTable.put(64, 41);
//		rssiToPercentTable.put(85, 37);
//		rssiToPercentTable.put(98, 32);
//		rssiToPercentTable.put(113, 25);
//		rssiToPercentTable.put(120, 21);
//		rssiToPercentTable.put(132, 16);
//		rssiToPercentTable.put(154, 10);
//		rssiToPercentTable.put(173, 5);
//		rssiToPercentTable.put(189, 0);
//	}
	
	public AC4790DataTerminal(SerialConnectionParams serialConnectionParams, DataTerminalType dataTerminalType, int dataLinkId, boolean compressOutputData, boolean uncompressInputData) {
		super(dataTerminalType, dataLinkId, compressOutputData, uncompressInputData);
		this.serialConnectionParams = serialConnectionParams;
	}

	@Override
	public void onActivate() throws Exception {
		connection = new AC4790SerialConnection(serialConnectionParams.resolveConnection(), this);
		
		//populate current report state with modem state
		boolean wasChanged = false;
		try {
			enterModemCommandMode();
			populateReportWithModemState();
//			connection.getOutputStream().storeDeviceSensibilityLongRange(true);
			wasChanged = setupModemForAPIControl();
		} finally {
			if(!wasChanged) {
				exitModemCommandMode();
			} else {
				connection.getOutputStream().performSoftReset();
			}
		}
		logger.info("Modem verified successfully");
		
		super.onActivate();
	}
	
	private boolean setupModemForAPIControl() throws IOException {
		//configure modem not to use probe reporting and to use receive /confirm sent packets api
		return connection.getOutputStream().setupAPIControl(true, false, true, true, false);
	}

	private void populateReportWithModemState() throws Exception {
		AC4790OutputStream os = connection.getOutputStream();
		DataLinkStatusReport sr = getDataLinkStatusReport();

		//SECURITY STATE
		if(os.isDeviceUsingEncryption()) {
			if(os.getDeviceDESKey()==0) {
				sr.setCommunicationSecurityState(CommunicationSecurityState.ZEROIZED);
			} else {
				sr.setCommunicationSecurityState(CommunicationSecurityState.KEYED);
			}
		} else {
			sr.setCommunicationSecurityState(CommunicationSecurityState.BYPASS);
		}
		
		//DATA LINK STATE
		if(txEnabled) {
			if(os.getDeviceTransmitterPower().equals(TransmitterPower.MAX_POWER)) {
				sr.setDataLinkState(DataLinkState.TX_HIGH_POWER_AND_RX);
			} else if(os.getDeviceTransmitterPower().equals(TransmitterPower.QUARTER_POWER)) {
				sr.setDataLinkState(DataLinkState.TX_AND_RX);
			} else if(os.getDeviceTransmitterPower().equals(TransmitterPower.TURN_OFF)) {
				if(rxEnabled) {
					sr.setDataLinkState(DataLinkState.RX_ONLY);
				} else {
					sr.setDataLinkState(DataLinkState.OFF);
				}
				logger.warning("Data Terminal is with txEnabled=true but modem device is indicating to be with transmitter turn off");
			}
		} else {
			if(rxEnabled) {
				sr.setDataLinkState(DataLinkState.RX_ONLY);
			} else {
				sr.setDataLinkState(DataLinkState.OFF);
			}
		}
		
		//DATA LINK CHANNEL/CARRIER - carrier data is ignored for AC4790, we just use channel number
		sr.setReportedChannel(os.getDeviceChannelNumber());
		if(sr.getReportedChannel()>=0x30) {
			//USA/Canada/Australia range
			sr.setReportedForwardLinkCarrierFreq(915);
			sr.setReportedReturnLinkCarrierFreq(915);
			sr.setReportedPrimaryHopPattern(22);
		} else if(sr.getReportedChannel()>=0x10) {
			//USA/Canada range
			sr.setReportedForwardLinkCarrierFreq(902);
			sr.setReportedReturnLinkCarrierFreq(902);
			sr.setReportedPrimaryHopPattern(50);
		} else {
			//USA/Canada range
			sr.setReportedForwardLinkCarrierFreq(902);
			sr.setReportedReturnLinkCarrierFreq(902);
			sr.setReportedPrimaryHopPattern(26);
		}

		//link channel priority is used for setting transmit retries
		sr.setLinkChannelPriorityState(LinkChannelPriorityState.values()[Math.max(0,os.getDeviceTransmitRetries()-1)]);
		
		//antenna state is used to set half/full duplex mode
		if(os.isDeviceTransmissionModeFullDuplex()) {
			sr.setAntennaState(AntennaState.OMNI);
		} else {
			sr.setAntennaState(AntennaState.DIRECTIONAL);
		}
		
	}

	@Override
	public void onDeactivate() throws Exception {
		connection.close();
		super.onDeactivate();
	}
	
	@Override
	protected InputStream getInputStream() {
		return connection.getInputStream();
	}

	@Override
	protected OutputStream getOutputStream() {
		return connection.getOutputStream();
	}

	@Override
	public void setupDataLink(DataLinkSetupMessage sm) {
		try {
			enterModemCommandMode();
			
			AC4790OutputStream os = connection.getOutputStream();
			os.storeAndSwitchDeviceChannelNumber((byte)sm.getSelectChannel());
			os.storeDeviceDESKey((byte)sm.getSetPnCode());

			//user must perform a control command (that will cause a soft reset) in order to the new DES key to take place
			os.performSoftReset();
			Thread.sleep(500);

			//update report with current settings
			populateReportWithModemState();
			
		} catch (Exception e) {
			logger.throwing(null, null, e);
			logger.warning("Couldn't apply setup configurations to modem. e=" + e.toString());
			e.printStackTrace();
		} finally {
			exitModemCommandMode();
		}
	}

	@Override
	public void controlDataLink(DataLinkControlCommand cm) {
		try {
			enterModemCommandMode();
			
			//security mode
			AC4790OutputStream os = connection.getOutputStream();
			if(cm.getCommunicationSecurityMode().equals(CommunicationSecurityMode.NORMAL)) {
				os.storeDeviceUseEncryption(true);
			} else if(cm.getCommunicationSecurityMode().equals(CommunicationSecurityMode.ZEROIZED)) {
				os.storeDeviceUseEncryption(false);
				os.storeDeviceDESKey((byte)0);
			}
			
			//use antenna mode to switch transmission mode
//			os.storeDeviceSensibilityLongRange(cm.getSetAntennaMode().equals(AntennaMode.OMNI));
//			os.storeDeviceTransmissionMode(cm.getSetAntennaMode().equals(AntennaMode.OMNI));
			
			//use channel priority to configure transmit retries
			os.storeDeviceTransmitRetries(cm.getLinkChannelPriority().ordinal()+1);
			
			//data link state
			if(cm.getSetDataLinkState().equals(DataLinkState.OFF)) {
				os.storeAndSwitchDeviceTxPower(TransmitterPower.TURN_OFF);
				rxEnabled = false;
				txEnabled = false;
			} else if(cm.getSetDataLinkState().equals(DataLinkState.RX_ONLY)) {
				os.storeAndSwitchDeviceTxPower(TransmitterPower.TURN_OFF);
				rxEnabled = true;
				txEnabled = false;
			} else if(cm.getSetDataLinkState().equals(DataLinkState.TX_AND_RX)) {
				os.storeAndSwitchDeviceTxPower(TransmitterPower.QUARTER_POWER);
				rxEnabled = true;
				txEnabled = true;
			} else if(cm.getSetDataLinkState().equals(DataLinkState.TX_HIGH_POWER_AND_RX)) {
				os.storeAndSwitchDeviceTxPower(TransmitterPower.MAX_POWER);
				rxEnabled = true;
				txEnabled = true;
			}

			//update report with current settings
			populateReportWithModemState();
			
			//apply new configurations
			os.performSoftReset();

		} catch (Exception e) {
			logger.throwing(null, null, e);
			logger.warning("Couldn't apply control commands to modem. e=" + e.toString());
			e.printStackTrace();
			exitModemCommandMode();
		}
	}
	
	@Override
	public int getDownlinkStatus() {
//		System.out.println("rssi: " + rssiLocalHeardRemote + "; pc: " + convertRssiToPercent(rssiLocalHeardRemote));
		return convertRssiToPercent(rssiLocalHeardRemote);
	}

	@Override
	public int getUplinkStatus() {
		return convertRssiToPercent(rssiRemoteHeardLocal);
	}
		private int convertRssiToPercent(int rssi) {
		//regression equations for subjetive analysis of rssi X dBm
		//points found in Aerocomm manual
		if(rssi<=17) {
			return (int)MathHelper.clamp(-6.1677*rssi + 167.83, 0, 100);
		} else {
			return (int)MathHelper.clamp(-0.3663*rssi + 69.227, 0, 100);
		}
		//find exact result in table
//		if(rssiToPercentTable.get(rssi)!=null) {
//			return rssiToPercentTable.get(rssi);
//			
//		//interpolate result from table value
//		} else {
//			//MAP IS NOT ORDERED. THIS ALGORITHM WON'T WORK
//			int foundPc = -1;
//			for (Entry<Integer,Integer> es : rssiToPercentTable.entrySet()) {
//				if(foundPc==-1) {
//					if(es.getKey()>rssi) {
//						foundPc = es.getValue();
//					}
//				} else {
//					return ((foundPc+es.getValue())/2);
//				}
//			}
//		}
//		return 0;
	}

	@Override
	protected void sendNextPacket(byte[] data, int len) throws IOException {
		if(txEnabled) {
			super.sendNextPacket(data, len);
		}
	}

	private void enterModemCommandMode() {
		try {
			connection.getOutputStream().enterCommandMode();
		} catch (Exception e) {
			logger.warning("Failed to enter command mode in modem");
			logger.throwing(null, null, e);
			e.printStackTrace();
		}
	}

	private void exitModemCommandMode() {
		try {
			connection.getOutputStream().exitCommandMode();
		} catch (Exception e) {
			logger.warning("Couldn't exit command mode in modem. Trying to perform a soft reset");
			try {
				connection.getOutputStream().performSoftReset();
			} catch (Exception e1) {
				logger.throwing(null, null, e1);
				e.printStackTrace();
			}
		}
	}

	@Override
	public void notifyTransmittedRFPacketResult(int rssiLocalHeardRemote, int rssiRemoteHeardLocal, boolean successfulDelivery) {
		if(successfulDelivery) {
			notifyUplinkActivity();//if my packet was sent successfully, my uplink is working well
			notifyDownlinkActivity();//if I received an acknowledge, I am receiving data from remote transmitter
		} else {
			txErrors++;
		}
//		System.out.println("Transmitted packet: localHeardRemote: " + rssiLocalHeardRemote + "; remoteHeardLocal: " + rssiRemoteHeardLocal + "; success: " + successfulDelivery);
		this.rssiLocalHeardRemote = rssiLocalHeardRemote!=-1?rssiLocalHeardRemote:this.rssiLocalHeardRemote;
		this.rssiRemoteHeardLocal = rssiRemoteHeardLocal!=-1?rssiRemoteHeardLocal:this.rssiRemoteHeardLocal;
		packetsResultCounter++;
	}

	@Override
	public void notifyReceivedRFPacketResult(int rssiLocalHeardRemote, int rssiRemoteHeardLocal, int payloadLen) {
		notifyDownlinkActivity();
//		System.out.println("Received packet: localHeardRemote: " + rssiLocalHeardRemote + "; remoteHeardLocal: " + rssiRemoteHeardLocal + "; len=" + payloadLen);
		this.rssiLocalHeardRemote = rssiLocalHeardRemote!=-1?rssiLocalHeardRemote:this.rssiLocalHeardRemote;
		this.rssiRemoteHeardLocal = rssiRemoteHeardLocal!=-1?rssiRemoteHeardLocal:this.rssiRemoteHeardLocal;;
	}
	
	@Override
	public String getInfo() {
		return "AC4790DataTerminal "+serialConnectionParams.toString();
	}
	
	@Override
	public int getTxErrors() {
		return txErrors;
	}
	
	@Override
	public int getPacketsSentAndGotByModem() {
		return packetsResultCounter;
	}
	
}
