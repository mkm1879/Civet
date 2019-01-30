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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.beans.Beans;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.jpedal.exception.PdfException;

import edu.clemson.lph.civet.files.OpenFile;
import edu.clemson.lph.civet.files.OpenFileList;
import edu.clemson.lph.civet.files.SourceFileException;
import edu.clemson.lph.civet.lookup.CertificateNbrLookup;
import edu.clemson.lph.civet.lookup.Counties;
import edu.clemson.lph.civet.lookup.ErrorTypeLookup;
import edu.clemson.lph.civet.lookup.LocalPremisesTableModel;
import edu.clemson.lph.civet.lookup.PurposeLookup;
import edu.clemson.lph.civet.lookup.SpeciesLookup;
import edu.clemson.lph.civet.lookup.States;
import edu.clemson.lph.civet.lookup.VetLookup;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.threads.OpenFilesThread;
import edu.clemson.lph.civet.threads.SaveCVIThread;
import edu.clemson.lph.civet.webservice.PremisesSearchDialog;
import edu.clemson.lph.civet.webservice.PremisesTableModel;
import edu.clemson.lph.civet.webservice.UsaHerdsLookupPrems;
import edu.clemson.lph.civet.webservice.VetSearchDialog;
import edu.clemson.lph.civet.webservice.WebServiceException;
import edu.clemson.lph.civet.xml.CviMetaDataXml;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.civet.xml.elements.Animal;
import edu.clemson.lph.civet.xml.elements.GroupLot;
import edu.clemson.lph.civet.xml.elements.SpeciesCode;
import edu.clemson.lph.civet.xml.elements.Veterinarian;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.OneLineQuestionDialog;
import edu.clemson.lph.dialogs.YesNoDialog;
import edu.clemson.lph.pdfgen.PDFOpener;
import edu.clemson.lph.pdfgen.PDFViewer;
import edu.clemson.lph.utils.CountyUtils;
import edu.clemson.lph.utils.FileUtils;
import edu.clemson.lph.utils.PremCheckSum;


public final class CivetEditDialogController {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private CivetEditDialog dlg;
	private String viewerTitle="Civet: ";
	private Window parent = null;

	ArrayList<SpeciesRecord> aSpecies = new ArrayList<SpeciesRecord>();
	HashMap<String,String> mSpeciesChanges;
	AnimalIDListTableModel idListModel = null; //new AnimalIDListTableModel();
	ArrayList<String> aErrorKeys;
	String sErrorNotes;
	
	private String sPriorPhone;
	private String sPriorAddress;
	private String sPriorCity;
	private boolean bLidFromHerds = false;
	
	private PremisesSearchDialog premSearch = new PremisesSearchDialog();

	boolean bGotoLast = false; // Flag to open thread to goto last page when finished loading.
	private String sPrevCVINo;
	private String sPreviousSpecies;
	private boolean bSppEntered = false;
	private boolean bInSppChangeByCode = false;
	boolean bInSearch = false;
	private FormEditListener formEditListener = null;
	private ArrayList<File> filesToOpen = null;
	private OpenFileList openFileList = null;
	private OpenFile currentFile = null;
	private File fLastSaved = null;
	private PDFViewer viewer = null;

	private String sDefaultPurpose;
	
	CivetEditOrderTraversalPolicy traversal;

	VetLookup vetLookup;

	/**
	 * construct an empty pdf viewer and pop up the open window
	 * @throws SourceFileException 
	 * @wbp.parser.constructor
	 */
	public CivetEditDialogController( CivetEditDialog dlg, ArrayList<File> files ) throws SourceFileException{
		this.dlg = dlg;
		this.filesToOpen = files;
		this.viewer = new PDFViewer();
		viewer.alterRotation(CivetConfig.getRotation());  // Not sure why inverted relative to acrobat.
		// Run various initialization routines that are not dependent on current file
		initializeActionHandlers();
		initializeDBComponents();
		addFormChangeListeners();
	}
	
	/** 
	 * Call from CivetEditDialog once constructor has returned
	 * but before the dialog is made visible.  Probably not truly thread-safe
	 * but helps, I hope.
	 */
	public void openFiles() {
		// Encapsulation breaks down here.  Viewer is too resource intensive to 
		// create anew when needed for metadata, etc.
		openFileList = new OpenFileList(viewer);
		OpenFilesThread t = new OpenFilesThread(this, filesToOpen, openFileList);
		t.start();
	}
	
	/**
	 * Called on the event dispatch thread after the OpenFilesThread completes.
	 */
	public void openFilesComplete() {
		try {
			if( openFileList == null ) {
				MessageDialog.showMessage(dlg, "Civet Error: Empty File List", "Open files returned an empty list");
				logger.error("Open files returned an empty list");
				dlg.setVisible(false);
			}
			else {
				openFileList.firstFile();
				dlg.setViewer(viewer);
			}
		} catch (PdfException e) {
			// TODO Auto-generated catch block
			logger.error(e);
			e.printStackTrace();
		}
		iCurrentSaveMode = OPEN;
		setupFilePage();
	}
		
