package common.nw.creator.gui.pages;

import common.nw.creator.Creator;
import common.nw.creator.gui.CreatorWindow;
import common.nw.creator.gui.Reference;
import common.nw.creator.gui.pages.dialog.EditArgumentsDialog;
import common.nw.gui.IPageHandler;
import common.nw.gui.PageHolder;
import common.nw.modpack.Strings;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PanelMinecraftSettings extends JPanel implements IPageHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtVersion;
	private JTextField txtJar;
	private JTextField txtJson;
	private JTextField txtInstallInfo;
	private final ButtonGroup btnGroupJar = new ButtonGroup();
	private final ButtonGroup btnGroupJson = new ButtonGroup();
	private JRadioButton rdbtnDownloadJar;
	private JRadioButton rdbtnJarLocal;
	private JRadioButton rdbtnJarManualDownload;
	private JRadioButton rdbtnDownloadJson;
	private JRadioButton rdbtnJsonLocal;
	private JRadioButton rdbtnJsonManualDownload;

	private Creator creator;
	private JFrame frame;

	/**
	 * Create the panel.
	 */
	public PanelMinecraftSettings(Creator creator, JFrame frame) {

		this.creator = creator;
		this.frame = frame;

		JLabel lblVersion = new JLabel("Version:");
		lblVersion.setToolTipText("The version. \r\nCan be anything.");

		JLabel lblJsonLocation = new JLabel("MC-json location:");
		lblJsonLocation
		.setToolTipText("The location of the minecraft-json file. \r\n\r\nUrl or relative path. \r\nDepends on the choice made in MC-json type.");

		JLabel lblJarLocation = new JLabel("MC-jar location:");
		lblJarLocation
		.setToolTipText("The location of the minecraft-jar file. \r\n\r\nUrl or relative path. \r\nDepends on the choice made in MC-jar type.");

		JLabel lblJsonType = new JLabel("MC-json type:");
		lblJsonType.setToolTipText("WIP");

		JLabel lblJarType = new JLabel("MC-jar type:");
		lblJarType.setToolTipText("WIP");

		JButton btnLibraries = new JButton("Edit Libraries");
		btnLibraries.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editLibraries();
			}
		});

		JButton btnArguments = new JButton("Edit Arguments");
		btnArguments.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editArguments();
			}
		});

		rdbtnDownloadJson = new JRadioButton("Download");
		btnGroupJson.add(rdbtnDownloadJson);
		rdbtnDownloadJson.setSelected(true);
		rdbtnDownloadJson.setEnabled(false);
		rdbtnDownloadJson.setActionCommand(Strings.jsonDirectDownload);

		rdbtnDownloadJar = new JRadioButton("Download");
		btnGroupJar.add(rdbtnDownloadJar);
		rdbtnDownloadJar.setSelected(true);
		rdbtnDownloadJar.setEnabled(false);
		rdbtnDownloadJar.setActionCommand(Strings.jarDirectDownload);

		txtVersion = new JTextField();
		txtVersion.setColumns(10);

		txtJar = new JTextField();
		txtJar.setColumns(10);

		txtJson = new JTextField();
		txtJson.setColumns(10);

		JLabel lblInstallationInfo = new JLabel("Modpack Info:");

		txtInstallInfo = new JTextField();
		txtInstallInfo.setColumns(10);
		txtInstallInfo.setToolTipText("URL pointing to a website displayed during installation.\nLeave empty for none.");

		JButton btnEdit = new JButton("Edit");

		JLabel lblOther = new JLabel("Other:");

		rdbtnJarLocal = new JRadioButton("Local");
		btnGroupJar.add(rdbtnJarLocal);
		rdbtnJarLocal.setEnabled(false);
		rdbtnJarLocal.setActionCommand(Strings.jarLocalFile);

		rdbtnJsonLocal = new JRadioButton("Local");
		btnGroupJson.add(rdbtnJsonLocal);
		rdbtnJsonLocal.setEnabled(false);
		rdbtnJsonLocal.setActionCommand(Strings.jsonLocalFile);

		rdbtnJarManualDownload = new JRadioButton("Manual Download");
		btnGroupJar.add(rdbtnJarManualDownload);
		rdbtnJarManualDownload.setEnabled(false);
		rdbtnJarManualDownload.setActionCommand(Strings.jarUserDownload);

		rdbtnJsonManualDownload = new JRadioButton("Manual Download");
		btnGroupJson.add(rdbtnJsonManualDownload);
		rdbtnJsonManualDownload.setEnabled(false);
		rdbtnJsonManualDownload.setActionCommand(Strings.jsonUserDownload);

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
										.addComponent(lblVersion, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
										.addGap(62)
										.addComponent(txtVersion, GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE))
										.addGroup(groupLayout.createSequentialGroup()
												.addComponent(lblJarType, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
												.addGap(40)
												.addComponent(rdbtnDownloadJar, GroupLayout.PREFERRED_SIZE, 73, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(rdbtnJarLocal)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(rdbtnJarManualDownload))
												.addGroup(groupLayout.createSequentialGroup()
														.addComponent(lblJsonType, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
														.addGap(33)
														.addComponent(rdbtnDownloadJson, GroupLayout.PREFERRED_SIZE, 73, GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(rdbtnJsonLocal)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(rdbtnJsonManualDownload))
														.addGroup(groupLayout.createSequentialGroup()
																.addComponent(lblJarLocation, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
																.addGap(25)
																.addComponent(txtJar, GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE))
																.addGroup(groupLayout.createSequentialGroup()
																		.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
																				.addComponent(lblJsonLocation, GroupLayout.PREFERRED_SIZE, 83, GroupLayout.PREFERRED_SIZE)
																				.addComponent(lblInstallationInfo)
																				.addComponent(lblOther))
																				.addGap(18)
																				.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
																						.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
																								.addComponent(btnArguments)
																								.addPreferredGap(ComponentPlacement.RELATED)
																								.addComponent(btnLibraries))
																								.addGroup(groupLayout.createSequentialGroup()
																										.addComponent(txtInstallInfo, GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
																										.addPreferredGap(ComponentPlacement.RELATED)
																										.addComponent(btnEdit))
																										.addComponent(txtJson, GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE))))
																										.addContainerGap())
				);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
										.addGap(14)
										.addComponent(lblVersion))
										.addGroup(groupLayout.createSequentialGroup()
												.addContainerGap()
												.addComponent(txtVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
												.addPreferredGap(ComponentPlacement.RELATED)
												.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
														.addGroup(groupLayout.createSequentialGroup()
																.addGap(4)
																.addComponent(lblJarType))
																.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
																		.addComponent(rdbtnDownloadJar)
																		.addComponent(rdbtnJarLocal)
																		.addComponent(rdbtnJarManualDownload)))
																		.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
																				.addGroup(groupLayout.createSequentialGroup()
																						.addGap(4)
																						.addComponent(lblJsonType))
																						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
																								.addComponent(rdbtnDownloadJson)
																								.addComponent(rdbtnJsonLocal)
																								.addComponent(rdbtnJsonManualDownload)))
																								.addGap(2)
																								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
																										.addGroup(groupLayout.createSequentialGroup()
																												.addGap(3)
																												.addComponent(lblJarLocation))
																												.addComponent(txtJar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																												.addGap(6)
																												.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
																														.addComponent(lblJsonLocation)
																														.addComponent(txtJson, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																														.addPreferredGap(ComponentPlacement.RELATED)
																														.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
																																.addGroup(groupLayout.createSequentialGroup()
																																		.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
																																				.addComponent(lblInstallationInfo)
																																				.addComponent(txtInstallInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																																				.addPreferredGap(ComponentPlacement.RELATED)
																																				.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
																																						.addComponent(lblOther)
																																						.addComponent(btnArguments)
																																						.addComponent(btnLibraries)))
																																						.addComponent(btnEdit))
																																						.addContainerGap(118, Short.MAX_VALUE))
				);
		setLayout(groupLayout);

	}

	private String getJsonUpdateType() {
		String s = btnGroupJson.getSelection().getActionCommand();
		if(s == null || s.isEmpty()) {
			return Strings.jsonDirectDownload;
		}
		return s;
	}

	private String getJarUpdateType() {
		String s = btnGroupJar.getSelection().getActionCommand();
		if(s == null || s.isEmpty()) {
			return Strings.jarDirectDownload;
		}
		return s;
	}

	/**
	 * used for import, sets the radio buttons
	 * @param type
	 */
	private void setJsonUpdateType(String type) {
		if(type == null || type.isEmpty()) {
			type = Strings.jsonDirectDownload;
		}
		if(type.equals(Strings.jsonDirectDownload)) {
			rdbtnDownloadJson.setSelected(true);
		} else if(type.equals(Strings.jsonLocalFile)) {
			rdbtnJsonLocal.setSelected(true);
		} else if(type.equals(Strings.jsonUserDownload)) {
			rdbtnJsonManualDownload.setSelected(true);
		} else {
			rdbtnDownloadJson.setSelected(true);
		}
	}

	/**
	 * used for import, sets the radio buttons
	 * @param type
	 */
	private void setJarUpdateType(String type) {
		if(type == null || type.isEmpty()) {
			type = Strings.jarDirectDownload;
		}
		if(type.equals(Strings.jarDirectDownload)) {
			rdbtnDownloadJar.setSelected(true);
		} else if(type.equals(Strings.jarLocalFile)) {
			rdbtnJarLocal.setSelected(true);
		} else if(type.equals(Strings.jarUserDownload)) {
			rdbtnJarManualDownload.setSelected(true);
		} else {
			rdbtnDownloadJar.setSelected(true);
		}
	}

	/**
	 * opens the edit arguments dialog
	 */
	private void editArguments() {
		Dialog d = new EditArgumentsDialog(creator.modpack.minecraft.arguments, frame, true);
		d.setVisible(true);
	}

	/**
	 * opens the edit libraries dialog
	 */
	private void editLibraries() {
		JOptionPane.showMessageDialog(this, "No implemented yet!");
	}

	@Override
	public Object getProperty(String s) {
		if(s.equals(Reference.KEY_NAME)) {
			return "Minecraft Settings";
		}
		if(s.equals(Reference.KEY_TURNABLE)) {
			return true;
		}
		return null;
	}

	@Override
	public void onPageOpened(PageHolder holder, boolean forward) {
		this.txtJar.setText(creator.modpack.minecraft.versionName);
		this.txtJson.setText(creator.modpack.minecraft.jsonName);
		this.txtVersion.setText(creator.modpack.minecraft.version);
		this.setJarUpdateType(creator.modpack.minecraft.jarUpdateType);
		this.setJsonUpdateType(creator.modpack.minecraft.jsonUpdateType);
		this.txtInstallInfo.setText(creator.modpack.minecraft.installInfoUrl);
	}

	@Override
	public boolean onPageClosed(PageHolder holder, boolean forward) {
		if (forward) {
			boolean b = true;
			if (txtVersion.getText() == null || txtVersion.getText().isEmpty()
					|| txtJar.getText() == null || txtJar.getText().isEmpty()
					|| txtJson.getText() == null || txtJson.getText().isEmpty()) {
				b = false;
			}
			if (!b) {
				//noinspection PointlessBooleanExpression
				if(!CreatorWindow.DEBUG) {
					JOptionPane.showMessageDialog(this,
							"Not all requiered fields are filled in!", "Error",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}

			creator.modpack.minecraft.jarUpdateType = this.getJarUpdateType();
			creator.modpack.minecraft.jsonUpdateType = this.getJsonUpdateType();
			creator.modpack.minecraft.versionName = this.txtJar.getText();
			creator.modpack.minecraft.jsonName = this.txtJson.getText();
			creator.modpack.minecraft.version = this.txtVersion.getText();
			creator.modpack.minecraft.installInfoUrl = this.txtInstallInfo.getText();
		}
		return true;	
	}
}
