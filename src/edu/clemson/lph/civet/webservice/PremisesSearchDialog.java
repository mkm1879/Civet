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

import org.apache.log4j.Logger;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetConfig;
import edu.clemson.lph.civet.lookup.LocalPremisesTableModel;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.SearchDialog;


@SuppressWarnings("serial")
public class PremisesSearchDialog extends JDialog implements SearchDialog<String> {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());

	private final JPanel contentPanel = new JPanel();
	private int deltaX;
	private int deltaY;
	private PremisesTableModel model = null;
	private String sPremisesId = null;
	private JTable tblResults;
	private JTextField jtfAddress1;
	private JTextField jtfAddress2;
	private JTextField jtfCity;
	private JTextField jtfCounty;
	private JTextField jtfState;
	private JTextField jtfZip;
	private JTextField jtfPhone;
	private JTextField jtfFax;
	private JButton btnSearch;
	private boolean bOK = false;
	private boolean bSearchNow = false;

	/**
	 * Create the dialog.
	 */
	public PremisesSearchDialog() {
		setModal(true);
		setBounds(100, 100, 618, 624);
		setTitle("Civet: Premises Search");
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
						if( tblResults.getSelectedRowCount() > 0 )
							bOK = true;
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
	
	/**
	 * For PhoneField focus lost
	 * @param sPhone
	 * @throws WebServiceException 
	 */
	public void searchPhone( String sPhone ) throws WebServiceException {
		clear();
		bSearchNow = true;
		jtfPhone.setText(sPhone);
		if( CivetConfig.isStandAlone() )
			model = new LocalPremisesTableModel( sPhone );
		else
			model = new UsaHerdsLookupPrems( sPhone );
		tblResults.setModel(model);
		if( model.getRowCount() == 1 ) {
			bOK = true;
		}
		else if( model.getRowCount() == 0 ) {
			bOK = false;
		}
		else {
			bSearchNow = false;
			setVisible( true );
		}
	}


	/**
	 * For Address, City, and ZipCode field focus lost
	 * @param sAddress
	 * @param sCity
	 * @param sStateCode
	 * @param sZipCode
	 * @throws WebServiceException 
	 */
	public void searchAddress( String sAddress, String sCity, String sStateCode, String sZipCode ) throws WebServiceException {
		clear();
		bSearchNow = true;
		jtfAddress1.setText(sAddress);
		jtfCity.setText(sCity);
		if( sStateCode == null ) 
			sStateCode = CivetConfig.getHomeStateAbbr();
		jtfState.setText(sStateCode);
		jtfZip.setText(sZipCode);
		if( CivetConfig.isStandAlone() )
			model = new LocalPremisesTableModel( sAddress, sCity, sStateCode, sZipCode, null );
		else
			model = new UsaHerdsLookupPrems( sAddress, sCity, sStateCode, sZipCode );
		tblResults.setModel(model);
		if( model.getRowCount() == 1 ) {
			bOK = true;
		}
		else if( model.getRowCount() == 0 ) {
			bOK = false;
		}
		else {
			bSearchNow = false;
			setVisible( true );
		}
	}

	public void clear() {
			sPremisesId = null;
		 jtfAddress1.setText(null);
		 jtfAddress2.setText(null);
		 jtfCity.setText(null);
		 jtfCounty.setText(null);
		 jtfState.setText(null);
		 jtfZip.setText(null);
		 jtfPhone.setText(null);
		 jtfFax.setText(null);
		 if( model != null )
			 model.clear();
	}
	
	private void doTableDblClick() {
		bOK = true;
		setVisible(false);
	}
	
	private void doSearch() throws WebServiceException {
		String sAddress1 = stringOrNull( jtfAddress1.getText() );
		String sCity = stringOrNull( jtfCity.getText() ); 
		String sStateCode = stringOrNull( jtfState.getText() );
		String sZipCode = stringOrNull( jtfZip.getText() ); 
		String sCounty = stringOrNull( jtfCounty.getText() ); 
		String sPhone = stringOrNull( jtfPhone.getText() );
		
		// Only search if something to search for!
		if( (sAddress1 != null && sAddress1.trim().length() > 0) ||
				(sCity != null && sCity.trim().length() > 0) &&
				(sStateCode != null && sStateCode.trim().length() > 0) ||
				(sZipCode != null && sZipCode.trim().length() > 0) ||
				(sCounty != null && sCounty.trim().length() > 0) ||
				(sPhone != null && sPhone.trim().length() > 0) ) {
			model = new UsaHerdsLookupPrems(null, null, 
					sAddress1, sCity, sStateCode, sZipCode, 
					sCounty, null, sPhone, null);
			if( CivetConfig.isStandAlone() )
				model = new LocalPremisesTableModel(sAddress1, sCity, sStateCode, sZipCode, sPhone);
			else
				model = new UsaHerdsLookupPrems(null, null, 
						sAddress1, sCity, sStateCode, sZipCode, 
						sCounty, null, sPhone, null);
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
	public String getSelectedKey() {
		int iTableRow = getSelectedRow();
		int iModelRow = getModelRow(iTableRow);
		sPremisesId = model.getPremIdAt( iModelRow );
		return sPremisesId;
	}
	
	public String getSelectedPhone() {
		int iTableRow = getSelectedRow();
		int iModelRow = getModelRow(iTableRow);
		String sRet = model.getPhoneAt( iModelRow );
		return sRet;
	}

	public String getSelectedAddress() {
		int iTableRow = getSelectedRow();
		int iModelRow = getModelRow(iTableRow);
		String sRet = model.getAddressAt( iModelRow );
		return sRet;
	}

	public String getSelectedCity( ) {
		int iTableRow = getSelectedRow();
		int iModelRow = getModelRow(iTableRow);
		String sRet = model.getCityAt( iModelRow );
		return sRet;
	}

	public String getSelectedStateCode() {
		int iTableRow = getSelectedRow();
		int iModelRow = getModelRow(iTableRow);
		String sRet = model.getStateCodeAt( iModelRow );
		return sRet;
	}

	public String getSelectedZipCode() {
		int iTableRow = getSelectedRow();
		int iModelRow = getModelRow(iTableRow);
		String sRet = model.getZipCodeAt( iModelRow );
		return sRet;
	}

	public String getSelectedPremName() {
		int iTableRow = getSelectedRow();
		int iModelRow = getModelRow(iTableRow);
		String sRet = model.getPremNameAt( iModelRow );
		return sRet;
	}

	public String getSelectedPremId() {
		int iTableRow = getSelectedRow();
		int iModelRow = getModelRow(iTableRow);
		String sRet = model.getPremIdAt( iModelRow );
		return sRet;
	}

	public void setPhone( String sPhone ) {
		jtfPhone.setText(sPhone);
	}

	public void setAddress( String sAddress ) {
		jtfAddress1.setText(sAddress);
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
	
	public void setDataModel( UsaHerdsLookupPrems model ) {
		this.model = model;
		tblResults.setModel(model);
	}

	private void setupParameterPanel() {
		JPanel pParameters = new JPanel();
		pParameters.setBorder(new EmptyBorder(2, 2, 2, 2));
		contentPanel.add(pParameters, BorderLayout.NORTH);
		
		GridBagLayout gbl_pParameters = new GridBagLayout();
		gbl_pParameters.columnWidths = new int[] {0, 0, 0, 0, 0, };
		gbl_pParameters.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
		gbl_pParameters.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pParameters.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		pParameters.setLayout(gbl_pParameters);
		{
			JLabel lblAddressLine = new JLabel("Address Line 1:");
			GridBagConstraints gbc_lblAddressLine = new GridBagConstraints();
			gbc_lblAddressLine.anchor = GridBagConstraints.EAST;
			gbc_lblAddressLine.insets = new Insets(0, 0, 5, 5);
			gbc_lblAddressLine.gridx = 0;
			gbc_lblAddressLine.gridy = 1;
			pParameters.add(lblAddressLine, gbc_lblAddressLine);
		}
		{
			jtfAddress1 = new JTextField();
			GridBagConstraints gbc_textField_1 = new GridBagConstraints();
			gbc_textField_1.insets = new Insets(0, 0, 5, 0);
			gbc_textField_1.anchor = GridBagConstraints.WEST;
			gbc_textField_1.gridx = 1;
			gbc_textField_1.gridy = 1;
			gbc_textField_1.gridwidth = 3;
			pParameters.add(jtfAddress1, gbc_textField_1);
			jtfAddress1.setColumns(50);
			setMinimumSize(jtfAddress1);
		}
		{
			JLabel lblAddressLine_1 = new JLabel("Address Line 2:");
			GridBagConstraints gbc_lblAddressLine_1 = new GridBagConstraints();
			gbc_lblAddressLine_1.anchor = GridBagConstraints.EAST;
			gbc_lblAddressLine_1.insets = new Insets(0, 0, 5, 5);
			gbc_lblAddressLine_1.gridx = 0;
			gbc_lblAddressLine_1.gridy = 2;
			pParameters.add(lblAddressLine_1, gbc_lblAddressLine_1);
		}
		{
			jtfAddress2 = new JTextField();
			GridBagConstraints gbc_textField_2 = new GridBagConstraints();
			gbc_textField_2.insets = new Insets(0, 0, 5, 0);
			gbc_textField_2.anchor = GridBagConstraints.WEST;
			gbc_textField_2.gridx = 1;
			gbc_textField_2.gridy = 2;
			gbc_textField_2.gridwidth = 3;
			pParameters.add(jtfAddress2, gbc_textField_2);
			jtfAddress2.setColumns(50);
			setMinimumSize(jtfAddress2);
		}
		{
			JLabel lblCity = new JLabel("City:");
			GridBagConstraints gbc_lblCity = new GridBagConstraints();
			gbc_lblCity.anchor = GridBagConstraints.EAST;
			gbc_lblCity.insets = new Insets(0, 0, 5, 5);
			gbc_lblCity.gridx = 0;
			gbc_lblCity.gridy = 3;
			pParameters.add(lblCity, gbc_lblCity);
		}
		{
			jtfCity = new JTextField();
			GridBagConstraints gbc_textField_3 = new GridBagConstraints();
			gbc_textField_3.insets = new Insets(0, 0, 5, 5);
			gbc_textField_3.anchor = GridBagConstraints.WEST;
			gbc_textField_3.gridx = 1;
			gbc_textField_3.gridy = 3;
			pParameters.add(jtfCity, gbc_textField_3);
			jtfCity.setColumns(25);
			setMinimumSize(jtfCity);
		}
		{
			JLabel lblCounty = new JLabel("County:");
			GridBagConstraints gbc_lblCounty = new GridBagConstraints();
			gbc_lblCounty.anchor = GridBagConstraints.EAST;
			gbc_lblCounty.insets = new Insets(0, 0, 5, 5);
			gbc_lblCounty.gridx = 2;
			gbc_lblCounty.gridy = 3;
			pParameters.add(lblCounty, gbc_lblCounty);
		}
		{
			jtfCounty = new JTextField();
			jtfCounty.setColumns(20);
			GridBagConstraints gbc_textField_4 = new GridBagConstraints();
			gbc_textField_4.insets = new Insets(0, 0, 5, 0);
			gbc_textField_4.anchor = GridBagConstraints.WEST;
			gbc_textField_4.gridx = 3;
			gbc_textField_4.gridy = 3;
			pParameters.add(jtfCounty, gbc_textField_4);
			jtfCounty.setColumns(25);
			setMinimumSize(jtfCounty);
		}
		{
			JLabel lblState = new JLabel("State:");
			GridBagConstraints gbc_lblState = new GridBagConstraints();
			gbc_lblState.anchor = GridBagConstraints.EAST;
			gbc_lblState.insets = new Insets(0, 0, 5, 5);
			gbc_lblState.gridx = 0;
			gbc_lblState.gridy = 4;
			pParameters.add(lblState, gbc_lblState);
		}
		{
			jtfState = new JTextField();
			GridBagConstraints gbc_textField_7 = new GridBagConstraints();
			gbc_textField_7.anchor = GridBagConstraints.WEST;
			gbc_textField_7.insets = new Insets(0, 0, 5, 5);
			gbc_textField_7.gridx = 1;
			gbc_textField_7.gridy = 4;
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
			gbc_lblZip.gridy = 4;
			pParameters.add(lblZip, gbc_lblZip);
		}
		{
			jtfZip = new JTextField();
			GridBagConstraints gbc_textField_8 = new GridBagConstraints();
			gbc_textField_8.anchor = GridBagConstraints.WEST;
			gbc_textField_8.insets = new Insets(0, 0, 5, 0);
			gbc_textField_8.gridx = 3;
			gbc_textField_8.gridy = 4;
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
			gbc_lblPhone.gridy = 5;
			pParameters.add(lblPhone, gbc_lblPhone);
		}
		{
			jtfPhone = new JTextField();
			GridBagConstraints gbc_textField_5 = new GridBagConstraints();
			gbc_textField_5.anchor = GridBagConstraints.WEST;
			gbc_textField_5.insets = new Insets(0, 0, 5, 5);
			gbc_textField_5.gridx = 1;
			gbc_textField_5.gridy = 5;
			pParameters.add(jtfPhone, gbc_textField_5);
			jtfPhone.setColumns(12);
			setMinimumSize(jtfPhone);
		}
		{
			JLabel lblFax = new JLabel("Fax:");
			GridBagConstraints gbc_lblFax = new GridBagConstraints();
			gbc_lblFax.anchor = GridBagConstraints.EAST;
			gbc_lblFax.insets = new Insets(0, 0, 5, 5);
			gbc_lblFax.gridx = 2;
			gbc_lblFax.gridy = 5;
			pParameters.add(lblFax, gbc_lblFax);
		}
		{
			jtfFax = new JTextField();
			GridBagConstraints gbc_textField_6 = new GridBagConstraints();
			gbc_textField_6.anchor = GridBagConstraints.WEST;
			gbc_textField_6.insets = new Insets(0, 0, 5, 0);
			gbc_textField_6.gridx = 3;
			gbc_textField_6.gridy = 5;
			pParameters.add(jtfFax, gbc_textField_6);
			jtfFax.setColumns(12);
			setMinimumSize(jtfFax);
		}
		{
			btnSearch = new JButton("Search");
			btnSearch.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						doSearch();
					} catch (WebServiceException e) {
						MessageDialog.showMessage(PremisesSearchDialog.this, "Civet: Error", "Web Service Failure\n" + e.getMessage());
						logger.error(e);
					}
				}
			});
			getRootPane().setDefaultButton(btnSearch);
			GridBagConstraints gbc_btnSearch = new GridBagConstraints();
			gbc_btnSearch.insets = new Insets(10, 0, 10, 0);
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
