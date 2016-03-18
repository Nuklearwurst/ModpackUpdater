package common.nw.creator.gui.transfer;

import java.io.File;

public interface IDropFileHandler {

	/**
	 * performs the file-drop handling
	 */
	boolean dropFile(File file);
}
