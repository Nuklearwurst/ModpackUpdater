package common.nw.utils;

public interface IProgressWatcher {

	public boolean displayError();

	public boolean isCancelled();

	public void setDownloadProgress(String msg);

	public void setDownloadProgress(int progress);

	public void setDownloadProgress(String msg, int progress);

	public void setDownloadProgress(String msg, int progress, int maxProgress);

	public void setOverallProgress(int progress);

	public void setOverallProgress(String msg, int progress);
}
