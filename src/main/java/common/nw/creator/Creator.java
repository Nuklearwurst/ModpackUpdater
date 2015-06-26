package common.nw.creator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.nw.modpack.ModpackValues;
import common.nw.modpack.RepoMod;
import common.nw.modpack.RepoModpack;
import common.nw.modpack.RepoVersionInfo;
import common.nw.utils.DownloadHelper;

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

	public String fileLoc;
	public String outputLoc;
	
	public boolean shouldReadFiles = false;

	private File workingDir;

	public Creator() {
		modpack = new RepoModpack();
		modpack.minecraft = new RepoVersionInfo();
		modpack.files = new ArrayList<RepoMod>();
		modpack.blacklist = new ArrayList<RepoMod>();
		modpack.minecraft.arguments = new ArrayList<String>();
		modpack.minecraft.libraries  = new ArrayList<String>();
		
	}

	/**
	 * checks wether all fields are valid (not null)
	 * @return
	 */
	@Deprecated
	public boolean validateEntries() {
		boolean noFieldNull = modpack.modpackName != null
				&& !modpack.modpackName.isEmpty()
				&& modpack.modpackRepo != null
				&& !modpack.modpackRepo.isEmpty()
				&& modpack.minecraft.version != null
				&& !modpack.minecraft.version.isEmpty()
				&& modpack.minecraft.jarUpdateType != null
				&& !modpack.minecraft.jarUpdateType.isEmpty()
				&& modpack.minecraft.versionName != null
				&& !modpack.minecraft.versionName.isEmpty()
				&& modpack.minecraft.jsonUpdateType != null
				&& !modpack.minecraft.jsonUpdateType.isEmpty()
				&& modpack.minecraft.jsonName != null
				&& !modpack.minecraft.jsonName.isEmpty()
				&& modpack.minecraft.arguments != null
				&& modpack.minecraft.libraries != null && fileLoc != null
				&& !fileLoc.isEmpty();
		if (!noFieldNull) {
			return false;
		}

		workingDir = new File(fileLoc);
		return !modpack.modpackRepo.contains(" ")
				&& (modpack.minecraft.jarUpdateType.equals(ModpackValues.jarDirectDownload)
						|| modpack.minecraft.jarUpdateType.equals(ModpackValues.jarLocalFile)
						|| modpack.minecraft.jarUpdateType.equals(ModpackValues.jarUserDownload))
				&& (modpack.minecraft.jsonUpdateType.equals(ModpackValues.jsonDirectDownload)
						|| modpack.minecraft.jsonUpdateType.equals(ModpackValues.jsonLocalFile)
						|| modpack.minecraft.jsonUpdateType.equals(ModpackValues.jsonUserDownload))
				&& workingDir.exists();
	}

	/**
	 * reads folder structure
	 * @return
	 */
	public boolean readFiles() {
		if(modpack.files == null) {
			modpack.files = new ArrayList<RepoMod>();
		}

		if (workingDir == null) {
			workingDir = new File(fileLoc);
		}

		findFiles(workingDir.listFiles(), "");

		return true;
	}

	/**
	 * searches a folder for files
	 * @param files
	 * @param base
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
	 * @param file
	 * @param base
	 */
	private void addMod(File file, String base) {
		System.out.println(base + "  Filename: " + file.getName());
		RepoMod mod = new RepoMod();

		// handle files in base dir

		mod.fileName = base.isEmpty() ? file.getName() : base + "/"
				+ file.getName();
		mod.fileName = mod.fileName.replace(File.separator, "/");
		mod.version = mod.fileName;
		mod.name = mod.fileName;
		
		//update types
		mod.versionType = ModpackValues.versionTypeFileName;
		mod.nameType = ModpackValues.nameTypeFileName;
		
		//handle config
		if(mod.fileName.startsWith("config/")) {
			mod.versionType = ModpackValues.versionTypeTracked;
			mod.version = DateFormat.getDateInstance().format(new Date(System.currentTimeMillis()));
		}

		// handle download url
		String dir = modpack.modpackRepo + base.replace(" ", "%20");

		// handle dir ending
		if (!dir.endsWith("/")) {
			dir = dir + "/";
		}

		//replace whitespaces in URL
		mod.downloadUrl = dir + file.getName().replace(" ", "%20");
		mod.downloadType = ModpackValues.modDirectDownload;

		mod.md5 = DownloadHelper.getHash(file);

		if (mod.fileName.endsWith(".litemod")) {
			// handle Litemod
		} else if (mod.fileName.endsWith(".zip")
				|| mod.fileName.endsWith(".jar")) {
			// handle forgemods
		}

		modpack.files.add(mod);
	}

	/**
	 * creates the output file (modpack.json)
	 * @param c
	 * @return
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
