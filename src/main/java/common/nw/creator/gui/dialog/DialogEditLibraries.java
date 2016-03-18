package common.nw.creator.gui.dialog;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import common.nw.core.modpack.Library;
import common.nw.creator.gui.table.ITableHolder;
import common.nw.creator.gui.table.ListDataTable;
import common.nw.creator.gui.table.TableModelDataList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class DialogEditLibraries extends JDialog implements ITableHolder<Library> {
	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JTable tableLibraries;
	private JButton btnNew;
	private JButton btnEdit;
	private JButton btnRemove;
	private JCheckBox chbxLock;

	private final List<String> libraries;
	@SuppressWarnings("unused")
	private final Window parentFrame;

	private List<Library> libraryList;

	public DialogEditLibraries(List<String> libraries, final Window frame) {
		super(frame, ModalityType.APPLICATION_MODAL);

		this.libraries = libraries;
		parentFrame = frame;
		resetListToDefault();

		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);

		setMinimumSize(new Dimension(400, 220));
		setBounds(frame.getX() + 40, frame.getY() + 40, 400, 160);
		setTitle("Edit libraries");

		buttonOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOK();
			}
		});

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});

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

		//init ui
//		tableLibraries.setModel(new TableModelList(new String[]{"Name", "Url"}, new String[]{"id", "url"}, libraryList));
		final String[] dataFields = new String[]{"id", "url"};
		tableLibraries.setModel(new TableModelDataList(new ListDataTable(dataFields, libraryList), new String[]{"Name", "Url"}, new String[]{"id", "url"}, true));

		btnNew.addActionListener(new ActionListener() {
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
				int[] index = tableLibraries.getSelectedRows();
				if (index.length < 0) {
					return;
				}
				if (index.length > 1) {
					final String msg;
					if (getValue(index[0]).isVital()) {
						msg = "Are you sure you want to remove this library from the list?\nThis lilbrary seems to be important!";
					} else {
						msg = "Are you sure you want to remove this library from the list?";
					}
					if (JOptionPane.showConfirmDialog(DialogEditLibraries.this, msg,
							"Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
						return;
					}
				}
				for (int i = index.length - 1; i >= 0; i--) {
					if (i < libraryList.size()) {
						libraryList.remove(tableLibraries.getRowSorter().convertRowIndexToModel(index[i]));
					}
				}
				tableLibraries.clearSelection();
				updateTable();
			}
		});
	}

	public DialogEditLibraries(List<String> libraries, JFrame frame, Library updater) {
		this(libraries, frame);
		addValue(updater);
		saveChanges();
	}

	/**
	 * @param create true equals new entry, false equals edit entry
	 */
	private void showEditDialog(boolean create) {
		int index = create ? libraryList.size() : tableLibraries.getRowSorter().convertRowIndexToModel(tableLibraries.getSelectedRow());
		if (index == -1) {
			return;
		}
		Dialog d = new DialogEditLibrary(this, this, index, create);
		d.pack();
		d.setVisible(true);
	}

	@Override
	public void setValue(int index, Library o) {
		libraryList.set(index, o);
	}

	@Override
	public void addValue(Library o) {
		libraryList.add(o);
	}

	@Override
	public void removeValue(int index) {
		libraryList.remove(index);
	}

	@Override
	public Library getValue(int index) {
		return libraryList.get(index);
	}

	@Override
	public void updateTable() {
		((TableModelDataList) tableLibraries.getModel()).fireTableDataChanged();
		tableLibraries.revalidate();
		tableLibraries.repaint();
	}

	private void resetListToDefault() {
		libraryList = Library.createFromString(libraries);
	}

	private void saveChanges() {
		libraries.clear();
		for (Library library : libraryList) {
			libraries.add(library.compileToJson());
		}
	}

	private void onOK() {
		saveChanges();
		dispose();
	}

	private void onCancel() {
		dispose();
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
		panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
		panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		buttonOK = new JButton();
		buttonOK.setText("OK");
		panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		buttonCancel = new JButton();
		buttonCancel.setText("Cancel");
		panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JScrollPane scrollPane1 = new JScrollPane();
		panel3.add(scrollPane1, new GridConstraints(0, 0, 5, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		tableLibraries = new JTable();
		tableLibraries.setAutoCreateRowSorter(true);
		tableLibraries.setFillsViewportHeight(true);
		scrollPane1.setViewportView(tableLibraries);
		btnNew = new JButton();
		btnNew.setText("New");
		panel3.add(btnNew, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnEdit = new JButton();
		btnEdit.setText("Edit");
		panel3.add(btnEdit, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		panel3.add(spacer2, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		btnRemove = new JButton();
		btnRemove.setText("Remove");
		panel3.add(btnRemove, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		chbxLock = new JCheckBox();
		chbxLock.setText("Lock");
		chbxLock.setToolTipText("When locked values cannot be edited within the table.");
		panel3.add(chbxLock, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return contentPane;
	}
}
