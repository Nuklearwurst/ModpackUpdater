package common.nw.updater.gui;

import common.nw.updater.Updater;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class UpdateWindow_legacy implements IProgressWatcher, WindowListener {

	private JFrame frmUpdater;
	private JLabel lblOverallProgress;
	private JLabel lblDownloadprogress;
	private JProgressBar pbOverall;
	private JProgressBar pbDownload;

	private boolean isCancelled = false;
	private boolean isPaused = false;
	private boolean quitToLauncher = false;

	/**
	 * Create the application.
	 */
	private UpdateWindow_legacy() {
		initialize();
	}
	
	public UpdateWindow_legacy(Updater updater) {
		this();
		// this.updater = updater;
		updater.setListener(this);
	}

	@SuppressWarnings("SameParameterValue")
	public void setVisible(boolean b) {
		frmUpdater.setVisible(b);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmUpdater = new JFrame();
		frmUpdater.setResizable(false);
		frmUpdater.setTitle("Updater");
		frmUpdater.setBounds(100, 100, 378, 199);
		frmUpdater.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 300, 0 };
		gridBagLayout.rowHeights = new int[] { 19, 18, 25, 14, 25, 23, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		frmUpdater.getContentPane().setLayout(gridBagLayout);

		lblOverallProgress = new JLabel("Overall Progress");
		GridBagConstraints gbc_lblOverallProgress = new GridBagConstraints();
		gbc_lblOverallProgress.insets = new Insets(0, 0, 5, 0);
		gbc_lblOverallProgress.gridx = 0;
		gbc_lblOverallProgress.gridy = 1;
		frmUpdater.getContentPane().add(lblOverallProgress,
				gbc_lblOverallProgress);

		pbOverall = new JProgressBar();
		pbOverall.setStringPainted(true);
		pbOverall.setPreferredSize(new Dimension(300, 25));
		GridBagConstraints gbc_pbOverall = new GridBagConstraints();
		gbc_pbOverall.insets = new Insets(0, 0, 5, 0);
		gbc_pbOverall.gridx = 0;
		gbc_pbOverall.gridy = 2;
		frmUpdater.getContentPane().add(pbOverall, gbc_pbOverall);

		lblDownloadprogress = new JLabel("DownloadProgress");
		GridBagConstraints gbc_lblDownloadprogress = new GridBagConstraints();
		gbc_lblDownloadprogress.insets = new Insets(0, 0, 5, 0);
		gbc_lblDownloadprogress.gridx = 0;
		gbc_lblDownloadprogress.gridy = 3;
		frmUpdater.getContentPane().add(lblDownloadprogress,
				gbc_lblDownloadprogress);

		pbDownload = new JProgressBar();
		pbDownload.setStringPainted(true);
		pbDownload.setPreferredSize(new Dimension(300, 25));
		GridBagConstraints gbc_pbDownload = new GridBagConstraints();
		gbc_pbDownload.insets = new Insets(0, 0, 5, 0);
		gbc_pbDownload.gridx = 0;
		gbc_pbDownload.gridy = 4;
		frmUpdater.getContentPane().add(pbDownload, gbc_pbDownload);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				cancel();
			}
		});
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.gridx = 0;
		gbc_btnCancel.gridy = 5;
		frmUpdater.getContentPane().add(btnCancel, gbc_btnCancel);
	}

	private void cancel() {
		isPaused = true;
		String[] options = new String[]{"Quit to launcher", "Continue without updating", "Cancel"};
		int r = JOptionPane.showOptionDialog(frmUpdater, "Are you sure you want to cancel the update process?", "Cancel", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if(r == JOptionPane.YES_OPTION) {
			isCancelled = true;
			quitToLauncher = true;
		} else if(r == JOptionPane.NO_OPTION) {
			isCancelled = true;
		}
		isPaused = false;
	}

	public void close() {
		frmUpdater.dispose();
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}
	
	@Override
	public boolean isPaused() {
		return isPaused;
	}
	
	public boolean quitToLauncher() {
		return quitToLauncher;
	}

	@Override
	public void setDownloadProgress(String msg) {
		Updater.logger.info("Secondary Progress: " + msg);
		lblDownloadprogress.setText(msg);
	}

	@Override
	public void setDownloadProgress(int progress) {
		pbDownload.setValue(progress);
	}

	@Override
	public void setDownloadProgress(String msg, int progress) {
		setDownloadProgress(msg);
		setDownloadProgress(progress);
	}

	@Override
	public void setDownloadProgress(String msg, int progress, int maxProgress) {
		pbDownload.setMaximum(maxProgress);
		setDownloadProgress(msg, progress);
	}

	@Override
	public void setOverallProgress(int progress) {
		pbOverall.setValue(progress);
	}

	@Override
	public void setOverallProgress(String msg, int progress) {
		Updater.logger.info("Primary Progress: " + msg + ": " + progress);
		lblOverallProgress.setText(msg);
		setOverallProgress(progress);
	}

	@Override
	public boolean hasGui() {
		return true;
	}

	@Override
	public Component getGui() {
		return frmUpdater;
	}

	public int showErrorDialog(String title, String message) {
		String[] options = { "Retry", "Quit To Launcher",
		"Continue without updating" };
		return JOptionPane.showOptionDialog(frmUpdater, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
	}

	@Override
	public int showConfirmDialog(String message, String title, int optionType,
			int messageType) {
		return JOptionPane.showConfirmDialog(frmUpdater, message, title, optionType, messageType);
	}

	@Override
	public String showInputDialog(String message) {
		return JOptionPane.showInputDialog(frmUpdater, message);
	}

	@Override
	public int showOptionDialog(String msg, String title, int optionType,
			int messageType, Icon icon, String[] options, String defaultOption) {
		return JOptionPane.showOptionDialog(frmUpdater, msg, title, optionType, messageType, icon, options, defaultOption);
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		isCancelled = true;
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
