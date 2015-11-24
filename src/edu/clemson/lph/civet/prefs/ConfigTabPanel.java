package edu.clemson.lph.civet.prefs;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.ScrollPaneConstants;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

@SuppressWarnings("serial")
public class ConfigTabPanel extends JPanel {
	private JPanel pRows;
	private int iYPosition = 5;

	/**
	 * Create the panel.
	 */
	public ConfigTabPanel() {
		setLayout(new BorderLayout(0, 0));
		JScrollPane spPrefs = new JScrollPane();
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
	}

}
