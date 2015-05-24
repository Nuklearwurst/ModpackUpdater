package common.nw.creator.gui.pages.dialog;

import common.nw.modpack.ModInfo;
import common.nw.modpack.RepoMod;
import common.nw.updater.gui.IProgressWatcher;
import common.nw.utils.DownloadHelper;
import common.nw.utils.UpdateResult;
import common.nw.utils.log.NwLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class DialogDownload extends JDialog implements IProgressWatcher {
	private JPanel contentPane;
	private JButton buttonCancel;
	private JProgressBar progressBar;
	private JLabel lblProgress;

	private boolean isCancelled = false;
	private boolean isPaused = false;
	private boolean isRunning = false;

	private File outputFile;
	private DownloadFileHandler handler;
	private ModInfo info;

	public interface DownloadFileHandler {
		public void onDownloadFinished(File file, UpdateResult result);
	}

	private class DownloadThread extends Thread {
		@Override
		public void run() {
			UpdateResult result = DownloadHelper.downloadMod(outputFile, info, DialogDownload.this);
			onFinished(result);
		}
	}

	public DialogDownload(Dialog parent, File output, DownloadFileHandler handler, ModInfo info) {
		super(parent, true);
		this.handler = handler;
		this.outputFile = output;
		this.info = info;

		setBounds(parent.getX() + 40, parent.getY() + 40, 400, 160);

		setContentPane(contentPane);
		getRootPane().setDefaultButton(buttonCancel);

		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});

// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

// call onCancel() on ESCAPE
		contentPane.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		startDownload();
	}

	public static ModInfo createModInfoFromUrl(String url) {
		int index = url.lastIndexOf("/");
		ModInfo info = new ModInfo(index > 0 ? url.substring(index) : url);
		RepoMod repoMod = new RepoMod();
		repoMod.downloadUrl = url;
		info.setRemoteInfo(repoMod);
		return info;
	}

	private void onCancel() {
		isCancelled = true;
		dispose();
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public boolean isPaused() {
		return isPaused;
	}

	@Override
	public void setDownloadProgress(String msg) {
		lblProgress.setText(msg);
	}

	@Override
	public void setDownloadProgress(int progress) {
		progressBar.setValue(progress);
	}

	@Override
	public void setDownloadProgress(String msg, int progress) {
		setDownloadProgress(msg);
		setDownloadProgress(progress);
	}

	@Override
	public void setDownloadProgress(String msg, int progress, int maxProgress) {
		progressBar.setMaximum(maxProgress);
		setDownloadProgress(msg, progress);
	}


	public void onFinished(UpdateResult result) {
		isRunning = false;
		if(isCancelled()) {
			//noinspection ResultOfMethodCallIgnored
			outputFile.delete();
			handler.onDownloadFinished(outputFile, UpdateResult.Cancelled);
		} else {
			handler.onDownloadFinished(outputFile, result);
		}
		dispose();
	}

	private void startDownload() {
		if(isRunning) {
			NwLogger.CREATOR_LOGGER.error("Download Thread is already running!!");
			return;
		}
		isRunning = true;
		new DownloadThread().start();
	}

	//
	//unimplemented methods:
	//

	@Override
	public boolean quitToLauncher() {
		return false;
	}

	@Override
	public int showErrorDialog(String title, String message) {
		return 0;
	}

	@Override
	public int showConfirmDialog(String message, String title, int optionType, int messageType) {
		return 0;
	}

	@Override
	public String showInputDialog(String message) {
		return null;
	}

	@Override
	public int showOptionDialog(String msg, String title, int optionType, int messageType, Icon icon, String[] options, String defaultOption) {
		return 0;
	}

	@Override
	public void setOverallProgress(int progress) {

	}

	@Override
	public void setOverallProgress(String msg, int progress) {

	}
}
