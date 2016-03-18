package common.nw.core.utils;

import java.io.File;

/**
 * @author Nuklearwurst
 */
public class FileUtils {

	/**
	 * @param file the directory to create
	 * @return true if succeeded of directory already exsists
	 */
	public static boolean createDirectoriesIfNecessary(File file) {
		if (file.exists()) {
			return file.isDirectory();
		}
		return file.mkdirs();
	}

	/**
	 * @param file the directory to create
	 * @return true if succeeded of directory already exsists
	 */
	public static boolean createDirectoryIfNecessary(File file) {
		if (file.exists()) {
			return file.isDirectory();
		}
		return file.mkdir();
	}
}
