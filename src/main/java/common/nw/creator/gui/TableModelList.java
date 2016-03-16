package common.nw.creator.gui;

import common.nw.utils.log.NwLogger;

import javax.swing.table.AbstractTableModel;
import java.util.Arrays;
import java.util.List;

public class TableModelList extends AbstractTableModel {

	private Object[] heads;
	private String[] fields;
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
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
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

	/**
	 * use updateData and setValues
	 * this will reset when calling updateData
	 */
	@Deprecated
	public void addRow(Object[] o) {
		if (values == null) {
			values = new Object[getColumnCount()][0];
		}
		Object[][] old = values.clone();
		values = new Object[old.length][old[0].length + 1];
		System.out.println("x: " + old.length + "y: " + old[0].length);
		for (int x = 0; x < old.length; x++) {
			System.arraycopy(old[x], 0, values[x], 0, old[0].length);
		}
		for (int i = 0; i < o.length; i++) {
			values[i][old[0].length] = o[i];
		}
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

	/**
	 * use setValues() and updateData()
	 * this will only clear until next update
	 */
	@Deprecated
	public void clear() {
		values = new Object[getColumnCount()][0];
	}

	public void setValues(List<?> list) {
		this.list = list;
		updateData();
	}

}
