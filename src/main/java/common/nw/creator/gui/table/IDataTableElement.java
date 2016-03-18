package common.nw.creator.gui.table;

/**
 * One row in a table
 *
 * @author Nuklearwurst
 */
public interface IDataTableElement {

	String[] getSupportedFields();

	Object getValue(final String field);

	void setValue(final String field, Object value);

	boolean canEditField(final String field);
}
