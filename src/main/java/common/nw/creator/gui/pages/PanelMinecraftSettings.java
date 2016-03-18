package common.nw.creator.gui.pages;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import common.nw.core.gui.IPageHandler;
import common.nw.core.gui.PageHolder;
import common.nw.core.modpack.Library;
import common.nw.core.modpack.ModpackValues;
import common.nw.creator.Creator;
import common.nw.creator.gui.CreatorWindow;
import common.nw.creator.gui.Reference;
import common.nw.creator.gui.dialog.DialogEditLibraries;
import common.nw.creator.gui_legacy.pages.dialog.EditArgumentsDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Nuklearwurst
 */
public class PanelMinecraftSettings implements IPageHandler {

	private JTextField txtVersion;

	private JRadioButton rdbtnDownloadJar;
	private JRadioButton rdbtnForgeJar;

	private JRadioButton rdbtnDownloadJson;
	private JRadioButton rdbtnJsonAutoCreate;

	private JTextField txtJar;
	private JTextField txtJson;
	private JTextField txtInstallInfo;
	private JButton btnEditInstallInfo;
	private JButton btnEditArguments;
	private JButton btnEditLibraries;
	private JPanel panel_minecraft_settings;
	private ButtonGroup btnGroupJson;
	private ButtonGroup btnGroupJar;

	private final Creator creator;
	private final JFrame frame;

