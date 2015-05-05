package common.nw.updater.gui;

import javax.swing.Icon;

public interface IProgressWatcher {

	public boolean isCancelled();
	
	/**
	 * used to force the updater to pause
	 * the updater will pause as long this method returns true
	 */
	public boolean isPaused();
	
	public boolean quitToLauncher();
	
	public int showErrorDialog(String title, String message);
	
	public int showConfirmDialog(String message, String title, int optionType, int messageType);

	public String showInputDialog(String message);
	
	public int showOptionDialog(String msg, String title, int optionType, int messageType, Icon icon, String[] options, String defaultOption);

	public void setDownloadProgress(String msg);

	public void setDownloadProgress(int progress);

	public void setDownloadProgress(String msg, int progress);

	public void setDownloadProgress(String msg, int progress, int maxProgress);

	public void setOverallProgress(int progress);

	public void setOverallProgress(String msg, int progress);
}
