package common.nw.creator.gui.pages;

import com.google.gson.Gson;
import common.nw.creator.Creator;
import common.nw.creator.gui.IDropFileHandler;
import common.nw.creator.gui.Reference;
import common.nw.creator.properties.CreatorProperties;
import common.nw.gui.IPageHandler;
import common.nw.gui.PageHolder;
import common.nw.modpack.RepoModpack;
import common.nw.utils.SwingUtils;
import common.nw.utils.Utils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;

/**
 * @author Nuklearwurst
 */
public class PanelInit implements IPageHandler, IDropFileHandler {
	private JRadioButton rdbtnCreate;
	private JPanel panel_init;
	private JRadioButton rdbtnLoad;
	private JTextField txtLoadFrom;
	private JButton btnLoadOpen;
	private JCheckBox chbxProperties;
	private JLabel lblLoading;

	private Creator creator;

	public PanelInit(Creator creator) {
		this.creator = creator;
	}

	public void setBorder(Border lineBorder) {
		panel_init.setBorder(lineBorder);
	}

	@Override
	public boolean dropFile(File file) {
		if (file != null && file.exists() && !file.isDirectory()) {
			if (!file.getAbsolutePath().endsWith(".json")) { // check for json
				int result = JOptionPane
						.showConfirmDialog(
								panel_init,
								"WARNING! \nFile appears to be no valid json File! \nThis might not work. \nContinue anyway?",
								"Invalid Filename", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE);
				if (result != JOptionPane.YES_OPTION) {
					return false;
				}
			}
			txtLoadFrom.setText(file.getAbsolutePath());
			// enable gui elements
			rdbtnLoad.setSelected(true);
			txtLoadFrom.setEnabled(true);
			btnLoadOpen.setEnabled(true);
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
		lblLoading.setVisible(false);
	}

	@Override
	public boolean onPageClosed(PageHolder holder, boolean forward) {
		if(txtLoadFrom.getText() != null && !txtLoadFrom.getText().isEmpty()) {
			CreatorProperties.LAST_OPENED_MODPACK = txtLoadFrom.getText();
		}
		if (forward && rdbtnLoad.isSelected()) {
			lblLoading.setVisible(true);
			File file = new File(txtLoadFrom.getText());
			if (file.exists() && !file.isDirectory()) {
				Gson gson = new Gson();
				try {
					FileReader reader = new FileReader(file);
					creator.modpack = gson.fromJson(reader,
							RepoModpack.class);
					creator.outputLoc = txtLoadFrom.getText();
					reader.close();

				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(panel_init,
							"Error reading file!", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(panel_init, "File not found!",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		return true;
	}

	public JPanel getPanel() {
		return panel_init;
	}

	private void createUIComponents() {
		ActionListener checkBoxListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("create")) {
					txtLoadFrom.setEnabled(false);
					btnLoadOpen.setEnabled(false);
					CreatorProperties.LOAD = false;
				} else {
					txtLoadFrom.setEnabled(true);
					btnLoadOpen.setEnabled(true);
					CreatorProperties.LOAD = true;
				}
			}
		};

		rdbtnCreate = new JRadioButton("Create new Modpack");
		rdbtnCreate.addActionListener(checkBoxListener);
		rdbtnCreate.setSelected(!CreatorProperties.LOAD);

		rdbtnLoad = new JRadioButton("Open existing Modpack");
		rdbtnLoad.addActionListener(checkBoxListener);
		rdbtnLoad.setSelected(CreatorProperties.LOAD);

		txtLoadFrom = new JTextField();
		txtLoadFrom.setEnabled(CreatorProperties.LOAD);
		txtLoadFrom.setColumns(10);
		txtLoadFrom.setText(CreatorProperties.LAST_OPENED_MODPACK);
		txtLoadFrom.setComponentPopupMenu(SwingUtils.createTextPopupMenu(txtLoadFrom));

		btnLoadOpen = new JButton("Open");
		btnLoadOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String file = Utils.openFile(panel_init, new File(txtLoadFrom.getText()));
				if (file != null) {
					txtLoadFrom.setText(file);
					CreatorProperties.LAST_OPENED_MODPACK = file;
				}
			}
		});
		btnLoadOpen.setEnabled(CreatorProperties.LOAD);

		chbxProperties = new JCheckBox("Generate a properties file", true);
		chbxProperties.setEnabled(!CreatorProperties.hasProperties());
	}

	public boolean shouldSaveProperties() {
		return chbxProperties.isSelected();
	}

}
