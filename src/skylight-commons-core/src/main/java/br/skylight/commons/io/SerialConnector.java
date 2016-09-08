package br.skylight.commons.io;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.logging.Logger;

import br.skylight.commons.JVMHelper;
import br.skylight.commons.JVMHelper.OS;

public class SerialConnector {

	private static final Logger logger = Logger.getLogger(SerialConnector.class.getName());
	public static final int DEFAULT_TIMEOUT = 2000;
	
	/**
	 * Opens a serial port according to parameters.
	 * If this is run in a Linux OS, it will use stty commands to setup serial port and connect
	 * to the device directly using a FileInputStream/FileOutputStream.
	 * If not in Linux, RXTX will be used. In Linux/ARM systems, 100% of CPU was detected using RXTX.
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public static synchronized SerialConnection openPort(SerialConnectionParams params) throws IOException {
		//don't use rxtx for connecting to serial port in Linux. Use a stream redirector
		if(JVMHelper.getCurrentOS().equals(OS.UNKNOWN)) {
			logger.info("Using tty2stdio for connecting to serial port " + params.getSerialPort() + "@" + params.getBaudRate());
			Process p = Runtime.getRuntime().exec("/Skylight/uav/tty2stdio " + params.getSerialPort() + " " + params.getDataBits() + " " + (params.getParity()==SerialPort.PARITY_NONE?"N":params.getParity()) + " " + params.getStopBits());
			System.out.println("/Skylight/uav/tty2stdio " + params.getSerialPort() + " " + params.getDataBits() + " " + (params.getParity()==SerialPort.PARITY_NONE?"N":params.getParity()) + " " + params.getStopBits());
			return new ProcessSerialConnection(p, params);
			
		//use rxtx library for connecting to serial port
		} else {
			logger.info("Using RXTX for connecting to serial port '"+ params.getSerialPort() +"@"+ params.getBaudRate() +"'");
			try {
				CommPortIdentifier com = CommPortIdentifier.getPortIdentifier(params.getSerialPort());
				CommPort thePort = com.open("SkylightSerialPortConnector", DEFAULT_TIMEOUT);
				int portType = com.getPortType();
				if(portType!=CommPortIdentifier.PORT_SERIAL) {
					throw new IllegalArgumentException(params.getSerialPort() + " must be a serial port. portType=" + portType);
				}
				SerialPort sp = (SerialPort) thePort;
//				sp.removeEventListener();
		//		sp.disableReceiveTimeout();
		//		sp.disableReceiveThreshold();
		//		sp.disableReceiveFraming();
//				sp.enableReceiveTimeout(120000);
				sp.disableReceiveTimeout();
				sp.setInputBufferSize(20000);
				sp.setOutputBufferSize(20000);
				sp.setFlowControlMode((int)params.getFlowControlMode().getData());
				sp.setSerialPortParams(params.getBaudRate(), params.getDataBits(), params.getStopBits(), params.getParity());
				return new RXTXSerialConnection(sp);
			} catch (UnsupportedCommOperationException e) {
				throw new IOException(e);
			} catch (NoSuchPortException e) {
				throw new IOException(e);
			} catch (PortInUseException e) {
				throw new IOException(e);
			}
		}
	}

}
