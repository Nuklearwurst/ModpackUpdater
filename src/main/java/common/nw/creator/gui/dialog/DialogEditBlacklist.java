package common.nw.creator.gui.dialog;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import common.nw.core.modpack.RepoMod;
import common.nw.creator.gui.table.ITableHolder;
import common.nw.creator.gui.table.TableModelList;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DialogEditBlacklist extends JDialog implements ITableHolder<RepoMod> {

	private final Window parent;
	private JPanel contentPane;
	private JTable tableBlacklist;
	private JButton btnAdd;
	private JButton btnEdit;
	private JButton btnRemove;
	private JButton btnFinish;

	private final List<RepoMod> blacklist;

	public DialogEditBlacklist(Window parent, List<RepoMod> blacklist) {
		super(parent);
		this.parent = parent;
		this.blacklist = blacklist;

		setContentPane(contentPane);
		setModal(true);
		setPreferredSize(new Dimension(300, 200));
		setTitle("Edit blacklist");
		setBounds(parent.getLocation().x + 10, parent.getLocation().y + 10, 400, 300);

		tableBlacklist.setModel(new TableModelList(new String[]{"Name", "Version"}, new String[]{"name", "version"}, blacklist));
		tableBlacklist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableBlacklist.setPreferredScrollableViewportSize(new Dimension(300, 150));
		tableBlacklist.setFillsViewportHeight(true);
		tableBlacklist.setAutoCreateRowSorter(true);

		btnAdd.addActionListener(e -> openEditDialog(true));

		btnEdit.addActionListener(e -> openEditDialog(false));

		btnRemove.addActionListener(e -> removeButton());

		btnFinish.addActionListener(e -> finish());

		updateTable();
	}

	private void finish() {
		this.dispose();
	}

	private void openEditDialog(boolean create) {
		int index = create ? blacklist.size() : tableBlacklist.getSelectedRow();
		if (index == -1) {
			return;
		}
		DialogEditMod dialog = new DialogEditMod(this, create, index, this);
		dialog.setVisible(true);
	}

	private void removeButton() {
		int index = tableBlacklist.getSelectedRow();
		if (index == -1 || index >= blacklist.size()) {
			return;
		}
		if (JOptionPane
				.showConfirmDialog(
						this,
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
		((TableModelList) tableBlacklist.getModel()).updateData();
		tableBlacklist.revalidate();
		tableBlacklist.repaint();
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
		contentPane.setLayout(new GridLayoutManager(6, 2, new Insets(10, 10, 10, 10), -1, -1));
		final JScrollPane scrollPane1 = new JScrollPane();
		contentPane.add(scrollPane1, new GridConstraints(0, 0, 6, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		tableBlacklist = new JTable();
		scrollPane1.setViewportView(tableBlacklist);
		btnAdd = new JButton();
		btnAdd.setText("Add");
		contentPane.add(btnAdd, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		contentPane.add(spacer1, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		btnEdit = new JButton();
		btnEdit.setText("Edit");
		contentPane.add(btnEdit, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnRemove = new JButton();
		btnRemove.setText("Remove");
		contentPane.add(btnRemove, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnFinish = new JButton();
		btnFinish.setText("Done");
		contentPane.add(btnFinish, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return contentPane;
	}
}
