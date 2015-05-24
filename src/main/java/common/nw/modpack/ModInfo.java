package common.nw.modpack;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import common.nw.utils.DownloadHelper;
import common.nw.utils.log.NwLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ModInfo {

	/** mod name */
	public String name;

	/** mod version */
	public String version;

	/** location of the modFile */
	public String fileName;

	/** local mod File */
	public File file;

	/** remote File */
	private RepoMod remoteInfo;

	/** is the name read of the zip file? */
	public boolean hasName = false;
	/** is the version read of the zip file? */
	public boolean hasVersionFile = false;

	/**
	 * create a modInfo instance using the file name, the modData is read by
	 * loadInfo, remoteData is added by setRemoteInfo
	 */
	public ModInfo(String fileName) {
		name = version = fileName.replace(File.separator, "/");
		this.fileName = fileName.replace("/", File.separator);
	}

	/**
	 * create a modInfo instance using the remote data(validateVersion checks
	 * for local files, but it might not exist)
	 */
	public ModInfo(RepoMod remoteMod) {
		this.fileName = remoteMod.fileName.replace("/", File.separator);
		this.name = remoteMod.name;
		this.version = null;
		this.remoteInfo = remoteMod;
	}

	/**
	 * 
	 * @return remote data of the mod
	 */
	public RepoMod getRemoteInfo() {
		return remoteInfo;
	}

	/**
	 * sets the remote data of a local mod (check first wether they are both the
	 * same using equals!) and validates the VersionData
	 * 
	 * @param mod
	 */
	public void setRemoteInfo(RepoMod mod) {
		this.remoteInfo = mod;
		validateVersion();
	}

	/**
	 * uses correcct version information
	 */
	private void validateVersion() {
		if (remoteInfo != null) {
			if (remoteInfo.versionType != null) {
				if (remoteInfo.versionType.equals(ModpackValues.versionTypeMD5) && file != null
						&& file.exists()) {
					version = DownloadHelper.getHash(file);
				} else if (remoteInfo.versionType.equals(ModpackValues.versionTypeFileName)
						&& this.hasVersionFile) {
					version = fileName;
				}
			}
		}
	}

	/**
	 * read version from file
	 * 
	 * @param baseDir
	 */
	@SuppressWarnings("unchecked")
	public void loadInfo(File baseDir) {
		loadInfoFromFile(new File(baseDir, fileName));
	}

	@SuppressWarnings("unchecked")
	public void loadInfoFromFile(File file) {
		// loading file
		if (file.exists()) {
			this.file = file;

			// scan version, update remote , fall back to other version typews
			// if necessary @see Mod
			// TODO read forge version

			// read .litemod version file
			if (fileName.endsWith(".litemod")) {
				String versionFile = getVersionFileFromZip(file, "litemod.json");
				if (versionFile != null && !versionFile.isEmpty()) {
					if (versionFile.trim().startsWith("{")) {
						HashMap<String, String> entryMap = null;
						try {
							entryMap = (new Gson().fromJson(versionFile,
									HashMap.class));
						} catch (JsonSyntaxException jsx) {
							jsx.printStackTrace();
						}
						if (entryMap != null) {
							//TODO: read litemod files
						}
					} else {
						version = versionFile;
						hasVersionFile = true;
					}
				}
			} else {
				//TODO: read forge mods
			}
			if (remoteInfo != null) {
				validateVersion(); // update version info according to the
									// version type specified in remoteMods
									// config

				// mot needed as validateVersion checks version of the mod file
				// (--> fixes redownloading configs when modpack.json got
				// corrupted)
				// if this mod file is only tracked on remote (not in list, eg.
				// manually added) and no forge or .litemod versiondata could be
				// found check md5
				// if(version == null &&
				// DownloadHelper.checkHash(remoteInfo.md5, file)) {
				// version = remoteInfo.version;
				// }
			}
		} else {
			// no local file found
			version = null;
		}
	}

	/**
	 * returns the entry String
	 * 
	 * @param file
	 *            zip file
	 * @param name
	 *            name of the file inside the zip
	 * @return
	 */
	@SuppressWarnings({"SameParameterValue", "WeakerAccess"})
	public static String getVersionFileFromZip(File file, String name) {
		try {
			String out = null;
			ZipFile modZip = new ZipFile(file);
			ZipEntry entry = modZip.getEntry(name);

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
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * if this mod has no remote representative any more it should be deleted
	 * 
	 * @return
	 */
	public boolean shouldBeDeleted() {
		return remoteInfo == null;
	}

	/**
	 * check versions
	 * 
	 * @return
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
	 * @return
	 */
	public boolean isMissing() {
		return version == null;
	}

	/**
	 * checks if two mods are the same
	 * 
	 * @param mod
	 * @param baseDir
	 * @return
	 */
	public boolean equals(RepoMod mod, File baseDir) {
		if (mod.nameType != null) {
			if (mod.nameType.equals(ModpackValues.nameTypeFileName) && this.hasName) {
				return this.fileName.equals(mod.name.replace(File.separator,
						"/"));
			} else if (mod.nameType.equals(ModpackValues.nameTypeZipEntry) && !this.hasName) {
				return false;
			}
		}
		return this.name.equals(mod.name.replace(File.separator, "/"));
	}
}
