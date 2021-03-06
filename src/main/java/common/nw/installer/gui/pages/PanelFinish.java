package common.nw.installer.gui.pages;


import common.nw.core.gui.PageHolder;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * @author Nuklearwurst
 */
public class PanelFinish implements PageHolder.IExtendedPageHandler {
	private JTextPane txtpnFinish;
	private JTextPane txtpnInstructions;
	private JTextField txtExampleJVMOptions;
	private JPanel panel_finish;
	private JLabel lblJVMArgs;

	public PanelFinish() {
		txtpnFinish.setBackground(SystemColor.menu);
		txtpnInstructions.setBackground(SystemColor.menu);
		txtpnInstructions.setBorder(new LineBorder(Color.BLACK));
	}

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
		return panel_finish;
	}

	public void setNoErrors() {
		txtpnFinish.setText("Installation finished without any errors!");
		txtpnFinish.setBackground(SystemColor.menu);
		txtpnInstructions.setVisible(true);
		txtExampleJVMOptions.setVisible(true);
		lblJVMArgs.setVisible(true);
	}

	public void setInstallErrorred(String errorMessage) {
		txtpnFinish.setText("Installation errored! \n" + errorMessage);
		txtpnFinish.setBackground(new Color(255, 187, 187));
		txtpnInstructions.setVisible(false);
		txtExampleJVMOptions.setVisible(false);
		lblJVMArgs.setVisible(false);
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
		panel_finish = new JPanel();
		panel_finish.setLayout(new GridBagLayout());
		txtpnInstructions = new JTextPane();
		txtpnInstructions.setEditable(false);
		txtpnInstructions.setFont(new Font("Arial", txtpnInstructions.getFont().getStyle(), 12));
		txtpnInstructions.setText("To use the modpack select the versionname you chose in the profile editor under 'use Version'\n\nYou might also want to change the Game-Directory and  JVM-arguments (for bigger modpacks)");
		GridBagConstraints gbc;
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		panel_finish.add(txtpnInstructions, gbc);
		lblJVMArgs = new JLabel();
		lblJVMArgs.setText("Example JVM arguments:");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(4, 0, 0, 0);
		panel_finish.add(lblJVMArgs, gbc);
		txtExampleJVMOptions = new JTextField();
		txtExampleJVMOptions.setEditable(false);
		txtExampleJVMOptions.setText("-Xmx2G -XX:PermSize=256m -XX:MaxPermSize=512m");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(4, 4, 4, 4);
		panel_finish.add(txtExampleJVMOptions, gbc);
		final JScrollPane scrollPane1 = new JScrollPane();
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		panel_finish.add(scrollPane1, gbc);
		txtpnFinish = new JTextPane();
		txtpnFinish.setEditable(false);
		txtpnFinish.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 12));
		txtpnFinish.setText("Installation Finished!");
		scrollPane1.setViewportView(txtpnFinish);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return panel_finish;
	}
}
