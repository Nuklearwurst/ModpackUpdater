package common.nw.creator.gui.dialog;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import common.nw.core.modpack.MCArgument;
import common.nw.core.utils.ObjectToDisplayName;
import common.nw.core.utils.SpecialComboboxRenderer;
import common.nw.creator.gui.table.ITableHolder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class DialogEditArgument extends JDialog {
	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JButton btnRemove;
	private JComboBox<MCArgument.Type> cmboxType;
	private JTextField txtArgument;
	private JComboBox<ObjectToDisplayName<String>> cmboxArgument;

	private final ITableHolder<MCArgument> table;
	private final int index;
	private final boolean create;

	public DialogEditArgument(Window parent, ITableHolder<MCArgument> table, int index, final boolean create) {
		super(parent);
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
		setTitle(create ? "Add argument" : "Edit argument");

		cmboxType.addActionListener(e -> updateValue());
		cmboxArgument.setModel(new DefaultComboBoxModel<>(MCArgument.specialArgumentDisplayList));
		cmboxArgument.setRenderer(new SpecialComboboxRenderer());

		cmboxType.setModel(new DefaultComboBoxModel<>(MCArgument.Type.values()));
		cmboxType.setRenderer(new SpecialComboboxRenderer());

		btnRemove.setVisible(!create);
		btnRemove.addActionListener(e -> remove());

		cmboxType.setEnabled(create);

		if (!create) {
			MCArgument argument = table.getValue(index);
			txtArgument.setText(argument.getValue());
			cmboxType.setSelectedIndex(argument.getType().ordinal());
			if (argument.getType() == MCArgument.Type.DYNAMIC) {
				cmboxArgument.setSelectedItem(argument.getValue());
			}
			updateValue();
		}
	}


	private void remove() {
		if (!create) {
			final String msg;
			switch (table.getValue(index).getType()) {
				case CUSTOM:
					msg = "Do you want to remove the custom argument?\nArgument: " + table.getValue(index).getValue();
					break;
				case PREDEFINED:
					msg = "Removing this will ignore the argumentlist inside the version json!\nDo you want to proceed?";
					break;
				case DYNAMIC:
					msg = "This generated argument might be important for minecraft to work!\nDo you want to proceed?";
					break;
				default:
					msg = "Do you want to delete this unknown argument?\nArgument: " + table.getValue(index).getValue();
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
		MCArgument argument;
		@SuppressWarnings("unchecked") final MCArgument.Type type = (MCArgument.Type) cmboxType.getSelectedItem();
		if (type == MCArgument.Type.DYNAMIC) {
			@SuppressWarnings("unchecked") final String arg = ((ObjectToDisplayName<String>) cmboxArgument.getSelectedItem()).getValue();
			argument = new MCArgument(type, arg);
		} else {
			final String value = txtArgument.getText();
			if (value == null || value.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Error! Argument ii empty!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			argument = new MCArgument(type, value);
		}
		if (create) {
			this.table.addValue(argument);
		} else {
			this.table.setValue(index, argument);
		}
		this.table.updateTable();
		dispose();
	}

	private void updateValue() {
		switch (cmboxType.getSelectedIndex()) {
			case 0: //CUSTOM
				txtArgument.setEnabled(true);
				txtArgument.setVisible(true);
				cmboxArgument.setEnabled(false);
				cmboxArgument.setVisible(false);
				txtArgument.setText("");
				break;
			case 1: //PREDEFINED
				txtArgument.setEnabled(false);
				txtArgument.setVisible(true);
				cmboxArgument.setEnabled(false);
				cmboxArgument.setVisible(false);
				txtArgument.setText(MCArgument.specialArgPredefined);
				break;
			case 2: //DYNAMIC
				txtArgument.setEnabled(false);
				txtArgument.setVisible(false);
				cmboxArgument.setEnabled(true);
				cmboxArgument.setVisible(true);
				break;


		}
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
		contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
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
		panel3.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		final JLabel label1 = new JLabel();
		label1.setText("Argument Type: ");
		panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		panel3.add(spacer2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		cmboxType = new JComboBox();
		cmboxType.setToolTipText("Choose what type of argument should be added.\nJson-imported will import arguments from the specified version-json ");
		panel3.add(cmboxType, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Argument: ");
		panel3.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		txtArgument = new JTextField();
		panel3.add(txtArgument, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(141, 24), null, 0, false));
		final JSeparator separator1 = new JSeparator();
		panel3.add(separator1, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		cmboxArgument = new JComboBox();
		cmboxArgument.setEnabled(false);
		cmboxArgument.setVisible(false);
		panel3.add(cmboxArgument, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		label1.setLabelFor(cmboxType);
		label2.setLabelFor(txtArgument);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return contentPane;
	}
}
