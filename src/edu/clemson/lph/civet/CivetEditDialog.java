package edu.clemson.lph.civet;
/*
Copyright 2014 - 2019 Michael K Martin

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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.jpedal.PdfDecoder;
import org.jpedal.objects.PdfPageData;

import edu.clemson.lph.civet.files.SourceFileException;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.controls.DBComboBox;
import edu.clemson.lph.controls.DBNumericField;
import edu.clemson.lph.controls.DBSearchComboBox;
import edu.clemson.lph.controls.DateField;
import edu.clemson.lph.controls.PhoneField;
import edu.clemson.lph.controls.SearchTextField;
import edu.clemson.lph.pdfgen.PDFViewer;

import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * This is the View part of MVC for the VERY complex Edit dialog that makes up 
 * almost all the functionality of Civet.  It should, after refactoring, contain
 * only data needed for the visual representation of the data as instructed by
 * the CivetEditDialogController (and OpenFilesController? or is that via the 
 * Dialog controller?)
 */
@SuppressWarnings("serial")
public final class CivetEditDialog extends JFrame {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private Window parent;
	private CivetEditDialog dialogParent;
	static final String sFileCopyAddress = CivetConfig.getEmailCopyTo();
	public static final int PDF_MODE = 0;
	public static final int XML_MODE = 1;
	public static final int VIEW_MODE = 2;

	PDFViewer viewer;
//	float fScale = 1.0f;
//	int iRotation = 180;  // 0 appears to be upside down relative to Acrobat ?!
	boolean bImport = true;
	int iMode;
	int iFileNo = 0;
	int iPageNo = 1;

	boolean bMultiSpecies = false;

	/** GUI components that need to be read or written outside of initialization **/
	// NOTE Use of default visibility for controls that need to be accessed by the 
	// invokeLater() methods of secondary threads.
	ImageIcon appIcon;
	CountersPanel pCounters;
	//	PinField jtfOtherPIN;
	JComboBox<String> cbOtherCounty;
	JTextField jtfThisState;
	SearchTextField jtfThisPIN;
	JTextField jtfPhone;
	JTextField jtfAddress;
	JTextField jtfThisCity;
	JComboBox<String> cbThisCounty;  // Hidden
	JTextField jtfZip;
	DBNumericField jtfNumber;
	DateField jtfDateInspected;
	JTextField jtfCVINo;
	ImageIcon iconPDF = null;
	ImageIcon iconXML = null;
	ImageIcon iconMAIL = null;
	Icon iconPDFPage;
	DBComboBox cbOtherState;
	DBComboBox cbSpecies;
	JRadioButton rbImport;
	JRadioButton rbExport;
	JRadioButton rbInState;
	JButton bAddSpecies;
	JButton bSave;
	JButton bError;
	JLabel bMode;
	JPanel pSpacer;
	JButton bPDFView;
	JButton bRotate;
	JButton bBigger;
	JButton bSmaller;
	JScrollPane display;
	JLabel lMultipleSpecies;
	ButtonGroup rbGroup;
	JPanel topBar;
	JLabel lError;
	DBSearchComboBox cbIssuedBy;
	JLabel lIssuedBy;
	JTextField jtfIssuedBy;
	JTextField jtfOtherCity;
	DateField jtfDateReceived;
	JCheckBox ckSticky;
	JPanel altDisplay;
	JPanel pView;
	JLabel lblCviNumber;
	JLabel lblDateInspected;
	JCheckBox ckAllVets;
	JTextField jtfOtherName;
	JTextField jtfThisName;
	DBComboBox cbPurpose;
	JTextField jtfOtherAddress;
	JTextField jtfOtherZip;
	TitledBorder tbOtherState;
	TitledBorder tbThisState;
	TitledBorder tbCVIDetails;
	JPanel pOtherState;
	JPanel pThisState;
	JPanel pButtons;
	TitledBorder tbButtons;
	JPanel pEdit;
	JButton bAddToLast;
	JButton bGotoPage;
	JButton bAddIDs;
	JButton bEditLast;
	JButton bPDFViewFile;
	boolean bPreview;
	JLabel lThisCity;
	JMenuItem mntmSave;
	JMenuItem mntmClose;
	JMenuItem mntmMinimizeAll;
	JMenuItem mntmRefresh;
	CivetEditDialogController controller;

	/**
	 * construct an empty pdf viewer and pop up the open window
	 * @throws SourceFileException 
	 * @wbp.parser.constructor
	 */
	public CivetEditDialog( Window parent, ArrayList<File> files ) throws SourceFileException{
		if( parent instanceof CivetEditDialog ) {
			this.dialogParent = (CivetEditDialog)parent;
			this.parent = dialogParent.parent;
		}
		else {
			this.parent = parent;
			this.dialogParent = null;
		}
		initializeDisplay();
		controller = new CivetEditDialogController( this, files);
		controller.openFiles();
	}
	
	public void setViewer( PDFViewer viewer ) {
		this.viewer = viewer;
		// This is the magic.  We simply put the pdfDecoder in the viewport of the scroll pane.
		display.setViewportView(viewer.getPdfDecoder());
	}
	
	public CivetEditDialog getDialogParent() {
		return dialogParent;
	}
	
	public CivetInbox getInboxParent() {
		CivetInbox ibRet = null;
		if( parent instanceof CivetInbox ) {
			ibRet = (CivetInbox)parent;
		}
		if( parent instanceof CivetEditDialog ) {
			ibRet = (CivetInbox)((CivetEditDialog)parent).getInboxParent();
		}
		return ibRet;
	}
	
	public void setController( CivetEditDialogController c ) {
		this.controller = c;
	}
	
	public CivetEditDialogController getDialogController() {
		return controller;
	}
	
