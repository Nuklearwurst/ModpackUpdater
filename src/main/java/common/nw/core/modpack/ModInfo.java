package common.nw.core.modpack;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.saj.InvalidSyntaxException;
import common.nw.core.utils.DownloadHelper;
import common.nw.core.utils.log.NwLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ModInfo {

	/**
	 * mod name
	 */
	public String name;

	/**
	 * mod version
	 */
	public String version;

	/**
	 * location of the modFile
	 */
	private String fileName;

	/**
	 * local mod File
	 */
	public File file;

	/**
	 * remote File
	 */
	private RepoMod remoteInfo;

	/**
	 * is the name read of the zip file?
	 */
	public boolean hasName = false;

	/**
	 * is the version read of the zip file?
	 */
	public boolean hasVersionFile = false;

	/**
	 * create a modInfo instance using the file name, the modData is read by
	 * loadInfo, remoteData is added by setRemoteInfo
	 */
	public ModInfo(String fileName) {
		this.setFileName(fileName);
		name = version = getFileName();
	}

	/**
	 * create a modInfo instance using the remote data(updateVersionInformation checks
	 * for local files, but it might not exist)
	 */
	public ModInfo(RepoMod remoteMod) {
		this.setFileName(remoteMod.getFileName());
		this.name = remoteMod.name;
		this.version = null;
		this.remoteInfo = remoteMod;
	}

	/**
	 * @return the filename using the File.seperator of the current system
	 */
	public String getFileNameSystem() {
		return fileName.replace("/", File.separator);
	}

	/**
	 * @return the filename of this mod (should be using '/' as File.seperator)
	 */
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName.replace(File.separator, "/");
	}

	/**
	 * @return remote data of the mod
	 */
	public RepoMod getRemoteInfo() {
		return remoteInfo;
	}

	/**
	 * sets the remote data of a local mod and validates the VersionData
	 * <p>
	 * note: this will not check if the given RemoteMod is equal to this instance!
	 *
	 * @param mod remote mod
	 */
	public void setRemoteInfo(RepoMod mod) {
		this.remoteInfo = mod;
		updateVersionInformation();
	}

	/**
	 * uses correct version information (uses the defined versiontype of remote)
	 * <p>
	 * This will use any read version data (eg. forge's mod.info file) or fallback to MD5 or filename, if specified in remoteInfo
	 * </p>
	 */
	private void updateVersionInformation() {
		if (remoteInfo != null) {
			if (remoteInfo.versionType != null) {
				if (remoteInfo.versionType.equals(ModpackValues.Version.versionTypeMD5) && file != null && file.exists()) {
					version = DownloadHelper.getHash(file);
				} else if (remoteInfo.versionType.equals(ModpackValues.Version.versionTypeFileName) && this.hasVersionFile) {
					version = getFileName();
				}
			}
		}
	}

	/**
	 * read version from file
	 *
	 * @param baseDir directory this mod file is contained in
	 */
	@SuppressWarnings("unchecked")
	public void loadInfo(File baseDir) {
		loadInfoFromFile(new File(baseDir, getFileNameSystem()));
	}

	/**
	 * reads the mod information of the given file
	 */
	@SuppressWarnings("unchecked")
	public void loadInfoFromFile(File file) {
		// loading file
		if (file.exists()) {
			this.file = file;

			// scan version, update remote , fall back to other version typews

			if (getFileName().endsWith(".litemod")) {
				readLitemodVersionData();
			} else if (getFileName().endsWith(".jar")) {
				readForgeVersionData();
			}
			// update version info according to the
			// version type specified in remoteMods
			updateVersionInformation();
		} else {
			// no local file found
			version = null;
		}
	}

	private void readLitemodVersionData() {
//		String versionFile = getVersionFileFromZip(file, "litemod.json");
//		if (versionFile != null && !versionFile.isEmpty()) {
//			if (versionFile.trim().startsWith("{")) {
//				HashMap<String, String> entryMap = null;
//				try {
//					entryMap = (new Gson().fromJson(versionFile,
//							HashMap.class));
//				} catch (JsonSyntaxException jsx) {
//					jsx.printStackTrace();
//				}
//				if (entryMap != null) {
//					//TODO: read litemod files
//				}
//			} else {
//				version = versionFile;
//				hasVersionFile = true;
//			}
//		}
	}

	/**
	 * reads forge's mod.info files from the jar file
	 */
	private void readForgeVersionData() {
		String versionFile = getVersionFileFromZip(file, "*mod.info");
		if (versionFile != null && !versionFile.isEmpty()) {
			JdomParser parser = new JdomParser();
			JsonNode versionData;
			try {
				versionData = parser.parse(versionFile);
				if (versionData.hasElements()) {
					//Old mcmod.info file format
					List<JsonNode> modinfo = versionData.getElements();
					parseModInfoList(modinfo);
				} else if (versionData.hasFields()) {
					if ("2".equals(versionData.getNumberValue("modListVersion"))) {
						List<JsonNode> modinfo = versionData.getArrayNode("modList");
						parseModInfoList(modinfo);
					} else {
						NwLogger.NW_LOGGER.error("Error reading forge version file! Unknown fileformat!");
					}
				} else {
					NwLogger.NW_LOGGER.error("Error reading forge version file! Unknown fileformat!");
				}
			} catch (InvalidSyntaxException | IllegalStateException | IllegalArgumentException e) {
				NwLogger.NW_LOGGER.error("Error reading forge version file", e);
			}
		}
	}

	/**
	 * parse the modinfo list that is contained in a mod.info file
	 *
	 * @param modinfo JsonNode array of modinfo data
	 * @throws IllegalArgumentException if list does not contain needed information (modid and version)
	 */
	private void parseModInfoList(List<JsonNode> modinfo) throws IllegalArgumentException {
		if (!modinfo.isEmpty()) {
			name = modinfo.get(0).getStringValue("modid");
			version = modinfo.get(0).getStringValue("version");
			hasName = true;
			hasVersionFile = true;
		}
	}

	/**
	 * returns the String of the specified file contained in the ZIP-archive
	 *
	 * @param file zip file
	 * @param name name of the file inside the zip
	 * @return null if no version file could be extracted
	 */
	@SuppressWarnings({"SameParameterValue", "WeakerAccess"})
	public static String getVersionFileFromZip(File file, String name) {
		try {
			String out = null;
			ZipFile modZip = new ZipFile(file);
			ZipEntry entry = null;
			if (name.startsWith("*")) {
				//search for file ending with given name
				name = name.substring(1);
				Enumeration<? extends ZipEntry> enumeration = modZip.entries();
				while (enumeration.hasMoreElements()) {
					ZipEntry zipEntry = enumeration.nextElement();
					if (zipEntry.getName().endsWith(name)) {
						entry = zipEntry;
						break;
					}
				}
			} else {
				entry = modZip.getEntry(name);
			}

			if (entry != null) {
				BufferedReader reader = null;
				StringBuilder outBuilder = new StringBuilder();
				try {
					InputStream inStream = modZip.getInputStream(entry);
					reader = new BufferedReader(new InputStreamReader(inStream));
					String versionFileLine;
					while ((versionFileLine = reader.readLine()) != null) {
						outBuilder.append(versionFileLine);
					}
					out = outBuilder.toString();
					inStream.close();
				} catch (Exception e) {
					NwLogger.NW_LOGGER.error("Error reading Version from zip!", e);
				} finally {
					if (reader != null) {
						reader.close();
					}
				}
			}
			modZip.close();
			return out;
		} catch (Exception e) {
			NwLogger.NW_LOGGER.error("Error reading Version from zip!", e);
		}
		return null;
	}

	/**
	 * if this mod has no remote representative any more it should be deleted
	 *
	 * @return whether this mod should be deleted
	 */
	public boolean shouldBeDeleted() {
		return remoteInfo == null;
	}

	/**
	 * @return true if this mod should be tracked
	 * @see ModpackValues.Version#versionTypeTracked
	 */
	public boolean shouldBeTracked() {
		return remoteInfo != null && ModpackValues.Version.versionTypeTracked.equals(remoteInfo.versionType);
	}

	/**
	 * check versions
	 * <p>
	 * will return {@code false} if this mod has no remote represantation
	 * <p>
	 * returns {@code true} if this mod is not yet available locally
	 *
	 * @return whether this mod needs an update
	 */
	public boolean needUpdate() {
		if (remoteInfo == null) {
			return false;
		}
		//noinspection SimplifiableIfStatement
		if (version == null) {
			return true;
		}
		return !remoteInfo.version.equals(version.replace(File.separator, "/"));
	}

	/**
	 * version is null when no local modFile was found
	 *
	 * @return whether this mod is missing on the client
	 */
	public boolean isMissing() {
		return version == null;
	}

	/**
	 * checks if two mods are the same
	 *
	 * @param mod remote mod to check against
	 * @return whether the specified remote mod is the same as this local mod
	 */
	public boolean equals(RepoMod mod) {
		if (mod.nameType != null) {
			//mod nameType conflict resolution
			if (mod.nameType.equals(ModpackValues.Name.nameTypeFileName) && this.hasName) {
				//local version was read from zip-version file but remote is managed by filename
				return this.getFileName().equals(mod.name);
			} else if (mod.nameType.equals(ModpackValues.Name.nameTypeZipEntry)) {
				if (!this.hasName) {
					//We could not read a zip name, this is a different mod1
					return false;
				}
			} else {
				// default to filename if nameType could not be parsed and we have read the name from zip
				if (hasName) {
					return this.getFileName().equals(mod.name);
				}
			}
		}
		//compare names
		return this.name.equals(mod.name);
	}

	@Override
	public String toString() {
		return name;
	}
}
