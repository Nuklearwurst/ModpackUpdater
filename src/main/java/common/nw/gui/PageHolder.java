package common.nw.gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Card Layout Handler
 */
public class PageHolder {
	
	private int page;
	
	private JPanel mainPanel;
	private CardLayout layout;
	
	private List<IPageHandler> handler;
	private IPageHandler globalHandler;
	
	private boolean globalHandlerFallbackMode;
	
	private List<IPageUpdateListener> updateListener;
	
	public PageHolder() {
		//init values
		handler = new ArrayList<IPageHandler>();
		updateListener = new ArrayList<IPageUpdateListener>();
		globalHandler = null;
		globalHandlerFallbackMode = false;
		page = 0;
		
		mainPanel = new JPanel();
		layout = new CardLayout();
		
		mainPanel.setLayout(layout);
	}
	
	public void addPageUpdateListener(IPageUpdateListener u) {
		this.updateListener.add(u);
	}
	
	public Object getCurrentPageProperty(String s) {
		return getPageProperty(s, page);
	}
	
	private Object getPageProperty(String s, int page) {
		return getCurrentPageHandler().getProperty(s);
	}
	
	/**
	 * returns the current page Handler, if there is none return the global one if wanted
	 * @return
	 */
	private IPageHandler getCurrentPageHandler() {
		return getCurrentPageHandler(true, false);
	}
	@SuppressWarnings("SameParameterValue")
	private IPageHandler getCurrentPageHandler(boolean fallback, boolean ignoreFallbackSettings) {
		IPageHandler handler = getPageHandler(getCurrentPageIndex());
		if(fallback && handler == null) {
			if(ignoreFallbackSettings) {
				//return globalHandler anyway
				return globalHandler;
			} else if(globalHandlerFallbackMode) {
				//return global handler
				return globalHandler;
			}
			//return null
		}
		return handler;
	}
	
	public IPageHandler getGlobalPageHandler() {
		return globalHandler;
	}
	
	private IPageHandler getPageHandler(int page) {
		return handler.get(page);
	}
	
	public void addPage(JPanel p) {
		mainPanel.add(p);
		if(p instanceof IPageHandler) {
			handler.add((IPageHandler) p);
		} else {
			handler.add(null);	
		}
	}
	
	public void addPage(JPanel p, String s) {
		mainPanel.add(p, s);
		if(p instanceof IPageHandler) {
			handler.add((IPageHandler) p);
		} else {
			handler.add(null);	
		}
	}
	
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
	 * @return
	 */
	private int getPageCount() {
		return mainPanel.getComponentCount();
	}
	
	/**
	 * shows the next page
	 */
	public void nextPage() {
		if(!tryLeavePage(true)) {
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
		if(!tryLeavePage(false)) {
			return;
		}
		layout.previous(mainPanel);
		page = page - 1; 
		if(page < 0) {
			page = getPageCount() + page;
		}
		tryOpenPage(false);
	}
	
	/**
	 * shows the first page
	 */
	public void firstPage() {
		if(!tryLeavePage(false)) {
			return;
		}
		layout.first(mainPanel);
		page = 0;
		tryOpenPage(false);
	}
	
	/**
	 * shows the last page
	 */
	public void lastPage() {
		if(!tryLeavePage(true)) {
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
		for(IPageUpdateListener l : updateListener) {
			l.onPageChanged(this, getCurrentPageHandler());
		}
	}
	
	/**
	 * returns the index of the last page
	 * @return
	 */
	public int getLastPageIndex() {
		return getPageCount() - 1;
	}
	
	/**
	 * is there a page after this one
	 * @return
	 */
	private boolean hasNextPage() {
		return page < getLastPageIndex();
	}
	
	/**
	 * is there a page before this one
	 * @return
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
		if(h != null) {
			b = h.onPageClosed(this, dir);
			if(globalHandlerFallbackMode) { //don't call global handler
				return b;
			}
		}
		//call global handler
		if(globalHandler != null) {
			return b && globalHandler.onPageClosed(this, dir);
		}
		return b;
	}
	
	private void tryOpenPage(boolean dir) {
		IPageHandler h = getCurrentPageHandler(false, false);
		if(h != null) {
			h.onPageOpened(this, dir);
			if(globalHandlerFallbackMode) { //don't call global handler
				return;
			}
		}
		//call global handler
		if(globalHandler != null) {
			globalHandler.onPageOpened(this, dir);
		}
		
		updatePage();
	}
	
	public JPanel getPanel() {
		return mainPanel;
	}
}
