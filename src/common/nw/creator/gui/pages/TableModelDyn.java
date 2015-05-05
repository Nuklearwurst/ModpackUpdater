package common.nw.creator.gui.pages;

import common.nw.creator.gui.TableModelList;

import javax.swing.table.AbstractTableModel;

public class TableModelDyn extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Object[] columns;
	private Object[][] values;

	/**
	 * unused, use {@link TableModelList}
	 * 
	 * @param initialValues
	 * @param heads
	 */
	@Deprecated
	public TableModelDyn(Object[][] initialValues, Object[] heads) {
		columns = heads;
		values = initialValues;
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
		if (columns == null) {
			return 0;
		}
		return columns.length;
	}

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
		return columns[col].toString();
	}

	@Override
	public Object getValueAt(int row, int column) {
		return values[column][row];
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		values[columnIndex][rowIndex] = aValue;
	}

	public void clear() {
		values = new Object[getColumnCount()][0];
	}

}
