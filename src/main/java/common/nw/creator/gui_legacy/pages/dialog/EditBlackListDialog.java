package common.nw.creator.gui_legacy.pages.dialog;

import common.nw.core.modpack.RepoMod;
import common.nw.creator.gui.dialog.DialogEditMod;
import common.nw.creator.gui.table.ITableHolder;
import common.nw.creator.gui.table.TableModelList;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class EditBlackListDialog extends JDialog implements ITableHolder<RepoMod> {

	private final JTable table;

	private final List<RepoMod> blacklist;

	@SuppressWarnings("unused")
	private final JFrame parent;

	/**
	 * Create the dialog.
	 */
	@SuppressWarnings("SameParameterValue")
	public EditBlackListDialog(JFrame parent, boolean modal, List<RepoMod> blacklist) {
		super(parent, modal);
		setPreferredSize(new Dimension(300, 200));
		setTitle("Edit blacklist");


		setBounds(parent.getLocation().x + 10, parent.getLocation().y + 10, 400, 300);

		this.blacklist = blacklist;
		this.parent = parent;

		JScrollPane scrollPane = new JScrollPane();

		JButton btnCreate = new JButton("Create");
		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openEditDialog(true);
			}
		});

		JButton btnEdit = new JButton("Edit");
		btnEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openEditDialog(false);
			}
		});

		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeButton();
			}
		});

		JButton btnDone = new JButton("Done");
		btnDone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				finish();
			}
		});

		JButton btnCancel = new JButton("Cancel");
		btnCancel.setVisible(false);
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
								.addContainerGap()
								.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
										.addComponent(btnRemove, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnEdit, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnCreate, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnCancel, GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
										.addComponent(btnDone, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addContainerGap())
		);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
								.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
										.addGroup(groupLayout.createSequentialGroup()
												.addContainerGap()
												.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE))
										.addGroup(groupLayout.createSequentialGroup()
												.addGap(39)
												.addComponent(btnCreate)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(btnEdit)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(btnRemove)
												.addPreferredGap(ComponentPlacement.RELATED, 78, Short.MAX_VALUE)
												.addComponent(btnCancel)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(btnDone)))
								.addContainerGap())
		);

		table = new JTable();
		table.setModel(new TableModelList(new String[]{"Name", "Version"}, new String[]{"name", "version"}, blacklist));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setPreferredScrollableViewportSize(new Dimension(300, 150));
		table.setFillsViewportHeight(true);
		scrollPane.setViewportView(table);
		getContentPane().setLayout(groupLayout);

		updateTable();
	}

	private void cancel() {
		this.dispose();
	}

	private void finish() {
		this.dispose();
	}

	private void openEditDialog(boolean create) {
		int index = create ? blacklist.size() : table.getSelectedRow();
		if (index == -1) {
			return;
		}
		DialogEditMod dialog = new DialogEditMod(this, create, index, this);
		dialog.setVisible(true);
	}

	private void removeButton() {
		int index = table.getSelectedRow();
		if (index == -1 || index >= blacklist.size()) {
			return;
		}
		if (JOptionPane
				.showConfirmDialog(
						EditBlackListDialog.this,
						"Are you sure you want to remove that file from the list?",
						"Are you sure", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
			blacklist.remove(index);
			updateTable();
		}
	}

	@Override
	public void setValue(int index, RepoMod o) {
		blacklist.set(index, o);
	}

	@Override
	public void addValue(RepoMod o) {
		blacklist.add(o);
	}

	@Override
	public void removeValue(int index) {
		blacklist.remove(index);
	}

	@Override
	public RepoMod getValue(int index) {
		return blacklist.get(index);
	}

	@Override
	public void updateTable() {
		((TableModelList) table.getModel()).updateData();
		table.revalidate();
		table.repaint();
	}
}
