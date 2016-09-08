package br.skylight.commons.io.dataterminal.ac4790;

import gnu.io.SerialPort;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import br.skylight.commons.infra.IOHelper;
import br.skylight.commons.infra.TimedBoolean;
import br.skylight.commons.io.FlowControlConfig;
import br.skylight.commons.io.SerialConnection;
import br.skylight.commons.io.SerialConnectionParams;
import br.skylight.commons.io.SerialConnector;
import br.skylight.commons.io.dataterminal.ac4790.AC4790OutputStream.TransmitterPower;

import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZInputStream;
import com.jcraft.jzlib.ZOutputStream;

public class AC4790Test {

	private static FlowControlConfig hardwareflow = new FlowControlConfig();
	static {
		hardwareflow.setRTSCTSIn(true);
		hardwareflow.setRTSCTSOut(true);
	}
	
	public static void main(String[] args) throws Exception {
		// testGetDestinationId();
		// testSimpleCommands();
		// testModemCommandsWhileSendingData();
		// testLoopbackClient();
		// testProbeReportWithoutModem();

		// GENERAL TESTS
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ZOutputStream zOut = new ZOutputStream(out, JZlib.Z_BEST_COMPRESSION);
		ObjectOutputStream objOut = new ObjectOutputStream(zOut);
		String hello = "Hello World!";
		objOut.writeObject(hello);
		zOut.close();

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		ZInputStream zIn = new ZInputStream(in);
		ObjectInputStream objIn = new ObjectInputStream(zIn);
		System.out.println(objIn.readObject());
	}