	/**
	 * Called from constructor to isolate all the graphical layout verbosity
	 */
	private void initializeDisplay() {
		appIcon = new ImageIcon(getClass().getResource("res/civet32.png"));
		this.setIconImage(appIcon.getImage());
		setupMenus();
		getContentPane().setLayout(new BorderLayout(0, 0));

		setupTopBar();
		setupEditPanel();
		setupViewPanel();
	}
	
	private void setupMenus() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);

		mntmClose = new JMenuItem("Close");
		mnFile.add(mntmClose);

		JMenu mnView = new JMenu("View");
		menuBar.add(mnView);

		mntmMinimizeAll = new JMenuItem("Minimize All");
		mnView.add(mntmMinimizeAll);
		
		mntmRefresh = new JMenuItem("Refresh Display");
		mnView.add(mntmRefresh);

	}
	
	private void setupTopBar() {
		topBar = new JPanel();
		getContentPane().add(topBar, BorderLayout.NORTH);

		iconPDF = new ImageIcon(getClass().getResource("/edu/clemson/lph/civet/res/pdf.gif"));
		iconXML = new ImageIcon(getClass().getResource("/edu/clemson/lph/civet/res/stdXml.gif"));
		iconMAIL = new ImageIcon(getClass().getResource("/edu/clemson/lph/civet/res/mail_find.png"));
		bMode = new JLabel();
		bMode.setIcon( iconPDF );
		bMode.setDoubleBuffered(false);
		bMode.setToolTipText("Shows Mode: PDF File or XML Record");
		bMode.setText("");
		topBar.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		topBar.add(bMode, null);
		pSpacer = new JPanel();
		topBar.add(pSpacer, null);
		bBigger = new JButton();
		bBigger.setFont(new java.awt.Font("Dialog", 1, 14));
		bBigger.setText("+");
		topBar.add(bBigger);
		topBar.add(new JPanel());
		bSmaller = new JButton();
		bSmaller.setFont(new java.awt.Font("Dialog", 1, 14));
		bSmaller.setText("-");
		topBar.add(bSmaller);
		topBar.add(new JPanel());
		bRotate = new JButton();
		bRotate.setIcon( new ImageIcon(getClass().getResource("/edu/clemson/lph/civet/res/rotate.gif")));
		bRotate.setToolTipText("Rotate Viewer");
		topBar.add(bRotate);
		topBar.add(new JPanel());
		bPDFView = new JButton();
		bPDFView.setFont(new java.awt.Font("Dialog", 1, 14));
		//    bPDFView.setText("View Page In Acrobat");  
		iconPDFPage = new ImageIcon(getClass().getResource("/edu/clemson/lph/civet/res/pdfPage.gif"));
		bPDFView.setIcon(iconPDFPage);
		bPDFView.setToolTipText("Open Page in Acrobat");
		topBar.add(bPDFView);
		
		bPDFViewFile = new JButton();
		bPDFViewFile.setFont(new java.awt.Font("Dialog", 1, 14));
		//    bPDFView.setText("View File In Acrobat");  
		bPDFViewFile.setIcon(iconPDF);
		bPDFViewFile.setToolTipText("Open File in Acrobat");
		topBar.add(bPDFViewFile);
		
		pCounters = new CountersPanel( this );
		topBar.add(pCounters);
		pCounters.setVisible(true);

		bGotoPage = new JButton("Goto Page");
		topBar.add(bGotoPage);

		ckSticky = new JCheckBox("All Values Sticky");
		ckSticky.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));
		ckSticky.setToolTipText("Check to make all values remain from one record to the next");
		topBar.add(ckSticky);
		
		bEditLast = new JButton("Edit Last");
		topBar.add(bEditLast);
		
	}

	private void setupViewPanel() {	
		//ensure non-embedded font map to sensible replacements
		//    PdfDecoder.setFontReplacements(pdfDecoder);
		pView = new JPanel();
		pView.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		getContentPane().add(pView, BorderLayout.CENTER);
		pView.setLayout(new BorderLayout(0, 0));
		display = new JScrollPane();
		display.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		display.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		pView.add(display, BorderLayout.CENTER);

		altDisplay = new JPanel();
		altDisplay.setLayout(new BorderLayout(0, 0));
		altDisplay.setBorder(new EmptyBorder(20,20,20,20));
		JLabel lNoPDF = new JLabel("No PDF Attached");

		lNoPDF.setFont(new Font("Tahoma", Font.BOLD, 16));
		altDisplay.add(lNoPDF, BorderLayout.CENTER);
		altDisplay.setVisible(false);
	}
	
	private void setupEditPanel() {
		pEdit = new JPanel();
		pEdit.setLayout(null);
		pEdit.setBorder(null);
		if( CivetConfig.isSmallScreen() ) {
			pEdit.setPreferredSize(new Dimension(290,700));
			JScrollPane jspEdit = new JScrollPane();
			jspEdit.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			jspEdit.setViewportView(pEdit);
			getContentPane().add(jspEdit, BorderLayout.WEST);
		}
		else {
			pEdit.setPreferredSize(new Dimension(275,700));
			getContentPane().add(pEdit, BorderLayout.WEST);
		}

		rbImport = new JRadioButton("Import");
		rbImport.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));

		rbImport.setBounds(10, 7, 65, 23);
		rbExport= new JRadioButton("Export");
		rbExport.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));
		rbExport.setBounds(98, 7, 73, 23);
		
		rbInState = new JRadioButton("In State");
		rbInState.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));
		rbInState.setBounds(184, 7, 81, 23);
		rbGroup = new ButtonGroup();
		rbGroup.add(rbImport);
		rbGroup.add(rbExport);
		rbGroup.add(rbInState);

		pEdit.add(rbImport);
		pEdit.add(rbInState);
		pEdit.add(rbExport);
		
		setupOtherStatePanel( pEdit );
		setupThisStatePanel( pEdit );
		setupCVIDetailsPanel( pEdit );
		setupButtonPanel( pEdit );
	}
	
	private void setupOtherStatePanel(JPanel pEdit ) {
		pOtherState = new JPanel();
		tbOtherState = new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "From:", TitledBorder.LEADING, TitledBorder.TOP, null, null);
		pOtherState.setBorder(tbOtherState);
		pOtherState.setBounds(0, 37, 275, 144);
		pEdit.add(pOtherState);

		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] {90, 117, 0};
		gbl_panel.rowHeights = new int[] {12, 12, 12, 12, 12, 12, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pOtherState.setLayout(gbl_panel);

		JLabel lblState = new JLabel("State:");
		GridBagConstraints gbc_lblState = new GridBagConstraints();
		gbc_lblState.fill = GridBagConstraints.BOTH;
		gbc_lblState.insets = new Insets(0, 0, 0, 10);
		gbc_lblState.gridx = 0;
		gbc_lblState.gridy = 0;
		pOtherState.add(lblState, gbc_lblState);
		lblState.setHorizontalAlignment(SwingConstants.RIGHT);
		lblState.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 11));

		cbOtherState = new DBComboBox();
		cbOtherState.setMaximumSize( new Dimension(cbOtherState.getWidth()-60, cbOtherState.getHeight()) );
		GridBagConstraints gbc_cbOtherState = new GridBagConstraints();
		gbc_cbOtherState.fill = GridBagConstraints.BOTH;
		gbc_cbOtherState.insets = new Insets(0, 0, 0, 0);
		gbc_cbOtherState.gridx = 1;
		gbc_cbOtherState.gridy = 0;	
		pOtherState.add(cbOtherState, gbc_cbOtherState);

		JLabel lOtherName = new JLabel("Name:");
		GridBagConstraints gbc_lOtherName = new GridBagConstraints();
		gbc_lOtherName.fill = GridBagConstraints.BOTH;
		gbc_lOtherName.insets = new Insets(0, 0, 0, 10);
		gbc_lOtherName.gridx = 0;
		gbc_lOtherName.gridy = 1;
		pOtherState.add(lOtherName, gbc_lOtherName);
		lOtherName.setHorizontalAlignment(SwingConstants.RIGHT);
		lOtherName.setFont(new Font("Tahoma", Font.PLAIN, 11));

		jtfOtherName = new JTextField();
		GridBagConstraints gbc_jtfOtherName = new GridBagConstraints();
		gbc_jtfOtherName.fill = GridBagConstraints.BOTH;
		gbc_jtfOtherName.insets = new Insets(0, 0, 0, 0);
		gbc_jtfOtherName.gridx = 1;
		gbc_jtfOtherName.gridy = 1;
		pOtherState.add(jtfOtherName, gbc_jtfOtherName);

		JLabel lOtherAddress = new JLabel("Address:");
		GridBagConstraints gbc_lOtherAddress = new GridBagConstraints();
		gbc_lOtherAddress.fill = GridBagConstraints.BOTH;
		gbc_lOtherAddress.insets = new Insets(0, 0, 0, 10);
		gbc_lOtherAddress.gridx = 0;
		gbc_lOtherAddress.gridy = 2;
		pOtherState.add(lOtherAddress, gbc_lOtherAddress);
		lOtherAddress.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));
		lOtherAddress.setHorizontalAlignment(SwingConstants.RIGHT);

		jtfOtherAddress = new JTextField();
		GridBagConstraints gbc_jtfOtherAddress = new GridBagConstraints();
		gbc_jtfOtherAddress.fill = GridBagConstraints.BOTH;
		gbc_jtfOtherAddress.insets = new Insets(0, 0, 0, 0);
		gbc_jtfOtherAddress.gridx = 1;
		gbc_jtfOtherAddress.gridy = 2;
		pOtherState.add(jtfOtherAddress, gbc_jtfOtherAddress);

		JLabel lOtherCity = new JLabel("City:");
		GridBagConstraints gbc_lOtherCity = new GridBagConstraints();
		gbc_lOtherCity.fill = GridBagConstraints.BOTH;
		gbc_lOtherCity.insets = new Insets(0, 0, 0, 10);
		gbc_lOtherCity.gridx = 0;
		gbc_lOtherCity.gridy = 3;
		pOtherState.add(lOtherCity, gbc_lOtherCity);
		lOtherCity.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));
		lOtherCity.setHorizontalAlignment(SwingConstants.RIGHT);

		jtfOtherCity = new JTextField();
		GridBagConstraints gbc_jtfOtherCity = new GridBagConstraints();
		gbc_jtfOtherCity.fill = GridBagConstraints.BOTH;
		gbc_jtfOtherCity.insets = new Insets(0, 0, 0, 0);
		gbc_jtfOtherCity.gridx = 1;
		gbc_jtfOtherCity.gridy = 3;
		pOtherState.add(jtfOtherCity, gbc_jtfOtherCity);

		JLabel lOtherZipCode = new JLabel("ZipCode:");
		GridBagConstraints gbc_lOtherZipCode = new GridBagConstraints();
		gbc_lOtherZipCode.fill = GridBagConstraints.BOTH;
		gbc_lOtherZipCode.insets = new Insets(0, 0, 0, 10);
		gbc_lOtherZipCode.gridx = 0;
		gbc_lOtherZipCode.gridy = 4;
		pOtherState.add(lOtherZipCode, gbc_lOtherZipCode);
		lOtherZipCode.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));
		lOtherZipCode.setHorizontalAlignment(SwingConstants.RIGHT);

		jtfOtherZip = new JTextField();
		GridBagConstraints gbc_jtfOtherZipCode = new GridBagConstraints();
		gbc_jtfOtherZipCode.fill = GridBagConstraints.BOTH;
		gbc_jtfOtherZipCode.insets = new Insets(0, 0, 0, 0);
		gbc_jtfOtherZipCode.gridx = 1;
		gbc_jtfOtherZipCode.gridy = 4;
		pOtherState.add(jtfOtherZip, gbc_jtfOtherZipCode);
	
		JLabel lblOtherCounty = new JLabel("County:");
		GridBagConstraints gbc_lblOtherCounty = new GridBagConstraints();
		gbc_lblOtherCounty.fill = GridBagConstraints.BOTH;
		gbc_lblOtherCounty.insets = new Insets(0, 0, 0, 10);
		gbc_lblOtherCounty.gridx = 0;
		gbc_lblOtherCounty.gridy = 5;
		pOtherState.add(lblOtherCounty, gbc_lblOtherCounty);
		lblOtherCounty.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));
		lblOtherCounty.setHorizontalAlignment(SwingConstants.RIGHT);

		cbOtherCounty = new JComboBox<String>();
		GridBagConstraints gbc_jtfOtherCounty = new GridBagConstraints();
		gbc_jtfOtherCounty.fill = GridBagConstraints.BOTH;
		gbc_jtfOtherCounty.gridx = 1;
		gbc_jtfOtherCounty.gridy = 5;
		pOtherState.add(cbOtherCounty, gbc_jtfOtherCounty);
	}

	private void setupThisStatePanel(JPanel pEdit ) {
		pThisState = new JPanel();
		tbThisState = new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "To:", TitledBorder.LEADING, TitledBorder.TOP, null, null);
		pThisState.setBorder(tbThisState);
		pThisState.setBounds(0, 181, 275, 162);
		pEdit.add(pThisState);

		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] {90, 125, 0};
		gbl_panel.rowHeights = new int[] {12, 12, 12, 12, 12, 12, 12, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pThisState.setLayout(gbl_panel);
		
		JLabel lblState = new JLabel("State:");
		GridBagConstraints gbc_lblState = new GridBagConstraints();
		gbc_lblState.fill = GridBagConstraints.BOTH;
		gbc_lblState.insets = new Insets(0, 0, 0, 10);
		gbc_lblState.gridx = 0;
		gbc_lblState.gridy = 0;
		pThisState.add(lblState, gbc_lblState);
		lblState.setHorizontalAlignment(SwingConstants.RIGHT);
		lblState.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 11));

		jtfThisState = new JTextField();
		jtfThisState.setText(CivetConfig.getHomeStateAbbr());
		jtfThisState.setEditable(false);
		GridBagConstraints gbc_cbThisState = new GridBagConstraints();
		gbc_cbThisState.fill = GridBagConstraints.BOTH;
		gbc_cbThisState.insets = new Insets(0, 0, 0, 0);
		gbc_cbThisState.gridx = 1;
		gbc_cbThisState.gridy = 0;
		pThisState.add(jtfThisState, gbc_cbThisState);

		JLabel lThisPhone = new JLabel("Phone:");
		GridBagConstraints gbc_lThisPhone = new GridBagConstraints();
		gbc_lThisPhone.fill = GridBagConstraints.BOTH;
		gbc_lThisPhone.insets = new Insets(0, 0, 0, 10);
		gbc_lThisPhone.gridx = 0;
		gbc_lThisPhone.gridy = 1;
		pThisState.add(lThisPhone, gbc_lThisPhone);
		lThisPhone.setHorizontalAlignment(SwingConstants.RIGHT);
		lThisPhone.setFont(new Font("Tahoma", Font.PLAIN, 11));

		jtfPhone = new PhoneField(true);
		GridBagConstraints gbc_jtfThisPhone = new GridBagConstraints();
		gbc_jtfThisPhone.fill = GridBagConstraints.BOTH;
		gbc_jtfThisPhone.insets = new Insets(0, 0, 0, 0);
		gbc_jtfThisPhone.gridx = 1;
		gbc_jtfThisPhone.gridy = 1;
		pThisState.add(jtfPhone, gbc_jtfThisPhone);
		
		JLabel lblPin = new JLabel("PIN:");
		GridBagConstraints gbc_lblPin = new GridBagConstraints();
		gbc_lblPin.fill = GridBagConstraints.BOTH;
		gbc_lblPin.insets = new Insets(0, 0, 0, 10);
		gbc_lblPin.gridx = 0;
		gbc_lblPin.gridy = 2;
		pThisState.add(lblPin, gbc_lblPin);
		lblPin.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));
		lblPin.setHorizontalAlignment(SwingConstants.RIGHT);

		jtfThisPIN = new SearchTextField();
		GridBagConstraints gbc_jtfThisPIN = new GridBagConstraints();
		gbc_jtfThisPIN.fill = GridBagConstraints.BOTH;
		gbc_jtfThisPIN.gridx = 1;
		gbc_jtfThisPIN.gridy = 2;
		pThisState.add(jtfThisPIN, gbc_jtfThisPIN);

		JLabel lThisName = new JLabel("Name:");
		GridBagConstraints gbc_lThisName = new GridBagConstraints();
		gbc_lThisName.fill = GridBagConstraints.BOTH;
		gbc_lThisName.insets = new Insets(0, 0, 0, 10);
		gbc_lThisName.gridx = 0;
		gbc_lThisName.gridy = 3;
		pThisState.add(lThisName, gbc_lThisName);
		lThisName.setHorizontalAlignment(SwingConstants.RIGHT);
		lThisName.setFont(new Font("Tahoma", Font.PLAIN, 11));

		jtfThisName = new JTextField();
		GridBagConstraints gbc_jtfThisName = new GridBagConstraints();
		gbc_jtfThisName.fill = GridBagConstraints.BOTH;
		gbc_jtfThisName.insets = new Insets(0, 0, 0, 0);
		gbc_jtfThisName.gridx = 1;
		gbc_jtfThisName.gridy = 3;
		pThisState.add(jtfThisName, gbc_jtfThisName);

		JLabel lThisAddress = new JLabel("Address:");
		GridBagConstraints gbc_lThisAddress = new GridBagConstraints();
		gbc_lThisAddress.fill = GridBagConstraints.BOTH;
		gbc_lThisAddress.insets = new Insets(0, 0, 0, 10);
		gbc_lThisAddress.gridx = 0;
		gbc_lThisAddress.gridy = 4;
		pThisState.add(lThisAddress, gbc_lThisAddress);
		lThisAddress.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 11));
		lThisAddress.setHorizontalAlignment(SwingConstants.RIGHT);

		jtfAddress = new JTextField();
		GridBagConstraints gbc_jtfThisAddress = new GridBagConstraints();
		gbc_jtfThisAddress.fill = GridBagConstraints.BOTH;
		gbc_jtfThisAddress.insets = new Insets(0, 0, 0, 0);
		gbc_jtfThisAddress.gridx = 1;
		gbc_jtfThisAddress.gridy = 4;
		pThisState.add(jtfAddress, gbc_jtfThisAddress);

		lThisCity = new JLabel("City:");
		GridBagConstraints gbc_lThisCity = new GridBagConstraints();
		gbc_lThisCity.fill = GridBagConstraints.BOTH;
		gbc_lThisCity.insets = new Insets(0, 0, 0, 10);
		gbc_lThisCity.gridx = 0;
		gbc_lThisCity.gridy = 5;
		pThisState.add(lThisCity, gbc_lThisCity);
		lThisCity.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 11));
		lThisCity.setHorizontalAlignment(SwingConstants.RIGHT);
		jtfThisCity = new JTextField();
		GridBagConstraints gbc_jtfThisCity = new GridBagConstraints();
		gbc_jtfThisCity.fill = GridBagConstraints.BOTH;
		gbc_jtfThisCity.insets = new Insets(0, 0, 0, 0);
		gbc_jtfThisCity.gridx = 1;
		gbc_jtfThisCity.gridy = 5;
		pThisState.add(jtfThisCity, gbc_jtfThisCity);
		// This control is hidden but used to force parallel logic
		cbThisCounty = new JComboBox<String>();
		cbThisCounty.setVisible(false);
		pThisState.add(cbThisCounty, gbc_jtfThisCity );

		JLabel lThisZipCode = new JLabel("ZipCode:");
		GridBagConstraints gbc_lThisZipCode = new GridBagConstraints();
		gbc_lThisZipCode.fill = GridBagConstraints.BOTH;
		gbc_lThisZipCode.insets = new Insets(0, 0, 0, 10);
		gbc_lThisZipCode.gridx = 0;
		gbc_lThisZipCode.gridy = 6;
		pThisState.add(lThisZipCode, gbc_lThisZipCode);
		lThisZipCode.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 11));
		lThisZipCode.setHorizontalAlignment(SwingConstants.RIGHT);

		jtfZip = new JTextField();
		GridBagConstraints gbc_jtfThisZipCode = new GridBagConstraints();
		gbc_jtfThisZipCode.fill = GridBagConstraints.BOTH;
		gbc_jtfThisZipCode.insets = new Insets(0, 0, 0, 0);
		gbc_jtfThisZipCode.gridx = 1;
		gbc_jtfThisZipCode.gridy = 6;
		pThisState.add(jtfZip, gbc_jtfThisZipCode);

	}
	
	private void setupCVIDetailsPanel(JPanel pEdit ) {
		JPanel pCVIDetails = new JPanel();
		tbCVIDetails = new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "CVI Info:", TitledBorder.LEADING, TitledBorder.TOP, null, null);
		pCVIDetails.setBorder(tbCVIDetails);
		pCVIDetails.setBounds(0, 342, 275, 214);
		pEdit.add(pCVIDetails);

		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] {90, 125, 0};
		gbl_panel.rowHeights = new int[] {12, 12, 12, 12, 12, 12, 12, 12, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,  0.0, 0.0, Double.MIN_VALUE};
		pCVIDetails.setLayout(gbl_panel);
				
		JLabel lblSpecies = new JLabel("Species:");
		GridBagConstraints gbc_lblSpecies = new GridBagConstraints();
		gbc_lblSpecies.fill = GridBagConstraints.BOTH;
		gbc_lblSpecies.insets = new Insets(0, 0, 0, 10);
		gbc_lblSpecies.gridx = 0;
		gbc_lblSpecies.gridy = 0;
		pCVIDetails.add(lblSpecies, gbc_lblSpecies);
		lblSpecies.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSpecies.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 11));

		cbSpecies = new DBComboBox();
		GridBagConstraints gbc_cbSpecies = new GridBagConstraints();
		gbc_cbSpecies.fill = GridBagConstraints.BOTH;
		gbc_cbSpecies.insets = new Insets(0, 0, 0, 0);
		gbc_cbSpecies.gridx = 1;
		gbc_cbSpecies.gridy = 0;
		pCVIDetails.add(cbSpecies, gbc_cbSpecies);

		JLabel lNumber = new JLabel("Number:");
		GridBagConstraints gbc_lNumber = new GridBagConstraints();
		gbc_lNumber.fill = GridBagConstraints.BOTH;
		gbc_lNumber.insets = new Insets(0, 0, 0, 10);
		gbc_lNumber.gridx = 0;
		gbc_lNumber.gridy = 1;
		pCVIDetails.add(lNumber, gbc_lNumber);
		lNumber.setHorizontalAlignment(SwingConstants.RIGHT);
		lNumber.setFont(new Font("Tahoma", java.awt.Font.BOLD, 11));

		jtfNumber = new edu.clemson.lph.controls.DBNumericField();
		jtfNumber.setSQLType(java.sql.Types.INTEGER);
		jtfNumber.setRange(1L, CivetConfig.getMaxAnimals());
		GridBagConstraints gbc_jtfNumber = new GridBagConstraints();
		gbc_jtfNumber.fill = GridBagConstraints.BOTH;
		gbc_jtfNumber.insets = new Insets(0, 0, 0, 0);
		gbc_jtfNumber.gridx = 1;
		gbc_jtfNumber.gridy = 1;
		pCVIDetails.add(jtfNumber, gbc_jtfNumber);
		
		lMultipleSpecies = new JLabel("Multiple Species");
		lMultipleSpecies.setVisible(false);
		GridBagConstraints gbc_lblMultiSpecies = new GridBagConstraints();
		gbc_lblMultiSpecies.fill = GridBagConstraints.BOTH;
		gbc_lblMultiSpecies.insets = new Insets(0, 0, 5, 10);
		gbc_lblMultiSpecies.gridx = 0;
		gbc_lblMultiSpecies.gridy = 2;
		pCVIDetails.add(lMultipleSpecies, gbc_lblMultiSpecies);
		lMultipleSpecies.setHorizontalAlignment(SwingConstants.RIGHT);
		lMultipleSpecies.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));
		
		bAddSpecies = new JButton("Add Species");
		bAddSpecies.setFocusable(false);
		GridBagConstraints gbc_AddSpecies = new GridBagConstraints();
		gbc_AddSpecies.fill = GridBagConstraints.BOTH;
		gbc_AddSpecies.insets = new Insets(2, 2, 2, 10);
		gbc_AddSpecies.gridx = 1;
		gbc_AddSpecies.gridy = 2;
		pCVIDetails.add(bAddSpecies, gbc_AddSpecies);
		
		lblDateInspected = new JLabel("Inspected:");
		GridBagConstraints gbc_lThisName = new GridBagConstraints();
		gbc_lThisName.fill = GridBagConstraints.BOTH;
		gbc_lThisName.insets = new Insets(0, 0, 0, 10);
		gbc_lThisName.gridx = 0;
		gbc_lThisName.gridy = 3;
		pCVIDetails.add(lblDateInspected, gbc_lThisName);
		lblDateInspected.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDateInspected.setFont(new Font("Tahoma", java.awt.Font.BOLD, 11));

		jtfDateInspected = new DateField();
		jtfDateInspected.setFutureStatus(DateField.ASK_FUTURE);
		jtfDateInspected.setColumns(10);
		GridBagConstraints gbc_jtfDateInspected = new GridBagConstraints();
		gbc_jtfDateInspected.fill = GridBagConstraints.BOTH;
		gbc_jtfDateInspected.insets = new Insets(0, 0, 0, 0);
		gbc_jtfDateInspected.gridx = 1;
		gbc_jtfDateInspected.gridy = 3;
		pCVIDetails.add(jtfDateInspected, gbc_jtfDateInspected);
		
		JLabel lblDateReceived = new JLabel("Received:");
		GridBagConstraints gbc_lReceived = new GridBagConstraints();
		gbc_lReceived.fill = GridBagConstraints.BOTH;
		gbc_lReceived.insets = new Insets(0, 0, 0, 10);
		gbc_lReceived.gridx = 0;
		gbc_lReceived.gridy = 4;
		pCVIDetails.add(lblDateReceived, gbc_lReceived);
		lblDateReceived.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDateReceived.setFont(new Font("Tahoma", java.awt.Font.BOLD, 11));

		jtfDateReceived = new DateField();
		jtfDateReceived.setFutureStatus(DateField.ASK_FUTURE);
		jtfDateReceived.setColumns(10);
		GridBagConstraints gbc_jtfDateReceived = new GridBagConstraints();
		gbc_jtfDateReceived.fill = GridBagConstraints.BOTH;
		gbc_jtfDateReceived.insets = new Insets(0, 0, 0, 0);
		gbc_jtfDateReceived.gridx = 1;
		gbc_jtfDateReceived.gridy = 4;
		pCVIDetails.add(jtfDateReceived, gbc_jtfDateReceived);
		
		lblCviNumber = new JLabel("CVI Number:");
		GridBagConstraints gbc_lCviNumber = new GridBagConstraints();
		gbc_lCviNumber.fill = GridBagConstraints.BOTH;
		gbc_lCviNumber.insets = new Insets(0, 0, 0, 10);
		gbc_lCviNumber.gridx = 0;
		gbc_lCviNumber.gridy = 5;
		pCVIDetails.add(lblCviNumber, gbc_lCviNumber);
		lblCviNumber.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCviNumber.setFont(new Font("Tahoma", java.awt.Font.BOLD, 11));

		jtfCVINo = new JTextField();
		GridBagConstraints gbc_jtfCviNumber = new GridBagConstraints();
		gbc_jtfCviNumber.fill = GridBagConstraints.BOTH;
		gbc_jtfCviNumber.insets = new Insets(0, 0, 0, 0);
		gbc_jtfCviNumber.gridx = 1;
		gbc_jtfCviNumber.gridy = 5;
		pCVIDetails.add(jtfCVINo, gbc_jtfCviNumber);
		
		lIssuedBy = new JLabel("Issued By:");
		GridBagConstraints gbc_lIssuedBy = new GridBagConstraints();
		gbc_lIssuedBy.fill = GridBagConstraints.BOTH;
		gbc_lIssuedBy.insets = new Insets(0, 0, 0, 10);
		gbc_lIssuedBy.gridx = 0;
		gbc_lIssuedBy.gridy = 6;
		pCVIDetails.add(lIssuedBy, gbc_lIssuedBy);
		lIssuedBy.setHorizontalAlignment(SwingConstants.RIGHT);
		lIssuedBy.setFont(new Font("Tahoma", Font.PLAIN, 11));

		cbIssuedBy = new DBSearchComboBox();
		jtfIssuedBy = new JTextField();
		cbIssuedBy.setVisible(false);
		GridBagConstraints gbc_cbIssuedBy = new GridBagConstraints();
		gbc_cbIssuedBy.fill = GridBagConstraints.BOTH;
		gbc_cbIssuedBy.insets = new Insets(0, 0, 0, 0);
		gbc_cbIssuedBy.gridx = 1;
		gbc_cbIssuedBy.gridy = 6;
		pCVIDetails.add(cbIssuedBy, gbc_cbIssuedBy);
		GridBagConstraints gbc_jtfIssuedBy = new GridBagConstraints();
		gbc_jtfIssuedBy.fill = GridBagConstraints.BOTH;
		gbc_jtfIssuedBy.insets = new Insets(0, 0, 0, 0);
		gbc_jtfIssuedBy.gridx = 1;
		gbc_jtfIssuedBy.gridy = 6;
		pCVIDetails.add(jtfIssuedBy, gbc_jtfIssuedBy);
	
		ckAllVets = new JCheckBox("Show All Vets");
		ckAllVets.setSelected(false);
		GridBagConstraints gbc_lAllVets = new GridBagConstraints();
		gbc_lAllVets.fill = GridBagConstraints.BOTH;
		gbc_lAllVets.insets = new Insets(0, 0, 0, 10);
		gbc_lAllVets.gridx = 1;
		gbc_lAllVets.gridy = 7;
		pCVIDetails.add(ckAllVets, gbc_lAllVets);
		ckAllVets.setHorizontalAlignment(SwingConstants.RIGHT);
		ckAllVets.setFont(new Font("Tahoma", Font.BOLD, 11));	
		ckAllVets.setVisible(false);
		
		JLabel lPurpose = new JLabel("Purpose:");
		GridBagConstraints gbc_lPurpose = new GridBagConstraints();
		gbc_lPurpose.fill = GridBagConstraints.BOTH;
		gbc_lPurpose.insets = new Insets(0, 0, 0, 10);
		gbc_lPurpose.gridx = 0;
		gbc_lPurpose.gridy = 8;
		pCVIDetails.add(lPurpose, gbc_lPurpose);
		lPurpose.setHorizontalAlignment(SwingConstants.RIGHT);
		lPurpose.setFont(new Font("Tahoma", Font.BOLD, 11));
	
		cbPurpose = new DBComboBox();
		GridBagConstraints gbc_cbPurpose = new GridBagConstraints();
		gbc_cbPurpose.fill = GridBagConstraints.BOTH;
		gbc_cbPurpose.insets = new Insets(0, 0, 0, 0);
		gbc_cbPurpose.gridx = 1;
		gbc_cbPurpose.gridy = 8;
		pCVIDetails.add(cbPurpose, gbc_cbPurpose);

	}
	
	private void setupButtonPanel( JPanel pEdit ) {
		pButtons = new JPanel();
	
		tbButtons = new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "", TitledBorder.LEADING, TitledBorder.TOP, null, null);
		pButtons.setBorder(tbButtons);
		pButtons.setBounds(2, 556, 270, 144);
		pEdit.add(pButtons);

		GridBagLayout gbl_panel_buttons = new GridBagLayout();
		gbl_panel_buttons.columnWidths = new int[] {90, 117, 0};
		gbl_panel_buttons.rowHeights = new int[] {5, 14, 14, 14, 14, 14, 0};
		gbl_panel_buttons.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_buttons.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pButtons.setLayout(gbl_panel_buttons);

		bError = new JButton("Enter / View Errors");
		GridBagConstraints gbc_bError = new GridBagConstraints();
		gbc_bError.fill = GridBagConstraints.BOTH;
		gbc_bError.insets = new Insets(2, 0, 0, 10);
		gbc_bError.gridx = 1;
		gbc_bError.gridy = 1;
		pButtons.add(bError, gbc_bError);
		lError = new JLabel("Has Errors");
		lError.setHorizontalAlignment(SwingConstants.RIGHT);
		lError.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 11));
		GridBagConstraints gbc_lError = new GridBagConstraints();
		gbc_lError.fill = GridBagConstraints.BOTH;
		gbc_lError.insets = new Insets(2, 0, 0, 10);
		gbc_lError.gridx = 0;
		gbc_lError.gridy = 1;
		pButtons.add(lError, gbc_lError);
		lError.setVisible(false);
	
		bAddToLast = new JButton("Add Page to Previous");
		GridBagConstraints gbc_bAddToLast = new GridBagConstraints();
		gbc_bAddToLast.fill = GridBagConstraints.BOTH;
		gbc_bAddToLast.insets = new Insets(2, 0, 0, 10);
		gbc_bAddToLast.gridx = 1;
		gbc_bAddToLast.gridy = 2;
		pButtons.add(bAddToLast, gbc_bAddToLast);

		bAddIDs = new JButton("Add Animal IDs");
		GridBagConstraints gbc_btnAddIDs = new GridBagConstraints();
		gbc_btnAddIDs.fill = GridBagConstraints.BOTH;
		gbc_btnAddIDs.insets = new Insets(2, 0, 0, 10);
		gbc_btnAddIDs.gridx = 1;
		gbc_btnAddIDs.gridy = 3;
		pButtons.add(bAddIDs, gbc_btnAddIDs);
		
		bSave = new JButton("Save");
		GridBagConstraints gbc_bSave = new GridBagConstraints();
		gbc_bSave.fill = GridBagConstraints.BOTH;
		gbc_bSave.insets = new Insets(2, 0, 0, 10);
		gbc_bSave.gridx = 1;
		gbc_bSave.gridy = 4;
		pButtons.add(bSave, gbc_bSave);
	}
	
	public void make90Percent() {
	    // Center the window (will take effect when normalized)
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    int height = (int)(screenSize.height * 0.90);
	    int width = (int)(screenSize.width * 0.90);
	    this.setSize(width,height);
	    this.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
	}
	
	public void make90PercentShift() {
	    // Center the window (will take effect when normalized)
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    int height = (int)(screenSize.height * 0.90);
	    int width = (int)(screenSize.width * 0.90);
	    this.setSize(width,height);
	    this.setLocation((int)((screenSize.width - width) / 1.5), (int)((screenSize.height - height) / 1.5));
	}

	/**
	 * Get current page number in current file.  1 indexed.
	 * @return
	 */
	public int getCurrentPageNo() { return iPageNo; }
	
	// Callbacks for Threads and Controller to set values in counter panel.
	void setPage( int iPageNo ) {
		this.iPageNo = iPageNo;
		pCounters.setPage(iPageNo);
	}

	void setPages( int iPages ) {
		pCounters.setPages(iPages);
	}
	
	void setFile( int iFileNo ) {
		this.iFileNo = iFileNo;
		pCounters.setFile(iFileNo); // currentFiles is 0 indexed array
	}

	void setFiles( int iFiles ) {
		pCounters.setFiles(iFiles);
	}
	
	/**
	 * Set the type of file and view-only nature of this dialog.
	 * @param iMode one of three constants
	 * 	PDF_MODE - Image PDF whether loaded as a PDF file or generated from some other image type.
	 *  XML_MODE - Read from a saved Civet "binary" file with attached image PDF.  This will be the mode
	 *  	for received Civet files when we get that far as well as when we reopen files saved
	 *  	but not yet submitted to HERDS
	 *  VIEW_MODE - Civet "binary" file being opened read-only (to view email, etc.)
	 */
	public void setMode( int iMode ) {
		this.iMode = iMode;
		switch(iMode) {
		case PDF_MODE:
			bMode.setIcon( iconPDF );
			ckSticky.setVisible(true);
			setFormEditable(true);
			bSave.setVisible(true);
			bAddSpecies.setEnabled(true);
			ckAllVets.setEnabled(true);
			break;
		case XML_MODE:
			bMode.setIcon( iconXML );
			ckSticky.setVisible(false);
			setFormEditable(true);
			bSave.setVisible(true);
			bAddSpecies.setEnabled(true);
			ckAllVets.setEnabled(true);
			break;
		case VIEW_MODE:
			bMode.setIcon( iconMAIL );
			ckSticky.setVisible(false);
			setFormEditable(false);
			bSave.setVisible(false);
			bAddSpecies.setEnabled(false);
			ckAllVets.setEnabled(false);
			break;
		default:
			logger.error("Unkown mode " + iMode );
		}
	}
	/**
	 * Enable or disable editing.
	 * @param bEditable
	 */
	void setFormEditable( boolean bEditable ) {
		rbImport.setEnabled(bEditable);
		rbExport.setEnabled(bEditable);
		rbInState.setEnabled(bEditable);
		cbOtherState.setEnabled(bEditable);
		jtfOtherName.setEnabled(bEditable);
		jtfOtherAddress.setEnabled(bEditable);
		jtfOtherCity.setEnabled(bEditable);
		jtfOtherZip.setEnabled(bEditable);
		cbOtherCounty.setEditable(false);
		cbOtherCounty.setEnabled(bEditable);
		jtfThisPIN.setEnabled(bEditable);
		jtfPhone.setEnabled(bEditable);
		jtfAddress.setEnabled(bEditable);
		jtfThisCity.setEnabled(bEditable);
		jtfZip.setEnabled(bEditable);
		jtfCVINo.setEnabled(bEditable);
		cbSpecies.setEnabled(bEditable);
		jtfNumber.setEnabled(bEditable);
		jtfThisState.setFocusable(false);
		jtfThisState.setEnabled(bEditable);
		jtfThisPIN.setEditable(bEditable);
		jtfThisName.setEnabled(bEditable);
		jtfPhone.setEditable(bEditable);
		jtfAddress.setEditable(bEditable);
		jtfThisCity.setEditable(bEditable);
		cbThisCounty.setEditable(false);
		cbThisCounty.setEnabled(bEditable);
		jtfZip.setEditable(bEditable);
		jtfDateInspected.setEnabled(bEditable);
		jtfDateInspected.setEditable(bEditable);
		jtfDateInspected.setBackground(Color.white);
		jtfDateReceived.setEnabled(bEditable);
		jtfDateReceived.setEditable(bEditable);
		jtfDateReceived.setBackground(Color.white);
		jtfCVINo.setEditable(bEditable);
		jtfNumber.setEditable(bEditable);
		jtfNumber.setBackground(jtfCVINo.getBackground());
		jtfDateInspected.setEnabled(bEditable);
		jtfDateInspected.setBackground(jtfCVINo.getBackground());
		jtfDateReceived.setEnabled(bEditable);
		jtfDateReceived.setBackground(jtfCVINo.getBackground());
		jtfIssuedBy.setEnabled(bEditable);
		jtfIssuedBy.setBackground(jtfCVINo.getBackground());
		cbPurpose.setEnabled(bEditable);
		cbIssuedBy.setEnabled(bEditable);
		bError.setEnabled(bEditable);
		bAddIDs.setEnabled(bEditable);
		bBigger.setEnabled(bEditable);
		bSmaller.setEnabled(bEditable);
	}

	/**
	 * Set direction of movement
	 * @param bImport
	 */
	void setImport(boolean bInbound) {
		bImport = bInbound;
		if( bInbound ) {
			tbOtherState.setTitle("From:");
			tbThisState.setTitle("To:");
			lIssuedBy.setFont(new Font("Tahoma", Font.PLAIN, 11));
			cbIssuedBy.setVisible(false);
			jtfIssuedBy.setVisible(true);
			ckAllVets.setVisible(false);
		}
		else {
			tbOtherState.setTitle("To:");
			tbThisState.setTitle("From:");
			lIssuedBy.setVisible(true);
			lIssuedBy.setFont(new Font("Tahoma", Font.BOLD, 11));
			cbIssuedBy.setVisible(true);
			jtfIssuedBy.setVisible(false);
			ckAllVets.setVisible(true);
		}
		pThisState.repaint();
		pOtherState.repaint();
	}

}// End Class CVIPdfEdit
