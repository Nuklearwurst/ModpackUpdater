package common.nw.creator.gui.pages;

import common.nw.core.gui.PageHolder;
import common.nw.core.modpack.ModInfo;
import common.nw.core.modpack.ModpackValues;
import common.nw.core.modpack.RepoMod;
import common.nw.core.utils.DownloadHelper;
import common.nw.creator.Creator;
import common.nw.creator.gui.dialog.DialogEditBlacklist;
import common.nw.creator.gui.dialog.DialogEditMod;
import common.nw.creator.gui.table.ITableHolder;
import common.nw.creator.gui.table.TableModelList;
import common.nw.creator.gui.transfer.FileTransferHandler;
import common.nw.creator.gui.transfer.IDropFileHandler;
import common.nw.creator.util.Reference;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Nuklearwurst
 */
public class PanelEditMods implements PageHolder.IExtendedPageHandler, IDropFileHandler, ITableHolder<RepoMod> {

	private JButton btnNew;
	private JButton btnEdit;
	private JButton btnRemove;
	private JButton btnEditBlacklist;

	private final JFrame parentFrame;

	private JPanel panel_editmods;
	private JTable tableMods;
	private JCheckBox chbxHideMods;
	private JCheckBox chbxHideConfig;

	private final Creator creator;

	private List<RepoMod> mods;
	private List<RepoMod> blacklist;


