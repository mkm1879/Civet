package edu.clemson.lph.civet.prefs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class TableCellPanel extends JPanel {
	private String sType = null;
	private File fCurrentDir = new File(".");
	JTextField jtfValue;
	JCheckBox ckValue = null;
	JButton bFile = null;
	JButton bDir = null;
	
	/**
	 * Create the panel.
	 */
	public TableCellPanel( String sType ) {
		this.sType = sType;
		this.setLayout(new BorderLayout());
		jtfValue = new JTextField();
		jtfValue.setColumns(50);
		this.add(jtfValue, BorderLayout.CENTER);
		if("Text".equals(sType)) {
			System.err.println("Text");
			jtfValue.setEditable(true);
		}
		else {
			jtfValue.setEditable(false);
		}
		if("Bool".equals(sType)) {
			ckValue = new JCheckBox("");
			ckValue.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectChecked();
				}
			});
			this.add(ckValue, BorderLayout.EAST);
		}
		if("File".equals(sType)) {
			bFile = new JButton("Browse");
			bFile.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectFile();
				}
			});
		}
		if("Dir".equals(sType)) {
			bDir = new JButton("Browse");
			bDir.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectDirectory();
				}
			});
		}
	}
	
	public void setValue(String sValue) {
		jtfValue.setText(sValue);
		if("Bool".equals(sType)) {
			if( "TRUE".equalsIgnoreCase(sValue) || "YES".equalsIgnoreCase(sValue) )
				ckValue.setSelected(true);
			else
				ckValue.setSelected(false);
		}
		if("File".equals(sType)) {
			File fCurrent = new File( sValue );
			if( fCurrent.exists() ) {
				fCurrentDir = fCurrent.getParentFile();
			}
		}
		if("Dir".equals(sType)) {
			File fCurrent = new File( sValue );
			if( fCurrent.exists() ) {
				fCurrentDir = fCurrent;
			}
		}
	}
	
	public String getValue() {
		return jtfValue.getText();
	}
	
	private void selectChecked() {
		if( ckValue.isSelected() ) {
			jtfValue.setText("TRUE");
		}
		else {
			jtfValue.setText("FALSE");
		}
	}
	
	private void selectFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setCurrentDirectory(fCurrentDir);
		int returnVal = chooser.showOpenDialog(this);
		if( returnVal == JFileChooser.APPROVE_OPTION ) {
			File fSel = chooser.getSelectedFile();
			String sSel = fSel.getAbsolutePath();
			jtfValue.setText(sSel);
		}
	}

	private void selectDirectory() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(fCurrentDir);
		int returnVal = chooser.showOpenDialog(this);
		if( returnVal == JFileChooser.APPROVE_OPTION ) {
			File fSel = chooser.getSelectedFile();
			String sSel = fSel.getAbsolutePath();
			jtfValue.setText(sSel);
		}
	}

}
