package common.nw.creator.gui.pages.dialog;

import common.nw.creator.gui_legacy.pages.dialog.ITableHolder;
import common.nw.creator.properties.CreatorProperties;
import common.nw.creator.util.CreatorUtils;
import common.nw.modpack.ModInfo;
import common.nw.modpack.RepoMod;
import common.nw.modpack.Strings;
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


		rdbtnNameFilename.setActionCommand(Strings.nameTypeFileName);
		rdbtnNameZip.setActionCommand(Strings.nameTypeZipEntry);
		rdbtnVersionFilename.setActionCommand(Strings.versionTypeFileName);
		rdbtnVersionZip.setActionCommand(Strings.versionTypeZipEntry);
		rdbtnVersionMD5.setActionCommand(Strings.versionTypeMD5);
		rdbtnVersionTracked.setActionCommand(Strings.versionTypeTracked);
		rdbtnDownloadDirect.setActionCommand(Strings.modDirectDownload);
		rdbtnDownloadFolder.setActionCommand(Strings.modUserDownload);
		rdbtnDownloadExtract.setActionCommand(Strings.modExtractDownload);

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
		btnOpen.setVisible(mode);

		btnDownload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				download();
			}
		});
		btnDownload.setVisible(mode);


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
		final String ans = JOptionPane.showInputDialog(this, "Enter a URL:");
		if(ans == null) {
			return;
		}
		//TODO: validate URL
		int index = ans.lastIndexOf("/");
		DialogDownload d = new DialogDownload(this, new File("." + File.separator + (index > 0 ? ans.substring(index) : ans)), new DialogDownload.DownloadFileHandler() {
			@Override
			public void onDownloadFinished(File file, UpdateResult result) {
				if(result == UpdateResult.Good) {
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
		} else if(mod.fileName.startsWith("config/")) {
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
		if(mod.fileName == null || mod.fileName.isEmpty()) {
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

	/** remove button */
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
				if (mod.nameType.equals(Strings.nameTypeZipEntry)) {
					rdbtnNameZip.setSelected(true);
				} else {
					rdbtnNameFilename.setSelected(true);
				}
			}

			// modVersionType
			if (mod.versionType != null) {
				if (mod.versionType.equals(Strings.versionTypeZipEntry)) {
					rdbtnVersionZip.setSelected(true);
				} else if (mod.versionType.equals(Strings.versionTypeMD5)) {
					rdbtnVersionMD5.setSelected(true);
				} else if (mod.versionType.equals(Strings.versionTypeTracked)) {
					rdbtnVersionTracked.setSelected(true);
				} else {
					rdbtnVersionFilename.setSelected(true);
				}
			}

			// modDownloadType
			if (mod.downloadType != null) {
				if (mod.downloadType.equals(Strings.modUserDownload)) {
					rdbtnDownloadFolder.setSelected(true);
				} else {
					rdbtnDownloadDirect.setSelected(true);
				}
			}
		}
	}
}
