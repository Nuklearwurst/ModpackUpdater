package common.nw.creator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.nw.core.modpack.*;
import common.nw.core.utils.DownloadHelper;
import common.nw.core.utils.log.NwLogger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Creator {

	public RepoModpack modpack;

	/**
	 * location of the directory to scan (only used when {@link #shouldReadFiles} is set to true)
	 */
	public String fileLoc;
	public String outputLoc;

	public boolean shouldReadFiles = false;
	public boolean defaultLibrariesGenerated = false;

	private File workingDir;

	public Creator() {
		modpack = new RepoModpack();
		modpack.minecraft = new RepoVersionInfo();
		modpack.files = new ArrayList<>();
		modpack.blacklist = new ArrayList<>();
		modpack.minecraft.arguments = new ArrayList<>();
		modpack.minecraft.libraries = new ArrayList<>();
		modpack.updaterRevision = VersionInfo.REPO_MODPACK_REVISION;

	}

	/**
	 * reads folder structure
	 */
	@SuppressWarnings("SameReturnValue")
	public boolean readFiles() {
		if (modpack.files == null) {
			modpack.files = new ArrayList<>();
		}

		if (workingDir == null) {
			workingDir = new File(fileLoc);
		}

		findFiles(workingDir.listFiles(), "");

		return true;
	}

	/**
	 * searches a folder for files
	 */
	private void findFiles(File[] files, String base) {
		System.out.println(base);
		for (File file : files) {

			if (base.startsWith("/")) {
				base = base.substring(1); // needed?
			}

			if (file.isDirectory()) {
				findFiles(file.listFiles(), base + "/" + file.getName());
			} else {
				addMod(file, base);
			}
		}
	}

	/**
	 * adds a modFile to the list
	 *
	 * @param base the path to the file (the directory the file is in) relative to the working dir
	 */
	private void addMod(File file, String base) {
		NwLogger.CREATOR_LOGGER.info("Adding Mod: " + base + "  Filename: " + file.getName());

		RepoMod mod = new RepoMod();
		// handle files in base dir
		mod.setFileName(base.isEmpty() ? file.getName() : base + "/"
				+ file.getName());

		//read version data
		ModInfo info = new ModInfo(mod.getFileName());
		info.loadInfoFromFile(file);

		//copy version data to remote mod
		mod.name = info.name;
		mod.version = info.version;

		if (info.hasName) {
			mod.nameType = ModpackValues.Name.nameTypeZipEntry;
		} else {
			mod.nameType = ModpackValues.Name.nameTypeFileName;
		}

		if (info.hasVersionFile) {
			mod.versionType = ModpackValues.Version.versionTypeZipEntry;
		} else if (mod.getFileName().startsWith("config/") || mod.getFileName().endsWith(".conf") || mod.getFileName().endsWith(".cfg")) {
			//handle config files
			mod.versionType = ModpackValues.Version.versionTypeTracked;
			mod.version = DateFormat.getDateInstance().format(new Date());
		} else {
			mod.versionType = ModpackValues.Version.versionTypeFileName;
		}

		// handle download url
		String dir = modpack.modpackRepo + base.replace(" ", "%20");
		// handle dir ending
		if (!dir.endsWith("/")) {
			dir = dir + "/";
		}

		//replace whitespaces in URL
		mod.downloadUrl = dir + file.getName().replace(" ", "%20");
		mod.downloadType = ModpackValues.Download.modDirectDownload;

		mod.md5 = DownloadHelper.getHash(file);


//		mod.version = mod.getFileName();
//		mod.name = mod.getFileName();
//
//		//update types
//		mod.versionType = ModpackValues.versionTypeFileName;
//		mod.nameType = ModpackValues.nameTypeFileName;

		modpack.files.add(mod);
	}

	/**
	 * creates the output file (modpack.json)
	 */
	@SuppressWarnings("UnusedReturnValue")
	public boolean createOutputFile(Component c) {
		if (outputLoc.lastIndexOf(File.separator) == -1) {
			JOptionPane.showMessageDialog(c, "Invalid Directory!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		File output = new File(outputLoc);

		if (output.exists()) {
			if (output.isDirectory()) {
				output = new File(output, modpack.modpackName + ".json");
			}
		} else {
			if (!outputLoc.endsWith(".json")) {
				JOptionPane.showMessageDialog(c, "Invalid Directory!", "Error",
						JOptionPane.ERROR_MESSAGE);
				return false;
			} else {
				File outputDir = new File(outputLoc.substring(0,
						outputLoc.lastIndexOf(File.separator)));

				if (!outputDir.exists()) {
					if (!outputDir.mkdirs()) {
						JOptionPane.showMessageDialog(c,
								"Could not create Directory!", "Error",
								JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
			}
		}

		if (output.exists()) {
			int i = JOptionPane.showConfirmDialog(c,
					"File already exists!\nDelete it?", "Warning",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (i == JOptionPane.YES_OPTION) {
				if (!output.delete()) {
					JOptionPane.showMessageDialog(c, "Could not delete File!",
							"Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			} else {
				return false;
			}
		}

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			FileWriter fileWriter = new FileWriter(output);
			gson.toJson(modpack, fileWriter);
			fileWriter.close();
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(c, "Error writing file!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
}
