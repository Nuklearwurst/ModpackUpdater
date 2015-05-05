package common.nw.gui;

public interface IPageHandler {
	
	public Object getProperty(String s);
	
	public void onPageOpened(PageHolder holder, boolean forward);

	public boolean onPageClosed(PageHolder holder, boolean forward);

}
