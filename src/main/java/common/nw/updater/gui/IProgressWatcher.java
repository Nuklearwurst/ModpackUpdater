package common.nw.updater.gui;

import javax.swing.Icon;
import java.awt.*;

public interface IProgressWatcher {

	boolean isCancelled();

	/**
	 * used to force the updater to pause
	 * the updater will pause as long this method returns true
	 */
	boolean isPaused();

	boolean quitToLauncher();

	@SuppressWarnings("SameParameterValue")
	int showErrorDialog(String title, String message);

	@SuppressWarnings("SameParameterValue")
	int showConfirmDialog(String message, String title, int optionType, int messageType);

	@SuppressWarnings("SameParameterValue")
	String showInputDialog(String message);

	@SuppressWarnings("SameParameterValue")
	int showOptionDialog(String msg, String title, int optionType, int messageType, Icon icon, String[] options, String defaultOption);

	void setDownloadProgress(String msg);

	void setDownloadProgress(int progress);

	void setDownloadProgress(String msg, int progress);

	void setDownloadProgress(String msg, int progress, int maxProgress);

	void setOverallProgress(int progress);

	void setOverallProgress(String msg, int progress);

	/**
	 * gets called to determine whether the Watcher has a gui that can be used
	 */
	boolean hasGui();

	/**
	 * gets called to get the current gui, return null if watcher has no gui
	 */
	Component getGui();
}
