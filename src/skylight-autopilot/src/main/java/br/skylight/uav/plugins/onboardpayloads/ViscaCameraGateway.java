package br.skylight.uav.plugins.onboardpayloads;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.infra.TimedBoolean;
import br.skylight.commons.io.SerialConnection;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.plugin.annotations.MemberInjection;
import br.skylight.uav.plugins.onboardintegration.OnboardConnections;

@ManagedMember
public class ViscaCameraGateway extends ThreadWorker {

	private static final Logger logger = Logger.getLogger(ViscaCameraGateway.class.getName());
	private InputStream iStream = null;
	private OutputStream oStream = null;
	private int cameraAddress;
	private int[] replyBuffer = new int[16];
	private int[] outputBuffer;
	private boolean cameraBusy = false;
	private static int[][] zoomTable = new int[25][4];
	private TimedBoolean timer = new TimedBoolean(0);

	private Map<ViscaCommandName, ViscaCommand> commands = Collections.synchronizedMap(new HashMap<ViscaCommandName, ViscaCommand>());

	static {
		zoomTable[0] = new int[] { 0x00, 0x00, 0x00, 0x00 };
		zoomTable[1] = new int[] { 0x01, 0x07, 0x08, 0x01 };
		zoomTable[2] = new int[] { 0x02, 0x01, 0x03, 0x0b };
		zoomTable[3] = new int[] { 0x02, 0x07, 0x05, 0x02 };
		zoomTable[4] = new int[] { 0x02, 0x0b, 0x0b, 0x03 };
		zoomTable[5] = new int[] { 0x02, 0x0f, 0x00, 0x03 };
		zoomTable[6] = new int[] { 0x03, 0x01, 0x05, 0x0d };
		zoomTable[7] = new int[] { 0x03, 0x03, 0x06, 0x04 };
		zoomTable[8] = new int[] { 0x03, 0x04, 0x0f, 0x0f };
		zoomTable[9] = new int[] { 0x03, 0x06, 0x02, 0x0c };
		zoomTable[10] = new int[] { 0x03, 0x07, 0x03, 0x0d };
		zoomTable[11] = new int[] { 0x03, 0x08, 0x06, 0x0a };
		zoomTable[12] = new int[] { 0x03, 0x09, 0x02, 0x09 };
		zoomTable[13] = new int[] { 0x03, 0x0a, 0x02, 0x00 };
		zoomTable[14] = new int[] { 0x03, 0x0a, 0x0f, 0x0a };
		zoomTable[15] = new int[] { 0x03, 0x0b, 0x0b, 0x0a };
		zoomTable[16] = new int[] { 0x03, 0x0c, 0x05, 0x0e };
		zoomTable[17] = new int[] { 0x03, 0x0c, 0x0c, 0x0b };
		zoomTable[18] = new int[] { 0x03, 0x0d, 0x07, 0x00 };
		zoomTable[19] = new int[] { 0x03, 0x0d, 0x0f, 0x08 };
		zoomTable[20] = new int[] { 0x03, 0x0e, 0x06, 0x06 };
		zoomTable[21] = new int[] { 0x03, 0x0e, 0x0d, 0x03 };
		zoomTable[22] = new int[] { 0x03, 0x0f, 0x02, 0x05 };
		zoomTable[23] = new int[] { 0x03, 0x0f, 0x09, 0x03 };
		zoomTable[24] = new int[] { 0x04, 0x00, 0x00, 0x00 };
	}

	@MemberInjection
	public OnboardConnections onboardConnections;

	public ViscaCameraGateway() {
		super(20, -1, -1);
	}

	@Override
	public void onActivate() throws Exception {
		// connect to serial port
		SerialConnection conn = onboardConnections.getCameraConnectionParams().resolveConnection();
		iStream = conn.getInputStream();
		oStream = conn.getOutputStream();
		
		// initialize network

		// Broadcast address request
		logger.finest("Sending address request");
		outputBuffer = new int[] { 0x88, 0x30, 0x01, 0xFF };
		sendBytes(outputBuffer);

		// Read address reply
		parseReply();
		cameraAddress = replyBuffer[2] - 1;
		logger.finest("Done. Camera adress is " + cameraAddress);

		// Clear buffers
		logger.finest("Clearing buffers");
		outputBuffer = new int[] { 0x80 | cameraAddress, 0x01, 0x00, 0x01, 0xFF };
		sendBytes(outputBuffer);
		parseReply();
		checkCompletion();
		logger.finest("Done. Buffers clear.");

		// Turn off camera
		logger.finest("Turning off camera");
		outputBuffer = new int[] { 0x80 | cameraAddress, 0x01, 0x04, 0x00, 0x03, 0xFF };
		sendBytes(outputBuffer);
		parseReply();
		checkACK();
		parseReply();
		checkCompletion();
	}

