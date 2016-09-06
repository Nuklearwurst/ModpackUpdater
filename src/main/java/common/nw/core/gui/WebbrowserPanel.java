package common.nw.core.gui;

import common.nw.core.utils.log.NwLogger;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static javafx.concurrent.Worker.State.FAILED;

/**
 * JPanel that provides webbrowsing capabilities using javafx
 */
public class WebbrowserPanel extends JPanel {

	public static String[] downloadableExtensions = {
			".jar", ".zip", ".json", ".txt", ".properties", ".prop", ".gif", ".png", ".litemod", ".cfg", ".config", ".conf"
	};

	private boolean editableAddress = true;
	private boolean hideAdress = false;
	private boolean hideTitle = false;

	private JPanel contentPanel;

	private JLabel lblTitle;
	private JTextField txtUrl;
	private JButton btnUrl;
	private JProgressBar progressBar;

	private JFXPanel webbrowserPanel;

	private WebEngine webEngine;

	private DownloadHandler downloadHandler;
	private ErrorHandler errorHandler;

	public WebbrowserPanel() {
		super();
		init();
	}


	private void init() {
		//Load swing components
		this.setLayout(new BorderLayout());
		this.add(contentPanel, BorderLayout.CENTER);

		//init adress
		btnUrl.addActionListener(e -> open(txtUrl.getText()));

		updateComponentVisibility();

		//init jfx
		Platform.runLater(() -> {
			//init webengine
			final WebView webView = new WebView();
			webEngine = webView.getEngine();

			//title listener
			webEngine.titleProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue != null) lblTitle.setText(newValue);
			});

			//pb status
			webEngine.getLoadWorker().workDoneProperty().addListener((observable, oldValue, newValue) -> {
				progressBar.setValue(newValue.intValue());
				progressBar.setVisible(newValue.intValue() != 100);
			});

			webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {

				String location = webEngine.getLocation();
				switch (webEngine.getLoadWorker().getState()) {
					case SCHEDULED:
					case RUNNING:
						lblTitle.setText("Loading");
						break;
					case CANCELLED:
					case FAILED:
						if (downloadHandler != null && downloadHandler.acceptsUrl(location)) {
							downloadHandler.download(location);
							loadDownloadInfo();
						} else {
							handleError();
						}
						break;
					case SUCCEEDED:
						if (location != null && !location.isEmpty()) {
							txtUrl.setText(location);
						}
						break;
					case READY:
						if (location != null && !location.isEmpty()) {
							txtUrl.setText(location);
						}
						break;
				}
			});

			webEngine.getLoadWorker().exceptionProperty().addListener((observable, oldValue, newValue) -> {
				if (webEngine.getLoadWorker().getState() == FAILED) {
					handleError();
				}
			});

			webEngine.setOnError(event -> handleError());

			webbrowserPanel.setScene(new Scene(webView));
		});
	}

	private void handleError() {
		if (errorHandler != null) {
			errorHandler.handleError(this);
		} else {
			webEngine.loadContent("<html><body><div style=\"text-align: center;\"><b>Error when loading website!</b></div></body></html>");
			lblTitle.setText("Error loading page!");
		}
	}

	private void loadDownloadInfo() {
		webEngine.loadContent("<html><body><div style=\"text-align: center;\"><b>Downloading...</b></div></body></html>");
		lblTitle.setText("Downloading");
	}

	public void open(final String urlString) {
		Platform.runLater(() -> {
			try {
				URL url = new URL(urlString);
				webEngine.load(url.toExternalForm());
			} catch (Exception e) {
				NwLogger.NW_LOGGER.fine("Error parsing url: " + urlString);
				NwLogger.NW_LOGGER.fine("Retrying...");

				try {
					URL url = new URL("http://" + urlString);
					webEngine.load(url.toExternalForm());
				} catch (Exception ex) {
					NwLogger.NW_LOGGER.fine("Error parsing url: http://" + urlString);
				}
			}
		});
	}

	public void open(final URL url) {
		Platform.runLater(() -> webEngine.load(url.toExternalForm()));
	}

	public void loadContent(String content) {
		Platform.runLater(() -> webEngine.loadContent(content));
	}

	public void setHideAdress(boolean hideAdress) {
		this.hideAdress = hideAdress;
		updateComponentVisibility();

	}

	public void setEditableAddress(boolean editableAddress) {
		this.editableAddress = editableAddress;
		updateComponentVisibility();
	}

	public void setHideTitle(boolean hideTitle) {
		this.hideTitle = hideTitle;
		updateComponentVisibility();
	}

	private void updateComponentVisibility() {
		txtUrl.setEditable(editableAddress);
		txtUrl.setVisible(!hideAdress);
		btnUrl.setVisible(!hideAdress && editableAddress);
		lblTitle.setVisible(!hideTitle);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame();
			frame.setPreferredSize(new Dimension(400, 300));
			final WebbrowserPanel webbrowserPanel = new WebbrowserPanel();
			webbrowserPanel.setDownloadHandler(url -> {
				JOptionPane.showMessageDialog(frame, "Download detected: " + url);
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("Select folder");
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					try {
						URL website = null;
						website = new URL(url);
						File file = new File(fileChooser.getSelectedFile(), url.substring(url.lastIndexOf("/") + 1));
						try (InputStream in = website.openStream()) {
							Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							e.printStackTrace();
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
			});
			frame.add(webbrowserPanel);
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.pack();
			frame.setVisible(true);
		});
	}

	public void setDownloadHandler(DownloadHandler downloadHandler) {
		this.downloadHandler = downloadHandler;
	}

	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

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
		contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout(0, 0));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new BorderLayout(0, 0));
		contentPanel.add(panel1, BorderLayout.NORTH);
		lblTitle = new JLabel();
		lblTitle.setHorizontalAlignment(0);
		lblTitle.setMaximumSize(new Dimension(52, 20));
		lblTitle.setMinimumSize(new Dimension(52, 20));
		lblTitle.setPreferredSize(new Dimension(52, 20));
		lblTitle.setText("Loading...");
		panel1.add(lblTitle, BorderLayout.NORTH);
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new BorderLayout(0, 0));
		panel1.add(panel2, BorderLayout.CENTER);
		panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), null));
		btnUrl = new JButton();
		btnUrl.setText("Go");
		panel2.add(btnUrl, BorderLayout.EAST);
		txtUrl = new JTextField();
		panel2.add(txtUrl, BorderLayout.CENTER);
		progressBar = new JProgressBar();
		progressBar.setStringPainted(false);
		panel1.add(progressBar, BorderLayout.SOUTH);
		webbrowserPanel = new JFXPanel();
		contentPanel.add(webbrowserPanel, BorderLayout.CENTER);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return contentPanel;
	}

	public interface DownloadHandler {

		/**
		 * @return whether the url should be downloadable
		 */
		default boolean acceptsUrl(String url) {
			for (String ext : downloadableExtensions) {
				if (url.endsWith(ext)) {
					return true;
				}
			}
			return false;
		}

		void download(String url);
	}

	public interface ErrorHandler {
		void handleError(WebbrowserPanel panel);
	}
}
