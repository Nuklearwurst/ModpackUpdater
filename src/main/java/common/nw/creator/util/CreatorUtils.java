package common.nw.creator.util;


import common.nw.core.modpack.ModInfo;

import java.io.File;

/**
 * @author Nuklearwurst
 */
public class CreatorUtils {

	public static ModInfo createModInfoFromFile(File file) {
		if (file != null && file.exists()) {
			String fileName = file.getAbsolutePath();
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
			mod.loadInfoFromFile(file);
			return mod;
		}
		return null;
	}
}
