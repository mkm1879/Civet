package edu.clemson.lph.civet;
/*
Copyright 2014 Michael K Martin

This file is part of Civet.

Civet is free software: you can redistribute it and/or modify
it under the terms of the Lesser GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Civet is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the Lesser GNU General Public License
along with Civet.  If not, see <http://www.gnu.org/licenses/>.
*/
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.lookup.ErrorTypeLookup;

import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class CVIErrorDialog extends JDialog {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private ArrayList<String> aErrorKeys;
	boolean bExitOK = false;

	private final JPanel contentPanel = new JPanel();
	private HashMap<String, String> hErrorTypes;
	private JTextField jtfNotes;
	private JPanel pErrorList;
	private HashMap<String, JCheckBox> hErrorBoxes;
	
	/**
	 * Create the dialog.
	 */
	public CVIErrorDialog( Window parent, ArrayList<String> aErrorKeys, String sNotes ) {
		super( parent );
		super.setModal(true);
		this.aErrorKeys = aErrorKeys;
		setTitle( "Civet: Errors in CVI");
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel pLabel = new JPanel();
			contentPanel.add(pLabel, BorderLayout.NORTH);
			pLabel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			{
				JLabel lblCheckAllApplicable = new JLabel("Check All Applicable Errors");
				pLabel.add(lblCheckAllApplicable);
			}
		}
		{
			pErrorList = new JPanel();
			contentPanel.add(pErrorList, BorderLayout.CENTER);
			pErrorList.setLayout(null);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
				okButton.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						CVIErrorDialog.this.aErrorKeys.clear();
						for( String sKey : hErrorTypes.keySet() ) {
							JCheckBox ckNext = hErrorBoxes.get(sKey);
							if( ckNext.isSelected() ) {
								CVIErrorDialog.this.aErrorKeys.add(sKey);
							}
						}
						bExitOK = true;
						setVisible(false);
					}
				});
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
				cancelButton.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						bExitOK = false;
						setVisible(false);
					}
				});
			}
			{
				JButton btnClearErrors = new JButton("Clear Errors");
				buttonPane.add(btnClearErrors);
				btnClearErrors.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						bExitOK = false;
						CVIErrorDialog.this.aErrorKeys.clear();
						jtfNotes.setText(null);
						for( String sKey : hErrorTypes.keySet() ) {
							JCheckBox ckNext = hErrorBoxes.get(sKey);
							ckNext.setSelected(false);
						}
					}
				});
			}
		}
		ErrorTypeLookup errors = new ErrorTypeLookup();
		this.hErrorTypes = new HashMap<String, String>();
		this.hErrorBoxes = new HashMap<String, JCheckBox>();
		int yValue = 5;
		for(String sError : errors.listErrorTypes() ) {
			String sKey = ErrorTypeLookup.getShortNameForDescription(sError);
			hErrorTypes.put(sKey, sError);
			JCheckBox ckNext = new JCheckBox(sError );
			ckNext.setBounds(20, yValue, 388, 23);
			if( aErrorKeys.contains(sKey) ) 
				ckNext.setSelected(true);
			else
				ckNext.setSelected(false);
			pErrorList.add(ckNext);
			hErrorBoxes.put(sKey, ckNext);
			yValue += 25;
		}
		jtfNotes = new JTextField();
		jtfNotes.setText(sNotes);
		yValue += 5;
		jtfNotes.setBounds(20, yValue, 388, 20);
		pErrorList.add(jtfNotes);
		jtfNotes.setColumns(10);
		setBounds(100, 100, 450, yValue + 150);
	}
	
	public boolean isExitOK() { return bExitOK; }
	
	public void setNotes( String sNotes ) {
		jtfNotes.setText(sNotes);
	}
	
	public String getNotes() { return jtfNotes.getText(); }
	
}
