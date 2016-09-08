package br.skylight.commons.io;

import gnu.io.SerialPort;

import java.io.IOException;
import java.util.Properties;

public class SerialConnectionParams {

	private String serialPort;
	private int baudRate = 57600;
	private FlowControlConfig flowControlMode = new FlowControlConfig();
	private int dataBits = SerialPort.DATABITS_8;
	private int stopBits = SerialPort.STOPBITS_1;
	private int parity = SerialPort.PARITY_NONE;
	
	private SerialConnection connection;

	public SerialConnectionParams(String serialPort, int baudRate, int dataBits, FlowControlConfig flowControlMode, int parity, int stopBits) {
		this.baudRate = baudRate;
		this.dataBits = dataBits;
		this.flowControlMode = flowControlMode;
		this.parity = parity;
		this.serialPort = serialPort;
		this.stopBits = stopBits;
	}

	public SerialConnectionParams(String serialPort) {
		this.serialPort = serialPort;
	}
	
	public SerialConnectionParams(Properties propertiesFile, String propertiesPrefix) {
		loadFromProperties(propertiesFile, propertiesPrefix);
	}
	
	/**
	 * Loads serial parameters from properties file whose properties starts by 'prefix'
	 */
	public void loadFromProperties(Properties prop, String prefix) {
		serialPort = prop.getProperty(prefix + ".serialPort");
		baudRate = Integer.parseInt(prop.getProperty(prefix + ".baudRate", "115200"));
		dataBits = Integer.parseInt(prop.getProperty(prefix + ".dataBits", ""+SerialPort.DATABITS_8));
		boolean flowControlHardware = Boolean.parseBoolean(prop.getProperty(prefix + ".flowControlHardware", "false"));
		boolean flowControlSoftware = Boolean.parseBoolean(prop.getProperty(prefix + ".flowControlSoftware", "false"));
		flowControlMode.setRTSCTSIn(flowControlHardware);
		flowControlMode.setRTSCTSOut(flowControlHardware);
		flowControlMode.setXOnXOffIn(flowControlSoftware);
		flowControlMode.setXOnXOffOut(flowControlSoftware);
		String p = prop.getProperty(prefix + ".parity", "none");
		if(p.equalsIgnoreCase("even")) {
			parity = SerialPort.PARITY_EVEN;
		} else if(p.equalsIgnoreCase("odd")) {
			parity = SerialPort.PARITY_ODD;
		} else {
			parity = SerialPort.PARITY_NONE;
		}
		stopBits = Integer.parseInt(prop.getProperty(prefix + ".stopBits", ""+SerialPort.STOPBITS_1));
	}
	
	public synchronized SerialConnection resolveConnection() throws IOException {
		if(connection==null || !connection.isConnected()) {
			connection = SerialConnector.openPort(this);
		}
		return connection;
	}
	
	public void closeConnection() throws IOException {
		if(connection!=null) {
			connection.close();
		}
	}
	
	public String getSerialPort() {
		return serialPort;
	}
	public void setSerialPort(String serialPort) {
		this.serialPort = serialPort;
	}
	public int getBaudRate() {
		return baudRate;
	}
	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
	}
	public FlowControlConfig getFlowControlMode() {
		return flowControlMode;
	}
	public void setFlowControlConfig(FlowControlConfig flowControlMode) {
		this.flowControlMode = flowControlMode;
	}
	public int getDataBits() {
		return dataBits;
	}
	public void setDataBits(int dataBits) {
		this.dataBits = dataBits;
	}
	public int getStopBits() {
		return stopBits;
	}
	public void setStopBits(int stopBits) {
		this.stopBits = stopBits;
	}
	public int getParity() {
		return parity;
	}
	public void setParity(int parity) {
		this.parity = parity;
	}
	
	public boolean isValid() {
		return serialPort!=null && serialPort.trim().length()>0;
	}
	
	@Override
	public String toString() {
		return getSerialPort()+"@"+getBaudRate()+ ";flowControl=" + (getFlowControlMode().isRTSCTSIn()||getFlowControlMode().isRTSCTSOut()?"rtscts":(getFlowControlMode().isXOnXOffIn()||getFlowControlMode().isXOnXOffOut()?"xon/xoff":"none")) + ";stopBits="+getStopBits()+";parity="+getParity();
	}

}
