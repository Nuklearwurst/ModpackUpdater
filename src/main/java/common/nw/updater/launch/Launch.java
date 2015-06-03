package common.nw.updater.launch;

import common.nw.updater.ConsoleListener;
import common.nw.updater.Updater;
import common.nw.updater.gui.UpdateWindow;
import common.nw.utils.Utils;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Launch implements ITweaker {

	/** default launch target */
	private static final String DEFAULT_LAUNCH_TARGET = "net.minecraft.client.main.Main";
	
	/** 
	 * the launch arguments 
	 * @see {@link ITweaker} 
	 */
	private final String[] launchArguments = {"modpack", "modpackrepo", "modpackversion"};

	/** should open gui? */
	private boolean useGui = true;

	/** probably unused */
	public Launch() {
	}

	/** used if launched directly */
	public Launch(boolean useGui) {
		this.useGui = useGui;
	}

	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir,
			String profile) {

		Updater.logger.info("Starting modpack updater!");
		if (useGui) {
			Updater.logger.fine("Setting lood and feel for gui");
			Utils.setOSLookAndFeel();
		}

		// init updater
		Updater updater = new Updater(args, gameDir, profile);

		UpdateWindow window = null;
		// init gui
		if (useGui) {
			window = new UpdateWindow(updater);
			window.setVisible(true);
		} else {
			updater.setListener(new ConsoleListener());
		}
		try {
			// begin update
			updater.beginUpdate();
			// wait until update is finished
			while (!updater.isFinished()) {
				Thread.sleep(100L);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Updater.logger.info("Update finished!");
		
		// close gui before process is terminated
		if (useGui) {
			//noinspection ConstantConditions
			window.close();
		}
		
		// close minecraft if needed
		if (updater.quitToLauncher()) {
			//error when exiting
			Updater.logger.warn("FML save exit not available, forcing exit...");
			Runtime.getRuntime().exit(0);
//			try {
//				Class clazz = null;
//				try {
//					clazz = Class.forName("cpw.mods.fml.common.FMLCommonHandler");
//				} catch(ClassNotFoundException e) {
//					try {
//						Updater.logger.fine("Forge Mod Loader not found for minecraft version 1.7...");
//						Updater.logger.fine("searching 1.8 package...");
//						clazz = Class.forName("net.minecraftforge.fml.common.FMLCommonHandler");
//					} catch(ClassNotFoundException e2) {
//						//no minecraft forge found...
//						Updater.logger.info("No Forge Mod Loader found, exiting normally");
//						Runtime.getRuntime().exit(0);
//					}
//				}
//				if(clazz != null) {
//					Updater.logger.info("Using FML exit");
//					Method instance = clazz.getMethod("instance", Void.class);
//					Method exit = clazz.getMethod("exitJava", int.class, boolean.class);
//					exit.invoke(instance.invoke(null), 0, false);
//				}
//			} catch(Exception e) {
//				e.printStackTrace();
//				//error when exiting
//				Updater.logger.info("FML save exit failed, forcing exit...");
//				Runtime.getRuntime().exit(0);
//			}
		} else {
			Updater.logger.info("Starting minecraft...");
		}
	}

	@Override
	public void injectIntoClassLoader(LaunchClassLoader classLoader) {
		
	}

	
	/**
	 * returns the Minecraft start-class
	 */
	@Override
	public String getLaunchTarget() {
		//TODO: remove custom arguments ?
		// https://github.com/MinecraftForge/FML/blob/1.7.10/src/main/java/cpw/mods/fml/common/launcher/FMLTweaker.java
		return DEFAULT_LAUNCH_TARGET;
	}

	@Override
	public String[] getLaunchArguments() {
		return launchArguments;
	}


	public static void main(String[] args) {
		boolean useGui = true;
		String gameDir = "./";
		String assetDir = "./";
		String profileName = "None";
		try {
			OptionParser optionParser = new OptionParser();
			ArgumentAcceptingOptionSpec<Boolean> noGuiOption = optionParser
					.accepts("nogui").withOptionalArg().ofType(Boolean.class).defaultsTo(false);
			ArgumentAcceptingOptionSpec<String> gamedirOption = optionParser
					.accepts("gamedir")
					.withRequiredArg().ofType(String.class).defaultsTo("user.dir");
			ArgumentAcceptingOptionSpec<String> assetdirOption = optionParser
					.accepts("assetdir")
					.withRequiredArg().ofType(String.class);
			ArgumentAcceptingOptionSpec<String> profileNameOption = optionParser
					.accepts("profile")
					.withRequiredArg().ofType(String.class);
			optionParser.allowsUnrecognizedOptions();
			OptionSet options = optionParser.parse(args);

			if(options.has(noGuiOption)) {
				useGui = !noGuiOption.value(options);
			}
			if (options.has(gamedirOption)) {
				assetDir = gameDir = gamedirOption.value(options);
			}
			if (options.has(assetdirOption)) {
				assetDir = gamedirOption.value(options);
			}
			if(options.has(profileNameOption)) {
				profileName = profileNameOption.value(options);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Updater.logger.severe("Error parsing commandline!");
		}

		Launch launch = new Launch(useGui);
		launch.getLaunchArguments(); // testing
		/** 
		 * launch updater using the working directory as gameFolder
		 * TODO support choosing directory via program arguments 
		 */
		launch.acceptOptions(Arrays.asList(args),
				new File(gameDir),
				new File(assetDir),
				profileName);
	}

}
