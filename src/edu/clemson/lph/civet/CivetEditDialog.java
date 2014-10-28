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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.beans.Beans;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.jpedal.PdfDecoder;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.raw.PdfDictionary;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.clemson.lph.civet.lookup.ErrorTypeLookup;
import edu.clemson.lph.civet.lookup.PurposeLookup;
import edu.clemson.lph.civet.lookup.SpeciesLookup;
import edu.clemson.lph.civet.lookup.States;
import edu.clemson.lph.civet.lookup.VetLookup;
import edu.clemson.lph.civet.webservice.PremisesSearchDialog;
import edu.clemson.lph.civet.webservice.UsaHerdsLookupPrems;
import edu.clemson.lph.civet.webservice.VetSearchDialog;
import edu.clemson.lph.civet.webservice.WebServiceException;
import edu.clemson.lph.civet.xml.CoKsXML;
import edu.clemson.lph.civet.xml.CviMetaDataXml;
import edu.clemson.lph.civet.xml.StdeCviXml;
import edu.clemson.lph.controls.DBComboBox;
import edu.clemson.lph.controls.DBNumericField;
import edu.clemson.lph.controls.DBSearchComboBox;
import edu.clemson.lph.controls.DateField;
import edu.clemson.lph.controls.PhoneField;
import edu.clemson.lph.controls.PinField;
import edu.clemson.lph.controls.SearchTextField;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.QuestionDialog;
import edu.clemson.lph.dialogs.YesNoDialog;
import edu.clemson.lph.pdfgen.PDFOpener;
import edu.clemson.lph.utils.PremCheckSum;

import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;


@SuppressWarnings("serial")
public final class CivetEditDialog extends JFrame {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private Window parent;
	private String viewerTitle="Civet: ";
	static final String sFileCopyAddress = CivetConfig.getEmailCopyTo();
	public static final int PDF_MODE = 0;
	public static final int XML_MODE = 1;
	public static final int VIEW_MODE = 2;

	/** Data behind the GUI **/
	/**the actual JPanel/decoder object*/
	private PdfDecoder pdfDecoder;
	private CVIFileController controller;

	private float fScale = 1.0f;
	private int iRotation = 180;  // 0 appears to be upside down relative to Acrobat ?!
	private boolean bImport = true;
	private int iMode;

	ArrayList<SpeciesRecord> aSpecies = new ArrayList<SpeciesRecord>();
	AnimalIDListTableModel idListModel = new AnimalIDListTableModel();
	private ArrayList<String> aErrorKeys;
	private String sErrorNotes;
	private String sPriorPhone;
	private String sPriorAddress;
	private String sPriorCity;
	private boolean bLidFromHerds = false;
	private PremisesSearchDialog premSearch = new PremisesSearchDialog();

	/** GUI components that need to be read or written outside of initialization **/
	// NOTE Use of default visibility for controls that need to be accessed by the 
	// invokeLater() methods of secondary threads.
	private ImageIcon appIcon;
	private CountersPanel pCounters;
	PinField jtfOtherPIN;
	private JTextField jtfThisState;
	SearchTextField jtfThisPIN;
	JTextField jtfPhone;
	JTextField jtfAddress;
	JTextField jtfThisCity;
	JTextField jtfZip;
	DBNumericField jtfNumber;
	DateField jtfDateInspected;
	JTextField jtfCVINo;
	private ImageIcon iconPDF = null;
	private ImageIcon iconXML = null;
	private ImageIcon iconMAIL = null;
	DBComboBox cbOtherState;
	DBComboBox cbSpecies;
	JRadioButton rbImport;
	JRadioButton rbExport;
	JRadioButton rbInState;
	private JButton bAddSpecies;
	private JButton bSave;
	private JButton bError;
	private JLabel bMode;
	private JPanel pSpacer;
	private JButton bPDFView;
	private JButton bRotate;
	private JButton bBigger;
	private JButton bSmaller;
	JScrollPane display;
	JLabel lMultipleSpecies;
	private ButtonGroup rbGroup;
	private JPanel topBar;
	private JLabel lError;
	DBSearchComboBox cbIssuedBy;
	private JLabel lIssuedBy;
	JTextField jtfIssuedBy;
	JTextField jtfOtherCity;
	DateField jtfDateReceived;
	private JCheckBox ckSticky;
	JPanel altDisplay;
	JPanel pView;
	JLabel lblCviNumber;
	JLabel lblDateInspected;
	private JCheckBox ckAllVets;
	boolean bInSearch = false;
	JTextField jtfOtherName;
	JTextField jtfThisName;
	private boolean bMultiSpecies = false;
	DBComboBox cbPurpose;
	private VetLookup vetLookup;
	JTextField jtfOtherAddress;
	JTextField jtfOtherZip;
	TitledBorder tbOtherState;
	TitledBorder tbThisState;
	TitledBorder tbCVIDetails;
	JPanel pOtherState;
	JPanel pThisState;
	private JPanel pButtons;
	private TitledBorder tbButtons;
	private CivetEditOrderTraversalPolicy traversal;
	private JPanel pEdit;
	private JButton bAddToLast;
	private JButton bGotoPage;
	private JButton bAddIDs;
	private JButton bEditLast;

	/**
	 * construct an empty pdf viewer and pop up the open window
	 * @wbp.parser.constructor
	 */
	public CivetEditDialog( Window parent ){
		this.parent = parent;
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		controller = new CVIFileController( this ); 
		initializeDisplay();
		initializeDBComponents();

	}

	private void make90Percent() {
	    // Center the window (will take effect when normalized)
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    int height = (int)(screenSize.height * 0.90);
	    int width = (int)(screenSize.width * 0.90);
	    this.setSize(width,height);
	    this.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
	}
	
	public void updateCounterPanel() {
		boolean morePagesBack = controller.morePagesBack();
		boolean morePagesForward = controller.morePagesForward();
		pCounters.setPageBackEnabled(morePagesBack);
		pCounters.setPageForwardEnabled(morePagesForward);
		pCounters.setFileBackEnabled(controller.moreFilesBack());
		pCounters.setFileForwardEnabled(controller.moreFilesForward());
		if( morePagesBack || morePagesForward )
			bGotoPage.setEnabled(true);
		else
			bGotoPage.setEnabled(false);		
	}
	
	public void updateDisplay() {
		updateDisplay(true);
	}
		
	public void updateDisplay(boolean bGUI) {
	      //wait to ensure decoded
		pdfDecoder.setPageParameters(getScale(),
									controller.getCurrentPageNo(),
									getRotation()); //values scaling (1=100%). page number
		pdfDecoder.waitForDecodingToFinish();
		if( bGUI ) {
			pdfDecoder.invalidate();
			pdfDecoder.updateUI();
			pdfDecoder.validate();
		}
	}

	/**
	 * This is misnamed now that we have no actual database connection.
	 * The idea is to isolate those actions that interfere with GUI design.
	 */
	private void initializeDBComponents() {
		if( !Beans.isDesignTime() ) {
			try {
			
				cbOtherState.setModel( new States() );
				cbOtherState.setBlankDefault(true);
				cbOtherState.refresh();
				
				cbSpecies.setModel( new SpeciesLookup() );
				cbSpecies.setBBlankDefault(true);
				cbSpecies.refresh();
				
				vetLookup = new VetLookup();
				vetLookup.setLevel2Check(true);
				vetLookup.setExpCheck(true);
			    cbIssuedBy.setModel(vetLookup);
				cbIssuedBy.setSearchDialog( new VetSearchDialog() );
			    cbIssuedBy.setHideCode(true);
			    cbIssuedBy.setToolTipText("CTRL F to Search for name");
			    cbIssuedBy.setSearchTitle("Civet Search: Veterinarian");
				cbIssuedBy.setBlankDefault(true);
			    cbIssuedBy.refresh();
			    
				cbPurpose.setModel( new PurposeLookup() );
			    cbPurpose.refresh();
			    cbPurpose.setSelectedItem("Interstate");
			    make90Percent();

			}
			catch( Exception e ) {
				logger.error(e.getMessage() + "\nError in DB Components",e);
				MessageDialog.showMessage(CivetEditDialog.this, "Civet Error: Database", "Error in DB Components" );
				e.printStackTrace();
			}
		}
		else {
		    this.setSize(new Dimension(732, 819));

		}
	}

