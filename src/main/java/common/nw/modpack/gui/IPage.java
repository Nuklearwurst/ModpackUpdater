package common.nw.modpack.gui;

public interface IPage {

	void onPageOpened(IPageHolder parent, boolean forward);

	/**
	 * 
	 * @param parent
	 * @param forward
	 *            direction (proceeding == true)
	 * @return
	 */
	boolean onPageClosed(IPageHolder parent, boolean forward);

	/**
	 * what should the button say, which leads to this page?
	 * 
	 * @return
	 */
	@Deprecated
	String getActionText();

	/**
	 * is it possible to flip to another page?
	 * 
	 * @return
	 */
	boolean canBeTurned();

	/**
	 * name of the page
	 * 
	 * @return
	 */
	String getPageName();
}
