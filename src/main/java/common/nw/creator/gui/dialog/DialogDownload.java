package common.nw.creator.gui.dialog;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import common.nw.core.modpack.ModInfo;
import common.nw.core.modpack.RepoMod;
import common.nw.core.utils.DownloadHelper;
import common.nw.core.utils.UpdateResult;
import common.nw.core.utils.log.NwLogger;
import common.nw.core.gui.IDownloadProgressListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class DialogDownload extends JDialog implements IDownloadProgressListener {
	private JPanel contentPane;
	private JButton buttonCancel;
	private JProgressBar progressBar;
	private JLabel lblProgress;

	private boolean isCancelled = false;
	@SuppressWarnings({"FieldCanBeLocal", "CanBeFinal"})
	private boolean isPaused = false;
	private boolean isRunning = false;

	private final File outputFile;
	private final DownloadFileHandler handler;
	private final ModInfo info;

	{
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		contentPane = new JPanel();
		contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		buttonCancel = new JButton();
		buttonCancel.setText("Cancel");
		buttonCancel.setMnemonic('C');
		buttonCancel.setDisplayedMnemonicIndex(0);
		panel1.add(buttonCancel);
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		progressBar = new JProgressBar();
		panel2.add(progressBar, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		lblProgress = new JLabel();
		lblProgress.setText("Downloading...");
		panel2.add(lblProgress, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		panel2.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		final Spacer spacer2 = new Spacer();
		panel2.add(spacer2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return contentPane;
	}

	public interface DownloadFileHandler {
		void onDownloadFinished(File file, UpdateResult result);
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


	private void onFinished(UpdateResult result) {
		isRunning = false;
		if (isCancelled()) {
			//noinspection ResultOfMethodCallIgnored
			outputFile.delete();
			handler.onDownloadFinished(outputFile, UpdateResult.Cancelled);
		} else {
			handler.onDownloadFinished(outputFile, result);
		}
		dispose();
	}

	private void startDownload() {
		if (isRunning) {
			NwLogger.CREATOR_LOGGER.error("Download Thread is already running!!");
			return;
		}
		isRunning = true;
		new DownloadThread().start();
	}
}
