package common.nw.creator.gui.pages.dialog;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import common.nw.creator.gui_legacy.pages.dialog.ITableHolder;
import common.nw.creator.properties.CreatorProperties;
import common.nw.creator.util.CreatorUtils;
import common.nw.modpack.ModInfo;
import common.nw.modpack.ModpackValues;
import common.nw.modpack.RepoMod;
import common.nw.utils.DownloadHelper;
import common.nw.utils.UpdateResult;
import common.nw.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;

public class DialogEditMod extends JDialog {
	private JPanel contentPane;
	private JButton btnOk;
	private JButton btnCancel;
	private JTextField txtName;
	private JRadioButton rdbtnNameFilename;
	private JRadioButton rdbtnNameZip;
	private JTextField txtVersion;
	private JRadioButton rdbtnVersionFilename;
	private JRadioButton rdbtnVersionZip;
	private JRadioButton rdbtnVersionMD5;
	private JRadioButton rdbtnVersionTracked;
	private JTextField txtUrl;
	private JRadioButton rdbtnDownloadDirect;
	private JRadioButton rdbtnDownloadFolder;
	private JTextField txtFile;
	private JTextField txtMD5;
	private JButton btnOpen;
	private JButton btnRemove;
	private JButton btnDownload;
	private JRadioButton rdbtnDownloadExtract;
	private ButtonGroup btnGroupDownloadType;
	private ButtonGroup btnGroupVersionType;
	private ButtonGroup btnGroupNameType;

	private boolean mode;
	private int index;
	private ITableHolder table;
	private RepoMod mod;

	public DialogEditMod(JFrame parent, boolean mode, int index, ITableHolder table) {
		super(parent, true);
		setContentPane(contentPane);
		getRootPane().setDefaultButton(btnOk);


		setBounds(parent.getLocation().x + 20, parent.getLocation().y + 20, 417, 310);
		setMinimumSize(new Dimension(456, 337));
		setTitle(mode ? "Add mod" : "Edit mod");

		this.mode = mode;
		this.index = index;
		this.table = table;


		rdbtnNameFilename.setActionCommand(ModpackValues.nameTypeFileName);
		rdbtnNameZip.setActionCommand(ModpackValues.nameTypeZipEntry);
		rdbtnVersionFilename.setActionCommand(ModpackValues.versionTypeFileName);
		rdbtnVersionZip.setActionCommand(ModpackValues.versionTypeZipEntry);
		rdbtnVersionMD5.setActionCommand(ModpackValues.versionTypeMD5);
		rdbtnVersionTracked.setActionCommand(ModpackValues.versionTypeTracked);
		rdbtnDownloadDirect.setActionCommand(ModpackValues.modDirectDownload);
		rdbtnDownloadFolder.setActionCommand(ModpackValues.modUserDownload);
		rdbtnDownloadExtract.setActionCommand(ModpackValues.modExtractDownload);

		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOK();
			}
		});

		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});


		btnOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openFile();
			}
		});
//		btnOpen.setVisible(mode);

		btnDownload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				download();
			}
		});
//		btnDownload.setVisible(mode);


		btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				remove();
			}
		});
		btnRemove.setVisible(!mode);

// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

