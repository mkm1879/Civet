package edu.clemson.lph.civet.webservice;
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;

import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.GridBagConstraints;

import javax.swing.JTextField;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import edu.clemson.lph.civet.lookup.VetLookup;
import edu.clemson.lph.dialogs.SearchDialog;


@SuppressWarnings("serial")
public class VetSearchDialog extends JDialog implements SearchDialog<Integer> {

	private final JPanel contentPanel = new JPanel();
	private int deltaX;
	private int deltaY;
	private VetLookup model = null;
	private JTable tblResults;
	private JTextField jtfAddress;
	private JTextField jtfCity;
	private JTextField jtfState;
	private JTextField jtfZip;
	private JTextField jtfPhone;
	private JButton btnSearch;
	private boolean bOK = false;
	private boolean bSearchNow = false;
	private JTextField jtfFirstName;
	private JTextField jtfLastName;
	private JTextField jtfNan;
	private JTextField jtfLicenseNbr;

	/**
	 * Create the dialog.
	 */
	public VetSearchDialog() {
		setModal(true);
		setBounds(100, 100, 618, 624);
		setTitle("Civet: Veterinarian Search");
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout());
		
		setupParameterPanel();
		{
			JScrollPane spResultTable = new JScrollPane();
			// Change this if edge of scrollpane looks funny
			spResultTable.setBorder(null);
			contentPanel.add(spResultTable, BorderLayout.CENTER);
			tblResults = new JTable();
			tblResults.setAutoCreateRowSorter(true);
			tblResults.addMouseListener( new java.awt.event.MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if( e.getClickCount() == 2 )
						doTableDblClick();
					else 
						super.mouseClicked(e);
				}
			});
			spResultTable.setViewportView(tblResults);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						if( tblResults.getSelectedRowCount() > 0 ) {
							bOK = true;
						}
						setVisible(false);
					}
				});
				buttonPane.add(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						bOK = false;
						setVisible(false);
					}
				});
				buttonPane.add(cancelButton);
			}
		}
	}
	
	public void clear() {
		 jtfAddress.setText(null);
		 jtfCity.setText(null);
		 jtfState.setText(null);
		 jtfZip.setText(null);
		 jtfPhone.setText(null);
		 if( model != null )
			 model.clear();
	}
	
	private void doTableDblClick() {
		bOK = true;
		setVisible(false);
	}
	
	private void doSearch() {
		String sFirstName = stringOrNull( jtfFirstName.getText() );
		String sLastName = stringOrNull( jtfLastName.getText() );
		String sAddress = stringOrNull( jtfAddress.getText() );
		String sCity = stringOrNull( jtfCity.getText() ); 
		String sStateCode = stringOrNull( jtfState.getText() );
		String sZipCode = stringOrNull( jtfZip.getText() ); 
		String sPhone = stringOrNull( jtfPhone.getText() );
		String sNan = stringOrNull( jtfNan.getText() );
		String sLicNbr = stringOrNull( jtfLicenseNbr.getText() );
		
		// Only search if something to search for!
		if( (sFirstName != null && sFirstName.trim().length() > 0) ||
				(sLastName != null && sLastName.trim().length() > 0) ||
				(sAddress != null && sAddress.trim().length() > 0) ||
				(sCity != null && sCity.trim().length() > 0) ||
				(sStateCode != null && sStateCode.trim().length() > 0) ||
				(sZipCode != null && sZipCode.trim().length() > 0) ||
				(sPhone != null && sPhone.trim().length() > 0) ||
				(sNan != null && sNan.trim().length() > 0) ||
				(sLicNbr != null && sLicNbr.trim().length() > 0) ) {
			model = new VetLookup(sLastName, sFirstName, 
					sAddress, sCity, sStateCode, sZipCode, 
					sPhone, sNan, sLicNbr);
			tblResults.setModel( model );
		}
	}
	
	public String stringOrNull( String sIn ) {
		String sOut = sIn;
		if( sOut != null && sOut.trim().length() == 0 ) sOut = null;
		return sOut;
	}

	@Override
	public void setDeltas( int deltaX, int deltaY ) {
		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}

	@Override
	public void center() {
		//Center the window
		boolean bSmall = false;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize();
		if( frameSize.height > screenSize.height ) {
			frameSize.height = screenSize.height;
			bSmall = true;
		}
		if( frameSize.width > screenSize.width ) {
			frameSize.width = screenSize.width;
			bSmall = true;
		}
		if( bSmall ) {
			setLocation( (screenSize.width - frameSize.width) / 2, 0);
		}
		else {
			setLocation( deltaX + (screenSize.width - frameSize.width) / 2,
					deltaY + (screenSize.height - frameSize.height) / 2);
		}
	}

	@Override
	public boolean exitOK() {
		return bOK;
	}

	private int getModelRow( int iTableRow ) {
		if( !bSearchNow )
			return tblResults.convertRowIndexToModel(iTableRow);
		else
			return iTableRow;
	}
	
	private int getSelectedRow() {
		if( !bSearchNow )
			return tblResults.getSelectedRow();
		else
			return 0;
	}
	

	/**
	 * This overrides the more general getSelectedKey for SearchDialog. In this case the returned 
	 * value is the State or Federal Premises ID. 
	 */
	@Override
	public Integer getSelectedKey() {
		int iTableRow = getSelectedRow();
		int iModelRow = getModelRow(iTableRow);
		return  (Integer)model.getValueAt( iModelRow, 0 );
	}
