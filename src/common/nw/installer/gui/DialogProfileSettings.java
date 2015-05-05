package common.nw.installer.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

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

public class DialogProfileSettings extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField txtProfileName;
	private JTextField txtGameDirectory;
	private JFormattedTextField txtUpdateFreq;
	private JTextField txtJavaOptions;

	public static final String DEFAULT_JAVA_OPTIONS = "-Xmx2G -XX:PermSize=256m -XX:MaxPermSize=512m";
	private final ButtonGroup buttonGroup = new ButtonGroup();

	private InstallerWindow installer;
	private JRadioButton rdbtnLaunch;
	private JRadioButton rdbtnDay;
	private JRadioButton rdbtnWeek;
	private JRadioButton rdbtnCustom;

	/**
	 * Create the dialog.
	 */
	public DialogProfileSettings(Frame parent, boolean modal,
			InstallerWindow installer) {
		super(parent, modal);
		this.installer = installer;

		setTitle("Profile Settings");

		setBounds(100, 100, 380, 233);

		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		JLabel lblProfileName = new JLabel("Profile Name:");

		txtProfileName = new JTextField();
		txtProfileName.setColumns(10);

		JButton btnModpackName = new JButton("Reset");
		btnModpackName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetName();
			}
		});

		JLabel lblGameDirectory = new JLabel("Game Directory:");

		txtGameDirectory = new JTextField();
		txtGameDirectory.setColumns(10);

		JButton btnOpen = new JButton("Open");
		btnOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String s;
				if (txtGameDirectory.getText() == null
						|| txtGameDirectory.getText().isEmpty()) {
					s = Utils.openFolder(contentPanel,
							new File(Utils.getMinecraftDir()));
				} else {
					s = txtGameDirectory.getText();
				}
				if (s != null) {
					txtGameDirectory.setText(s);
				}
			}
		});

		JLabel lblUpdateFrequency = new JLabel("Update Frequency:");

		rdbtnLaunch = new JRadioButton("Every Launch");
		rdbtnLaunch.setActionCommand("launch");
		rdbtnLaunch.setSelected(true);
		rdbtnLaunch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateRadioButtons();
			}
		});
		buttonGroup.add(rdbtnLaunch);

		rdbtnDay = new JRadioButton("Every Day");
		rdbtnDay.setActionCommand("day");
		rdbtnDay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateRadioButtons();
			}
		});
		buttonGroup.add(rdbtnDay);

		rdbtnWeek = new JRadioButton("Every 7 Days");
		rdbtnWeek.setActionCommand("week");
		rdbtnWeek.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateRadioButtons();
			}
		});
		buttonGroup.add(rdbtnWeek);

		rdbtnCustom = new JRadioButton("Every");
		rdbtnCustom.setActionCommand("custom");
		rdbtnCustom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateRadioButtons();
			}
		});
		buttonGroup.add(rdbtnCustom);

		txtUpdateFreq = new JFormattedTextField(NumberFormat.getInstance());
		txtUpdateFreq.setText("0");
		txtUpdateFreq.setColumns(10);

		JLabel lblDays = new JLabel("Days");

		JLabel lblJavaOptions = new JLabel("Java Options:");

		txtJavaOptions = new JTextField();
		txtJavaOptions.setColumns(10);

		JButton btnDefault = new JButton("Default");
		btnDefault.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				txtJavaOptions.setText(DEFAULT_JAVA_OPTIONS);
			}
		});

		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel
		.setHorizontalGroup(gl_contentPanel
				.createParallelGroup(Alignment.LEADING)
				.addGroup(
						gl_contentPanel
						.createSequentialGroup()
						.addGroup(
								gl_contentPanel
								.createParallelGroup(
										Alignment.LEADING)
										.addComponent(
												lblGameDirectory)
												.addComponent(
														lblProfileName))
														.addPreferredGap(
																ComponentPlacement.UNRELATED)
																.addGroup(
																		gl_contentPanel
																		.createParallelGroup(
																				Alignment.LEADING)
																				.addGroup(
																						gl_contentPanel
																						.createSequentialGroup()
																						.addComponent(
																								txtProfileName,
																								GroupLayout.DEFAULT_SIZE,
																								169,
																								Short.MAX_VALUE)
																								.addPreferredGap(
																										ComponentPlacement.RELATED)
																										.addComponent(
																												btnModpackName))
																												.addGroup(
																														gl_contentPanel
																														.createSequentialGroup()
																														.addComponent(
																																txtGameDirectory,
																																GroupLayout.DEFAULT_SIZE,
																																215,
																																Short.MAX_VALUE)
																																.addPreferredGap(
																																		ComponentPlacement.RELATED)
																																		.addComponent(
																																				btnOpen))))
																																				.addGroup(
																																						gl_contentPanel.createSequentialGroup()
																																						.addComponent(lblUpdateFrequency)
																																						.addContainerGap())
																																						.addGroup(
																																								gl_contentPanel
																																								.createSequentialGroup()
																																								.addGap(10)
																																								.addGroup(
																																										gl_contentPanel
																																										.createParallelGroup(
																																												Alignment.LEADING,
																																												false)
																																												.addGroup(
																																														gl_contentPanel
																																														.createSequentialGroup()
																																														.addComponent(
																																																rdbtnCustom)
																																																.addPreferredGap(
																																																		ComponentPlacement.RELATED)
																																																		.addComponent(
																																																				txtUpdateFreq,
																																																				0,
																																																				0,
																																																				Short.MAX_VALUE))
																																																				.addComponent(
																																																						rdbtnLaunch))
																																																						.addPreferredGap(
																																																								ComponentPlacement.RELATED)
																																																								.addGroup(
																																																										gl_contentPanel
																																																										.createParallelGroup(
																																																												Alignment.LEADING)
																																																												.addGroup(
																																																														gl_contentPanel
																																																														.createSequentialGroup()
																																																														.addComponent(
																																																																rdbtnDay)
																																																																.addGap(18)
																																																																.addComponent(
																																																																		rdbtnWeek))
																																																																		.addComponent(lblDays))
																																																																		.addGap(53))
																																																																		.addGroup(
																																																																				gl_contentPanel
																																																																				.createSequentialGroup()
																																																																				.addComponent(lblJavaOptions)
																																																																				.addPreferredGap(
																																																																						ComponentPlacement.RELATED)
																																																																						.addComponent(txtJavaOptions,
																																																																								GroupLayout.DEFAULT_SIZE, 210,
																																																																								Short.MAX_VALUE)
																																																																								.addPreferredGap(
																																																																										ComponentPlacement.RELATED)
																																																																										.addComponent(btnDefault)));
		gl_contentPanel
		.setVerticalGroup(gl_contentPanel
				.createParallelGroup(Alignment.LEADING)
				.addGroup(
						gl_contentPanel
						.createSequentialGroup()
						.addGroup(
								gl_contentPanel
								.createParallelGroup(
										Alignment.BASELINE)
										.addComponent(
												lblProfileName)
												.addComponent(
														txtProfileName,
														GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE,
														GroupLayout.PREFERRED_SIZE)
														.addComponent(
																btnModpackName))
																.addPreferredGap(
																		ComponentPlacement.RELATED)
																		.addGroup(
																				gl_contentPanel
																				.createParallelGroup(
																						Alignment.BASELINE)
																						.addComponent(
																								lblGameDirectory)
																								.addComponent(
																										txtGameDirectory,
																										GroupLayout.PREFERRED_SIZE,
																										GroupLayout.DEFAULT_SIZE,
																										GroupLayout.PREFERRED_SIZE)
																										.addComponent(btnOpen))
																										.addPreferredGap(
																												ComponentPlacement.RELATED)
																												.addComponent(lblUpdateFrequency)
																												.addPreferredGap(
																														ComponentPlacement.RELATED)
																														.addGroup(
																																gl_contentPanel
																																.createParallelGroup(
																																		Alignment.BASELINE)
																																		.addComponent(
																																				rdbtnLaunch)
																																				.addComponent(rdbtnDay)
																																				.addComponent(rdbtnWeek))
																																				.addPreferredGap(
																																						ComponentPlacement.RELATED)
																																						.addGroup(
																																								gl_contentPanel
																																								.createParallelGroup(
																																										Alignment.BASELINE)
																																										.addComponent(
																																												rdbtnCustom)
																																												.addComponent(
																																														txtUpdateFreq,
																																														GroupLayout.PREFERRED_SIZE,
																																														GroupLayout.DEFAULT_SIZE,
																																														GroupLayout.PREFERRED_SIZE)
																																														.addComponent(lblDays))
																																														.addPreferredGap(
																																																ComponentPlacement.RELATED)
																																																.addGroup(
																																																		gl_contentPanel
																																																		.createParallelGroup(
																																																				Alignment.BASELINE)
																																																				.addComponent(
																																																						lblJavaOptions)
																																																						.addComponent(
																																																								txtJavaOptions,
																																																								GroupLayout.PREFERRED_SIZE,
																																																								GroupLayout.DEFAULT_SIZE,
																																																								GroupLayout.PREFERRED_SIZE)
																																																								.addComponent(
																																																										btnDefault))
																																																										.addContainerGap(80, Short.MAX_VALUE)));
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						finish();
					}
				});
				{
					JButton btnUseExistingProfile = new JButton(
							"Use Existing Profile");
					btnUseExistingProfile.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							chooseExistingProfile();
						}
					});
					buttonPane.add(btnUseExistingProfile);
				}
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cancel();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
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
			if(entry.getKey().getText() == selection) {
				//minecraft data
				txtProfileName.setText(entry.getValue().getStringValue("name"));
				txtGameDirectory.setText(entry.getValue().getStringValue("gameDir"));
				txtJavaOptions.setText(entry.getValue().getStringValue("javaArgs"));
				File modpack = new File(txtGameDirectory.getText() + File.separator + "modpack.json");
				//modpack data
				if(modpack.exists()) {
					try {
						LocalModpack local = new Gson().fromJson(new FileReader(modpack),
								LocalModpack.class);
						rdbtnCustom.setSelected(true);
						txtUpdateFreq.setText(local.updateFrequency + "");
						txtUpdateFreq.setEnabled(true);
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
		txtProfileName.setText(installer.txtProfile.getText());
	}

	private void updateRadioButtons() {
		if (rdbtnCustom.isSelected()) {
			txtUpdateFreq.setEnabled(true);
		} else {
			txtUpdateFreq.setEnabled(false);
		}
		txtUpdateFreq.setText("" + getUpdateFrequency());
	}

	/** ok button */
	private void finish() {
		installer.profile_gameDirectory = txtGameDirectory.getText();
		installer.profile_javaOptions = txtJavaOptions.getText();
		installer.profile_updateFrequency = getUpdateFrequency();
		installer.txtProfile.setText(txtProfileName.getText());
		this.dispose();
	}

	private int getUpdateFrequency() {
		String s = buttonGroup.getSelection().getActionCommand();
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
			String freq = txtUpdateFreq.getText();
			try {
				return Integer.parseInt(freq);
			} catch (NumberFormatException e) {
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
		txtProfileName.setText(installer.txtProfile.getText());
		txtGameDirectory.setText(installer.profile_gameDirectory);
		txtJavaOptions.setText(installer.profile_javaOptions);
		txtUpdateFreq.setText("" + installer.profile_updateFrequency);
		switch (installer.profile_updateFrequency) {
		case 0:
			rdbtnLaunch.setSelected(true);
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
