package br.skylight.commons.io.dataterminal.ac4790;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.MathHelper;
import br.skylight.commons.infra.SyncCondition;
import br.skylight.commons.io.FilteredInputStream;
import br.skylight.commons.io.ZChunkedInputStream;
import br.skylight.commons.io.ZChunkedOutputStream;

public class AC4790OutputStream extends FilterOutputStream {

	private static final Logger logger = Logger.getLogger(AC4790OutputStream.class.getName());

	//time to wait until sending command mode command
	//IMPORTANT: when sending data to unreachable destination, the modem doesn't accept an enter at command until all rf packets are timed out (may last some seconds)
	//Maximum Time to Transmit Packet = 50ms * Transmit Retries * (Full Duplex + 1) * (Random Backoff + 1)
	private static int waitTimeToEnterCommandMode = -1;
	
	//time to wait for entering command mode before timeout
	private static final int TIMEOUT_COMMAND_MODE = 15000;
	
	//time to wait for a desired sequence in response stream
	private static final int TIMEOUT_READ_BUFFER = 2000;

	public static final byte CC_BYTE = (byte) 0xCC;
	
	public static final byte POWER_MIN = 0x01;
	public static final byte POWER_LOW = 0x0F;
	public static final byte POWER_QUARTER = 0x07;
	public static final byte POWER_HALF = 0x0A;

	//RSSI probe
//	public static final byte PROBE_REPORT = (byte) 0x86;
//	public static final byte RESPONSE_PROBE = (byte) 0x87;
	
	// enter commands mode
	public static final byte[] COMMAND_ENTER_AT_MODE = new byte[] { 0x41, 0x54, 0x2B, 0x2B, 0x2B, 0x0D };
	public static final byte[] COMMAND_ENTER_AT_MODE_RETURN = new byte[] { CC_BYTE, 0x43, 0x4F, 0x4D };

	public static final byte[] COMMAND_READ_DESTINATION_MAC = new byte[] { CC_BYTE, (byte) 0x11 };
	public static final byte[] COMMAND_ENTER_PROBE_MODE = new byte[] { CC_BYTE, (byte)0x8E };
	public static final byte[] COMMAND_SET_MAX_POWER = new byte[] { CC_BYTE, 0x25 };
	public static final byte[] COMMAND_CHANGE_CHANNEL = new byte[] { CC_BYTE, 0x01 };

//	public static final byte[] COMMAND_READ_TEMPERATURE = new byte[] { CC_BYTE, (byte) 0xA4 };

	// store eeprom configurations (need reset after writing)
	public static final byte[] COMMAND_EEPROM_BYTE_READ = new byte[] { CC_BYTE, (byte) 0xC0 };
	public static final byte[] COMMAND_EEPROM_BYTE_WRITE = new byte[] { CC_BYTE, (byte) 0xC1 };
	public static final byte EEPROM_ADDRESS_TRANSMIT_RETRIES = (byte) 0x4C;
	public static final byte EEPROM_ADDRESS_RANDOM_BACKOFF = (byte) 0xC3;
	
	public static final byte EEPROM_ADDRESS_DES_KEY = (byte) 0xD0;
	public static final byte EEPROM_ADDRESS_CONTROL_0 = (byte) 0x45;// enable encryption
	
	public static final byte EEPROM_ADDRESS_CONTROL_1 = (byte) 0x56;// half/full
	public static final byte EEPROM_ADDRESS_API_CONTROL = (byte) 0xC1;
	public static final byte EEPROM_ADDRESS_SENSE_ADJUST = (byte) 0xC8;
	public static final byte EEPROM_ADDRESS_CHANNEL_NUMBER = (byte) 0x40;
	public static final byte EEPROM_ADDRESS_MAX_POWER = (byte) 0x63;
	public static final byte EEPROM_ADDRESS_ORIGINAL_MAX_POWER = (byte) 0x8E;
		
	//destination
//	public static final byte EEPROM_ADDRESS_DESTINATION_ID = (byte)0x70;

	// soft reset device
	public static final byte[] COMMAND_SOFT_RESET = new byte[] { CC_BYTE, (byte) 0xFF };

	// exit commands mode
	public static final byte[] COMMAND_EXIT_AT_MODE = new byte[] { CC_BYTE, 0x41, 0x54, 0x4F, 0x0D };
	public static final byte[] COMMAND_EXIT_AT_MODE_RETURN = new byte[] { CC_BYTE, 0x44, 0x41, 0x54 };

