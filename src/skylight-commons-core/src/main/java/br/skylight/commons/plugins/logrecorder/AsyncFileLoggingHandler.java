package br.skylight.commons.plugins.logrecorder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import br.skylight.commons.infra.ThreadWorker;
import br.skylight.commons.infra.TimedBoolean;
import br.skylight.commons.plugin.annotations.ManagedMember;
import br.skylight.commons.plugin.annotations.ServiceInjection;
import br.skylight.commons.services.StorageService;

@ManagedMember
public class AsyncFileLoggingHandler extends ThreadWorker {

	private final long MIN_FREE_DISK = 10 * 1024 * 1024;
	private TimedBoolean checkFreeSpace = new TimedBoolean(5000);

	private ArrayBlockingQueue<String> queue;
//	private SimpleObjectFIFO queue;
	private OutputStream logFileOut;
	private File logFile;
	private java.util.logging.Formatter logFormatter = new SimpleFormatter();
	// private Formatter logFormatter = new LogFormatter();

//	private String lineSeparator = (String) java.security.AccessController.doPrivileged(new sun.security.action.GetPropertyAction("line.separator"));

	@ServiceInjection
	public StorageService storageService;

	public AsyncFileLoggingHandler() {
		super(30);
	}

	@Override
	public void onActivate() throws Exception {
		// prepare file and write queue
		logFile = storageService.resolveFile("sysout.log");
		logFileOut = new FileOutputStream(logFile);
		queue = new ArrayBlockingQueue<String>(300, true);

		// setup logging handlers
		Logger logger = Logger.getLogger("");
		for (Handler handler : logger.getHandlers()) {
			logger.removeHandler(handler);
		}

		// add console handler
		ConsoleHandler ch = new ConsoleHandler();
		ch.setFormatter(logFormatter);
		logger.addHandler(ch);

		// add async file logging handler
		logger.addHandler(new Handler() {
			@Override
			public void publish(LogRecord record) {
				scheduleLog(logFormatter.format(record));
			}

			@Override
			public void flush() {
			}

			@Override
			public void close() throws SecurityException {
			}
		});

		setReady(true);
	}

	@Override
	public void onDeactivate() throws Exception {
		//flush all messages in queue to disk
		String message;
		while((message=queue.poll())!=null) {
			logFileOut.write(message.getBytes());
		}
		logFileOut.flush();
		logFileOut.close();
	}

	public void step() throws Exception {
		String message = (String) queue.poll(1000, TimeUnit.MILLISECONDS);
		if(message!=null) {
			logFileOut.write(message.getBytes());
			logFileOut.flush();
		}
	}

	private void scheduleLog(String message) {
		if (message == null)
			return;
		if (checkFreeSpace.checkTrue() && logFile.getFreeSpace() < MIN_FREE_DISK) {
//			System.out.println("Won't save log message because of low free disk space (<10MB). message=" + message);
			return;
		}

		// timestamp message
//		Calendar calendar = Calendar.getInstance();
//		String timeStamp = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + ":" + calendar.get(Calendar.MILLISECOND);
//		message = timeStamp + ": " + message;
		// add new line
		// message += lineSeparator;

		// queue to be logged to disk
		if(!queue.offer(message)) {
//			System.out.println("Won't save log message because queue is full. message=" + message);
		}
	}

}
