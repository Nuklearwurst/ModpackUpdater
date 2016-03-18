package common.nw.creator.gui.table;

import java.util.List;

/**
 * @author Nuklearwurst
 */
public class ListDataTable implements IDataTable {

	private String[] dataFields;
	private List<? extends IDataTableElement> elements;

	public ListDataTable(String[] dataFields, List<? extends IDataTableElement> elements) {
		this.dataFields = dataFields;
		this.elements = elements;
	}

	@Override
	public String[] getDataFields() {
		return dataFields;
	}

	@Override
	public Object getElement(int row, String field) {
		return getRowEntry(row).getValue(field);
	}

	@Override
	public IDataTableElement getRowEntry(int row) {
		return elements.get(row);
	}

	@Override
	public int getRowCount() {
		return elements.size();
	}

	@Override
	public boolean canEditField(int row, String field) {
		return getRowEntry(row).canEditField(field);
	}

	@Override
	public void setElement(int row, String field, Object value) {
		getRowEntry(row).setValue(field, value);
	}
}
