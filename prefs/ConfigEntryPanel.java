package edu.clemson.lph.civet.prefs;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;

public class ConfigEntryPanel extends JPanel {
	private JTextField jtfValue;
	private ConfigEntry entry;

	/**
	 * Create the panel.
	 */
	public ConfigEntryPanel( ConfigEntry entry) {
		this.entry = entry;
		
		JLabel lblName = new JLabel(entry.sName);
		add(lblName, BorderLayout.WEST);
		
		jtfValue = new JTextField();
		jtfValue.setText(entry.sValue);
		jtfValue.setToolTipText(entry.sHelp);
		add(jtfValue, BorderLayout.CENTER);
		jtfValue.setColumns(50);
		if("Text".equalsIgnoreCase(entry.sType) || "Num".equalsIgnoreCase(entry.sType) ) {
			JLabel lblNull = new JLabel("");
			add(lblNull, BorderLayout.EAST);
		}
		else if( "Bool".equalsIgnoreCase(entry.sType) ) {
			JCheckBox chk = new JCheckBox();
			chk.setText("Check for TRUE");
			chk.setToolTipText(entry.sHelp);
			chk.setSelected(( "True".equalsIgnoreCase(entry.sValue) ) || ("Yes".equalsIgnoreCase(entry.sValue)) );
			add(chk, BorderLayout.EAST);		}
		else if( "Select".equalsIgnoreCase(entry.sType) ) {
			JComboBox<String> cb = new JComboBox<String>();
//			cb.setModel(new javax.swing.DefaultComboBoxModel<String>(new Vector<String>(model.getChoices(sName))) );
			cb.setSelectedItem(entry.sValue);
			cb.setToolTipText(entry.sHelp);
			cb.setEnabled(true);
			add(cb, BorderLayout.EAST);		}
		else if( "File".equalsIgnoreCase(entry.sType) ) {
			JButton button = new JButton("Browse");
			final String sFile = entry.sValue;
			button.setToolTipText(entry.sHelp);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectFile( sFile );
				}
			});
			add(button, BorderLayout.EAST);		}
		else if( "Dir".equalsIgnoreCase(entry.sType) ) {
			JButton button = new JButton("Browse");
			final String sDir = entry.sValue;
			button.setToolTipText(entry.sHelp);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectDir(sDir);
				}
			});
			add(button, BorderLayout.EAST);
		}

	}
	
	private void selectFile( String sPath ) {
		System.out.println( sPath );
	}
	
	private void selectDir( String sPath ) {
		System.out.println( sPath );		
	}

}
