package common.nw.creator.gui.pages.dialog;

public interface ITableHolder {

	void setValue(int index, Object o);
	void addValue(Object o);
	void removeValue(int index);
	Object getValue(int index);
	void updateTable();
}
