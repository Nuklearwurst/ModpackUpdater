package common.nw.creator.gui.pages;

import com.google.gson.Gson;
import common.nw.creator.Creator;
import common.nw.creator.gui.FileTransferHandler;
import common.nw.creator.gui.IDropFileHandler;
import common.nw.creator.gui.Reference;
import common.nw.gui.IPageHandler;
import common.nw.gui.PageHolder;
import common.nw.modpack.RepoModpack;
import common.nw.utils.Utils;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;

public class PanelInit extends JPanel implements IPageHandler, IDropFileHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtOpen;
	private JButton btnOpen;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton rbtnEdit;

	private JLabel lblImporting;
	
	private Creator creator;

	/**
	 * Create the panel.
	 */
	public PanelInit(Creator creator) {

		this.creator = creator;
		JRadioButton rbtnCreate = new JRadioButton("Create new Modpack");
		rbtnCreate.setActionCommand("create");
		rbtnCreate.addActionListener(checkBoxListener);
		buttonGroup.add(rbtnCreate);
		rbtnCreate.setToolTipText("Create a new modpack from scratch.");
		rbtnCreate.setSelected(true);

		rbtnEdit = new JRadioButton("Open existing Modpack");
		rbtnEdit.setActionCommand("edit");
		rbtnEdit.addActionListener(checkBoxListener);
		buttonGroup.add(rbtnEdit);
		rbtnEdit.setToolTipText("Edit an exsiting modpack.json file.");

		txtOpen = new JTextField();
		txtOpen.setEnabled(false);
		txtOpen.setColumns(10);

		btnOpen = new JButton("Open");
		btnOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String file = Utils.openFile(PanelInit.this, null);
				if (file != null) {
					txtOpen.setText(file);
				}
			}
		});
		btnOpen.setEnabled(false);

		lblImporting = new JLabel("Importing...");
		lblImporting.setVisible(false);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout
				.setHorizontalGroup(groupLayout
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								groupLayout
										.createSequentialGroup()
										.addGap(19)
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.LEADING)
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addComponent(
																				rbtnCreate,
																				GroupLayout.PREFERRED_SIZE,
																				127,
																				GroupLayout.PREFERRED_SIZE))
														.addGroup(
																groupLayout
																		.createSequentialGroup()
																		.addPreferredGap(
																				ComponentPlacement.RELATED)
																		.addGroup(
																				groupLayout
																						.createParallelGroup(
																								Alignment.LEADING)
																						.addComponent(
																								rbtnEdit,
																								GroupLayout.PREFERRED_SIZE,
																								137,
																								GroupLayout.PREFERRED_SIZE)
																						.addGroup(
																								groupLayout
																										.createSequentialGroup()
																										.addGap(21)
																										.addGroup(
																												groupLayout
																														.createParallelGroup(
																																Alignment.LEADING)
																														.addGroup(
																																groupLayout
																																		.createSequentialGroup()
																																		.addGap(10)
																																		.addComponent(
																																				lblImporting))
																														.addGroup(
																																groupLayout
																																		.createSequentialGroup()
																																		.addComponent(
																																				txtOpen,
																																				GroupLayout.DEFAULT_SIZE,
																																				321,
																																				Short.MAX_VALUE)
																																		.addPreferredGap(
																																				ComponentPlacement.RELATED)
																																		.addComponent(
																																				btnOpen,
																																				GroupLayout.PREFERRED_SIZE,
																																				73,
																																				GroupLayout.PREFERRED_SIZE)))))))
										.addContainerGap()));
		groupLayout
				.setVerticalGroup(groupLayout
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								groupLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(rbtnCreate)
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addComponent(rbtnEdit)
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addGroup(
												groupLayout
														.createParallelGroup(
																Alignment.BASELINE)
														.addComponent(
																txtOpen,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(btnOpen))
										.addPreferredGap(
												ComponentPlacement.RELATED)
										.addComponent(lblImporting)
										.addContainerGap(202, Short.MAX_VALUE)));
		setLayout(groupLayout);

		this.setTransferHandler(new FileTransferHandler(this));
	}

	private ActionListener checkBoxListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("create")) {
				txtOpen.setEnabled(false);
				btnOpen.setEnabled(false);
			} else {
				txtOpen.setEnabled(true);
				btnOpen.setEnabled(true);
			}
		}
	};

	@Override
	public boolean dropFile(File file) {
		if (file != null && file.exists() && !file.isDirectory()) {
			if (!file.getAbsolutePath().endsWith(".json")) { // check for json
				int result = JOptionPane
						.showConfirmDialog(
								this,
								"WARNING! \nFile appears to be no valid json File! \nThis might not work. \nContinue anyway?",
								"Invalid Filename", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE);
				if (result != JOptionPane.YES_OPTION) {
					return false;
				}
			}
			txtOpen.setText(file.getAbsolutePath());
			// enable gui elements
			rbtnEdit.setSelected(true);
			txtOpen.setEnabled(true);
			btnOpen.setEnabled(true);
		}
		return false;
	}

	@Override
	public Object getProperty(String s) {
		if(s.equals(Reference.KEY_NAME)) {
			return "Init";
		}
		if(s.equals(Reference.KEY_TURNABLE)) {
			return true;
		}
		return null;
	}

	@Override
	public void onPageOpened(PageHolder holder, boolean forward) {
		lblImporting.setVisible(false);
	}

	@Override
	public boolean onPageClosed(PageHolder holder, boolean forward) {
		if (forward && rbtnEdit.isSelected()) {
			lblImporting.setVisible(true);
			File file = new File(txtOpen.getText());
			if (file.exists() && !file.isDirectory()) {
				Gson gson = new Gson();
				try {
					FileReader reader = new FileReader(file);
					creator.modpack = gson.fromJson(reader,
							RepoModpack.class);
					creator.outputLoc = txtOpen.getText();
					reader.close();

				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(this,
							"Error reading file!", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(this, "File not found!",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		return true;
	}

}
