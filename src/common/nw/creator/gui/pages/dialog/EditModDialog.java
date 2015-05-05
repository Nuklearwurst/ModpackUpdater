package common.nw.creator.gui.pages.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import common.nw.modpack.ModInfo;
import common.nw.modpack.RepoMod;
import common.nw.modpack.Strings;
import common.nw.utils.DownloadHelper;
import common.nw.utils.Utils;

public class EditModDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();

	private boolean mode;
	private int index;
	private ITableHolder table;
	private RepoMod mod;
	private JTextField txtName;
	private JTextField txtVersion;
	private JTextField txtURL;
	private JTextField txtFilename;
	private JTextField txtMD5;
	private final ButtonGroup btnGroupNameType = new ButtonGroup();
	private final ButtonGroup btnGroupVersionType = new ButtonGroup();
	private final ButtonGroup btnGroupDownloadType = new ButtonGroup();
	private JRadioButton rdbtnNameFilename;
	private JRadioButton rdbtnNameZip;
	private JRadioButton rdbtnVersionFilename;
	private JRadioButton rdbtnVerisionZip;
	private JRadioButton rdbtnVersionMD5;
	private JRadioButton rdbtnDownloadDirect;
	private JRadioButton rdbtnDownloadFolder;
	private JRadioButton rdbtnVersionTracked;

	/**
	 * Create the dialog.
	 */
	@SuppressWarnings("SameParameterValue")
	public EditModDialog(Frame parent, boolean modal, boolean mode, int index,
			ITableHolder table) {
		super(parent, modal);
		setTitle("Add/Edit mod");
		this.mode = mode;
		this.index = index;
		this.table = table;

		setBounds(parent.getLocation().x + 20, parent.getLocation().y + 20, 417, 310);

		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		JLabel lblName = new JLabel("Name:");
		txtName = new JTextField();
		txtName.setColumns(10);
		JLabel lblNametype = new JLabel("NameType:");

		rdbtnNameFilename = new JRadioButton("Filename");
		rdbtnNameFilename.setSelected(true);
		rdbtnNameFilename.setActionCommand(Strings.nameTypeFileName);
		btnGroupNameType.add(rdbtnNameFilename);

		JLabel lblVersion = new JLabel("Version:");

		txtVersion = new JTextField();
		txtVersion.setColumns(10);

		JLabel lblVersiontype = new JLabel("VersionType:");

		rdbtnVersionFilename = new JRadioButton("Filename");
		rdbtnVersionFilename.setSelected(true);
		rdbtnVersionFilename.setActionCommand(Strings.versionTypeFileName);
		btnGroupVersionType.add(rdbtnVersionFilename);

		JLabel lblDownloadUrl = new JLabel("Download URL:");

		JLabel lblDownloadType = new JLabel("Download Type:");

		JLabel lblModFilename = new JLabel("Mod Filename:");

		JLabel lblMd = new JLabel("MD5:");

		txtURL = new JTextField();
		txtURL.setColumns(10);

		rdbtnDownloadDirect = new JRadioButton("Direct");
		rdbtnDownloadDirect.setSelected(true);
		rdbtnDownloadDirect.setActionCommand(Strings.modDirectDownload);
		btnGroupDownloadType.add(rdbtnDownloadDirect);

		txtFilename = new JTextField();
		txtFilename.setColumns(10);

		txtMD5 = new JTextField();
		txtMD5.setColumns(10);

		rdbtnNameZip = new JRadioButton("Zip-Mod-Name");
		rdbtnNameZip.setEnabled(false);
		rdbtnNameZip.setActionCommand(Strings.nameTypeZipEntry);
		btnGroupNameType.add(rdbtnNameZip);

		rdbtnVerisionZip = new JRadioButton("Zip-Mod-Version");
		rdbtnVerisionZip.setEnabled(false);
		rdbtnVerisionZip.setActionCommand(Strings.versionTypeZipEntry);
		btnGroupVersionType.add(rdbtnVerisionZip);

		rdbtnDownloadFolder = new JRadioButton("Folder");
		rdbtnDownloadFolder.setActionCommand(Strings.modUserDownload);
		btnGroupDownloadType.add(rdbtnDownloadFolder);
		rdbtnDownloadFolder.setEnabled(false);

		rdbtnVersionMD5 = new JRadioButton("MD5");
		btnGroupVersionType.add(rdbtnVersionMD5);
		rdbtnVersionMD5.setActionCommand(Strings.versionTypeMD5);
		
		rdbtnVersionTracked = new JRadioButton("Tracked");
		btnGroupVersionType.add(rdbtnVersionTracked);
		rdbtnVersionTracked.setActionCommand(Strings.versionTypeTracked);

		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(lblName)
						.addComponent(lblNametype)
						.addComponent(lblVersiontype)
						.addComponent(lblModFilename)
						.addComponent(lblMd)
						.addComponent(lblVersion)
						.addComponent(lblDownloadUrl)
						.addComponent(lblDownloadType))
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addGap(19)
							.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(txtMD5, GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
								.addComponent(txtFilename, GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
								.addGroup(gl_contentPanel.createSequentialGroup()
									.addComponent(rdbtnVersionFilename)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(rdbtnVerisionZip)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(rdbtnVersionMD5)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(rdbtnVersionTracked))
								.addComponent(txtVersion, GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
								.addGroup(gl_contentPanel.createSequentialGroup()
									.addComponent(rdbtnNameFilename)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(rdbtnNameZip))
								.addComponent(txtName, GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
								.addComponent(txtURL, GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)))
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addGap(18)
							.addComponent(rdbtnDownloadDirect)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(rdbtnDownloadFolder)))
					.addContainerGap())
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblName)
						.addComponent(txtName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNametype)
						.addComponent(rdbtnNameFilename)
						.addComponent(rdbtnNameZip))
					.addGap(8)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblVersion))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblVersiontype)
						.addComponent(rdbtnVersionFilename)
						.addComponent(rdbtnVerisionZip)
						.addComponent(rdbtnVersionMD5)
						.addComponent(rdbtnVersionTracked))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtURL, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblDownloadUrl))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblDownloadType)
						.addComponent(rdbtnDownloadDirect)
						.addComponent(rdbtnDownloadFolder))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblModFilename)
						.addComponent(txtFilename, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblMd)
						.addComponent(txtMD5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);

			{
				if (!mode) {
					JButton btnRemove = new JButton("Remove");
					btnRemove.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							remove();
						}
					});
					buttonPane.add(btnRemove);
				}
			}
			{
				if (mode) {
					JButton btnOpenFile = new JButton("Open File");
					btnOpenFile.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							openFile();
						}
					});
					buttonPane.add(btnOpenFile);
				}
			}
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						// if(EditModDialog.this.mode)
						// {
						// EditModDialog.this.panel.creator.modpack.files.add(new
						// RepoMod());
						// EditModDialog.this.panel.updateTable();
						// EditModDialog.this.dispose();
						// }
						finish();
					}
				});
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

	private void openFile() {
		String filePath = Utils.openFile(this, null);
		if (filePath != null) {
			// int lIndex = filePath.lastIndexOf(File.separator);
			// String dir = filePath.substring(0, lIndex);
			// String file = filePath.substring(lIndex + 1);
			ModInfo mod = new ModInfo(filePath);
			mod.loadInfo(null);
			txtName.setText(mod.name);
			txtVersion.setText(mod.version);
			txtFilename.setText(mod.fileName);
			txtMD5.setText(DownloadHelper.getHash(mod.file));

			if (mod.hasName) {
				rdbtnNameZip.setSelected(true);
			} else {
				rdbtnNameFilename.setSelected(true);
			}

			if (mod.hasVersionFile) {
				rdbtnVerisionZip.setSelected(true);
			} else if(mod.fileName.startsWith("config/")) {
				txtVersion.setText(DateFormat.getDateInstance().format(new Date(System.currentTimeMillis())));
				rdbtnVersionTracked.setSelected(true);
			} else {
				rdbtnVersionFilename.setSelected(true);
			}
		}

	}

	/**
	 * ok button setValues
	 */
	private void finish() {
		mod.name = txtName.getText();
		mod.version = txtVersion.getText();
		mod.downloadUrl = txtURL.getText();
		mod.fileName = txtFilename.getText();
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

	/** cancel button */
	private void cancel() {
		this.dispose();
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
			txtURL.setText(mod.downloadUrl);
			txtFilename.setText(mod.fileName);
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
					rdbtnVerisionZip.setSelected(true);
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
