package common.nw.core.utils.log;

@SuppressWarnings("WeakerAccess")
public class NwLogHelper {


	public static final NwLogger LOGGER = NwLogger.NW_LOGGER;

	public static void severe(String msg) {
		LOGGER.severe(msg);
	}

	public static void error(String msg) {
		LOGGER.severe(msg);
	}

	public static void warn(String msg) {
		LOGGER.warn(msg);
	}

	public static void info(String msg) {
		LOGGER.info(msg);
	}

}
