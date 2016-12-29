package common.nw.installer;

import argo.format.PrettyJsonFormatter;
import argo.jdom.*;
import argo.saj.InvalidSyntaxException;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import common.nw.core.modpack.*;
import common.nw.core.utils.DownloadHelper;
import common.nw.core.utils.FileUtils;
import common.nw.core.utils.Utils;
import common.nw.core.utils.log.NwLogger;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.List;

public class Installer {

	private static final String JSON_MC_ARGUMENTS = "--username ${auth_player_name} --version ${version_name} --gameDir ${game_directory} --assetsDir ${assets_root} --assetIndex ${assets_index_name} --uuid ${auth_uuid} --accessToken ${auth_access_token} --userProperties ${user_properties} --userType ${user_type} --tweakClass common.nw.updater.launch.Launch --tweakClass cpw.mods.fml.common.launcher.FMLTweaker --modpackrepo %s --modpackversion %s --versionType Forge";
	private static final String JSON_TYPE = "release";
	private static final String JSON_TIME = "2015-12-10T00:05:37-0500";

	private static final String JSON_MC_ARGUMENTS_FORGE_PRE_1_10 = "--tweakClass cpw.mods.fml.common.launcher.FMLTweaker --versionType Forge";
	private static final String JSON_MC_ARGUMENTS_FORGE = "--tweakClass net.minecraftforge.fml.common.launcher.FMLTweaker --versionType Forge";
	private static final String JSON_MC_ARGUMENTS_MINECRAFT = "--username ${auth_player_name} --version ${version_name} --gameDir ${game_directory} --assetsDir ${assets_root} --assetIndex ${assets_index_name} --uuid ${auth_uuid} --accessToken ${auth_access_token} --userProperties ${user_properties} --userType ${user_type} ";
	private static final String JSON_MC_ARGUMENTS_UPDATER = "--tweakClass common.nw.updater.launch.Launch --modpackrepo %s --modpackversion %s";

	/**
	 * version Name (mc-launcher)
	 */
	private final String name;

	/**
	 * .minecraft Path
	 */
	private final String dir;

	/**
	 * should a profile be created?
	 * (mc-launcher)
	 */

	private final boolean createProfile;

	/**
	 * should the libs be downloaded?
	 */
	private final boolean downloadLib;


	/**
	 * .minecraft folder
	 */
	private File minecraftDirectory;

	/**
	 * our VersionDirectory (mc-launcher)
	 */
	private File ourDir;

	private final RepoModpack repo;

	/**
	 * json-version information (mc-launcher)
	 */
	private JsonRootNode data;

	/**
	 * download url of this modpack
	 */
	private final String modpackUrl;


	public Installer(RepoModpack repo, String name, String dir, String url,
	                 boolean createProfile, boolean downloadLib) {
		this.repo = repo;
		this.name = name;
		this.dir = dir;
		this.modpackUrl = url;
		this.createProfile = createProfile;
		minecraftDirectory = new File(dir);
		this.downloadLib = downloadLib;
	}