	/**
	 * Called from constructor to isolate all the graphical layout verbosity
	 */
	private void initializeDisplay() {
		setTitle(viewerTitle);
		appIcon = new ImageIcon(getClass().getResource("res/civet32.png"));
		this.setIconImage(appIcon.getImage());
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);		
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if( iMode == VIEW_MODE || YesNoDialog.ask(CivetEditDialog.this, "Civet: Close", "Close without saving?") ) {
					doCleanup();
					setVisible(false);
					dispose();
				}
			}
		});
		setupMenus();
		getContentPane().setLayout(new BorderLayout(0, 0));

		setupTopBar();
		setupEditPanel();
		setupViewPanel();
		
		traversal = new CivetEditOrderTraversalPolicy( this );
		traversal.loadComponentOrderMaps();
		traversal.selectMainMap();
		traversal.setFirstComponent("OtherState");
		this.setFocusTraversalPolicy(traversal);
		
		String sDirection = CivetConfig.getDefaultDirection();
		if( "Import".equalsIgnoreCase(sDirection) ) {
			rbImport.setSelected(true);
			setImport(true);
		}
		else {
			rbExport.setSelected(true);
			setImport(false);
		}

		setAllFocus();
	}
	
	private void setupMenus() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmSave = new JMenuItem("Save");
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doSave();
			}
		});
		mnFile.add(mntmSave);

		JMenuItem mntmClose = new JMenuItem("Close");
		mntmClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cancel();
			}
		});
		mnFile.add(mntmClose);

		JMenu mnView = new JMenu("View");
		menuBar.add(mnView);

		JMenuItem mntmMinimizeAll = new JMenuItem("Minimize All");
		mntmMinimizeAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				minimizeAll();
			}
		});
		mnView.add(mntmMinimizeAll);

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
		bBigger.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fScale = fScale * 1.1f;
				pdfDecoder.setPageParameters(fScale,controller.getCurrentPageNo(),iRotation);
				pdfDecoder.invalidate();
				display.setViewportView(pdfDecoder);
				repaint();
			}
		});
		topBar.add(bBigger);
		topBar.add(new JPanel());
		bSmaller = new JButton();
		bSmaller.setFont(new java.awt.Font("Dialog", 1, 14));
		bSmaller.setText("-");
		bSmaller.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fScale = fScale / 1.1f;
				pdfDecoder.setPageParameters(fScale,controller.getCurrentPageNo(),iRotation);
				pdfDecoder.invalidate();
				display.setViewportView(pdfDecoder);
				repaint();
			}
		});
		topBar.add(bSmaller);
		topBar.add(new JPanel());
		bRotate = new JButton();
		bRotate.setIcon( new ImageIcon(getClass().getResource("/edu/clemson/lph/civet/res/rotate.gif")));
		bRotate.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				iRotation = (iRotation + 90) % 360;
				pdfDecoder.setPageParameters(fScale,controller.getCurrentPageNo(),iRotation);
				pdfDecoder.invalidate();
				display.setViewportView(pdfDecoder);
				repaint();
			}
		});
		topBar.add(bRotate);
		topBar.add(new JPanel());
		bPDFView = new JButton();
		bPDFView.setFont(new java.awt.Font("Dialog", 1, 14));
		//    bPDFView.setText("View In Acrobat");  // Change to icon for rotate.
		bPDFView.setIcon(iconPDF);
		bPDFView.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pdfView();
			}
		});
		topBar.add(bPDFView);
		
		pCounters = new CountersPanel( this );
		topBar.add(pCounters);
		pCounters.setVisible(true);

		bGotoPage = new JButton("Goto Page");
		bGotoPage.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doPickPage();
			}
		});
		topBar.add(bGotoPage);

		ckSticky = new JCheckBox("All Values Sticky");
		ckSticky.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));
		ckSticky.setToolTipText("Check to make all values remain from one record to the next");
		topBar.add(ckSticky);
		
		bEditLast = new JButton("Edit Last");
		bEditLast.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doEditLast();
			}
		});
		topBar.add(bEditLast);
		
	}
	
	void doEditLast() {
		File fLast = controller.getLastSavedFile();
		File aFiles[] = new File[1];
		aFiles[0] = fLast;
		CivetEditDialog dlgLast = new CivetEditDialog(parent);
		dlgLast.openFiles(aFiles, false);
		dlgLast.setVisible(true);
	}
	
	void doAddToLast() {
		controller.addCurrentPage();
		if( controller.isXFADocument() ) {
			MessageDialog.showMessage(this, "Civet: Error", "PDF Form Files are not save as pages.");
			return;
		}
		byte[] bExtractedPageBytes = controller.extractPagesToNewPDF();
		File fLastSavedFile = controller.getLastSavedFile();
		AddPageToCviThread addThread = new AddPageToCviThread( this, fLastSavedFile, bExtractedPageBytes );
		addThread.start();
	}

	private void setupViewPanel() {	
		pdfDecoder = new PdfDecoder(true);
		//ensure non-embedded font map to sensible replacements
		//    PdfDecoder.setFontReplacements(pdfDecoder);
		pView = new JPanel();
		pView.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		getContentPane().add(pView, BorderLayout.CENTER);
		pView.setLayout(new BorderLayout(0, 0));
		display = new JScrollPane();
		display.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		display.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		// This is the magic.  We simply put the pdfDecoder in the viewport of the scroll pane.
		display.setViewportView(pdfDecoder);
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
		getContentPane().add(pEdit, BorderLayout.WEST);

		rbImport = new JRadioButton("Import");
		rbImport.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));

		rbImport.setBounds(10, 7, 65, 23);
		rbImport.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rbInbound_actionPerformed(e);
			}
		});
		rbExport= new JRadioButton("Export");
		rbExport.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));
		rbExport.setBounds(98, 7, 73, 23);
		rbExport.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rbOutbound_actionPerformed(e);
			}
		});
		
		rbInState = new JRadioButton("In State");
		rbInState.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));
		rbInState.setBounds(184, 7, 81, 23);
		rbInState.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rbInState_actionPerformed(e);
			}
		});
		rbGroup = new ButtonGroup();
		rbGroup.add(rbImport);
		rbGroup.add(rbExport);
		rbGroup.add(rbInState);

		pEdit.setPreferredSize(new Dimension(275,250));
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
//		cbOtherState.addFocusListener(new FocusAdapter() {
//			@Override
//			public void focusLost(FocusEvent arg0) {
//				if( !CivetConfig.isStandAlone() )
//					jtfOtherCity.requestFocus();
//			}
//		});


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
//		jtfOtherCity.addFocusListener(new FocusAdapter() {
//			@Override
//			public void focusLost(FocusEvent arg0) {
//				if( !CivetConfig.isStandAlone() )
//					jtfPhone.requestFocus();
//			}
//		});

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
	
		JLabel lblPin = new JLabel("PIN:");
		GridBagConstraints gbc_lblPin = new GridBagConstraints();
		gbc_lblPin.fill = GridBagConstraints.BOTH;
		gbc_lblPin.insets = new Insets(0, 0, 0, 10);
		gbc_lblPin.gridx = 0;
		gbc_lblPin.gridy = 5;
		pOtherState.add(lblPin, gbc_lblPin);
		lblPin.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));
		lblPin.setHorizontalAlignment(SwingConstants.RIGHT);

		jtfOtherPIN = new PinField();
		GridBagConstraints gbc_jtfOtherPIN = new GridBagConstraints();
		gbc_jtfOtherPIN.fill = GridBagConstraints.BOTH;
		gbc_jtfOtherPIN.gridx = 1;
		gbc_jtfOtherPIN.gridy = 5;
		pOtherState.add(jtfOtherPIN, gbc_jtfOtherPIN);
	
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

		jtfPhone = new PhoneField();
		jtfPhone.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(FocusEvent e) {
				jtfPhone_focusLost(e);
			}
			public void focusGained(FocusEvent e) {
				sPriorPhone = jtfPhone.getText();
			}
		});
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
		jtfThisPIN.setSearchDialog( premSearch );
		jtfThisPIN.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(FocusEvent e) {
				jtfThisPIN_focusLost(e);
			}
		});
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
		jtfAddress.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(FocusEvent e) {
				jtfAddrCity_focusLost(e);
			}
			public void focusGained(FocusEvent e) {
				sPriorAddress = jtfAddress.getText();
			}
		});
		GridBagConstraints gbc_jtfThisAddress = new GridBagConstraints();
		gbc_jtfThisAddress.fill = GridBagConstraints.BOTH;
		gbc_jtfThisAddress.insets = new Insets(0, 0, 0, 0);
		gbc_jtfThisAddress.gridx = 1;
		gbc_jtfThisAddress.gridy = 4;
		pThisState.add(jtfAddress, gbc_jtfThisAddress);


		JLabel lThisCity = new JLabel("City:");
		GridBagConstraints gbc_lThisCity = new GridBagConstraints();
		gbc_lThisCity.fill = GridBagConstraints.BOTH;
		gbc_lThisCity.insets = new Insets(0, 0, 0, 10);
		gbc_lThisCity.gridx = 0;
		gbc_lThisCity.gridy = 5;
		pThisState.add(lThisCity, gbc_lThisCity);
		lThisCity.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 11));
		lThisCity.setHorizontalAlignment(SwingConstants.RIGHT);

		jtfThisCity = new JTextField();
		jtfThisCity.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(FocusEvent e) {
				jtfAddrCity_focusLost(e);
			}
			public void focusGained(FocusEvent e) {
				sPriorCity = jtfThisCity.getText();
			}
		});
		GridBagConstraints gbc_jtfThisCity = new GridBagConstraints();
		gbc_jtfThisCity.fill = GridBagConstraints.BOTH;
		gbc_jtfThisCity.insets = new Insets(0, 0, 0, 0);
		gbc_jtfThisCity.gridx = 1;
		gbc_jtfThisCity.gridy = 5;
		pThisState.add(jtfThisCity, gbc_jtfThisCity);

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
		bAddSpecies.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateSpeciesList( true );
			}
		});
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
		jtfDateInspected.addFocusListener( new java.awt.event.FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if( jtfDateInspected.isAcceptedDate() )
					checkPreDate();
			}
		});
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
		jtfDateReceived.addFocusListener( new java.awt.event.FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if( jtfDateReceived.isAcceptedDate() )
					checkPreDate();
			}
		});
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
		ckAllVets.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean bChecked = ckAllVets.isSelected();
				doShowAllVets(!bChecked);
			}
		});
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
		bError.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runErrorDialog();
			}
		});
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
		bAddToLast.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				controller.doAddToLast();
				doAddToLast();
			}
		});

		bAddIDs = new JButton("Add Animal IDs");
		GridBagConstraints gbc_btnAddIDs = new GridBagConstraints();
		gbc_btnAddIDs.fill = GridBagConstraints.BOTH;
		gbc_btnAddIDs.insets = new Insets(2, 0, 0, 10);
		gbc_btnAddIDs.gridx = 1;
		gbc_btnAddIDs.gridy = 3;
		pButtons.add(bAddIDs, gbc_btnAddIDs);
		bAddIDs.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if( cbSpecies.getSelectedKeyInt() <= 0 && aSpecies.size() == 0 ) {
					MessageDialog.showMessage( CivetEditDialog.this, "Civet Error", "Species must be added before IDs" );
				}
				else {
					ArrayList<String> aSpeciesStrings = new ArrayList<String>();
					if( aSpecies.size() > 0 ) {
						for( SpeciesRecord r : aSpecies ) {
							aSpeciesStrings.add(cbSpecies.getValueForKey(r.iSpeciesKey));
						}
					}
					if( cbSpecies.getSelectedKeyInt() >= 0 ) {
						String sSpecies = cbSpecies.getSelectedValue();
						if( !aSpeciesStrings.contains(sSpecies) ) {
							aSpeciesStrings.add(sSpecies);
						}
					}
					HashMap<Integer, String> hSpecies = new HashMap<Integer, String>();
					for( String sSp : aSpeciesStrings ) {
						int iKey = cbSpecies.getKeyForValue(sSp);
						hSpecies.put(iKey,sSp);
					}
					AddAnimalsDialog dlg = new AddAnimalsDialog( hSpecies, idListModel );
					dlg.setModal(true);
					dlg.setVisible(true);
				}
			}
		});
		
		bSave = new JButton("Save");
		GridBagConstraints gbc_bSave = new GridBagConstraints();
		gbc_bSave.fill = GridBagConstraints.BOTH;
		gbc_bSave.insets = new Insets(2, 0, 0, 10);
		gbc_bSave.gridx = 1;
		gbc_bSave.gridy = 4;
		pButtons.add(bSave, gbc_bSave);
		bSave.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSave();
			}
		});

	}
	
	public void setErrors( ArrayList<String> aErrorKeys ) {
		if( this.aErrorKeys == null )
			this.aErrorKeys = new ArrayList<String>();
		else
			this.aErrorKeys.clear();
		if( aErrorKeys == null || aErrorKeys.isEmpty() ) {
			lError.setVisible(false);
		}
		else {
			lError.setVisible(true);
			for( String s : aErrorKeys ) 
				this.aErrorKeys.add(s);
		}
	}
	
	public void setErrorNote( String sNote ) {
		this.sErrorNotes = sNote;
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
	 * This is used to make each field select the entire text 
	 * when tabbed into so it can be easily overwritten.
	 */
	private void setAllFocus() {
		 jtfOtherCity.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(FocusEvent e) {
					selectAll(e);
				}
			});
		 jtfOtherName.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(FocusEvent e) {
					selectAll(e);
				}
			});
		 jtfThisPIN.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(FocusEvent e) {
					jtfThisPIN.selectAll();
				}
			});
		 jtfThisState.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(FocusEvent e) {
					selectAll(e);
				}
			});
		 jtfPhone.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(FocusEvent e) {
					selectAll(e);
				}
			});
		 jtfAddress.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(FocusEvent e) {
					selectAll(e);
				}
			});
		 jtfThisName.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(FocusEvent e) {
					selectAll(e);
				}
			});
		 jtfThisCity.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(FocusEvent e) {
					selectAll(e);
				}
			});
		 jtfZip.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(FocusEvent e) {
					selectAll(e);
				}
			});
		 jtfNumber.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(FocusEvent e) {
					selectAll(e);
				}
			});
		 jtfDateInspected.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(FocusEvent e) {
					selectAll(e);
				}
			});
		 jtfCVINo.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(FocusEvent e) {
					selectAll(e);
				}
			});
		 jtfOtherCity.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(FocusEvent e) {
					selectAll(e);
				}
			});
		 jtfDateReceived.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(FocusEvent e) {
					selectAll(e);
				}
			});
	}

	/**
	 * Open previously selected files.
	 * @param selectedFiles
	 */
	public void openFiles(File selectedFiles[], boolean bView ) {
		controller.setCurrentFiles(selectedFiles, bView);
	}

	/**
	 * opens a chooser and allows user to select One or more pdf or jpg files and opens a pdf created from them.
	 */
	public void selectFiles() {
		iRotation = CivetConfig.getRotation();
		File fDir = new File( CivetConfig.getInputDirPath() );
		JFileChooser open = new JFileChooser( fDir );
		open.setDialogTitle("Civet: Open multiple PDF and Image Files");
		open.setFileSelectionMode(JFileChooser.FILES_ONLY);
		open.setFileFilter(new FileNameExtensionFilter(
		        "Image, PDF, and Civet Files", "jpg", "png", "pdf", "jpeg", "gif", "bmp", "cvi"));
		open.setMultiSelectionEnabled(true);

		int resultOfFileSelect = JFileChooser.ERROR_OPTION;
		while(resultOfFileSelect==JFileChooser.ERROR_OPTION){

			resultOfFileSelect = open.showOpenDialog(this);

			if(resultOfFileSelect==JFileChooser.ERROR_OPTION) {
				logger.error("JFileChooser error");
			}

			if(resultOfFileSelect==JFileChooser.APPROVE_OPTION){
				File selectedFiles[] = open.getSelectedFiles();
				openFiles(selectedFiles, false);
			}
		}
	}
	
	public PdfDecoder getPdfDecoder() { return pdfDecoder; }
	public CVIFileController getController() { return controller; }
	public float getScale() { return fScale; }
	public int getRotation() { return iRotation; }
	public void setRotation( int iRotation ) { this.iRotation = iRotation; }
	public String getViewerTitle() { return viewerTitle; }
	public int getPageNo() { return controller.getCurrentPageNo(); }
	
	private void checkPreDate() {
		java.util.Date dateInspected = jtfDateInspected.getDate();
		java.util.Date dateReceived = jtfDateReceived.getDate();
		if( (dateInspected != null && dateReceived != null && dateInspected.getTime() > dateReceived.getTime() )
				|| ( jtfDateInspected.isAcceptedDate() && jtfDateInspected.isFuture() ) ) {
			String sShortName = ErrorTypeLookup.getShortNameForDescription("Pre-dated signature");
			if( aErrorKeys == null )
				aErrorKeys = new ArrayList<String>();
			if( !aErrorKeys.contains(sShortName) ) 
				aErrorKeys.add(sShortName);
			lError.setVisible(true);
		}
	}

	
	private void cancel() {
		if( iMode != VIEW_MODE ) {
			if( YesNoDialog.ask(this, "Civet: Close", "Close without saving?") ) {
				doCleanup();
				setVisible(false);
			}
		}
	}
	
	
	private void doCleanup() {
		//TODO Add logic to handle partially complete file or file list.
		File[] completeFiles = controller.getCompleteFiles();
    	moveCompleteFiles( completeFiles );
    	if( parent instanceof CivetInbox) {
    		((CivetInbox)parent).refreshTables();
    	}
	}
	
	// Probably fast enough to leave in event dispatch thread
	// but not good practice.
	public void allFilesDone() {
		pdfDecoder.closePdfFile();
	    if( !isReopened() ) {
	    	File completeFiles[] = controller.getCompleteFiles();
	    	moveCompleteFiles( completeFiles );
	    }
	    if( parent instanceof CivetInbox) {
			((CivetInbox)parent).refreshTables();
		}
		setVisible(false);
	}
	
	private void moveCompleteFiles( File[] completeFiles ) {
    	// Destination for files 
    	File dir = new File(CivetConfig.getOutputDirPath());
    	int iFiles = 0;
    	for( File fCurrent : completeFiles ) {
    		// Move file to new directory
    		File fNew = new File(dir, fCurrent.getName());
    		boolean success = fCurrent.renameTo(fNew);
    		if (!success) {
    			MessageDialog.showMessage(this, "Civet Error", "Could not move " + fCurrent.getAbsolutePath() + " to " + fNew.getAbsolutePath() );
    		}
    		else {
    			iFiles++; 
    		}
    	}
    	if( iFiles > 0 )
    		MessageDialog.showMessage(this, "Civet Complete", iFiles + " files ready to submit to USAHERDS.\n Original files moved to " + dir.getPath() );

	}

	public boolean isReopened() {
		String sCurrentPath = controller.getCurrentFilePath();
		String sCurrentDir = sCurrentPath.substring(0,sCurrentPath.lastIndexOf('\\')+1);
		// This is a huge kluge to detect that we are reopening a toBeFiled stdXML
	    return( sCurrentDir.equalsIgnoreCase(CivetConfig.getToFileDirPath()) );
	}

	void doPickPage( MouseEvent e ) {
		if( e.getClickCount() == 2 ) {
			doPickPage();
		}
	}
	
	// Callbacks for Threads to set values in counter panel.
	void setPage( int iPageNo ) {
		pCounters.setPage(iPageNo);
	}

	void setPages( int iPages ) {
		pCounters.setPages(iPages);
	}
	
	void setFile( int iFileNo ) {
		pCounters.setFile(iFileNo); // currentFiles is 0 indexed array
	}

	void setFiles( int iFiles ) {
		pCounters.setFiles(iFiles);
	}
	
	private void doShowAllVets( boolean bChecked ) {
		int iSel = cbIssuedBy.getSelectedKeyInt();
		// Note isSelected() gives the value BEFORE the action takes place so we reverse the logic
		// But logic is already backward so only negate the selection!  
		// ShowAll == NOT Checking Expiration or NAN Level
		vetLookup.setExpCheck(bChecked);
		vetLookup.setLevel2Check(bChecked);
		ckAllVets.setSelected(!bChecked);
		cbIssuedBy.refresh();
		if( iSel >= 0 )
			cbIssuedBy.setSelectedKey(iSel);
	}

	private void doPickPage() {
		String sPage = QuestionDialog.ask(this, "Civet: Goto Page", "Page number?");
		int iPage = -1;
		try {
			iPage = Integer.valueOf(sPage);
		} catch (NumberFormatException ex) {
			MessageDialog.showMessage(this, "Civet: Error", "Cannot parse " + sPage + " as a number");
			return;
		}
		controller.setPage(iPage);
	}
	
	void setupForm( String sFileName, int iPageNo, int iPagesInFile, int iFileNo, int iFiles, boolean bPageComplete ) {
		setTitle(getViewerTitle()+sFileName);
		setPage(iPageNo);
		setPages(iPagesInFile);
		setFile(iFileNo);
		setFiles(iFiles);
		setFormEditable(!bPageComplete && iMode != VIEW_MODE);
		updateCounterPanel();
		setupSaveButtons();	
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
		jtfOtherPIN.setEnabled(bEditable);
		jtfThisPIN.setEnabled(bEditable);
		jtfPhone.setEnabled(bEditable);
		jtfAddress.setEnabled(bEditable);
		jtfThisCity.setEnabled(bEditable);
		jtfZip.setEnabled(bEditable);
		jtfCVINo.setEnabled(bEditable);
		cbSpecies.setEnabled(bEditable);
		jtfNumber.setEnabled(bEditable);
		jtfOtherPIN.setEditable(bEditable);
		jtfThisState.setFocusable(false);
		jtfThisState.setEnabled(bEditable);
		jtfThisPIN.setEditable(bEditable);
		jtfThisName.setEnabled(bEditable);
		jtfPhone.setEditable(bEditable);
		jtfAddress.setEditable(bEditable);
		jtfThisCity.setEditable(bEditable);
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
		this.bImport = bInbound;
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

	void jtfThisPIN_focusLost(FocusEvent e) {
		String sThisPremId = jtfThisPIN.getText();		
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
			MessageDialog.showMessage(this, 
						"Civet: Premises ID Error", sThisPremId + " is not a valid Premises ID", MessageDialog.OK_ONLY);
			bInSearch = false;
			return;
		}
		else {
			sThisPremId = sUpperPin;
		}
		if( bInSearch ) 
			return;
		bInSearch = true;
		// don't overwrite existing content UNLESS sticky is on so the content may be 
		// from last entry.
		if( ckSticky.isSelected() || (
			(jtfThisName.getText() == null || jtfThisName.getText().trim().length() == 0) &&
			(jtfAddress.getText() == null || jtfAddress.getText().trim().length() == 0) &&
			(jtfThisCity.getText() == null || jtfThisCity.getText().trim().length() == 0) &&
			(jtfZip.getText() == null || jtfZip.getText().trim().length() == 0) ) ) {
			// Sort out whether we have a PIN or a LID
			String sStatePremisesId = null;
			String sFedPremisesId = null;
			if( sThisPremId != null && sThisPremId.trim().length() == 7 )
				sFedPremisesId = sThisPremId;
			else {
				sStatePremisesId = sThisPremId;
				String sThisState = CivetConfig.getHomeStateAbbr();
				bLidFromHerds = isValidLid( sStatePremisesId, sThisState);
			}
			PremisesSearchDialog dlg = (PremisesSearchDialog)jtfThisPIN.getSearchDialog(); 
			if( dlg.exitOK() ) {
				jtfThisName.setText(dlg.getSelectedPremName());
				jtfAddress.setText(dlg.getSelectedAddress1());
				jtfThisCity.setText(dlg.getSelectedCity());
				jtfZip.setText(dlg.getSelectedZipCode());
				String sNewPhone = dlg.getSelectedPhone();
				if( jtfPhone.getText() == null || jtfPhone.getText().trim().length() == 0 ) {
					if( sNewPhone != null ) {
						jtfPhone.setText(sNewPhone);
					}
				}
				Component c = traversal.getComponentByName(traversal.getProperty("pPremiseFound"));
				if( c != null && c != jtfThisPIN )
					c.requestFocus();
			}
			else {
				// Note, this route is broken until search logic gets refined.
				UsaHerdsLookupPrems model;
				try {
					model = new UsaHerdsLookupPrems( sStatePremisesId, sFedPremisesId );
					if( model.getRowCount() == 1 ) {
						if( model.next() ) {
							jtfThisPIN.setText(sThisPremId);
							jtfThisName.setText(model.getPremName());
							jtfAddress.setText(model.getAddress1());
							jtfThisCity.setText(model.getCity());
							jtfZip.setText(model.getZipCode());
							String sNewPhone = model.getPhone();
							if( jtfPhone.getText() == null || jtfPhone.getText().trim().length() == 0 ) {
								if( sNewPhone != null ) {
									jtfPhone.setText(sNewPhone);
								}
							}
							Component c = traversal.getComponentByName(traversal.getProperty("pPremiseFound"));
							if( c != null && c != jtfThisPIN )
								c.requestFocus();
							
						}
					}
					else {
						MessageDialog.showMessage(this, "Civet: Error Premises Not Found", "Found " + model.getRowCount() + " premises for " + jtfThisPIN.getText());
					}
				} catch (WebServiceException e1) {
					MessageDialog.showMessage(this, "Civet: Error", "Web Service Failure\n" + e1.getMessage());
					logger.error("Web Service Failure", e1);
				}
			}
		}
		bInSearch = false;
	}

	void jtfPhone_focusLost(FocusEvent e) {
		if( !CivetConfig.isStandAlone() ) {
			String sPhone = jtfPhone.getText();
			if( sPhone == null || sPhone.trim().length() == 0 || sPhone.equals(sPriorPhone) ) return;
			PremisesSearchDialog dlg = new PremisesSearchDialog();
			try {
				dlg.searchPhone(sPhone);
				if( dlg.exitOK() ) {
					jtfThisName.setText(dlg.getSelectedPremName());
					jtfAddress.setText(dlg.getSelectedAddress1());
					jtfThisCity.setText(dlg.getSelectedCity());
					jtfZip.setText(dlg.getSelectedZipCode());
					String sPin = dlg.getSelectedFedPremId();
					if( sPin == null ) {
						sPin = dlg.getSelectedStatePremId();
						String sThisState = CivetConfig.getHomeStateAbbr();
						bLidFromHerds = isValidLid( sPin, sThisState);
					}
					jtfThisPIN.setText(sPin);
					Component c = traversal.getComponentByName(traversal.getProperty("pPremiseFound"));
					if( c != null)
						c.requestFocus();
				}
			} catch (WebServiceException e1) {
				MessageDialog.showMessage(this, "Civet: Error", "Web Service Failure\n" + e1.getMessage());
				logger.error("Web Service Failure", e1);
			}
		}
	}

	void jtfAddrCity_focusLost(FocusEvent e) {
		String sCity = jtfThisCity.getText();
		String sAddress = jtfAddress.getText();
		// don't bother if either address or city is blank or neither has changed.
		if( sAddress == null || sAddress.trim().length() == 0 || sCity == null || sCity.trim().length() == 0 
				|| (sCity.equals(sPriorCity) && sAddress.equals(sPriorAddress)) ) return;
		if(bInSearch) return;
		bInSearch = true;
		if( ckSticky.isSelected() || (
				(jtfThisPIN.getText() == null || jtfThisPIN.getText().trim().length() == 0) &&
				(jtfPhone.getText() == null || jtfPhone.getText().trim().length() == 0) &&
				(jtfZip.getText() == null || jtfZip.getText().trim().length() == 0)  ) ) {
			PremisesSearchDialog dlg = new PremisesSearchDialog();
			try {
				dlg.searchAddress( sAddress, sCity, null, null );
				if( dlg.exitOK() ) {
					if( jtfPhone.getText() == null || jtfPhone.getText().trim().length() == 0 )
						jtfPhone.setText(dlg.getSelectedPhone());
					jtfThisName.setText(dlg.getSelectedPremName());
					jtfAddress.setText(dlg.getSelectedAddress1());
					jtfThisCity.setText(dlg.getSelectedCity());
					jtfZip.setText(dlg.getSelectedZipCode());
					String sPin = dlg.getSelectedFedPremId();
					if( sPin == null ) {
						sPin = dlg.getSelectedStatePremId();
						String sThisState = CivetConfig.getHomeStateAbbr();
						bLidFromHerds = isValidLid( sPin, sThisState);
					}
					jtfThisPIN.setText(sPin);
					Component c = traversal.getComponentByName(traversal.getProperty("pPremiseFound"));
					if( c != null)
						c.requestFocus();
				}
			} catch (WebServiceException e1) {
				MessageDialog.showMessage(this, "Civet: Error", "Web Service Failure\n" + e1.getMessage());
				logger.error("Web Service Failure", e1);
			}
		}
		bInSearch = false;
	}
	
	private boolean isValidLid( String sLid, String sThisStateCode ) {
		if( sLid == null || sThisStateCode == null || sLid.trim().length() == 0 || sThisStateCode.trim().length() == 0 )
			return false;
		int iLidLen = sLid.trim().length();
		if( iLidLen != 6 && iLidLen != 8 )
			return false;
		return sLid.startsWith(sThisStateCode);
	}

	/**
	 * Clearing the form is complicated by various modes and settings.
	 */
	private void clearForm() {
		// clear the form and select main map if the check box is not checked or we just did an XFA
		if( !ckSticky.isSelected() || (controller.isLastSavedXFA() && getPageNo() == 1) ){
			traversal.selectMainMap();
			cbOtherState.setSelectedKey(-1);
			jtfOtherPIN.setText("");
			jtfOtherName.setText("");
			jtfOtherAddress.setText("");
			jtfOtherCity.setText("");
			jtfOtherZip.setText("");
			jtfThisPIN.setText("");
			jtfThisName.setText("");
			jtfPhone.setText("");
			jtfAddress.setText("");
			jtfThisCity.setText("");
			jtfZip.setText("");
			jtfDateInspected.setText("");
			jtfDateReceived.setText("");
			jtfCVINo.setText("");
			cbSpecies.setSelectedKey(-1);
			jtfNumber.setText("");
			cbIssuedBy.setSelectedKey(-1);
			jtfIssuedBy.setText("");
		}
		else {
			// Note: we don't switch to the alternate map right away when the check box is selected
			// because the form is still blank at that point.  Only when editing a box with 
			// sticky data included.
			traversal.selectAltMap();
		}
		if(rbInState.isSelected()) {
			setImport( false );
			cbOtherState.setSelectedValue(CivetConfig.getHomeState());
			cbOtherState.setEnabled(false);
			lIssuedBy.setVisible(true);
			lIssuedBy.setFont(new Font("Tahoma", Font.BOLD, 11));
			cbIssuedBy.setVisible(true);
			jtfIssuedBy.setVisible(false);
			ckAllVets.setVisible(true);
			Component c = traversal.getComponentByName(traversal.getProperty("pInstateFirst"));
			if( c != null)
				c.requestFocus();
		}
		if(rbExport.isSelected()) {
			lIssuedBy.setVisible(true);
			lIssuedBy.setFont(new Font("Tahoma", Font.BOLD, 11));
			cbIssuedBy.setVisible(true);
			jtfIssuedBy.setVisible(false);
			ckAllVets.setVisible(true);
			Component c = traversal.getComponentByName(traversal.getProperty("pFirstControl"));
			if( c != null)
				c.requestFocus();
		}
		if(rbImport.isSelected()) {
			lIssuedBy.setVisible(true);
			lIssuedBy.setFont(new Font("Tahoma", Font.PLAIN, 11));
			cbIssuedBy.setVisible(false);
			jtfIssuedBy.setVisible(true);
			ckAllVets.setVisible(false);
			Component c = traversal.getComponentByName(traversal.getProperty("pFirstControl"));
			if( c != null)
				c.requestFocus();
		}
		idListModel.clear();
		aErrorKeys = new ArrayList<String>();
		aSpecies = new ArrayList<SpeciesRecord>();
		lError.setVisible(false);
		bMultiSpecies = false;
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
	 * This will leave things in a inconsistent state because the controller won't know
	 * what is open.
	 * Never called currently.
	 * @param xStd
	 */
	public void openStdXml( StdeCviXml xStd ) {
		populateFromStdXml( xStd );
		controller.setStdXml( xStd );
		setMode(XML_MODE);
		OpenFileThread thread = new OpenFileThread( this, xStd );
		thread.start();
	}

	/**
	 * Populate dialog form from standard eCVI XML
	 * @param xStd StdeCviXml object representation of a CVI
	 */
	void populateFromStdXml( StdeCviXml xStd ) {
		if( xStd == null ) {
			logger.error(new Exception("populateFromStdXml called with null StdeCviXml"));
			return;
		}
		else {
			String sOriginState = xStd.getOriginState();
			if( sOriginState != null ) {
				sOriginState = States.getState(sOriginState);
				if( sOriginState.equalsIgnoreCase(CivetConfig.getHomeState()) ) {
					setImport(false);
					rbExport.setSelected(true);
					cbOtherState.setSelectedValue(States.getState(xStd.getDestinationState()));
					jtfOtherPIN.setText("");
					// For display purposes only!  Display person name if no prem name.
					String sOtherName = xStd.getDestinationPremName();
					if( sOtherName == null || sOtherName.trim().length() == 0 )
						sOtherName = xStd.getDestinationPersonName();
					jtfOtherPIN.setText(xStd.getDestinationPremId());
					jtfOtherName.setText(sOtherName);
					jtfOtherAddress.setText(xStd.getDestinationStreet());
					jtfOtherCity.setText(xStd.getDestinationCity());
					jtfOtherZip.setText(xStd.getDestinationZip());
					jtfThisPIN.setText("");
					String sThisName = xStd.getOriginPremName();
					if( sThisName == null || sThisName.trim().length() == 0 )
						sThisName = xStd.getOriginPersonName();
					jtfThisPIN.setText(xStd.getOriginPremId());
					jtfThisName.setText(sThisName);
					jtfPhone.setText(xStd.getOriginPhone());
					jtfAddress.setText(xStd.getOriginStreet());
					jtfThisCity.setText(xStd.getOriginCity());
					jtfZip.setText(xStd.getOriginZip());
					String sNAN = xStd.getVetNAN();
					if( sNAN != null && sNAN.trim().length() > 0 ) {
						VetLookup vet = new VetLookup( sNAN );
						cbIssuedBy.setSelectedKey(vet.getKey());
					}
					else {
						// May not be accredited.
						doShowAllVets( true );
						String sVetName = xStd.getVetName();
						StringTokenizer tok = new StringTokenizer(sVetName, ", ");
						String sVetLastName = null;
						String sVetFirstName = null;
						if(tok.hasMoreTokens())
							sVetLastName = tok.nextToken();
						if(tok.hasMoreTokens())
							sVetFirstName = tok.nextToken();
						VetLookup vet = new VetLookup( sVetLastName, sVetFirstName );
						if( vet.isUniqueMatch() )
							cbIssuedBy.setSelectedKey(vet.getKey());
					}
				}
				else {
					setImport(true);
					rbImport.setSelected(true);
					cbOtherState.setSelectedValue(States.getState(xStd.getOriginState()));
					jtfOtherPIN.setText("");
					// For display purposes only!  Display person name if no prem name.
					String sOtherName = xStd.getOriginPremName();
					if( sOtherName == null || sOtherName.trim().length() == 0 )
						sOtherName = xStd.getOriginPersonName();
					jtfOtherPIN.setText(xStd.getOriginPremId());
					jtfOtherName.setText(sOtherName);
					jtfOtherAddress.setText(xStd.getOriginStreet());
					jtfOtherCity.setText(xStd.getOriginCity());
					jtfOtherZip.setText(xStd.getOriginZip());
					jtfThisPIN.setText("");
					String sThisName = xStd.getDestinationPremName();
					if( sThisName == null || sThisName.trim().length() == 0 )
						sThisName = xStd.getDestinationPersonName();
					jtfThisPIN.setText(xStd.getDestinationPremId());
					jtfThisName.setText(sThisName);
					jtfPhone.setText(xStd.getDestinationPhone());
					jtfAddress.setText(xStd.getDestinationStreet());
					jtfThisCity.setText(xStd.getDestinationCity());
					jtfZip.setText(xStd.getDestinationZip());
					String sVetName = xStd.getVetName();
					jtfIssuedBy.setText(sVetName);

				}
				java.util.Date dIssueDate = xStd.getIssueDate();
				jtfDateInspected.setDate(dIssueDate);
				jtfCVINo.setText(xStd.getCertificateNumber());
				// Species multiples are an issue
				loadSpeciesFromStdXml(xStd);
				cbPurpose.setSelectedValue(xStd.getMovementPurpose());
				// Load data from included XmlMetaData "file"
				CviMetaDataXml meta = xStd.getMetaData();
				if( meta != null ) {
					// Make no assumption about received date.
					java.util.Date dReceived = meta.getBureauReceiptDate();
					jtfDateReceived.setDate(dReceived);
					ArrayList<String> aErrors = meta.listErrors();
					if( aErrors == null ) {
						aErrorKeys = new ArrayList<String>();
						lError.setVisible(false);
					}
					else {
						aErrorKeys = aErrors;
						if( aErrorKeys.size() > 0 )
							lError.setVisible(true);
						else
							lError.setVisible(false);
					}
					sErrorNotes = meta.getErrorNote();
					
				}
				else {
					// Make no assumption about received date.
					jtfDateReceived.setText("");
					aErrorKeys = new ArrayList<String>();
					lError.setVisible(false);					
				}
				Component c = traversal.getComponentByName(traversal.getProperty("pPDFLoaded"));
				if( c != null)
					c.requestFocus();
			}
		}
	}

	/**
	 * Subroutine for above, just to keep the populateFromStdXml method somewhat sane
	 * @param std StdeCviXml object being loaded.
	 */
	private void loadSpeciesFromStdXml(StdeCviXml std) {
		aSpecies = new ArrayList<SpeciesRecord>();
		NodeList animals = std.listAnimals();
		int iKey = -1;
		for( int i = 0; i < animals.getLength(); i++ ) {
			String sSpecies = std.getSpeciesCode(animals.item(i));
			String sAnimalID = std.getAnimalID(animals.item(i));
			boolean bSet = false;
			SpeciesLookup sppLookup = new SpeciesLookup( sSpecies );
			int iNextKey = sppLookup.getKey();
			if( iKey < 0 )
				iKey = iNextKey;
			String sSpeciesValue = sppLookup.getSpeciesName();
			if( iNextKey > 0 && sAnimalID != null && sAnimalID.trim().length() > 0 ) {
				idListModel.addRow(iKey, sSpeciesValue, sAnimalID);
			}
			if( aSpecies.size() > 0 ) {
				for( SpeciesRecord r : aSpecies ) {
					if( r.iSpeciesKey == iNextKey ) {
						r.iNumber++;
						bSet = true;
						break;
					}
				}
			}
			if( !bSet ) {
				SpeciesRecord sp = new SpeciesRecord( iNextKey, 1 );
				aSpecies.add(sp);
			}
		}
		NodeList groups = std.listGroups();
		for( int i = 0; i < groups.getLength(); i++ ) {
			String sSpecies = std.getSpeciesCode(groups.item(i));
			int iQuantity = std.getQuantity(groups.item(i));
			int iNextKey = -1;
			SpeciesLookup sppLookup = new SpeciesLookup( sSpecies );
			iNextKey = sppLookup.getKey();
			if( iKey < 0 )
				iKey = iNextKey;
			boolean bSet = false;
			if( aSpecies.size() > 0 ) {
				for( SpeciesRecord r : aSpecies ) {
					if( r.iSpeciesKey == iNextKey ) {
						r.iNumber += iQuantity;
						bSet = true;
						break;
					}
				}
				if( bSet )
					break;
			}
			if( !bSet ) {
				SpeciesRecord sp = new SpeciesRecord( iNextKey, iQuantity );
				aSpecies.add(sp);
			}
		}

		int iMax = 0;
		for( SpeciesRecord r : aSpecies ) {
			if( r.iNumber > iMax ) {
				iMax = r.iNumber;
				iKey = r.iSpeciesKey;
			}
		}
		cbSpecies.setSelectedKey(iKey);
		jtfNumber.setText(Integer.toString(iMax));
		bMultiSpecies = (aSpecies.size() > 1);
		lMultipleSpecies.setVisible( bMultiSpecies );

	}
	
	/**
	 * Convert CO/KS xml to standard and extract data from there.
	 * "Virtual" parameter PDF File loaded in pdfDecoder.
	 */
	void populateFromPDF() {
		if( !CivetConfig.isJPedalXFA() ) {
			logger.error("populateFromPDF called without XFA document", new Exception("Civet Error"));
			return;
		}
		AcroRenderer rend = pdfDecoder.getFormRenderer();
		if( rend.isXFA() ) {
			Node xmlNode = rend.getXMLContentAsNode(PdfDictionary.XFA_DATASET);
			CoKsXML coks = new CoKsXML( xmlNode );
			StdeCviXml std = coks.getStdeCviXml();
			if( std == null ) {
				MessageDialog.messageWait(this, "Civet Error:", "Could not convert PDF content to USAHA Standard XML using XSLT\n" +
												CivetConfig.getCoKsXSLTFile());
			}
			else {
				populateFromStdXml( std );
			}
		}
	}
	

	void setupSaveButtons() {
		if( isReopened() ) {
			bAddToLast.setVisible(false);			
		}
		else if( controller.isXFADocument() || controller.isLastSavedXFA()) {
			bAddToLast.setVisible(false);			
		}
		else if( controller.getLastSavedFile() == null ){
			bAddToLast.setVisible(false);
		}
		else if( iMode != PDF_MODE ) {
			bAddToLast.setVisible(false);
		}
		else {
			bAddToLast.setVisible(true);
		}
		if( controller.hasLastSaved() ) {
			bEditLast.setEnabled(true);
		}
		else {
			bEditLast.setEnabled(false);
		}
	}
	
	private void doSave() {
		// If save() finds errors be sure form is editable so they can be corrected.
		if( !save() )
			setFormEditable( true );
	}

	/** 
	 * Gather values from dialog controls and dispatch to appropriate insert or update thread.
	 */
	boolean save() {
		controller.addCurrentPage();
		// Collect up all values needed
		setImport(rbImport.isSelected());
		boolean bInbound = rbImport.isSelected();
		String sOtherState = cbOtherState.getSelectedValue();
		String sOtherStateCode = States.getStateCode(sOtherState);
		String sOtherName = jtfOtherName.getText();
		String sOtherAddress = jtfOtherAddress.getText();
		String sOtherCity = jtfOtherCity.getText();
		String sOtherZipcode = jtfOtherZip.getText();
		String sOtherPIN = jtfOtherPIN.getText();
		// Only save actual PINs for other state and only save PINs or HERDS State PremIds (they aren't really LIDS!)
		if( sOtherPIN != null && sOtherPIN.trim().length() != 7 )
			sOtherPIN = null;
		String sThisPremisesId = jtfThisPIN.getText();
		if( sThisPremisesId != null && sThisPremisesId.trim().length() != 7 && !bLidFromHerds )
			sOtherPIN = null;
		String sPhone = jtfPhone.getText();
		String sThisName = jtfThisName.getText();
		String sStreetAddress = jtfAddress.getText();
		String sCity = jtfThisCity.getText();
		String sZipcode = jtfZip.getText();
		java.util.Date dDateIssued = jtfDateInspected.getDate();
		java.util.Date dDateReceived = jtfDateReceived.getDate();
		// need to incorporate.  Wait for XML upload?
		String sMovementPurpose = cbPurpose.getSelectedValue();
		Integer iIssuedByKey = cbIssuedBy.getSelectedKeyInt();
		String sIssuedByName = jtfIssuedBy.getText();
		if( iIssuedByKey == null && sIssuedByName == null ) {
			MessageDialog.showMessage(this, "Civet Error", "Issuing Veterinarian is required");
			return false;
		}
		String sCVINo = jtfCVINo.getText();
		updateSpeciesList(false);
		StdeCviXml stdXml = controller.getStdXml();
		if( controller.isXFADocument() ) {
			AcroRenderer rend = getPdfDecoder().getFormRenderer();
			byte[] xmlBytes = rend.getXMLContentAsBytes(PdfDictionary.XFA_DATASET);
			CoKsXML coks = new CoKsXML( xmlBytes );
			stdXml = coks.getStdeCviXml();
		}
		if( sOtherState == null || sOtherState.trim().length() == 0 || aSpecies.size() == 0 
				|| dDateIssued == null || dDateReceived == null ) {
			String sFields = "";
			if( sOtherState == null || sOtherState.trim().length() == 0 ) 
				sFields += " Other State,";
			if( aSpecies.size() == 0 ) 
				sFields += " Species or Number,";
			if( dDateIssued == null ) 
				sFields += " Date Issued,";
			if( dDateReceived == null ) 
				sFields += " Date Received,";
			MessageDialog.showMessage(this, "Civet Error", "One or more required fields:" + sFields + " are empty");
			return false;
		}
		// Setup the two variables that combine to tell what to save where.
		// XFAPdf or Image files send and save the original file contents (bytes null, file populated)
		// Pageable Pdf send and save extracted bytes (bytes populated, file null)
		// Image file send Pdf, save original (both populated)
		// 
		byte[] bAttachmentBytes = null;
		String sAttachmentFileName = null;
		File fAttachmentFile = null;
		if( stdXml != null ) {
			sAttachmentFileName = stdXml.getOriginalCVIFileName();
			if( sAttachmentFileName != null ) {
				bAttachmentBytes = stdXml.getOriginalCVI();
			}
			else {
				fAttachmentFile = controller.getCurrentFile();
				sAttachmentFileName = fAttachmentFile.getName();
			}
		}
		else {
			if( controller.isPageable() ) {
				bAttachmentBytes = controller.extractPagesToNewPDF();
				// leave filename to save thread
			}
			else {
				bAttachmentBytes = controller.getCurrentPdfBytes();
				sAttachmentFileName = controller.getCurrentFileName();
			}
		}
		SaveCVIThread thread = new SaveCVIThread(this, stdXml, bAttachmentBytes,
				sAttachmentFileName, fAttachmentFile, bInbound, sOtherStateCode,
				sOtherName, sOtherAddress, sOtherCity, sOtherZipcode, sOtherPIN,
				sThisPremisesId, sThisName, sPhone,
				sStreetAddress, sCity, sZipcode,
				dDateIssued, dDateReceived, iIssuedByKey, sIssuedByName, sCVINo,
				sMovementPurpose,
				aSpecies,
				aErrorKeys, sErrorNotes,
				idListModel.getRows()	);
		if( isReopened() )
			thread.setNoEmail();
		thread.start();
		return true;
	}
		
	public void saveComplete() {
		controller.setCurrentPagesComplete();
		controller.clearCurrentPages();
		clearForm();
		if( iMode == XML_MODE ) {
			allFilesDone();
			return;
		}
		if (controller.moreIncompleteForward()) {
			controller.moveToNextIncompletePage();
		}
		else if (controller.moreIncompleteBack()) {
			controller.moveToPreviousIncompletePage();
		}
		else {
			allFilesDone();
		}
	}

	private void minimizeAll() {
		this.setExtendedState(Frame.ICONIFIED);
		if( parent instanceof Frame )
			((Frame)parent).setExtendedState(Frame.ICONIFIED);
	}

	void rbOutbound_actionPerformed(ActionEvent e) {
		setImport( false );
		cbOtherState.setSelectedIndex(0);
		cbOtherState.setEnabled(true);
		Component c = traversal.getComponentByName(traversal.getProperty("pFirstControl"));
		if( c != null)
			c.requestFocus();
	}

	void rbInbound_actionPerformed(ActionEvent e) {
		setImport( true );
		cbOtherState.setSelectedIndex(0);
		cbOtherState.setEnabled(true);
		Component c = traversal.getComponentByName(traversal.getProperty("pFirstControl"));
		if( c != null)
			c.requestFocus();

	}

	void rbInState_actionPerformed(ActionEvent e) {
		setImport( false );
		cbOtherState.setSelectedValue(CivetConfig.getHomeState());
		cbOtherState.setEnabled(false);
		lIssuedBy.setVisible(true);
		cbIssuedBy.setVisible(true);
		ckAllVets.setVisible(true);
		Component c = traversal.getComponentByName(traversal.getProperty("pInstateFirst"));
		if( c != null)
			c.requestFocus();
	}

	void pdfView() {
		PDFOpener opener = new PDFOpener(this);
		// If the file is really a PDF, open it from disk so even things like CO/KS open natively.
		if( controller.isPageable() & !controller.isXFADocument() ) {
			opener.openPageContentInAcrobat(controller.getCurrentFilePath(), controller.getCurrentPageNo());
		}
		// Otherwise use the content converted to PDF earlier
		else {
			opener.openPDFContentInAcrobat(controller.getCurrentPdfBytes());
		}
	}
	

	void pdfView( byte pdfBytes[] ) {
		PDFOpener opener = new PDFOpener(this);
		opener.openPDFContentInAcrobat(pdfBytes);
	}


	private void updateSpeciesList( boolean bClear )  {
		String sNum = jtfNumber.getText();
		int iSpecies = cbSpecies.getSelectedKeyInt();
		if( iSpecies >= 0 ) {
			int iNum = -1;
			try {
				iNum = Integer.parseInt(sNum);
			}
			catch (NumberFormatException nfe) {
				// Will trigger later error.
				return;
			}
			SpeciesRecord rSpecies = new SpeciesRecord(iSpecies, iNum);
			if( aSpecies.contains(rSpecies) ) {
				aSpecies.remove(rSpecies);
			}
			aSpecies.add(rSpecies);
			if( bClear ) {
				jtfNumber.setText("");
				cbSpecies.setSelectedItem(null);
			}
		}
		if (aSpecies.size() > 1 || (aSpecies.size() == 1 && bClear) )
			lMultipleSpecies.setVisible(true);
		else
			lMultipleSpecies.setVisible(false);
	}

	/**
	 * @return the bImport
	 */
	public boolean isInbound() {
		return bImport;
	}

	private void runErrorDialog() {
		if( aErrorKeys == null )
			aErrorKeys = new ArrayList<String>();
		CVIErrorDialog dlgErrorDialog = new CVIErrorDialog( this, aErrorKeys, sErrorNotes );
		dlgErrorDialog.setVisible(true);
		if( dlgErrorDialog.isExitOK() ) {
			sErrorNotes = dlgErrorDialog.getNotes();
			logger.info( "Error note: " + sErrorNotes);
			for( String sError : aErrorKeys ) {
				logger.info("Error key " + sError + " noted");
			}
			// Process the error list and notes
			if( aErrorKeys.size() > 0 ) {
				lError.setVisible(true);
			}
			else {
				lError.setVisible(false);
			}
		}
	}
}// End Class CVIPdfEdit
