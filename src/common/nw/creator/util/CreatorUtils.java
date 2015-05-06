package common.nw.creator.util;

import common.nw.modpack.ModInfo;

import java.io.File;

/**
 * @author Nuklearwurst
 */
public class CreatorUtils {

	public static ModInfo createModInfoFromFile(String pathToFile) {
		if (pathToFile != null) {
			String fileName = pathToFile;
			if (fileName.contains(File.separator + "mods" + File.separator)) {
				int index = fileName.indexOf("mods" + File.separator);
				fileName = fileName.substring(index);
			} else if (fileName.contains(File.separator + "config" + File.separator)) {
				int index = fileName.indexOf("config" + File.separator);
				fileName = fileName.substring(index);
			} else if (fileName.endsWith(".jar")) {
				int index = fileName.lastIndexOf(File.separator);
				fileName = "mods" + fileName.substring(index);
			} else if (fileName.endsWith(".cfg")) {
				int index = fileName.lastIndexOf(File.separator);
				fileName = "config" + fileName.substring(index);
			}
			ModInfo mod = new ModInfo(fileName);
			mod.loadInfoFromFile(new File(pathToFile));
			return mod;
		}
		return null;
	}
}
