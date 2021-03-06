package common.nw.creator.gui.table;


import common.nw.core.utils.log.NwLogger;

import javax.swing.table.AbstractTableModel;
import java.util.Arrays;
import java.util.List;

public class TableModelList extends AbstractTableModel {

	private final Object[] heads;
	private final String[] fields;
	private List<?> list;

	private Object[][] values;

	public TableModelList(Object[] heads, String[] fieldNames, List<?> list) {
		//display warnings
		if (fieldNames == null && heads.length > 1) {
			NwLogger.CREATOR_LOGGER.error("Warning, array length of 'heads' is too long!");
		} else if (fieldNames != null && heads.length != fieldNames.length) {
			NwLogger.CREATOR_LOGGER.error("Warning, array length of 'heads' is not the same as 'fieldNames'!");
		}
		this.heads = heads;
		this.fields = fieldNames;
		this.list = list;
	}


	public void updateData() {
		//error checking
		if (heads == null || list == null) {
			NwLogger.CREATOR_LOGGER.error("Warning, 'list' = " + list + " 'heads' = "
					+ Arrays.toString(heads) + " !");
			return;
		}
		// init with new size
		values = new Object[heads.length][list.size()];

		//cycle through all elements
		for (int y = 0; y < list.size(); y++) {
			//get element
			Object o = list.get(y);
			if (o == null) { //null check
				return;
			}
			//hanlde datatypes
			if (fields == null) {
				//datatype -->String, etc
				values[0][y] = o;
			} else {
				//handle objects containing more information
				for (int x = 0; x < heads.length; x++) {
					try {
						//object
						values[x][y] = o.getClass().getField(fields[x]).get(o);
					} catch (IllegalArgumentException | SecurityException | NoSuchFieldException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
		this.fireTableDataChanged();
	}

	@Override
	public int getRowCount() {
		if (values == null) {
			return 0;
		}
		return values[0].length;
	}

	@Override
	public int getColumnCount() {
		if (fields == null) {
			//handle datatypes
			return 1;
		}
		//handle objects
		return fields.length;
	}

	@Override
	public String getColumnName(int col) {
		//read head data
		return heads[col].toString();
	}

	@Override
	public Object getValueAt(int row, int column) {
		return values[column][row];
	}

	/**
	 * use updateData
	 * this will only set until next update
	 */
	@Override
	@Deprecated
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		values[columnIndex][rowIndex] = aValue;
	}

	public void setValues(List<?> list) {
		this.list = list;
		updateData();
	}
}
