package common.nw.utils;

public interface IProgressWatcher {

	boolean displayError();

	boolean isCancelled();

	void setDownloadProgress(String msg);

	void setDownloadProgress(int progress);

	void setDownloadProgress(String msg, int progress);

	void setDownloadProgress(String msg, int progress, int maxProgress);

	void setOverallProgress(int progress);

	void setOverallProgress(String msg, int progress);
}
