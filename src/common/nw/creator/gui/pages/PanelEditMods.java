package common.nw.creator.gui.pages;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import common.nw.creator.Creator;
import common.nw.creator.gui.FileTransferHandler;
import common.nw.creator.gui.IDropFileHandler;
import common.nw.creator.gui.Reference;
import common.nw.creator.gui.TableModelList;
import common.nw.creator.gui.pages.dialog.EditBlackListDialog;
import common.nw.creator.gui.pages.dialog.EditModDialog;
import common.nw.creator.gui.pages.dialog.ITableHolder;
import common.nw.gui.IPageHandler;
import common.nw.gui.PageHolder;
import common.nw.modpack.ModInfo;
import common.nw.modpack.RepoMod;
import common.nw.modpack.Strings;
import common.nw.utils.DownloadHelper;

public class PanelEditMods extends JPanel implements IPageHandler, IDropFileHandler, ITableHolder {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTable table;
	public Creator creator;

	public List<RepoMod> mods;
	public List<RepoMod> blacklist;

	private List<RepoMod> hiddenFiles;


	private JFrame frame;
	private JScrollPane scrollPane;
	private JCheckBox chckbxHideConfig;
	private JCheckBox chckbxHideMods;

	/**
	 * Create the panel.
	 * 
	 * @param creator
	 */
	public PanelEditMods(Creator creator, JFrame frame) {

		this.frame = frame;
		this.creator = creator;

		scrollPane = new JScrollPane((Component) null);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] index = table.getSelectedRows();
				if (index.length < 0) {
					return;
				}
				// creator.modpack.files.remove(index);
				if(index.length > 1) {
					if (JOptionPane
							.showConfirmDialog(
									PanelEditMods.this,
									"Are you sure you want to remove that file from the list?",
									"Are you sure", JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
						return;
					}
				}
				for(int i = index.length - 1; i >= 0; i--) {
					if(i < mods.size()) {
						mods.remove(index[i]);
					}
				}
				table.clearSelection();
				updateTable();
			}
		});

		JButton btnEdit = new JButton("Edit");
		btnEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showEditDialog(false);
			}
		});

		JButton btnNew = new JButton("New");
		btnNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showEditDialog(true);
			}
		});

		JButton btnEditBlacklist = new JButton("Edit Blacklist");
		btnEditBlacklist.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				editBlackList();
			}
		});

		chckbxHideMods = new JCheckBox("Hide mods");
		chckbxHideMods.setActionCommand("hideMods");
		chckbxHideMods.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateTable();
			}
		});

		chckbxHideConfig = new JCheckBox("Hide config");
		chckbxHideConfig.setActionCommand("hideConfig");
		chckbxHideConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateTable();
			}
		});
		
		JLabel lblSorting = new JLabel("Sorting:");
		
		JButton button = new JButton("+");
		
		JButton btnUp = new JButton("UP");
		
		JButton btnDown = new JButton("DOWN");
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
							.addComponent(btnNew, GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
							.addComponent(btnEdit, GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
							.addComponent(btnEditBlacklist, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(btnRemove, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE))
						.addComponent(chckbxHideMods)
						.addComponent(chckbxHideConfig)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblSorting)
								.addComponent(button))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
								.addComponent(btnUp, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(btnDown, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(40)
							.addComponent(btnNew)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnEdit)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnRemove)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(chckbxHideMods)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(chckbxHideConfig)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblSorting)
								.addComponent(btnUp))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(button)
								.addComponent(btnDown))
							.addPreferredGap(ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
							.addComponent(btnEditBlacklist))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)))
					.addContainerGap())
		);

		table = new JTable();
		table.setModel(new TableModelList(new String[] { "Name", "Version",
		"URL" }, new String[] { "name", "version", "downloadUrl" },
		creator.modpack.files));
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setPreferredScrollableViewportSize(new Dimension(300, 150));
		table.setFillsViewportHeight(true);
		scrollPane.setViewportView(table);
		setLayout(groupLayout);

		table.setDropMode(DropMode.ON);
		table.setTransferHandler(new FileTransferHandler(this));

	}

	private void hideFiles() {
		if(hiddenFiles == null) {
			hiddenFiles = new ArrayList<RepoMod>();
		}
		//hide mods
		if(chckbxHideMods.isSelected()) {
			for(int i = mods.size() - 1; i >= 0; i--) {
				if(mods.get(i).fileName.startsWith("mods/") && !hiddenFiles.contains(mods.get(i))) {
					hiddenFiles.add(mods.get(i));
					mods.remove(i);
				}
			}
		} else {
			for(int i = hiddenFiles.size() - 1; i >= 0 ; i--) {
				if(hiddenFiles.get(i).fileName.startsWith("mods/")) {
					if(!mods.contains(hiddenFiles.get(i))) {
						mods.add(hiddenFiles.get(i));
					}
					hiddenFiles.remove(i);
				}
			}
		}
		//hids config
		if(chckbxHideConfig.isSelected()) {
			for(int i = mods.size() - 1; i >= 0; i--) {
				if(mods.get(i).fileName.startsWith("config/") && !hiddenFiles.contains(mods.get(i))) {
					hiddenFiles.add(mods.get(i));
					mods.remove(i);
				}
			}
		} else {
			for(int i = hiddenFiles.size() - 1; i >= 0 ; i--) {
				if(hiddenFiles.get(i).fileName.startsWith("config/")) {
					if(!mods.contains(hiddenFiles.get(i))) {
						mods.add(hiddenFiles.get(i));
					}
					hiddenFiles.remove(i);
				}
			}
		}
	}
	/**
	 * 
	 * @param mode
	 *            true equals new entry, false equals edit entry
	 */
	private void showEditDialog(boolean mode) {
		int index = mode ? mods.size() : table.getSelectedRow();
		if (index == -1) {
			return;
		}
		Dialog d = new EditModDialog(frame, true, mode, index, this);
		d.setVisible(true);
	}

	public void updateTable() {
		hideFiles();
		((TableModelList) table.getModel()).updateData();
		table.revalidate();
		table.repaint();
	}

	@Override
	public boolean dropFile(File file) {
		if (!file.exists()) {
			return false;
		}
		String absolutePath = file.getAbsolutePath();
		if (absolutePath != null) {
			
			/** path within the minecraft installation */
			String mcRelativePath = absolutePath;
			/** base dir */
			String baseDirPath = null;
			
			//split absolutePath in two parts
			if(mcRelativePath.contains(File.separator + "mods" + File.separator)) {
				int index = absolutePath.indexOf("mods" + File.separator);
				mcRelativePath = absolutePath.substring(index);
				baseDirPath = absolutePath.substring(0, index);
			} else if(mcRelativePath.contains(File.separator + "config" + File.separator)) {
				int index = absolutePath.indexOf("config" + File.separator);
				mcRelativePath = absolutePath.substring(index);
				baseDirPath = absolutePath.substring(0, index);
			}
			
			ModInfo mod = new ModInfo(mcRelativePath);
			mod.loadInfo(new File(baseDirPath));
			RepoMod repo = new RepoMod();
			repo.name = mod.name;
			repo.version = mod.version;
			if(repo.version == null) {
				repo.version = repo.name;
			}
			repo.downloadUrl = "EDIT_DOWNLOAD_URL_PLEASE";
			repo.downloadType = "PLEASE_EDIT";
			repo.fileName = mod.fileName;
			repo.md5 = DownloadHelper.getHash(file);

			if (mod.hasName) {
				repo.nameType = Strings.nameTypeZipEntry;
			} else {
				repo.nameType = Strings.nameTypeFileName;
			}
			if (mod.hasVersionFile) {
				repo.versionType = Strings.versionTypeZipEntry;
			} else if(mod.fileName.startsWith("config" + File.separator)) {
				repo.version = DateFormat.getDateInstance().format(new Date(System.currentTimeMillis()));
				repo.versionType = Strings.versionTypeTracked;
			} else {
				repo.versionType = Strings.versionTypeFileName;
			}
			this.mods.add(repo);
			updateTable();
			return true;
		}
		return false;
	}

	private void editBlackList() {
		EditBlackListDialog dialog = new EditBlackListDialog(frame, true, blacklist);
		dialog.setVisible(true);
		//TODO edit bloacklist dialog (--> editmod dialog)
	}

	@Override
	public void setValue(int index, Object o) {
		mods.set(index, (RepoMod) o);
	}

	@Override
	public void addValue(Object o) {
		mods.add((RepoMod) o);
	}

	@Override
	public void removeValue(int index) {
		mods.remove(index);
	}

	@Override
	public Object getValue(int index) {
		return mods.get(index);
	}

	@Override
	public Object getProperty(String s) {
		if(s.equals(Reference.KEY_NAME)) {
			return "Edit Mods";
		}
		if(s.equals(Reference.KEY_TURNABLE)) {
			return true;
		}
		return null;
	}

	@Override
	public void onPageOpened(PageHolder holder, boolean forward) {
		this.mods = creator.modpack.files;
		this.blacklist = creator.modpack.blacklist;
		((TableModelList)this.table.getModel()).setValues(creator.modpack.files);
		updateTable();
	}

	@Override
	public boolean onPageClosed(PageHolder holder, boolean forward) {
		//insert hidden files
		this.mods.addAll(hiddenFiles);
		hiddenFiles.clear();
		//probably not needed
		creator.modpack.files = this.mods;
		creator.modpack.blacklist = this.blacklist;
		return true;
	}
}
