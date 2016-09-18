package common.nw.updater.launch;

import common.nw.core.utils.SwingUtils;
import common.nw.core.utils.log.NwLogger;
import common.nw.updater.ConsoleListener;
import common.nw.updater.Updater;
import common.nw.updater.gui.IProgressWatcher;
import common.nw.updater.gui.UpdateWindow;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import javax.swing.*;
import java.awt.*;
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

		NwLogger.UPDATER_LOGGER.info("Starting modpack updater!");

		if (useGui && GraphicsEnvironment.isHeadless()) {
			//Check for headless mode
			NwLogger.UPDATER_LOGGER.info("Headless mode enabled!");
			useGui = false;
		}
		if (useGui) {
			SwingUtils.setOSLookAndFeel();
		}

		// init gui
		IProgressWatcher userInterface;
		if (useGui) {
			userInterface = new UpdateWindow();
		} else {
			userInterface = new ConsoleListener();
		}
		userInterface.show();

		boolean quitToLauncher = true;
		try {
			boolean retry = true;
			while (retry) {
				// begin update
				final Updater updater = new Updater(args, gameDir, versionName);
				updater.setListener(userInterface);
				updater.start();
				// wait until update is finished
				while (!updater.isFinished()) {
					Thread.sleep(100L);
				}
				retry = updater.shouldRetry();
				quitToLauncher = updater.quitToLauncher();
			}
		} catch (Exception e) {
			NwLogger.UPDATER_LOGGER.error("Unknown error occurred!", e);
			userInterface.showMessageDialog(e.getMessage(), "Unknown error occured!", JOptionPane.ERROR_MESSAGE);
		}

		NwLogger.UPDATER_LOGGER.info("Update finished!");

		// close gui before process is terminated
		userInterface.close();

		// close minecraft if needed
		if (quitToLauncher) {
			NwLogger.UPDATER_LOGGER.info("Quitting to launcher...");
			NwLogger.UPDATER_LOGGER.info("If you have minecraftforge installed this will error...");
			Runtime.getRuntime().exit(0);
		} else {
			NwLogger.UPDATER_LOGGER.info("Starting minecraft...");
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


	/**
	 * Main entry point
	 * <p>
	 * Allowed arguments:
	 * <ul>
	 * <li>nogui [true/false] - disabled/enables gui</li>
	 * <li>gamedir [dir] - specify working directory</li>
	 * <li>assetdir [dir] - specify directory used for assets (unused)</li>
	 * <li>versionName [string] - minecraft-launcher version name</li>
	 * <li>all arguments used by the updater</li>
	 * </ul>
	 *
	 * @see Updater#parseCommandLineModpack()
	 */
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
			NwLogger.UPDATER_LOGGER.severe("Error parsing commandline!", e);
		}

		Launch launch = new Launch(useGui);
		// launch updater using the working directory as gameFolder
		launch.acceptOptions(Arrays.asList(args),
				new File(gameDir),
				new File(assetDir),
				versionName);
	}

}
