package common.nw.creator.gui_legacy.pages.dialog;

import common.nw.creator.gui.TableModelList;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class EditArgumentsDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTable table;

	private List<String> arguments;


	/**
	 * Create the dialog.
	 */
	@SuppressWarnings("SameParameterValue")
	public EditArgumentsDialog(List<String> arguments, JFrame parent, boolean modal) {
		super(parent, modal);
		this.arguments = arguments;

		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane();

		JButton btnCreate = new JButton("Add");
		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addArg();
			}
		});

		JButton btnEdit = new JButton("Edit");
		btnEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editArg();
			}
		});

		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				remove();
			}
		});

		JButton btnUp = new JButton("Up");
		btnUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sortUp();
			}
		});

		JButton btnDown = new JButton("Down");
		btnDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sortDown();
			}
		});

		JButton btnAddSpecial = new JButton("Add S.");
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
				gl_contentPanel.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_contentPanel.createSequentialGroup()
								.addContainerGap()
								.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING, false)
										.addComponent(btnAddSpecial, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnRemove, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnEdit, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnCreate, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnUp, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(btnDown, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
		);
		gl_contentPanel.setVerticalGroup(
				gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPanel.createSequentialGroup()
								.addGap(29)
								.addComponent(btnCreate)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(btnEdit)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(btnRemove)
								.addGap(18)
								.addComponent(btnUp)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(btnDown)
								.addPreferredGap(ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
								.addComponent(btnAddSpecial))
						.addGroup(gl_contentPanel.createSequentialGroup()
								.addContainerGap()
								.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE))
		);

		table = new JTable();
		table.setModel(new TableModelList(new String[]{"Argument"}, null, arguments));
		table.setFillsViewportHeight(true);
		scrollPane.setViewportView(table);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						finish();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
		updateTable();
	}

	private void addArg() {
		String s = JOptionPane.showInputDialog(this, "Please enter argument:", "Add argument", JOptionPane.QUESTION_MESSAGE);
		if (s != null && !s.isEmpty()) {
			this.arguments.add(s);
		}
		updateTable();
	}

	private void editArg() {
		if (table.getSelectedRow() == -1) {
			return;
		}
		int i = table.getSelectedRow();
		String s = arguments.get(i);
		s = (String) JOptionPane.showInputDialog(this, "Please enter argument:", "Edit argument", JOptionPane.QUESTION_MESSAGE, null, null, s);
		if (s != null && !s.isEmpty()) {
			arguments.set(i, s);
		}
		updateTable();
	}

	private void remove() {
		int c = table.getSelectedRowCount();
		if (c > 1) {
			int r = JOptionPane.showConfirmDialog(this, "Do you want to delete this " + c + " arguments?");
			if (r != JOptionPane.YES_OPTION) {
				return;
			}
		}
		int j = 0;
		for (int i : table.getSelectedRows()) {
			arguments.remove(i - j);
			j++;
		}
		table.clearSelection();
		updateTable();
	}

	private void updateTable() {
		TableModelList model = (TableModelList) table.getModel();
		model.updateData();
		table.revalidate();
		table.repaint();
	}

	private void finish() {
		this.dispose();
	}

	private void sortUp() {
		if (table.getSelectedRowCount() > 1) {
			return;
		}
		int i = table.getSelectedRow();
		if (i <= 0) {
			return;
		}
		String old = arguments.get(i - 1);
		arguments.set(i - 1, arguments.get(i));
		arguments.set(i, old);
		table.setRowSelectionInterval(i - 1, i - 1);
		updateTable();
	}

	private void sortDown() {
		if (table.getSelectedRowCount() > 1) {
			return;
		}
		int i = table.getSelectedRow();
		if (i < 0 || i >= arguments.size() - 1) {
			return;
		}
		String old = arguments.get(i + 1);
		arguments.set(i + 1, arguments.get(i));
		arguments.set(i, old);
		table.setRowSelectionInterval(i + 1, i + 1);
		updateTable();
	}
}
