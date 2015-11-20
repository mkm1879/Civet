package edu.clemson.lph.civet.prefs;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;

import javax.swing.ScrollPaneConstants;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

@SuppressWarnings("serial")
public class ConfigTabPanel extends JPanel {
	private JTable tblPrefs;
	private ConfigTableModel model = new ConfigTableModel();

	/**
	 * Create the panel.
	 */
	public ConfigTabPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane spPrefs = new JScrollPane();
		spPrefs.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(spPrefs);
		
		tblPrefs = new JTable();
		spPrefs.setViewportView(tblPrefs);
		tblPrefs.setModel( model );
	}
	
	public JTable getTable() {
		return tblPrefs;
	}
	public ConfigTableModel getModel() {
		return model;
	}

}
