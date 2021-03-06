package common.nw.core.gui;

import common.nw.creator.util.Reference;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Card Layout Handler
 */
public class PageHolder {

	private int page;

	private final JPanel mainPanel;
	private final CardLayout layout;

	private final List<IPageHandler> handler;
	private final IPageHandler globalHandler;

	@SuppressWarnings("CanBeFinal")
	private boolean globalHandlerFallbackMode;

	private final List<IPageUpdateListener> updateListener;

	public PageHolder() {
		//init values
		handler = new ArrayList<>();
		updateListener = new ArrayList<>();
		globalHandler = null;
		globalHandlerFallbackMode = false;
		page = 0;

		mainPanel = new JPanel();
		layout = new CardLayout();

		mainPanel.setLayout(layout);
	}

	@SuppressWarnings("unused")
	public void addPageUpdateListener(IPageUpdateListener u) {
		this.updateListener.add(u);
	}

	public Object getCurrentPageProperty(String s) {
		return getCurrentPageHandler().getProperty(s);
	}

	/**
	 * returns the current page Handler, if there is none return the global one if wanted
	 */
	private IPageHandler getCurrentPageHandler() {
		return getCurrentPageHandler(true, false);
	}

	@SuppressWarnings("SameParameterValue")
	private IPageHandler getCurrentPageHandler(boolean fallback, boolean ignoreFallbackSettings) {
		IPageHandler handler = getPageHandler(getCurrentPageIndex());
		if (fallback && handler == null) {
			if (ignoreFallbackSettings) {
				//return globalHandler anyway
				return globalHandler;
			} else if (globalHandlerFallbackMode) {
				//return global handler
				return globalHandler;
			}
			//return null
		}
		return handler;
	}

	@SuppressWarnings("unused")
	public IPageHandler getGlobalPageHandler() {
		return globalHandler;
	}

	private IPageHandler getPageHandler(int page) {
		return handler.get(page);
	}

	@SuppressWarnings("unused")
	public void addPageWithName(IExtendedPageHandler p) {
		addPage(p.getPanel(), p, (String) p.getProperty(Reference.KEY_NAME));
	}

	@SuppressWarnings("unused")
	public void addPageWithName(JPanel p, IPageHandler h) {
		addPage(p, h, (String) h.getProperty(Reference.KEY_NAME));
	}

	public void addPage(IExtendedPageHandler p) {
		addPage(p.getPanel(), p);
	}

	@SuppressWarnings("unused")
	public void addPage(IExtendedPageHandler p, String s) {
		addPage(p.getPanel(), p, s);
	}

	@SuppressWarnings("unused")
	public void addPage(JPanel p) {
		mainPanel.add(p);
		if (p instanceof IPageHandler) {
			handler.add((IPageHandler) p);
		} else {
			handler.add(null);
		}
	}

	public void addPage(JPanel p, String s) {
		mainPanel.add(p, s);
		if (p instanceof IPageHandler) {
			handler.add((IPageHandler) p);
		} else {
			handler.add(null);
		}
	}

	@SuppressWarnings("WeakerAccess")
	public void addPage(JPanel p, IPageHandler handler) {
		mainPanel.add(p);
		this.handler.add(handler);
	}

	public void addPage(JPanel p, IPageHandler handler, String s) {
		mainPanel.add(p, s);
		this.handler.add(handler);
	}

	public int getCurrentPageIndex() {
		return page;
	}

	/**
	 * returns the number of pages
	 */
	private int getPageCount() {
		return mainPanel.getComponentCount();
	}

	/**
	 * shows the next page
	 */
	public void nextPage() {
		if (!tryLeavePage(true)) {
			return;
		}
		layout.next(mainPanel);
		page = (page + 1) % getPageCount();
		tryOpenPage(true);
	}

	/**
	 * shows the previous page
	 */
	public void previousPage() {
		if (!tryLeavePage(false)) {
			return;
		}
		layout.previous(mainPanel);
		page = page - 1;
		if (page < 0) {
			page = getPageCount() + page;
		}
		tryOpenPage(false);
	}

	/**
	 * shows the first page
	 */
	public void firstPage() {
		if (!tryLeavePage(false)) {
			return;
		}
		layout.first(mainPanel);
		page = 0;
		tryOpenPage(false);
	}

	/**
	 * shows the last page
	 */
	@SuppressWarnings("unused")
	public void lastPage() {
		if (!tryLeavePage(true)) {
			return;
		}
		layout.last(mainPanel);
		page = getLastPageIndex();
		tryOpenPage(true);
	}

	/**
	 * update Page
	 */
	public void updatePage() {
		for (IPageUpdateListener l : updateListener) {
			l.onPageChanged(this, getCurrentPageHandler());
		}
	}

	/**
	 * returns the index of the last page
	 */
	public int getLastPageIndex() {
		return getPageCount() - 1;
	}

	/**
	 * is there a page after this one
	 */
	private boolean hasNextPage() {
		return page < getLastPageIndex();
	}

	/**
	 * is there a page before this one
	 */
	private boolean hasPreviousPage() {
		return page > 0;
	}

	public boolean isLastPage() {
		return !hasNextPage();
	}

	public boolean isFirstPage() {
		return !hasPreviousPage();
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean tryLeavePage(boolean dir) {
		IPageHandler h = getCurrentPageHandler(false, false);
		boolean b = true;
		if (h != null) {
			b = h.onPageClosed(this, dir);
			if (globalHandlerFallbackMode) { //don't call global handler
				return b;
			}
		}
		//call global handler
		if (globalHandler != null) {
			return b && globalHandler.onPageClosed(this, dir);
		}
		return b;
	}

	private void tryOpenPage(boolean dir) {
		IPageHandler h = getCurrentPageHandler(false, false);
		if (h != null) {
			h.onPageOpened(this, dir);
			if (globalHandlerFallbackMode) { //don't call global handler
				return;
			}
		}
		//call global handler
		if (globalHandler != null) {
			globalHandler.onPageOpened(this, dir);
		}

		updatePage();
	}

	public JPanel getPanel() {
		return mainPanel;
	}

	/**
	 * update listener, get notified whenever the page changes
	 */
	@SuppressWarnings("WeakerAccess")
	public interface IPageUpdateListener {
		void onPageChanged(PageHolder holder, IPageHandler handler);
	}

	/**
	 * receives callbacks on page changes etc.
	 */
	public interface IPageHandler {

		Object getProperty(String s);

		void onPageOpened(PageHolder holder, boolean forward);

		@SuppressWarnings("UnusedParameters")
		boolean onPageClosed(PageHolder holder, boolean forward);

	}

	/**
	 * Extension to {@link IPageHandler} that also provides a {@link JPanel}
	 *
	 * @author Nuklearwurst
	 */
	public interface IExtendedPageHandler extends IPageHandler {
		JPanel getPanel();
	}
}
