package common.nw.updater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.nw.core.modpack.*;
import common.nw.core.utils.DownloadHelper;
import common.nw.core.utils.UpdateResult;
import common.nw.core.utils.Utils;
import common.nw.core.utils.log.NwLogger;
import common.nw.installer.Installer;
import common.nw.updater.gui.IProgressWatcher;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Updater extends Thread {

	private static final int FLAG_SERVER = 1;

	/**
	 * arguments
	 */
	private final List<String> args;

	/**
	 * the minecraft profile name
	 */
	private final String versionName;

	/**
	 * the minecraft game directory to update into
	 */
	@SuppressWarnings("CanBeFinal")
	private File gameDir;

	/**
	 * configuration flags of outr instance (are we on a server/client/etc.)<br>
	 *
	 * @see RepoMod#fileType
	 */
	private int flags = 0;

	/**
	 * should we quit to launcher (--> error)
	 */
	private boolean quitToLauncher = false;

	/**
	 * is the update finished
	 */
	private boolean finished = false;

	/**
	 * retry the whole update?
	 */
	private boolean retry = false;

	/**
	 * progress listener
	 */
	private IProgressWatcher listener;

	/**
	 * error messages
	 */
	private List<String> warnings = new ArrayList<>();

	private boolean errored = false;

	/**
	 * local modpack data
	 */
	private LocalModpack local;

	/**
	 * remote modpack data
	 */
	private RepoModpack remote;

	/**
	 * mod list --> all
	 */
	private List<ModInfo> mods;

	public Updater(List<String> args, File gameDir, String versionName) {
		this.args = args;
		this.gameDir = gameDir;
		this.versionName = versionName;

		setDaemon(true);
		setName("Modpack Update Thread");
	}

	/**
	 * do we need to exit minecraft
	 */
	public boolean quitToLauncher() {
		return quitToLauncher || listener.quitToLauncher();
	}

	/**
	 * is the update finished?
	 */
	public boolean isFinished() {
		return finished || listener.isCancelled();
	}

	/**
	 * sets the update listener
	 */
	public void setListener(IProgressWatcher listener) {
		this.listener = listener;
	}

	public boolean shouldRetry() {
		return retry;
	}

	@Override
	public void run() {
		try {
			// run update
			doUpdate();
		} catch (Exception ex) {
			//handle unknown errors
			NwLogger.UPDATER_LOGGER.error("Unknown Error updating!", ex);
			addErrorMessage("Error:  " + ex.getMessage() + "\nData: " + ex.toString());
			errored = true;
		}
		onUpdateFinished();
		finished = true;
	}

	/**
	 * performs the update
	 */
	private void doUpdate() {
		//init vars
		finished = false;
		retry = false;
		quitToLauncher = false;
		//listener
		if (listener == null) {
			listener = new ConsoleListener();
			NwLogger.UPDATER_LOGGER.warning("No listener specified! Defaulting to Console output");
		}

		// checking local stuff
		listener.setOverallProgress("Reading local modpack info...", 0);
		if (!readLocalModpack()) { // read modpack
			addErrorMessage("Error: no modpack found!");
			errored = true;
			return;
		}

		waitForUi();

		// read local mod info
		listener.setOverallProgress(2);
		if (!readLocalData()) {
			errored = true;
			addErrorMessage("Error reading local data!");
		}
		listener.setDownloadProgress("", 0);

		waitForUi();
		if (listener.isCancelled()) {
			return;
		}

		// check for updates
		listener.setOverallProgress("Reading remote data!", 3);
		if (!readRemoteData()) { // read remote mod info
			errored = true;
			listener.setOverallProgress("Could not download modpack.json!", 0);
			return; // don't continue or save when internet connection failes
		}

		waitForUi();
		if (listener.isCancelled()) {
			return;
		}

		// update mc
		if ((flags & FLAG_SERVER) == 0) {
			listener.setOverallProgress("Checking for minecraft update", 6);
			if (checkUpdate()) {
				waitForUi();
				if (listener.isCancelled()) {
					return;
				}

				listener.setOverallProgress("Updating minecraft", 6);
				if (!updateVersion()) {
					addErrorMessage("Failed updating minecraft!");
					//no need to set the errored flag, this is handled by updateVersion
					//not really needed as we should always cancel further updating, after having updated minecraft
					return;
				}
			}
		} else {
			NwLogger.UPDATER_LOGGER.info("Skipping version update check: We are on a server!");
		}


		waitForUi();
		if (listener.isCancelled()) {
			return;
		}

		// update mods
		//update mod list
		listener.setOverallProgress("Adding remote information!", 7);
		if (!addRemoteInformation()) {
			errored = true;
			addErrorMessage("Unknown error! This is a Bug!");
		}
		waitForUi();
		if (listener.isCancelled()) {
			return;
		}

		//delete old mods
		listener.setOverallProgress("Deleting old mods!", 8);
		if (!deleteOldMods()) {
			addErrorMessage("Error deleting mods. Is an other instance running? \nPlease check Permissions!");
			errored = true;
		}
		waitForUi();
		if (listener.isCancelled()) {
			return;
		}

		//update mods
		listener.setOverallProgress("Updating Mods!", 10);
		if (!updateMods()) {
			addErrorMessage("Error updating mods!");
			errored = true;
		}
		waitForUi();
		if (listener.isCancelled()) {
			return;
		}

		// save
		if (!save()) {
			int ans = listener.showConfirmDialog(
					"Error when saving!\nDo you want to try again?", "Error",
					JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
			if (ans == JOptionPane.YES_OPTION) {
				if (!save()) {
					addErrorMessage("Error while saving modpack.json!");
					errored = true;
				}
			} else {
				addErrorMessage("Error while saving modpack.json!");
				errored = true;
			}
		}
	}

	/**
	 * handles finish of the update
	 * prints error messages
	 */
	private void onUpdateFinished() {
		if (errored) {
			int ans = listener.showErrorDialog("Error during update", "Following Errors occurred:\n" + getErrorMessageString());
			if (ans == JOptionPane.YES_OPTION) {
				retry = true;
			} else if (ans == JOptionPane.NO_OPTION) {
				quitToLauncher = true;
			}
		}
	}

	/**
	 * creates a modpack from commandline parameters (used when no modpack.json file is found)
	 * <p>
	 * Allowed arguments:
	 * <ul>
	 * <li>modpack [string] - name of the modpack</li>
	 * <li>modpackrepo [string] - url of the modpack</li>
	 * <li>modpackversion [string] - version of the modpack</li>
	 * <li>serverMode [true/false] - whether to run in serverMode and skip launcher profile updates</li>
	 * </ul>
	 */
	private LocalModpack parseCommandLineModpack() {
		try {
			OptionParser optionParser = new OptionParser();
			ArgumentAcceptingOptionSpec<String> modPackOption = optionParser
					.accepts("modpack").withRequiredArg().ofType(String.class);
			ArgumentAcceptingOptionSpec<String> modPackRepoOption = optionParser
					.accepts("modpackrepo")
					.requiredIf(modPackOption)
					.withRequiredArg().ofType(String.class);
			ArgumentAcceptingOptionSpec<String> modPackVersionOption = optionParser
					.accepts("modpackversion")
					.withRequiredArg().ofType(String.class);
			ArgumentAcceptingOptionSpec<Boolean> serverOption = optionParser
					.accepts("serverMode").withOptionalArg().ofType(Boolean.class).defaultsTo(true);

			optionParser.allowsUnrecognizedOptions();
			OptionSet options = optionParser.parse(args.toArray(new String[args.size()]));

			/////////////////
			// Parse flags //
			/////////////////
			if (options.has(serverOption)) {
				if (serverOption.value(options)) {
					NwLogger.UPDATER_LOGGER.info("Applying server specific settings!");
					flags |= FLAG_SERVER;
				} else if (GraphicsEnvironment.isHeadless()) {
					NwLogger.UPDATER_LOGGER.warn("Running in a headless environment, but serverOption is set to false!");
					NwLogger.UPDATER_LOGGER.warn("Please check programm arguments!");
				}
			} else {
				if (versionName == null || GraphicsEnvironment.isHeadless()) {
					//assume that we are on a server
					//as we have no profile and none of the passed parameters indicate a client
					flags |= FLAG_SERVER;
					NwLogger.UPDATER_LOGGER.info("Detected headless mode!");
					NwLogger.UPDATER_LOGGER.info("You can force disable headless mode by specifying the \"serverMode\" argument");
				}
			}

			///////////////////
			// Parse modpack //
			///////////////////
			if (options.has(modPackRepoOption)) {
				String modPackName = modPackOption.value(options);
				String modPackRepo = modPackRepoOption.value(options);
				String modPackVersion = modPackVersionOption.value(options);
				NwLogger.UPDATER_LOGGER.info("parsing command line. Repo: " + modPackRepo + ", Version: " + modPackVersion + ", Name: " + modPackName);
				return new LocalModpack(modPackName, modPackRepo, modPackVersion);
			}
		} catch (Exception e) {
			e.printStackTrace();
			NwLogger.UPDATER_LOGGER.severe("Error parsing commandline modpack!");
		}
		return null;
	}

	/**
	 * read the local modpack.json, if it is not available parse modpack from
	 * commandline
	 */
	private boolean readLocalModpack() {
		File modpackJson = new File(gameDir, "modpack.json");
		local = null;
		if (modpackJson.exists()) {
			NwLogger.UPDATER_LOGGER.info("modpack.json found, parsing...");
			try {
				local = new Gson().fromJson(new FileReader(modpackJson), LocalModpack.class);
			} catch (Exception e) {
				NwLogger.UPDATER_LOGGER.severe("Error parsing modpack.json, try to use commandline modpack", e);
			}
		}
		LocalModpack commandLine = parseCommandLineModpack();
		if (local == null) {
			local = commandLine;
			NwLogger.UPDATER_LOGGER.info("Using commandlineModpack!");
		} else {
			//check if commandline data is equal to local stored data
			//usually this should be the case
			//when updating however, the commandline args might be different
			//this could also happen when reusing an installation for another modpack
			if (commandLine != null) {
				//compare url
				if (commandLine.url != null && !commandLine.url.isEmpty() && !commandLine.url.equals(local.url)) {
					// error
					int ans = listener.showConfirmDialog(
							"An url was set in the program-arguments, but it is different from the local file!\nDo you want to overwrite saved data?\nArgument: "
									+ commandLine.url
									+ "\nSaved data: " + local.url,
							"Warning", JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);
					if (ans == JOptionPane.YES_OPTION) {
						local.url = commandLine.url;
					}
				}
				//compare name
				if (commandLine.name != null && !commandLine.name.isEmpty() && !commandLine.name.equals(local.name)) {
					// error
					int ans = listener.showConfirmDialog(
							"A name was set in the program-arguments, but it is different from the local file!\nDo you want to overwrite saved data?\nArgument: "
									+ commandLine.name
									+ "\nSaved data: " + local.name,
							"Warning", JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);
					if (ans == JOptionPane.YES_OPTION) {
						local.name = commandLine.name;
					}
				}
				//compare version
				if (commandLine.version != null && !commandLine.version.isEmpty() && !commandLine.version.equals(local.version)) {
					// error
					int ans = listener.showConfirmDialog(
							"A version was set in the program-arguments, but it is different from the local file!\nDo you want to overwrite saved data?\nArgument: "
									+ commandLine.version
									+ "\nSaved data: " + local.version,
							"Warning", JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);
					if (ans == JOptionPane.YES_OPTION) {
						local.version = commandLine.version;
					}
				}
			}
		}
		if (local == null) {
			String ans = listener.showInputDialog("No Modpack found!\nPlease enter the Modpack-URL!");
			if (ans != null) {
				local = new LocalModpack(null, ans, null);
			}
			NwLogger.UPDATER_LOGGER.info("Configuring modpack!!!");
		}
		if (local == null) {
			addErrorMessage("No modpack could be found!");
			return false;
		}
		return true;
	}

	/**
	 * reads data of the local mods from filesystem
	 */
	private boolean readLocalData() {
		if (mods == null) {
			mods = new ArrayList<>();
		}
		// no mods found
		//continue updating
		if (local.files == null) {
			NwLogger.UPDATER_LOGGER.info("No Files Found! Updating...");
			return true;
		}
		listener.setDownloadProgress("Checking Local files...", 0, local.files.size());
		try {
			for (int i = 0; i < local.files.size(); i++) {
				listener.setDownloadProgress(i);
				ModInfo info = new ModInfo(local.files.get(i));
				info.loadInfo(gameDir); // load info
				//update tracked version information
				if (info.version != null && local.trackedFileVersions != null && local.trackedFileVersions.containsKey(info.getFileNameSystem())) {
					info.version = local.trackedFileVersions.get(info.getFileNameSystem());
				}
				mods.add(info);
			}
		} catch (Exception e) {
			NwLogger.UPDATER_LOGGER.warning("error reading local info!");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * downloads remote modpack.json information
	 */
	private boolean readRemoteData() {
		try {
			String json = DownloadHelper.getString(local.url, listener);
			listener.setOverallProgress(4);
			listener.setDownloadProgress("Parsing...");
			remote = new Gson().fromJson(json, RepoModpack.class);
			listener.setOverallProgress(5);
			if (local.name == null) {
				local.name = remote.modpackName;
			}
			if (local.version == null) {
				local.version = remote.minecraft.version;
			}

			if (local.name != null && remote.modpackName != null && !local.name.equals(remote.modpackName)) {
				int ans = listener.showConfirmDialog(
						"Local name is different from remote!\nShould it be set to remote?\nOnly do this if you are sure you are downloading the correct modpack.",
						"Warning!", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (ans == JOptionPane.YES_OPTION) {
					local.name = remote.modpackName;
				}
			}
		} catch (Exception e) {
			NwLogger.UPDATER_LOGGER.error("Error downloading!", e);
			addErrorMessage("error downloading modpack.json from: " + local.url);
			errored = true;
			return false;
		}
		return true;
	}

	/**
	 * checks for minecraft or library update
	 */
	private boolean checkUpdate() {
		if (VersionInfo.REPO_MODPACK_REVISION < remote.updaterRevision) {
			NwLogger.UPDATER_LOGGER.info("New Updater necessary!!");
			return true;
		}
		if ((local.version != null && local.version
				.equals(remote.minecraft.version))
				|| remote.minecraft.version == null) {
			NwLogger.UPDATER_LOGGER.info("minecraft up to date!");
			return false;
		}
		NwLogger.UPDATER_LOGGER.info("New version found! Local is: " + local.version
				+ " Remote is: " + remote.minecraft.version);
		return true;
	}

	/**
	 * adds remote information to existing mods, used to check which mods need
	 * an update
	 */
	@SuppressWarnings("SameReturnValue")
	private boolean addRemoteInformation() {
		for (RepoMod remoteMod : remote.files) {
			// updating entries
			boolean missing = true;
			for (ModInfo mod : mods) {
				if (mod.equals(remoteMod)) {
					mod.setRemoteInfo(remoteMod);
					missing = false;
					break;
				}
			}
			// adding missing entries (only if we need that mod, if mods are added manually we don't remove them)
			if (missing && Utils.doFlagCombine(remoteMod.fileType, flags)) {
				ModInfo info = new ModInfo(remoteMod);
				mods.add(info);
			}
		}
		return true;
	}

	/**
	 * updates minecraft or library version
	 *
	 * @return true when the updater should continue, false when not
	 */
	private boolean updateVersion() {
		//update jar and json

		String[] options = {"Install", "Continue without installing", "Quit to launcher"};
		int ans = listener.showOptionDialog(
				"A new Modpack version is available. Do you want to install it?\nCurrent Version: " + local.version
						+ ", New Version: " + remote.minecraft.version + "\nThis is not required on the server!!",
				"Update", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		//install the new version
		if (ans == JOptionPane.YES_OPTION) {
			listener.setDownloadProgress("Installing new Version", 0);
			String mcDir;

			File selection = listener.selectFile(Utils.getMinecraftDir(), JFileChooser.DIRECTORIES_ONLY, "Select .minecraft directory!");
			if (selection != null) {
				mcDir = selection.getAbsolutePath();
			} else {
				addErrorMessage("User cancelled modpack update!");
				return false;
			}

			Installer installer = new Installer(remote, versionName, mcDir, local.url, false, true);

			listener.setDownloadProgress("Creating directories...", 10);
			if (!installer.createDirs()) {
				addErrorMessage("Error when creating dirs.");
				errored = true;
				return false;
			}
			listener.setDownloadProgress("Creating version .json...", 20);
			if (!installer.createJson()) {
				addErrorMessage("Error when creating version.json file.");
				errored = true;
				return false;
			}
			listener.setDownloadProgress("Writing version .json...", 40);
			if (!installer.writeJson()) {
				addErrorMessage("Error when creating version.json file.");
				errored = true;
				return false;
			}
			listener.setDownloadProgress("Creating jar...", 50);
			if (!installer.createJar(listener.hasGui(), listener.getGui())) {
				addErrorMessage("Error when creating version.jar file.");
				errored = true;
				return false;
			}
			listener.setDownloadProgress("Updating updater...", 80);
			if (!installer.downloadLibraries()) {
				addErrorMessage("Error when downloading updater.\nRun the installer manually if you experience problems");
				errored = true;
				//do not return as error is not critical
			}

			//no profile stuff as launcher will discard any changes
			//settings new version information
			local.version = remote.minecraft.version;
			listener.setDownloadProgress("Installation Complete!", 100);

			if (!save()) {
				addErrorMessage("error during saving modpack.json!");
				return false;
			}

			if (listener.showConfirmDialog("You probably need to restart Minecraft.\nDo you want to continue anyway?", "Question", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
				quitToLauncher = true;
				return false;
			} else {
				//trying to continue updating
				return true;
			}
		} else if (ans == JOptionPane.NO_OPTION) {
			int ans2 = listener.showConfirmDialog("Overwrite local version?", "If this is a server you can update the local version information.\nThis will make sure that you won't get asked to update on this version again.", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
			if (ans2 == JOptionPane.YES_OPTION) {
				local.version = remote.minecraft.version;
				NwLogger.UPDATER_LOGGER.info("Updated local version info to remote!");
			}
			//continue updating
			return true;
		} else {
			//return to launcher
			quitToLauncher = true;
			return false;
		}

	}

	/**
	 * deletes mods that aro no longer allowed (doesn't apply to mods not
	 * downloaded by the updater and not being on the blacklist )
	 */
	private boolean deleteOldMods() {
		List<ModInfo> modsToDelete = new ArrayList<>();
		// blacklist
		if (remote.blacklist != null) {
			for (RepoMod mod : remote.blacklist) {
				modsToDelete.add(new ModInfo(mod));
			}
		}
		// old mods
		for (ModInfo mod : mods) {
			if (mod.shouldBeDeleted()) {
				modsToDelete.add(mod);
			}
		}

		NwLogger.UPDATER_LOGGER.info("Found " + modsToDelete.size() + " files to Delete!");
		boolean success = true;

		// delete old or blacklisted mods
		if (modsToDelete.size() > 0) {
			for (ModInfo mod : modsToDelete) {
				File fileToDelete = new File(gameDir, mod.getFileNameSystem());
				if (fileToDelete.exists()) {
					if (!fileToDelete.delete()) {
						success = false;
						NwLogger.UPDATER_LOGGER.severe("Mod " + mod.getFileNameSystem()
								+ "could not be deleted!");
					}
				}
			}
		}
		return success;
	}

	/**
	 * downloads all mods that need an update
	 */
	private boolean updateMods() {
		//collect mods that need updating
		List<ModInfo> modsToUpdate = new ArrayList<>();
		for (ModInfo mod : mods) {
			if (mod.needUpdate()) {
				modsToUpdate.add(mod);
			}
		}
		NwLogger.UPDATER_LOGGER.info("Found " + modsToUpdate.size() + " files to Update!");

		if (modsToUpdate.size() < 1) {
			listener.setOverallProgress("All mods up to date", 100);
			listener.setDownloadProgress("", 2, 2);
			return true;
		}
		// number of the current mod
		int modNumber = 0;
		// progress bar -- value of one mod (first 10% is checking for update)
		float modValue = 90.0F / modsToUpdate.size();

		for (ModInfo mod : modsToUpdate) {

			//cancel if needed
			waitForUi();
			if (listener.isCancelled()) {
				return true;
			}

			NwLogger.UPDATER_LOGGER.info(String.format("Starting update for %s mod %s [%s] to version [%s] from %s",
					mod.isMissing() ? "MISSING" : "OUTDATED", mod.name, mod.version,
					mod.getRemoteInfo().version,
					mod.getRemoteInfo().downloadUrl));

			modNumber++;
			//fallback to default downloadtype
			if (mod.getRemoteInfo().downloadType == null) {
				mod.getRemoteInfo().downloadType = ModpackValues.Download.modDirectDownload;
			}
			switch (mod.getRemoteInfo().downloadType) {
				case ModpackValues.Download.modDirectDownload:
					if (!performDirectModDownload(mod, modNumber, modValue)) {
						addErrorMessage("Failed downloading Mod: " + mod);
						return false;
					}
					break;
				case ModpackValues.Download.modExtractDownload:
					if (!performDirectModDownload(mod, modNumber, modValue)) {
						addErrorMessage("Failed downloading Mod: " + mod);
						return false;
					}
					if (!DownloadHelper.extractArchive(mod.file, mod.file.getParentFile())) {
						addErrorMessage("Failed extracting Archive from: " + mod.file + ", to: " + mod.file.getParentFile());
						return false;
					}
					//keep zip file for version tracking
					break;
				case ModpackValues.Download.modUserDownload:
					addErrorMessage("Unsupported downloadType: " + mod.getRemoteInfo().downloadType + " \nConsider updating your updater.jar to the newest version!");
					errored = true;
					return false;
				default:
					//defaulting to direct download
					addErrorMessage("Unsupported downloadType: " + mod.getRemoteInfo().downloadType + " \nDefaulting to " + ModpackValues.Download.modDirectDownload + "\nConsider updating your updater.jar to the newest version!");
					if (!performDirectModDownload(mod, modNumber, modValue)) {
						addErrorMessage("Failed downloading Mod with unsupported downloadType: " + mod);
						addErrorMessage("Try reinstalling the modpack, otherwise contact your modpack author!");
						errored = true;
						return false;
					}
					break;
			}
		}
		return true;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean performDirectModDownload(ModInfo mod, int modNumber, float modValue) {
		int attempts = 0;
		boolean retry;
		do {
			//download mod
			UpdateResult result = DownloadHelper.getMod(listener, mod,
					modNumber, modValue, gameDir, false);
			//automatically try again if download failed (up to 4 times)
			if (result != UpdateResult.Good) {
				attempts++;
			}
			retry = attempts < 4;

			waitForUi();
			if (listener.isCancelled()) {
				NwLogger.UPDATER_LOGGER.severe("User cancelled update!");
				return true;
			}

			//display dialog when download finally failed
			//giving user the opportunity to retry the download
			if ((result != UpdateResult.Good) && (!retry)) {
				final int dialogResult = listener.showErrorDialog("Retry?", "Downloading mod \"" + mod.name + "\" version: \"" + mod.version + "\" failed!\nDo you want to retry?");
				switch (dialogResult) {
					case JOptionPane.YES_OPTION:
						retry = true;
						break;
					case JOptionPane.CANCEL_OPTION:
						addErrorMessage("Failed updating mod: " + mod.getFileNameSystem());
						addErrorMessage("Error when downloading: " + result);
						addErrorMessage("Skipping download...");
						errored = true;
						return true;
					case JOptionPane.NO_OPTION:
					default:
						addErrorMessage("Failed updating mod: " + mod.getFileNameSystem());
						addErrorMessage("Error when downloading: " + result);
						errored = true;
						return false;

				}
			}
			if (result == UpdateResult.Good) {
				retry = false;
				//updating local version information
				mod.version = mod.getRemoteInfo().version;
			}
		} while (retry);
		return true;
	}

	/**
	 * saves data to modpack.json file
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean save() {
		// setting new variables
		local.files = new ArrayList<>();
		local.trackedFileVersions = new HashMap<>();

		for (ModInfo mod : mods) {
			if (!mod.shouldBeDeleted()) {
				//add mod to local file list
				local.files.add(mod.getFileNameSystem());
				if (mod.shouldBeTracked()) {
					//save local tracked versions to disk
					local.trackedFileVersions.put(mod.getFileNameSystem(), mod.version);
				}
			}
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			FileWriter fileWriter = new FileWriter(new File(gameDir, "modpack.json"));
			gson.toJson(local, fileWriter);
			fileWriter.close();
		} catch (IOException ex) {
			NwLogger.UPDATER_LOGGER.severe("Error writing modpack.json", ex);
			addErrorMessage("Could not save modpack.json!");
			return false;
		}
		return true;
	}

	private synchronized void waitForUi() {
		while (listener.isPaused()) {
			try {
				this.wait(100);
			} catch (InterruptedException e) {
				NwLogger.UPDATER_LOGGER.error("Error occurred when waiting for ui!", e);
				addErrorMessage("Critical error when waiting for UI to catch up!");
				break;
			}
		}
	}

	private void addErrorMessage(final String msg) {
		NwLogger.UPDATER_LOGGER.warn(msg);
		warnings.add(msg);
	}

	private String getErrorMessageString() {
		StringBuilder builder = new StringBuilder();
		for (String s : warnings) {
			builder.append(s).append("\n");
		}
		return builder.toString();
	}

	// read local modpack error? exit
	// read local data and update version info
	// read remote data and update version info error ? exit
	// check mc update error ? continue
	// update mc error ? continue
	// check mods to update
	// check mods to delete
	// update/delete mods error ? save and exit
	// save

}
