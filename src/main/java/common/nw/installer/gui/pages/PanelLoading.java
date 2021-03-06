package common.nw.installer.gui.pages;


import common.nw.core.gui.PageHolder;

import javax.swing.*;
import java.awt.*;

/**
 * @author Nuklearwurst
 */
public class PanelLoading implements PageHolder.IExtendedPageHandler {
	public JProgressBar progressbar;
	public JLabel lblProgress;
	private JPanel panel_loading;

	@Override
	public Object getProperty(String s) {
		return null;
	}

	@Override
	public void onPageOpened(PageHolder holder, boolean forward) {

	}

	@Override
	public boolean onPageClosed(PageHolder holder, boolean forward) {
		return true;
	}

	@Override
	public JPanel getPanel() {
		return panel_loading;
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
		panel_loading = new JPanel();
		panel_loading.setLayout(new GridBagLayout());
		progressbar = new JProgressBar();
		progressbar.setStringPainted(false);
		GridBagConstraints gbc;
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 10, 0, 10);
		panel_loading.add(progressbar, gbc);
		lblProgress = new JLabel();
		lblProgress.setText("Initializing...");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel_loading.add(lblProgress, gbc);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return panel_loading;
	}
}
