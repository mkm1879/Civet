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
//	private JTable tblPrefs;
private JPanel pRows;

	/**
	 * Create the panel.
	 */
	public ConfigTabPanel() {
		setLayout(new BorderLayout(0, 0));
		JScrollPane spPrefs = new JScrollPane();
		spPrefs.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(spPrefs);
		pRows = new JPanel();
		GridLayout gRows = new GridLayout(20,3,2,2);
		pRows.setLayout(gRows);
		spPrefs.setViewportView(pRows);
	}
	
	public void addEntry( ConfigEntryPanel panel ) {
		pRows.add(panel);
	}

}
