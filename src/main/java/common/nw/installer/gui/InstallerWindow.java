package common.nw.installer.gui;

import common.nw.gui.PageHolder;
import common.nw.installer.Installer;
import common.nw.installer.PrepackedInstall;
import common.nw.modpack.ModpackValues;
import common.nw.modpack.RepoModpack;
import common.nw.modpack.VersionInfo;
import common.nw.utils.Utils;
import common.nw.utils.log.NwLogger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * @author Nuklearwurst
 */
public class InstallerWindow {

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		createUIComponents();
		content_panel = new JPanel();
		content_panel.setLayout(new BorderLayout(0, 0));
		btn_panel = new JPanel();
		btn_panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		content_panel.add(btn_panel, BorderLayout.SOUTH);
		btnBack = new JButton();
		btnBack.setText("Back");
		btnBack.setMnemonic('B');
		btnBack.setDisplayedMnemonicIndex(0);
		btn_panel.add(btnBack);
		btnNext = new JButton();
		btnNext.setText("Next");
		btnNext.setMnemonic('N');
		btnNext.setDisplayedMnemonicIndex(0);
		btn_panel.add(btnNext);
		btnCancel = new JButton();
		btnCancel.setText("Cancel");
		btnCancel.setMnemonic('C');
		btnCancel.setDisplayedMnemonicIndex(0);
		btn_panel.add(btnCancel);
		content_panel.add(card_panel, BorderLayout.CENTER);
		card_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(4, 4, 8, 4), null));
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return content_panel;
	}

	/**
	 * downloads the modpack.json and loads modpackInformation
	 *
	 * @author Nukelarwurst
	 */
	private class DownloadThread extends Thread {
		@Override
		public void run() {
			//read the modpack url
			String url = page0.txtUrl.getText();
			if (url == null || url.isEmpty()) {
				//Error: no url specified
				installing = false;
				JOptionPane.showMessageDialog(mainFrame,
						"Please enter a modpack url!", "Error",
						JOptionPane.ERROR_MESSAGE);
				pageHolder.firstPage();
				updatePage();
				return;
			}
			modpack = Installer.downloadModpack(url);
			if (modpack != null) {
				if (modpack.updaterRevision > VersionInfo.REPO_MODPACK_REVISION) {
					if (JOptionPane.showConfirmDialog(mainFrame, "This modpack requires a newer of the installer!\nDo you want to continue anyways?\n(This may not work and might corrupt your minecraft installation)", "Newer version needed!", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) != JOptionPane.YES_OPTION) {
						//Error: old installer
						installing = false;
						pageHolder.firstPage();
						updatePage();
						return;
					}
				}
				//success
				installing = false;
				String info = modpack.minecraft.installInfoUrl;
				if (info == null) {
					nextPage();
				} else {
					if (info.startsWith("http://")
							|| info.startsWith("https://")
							|| info.startsWith("www.")) {
						try {
							if (info.startsWith("www.")) {
								info = "http://" + info;
							}
							page1.txtpnModpackInfo.setPage(info);
						} catch (IOException e) {
							NwLogger.INSTALLER_LOGGER.error("Error: invalid modpackinfo url!", e);
							page1.txtpnModpackInfo.setText("Error, modpack info could not be downloadded!");
						}
					} else {
						page1.txtpnModpackInfo.setText(info);
					}
					updatePage();
				}
			} else {
				//Error: invalid download
				installing = false;
				JOptionPane.showMessageDialog(
						mainFrame,
						"Error when downloading modpack.json!\n Make sure you've got the right link!",
						"Error", JOptionPane.ERROR_MESSAGE);
				pageHolder.firstPage();
				updatePage();
			}
		}
	}

	/**
	 * runs the installer
	 *
	 * @author Nukelarwurst
	 */
	private class InstallThread extends Thread {

		@Override
		public void run() {
			// Install
			setProgress("Starting installation...", 0);
			Installer installer = new Installer(modpack,
					page2.txtVersionName.getText(), page2.txtMinecraft.getText(),
					page2.chbxCreateProfile.isSelected(),
					page0.chbxDownloadLibraries.isSelected());

			setProgress("Validating settings...", 5);
			if (!installer.validateEntries()) {
				JOptionPane.showMessageDialog(mainFrame,
						"Some Values are not filled in correctly!", "Error",
						JOptionPane.ERROR_MESSAGE);
				installing = false;
				previousPage();
				return;
			}

			setProgress("Preparing Minecraft directories", 10);
			if (!installer.createDirs()) {
				errorMessage += "\nAn error occurred while creating directories! \nPlease check if you have permission!";
				installFinish();
				return;
			}
			setProgress("Downloading Minecraft Version file", 20);
			if (!installer.createJson()) {
				if (JOptionPane
						.showConfirmDialog(
								mainFrame,
								"Failed creating version.json file! \nDo you want to try again?",
								"Error", JOptionPane.YES_NO_OPTION,
								JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION) {
					if (!installer.createJson()) {
						errorMessage += "\nAn error occurred while creating version.json file! \nPlease check if you have permission! \nPlease  check your internet connection!";
						installFinish();
						return;
					}
				} else {
					errorMessage += "\nAn error occurred while creating version.json file! \nPlease check if you have permission! \nPlease  check your internet connection!";
					installFinish();
					return;
				}
			}
			setProgress(modpack.minecraft.jarUpdateType.equals(ModpackValues.jarForgeInherit) ? "Downloading and executing MinecraftForge, this may take a while..." : "Downloading Minecraft Jar", 40);
			if (!installer.createJar(true, content_panel)) {
				if (JOptionPane.showConfirmDialog(mainFrame,
						"Failed creating version.jar file! "
								+ "\nDo you want to try again?", "Error",
						JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION) {
					if (!installer.createJar(true, content_panel)) {
						errorMessage += "\nAn error occurred while creating version.jar file! "
								+ "\nPlease check if you have permission! "
								+ "\nPlease  check your internet connection!";
						installFinish();
						return;
					}
				} else {
					errorMessage += "\nAn error occurred while creating version.jar file! "
							+ "\nPlease check if you have permission! "
							+ "\nPlease  check your internet connection!";
					installFinish();
					return;
				}
			}
			setProgress("Downloading updater...", 75);
			if (!installer.downloadLibraries()) {
				if (JOptionPane.showConfirmDialog(mainFrame,
						"Failed downloading libraries! "
								+ "\nDo you want to try again?", "Error",
						JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION) {
					if (!installer.downloadLibraries()) {
						errorMessage += "\nAn error occurred while downloading libraries! "
								+ "\nPlease check if you have permission! "
								+ "\nPlease  check your internet connection!";
						if (JOptionPane.showConfirmDialog(mainFrame,
								"Failed downloading libraries! "
										+ "\nDo you want to continue anyway?",
								"Error", JOptionPane.YES_NO_OPTION,
								JOptionPane.ERROR_MESSAGE) != JOptionPane.YES_OPTION) {
							installFinish();
							return;
						}
					}
				} else {
					errorMessage += "\nAn error occurred while downloading libraries! \nPlease check if you have permission! \nPlease  check your internet connection!";
					installFinish();
					return;
				}
			}
			setProgress("Creating profile...", 90);
			if (!installer.createProfile(page2.txtProfile.getText(), profile_javaOptions, profile_gameDirectory, profile_updateFrequency)) {
				errorMessage += "Failed to create profile! \nMake sure the minecraft launcher is not running!";
				installFinish();
				return;
			}
			setProgress("Installation Complete!", 100);
			installFinish();
		}

	}

	/**
	 * Main Window
	 */
	public JFrame mainFrame;
	/////////////////////////////////////////
	// Button Panel and other gui elements //
	/////////////////////////////////////////
	private JButton btnBack;
	private JPanel content_panel;
	private JButton btnNext;
	private JButton btnCancel;
	private JPanel card_panel;
	private JPanel btn_panel;

	///////////
	// Pages //
	///////////
	private PanelInit page0;
	private PanelOverview page1;
	protected PanelSettings page2;
	private PanelLoading page3;
	private PanelFinish page4;

	/**
	 * btnNext Text
	 */
	private static final String[] btnNextText = {"Next", "Next", "Install", "Install", "Finish"};

	/**
	 * Page Holder
	 */
	private PageHolder pageHolder;

	//////////////////
	// Modpack Data //
	//////////////////

	public String profile_gameDirectory;
	public String profile_javaOptions;
	public int profile_updateFrequency = 0;
	protected RepoModpack modpack;

	/**
	 * true when an installation or download is in progress
	 */
	private boolean installing = false;

	/**
	 * error log
	 */
	private String errorMessage = "";

	public InstallerWindow() {
		this(false);
	}

	public InstallerWindow(boolean preset) {
		this(preset ? PrepackedInstall.MODPACK_URL : null);
	}

	public InstallerWindow(String url) {
		//Initialize UI
		mainFrame = new JFrame("Installer Window");
		mainFrame.setPreferredSize(new Dimension(450, 300));
		mainFrame.setMinimumSize(new Dimension(400, 300));
		mainFrame.setLocationByPlatform(true);
		$$$setupUI$$$();
		mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		mainFrame.setContentPane(content_panel);
		content_panel.setBorder(new EmptyBorder(2, 5, 2, 5));

		page0 = new PanelInit();
		page1 = new PanelOverview();
		page2 = new PanelSettings(this);
		page3 = new PanelLoading();
		page4 = new PanelFinish();

		pageHolder.addPage(page0);
		pageHolder.addPage(page1);
		pageHolder.addPage(page2);
		pageHolder.addPage(page3);
		pageHolder.addPage(page4);


		btnNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextPage();
			}
		});
		btnBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				previousPage();
			}
		});
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				exit();
			}
		});

		updatePage();

		if (url != null) {
			page0.txtUrl.setText(url);
			nextPage();
		}
	}

	private void exit() {
		mainFrame.dispose();
		System.exit(0);
	}

	private void updatePage() {
		//update modpack
		switch (pageHolder.getCurrentPageIndex()) {
			case 0: //reset
				modpack = null;
				break;
			case 1: //download modpack, or go next page
				if (modpack == null) {
					installing = true;
					NwLogger.INSTALLER_LOGGER.info("Downloading modpack.json...");
					new DownloadThread().start();
				} else {
					if (modpack.minecraft.installInfoUrl == null
							|| modpack.minecraft.installInfoUrl.isEmpty()) {
						nextPage();
						break;
					}
				}
				break;
			case 2:
				// settings default values
				if (page2.txtMinecraft.getText() == null
						|| page2.txtMinecraft.getText().isEmpty()) {
					page2.txtMinecraft.setText(Utils.getMinecraftDir());
				}
				if (page2.txtVersionName.getText() == null
						|| page2.txtVersionName.getText().isEmpty()) {
					page2.txtVersionName.setText(modpack.modpackName);
				}
				break;
			case 3:

				errorMessage = "";
				installing = true;
				new InstallThread().start();
				break;
			case 4:
				btnBack.setEnabled(true);
				btnNext.setEnabled(true);
				btnNext.setText("Finish");
				if (errorMessage == null || errorMessage.isEmpty()) {
					page4.txtpnFinish.setText("Installation finished without any errors!");
				} else {
					page4.txtpnFinish.setText("Installation errored! \n" + errorMessage);
				}
				break;
		}

		// updateButtons
		btnBack.setEnabled(!pageHolder.isFirstPage() && !installing);
		btnNext.setEnabled(!installing);
		btnCancel.setEnabled(!pageHolder.isLastPage());
		btnNext.setText(btnNextText[pageHolder.getCurrentPageIndex()]);
	}

	private void nextPage() {
		if (pageHolder.isLastPage()) {
			exit();
			return;
		}
		pageHolder.nextPage();
		updatePage();
	}

	private void previousPage() {
		if (!pageHolder.isFirstPage()) {
			if (pageHolder.isLastPage()) {
				pageHolder.firstPage();
			} else {
				pageHolder.previousPage();
			}
			updatePage();
		}
	}

	/**
	 * open a dialog to configure profile settings
	 */
	protected void openProfileSettingsDialog() {
		DialogProfileSettings dialog = new DialogProfileSettings(mainFrame, true, this);
		dialog.setVisible(true);
	}

	/**
	 * sets the progress of the progressbar/progressLabel (page3)
	 */
	private void setProgress(String s, int i) {
		NwLogger.INSTALLER_LOGGER.info(s + "  Progress: " + i);
		page3.lblProgress.setText(s);
		page3.progressbar.setValue(i);
	}

	/**
	 * finish installing (and open next page)
	 */
	private void installFinish() {
		installing = false;
		nextPage();
	}

	// Main entry point
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
					InstallerWindow window = new InstallerWindow();
					window.mainFrame.pack();
					window.mainFrame.setVisible(true);
				} catch (Exception e) {
					NwLogger.INSTALLER_LOGGER.error("Error when starting GUI!", e);
				}
			}
		});
	}

	private void createUIComponents() {
		pageHolder = new PageHolder();
		card_panel = pageHolder.getPanel();
	}

}
