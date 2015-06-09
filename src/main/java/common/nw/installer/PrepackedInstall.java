package common.nw.installer;

import common.nw.installer.gui_legacy.InstallerWindow;
import common.nw.utils.log.NwLogger;

import javax.swing.*;
import java.awt.*;

/**
 * used to provide preset modpack installer
 * @author Nuklearwurst
 */
public final class PrepackedInstall {
	public static final String MODPACK_URL = "https://dl.dropboxusercontent.com/u/87474141/minecraft/Modpack%203%20%281.7.10%29/modpack.json";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				// look and feel
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				} catch (Throwable t) {
					NwLogger.INSTALLER_LOGGER.error("Error when setting Look and Feel!", t);
				}
				try {
					InstallerWindow window = new InstallerWindow(true);
					window.mainFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
