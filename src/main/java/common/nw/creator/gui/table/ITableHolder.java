package common.nw.creator.gui.table;

public interface ITableHolder<T> {

	void setValue(int index, T o);

	void addValue(T o);

	void removeValue(int index);

	T getValue(int index);

	void updateTable();
}
