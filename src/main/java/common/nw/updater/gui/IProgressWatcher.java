package common.nw.updater.gui;

import common.nw.core.gui.IDownloadProgressListener;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * interface for interaction between user and updater
 */
public interface IProgressWatcher extends IDownloadProgressListener {

	/**
	 * @return whether the updater should quit to the mc-launcher
	 */
	boolean quitToLauncher();

	/**
	 * shows an error dialog with three options
	 *
	 * @param message message to display
	 * @param title   title to display
	 * @return which option was selected
	 * @see JOptionPane#YES_NO_CANCEL_OPTION
	 */
	@SuppressWarnings("SameParameterValue")
	int showErrorDialog(String title, String message);

	/**
	 * Shows a simple confirm dialog
	 *
	 * @param message     message to display
	 * @param title       title to display
	 * @param optionType  optionType, see {@link JOptionPane}
	 * @param messageType messageType, see {@link JOptionPane}
	 * @return which option was selected
	 */
	@SuppressWarnings("SameParameterValue")
	int showConfirmDialog(String message, String title, int optionType, int messageType);

	/**
	 * Shows a simple message dialog
	 *
	 * @param message     message to display
	 * @param title       title to display
	 * @param messageType messageType, see {@link JOptionPane}
	 */
	void showMessageDialog(String message, String title, int messageType);

	/**
	 * Requests input from the user
	 *
	 * @param message message to display
	 * @return String input of the user
	 */
	@SuppressWarnings("SameParameterValue")
	String showInputDialog(String message);

	/**
	 * shows an option dialog
	 *
	 * @param msg           message to display
	 * @param title         title to display
	 * @param optionType    option type , see {@link JOptionPane}
	 * @param messageType   message type , see {@link JOptionPane}
	 * @param icon          icon for ui
	 * @param options       available options
	 * @param defaultOption default option that should be selected
	 * @return the selected option
	 * @see JOptionPane#showOptionDialog(Component, Object, String, int, int, Icon, Object[], Object)
	 */
	@SuppressWarnings("SameParameterValue")
	int showOptionDialog(String msg, String title, int optionType, int messageType, Icon icon, String[] options, String defaultOption);

	/**
	 * sets overall progress
	 */
	void setOverallProgress(int progress);

	/**
	 * sets overall progress and progress string
	 */
	void setOverallProgress(String msg, int progress);

	/**
	 * gets called to determine whether the Watcher has a gui that can be used
	 */
	boolean hasGui();

	/**
	 * gets called to get the current gui, return null if watcher has no gui
	 */
	Component getGui();

	/**
	 * select a file
	 * @param directory starting directory
	 * @param mode file selection mod
	 * @param title title of the dialog
	 * @return selected file
	 * @see JFileChooser
	 */
	@SuppressWarnings("SameParameterValue")
	File selectFile(String directory, int mode, String title);

	/**
	 * gets called once when the ui should be displayed
	 */
	void show();

	/**
	 * gets called when ui should get closed
	 */
	void close();
}
