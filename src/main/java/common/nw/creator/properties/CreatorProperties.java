package common.nw.creator.properties;

import common.nw.utils.log.NwLogger;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;

/**
 * @author Nuklearwurst
 */
public class CreatorProperties {

	private static final String PROPERTIES_FILE = "." + File.separator + "creator.properties";


	public static final class Keys {
		public static final String LAST_OPENED_MODPACK = "last_opened_modpack";
		public static final String LAST_OPENED_MOD_DIRECTORY = "last_opened_mod_directory";
		public static final String LAST_INPUT_DIRECTORY = "last_input_directory";
		public static final String LOAD = "load";
	}

	public static String LAST_OPENED_MODPACK = "";
	public static String LAST_OPENED_MOD_DIRECTORY = "";
	public static String LAST_INPUT_DIRECTORY = "";
	public static boolean LOAD = false;

	public static boolean readProperties() {
		File propFile = new File(CreatorProperties.PROPERTIES_FILE);
		if(propFile.exists()) {
			Properties prop = new Properties();
			try {
				FileInputStream fs = new FileInputStream(propFile);
				prop.load(fs);
				LAST_OPENED_MOD_DIRECTORY = prop.getProperty(Keys.LAST_OPENED_MOD_DIRECTORY);
				LAST_OPENED_MODPACK = prop.getProperty(Keys.LAST_OPENED_MODPACK);
				LAST_INPUT_DIRECTORY = prop.getProperty(Keys.LAST_INPUT_DIRECTORY);
				Boolean load = Boolean.valueOf(prop.getProperty(Keys.LOAD));
				if (load != null) {
					LOAD = load;
				}
				try {
					fs.close();
				} catch (IOException ignored) {
					NwLogger.CREATOR_LOGGER.log(Level.FINE, "Error closing properties file inputstream!", ignored);
				}
				return true;
			} catch (FileNotFoundException e) {
				NwLogger.CREATOR_LOGGER.error("Properties File not found!", e);
				return false;
			} catch (IOException e) {
				NwLogger.CREATOR_LOGGER.error("Error reading Properties File!", e);
				return false;
			}
		} else {
			NwLogger.CREATOR_LOGGER.info("No Properties File found!");
			return false;
		}
	}

	public static void saveProperties() {
		try {
			FileOutputStream propFile = new FileOutputStream(CreatorProperties.PROPERTIES_FILE);
			Properties prop = new Properties();
			if(LAST_OPENED_MODPACK == null) {
				LAST_OPENED_MODPACK = "";
			}
			if(LAST_OPENED_MOD_DIRECTORY == null) {
				LAST_OPENED_MOD_DIRECTORY = "";
			}
			if(LAST_INPUT_DIRECTORY == null) {
				LAST_INPUT_DIRECTORY = "";
			}
			prop.setProperty(Keys.LAST_OPENED_MODPACK, LAST_OPENED_MODPACK);
			prop.setProperty(Keys.LAST_OPENED_MOD_DIRECTORY, LAST_OPENED_MOD_DIRECTORY);
			prop.setProperty(Keys.LAST_INPUT_DIRECTORY, LAST_INPUT_DIRECTORY);
			prop.setProperty(Keys.LOAD, String.valueOf(LOAD));
			prop.store(propFile, "Modpack Creator Properties File");
			try {
				propFile.close();
			} catch (IOException ignored) {
				NwLogger.CREATOR_LOGGER.log(Level.FINE, "Error closing properties file outputstream!", ignored);
			}
		} catch (FileNotFoundException e) {
			NwLogger.CREATOR_LOGGER.error("Error when trying to open properties file for writing!", e);
		} catch (IOException e) {
			NwLogger.CREATOR_LOGGER.error("Error saving properties!", e);
		}
	}

	public static boolean hasProperties() {
		return new File(CreatorProperties.PROPERTIES_FILE).exists();
	}
}