	private void setupFilePage() {
		try {
		currentFile = openFileList.getCurrentFile();
		idListModel = new AnimalIDListTableModel(currentFile.getModel());
		setAllFocus();
		dlg.setTitle(getViewerTitle()+currentFile.getSource().getFileName());
		setPage(currentFile.getCurrentPageNo());
		setPages(currentFile.getPageCount());
		setFile(openFileList.getCurrentFileNo());
		setFiles(openFileList.getFileCount());
		dlg.setFormEditable(dlg.iMode != CivetEditDialog.VIEW_MODE);
		updateCounterPanel();
		setupSaveLogic();	
		viewer.viewPage(currentFile.getCurrentPageNo()); 
		clearForm();
		viewer.updatePdfDisplay();
		if( currentFile.getSource().isDataFile() )
			populateFromStdXml(currentFile.getSource().getDataModel()) ;
		dlg.make90Percent();
		dlg.setVisible(true);  // Only now display
		} catch( Exception e ) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	public CivetEditDialog getDialog() {
		return dlg;
	}
	
	public CivetEditDialog getDialogParent() {
		return dlg.getDialogParent();
	}
	
	/**
	 * This is misnamed now that we have no actual database connection.
	 * The idea is to isolate those actions that interfere with GUI design.
	 * Now mostly pulled from lookup tables filled via web service.
	 */
	private void initializeDBComponents() {
		if( !Beans.isDesignTime() ) {
			try {
				setTraversal();
				String sDirection = CivetConfig.getDefaultDirection();
				if( "Import".equalsIgnoreCase(sDirection) ) {
					dlg.rbImport.setSelected(true);
					dlg.setImport(true);
				}
				else {
					dlg.rbExport.setSelected(true);
					dlg.setImport(false);
				}

				dlg.cbOtherState.setModel( new States() );
				dlg.cbOtherState.setBlankDefault(true);
				dlg.cbOtherState.refresh();
				
				dlg.cbSpecies.setModel( new SpeciesLookup() );
				dlg.cbSpecies.setBlankDefault(true);
				dlg.cbSpecies.refresh();
				mSpeciesChanges = new HashMap<String, String>();
				
				vetLookup = new VetLookup();
				vetLookup.setLevel2Check(true);
				vetLookup.setExpCheck(true);
				dlg.cbIssuedBy.setModel(vetLookup);
				dlg.cbIssuedBy.setSearchDialog( new VetSearchDialog() );
				dlg.cbIssuedBy.setHideCode(true);
				dlg.cbIssuedBy.setToolTipText("CTRL F to Search for name");
				dlg.cbIssuedBy.setSearchTitle("Civet Search: Veterinarian");
				dlg.cbIssuedBy.setBlankDefault(true);
				dlg.cbIssuedBy.refresh();
			    
				dlg.cbPurpose.setModel( new PurposeLookup() );
				dlg.cbPurpose.refresh();
				sDefaultPurpose = CivetConfig.getDefaultPurpose();
				if( sDefaultPurpose != null )
					dlg.cbPurpose.setSelectedItem(sDefaultPurpose);
				refreshThisCounties();

			}
			catch( Exception e ) {
				logger.error(e.getMessage() + "\nError loading values from lookup tables",e);
				MessageDialog.showMessage(dlg, "Civet Error: Database", "Error loading values from lookup tables" );
				e.printStackTrace();
			}
		}
		else {
		    dlg.setSize(new Dimension(732, 819));

		}
	}

	/**
	 * Called from constructor to isolate all the add****Handler verbosity
	 */
	private void initializeActionHandlers() {
		dlg.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);		
		dlg.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if( dlg.iMode == CivetEditDialog.VIEW_MODE || YesNoDialog.ask(dlg, "Civet: Close", "Close without saving?") ) {
					doCleanup();
					dlg.setVisible(false);
					dlg.dispose();
				}
			}
		});
		dlg.mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doSaveOptions();
			}
		});
		dlg.mntmClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cancel();
			}
		});
		dlg.mntmMinimizeAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				minimizeAll();
			}
		});
		dlg.mntmRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dlg.viewer.updatePdfDisplay();
			}
		});
		dlg.bBigger.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dlg.viewer.alterScale( 1.1f );
				dlg.viewer.updatePdfDisplay();
			}
		});
		dlg.bSmaller.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dlg.viewer.alterScale( 1.0f/1.1f );
				dlg.viewer.updatePdfDisplay();
			}
		});
		dlg.bRotate.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dlg.viewer.alterRotation(90);
				dlg.viewer.updatePdfDisplay();
			}
		});
		dlg.bPDFView.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pdfView();
			}
		});
		dlg.bPDFViewFile.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pdfViewFile();
			}
		});
		dlg.bGotoPage.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doGotoPage();
			}
		});
		/*
		 * The page navigation panel has lots of click events.
		 */
		{
			dlg.pCounters.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					doPickPage(e);
				}
			});
			dlg.pCounters.bFileBack.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						if( openFileList.moreFilesBack(false) ) {
							openFileList.fileBackward(false);
							setupFilePage();
						}
					} catch (PdfException e1) {
						// TODO Auto-generated catch block
						logger.error(e1);
					}
				}
			});
			dlg.pCounters.bPageBack.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						if( currentFile.morePagesBack(false) )
							currentFile.pageBackward(false);
						else if( openFileList.moreFilesBack(false) ) {
							openFileList.fileBackward(false);
						}
						else {
							logger.error("Attempt to move past first page");
						}
						setupFilePage();
					} catch (SourceFileException | PdfException e1) {
						// TODO Auto-generated catch block
						logger.error(e1);
					}
				}
			});
			dlg.pCounters.fileCounter1.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					doPickPage(e);
				}
			});
			dlg.pCounters.fileCounter2.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					doPickPage(e);
				}
			});
			dlg.pCounters.pageCounter1.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					doPickPage(e);
				}
			});
			dlg.pCounters.pageCounter2.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					doPickPage(e);
				}
			});
			dlg.pCounters.bPageForward.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						if( currentFile.morePagesForward(false) )
							currentFile.pageForward(false);
						else if( openFileList.moreFilesForward(false) ) {
							openFileList.fileForward(false);
						}
						else {
							logger.error("Attempt to move past last page");
						}
						setupFilePage();
					} catch (SourceFileException | PdfException e1) {
						// TODO Auto-generated catch block
						logger.error(e1);
						e1.printStackTrace();
					}
				}
			});
			dlg.pCounters.bFileForward.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						openFileList.fileForward(false);
						setupFilePage();
					} catch (PdfException e1) {
						// TODO Auto-generated catch block
						logger.error(e1);
					}
				}
			});
		}
		dlg.bEditLast.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doEditLast();
			}
		});
		dlg.rbImport.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rbInbound_actionPerformed(e);
			}
		});
		dlg.rbExport.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rbOutbound_actionPerformed(e);
			}
		});
		dlg.rbInState.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rbInState_actionPerformed(e);
			}
		});
		dlg.cbOtherState.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				refreshOtherCounties();
			}
		});
		dlg.jtfOtherZip.addFocusListener( new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
			}
			@Override
			public void focusLost(FocusEvent arg0) {
				checkZipcode( dlg.jtfOtherZip );
				String sZip = dlg.jtfOtherZip.getText();
				if( sZip != null && sZip.trim().length() > 0 ) {
					try {
						String sZipCounty = CountyUtils.getCounty(sZip);
						String sHerdsCounty = Counties.getHerdsCounty(dlg.cbOtherState.getSelectedCode(), sZipCounty);
						dlg.cbOtherCounty.setSelectedItem(sHerdsCounty);
					} catch (IOException e) {
						logger.error(e);
					}
				}
			}
		});
		dlg.jtfPhone.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(FocusEvent e) {
				jtfPhone_focusLost(e);
			}
			public void focusGained(FocusEvent e) {
				sPriorPhone = dlg.jtfPhone.getText();
			}
		});
		dlg.jtfThisPIN.setSearchDialog( premSearch );
		dlg.jtfThisPIN.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(FocusEvent e) {
				jtfThisPIN_focusLost(e);
			}
		});
		dlg.jtfAddress.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(FocusEvent e) {
				jtfAddrCity_focusLost(e);
			}
			public void focusGained(FocusEvent e) {
				sPriorAddress = dlg.jtfAddress.getText();
			}
		});
		dlg.lThisCity.addMouseListener( new MouseAdapter() {
			public void mouseClicked(MouseEvent e)  
		    {  
		       String sValue = dlg.lThisCity.getText();
		       if( "City:".equals(sValue)) {
		    	   dlg.lThisCity.setText("County:");
		    	   dlg.cbThisCounty.setVisible(true);
		    	   dlg.jtfThisCity.setVisible(false);
		       }
		       else {
		    	   dlg.lThisCity.setText("City:");
		    	   dlg.cbThisCounty.setVisible(false);
		    	   dlg.jtfThisCity.setVisible(true);
		       }
		    }  
		});
		dlg.jtfZip.addFocusListener( new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
			}
			@Override
			public void focusLost(FocusEvent arg0) {
				checkZipcode( dlg.jtfZip );
				if( dlg.cbThisCounty.getSelectedItem() == null || ((String)dlg.cbThisCounty.getSelectedItem()).trim().length() == 0 ) {
					String sZipCounty;
					try {
						sZipCounty = CountyUtils.getCounty(dlg.jtfZip.getText());
						if( sZipCounty != null ) {
							String sHerdsCounty = Counties.getHerdsCounty(dlg.jtfThisState.getText(), sZipCounty);
							dlg.cbThisCounty.setSelectedItem(sHerdsCounty);
						}
					} catch (IOException e) {
						logger.error(e);
					}
				}
			}
		});
		dlg.jtfThisCity.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(FocusEvent e) {
				jtfAddrCity_focusLost(e);
			}
			public void focusGained(FocusEvent e) {
				sPriorCity = dlg.jtfThisCity.getText();
			}
		});
		// This pair of handlers is a complicated way to detect if the selected species
		// has been changed from a previously recorded value.  Most of the logic is
		// simply NOT triggering a change for harmless things like clearing the form.
		dlg.cbSpecies.addFocusListener( new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				doCheckSpeciesChange1();
				
			}
		});
		dlg.cbSpecies.addItemListener( new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				doCheckSpeciesChange2();
			}
		});
		dlg.bAddSpecies.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateSpeciesList( true );
			}
		});
		dlg.jtfDateInspected.addFocusListener( new java.awt.event.FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if( dlg.jtfDateInspected.isAcceptedDate() )
					checkPreDate();
			}
		});
		dlg.jtfDateReceived.addFocusListener( new java.awt.event.FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if( dlg.jtfDateReceived.isAcceptedDate() )
					checkPreDate();
			}
		});
		dlg.jtfCVINo.addFocusListener( new FocusListener() {
			public void focusGained(FocusEvent arg0) {
			}
			@Override
			public void focusLost(FocusEvent arg0) {
				String sCertNbr = dlg.jtfCVINo.getText();
				if( CertificateNbrLookup.certficateNbrExists(sCertNbr) ) {
					MessageDialog.showMessage(dlg, "Civet Error", "Certificate number " + sCertNbr + " already exists");
					dlg.jtfCVINo.requestFocus();
				}
				String sOtherStateCode = States.getStateCode(dlg.cbOtherState.getSelectedValue());
				boolean bInbound = dlg.rbImport.isSelected();
				if( !isReopened() ) {
					if( CertificateNbrLookup.certficateNbrExistsThisSession(sCertNbr, sOtherStateCode, bInbound) ) {
						MessageDialog.showMessage(dlg, "Civet Error", "Certificate Error " + sCertNbr + " has already been saved but not uploaded.\n" +
								"Resolve and try again.");
						dlg.jtfCVINo.requestFocus();
					}
				}
			}
		});
		dlg.ckAllVets.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean bChecked = dlg.ckAllVets.isSelected();
				doShowAllVets(!bChecked);
			}
		});
		dlg.bError.addActionListener( new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runErrorDialog();
			}
		});
		dlg.bPageOptions.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doPageOptions();
			}
		});
		dlg.bAddIDs.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doAddIDs();
			}
		});
		dlg.bSave.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSaveOptions();
			}
		});

	}
	
	/**
	 * Add formChangeListener to all editable components
	 */
	private void addFormChangeListeners() {
		formEditListener = new FormEditListener();
		dlg.cbOtherState.addItemListener(formEditListener);
//		dlg.jtfOtherPIN.getDocument().addDocumentListener(formEditListener);
		dlg.jtfOtherName.getDocument().addDocumentListener(formEditListener);
		dlg.jtfOtherAddress.getDocument().addDocumentListener(formEditListener);
		dlg.jtfOtherCity.getDocument().addDocumentListener(formEditListener);
		dlg.cbOtherCounty.setSelectedItem(null);
		dlg.jtfOtherZip.getDocument().addDocumentListener(formEditListener);
		dlg.jtfThisPIN.getDocument().addDocumentListener(formEditListener);
		dlg.jtfThisName.getDocument().addDocumentListener(formEditListener);
		dlg.jtfPhone.getDocument().addDocumentListener(formEditListener);
		dlg.jtfAddress.getDocument().addDocumentListener(formEditListener);
		dlg.jtfThisCity.getDocument().addDocumentListener(formEditListener);
		dlg.cbThisCounty.setSelectedItem(null);
		dlg.jtfZip.getDocument().addDocumentListener(formEditListener);
		dlg.jtfDateInspected.getDocument().addDocumentListener(formEditListener);
		dlg.jtfDateReceived.getDocument().addDocumentListener(formEditListener);
		dlg.jtfCVINo.getDocument().addDocumentListener(formEditListener);
		dlg.cbSpecies.addItemListener(formEditListener);
		dlg.jtfNumber.getDocument().addDocumentListener(formEditListener);
		dlg.cbIssuedBy.addItemListener(formEditListener);
		dlg.jtfIssuedBy.getDocument().addDocumentListener(formEditListener);
		dlg.cbPurpose.setSelectedItem( sDefaultPurpose );
		dlg.rbInState.addItemListener(formEditListener);
		dlg.jtfIssuedBy.getDocument().addDocumentListener(formEditListener);
		dlg.rbImport.addItemListener(formEditListener);
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
		dlg.jtfOtherName.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				selectAll(e);
			}
		});
		dlg.jtfOtherAddress.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				selectAll(e);
			}
		});
		dlg.jtfOtherCity.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				selectAll(e);
			}
		});
		dlg.jtfOtherZip.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				selectAll(e);
			}
		});
		dlg.jtfThisPIN.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dlg.jtfThisPIN.selectAll();
			}
		});
		dlg.jtfPhone.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				selectAll(e);
			}
		});
		dlg.jtfThisName.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				selectAll(e);
			}
		});
		dlg.jtfAddress.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				selectAll(e);
			}
		});
		dlg.jtfThisCity.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				selectAll(e);
			}
		});
		dlg.jtfZip.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				selectAll(e);
			}
		});
		dlg.jtfNumber.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				selectAll(e);
			}
		});
		dlg.jtfDateInspected.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				selectAll(e);
			}
		});
		dlg.jtfCVINo.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				selectAll(e);
			}
		});
		dlg.jtfDateReceived.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				selectAll(e);
			}
		});
	}

	private void setTraversal() {
		traversal = new CivetEditOrderTraversalPolicy( dlg );
		traversal.loadComponentOrderMaps();
		traversal.selectMainMap();
		traversal.setFirstComponent("OtherState");
		dlg.setFocusTraversalPolicy(traversal);
	}

	private static final int OPEN = 0;
	private static final int ONEPAGE = 1;
	private static final int NOSPLITMORE = 2;
	private static final int LASTPAGE = 3;
	private static final int SPLITMORE = 4;
	private static final int CHOOSE = 5;
	private static final int CHOSEADD = 6;
	private static final int CHOSENEW = 7;
	private static final int CHOSEVIEW = 8;
	private static final int SAVED = 9;
	private int iCurrentSaveMode = ONEPAGE;
	
	/**
	 * Complex logic around saving versus adding pages
	 * if multipage PDF and there are more pages forward, delay saving until next page is viewed
	 * Save = Next Page
	 * Add to Last = invisible unless in preview or Add Page to Previous
	 * This method determines appropriate mode and then calls setupSaveButtons
	 */
	private void setupSaveLogic() {
		switch(iCurrentSaveMode) {
		case OPEN:
		case CHOSEADD:
		case CHOSENEW:
		case SAVED:
			if( currentFile.getPageCount() == 1 )
				iCurrentSaveMode = ONEPAGE;
			else if( currentFile.getCurrentPageNo() == currentFile.getPageCount() )
				iCurrentSaveMode = LASTPAGE;
			else if( !currentFile.getSource().canSplit() && currentFile.getCurrentPageNo() != currentFile.getPageCount() )
				iCurrentSaveMode = NOSPLITMORE;
			else if( currentFile.getSource().canSplit() && currentFile.getCurrentPageNo() != currentFile.getPageCount() )
				iCurrentSaveMode = SPLITMORE;
			break;
		case CHOSEVIEW:
			if( currentFile.getCurrentPageNo() == currentFile.getPageCount() )
				iCurrentSaveMode = LASTPAGE;
			else if( !currentFile.getSource().canSplit() && currentFile.getCurrentPageNo() != currentFile.getPageCount() )
				iCurrentSaveMode = NOSPLITMORE;
			else if( currentFile.getSource().canSplit() && currentFile.getCurrentPageNo() != currentFile.getPageCount() )
				iCurrentSaveMode = CHOOSE;		}
		setupSaveButtons();
	}
	
	/**
	 * This method sets button text and visibility
	 */
	private void setupSaveButtons() {
		switch( iCurrentSaveMode ) {
		case ONEPAGE:
		case LASTPAGE:
			dlg.bPageOptions.setVisible(false);	
			dlg.bSave.setVisible(true);
			dlg.bSave.setText("Save");
			dlg.setFormEditable(true);
			break;
		case NOSPLITMORE:
			dlg.bPageOptions.setVisible(true);	
			dlg.bPageOptions.setText("View Next Page");
			dlg.bSave.setVisible(true);
			dlg.bSave.setText("Save All Pages");
			dlg.setFormEditable(true);
			break;
		case SPLITMORE:
			dlg.bPageOptions.setVisible(true);	
			dlg.bPageOptions.setText("View Next Page");
			dlg.bSave.setVisible(false);
			dlg.setFormEditable(true);
			break;
		case CHOOSE:
			dlg.bPageOptions.setVisible(true);	
			dlg.bPageOptions.setText("Add Page to Previous");
			dlg.bSave.setVisible(true);
			dlg.bSave.setText("Save New CVI");
			dlg.setFormEditable(false);
			break;
		default:
			logger.error("Invalid Save Mode in setupButtons " + iCurrentSaveMode);
		}
	}
	
	/** 
	 * Perform current function of the Page Options button
	 */
	private void doPageOptions() {
		if( currentFile.getSource().canSplit() ) {
			try {
				currentFile.addPageToCurrent();	 
			} catch (SourceFileException e) {
				// TODO Auto-generated catch block
				logger.error(e);
			}
		}
		switch( iCurrentSaveMode ) {
		case ONEPAGE:
		case LASTPAGE:
			// Invisible
			break;
		case NOSPLITMORE:
			doViewNext();
			break;
		case CHOOSE:
			doAddToPrevious();
			break;
		case SPLITMORE:
			doViewNext();
			break;
		default:
			logger.error("Invalid Save Mode in doPageOptions " + iCurrentSaveMode);
		}
	}
	
	/**
	 *  Perform current function of Save button.
	 */
	private void doSaveOptions() {
		switch( iCurrentSaveMode ) {
		case ONEPAGE:
		case LASTPAGE:
		case NOSPLITMORE:
			doSave();
			break;
		case CHOOSE:
			doSaveNew();
			break;
		case SPLITMORE:
			// Invisible
			break;
		default:
			logger.error("Invalid Save Mode in doSaveOptions " + iCurrentSaveMode);
		}
	}

	/**
	 * Default save action for currently loaded model and form.
	 */
	private void doSave() {
		try {
			// If save() finds errors be sure form is editable so they can be corrected.
			dlg.setFormEditable( false );
			currentFile.setCurrentPagesDone();
			setFileCompleteStatus();
			int iSaveModeIn = iCurrentSaveMode;
			iCurrentSaveMode = SAVED;
			if( !save() ) {
				iCurrentSaveMode = iSaveModeIn;
				dlg.setFormEditable( true );
			}
		} catch (SourceFileException e) {
			String sCVINo = dlg.jtfCVINo.getText();
			MessageDialog.showMessage(getDialog(), "Civet Error", "Failed to save Certificate number " + sCVINo);
			logger.error(e);
		}
		// Processing returns in saveComplete() after thread completes.;
	}
	
	private void setFileCompleteStatus() {
		if( !currentFile.getSource().canSplit() // File goes as a whole
				|| (!currentFile.morePagesForward(true) && !currentFile.morePagesBack(true)) ) { // or no pages left
			openFileList.markFileComplete(currentFile);
		}
	}
	
	/**
	 * Save the previously entered data and clear form for current page
	 */
	private void doSaveNew() {
		try {
			dlg.setFormEditable( false );
			currentFile.pageBackward(true);  // Kluge move back so we can move forward after save
			int iSaveModeIn = iCurrentSaveMode;
			currentFile.setCurrentPagesDone();
			iCurrentSaveMode = SAVED;
			if( !save() ) {
				iCurrentSaveMode = iSaveModeIn;
				dlg.setFormEditable( true );
			}
		} catch (SourceFileException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
		// Processing returns in saveComplete() after thread completes.;
	}
	
	/**
	 * View the next page while retaining data and model 
	 * before splitting or saving.
	 */
	private void doViewNext() {
		try {
			currentFile.pageForward(true);
			iCurrentSaveMode = CHOSEVIEW;
			setupFilePage();  // Update the display
		} catch (SourceFileException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
	}
	
	/**
	 * Add the currently displayed page to model retained 
	 * from previous page and open controls for further editing
	 */
	private void doAddToPrevious() {
		try {
			// Adds the current PDF page to the data model attachment
			// already partially edited.
			// This drills all the way down to the PDFSourceFile subclass
			// which is the only one that can be split and assembled.
			currentFile.addPageToCurrent();
		} catch (SourceFileException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
		iCurrentSaveMode = CHOSEADD;
		setupSaveLogic();
	}
	
	public String getViewerTitle() { return viewerTitle; }

	/**
	 * Use OpenFiles controller to set the file/page counter
	 */
	public void updateCounterPanel() {
		boolean morePagesBack = currentFile.morePagesBack(false);
		boolean morePagesForward = currentFile.morePagesForward(false);
		dlg.pCounters.setPageBackEnabled(morePagesBack);
		dlg.pCounters.setPageForwardEnabled(morePagesForward);
		dlg.pCounters.setFileBackEnabled(openFileList.moreFilesBack(false));
		dlg.pCounters.setFileForwardEnabled(openFileList.moreFilesForward(false));
		if( morePagesBack || morePagesForward )
			dlg.bGotoPage.setEnabled(true);
		else
			dlg.bGotoPage.setEnabled(false);		
	}

	private void refreshOtherCounties() {
		dlg.cbOtherCounty.removeAllItems();
		dlg.cbOtherCounty.addItem(null);
		String sOtherState = dlg.cbOtherState.getSelectedCode();
		if( sOtherState != null ) {
			for( String sCounty : Counties.getCounties(sOtherState) ) {
				dlg.cbOtherCounty.addItem(sCounty);
			}
		}
	}

	private void refreshThisCounties() {
		dlg.cbThisCounty.removeAllItems();
		dlg.cbThisCounty.addItem(null);
		String sThisState = CivetConfig.getHomeStateAbbr();
		if( sThisState != null ) {
			for( String sCounty : Counties.getCounties(sThisState) ) {
				dlg.cbThisCounty.addItem(sCounty);
			}
		}
	}



	void doEditLast() {
		doEditLast( false );
	}
		
	/**
	 * Open a new dialog with the last saved file.
	 * Simple utility for quick corrections.
	 * @param bLastPage
	 * @throws SourceFileException 
	 */
	void doEditLast(boolean bLastPage) {
		try {
			ArrayList<File> aFiles = new ArrayList<File>();
			aFiles.add(fLastSaved);
			CivetEditDialog dlgLast = new CivetEditDialog( dlg, aFiles);
			dlgLast.make90PercentShift();  // make it obvious that this is over the other.
			dlgLast.setVisible(true);
		} catch (SourceFileException e) {
			logger.error(e);
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
	    	  MessageDialog.showMessage(dlg, "Civet Error:", sZip + " is not a valid zipcode");
	    	  jtfZip.requestFocus();
	      }
	}

	private void setActiveSpecies( String sSpecies ) {
		bInSppChangeByCode = true;
		updateSpeciesList(false);
		dlg.cbSpecies.setSelectedItem(sSpecies);
		String sSpeciesCode = dlg.cbSpecies.getSelectedCode();
		for( SpeciesRecord r : aSpecies ) {
			if( r.sSpeciesCode.equals(sSpeciesCode) ) {
				dlg.jtfNumber.setText(Integer.toString(r.iNumber) );
			}
		}
		bInSppChangeByCode = false;
	}
	
	public String getOriginalFileName() {
		return currentFile.getSource().getFileName();
	}

	public int getPageNo() { return currentFile.getCurrentPageNo(); }
	
	private void checkPreDate() {
		java.util.Date dateInspected = dlg.jtfDateInspected.getDate();
		java.util.Date dateReceived = dlg.jtfDateReceived.getDate();
		if( (dateInspected != null && dateReceived != null && dateInspected.getTime() > dateReceived.getTime() )
				|| ( dlg.jtfDateInspected.isAcceptedDate() && dlg.jtfDateInspected.isFuture() ) ) {
			String sShortName = ErrorTypeLookup.getShortNameForDescription("Pre-dated signature");
			// Don't add if error table doesn't include Pre-dated.
			if( sShortName != null ) {
				if( aErrorKeys == null )
					aErrorKeys = new ArrayList<String>();
				if( !aErrorKeys.contains(sShortName) ) 
					aErrorKeys.add(sShortName);
				dlg.lError.setVisible(true);
			}
		}
	}

	private void cancel() {
		if( formEditListener.isChanged() || YesNoDialog.ask(dlg, "Civet: Close", "Close without saving?") ) {
			doCleanup();
			dlg.setVisible(false);
		}
	}
	
	private void doCleanup() {
    	if( parent instanceof CivetInbox) {
    		((CivetInbox)parent).inboxController.refreshTables();
    	}
	}

	public boolean isReopened() {
		String sCurrentPath = currentFile.getSource().getFilePath();
		String sCurrentDir = sCurrentPath.substring(0,sCurrentPath.lastIndexOf('\\')+1);
		// This is a huge kluge to detect that we are reopening a toBeFiled stdXML
	    return( sCurrentDir.equalsIgnoreCase(CivetConfig.getToFileDirPath()) );
	}

	void doPickPage( MouseEvent e ) {
		if( e.getClickCount() == 2 ) {
			doGotoPage();
		}
	}
	
	/**
	 * Move to specified page in currentFile
	 * @param iPage Both page and file numbers are 1 based indexes they are converted to zero based
	 * as needed inside each method.
	 */
	public void gotoPage( int iPage ) {
		try {
			currentFile.gotoPageNo(iPage);
			setupFilePage();
		} catch (SourceFileException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
	}
	
	public void setPage( int iPage ) {
		dlg.pCounters.setPage(iPage);
	}

	void setPages( int iPages ) {
		dlg.pCounters.setPages(iPages);
	}
	
	void setFile( int iFileNo ) {
		dlg.pCounters.setFile(iFileNo); // currentFiles is 0 indexed array
	}

	void setFiles( int iFiles ) {
		dlg.pCounters.setFiles(iFiles);
	}
	
	private void doShowAllVets( boolean bChecked ) {
		int iSel = dlg.cbIssuedBy.getSelectedKeyInt();
		// Note isSelected() gives the value BEFORE the action takes place so we reverse the logic
		// But logic is already backward so only negate the selection!  
		// ShowAll == NOT Checking Expiration or NAN Level
		vetLookup.setExpCheck(bChecked);
		vetLookup.setLevel2Check(bChecked);
		dlg.ckAllVets.setSelected(!bChecked);
		dlg.cbIssuedBy.refresh();
		if( iSel >= 0 )
			dlg.cbIssuedBy.setSelectedKey(iSel);
	}

	private void doGotoPage() {
		String sPage = OneLineQuestionDialog.ask(dlg, "Civet: Goto Page", "Page number?");
		int iPage = -1;
		try {
			iPage = Integer.parseInt(sPage);
		} catch (NumberFormatException ex) {
			MessageDialog.showMessage(dlg, "Civet: Error", "Cannot parse " + sPage + " as a number");
			return;
		}
		gotoPage(iPage);
	}

	void jtfThisPIN_focusLost(FocusEvent e) {
		String sThisPremId = dlg.jtfThisPIN.getText();		
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
			MessageDialog.showMessage(dlg, 
						"Civet: Premises ID Error", sThisPremId + " is not a valid Premises ID", MessageDialog.OK_ONLY);
			bInSearch = false;
			return;
		}
		else {
			sThisPremId = sUpperPin;
			dlg.jtfThisPIN.setText(sUpperPin);
		}
		if( bInSearch ) 
			return;
		bInSearch = true;
		// don't overwrite existing content UNLESS sticky is on so the content may be 
		// from last entry.
		if( dlg.ckSticky.isSelected() || (
			(dlg.jtfThisName.getText() == null || dlg.jtfThisName.getText().trim().length() == 0) &&
			(dlg.jtfAddress.getText() == null || dlg.jtfAddress.getText().trim().length() == 0) &&
			(dlg.jtfThisCity.getText() == null || dlg.jtfThisCity.getText().trim().length() == 0) &&
			(dlg.jtfZip.getText() == null || dlg.jtfZip.getText().trim().length() == 0) ) ) {
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
			PremisesSearchDialog dlgSearch = (PremisesSearchDialog)dlg.jtfThisPIN.getSearchDialog(); 
			if( dlgSearch.exitOK() ) {
				dlg.jtfThisName.setText(dlgSearch.getSelectedPremName());
				dlg.jtfAddress.setText(dlgSearch.getSelectedAddress());
				dlg.jtfThisCity.setText(dlgSearch.getSelectedCity());
				dlg.cbThisCounty.setSelectedItem(Counties.getHerdsCounty(CivetConfig.getHomeStateAbbr(), dlgSearch.getSelectedCounty()));
				dlg.jtfZip.setText(dlgSearch.getSelectedZipCode());
				String sNewPhone = dlgSearch.getSelectedPhone();
				if( dlg.jtfPhone.getText() == null || dlg.jtfPhone.getText().trim().length() == 0 ) {
					if( sNewPhone != null ) {
						dlg.jtfPhone.setText(sNewPhone);
					}
				}
				Component c = traversal.getComponentByName(traversal.getProperty("pPremiseFound"));
				if( c != null && c != dlg.jtfThisPIN )
					c.requestFocus();
			}
			else {
				// Note, this route is broken until search logic gets refined.
				PremisesTableModel model;
				try {
					if( CivetConfig.isStandAlone() )
						model = new LocalPremisesTableModel( sStatePremisesId, sFedPremisesId );
					else
						model = new UsaHerdsLookupPrems( sStatePremisesId, sFedPremisesId );
					if( model.getRowCount() == 1 ) {
						if( model.next() ) {
							dlg.jtfThisPIN.setText(sThisPremId);
							dlg.jtfThisName.setText(model.getPremName());
							dlg.jtfAddress.setText(model.getAddress());
							dlg.jtfThisCity.setText(model.getCity());
							dlg.cbThisCounty.setSelectedItem(Counties.getHerdsCounty(CivetConfig.getHomeStateAbbr(), model.getCounty()));
							dlg.jtfZip.setText(model.getZipCode());
							String sNewPhone = model.getPhone();
							if( dlg.jtfPhone.getText() == null || dlg.jtfPhone.getText().trim().length() == 0 ) {
								if( sNewPhone != null ) {
									dlg.jtfPhone.setText(sNewPhone);
								}
							}
							Component c = traversal.getComponentByName(traversal.getProperty("pPremiseFound"));
							if( c != null && c != dlg.jtfThisPIN )
								c.requestFocus();
							
						}
					}
					else {
						MessageDialog.showMessage(dlg, "Civet: Error Premises Not Found", 
								"Found " + model.getRowCount() + " premises for " + dlg.jtfThisPIN.getText());
					}
				} catch (WebServiceException e1) {
					MessageDialog.showMessage(dlg, "Civet: Error", "Web Service Failure\n" + e1.getMessage());
					logger.error("Web Service Failure", e1);
				}
			}
		}
		bInSearch = false;
	}

	void jtfPhone_focusLost(FocusEvent e) {
	String sPhone = dlg.jtfPhone.getText();
		if( sPhone == null || sPhone.trim().length() == 0 || sPhone.equals(sPriorPhone) ) return;
		PremisesSearchDialog dlgSearch = new PremisesSearchDialog();
		try {
			dlgSearch.searchPhone(sPhone);
			if( dlgSearch.exitOK() ) {
				dlg.jtfThisName.setText(dlgSearch.getSelectedPremName());
				dlg.jtfAddress.setText(dlgSearch.getSelectedAddress());
				dlg.jtfThisCity.setText(dlgSearch.getSelectedCity());
				dlg.cbThisCounty.setSelectedItem(Counties.getHerdsCounty(CivetConfig.getHomeStateAbbr(), dlgSearch.getSelectedCounty()));
				dlg.jtfZip.setText(dlgSearch.getSelectedZipCode());
				String sPin = dlgSearch.getSelectedPremId();
				if( sPin != null && sPin.trim().length() != 7 ) {
					String sThisState = CivetConfig.getHomeStateAbbr();
					bLidFromHerds = isValidLid( sPin, sThisState);
				}
				dlg.jtfThisPIN.setText(sPin);
				Component c = traversal.getComponentByName(traversal.getProperty("pPremiseFound"));
				if( c != null)
					c.requestFocus();
			}
		} catch (WebServiceException e1) {
			MessageDialog.showMessage(dlg, "Civet: Error", "Web Service Failure\n" + e1.getMessage());
			logger.error("Web Service Failure", e1);
		}
	}

	void jtfAddrCity_focusLost(FocusEvent e) {
		String sCity = dlg.jtfThisCity.getText();
		String sAddress = dlg.jtfAddress.getText();
		// don't bother if either address or city is blank or neither has changed.
		if( sAddress == null || sAddress.trim().length() == 0 || sCity == null || sCity.trim().length() == 0 
				|| (sCity.equals(sPriorCity) && sAddress.equals(sPriorAddress)) ) return;
		if(bInSearch) return;
		bInSearch = true;
		if( dlg.ckSticky.isSelected() || (
				(dlg.jtfThisPIN.getText() == null || dlg.jtfThisPIN.getText().trim().length() == 0) &&
				(dlg.jtfPhone.getText() == null || dlg.jtfPhone.getText().trim().length() == 0) &&
				(dlg.jtfZip.getText() == null || dlg.jtfZip.getText().trim().length() == 0)  ) ) {
			PremisesSearchDialog dlgSearch = new PremisesSearchDialog();
			try {
				dlgSearch.searchAddress( sAddress, sCity, null, null );
				if( dlgSearch.exitOK() ) {
					if( dlg.jtfPhone.getText() == null || dlg.jtfPhone.getText().trim().length() == 0 )
						dlg.jtfPhone.setText(dlgSearch.getSelectedPhone());
					dlg.jtfThisName.setText(dlgSearch.getSelectedPremName());
					dlg.jtfAddress.setText(dlgSearch.getSelectedAddress());
					dlg.jtfThisCity.setText(dlgSearch.getSelectedCity());
					dlg.cbThisCounty.setSelectedItem(Counties.getHerdsCounty(CivetConfig.getHomeStateAbbr(), 
							dlgSearch.getSelectedCounty()));
					dlg.jtfZip.setText(dlgSearch.getSelectedZipCode());
					String sPin = dlgSearch.getSelectedPremId();
					if( sPin != null && sPin.trim().length() != 7 ) {
						String sThisState = CivetConfig.getHomeStateAbbr();
						bLidFromHerds = isValidLid( sPin, sThisState);
					}
					dlg.jtfThisPIN.setText(sPin);
					Component c = traversal.getComponentByName(traversal.getProperty("pPremiseFound"));
					if( c != null)
						c.requestFocus();
				}
			} catch (WebServiceException e1) {
				MessageDialog.showMessage(dlg, "Civet: Error", "Web Service Failure\n" + e1.getMessage());
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
	public void clearForm() {
		bSppEntered = false;
		bInSppChangeByCode = true;
		formEditListener.clear();
		// Always start with a blank search box.
		dlg.jtfThisPIN.getSearchDialog().clear(); 
		// clear the form and select main map if the check box is not checked or we just did an XFA
		if( !dlg.ckSticky.isSelected() ) {  // need to restore logic for file after XFA.
			traversal.selectMainMap();
			dlg.cbOtherState.setSelectedKey(-1);
//			dlg.jtfOtherPIN.setText("");
			dlg.jtfOtherName.setText("");
			dlg.jtfOtherAddress.setText("");
			dlg.jtfOtherCity.setText("");
			dlg.cbOtherCounty.setSelectedItem(null);
			dlg.jtfOtherZip.setText("");
			dlg.jtfThisPIN.setText("");
			dlg.jtfThisName.setText("");
			dlg.jtfPhone.setText("");
			dlg.jtfAddress.setText("");
			dlg.jtfThisCity.setText("");
			dlg.cbThisCounty.setSelectedItem(null);
			dlg.jtfZip.setText("");
			dlg.jtfDateInspected.setText("");
			dlg.jtfDateReceived.setText("");
			dlg.jtfCVINo.setText("");
			String sSpDefault = CivetConfig.getDefaultSpecies();
			if( sSpDefault != null )
				dlg.cbSpecies.setSelectedValue(sSpDefault);
			else
				dlg.cbSpecies.setSelectedKey(-1);
			mSpeciesChanges = new HashMap<String, String>();
			dlg.jtfNumber.setText("");
			dlg.cbIssuedBy.setSelectedKey(-1);
			dlg.jtfIssuedBy.setText("");
			if( sDefaultPurpose != null )
				dlg.cbPurpose.setSelectedItem( sDefaultPurpose );
		}
		else {
			// Note: we don't switch to the alternate map right away when the check box is selected
			// because the form is still blank at that point.  Only when editing a box with 
			// sticky data included.
			traversal.selectAltMap();
		}
		if(dlg.rbInState.isSelected()) {
			dlg.setImport( false );
			dlg.cbOtherState.setSelectedValue(CivetConfig.getHomeState());
			refreshOtherCounties();
			dlg.cbOtherState.setEnabled(false);
			dlg.lIssuedBy.setVisible(true);
			dlg.lIssuedBy.setFont(new Font("Tahoma", Font.BOLD, 11));
			dlg.cbIssuedBy.setVisible(true);
			dlg.jtfIssuedBy.setVisible(false);
			dlg.ckAllVets.setVisible(true);
			Component c = traversal.getComponentByName(traversal.getProperty("pInstateFirst"));
			if( c != null)
				c.requestFocus();
		}
		if(dlg.rbExport.isSelected()) {
			dlg.lIssuedBy.setVisible(true);
			dlg.lIssuedBy.setFont(new Font("Tahoma", Font.BOLD, 11));
			dlg.cbIssuedBy.setVisible(true);
			dlg.jtfIssuedBy.setVisible(false);
			dlg.ckAllVets.setVisible(true);
			Component c = traversal.getComponentByName(traversal.getProperty("pFirstControl"));
			if( c != null)
				c.requestFocus();
		}
		if(dlg.rbImport.isSelected()) {
			dlg.lIssuedBy.setVisible(true);
			dlg.lIssuedBy.setFont(new Font("Tahoma", Font.PLAIN, 11));
			dlg.cbIssuedBy.setVisible(false);
			dlg.jtfIssuedBy.setVisible(true);
			dlg.ckAllVets.setVisible(false);
			Component c = traversal.getComponentByName(traversal.getProperty("pFirstControl"));
			if( c != null)
				c.requestFocus();
		}
		aErrorKeys = new ArrayList<String>();
		sErrorNotes = null;
		aSpecies = new ArrayList<SpeciesRecord>();
		dlg.lError.setVisible(false);
		dlg.bMultiSpecies = false;
		bInSppChangeByCode = false;
	}

	/**
	 * Populate dialog form from standard eCVI XML
	 * @param xStd StdeCviXml object representation of a CVI
	 */
	public void populateFromStdXml( StdeCviXmlModel xStd ) {
		if( xStd == null ) {
			logger.error(new Exception("populateFromStdXml called with null StdeCviXml"));
			return;
		}
		else {
			try {
				bSppEntered = true;
				bInSppChangeByCode = true;
				String sOriginState = xStd.getOrigin().address.state;
				if( sOriginState != null ) {
					sOriginState = States.getState(sOriginState);
					if( sOriginState != null && sOriginState.equalsIgnoreCase(CivetConfig.getHomeState()) ) {
						dlg.setImport(false);
						dlg.rbExport.setSelected(true);
						String sOtherState = States.getState(xStd.getDestination().address.state);
						dlg.cbOtherState.setSelectedValue(sOtherState);
						refreshOtherCounties();
						//					jtfOtherPIN.setText("");
						// For display purposes only!  Display person name if no prem name.
						String sOtherName = xStd.getDestination().premName;
						if( sOtherName == null || sOtherName.trim().length() == 0 )
							sOtherName = xStd.getDestination().personName;
						//					jtfOtherPIN.setText(xStd.getDestinationPremId());
						dlg.jtfOtherName.setText(sOtherName);
						dlg.jtfOtherAddress.setText(xStd.getDestination().address.line1);
						dlg.jtfOtherCity.setText(xStd.getDestination().address.town);
						String sOtherStateCode = dlg.cbOtherState.getSelectedCode();
						String sOtherCountyIn = xStd.getDestination().address.county;
						String sOtherZip = xStd.getDestination().address.zip;
						String sOtherHerdsCounty = getHerdsCounty( sOtherStateCode, sOtherCountyIn, sOtherZip );
						dlg.cbOtherCounty.setSelectedItem(sOtherHerdsCounty);
						dlg.jtfOtherZip.setText(sOtherZip);
						dlg.jtfThisPIN.setText("");
						String sThisName = xStd.getOrigin().premName;
						if( sThisName == null || sThisName.trim().length() == 0 )
							sThisName = xStd.getOrigin().personName;
						dlg.jtfThisPIN.setText(xStd.getOrigin().premid);
						dlg.jtfThisName.setText(sThisName);
						dlg.jtfPhone.setText(xStd.getOrigin().personPhone);
						dlg.jtfAddress.setText(xStd.getOrigin().address.line1);
						dlg.jtfThisCity.setText(xStd.getOrigin().address.town);
						String sThisStateCode = CivetConfig.getHomeStateAbbr();
						String sThisCountyIn = xStd.getOrigin().address.county;
						String sThisZip = xStd.getOrigin().address.zip;
						String sThisHerdsCounty = getHerdsCounty( sThisStateCode, sThisCountyIn, sThisZip );
						dlg.cbThisCounty.setSelectedItem(sThisHerdsCounty);
						dlg.jtfZip.setText(sThisZip);
						String sNAN = xStd.getVet().nationalAccreditationNumber;
						if( sNAN != null && sNAN.trim().length() > 0 ) {
							VetLookup vet = new VetLookup( sNAN );
							dlg.cbIssuedBy.setSelectedKey(vet.getKey());
						}
						else {
							// May not be accredited.
							doShowAllVets( true );
							String sVetName = xStd.getVet().person.name;
							StringTokenizer tok = new StringTokenizer(sVetName, ", ");
							String sVetLastName = null;
							String sVetFirstName = null;
							if(tok.hasMoreTokens())
								sVetLastName = tok.nextToken();
							if(tok.hasMoreTokens())
								sVetFirstName = tok.nextToken();
							VetLookup vet = new VetLookup( sVetLastName, sVetFirstName );
							if( vet.isUniqueMatch() )
								dlg.cbIssuedBy.setSelectedKey(vet.getKey());
						}
					}
					else {
						dlg.setImport(true);
						dlg.rbImport.setSelected(true);
						String sOtherState = States.getState(xStd.getOrigin().address.state);
						dlg.cbOtherState.setSelectedValue(sOtherState);
						//					jtfOtherPIN.setText("");
						// For display purposes only!  Display person name if no prem name.
						String sOtherName = xStd.getOrigin().premName;
						if( sOtherName == null || sOtherName.trim().length() == 0 )
							sOtherName = xStd.getOrigin().premName;
						//					cbOtherCounty.setText(xStd.getOriginPremId());
						dlg.jtfOtherName.setText(sOtherName);
						dlg.jtfOtherAddress.setText(xStd.getOrigin().address.line1);
						dlg.jtfOtherCity.setText(xStd.getOrigin().address.town);
						refreshOtherCounties();
						String sOtherStateCode = dlg.cbOtherState.getSelectedCode();
						String sOtherCountyIn = xStd.getOrigin().address.county;
						String sOtherZip = xStd.getOrigin().address.zip;
						String sOtherHerdsCounty = getHerdsCounty( sOtherStateCode, sOtherCountyIn, sOtherZip );
						dlg.jtfOtherZip.setText(sOtherZip);
						dlg.cbOtherCounty.setSelectedItem(sOtherHerdsCounty);
						dlg.jtfThisPIN.setText("");
						String sThisName = xStd.getDestination().premName;
						if( sThisName == null || sThisName.trim().length() == 0 )
							sThisName = xStd.getDestination().personName;
						dlg.jtfThisPIN.setText(xStd.getDestination().premid);
						dlg.jtfThisName.setText(sThisName);
						dlg.jtfPhone.setText(xStd.getDestination().personPhone);
						dlg.jtfAddress.setText(xStd.getDestination().address.line1);
						dlg.jtfThisCity.setText(xStd.getDestination().address.town);
						String sThisState = CivetConfig.getHomeStateAbbr();
						String sThisCountyIn = xStd.getDestination().address.county;
						String sThisZip = xStd.getDestination().address.zip;
						String sThisHerdsCounty = getHerdsCounty( sThisState, sThisCountyIn, sThisZip);
						dlg.cbThisCounty.setSelectedItem(sThisHerdsCounty);
						dlg.jtfZip.setText(sThisZip);
						Veterinarian vet = xStd.getVet();
						String sVetName = "";
						if( vet != null )
							sVetName = vet.person.name;
						dlg.jtfIssuedBy.setText(sVetName);

					}
					java.util.Date dIssueDate = xStd.getIssueDate();
					dlg.jtfDateInspected.setDate(dIssueDate);
					dlg.jtfCVINo.setText(xStd.getCertificateNumber());
					// Species multiples are an issue
					loadSpeciesFromStdXml(xStd);
					// Overly simplistic.  Only works if spelling matches
					String sPurposeCode = xStd.getPurpose();
					dlg.cbPurpose.setSelectedKey(sPurposeCode);
					// Load data from included XmlMetaData "file"
					CviMetaDataXml meta = xStd.getMetaData();
					//			System.out.println( meta.getXmlString() );
					if( meta != null ) {
						// Make no assumption about received date.
						java.util.Date dReceived = meta.getBureauReceiptDate();
						dlg.jtfDateReceived.setDate(dReceived);
						ArrayList<String> aErrors = meta.listErrors();
						if( aErrors == null ) {
							aErrorKeys = new ArrayList<String>();
							dlg.lError.setVisible(false);
						}
						else {
							aErrorKeys = aErrors;
							if( aErrorKeys.size() > 0 )
								dlg.lError.setVisible(true);
							else
								dlg.lError.setVisible(false);
						}
						sErrorNotes = meta.getErrorNote();

					}
					else {
						if( dlg.jtfDateReceived.getDate() == null ) {
							if( CivetConfig.isDefaultReceivedDate() ) 
								dlg.jtfDateReceived.setDate(new java.util.Date());
							else
								dlg.jtfDateReceived.setText("");
						}
						aErrorKeys = new ArrayList<String>();
						dlg.lError.setVisible(false);					
					}
//TODO Fix this.
//					Component c = traversal.getComponentByName(traversal.getProperty("pPDFLoaded"));
//					if( c != null)
//						c.requestFocus();
				}
				bInSppChangeByCode = false;
			} catch( Exception e ) {
				logger.error("Unexpected error loading from XML standard document", e);
			}
		}
	}
	
	private String getHerdsCounty( String sStateCode, String sCounty, String sZip ) {
		String sRet = Counties.getHerdsCounty(sStateCode, sCounty);
		if( sRet == null && sZip != null && sZip.trim().length() >= 5 && !sZip.equals("00000") ) {
				try {
					String sZipCounty = CountyUtils.getCounty(sZip);
					sRet = Counties.getHerdsCounty(sStateCode, sZipCounty);	
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error(e);
				}
		}
		return sRet;
	}

	/**
	 * Subroutine for above, just to keep the populateFromStdXml method somewhat sane
	 * @param std StdeCviXml object being loaded.
	 */
	private void loadSpeciesFromStdXml(StdeCviXmlModel std) {
		aSpecies = new ArrayList<SpeciesRecord>();
		ArrayList<String> aBadSpecies = new ArrayList<String>();
		ArrayList<Animal> aAnimals = std.getAnimals();
		String sSpeciesCode = null;
		for( Animal animal : aAnimals ) {
			sSpeciesCode = animal.speciesCode.code;
			if( sSpeciesCode == null || sSpeciesCode.trim().length() == 0 ) {
				MessageDialog.showMessage(dlg, "Civet Warning: Invalid Species", "Species code is blank.\nSaving as Other.");
				sSpeciesCode = "OTH";
			}
			if( !SpeciesLookup.isCodeStandard(sSpeciesCode) && !sSpeciesCode.equalsIgnoreCase("OTH") ) {
				MessageDialog.showMessage(dlg, "Civet Warning: Nonstandard Species", "Species code " + sSpeciesCode + " is not in standard");
				aBadSpecies.add(sSpeciesCode);
			}
			if( !SpeciesLookup.isCodeInHerds(sSpeciesCode) && !sSpeciesCode.equalsIgnoreCase("OTH") ) {
				MessageDialog.showMessage(dlg, "Civet Warning: Invalid Species", "Species code " + sSpeciesCode 
						+ " is not in USAHERDS as CVI species.\nSaving as Other.");
				aBadSpecies.add(sSpeciesCode);
			}
			boolean bSet = false;
			if( aSpecies.size() > 0 ) {
				for( SpeciesRecord r : aSpecies ) {
					if( sSpeciesCode != null && sSpeciesCode.equals(r.sSpeciesCode) ) {
						r.iNumber++;
						bSet = true;
						break;
					}
				}
			}
			if( !bSet ) {
				SpeciesRecord sp = new SpeciesRecord( sSpeciesCode, 1 );
				aSpecies.add(sp);
			}
		}
		ArrayList<GroupLot> aGroups = std.getGroups();
		for( GroupLot group : aGroups ) {
			sSpeciesCode = group.speciesCode.code;
			if( sSpeciesCode == null || sSpeciesCode.trim().length() == 0 ) {
				MessageDialog.showMessage(dlg, "Civet Warning: Invalid Species", "Species code is blank.\nSaving as Other.");
				sSpeciesCode = "OTH";
			}
			if( !SpeciesLookup.isCodeStandard(sSpeciesCode) && !sSpeciesCode.equalsIgnoreCase("OTH") ) {
				MessageDialog.showMessage(dlg, "Civet Warning: Nonstandard Species", "Species code " + sSpeciesCode + " is not in standard");
				aBadSpecies.add(sSpeciesCode);
			}
			if( !SpeciesLookup.isCodeInHerds(sSpeciesCode) && !sSpeciesCode.equalsIgnoreCase("OTH") ) {
				MessageDialog.showMessage(dlg, "Civet Warning: Invalid Species", "Species code " + sSpeciesCode 
						+ " is not in USAHERDS as CVI species.\nSaving as Other.");
				aBadSpecies.add(sSpeciesCode);
			}
			int iQuantity = group.quantity.intValue();
			boolean bSet = false;
			if( aSpecies.size() > 0 ) {
				for( SpeciesRecord r : aSpecies ) {
					if( sSpeciesCode != null && sSpeciesCode.equals(r.sSpeciesCode) ) {
						r.iNumber += iQuantity;
						bSet = true;
						break;
					}
				}
				if( bSet )
					break;
			}
			if( !bSet ) {
				SpeciesRecord sp = new SpeciesRecord( sSpeciesCode, iQuantity );
				aSpecies.add(sp);
			}
		}
		int iMax = 0;
		for( SpeciesRecord r : aSpecies ) {
			if( r.iNumber > iMax ) {
				iMax = r.iNumber;
				sSpeciesCode = r.sSpeciesCode;
			}
		}
		if( sSpeciesCode != null ) {
			dlg.cbSpecies.setSelectedCode(sSpeciesCode);
			dlg.jtfNumber.setText(Integer.toString(iMax));
			dlg.bMultiSpecies = (aSpecies.size() > 1);
			dlg.lMultipleSpecies.setVisible( dlg.bMultiSpecies );
		}
		else {
			dlg.cbSpecies.setSelectedCode(null);
			dlg.jtfNumber.setText("");
			dlg.bMultiSpecies = false;
			dlg.lMultipleSpecies.setVisible( dlg.bMultiSpecies );
		}

	}
	
	// Probably fast enough to leave in event dispatch thread
	// but not good practice.
	// May be too slow if moving to different drive instead of just renaming to new folder.
	public void allFilesDone() {
		dlg.viewer.closePdfFile();
	    if( !isReopened() ) {
//	    	TODO Deal with complete files
//	    	File completeFiles[] = cviFileController.getCompleteFiles();
//	    	moveCompleteFiles( completeFiles );
	    }
	    if( parent instanceof CivetInbox) {
			((CivetInbox)parent).inboxController.refreshTables();
		}
	    dlg.setVisible(false);
	}
	
	private void doAddIDs() {
		if( ( dlg.cbSpecies.getSelectedCode() == null || dlg.cbSpecies.getSelectedCode().trim().length() == 0 ) && aSpecies.size() == 0 ) {
			MessageDialog.showMessage(dlg, "Civet Error", "Species must be added before IDs" );
		}
		else {
			// TODO Rework this using the Animal list from XML model
			ArrayList<String> aSpeciesStrings = new ArrayList<String>();
			if( aSpecies.size() > 0 ) {
				for( SpeciesRecord r : aSpecies ) {
					aSpeciesStrings.add(dlg.cbSpecies.getValueForCode(r.sSpeciesCode));
				}
			}
			if( dlg.cbSpecies.getSelectedCode() != null ) {
				String sSpecies = dlg.cbSpecies.getSelectedValue();
				if( !aSpeciesStrings.contains(sSpecies) ) {
					aSpeciesStrings.add(sSpecies);
				}
			}
			HashMap<String, String> hSpecies = new HashMap<String, String>();
			for( String sSp : aSpeciesStrings ) {
				String sCode = SpeciesLookup.getCodeForName(sSp);
				hSpecies.put(sCode,sSp);
			}
			AddAnimalsDialog dlg = new AddAnimalsDialog( hSpecies, idListModel );
			dlg.setModal(true);
			dlg.setVisible(true);
			setActiveSpecies( dlg.getSelectedSpecies() );
			FileUtils.writeTextFile(currentFile.getModel().getXMLString(), "AfterAdd.xml");
		}
	}

	/** 
	 * Gather values from dialog controls and dispatch to appropriate insert or update thread.
	 * @throws SourceFileException 
	 */
	boolean save() throws SourceFileException {
		String sCVINo = dlg.jtfCVINo.getText();
			if( sCVINo.equalsIgnoreCase(sPrevCVINo) ) {
				MessageDialog.showMessage(dlg, "Civet Error", "Certificate number " + sCVINo + " hasn't changed since last save");
				dlg.jtfCVINo.requestFocus();
				return false;
			}
			if( CertificateNbrLookup.certficateNbrExists(dlg.jtfCVINo.getText()) ) {
				MessageDialog.showMessage(dlg, "Civet Error", "Certificate number " + sCVINo + " already exists");
				dlg.jtfCVINo.requestFocus();
				return false;
			}
		// Collect up all values needed
		dlg.setImport(dlg.rbImport.isSelected());
		boolean bInbound = dlg.rbImport.isSelected();
		boolean bDataFile = currentFile.isDataFile();
		String sOtherState = dlg.cbOtherState.getSelectedValue();
		String sOtherStateCode = States.getStateCode(sOtherState);
		String sOtherName = dlg.jtfOtherName.getText();
		String sOtherAddress = dlg.jtfOtherAddress.getText();
		String sOtherCity = dlg.jtfOtherCity.getText();
		String sOtherCounty = (String)dlg.cbOtherCounty.getSelectedItem();
		String sOtherZipcode = dlg.jtfOtherZip.getText();
		String sOtherPIN = null; //dlg.jtfOtherPIN.getText();
		String sThisPremisesId = dlg.jtfThisPIN.getText();
		if( sThisPremisesId != null && sThisPremisesId.trim().length() != 7 && CivetConfig.hasBrokenLIDs() && !bLidFromHerds )
			sOtherPIN = null;
		String sPhone = dlg.jtfPhone.getText();
		String sThisName = dlg.jtfThisName.getText();
		String sStreetAddress = dlg.jtfAddress.getText();
		String sCity = dlg.jtfThisCity.getText();
		String sZipcode = dlg.jtfZip.getText();
		String sThisState = dlg.jtfThisState.getText();
		String sThisCounty = (String)dlg.cbThisCounty.getSelectedItem();
		if( sThisCounty == null || sThisCounty.trim().length() < 3 ) {
			try {
				String sThisHerdsCounty = CountyUtils.getCounty(sZipcode);
				if( sThisHerdsCounty != null ) {
					sThisCounty = Counties.getHerdsCounty(sThisState, sThisHerdsCounty);
				}
			} catch (IOException e) {
				logger.error(e);
			}
		}
		java.util.Date dDateIssued = dlg.jtfDateInspected.getDate();
		java.util.Date dDateReceived = dlg.jtfDateReceived.getDate();
		// need to incorporate.  Wait for XML upload?
		String sMovementPurpose = dlg.cbPurpose.getSelectedValue();
		Integer iIssuedByKey = dlg.cbIssuedBy.getSelectedKeyInt();
		String sIssuedByName = dlg.jtfIssuedBy.getText();
		if( !bInbound && (iIssuedByKey == null || iIssuedByKey == -1) ) {
			MessageDialog.showMessage(dlg, "Civet Error", "Issuing Veterinarian is required");
			return false;
		}
		updateSpeciesList(false);
		StdeCviXmlModel model = currentFile.getModel();
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
			MessageDialog.showMessage(dlg, "Civet Error", "One or more required fields:" + sFields + " are empty");
			return false;
		}

		// Precondition: PDF, Animals and Errors already in model.
		// NOTE!!!!  model is not thread safe at this point.  
		SaveCVIThread thread = new SaveCVIThread(this, model, bInbound, bDataFile, 
				aSpecies, sOtherStateCode,
				sOtherName, sOtherAddress, sOtherCity, sOtherCounty, sOtherZipcode, sOtherPIN,
				sThisPremisesId, sThisName, sPhone,
				sStreetAddress, sCity, sThisCounty, sZipcode,
				dDateIssued, dDateReceived, iIssuedByKey, sIssuedByName, sCVINo,
				sMovementPurpose);
		sPrevCVINo = sCVINo;
		thread.start();
		return true;
	}
	
	/**
	 * Called by save thread to allow edit last, add page to last, etc.
	 * @param fLast
	 */
	public void setLastSavedFile( File fLast ) {
		this.fLastSaved = fLast;
	}
	
	/**
	 * Navigate to the next page to edit
	 * Here we ask for only incomplete pages.  (Nav bar steps through all pages and files).
	 * TODO Decide if the next page MIGHT be part of the just saved CVI and offer correct choices.
	 */
	public void saveComplete() {
		try {
			// Only on multipage PDF do we page forward on save
			if( currentFile.getSource().canSplit() && currentFile.morePagesForward(true) ) {
				currentFile.pageForward(true);
				viewer.viewPage(currentFile.getCurrentPageNo());
			}
			// Backup to get skipped pages?  Not currently.
			else if ( openFileList.moreFilesForward(true) || openFileList.moreFilesBack(true) ) {
				currentFile = openFileList.nextFile(true);
				currentFile.viewFile();
			}
			else {
				moveCompletedFiles();
				CivetInboxController.getCivetInboxController().refreshTables();
				dlg.setVisible(false);
				dlg.dispose();
				return;
			}
			setupFilePage();
		} catch (SourceFileException | PdfException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
	}
	
	private void moveCompletedFiles() {
		int iFiles = openFileList.moveCompleteFiles();
		String sDirOut = CivetConfig.getOutputDirPath();
    	if( iFiles > 0 )
    		MessageDialog.showMessage(dlg, "Civet Complete", iFiles + " files ready to submit to USAHERDS.\n Original files moved to " + sDirOut );
	}

	private void minimizeAll() {
		dlg.setExtendedState(Frame.ICONIFIED);
		if( parent instanceof Frame )
			((Frame)parent).setExtendedState(Frame.ICONIFIED);
	}

	void rbOutbound_actionPerformed(ActionEvent e) {
		dlg.setImport( false );
		dlg.cbOtherState.setSelectedIndex(0);
		dlg.cbOtherState.setEnabled(true);
		Component c = traversal.getComponentByName(traversal.getProperty("pFirstControl"));
		if( c != null)
			c.requestFocus();
	}

	void rbInbound_actionPerformed(ActionEvent e) {
		dlg.setImport( true );
		dlg.cbOtherState.setSelectedIndex(0);
		dlg.cbOtherState.setEnabled(true);
		Component c = traversal.getComponentByName(traversal.getProperty("pFirstControl"));
		if( c != null)
			c.requestFocus();

	}

	void rbInState_actionPerformed(ActionEvent e) {
		dlg.setImport( false );
		dlg.cbOtherState.setSelectedValue(CivetConfig.getHomeState());
		dlg.cbOtherState.setEnabled(false);
		dlg.lIssuedBy.setVisible(true);
		dlg.cbIssuedBy.setVisible(true);
		dlg.ckAllVets.setVisible(true);
		Component c = traversal.getComponentByName(traversal.getProperty("pInstateFirst"));
		if( c != null)
			c.requestFocus();
	}

	void pdfView() {
		PDFOpener opener = new PDFOpener(dlg);
		// If the file is really a PDF, open it from disk so even things like CO/KS open natively.
		if( currentFile.getSource().canSplit() ) {
			opener.openPageContentInAcrobat(currentFile.getSource().getFilePath(), currentFile.getCurrentPageNo());
		}
		// Otherwise use the content converted to PDF earlier
		else {
			opener.openPDFContentInAcrobat(currentFile.getModel().getPDFAttachmentBytes());
		}
	}
	

	void pdfView( byte pdfBytes[] ) {
		PDFOpener opener = new PDFOpener(dlg);
		opener.openPDFContentInAcrobat(pdfBytes);
	}

	
	protected void pdfViewFile() {
		PDFOpener opener = new PDFOpener(dlg);
		opener.openPDFContentInAcrobat(currentFile.getPDFBytes());
	}

	/**
	 * Save logic is pretty clear EXCEPT when we do weird things like add species
	 * to allow multi-species CVIs.
	 * The ArrayList aSpecies holds the species code and number of each species
	 * for use in the AddIds dialog and save().
	 * @param bClear
	 */
	private void updateSpeciesList( boolean bClear )  {
		String sNum = dlg.jtfNumber.getText();
		String sSpeciesCode = dlg.cbSpecies.getSelectedCode();
		if( sSpeciesCode != null ) {
			int iNum = -1;
			try {
				iNum = Integer.parseInt(sNum);
			}
			catch (NumberFormatException nfe) {
				// Will trigger later error.
				return;
			}
			boolean bFound = false;
			for( SpeciesRecord sr : aSpecies ) {
				if( sr.sSpeciesCode != null && sr.sSpeciesCode.equals(sSpeciesCode) ) {
					bFound = true;
					sr.iNumber = iNum;
				}
			}
			if( !bFound ) {
				SpeciesRecord rSpecies = new SpeciesRecord(sSpeciesCode, iNum);
				aSpecies.add(rSpecies);
			}
			if( bClear ) {
				dlg.jtfNumber.setText("");
				dlg.cbSpecies.setSelectedItem(null);
			}
		}
		if (aSpecies.size() > 1 || (aSpecies.size() == 1 && bClear) )
			dlg.lMultipleSpecies.setVisible(true);
		else
			dlg.lMultipleSpecies.setVisible(false);
	}

	/** This pair of handlers is a complicated way to detect if the selected species
	    has been changed from a previously recorded value.  Most of the logic is
	    simply NOT triggering a change for harmless things like clearing the form.
	*/
	private void doCheckSpeciesChange1() {
		if( !bInSppChangeByCode ) {  // prevent this being called by other triggers.
			sPreviousSpecies = dlg.cbSpecies.getSelectedValue();
		}
	}
	
	/** This pair of handlers is a complicated way to detect if the selected species
    has been changed from a previously recorded value.  Most of the logic is
    simply NOT triggering a change for harmless things like clearing the form.
    
    If the change is confirmed, perform the change.
	*/	
	private void doCheckSpeciesChange2() {
		if( !bInSppChangeByCode ) {  // prevent this being called by other triggers.
			bInSppChangeByCode = true; 
			String sNewSpecies = dlg.cbSpecies.getSelectedValue();
			if( bSppEntered ) {
			if( sPreviousSpecies != null && sPreviousSpecies.trim().length() > 0 &&
					sNewSpecies != null && sNewSpecies.trim().length() > 0 &&
					!sNewSpecies.equals(sPreviousSpecies) ) {
				YesNoDialog dlgYN = new YesNoDialog(dlg, "Civet: Species Change",
						"This will change all occurances of " + sPreviousSpecies +" to " + sNewSpecies 
						+ ".\nProceed?");
				dlgYN.setVisible(true);
				dlgYN.requestFocus();
				if( dlgYN.getAnswer() ) {
					changeSpecies( sPreviousSpecies, sNewSpecies );
				}
				else {
					dlg.cbSpecies.setSelectedItem(sPreviousSpecies);
				}
			}
			}
			bInSppChangeByCode = false;
			bSppEntered = true;
			sPreviousSpecies = null;
		}
	}
	
	/**
	 * Change species in both animal and species lists.
	 * @param sPreviousSpecies
	 * @param sNewSpecies
	 */
	private void changeSpecies( String sPreviousCode, String sNewSpecies ) {
		// Species has changed.  Change Previous to New where ever if exists.
		String sNewCode = SpeciesLookup.getCodeForName(sNewSpecies);
		System.out.println( "Old: " + sPreviousCode + " to " + sNewCode );
		mSpeciesChanges.put(sPreviousCode, sNewCode);
		for( SpeciesRecord sr : aSpecies ) {
			if(sPreviousCode.equals(sr.sSpeciesCode))
				sr.sSpeciesCode = sNewCode;
		}
		for( AnimalIDRecord ar : idListModel.getRows() ) {
			if(sPreviousCode.equals(ar.animal.speciesCode.code))
				ar.animal.speciesCode = new SpeciesCode (sNewCode);
		}

	}


	/**
	 * @return the bImport
	 */
	public boolean isInbound() {
		return dlg.bImport;
	}

	private void runErrorDialog() {
		if( aErrorKeys == null )
			aErrorKeys = new ArrayList<String>();
		CVIErrorDialog dlgErrorDialog = new CVIErrorDialog( dlg, aErrorKeys, sErrorNotes );
		dlgErrorDialog.setModal(true);
		dlgErrorDialog.setVisible(true);
		if( dlgErrorDialog.isExitOK() ) {
			sErrorNotes = dlgErrorDialog.getNotes();
			currentFile.getModel().getMetaData().setErrorNote(sErrorNotes);
			logger.info( "Error note: " + sErrorNotes);
			for( String sError : aErrorKeys ) {
				currentFile.getModel().getMetaData().addError(sError);
			}
			// Process the error list and notes
			if( aErrorKeys.size() > 0 ) {
				dlg.lError.setVisible(true);
			}
			else {
				dlg.lError.setVisible(false);
			}
		}
	}
}// End Class CVIPdfEdit
