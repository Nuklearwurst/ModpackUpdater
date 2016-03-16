package common.nw.utils;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

/**
 * @author Nuklearwurst
 */
public class SwingUtils {

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
}