//	
//	public String getSelectedPhone() {
//		int iTableRow = getSelectedRow();
//		int iModelRow = getModelRow(iTableRow);
//		String sRet = model.getPhoneAt( iModelRow );
//		return sRet;
//	}
//
//	public String getSelectedAddress() {
//		int iTableRow = getSelectedRow();
//		int iModelRow = getModelRow(iTableRow);
//		String sRet = model.getAddress1At( iModelRow );
//		return sRet;
//	}
//
//	public String getSelectedCity( ) {
//		int iTableRow = getSelectedRow();
//		int iModelRow = getModelRow(iTableRow);
//		String sRet = model.getCityAt( iModelRow );
//		return sRet;
//	}
//
//	public String getSelectedStateCode() {
//		int iTableRow = getSelectedRow();
//		int iModelRow = getModelRow(iTableRow);
//		String sRet = model.getStateCodeAt( iModelRow );
//		return sRet;
//	}
//
//	public String getSelectedZipCode() {
//		int iTableRow = getSelectedRow();
//		int iModelRow = getModelRow(iTableRow);
//		String sRet = model.getZipCodeAt( iModelRow );
//		return sRet;
//	}

	

	public void setPhone( String sPhone ) {
		jtfPhone.setText(sPhone);
	}

	public void setAddress( String sAddress ) {
		jtfAddress.setText(sAddress);
	}

	public void setCity( String sCity ) {
		jtfCity.setText(sCity);
	}

	public void setStateCode( String sStateCode ) {
		jtfState.setText(sStateCode);
	}

	public void setZipCode( String sZipCode ) {
		jtfZip.setText(sZipCode);
	}
	
	public void setDataModel( VetLookup model ) {
		this.model = model;
		tblResults.setModel(model);
	}

	private void setupParameterPanel() {
		JPanel pParameters = new JPanel();
		pParameters.setBorder(new EmptyBorder(2, 2, 2, 2));
		contentPanel.add(pParameters, BorderLayout.NORTH);
		
		GridBagLayout gbl_pParameters = new GridBagLayout();
		gbl_pParameters.columnWidths = new int[] {0, 0, 0, 0 };
		gbl_pParameters.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
		gbl_pParameters.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pParameters.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
		pParameters.setLayout(gbl_pParameters);
		{
			JLabel lblFirstName = new JLabel("First Name:");
			GridBagConstraints gbc_lblFirstName = new GridBagConstraints();
			gbc_lblFirstName.anchor = GridBagConstraints.EAST;
			gbc_lblFirstName.insets = new Insets(5, 0, 5, 5);
			gbc_lblFirstName.gridx = 0;
			gbc_lblFirstName.gridy = 0;
			pParameters.add(lblFirstName, gbc_lblFirstName);
		}
		{
			jtfFirstName = new JTextField();
			GridBagConstraints gbc_jtfFirstName = new GridBagConstraints();
			gbc_jtfFirstName.anchor = GridBagConstraints.WEST;
			gbc_jtfFirstName.insets = new Insets(5, 0, 5, 5);
			gbc_jtfFirstName.gridx = 1;
			gbc_jtfFirstName.gridy = 0;
			pParameters.add(jtfFirstName, gbc_jtfFirstName);
			jtfFirstName.setColumns(25);
			setMinimumSize(jtfFirstName);
		}
		{
			JLabel lblLastName = new JLabel("Last Name:");
			GridBagConstraints gbc_lblLastName = new GridBagConstraints();
			gbc_lblLastName.anchor = GridBagConstraints.EAST;
			gbc_lblLastName.insets = new Insets(5, 0, 5, 5);
			gbc_lblLastName.gridx = 2;
			gbc_lblLastName.gridy = 0;
			pParameters.add(lblLastName, gbc_lblLastName);
		}
		{
			jtfLastName = new JTextField();
			GridBagConstraints gbc_jtfLastName = new GridBagConstraints();
			gbc_jtfLastName.anchor = GridBagConstraints.WEST;
			gbc_jtfLastName.insets = new Insets(5, 0, 5, 5);
			gbc_jtfLastName.gridx = 3;
			gbc_jtfLastName.gridy = 0;
			pParameters.add(jtfLastName, gbc_jtfLastName);
			jtfLastName.setColumns(25);
			setMinimumSize(jtfLastName);
		}
		{
			JLabel lblAddressLine = new JLabel("Address:");
			GridBagConstraints gbc_lblAddressLine = new GridBagConstraints();
			gbc_lblAddressLine.anchor = GridBagConstraints.EAST;
			gbc_lblAddressLine.insets = new Insets(0, 0, 5, 5);
			gbc_lblAddressLine.gridx = 0;
			gbc_lblAddressLine.gridy = 1;
			pParameters.add(lblAddressLine, gbc_lblAddressLine);
		}
		{
			jtfAddress = new JTextField();
			GridBagConstraints gbc_textField_1 = new GridBagConstraints();
			gbc_textField_1.insets = new Insets(0, 0, 5, 5);
			gbc_textField_1.anchor = GridBagConstraints.WEST;
			gbc_textField_1.gridx = 1;
			gbc_textField_1.gridy = 1;
			gbc_textField_1.gridwidth = 3;
			pParameters.add(jtfAddress, gbc_textField_1);
			jtfAddress.setColumns(50);
			setMinimumSize(jtfAddress);
		}
		{
			JLabel lblCity = new JLabel("City:");
			GridBagConstraints gbc_lblCity = new GridBagConstraints();
			gbc_lblCity.anchor = GridBagConstraints.EAST;
			gbc_lblCity.insets = new Insets(0, 0, 5, 5);
			gbc_lblCity.gridx = 0;
			gbc_lblCity.gridy = 2;
			pParameters.add(lblCity, gbc_lblCity);
		}
		{
			jtfCity = new JTextField();
			GridBagConstraints gbc_textField_3 = new GridBagConstraints();
			gbc_textField_3.insets = new Insets(0, 0, 5, 5);
			gbc_textField_3.anchor = GridBagConstraints.WEST;
			gbc_textField_3.gridx = 1;
			gbc_textField_3.gridy = 2;
			gbc_textField_3.gridwidth = 3;
			pParameters.add(jtfCity, gbc_textField_3);
			jtfCity.setColumns(50);
			setMinimumSize(jtfCity);
		}
		{
			JLabel lblState = new JLabel("State:");
			GridBagConstraints gbc_lblState = new GridBagConstraints();
			gbc_lblState.anchor = GridBagConstraints.EAST;
			gbc_lblState.insets = new Insets(0, 0, 5, 5);
			gbc_lblState.gridx = 0;
			gbc_lblState.gridy = 3;
			pParameters.add(lblState, gbc_lblState);
		}
		{
			jtfState = new JTextField();
			GridBagConstraints gbc_textField_7 = new GridBagConstraints();
			gbc_textField_7.anchor = GridBagConstraints.WEST;
			gbc_textField_7.insets = new Insets(0, 0, 5, 5);
			gbc_textField_7.gridx = 1;
			gbc_textField_7.gridy = 3;
			pParameters.add(jtfState, gbc_textField_7);
			jtfState.setColumns(10);
			setMinimumSize(jtfState);
	}
		{
			JLabel lblZip = new JLabel("Zip:");
			GridBagConstraints gbc_lblZip = new GridBagConstraints();
			gbc_lblZip.anchor = GridBagConstraints.EAST;
			gbc_lblZip.insets = new Insets(0, 0, 5, 5);
			gbc_lblZip.gridx = 2;
			gbc_lblZip.gridy = 3;
			pParameters.add(lblZip, gbc_lblZip);
		}
		{
			jtfZip = new JTextField();
			GridBagConstraints gbc_textField_8 = new GridBagConstraints();
			gbc_textField_8.anchor = GridBagConstraints.WEST;
			gbc_textField_8.insets = new Insets(0, 0, 5, 5);
			gbc_textField_8.gridx = 3;
			gbc_textField_8.gridy = 3;
			pParameters.add(jtfZip, gbc_textField_8);
			jtfZip.setColumns(10);
			setMinimumSize(jtfZip);
		}
		{
			JLabel lblPhone = new JLabel("Phone:");
			GridBagConstraints gbc_lblPhone = new GridBagConstraints();
			gbc_lblPhone.anchor = GridBagConstraints.EAST;
			gbc_lblPhone.insets = new Insets(0, 0, 5, 5);
			gbc_lblPhone.gridx = 0;
			gbc_lblPhone.gridy = 4;
			pParameters.add(lblPhone, gbc_lblPhone);
		}
		{
			jtfPhone = new JTextField();
			GridBagConstraints gbc_textField_5 = new GridBagConstraints();
			gbc_textField_5.anchor = GridBagConstraints.WEST;
			gbc_textField_5.insets = new Insets(0, 0, 5, 5);
			gbc_textField_5.gridx = 1;
			gbc_textField_5.gridy = 4;
			pParameters.add(jtfPhone, gbc_textField_5);
			jtfPhone.setColumns(12);
			setMinimumSize(jtfPhone);
		}
		{
			JLabel lblNan = new JLabel("NAN #:");
			GridBagConstraints gbc_lblNan = new GridBagConstraints();
			gbc_lblNan.anchor = GridBagConstraints.EAST;
			gbc_lblNan.insets = new Insets(0, 0, 5, 5);
			gbc_lblNan.gridx = 0;
			gbc_lblNan.gridy = 5;
			pParameters.add(lblNan, gbc_lblNan);
		}
		{
			jtfNan = new JTextField();
			GridBagConstraints gbc_jtfNAN = new GridBagConstraints();
			gbc_jtfNAN.anchor = GridBagConstraints.WEST;
			gbc_jtfNAN.insets = new Insets(0, 0, 5, 5);
			gbc_jtfNAN.gridx = 1;
			gbc_jtfNAN.gridy = 5;
			pParameters.add(jtfNan, gbc_jtfNAN);
			jtfNan.setColumns(10);
			setMinimumSize(jtfNan);
		}
		{
			JLabel lblLic = new JLabel("Lic #:");
			GridBagConstraints gbc_lblLic = new GridBagConstraints();
			gbc_lblLic.anchor = GridBagConstraints.EAST;
			gbc_lblLic.insets = new Insets(0, 0, 5, 5);
			gbc_lblLic.gridx = 2;
			gbc_lblLic.gridy = 5;
			pParameters.add(lblLic, gbc_lblLic);
		}
		{
			jtfLicenseNbr = new JTextField();
			GridBagConstraints gbc_jtfLicenseNbr = new GridBagConstraints();
			gbc_jtfLicenseNbr.anchor = GridBagConstraints.WEST;
			gbc_jtfLicenseNbr.insets = new Insets(0, 0, 5, 5);
			gbc_jtfLicenseNbr.gridx = 3;
			gbc_jtfLicenseNbr.gridy = 5;
			pParameters.add(jtfLicenseNbr, gbc_jtfLicenseNbr);
			jtfLicenseNbr.setColumns(10);
			setMinimumSize(jtfLicenseNbr);
		}
		{
			btnSearch = new JButton("Search");
			btnSearch.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					doSearch();
				}
			});

			getRootPane().setDefaultButton(btnSearch);
			GridBagConstraints gbc_btnSearch = new GridBagConstraints();
			gbc_btnSearch.insets = new Insets(10, 0, 10, 5);
			gbc_btnSearch.gridx = 3;
			gbc_btnSearch.gridy = 7;
			pParameters.add(btnSearch, gbc_btnSearch);
		}
		
	}

	private static void setMinimumSize(final JTextField c) {
		c.setMinimumSize(new Dimension(c
				.getPreferredSize().width / 2,
				c.getPreferredSize().height));
	}

}
