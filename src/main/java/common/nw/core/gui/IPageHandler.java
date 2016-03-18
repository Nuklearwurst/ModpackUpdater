package common.nw.core.gui;

public interface IPageHandler {

	Object getProperty(String s);

	void onPageOpened(PageHolder holder, boolean forward);

	@SuppressWarnings("UnusedParameters")
	boolean onPageClosed(PageHolder holder, boolean forward);

}