	/**
	 * Create the panel
	 */
	public PanelEditMods(Creator creator, JFrame parentFrame) {

		this.parentFrame = parentFrame;
		this.creator = creator;

		btnRemove.addActionListener(e -> {
			int[] index = tableMods.getSelectedRows();
			if (index.length < 0) {
				return;
			}
			if (index.length > 1) {
				if (JOptionPane.showConfirmDialog(panel_editmods,
						"Are you sure you want to remove these files from the list?",
						"Are you sure", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
					return;
				}
			}
			for (int i = index.length - 1; i >= 0; i--) {
				if (i < mods.size()) {
					mods.remove(tableMods.getRowSorter().convertRowIndexToModel(index[i]));
				}
			}
			tableMods.clearSelection();
			updateTable();
		});

		btnEdit.addActionListener(e -> showEditDialog(false));

		btnNew.addActionListener(arg0 -> showEditDialog(true));

		btnEditBlacklist.addActionListener(arg0 -> editBlackList());

		chbxHideMods.setActionCommand("hideMods");
		chbxHideMods.addActionListener(e -> updateTable());

		chbxHideConfig.setActionCommand("hideConfig");
		chbxHideConfig.addActionListener(arg0 -> updateTable());

		//TODO switch to a better table model
		tableMods.setModel(new TableModelList(new String[]{"Name", "Version",
				"URL"}, new String[]{"name", "version", "downloadUrl"},
				creator.modpack.files));
		tableMods.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		tableMods.setTransferHandler(new FileTransferHandler(this));
		tableMods.setAutoCreateRowSorter(true);
		@SuppressWarnings("unchecked") TableRowSorter<TableModelList> rowSorter = (TableRowSorter) tableMods.getRowSorter();
		rowSorter.setRowFilter(new RowFilter<TableModelList, Integer>() {
			@Override
			public boolean include(Entry<? extends TableModelList, ? extends Integer> entry) {
				int id = entry.getIdentifier();
				if (id >= 0 && id < mods.size()) {
					RepoMod mod = mods.get(id);
					if (chbxHideMods.isSelected() && mod.getFileName().startsWith("mods/")) {
						return false;
					} else if (chbxHideConfig.isSelected() && mod.getFileName().startsWith("config/")) {
						return false;
					}
				}
				return true;
			}
		});

	}

	/**
	 * @param mode true equals new entry, false equals edit entry
	 */
	private void showEditDialog(boolean mode) {
		int index = mode ? mods.size() : tableMods.getRowSorter().convertRowIndexToModel(tableMods.getSelectedRow());
		if (index == -1) {
			return;
		}
		Dialog d = new DialogEditMod(parentFrame, mode, index, this);
		d.pack();
		d.setVisible(true);
	}

	public void updateTable() {
//		hideFiles();
		((TableModelList) tableMods.getModel()).updateData();
		tableMods.revalidate();
		tableMods.repaint();
	}

	@Override
	public boolean dropFile(File file) {
		if (!file.exists()) {
			return false;
		}
		String absolutePath = file.getAbsolutePath();

		// path within the minecraft installation
		String mcRelativePath = absolutePath;
		// base dir
		String baseDirPath = "";
		// added base dir
		String baseDirToAdd = "";

		//split absolutePath in two parts
		if (mcRelativePath.contains(File.separator + "mods" + File.separator)) {
			int index = absolutePath.indexOf("mods" + File.separator);
			mcRelativePath = absolutePath.substring(index);
			baseDirPath = absolutePath.substring(0, index);
		} else if (mcRelativePath.contains(File.separator + "config" + File.separator)) {
			int index = absolutePath.indexOf("config" + File.separator);
			mcRelativePath = absolutePath.substring(index);
			baseDirPath = absolutePath.substring(0, index);
		} else if (mcRelativePath.endsWith(".jar")) {
			int index = absolutePath.lastIndexOf(File.separator);
			mcRelativePath = absolutePath.substring(index);
			baseDirPath = absolutePath.substring(0, index);
			baseDirToAdd = File.separator + "mods";
		} else if (mcRelativePath.endsWith(".cfg")) {
			int index = absolutePath.lastIndexOf(File.separator);
			mcRelativePath = absolutePath.substring(index);
			baseDirPath = absolutePath.substring(0, index);
			baseDirToAdd = File.separator + "config";
		}

		ModInfo mod = new ModInfo(mcRelativePath);
		mod.loadInfo(new File(baseDirPath));
		//insert default folders if needed after info loaded
		mod.setFileName(baseDirToAdd + mod.getFileName());
		mod.name = mod.version = mod.getFileName();

		RepoMod repo = new RepoMod();
		repo.name = mod.name;
		repo.version = mod.version;
		if (repo.version == null) {
			repo.version = repo.name;
		}
		repo.downloadUrl = "EDIT_DOWNLOAD_URL_PLEASE";
		repo.downloadType = "PLEASE_EDIT";
		repo.setFileName(mod.getFileName());
		repo.md5 = DownloadHelper.getHash(file);

		if (mod.hasName) {
			repo.nameType = ModpackValues.Name.nameTypeZipEntry;
		} else {
			repo.nameType = ModpackValues.Name.nameTypeFileName;
		}
		if (mod.hasVersionFile) {
			repo.versionType = ModpackValues.Version.versionTypeZipEntry;
		} else if (mod.getFileNameSystem().startsWith("config" + File.separator)) {
			repo.version = DateFormat.getDateInstance().format(new Date(System.currentTimeMillis()));
			repo.versionType = ModpackValues.Version.versionTypeTracked;
		} else {
			repo.versionType = ModpackValues.Version.versionTypeFileName;
		}
		this.mods.add(repo);
		updateTable();
		return true;
	}

	private void editBlackList() {
		DialogEditBlacklist dialog = new DialogEditBlacklist(parentFrame, blacklist);
		dialog.setVisible(true);
	}

	@Override
	public void setValue(int index, RepoMod o) {
		mods.set(index, o);
	}

	@Override
	public void addValue(RepoMod o) {
		mods.add(o);
	}

	@Override
	public void removeValue(int index) {
		mods.remove(index);
	}

	@Override
	public RepoMod getValue(int index) {
		return mods.get(index);
	}

	@Override
	public Object getProperty(String s) {
		if (s.equals(Reference.KEY_NAME)) {
			return "Edit Mods";
		}
		if (s.equals(Reference.KEY_TURNABLE)) {
			return true;
		}
		return null;
	}

	@Override
	public void onPageOpened(PageHolder holder, boolean forward) {
		this.mods = creator.modpack.files;
		this.blacklist = creator.modpack.blacklist;
		((TableModelList) this.tableMods.getModel()).setValues(creator.modpack.files);
		updateTable();
	}

	@Override
	public boolean onPageClosed(PageHolder holder, boolean forward) {
		//probably not needed
		creator.modpack.files = this.mods;
		creator.modpack.blacklist = this.blacklist;
		return true;
	}

	@Override
	public JPanel getPanel() {
		return panel_editmods;
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
		panel_editmods = new JPanel();
		panel_editmods.setLayout(new GridBagLayout());
		btnNew = new JButton();
		btnNew.setText("New");
		btnNew.setMnemonic('N');
		btnNew.setDisplayedMnemonicIndex(0);
		GridBagConstraints gbc;
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel_editmods.add(btnNew, gbc);
		final JPanel spacer1 = new JPanel();
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.VERTICAL;
		panel_editmods.add(spacer1, gbc);
		btnEdit = new JButton();
		btnEdit.setText("Edit");
		btnEdit.setMnemonic('E');
		btnEdit.setDisplayedMnemonicIndex(0);
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel_editmods.add(btnEdit, gbc);
		btnRemove = new JButton();
		btnRemove.setText("Remove");
		btnRemove.setMnemonic('R');
		btnRemove.setDisplayedMnemonicIndex(0);
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel_editmods.add(btnRemove, gbc);
		chbxHideMods = new JCheckBox();
		chbxHideMods.setText("Hide mods");
		chbxHideMods.setMnemonic('M');
		chbxHideMods.setDisplayedMnemonicIndex(5);
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		panel_editmods.add(chbxHideMods, gbc);
		chbxHideConfig = new JCheckBox();
		chbxHideConfig.setText("Hide configs");
		chbxHideConfig.setMnemonic('C');
		chbxHideConfig.setDisplayedMnemonicIndex(5);
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 5;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		panel_editmods.add(chbxHideConfig, gbc);
		final JScrollPane scrollPane1 = new JScrollPane();
		scrollPane1.setBackground(new Color(-1));
		scrollPane1.setVerticalScrollBarPolicy(22);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 8;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		panel_editmods.add(scrollPane1, gbc);
		tableMods = new JTable();
		tableMods.setDropMode(DropMode.ON);
		tableMods.setFillsViewportHeight(true);
		tableMods.setPreferredScrollableViewportSize(new Dimension(300, 150));
		scrollPane1.setViewportView(tableMods);
		final JPanel spacer2 = new JPanel();
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 6;
		gbc.gridwidth = 2;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.VERTICAL;
		panel_editmods.add(spacer2, gbc);
		btnEditBlacklist = new JButton();
		btnEditBlacklist.setText("Edit Blacklist");
		btnEditBlacklist.setMnemonic('B');
		btnEditBlacklist.setDisplayedMnemonicIndex(5);
		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 7;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel_editmods.add(btnEditBlacklist, gbc);
		final JPanel spacer3 = new JPanel();
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 8;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel_editmods.add(spacer3, gbc);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return panel_editmods;
	}
}
