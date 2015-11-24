package edu.clemson.lph.civet.prefs;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class ConfigEntryPanel extends JPanel {
	private ConfigEntry entry = null;
	private JTextField jtfValue;
	private List<String> aChoices = null;
	private JComboBox<String> cbSelectValue;
	private JCheckBox chkBoolValue;

	/**
	 * Create the panel.
	 */
	public ConfigEntryPanel(ConfigEntry entry, List<String> aChoices) {
		this.entry = entry;
		setLayout(null);
		this.aChoices = aChoices;
		
		JLabel lblName = new JLabel(entry.sName);
		lblName.setHorizontalAlignment(SwingConstants.RIGHT);
		lblName.setBounds(5, 9, 160, 14);
		lblName.setToolTipText(entry.sHelp);
		add(lblName);
		
		jtfValue = new JTextField();
		jtfValue.setText(entry.sValue);
		jtfValue.setBounds(175, 4, 300, 20);
		add(jtfValue);
		jtfValue.setColumns(50);
		jtfValue.setEditable(false);
		jtfValue.setToolTipText(entry.sHelp);
		
		// Dynamic based on type
		if( "Text".equalsIgnoreCase(entry.sType) ) {
			jtfValue.setEditable(true);
		}
		else if( "Num".equalsIgnoreCase(entry.sType) ) {
			jtfValue.setEditable(true);
		}
		else if( "File".equalsIgnoreCase(entry.sType) ) {
			JButton btnBrowse = new JButton("Browse");
			btnBrowse.setBounds(485, 4, 91, 21);
			btnBrowse.setToolTipText(entry.sHelp);
			add(btnBrowse);
			btnBrowse.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectFile();
				}
			});
		}
		else if( "Dir".equalsIgnoreCase(entry.sType) ) {
			JButton btnBrowse = new JButton("Browse");
			btnBrowse.setBounds(485, 4, 91, 21);
			btnBrowse.setToolTipText(entry.sHelp);
			add(btnBrowse);
			btnBrowse.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectDir();
				}
			});
		}
		else if( "Bool".equalsIgnoreCase(entry.sType) ) {
			chkBoolValue = new JCheckBox("Check True");
			chkBoolValue.setBounds(485, 4, 91, 23);
			chkBoolValue.setToolTipText(entry.sHelp);
			if( "YES".equalsIgnoreCase(entry.sValue) || "TRUE".equalsIgnoreCase(entry.sValue))
				chkBoolValue.setSelected(true);
			else 
				chkBoolValue.setSelected(false);
			add(chkBoolValue);
			chkBoolValue.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					toggleBool();
				}
			});
		}
		else if( "Select".equalsIgnoreCase(entry.sType) ) {
			cbSelectValue = new JComboBox<String>();
			cbSelectValue.setToolTipText(entry.sHelp);
			if( aChoices == null || aChoices.size() == 0 )
				System.err.println(entry.sName + " needs choices");
			for( String sChoice : aChoices ) {
				cbSelectValue.addItem(sChoice);
			}
			cbSelectValue.setBounds(485, 4, 91, 21);
			add(cbSelectValue);
			cbSelectValue.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					saveChoice();
				}
			});
		}
	} // End Constructor for ConfigEntryPanel
	
	public boolean hasChanged() {
		String sNewValue = jtfValue.getText();
		String sOldValue = entry.sValue;
		if( sNewValue == null && sOldValue == null )
			return true;
		if( sNewValue == null && sOldValue != null )
			return false;
		if( sNewValue != null && sOldValue == null )
			return false;
		if( sNewValue.equals(sOldValue) )
			return true;
		return false;
	}
	
	public String getName() {
		return entry.sName;
	}
	
	public String getValue() {
		return entry.sValue;
	}
	
	public ConfigEntry getEntry() {
		return entry;
	}
	
	// Add the action!
	private void selectFile() {
		
	}
	
	private void selectDir() {
		
	}
	
	private void toggleBool() {
		
	}
	
	private void saveChoice() {
		
	}
} // End Class ConfigEntryPanel