	@Override
	public void onDeactivate() throws Exception {
		// Turn off camera
		logger.finest("Turning off camera");
		outputBuffer = new int[] { 0x80 | cameraAddress, 0x01, 0x04, 0x00, 0x03, 0xFF };
		sendBytes(outputBuffer);
		parseReply();
		checkACK();
		parseReply();
		checkCompletion();
	}

	@Override
	public void step() throws Exception {

		// while executing slow commands like power on
		if (timer.isTimedOut() == false) {
			return;
		} else if (timer.isFirstTestAfterTimeOut()) {
			cameraBusy = false;
		}

		ArrayList<ViscaCommand> toBeSent = new ArrayList<ViscaCommand>(commands.values());

		for (ViscaCommand viscaCommand : toBeSent) {

			ViscaCommand commandCopy = viscaCommand;
			logger.finest("Sending command: " + commandCopy.getCommand().name());
			sendBytes(commandCopy.getCommandBytes());

			try {
				parseReply();
				checkACK();
				parseReply();
				checkCompletion();
				// before removing check if a new command hasn't been included
				if (Arrays.equals(commands.get(commandCopy.getCommand()).getCommandBytes(), commandCopy.getCommandBytes())) {
					commands.remove(commandCopy.getCommand());
				}

				if (commandCopy.getCommand().equals(ViscaCommandName.POWER)) {
					timer.setTime(8000);
					timer.reset();
				}

			} catch (IllegalAccessException e) {
				logger.finest("Illegal access.");
				timer.setTime(500);
				timer.reset();
				cameraBusy = true;
				return;
			}
		}
	}

	public boolean isCameraBusy() {
		return cameraBusy;
	}

	private void sendBytes(int[] toGo) throws IOException {
		for (int i = 0; i < toGo.length; i++) {
			oStream.write(toGo[i]);
		}
		oStream.flush();
	}

	private boolean parseReply() throws IOException, IllegalAccessException {
		int i = 0;

		do {
			replyBuffer[i] = iStream.read();
			if (replyBuffer[i] == 0xFF && i == 3 && replyBuffer[i - 2] >> 4 == 6) {
				switch (replyBuffer[i - 1]) {
				case 0x01:
					throw new RuntimeException("Camera: Message length error");
				case 0x02:
					throw new RuntimeException("Camera: Syntax error");
				case 0x03:
					throw new RuntimeException("Camera: Command buffer full");
				case 0x04:
					throw new RuntimeException("Camera: Command cancelled");
				case 0x05:
					throw new RuntimeException("Camera: No socket");
				case 0x41:
					throw new IllegalAccessException("Camera: Command not executable");
				}
			}
			i++;
		} while (replyBuffer[i - 1] != 0xFF);

		return true;
	}

	private void checkCompletion() {

		if (replyBuffer[0] != (cameraAddress + 8) * 16 || (replyBuffer[1] >> 4) != 5 || replyBuffer[2] != 0xFF)
			throw new RuntimeException("Camera: Completion not received");
	}

	private void checkACK() {

		if (replyBuffer[0] != (cameraAddress + 8) * 16 || (replyBuffer[1] >> 4) != 4 || replyBuffer[2] != 0xFF)
			throw new RuntimeException("Camera: ACK not received");
	}

	private class ViscaCommand {

		private int[] commandBytes;
		private ViscaCommandName command;

		public ViscaCommand(int[] commandBytes, ViscaCommandName command) {
			super();
			this.commandBytes = commandBytes;
			this.command = command;
		}

		public ViscaCommandName getCommand() {
			return command;
		}

		public int[] getCommandBytes() {
			return commandBytes;
		}
	}

	private enum ViscaCommandName {
		POWER, ZOOM_DIRECT, PICTURE_REVERSE, PICTURE_FLIP, MEMORY_RECALL
	}

	public void setCameraPower(boolean on) {

		if (on) {
			commands.put(ViscaCommandName.POWER, new ViscaCommand(new int[] { 0x80 | cameraAddress, 0x01, 0x04, 0x00, 0x02, 0xFF }, ViscaCommandName.POWER));
			cameraBusy = true;
		} else {
			commands.put(ViscaCommandName.POWER, new ViscaCommand(new int[] { 0x80 | cameraAddress, 0x01, 0x04, 0x00, 0x03, 0xFF }, ViscaCommandName.POWER));
		}
	}

	public void setPictureReverse(boolean on) {

		if (on) {
			commands.put(ViscaCommandName.PICTURE_REVERSE, new ViscaCommand(new int[] { 0x80 | cameraAddress, 0x01, 0x04, 0x61, 0x02, 0xFF }, ViscaCommandName.PICTURE_REVERSE));
		} else {
			commands.put(ViscaCommandName.PICTURE_REVERSE, new ViscaCommand(new int[] { 0x80 | cameraAddress, 0x01, 0x04, 0x61, 0x03, 0xFF }, ViscaCommandName.PICTURE_REVERSE));
		}
	}

