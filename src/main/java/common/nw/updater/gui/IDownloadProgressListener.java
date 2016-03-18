package common.nw.updater.gui;

/**
 * @author Nuklearwurst
 */
public interface IDownloadProgressListener {

	boolean isCancelled();

	/**
	 * used to force the updater to pause
	 * the updater will pause as long this method returns true
	 */
	boolean isPaused();

	void setDownloadProgress(String msg);

	void setDownloadProgress(int progress);

	void setDownloadProgress(String msg, int progress);

	void setDownloadProgress(String msg, int progress, int maxProgress);
}
