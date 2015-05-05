package common.nw.installer.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import common.nw.installer.Installer;
import common.nw.modpack.RepoModpack;
import common.nw.utils.Utils;

public class InstallerWindow {

	private JFrame mainFrame;
	private JTextField txtUrl;
	private JCheckBox chckbxDownloadLib;
	private JButton btnNext;
	private JButton btnBack;
	private JEditorPane txtpnModpackInfo;
	public JTextField txtMinecraft;
	private JTextField txtVersionName;

	public JTextField txtProfile;

	private JLabel lblProgress;

	private CardLayout cl_contentPanel;

	private int currentPage = 0;
	private static final int lastPage = 4;
	private JPanel contentPanel;

	private boolean installing = false;
	private String errorMessage = "";
	private JProgressBar progressBar;

	private RepoModpack modpack;
	private JCheckBox chckbxCreateProfile;
	private JTextPane txtpnFinish;
	private JLabel lblProfileName;
	private JButton btnProfileSettings;

	public String profile_gameDirectory;
	public String profile_javaOptions;
	public int profile_updateFrequency = 0;
	private JTextField txtJVMOptions;

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
				}
				try {
					InstallerWindow window = new InstallerWindow();
					window.mainFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public InstallerWindow() {
		initialize();
		updatePageInfo();
	}

	/**
	 * updates window information, called whenever a site is opened
	 */
	private void updatePageInfo() {
		switch (currentPage) {
		case 0:
			btnBack.setEnabled(false);
			btnNext.setEnabled(true);
			btnNext.setText("Next");
			modpack = null;
			break;
		case 1:
			if (modpack == null) {
				installing = true;
				System.out.println("Downloading modpack.json...");
				new DownloadThread().start();
			} else {
				if (modpack.minecraft.installInfoUrl == null
						|| modpack.minecraft.installInfoUrl.isEmpty()) {
					nextPage();
					break;
				}
			}
			if (installing) {
				btnBack.setEnabled(false);
				btnNext.setEnabled(false);
			} else {
				btnBack.setEnabled(true);
				btnNext.setEnabled(true);
			}
			btnNext.setText("Next");
			break;
		case 2:
			btnBack.setEnabled(true);
			btnNext.setEnabled(true);
			btnNext.setText("Install");
			// settings default values
			if (txtMinecraft.getText() == null
					|| txtMinecraft.getText().isEmpty()) {
				txtMinecraft.setText(Utils.getMinecraftDir());
			}
			if (txtVersionName.getText() == null
					|| txtVersionName.getText().isEmpty()) {
				txtVersionName.setText(modpack.modpackName);
			}
			break;
		case 3:

			errorMessage = "";
			new InstallThread().start();
			if (installing) {
				btnBack.setEnabled(false);
				btnNext.setEnabled(false);
			} else {
				btnBack.setEnabled(true);
				btnNext.setEnabled(true);
			}
			btnNext.setText("Next");
			break;
		case 4:
			btnBack.setEnabled(true);
			btnNext.setEnabled(true);
			btnNext.setText("Finish");
			if (errorMessage == null || errorMessage.isEmpty()) {
				txtpnFinish
						.setText("Installation finished without any errors!");
			} else {
				txtpnFinish.setText("Installation errored! \n" + errorMessage);
			}
			break;
		}

	}

	/**
	 * downloads the modpack.json and loads modpackInformation
	 * 
	 * @author Nukelarwurst
	 * 
	 */
	private class DownloadThread extends Thread {
		@Override
		public void run() {
			String url = txtUrl.getText();
			if (url == null || url.isEmpty()) {
				installing = false;
				JOptionPane.showMessageDialog(mainFrame,
						"Invalid Modpack URL!", "Error",
						JOptionPane.ERROR_MESSAGE);
				currentPage = 0;
				cl_contentPanel.first(contentPanel);
				updatePageInfo();
				return;
			}
			modpack = Installer.downloadModpack(url);
			if (modpack != null) {
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
							txtpnModpackInfo.setPage(info);
						} catch (IOException e) {
							e.printStackTrace();
							System.out
									.println("Error: invalid modpackinfo url!");
							txtpnModpackInfo
									.setText("Error, modpack info could not be downloadded!");
						}
					} else {
						txtpnModpackInfo.setText(info);
					}
					updatePageInfo();
				}
				return;
			} else {
				installing = false;
				JOptionPane
						.showMessageDialog(
								mainFrame,
								"Error when downloading modpack.json!\n Make sure you've got the right link!",
								"Error", JOptionPane.ERROR_MESSAGE);
				cl_contentPanel.first(contentPanel);
				currentPage = 0;
				updatePageInfo();
				return;
			}
		}
	}

	/**
	 * runs the installer
	 * 
	 * @author Nukelarwurst
	 * 
	 */
	private class InstallThread extends Thread {

		@Override
		public void run() {
			// Install
			setProgress("Starting installation...", 0);
			Installer installer = new Installer(modpack,
					txtVersionName.getText(), txtMinecraft.getText(),
					chckbxCreateProfile.isSelected(),
					chckbxDownloadLib.isSelected());

			setProgress("Validating settings...", 5);
			if (!installer.validateEntries()) {
				JOptionPane.showMessageDialog(mainFrame,
						"Some Values are not filled in corecctly!", "Error",
						JOptionPane.ERROR_MESSAGE);
				installing = false;
				previousPage();
				return;
			}

			setProgress("Creating dirs...", 10);
			if (!installer.createDirs()) {
				errorMessage += "\nAn error occured while creating directories! \nPlease check if you have permission!";
				installFinish();
				return;
			}
			setProgress("Creating version.json...", 20);
			if (!installer.createJson()) {
				if (JOptionPane
						.showConfirmDialog(
								mainFrame,
								"Failed creating version.json file! \nDo you want to try again?",
								"Error", JOptionPane.YES_NO_OPTION,
								JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION) {
					if (!installer.createJson()) {
						errorMessage += "\nAn error occured while creating version.json file! \nPlease check if you have permission! \nPlease  check your internet connection!";
						installFinish();
						return;
					}
				} else {
					errorMessage += "\nAn error occured while creating version.json file! \nPlease check if you have permission! \nPlease  check your internet connection!";
					installFinish();
					return;
				}
			}
			setProgress("Creating jar...", 40);
			if (!installer.createJar()) {
				if (JOptionPane.showConfirmDialog(mainFrame,
						"Failed creating version.jar file! "
								+ "\nDo you want to try again?", "Error",
						JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION) {
					if (!installer.createJar()) {
						errorMessage += "\nAn error occured while creating version.jar file! "
								+ "\nPlease check if you have permission! "
								+ "\nPlease  check your internet connection!";
						installFinish();
						return;
					}
				} else {
					errorMessage += "\nAn error occured while creating version.jar file! "
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
						errorMessage += "\nAn error occured while downloading libraries! "
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
					errorMessage += "\nAn error occured while downloading libraries! \nPlease check if you have permission! \nPlease  check your internet connection!";
					installFinish();
					return;
				}
			}
			setProgress("Creating profile...", 90);
			if (!installer.createProfile(txtProfile.getText(), profile_javaOptions, profile_gameDirectory, profile_updateFrequency)) {
				errorMessage += "Failed to create profile! \nMake sure the minecraft launcher is not running!";
				installFinish();
				return;
			}
			setProgress("Installation Complete!", 100);
			installFinish();
		}

	}

	/**
	 * sets the progress of the progressbar/progressLabel (page3)
	 * 
	 * @param s
	 * @param i
	 */
	private void setProgress(String s, int i) {
		System.out.println(s + "  Progress: " + i);
		lblProgress.setText(s);
		progressBar.setValue(i);
	}

	/**
	 * finish installing (and open next page)
	 */
	private void installFinish() {
		installing = false;
		nextPage();
	}

	/**
	 * open the next page
	 */
	private void nextPage() {
		if (currentPage < lastPage) {
			cl_contentPanel.next(contentPanel);
			currentPage++;
			updatePageInfo();
		} else {
			cancel();
		}

	}

	/**
	 * open the previousPage
	 */
	private void previousPage() {
		if (currentPage == lastPage) {
			cl_contentPanel.first(contentPanel);
			currentPage = 0;
			updatePageInfo();
		} else if (currentPage > 0) {
			cl_contentPanel.previous(contentPanel);
			currentPage--;
			updatePageInfo();
		}
	}

	/**
	 * cancel/exit the programm
	 */
	private void cancel() {
		System.exit(0);
	}

	/**
	 * open a dialog to configure profile settings
	 */
	private void openProfileSettingsDialog() {
		DialogProfileSettings dialog = new DialogProfileSettings(mainFrame,
				true, this);
		dialog.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame. (Gui)
	 */
	private void initialize() {
		mainFrame = new JFrame();
		mainFrame.setTitle("Modpack Installer");
		mainFrame.setBounds(100, 100, 450, 300);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.getContentPane().setLayout(new BorderLayout(0, 0));

		cl_contentPanel = new CardLayout();

		contentPanel = new JPanel();
		mainFrame.getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(cl_contentPanel);

		JPanel page0 = new JPanel();
		page0.setBackground(UIManager.getColor("Panel.background"));
		contentPanel.add(page0, "name_35362180514351");

		JTextPane txtpnWelcome = new JTextPane();
		txtpnWelcome.setBackground(SystemColor.menu);
		txtpnWelcome.setFont(new Font("Arial", Font.BOLD, 12));
		txtpnWelcome.setEditable(false);
		txtpnWelcome
				.setText("Welcome to The Modpack Installer\r\n\r\nThis installer will guide you through the modpack installation process.\r\n\r\nNow insert the modpack Url:");

		JLabel lblModpackUrl = new JLabel("Modpack Url:");

		txtUrl = new JTextField();
		txtUrl.setToolTipText("Insert you Modpack Url here.");
		txtUrl.setColumns(10);

		chckbxDownloadLib = new JCheckBox(
				"Download supported Libraries (recommended)");
		chckbxDownloadLib
				.setToolTipText("You should download supportet libraries using the installer. Otherwise you have to do it manually.");
		chckbxDownloadLib.setSelected(true);
		GroupLayout gl_page0 = new GroupLayout(page0);
		gl_page0.setHorizontalGroup(gl_page0
				.createParallelGroup(Alignment.LEADING)
				.addGroup(
						gl_page0.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										gl_page0.createParallelGroup(
												Alignment.LEADING)
												.addGroup(
														gl_page0.createParallelGroup(
																Alignment.LEADING,
																false)
																.addComponent(
																		txtpnWelcome,
																		GroupLayout.PREFERRED_SIZE,
																		GroupLayout.DEFAULT_SIZE,
																		GroupLayout.PREFERRED_SIZE)
																.addGroup(
																		gl_page0.createSequentialGroup()
																				.addComponent(
																						lblModpackUrl)
																				.addPreferredGap(
																						ComponentPlacement.RELATED)
																				.addComponent(
																						txtUrl)))
												.addComponent(chckbxDownloadLib))
								.addContainerGap(24, Short.MAX_VALUE)));
		gl_page0.setVerticalGroup(gl_page0
				.createParallelGroup(Alignment.LEADING)
				.addGroup(
						gl_page0.createSequentialGroup()
								.addContainerGap()
								.addComponent(txtpnWelcome,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(18)
								.addGroup(
										gl_page0.createParallelGroup(
												Alignment.BASELINE)
												.addComponent(lblModpackUrl)
												.addComponent(
														txtUrl,
														GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE,
														GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addComponent(chckbxDownloadLib)
								.addContainerGap(68, Short.MAX_VALUE)));
		page0.setLayout(gl_page0);

		JPanel page1 = new JPanel();
		contentPanel.add(page1, "name_36245393552056");

		JLabel lblModpackOverviewPage = new JLabel("Modpack Overview Page:");

		JScrollPane scrpOverview = new JScrollPane();
		scrpOverview.setPreferredSize(new Dimension(400, 200));

		txtpnModpackInfo = new JEditorPane();
		txtpnModpackInfo.setContentType("text/html");
		txtpnModpackInfo.setEditable(false);
		txtpnModpackInfo.setText("Loading modpack info-page...");
		txtpnModpackInfo.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(e.getURL().toURI());
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (URISyntaxException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});

		scrpOverview.setViewportView(txtpnModpackInfo);
		GroupLayout gl_page1 = new GroupLayout(page1);
		gl_page1.setHorizontalGroup(gl_page1
				.createParallelGroup(Alignment.LEADING)
				.addGroup(
						gl_page1.createSequentialGroup()
								.addGap(152)
								.addComponent(lblModpackOverviewPage,
										GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE).addGap(160))
				.addGroup(
						gl_page1.createSequentialGroup()
								.addContainerGap()
								.addComponent(scrpOverview,
										GroupLayout.DEFAULT_SIZE, 414,
										Short.MAX_VALUE).addContainerGap()));
		gl_page1.setVerticalGroup(gl_page1.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_page1.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblModpackOverviewPage)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(scrpOverview, GroupLayout.DEFAULT_SIZE,
								189, Short.MAX_VALUE).addContainerGap()));
		page1.setLayout(gl_page1);

		JPanel page2 = new JPanel();
		contentPanel.add(page2, "name_36568618986854");

		JScrollPane scrpSettings = new JScrollPane();

		JLabel lblMinecraft = new JLabel("Minecraft:");

		txtMinecraft = new JTextField();
		txtMinecraft.setColumns(10);

		JButton btnOpen = new JButton("Open");
		btnOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File folder = new File(txtMinecraft.getText());
				if (!folder.exists()) {
					folder = new File(Utils.getMinecraftDir());
				}
				String s = Utils.openFolder(mainFrame, folder);
				if (s != null) {
					txtMinecraft.setText(s);
				}
			}
		});

		JLabel lblVersionName = new JLabel("Version name:");

		txtVersionName = new JTextField();
		txtVersionName.setColumns(10);

		JButton btnVersionName = new JButton("Modpack Name");
		btnVersionName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				txtVersionName.setText(modpack.modpackName);
			}
		});

		chckbxCreateProfile = new JCheckBox("Create Profile");
		chckbxCreateProfile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean b = chckbxCreateProfile.isSelected();
				lblProfileName.setEnabled(b);
				txtProfile.setEnabled(b);
				btnProfileSettings.setEnabled(b);
				if (profile_gameDirectory == null
						|| profile_gameDirectory.isEmpty()) {
					profile_gameDirectory = Utils.getMinecraftDir();
					if (profile_gameDirectory == null) {
						openProfileSettingsDialog();
					} else {
						profile_gameDirectory += File.separator + "modpacks"
								+ File.separator + modpack.modpackName;
					}
				}
				if (profile_javaOptions == null
						|| profile_javaOptions.isEmpty()) {
					profile_javaOptions = DialogProfileSettings.DEFAULT_JAVA_OPTIONS;
				}
				if (txtProfile.getText() == null
						|| txtProfile.getText().isEmpty()) {
					txtProfile.setText(modpack.modpackName);
				}
			}
		});

		lblProfileName = new JLabel("Profile name:");
		lblProfileName.setEnabled(false);

		txtProfile = new JTextField();
		txtProfile.setEnabled(false);
		txtProfile.setColumns(10);

		btnProfileSettings = new JButton("Modpack Settings");
		btnProfileSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openProfileSettingsDialog();
			}
		});
		btnProfileSettings.setEnabled(false);
		GroupLayout gl_page2 = new GroupLayout(page2);
		gl_page2.setHorizontalGroup(gl_page2
				.createParallelGroup(Alignment.LEADING)
				.addGroup(
						gl_page2.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										gl_page2.createParallelGroup(
												Alignment.LEADING)
												.addComponent(
														scrpSettings,
														GroupLayout.DEFAULT_SIZE,
														414, Short.MAX_VALUE)
												.addComponent(
														chckbxCreateProfile)
												.addGroup(
														gl_page2.createSequentialGroup()
																.addGap(21)
																.addComponent(
																		lblProfileName)
																.addGap(9)
																.addComponent(
																		txtProfile,
																		GroupLayout.DEFAULT_SIZE,
																		210,
																		Short.MAX_VALUE)
																.addPreferredGap(
																		ComponentPlacement.RELATED)
																.addComponent(
																		btnProfileSettings))
												.addGroup(
														gl_page2.createSequentialGroup()
																.addGroup(
																		gl_page2.createParallelGroup(
																				Alignment.LEADING)
																				.addComponent(
																						lblVersionName)
																				.addComponent(
																						lblMinecraft))
																.addPreferredGap(
																		ComponentPlacement.UNRELATED)
																.addGroup(
																		gl_page2.createParallelGroup(
																				Alignment.LEADING)
																				.addGroup(
																						Alignment.TRAILING,
																						gl_page2.createSequentialGroup()
																								.addComponent(
																										txtMinecraft,
																										GroupLayout.DEFAULT_SIZE,
																										271,
																										Short.MAX_VALUE)
																								.addPreferredGap(
																										ComponentPlacement.RELATED)
																								.addComponent(
																										btnOpen))
																				.addGroup(
																						gl_page2.createSequentialGroup()
																								.addComponent(
																										txtVersionName,
																										GroupLayout.DEFAULT_SIZE,
																										225,
																										Short.MAX_VALUE)
																								.addPreferredGap(
																										ComponentPlacement.RELATED)
																								.addComponent(
																										btnVersionName)))))
								.addContainerGap()));
		gl_page2.setVerticalGroup(gl_page2
				.createParallelGroup(Alignment.LEADING)
				.addGroup(
						gl_page2.createSequentialGroup()
								.addGroup(
										gl_page2.createParallelGroup(
												Alignment.LEADING)
												.addGroup(
														gl_page2.createSequentialGroup()
																.addContainerGap()
																.addComponent(
																		scrpSettings,
																		GroupLayout.PREFERRED_SIZE,
																		GroupLayout.DEFAULT_SIZE,
																		GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(
																		ComponentPlacement.RELATED)
																.addGroup(
																		gl_page2.createParallelGroup(
																				Alignment.BASELINE)
																				.addComponent(
																						lblVersionName)
																				.addComponent(
																						txtVersionName,
																						GroupLayout.PREFERRED_SIZE,
																						GroupLayout.DEFAULT_SIZE,
																						GroupLayout.PREFERRED_SIZE)
																				.addComponent(
																						btnVersionName))
																.addPreferredGap(
																		ComponentPlacement.RELATED)
																.addComponent(
																		btnOpen))
												.addGroup(
														gl_page2.createSequentialGroup()
																.addGap(144)
																.addGroup(
																		gl_page2.createParallelGroup(
																				Alignment.BASELINE)
																				.addComponent(
																						lblMinecraft)
																				.addComponent(
																						txtMinecraft,
																						GroupLayout.PREFERRED_SIZE,
																						GroupLayout.DEFAULT_SIZE,
																						GroupLayout.PREFERRED_SIZE))))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(chckbxCreateProfile)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(
										gl_page2.createParallelGroup(
												Alignment.BASELINE)
												.addComponent(lblProfileName)
												.addComponent(
														txtProfile,
														GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE,
														GroupLayout.PREFERRED_SIZE)
												.addComponent(
														btnProfileSettings))
								.addContainerGap(GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)));

		JTextPane txtpnInstallerSettings = new JTextPane();
		scrpSettings.setViewportView(txtpnInstallerSettings);
		txtpnInstallerSettings.setBackground(SystemColor.menu);
		txtpnInstallerSettings.setFont(new Font("Arial", Font.BOLD, 12));
		txtpnInstallerSettings
				.setText("Installer Settings:\r\n\r\nNow select your .minecraft-folder (usually preset) and the version name (used inside the mc-launcher)\r\n\r\nYou can also create a Profile.");
		page2.setLayout(gl_page2);

		JPanel page3 = new JPanel();
		contentPanel.add(page3, "name_37458451519289");
		GridBagLayout gbl_page3 = new GridBagLayout();
		gbl_page3.columnWidths = new int[] { 0, 0 };
		gbl_page3.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_page3.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_page3.rowWeights = new double[] { 1.0, 0.0, 0.0, 1.0,
				Double.MIN_VALUE };
		page3.setLayout(gbl_page3);

		lblProgress = new JLabel("Doing something...");
		GridBagConstraints gbc_lblProgress = new GridBagConstraints();
		gbc_lblProgress.insets = new Insets(0, 0, 5, 0);
		gbc_lblProgress.gridx = 0;
		gbc_lblProgress.gridy = 1;
		page3.add(lblProgress, gbc_lblProgress);

		progressBar = new JProgressBar();
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.insets = new Insets(0, 0, 5, 0);
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 2;
		page3.add(progressBar, gbc_progressBar);

		JPanel page4 = new JPanel();
		contentPanel.add(page4, "name_37719617272888");

		txtpnFinish = new JTextPane();
		txtpnFinish.setBackground(SystemColor.menu);
		txtpnFinish.setText("Installation finished!");
		
		JTextPane txtpnToUseThe = new JTextPane();
		txtpnToUseThe.setText("To use the modpack select the versionname you chose\r\nin the profile editor under 'use Version'\r\nYou might also want to change the Game-Directory and \r\nJVM-arguments (for bigger modpacks)");
		
		txtJVMOptions = new JTextField();
		txtJVMOptions.setEditable(false);
		txtJVMOptions.setText("-Xmx2G -XX:PermSize=256m -XX:MaxPermSize=512m");
		txtJVMOptions.setColumns(10);
		
		JLabel lblExampleJvmarguments = new JLabel("Example JVM-arguments:");
		GroupLayout gl_page4 = new GroupLayout(page4);
		gl_page4.setHorizontalGroup(
			gl_page4.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_page4.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_page4.createParallelGroup(Alignment.TRAILING)
						.addComponent(txtpnFinish, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
						.addComponent(txtJVMOptions, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
						.addComponent(lblExampleJvmarguments, Alignment.LEADING)
						.addComponent(txtpnToUseThe, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_page4.setVerticalGroup(
			gl_page4.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_page4.createSequentialGroup()
					.addContainerGap()
					.addComponent(txtpnFinish, GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(txtpnToUseThe, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblExampleJvmarguments)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(txtJVMOptions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		page4.setLayout(gl_page4);

		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		mainFrame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		btnBack = new JButton("Back");
		btnBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				previousPage();
			}
		});
		buttonPanel.add(btnBack);

		btnNext = new JButton("Next");
		btnNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextPage();
			}
		});
		buttonPanel.add(btnNext);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				cancel();
			}
		});
		buttonPanel.add(btnCancel);
	}
}
