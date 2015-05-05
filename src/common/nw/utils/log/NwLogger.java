package common.nw.utils.log;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class NwLogger {
	
	public static NwLogger NW_LOGGER = new NwLogger("NwLogger");
	public static NwLogger UPDATER_LOGGER = new NwLogger("Modpack Updater");
	public static NwLogger INSTALLER_LOGGER = new NwLogger("Modpack Installer");
	public static NwLogger CREATOR_LOGGER = new NwLogger("Modpack Creator");

	public Logger logger;
	
	public NwLogger(String name) {
		logger = Logger.getLogger(name);
		LogFormatter formatter = new LogFormatter();
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatter);
        for(Handler handler : logger.getHandlers()) {
        	logger.removeHandler(handler);
        }
        logger.setUseParentHandlers(false);
        logger.addHandler(consoleHandler);
	}
	
	public NwLogger(String name, NwLogHelper parent) {
		this(name);
	}
	
	public void severe(String msg) {
		logger.severe(msg);
	}
	
	public void error(String msg) {
		logger.severe(msg);
	}
	
	public void warn(String msg) {
		logger.warning(msg);
	}
	
	public void warning(String msg) {
		logger.warning(msg);
	}
	
	public void info(String msg) {
		logger.info(msg);
	}
	
	public void fine(String msg) {
		logger.fine(msg);
	}
	
}
