package br.skylight.commons.io.dataterminal;

import java.io.IOException;
import java.net.NetworkInterface;

import br.skylight.commons.dli.datalink.DataLinkControlCommand;
import br.skylight.commons.dli.datalink.DataLinkSetupMessage;
import br.skylight.commons.dli.datalink.DataLinkStatusReport;
import br.skylight.commons.dli.enums.AntennaMode;
import br.skylight.commons.dli.enums.AntennaState;
import br.skylight.commons.dli.enums.CommunicationSecurityMode;
import br.skylight.commons.dli.enums.CommunicationSecurityState;
import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.dli.services.Message;
import br.skylight.commons.dli.services.MessageListener;
import br.skylight.commons.infra.CounterStats;
import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.plugins.datarecorder.LoggerDataPacketListener;

public abstract class DataTerminal extends ThreadWorker implements MessageListener {

	public static final int MAX_PACKETS_PER_SECOND = 600;
	
	public static final String DEFAULT_MULTICAST_ADDRESS = "224.0.0.1";
	public static final NetworkInterface DEFAULT_MULTICAST_NETWORK_INTERFACE = IOHelper.getDefaultNetworkInterface();
//	public static NetworkInterface DEFAULT_MULTICAST_NETWORK_INTERFACE = IOHelper.getLoopbackInterface();

	public static final int DEFAULT_MULTICAST_CUCS_TO_VSM_PORT = 1111;
	public static final int DEFAULT_MULTICAST_VSM_TO_CUCS_PORT = 2222;
	
	private byte[] inBuffer = new byte[1024];
	protected DataPacketListener dataPacketListener = null;
	protected boolean txEnabled = true;
	protected boolean rxEnabled = true;
	protected LoggerDataPacketListener loggerPacketListener = null;
//	private int downlinkStatus = -1;
//	private int uplinkStatus = -1;
//	private int txErrors = -1;

	//STATISTICS
//	private boolean enableStatistics = true;
	private CounterStats sentBytesCounter = new CounterStats(500);
	private CounterStats sentPacketsCounter = new CounterStats(500);
	private CounterStats receivedBytesCounter = new CounterStats(500);
	private CounterStats receivedPacketsCounter = new CounterStats(500);
	
	private double lastPacketSentTime;
	private double lastPacketReceivedTime;

	private double lastDownlinkActivityTime;
	private double lastUplinkActivityTime;
	
	private DataTerminalType dataTerminalType;
	private int dataLinkId;

	private DataLinkStatusReport dataLinkStatusReport = new DataLinkStatusReport();

	public DataTerminal() {
		this(null, 0);
	}
	
	public DataTerminal(DataTerminalType dataTerminalType, int dataLinkId) {
//		super(MAX_PACKETS_PER_SECOND);
		super(-1);
		this.dataTerminalType = dataTerminalType;
		this.dataLinkId = dataLinkId;
		dataLinkStatusReport.setAddressedTerminal(dataTerminalType);
		dataLinkStatusReport.setDataLinkId(dataLinkId);
	}

	public void setDataPacketListener(DataPacketListener dataPacketListener) {
		this.dataPacketListener = dataPacketListener;
	}

	@Override
	public void step() throws Exception {
		if(!rxEnabled) return;
//		long t = System.currentTimeMillis();
		int len = readNextPacket(inBuffer);
//		System.out.println(name + ": recv " + totalBytesReceived + "; sent " + totalBytesSent + "; fq " + getStepFrequencyAverage() + "Hz; timeToRead: "+ (System.currentTimeMillis()-t) +"ms; lastWholeStepTime: "+ getLastWholeStepTime() +"ms; lastRecv: " + len);
		lastPacketReceivedTime = getCurrentTime();
		notifyDownlinkActivity();
		if(len>0 && dataPacketListener!=null) {
			lastPacketReceivedTime = getCurrentTime();
			dataPacketListener.onPacketReceived(inBuffer, len, lastPacketReceivedTime);
			if(loggerPacketListener!=null) {
				loggerPacketListener.onPacketReceived(inBuffer, len, lastPacketReceivedTime);
			}
//			if(enableStatistics) {
				receivedBytesCounter.addValue(len);
				receivedPacketsCounter.addValue(1);
//			}
		}
	}

