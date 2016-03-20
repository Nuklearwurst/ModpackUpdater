package common.nw.creator.gui_legacy.pages;

import common.nw.core.gui.PageHolder;
import common.nw.creator.Creator;
import common.nw.creator.util.Reference;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class PanelLoading extends JPanel implements PageHolder.IPageHandler {

	private final JProgressBar progressBar;
	@SuppressWarnings("FieldCanBeLocal")
	private final JLabel label;

	private final Creator creator;

	/**
	 * Create the panel.
	 */
	public PanelLoading(Creator creator) {
		this.creator = creator;

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{119, 29, -18, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0,
				Double.MIN_VALUE};
		setLayout(gridBagLayout);

		label = new JLabel("Creating");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 0);
		gbc_label.gridx = 0;
		gbc_label.gridy = 1;
		add(label, gbc_label);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(new Dimension(300, 20));
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.insets = new Insets(0, 0, 5, 0);
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 2;
		add(progressBar, gbc_progressBar);

	}

	private void setProgress(int i) {
		progressBar.setValue(i);
	}

	@Override
	public Object getProperty(String s) {
		if (s.equals(Reference.KEY_NAME)) {
			return "Loading";
		}
		if (s.equals(Reference.KEY_TURNABLE)) {
			return !(progressBar.getValue() < 100) || progressBar.getValue() <= 0;
		}
		return null;
	}

	@Override
	public void onPageOpened(PageHolder holder, boolean forward) {
		if (forward) {
			this.setProgress(1);
			if (creator.shouldReadFiles) {
				if (new File(creator.fileLoc).exists() && creator.readFiles()) {
					holder.nextPage();
				} else {
					JOptionPane.showMessageDialog(this,
							"Error when reading Files!", "Error",
							JOptionPane.ERROR_MESSAGE);
					this.setProgress(0);
					holder.updatePage();
				}
			} else {
				holder.nextPage(); // anyway
			}
		} else {
			holder.previousPage();
		}
	}

	@Override
	public boolean onPageClosed(PageHolder holder, boolean forward) {
		return true;
	}
}