	public void setPictureFlip(boolean on) {

		if (on) {
			commands.put(ViscaCommandName.PICTURE_FLIP, new ViscaCommand(new int[] { 0x80 | cameraAddress, 0x01, 0x04, 0x66, 0x02, 0xFF }, ViscaCommandName.PICTURE_FLIP));
		} else {
			commands.put(ViscaCommandName.PICTURE_FLIP, new ViscaCommand(new int[] { 0x80 | cameraAddress, 0x01, 0x04, 0x66, 0x03, 0xFF }, ViscaCommandName.PICTURE_FLIP));
		}
	}

	public void recallMemoryPreset(int preset) {

		if (preset > 5 || preset < 0)
			throw new IllegalArgumentException("Preset out of range. Allowed: 0 to 5");

		commands.put(ViscaCommandName.MEMORY_RECALL, new ViscaCommand(new int[] { 0x80 | cameraAddress, 0x01, 0x04, 0x3F, 0x02, preset, 0xFF }, ViscaCommandName.MEMORY_RECALL));
	}

	/**
	 * Sets camera zoom ratio (1 to 25x)
	 * 
	 * @param multiplier
	 */
	public void setZoomRatio(int multiplier) {

		if (multiplier > 25 || multiplier < 1)
			throw new IllegalArgumentException("Zoom out of range. Allowed: 1 to 25x");

		int tableIndex = multiplier - 1;

		int[] commandBytes = new int[] { 0x80 | cameraAddress, 0x01, 0x04, 0x47, zoomTable[tableIndex][0], zoomTable[tableIndex][1], zoomTable[tableIndex][2], zoomTable[tableIndex][3], 0xFF };

		commands.put(ViscaCommandName.ZOOM_DIRECT, new ViscaCommand(commandBytes, ViscaCommandName.ZOOM_DIRECT));
	}

	public static void main(String[] args) throws Exception {
		// if(true) {
		// ByteArrayOutputStream bos = new ByteArrayOutputStream();
		// DataOutputStream dos = new DataOutputStream(bos);
		// for (int i=0; i<24; i++) {
		// for(int a=0; a<4; a++) {
		// dos.write(zoomTable[i][a]);
		// }
		// }
		// ByteArrayInputStream bis = new
		// ByteArrayInputStream(bos.toByteArray());
		// DataInputStream dis = new DataInputStream(bis);
		// for (int i=0; i<24; i++) {
		// System.out.println(i + "\t" + dis.readInt());
		// }
		// return;
		// }

		ViscaCameraGateway instance = new ViscaCameraGateway();
		instance.activate();

		System.out.println("Sleeping.");
		Thread.sleep(3000);
		System.out.println("Turning camera back on");
		instance.setCameraPower(true);
		while (instance.isCameraBusy()) {
			Thread.sleep(100);
		}
		for (int i = 1; i < 26; i++) {
			while (instance.isCameraBusy()) {
				Thread.sleep(100);
				System.out.println("Waiting for camera");
			}
			System.out.println("Zooming to " + i + "x");
			instance.setZoomRatio(i);
			Thread.sleep(100);
		}

		System.out.println("Sleeping.");
		Thread.sleep(5000);
		for (int i = 25; i > 0; i--) {
			while (instance.isCameraBusy()) {
				Thread.sleep(100);
				System.out.println("Waiting for camera");
			}
			System.out.println("Zooming to " + i + "x");
			instance.setZoomRatio(i);
			Thread.sleep(100);
		}

		System.out.println("Sleeping.");
		Thread.sleep(3000);
		System.out.println("Inverting image.");
		instance.setPictureReverse(true);
		System.out.println("Sleeping.");
		Thread.sleep(3000);
		System.out.println("Deinverting image.");
		instance.setPictureReverse(false);
		System.out.println("Sleeping.");
		Thread.sleep(3000);
		System.out.println("Flipping image.");
		instance.setPictureFlip(true);
		System.out.println("Sleeping.");
		Thread.sleep(3000);
		System.out.println("Deflipping image.");
		instance.setPictureFlip(false);
		System.out.println("Sleeping.");
		Thread.sleep(3000);
		System.out.println("Recalling preset 1.");
		instance.recallMemoryPreset(1);
		System.out.println("Sleeping.");
		Thread.sleep(3000);
		System.out.println("Recalling preset 0.");
		instance.recallMemoryPreset(0);
		System.out.println("Sleeping.");
		Thread.sleep(3000);
		instance.deactivate();
	}
}
