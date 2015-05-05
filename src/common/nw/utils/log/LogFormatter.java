package common.nw.utils.log;

import common.nw.utils.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public final class LogFormatter extends Formatter {

	private static final String LINE_SEPARATOR = System
			.getProperty("line.separator");

	@Override
	public String format(LogRecord record) {
		StringBuilder sb = new StringBuilder();
		// Date date = new Date(record.getMillis());
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(record.getMillis());
		sb.append(" [")
				.append(Utils.parseIntWithMinLength(calendar.get(Calendar.HOUR_OF_DAY), 2)).append(":")
				.append(Utils.parseIntWithMinLength(calendar.get(Calendar.MINUTE), 2)).append(":")
				.append(Utils.parseIntWithMinLength(calendar.get(Calendar.SECOND), 2)).append("][")
				.append(record.getLevel().getLocalizedName());
		String name = record.getLoggerName();
		if(name != null && !name.isEmpty()) {
			sb.append("][")
				.append(name);
		}
		sb.append("] ")
			.append(formatMessage(record))
			.append(LINE_SEPARATOR);

		if (record.getThrown() != null) {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				sb.append(sw.toString());
			} catch (Exception ex) {
				// ignore
			}
		}

		return sb.toString();
	}
}