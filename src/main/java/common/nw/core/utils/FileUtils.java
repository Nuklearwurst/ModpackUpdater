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

	public static int compareVersions(String v1, String v2) {
		if (v1 == null) {
			return v2 == null ? 0 : 1;
		}
		if (v2 == null) {
			return -1;
		}
		String[] v1Parts = v1.split("\\.");
		String[] v2Parts = v2.split("\\.");
		int length = Math.max(v1Parts.length, v2Parts.length);
		for (int i = 0; i < length; i++) {
			int v1Part = i < v1Parts.length ? Utils.tryParseInt(v1Parts[i]) : 0;
			int v2Part = i < v2Parts.length ? Utils.tryParseInt(v2Parts[i]) : 0;
			if (v1Part < v2Part)
				return -1;
			if (v1Part > v2Part)
				return 1;
		}
		return 0;
	}
}
