package common.nw.creator.gui_legacy.pages;

import common.nw.core.gui.IPageHandler;
import common.nw.core.gui.PageHolder;
import common.nw.creator.Creator;
import common.nw.creator.gui.Reference;

import javax.swing.*;

public class PanelFinish extends JPanel implements IPageHandler {

	private Creator creator;

	/**
	 * Create the panel.
	 */
	public PanelFinish(Creator creator) {

		this.creator = creator;
		JLabel lblFinish = new JLabel("Finished");
		add(lblFinish);

	}

	@Override
	public Object getProperty(String s) {
		if (s.equals(Reference.KEY_NAME)) {
			return "Finish";
		}
		if (s.equals(Reference.KEY_TURNABLE)) {
			return true;
		}
		return null;
	}

	@Override
	public void onPageOpened(PageHolder holder, boolean forward) {
		if (forward) {
			creator.createOutputFile(this);
		}
	}

	@Override
	public boolean onPageClosed(PageHolder holder, boolean forward) {
		return true;
	}

}
