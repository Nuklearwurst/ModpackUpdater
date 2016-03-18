package common.nw.core.utils;

import common.nw.core.utils.log.NwLogger;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.File;

/**
 * @author Nuklearwurst
 */
public class SwingUtils {

	/**
	 * creates a default popupt menu for editable textfields
	 * <br>
	 * added actions are: <i>Copy, Cut, Paste </i>and<i> Select All</i>
	 *
	 * @param textComponent textcomponent used to create the default actions
	 * @return created {@link JPopupMenu}
	 */
	public static JPopupMenu createTextPopupMenu(JTextComponent textComponent) {
		JPopupMenu popup = new JPopupMenu("Edit");

		JMenuItem copyAction = new JMenuItem(textComponent.getActionMap().get(DefaultEditorKit.copyAction));
		copyAction.setText("Copy");
		JMenuItem cutAction = new JMenuItem(textComponent.getActionMap().get(DefaultEditorKit.cutAction));
		cutAction.setText("Cut");
		JMenuItem pasteAction = new JMenuItem(textComponent.getActionMap().get(DefaultEditorKit.pasteAction));
		pasteAction.setText("Paste");
		JMenuItem selectAllAction = new JMenuItem(textComponent.getActionMap().get(DefaultEditorKit.selectAllAction));
		selectAllAction.setText("Select All");

		popup.add(cutAction);
		popup.add(copyAction);
		popup.add(pasteAction);
		popup.addSeparator();
		popup.add(selectAllAction);

		return popup;
	}

	/**
	 * sets LookAndFeel
	 */
	public static void setOSLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable t) {
			NwLogger.NW_LOGGER.error("Error when setting Look and Feel!", t);
		}
	}

	/**
	 * opens a FileChooser to select a file
	 *
	 * @param c parent
	 * @return null if nothing is selected, otherwise the absolutePath
	 */
	@SuppressWarnings("SameParameterValue")
	public static String openFile(Component c, File currrentDirectory) {
		File file = openJFileChooser(c, currrentDirectory,
				JFileChooser.FILES_ONLY, null);
		if (file != null) {
			return file.getAbsolutePath();
		}
		return null;
	}

	/**
	 * opens a FileChooser to select a folder
	 *
	 * @param c parent
	 * @return null if nothing is selected, otherwise the absolutePath
	 */
	public static String openFolder(Component c, File currrentDirectory) {
		File file = openJFileChooser(c, currrentDirectory,
				JFileChooser.DIRECTORIES_ONLY, null);
		if (file != null) {
			return file.getAbsolutePath();
		}
		return null;
	}

	/**
	 * opens a FileChooser to select a file or folder if a folder is selected a
	 * default filename is appended
	 *
	 * @param c        parent
	 * @param fileName default file name (in case a folder is selected)
	 * @return null if nothing got selected, otherwise the absolute path
	 */
	@SuppressWarnings("SameParameterValue")
	public static String openFileOrDirectoryWithDefaultFileName(Component c,
	                                                            File currentDirectory, String fileName) {
		File file = openJFileChooser(c, currentDirectory,
				JFileChooser.FILES_AND_DIRECTORIES, null);
		if (file == null) {
			return null;
		}
		String path = file.getAbsolutePath();
		if (file.isDirectory()) {
			if (path.endsWith(File.separator)) {
				path = path + fileName;
			} else {
				path = path + File.separator + fileName;
			}
		}
		return path;
	}

	/**
	 * opens a file using a JFileChooser
	 *
	 * @param c          parent
	 * @param mode       selection Mode {@link JFileChooser}
	 * @param buttonText buttonText, uses "Open" when null ({@link JFileChooser})
	 * @return File
	 */
	@SuppressWarnings({"SameParameterValue", "WeakerAccess"})
	public static File openJFileChooser(Component c, File currentDirectory,
	                                    int mode, String buttonText) {
		JFileChooser fc = new JFileChooser(currentDirectory);
		fc.setFileSelectionMode(mode);
		int result;
		if (buttonText != null) {
			result = fc.showDialog(c, buttonText);
		} else {
			result = fc.showOpenDialog(c);
		}
		if (result == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		}
		return null;
	}
}
