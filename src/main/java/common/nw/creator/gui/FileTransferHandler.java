package common.nw.creator.gui;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

public class FileTransferHandler extends TransferHandler {

	private IDropFileHandler target;

	public FileTransferHandler(IDropFileHandler dropTarget) {
		this.target = dropTarget;
	}

	/**
	 * We only support importing files.
	 */
	@Override
	public boolean canImport(TransferHandler.TransferSupport info) {
		// Check for String flavor
		return info.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
	}

	/**
	 * We support both copy and move actions.
	 */
	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY;
	}

	/**
	 * Perform the actual import. This only supports drop.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean importData(TransferHandler.TransferSupport info) {
		if (!info.isDrop()) {
			return false;
		}

		// Get the string that is being dropped.
		Transferable t = info.getTransferable();
		List<File> data;
		try {
			data = (List<File>) t
					.getTransferData(DataFlavor.javaFileListFlavor);
		} catch (Exception e) {
			return false;
		}

		for (File file : data) {
			if (!target.dropFile(file)) {
				return false;
			}
		}
		return true;
	}
}
