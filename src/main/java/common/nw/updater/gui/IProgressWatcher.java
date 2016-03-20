package common.nw.updater.gui;

import common.nw.core.gui.IDownloadProgressListener;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public interface IProgressWatcher extends IDownloadProgressListener {

	boolean quitToLauncher();

	@SuppressWarnings("SameParameterValue")
	int showErrorDialog(String title, String message);

	@SuppressWarnings("SameParameterValue")
	int showConfirmDialog(String message, String title, int optionType, int messageType);

	@SuppressWarnings("SameParameterValue")
	String showInputDialog(String message);

	@SuppressWarnings("SameParameterValue")
	int showOptionDialog(String msg, String title, int optionType, int messageType, Icon icon, String[] options, String defaultOption);

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

	@SuppressWarnings("SameParameterValue")
	File selectFile(String directory, int mode, String title);
}
