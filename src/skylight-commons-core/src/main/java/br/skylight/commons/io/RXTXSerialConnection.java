package br.skylight.commons.io;

import gnu.io.SerialPort;

import java.io.IOException;

import br.skylight.commons.infra.IOHelper;

public class RXTXSerialConnection extends SerialConnection {

	private SerialPort serialPort;
	
	public RXTXSerialConnection(SerialPort serialPort) throws IOException {
		super(serialPort.getInputStream(), serialPort.getOutputStream(), new SerialConnectionParams(serialPort.getName(), serialPort.getBaudRate(), serialPort.getDataBits(), getFlowControlMode(serialPort), serialPort.getParity(), serialPort.getStopBits()));
		this.serialPort = serialPort;
	}
	
	private static FlowControlConfig getFlowControlMode(SerialPort serialPort) {
		FlowControlConfig fc = new FlowControlConfig();
		fc.setBit(0, IOHelper.getBit(serialPort.getFlowControlMode(),(byte)0));
		fc.setBit(1, IOHelper.getBit(serialPort.getFlowControlMode(),(byte)1));
		fc.setBit(2, IOHelper.getBit(serialPort.getFlowControlMode(),(byte)2));
		fc.setBit(3, IOHelper.getBit(serialPort.getFlowControlMode(),(byte)3));
		return fc;
	}

	public SerialPort getSerialPort() {
		return serialPort;
	}
	
	@Override
	public void close() throws IOException {
		serialPort.close();
		super.close();
	}

}