	protected abstract int readNextPacket(byte[] buffer) throws IOException;
	
	public final void sendPacket(byte[] data, int len) throws IOException {
		if(!txEnabled) return;
		sendNextPacket(data, len);
		lastPacketSentTime = getCurrentTime();
		notifyUplinkActivity();
		if(loggerPacketListener!=null) {
			loggerPacketListener.onPacketSent(data, len);
		}
//		if(enableStatistics) {
			sentBytesCounter.addValue(len);
			sentPacketsCounter.addValue(1);
//		}
	}
	protected abstract void sendNextPacket(byte[] data, int len) throws IOException;
	
//	public void setStatisticsEnabled(boolean enabled) {
//		this.enableStatistics = enabled;
//	}
//	
//	public boolean isStatisticsEnabled() {
//		return enableStatistics;
//	}
	
	public float getOutputRate() {
//		if(!enableStatistics) return -1;//throw new IllegalStateException("DataTerminal not in statistics mode");
		return (float)sentBytesCounter.getRate();
	}
	
	public float getPacketsSentRate() {
//		if(!enableStatistics) return -1;//throw new IllegalStateException("DataTerminal not in statistics mode");
		return (float)sentPacketsCounter.getRate();
	}
	
	public float getInputRate() {
//		if(!enableStatistics) return -1;//throw new IllegalStateException("DataTerminal not in statistics mode");
		return (float)receivedBytesCounter.getRate();
	}

	public float getPacketsReceivedRate() {
//		if(!enableStatistics) return -1;//throw new IllegalStateException("DataTerminal not in statistics mode");
		return (float)receivedPacketsCounter.getRate();
	}
	
	public double getCurrentTime() {
		return System.currentTimeMillis()/1000.0;
	}
	
	public long getTotalBytesReceived() {
		return (long)receivedBytesCounter.getTotal();
	}
	
	public long getTotalBytesSent() {
		return (long)sentBytesCounter.getTotal();
	}
	
	public long getTotalPacketsReceived() {
		return (long)receivedPacketsCounter.getTotal();
	}
	
	public long getTotalPacketsSent() {
		return (long)sentPacketsCounter.getTotal();
	}
	
	public void setLoggerPacketListener(LoggerDataPacketListener loggerPacketListener) {
		this.loggerPacketListener = loggerPacketListener;
	}

	public double getTimeSinceLastPacketReceived() {
		return getCurrentTime()-lastPacketReceivedTime;
	}
	public double getTimeSinceLastPacketSent() {
		return getCurrentTime()-lastPacketSentTime;
	}
	
	@Override
	public void setName(String name) {
		super.setName(name + " data terminal");
	}

	
	/** LINK SETUP, CONTROL AND REPORTING FEATURES **/
	
	/**
	 * Returns current downlink signal strength from 0-100 percent.
	 * This is how strong the local transceiver is hearing remote transmitter
	 * If this information is not supported by data terminal implementation, return -1;
	 */
	public int getDownlinkStatus() {
		return -1;
	}

	/**
	 * Returns current uplink signal strength from 0-100 percent.
	 * This is how strong the remote transceiver is hearing local transmitter
	 * If this information is not supported by data terminal implementation, return -1;
	 */
	public int getUplinkStatus() {
		return -1;
	}
	
	public abstract void setupDataLink(DataLinkSetupMessage sm);
	protected DataLinkStatusReport populateStatusReportWithSetupMessage(DataLinkSetupMessage sm) {
		dataLinkStatusReport.setVehicleID(sm.getVehicleID());
		dataLinkStatusReport.setAddressedTerminal(sm.getAddressedTerminal());
		dataLinkStatusReport.setDataLinkId(sm.getDataLinkId());
		dataLinkStatusReport.setReportedChannel(sm.getSelectChannel());
		dataLinkStatusReport.setReportedForwardLinkCarrierFreq(sm.getSelectForwardLinkCarrierFreq());
		dataLinkStatusReport.setReportedReturnLinkCarrierFreq(sm.getSelectReturnLinkCarrierFreq());
		dataLinkStatusReport.setReportedPrimaryHopPattern(sm.getSelectPrimaryHopPattern());
		return dataLinkStatusReport;
	}
	
