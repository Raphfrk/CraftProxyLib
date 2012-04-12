package com.raphfrk.craftproxylib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public class CraftProxyLib {
	
	private static Logger logger = Logger.getLogger("CraftProxyLib");
	private static String logFile = "logs" + File.separator + "log-%D.txt";
	
	static {
		if (new File(logFile).getParentFile() != null) {
			new File(logFile).getParentFile().mkdirs();
		}
		RotatingFileHandler fileHandler = new RotatingFileHandler(logFile);
		Formatter formatter = new DateOutputFormatter(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"));
		fileHandler.setFormatter(formatter);
		logger.getParent().getHandlers()[0].setFormatter(formatter);
		logger.addHandler(fileHandler);
	}
	
	public static void log(String message) {
		if (logger != null) {
			logger.info(message);
		}
	}
	
	public static Logger getLogger() {
		return logger;
	}

	
	// Borrowed from Spout
	private static class RotatingFileHandler extends StreamHandler {
		private final SimpleDateFormat date;
		private final String logFile;
		private String filename;

		public RotatingFileHandler(String logFile) {
			this.logFile = logFile;
			date = new SimpleDateFormat("yyyy-MM-dd");
			filename = calculateFilename();
			try {
				setOutputStream(new FileOutputStream(filename, true));
			} catch (FileNotFoundException ex) {
				getLogger().log(Level.SEVERE, "Unable to open {0} for writing: {1}", new Object[] {filename, ex.getMessage()});
				ex.printStackTrace();
			}
		}

		@Override
		public synchronized void flush() {
			if (!filename.equals(calculateFilename())) {
				filename = calculateFilename();
				getLogger().log(Level.INFO, "Log rotating to {0}...", filename);
				try {
					setOutputStream(new FileOutputStream(filename, true));
				} catch (FileNotFoundException ex) {
					getLogger().log(Level.SEVERE, "Unable to open {0} for writing: {1}", new Object[] {filename, ex.getMessage()});
					ex.printStackTrace();
				}
			}
			super.flush();
		}

		private String calculateFilename() {
			return logFile.replace("%D", date.format(new Date()));
		}
	}
	
	private static class DateOutputFormatter extends Formatter {
		private final SimpleDateFormat date;

		public DateOutputFormatter(SimpleDateFormat date) {
			this.date = date;
		}

		@Override
		public String format(LogRecord record) {
			StringBuilder builder = new StringBuilder();

			builder.append(date.format(record.getMillis()));
			builder.append(" [");
			builder.append(record.getLevel().getLocalizedName().toUpperCase());
			builder.append("] ");
			builder.append(formatMessage(record));
			builder.append('\n');

			if (record.getThrown() != null) {
				StringWriter writer = new StringWriter();
				record.getThrown().printStackTrace(new PrintWriter(writer));
				builder.append(writer.toString());
			}

			return builder.toString();
		}
	}
	
}
