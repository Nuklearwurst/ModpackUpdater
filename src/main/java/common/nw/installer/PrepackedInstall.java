package common.nw.installer;

import common.nw.core.utils.log.NwLogger;
import common.nw.installer.gui.InstallerWindow;

import javax.swing.*;
import java.awt.*;

/**
 * used to provide preset modpack installer
 *
 * @author Nuklearwurst
 */
public final class PrepackedInstall {
	@SuppressWarnings("WeakerAccess")
	public static final String MODPACK_URL = "https://dl.dropboxusercontent.com/u/87474141/minecraft/Modpack%204%20%281.7.10%29/modpack.json";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			// look and feel
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (Throwable t) {
				NwLogger.INSTALLER_LOGGER.error("Error when setting Look and Feel!", t);
			}
			try {
				InstallerWindow window = new InstallerWindow(MODPACK_URL);
				window.mainFrame.pack();
				window.mainFrame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

}
