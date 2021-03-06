package common.nw.creator.gui.dialog;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import common.nw.core.modpack.Library;
import common.nw.creator.gui.table.ITableHolder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Dialog shown to edit or add a library to the given {@link ITableHolder}
 */
public class DialogEditLibrary extends JDialog {
	private final ITableHolder<Library> table;
	private final int index;
	private final boolean create;


	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JTextField txtName;
	private JTextField txtUrl;
	private JButton btnRemove;

	/**
	 * instance of the currently editing library
	 */
	private final Library library;

	public DialogEditLibrary(Window parent, ITableHolder<Library> table, int index, final boolean create) {
		super(parent, ModalityType.APPLICATION_MODAL);
		this.table = table;
		this.index = index;
		this.create = create;

		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);

		buttonOK.addActionListener(e -> onOK());

		buttonCancel.addActionListener(e -> onCancel());

// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

// call onCancel() on ESCAPE
		contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		//init

		setBounds(parent.getLocation().x + 20, parent.getLocation().y + 20, 417, 310);
		setMinimumSize(new Dimension(400, 120));
		setTitle(create ? "Add library" : "Edit library");

		btnRemove.setVisible(!create);
		btnRemove.addActionListener(e -> remove());

		if (create) {
			library = new Library();
		} else {
			library = table.getValue(index);
			txtName.setText(library.id);
			txtUrl.setText(library.url);
		}

	}

	private void remove() {
		if (!create) {
			final String msg;
			if (table.getValue(index).isVital()) {
				msg = "Are you sure you want to exclude this library?\nThis lilbrary seems to be important!";
			} else {
				msg = "Are you sure you want to exclude this library?";
			}
			if (JOptionPane.showConfirmDialog(this, msg, "Are you sure?",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
				table.removeValue(index);
				table.updateTable();
				dispose();
			}
		}
	}

	private void onOK() {
		final String name = txtName.getText();
		final String url = txtUrl.getText();
		if (name == null || url == null || name.isEmpty() || url.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Error! Some fields are not filled in!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		library.id = name;
		library.url = url;
		if (create) {
			this.table.addValue(library);
		} else {
			this.table.setValue(index, library);
		}
		this.table.updateTable();
		dispose();
	}

	private void onCancel() {
// add your code here if necessary
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
		contentPane.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
		panel1.add(panel2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		buttonOK = new JButton();
		buttonOK.setText("OK");
		panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		buttonCancel = new JButton();
		buttonCancel.setText("Cancel");
		panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		btnRemove = new JButton();
		btnRemove.setText("Remove");
		panel1.add(btnRemove, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		txtName = new JTextField();
		panel3.add(txtName, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label1 = new JLabel();
		label1.setText("Name:");
		panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		txtUrl = new JTextField();
		panel3.add(txtUrl, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Url:");
		panel3.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		contentPane.add(spacer2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		label1.setLabelFor(txtName);
		label2.setLabelFor(txtUrl);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return contentPane;
	}
}
