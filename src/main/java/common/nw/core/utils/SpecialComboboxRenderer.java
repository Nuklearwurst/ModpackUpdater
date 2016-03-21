package common.nw.core.utils;

import javax.swing.*;
import java.awt.*;

/**
 * @author Nuklearwurst
 */
public class SpecialComboboxRenderer extends DefaultListCellRenderer {

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		if (value instanceof IDisplayNameProvider) {
			value = ((IDisplayNameProvider) value).getDisplayName();
		}
		return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	}


}
