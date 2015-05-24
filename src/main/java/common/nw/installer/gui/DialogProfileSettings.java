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

	private void chooseExistingProfile()  {
		//open profileFile
		File launcherProfiles = new File(installer.txtMinecraft.getText() + File.separator + "launcher_profiles.json");
		if(!launcherProfiles.exists()) {
			JOptionPane.showMessageDialog(null, "The launcher_profiles.json file is missing!\nYou need to run the minecraft launcher at least once!", "File not found", JOptionPane.ERROR_MESSAGE);
			return;
		}
		//init parser
		JdomParser parser = new JdomParser();
		JsonRootNode jsonProfileData;

		//parse File
		try
		{
			jsonProfileData = parser.parse(Files.newReader(launcherProfiles, Charsets.UTF_8));
		}
		catch (InvalidSyntaxException e)
		{
			JOptionPane.showMessageDialog(null, "The launcher profile file is corrupted. Re-run the minecraft launcher to fix it!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (Exception e)
		{
			throw Throwables.propagate(e);
		}
		//getData
		HashMap<JsonStringNode, JsonNode> profileCopy = Maps.newHashMap(jsonProfileData.getNode("profiles").getFields());
		//format values to String array
		ArrayList<String> options = new ArrayList<String>();
		for(JsonStringNode node : profileCopy.keySet()) {
			options.add(node.getText());
		}
		//error message
		if(options.isEmpty()) {
			JOptionPane.showMessageDialog(null, "No profiles were found!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		//get user input
		String selection = (String) JOptionPane.showInputDialog(this, "Select a profile", "Profiles", JOptionPane.QUESTION_MESSAGE, null, options.toArray(), options.get(0));
		if(selection == null || selection.isEmpty()) {
			return;
		}
		//update data
		for(Map.Entry<JsonStringNode, JsonNode> entry : profileCopy.entrySet()) {
			if(entry.getKey().getText().equals(selection)) {
				//minecraft data
				txtName.setText(entry.getValue().getStringValue("name"));
				txtDirectory.setText(entry.getValue().getStringValue("gameDir"));
				txtJavaOptions.setText(entry.getValue().getStringValue("javaArgs"));
				File modpack = new File(txtDirectory.getText() + File.separator + "modpack.json");
				//modpack data
				if(modpack.exists()) {
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

	/** ok button */
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

	/** cancel button */
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
}
