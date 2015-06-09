package common.nw.installer.gui;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import argo.jdom.JsonStringNode;
import argo.saj.InvalidSyntaxException;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import common.nw.installer.gui_legacy.InstallerWindow;
import common.nw.modpack.LocalModpack;
import common.nw.utils.Utils;
import common.nw.utils.log.NwLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DialogProfileSettings extends JDialog {
	private JPanel contentPane;
	private JButton btnOk;
	private JButton btnCancel;
	private JButton btnUseProfile;
	private JTextField txtName;
	private JButton btnReset;
	private JTextField txtDirectory;
	private JButton btnOpen;
	private JRadioButton rdbtnDay;
	private JRadioButton rdbtnLaunch;
	private JRadioButton rdbtnWeek;
	private JRadioButton rdbtnCustom;
	private JFormattedTextField txtFreq;
	private JTextField txtJavaOptions;
	private JButton btnDefault;
	private ButtonGroup btnGroupUpdateFreq;

	private InstallerWindow installer;

	public static final String DEFAULT_JAVA_OPTIONS = "-Xmx2G -XX:PermSize=256m -XX:MaxPermSize=512m";

	public DialogProfileSettings(Frame parent, boolean modal,
	                             InstallerWindow installer) {
		super(parent, modal);
		this.installer = installer;

		setTitle("Profile Settings");

		setBounds(parent.getX() + 10, parent.getY() + 10, 400, 260);

		setContentPane(contentPane);
		getRootPane().setDefaultButton(btnOk);


		btnReset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetName();
			}
		});

		btnOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String s;
				if (txtDirectory.getText() == null
						|| txtDirectory.getText().isEmpty()) {
					s = Utils.getMinecraftDir();
				} else {
					s = txtDirectory.getText();
				}
				s = Utils.openFolder(contentPane,
						new File(s));
				if (s != null) {
					txtDirectory.setText(s);
				}
			}
		});

		btnOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				finish();
			}
		});

		btnUseProfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chooseExistingProfile();
			}
		});

		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});

		btnReset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				txtJavaOptions.setText(DEFAULT_JAVA_OPTIONS);
			}
		});

		rdbtnLaunch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateRadioButtons();
			}
		});

		rdbtnDay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateRadioButtons();
			}
		});

		rdbtnWeek.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateRadioButtons();
			}
		});

		rdbtnCustom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateRadioButtons();
			}
		});

		txtFreq.setValue(0);

		init();
	}

	private void chooseExistingProfile() {
		//open profileFile
		File launcherProfiles = new File(installer.txtMinecraft.getText() + File.separator + "launcher_profiles.json");
		if (!launcherProfiles.exists()) {
			JOptionPane.showMessageDialog(null, "The launcher_profiles.json file is missing!\nYou need to run the minecraft launcher at least once!", "File not found", JOptionPane.ERROR_MESSAGE);
			return;
		}
		//init parser
		JdomParser parser = new JdomParser();
		JsonRootNode jsonProfileData;

		//parse File
		try {
			jsonProfileData = parser.parse(Files.newReader(launcherProfiles, Charsets.UTF_8));
		} catch (InvalidSyntaxException e) {
			JOptionPane.showMessageDialog(null, "The launcher profile file is corrupted. Re-run the minecraft launcher to fix it!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		//getData
		HashMap<JsonStringNode, JsonNode> profileCopy = Maps.newHashMap(jsonProfileData.getNode("profiles").getFields());
		//format values to String array
		ArrayList<String> options = new ArrayList<String>();
		for (JsonStringNode node : profileCopy.keySet()) {
			options.add(node.getText());
		}
		//error message
		if (options.isEmpty()) {
			JOptionPane.showMessageDialog(null, "No profiles were found!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		//get user input
		String selection = (String) JOptionPane.showInputDialog(this, "Select a profile", "Profiles", JOptionPane.QUESTION_MESSAGE, null, options.toArray(), options.get(0));
		if (selection == null || selection.isEmpty()) {
			return;
		}
		//update data
		for (Map.Entry<JsonStringNode, JsonNode> entry : profileCopy.entrySet()) {
			if (entry.getKey().getText().equals(selection)) {
				//minecraft data
				txtName.setText(entry.getValue().getStringValue("name"));
				txtDirectory.setText(entry.getValue().getStringValue("gameDir"));
				txtJavaOptions.setText(entry.getValue().getStringValue("javaArgs"));
				File modpack = new File(txtDirectory.getText() + File.separator + "modpack.json");
				//modpack data
				if (modpack.exists()) {
					try {
						LocalModpack local = new Gson().fromJson(new FileReader(modpack),
								LocalModpack.class);
						rdbtnCustom.setSelected(true);
						txtFreq.setText(local.updateFrequency + "");
						txtFreq.setEnabled(true);
					} catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(this, "Error when reading existing modpack.json file!", "Warning!", JOptionPane.WARNING_MESSAGE);
					}
				}
				return;
			}
		}

	}

	private void resetName() {
		txtName.setText(installer.txtProfile.getText());
	}

	private void updateRadioButtons() {
		if (rdbtnCustom.isSelected()) {
			txtFreq.setEnabled(true);
		} else {
			txtFreq.setEnabled(false);
		}
		txtFreq.setText("" + getUpdateFrequency());
	}

	/**
	 * ok button
	 */
	private void finish() {
		installer.profile_gameDirectory = txtDirectory.getText();
		installer.profile_javaOptions = txtJavaOptions.getText();
		installer.profile_updateFrequency = getUpdateFrequency();
		installer.txtProfile.setText(txtName.getText());
		this.dispose();
	}

	private int getUpdateFrequency() {
		String s = btnGroupUpdateFreq.getSelection().getActionCommand();
		if (s.equals("launch")) {
			return 0;
		}
		if (s.equals("day")) {
			return 1;
		}
		if (s.equals("week")) {
			return 7;
		}
		if (s.equals("custom")) {
			String freq = txtFreq.getText();
			try {
				return Integer.parseInt(freq);
			} catch (NumberFormatException e) {
				NwLogger.INSTALLER_LOGGER.warn("Error parsing Update Frequency (" + s + ")", e);
			}
		}
		return 0;
	}

	/**
	 * cancel button
	 */
	private void cancel() {
		this.dispose();
	}

	/**
	 * read values
	 */
	private void init() {
		txtName.setText(installer.txtProfile.getText());
		txtDirectory.setText(installer.profile_gameDirectory);
		txtJavaOptions.setText(installer.profile_javaOptions);
		txtFreq.setText("" + installer.profile_updateFrequency);
		switch (installer.profile_updateFrequency) {
			case 0:
				rdbtnDay.setSelected(true);
				break;
			case 1:
				rdbtnDay.setSelected(true);
				break;
			case 7:
				rdbtnWeek.setSelected(true);
				break;
			default:
				rdbtnCustom.setSelected(true);
				break;
		}
		updateRadioButtons();
	}

	{
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		contentPane = new JPanel();
		contentPane.setLayout(new GridBagLayout());
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 2, new Insets(5, 5, 5, 5), -1, -1));
		GridBagConstraints gbc;
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		contentPane.add(panel1, gbc);
		final Spacer spacer1 = new Spacer();
		panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
		panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		btnOk = new JButton();
		btnOk.setText("OK");
		panel2.add(btnOk, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnCancel = new JButton();
		btnCancel.setText("Cancel");
		panel2.add(btnCancel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnUseProfile = new JButton();
		btnUseProfile.setText("Use existing profile");
		panel2.add(btnUseProfile, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayoutManager(7, 6, new Insets(5, 5, 5, 5), -1, -1));
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		contentPane.add(panel3, gbc);
		final JLabel label1 = new JLabel();
		label1.setText("Profile Name:");
		panel3.add(label1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		txtName = new JTextField();
		panel3.add(txtName, new GridConstraints(0, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		btnReset = new JButton();
		btnReset.setText("Reset");
		panel3.add(btnReset, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Game Directory:");
		panel3.add(label2, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		txtDirectory = new JTextField();
		panel3.add(txtDirectory, new GridConstraints(1, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		btnOpen = new JButton();
		btnOpen.setText("Open");
		panel3.add(btnOpen, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label3 = new JLabel();
		label3.setText("Update Frequency:");
		panel3.add(label3, new GridConstraints(2, 0, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		panel3.add(spacer2, new GridConstraints(6, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		rdbtnLaunch = new JRadioButton();
		rdbtnLaunch.setActionCommand("launch");
		rdbtnLaunch.setSelected(true);
		rdbtnLaunch.setText("Every Launch");
		panel3.add(rdbtnLaunch, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
		rdbtnCustom = new JRadioButton();
		rdbtnCustom.setActionCommand("custom");
		rdbtnCustom.setText("Every");
		panel3.add(rdbtnCustom, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
		rdbtnWeek = new JRadioButton();
		rdbtnWeek.setActionCommand("week");
		rdbtnWeek.setText("Every Week");
		panel3.add(rdbtnWeek, new GridConstraints(3, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label4 = new JLabel();
		label4.setText("Java Options:");
		panel3.add(label4, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnDefault = new JButton();
		btnDefault.setText("Default");
		panel3.add(btnDefault, new GridConstraints(5, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		txtJavaOptions = new JTextField();
		panel3.add(txtJavaOptions, new GridConstraints(5, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		txtFreq = new JFormattedTextField();
		txtFreq.setColumns(10);
		txtFreq.setText("0");
		panel3.add(txtFreq, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(40, -1), new Dimension(40, 24), new Dimension(40, -1), 0, false));
		rdbtnDay = new JRadioButton();
		rdbtnDay.setActionCommand("day");
		rdbtnDay.setText("Every Day");
		panel3.add(rdbtnDay, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label5 = new JLabel();
		label5.setText("Days");
		panel3.add(label5, new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnGroupUpdateFreq = new ButtonGroup();
		btnGroupUpdateFreq.add(rdbtnLaunch);
		btnGroupUpdateFreq.add(rdbtnCustom);
		btnGroupUpdateFreq.add(rdbtnDay);
		btnGroupUpdateFreq.add(rdbtnWeek);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return contentPane;
	}
}