	private Lock writeLock = new ReentrantLock();
	
	private long lastRFWriteTime;

	private FilteredInputStream<AC4790StreamFilter> filteredInputStream;
	private AC4790StreamFilter streamFilter;

//	private boolean internalLock;
	public enum TransmitterPower {MAX_POWER, QUARTER_POWER, TURN_OFF}
	
	private boolean inCommandMode = false;
	private InputStream is;
	private int longRangeSensibility;

	protected AC4790OutputStream(OutputStream outputStream, InputStream inputStream, int longRangeSensibility, AC4790StreamFilterListener listener) throws IOException {
		super(outputStream);
		this.is = inputStream;
		this.longRangeSensibility = longRangeSensibility;
		streamFilter = new AC4790StreamFilter(inputStream, listener);
		this.filteredInputStream = new FilteredInputStream<AC4790StreamFilter>(inputStream, streamFilter);
		this.filteredInputStream.startFilteredMode();
		try {
			changeToNormalOperationFilter();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private int getWaitTimeToEnterCommandMode() throws Exception {
		if(waitTimeToEnterCommandMode==-1) {
//			System.out.println("WT enter command");
			enterCommandModeInternal(2500);
			//Maximum Time to Transmit Packet = 50ms * Transmit Retries * (Full Duplex + 1) * (Random Backoff + 1)
//			System.out.println("WT eeprom read");
			int transmitRetries = eepromByteRead(EEPROM_ADDRESS_TRANSMIT_RETRIES);
			byte control1 = eepromByteRead(EEPROM_ADDRESS_CONTROL_1);
			int fullDuplex = IOHelper.getBit(control1, (byte) 1)?1:0;
			int backoff = eepromByteRead(EEPROM_ADDRESS_RANDOM_BACKOFF);
			//número máximo de bytes em buffer para CTS ON
//				int ctsOnBytes = eepromByteRead(EEPROM_ADDRESS_CTS_ON) + 128;
//				int ctsOffBytes = eepromByteRead(EEPROM_ADDRESS_CTS_OFF) + 128;
			int maxTimeToTransmitPacket = 50 * transmitRetries * (fullDuplex+1) * (backoff+1);
			
			//times 8 is a multiplication determined experimentally (may be wrong)
			waitTimeToEnterCommandMode = maxTimeToTransmitPacket * 10;
		}
		return waitTimeToEnterCommandMode;
	}
	
	public void enterCommandMode() throws Exception {
		writeLock.lock();
		
		//turn-off stream compression (if any) before talking to modem
		if(out instanceof ZChunkedOutputStream) {
			((ZChunkedOutputStream)out).setCompressionBypass(true);
		}
		if(is instanceof ZChunkedInputStream) {
			((ZChunkedInputStream)is).setCompressionBypass(true);
		}
		
		int wt = getWaitTimeToEnterCommandMode();
		
		//may be getWaitTime...() has entered command mode, so we don't need to do that anymore
		if(inCommandMode) {
			return;
		} else {
			enterCommandModeInternal(wt);
		}
	}

	/**
	 * Enter command mode. 
	 * If there is a timeout waiting for command mode confirmation, 
	 * try to exit command mode and try again.
	 * @param waitTime Wait to timeout if enter command mode is not confirmed
	 * @throws Exception
	 */
	private void enterCommandModeInternal(int waitTime) throws Exception {
		long st = System.currentTimeMillis();
		while((System.currentTimeMillis()-st)<TIMEOUT_COMMAND_MODE) {
			try {
				tryToEnterCommandMode(waitTime);
				inCommandMode = true;
				return;
			} catch (Exception e) {
				logger.warning("Failed to enter command mode. Retrying. waitTime=" + waitTime + "ms; e=" + e.toString());
//				System.out.println("Failed to enter command mode. Retrying. waitTime=" + waitTime + "ms; e=" + e.toString());
//				System.out.println("SEND EXIT AT COMMAND");
				//send exit command to guarantee we are not already in command mode in next retry
				super.out.write(COMMAND_EXIT_AT_MODE);
				super.out.flush();
				//drain stream so we will know input state
				drainInputStream();
//				System.out.println("STREAM DRAINED");
				Thread.sleep(waitTime);
			}
		}
		//failed to enter command mode
		writeLock.unlock();
//		System.out.println("ENTER COMMAND LOCK RELEASE - FAILED TO ENTER");
		throw new TimeoutException("Timeout trying to enter command mode");
	}	
	
	private void tryToEnterCommandMode(int waitTime) throws Exception {
		// execute and read confirmation. In meanwhile, all incoming bytes that
		// are not the command confirmation are routed normally to inputstream's
		// caller

		// prepare filter stream for waiting for command confirmation
		streamFilter.setStartSequence(COMMAND_ENTER_AT_MODE_RETURN);
		streamFilter.setEndSequence(null);
		streamFilter.useNormalMode();
		changeToCommandOperationFilter();

		// execute later so that the filter will be waiting before sending the command
//		final long waitTimeForCommandMode = getWaitTimeToEnterCommandMode();
		final long waitTimeForCommandMode = Math.max(0, waitTime-getTimeSinceLastRFWrite());
		final SyncCondition waitingSequenceCondition = new SyncCondition("waiting sequence");
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
//					System.out.println("wait time " + waitTimeForCommandMode);
					Thread.sleep(waitTimeForCommandMode);
					//write only after waiting for response sequence
//					System.out.print("waiting to write...");
					waitingSequenceCondition.waitForCondition(TIMEOUT_READ_BUFFER);
					Thread.sleep(10);
					getSuperOut().write(COMMAND_ENTER_AT_MODE);
					getSuperOut().flush();
//					System.out.println("WROTE ENTER AT");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
		
		try {
			waitingSequenceCondition.notifyConditionMet();
//			System.out.println("Waiting for enter at response " + (waitTimeForCommandMode+TIMEOUT_READ_BUFFER*2));
			filteredInputStream.getStreamFilter().waitForSequence(waitTimeForCommandMode+TIMEOUT_READ_BUFFER*2);
//			System.out.println("Response gotten");
			logger.finer("Entered command mode");
		} finally {
			changeToNormalOperationFilter();
		}
	}

	public void exitCommandMode() throws Exception {
		long st = System.currentTimeMillis();
		while((System.currentTimeMillis()-st)<TIMEOUT_COMMAND_MODE) {
			try {
				executeCommandAsserted(COMMAND_EXIT_AT_MODE, new byte[COMMAND_EXIT_AT_MODE_RETURN.length], COMMAND_EXIT_AT_MODE_RETURN);
				logger.finer("Exited command mode");
				inCommandMode = false;
				
				//turn-on stream compression (if any) before talking to modem
				if(out instanceof ZChunkedOutputStream) {
					((ZChunkedOutputStream)out).setCompressionBypass(false);
				}
				if(is instanceof ZChunkedInputStream) {
					((ZChunkedInputStream)is).setCompressionBypass(false);
				}

				writeLock.unlock();
				return;
			} catch (Exception e) {
				logger.warning("Failed to exit command mode. Retrying. " + e.toString());
//				System.out.println("Failed to exit command mode. Retrying. " + e.toString());
				e.printStackTrace();
				Thread.sleep(7000);
//				System.out.println("SEND ENTER AT COMMAND");
				super.out.write(COMMAND_ENTER_AT_MODE);
				super.out.flush();
				//drain stream so we will know input state
				drainInputStream();
//				System.out.println("STREAM DRAINED");
			}
		}
		//failed to exit command mode. release any lock
		writeLock.unlock();
//		System.out.println("ENTER COMMAND LOCK RELEASE - EXIT");
		throw new TimeoutException("Timeout trying to exit command mode");
	}
	
	private void changeToCommandOperationFilter() throws IOException {
		streamFilter.setEnableSequenceFiltering(true);
	}

	private void changeToNormalOperationFilter() throws Exception {
		streamFilter.setEnableSequenceFiltering(false);
	}
	
	private long getTimeSinceLastRFWrite() {
		return System.currentTimeMillis()-lastRFWriteTime;
	}
	
	private void drainInputStream() throws IOException {
		getFilteredInputStream().getStreamFilter().useStreamMode();
		getFilteredInputStream().startFilteredMode();
		IOHelper.drainStream(getFilteredInputStream().getStreamFilter().getFilterIn(), 500);
	}
	
	public OutputStream getSuperOut() {
		return super.out;
	}

	@Override
	public void write(int b) throws IOException {
		writeLock.lock();
		try {
			super.out.write(b);
			lastRFWriteTime = System.currentTimeMillis();
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		writeLock.lock();
		try {
			super.out.write(b);
			lastRFWriteTime = System.currentTimeMillis();
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		writeLock.lock();
		try {
			super.out.write(b, off, len);
			lastRFWriteTime = System.currentTimeMillis();
		} finally {
			writeLock.unlock();
		}
	}

	public FilteredInputStream<AC4790StreamFilter> getFilteredInputStream() {
		return filteredInputStream;
	}

	public void eepromByteWrite(byte address, byte value) throws IOException {
		checkCommandMode();
		byte[] result = new byte[3];
		executeCommand(IOHelper.add(COMMAND_EEPROM_BYTE_WRITE, new byte[] { address, (byte) 1, value }), result);
		IOHelper.assertEquals(result[0], address);
		IOHelper.assertEquals(result[1], (byte) 1);
		IOHelper.assertEquals(result[2], value);
	}

	public byte eepromByteRead(byte address) throws IOException {
		checkCommandMode();
//		byte[] result = new byte[4];
//		executeCommand(IOHelper.add(COMMAND_EEPROM_BYTE_READ, new byte[] { address, (byte) 1 }), result);
//		IOHelper.assertEquals(result[0], AC4790Helper.CC_BYTE_RESULT[0]);
//		IOHelper.assertEquals(result[1], address);
//		IOHelper.assertEquals(result[2], (byte) 1);
//		return result[3];
		return eepromBytesRead(address, 1)[0];
	}

	public byte[] eepromBytesRead(byte address, int size) throws IOException {
		checkCommandMode();
		byte[] result = new byte[3 + size];
		executeCommand(IOHelper.add(COMMAND_EEPROM_BYTE_READ, new byte[] { address, (byte) size }), result);
		IOHelper.assertEquals(result[0], AC4790OutputStream.CC_BYTE);
		IOHelper.assertEquals(result[1], address);
		IOHelper.assertEquals(result[2], (byte) size);
		byte[] r = new byte[size];
		System.arraycopy(result, 3, r, 0, size);
		return r;
	}
	
	public void executeCommandAsserted(byte[] params, byte[] expectedResult) throws IOException {
		byte[] result = new byte[expectedResult.length];
		executeCommand(params, result);
		IOHelper.assertEquals(result, expectedResult);
	}

	public void executeCommandAsserted(byte[] params, byte[] result, byte[] expectedResult) throws IOException {
		executeCommand(params, result);
		IOHelper.assertEquals(result, expectedResult);
	}

	public void executeCommand(final byte[] params, byte[] result) throws IOException {
		// execute command
		streamFilter.useStreamMode();
		changeToCommandOperationFilter();
		// verify command execution
		try {
//			System.out.println("exec command: WRITE params");
			super.out.write(params);
			super.out.flush();
			if(logger.isLoggable(Level.FINE)) {
				logger.fine("Sent command " + IOHelper.bytesToHexString(params));
			}
	//		getFilteredInputStream().getStreamFilter().setBytesToFilterOut(result.length);

//			System.out.println("exec command: READ RESPONSE");
			IOHelper.readFully(streamFilter.getFilterIn(), result, TIMEOUT_READ_BUFFER, true);
//			System.out.println("exec command: READ OK");
		} catch (Exception e) {
			throw new IOException(e);
		} finally {
			try {
//				getFilteredInputStream().stopFilteredMode();
				changeToNormalOperationFilter();
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}

	/**
	 * This configuration is stored on EPROM. You will have to reset the unit
	 * for this configuration to be used.
	 * 
	 * @param numberOfTransmits
	 *            value between 0-255
	 * @throws Exception
	 */
	public void storeDeviceTransmitRetries(int numberOfTransmits) throws Exception {
		checkCommandMode();
		eepromByteWrite(AC4790OutputStream.EEPROM_ADDRESS_TRANSMIT_RETRIES, (byte) MathHelper.clamp(numberOfTransmits, 0, 255));
		waitTimeToEnterCommandMode = -1;
	}
	public int getDeviceTransmitRetries() throws Exception {
		checkCommandMode();
		return eepromByteRead(AC4790OutputStream.EEPROM_ADDRESS_TRANSMIT_RETRIES);
	}

	public void performSoftReset() throws IOException {
		executeCommand(AC4790OutputStream.COMMAND_SOFT_RESET, new byte[0]);
		inCommandMode = false;
		writeLock.unlock();
	}

	/**
	 * This configuration is stored on EPROM. You will have to reset the unit
	 * for this configuration to be used.
	 * @throws Exception 
	 */
	public void storeDeviceUseEncryption(boolean use) throws Exception {
		checkCommandMode();
		byte p = eepromByteRead(AC4790OutputStream.EEPROM_ADDRESS_CONTROL_0);
		p = IOHelper.setBit(p, (byte)6, use);
		eepromByteWrite(AC4790OutputStream.EEPROM_ADDRESS_CONTROL_0, p);
	}
	public boolean isDeviceUsingEncryption() throws Exception {
		checkCommandMode();
		byte p = eepromByteRead(AC4790OutputStream.EEPROM_ADDRESS_CONTROL_0);
		return IOHelper.getBit(p, (byte)6);
	}

	
	/**
	 * This configuration is stored on EPROM.
	 * Channel number between 0 and 55. For Australia use 48 to 55
	 * @throws Exception
	 */
	public void storeAndSwitchDeviceChannelNumber(byte channelNumber) throws Exception {
		checkCommandMode();
		//store configuration in eeprom
		eepromByteWrite(AC4790OutputStream.EEPROM_ADDRESS_CHANNEL_NUMBER, channelNumber);
		//execute command to change channel now
		executeCommandAsserted(IOHelper.add(COMMAND_CHANGE_CHANNEL, channelNumber), IOHelper.add(AC4790OutputStream.CC_BYTE, channelNumber));
	}
	public byte getDeviceChannelNumber() throws Exception {
		checkCommandMode();
		return eepromByteRead(AC4790OutputStream.EEPROM_ADDRESS_CHANNEL_NUMBER);
	}

	/**
	 * This configuration is stored on EPROM. You will have to reset the unit
	 * for this configuration to be used.
	 * @throws Exception
	 */
	public void storeDeviceTransmissionMode(boolean fullDuplex) throws Exception {
		checkCommandMode();
		byte p = eepromByteRead(AC4790OutputStream.EEPROM_ADDRESS_CONTROL_1);
		p = IOHelper.setBit(p, (byte) 1, fullDuplex);
		eepromByteWrite(AC4790OutputStream.EEPROM_ADDRESS_CONTROL_1, p);
	}
	public boolean isDeviceTransmissionModeFullDuplex() throws Exception {
		checkCommandMode();
		byte p = eepromByteRead(AC4790OutputStream.EEPROM_ADDRESS_CONTROL_1);
		return IOHelper.getBit(p, (byte) 1);
	}

	public void storeDeviceSensibilityLongRange(boolean longRange) throws IOException {
		checkCommandMode();
		eepromByteWrite(AC4790OutputStream.EEPROM_ADDRESS_SENSE_ADJUST, (byte)(longRange?longRangeSensibility:longRangeSensibility-40));
	}
	public boolean isDeviceSensibilityLongRange() throws Exception {
		checkCommandMode();
		byte p = eepromByteRead(AC4790OutputStream.EEPROM_ADDRESS_SENSE_ADJUST);
		return longRangeSensibility==(int)p;
	}
	
	/**
	 * This configuration is stored on EPROM. You will have to reset the unit
	 * for this configuration to be used.
	 * @throws Exception
	 */
	public void storeDeviceDESKey(byte key) throws Exception {
		checkCommandMode();
		eepromByteWrite(AC4790OutputStream.EEPROM_ADDRESS_DES_KEY, key);
	}
	public byte getDeviceDESKey() throws Exception {
		checkCommandMode();
		return eepromByteRead(AC4790OutputStream.EEPROM_ADDRESS_DES_KEY);
	}
	
	/**
	 * This configuration is stored on EPROM.
	 * @throws Exception
	 */
	public void storeAndSwitchDeviceTxPower(TransmitterPower powerType) throws Exception {
		checkCommandMode();
		//store configuration in eeprom
		byte maxPower = eepromByteRead(AC4790OutputStream.EEPROM_ADDRESS_ORIGINAL_MAX_POWER);
		byte power = maxPower;
		if(powerType.equals(TransmitterPower.TURN_OFF)) {
			power = POWER_MIN;//min allowed power
		} else if(powerType.equals(TransmitterPower.QUARTER_POWER)) {
//			power = POWER_LOW;//low 10mW
			power = POWER_QUARTER;//quarter 200mW
		}
		
		//eeprom write
		eepromByteWrite(AC4790OutputStream.EEPROM_ADDRESS_MAX_POWER, power);
		
		//execute command to change power now
		executeCommandAsserted(IOHelper.add(COMMAND_SET_MAX_POWER, power), IOHelper.add(AC4790OutputStream.CC_BYTE, power));
	}
	public TransmitterPower getDeviceTransmitterPower() throws Exception {
		checkCommandMode();
		byte maxPower = eepromByteRead(AC4790OutputStream.EEPROM_ADDRESS_ORIGINAL_MAX_POWER);
		byte currentPower = eepromByteRead(AC4790OutputStream.EEPROM_ADDRESS_MAX_POWER);
		if(currentPower==maxPower) {
			return TransmitterPower.MAX_POWER;
		} else if(currentPower==POWER_MIN) {
			return TransmitterPower.TURN_OFF;
		} else {
			return TransmitterPower.QUARTER_POWER;
		}
	}
	
	/**
	 * Get the last 3 bytes of destination address
	 * @return
	 * @throws Exception
	 */
	public byte[] getDestinationID() throws Exception {
		checkCommandMode();
		byte[] response = new byte[4];
		executeCommand(AC4790OutputStream.COMMAND_READ_DESTINATION_MAC, response);
//			System.out.println(">> " + IOHelper.byteToHex(response[0]) + " " + IOHelper.byteToHex(response[1]) + " " + IOHelper.byteToHex(response[2]) + " " + IOHelper.byteToHex(response[3]));
		IOHelper.assertEquals(response[0], AC4790OutputStream.CC_BYTE);
		byte[] r = new byte[3];
		System.arraycopy(response, 1, r, 0, 3);
		return r;
	}

	private void checkCommandMode() {
		if(!inCommandMode) {
			throw new IllegalStateException("You cannot perform this operation now: modem not in command mode");
		}
	}

	public boolean setupAPIControl(boolean apiReceiveEnable, boolean apiTransmitEnable, boolean sendDataCompleteEnable, boolean unicastOnly, boolean probeEnable) throws IOException {
		byte apiControl = eepromByteRead(AC4790OutputStream.EEPROM_ADDRESS_API_CONTROL);
		byte newApiControl = apiControl;
//		bit-7 � Broadcast Packets	0 = Addressed Packets		1 = Broadcast Packets
		newApiControl = IOHelper.setBit(newApiControl, (byte)7, !unicastOnly);
//		bit-6 � Probe	0 = Disable Probe	1 = Enable Probe
		newApiControl = IOHelper.setBit(newApiControl, (byte)6, false);
//		bit-5 � SLock1	0 = Disable		1 = Enable
//		bit-4 � SLock0	0 = Disable		1 = Enable
//		bit-3 � Unicast Packets	0 = Broadcast or Addressed Packets	1 = Addressed Packets only
		newApiControl = IOHelper.setBit(newApiControl, (byte)3, unicastOnly);
//		bit-2 � Send Data Complete Enable	0 = Disable	1 = Enable
		newApiControl = IOHelper.setBit(newApiControl, (byte)2, sendDataCompleteEnable);
//		bit-1 � API Transmit Packet Enable	0 = Disable Transmit API Packet	1 = Enable Transmit API Packet
		newApiControl = IOHelper.setBit(newApiControl, (byte)1, apiTransmitEnable);
//		bit-0 � API Receive Packet Enable	0 = Disable Receive API Packet	1 = Enable Receive API Packet		
		newApiControl = IOHelper.setBit(newApiControl, (byte)0, apiReceiveEnable);
		
		//store only if it has changed
		if(apiControl!=newApiControl) {
			logger.info("Changing modem API Control (0xC1) configurations");
			eepromByteWrite(AC4790OutputStream.EEPROM_ADDRESS_API_CONTROL, newApiControl);
			return true;
		}
		return false;
	}
	
	public void setProbeMode(boolean active) throws Exception {
		byte mode = active?(byte)0x01:(byte)0x00;
		executeCommandAsserted(IOHelper.add(AC4790OutputStream.COMMAND_ENTER_PROBE_MODE, mode), 
		 					   IOHelper.add(AC4790OutputStream.CC_BYTE, mode));
	}

}
