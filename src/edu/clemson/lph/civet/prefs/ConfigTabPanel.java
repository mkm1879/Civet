package edu.clemson.lph.civet.prefs;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.ScrollPaneConstants;

@SuppressWarnings("serial")
public class ConfigTabPanel extends JPanel {
	private JPanel pRows;
	private int iYPosition = 5;
	private JScrollPane spPrefs;

	/**
	 * Create the panel.
	 */
	public ConfigTabPanel() {
		setLayout(new BorderLayout(0, 0));
		spPrefs = new JScrollPane();
		spPrefs.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(spPrefs);
		pRows = new JPanel();
		pRows.setLayout( null );
		spPrefs.setViewportView(pRows);
	}
	
	public void addEntry( ConfigEntryPanel panel ) {
		panel.setBounds( 2, iYPosition, 600, 25 );
		iYPosition += 27;
		pRows.add(panel);
		// Needed to let scrollpane know if slider is required.
		pRows.setPreferredSize(new Dimension(iYPosition + 10, 600));
	}

}
