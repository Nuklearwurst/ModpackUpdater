package common.nw.modpack.gui;

import common.nw.creator.gui.CreatorEvent;

public interface IPageHolder {

	public void next();

	public void back();

	public void sendEvent(CreatorEvent event);
}
