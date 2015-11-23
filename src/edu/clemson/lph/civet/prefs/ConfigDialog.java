package edu.clemson.lph.civet.prefs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JTabbedPane;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

@SuppressWarnings("serial")
public class ConfigDialog extends JDialog {

	private JTabbedPane tabbedPane;
	private HashMap<String, Object> hControls;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ConfigDialog dialog = new ConfigDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ConfigDialog() {
		hControls = new HashMap<String, Object>();
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		{
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			getContentPane().add(tabbedPane, BorderLayout.CENTER);
			addTabs();
		}
	}
	
	private void addTabs() {
		ConfigCsv me = new ConfigCsv();
		String sTab = null;
		while( (sTab = me.nextTab()) != null ) {
			int iRows = me.getCurrentTabSize();
			ConfigTabPanel pThis = addTab(sTab, iRows);
			for( int i = 0; i < iRows; i++ ) {
				if(	me.nextRow() != null ) {
					addRow(me, pThis, i);
				}
			}
//			TableColumnModel colModel = pThis.getTable().getColumnModel();
//			TableColumn col = colModel.getColumn(1);
//			ConfigRenderer editor = new ConfigRenderer(model, false);
//			col.setCellRenderer(editor);
//			col.setCellEditor(editor);
//			TableColumn col2 = colModel.getColumn(2);
//			ConfigRenderer editor2 = new ConfigRenderer(model, true);
//			col2.setCellRenderer(editor2);
//			col2.setCellEditor(editor);
		} 

	}
	
	private ConfigTabPanel addTab( String sTabName, int iRows ) {
		ConfigTabPanel pThis = new ConfigTabPanel();
		tabbedPane.addTab(sTabName, null, pThis, null);
		hControls.put(sTabName, pThis);
		return pThis;
	}
	
	private void addRow( ConfigCsv me, ConfigTabPanel pThis, int iRow ) {
		final String sName = me.getName();
		final String sType = me.getType();
		final String sDesc = me.getDescription();
		final String sHelp = me.getHelpText();
		final String sValue = me.getDefault();
		final List<String> lChoices = me.getChoices();
		ConfigEntry entry = new ConfigEntry( sName, sValue, sType, sHelp );
		ConfigEntryPanel pEntry = new ConfigEntryPanel( entry );
		pThis.addEntry(pEntry);
		
//		model = pThis.getModel();
//		model.addRow(sName, sValue, sDesc, sType, sHelp);
//		if( "Select".equalsIgnoreCase(sType) ) {
//			model.setChoices(sName, me.getChoices() );
//		}
	}

}
