package br.skylight.commons.infra;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import br.skylight.commons.dli.services.ByteArrayInputStream2;
import br.skylight.commons.dli.services.ByteArrayOutputStream2;

public class IOHelper {

	private static final Logger logger = Logger.getLogger(IOHelper.class.getName());

	public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private static final DateFormat tf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

	// input and output streams will share the same buffer
	private static final byte[] COPY_BUFFER = new byte[500000];
	private static final ByteArrayInputStream2 COPY_DIS_IS = new ByteArrayInputStream2(COPY_BUFFER);
	private static final DataInputStream COPY_DIS = new DataInputStream(COPY_DIS_IS);
	private static final ByteArrayOutputStream2 COPY_DOS_OS = new ByteArrayOutputStream2(COPY_BUFFER);
	private static final DataOutputStream COPY_DOS = new DataOutputStream(COPY_DOS_OS);
	
	private static Lock copyStateLock = new ReentrantLock();
	private static Lock resolveFileLock = new ReentrantLock();
	private static Lock resolveDirLock = new ReentrantLock();

	public static void readInputStreamIntoOutputStream(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[256];
		int length = -1;
		while ((length = is.read(buffer)) > 0) {
			os.write(buffer, 0, length);
		}
	}

	public static void writeByteArray(byte[] data, DataOutput out) throws IOException {
		if (data != null) {
			out.writeInt(data.length);
			out.write(data);
		} else {
			out.writeInt(-1);
		}
	}

	public static byte[] readByteArray(DataInput in) throws IOException {
		int l = in.readInt();// array length
		if (l >= 0) {
			byte[] result = new byte[l];
			in.readFully(result);
			return result;
		} else {
			return null;
		}
	}

