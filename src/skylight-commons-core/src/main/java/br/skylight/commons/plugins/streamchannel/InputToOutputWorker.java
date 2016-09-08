package br.skylight.commons.plugins.streamchannel;

import java.io.InputStream;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import br.skylight.commons.dli.services.MessageType;
import br.skylight.commons.dli.services.MessagingService;
import br.skylight.commons.dli.skylight.StreamChannelData;
import br.skylight.commons.infra.SyncCondition;
import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.plugin.annotations.ServiceInjection;

public class InputToOutputWorker extends ThreadWorker {

	private static final Logger logger = Logger.getLogger(InputToOutputWorker.class.getName());
	
	private InputStream is;
	private byte[] buffer = new byte[StreamChannelData.DATA_LENGTH];
	private int channelNumber;
	private StreamChannelOperator co;
	private SyncCondition pendingBytesToBeSent = new SyncCondition("Pending bytes to be sent");

	@ServiceInjection
	public MessagingService messagingService;
	
	public InputToOutputWorker(StreamChannelOperator co, InputStream is, int channelNumber, int maxBytesPerSecond) {
		super((float)maxBytesPerSecond/(float)StreamChannelData.DATA_LENGTH);//limit frequency
//		super(99);//limit frequency
		this.is = is;
		this.channelNumber = channelNumber;
		this.co = co;
	}
	
	@Override
	public void onDeactivate() throws Exception {
		super.onDeactivate();
	}
	@Override
	public void step() throws Exception {
		try {
//			System.out.println("InputToOutputWorker: fq " + getStepFrequencyAverage() + "Hz; lastStepTime " + getLastStepTime() + "ms");
			if(is.available()>0) {
				pendingBytesToBeSent.notifyConditionMet();
				int s = is.read(buffer);
				StreamChannelData m = messagingService.resolveMessageForSending(MessageType.M2014.getImplementation());
				m.setChannelNumber(channelNumber);
				m.setData(buffer, s);
				m.setCucsID(co.getCucsId());
				m.setVehicleID(co.getVehicleId());
				messagingService.sendMessage(m);
			} else {
				pendingBytesToBeSent.notifyConditionNotMet();
			}
		} catch(Exception e) {
			logger.fine("Cannot read from tcp stream. Closing channel.");
			co.closeChannel();
		}
	}
	
	public void waitUntilAllPendingBytesWereSent() throws TimeoutException {
		pendingBytesToBeSent.waitForConditionNotMet(20000);
	}
	
}