	/**
	 * Create the panel.
	 */
	public PanelMinecraftSettings(Creator creator, JFrame frame) {

		this.creator = creator;
		this.frame = frame;

		btnEditLibraries.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editLibraries();
			}
		});

		btnEditArguments.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editArguments();
			}
		});

		rdbtnDownloadJson.setActionCommand(ModpackValues.jsonDirectDownload);
		rdbtnJsonAutoCreate.setActionCommand(ModpackValues.jsonGenerate);

		rdbtnDownloadJar.setActionCommand(ModpackValues.jarDirectDownload);
		rdbtnForgeJar.setActionCommand(ModpackValues.jarForgeInherit);

		rdbtnDownloadJson.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateJsonGenerated(false);
			}
		});

		rdbtnJsonAutoCreate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateJsonGenerated(true);
				createDefaultLibs();
			}
		});

		rdbtnForgeJar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setJsonGeneratedTypeEnabled(true);
				setJsonUpdateType(ModpackValues.jsonGenerate);
				createDefaultLibs();
			}
		});

		rdbtnDownloadJar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setJsonGeneratedTypeEnabled(false);
				setJsonUpdateType(ModpackValues.jsonDirectDownload);
			}
		});
	}

	private String getJsonUpdateType() {
		String s = btnGroupJson.getSelection().getActionCommand();
		if (s == null || s.isEmpty()) {
			return ModpackValues.jsonDirectDownload;
		}
		return s;
	}

	private String getJarUpdateType() {
		String s = btnGroupJar.getSelection().getActionCommand();
		if (s == null || s.isEmpty()) {
			return ModpackValues.jarDirectDownload;
		}
		return s;
	}

	/**
	 * used for import, sets the radio buttons
	 */
	private void setJsonUpdateType(String type) {
		if (type == null || type.isEmpty()) {
			type = ModpackValues.jsonDirectDownload;
		}
		switch (type) {
			case ModpackValues.jsonDirectDownload:
				rdbtnDownloadJson.setSelected(true);
				updateJsonGenerated(false);
				break;
			case ModpackValues.jsonGenerate:
				rdbtnJsonAutoCreate.setSelected(true);
				updateJsonGenerated(true);
				break;
			default:
				rdbtnDownloadJson.setSelected(true);
				updateJsonGenerated(false);
				break;
		}
	}

	/**
	 * sets enabled status of json type buttons
	 */
	private void setJsonGeneratedTypeEnabled(boolean b) {
		rdbtnDownloadJson.setEnabled(b);
		rdbtnJsonAutoCreate.setEnabled(b);
	}

	private void createDefaultLibs() {
		if (!creator.defaultLibrariesGenerated) {
			Library updater = Library.createUpdaterLibrary(Reference.DEFAULT_UPDATER_VERSION);
			JOptionPane.showMessageDialog(frame, "A default library has been added!");
			Dialog d = new DialogEditLibraries(creator.modpack.minecraft.libraries, frame, updater);
			d.pack();
			d.setVisible(true);
			creator.defaultLibrariesGenerated = true;
		}

	}

	/**
	 * used for import, sets the radio buttons
	 */
	@SuppressWarnings("Duplicates")
	private void setJarUpdateType(String type) {
		if (type == null || type.isEmpty()) {
			type = ModpackValues.jarDirectDownload;
		}
		switch (type) {
			case ModpackValues.jarDirectDownload:
				rdbtnDownloadJar.setSelected(true);

				setJsonGeneratedTypeEnabled(false);
				rdbtnDownloadJson.setSelected(true);
				updateJsonGenerated(false);

				break;
			case ModpackValues.jarForgeInherit:
				rdbtnForgeJar.setSelected(true);

				setJsonGeneratedTypeEnabled(true);
				rdbtnJsonAutoCreate.setSelected(true);

				updateJsonGenerated(true);
				break;
			default:
				rdbtnDownloadJar.setSelected(true);

				setJsonGeneratedTypeEnabled(false);
				rdbtnDownloadJson.setSelected(true);
				updateJsonGenerated(false);
				break;
		}
	}

	/**
	 * sets json mode generated (or not)
	 * <p/>
	 * depending on the value json textfield etc will get updated
	 * true --> textfield gets disabled and text gets replaced
	 * false --> textfields will be enabled and text cleared
	 */
	private void updateJsonGenerated(boolean generated) {
		if (generated) {
			txtJson.setEnabled(false);
			txtJson.setText("-- generated --");
		} else {
			txtJson.setEnabled(true);
			txtJson.setText("");
		}
	}

	/**
	 * opens the edit arguments dialog
	 */
	private void editArguments() {
		Dialog d = new EditArgumentsDialog(creator.modpack.minecraft.arguments, frame, true);
		d.pack();
		d.setVisible(true);
	}

	/**
	 * opens the edit libraries dialog
	 */
	private void editLibraries() {
		Dialog d = new DialogEditLibraries(creator.modpack.minecraft.libraries, frame);
		d.pack();
		d.setVisible(true);
	}

	@Override
	public Object getProperty(String s) {
		if (s.equals(Reference.KEY_NAME)) {
			return "Minecraft Settings";
		}
		if (s.equals(Reference.KEY_TURNABLE)) {
			return true;
		}
		return null;
	}

	public JPanel getPanel() {
		return panel_minecraft_settings;
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
				if (!CreatorWindow.DEBUG) {
					JOptionPane.showMessageDialog(panel_minecraft_settings,
							"Not all requiered fields are filled in!", "Error",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}

			creator.modpack.minecraft.jarUpdateType = this.getJarUpdateType();
			creator.modpack.minecraft.jsonUpdateType = this.getJsonUpdateType();
			creator.modpack.minecraft.versionName = this.txtJar.getText();
			if (creator.modpack.minecraft.jsonUpdateType.equals(ModpackValues.jsonGenerate)) {
				creator.modpack.minecraft.jsonName = null;
			} else {
				creator.modpack.minecraft.jsonName = this.txtJson.getText();
			}
			creator.modpack.minecraft.version = this.txtVersion.getText();
			creator.modpack.minecraft.installInfoUrl = this.txtInstallInfo.getText();
		}
		return true;
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
		panel_minecraft_settings = new JPanel();
		panel_minecraft_settings.setLayout(new GridLayoutManager(8, 7, new Insets(0, 0, 0, 0), -1, -1));
		final JLabel label1 = new JLabel();
		label1.setText("Version:");
		label1.setToolTipText("The version.  Can be anything.");
		panel_minecraft_settings.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		panel_minecraft_settings.add(spacer1, new GridConstraints(7, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		txtVersion = new JTextField();
		panel_minecraft_settings.add(txtVersion, new GridConstraints(0, 1, 1, 6, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("MC-jar type:");
		panel_minecraft_settings.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		rdbtnDownloadJar = new JRadioButton();
		rdbtnDownloadJar.setSelected(true);
		rdbtnDownloadJar.setText("Download");
		panel_minecraft_settings.add(rdbtnDownloadJar, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		panel_minecraft_settings.add(spacer2, new GridConstraints(1, 5, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JLabel label3 = new JLabel();
		label3.setText("MC-json type:");
		panel_minecraft_settings.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		rdbtnDownloadJson = new JRadioButton();
		rdbtnDownloadJson.setEnabled(false);
		rdbtnDownloadJson.setSelected(true);
		rdbtnDownloadJson.setText("Download");
		panel_minecraft_settings.add(rdbtnDownloadJson, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label4 = new JLabel();
		label4.setText("MC-jar location:");
		label4.setToolTipText("The location of the minecraft-json file.   Url or relative path.  Depends on the choice made in MC-json type.");
		panel_minecraft_settings.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label5 = new JLabel();
		label5.setText("MC-json location:");
		label5.setToolTipText("The location of the minecraft-jar file.   Url or relative path.  Depends on the choice made in MC-jar type.");
		panel_minecraft_settings.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		txtJar = new JTextField();
		panel_minecraft_settings.add(txtJar, new GridConstraints(3, 1, 1, 6, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		txtJson = new JTextField();
		panel_minecraft_settings.add(txtJson, new GridConstraints(4, 1, 1, 6, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label6 = new JLabel();
		label6.setText("Modpack Info:");
		label6.setToolTipText("URL pointing to a website displayed during installation. Leave empty for none.");
		panel_minecraft_settings.add(label6, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		txtInstallInfo = new JTextField();
		txtInstallInfo.setToolTipText("URL pointing to a website displayed during installation. Leave empty for none.");
		panel_minecraft_settings.add(txtInstallInfo, new GridConstraints(5, 1, 1, 5, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		btnEditInstallInfo = new JButton();
		btnEditInstallInfo.setText("Edit");
		panel_minecraft_settings.add(btnEditInstallInfo, new GridConstraints(5, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label7 = new JLabel();
		label7.setText("Other:");
		panel_minecraft_settings.add(label7, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnEditArguments = new JButton();
		btnEditArguments.setText("Edit Arguments");
		panel_minecraft_settings.add(btnEditArguments, new GridConstraints(6, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnEditLibraries = new JButton();
		btnEditLibraries.setText("Edit Libraries");
		panel_minecraft_settings.add(btnEditLibraries, new GridConstraints(6, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		rdbtnForgeJar = new JRadioButton();
		rdbtnForgeJar.setText("Forge");
		panel_minecraft_settings.add(rdbtnForgeJar, new GridConstraints(1, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		rdbtnJsonAutoCreate = new JRadioButton();
		rdbtnJsonAutoCreate.setEnabled(false);
		rdbtnJsonAutoCreate.setText("Generate");
		rdbtnJsonAutoCreate.setToolTipText("Auto-generates version json-file.\nOnly available when using Forge-jar-type.");
		panel_minecraft_settings.add(rdbtnJsonAutoCreate, new GridConstraints(2, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		label1.setLabelFor(txtVersion);
		label4.setLabelFor(txtJar);
		label5.setLabelFor(txtJson);
		label6.setLabelFor(txtInstallInfo);
		btnGroupJar = new ButtonGroup();
		btnGroupJar.add(rdbtnDownloadJar);
		btnGroupJar.add(rdbtnForgeJar);
		btnGroupJson = new ButtonGroup();
		btnGroupJson.add(rdbtnDownloadJson);
		btnGroupJson.add(rdbtnJsonAutoCreate);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return panel_minecraft_settings;
	}
}
