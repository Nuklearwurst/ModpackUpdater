package common.nw.updater.launch;

import common.nw.core.utils.SwingUtils;
import common.nw.core.utils.log.NwLogger;
import common.nw.updater.ConsoleListener;
import common.nw.updater.Updater;
import common.nw.updater.gui.UpdateWindow;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Launch implements ITweaker {

	/**
	 * default launch target
	 */
	private static final String DEFAULT_LAUNCH_TARGET = "net.minecraft.client.main.Main";

	/**
	 * should open gui?
	 */
	private boolean useGui = true;

	/**
	 * probably unused
	 */
	@SuppressWarnings("unused")
	public Launch() {
	}

	/**
	 * used if launched directly
	 */
	@SuppressWarnings("WeakerAccess")
	public Launch(boolean useGui) {
		this.useGui = useGui;
	}

	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir,
	                          String versionName) {

		Updater.logger.info("Starting modpack updater!");
		if (useGui) {
			SwingUtils.setOSLookAndFeel();
		}

		// init updater
		Updater updater = new Updater(args, gameDir, versionName);

		UpdateWindow window = null;
		// init gui
		if (useGui) {
			window = new UpdateWindow(updater);
			window.setVisible(true);
		} else {
			updater.setListener(new ConsoleListener());
		}
		try {
			do {
				// begin update
				updater.beginUpdate();
				// wait until update is finished
				while (!updater.isFinished()) {
					Thread.sleep(100L);
				}
			} while (updater.shouldRetry());
		} catch (Exception e) {
			NwLogger.UPDATER_LOGGER.error("Unknown error occurred!", e);
		}

		Updater.logger.info("Update finished!");

		// close gui before process is terminated
		if (useGui) {
			//noinspection ConstantConditions
			window.close();
		}

		// close minecraft if needed
		if (updater.quitToLauncher()) {
			Updater.logger.info("Quitting to launcher...");
			Updater.logger.info("If you have minecraftforge installed this will error...");
			Runtime.getRuntime().exit(0);
		} else {
			Updater.logger.info("Starting minecraft...");
		}
	}

	@Override
	public void injectIntoClassLoader(LaunchClassLoader classLoader) {
		//We don't have anything to inject into minecraft
	}


	/**
	 * returns the Minecraft start-class
	 */
	@Override
	public String getLaunchTarget() {
		//Default launch target --> Minecraft main
		return DEFAULT_LAUNCH_TARGET;
	}

	@Override
	public String[] getLaunchArguments() {
		//We don't want to pass any arguments to minecraft
		return new String[0];
	}


	public static void main(String[] args) {
		boolean useGui = true;
		String gameDir = "./";
		String assetDir = "./";
		String versionName = null;
		try {
			OptionParser optionParser = new OptionParser();
			ArgumentAcceptingOptionSpec<Boolean> noGuiOption = optionParser
					.accepts("nogui").withOptionalArg().ofType(Boolean.class).defaultsTo(true);
			ArgumentAcceptingOptionSpec<String> gamedirOption = optionParser
					.accepts("gamedir")
					.withRequiredArg().ofType(String.class);
			ArgumentAcceptingOptionSpec<String> assetdirOption = optionParser
					.accepts("assetdir")
					.withRequiredArg().ofType(String.class);
			ArgumentAcceptingOptionSpec<String> versionNameOption = optionParser
					.accepts("versionName")
					.withRequiredArg().ofType(String.class);
			optionParser.allowsUnrecognizedOptions();
			OptionSet options = optionParser.parse(args);

			if (options.has(noGuiOption)) {
				useGui = !noGuiOption.value(options);
			}
			if (options.has(gamedirOption)) {
				assetDir = gameDir = gamedirOption.value(options);
			}
			if (options.has(assetdirOption)) {
				assetDir = assetdirOption.value(options);
			}
			if (options.has(versionNameOption)) {
				versionName = versionNameOption.value(options);
			}
		} catch (Exception e) {
			Updater.logger.severe("Error parsing commandline!", e);
		}

		Launch launch = new Launch(useGui);
		/**
		 * launch updater using the working directory as gameFolder
		 */
		launch.acceptOptions(Arrays.asList(args),
				new File(gameDir),
				new File(assetDir),
				versionName);
	}

}
