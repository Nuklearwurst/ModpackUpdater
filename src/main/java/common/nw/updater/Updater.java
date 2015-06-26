package common.nw.updater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import common.nw.installer.Installer;
import common.nw.modpack.*;
import common.nw.updater.gui.IProgressWatcher;
import common.nw.utils.DownloadHelper;
import common.nw.utils.UpdateResult;
import common.nw.utils.Utils;
import common.nw.utils.log.NwLogger;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Updater {

	private static final int FLAG_SERVER = 1;

	/**
	 * arguments
	 */
	private List<String> args;
	/**
	 * the minecraft profile name TODO support stuff with profiles
	 */
	private String profileName;
	/**
	 * the minecraft game directory to update into
	 */
	private File gameDir;

	/**
	 * configuration flags<br>
	 * @see {@link RepoMod#fileType}
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
	 * error message, if not empty error gets displayed to the user
	 */
	private String warningMessage = "";
	private boolean errored = false;

	/**
	 * ModpackUpdater Logger
	 */
	public static final NwLogger logger = NwLogger.UPDATER_LOGGER;

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

	private UpdateThread updateThread = null;

	public Updater(List<String> args, File gameDir, String profile) {
		this.args = args;
		this.gameDir = gameDir;
		this.profileName = profile;
	}

	/**
	 * do we need to exit minecraft
	 */
	public boolean quitToLauncher() {
		return quitToLauncher || listener.quitToLauncher();
	}

	/**
	 * is the update finished?
	 *
	 * @return
	 */
	public boolean isFinished() {
		return finished || listener.isCancelled();
	}

	/**
	 * starts the update in a new Thread
	 */
	public void beginUpdate() {
		updateThread = new UpdateThread();
		updateThread.start();
	}

	/**
	 * sets the update listener
	 *
	 * @param listener
	 */
	public void setListener(IProgressWatcher listener) {
		this.listener = listener;
	}

	/**
	 * thread to run the update
	 *
	 * @author Nukelarwurst
	 */
	private class UpdateThread extends Thread {

		UpdateThread() {
			setDaemon(true);
			setName("Modpack Update Thread");
		}

		@Override
		public void run() {
			try {
				// run update
				Updater.this.doUpdate();
			} catch (Exception ex) {
				NwLogger.UPDATER_LOGGER.error("Unknown Error updating!", ex);
				Updater.this.warningMessage += "\nError:  " + ex.getMessage() + "\nData: " + ex.toString(); //handle unknown errors
				errored = true;
			}
			Updater.this.onUpdateFinished();
			Updater.this.finished = true;
		}
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
			logger.warning("No listener specified!");
		}

		// checking local stuff
		listener.setOverallProgress("Reading local modpack info!", 0);
		if (!readLocalModpack()) { // read modpack
			//do not set error flag, respect cancelling
			warningMessage = warningMessage + "\nError: no modpack found!";
			return;
		}

		waitForUi();

		// should update
		listener.setOverallProgress(1);
		if (!shouldCheckUpdate()) {
			return;
		}

		// read local mod info
		listener.setOverallProgress(2);
		if (!readLocalData()) {
			errored = true;
			warningMessage = warningMessage + "\nError reading local data!";
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
			logger.warning(warningMessage);
			return; // don't continue or save when internet connection failes
		}

		waitForUi();
		if (listener.isCancelled()) {
			return;
		}

		// update mc
		if((flags & FLAG_SERVER) == 0) {
			listener.setOverallProgress("Checking for minecraft update", 6);
			if (checkUpdate()) {
				waitForUi();
				if (listener.isCancelled()) {
					return;
				}

				listener.setOverallProgress("Updating minecraft", 6);
				if (!updateVersion()) {
					warningMessage = warningMessage + "\nFailed updating minecraft!";
					//no need to set the errored flag, this is handled by updateVersion
					//not really needed as we should always cancel further updating, after having updated minecraft
					return;
				}
			}
		} else {
			logger.info("Skipping version update check: We are on a server!");
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
			warningMessage = warningMessage + "\nUnknown error! This is a Bug!";
		}
		waitForUi();
		if (listener.isCancelled()) {
			return;
		}

		//delete old mods
		listener.setOverallProgress("Deleting old mods!", 8);
		if (!deleteOldMods()) {
			warningMessage = warningMessage
					+ "\nError deleting mods. Is an other instance running? \nPlease check Permissions!";
			errored = true;
		}
		waitForUi();
		if (listener.isCancelled()) {
			return;
		}

		//update mods
		listener.setOverallProgress("Updating Mods!", 10);
		if (!updateMods()) {
			warningMessage = warningMessage + "\nError updating mods!";
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
					warningMessage = warningMessage
							+ "\nError while saving modpack.json!";
					errored = true;
				}
			} else {
				warningMessage = warningMessage
						+ "\nError while saving modpack.json!";
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
			int ans = listener.showErrorDialog("Error during update", "Following Errors occurred:\n" + warningMessage);
			if (ans == JOptionPane.YES_OPTION) {
				retry = true;
			} else if (ans == JOptionPane.NO_OPTION) {
				quitToLauncher = true;
			}
		}
	}

	/**
	 * creates a modpack from commandline parameters (used when no modpack.json
	 * file is found)
	 *
	 * @return
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
					.accepts("server").withOptionalArg().ofType(Boolean.class).defaultsTo(true);

			optionParser.allowsUnrecognizedOptions();
			OptionSet options = optionParser.parse(args.toArray(new String[args
					.size()]));

			/////////////////
			// Parse flags //
			/////////////////
			if(options.has(serverOption)) {
				if(serverOption.value(options)) {
					logger.info("Applying server specific settings!");
					flags |= FLAG_SERVER;
				}
			}

			///////////////////
			// Parse modpack //
			///////////////////
			if (options.has(modPackRepoOption)) {
				String modPackName = modPackOption.value(options);
				String modPackRepo = modPackRepoOption.value(options);
				String modPackVersion = modPackVersionOption.value(options);
				logger.info("parsing command line. Repo: " + modPackRepo + ", Version: " + modPackVersion + ", Name: " + modPackName);
				return new LocalModpack(modPackName, modPackRepo, modPackVersion);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe("Error parsing commandline modpack!");
		}
		return null;
	}

	/**
	 * read the local modpack.json, if it is not available parse modpack from
	 * commandline
	 *
	 * @return
	 */
	private boolean readLocalModpack() {
		File modpackJson = new File(gameDir, "modpack.json");
		local = null;
		if (modpackJson.exists()) {
			logger.info("modpack.json found, parsing...");
			try {
				local = new Gson().fromJson(new FileReader(modpackJson),
						LocalModpack.class);
			} catch (Exception e) {
				logger.severe("Error parsing modpack.json, try to use commandline modpack", e);
			}
		}
		LocalModpack commandLine = parseCommandLineModpack();
		if (local == null) {
			local = commandLine;
			logger.info("Using commandlineModpack!");
		} else {
			if (commandLine != null) {
				if (commandLine.name != null && !commandLine.name.isEmpty() && !commandLine.name.equals(local.name)) {
					// error
					int ans = listener.showConfirmDialog(
							"A name was set in the program-arguments, but it is different from the local file!\nDo you want to overwrite saved data?\nArgument: "
									+ commandLine.name
									+ "\nSaved data: " + local.name,
							"Waring", JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);
					if (ans == JOptionPane.YES_OPTION) {
						local.name = commandLine.name;
					}
				}
				if (commandLine.version != null && !commandLine.version.isEmpty() && !commandLine.version.equals(local.version)) {
					// error
					int ans = listener.showConfirmDialog(
							"A version was set in the program-arguments, but it is different from the local file!\nDo you want to overwrite saved data?\nArgument: "
									+ commandLine.version
									+ "\nSaved data: " + local.version,
							"Waring", JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);
					if (ans == JOptionPane.YES_OPTION) {
						local.version = commandLine.version;
					}
				}
			}
		}
		if (local == null) {
			// TODO modpack editing dialog
			String ans = listener.showInputDialog(
					"No Modpack found!\nPlease enter the Modpack-URL!");
			if (ans != null) {
				local = new LocalModpack(null, ans, null);
			}
			logger.info("Configuring modpack!!!");
		}
		if (local == null) {
			warningMessage = warningMessage + "\nNo modpack could be found!";
			errored = true;
			return false;
		}
		return true;
	}

	/**
	 * is it time to check for new updates?
	 *
	 * @return
	 */
	private boolean shouldCheckUpdate() {
		if (local.updateFrequency < 0) {
			local.updateFrequency = local.updateFrequency * -1;
			return true;
		}
		if (local.updateFrequency == 0) {
			return true;
		}
		return true;
	}

	/**
	 * reads data of the local mods from filesystem
	 *
	 * @return
	 */
	private boolean readLocalData() {
		if (mods == null) {
			mods = new ArrayList<ModInfo>();
		}
		// no mods found
		//continue updating
		if (local.files == null) {
			logger.info("No Files Found! Updating...");
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
			logger.warning("error reading local info!");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * downloads remote modpack.json information
	 *
	 * @return
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

			if (local.name != null && remote.modpackName != null
					&& !local.name.equals(remote.modpackName)) {
				int ans = listener.showConfirmDialog(
						"Local name is different from remote!\nShould it be set to remote?",
						"Warning!", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (ans == JOptionPane.YES_OPTION) {
					local.name = remote.modpackName;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			warningMessage = "error downloading modpack.json from: " + local.url;
			errored = true;
			return false;
		}
		return true;
	}

	/**
	 * checks for minecraft or library update
	 *
	 * @return
	 */
	private boolean checkUpdate() {
		if ((local.version != null && local.version
				.equals(remote.minecraft.version))
				|| remote.minecraft.version == null) {
			logger.info("minecraft up to date!");
			return false;
		}
		logger.info("New version found! Local is: " + local.version
				+ " Remote is: " + remote.minecraft.version);
		return true;
	}

	/**
	 * adds remote information to existing mods, used to check which mods need
	 * an update
	 *
	 * @return
	 */
	private boolean addRemoteInformation() {
		for (RepoMod remoteMod : remote.files) {
			// updating entries
			boolean missing = true;
			for (ModInfo mod : mods) {
				if (mod.equals(remoteMod, gameDir)) {
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

		String[] options = {"Install", "Continue without installing",
				"Quit to launcher"};
		int ans = listener.showOptionDialog(
				"A new Modpack version is available. Do you want to install it?\nThis is not required on the server!!",
				"Update", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		//install the new version
		if (ans == JOptionPane.YES_OPTION) {
			listener.setDownloadProgress("Installing new Version", 0);
			String mcDir;
			JFileChooser fc = new JFileChooser(Utils.getMinecraftDir());
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				mcDir = fc.getSelectedFile().getAbsolutePath();
			} else {
				warningMessage = warningMessage
						+ "\nUser cancelled modpack update!";
				return false;
			}

			Installer installer = new Installer(remote, remote.modpackName
					+ "-" + remote.minecraft.version, mcDir, false, true);

			listener.setDownloadProgress("Creating directories...", 10);
			if (!installer.createDirs()) {
				warningMessage = warningMessage + "\nError when creating dirs.";
				errored = true;
				return false;
			}
			listener.setDownloadProgress("Creating version .json...", 20);
			if (!installer.createJson()) {
				warningMessage = warningMessage
						+ "\nError when creating version.json file.";
				errored = true;
				return false;
			}
			listener.setDownloadProgress("Creating jar...", 50);
			if (!installer.createJar()) {
				warningMessage = warningMessage
						+ "\nError when creating version.jar file.";
				errored = true;
				return false;
			}
			listener.setDownloadProgress("Updating updater...", 80);
			if (!installer.downloadLibraries()) {
				warningMessage = warningMessage
						+ "\nError when downloading updater.\nRun the installer manually if you experience problems";
				errored = true;
				//do not return as error is not critical
//				return true;
			}
			listener.setDownloadProgress("Creating profile", 90);
			//updating existing minecraft profile
			//currently unused, probably not needed as existing version can be Overwritten
			//TODO decide about profile creation
			if (!installer.createProfile(profileName, null, null, -1)) {
				warningMessage = warningMessage + "\nError when creating profile.";
				errored = true;
				//do not return as the error is not critical
			}

			//settings new version information
			local.version = remote.minecraft.version;
			listener.setDownloadProgress("Installation Complete!", 100);

			if (!save()) {
				warningMessage += "error during saving modpack.json!";
				return false;
			}

			if (listener.showConfirmDialog(
					"You have to select the new Version in the Minecraft Launcher!\nDo you want to continue without updating?", "Question", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
				return false;
			} else {
				quitToLauncher = true;
				return false;
			}
		} else if (ans == JOptionPane.NO_OPTION) {
			int ans2 = listener.showConfirmDialog("Overwrite local version?", "If this is a server you can update the local version information.\nThis will make sure that you won't get asked to update on this version again.", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
			if(ans2 ==  JOptionPane.YES_OPTION) {
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
	 *
	 * @return
	 */
	private boolean deleteOldMods() {
		List<ModInfo> modsToDelete = new ArrayList<ModInfo>();
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

		logger.info("Found " + modsToDelete.size() + " files to Delete!");
		boolean success = true;

		// delete old or blacklisted mods
		if (modsToDelete.size() > 0) {
			for (ModInfo mod : modsToDelete) {
				File fileToDelete = new File(gameDir, mod.getFileNameSystem());
				if (fileToDelete.exists()) {
					if (!fileToDelete.delete()) {
						success = false;
						logger.severe("Mod " + mod.getFileNameSystem()
								+ "could not be deleted!");
					}
				}
			}
		}
		return success;
	}

	/**
	 * downloads all mods that need an update
	 *
	 * @return
	 */
	private boolean updateMods() {
		List<ModInfo> modsToUpdate = new ArrayList<ModInfo>();

		for (ModInfo mod : mods) {
			if (mod.needUpdate()) {
				modsToUpdate.add(mod);
			}
		}
		logger.info("Found " + modsToUpdate.size() + " files to Update!");

		if (modsToUpdate.size() < 1) {
			listener.setOverallProgress("All mods up to date", 100);
			listener.setDownloadProgress("", 2, 2);
			return true;
		}

		int modNumber = 0; // index
		float modValue = 90.0F / modsToUpdate.size(); // progress bar

		for (ModInfo mod : modsToUpdate) {

			waitForUi();
			if (listener.isCancelled()) {
				return true;
			}

			String updateReason = mod.isMissing() ? "MISSING" : "OUTDATED";

			logger.info(String.format("Starting update for %s mod %s [%s] to version [%s] from %s",
					updateReason, mod.name, mod.version,
					mod.getRemoteInfo().version,
					mod.getRemoteInfo().downloadUrl));

			modNumber++;
			if (mod.getRemoteInfo().downloadType == null || mod.getRemoteInfo().downloadType.equals(ModpackValues.modDirectDownload)) {
				if (!performDirectModDownload(mod, modNumber, modValue)) {
					warningMessage += "\nFailed downloading Mod: " + mod;
					return false;
				}
			} else if (mod.getRemoteInfo().downloadType.equals(ModpackValues.modExtractDownload)) {
				if (!performDirectModDownload(mod, modNumber, modValue)) {
					warningMessage += "\nFailed downloading Mod: " + mod;
					return false;
				}
				if (!DownloadHelper.extractArchive(mod.file, mod.file.getParentFile())) {
					warningMessage += "\nFailed extracting Archive from: " + mod.file + ", to: " + mod.file.getParentFile();
					return false;
				}
				//keep file for versioning
			} else if (mod.getRemoteInfo().downloadType.equals(ModpackValues.modUserDownload)) {
				warningMessage += "\nUnsupported downloadType: " + mod.getRemoteInfo().downloadType + " \nConsider updating your updater.jar to the newest version!";
				errored = true;
				return false;
			} else {
				//TODO default to user Download
				warningMessage += "\nUnsupported downloadType: " + mod.getRemoteInfo().downloadType + " \nDefaulting to " + ModpackValues.modDirectDownload + "\nConsider updating your updater.jar to the newest version!";
				if (!performDirectModDownload(mod, modNumber, modValue)) {
					warningMessage += "\nFailed downloading Mod with unsupported downloadType: " + mod;
					warningMessage += "\nTry reinstalling the modpack, otherwise contact your modpack author!";
					errored = true;
					return false;
				}
			}
		}
		return true;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean performDirectModDownload(ModInfo mod, int modNumber, float modValue) {
		int attempts = 0;
		boolean retry;
		do {
			//TODO have an option to ignore duplicates
			UpdateResult result = DownloadHelper.getMod(listener, mod,
					modNumber, modValue, gameDir, false);
			if (result != UpdateResult.Good) {
				attempts++;
			}
			retry = attempts < 4;

			waitForUi();
			if (listener.isCancelled()) {
				logger.severe("User cancelled update!");
				return true;
			}

			if ((result != UpdateResult.Good) && (!retry)) {
				int r = listener.showConfirmDialog("Downloading mod \"" + mod.name + "\" version: \"" + mod.version + "\" failed!\nDo you want to retry?", "Retry?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (r != JOptionPane.YES_OPTION) {
					warningMessage += "\nFailed updating mod: " + mod.getFileNameSystem() + "\nError when downloading: " + result;
					errored = true;
					logger.severe(warningMessage);
					return false;
				} else {
					retry = true;
				}
			}
			if (result == UpdateResult.Good) {
				retry = false;
				//TODO check if this works in all cases
				//updating local version infromation
				mod.version = mod.getRemoteInfo().version;
			}
		} while (retry);
		return true;
	}

	/**
	 * saves data to modpack.json file
	 *
	 * @return
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean save() {
		// setting new variables

		local.lastUpdate = new Date(System.currentTimeMillis());
		local.files = new ArrayList<String>();
		local.trackedFileVersions = new HashMap<String, String>();

		for (ModInfo mod : mods) {
			if (!mod.shouldBeDeleted()) {
				local.files.add(mod.getFileNameSystem());
				if (mod.getRemoteInfo() != null && mod.getRemoteInfo().versionType != null && mod.getRemoteInfo().versionType.equals(ModpackValues.versionTypeTracked)) {
					//save local tracked versions to disk 
					local.trackedFileVersions.put(mod.getFileNameSystem(), mod.version);
				}
			}
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			FileWriter fileWriter = new FileWriter(new File(gameDir,
					"modpack.json"));
			gson.toJson(local, fileWriter);
			fileWriter.close();
		} catch (IOException ex) {
			logger.severe("Error writing modpack.json", ex);
			warningMessage += "\nCould not save modpack.json!";
			return false;
		}
		return true;
	}

	private void waitForUi() {
		//noinspection SynchronizeOnNonFinalField
		synchronized (updateThread) {
			while (listener.isPaused()) {
				try {
					updateThread.wait(100);
				} catch (InterruptedException e) {
					logger.error("Error occurred when waiting for ui!", e);
					warningMessage += "\nCritical error when waiting for UI to catch up!";
					break;
				}
			}
		}
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
