package common.nw.creator.gui.pages.dialog;

public interface ITableHolder {

	public void setValue(int index, Object o);
	public void addValue(Object o);
	public void removeValue(int index);
	public Object getValue(int index);
	public void updateTable();
}