// call onCancel() on ESCAPE
		contentPane.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


		init();
	}

	private void download() {
		final String ans = JOptionPane.showInputDialog(this, "Enter a URL:", Utils.getStringClipboard());
		if (ans == null) {
			return;
		}
		//TODO: validate URL
		int index = ans.lastIndexOf("/");
		DialogDownload d = new DialogDownload(this, new File("" + File.separator + (index > 0 ? ans.substring(index) : ans)), new DialogDownload.DownloadFileHandler() {
			@Override
			public void onDownloadFinished(File file, UpdateResult result) {
				if (result == UpdateResult.Good) {
					openFile(file);
					txtUrl.setText(ans);
				} else {
					JOptionPane.showMessageDialog(contentPane, "Error Downloading File\nResult: " + result, "Error", JOptionPane.ERROR_MESSAGE);
				}
				//noinspection ResultOfMethodCallIgnored
				file.delete();
			}
		}, DialogDownload.createModInfoFromUrl(ans));
		d.setVisible(true);
	}

	private void openFile() {
		String filePath = Utils.openFile(this, new File(CreatorProperties.LAST_OPENED_MOD_DIRECTORY));
		if (filePath != null) {
			CreatorProperties.LAST_OPENED_MOD_DIRECTORY = filePath;
			openFile(new File(filePath));
		}
	}

	private void openFile(File filePath) {
		ModInfo mod = CreatorUtils.createModInfoFromFile(filePath);
		txtName.setText(mod.name);
		txtVersion.setText(mod.version);
		txtFile.setText(mod.fileName);
		txtMD5.setText(DownloadHelper.getHash(mod.file));

		if (mod.hasName) {
			rdbtnNameZip.setSelected(true);
		} else {
			rdbtnNameFilename.setSelected(true);
		}

		if (mod.hasVersionFile) {
			rdbtnVersionZip.setSelected(true);
		} else if (mod.fileName.startsWith("config/")) {
			txtVersion.setText(DateFormat.getDateInstance().format(new Date(System.currentTimeMillis())));
			rdbtnVersionTracked.setSelected(true);
		} else {
			rdbtnVersionFilename.setSelected(true);
		}

	}

	/**
	 * ok button setValues
	 */
	private void onOK() {
		mod.name = txtName.getText();
		mod.version = txtVersion.getText();
		mod.downloadUrl = txtUrl.getText();
		mod.fileName = txtFile.getText();
		if (mod.fileName == null || mod.fileName.isEmpty()) {
			mod.fileName = mod.name;
		}
		mod.md5 = txtMD5.getText();
		mod.downloadType = btnGroupDownloadType.getSelection()
				.getActionCommand();
		mod.nameType = btnGroupNameType.getSelection().getActionCommand();
		mod.versionType = btnGroupVersionType.getSelection().getActionCommand();

		if (mod.name == null || mod.name.isEmpty()) {
			JOptionPane.showMessageDialog(this,
					"Error! Some fields are not filled in!", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (mode) {
			this.table.addValue(mod);
		} else {
			this.table.setValue(index, mod);
		}
		this.table.updateTable();
		this.dispose();
	}

	/**
	 * remove button
	 */
	private void remove() {
		if (!mode) {
			if (JOptionPane.showConfirmDialog(this,
					"Are you sure you want to exclude this file?",
					"Are you sure?", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
				this.table.removeValue(index);
				this.table.updateTable();
				this.dispose();
			}
		}
	}

	private void onCancel() {
// add your code here if necessary
		dispose();
	}

	/**
	 * read values
	 */
	private void init() {
		if (mod == null) {
			mod = new RepoMod();
		}
		if (!mode) {
			mod = (RepoMod) this.table.getValue(index);
			txtName.setText(mod.name);
			txtVersion.setText(mod.version);
			txtUrl.setText(mod.downloadUrl);
			txtFile.setText(mod.fileName);
			txtMD5.setText(mod.md5);

			// modNameType
			if (mod.nameType != null) {
				if (mod.nameType.equals(ModpackValues.nameTypeZipEntry)) {
					rdbtnNameZip.setSelected(true);
				} else {
					rdbtnNameFilename.setSelected(true);
				}
			}

			// modVersionType
			if (mod.versionType != null) {
				if (mod.versionType.equals(ModpackValues.versionTypeZipEntry)) {
					rdbtnVersionZip.setSelected(true);
				} else if (mod.versionType.equals(ModpackValues.versionTypeMD5)) {
					rdbtnVersionMD5.setSelected(true);
				} else if (mod.versionType.equals(ModpackValues.versionTypeTracked)) {
					rdbtnVersionTracked.setSelected(true);
				} else {
					rdbtnVersionFilename.setSelected(true);
				}
			}

			// modDownloadType
			if (mod.downloadType != null) {
				if (mod.downloadType.equals(ModpackValues.modUserDownload)) {
					rdbtnDownloadFolder.setSelected(true);
				} else {
					rdbtnDownloadDirect.setSelected(true);
				}
			}
		}
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
		contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridBagLayout());
		contentPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JLabel label1 = new JLabel();
		label1.setText("Name:");
		GridBagConstraints gbc;
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		panel1.add(label1, gbc);
		txtName = new JTextField();
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 4;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(4, 0, 4, 0);
		panel1.add(txtName, gbc);
		final JLabel label2 = new JLabel();
		label2.setText("Name Type:");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		panel1.add(label2, gbc);
		rdbtnNameFilename = new JRadioButton();
		rdbtnNameFilename.setSelected(true);
		rdbtnNameFilename.setText("Filename");
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(4, 0, 4, 0);
		panel1.add(rdbtnNameFilename, gbc);
		rdbtnNameZip = new JRadioButton();
		rdbtnNameZip.setEnabled(true);
		rdbtnNameZip.setText("ZipName");
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.WEST;
		panel1.add(rdbtnNameZip, gbc);
		final JPanel spacer1 = new JPanel();
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel1.add(spacer1, gbc);
		final JLabel label3 = new JLabel();
		label3.setText("Version:");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		panel1.add(label3, gbc);
		final JLabel label4 = new JLabel();
		label4.setText("Version Type:");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		panel1.add(label4, gbc);
		final JLabel label5 = new JLabel();
		label5.setText("Download URL:");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.WEST;
		panel1.add(label5, gbc);
		final JLabel label6 = new JLabel();
		label6.setText("Download Type:");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.WEST;
		panel1.add(label6, gbc);
		final JLabel label7 = new JLabel();
		label7.setText("Mod Filename:");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.anchor = GridBagConstraints.WEST;
		panel1.add(label7, gbc);
		final JLabel label8 = new JLabel();
		label8.setText("MD5:");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.anchor = GridBagConstraints.WEST;
		panel1.add(label8, gbc);
		txtVersion = new JTextField();
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.gridwidth = 4;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(4, 0, 4, 0);
		panel1.add(txtVersion, gbc);
		rdbtnVersionFilename = new JRadioButton();
		rdbtnVersionFilename.setSelected(true);
		rdbtnVersionFilename.setText("Filename");
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(4, 0, 4, 0);
		panel1.add(rdbtnVersionFilename, gbc);
		rdbtnVersionZip = new JRadioButton();
		rdbtnVersionZip.setEnabled(true);
		rdbtnVersionZip.setText("Zip-Mod Version");
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		panel1.add(rdbtnVersionZip, gbc);
		rdbtnVersionMD5 = new JRadioButton();
		rdbtnVersionMD5.setEnabled(true);
		rdbtnVersionMD5.setText("MD5");
		gbc = new GridBagConstraints();
		gbc.gridx = 4;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		panel1.add(rdbtnVersionMD5, gbc);
		rdbtnVersionTracked = new JRadioButton();
		rdbtnVersionTracked.setEnabled(true);
		rdbtnVersionTracked.setText("Tracked");
		gbc = new GridBagConstraints();
		gbc.gridx = 5;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		panel1.add(rdbtnVersionTracked, gbc);
		txtUrl = new JTextField();
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 4;
		gbc.gridwidth = 4;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(4, 0, 4, 0);
		panel1.add(txtUrl, gbc);
		rdbtnDownloadDirect = new JRadioButton();
		rdbtnDownloadDirect.setSelected(true);
		rdbtnDownloadDirect.setText("Direct");
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(4, 0, 4, 0);
		panel1.add(rdbtnDownloadDirect, gbc);
		rdbtnDownloadFolder = new JRadioButton();
		rdbtnDownloadFolder.setEnabled(false);
		rdbtnDownloadFolder.setText("Folder");
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.WEST;
		panel1.add(rdbtnDownloadFolder, gbc);
		txtFile = new JTextField();
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 6;
		gbc.gridwidth = 4;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(4, 0, 4, 0);
		panel1.add(txtFile, gbc);
		txtMD5 = new JTextField();
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 7;
		gbc.gridwidth = 4;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(4, 0, 4, 0);
		panel1.add(txtMD5, gbc);
		rdbtnDownloadExtract = new JRadioButton();
		rdbtnDownloadExtract.setText("Extract");
		rdbtnDownloadExtract.setToolTipText("Caution! This option will deactivate proper version control this file!");
		gbc = new GridBagConstraints();
		gbc.gridx = 4;
		gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.WEST;
		panel1.add(rdbtnDownloadExtract, gbc);
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		panel2.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
		panel2.add(panel3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		btnOk = new JButton();
		btnOk.setText("OK");
		panel3.add(btnOk, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnCancel = new JButton();
		btnCancel.setText("Cancel");
		panel3.add(btnCancel, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnOpen = new JButton();
		btnOpen.setText("Open");
		panel3.add(btnOpen, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnRemove = new JButton();
		btnRemove.setText("Remove");
		panel3.add(btnRemove, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnDownload = new JButton();
		btnDownload.setText("Download");
		panel3.add(btnDownload, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnGroupNameType = new ButtonGroup();
		btnGroupNameType.add(rdbtnNameFilename);
		btnGroupNameType.add(rdbtnNameZip);
		btnGroupVersionType = new ButtonGroup();
		btnGroupVersionType.add(rdbtnVersionFilename);
		btnGroupVersionType.add(rdbtnVersionZip);
		btnGroupVersionType.add(rdbtnVersionMD5);
		btnGroupVersionType.add(rdbtnVersionTracked);
		btnGroupDownloadType = new ButtonGroup();
		btnGroupDownloadType.add(rdbtnDownloadDirect);
		btnGroupDownloadType.add(rdbtnDownloadFolder);
		btnGroupDownloadType.add(rdbtnDownloadExtract);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return contentPane;
	}
}
