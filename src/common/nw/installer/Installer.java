package common.nw.installer;

import java.io.BufferedWriter;
import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import argo.format.PrettyJsonFormatter;
import argo.jdom.JdomParser;
import argo.jdom.JsonField;
import argo.jdom.JsonNode;
import argo.jdom.JsonNodeFactories;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;
import argo.saj.InvalidSyntaxException;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;

import common.nw.modpack.RepoModpack;
import common.nw.utils.DownloadHelper;
import common.nw.utils.Utils;
import common.nw.utils.log.NwLogHelper;

public class Installer {

	/** version Name */
	private String name;
	/** .minecraft Path */
	private String dir;
	/** should a profile be created? */
	private boolean createProfile;
	/** should the libs be downloaded? */
	private boolean downloadLib;

	

	/** .minecraft folder */
	private File baseDir;
	/** our VersionDirectory */
	private File ourDir;

	private RepoModpack repo;

	private String data;


	
	public Installer(RepoModpack repo, String name, String dir,
			boolean createProfile, boolean downloadLib) {
		this.repo = repo;
		this.name = name;
		this.dir = dir;
		this.createProfile = createProfile;
		baseDir = new File(dir);
		this.downloadLib = downloadLib;
	}

	/**
	 * downloads modapck.json file
	 * 
	 * @return success
	 */
	public static RepoModpack downloadModpack(String url) {
		try {
			String json = DownloadHelper.getString(url, null);
			if (json == null || json.isEmpty()) {
				return null;
			}
			Gson gson = new Gson();
			return gson.fromJson(json, RepoModpack.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * are all entries valid?
	 * 
	 * @return
	 */
	public boolean validateEntries() {
		boolean notNull = name != null && !name.isEmpty() && dir != null
				&& !dir.isEmpty();
		baseDir = new File(dir);
		boolean allValid = baseDir.exists() && baseDir.isDirectory();
		return notNull && allValid;
	}

	/**
	 * create needed dirs
	 * 
	 * @return success
	 */
	public boolean createDirs() {
		File versions = new File(baseDir, "versions");
		if (!versions.exists()) {
			if(!versions.mkdir()) {
				return false;
			}
		}
		ourDir = new File(versions, name);
		if (!ourDir.exists()) {
			if (!ourDir.mkdir()) {
				NwLogHelper.severe("Error creating version directory!");
				return false;
			}
		} else {
			//compensate wrong upper/lower case
			if(!Utils.deleteFileOrDir(ourDir)) {
				NwLogHelper.error("Error deleting old version dir!");
				return false;
			}
			if (!ourDir.mkdir()) {
				NwLogHelper.error("Error recreating version dir!");
				return false;
			}
		}
		return true;
	}

	/**
	 * downloads libraries (only nw-updater atm)
	 * 
	 * @return success
	 */
	public boolean downloadLibraries() {
		if (!downloadLib) {
			return true;
		}
		JdomParser parser = new JdomParser();
		JsonNode versionData;
		try {
			versionData = parser.parse(data);
			List<JsonNode> libCopy = versionData.getArrayNode("libraries");
			for (JsonNode node : libCopy) {
				String s = node.getStringValue("name");
				if (s.contains("common.nuklearwurst:updater")) {
					String url = node.getStringValue("url");
					if (url.endsWith("/")) {
						url = url.substring(0, url.length() - 1);
					}
					File lib = new File(baseDir, "libraries");
					if (!lib.exists()) {
						lib.mkdir();
					}
					File updater = new File(lib, "common" + File.separator
							+ "nuklearwurst" + File.separator + "updater");
					if (!updater.exists()) {
						updater.mkdirs();
					}
					String version = s.substring(s.lastIndexOf(":") + 1);
					File dir = new File(updater, version);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					File file = new File(dir, "updater-" + version + ".jar");
					String realUrl = url + "/common/nuklearwurst/updater/"
							+ version + "/updater-" + version + ".jar";
					DownloadHelper.downloadFile(realUrl, file);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * download json version file
	 * 
	 * @return
	 */
	public boolean createJson() {
		try {
			data = DownloadHelper.getString(repo.minecraft.jsonName, null);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		JdomParser parser = new JdomParser();
		JsonRootNode versionData;

		try {
			versionData = parser.parse(data);
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
			return false;
		}
		HashMap<JsonStringNode, JsonNode> dataCopy = Maps
				.newHashMap(versionData.getFields());
		dataCopy.put(JsonNodeFactories.string("id"),
				JsonNodeFactories.string(name));
		versionData = JsonNodeFactories.object(dataCopy);
		try {
			File file = new File(ourDir, name + ".json");
			if (file.exists()) {
				file.delete();
			}
			BufferedWriter newWriter = Files.newWriter(file, Charsets.UTF_8);
			PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter()
					.format(versionData, newWriter);
			newWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * download version jar
	 * 
	 * @return true if successful
	 */
	public boolean createJar() {
		File file = new File(ourDir, name + ".jar");
		if (file.exists()) {
			file.delete();
		}
		if(repo.minecraft.versionName != null && !repo.minecraft.versionName.isEmpty()) {
			return DownloadHelper.downloadFile(repo.minecraft.versionName, file);
		}
		return true;
	}

	/**
	 * create minecraft launcher profile <br>
	 * code is based on the MinecraftForge-Installer
	 * 
	 * @see <a href=https://github.com/MinecraftForge/Installer>https://github.com/MinecraftForge/Installer</a>
	 * @return success of the profile creation
	 */
	public boolean createProfile(String profileName, String javaOptions, String gameDirectory, int updateFrequency) {
		if (createProfile) {
			File launcherProfiles = new File(baseDir, "launcher_profiles.json");
			if(!launcherProfiles.exists()) {
				JOptionPane.showMessageDialog(null, "The launcher_profiles.json file is missing!\nYou need to run the minecraft launcher at least once!", "File not found", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			JdomParser parser = new JdomParser();
	        JsonRootNode jsonProfileData;

	        try
	        {
	            jsonProfileData = parser.parse(Files.newReader(launcherProfiles, Charsets.UTF_8));
	        }
	        catch (InvalidSyntaxException e)
	        {
	            JOptionPane.showMessageDialog(null, "The launcher profile file is corrupted. Re-run the minecraft launcher to fix it!", "Error", JOptionPane.ERROR_MESSAGE);
	            return false;
	        }
	        catch (Exception e)
	        {
	            throw Throwables.propagate(e);
	        }

	        //our Data
	        JsonField[] fields = new JsonField[] {
	            JsonNodeFactories.field("name", JsonNodeFactories.string(profileName)),
	            JsonNodeFactories.field("lastVersionId", JsonNodeFactories.string(this.name)),
	            JsonNodeFactories.field("gameDir", JsonNodeFactories.string(gameDirectory)),
	            JsonNodeFactories.field("javaArgs", JsonNodeFactories.string(javaOptions)),
	        };

	        HashMap<JsonStringNode, JsonNode> profileCopy = Maps.newHashMap(jsonProfileData.getNode("profiles").getFields());
	        HashMap<JsonStringNode, JsonNode> rootCopy = Maps.newHashMap(jsonProfileData.getFields());
	        profileCopy.put(JsonNodeFactories.string(profileName), JsonNodeFactories.object(fields));
	        JsonRootNode profileJsonCopy = JsonNodeFactories.object(profileCopy);

	        rootCopy.put(JsonNodeFactories.string("profiles"), profileJsonCopy);

	        jsonProfileData = JsonNodeFactories.object(rootCopy);

	        try
	        {
	            BufferedWriter newWriter = Files.newWriter(launcherProfiles, Charsets.UTF_8);
	            PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(jsonProfileData,newWriter);
	            newWriter.close();
	        }
	        catch (Exception e)
	        {
	            JOptionPane.showMessageDialog(null, "There was a problem writing the launch profile,  is it write protected?", "Error", JOptionPane.ERROR_MESSAGE);
	            return false;
	        }
			return true;
		} else {
			return true;
		}
	}

}
