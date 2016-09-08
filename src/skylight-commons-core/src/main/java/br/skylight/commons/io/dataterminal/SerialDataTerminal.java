package br.skylight.commons.io.dataterminal;

import java.io.InputStream;
import java.io.OutputStream;

import br.skylight.commons.dli.datalink.DataLinkControlCommand;
import br.skylight.commons.dli.datalink.DataLinkSetupMessage;
import br.skylight.commons.dli.enums.DataLinkState;
import br.skylight.commons.dli.enums.DataTerminalType;
import br.skylight.commons.io.SerialConnection;
import br.skylight.commons.io.SerialConnectionParams;

public class SerialDataTerminal extends StreamDataTerminal {

	private SerialConnectionParams serialConnectionParams;
	protected SerialConnection connection;

	public SerialDataTerminal(SerialConnectionParams serialConnectionParams, DataTerminalType dataTerminalType, int dataLinkId, boolean compressOutputData, boolean uncompressInputData) {
		super(dataTerminalType, dataLinkId, compressOutputData, uncompressInputData);
		this.serialConnectionParams = serialConnectionParams;
	}

	@Override
	public void onActivate() throws Exception {
		connection = serialConnectionParams.resolveConnection();
		getDataLinkStatusReport().setDataLinkState(DataLinkState.TX_AND_RX);
		
		//FOR TESTING PURPOSES
//		BadInputStream badIs = new BadInputStream(connection.getInputStream());
//		badIs.setLossRate(0.001F);
//		badIs.setMaxSequenceBytesLoss(128);
//		badIs.setShortStopRate(0.002F);
//		badIs.setMaxShortStopTimeMillis(200);
//		badIs.setLongStopRate(0.0001F);
//		badIs.setMaxLongStopTimeMillis(5000);
//		badIs.setCorruptRate(0.0007F);
//		connection.setInputStream(badIs);
		
		super.onActivate();
	}
	
	@Override
	public void onDeactivate() throws Exception {
		connection.close();
		super.onDeactivate();
	}
	
	@Override
	protected InputStream getInputStream() {
		return connection.getInputStream();
	}

	@Override
	protected OutputStream getOutputStream() {
		return connection.getOutputStream();
//		return new FilterOutputStream(connection.getOutputStream()) {
//			int c = 0;
//			@Override
//			public void write(int b) throws IOException {
//				write(new byte[]{(byte)b}, 0, 1);
//			}
//			@Override
//			public void write(byte[] b) throws IOException {
//				write(b, 0, b.length);
//			}
//			@Override
//			public void write(byte[] b, int off, int len) throws IOException {
//				for (int i=off; i<len+off; i++) {
//					super.write(b[i]);
//					c++;
//					if(c>20) {
//						try {
//							Thread.sleep(100);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//						c = 0;
//					}
//				}
//			}
//		};
	}

	@Override
	public void controlDataLink(DataLinkControlCommand cm) {
		if(cm.getSetDataLinkState().equals(DataLinkState.OFF)) {
			rxEnabled = false;
			txEnabled = false;
		} else if(cm.getSetDataLinkState().equals(DataLinkState.RX_ONLY)) {
			rxEnabled = true;
			txEnabled = false;
		} else if(cm.getSetDataLinkState().equals(DataLinkState.TX_AND_RX)) {
			rxEnabled = true;
			txEnabled = true;
		} else if(cm.getSetDataLinkState().equals(DataLinkState.TX_HIGH_POWER_AND_RX)) {
			rxEnabled = true;
			txEnabled = true;
		}
		populateStatusReportWithControlCommand(cm);
	}

	@Override
	public void setupDataLink(DataLinkSetupMessage sm) {
		populateStatusReportWithSetupMessage(sm);
	}
	
	@Override
	public String getInfo() {
		return "SerialDataTerminal "+serialConnectionParams.toString();

	}
	
}
