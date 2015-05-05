package common.nw.creator.gui.pages;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import common.nw.creator.Creator;
import common.nw.creator.gui.CreatorWindow;
import common.nw.creator.gui.Reference;
import common.nw.gui.IPageHandler;
import common.nw.gui.PageHolder;
import common.nw.utils.Utils;

public class PanelSettings extends JPanel implements IPageHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JTextField txtOutput;
	public JTextField txtName;
	public JTextField txtFiles;
	public JTextField txtUrl;
	public JCheckBox chbxRead;
	private JButton btnOpenFiles;

	private Creator creator;
	/**
	 * Create the panel.
	 */
	public PanelSettings(Creator creator) {

		this.creator = creator;

		chbxRead = new JCheckBox("Read mods-structure from folder.");
		chbxRead.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (chbxRead.isSelected()) {
					btnOpenFiles.setEnabled(true);
					txtFiles.setEnabled(true);
					txtUrl.setEnabled(true);
				} else {
					btnOpenFiles.setEnabled(false);
					txtFiles.setEnabled(false);
					txtUrl.setEnabled(false);
				}
			}
		});
		chbxRead.setActionCommand("readStructure");
		chbxRead.setToolTipText("Do you want do read the mods from an existing folder structure?");

		JLabel lblName = new JLabel("Modpack name:");
		lblName.setToolTipText("The name of the Modpack");

		JLabel lblOutput = new JLabel("Output file:");
		lblOutput
		.setToolTipText("The generated .json file. \r\nThis is the file needed for the modpack updater.");

		txtOutput = new JTextField();
		txtOutput.setColumns(10);

		txtName = new JTextField();
		txtName.setColumns(10);

		JLabel lblUrl = new JLabel("Base URL:");
		lblUrl.setToolTipText("The url of the repo directory. \r\nUsed to generate the mods urls.");

		JLabel lblFiles = new JLabel("Modpack files:");
		lblFiles.setToolTipText("The directory that contains all the files that should be part of this modpack");

		txtFiles = new JTextField();
		txtFiles.setEnabled(false);
		txtFiles.setColumns(10);

		txtUrl = new JTextField();
		txtUrl.setEnabled(false);
		txtUrl.setColumns(10);

		JButton btnOpenOutput = new JButton("Open");
		btnOpenOutput.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String file = Utils.openFileOrDirectoryWithDefaultFileName(
						PanelSettings.this, null, "modpack.json");
				if (file != null) {
					txtOutput.setText(file);
				}
			}
		});
		btnOpenOutput.setActionCommand("Output");

		btnOpenFiles = new JButton("Open");
		btnOpenFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String file = Utils.openFolder(PanelSettings.this, null);
				if (file != null) {
					txtFiles.setText(file);
				}
			}
		});
		btnOpenFiles.setEnabled(false);
		btnOpenFiles.setActionCommand("Input");
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout
		.setHorizontalGroup(groupLayout
				.createParallelGroup(Alignment.LEADING)
				.addGroup(
						groupLayout
						.createSequentialGroup()
						.addContainerGap()
						.addGroup(
								groupLayout
								.createParallelGroup(
										Alignment.LEADING)
										.addGroup(
												groupLayout
												.createSequentialGroup()
												.addComponent(
														lblName,
														GroupLayout.PREFERRED_SIZE,
														75,
														GroupLayout.PREFERRED_SIZE)
														.addGap(18)
														.addComponent(
																txtName,
																GroupLayout.DEFAULT_SIZE,
																244,
																Short.MAX_VALUE)
																.addGap(93))
																.addGroup(
																		groupLayout
																		.createSequentialGroup()
																		.addComponent(
																				lblOutput,
																				GroupLayout.PREFERRED_SIZE,
																				55,
																				GroupLayout.PREFERRED_SIZE)
																				.addGap(38)
																				.addComponent(
																						txtOutput,
																						GroupLayout.DEFAULT_SIZE,
																						244,
																						Short.MAX_VALUE)
																						.addPreferredGap(
																								ComponentPlacement.RELATED)
																								.addComponent(
																										btnOpenOutput,
																										GroupLayout.PREFERRED_SIZE,
																										87,
																										GroupLayout.PREFERRED_SIZE))
																										.addComponent(
																												chbxRead,
																												GroupLayout.PREFERRED_SIZE,
																												187,
																												GroupLayout.PREFERRED_SIZE)
																												.addGroup(
																														groupLayout
																														.createSequentialGroup()
																														.addGap(21)
																														.addComponent(
																																lblUrl,
																																GroupLayout.PREFERRED_SIZE,
																																49,
																																GroupLayout.PREFERRED_SIZE)
																																.addGap(37)
																																.addComponent(
																																		txtUrl,
																																		GroupLayout.DEFAULT_SIZE,
																																		230,
																																		Short.MAX_VALUE)
																																		.addGap(93))
																																		.addGroup(
																																				groupLayout
																																				.createSequentialGroup()
																																				.addGap(21)
																																				.addComponent(
																																						lblFiles,
																																						GroupLayout.PREFERRED_SIZE,
																																						68,
																																						GroupLayout.PREFERRED_SIZE)
																																						.addGap(18)
																																						.addComponent(
																																								txtFiles,
																																								GroupLayout.DEFAULT_SIZE,
																																								230,
																																								Short.MAX_VALUE)
																																								.addPreferredGap(
																																										ComponentPlacement.RELATED)
																																										.addComponent(
																																												btnOpenFiles,
																																												GroupLayout.PREFERRED_SIZE,
																																												87,
																																												GroupLayout.PREFERRED_SIZE)))
																																												.addContainerGap()));
		groupLayout
		.setVerticalGroup(groupLayout
				.createParallelGroup(Alignment.LEADING)
				.addGroup(
						groupLayout
						.createSequentialGroup()
						.addGroup(
								groupLayout
								.createParallelGroup(
										Alignment.LEADING)
										.addGroup(
												groupLayout
												.createSequentialGroup()
												.addContainerGap()
												.addGroup(
														groupLayout
														.createParallelGroup(
																Alignment.LEADING)
																.addGroup(
																		groupLayout
																		.createSequentialGroup()
																		.addGap(3)
																		.addComponent(
																				lblName))
																				.addComponent(
																						txtName,
																						GroupLayout.PREFERRED_SIZE,
																						GroupLayout.DEFAULT_SIZE,
																						GroupLayout.PREFERRED_SIZE))
																						.addGap(6)
																						.addGroup(
																								groupLayout
																								.createParallelGroup(
																										Alignment.LEADING)
																										.addGroup(
																												groupLayout
																												.createSequentialGroup()
																												.addGap(4)
																												.addComponent(
																														lblOutput))
																														.addGroup(
																																groupLayout
																																.createSequentialGroup()
																																.addGap(1)
																																.addComponent(
																																		txtOutput,
																																		GroupLayout.PREFERRED_SIZE,
																																		GroupLayout.DEFAULT_SIZE,
																																		GroupLayout.PREFERRED_SIZE)))
																																		.addGap(20)
																																		.addComponent(
																																				chbxRead)
																																				.addGap(7)
																																				.addGroup(
																																						groupLayout
																																						.createParallelGroup(
																																								Alignment.LEADING)
																																								.addGroup(
																																										groupLayout
																																										.createSequentialGroup()
																																										.addGap(3)
																																										.addComponent(
																																												lblUrl))
																																												.addComponent(
																																														txtUrl,
																																														GroupLayout.PREFERRED_SIZE,
																																														GroupLayout.DEFAULT_SIZE,
																																														GroupLayout.PREFERRED_SIZE)))
																																														.addGroup(
																																																groupLayout
																																																.createSequentialGroup()
																																																.addGap(37)
																																																.addComponent(
																																																		btnOpenOutput)))
																																																		.addGroup(
																																																				groupLayout
																																																				.createParallelGroup(
																																																						Alignment.LEADING)
																																																						.addGroup(
																																																								groupLayout
																																																								.createSequentialGroup()
																																																								.addGap(6)
																																																								.addGroup(
																																																										groupLayout
																																																										.createParallelGroup(
																																																												Alignment.LEADING)
																																																												.addGroup(
																																																														groupLayout
																																																														.createSequentialGroup()
																																																														.addGap(4)
																																																														.addComponent(
																																																																lblFiles))
																																																																.addComponent(
																																																																		btnOpenFiles)))
																																																																		.addGroup(
																																																																				groupLayout
																																																																				.createSequentialGroup()
																																																																				.addGap(7)
																																																																				.addComponent(
																																																																						txtFiles,
																																																																						GroupLayout.PREFERRED_SIZE,
																																																																						GroupLayout.DEFAULT_SIZE,
																																																																						GroupLayout.PREFERRED_SIZE)))
																																																																						.addContainerGap(143, Short.MAX_VALUE)));
		setLayout(groupLayout);

	}

	@Override
	public Object getProperty(String s) {
		if(s.equals(Reference.KEY_NAME)) {
			return "Settings";
		}
		if(s.equals(Reference.KEY_TURNABLE)) {
			return true;
		}
		return null;
	}

	@Override
	public void onPageOpened(PageHolder holder, boolean forward) {
		if(forward) {
			//import data
			this.txtName.setText(creator.modpack.modpackName);
			this.txtUrl.setText(creator.modpack.modpackRepo);
			
			this.txtOutput.setText(creator.outputLoc);
			this.txtFiles.setText(creator.fileLoc);
		} else {
			//don't read a second time
			this.chbxRead.setSelected(false);
			btnOpenFiles.setEnabled(false);
			txtFiles.setEnabled(false);
			txtUrl.setEnabled(false);
		}
	}

	@Override
	public boolean onPageClosed(PageHolder holder, boolean forward) {
		// validating entries
		if (forward) {
			boolean b = true;
			if (txtName.getText() == null || txtName.getText().isEmpty()
					|| txtOutput.getText() == null
					|| txtOutput.getText().isEmpty()) {
				b = false;
			}
			if (!new File(txtOutput.getText()).exists()) {
				b = false;
			}
			if (chbxRead.isSelected()) {
				if (txtFiles.getText() == null || txtFiles.getText().isEmpty()
						|| txtUrl.getText() == null
						|| txtUrl.getText().isEmpty()) {
					b = false;
				}
				if (!new File(txtFiles.getText()).exists()) {
					b = false;
				}
			}
			if (!b) {
				if(!CreatorWindow.DEBUG) {
					JOptionPane.showMessageDialog(this,
							"Not all requiered fields are filled in!", "Error",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}
		
		
		
		//set new values
		creator.fileLoc = this.txtFiles.getText();
		creator.outputLoc = this.txtOutput.getText();
		creator.modpack.modpackName = this.txtName.getText();
		creator.modpack.modpackRepo = this.txtUrl.getText();
		creator.shouldReadFiles = chbxRead.isSelected();

		return true;
	}
}
