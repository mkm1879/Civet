package edu.clemson.lph.civet.prefs;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.List;

import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class ConfigEntryPanel extends JPanel {
	private ConfigEntry entry = null;
	private ConfigDialog dlgParent = null;
	private JTextField jtfValue;
	private List<String> aChoices = null;
	private JComboBox<String> cbSelectValue;
	private JCheckBox chkBoolValue;

	/**
	 * Create the panel.
	 */
	public ConfigEntryPanel(ConfigDialog parent, ConfigEntry entry, List<String> aChoices) {
		this.entry = entry;
		this.dlgParent = parent;
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
			jtfValue.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					dlgParent.updateStatus();
				}
			});
		}
		else if( "Num".equalsIgnoreCase(entry.sType) ) {
			jtfValue.setEditable(true);
			jtfValue.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					// TODO check number format
					dlgParent.updateStatus();
				}
			});
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
			cbSelectValue.setSelectedItem(entry.sValue);
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
			return false;
		if( sNewValue == null && sOldValue != null )
			return true;
		if( sNewValue != null && sOldValue == null )
			return true;
		if( !sNewValue.equals(sOldValue) )
			return true;
		return false;
	}
	
	public boolean isMandatory() {
		return entry.bMandatory;
	}
	
	public String getName() {
		return entry.sName;
	}
	
	public String getValue() {
		return jtfValue.getText();
	}
	
	public ConfigEntry getEntry() {
		return entry;
	}
	
	// Add the action!
	private void selectFile() {
		JFileChooser chooser = new JFileChooser();
		File fCurrent = null;
		if( entry.sValue != null && !entry.sValue.equals("")) {
			fCurrent = new File( entry.sValue );
		}
		if( fCurrent == null || !fCurrent.exists() ) {
			fCurrent = new File(".");
		}
		chooser.setCurrentDirectory(fCurrent);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Civet Files", "csv", "txt", "xsd", "xslt", "exe");
		chooser.setFileFilter(filter);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = chooser.showDialog(this, "Select");
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			String sPath = chooser.getSelectedFile().getAbsolutePath();
			jtfValue.setText(sPath);
		}		
		dlgParent.updateStatus();
	}
	
	private void selectDir() {
		JFileChooser chooser = new JFileChooser();
		File fCurrent = null;
		if( entry.sValue != null && !entry.sValue.equals("")) {
			fCurrent = new File( entry.sValue );
		}
		if( fCurrent == null || !fCurrent.exists() ) {
			fCurrent = new File(".");
		}
		chooser.setCurrentDirectory(fCurrent);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = chooser.showDialog(this, "Select");
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			String sPath = chooser.getSelectedFile().getAbsolutePath();
			FileSystem fs = FileSystems.getDefault();
			String sSep = fs.getSeparator();
			sPath += sSep;
			jtfValue.setText(sPath);
		}		
		dlgParent.updateStatus();
	}
	
	private void toggleBool() {
		boolean bValue = chkBoolValue.isSelected();
		if( bValue ) 
			jtfValue.setText("TRUE");
		else
			jtfValue.setText("FALSE");
		dlgParent.updateStatus();
	}
	
	private void saveChoice() {
		String sChoice = (String)cbSelectValue.getSelectedItem();
		jtfValue.setText(sChoice);
		dlgParent.updateStatus();
	}
	
} // End Class ConfigEntryPanel
