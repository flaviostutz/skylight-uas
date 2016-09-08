package br.skylight.commons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import br.skylight.commons.infra.IOHelper;

public class JVMHelper {

	private static final Logger logger = Logger.getLogger(JVMHelper.class.getName());
	
	public enum OS {
		LINUX,WINXP,WIN7,MACOS,UNKNOWN;
	}
	
	public static Process startJVM(Class mainClass, boolean mergeJVMOutputs, boolean killLaunchedJVMOnShutdown, String... jvmArgs) throws Exception {
		String separator = System.getProperty("file.separator");
		String classpath = System.getProperty("java.class.path");
		String path = System.getProperty("java.home") + separator + "bin" + separator + "java";

		// build arguments
		List<String> args = new ArrayList<String>();
		args.add(path);
		for (String arg : jvmArgs) {
			args.add(arg);
		}
		args.add("-cp");
		args.add(classpath);
		args.add(mainClass.getName());

		ProcessBuilder processBuilder = new ProcessBuilder(args.toArray(new String[0]));
		final Process p = processBuilder.start();
		if (mergeJVMOutputs) {
			System.out.println("==Merging this JVM output with current JVM. Logs will be mixed.==");
			Thread t1 = new Thread() {
				public void run() {
					try {
						IOHelper.readInputStreamIntoOutputStream(p.getInputStream(), new OutputStream() {
							public void write(int b) throws IOException {
								System.out.print((char) b);
							}
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			t1.start();

			Thread t2 = new Thread() {
				public void run() {
					try {
						IOHelper.readInputStreamIntoOutputStream(p.getErrorStream(), new OutputStream() {
							public void write(int b) throws IOException {
								System.err.print((char) b);
							}
						});
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			t2.start();
		}
		if (killLaunchedJVMOnShutdown) {
			Thread t3 = new Thread() {
				public void run() {
					System.out.println("==Killing another JVM launched from this JVM==");
					p.destroy();
				}
			};
			Runtime.getRuntime().addShutdownHook(t3);
		}
		return p;
	}

	public static boolean isTrue(Properties prop, String property) {
		if (prop.getProperty(property) != null) {
			return Boolean.parseBoolean(prop.getProperty(property));
		}
		return false;
	}

	public static void shutdownOS() {
		try {
			String shutdownCommand = "";
			if (getCurrentOS().equals(OS.LINUX) || getCurrentOS().equals(OS.MACOS)) {
				shutdownCommand = "shutdown -h now";
			} else if (getCurrentOS().equals(OS.WINXP)) {
				shutdownCommand = "shutdown -s -f -c \"Commanded shutdown from Skylight UAV\"";
			} else if (getCurrentOS().equals(OS.WIN7)) {
				shutdownCommand = "shutdown /s /f /c \"Commanded shutdown from Skylight UAV\"";
			} else {
				logger.warning("A computer shutdown was commanded but this OS is not supported for shutdown. os=" + getCurrentOS());
			}
			Runtime.getRuntime().exec(shutdownCommand);
		} catch (IOException ex) {
			logger.throwing(null,null,ex);
			ex.printStackTrace();
		}
	}

	public static void rebootOS() {
		try {
			String shutdownCommand = "";
			if (getCurrentOS().equals(OS.LINUX) || getCurrentOS().equals(OS.MACOS)) {
				shutdownCommand = "shutdown -r now";
			} else if (getCurrentOS().equals(OS.WINXP)) {
				shutdownCommand = "shutdown -r -f -c \"Reboot commanded from Skylight UAV\"";
			} else if (getCurrentOS().equals(OS.WIN7)) {
				shutdownCommand = "shutdown /r /f /c \"Reboot commanded from Skylight UAV\"";
			} else {
				logger.warning("A computer reboot was commanded but this OS is not supported for this operation. os=" + getCurrentOS());
			}
			Runtime.getRuntime().exec(shutdownCommand);
		} catch (IOException ex) {
			logger.throwing(null,null,ex);
			ex.printStackTrace();
		}
	}
	
	public static OS getCurrentOS() {
		String os = System.getProperty("os.name");
		if(os.equalsIgnoreCase("linux")) {
			return OS.LINUX;
		} else if(os.equalsIgnoreCase("windows xp")) {
			return OS.WINXP;
		} else if(os.equalsIgnoreCase("windows 7")) {
			return OS.WIN7;
		} else if(os.equalsIgnoreCase("mac os x")) {
			return OS.MACOS;
		} else {
			return OS.UNKNOWN;
		}
	}

	public static void setDateTime(long time) throws IOException {
		if(getCurrentOS().equals(OS.LINUX)) {
			SimpleDateFormat sdf = new SimpleDateFormat("dMkmYYYY.s");
			//setup OS clock
			Runtime.getRuntime().exec("date " + sdf.format(new Date(time)));
			//store current datetime in BIOS
			Runtime.getRuntime().exec("setclock");
		} else {
			logger.info("Cannot setup clock in this OS. currentTime=" + new Date() + "; os=" + getCurrentOS());
		}
	}

	public static String getExceptionString(Throwable t, int stringLimit) {
		if(t!=null) {
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				PrintWriter pw = new PrintWriter(bos);
				t.printStackTrace(pw);
				bos.close();
				String s = bos.toString();
				if(s.length()>stringLimit) {
					s = s.substring(0, stringLimit);
				}
				return s;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return "Exception is null";
		}
	}

}