	/**
	 * downloads modpack.json file
	 *
	 * @return success
	 */
	public static RepoModpack downloadModpack(String url) {
		try {
			String json = null;
			if (!url.startsWith("http:") && !url.startsWith("www.") && !url.startsWith("https:") || !url.contains("/")) {
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
	 */
	public boolean validateEntries() {
		boolean notNull = name != null && !name.isEmpty() && !dir.isEmpty();
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
				NwLogger.NW_LOGGER.severe("Error creating version directory!");
				return false;
			}
		} else {
			//compensate wrong upper/lower case
			if (!Utils.deleteFileOrDir(ourDir)) {
				NwLogger.NW_LOGGER.severe("Error deleting old version dir!");
				return false;
			}
			if (!ourDir.mkdir()) {
				NwLogger.NW_LOGGER.severe("Error recreating version dir!");
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
		try {
			List<JsonNode> libCopy = data.getArrayNode("libraries");
			for (JsonNode node : libCopy) {
				final String libName = node.getStringValue("name");
				if (libName.contains("common.nuklearwurst:updater")) {
					String url = node.getStringValue("url");
					if (url.endsWith("/")) {
						url = url.substring(0, url.length() - 1);
					}
					File libraryDirectory = new File(minecraftDirectory, "libraries");
					if (!FileUtils.createDirectoryIfNecessary(libraryDirectory)) {
						NwLogger.INSTALLER_LOGGER.error("Error creating library directory!");
						return false;
					}
					final String version = libName.substring(libName.lastIndexOf(":") + 1);
					File updaterDirectory = new File(libraryDirectory, "common" + File.separator + "nuklearwurst" + File.separator + "updater" + File.separator + version);
					if (!FileUtils.createDirectoriesIfNecessary(updaterDirectory)) {
						NwLogger.INSTALLER_LOGGER.error("Error creating library directory!");
						return false;
					}
					File updaterJarFile = new File(updaterDirectory, "updater-" + version + ".jar");
					String realUrl = url + "/common/nuklearwurst/updater/" + version + "/updater-" + version + ".jar";
					if (!DownloadHelper.downloadFile(realUrl, updaterJarFile)) {
						NwLogger.INSTALLER_LOGGER.error("Error downloading updater jar-file!");
						return false;
					}
				}
			}
		} catch (Exception e) {
			NwLogger.INSTALLER_LOGGER.error("Unknown error when downloading libraries!", e);
			return false;
		}
		return true;
	}


	/**
	 * downloads and parses json version file
	 * <p/>
	 * note: does not write json file to disk
	 *
	 * @see #writeJson()
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean createJson() {
		JdomParser parser = new JdomParser();
		JsonRootNode versionJson;
		HashMap<JsonStringNode, JsonNode> versionDataCopy;

		final JsonStringNode mcArgumentsKey = JsonNodeFactories.string("minecraftArguments");

		if (ModpackValues.Download.jsonGenerate.equals(repo.minecraft.jsonUpdateType)) {
			//Generate new version-json file
			versionDataCopy = Maps.newHashMap();
			versionDataCopy.put(JsonNodeFactories.string("time"), JsonNodeFactories.string(JSON_TIME));
			versionDataCopy.put(JsonNodeFactories.string("type"), JsonNodeFactories.string(JSON_TYPE));
//			versionDataCopy.put(JsonNodeFactories.string("minecraftArguments"), JsonNodeFactories.string(String.format(JSON_MC_ARGUMENTS, modpackUrl, repo.minecraft.version)));
			versionDataCopy.put(mcArgumentsKey, JsonNodeFactories.string(parseMCArguments(String.format(JSON_MC_ARGUMENTS, modpackUrl, repo.minecraft.version), "")));

		} else if (ModpackValues.Download.jsonDirectDownload.equals(repo.minecraft.jsonUpdateType)) {
			//download version-json file
			try {
				String jsonString = DownloadHelper.getString(repo.minecraft.jsonName, null);
				versionJson = parser.parse(jsonString);
				versionDataCopy = Maps.newHashMap(versionJson.getFields());
				final String old = versionDataCopy.get(mcArgumentsKey).getText();
				versionDataCopy.put(mcArgumentsKey, JsonNodeFactories.string(parseMCArguments(old, old)));
			} catch (InvalidSyntaxException e) {
				NwLogger.INSTALLER_LOGGER.error("Error parsing version file!", e);
				return false;
			} catch (IOException e) {
				NwLogger.INSTALLER_LOGGER.error("Error downloading version.json", e);
				return false;
			} catch (Exception e) {
				NwLogger.INSTALLER_LOGGER.error("Unknown error downloading version.json", e);
				return false;
			}
		} else {
			NwLogger.INSTALLER_LOGGER.error("Unsupported version format: " + repo.minecraft.jsonUpdateType);
			return false;
		}

		//Add libraries
		final JsonStringNode libs = JsonNodeFactories.string("libraries");
		final JsonNode libraryNode = versionDataCopy.get(libs);
		final List<JsonNode> libraryList;
		if (libraryNode == null) {
			libraryList = new ArrayList<>();
		} else {
			//init with values of the downloaded version file
			libraryList = new ArrayList<>(libraryNode.getElements());
		}

		try {
			//add custom modpack libs, as specified in modpack.json
			libraryList.addAll(Library.parseJsonListFromStrings(repo.minecraft.libraries));
		} catch (InvalidSyntaxException e) {
			NwLogger.INSTALLER_LOGGER.error("Error reading modpack libraries!", e);
			return false;
		}
		versionDataCopy.put(libs, JsonNodeFactories.array(libraryList));


		//Add inheritance if needed
		if (repo.minecraft.jsonUpdateType.equals(ModpackValues.Download.jsonGenerate)) {
			String forgeVersionFull = null;
			try {
				//noinspection StatementWithEmptyBody
				if (repo.minecraft.versionName.contains("/")) {
					//this seems to be direct link, we don't know which version
				} else if (repo.minecraft.versionName.contains("-")) {
					//parse as full version name
					forgeVersionFull = repo.minecraft.versionName;
				} else {
					//parse as build number
					String s = DownloadHelper.getString(ModpackValues.URL.forgeVersionJson, null);
					JdomParser forgeParser = new JdomParser();
					JsonRootNode forgeVersionData = forgeParser.parse(s);
					JsonNode build = forgeVersionData.getNode("number", repo.minecraft.versionName);
					String branch = build.isStringValue("branch") ? "-" + build.getStringValue("branch") : "";
					String mcversion = build.getStringValue("mcversion");
					String forgeversion = build.getStringValue("version");

					if (FileUtils.compareVersions(mcversion, "1.10.0") == -1) {
						forgeVersionFull = String.format("%s-Forge%s%s", mcversion, forgeversion, branch);
					} else {
						forgeVersionFull = String.format("%s-forge%s-%s%s", mcversion, mcversion, forgeversion, branch);
					}
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
			if (forgeVersionFull != null) {
				versionDataCopy.put(JsonNodeFactories.string("inheritsFrom"), JsonNodeFactories.string(forgeVersionFull));
			}
		}
		//Add Version Id
		versionDataCopy.put(JsonNodeFactories.string("id"), JsonNodeFactories.string(name));
		versionJson = JsonNodeFactories.object(versionDataCopy);

		//save json file data for later use
		data = versionJson;
		return true;
	}

	private String parseMCArguments(String defaultArg, String jsonArgs) {
		if (repo.minecraft.arguments == null || repo.minecraft.arguments.isEmpty()) {
			return defaultArg;
		}
		StringBuilder builder = new StringBuilder();
		for (String s : repo.minecraft.arguments) {
			switch (s) {
				case MCArgument.specialArgPredefined:
					builder.append(jsonArgs).append(" ");
					break;
				case MCArgument.specialArgForgeNew:
					builder.append(JSON_MC_ARGUMENTS_FORGE).append(" ");
					break;
				case MCArgument.specialArgForge:
					builder.append(JSON_MC_ARGUMENTS_FORGE_PRE_1_10).append(" ");
					break;
				case MCArgument.specialArgMinecraft:
					builder.append(JSON_MC_ARGUMENTS_MINECRAFT).append(" ");
					break;
				case MCArgument.specialArgUpdater:
					builder.append(String.format(JSON_MC_ARGUMENTS_UPDATER, modpackUrl, repo.minecraft.version)).append(" ");
					break;
				default:
					builder.append(s).append(" ");
					break;
			}
		}
		return builder.toString().trim();

	}

	/**
	 * writes the saved json file to disk
	 *
	 * @return success
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean writeJson() {
		if (data == null) {
			NwLogger.INSTALLER_LOGGER.error("Error writing json-file: No data available!");
			return false;
		}
		try {
			File file = new File(ourDir, name + ".json");
			if (file.exists()) {
				if (!file.delete()) {
					return false;
				}
			}
			BufferedWriter newWriter = Files.newWriter(file, Charsets.UTF_8);
			PrettyJsonFormatter.fieldOrderPreservingPrettyJsonFormatter().format(data, newWriter);
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
	@SuppressWarnings({"unchecked", "BooleanMethodIsAlwaysInverted"})
	public boolean createJar(boolean allowGui, Component parentWindow) {
		//delete old file
		File file = new File(ourDir, name + ".jar");
		if (file.exists()) {
			if (!file.delete()) {
				return false;
			}
		}
		if (repo.minecraft.versionName != null && !repo.minecraft.versionName.isEmpty()) {
			if (repo.minecraft.jarUpdateType != null) {
				if (repo.minecraft.jarUpdateType.equals(ModpackValues.Download.jarForgeInherit)) {
					NwLogger.INSTALLER_LOGGER.info("Starting Minecraft Forge Installation.");
					try {
						URL url;
						if (repo.minecraft.versionName.contains("/")) {
							//parse as direct Link
							url = new URL(repo.minecraft.versionName);
						} else if (repo.minecraft.versionName.contains("-")) {
							//parse as full version name
							url = new URL(ModpackValues.URL.forgeInstaller + repo.minecraft.versionName + "/forge-" + repo.minecraft.versionName + "-installer.jar");
						} else {
							//parse as build number
							String s = DownloadHelper.getString(ModpackValues.URL.forgeVersionJson, null);
							JdomParser parser = new JdomParser();
							JsonRootNode versionData = parser.parse(s);
							JsonNode build = versionData.getNode("number", repo.minecraft.versionName);
							String branch = build.isStringValue("branch") ? "-" + build.getStringValue("branch") : "";
							String mcversion = build.getStringValue("mcversion");
							String forgeversion = build.getStringValue("version");

							if (allowGui) {
								String forgeDir;
								if (FileUtils.compareVersions(mcversion, "1.10.0") == -1) {
									forgeDir = String.format("%s-Forge%s%s", mcversion, forgeversion, branch);
								} else {
									forgeDir = String.format("%s-forge%s-%s%s", mcversion, mcversion, forgeversion, branch);
								}
								File forgeVersionDir = new File(minecraftDirectory, "versions/" + forgeDir);
								NwLogger.INSTALLER_LOGGER.fine("Searching for minecraftforge Installation at: " + forgeVersionDir.getAbsolutePath());
								if (forgeVersionDir.exists() && forgeVersionDir.isDirectory()) {
									File forgeVersionJson = new File(forgeVersionDir, forgeDir + ".json");
									if (forgeVersionJson.exists()) {
										int result = JOptionPane.showConfirmDialog(parentWindow, "A MinecraftForge Installation was found!\nDo you want to skip MinecraftForge Installation?", "MinecraftForge detected!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
										if (result == JOptionPane.YES_OPTION) {
											return true;
										}
									}
								}
							}

							url = new URL(ModpackValues.URL.forgeInstaller + mcversion + "-" + forgeversion + branch + "/forge-" + mcversion + "-" + forgeversion + branch + "-installer.jar");
						}

						try {

							//Class loading
							NwLogger.INSTALLER_LOGGER.fine("Loading MC-Forge Installer...");
							URLClassLoader child = new URLClassLoader(new URL[]{url}, Installer.class.getClassLoader().getParent());
							Class forgeClientInstall = Class.forName("net.minecraftforge.installer.ClientInstall", true, child);

							Object result;
							try {
								Method runMethod = forgeClientInstall.getDeclaredMethod("run", File.class);
								Object instance = forgeClientInstall.newInstance();

								//Invoking Run Method
								NwLogger.INSTALLER_LOGGER.fine("Starting Client Installation...");
								result = runMethod.invoke(instance, minecraftDirectory);
							} catch (Exception e) {
								NwLogger.INSTALLER_LOGGER.warn("Error Initializing Minecraft Forge Installer...", e);
								Method runMethod = Arrays.stream(forgeClientInstall.getDeclaredMethods())
										.filter((m) -> m.getName().equals("run"))
										.findAny().orElseThrow(NoSuchMethodException::new);
								Object instance = forgeClientInstall.newInstance();

								//Invoking Run Method
								NwLogger.INSTALLER_LOGGER.fine("Starting Client Installation...");
								Object pred = Class.forName("com.google.common.base.Predicates", true, child).getDeclaredMethod("alwaysTrue").invoke(null);
								result = runMethod.invoke(instance, minecraftDirectory, pred);
							}
							if ((Boolean) result) {
								NwLogger.INSTALLER_LOGGER.info("Minecraft Forge Installation finished.");
								return true;
							} else {
								NwLogger.INSTALLER_LOGGER.error("Minecraft Forge Installation has encountered an error!");
								return false;
							}
						} catch (Exception e) {
							NwLogger.INSTALLER_LOGGER.error("Error Executing Minecraft Forge Installer...", e);
							if (allowGui) {
								int result = JOptionPane.showConfirmDialog(parentWindow, "Do you want to manually execute the installer?", "MinecraftForge Installation failed!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
								if (result == JOptionPane.YES_OPTION) {
									try {
										NwLogger.INSTALLER_LOGGER.fine("Loading MC-Forge Installer for manual installation...");
										URLClassLoader child = new URLClassLoader(new URL[]{url}, Installer.class.getClassLoader().getParent());
										Class mainClass = Class.forName("net.minecraftforge.installer.SimpleInstaller", true, child);
										Method main = mainClass.getDeclaredMethod("main", String[].class);
										main.invoke(null, new Object[]{new String[0]});
										return true;
									} catch (Exception ex) {
										NwLogger.INSTALLER_LOGGER.error("Minecraft Forge Installation has encountered an error!", ex);
										return false;
									}
								}
							}
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
	 * code is based on the MinecraftForge-Installer <br>
	 * will also update local modpack version, if file is found
	 *
	 * @return success of the profile creation
	 * @see <a href=https://github.com/MinecraftForge/Installer>https://github.com/MinecraftForge/Installer</a>
	 */
	@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "UnusedParameters"})
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
			HashMap<JsonStringNode, JsonNode> newData = Maps.newHashMap();
			newData.put(JsonNodeFactories.string("name"), JsonNodeFactories.string(profileName));
			newData.put(JsonNodeFactories.string("lastVersionId"), JsonNodeFactories.string(this.name));
			newData.put(JsonNodeFactories.string("gameDir"), JsonNodeFactories.string(gameDirectory));
			newData.put(JsonNodeFactories.string("javaArgs"), JsonNodeFactories.string(javaOptions));


			HashMap<JsonStringNode, JsonNode> rootCopy = Maps.newHashMap(jsonProfileData.getFields());
			HashMap<JsonStringNode, JsonNode> profileCopy = Maps.newHashMap(jsonProfileData.getNode("profiles").getFields());


			JsonNode node = profileCopy.get(JsonNodeFactories.string(profileName));
			//keep data that we don't modify
			if (node != null && node.hasFields()) {
				Map<JsonStringNode, JsonNode> fieldList = Maps.newHashMap(node.getFields());
				fieldList.remove(JsonNodeFactories.string("name"));
				fieldList.remove(JsonNodeFactories.string("gameDir"));
				fieldList.remove(JsonNodeFactories.string("javaArgs"));
				fieldList.remove(JsonNodeFactories.string("lastVersionId"));
				fieldList.putAll(newData);
				profileCopy.put(JsonNodeFactories.string(profileName), JsonNodeFactories.object(fieldList));
			} else {
				profileCopy.put(JsonNodeFactories.string(profileName), JsonNodeFactories.object(newData));
			}


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

			//update version info
			File modpack = new File(gameDirectory, "modpack.json");
			//modpack data
			if (modpack.exists()) {
				try {
					Gson gson = new Gson();
					LocalModpack local = gson.fromJson(new FileReader(modpack),
							LocalModpack.class);
					local.version = repo.minecraft.version;
					FileWriter fileWriter = new FileWriter(modpack);
					gson.toJson(local, fileWriter);
					fileWriter.close();
				} catch (Exception e) {
					NwLogger.INSTALLER_LOGGER.warn("Could not read local modpack.json file of profile: " + profileName, e);
					JOptionPane.showMessageDialog(null, "Error when reading existing modpack.json file!\nInstalltion will continue...", "Warning!", JOptionPane.WARNING_MESSAGE);
				}
			}
			return true;
		} else {
			return true;
		}
	}

}
