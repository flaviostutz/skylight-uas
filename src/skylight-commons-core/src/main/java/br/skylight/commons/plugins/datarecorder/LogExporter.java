package br.skylight.commons.plugins.datarecorder;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.skylight.commons.dli.services.Message;

import com.gc.iotools.stream.is.StatsInputStream;

public class LogExporter {

	private static final String SEPARATOR = ";";
	private static DateFormat tf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss.SSS");
	private static NumberFormat nf = NumberFormat.getInstance(Locale.US);
	static {
		nf.setMaximumFractionDigits(8);
		nf.setGroupingUsed(false);
	}
	
	public static void exportLogsToCSV(File inLogFile, File outLogFile, File csvFile, LogExporterListener listener) throws IOException {
		long totalBytes = inLogFile.length() + outLogFile.length();
		
		//prepare streams
		StatsInputStream iis = new StatsInputStream(new FileInputStream(inLogFile));
		DataInputStream inLog = new DataInputStream(iis);
		LogExporterWorker inWorker = new LogExporterWorker(inLog);
		inWorker.fetchNextMessage();
		
		StatsInputStream ois = new StatsInputStream(new FileInputStream(outLogFile));
		DataInputStream outLog = new DataInputStream(ois);
		LogExporterWorker outWorker = new LogExporterWorker(outLog);
		outWorker.fetchNextMessage();
		
		FileOutputStream csvStream = new FileOutputStream(csvFile);
		csvStream.write(("S/R" + SEPARATOR 
					+ "Type" + SEPARATOR 
					+ "Timestamp" + SEPARATOR 
					+ "Instance Id" + SEPARATOR 
					+ "Latency" + SEPARATOR 
					+ "Timestamp" + SEPARATOR 
					+ "Vehicle Id" + SEPARATOR 
					+ "Cucs Id" + SEPARATOR
					+ "Specific" + "\n").getBytes());

		while(inWorker.getLastMessage()!=null || outWorker.getLastMessage()!=null) {
			if(inWorker.getLastMessage()==null) {
				exportMessageToCSV(outWorker.getLastMessage(), csvStream, true);
				outWorker.fetchNextMessage();
				listener.onProgress((iis.getSize() + ois.getSize())/totalBytes);
				
			} else if(outWorker.getLastMessage()==null) {
				exportMessageToCSV(inWorker.getLastMessage(), csvStream, false);
				inWorker.fetchNextMessage();
				listener.onProgress((iis.getSize() + ois.getSize())/totalBytes);
				
			} else {
				//order messages
				if(inWorker.getLastMessage().getTimeStamp()<outWorker.getLastMessage().getTimeStamp()) {
					exportMessageToCSV(inWorker.getLastMessage(), csvStream, false);
					inWorker.fetchNextMessage();
					listener.onProgress((iis.getSize() + ois.getSize())/totalBytes);
				} else {
					exportMessageToCSV(outWorker.getLastMessage(), csvStream, true);
					outWorker.fetchNextMessage();
					listener.onProgress((iis.getSize() + ois.getSize())/totalBytes);
				}
			}
		}
	}

	protected static void exportMessageToCSV(Message message, OutputStream csvStream, boolean outLog) throws IOException {
		csvStream.write(((outLog?"SEND":"RECV") + SEPARATOR 
						+ message.getMessageType().getNumber() + SEPARATOR 
						+ tf.format(new Date((long)(message.getTimeStamp()*1000.0))) + SEPARATOR 
						+ message.getMessageInstanceId() + SEPARATOR 
						+ (long)(message.getLatency()*1000) + SEPARATOR 
						+ messageToCSV(message)).getBytes());
	}
	
	protected static String messageToCSV(Message message) {
	 	String line = "";
		for (int i=1; i<=message.getFieldCount(); i++) {
			try {
				line += format(message.getField(i).get(message));
			} catch (Exception e) {
				e.printStackTrace();
			}
			line += SEPARATOR;
		}
		if(line.length()==0) {
			return "\n";
		} else {
			return line.substring(0,line.length()-1) + "\n";
		}
	}

	private static String format(Object obj) {
		if(obj.getClass().equals(Double.class)) {
			return nf.format((Double)obj);
		} else if(obj.getClass().equals(Float.class)) {
			return nf.format((Float)obj);
		} else {
			return obj.toString();
		}
	}
	
}
