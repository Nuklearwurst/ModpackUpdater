package common.nw.installer;

import argo.format.PrettyJsonFormatter;
import argo.jdom.*;
import argo.saj.InvalidSyntaxException;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import common.nw.modpack.ModpackValues;
import common.nw.modpack.RepoModpack;
import common.nw.utils.DownloadHelper;
import common.nw.utils.Utils;
import common.nw.utils.log.NwLogHelper;
import common.nw.utils.log.NwLogger;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;

public class Installer {

	/**
	 * version Name
	 */
	private String name;
	/**
	 * .minecraft Path
	 */
	private String dir;
	/**
	 * should a profile be created?
	 */
	private boolean createProfile;
	/**
	 * should the libs be downloaded?
	 */
	private boolean downloadLib;


	/**
	 * .minecraft folder
	 */
	private File minecraftDirectory;
	/**
	 * our VersionDirectory
	 */
	private File ourDir;

	private RepoModpack repo;

	private String data;


	public Installer(RepoModpack repo, String name, String dir,
	                 boolean createProfile, boolean downloadLib) {
		this.repo = repo;
		this.name = name;
		this.dir = dir;
		this.createProfile = createProfile;
		minecraftDirectory = new File(dir);
		this.downloadLib = downloadLib;
	}

	/**
	 * downloads modapck.json file
	 *
	 * @return success
	 */
	public static RepoModpack downloadModpack(String url) {
		try {
			String json = null;
			if(!url.startsWith("http:") && !url.startsWith("www.") || !url.contains("/")) {
				//try and read local file
				NwLogger.INSTALLER_LOGGER.info("Modpack URL does not seem to be an internet url! Trying to get local File");
				json = DownloadHelper.getStringFromFile(url, null);
			}
			if (json == null || json.isEmpty()) {
				json = DownloadHelper.getString(url, null);
			}
			if (json == null || json.isEmpty()) {
				return null;
			}
			Gson gson = new Gson();
			return gson.fromJson(json, RepoModpack.class);
		} catch (Exception e) {
			NwLogger.INSTALLER_LOGGER.error("Error downloading Modpack.json", e);
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
		if (notNull) {
			minecraftDirectory = new File(dir);
			return minecraftDirectory.exists() && minecraftDirectory.isDirectory();
		}
		return false;
	}

	/**
	 * create needed dirs
	 *
	 * @return success
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean createDirs() {
		File versions = new File(minecraftDirectory, "versions");
		if (!versions.exists()) {
			if (!versions.mkdir()) {
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
			if (!Utils.deleteFileOrDir(ourDir)) {
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
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
					File lib = new File(minecraftDirectory, "libraries");
					if (!lib.exists()) {
						if (!lib.mkdir()) {
							return false;
						}
					}
					File updater = new File(lib, "common" + File.separator
							+ "nuklearwurst" + File.separator + "updater");
					if (!updater.exists()) {
						if (!updater.mkdirs()) {
							return false;
						}
					}
					String version = s.substring(s.lastIndexOf(":") + 1);
					File dir = new File(updater, version);
					if (!dir.exists()) {
						if (!dir.mkdirs()) {
							return false;
						}
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
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean createJson() {
		try {
			data = DownloadHelper.getString(repo.minecraft.jsonName, null);
		} catch (Exception e) {
			NwLogger.INSTALLER_LOGGER.error("Error downloading version.json", e);
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
		HashMap<JsonStringNode, JsonNode> dataCopy = Maps.newHashMap(versionData.getFields());
		//Add inheritance if needed
		if(repo.minecraft.jarUpdateType.equals(ModpackValues.jarForgeInherit)) {
			String forgeVersion = null;
			try {
				if (repo.minecraft.versionName.contains("/")) {
					//this seems to be direct link, we don't know which version
				} else if (repo.minecraft.versionName.contains("-")) {
					//parse as full version name
					forgeVersion = repo.minecraft.versionName;
				} else {
					//parse as build number
					String s = DownloadHelper.getString(ModpackValues.URL_FORGE_VERSION_JSON, null);
					JdomParser forgeParser = new JdomParser();
					JsonRootNode forgeVersionData = forgeParser.parse(s);
					JsonNode build = forgeVersionData.getNode("number", repo.minecraft.versionName);
					int buildNumber = Integer.parseInt(build.getNumberValue("build"));
					String branch = build.getStringValue("branch");
					if (branch == null) {
						branch = "";
					} else {
						branch = "-" + branch;
					}
					String mcversion = build.getStringValue("mcversion");
					String forgeversion = build.getStringValue("version");
					forgeVersion =  mcversion + "-Forge" + forgeversion + branch;
				}
			} catch (MalformedURLException e) {
				NwLogger.INSTALLER_LOGGER.error("Error parsing Minecraft Forge Installer version...", e);
			} catch (IOException e) {
				NwLogger.INSTALLER_LOGGER.error("Error reading Minecraft Forge Version Data", e);
			} catch (InvalidSyntaxException e) {
				NwLogger.INSTALLER_LOGGER.error("Error parsing Minecraft Forge Version Data", e);
			} catch (NumberFormatException e) {
				NwLogger.INSTALLER_LOGGER.error("Error parsing Minecraft Forge Build Number", e);
			} catch (IllegalArgumentException e) {
				NwLogger.INSTALLER_LOGGER.error("Error parsing Minecraft Forge Version Data", e);
			} catch (Exception e) {
				NwLogger.INSTALLER_LOGGER.error("Unknown Error occurred!", e);
			}
			if(forgeVersion != null) {
				dataCopy.put(JsonNodeFactories.string("inheritsFrom"), JsonNodeFactories.string(forgeVersion));
			}
		}
		//Add Version Id
		dataCopy.put(JsonNodeFactories.string("id"), JsonNodeFactories.string(name));
		versionData = JsonNodeFactories.object(dataCopy);
		try {
			File file = new File(ourDir, name + ".json");
			if (file.exists()) {
				if (!file.delete()) {
					return false;
				}
			}
			BufferedWriter newWriter = Files.newWriter(file, Charsets.UTF_8);
			PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(versionData, newWriter);
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
	@SuppressWarnings("unchecked")
	public boolean createJar(boolean allowGui, Component parentWindow) {
		//delete old file
		//FIXME: this might create errors with forge installs and maybe should be removed
		File file = new File(ourDir, name + ".jar");
		if (file.exists()) {
			if (!file.delete()) {
				return false;
			}
		}
		if (repo.minecraft.versionName != null && !repo.minecraft.versionName.isEmpty()) {
			if (repo.minecraft.jarUpdateType != null) {
				if (repo.minecraft.jarUpdateType.equals(ModpackValues.jarForgeInherit)) {
					NwLogger.INSTALLER_LOGGER.info("Starting Minecraft Forge Installation.");
					try {
						URL url;
						if (repo.minecraft.versionName.contains("/")) {
							//parse as direct Link
							url = new URL(repo.minecraft.versionName);
						} else if (repo.minecraft.versionName.contains("-")) {
							//parse as full version name
							url = new URL(ModpackValues.URL_FORGE_INSTALLER + repo.minecraft.versionName + "/forge-" + repo.minecraft.versionName + "-installer.jar");
						} else {
							//parse as build number
							String s = DownloadHelper.getString(ModpackValues.URL_FORGE_VERSION_JSON, null);
							JdomParser parser = new JdomParser();
							JsonRootNode versionData = parser.parse(s);
							JsonNode build = versionData.getNode("number", repo.minecraft.versionName);
							int buildNumber = Integer.parseInt(build.getNumberValue("build"));
							String branch = build.getStringValue("branch");
							if (branch == null) {
								branch = "";
							} else {
								branch = "-" + branch;
							}
							String mcversion = build.getStringValue("mcversion");
							String forgeversion = build.getStringValue("version");

							if(allowGui) {
								String forgeDir = String.format("%s-Forge%s%s", mcversion, forgeversion, branch);
								File forgeVersionDir = new File(minecraftDirectory, "versions/" + forgeDir);
								NwLogger.INSTALLER_LOGGER.fine("Searching for minecraftforge Installation at: " + forgeVersionDir.getAbsolutePath());
								if (forgeVersionDir.exists() && forgeVersionDir.isDirectory()) {
									File forgeVersionJson = new File(forgeVersionDir, forgeDir + ".json");
									if (forgeVersionJson.exists()) {
										int result = JOptionPane.showConfirmDialog(parentWindow, "A MinecraftForge Installation was found!\nDo you want to skip MinecraftForge Installation?", "MinecraftForge detected!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
										if(result == JOptionPane.YES_OPTION) {
											return true;
										}
									}
								}
							}

							url = new URL(ModpackValues.URL_FORGE_INSTALLER + mcversion + "-" + forgeversion + branch + "/forge-" + mcversion + "-" + forgeversion + branch + "-installer.jar");
						}

						//Class loading
						NwLogger.INSTALLER_LOGGER.fine("Loading MC-Forge Installer...");
						URLClassLoader child = new URLClassLoader(new URL[]{url}, Installer.class.getClassLoader().getParent());
						Class forgeClientInstall = Class.forName("net.minecraftforge.installer.ClientInstall", true, child);
						Method runMethod = forgeClientInstall.getDeclaredMethod("run", File.class);
						Object instance = forgeClientInstall.newInstance();

						//Invoking Run Method
						NwLogger.INSTALLER_LOGGER.fine("Starting Client Installation...");
						Object result = runMethod.invoke(instance, minecraftDirectory);
						if ((Boolean) result) {
							NwLogger.INSTALLER_LOGGER.info("Minecraft Forge Installation finished.");
							return true;
						} else {
							NwLogger.INSTALLER_LOGGER.error("Minecraft Forge Installation has encountered an error!");
							return false;
						}
					} catch (ClassNotFoundException e) {
						NwLogger.INSTALLER_LOGGER.error("Error Loading Minecraft Forge Installer...", e);
					} catch (MalformedURLException e) {
						NwLogger.INSTALLER_LOGGER.error("Error parsing Minecraft Forge Installer version...", e);
					} catch (IOException e) {
						NwLogger.INSTALLER_LOGGER.error("Error reading Minecraft Forge Version Data", e);
					} catch (InvalidSyntaxException e) {
						NwLogger.INSTALLER_LOGGER.error("Error parsing Minecraft Forge Version Data", e);
					} catch (NumberFormatException e) {
						NwLogger.INSTALLER_LOGGER.error("Error parsing Minecraft Forge Build Number", e);
					} catch (IllegalArgumentException e) {
						NwLogger.INSTALLER_LOGGER.error("Error parsing Minecraft Forge Version Data", e);
					} catch (Exception e) {
						NwLogger.INSTALLER_LOGGER.error("Unknown Error occurred!", e);
					}
					return false;
				}
			} else {
				NwLogger.INSTALLER_LOGGER.info("Invalid Jar update type, falling back to direct download...");
				return DownloadHelper.downloadFile(repo.minecraft.versionName, file);
			}
		}
		return true;
	}

	/**
	 * create minecraft launcher profile <br>
	 * code is based on the MinecraftForge-Installer
	 *
	 * @return success of the profile creation
	 * @see <a href=https://github.com/MinecraftForge/Installer>https://github.com/MinecraftForge/Installer</a>
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean createProfile(String profileName, String javaOptions, String gameDirectory, int updateFrequency) {
		if (createProfile) {
			File launcherProfiles = new File(minecraftDirectory, "launcher_profiles.json");
			if (!launcherProfiles.exists()) {
				JOptionPane.showMessageDialog(null, "The launcher_profiles.json file is missing!\nYou need to run the minecraft launcher at least once!", "File not found", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			JdomParser parser = new JdomParser();
			JsonRootNode jsonProfileData;

			try {
				jsonProfileData = parser.parse(Files.newReader(launcherProfiles, Charsets.UTF_8));
			} catch (InvalidSyntaxException e) {
				JOptionPane.showMessageDialog(null, "The launcher profile file is corrupted. Re-run the minecraft launcher to fix it!", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}

			//our Data
			JsonField[] fields = new JsonField[]{
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

			try {
				BufferedWriter newWriter = Files.newWriter(launcherProfiles, Charsets.UTF_8);
				PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(jsonProfileData, newWriter);
				newWriter.close();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "There was a problem writing the launch profile,  is it write protected?", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
		} else {
			return true;
		}
	}

}