	public abstract void controlDataLink(DataLinkControlCommand cm);
	protected DataLinkStatusReport populateStatusReportWithControlCommand(DataLinkControlCommand cm) {
		dataLinkStatusReport.setVehicleID(cm.getVehicleID());
		dataLinkStatusReport.setAddressedTerminal(cm.getAddressedTerminal());
		dataLinkStatusReport.setDataLinkId(cm.getDataLinkId());
		
		if(cm.getSetAntennaMode().equals(AntennaMode.AUTO)) {
			dataLinkStatusReport.setAntennaState(AntennaState.OMNI);
		} else {
			dataLinkStatusReport.setAntennaState(AntennaState.values()[cm.getSetAntennaMode().ordinal()]);
		}
		
		if(cm.getCommunicationSecurityMode().equals(CommunicationSecurityMode.NORMAL)) {
			dataLinkStatusReport.setCommunicationSecurityState(CommunicationSecurityState.KEYED);
		} else if(cm.getCommunicationSecurityMode().equals(CommunicationSecurityMode.ZEROIZED)) {
			dataLinkStatusReport.setCommunicationSecurityState(CommunicationSecurityState.ZEROIZED);
		}
		
		dataLinkStatusReport.setLinkChannelPriorityState(cm.getLinkChannelPriority());
		dataLinkStatusReport.setDataLinkState(cm.getSetDataLinkState());
		return dataLinkStatusReport;
	}
	
	public DataLinkStatusReport getDataLinkStatusReport() {
		dataLinkStatusReport.setDownlinkStatus((short)getDownlinkStatus());
		return dataLinkStatusReport;
	}
	
	@Override
	/**
	 * Receive messages addressed to data terminal
	 */
	public void onMessageReceived(Message message) {
		//ignore messages if this data terminal is not GDT or ADT
		if(dataTerminalType!=null) {
			if(message instanceof DataLinkStatusReport) {
				DataLinkStatusReport m = (DataLinkStatusReport)message;
				//use other data terminal status
				if(!m.getAddressedTerminal().equals(dataTerminalType) && m.getDataLinkId()==dataLinkId) {
					
				}
			} else if(message instanceof DataLinkSetupMessage) {
				DataLinkSetupMessage m = (DataLinkSetupMessage)message;
				if(m.getAddressedTerminal().equals(dataTerminalType) && m.getDataLinkId()==dataLinkId) {
					setupDataLink(m);
				}
			} else if(message instanceof DataLinkControlCommand) {
				DataLinkControlCommand m = (DataLinkControlCommand)message;
				if(m.getAddressedTerminal().equals(dataTerminalType) && m.getDataLinkId()==dataLinkId) {
					controlDataLink(m);
				}
			}
		}
	}
	
	public DataTerminalType getDataTerminalType() {
		return dataTerminalType;
	}
	
	public void notifyDownlinkActivity() {
		lastDownlinkActivityTime = getCurrentTime();
	}
	public void notifyUplinkActivity() {
		lastUplinkActivityTime = getCurrentTime();
	}
	public double getTimeSinceLastUplinkActivity() {
		return getCurrentTime()-lastUplinkActivityTime;
	}
	public double getTimeSinceLastDownlinkActivity() {
		return getCurrentTime()-lastDownlinkActivityTime;
	}
	
	public String getInfo() {
		return (rxEnabled?"RX on":"RX off") + ";" + (txEnabled?"TX on":"TX off") + (dataTerminalType!=null?";"+dataTerminalType.name():"");
	}
	
	public int getTxErrors() {
		return -1;
	}
	
	public int getPacketsSentAndGotByModem() {
		return -1;
	}

}
