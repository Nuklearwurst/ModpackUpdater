package common.nw.modpack;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.saj.InvalidSyntaxException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import common.nw.utils.DownloadHelper;
import common.nw.utils.log.NwLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
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
	 * create a modInfo instance using the remote data(updateVersionInformation checks
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
	 * @param mod remote mod
	 */
	public void setRemoteInfo(RepoMod mod) {
		this.remoteInfo = mod;
		updateVersionInformation();
	}

	/**
	 * uses correct version information
	 */
	private void updateVersionInformation() {
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
	 * @param baseDir directory this mod file is contained in
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
			} else if(fileName.endsWith(".jar")){
				String versionFile = getVersionFileFromZip(file, "*mod.info");
				if (versionFile != null && !versionFile.isEmpty()) {
					JdomParser parser = new JdomParser();
					JsonNode versionData;
					try {
						versionData = parser.parse(versionFile);
						List<JsonNode> modinfo = versionData.getElements();
						if(!modinfo.isEmpty()) {
							name = modinfo.get(0).getStringValue("modid");
							version = modinfo.get(0).getStringValue("version");
							hasName = true;
							hasVersionFile = true;
						}
					} catch (InvalidSyntaxException e) {
						NwLogger.NW_LOGGER.error("Error reading forge version file", e);
					}
				}
			}
			if (remoteInfo != null) {
				updateVersionInformation(); // update version info according to the
									// version type specified in remoteMods
									// config
			}
		} else {
			// no local file found
			version = null;
		}
	}

	/**
	 * returns the String of the specified file contained in the ZIP-archive
	 * 
	 * @param file
	 *            zip file
	 * @param name
	 *            name of the file inside the zip
	 */
	@SuppressWarnings({"SameParameterValue", "WeakerAccess"})
	public static String getVersionFileFromZip(File file, String name) {
		try {
			String out = null;
			ZipFile modZip = new ZipFile(file);
			ZipEntry entry = null;
			if(name.startsWith("*")) {
				name = name.substring(1);
				Enumeration<? extends ZipEntry> enumeration = modZip.entries();
				while(enumeration.hasMoreElements()) {
					ZipEntry zipEntry = enumeration.nextElement();
					if(zipEntry.getName().endsWith(name)) {
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
	 * check versions
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
	 * @param baseDir Base directory (unused)
	 * @return whether the specified remote mod is the same as this local mod
	 */
	public boolean equals(RepoMod mod, File baseDir) {
		if (mod.nameType != null) {
			if (mod.nameType.equals(ModpackValues.nameTypeFileName) && this.hasName) {
				return this.fileName.equals(mod.name.replace("/",
						File.separator));
			} else if (mod.nameType.equals(ModpackValues.nameTypeZipEntry) && !this.hasName) {
				return false;
			}
		}
		// work around incase an zip version is readable but not specified on the server-modpack
		if(hasName) {
			return this.fileName.replace(File.separator, "/").equals(mod.name.replace(File.separator, "/"));
		}
		return this.name.equals(mod.name.replace(File.separator, "/"));
	}

	@Override
	public String toString() {
		return name;
	}
}
