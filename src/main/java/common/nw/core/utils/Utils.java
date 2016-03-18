package common.nw.core.utils;

import common.nw.core.utils.log.NwLogger;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.Locale;

public class Utils {

	/**
	 * calculate the time passed since the given date(in Days)
	 */
	public static int getDaysSimceUpdate(Date date) {
		long time = System.currentTimeMillis() - date.getTime();
		double days = time / 86400000D;
		return (int) Math.floor(days);
	}

	/**
	 * get the default minecraft directory for different operating systems
	 */
	public static String getMinecraftDir() {
		String osType = System.getProperty("os.name").toLowerCase(
				Locale.ENGLISH);
		String homeDir = System.getProperty("user.home", ".");
		String out;
		if ((osType.contains("win")) && (System.getenv("APPDATA") != null)) {
			out = System.getenv("APPDATA") + File.separator + ".minecraft";
		} else if (osType.contains("mac")) {
			out = homeDir + File.separator + "Library" + File.separator
					+ "Application Support" + File.separator + "minecraft";
		} else {
			out = homeDir + File.separator + ".minecraft";
		}
		return out;
	}

	/**
	 * recursively deletes the given file or directory
	 * <br>
	 * returns true if the given file does not exsist
	 *
	 * @param file file to delete
	 * @return <i>false</i> if a file could not be deleted, <i>true</i> otherwise
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean deleteFileOrDir(File file) {
		if (!file.exists()) {
			return true;
		}
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File sub : files) {
					if (!deleteFileOrDir(sub)) {
						return false;
					}
				}
			} else {
				return false;
			}
			return file.delete();
		} else {
			return file.delete();
		}
	}

	/**
	 * converts an integer to a String, adding leading zeroes if necessary
	 *
	 * @param number number to be converted to String
	 * @param length minimum length of the string returned
	 * @return string representation of the given integer
	 */
	@SuppressWarnings("SameParameterValue")
	public static String parseIntWithMinLength(int number, int length) {
		return String.format("%0" + length + "d", number);
//		String str = String.valueOf(number);
//		String prefix = "";
//		for (int i = 0; i < length - str.length(); i++) {
//			prefix += "0";
//		}
//		return prefix + str;
	}

	/**
	 * Gets System Text Clipboard if available
	 *
	 * @return found value
	 */
	public static String getStringClipboard() {
		Object o = getSystemClipboard(DataFlavor.stringFlavor);
		if (o instanceof String) {
			return (String) o;
		}
		NwLogger.NW_LOGGER.error("Unknown error getting system clipboard!!");
		NwLogger.NW_LOGGER.error("Wrong data recieved: " + o);
		return null;
	}

	/**
	 * Gets System Clipboard if available
	 * <br>
	 * returns null on error
	 *
	 * @param flavor requested DataFlavor
	 * @return found value
	 */
	@SuppressWarnings("SameParameterValue")
	public static Object getSystemClipboard(DataFlavor flavor) {
		try {
			Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			if (systemClipboard.isDataFlavorAvailable(flavor)) {
				return systemClipboard.getData(flavor);
			}
		} catch (IOException | IllegalStateException e) {
			NwLogger.NW_LOGGER.error("Error getting system clipboard", e);
		} catch (UnsupportedFlavorException e) {
			NwLogger.NW_LOGGER.error("Unknown error getting system clipboard", e);
		}
		return null;
	}

	/**
	 * checks whether these two integers share one bit (flag), or the parentFlag is zero
	 *
	 * @param parentFlag  the flag to check against, true is returned if this parameter is zero
	 * @param combineFlag the flag to check
	 * @return whether combineFlag contains elements of parentFlag
	 */
	public static boolean doFlagCombine(int parentFlag, int combineFlag) {
		return parentFlag == 0 || (parentFlag & combineFlag) > 0;
	}
}
