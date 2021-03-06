package common.nw.creator.gui.pages;

import common.nw.core.gui.PageHolder;
import common.nw.creator.Creator;
import common.nw.creator.util.Reference;

import javax.swing.*;
import java.awt.*;

/**
 * @author Nuklearwurst
 */
public class PanelFinish implements PageHolder.IExtendedPageHandler {
	private final Creator creator;
	private JTextArea txtMain;
	private JPanel panelFinish;


	public PanelFinish(Creator creator) {
		this.creator = creator;
		txtMain.setBackground(SystemColor.menu);
	}

	@Override
	public Object getProperty(final String s) {
		switch (s) {
			case Reference.KEY_NAME:
				return "Finish";
			case Reference.KEY_TURNABLE:
				return true;
		}
		return null;
	}

	@Override
	public void onPageOpened(PageHolder holder, boolean forward) {
		if (forward) {
			txtMain.setText("Loading...");
			if (creator.createOutputFile(txtMain)) {
				txtMain.setText("Modpack creation finished!");
			} else {
				txtMain.setText("Modpack creation incomplete!\nOutput file not created!!!");
			}
		}
	}

	@Override
	public boolean onPageClosed(PageHolder holder, boolean forward) {
		return true;
	}

	@Override
	public JPanel getPanel() {
		return panelFinish;
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
		panelFinish = new JPanel();
		panelFinish.setLayout(new GridBagLayout());
		txtMain = new JTextArea();
		txtMain.setFont(new Font("Arial", Font.BOLD, 12));
		txtMain.setText("Loading...");
		GridBagConstraints gbc;
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		panelFinish.add(txtMain, gbc);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return panelFinish;
	}
}
