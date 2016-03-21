package common.nw.creator.gui.table;


import common.nw.core.utils.IDisplayNameProvider;
import common.nw.core.utils.log.NwLogger;

import javax.swing.table.AbstractTableModel;

/**
 * Table model that uses data from  {@link IDataTable}
 *
 * @author Nuklearwurst
 */
public class TableModelDataList extends AbstractTableModel {

	private final IDataTable data;

	private final String[] tableHeader;
	private final String[] fieldNames;

	private boolean editable;

	public TableModelDataList(IDataTable data, String[] tableHeader, String[] fieldNames, boolean editable) {
		//display warnings
		if (fieldNames == null && tableHeader.length > 1) {
			NwLogger.CREATOR_LOGGER.error("Warning, array length of 'heads' is too long!");
		} else if (fieldNames != null && tableHeader.length != fieldNames.length) {
			NwLogger.CREATOR_LOGGER.error("Warning, array length of 'heads' is not the same as 'fieldNames'!");
		}
		this.data = data;
		this.tableHeader = tableHeader;
		this.fieldNames = fieldNames;
		this.editable = editable;
	}

	@SuppressWarnings("unused")
	public TableModelDataList(IDataTable data, String[] tableHeader, String[] fieldNames) {
		this(data, tableHeader, fieldNames, false);
	}

	@Override
	public int getRowCount() {
		return data.getRowCount();
	}

	@Override
	public int getColumnCount() {
		return fieldNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex >= getRowCount() || columnIndex >= getColumnCount()) {
			return null;
		}
		Object o = data.getElement(rowIndex, fieldNames[columnIndex]);
		if (o instanceof IDisplayNameProvider) {
			return ((IDisplayNameProvider) o).getDisplayName();
		}
		return o;
	}

	@SuppressWarnings("unused")
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	@SuppressWarnings("unused")
	public boolean isEditable() {
		return editable;
	}

	@SuppressWarnings("SimplifiableIfStatement")
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (rowIndex >= getRowCount() || columnIndex >= getColumnCount()) {
			return false;
		}
		return editable && data.canEditField(rowIndex, fieldNames[columnIndex]);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (rowIndex >= getRowCount() || columnIndex >= getColumnCount() || !isCellEditable(rowIndex, columnIndex)) {
			return;
		}
		data.setElement(rowIndex, fieldNames[columnIndex], aValue);
	}

	@Override
	public String getColumnName(int column) {
		if (column > tableHeader.length) {
			return "#" + column;
		}
		return tableHeader[column];
	}
}