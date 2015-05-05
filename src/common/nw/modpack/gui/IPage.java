package common.nw.modpack.gui;

public interface IPage {

	public void onPageOpened(IPageHolder parent, boolean forward);

	/**
	 * 
	 * @param parent
	 * @param forward
	 *            direction (proceeding == true)
	 * @return
	 */
	public boolean onPageClosed(IPageHolder parent, boolean forward);

	/**
	 * what should the button say, which leads to this page?
	 * 
	 * @return
	 */
	@Deprecated
	public String getActionText();

	/**
	 * is it possible to flip to another page?
	 * 
	 * @return
	 */
	public boolean canBeTurned();

	/**
	 * name of the page
	 * 
	 * @return
	 */
	public String getPageName();
}
