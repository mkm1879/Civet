package edu.clemson.lph.civet.addons;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetEditDialog;
import edu.clemson.lph.civet.CivetInbox;
import edu.clemson.lph.civet.lookup.Counties;
import edu.clemson.lph.civet.lookup.LocalPremisesTableModel;
import edu.clemson.lph.civet.lookup.States;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.webservice.PremisesSearchDialog;
import edu.clemson.lph.civet.webservice.PremisesTableModel;
import edu.clemson.lph.civet.webservice.UsaHerdsLookupPrems;
import edu.clemson.lph.civet.webservice.WebServiceException;
import edu.clemson.lph.controls.DBComboBox;
import edu.clemson.lph.controls.DBSearchTextField;
import edu.clemson.lph.controls.DBSearchTextFieldListener;
import edu.clemson.lph.controls.SearchTextField;
import edu.clemson.lph.controls.SearchTextFieldListener;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.utils.CountyUtils;
import edu.clemson.lph.utils.PremCheckSum;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.Beans;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JComboBox;

@SuppressWarnings("serial")
public class ParticipantPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	static {
	     logger.setLevel(CivetConfig.getLogLevel());
	}
	String sLabel;
	JCheckBox ckSticky;
	SearchTextField jtfPIN;
	private PremisesSearchDialog premSearch = new PremisesSearchDialog();
	JTextField jtfBusiness;
	JTextField jtfName;
	JTextField jtfAddress;
	JTextField jtfCity;
	JTextField jtfZip;
	DBComboBox cbState;
	JComboBox<String> cbCounty;
	private boolean bInSearch = false;
	NineDashThreeDialog myParent = null;

	public void setParent( NineDashThreeDialog parent ) {
		myParent = parent;
	}
	
	/**
	 * Create the panel.
	 */
	public ParticipantPanel(String sLabel) {
		this.sLabel = sLabel;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {30, 50, 120, 90, 55, 5};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblConsignor = new JLabel(sLabel);
		lblConsignor.setFont(new Font("Tahoma", Font.PLAIN, 15));
		GridBagConstraints gbc_lblConsignor = new GridBagConstraints();
		gbc_lblConsignor.gridwidth = 2;
		gbc_lblConsignor.anchor = GridBagConstraints.WEST;
		gbc_lblConsignor.insets = new Insets(0, 0, 5, 5);
		gbc_lblConsignor.gridx = 1;
		gbc_lblConsignor.gridy = 1;
		add(lblConsignor, gbc_lblConsignor);
		
		JLabel lblNewLabel = new JLabel("Sticky?");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 3;
		gbc_lblNewLabel.gridy = 1;
		add(lblNewLabel, gbc_lblNewLabel);
		
		ckSticky = new JCheckBox("");
		GridBagConstraints gbc_chkSticky = new GridBagConstraints();
		gbc_chkSticky.anchor = GridBagConstraints.NORTH;
		gbc_chkSticky.insets = new Insets(0, 0, 5, 0);
		gbc_chkSticky.gridx = 4;
		gbc_chkSticky.gridy = 1;
		add(ckSticky, gbc_chkSticky);
		
		JLabel lblPin = new JLabel("PIN:");
		lblPin.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblPin = new GridBagConstraints();
		gbc_lblPin.anchor = GridBagConstraints.EAST;
		gbc_lblPin.insets = new Insets(0, 0, 5, 5);
		gbc_lblPin.gridx = 1;
		gbc_lblPin.gridy = 3;
		add(lblPin, gbc_lblPin);
		
		jtfPIN = new SearchTextField();
		jtfPIN.setSearchDialog( premSearch );
		jtfPIN.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(FocusEvent e) {
				jtfThisPIN_focusLost(e);
			}
		});
		
		GridBagConstraints gbc_jtfPIN = new GridBagConstraints();
		gbc_jtfPIN.anchor = GridBagConstraints.NORTH;
		gbc_jtfPIN.insets = new Insets(0, 0, 5, 5);
		gbc_jtfPIN.fill = GridBagConstraints.HORIZONTAL;
		gbc_jtfPIN.gridx = 2;
		gbc_jtfPIN.gridy = 3;
		add(jtfPIN, gbc_jtfPIN);
		jtfPIN.setColumns(8);
		
		JLabel lblBusiness = new JLabel("Business:");
		lblBusiness.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblBusiness = new GridBagConstraints();
		gbc_lblBusiness.anchor = GridBagConstraints.EAST;
		gbc_lblBusiness.insets = new Insets(0, 0, 5, 5);
		gbc_lblBusiness.gridx = 1;
		gbc_lblBusiness.gridy = 4;
		add(lblBusiness, gbc_lblBusiness);
		
		jtfBusiness = new JTextField();
		GridBagConstraints gbc_jtfBusiness = new GridBagConstraints();
		gbc_jtfBusiness.gridwidth = 3;
		gbc_jtfBusiness.insets = new Insets(0, 0, 5, 5);
		gbc_jtfBusiness.fill = GridBagConstraints.HORIZONTAL;
		gbc_jtfBusiness.gridx = 2;
		gbc_jtfBusiness.gridy = 4;
		add(jtfBusiness, gbc_jtfBusiness);
		jtfBusiness.setColumns(10);
		
		JLabel lblName = new JLabel("Name:");
		lblName.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 1;
		gbc_lblName.gridy = 5;
		add(lblName, gbc_lblName);
		
		jtfName = new JTextField();
		GridBagConstraints gbc_jtfName = new GridBagConstraints();
		gbc_jtfName.gridwidth = 3;
		gbc_jtfName.insets = new Insets(0, 0, 5, 5);
		gbc_jtfName.fill = GridBagConstraints.HORIZONTAL;
		gbc_jtfName.gridx = 2;
		gbc_jtfName.gridy = 5;
		add(jtfName, gbc_jtfName);
		jtfName.setColumns(10);
		
		JLabel lblAddress = new JLabel("Address:");
		lblAddress.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblAddress = new GridBagConstraints();
		gbc_lblAddress.anchor = GridBagConstraints.EAST;
		gbc_lblAddress.insets = new Insets(0, 0, 5, 5);
		gbc_lblAddress.gridx = 1;
		gbc_lblAddress.gridy = 6;
		add(lblAddress, gbc_lblAddress);
		
		jtfAddress = new JTextField();
		GridBagConstraints gbc_jtfAddress = new GridBagConstraints();
		gbc_jtfAddress.gridwidth = 3;
		gbc_jtfAddress.insets = new Insets(0, 0, 5, 5);
		gbc_jtfAddress.fill = GridBagConstraints.HORIZONTAL;
		gbc_jtfAddress.gridx = 2;
		gbc_jtfAddress.gridy = 6;
		add(jtfAddress, gbc_jtfAddress);
		jtfAddress.setColumns(10);
		
		JLabel lblCity = new JLabel("City:");
		lblCity.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblCity = new GridBagConstraints();
		gbc_lblCity.anchor = GridBagConstraints.EAST;
		gbc_lblCity.insets = new Insets(0, 0, 5, 5);
		gbc_lblCity.gridx = 1;
		gbc_lblCity.gridy = 7;
		add(lblCity, gbc_lblCity);
		
		jtfCity = new JTextField();
		GridBagConstraints gbc_jtfCity = new GridBagConstraints();
		gbc_jtfCity.gridwidth = 3;
		gbc_jtfCity.insets = new Insets(0, 0, 5, 5);
		gbc_jtfCity.fill = GridBagConstraints.HORIZONTAL;
		gbc_jtfCity.gridx = 2;
		gbc_jtfCity.gridy = 7;
		add(jtfCity, gbc_jtfCity);
		jtfCity.setColumns(10);
		
		JLabel lblState = new JLabel("State:");
		lblState.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblState = new GridBagConstraints();
		gbc_lblState.anchor = GridBagConstraints.EAST;
		gbc_lblState.insets = new Insets(0, 0, 5, 5);
		gbc_lblState.gridx = 1;
		gbc_lblState.gridy = 8;
		add(lblState, gbc_lblState);
		
		cbState = new DBComboBox();
		GridBagConstraints gbc_cbState = new GridBagConstraints();
		gbc_cbState.gridwidth = 3;
		gbc_cbState.insets = new Insets(0, 0, 5, 5);
		gbc_cbState.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbState.gridx = 2;
		gbc_cbState.gridy = 8;
		add(cbState, gbc_cbState);
		cbState.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				refreshOtherCounties();
			}
		});
		
		JLabel lblZip = new JLabel("Zip:");
		lblZip.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblZip = new GridBagConstraints();
		gbc_lblZip.anchor = GridBagConstraints.EAST;
		gbc_lblZip.insets = new Insets(0, 0, 5, 5);
		gbc_lblZip.gridx = 1;
		gbc_lblZip.gridy = 9;
		add(lblZip, gbc_lblZip);
		
		jtfZip = new JTextField();
		GridBagConstraints gbc_jtfZip = new GridBagConstraints();
		gbc_jtfZip.insets = new Insets(0, 0, 5, 5);
		gbc_jtfZip.fill = GridBagConstraints.HORIZONTAL;
		gbc_jtfZip.gridx = 2;
		gbc_jtfZip.gridy = 9;
		add(jtfZip, gbc_jtfZip);
		jtfZip.setColumns(10);
		jtfZip.addFocusListener( new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
			}
			@Override
			public void focusLost(FocusEvent arg0) {
				checkZipcode( jtfZip );
				String sZip = jtfZip.getText();
				if( sZip != null && sZip.trim().length() > 0 ) {
					try {
						String sZipCounty = CountyUtils.getCounty(sZip);
						String sHerdsCounty = Counties.getHerdsCounty(cbState.getSelectedCode(), sZipCounty);
						cbCounty.setSelectedItem(sHerdsCounty);
					} catch (IOException e) {
						logger.error(e);
					}
				}
			}
		});

		JLabel lblCounty = new JLabel("County:");
		lblCounty.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblCounty = new GridBagConstraints();
		gbc_lblCounty.insets = new Insets(0, 0, 0, 5);
		gbc_lblCounty.anchor = GridBagConstraints.EAST;
		gbc_lblCounty.gridx = 1;
		gbc_lblCounty.gridy = 10;
		add(lblCounty, gbc_lblCounty);
		
		GridBagConstraints gbc_cbCounty = new GridBagConstraints();
		cbCounty = new JComboBox<String>();
		gbc_cbCounty.gridwidth = 3;
		gbc_cbCounty.insets = new Insets(0, 0, 0, 5);
		gbc_cbCounty.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbCounty.gridx = 2;
		gbc_cbCounty.gridy = 10;
		add(cbCounty, gbc_cbCounty);

		initializeDBComponents();
	}
	
	private void refreshOtherCounties() {
		cbCounty.removeAllItems();
		cbCounty.addItem(null);
		String sOtherState = cbState.getSelectedCode();
		if( sOtherState != null ) {
			for( String sCounty : Counties.getCounties(sOtherState) ) {
				cbCounty.addItem(sCounty);
			}
		}
	}
	
	
	protected void checkZipcode(JTextField jtfZip) {
		if( jtfZip == null ) return;
		String sZip = jtfZip.getText().trim();
		if( sZip == null || sZip.trim().length() == 0 )
			return;
	      Pattern r = Pattern.compile("^\\d{5}(-?\\d{4})?$");

	      // Now create matcher object.
	      Matcher m = r.matcher(sZip);
	      if( !m.find() ) {
	    	  MessageDialog.showMessage(this.getWindowParent(), "Civet Error:", sZip + " is not a valid zipcode");
	    	  jtfZip.requestFocus();
	      }
	}
	
	private Window getWindowParent() {
		Window wP = null;
		Container p = getParent();
		while( p != null && !(p instanceof Window) ) {
			p = p.getParent();
		}
		return wP;
	}

	private void initializeDBComponents() {
		if( !Beans.isDesignTime() ) {
			try {
			
				cbState.setModel( new States() );
				cbState.setBlankDefault(true);
				cbState.refresh();
			}
			catch( Exception e ) {
				logger.error(e.getMessage() + "\nError loading values from lookup tables",e);
				MessageDialog.showMessage(getWindowParent(), "Civet Error: Database", "Error loading values from lookup tables" );
				e.printStackTrace();
			}
			
		}
	}
	
	void jtfThisPIN_focusLost(FocusEvent e) {
		boolean bFound = false;
		String sThisPremId = jtfPIN.getText();		
		if( sThisPremId == null || sThisPremId.trim().length() == 0 ) return;
		// Here we mimic the behavior of the PinField control.  We couldn't be both a SearchTextField
		// and a PinField.  Maybe should have used a decorator pattern instead of inheritance!
		String sUpperPin = sThisPremId.toUpperCase();
		boolean bValid = false;
		try {
			if( PremCheckSum.isValid(sUpperPin) ) {
				bValid = true;
			}
		} catch (Exception es) {
			//
		}
		if( !bValid ) {
			MessageDialog.showMessage(getWindowParent(), 
						"Civet: Premises ID Error", sThisPremId + " is not a valid Premises ID", MessageDialog.OK_ONLY);
			return;
		}
		else {
			sThisPremId = sUpperPin;
			jtfPIN.setText(sUpperPin);
		}
		if( bInSearch ) 
			return; 
		bInSearch = true;
		// don't overwrite existing content UNLESS sticky is on so the content may be 
		// from last entry.
		if( ckSticky.isSelected() || (
			(jtfBusiness.getText() == null || jtfBusiness.getText().trim().length() == 0) &&
			(jtfAddress.getText() == null || jtfAddress.getText().trim().length() == 0) &&
			(jtfCity.getText() == null || jtfCity.getText().trim().length() == 0) &&
			(jtfZip.getText() == null || jtfZip.getText().trim().length() == 0) ) ) {
			// Sort out whether we have a PIN or a LID
			String sStatePremisesId = null;
			String sFedPremisesId = null;
			if( sThisPremId != null && sThisPremId.trim().length() == 7 )
				sFedPremisesId = sThisPremId;
			else {
				sStatePremisesId = sThisPremId;
			}
			PremisesSearchDialog dlg = (PremisesSearchDialog)jtfPIN.getSearchDialog(); 
			if( dlg.exitOK() ) {
				jtfBusiness.setText(dlg.getSelectedPremName());
				jtfAddress.setText(dlg.getSelectedAddress());
				jtfCity.setText(dlg.getSelectedCity());
				String sStateCode = dlg.getSelectedStateCode();
				cbState.setSelectedItem(States.getState(sStateCode));
				cbCounty.setSelectedItem(dlg.getSelectedCounty());
				jtfZip.setText(dlg.getSelectedZipCode());
				bFound = true;
			}
			else {
				// Note, this route is broken until search logic gets refined.
				PremisesTableModel model;
				try {
					model = new UsaHerdsLookupPrems( sStatePremisesId, sFedPremisesId );
					if( model.getRowCount() == 1 ) {
						if( model.next() ) {
							jtfPIN.setText(sThisPremId);
							jtfBusiness.setText(model.getPremName());
							jtfAddress.setText(model.getAddress());
							jtfCity.setText(model.getCity());
							String sStateCode = model.getStateCode();
							cbState.setSelectedItem(States.getState(sStateCode));
							cbCounty.setSelectedItem(model.getCounty());
							jtfZip.setText(model.getZipCode());
							bFound = true;
						}
					}
					else {
						MessageDialog.showMessage(getWindowParent(), "Civet: Error Premises Not Found", 
								"Found " + model.getRowCount() + " premises for " + jtfPIN.getText());
					}
				} catch (WebServiceException e1) {
					MessageDialog.showMessage(getWindowParent(), "Civet: Error", "Web Service Failure\n" + e1.getMessage());
					logger.error("Web Service Failure", e1);
				}
			}
		}
		bInSearch = false;
		if( bFound ) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					myParent.participantComplete(ParticipantPanel.this);
				}
			});
		}
	}

}
