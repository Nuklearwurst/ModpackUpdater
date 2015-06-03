package common.nw.utils;

import common.nw.utils.log.NwLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.Locale;

public class Utils {

	/**
	 * sets LookAndFeel
	 */
	public static void setOSLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable t) {
			NwLogger.NW_LOGGER.error("Error when setting Look and Feel!", t);
		}
	}

	/**
	 * calculate the time passed since the given date(in Days)
	 *
	 * @param date
	 * @return
	 */
	public static int getDaysSimceUpdate(Date date) {
		long time = System.currentTimeMillis() - date.getTime();
		double days = time / 86400000D;
		return (int) Math.floor(days);
	}

	/**
	 * get the default minecraft directory for different operating systems
	 *
	 * @return
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
	 * opens a FileChooser to select a file
	 *
	 * @param c                 parent
	 * @param currrentDirectory
	 * @return null if nothing is selected, otherwise the absolutePath
	 */
	@SuppressWarnings("SameParameterValue")
	public static String openFile(Component c, File currrentDirectory) {
		File file = openJFileChooser(c, currrentDirectory,
				JFileChooser.FILES_ONLY, null);
		if (file != null) {
			return file.getAbsolutePath();
		}
		return null;
	}

	/**
	 * opens a FileChooser to select a folder
	 *
	 * @param c                 parent
	 * @param currrentDirectory
	 * @return null if nothing is selected, otherwise the absolutePath
	 */
	public static String openFolder(Component c, File currrentDirectory) {
		File file = openJFileChooser(c, currrentDirectory,
				JFileChooser.DIRECTORIES_ONLY, null);
		if (file != null) {
			return file.getAbsolutePath();
		}
		return null;
	}

	/**
	 * opens a FileChooser to select a file or folder if a folder is selected a
	 * default filename is appended
	 *
	 * @param c                parent
	 * @param currentDirectory
	 * @param fileName         default file name (in case a folder is selected)
	 * @return null if nothing got selected, otherwise the absolute path
	 */
	@SuppressWarnings("SameParameterValue")
	public static String openFileOrDirectoryWithDefaultFileName(Component c,
	                                                            File currentDirectory, String fileName) {
		File file = openJFileChooser(c, currentDirectory,
				JFileChooser.FILES_AND_DIRECTORIES, null);
		if (file == null) {
			return null;
		}
		String path = file.getAbsolutePath();
		if (file.isDirectory()) {
			if (path.endsWith(File.separator)) {
				path = path + fileName;
			} else {
				path = path + File.separator + fileName;
			}
		}
		return path;
	}

	/**
	 * opens a file using a JFileChooser
	 *
	 * @param c                parent
	 * @param currentDirectory
	 * @param mode             selection Mode {@link JFileChooser}
	 * @param buttonText       buttonText, uses "Open" when null ({@link JFileChooser})
	 * @return File
	 */
	@SuppressWarnings({"SameParameterValue", "WeakerAccess"})
	public static File openJFileChooser(Component c, File currentDirectory,
	                                    int mode, String buttonText) {
		JFileChooser fc = new JFileChooser(currentDirectory);
		fc.setFileSelectionMode(mode);
		int result;
		if (buttonText != null) {
			result = fc.showDialog(c, buttonText);
		} else {
			result = fc.showOpenDialog(c);
		}
		if (result == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		}
		return null;
	}

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

	@SuppressWarnings("SameParameterValue")
	public static String parseIntWithMinLength(int number, int length) {
		String str = String.valueOf(number);
		String prefix = "";
		for (int i = 0; i < length - str.length(); i++) {
			prefix += "0";
		}
		return prefix + str;
	}

	/**
	 * Gets System Text Clipboard if available
	 * @return found value
	 */
	public static String getStringClipboard() {
		try {
			Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable transferData = systemClipboard.getContents(null);
			return  (String) transferData.getTransferData(DataFlavor.stringFlavor);
		} catch (IOException e) {
			NwLogger.NW_LOGGER.error("Error getting system clipboard", e);
		} catch (UnsupportedFlavorException e) {
			NwLogger.NW_LOGGER.error("Error getting system clipboard", e);
		}
		return null;
	}
}
