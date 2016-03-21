package common.nw.creator.gui.dialog;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import common.nw.core.modpack.MCArgument;
import common.nw.creator.gui.table.ITableHolder;
import common.nw.creator.gui.table.ListDataTable;
import common.nw.creator.gui.table.TableModelDataList;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;

public class DialogEditArguments extends JDialog implements ITableHolder<MCArgument> {
	private JPanel contentPane;
	private JTable table;
	private JButton btnAdd;
	private JButton btnEdit;
	private JButton btnRemove;
	private JButton btnUp;
	private JButton btnDown;
	private JButton btnFinish;

	private List<String> argumentStringList;
	private List<MCArgument> argumentList;

	public DialogEditArguments(Window parent, List<String> argumentsString) {
		super(parent);
		setContentPane(contentPane);
		setModal(true);


		setMinimumSize(new Dimension(400, 220));
		setBounds(parent.getX() + 40, parent.getY() + 40, 400, 160);
		setTitle("Edit arguments");

		this.argumentStringList = argumentsString;
		resetListToDefault();

		final String[] dataFields = new String[]{"type", "value"};
		table.setModel(new TableModelDataList(new ListDataTable(dataFields, argumentList), new String[]{"Type", "Value"}, dataFields, true));
		table.setFillsViewportHeight(true);

		final DefaultTableCellRenderer tableCellRenderer = new DefaultTableCellRenderer();
		tableCellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.setDefaultRenderer(Object.class, tableCellRenderer);


		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showEditDialog(true);
			}
		});

		btnEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showEditDialog(false);
			}
		});

		btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				remove();
			}
		});

		btnUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				moveArgUp();
			}
		});

		btnDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				moveArgDown();
			}
		});

		btnFinish.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				finish();
			}
		});
	}

	private void remove() {
		int selectedRowCount = table.getSelectedRowCount();
		if (selectedRowCount > 1) {
			int confirmDialog = JOptionPane.showConfirmDialog(this, "Do you want to delete these " + selectedRowCount + " arguments?");
			if (confirmDialog != JOptionPane.YES_OPTION) {
				return;
			}
		}
		HashSet<MCArgument> toRemove = new HashSet<>();
		boolean hasGeneratedOrJson = false;
		for (int i : table.getSelectedRows()) {
			MCArgument argument = argumentList.get(i);
			toRemove.add(argument);
			if (!hasGeneratedOrJson && (argument.getType() == MCArgument.Type.PREDEFINED || argument.getType() == MCArgument.Type.DYNAMIC)) {
				hasGeneratedOrJson = true;
			}
		}
		if (hasGeneratedOrJson) {
			int confirmDialog = JOptionPane.showConfirmDialog(this, "You have selected " + (selectedRowCount > 1 ? "at least one" : "a") + " generated argument.\nIt might be necessary for minecraft to work.\nAre you sure that you want to remove it?");
			if (confirmDialog != JOptionPane.YES_OPTION) {
				return;
			}
		}

		argumentList.removeAll(toRemove);
		table.clearSelection();
		updateTable();
	}

	private void moveArgUp() {
		if (table.getSelectedRowCount() > 1) {
			return;
		}
		int i = table.getSelectedRow();
		if (i <= 0) {
			return;
		}
		//Get the argument that need to be moved down
		MCArgument old = argumentList.get(i - 1);
		//Set the new argument
		argumentList.set(i - 1, argumentList.get(i));
		//Set the old argument
		argumentList.set(i, old);
		//update selection
		updateTable();
		table.setRowSelectionInterval(i - 1, i - 1);
	}

	private void moveArgDown() {
		if (table.getSelectedRowCount() > 1) {
			return;
		}
		int i = table.getSelectedRow();
		if (i < 0 || i + 1 >= argumentList.size()) {
			return;
		}
		//Get the argument that need to be moved up
		MCArgument old = argumentList.get(i + 1);
		//Set the new argument
		argumentList.set(i + 1, argumentList.get(i));
		//Set the old argument
		argumentList.set(i, old);
		//update selection
		updateTable();
		table.setRowSelectionInterval(i + 1, i + 1);
	}

	/**
	 * @param create true equals new entry, false equals edit entry
	 */
	private void showEditDialog(boolean create) {
		int index = create ? argumentList.size() : table.getSelectedRow();
		if (index == -1) {
			return;
		}
		Dialog d = new DialogEditArgument(this, this, index, create);
		d.pack();
		d.setVisible(true);
	}

	private void resetListToDefault() {
		argumentList = MCArgument.getArgumentsFromStringList(argumentStringList);
	}

	private void saveChanges() {
		argumentStringList.clear();
		for (MCArgument argument : argumentList) {
			argumentStringList.add(argument.getValue());
		}
	}

	private void finish() {
		saveChanges();
		dispose();
	}

	@Override
	public void setValue(int index, MCArgument o) {
		argumentList.set(index, o);
	}

	@Override
	public void addValue(MCArgument o) {
		argumentList.add(o);
	}

	@Override
	public void removeValue(int index) {
		argumentList.remove(index);
	}

	@Override
	public MCArgument getValue(int index) {
		return argumentList.get(index);
	}

	@Override
	public void updateTable() {
		int selection = table.getSelectedRow();
		((TableModelDataList) table.getModel()).fireTableDataChanged();
		table.revalidate();
		table.repaint();
		if (selection >= 0 && selection < table.getRowCount()) {
			table.setRowSelectionInterval(selection, selection);
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
		contentPane.setLayout(new GridLayoutManager(9, 2, new Insets(10, 10, 10, 10), -1, -1));
		final JScrollPane scrollPane1 = new JScrollPane();
		contentPane.add(scrollPane1, new GridConstraints(0, 0, 9, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		table = new JTable();
		scrollPane1.setViewportView(table);
		btnEdit = new JButton();
		btnEdit.setText("Edit");
		contentPane.add(btnEdit, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnRemove = new JButton();
		btnRemove.setText("Remove");
		contentPane.add(btnRemove, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnUp = new JButton();
		btnUp.setText("Up");
		contentPane.add(btnUp, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnDown = new JButton();
		btnDown.setText("Down");
		contentPane.add(btnDown, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnAdd = new JButton();
		btnAdd.setText("Add");
		contentPane.add(btnAdd, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnFinish = new JButton();
		btnFinish.setText("Done");
		contentPane.add(btnFinish, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		contentPane.add(spacer1, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		final JSeparator separator1 = new JSeparator();
		contentPane.add(separator1, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return contentPane;
	}
}
