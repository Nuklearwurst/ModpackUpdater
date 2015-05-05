package common.nw.creator.gui.pages;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import common.nw.creator.Creator;
import common.nw.creator.IProgressListener;
import common.nw.creator.gui.Reference;
import common.nw.gui.IPageHandler;
import common.nw.gui.PageHolder;

public class PanelLoading extends JPanel implements IPageHandler, IProgressListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JProgressBar progressBar;
	private JLabel label;
	
	private Creator creator;

	/**
	 * Create the panel.
	 */
	public PanelLoading(Creator creator) {
		this.creator = creator;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 119, 29, -18, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0,
				Double.MIN_VALUE };
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

	@Override
	public void setProgress(String s) {
		label.setText(s);
	}

	@Override
	public void setProgress(int i) {
		progressBar.setValue(i);
	}

	@Override
	public Object getProperty(String s) {
		if(s.equals(Reference.KEY_NAME)) {
			return "Loading";
		}
		if(s.equals(Reference.KEY_TURNABLE)) {
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
