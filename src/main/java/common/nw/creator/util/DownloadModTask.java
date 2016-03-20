package common.nw.creator.util;

import common.nw.core.gui.IDownloadProgressListener;
import common.nw.core.modpack.ModInfo;
import common.nw.core.modpack.RepoMod;
import common.nw.core.utils.DownloadHelper;
import common.nw.core.utils.UpdateResult;

import java.io.File;

/**
 * @author Nuklearwurst
 */
public class DownloadModTask extends Thread {


	private final IDownloadProgressListener listener;
	private final DownloadFinishedHandler finishedHandler;
	private final File outputFile;
	private final ModInfo modInfo;

	public DownloadModTask(IDownloadProgressListener listener, DownloadFinishedHandler finishedHandler, File outputFile, ModInfo modInfo) {
		this.listener = listener;
		this.finishedHandler = finishedHandler;
		this.outputFile = outputFile;
		this.modInfo = modInfo;
	}

	@Override
	public void run() {
		UpdateResult result = DownloadHelper.downloadMod(outputFile, modInfo, listener);
		onFinished(result);
	}

	private void onFinished(UpdateResult result) {
		if (listener.isCancelled()) {
			//noinspection ResultOfMethodCallIgnored
			outputFile.delete();
			finishedHandler.onDownloadFinished(outputFile, UpdateResult.Cancelled);
		} else {
			finishedHandler.onDownloadFinished(outputFile, result);
		}
	}

	public static ModInfo createModInfoFromUrl(String url) {
		int index = url.lastIndexOf("/");
		ModInfo info = new ModInfo(index > 0 ? url.substring(index) : url);
		RepoMod repoMod = new RepoMod();
		repoMod.downloadUrl = url;
		info.setRemoteInfo(repoMod);
		return info;
	}

	public interface DownloadFinishedHandler {
		void onDownloadFinished(File file, UpdateResult result);
	}
}