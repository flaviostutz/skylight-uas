package br.skylight.commons.plugins.logrecorder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

	Date dat = new Date();
	private final static String format = "{0,date} {0,time}";
	private MessageFormat formatter;

	private Object args[] = new Object[1];
	
	private boolean showOnlyLogMessage = false;

	// Line separator string. This is the value of the line.separator
	// property at the moment that the SimpleFormatter was created.
	private String lineSeparator = (String) java.security.AccessController.doPrivileged(new sun.security.action.GetPropertyAction("line.separator"));

	/**
	 * Format the given LogRecord.
	 * 
	 * @param record
	 *            the log record to be formatted.
	 * @return a formatted log record
	 */
	public synchronized String format(LogRecord record) {
		String msg = "";
		
		msg += (record.getLevel().getLocalizedName()) + " ";
		
		//DATE TIME PART
		dat.setTime(record.getMillis());
		args[0] = dat;
		StringBuffer text = new StringBuffer();
		if (formatter == null) {
			formatter = new MessageFormat(format);
		}
		formatter.format(args, text, null);
		msg += text.toString() + " ";

		//SOURCE ELEMENT PART
		if (record.getSourceClassName() != null) {
			msg += simpleClassName(record.getSourceClassName());
		} else {
			msg += simpleClassName(record.getLoggerName());
		}
		if (record.getSourceMethodName() != null) {
			msg += "." + record.getSourceMethodName();
		}
		msg += " ";
		
		//fix the length of prefix part
		msg += "                                                         ";
		msg = msg.substring(0, 50) + " ";

		//LOG TEXT PART
		String message = formatMessage(record);
		msg += message + lineSeparator;
		if (record.getThrown() != null) {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				msg += sw.toString();
			} catch (Exception ex) {
			}
		}
		return msg;
	}

	private String simpleClassName(String className) {
		int i = className.lastIndexOf(".");
		if(i>0 && className.length()>(i+1)) {
			return className.substring(i+1);
		} else {
			return className;
		}
	}

}
