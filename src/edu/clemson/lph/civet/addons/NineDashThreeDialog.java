package edu.clemson.lph.civet.addons;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.clemson.lph.civet.AddAnimalsDialog;
import edu.clemson.lph.civet.AddOn;
import edu.clemson.lph.civet.AnimalIDListTableModel;
import edu.clemson.lph.civet.AnimalIDRecord;
import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetInbox;
import edu.clemson.lph.civet.SpeciesRecord;
import edu.clemson.lph.civet.lookup.Counties;
import edu.clemson.lph.civet.lookup.LookupFilesGenerator;
import edu.clemson.lph.civet.lookup.SpeciesLookup;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.controls.DBNumericField;
import edu.clemson.lph.controls.DateField;
import edu.clemson.lph.db.DatabaseConnectionFactory;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.ProgressDialog;
import edu.clemson.lph.utils.CountyUtils;
import edu.clemson.lph.utils.FileUtils;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("serial")
public class NineDashThreeDialog extends JFrame {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());

	DatabaseConnectionFactory factory = null;
	JTextField jtfCVINo;
	DateField jtfDate;
	JComboBox<String> cbSpecies = new JComboBox<String>();
	JComboBox<String> cbProduct = new JComboBox<String>();
	
	StdeCviXmlModel xmlModel = null;
	AnimalIDListTableModel idModel = null;
	JTable tblIDs;
	JList<String> lbSpecies;
	ArrayList<SpeciesRecord> aSpecies = new ArrayList<SpeciesRecord>();
	// maintaining this hashmap is redundant but done to match the signature 
	// of the AddAnimalsDialog borrowed from the main Civet code base.
	DefaultListModel<String> mSpListModel = new DefaultListModel<String>();
	ManualOrderFocusTraversalPolicy policy = new ManualOrderFocusTraversalPolicy();

	ParticipantPanel pConsignor = new ParticipantPanel("Consignor (Shipper)");
	ParticipantPanel pConsignee = new ParticipantPanel("Consignee (Purchaser)");
	
	JButton okButton;
	DBNumericField jtfNumber;
	/**
	 * Launch the application.  Just for testing.
	 */
	public static void main(String[] args) {
		try {
			PropertyConfigurator.configure("CivetConfig.txt");
			CivetConfig.checkAllConfig();
			logger.setLevel(CivetConfig.getLogLevel());
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			try {
				CivetConfig.validateHerdsCredentials();
			} catch (Exception e) {
				logger.error("Error running main program in event thread", e);
			}
			NineDashThreeDialog dialog = new NineDashThreeDialog(null, null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public NineDashThreeDialog(Window parent, DatabaseConnectionFactory factory) {
		this.factory = factory;
		xmlModel = new StdeCviXmlModel();
		idModel = new AnimalIDListTableModel( xmlModel );
		setBounds(100, 100, 700, 500);
		setTitle("Civet: NPIP 9-3 Entry Form");
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel pCertPane = new JPanel();
			GridLayout bCertLayout = new GridLayout();
			bCertLayout.setRows(1);
			bCertLayout.setColumns(2);
			bCertLayout.setHgap(5);
			bCertLayout.setVgap(5);
			pCertPane.setLayout(bCertLayout);
			
			JPanel pCertificate = new JPanel();
			pCertificate.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			getContentPane().add(pCertPane, BorderLayout.NORTH);
			pCertPane.add(pCertificate);
			pCertificate.setPreferredSize(new Dimension(650,200));
			GridBagLayout gbl_pCertificate = new GridBagLayout();
			gbl_pCertificate.columnWidths = new int[] {80, 120, 90, 55, 5};
			gbl_pCertificate.rowHeights = new int[] {30, 10, 22, 22, 22, 22, 33, 33, 0, 30};
			gbl_pCertificate.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
			gbl_pCertificate.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			pCertificate.setLayout(gbl_pCertificate);
			{
				JLabel label = new JLabel("");
				GridBagConstraints gbc_label = new GridBagConstraints();
				gbc_label.insets = new Insets(0, 0, 5, 0);
				gbc_label.gridx = 5;
				gbc_label.gridy = 1;
				pCertificate.add(label, gbc_label);
			}
			{
				JLabel lblNewLabel = new JLabel("CVI#:");
				lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
				gbc_lblNewLabel.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
				gbc_lblNewLabel.gridx = 0;
				gbc_lblNewLabel.gridy = 2;
				pCertificate.add(lblNewLabel, gbc_lblNewLabel);
			}
			{
				jtfCVINo = new JTextField();
				policy.addControl(jtfCVINo);
				GridBagConstraints gbc_jtfCVINo = new GridBagConstraints();
				gbc_jtfCVINo.gridwidth = 3;
				gbc_jtfCVINo.anchor = GridBagConstraints.NORTH;
				gbc_jtfCVINo.fill = GridBagConstraints.HORIZONTAL;
				gbc_jtfCVINo.insets = new Insets(0, 0, 5, 5);
				gbc_jtfCVINo.gridx = 1;
				gbc_jtfCVINo.gridy = 2;
				pCertificate.add(jtfCVINo, gbc_jtfCVINo);
				jtfCVINo.setColumns(10);
			}
			{
				JLabel lblNewLabel_1 = new JLabel("Date:");
				lblNewLabel_1.setHorizontalAlignment(SwingConstants.RIGHT);
				GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
				gbc_lblNewLabel_1.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
				gbc_lblNewLabel_1.gridx = 0;
				gbc_lblNewLabel_1.gridy = 3;
				pCertificate.add(lblNewLabel_1, gbc_lblNewLabel_1);
			}
			{
				jtfDate = new DateField();
				policy.addControl(jtfDate);
				GridBagConstraints gbc_jtfDate = new GridBagConstraints();
				gbc_jtfDate.fill = GridBagConstraints.HORIZONTAL;
				gbc_jtfDate.anchor = GridBagConstraints.NORTHWEST;
				gbc_jtfDate.insets = new Insets(0, 0, 5, 5);
				gbc_jtfDate.gridx = 1;
				gbc_jtfDate.gridy = 3;
				pCertificate.add(jtfDate, gbc_jtfDate);
				jtfDate.setColumns(10);
			}
			{
				JLabel lblProduct = new JLabel("Product:");
				lblProduct.setHorizontalAlignment(SwingConstants.RIGHT);
				GridBagConstraints gbc_lblProduct = new GridBagConstraints();
				gbc_lblProduct.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblProduct.insets = new Insets(0, 0, 5, 5);
				gbc_lblProduct.gridx = 0;
				gbc_lblProduct.gridy = 4;
				pCertificate.add(lblProduct, gbc_lblProduct);
			}
			{
				
				GridBagConstraints gbc_cbProduct = new GridBagConstraints();
				gbc_cbProduct.insets = new Insets(0, 0, 5, 5);
				gbc_cbProduct.gridwidth = 3;
				gbc_cbProduct.anchor = GridBagConstraints.NORTH;
				gbc_cbProduct.fill = GridBagConstraints.HORIZONTAL;
				gbc_cbProduct.gridx = 1;
				gbc_cbProduct.gridy = 4;
				cbProduct.addItem("");
				cbProduct.addItem("Live Animals");
				cbProduct.addItem("Eggs");
				cbProduct.setSelectedItem("Live Animals");
				pCertificate.add(cbProduct, gbc_cbProduct);
				policy.addControl(cbProduct);
			}
			{
				JLabel lblNewLabel_2 = new JLabel("Species:");
				lblNewLabel_2.setHorizontalAlignment(SwingConstants.RIGHT);
				GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
				gbc_lblNewLabel_2.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
				gbc_lblNewLabel_2.gridx = 0;
				gbc_lblNewLabel_2.gridy = 5;
				pCertificate.add(lblNewLabel_2, gbc_lblNewLabel_2);
			}
			{
				
				GridBagConstraints gbc_cbSpecies = new GridBagConstraints();
				gbc_cbSpecies.anchor = GridBagConstraints.NORTH;
				gbc_cbSpecies.fill = GridBagConstraints.HORIZONTAL;
				gbc_cbSpecies.insets = new Insets(0, 0, 5, 5);
				gbc_cbSpecies.gridx = 1;
				gbc_cbSpecies.gridy = 5;
				cbSpecies.addItem("");
				cbSpecies.addItem("Chickens");
				cbSpecies.addItem("Turkeys");
				cbSpecies.addItem("Pigeon");
				cbSpecies.addItem("Duck");
				cbSpecies.addItem("Poultry");
				cbSpecies.setSelectedItem("");
				pCertificate.add(cbSpecies, gbc_cbSpecies);
				policy.addControl(cbSpecies);
			}
			{
				JLabel lblNumber = new JLabel("Number:");
				
				GridBagConstraints gbc_lblNumber = new GridBagConstraints();
				gbc_lblNumber.anchor = GridBagConstraints.EAST;
				gbc_lblNumber.insets = new Insets(0, 0, 5, 5);
				gbc_lblNumber.gridx = 2;
				gbc_lblNumber.gridy = 5;
				pCertificate.add(lblNumber, gbc_lblNumber);
			}
			{
				jtfNumber = new DBNumericField();
				jtfNumber.addFocusListener( new FocusListener() {
					@Override
					public void focusGained(FocusEvent arg0) {
					}
					@Override
					public void focusLost(FocusEvent arg0) {
						updateSpecies();
					}
				});
				GridBagConstraints gbc_textField = new GridBagConstraints();
				gbc_textField.insets = new Insets(0, 0, 5, 5);
				gbc_textField.fill = GridBagConstraints.HORIZONTAL;
				gbc_textField.gridx = 3;
				gbc_textField.gridy = 5;
				pCertificate.add(jtfNumber, gbc_textField);
				jtfNumber.setColumns(5);
				policy.addControl(jtfNumber);
			}
			{
				JScrollPane spSpecies = new JScrollPane();
				
				lbSpecies = new JList<String>(mSpListModel);
				spSpecies.setViewportView(lbSpecies);
				GridBagConstraints gbc_scrollPane = new GridBagConstraints();
				gbc_scrollPane.gridheight = 3;
				gbc_scrollPane.gridwidth = 3;
				gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
				gbc_scrollPane.fill = GridBagConstraints.BOTH;
				gbc_scrollPane.gridx = 1;
				gbc_scrollPane.gridy = 6;
				pCertificate.add(spSpecies, gbc_scrollPane);
			}
			
			//			{
			//				JButton btnAddSpecies = new JButton("Add");
			//				btnAddSpecies.addActionListener( new ActionListener() {
			//					@Override
			//					public void actionPerformed(ActionEvent e) {
			//						String species = (String)cbSpecies.getSelectedItem();
			//						mSpListModel.addElement(species);
			//					}
			//					
			//				});
			//				btnAddSpecies.setVerticalAlignment(SwingConstants.TOP);
			//				GridBagConstraints gbc_btnAddSpecies = new GridBagConstraints();
			//				gbc_btnAddSpecies.anchor = GridBagConstraints.EAST;
			//				gbc_btnAddSpecies.insets = new Insets(0, 0, 5, 5);
			//				gbc_btnAddSpecies.gridx = 0;
			//				gbc_btnAddSpecies.gridy = 6;
			//				pCertificate.add(btnAddSpecies, gbc_btnAddSpecies);
			//				policy.addControl(btnAddSpecies);
			//			}
			{
				JButton bRemove = new JButton("Remove");
				bRemove.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int iSelected = lbSpecies.getSelectedIndex();
						if( iSelected >= 0 )
							mSpListModel.remove(iSelected);
					}

				});
				GridBagConstraints gbc_bRemove = new GridBagConstraints();
				gbc_bRemove.anchor = GridBagConstraints.EAST;
				gbc_bRemove.insets = new Insets(0, 0, 15, 5);
				gbc_bRemove.gridx = 0;
				gbc_bRemove.gridy = 8;
				pCertificate.add(bRemove, gbc_bRemove);
			}
			JPanel pIDs = new JPanel();
			{
				JLabel lblNewLabel_3 = new JLabel("IDs:");
				lblNewLabel_3.setBounds(80, 39, 20, 14);
				lblNewLabel_3.setHorizontalAlignment(SwingConstants.RIGHT);
				pIDs.add(lblNewLabel_3);
			}
			{
				JButton bAddIDs = new JButton("Add IDs");
				policy.addControl(bAddIDs);
				bAddIDs.setBounds(27, 76, 73, 23);
				pIDs.add(bAddIDs);
				bAddIDs.addActionListener( new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						addIDs();
					}
					
				});
				
			}
			{
				pIDs.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
				pIDs.setLayout(null);
				pCertPane.add(pIDs);
				JScrollPane spIDs = new JScrollPane();
				spIDs.setBounds(110, 11, 223, 170);
				pIDs.add(spIDs);
				tblIDs = new JTable();
				tblIDs.setModel(idModel);
				tblIDs.setPreferredSize(new Dimension(200, 200));
				TableColumnModel tcm = tblIDs.getColumnModel();
				tcm.removeColumn(tcm.getColumn(0));
				spIDs.setViewportView(tblIDs);
			}
			{
				JPanel pParticipants = new JPanel();
				GridLayout gl_pParticipants = new GridLayout();
				gl_pParticipants.setHgap(5);
				gl_pParticipants.setVgap(5);
				gl_pParticipants.setColumns(2);
				gl_pParticipants.setRows(0);
				pParticipants.setLayout(gl_pParticipants);
				getContentPane().add(pParticipants, BorderLayout.CENTER);
				{
					pConsignor.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
					pConsignor.setParent(this);
					pParticipants.add(pConsignor);
					policy.addControl(pConsignor.jtfPIN);
					policy.addControl(pConsignor.jtfBusiness);
					policy.addControl(pConsignor.jtfName);
					policy.addControl(pConsignor.jtfAddress);
					policy.addControl(pConsignor.jtfCity);
					policy.addControl(pConsignor.cbState);
					policy.addControl(pConsignor.jtfZip);
					policy.addControl(pConsignor.cbCounty);
				}
				{
					pConsignee.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
					pConsignee.setParent(this);
					pParticipants.add(pConsignee);
					policy.addControl(pConsignee.jtfPIN);
					policy.addControl(pConsignee.jtfBusiness);
					policy.addControl(pConsignee.jtfName);
					policy.addControl(pConsignee.jtfAddress);
					policy.addControl(pConsignee.jtfCity);
					policy.addControl(pConsignee.cbState);
					policy.addControl(pConsignee.jtfZip);
					policy.addControl(pConsignee.cbCounty);
				}
			}
			{
				JPanel buttonPane = new JPanel();
				buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
				getContentPane().add(buttonPane, BorderLayout.SOUTH);
				{
					okButton = new JButton("Save");
					buttonPane.add(okButton);
					getRootPane().setDefaultButton(okButton);
					okButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							doSave();
						}
					});
				}
				{
					JButton cancelButton = new JButton("Cancel");
					cancelButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							setVisible(false);
						}
					});
					buttonPane.add(cancelButton);
				}
			}
			this.setFocusTraversalPolicy(policy);
		}
		setAllFocus();
	}
	
	public void participantComplete( ParticipantPanel participant ) {
		if( participant == pConsignor ) {
			pConsignee.jtfPIN.requestFocus();
		}
		else {
			okButton.requestFocus();
		}
	}
	 
	private void doSave() {
		// Gather values and save.
		// In case one sp and not in list yet.
		updateSpecies();
		java.util.Date issueDate = jtfDate.getDate();
		if( issueDate == null ) {
			MessageDialog.showMessage(this, "Civet Error", "Date Issued Is Required");
			jtfDate.requestFocus();
			return;
		}
		ArrayList<AnimalIDRecord> aAnimalIDs = idModel.cloneRows();
		SubmitNineDashThreeThread submitThread = 
				new SubmitNineDashThreeThread( factory, (Window)this,
				 xmlModel,
				 jtfCVINo.getText(), 
				 jtfDate.getDate(), 
				 (String)cbProduct.getSelectedItem(), 
				 aSpecies, 
				 aAnimalIDs, 
				 pConsignor.cbState.getSelectedCode(), 
				 pConsignor.jtfPIN.getText(), 
				 pConsignor.jtfBusiness.getText(),
				 pConsignor.jtfName.getText(), 
				 pConsignor.jtfAddress.getText(), 
				 pConsignor.jtfCity.getText(), 
				 (String)pConsignor.cbCounty.getSelectedItem(), 
				 pConsignor.jtfZip.getText(), 
				 pConsignee.cbState.getSelectedCode(), 
				 pConsignee.jtfPIN.getText(), 
				 pConsignee.jtfBusiness.getText(),
				 pConsignee.jtfName.getText(), 
				 pConsignee.jtfAddress.getText(), 
				 pConsignee.jtfCity.getText(), 
				 (String)pConsignee.cbCounty.getSelectedItem(), 
				 pConsignee.jtfZip.getText() );
		submitThread.start();
		clear();
	}
	
	private void clear() {
		jtfCVINo.setText("");
		jtfDate.setText("");
		cbProduct.setSelectedItem("Live Animals");
		cbSpecies.setSelectedItem(null);
		jtfNumber.setText("");
		aSpecies = new ArrayList<SpeciesRecord>();
		mSpListModel.clear();
		xmlModel = new StdeCviXmlModel();
		idModel = new AnimalIDListTableModel( xmlModel );
		if( !pConsignor.ckSticky.isSelected() )
		{
			pConsignor.jtfPIN.setText("");
			pConsignor.jtfBusiness.setText("");
			pConsignor.jtfName.setText("");
			pConsignor.jtfAddress.setText("");
			pConsignor.jtfCity.setText("");
			pConsignor.cbState.setSelectedItem(null);
			pConsignor.jtfZip.setText("");
			pConsignor.cbCounty.setSelectedItem(null);
		}
		if( !pConsignee.ckSticky.isSelected() )
		{
			pConsignee.jtfPIN.setText("");
			pConsignee.jtfBusiness.setText("");
			pConsignee.jtfName.setText("");
			pConsignee.jtfAddress.setText("");
			pConsignee.jtfCity.setText("");
			pConsignee.cbState.setSelectedItem(null);
			pConsignee.jtfZip.setText("");
			pConsignee.cbCounty.setSelectedItem(null);
		}
		setAllFocus();
		jtfCVINo.requestFocus();
	}

	protected void addIDs() {
		updateSpecies();
		AddAnimalsDialog dlg = new AddAnimalsDialog( aSpecies, idModel );
		dlg.setVisible(true);
	}

	private void updateSpecies() {
		String sSpecies = (String)cbSpecies.getSelectedItem();
		if( sSpecies == null || sSpecies.trim().length() == 0 ) {
			if( jtfNumber.getText().trim().length() != 0 ) {
				logger.error("Number added without species");
			}
 		}
		String sSpCode = SpeciesLookup.getCodeForName(sSpecies);
		if( sSpCode == null || sSpCode.equals("ERROR") ) {
			logger.error((String)cbSpecies.getSelectedItem() + " resulted in ERROR on lookup");
			return;
		}
		String sNumber = jtfNumber.getText();
		int iNumber = 1;
		try {
			iNumber = Integer.parseInt(sNumber);
		} catch( NumberFormatException nfe ) {
			logger.error(nfe);
		}
		if( !mSpListModel.contains(sSpecies)) {
			mSpListModel.addElement(sSpecies);
		}
		SpeciesRecord srThis = new SpeciesRecord(sSpCode, iNumber);
		if( !aSpecies.contains(srThis) ) {
			aSpecies.add(srThis);
		}
		else {
			SpeciesRecord rExisting = aSpecies.get(aSpecies.indexOf(srThis) );
			rExisting.iNumber = iNumber;
		}
	}
	
	/**
	 * This is used by setAllFocus to make each field select the entire text 
	 * when tabbed into so it can be easily overwritten.
	 * @param e
	 */
	private void selectAll(FocusEvent e) {
		Component c = e.getComponent();
		if( c instanceof JTextComponent ) {
			((JTextComponent)c).selectAll();
		}
	}

	/** 
	 * As a control is entered, select all.  
	 */
	private void setAllFocus() {
		jtfCVINo.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				selectAll(e);
			}
		});
		jtfDate.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				selectAll(e);
			}
		});
		pConsignor.setAllFocus();
		pConsignee.setAllFocus();
	}
}