	public static <K extends SerializableState, V extends SerializableState> void readMap(DataInputStream in, Class<K> keyClass, Class<V> valueClass, Map<K, V> targetMap) throws IOException {
		targetMap.clear();
		int s = in.readInt();// map size
		for (int i = 0; i < s; i++) {
			try {
				// key
				K k = keyClass.newInstance();
				SerializableState kk = (SerializableState) k;
				kk.readState(in);

				// value
				V v = valueClass.newInstance();
				SerializableState vv = (SerializableState) v;
				vv.readState(in);

				targetMap.put(k, v);
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}

	public static <K extends SerializableState, V extends SerializableState> void writeMap(DataOutputStream out, Class<K> keyClass, Class<V> valueClass, Map<K, V> sourceMap) throws IOException {
		out.writeInt(sourceMap.size());// map size
		for (Entry<K,V> ev : sourceMap.entrySet()) {
			writeState(ev.getKey(), out);
			writeState(ev.getValue(), out);
		}
	}

	public static <T extends SerializableState> void readArrayList(DataInputStream in, Class<T> beanClass, List<T> targetList) throws IOException {
		targetList.clear();
		int l = in.readInt();// list length
		for (int i = 0; i < l; i++) {
			try {
				T o = beanClass.newInstance();
				SerializableState ss = (SerializableState) o;
				ss.readState(in);
				targetList.add(o);
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}

	public static void writeArrayList(DataOutputStream out, List<? extends SerializableState> list) throws IOException {
		int l = list.size();
		out.writeInt(l);
		for (SerializableState o : list) {
			SerializableState ss = (SerializableState) o;
			ss.writeState(out);
		}
	}

	public static File resolveFile(File dir, String fileName) throws IOException {
		try {
			resolveFileLock.lock();
			File f = new File(resolveDir(dir), fileName);
			if (!f.exists()) {
				f.createNewFile();
			}
			return f;
		} finally {
			resolveFileLock.unlock();
		}
	}

	public static File resolveDir(File baseDir, String dirName) {
		File d = new File(baseDir, dirName);
		return resolveDir(d);
	}

	public static File resolveDir(File d) {
		try {
			resolveDirLock.lock();
			if (!d.exists()) {
				d.mkdirs();
			}
			return d;
		} finally {
			resolveDirLock.unlock();
		}
	}

	/**
	 * Writes state to file. If state is an extension of ExtendedSerializableState, it will
	 * use the writeStateExtended(..) method to save contents 
	 */
	public static void writeStateToFile(SerializableState state, File file) throws IOException {
		if(file.exists()){
			file.delete();
		}
		FileOutputStream fos = new FileOutputStream(file);
		try {
			CRC32 crc = new CRC32();
			CheckedOutputStream co = new CheckedOutputStream(fos, crc);
			DataOutputStream dos = new DataOutputStream(co);
			dos.writeUTF(state.getClass().getName());
			if(state instanceof ExtendedSerializableState) {
				((ExtendedSerializableState)state).writeStateExtended(dos);
			} else {
				state.writeState(dos);
			}
			long c = crc.getValue();
			dos.writeLong(c);// CRC
			fos.flush();
		} catch (Exception e) {
			throw new IOException("Problem writing state to file '" + file + "'", e);
		} finally {
			fos.close();
		}
	}

	/**
	 * Reads state from file. If baseClass is an extension of ExtendedSerializableState, it will
	 * use the readStateExtended(..) method to load contents 
	 */
	public static <T extends SerializableState> T readStateFromFile(File file, Class<T> baseClass) throws IOException {
		if(file.exists()) {
			FileInputStream fis = new FileInputStream(file);
			try {
				CRC32 crc = new CRC32();
				CheckedInputStream ci = new CheckedInputStream(fis, crc);
				DataInputStream dis = new DataInputStream(ci);
				if (fis.available() > 0) {
					String className = dis.readUTF();
					T t = (T)Class.forName(className).newInstance();
					if(t instanceof ExtendedSerializableState) {
						((ExtendedSerializableState)t).readStateExtended(dis);
					} else {
						t.readState(dis);
					}
					long c = crc.getValue();
					long cs = dis.readLong();
					if (cs != c) {
						throw new IOException("File contents CRC doesn't match. streamCRC=" + cs + "; calculatedCRC=" + c);
					} else {
						return t;
					}
				} else {
					throw new IOException("Cannot read state from file '" + file + "'. Insufficient data");
				}
			} catch (Exception e) {
				throw new IOException("Problem reading state from file '" + file + "'", e);
			} finally {
				fis.close();
			}
		} else {
			return null;
		}
	}

	public static void saveJPEGImage(BufferedImage image, File file) throws IOException {
		// Find a jpeg writer
		ImageWriter writer = null;
		Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
		if (iter.hasNext()) {
			writer = (ImageWriter) iter.next();
		}

		// Prepare output file
		ImageOutputStream ios = ImageIO.createImageOutputStream(file);
		writer.setOutput(ios);

		// Set the compression quality
		ImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
		iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwparam.setCompressionQuality(0.9F);

		// Write the image
		IIOImage img = new IIOImage(image, null, null);
		writer.write(null, img, iwparam);

		// Cleanup
		ios.flush();
		writer.dispose();
		ios.close();
	}

	public static byte[] add(byte byte1, byte byte2) {
		return new byte[] { byte1, byte2 };
	}

	public static byte[] add(byte[] array, byte addByte) {
		byte[] result = new byte[array.length + 1];
		System.arraycopy(array, 0, result, 0, array.length);
		result[array.length] = addByte;
		return result;
	}

	public static byte[] add(byte[] array1, byte[] array2) {
		byte[] result = new byte[array1.length + array2.length];
		System.arraycopy(array1, 0, result, 0, array1.length);
		System.arraycopy(array2, 0, result, array1.length, array2.length);
		return result;
	}

	public static void readFully(InputStream is, byte[] buffer, int timeout, boolean failIfNotFull) throws IOException, TimeoutException {
		long st = System.currentTimeMillis();
		int i = 0;
		while ((System.currentTimeMillis() - st) < timeout && i < buffer.length) {
			if (is.available() > 0) {
				int b = is.read();
				if (b == -1) {
					throw new EOFException();
				}
				buffer[i++] = (byte) b;
				// System.out.println(">" + byteToHex((byte)b));
			} else {
				// System.out.println(">NO DATA");
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if (failIfNotFull && i < buffer.length) {
			throw new TimeoutException("Timeout reading bytes to fill buffer");
		}
	}

	public static boolean getBit(byte b, byte pos) {
		return ((b & (byte) (1 << pos)) != 0);
	}

	public static byte setBit(byte b, byte pos, boolean newBitValue) {
		byte mask = (byte) (1 << pos);
		if (newBitValue) {
			return (byte) (b | mask);
		} else {
			return (byte) (b & ~mask);
		}
	}

	public static boolean getBit(int b, byte pos) {
		return ((b & (int) (1 << pos)) != 0);
	}

	public static int setBit(int b, byte pos, boolean newBitValue) {
		int mask = (int) (1 << pos);
		if (newBitValue) {
			return (int) (b | mask);
		} else {
			return (int) (b & ~mask);
		}
	}

	public static void assertEquals(byte value, byte reference) {
		if (value != reference) {
			throw new AssertionError("Values not equals. Should be " + byteToHex(reference) + " but was " + byteToHex(value));
		}
	}

	public static void assertEquals(int value, int reference) {
		if (value != reference) {
			throw new AssertionError("Values not equal. Should be " + reference + " but was " + value);
		}
	}

	public static void assertEquals(byte[] value, byte[] reference) {
		for (int i = 0; i < reference.length; i++) {
			// System.out.println(byteToHex(value[i]) + "=" +
			// byteToHex(reference[i]) + " ");
			if (value[i] != reference[i]) {
				throw new AssertionError("Arrays are different at position " + i + ". Should be " + byteToHex(reference[i]) + " but was " + byteToHex(value[i]));
			}
		}
	}

	public static boolean startsWith(byte[] value, byte[] reference) {
		for (int i = 0; i < reference.length; i++) {
			if (value[i] != reference[i]) {
				return false;
			}
		}
		return true;
	}

	public static String charToHex(char c) {
		// Returns hex String representation of char c
		byte hi = (byte) (c >>> 8);
		byte lo = (byte) (c & 0xff);
		return byteToHex(hi) + byteToHex(lo);
	}

	public static String byteToHex(byte b) {
		// Returns hex String representation of byte b
		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
		return new String(array);
	}

	public static String bytesToHexString(byte[] params) {
		String result = "";
		for (byte b : params) {
			result += byteToHex(b) + " ";
		}
		return result;
	}

	public static void drainStream(InputStream is, int time) throws IOException {
		byte[] buffer = new byte[32];
		long s = System.currentTimeMillis();
		while ((System.currentTimeMillis() - s) < time) {
			if (is.available() > 0) {
				is.read(buffer);
			} else {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static long readUnsignedInt(DataInput in) throws IOException {
		long n = in.readInt();
		if(n<0) {
			n = n&0XFFFFFFFFL;
		}
		return n;
	}

	public static List<RankedNetworkInterface> getRankedNetworkInterfaces() {
		List<RankedNetworkInterface> result = new ArrayList<RankedNetworkInterface>();
		gatherAllNetworkInterfaces(null, result);
		Collections.sort(result);
		return result;
	}
	
	private static void gatherAllNetworkInterfaces(NetworkInterface rootNetworkInterface, List<RankedNetworkInterface> result) {
		try {
			Enumeration<NetworkInterface> nis;
			if(rootNetworkInterface==null) {
				nis = NetworkInterface.getNetworkInterfaces();
			} else {
				nis = rootNetworkInterface.getSubInterfaces();
			}
			
//			NetworkInterface r = null;
			if(nis!=null) {
				while (nis.hasMoreElements()) {
					int niScore = 0;
					NetworkInterface ni = nis.nextElement();
					if (ni.isUp()) {
						if (niScore < 1) {
							niScore = 1;
	//						r = ni;
						}
						if (ni.supportsMulticast()) {
							if (niScore < 2) {
								niScore = 2;
	//							r = ni;
							}
							if (!ni.isLoopback()) {
								if (niScore < 3) {
									niScore = 3;
	//								r = ni;
								}
								if (ni.getInetAddresses().hasMoreElements()) {
									if (niScore < 4) {
										niScore = 4;
	//									r = ni;
									}
									Enumeration<InetAddress> addrs = ni.getInetAddresses();
									boolean hasPrivateAddress = false;
									boolean hasLoopback = false;
									boolean has10Net = false;
									while(addrs.hasMoreElements()) {
										InetAddress addr = addrs.nextElement();
										if (addr.getHostAddress().startsWith("172.16") || addr.getHostAddress().startsWith("192.168") || addr.getHostAddress().startsWith("10")) {
											hasPrivateAddress = true;
										}
										if(addr.getHostAddress().startsWith("10")) {
											has10Net = true;
										}
										if(addr.getHostAddress().startsWith("127")) {
											hasLoopback = true;
										}
									}
									if(hasLoopback) {
										if (niScore < 5) {
											niScore = 5;
										}												
									} else {
										if(hasPrivateAddress) {
											if (niScore < 6) {
												niScore = 6;
		//										r = ni;
												if(has10Net) {
													if (niScore < 7) {
														niScore = 7;
													}												
												}
											}
										}
									}
								}
							}
						}
					}
					result.add(new RankedNetworkInterface(niScore, ni));
					gatherAllNetworkInterfaces(ni, result);
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static NetworkInterface getDefaultNetworkInterface() {
		List<RankedNetworkInterface> r = getRankedNetworkInterfaces();
		if(r.size()>0) {
			return r.get(0).getNetworkInterface();
		} else {
			return null;
		}
	}

	// public static NetworkInterface getNetworkInterfaceByAddress(byte[]
	// interfaceAddress) throws SocketException {
	// String networkHardwareAddress = new String(interfaceAddress,
	// CHARSET_UTF8);
	// Enumeration<NetworkInterface> nis =
	// NetworkInterface.getNetworkInterfaces();
	// while(nis.hasMoreElements()) {
	// NetworkInterface r = nis.nextElement();
	// String nia = new String(r.getHardwareAddress(), CHARSET_UTF8);
	// if(nia.equals(networkHardwareAddress)) {
	// return r;
	// }
	// }
	// return null;
	// }

	public static NetworkInterface getNetworkInterfaceByName(String interfaceName) throws SocketException {
		Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
		while (nis.hasMoreElements()) {
			NetworkInterface r = nis.nextElement();
			if (r.getName().equals(interfaceName)) {
				return r;
			}
		}
		return null;
	}
	
	public static NetworkInterface getLoopbackInterface() {
		try {
			NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName("127.0.0.1"));
			if(ni==null) {
				ni = NetworkInterface.getByInetAddress(InetAddress.getByName("localhost"));
			}
			return ni;
		} catch (SocketException e) {
			throw new RuntimeException(e);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		//		NetworkInterface ni = getNetworkInterfaceByName("Microsoft Loopback Adapter");
//		if(ni==null) {
//			ni = getNetworkInterfaceByName("lo");
//		}
//		return ni;
	}

	public static int parseUnsignedHex(String hex) {
		long value = Long.parseLong(hex, 16);
		return (int) (value & 0xFFFFFFFF);
	}

	public static String parseToUnsignedHex(int value) {
		return Integer.toHexString(value);
	}

	public static void copyState(SerializableState target, SerializableState source) {
		try {
			copyStateLock.lock();
			if(!target.getClass().equals(source.getClass())) {
				throw new IllegalArgumentException("Class type for source and target must be equal");
			}
			COPY_DOS_OS.reset();
			source.writeState(COPY_DOS);
			COPY_DIS_IS.reset();
			target.readState(COPY_DIS);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			copyStateLock.unlock();
		}
	}

	public static void copyStateExtended(ExtendedSerializableState target, ExtendedSerializableState source) {
		try {
			copyStateLock.lock();
			if(!target.getClass().equals(source.getClass())) {
				throw new IllegalArgumentException("Class type for source and target must be equal");
			}
			COPY_DOS_OS.reset();
			source.writeStateExtended(COPY_DOS);
			COPY_DIS_IS.reset();
			target.readStateExtended(COPY_DIS);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			copyStateLock.unlock();
		}
	}
	
	public static File resolveFile(File baseDir, String subject, String fileSufix, boolean useDailyDirs) throws IOException {
		// subject = subject.replaceAll("\\s", "");
		File d = resolveDir(baseDir, subject, useDailyDirs);
		String fn = subject + " " + tf.format(new Date()) + fileSufix;
		return IOHelper.resolveFile(d, fn);
	}

	private static File resolveDir(File baseDir, String subject, boolean useDailyDirs) {
		if (subject == null || subject.trim().length() == 0) {
			subject = "noname";
		}
		File f = resolveDir(baseDir, subject);
		if (!useDailyDirs) {
			return f;
		} else {
			return new File(f, df.format(new Date()));
		}
	}

	public static void close(OutputStream os) {
		if(os!=null) {
			try {
				os.close();
			} catch (IOException e) {
				//do nothing
			}
		}
	}

	public static void close(InputStream is) {
		if(is!=null) {
			try {
				is.close();
			} catch (IOException e) {
				//do nothing
			}
		}
	}
	
	public static void setFileModificationTime(File file, long time) throws IOException {
		if(!file.exists()) {
			if(!file.createNewFile()) {
				throw new IOException("Couldn't create touch file");
			}
		}
		if(!file.setLastModified(time)) {
			throw new IOException("Couldn't update file modification time");
		}
	}

	public static void writeState(SerializableState state, DataOutputStream out) throws IOException {
		out.writeBoolean(state!=null);
		if(state!=null) {
			state.writeState(out);
		} 
	}

	public static <T extends SerializableState> T readState(Class<T> stateClass, DataInputStream in) throws IOException {
		T t = null;
		if(in.readBoolean()) {
			try {
				t = stateClass.newInstance();
				t.readState(in);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return t;
	}
	
	public static void writeMapStateIntKey(Map<Integer,? extends SerializableState> map, DataOutputStream out) throws IOException {
		out.writeInt(map.size());
		for (Entry<Integer,? extends SerializableState> ec : map.entrySet()) {
			out.writeInt(ec.getKey());
			writeState(ec.getValue(), out);
		}
	}
	
	public static <T extends SerializableState> void readMapStateIntKey(Map<Integer,T> map, Class<T> stateClass, DataInputStream in) throws IOException {
		int s = in.readInt();
		for(int i=0; i<s; i++) {
			int k = in.readInt();
			T v = readState(stateClass, in);
			map.put(k,v);
		}
	}
	
	public static <T extends ExtendedSerializableState> T createCopyExtended(T source) {
		try {
			T m = (T)source.getClass().newInstance();
			IOHelper.copyStateExtended(m, source);
			return m;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static long calculateCRC(SerializableState serializableState) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(20000);
		CRC32 crc = new CRC32();
		CheckedOutputStream cos = new CheckedOutputStream(bos, crc);
		DataOutputStream dos = new DataOutputStream(cos);
		try {
			serializableState.writeState(dos);
		} catch (IOException e) {
			logger.warning("Exception on crc calculation. Returning 0.");
			e.printStackTrace();
			return 0;
		}
		return crc.getValue();
	}
	
	public static File resolveTempDir() throws IOException {
		File td = File.createTempFile("skylight", ".tmp");
		try {
			return td.getParentFile();
		} finally {
			td.delete();
		}
	}
	
}
