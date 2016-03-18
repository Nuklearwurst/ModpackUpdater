package common.nw.creator.gui.table;

import java.util.Map;

/**
 * @author Nuklearwurst
 */
public interface IDataTable {

	/**
	 * @return the data fields available
	 */
	@SuppressWarnings("unused")
	String[] getDataFields();

	/**
	 * @param row   the row of the entry
	 * @param field the field value that should get retrieved
	 * @return element at the given position in the table
	 */
	Object getElement(int row, String field);

	/**
	 * @param row the row of the entry
	 * @return an {@link IDataTableElement} describing the entry at the given row
	 */
	@SuppressWarnings("unused")
	IDataTableElement getRowEntry(int row);

	/**
	 * @return how many rows of data are available
	 */
	int getRowCount();

	/**
	 * @return whether the given field is editable
	 */
	boolean canEditField(int row, String field);

	/**
	 * sets the element at the given position
	 *
	 * @param row   the row of the entry
	 * @param field the field value that should get set
	 * @param value the value that gets set
	 */
	void setElement(int row, String field, Object value);

	class SimpleDataTableElement implements IDataTableElement {
		private final Map<String, Object> values;

		@SuppressWarnings("unused")
		public SimpleDataTableElement(Map<String, Object> values) {
			this.values = values;
		}

		@Override
		public boolean canEditField(String field) {
			return true;
		}

		@Override
		public Object getValue(String field) {
			return values.get(field);
		}

		@Override
		public void setValue(String field, Object value) {
			values.put(field, value);
		}

		@Override
		public String[] getSupportedFields() {
			return values.keySet().toArray(new String[values.size()]);
		}
	}
}