	private static void testGetDestinationId() throws Exception {
		SerialConnectionParams connectionParams = new SerialConnectionParams("COM4", 57600, SerialPort.DATABITS_8, hardwareflow, SerialPort.PARITY_NONE, SerialPort.STOPBITS_1);
		SerialConnection c = SerialConnector.openPort(connectionParams);
		final AC4790SerialConnection ac = new AC4790SerialConnection(c, null);

		Thread s = new Thread() {
			public void run() {
				while (true) {
					try {
						ac.getOutputStream().write('f');
						System.out.print(".");
						Thread.sleep(10);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			};
		};
		s.start();

		Thread r = new Thread() {
			public void run() {
				while (true) {
					try {
						System.out.print((char) ac.getInputStream().read());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			};
		};
		r.start();

		Thread.sleep(3000);
		while (true) {
			System.out.println("Getting destination...");
			ac.getOutputStream().enterCommandMode();
			System.out.println(IOHelper.bytesToHexString(ac.getOutputStream().getDestinationID()));
			ac.getOutputStream().exitCommandMode();
			Thread.sleep(500);
		}
	}

	private static void testProbeReportWithoutModem() throws IOException {
		Thread sender = new Thread() {
			int i = 'a';

			@Override
			public void run() {
				try {
					SerialConnectionParams connectionParams = new SerialConnectionParams("COM1", 57600, SerialPort.DATABITS_8, hardwareflow, SerialPort.PARITY_NONE, SerialPort.STOPBITS_1);
					SerialConnection c = SerialConnector.openPort(connectionParams);
					AC4790SerialConnection ac = new AC4790SerialConnection(c, null);
					final AC4790OutputStream aos = ac.getOutputStream();

					System.out.println(">> Writing lots of data along with some probe reports");
					TimedBoolean t = new TimedBoolean(1000);
					long e = System.currentTimeMillis();
					while ((System.currentTimeMillis() - e) < 100000) {
						char a = (char) i++;
						long s = System.currentTimeMillis();
						aos.write(a);
						long time = System.currentTimeMillis() - s;
						if (time > 16) {
							System.out.println("DELAY TO SEND! " + time);
						}
						// System.out.print(a+"|");
						if (i > 'z')
							i = 'a';

						// send probe report
						if (t.checkTrue()) {
							aos.write(new byte[] { (byte) 0x86, (byte) (Math.random() * 127), 0x01, 0x02, 0x03 });
						}
					}
					System.out.println(">> Stopped sending data to RF");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};

		Thread receiver = new Thread() {
			int expectedRead = 'a';

			@Override
			public void run() {
				try {
					SerialConnectionParams connectionParams = new SerialConnectionParams("COM2", 57600, SerialPort.DATABITS_8, hardwareflow, SerialPort.PARITY_NONE, SerialPort.STOPBITS_1);
					SerialConnection c = SerialConnector.openPort(connectionParams);
					AC4790SerialConnection ac = new AC4790SerialConnection(c, null);
//					ac.setInputStreamListener(new AC4790StreamListener() {
//						@Override
//						public void onProbeReportReceived(byte[] remoteMAC, int rssi) {
//							System.out.println(">>> Probe report received. rssi=" + rssi + "; remoteMAC=" + IOHelper.bytesToHexString(remoteMAC));
//						}
//					});
					final InputStream ais = ac.getInputStream();
					System.out.println(">> Reading data from RF");
					while (true) {
						int i = ais.read();
						if (i != -1) {
							if (i != expectedRead) {
								System.out.println("Expected '" + (char) expectedRead + "' but read '" + (char) i + "'");
							}
							if (expectedRead >= 'z') {
								expectedRead = 'a';
							} else {
								expectedRead = i + 1;
							}
							// System.out.print(IOHelper.byteToHex((byte)i)+". ");
						} else {
							return;
						}
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};

		sender.start();
		receiver.start();
	}

	private static void testSimpleCommands() throws IOException {
		// prepare connection
		SerialConnectionParams connectionParams = new SerialConnectionParams("COM1", 57600, SerialPort.DATABITS_8, new FlowControlConfig(), SerialPort.PARITY_NONE, SerialPort.STOPBITS_1);
		SerialConnection c = SerialConnector.openPort(connectionParams);

		final InputStream is = c.getInputStream();
		final OutputStream os = c.getOutputStream();

		if (true) {
			os.write(AC4790OutputStream.COMMAND_ENTER_AT_MODE);
			os.write(AC4790OutputStream.COMMAND_EXIT_AT_MODE);
		}
	}

	private static void testModemCommandsWhileSendingData() throws Exception {
		// prepare connection
		SerialConnectionParams connectionParams = new SerialConnectionParams("COM4", 57600, SerialPort.DATABITS_8, new FlowControlConfig(), SerialPort.PARITY_NONE, SerialPort.STOPBITS_1);
		SerialConnection c = SerialConnector.openPort(connectionParams);

		AC4790SerialConnection ac = new AC4790SerialConnection(c, null);
		final InputStream ais = ac.getInputStream();
		final AC4790OutputStream aos = ac.getOutputStream();

		// InputStream ais = new FilterInputStream(c.getInputStream()) {
		// public int read() throws IOException {
		// int b = super.read();
		// System.out.print(" R" + IOHelper.byteToHex((byte)b));
		// return b;
		// }
		// public int read(byte[] b) throws IOException {
		// int i = super.read(b);
		// byte[] a = new byte[i];
		// System.arraycopy(b, 0, a, 0, i);
		// System.out.print(" R" + IOHelper.bytesToHexString(a));
		// return i;
		// }
		// public int read(byte[] b, int off, int len) throws IOException {
		// int i = super.read(b, off, len);
		// if(i>0) {
		// byte[] a = new byte[i];
		// System.arraycopy(b, off, a, 0, i);
		// System.out.print(" R" + IOHelper.bytesToHexString(a));
		// }
		// return i;
		// }
		// };

		// OutputStream aos = new FilterOutputStream(c.getOutputStream()) {
		// public void write(int b) throws IOException {
		// System.out.print(" W" + IOHelper.byteToHex((byte)b));
		// super.out.write(b);
		// }
		// public void write(byte[] b) throws IOException {
		// System.out.print(" W" + IOHelper.bytesToHexString(b));
		// super.out.write(b);
		// }
		// public void write(byte[] b, int off, int len) throws IOException {
		// System.out.print(" W" + IOHelper.bytesToHexString(b));
		// super.out.write(b, off, len);
		// }
		// };

		// SequenceStreamFilter stf = new SequenceStreamFilter(null, 0, true);
		// c.setInputStream(new FilteredInputStream<SequenceStreamFilter>(ais,
		// stf));
		// final FilteredInputStream<SequenceStreamFilter> is =
		// (FilteredInputStream<SequenceStreamFilter>) c.getInputStream();

//		ac.setInputStreamListener(new AC4790StreamListener() {
//			@Override
//			public void onProbeReportReceived(byte[] remoteMAC, int rssi) {
//				System.out.println(">>> Probe report received. rssi=" + rssi + "; remoteMAC=" + IOHelper.bytesToHexString(remoteMAC));
//			}
//		});

		// send data on another thread
		Thread t = new Thread(new Runnable() {
			int i = 'a';

			public void run() {
				try {
					System.out.println(">> Writing lots of data to RF");
					long e = System.currentTimeMillis();
					while ((System.currentTimeMillis() - e) < 100000) {
						char a = (char) i++;
						long s = System.currentTimeMillis();
						aos.write(a);
						long time = System.currentTimeMillis() - s;
						if (time > 16) {
							System.out.println("DELAY TO SEND! " + time);
						}
						System.out.print(a + "|");
						if (i > 'z')
							i = 'a';
					}
					System.out.println(">> Stopped sending data to RF");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();

		// read data on another thread
		Thread r = new Thread(new Runnable() {
			int expectedRead = 'a';

			public void run() {
				try {
					System.out.println(">> Reading data from RF");
					while (true) {
						int i = ais.read();
						if (i != -1) {
							if (i != expectedRead) {
								System.out.println("Expected '" + (char) expectedRead + "' but read '" + (char) i + "'");
							}
							if (expectedRead >= 'z') {
								expectedRead = 'a';
							} else {
								expectedRead = i + 1;
							}
							System.out.print(IOHelper.byteToHex((byte) i) + ". ");
						} else {
							return;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		r.start();

		Thread.sleep(2000);

		// send modem commands while sending and reading rf modem data
		// System.out.println("Entering/exiting command mode");
		// os.acquireWriteLock(true);
		// System.out.println("ENTER COMMAND LOCK ACQUIRE 2");
		//		
		// Thread.sleep(10000);
		// os.getSuperOut().write(AC4790OutputStream.COMMAND_ENTER_AT_MODE);
		// System.out.println("WROTE ENTER AT");
		// ais.read();
		// ais.read();
		// ais.read();
		// ais.read();
		// ais.read();
		// os.enterCommandMode();
		// os.exitCommandMode();

		System.out.println("Querying destination ID for 10s...");
		long start = System.currentTimeMillis();
		while ((System.currentTimeMillis() - start) < 10000) {
			try {
				// os.write("abcdefghijklmnopqrstuvwxyz".getBytes());
				byte[] d = aos.getDestinationID();
				System.out.println("Destination=" + IOHelper.byteToHex(d[0]) + " " + IOHelper.byteToHex(d[1]) + " " + IOHelper.byteToHex(d[2]));
				// os.write("012345678901234567890123456789".getBytes());
				Thread.sleep(300);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Setup operations...");
		start = System.currentTimeMillis();
		try {
			System.out.println("======");
			System.out.println("Entering probe mode...");
			aos.setProbeMode(true);
			System.out.println("Exiting probe mode...");
			aos.setProbeMode(false);
			// if(true) return;
			System.out.println("Setting retries to 4...");
			aos.storeDeviceTransmitRetries(4);
			System.out.println("OK");
			System.out.println("Setting to half duplex...");
			aos.storeDeviceTransmissionMode(false);
			System.out.println("OK");
			System.out.println("Setting to not use encryption...");
			aos.storeDeviceUseEncryption(false);
			System.out.println("OK");
			System.out.println("Setting tx to half power...");
			aos.storeAndSwitchDeviceTxPower(TransmitterPower.QUARTER_POWER);
			System.out.println("OK");
			System.out.println("======");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void testLoopbackClient() throws InterruptedException, IOException {
		// prepare connection
		SerialConnectionParams connectionParams = new SerialConnectionParams("COM1", 57600, SerialPort.DATABITS_8, new FlowControlConfig(), SerialPort.PARITY_NONE, SerialPort.STOPBITS_1);
		SerialConnection c = SerialConnector.openPort(connectionParams);

		AC4790SerialConnection ac = new AC4790SerialConnection(c, null);
		final InputStream is = ac.getInputStream();
		final OutputStream os = ac.getOutputStream();

		// send data to RF
		System.out.println("Sending RF data for 60s...");
		Thread t = new Thread(new Runnable() {
			public void run() {
				long start = System.currentTimeMillis();
				try {
					// BufferedOutputStream bos = new BufferedOutputStream(os);
					// BufferedInputStream bis = new BufferedInputStream(is);
					while ((System.currentTimeMillis() - start) < 60000) {
						// send data
						System.out.println("Sending bytes...");
						byte[] testBytes = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".getBytes();
						for (int i = 0; i < testBytes.length; i++) {
							os.write(testBytes[i]);
							System.out.print(" " + (char) testBytes[i]);
							// Thread.sleep(100);
						}

						// read loopback data
						// Thread.sleep(1000);
						System.out.println("\nReading bytes...");
						for (int i = 0; i < testBytes.length; i++) {
							int rs = is.read();
							System.out.print(" " + (char) rs);
							if (rs != testBytes[i]) {
								throw new AssertionError("Different bytes. expected=" + (char) testBytes[i] + "; actual=" + (char) rs);
							}
							// Thread.sleep(100);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();

		System.out.println("Waiting 5s...");
		Thread.sleep(5000);
	}

}
