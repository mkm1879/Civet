package edu.clemson.lph.civet.prefs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class ConfigDialog extends JDialog {

	private List<JPanel> contentPanel = new ArrayList<JPanel>();
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
			JPanel pThis = addTab(sTab, iRows);
			for( int i = 0; i < iRows; i++ ) {
				if(	me.nextRow() != null ) {
					addRow(me, pThis, i);
				}
			}
		} 

	}
	
	private JPanel addTab( String sTabName, int iRows ) {
		JPanel pThis = new JPanel();
		tabbedPane.addTab(sTabName, null, pThis, null);
		pThis.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gbl_pThis = new GridBagLayout();
		gbl_pThis.columnWidths = new int[]{0,0,0,0};
		gbl_pThis.rowHeights = new int[iRows+1];
		for( int i = 0; i < iRows; i++ )
			gbl_pThis.rowHeights[i] = 0;
		gbl_pThis.rowHeights[iRows] = 1;
		gbl_pThis.columnWeights = new double[]{0.0,0.0,1.0,0.0};
		gbl_pThis.rowWeights = new double[iRows+1];
		for( int i = 0; i < iRows; i++ ) {
			gbl_pThis.rowWeights[i] = 0.0;
		}
		gbl_pThis.rowWeights[iRows] = Double.MAX_VALUE;
		
		pThis.setLayout(gbl_pThis);
		return pThis;
	}
	
	private void addRow( ConfigCsv me, JPanel pThis, int iRow ) {
		final String sName = me.getName();
		final String sType = me.getType();
		final String sDesc = me.getDescription();
		final String sHelp = me.getHelpText();
		final String sValue = me.getDefault();
		{
			JLabel lblNewLabel = new JLabel(sName);
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel.gridx = 0;
			gbc_lblNewLabel.gridy = iRow;
			pThis.add(lblNewLabel, gbc_lblNewLabel);
		}
		{
			JLabel lblNewLabel_1 = new JLabel(sDesc);
			GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
			gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_1.gridx = 1;
			gbc_lblNewLabel_1.gridy = iRow;
			pThis.add(lblNewLabel_1, gbc_lblNewLabel_1);
		}
		if( "Text".equals(sType) || "Num".equals(sType) ) {
			JTextField jtfValue = new JTextField();
			jtfValue.setColumns(50);
			jtfValue.setToolTipText(sHelp);
			GridBagConstraints gbc_textField = new GridBagConstraints();
			gbc_textField.insets = new Insets(0, 0, 5, 0);
			gbc_textField.fill = GridBagConstraints.HORIZONTAL;
			gbc_textField.gridx = 2;
			gbc_textField.gridy = iRow;
			pThis.add(jtfValue, gbc_textField);
			hControls.put(sName, jtfValue);
		}
		else if ( "Bool".equals(sType))	{
			JCheckBox chkValue = new JCheckBox("Check for yes/true");
			chkValue.setToolTipText(sHelp);
			GridBagConstraints gbc_chckbxTrue = new GridBagConstraints();
			gbc_chckbxTrue.insets = new Insets(0, 0, 5, 0);
			gbc_chckbxTrue.gridx = 2;
			gbc_chckbxTrue.gridy = iRow;
			pThis.add(chkValue, gbc_chckbxTrue);
			hControls.put(sName, chkValue);
		}
		else if ( "Select".equals(sType) )	{
			JComboBox cbValue = new JComboBox();
			cbValue.setToolTipText(sHelp);
			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 2;
			gbc_comboBox.gridy = iRow;
			pThis.add(cbValue, gbc_comboBox);
			hControls.put(sName, cbValue);
		}
		else if ( "Dir".equals(sType) )	{
			JLabel lValue = new JLabel(sValue);
			JButton jbValue = new JButton("Choose");
			jbValue.setToolTipText(sHelp);
			jbValue.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ConfigDialog.this.runFileChooser(sName, sType, sDesc, sValue, hControls);
				}
			});
			GridBagConstraints gbc_label = new GridBagConstraints();
			gbc_label.fill = GridBagConstraints.HORIZONTAL;
			gbc_label.gridx = 2;
			gbc_label.gridy = iRow;
			pThis.add(lValue, gbc_label);
			GridBagConstraints gbc_comboBox = new GridBagConstraints();
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 3;
			gbc_comboBox.gridy = iRow;
			pThis.add(jbValue, gbc_comboBox);
		}
	}

	void runFileChooser( String sName, String sType, String sDescription, String sPath, HashMap hControls ) {
		JFileChooser chooser = new JFileChooser();
		// Setup and run
		File f = chooser.getSelectedFile();
		String sNewPath = f.getAbsolutePath();
		hControls.put(sName, sNewPath);
	}

}
