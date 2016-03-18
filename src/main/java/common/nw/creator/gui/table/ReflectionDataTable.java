package common.nw.creator.gui.table;


import common.nw.core.utils.log.NwLogger;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Nuklearwurst
 */
public class ReflectionDataTable<T> implements IDataTable {

	private Map<String, Field> fieldMap;
	private List<T> elements;

	public ReflectionDataTable(String[] dataFields, List<T> objects, Class<T> clazz) {
		this.elements = objects;
		fieldMap = new HashMap<>(dataFields.length);
		for (String dataField : dataFields) {
			try {
				fieldMap.put(dataField, clazz.getField(dataField));
			} catch (IllegalArgumentException | SecurityException | NoSuchFieldException e) {
				NwLogger.NW_LOGGER.error("Error reading data...", e);
			}
		}

	}

	@Override
	public String[] getDataFields() {
		return fieldMap.keySet().toArray(new String[fieldMap.size()]);
	}

	@Override
	public Object getElement(int row, String field) {
		try {
			return fieldMap.get(field).get(elements.get(row));
		} catch (IllegalAccessException e) {
			NwLogger.NW_LOGGER.error("Error reading data...", e);
		}
		return null;
	}

	@Override
	public IDataTableElement getRowEntry(int row) {
		return new ReflectionDataEntry(elements.get(row));
	}

	@Override
	public int getRowCount() {
		return elements.size();
	}

	@Override
	public boolean canEditField(int row, String field) {
		return fieldMap.get(field).isAccessible();
	}

	@Override
	public void setElement(int row, String field, Object value) {
		try {
			fieldMap.get(field).set(elements.get(row), value);
		} catch (IllegalAccessException e) {
			NwLogger.NW_LOGGER.error("Error setting value...", e);
		}
	}

	public class ReflectionDataEntry implements IDataTableElement {

		private T object;

		public ReflectionDataEntry(T object) {
			this.object = object;
		}

		@Override
		public String[] getSupportedFields() {
			return getDataFields();
		}

		@Override
		public T getValue(String field) {
			try {
				fieldMap.get(field).get(object);
			} catch (IllegalAccessException e) {
				NwLogger.NW_LOGGER.error("Error getting value...", e);
			}
			return null;
		}

		@Override
		public void setValue(String field, Object value) {
			try {
				fieldMap.get(field).set(object, value);
			} catch (IllegalAccessException e) {
				NwLogger.NW_LOGGER.error("Error setting value...", e);
			}
		}

		@Override
		public boolean canEditField(String field) {
			return fieldMap.get(field).isAccessible();
		}
	}
}
