package common.nw.creator.gui;

import java.io.File;

public interface IDropFileHandler {

	/**
	 * performs the file-drop handling
	 * @param file
	 * @return
	 */
	public boolean dropFile(File file);
}
