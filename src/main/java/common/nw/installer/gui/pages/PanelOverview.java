package common.nw.installer.gui.pages;


import common.nw.core.gui.PageHolder;
import common.nw.core.gui.WebbrowserPanel;

import javax.swing.*;
import java.awt.*;

/**
 * @author Nuklearwurst
 */
public class PanelOverview implements PageHolder.IExtendedPageHandler {
	private JPanel panel_overview;
	private WebbrowserPanel webView;

	public PanelOverview() {
		webView.setHideAdress(true);
		webView.setHideTitle(true);
		webView.loadContent("<html><body><div style=\"text-align: center;\"><b>Loading...</b></div></body></html>");
//		txtpnModpackInfo.addHyperlinkListener(new HyperlinkListener() {
//			@Override
//			public void hyperlinkUpdate(HyperlinkEvent e) {
//				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
//					if (Desktop.isDesktopSupported()) {
//						try {
//							Desktop.getDesktop().browse(e.getURL().toURI());
//						} catch (IOException | URISyntaxException e1) {
//							e1.printStackTrace();
//						}
//					}
//				}
//			}
//		});
	}

	public void openPage(String info) {
		if (info.startsWith("http://")
				|| info.startsWith("https://")
				|| info.startsWith("www.")) {
			webView.open(info);
		} else {
			webView.loadContent(info);
		}
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
		return panel_overview;
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
		panel_overview = new JPanel();
		panel_overview.setLayout(new BorderLayout(0, 0));
		webView = new WebbrowserPanel();
		panel_overview.add(webView, BorderLayout.CENTER);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return panel_overview;
	}
}
