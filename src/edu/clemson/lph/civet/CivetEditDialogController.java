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
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.jpedal.exception.PdfException;

import edu.clemson.lph.civet.files.OpenFile;
import edu.clemson.lph.civet.files.OpenFileList;
import edu.clemson.lph.civet.files.OpenFileSaveQueue;
import edu.clemson.lph.civet.files.SourceFileException;
import edu.clemson.lph.civet.lookup.CertificateNbrLookup;
import edu.clemson.lph.civet.lookup.Counties;
import edu.clemson.lph.civet.lookup.ErrorTypeLookup;
import edu.clemson.lph.civet.lookup.PurposeLookup;
import edu.clemson.lph.civet.lookup.SpeciesLookup;
import edu.clemson.lph.civet.lookup.States;
import edu.clemson.lph.civet.lookup.VetLookup;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.threads.OpenFilesThread;
import edu.clemson.lph.civet.webservice.PremisesSearchDialog;
import edu.clemson.lph.civet.webservice.PremisesTableModel;
import edu.clemson.lph.civet.webservice.UsaHerdsLookupPrems;
import edu.clemson.lph.civet.webservice.VetSearchDialog;
import edu.clemson.lph.civet.webservice.WebServiceException;
import edu.clemson.lph.civet.xml.CviMetaDataXml;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.civet.xml.elements.Address;
import edu.clemson.lph.civet.xml.elements.Animal;
import edu.clemson.lph.civet.xml.elements.GroupLot;
import edu.clemson.lph.civet.xml.elements.NameParts;
import edu.clemson.lph.civet.xml.elements.Person;
import edu.clemson.lph.civet.xml.elements.Premises;
import edu.clemson.lph.civet.xml.elements.SpeciesCode;
import edu.clemson.lph.civet.xml.elements.Veterinarian;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.OneLineQuestionDialog;
import edu.clemson.lph.dialogs.ProgressDialog;
import edu.clemson.lph.dialogs.YesNoDialog;
import edu.clemson.lph.pdfgen.PDFOpener;
import edu.clemson.lph.pdfgen.PDFViewer;
import edu.clemson.lph.utils.CountyUtils;
import edu.clemson.lph.utils.PremCheckSum;


public final class CivetEditDialogController {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private CivetEditDialog dlg;
	private String viewerTitle="Civet: ";
	private Window parent = null;

	private ArrayList<SpeciesRecord> aSpecies = new ArrayList<SpeciesRecord>();
	private HashMap<String,String> mSpeciesChanges;
	private AnimalIDListTableModel idListModel = null; //new AnimalIDListTableModel();
	private ArrayList<String> aErrorKeys = new ArrayList<String>();
	private String sErrorNotes;
	
	private String sPriorPhone;
	private String sPriorAddress;
	private String sPriorCity;
	private boolean bLidFromHerds = false;
	
	private PremisesSearchDialog premSearch = new PremisesSearchDialog();

	private String sPrevCVINo;
	private String sPreviousSpecies;
	private boolean bSppEntered = false;
	private boolean bInSppChangeByCode = false;
	private boolean bInSearch = false;
	private boolean bReOpened = false;
	private FormEditListener formEditListener = null;
	private ArrayList<File> filesToOpen = null;
	private OpenFileList openFileList = null;
	private OpenFile currentFile = null;
	private OpenFileSaveQueue saveQueue = null;
	private PDFViewer viewer = null;

	private String sDefaultPurpose;
	
	private CivetEditOrderTraversalPolicy traversal;
	private Component cStartingComponentFocus = null;

	private VetLookup vetLookup;
	private boolean bInCleanup = false;
	private boolean bInEditLast;
	private String sLastSaved = null;

	/**
	 * construct an empty pdf viewer and pop up the open window
	 * @throws SourceFileException 
	 * @wbp.parser.constructor
	 */
	public CivetEditDialogController( Window parent, CivetEditDialog dlg, ArrayList<File> files ) throws SourceFileException{
		this.dlg = dlg;
		this.parent = parent;
		this.filesToOpen = files;
		this.viewer = new PDFViewer();
		this.saveQueue = new OpenFileSaveQueue(this);  // Queue needs reference for call-back from thread complete.
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
		openFileList = new OpenFileList();
		OpenFilesThread t = new OpenFilesThread(this, filesToOpen, openFileList);
		t.start();
	}
	
	/**
	 * Called on the event dispatch thread after the OpenFilesThread completes.
	 */
	public void openFilesComplete() {
		try {
			if( openFileList == null  || openFileList.isEmpty() ) {
				MessageDialog.showMessage(dlg, "Civet Error: Empty File List", "Open files returned an empty list");
				logger.error("Open files returned an empty list");
				dlg.setVisible(false);
			}
			else {
				openFileList.firstFile();
				dlg.setViewer(viewer);
			}
		} catch (PdfException e) {
			MessageDialog.showMessage(dlg, "Civet Error: PDF Error", "Failed to open PDF\n" + e.getMessage() );
			logger.error(e);
		}
		setupNewFilePage();
	}
		
	private void setupNewFilePage() {
		try {
			currentFile = openFileList.getCurrentFile();
			idListModel = new AnimalIDListTableModel(currentFile.getModel());
			setAllFocus();
			dlg.setTitle(getViewerTitle()+currentFile.getSource().getFileName());
			setPage(currentFile.getCurrentPageNo());
			setPages(currentFile.getPageCount());
			setFile(openFileList.getCurrentFileNo());
			setFiles(openFileList.getFileCount());
			dlg.setFormEditable(dlg.iMode != CivetEditDialog.VIEW_MODE 
					&& !currentFile.isCurrentPageComplete() );
			updateCounterPanel();
			viewer.setPdfBytes(currentFile.getPDFBytes(), currentFile.isXFA() );
			viewer.viewPage(currentFile.getCurrentPageNo()); 
			clearForm();
			viewer.setRotation(currentFile.getSource().getRotation());
			viewer.updatePdfDisplay();
			if( currentFile.getSource().isDataFile() )
				populateFromStdXml(currentFile.getSource().getDataModel()) ;
			setupSaveButtons();
			dlg.make90Percent();
			dlg.setVisible(true);  // Only now display
			setupFirstControl();

		} catch( Exception e ) {
			logger.error(e);
		}
	}
	
	/**
	 * Essentially the same as above but don't spend time loading the pdfBytes already in viewer.
	 * For use when currentFile has not changed.
	 */
	private void updateFilePage() {
		try {
			clearForm();
			if( currentFile.isDataFile() )
				populateFromStdXml( currentFile.getModel() );
			idListModel = new AnimalIDListTableModel( currentFile.getModel() );
//			dlg.setTitle(getViewerTitle()+currentFile.getSource().getFileName());
			setPage(currentFile.getCurrentPageNo());
			setPages(currentFile.getPageCount());
			setFile(openFileList.getCurrentFileNo());
			setFiles(openFileList.getFileCount());
			dlg.setFormEditable(dlg.iMode != CivetEditDialog.VIEW_MODE 
					&& !currentFile.isCurrentPageComplete() );
			updateCounterPanel();
//			viewer.setRotation(currentFile.getSource().getRotation());
			viewer.viewPage(currentFile.getCurrentPageNo()); 
			viewer.updatePdfDisplay();
			setupSaveButtons();
			setupFirstControl();

		} catch( Exception e ) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	private void setupFirstControl() {
		if( currentFile.isDataFile() ) {
			cStartingComponentFocus = traversal.getComponentByName(traversal.getProperty("pPDFLoaded"));
		}
		else if(dlg.rbInState.isSelected()) {
			cStartingComponentFocus = traversal.getComponentByName(traversal.getProperty("pInstateFirst"));
		}
		else if(dlg.rbExport.isSelected() || dlg.rbImport.isSelected() ) {
			cStartingComponentFocus = traversal.getComponentByName(traversal.getProperty("pFirstControl"));
		}
		if( cStartingComponentFocus != null)
			cStartingComponentFocus.requestFocus();
	}
	
	private void setupSaveButtons() {
		if( currentFile.getSource().canSplit() && currentFile.getCurrentPageNo() > 1 && saveQueue.hasFileInQueue()) {
			dlg.bAddToPrevious.setVisible(true);
		}
		else {
			dlg.bAddToPrevious.setVisible(false);
		}
		if( saveQueue.hasFileInQueue() || sLastSaved != null ) {
			dlg.bEditLast.setEnabled(true);
		}
		else {
			dlg.bEditLast.setEnabled(false);
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
				doSave();
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
							currentFile = openFileList.getCurrentFile();
							updateCounterPanel();
							setupNewFilePage();
						}
					} catch (PdfException e1) {
						logger.error(e1);
					}
				}
			});
			dlg.pCounters.bPageBack.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						if( currentFile.morePagesBack(false) ) {
							currentFile.pageBackward(false);
						}
						else if( openFileList.moreFilesBack(false) ) {
							openFileList.fileBackward(false);
						}
						else {
							logger.error("Attempt to move past first page");
						}
						updateCounterPanel();
						viewer.viewPage(currentFile.getCurrentPageNo());
					} catch (SourceFileException | PdfException e1) {
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
						if( currentFile.morePagesForward(false) ) {
							currentFile.pageForward(false);
						}
						else if( openFileList.moreFilesForward(false) ) {
							openFileList.fileForward(false);
						}
						else {
							logger.error("Attempt to move past last page");
						}
						updateCounterPanel();
						viewer.viewPage(currentFile.getCurrentPageNo());
					} catch (SourceFileException | PdfException e1) {
						logger.error(e1);
					}
				}
			});
			dlg.pCounters.bFileForward.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						if( openFileList.moreFilesForward(false) ) {
							openFileList.fileForward(false);
							currentFile = openFileList.getCurrentFile();
							updateCounterPanel();
							setupNewFilePage();
						}
					} catch (PdfException e1) {
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
				rbImport_actionPerformed(e);
			}
		});
		dlg.rbExport.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rbExport_actionPerformed(e);
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
		dlg.jtfOtherName.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusLost(FocusEvent e) {
				jtfOtherName_focusLost(e);
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
					sZip = sZip.trim();
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
		dlg.bAddToPrevious.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doAddToPrevious();
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
				doSave();
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
	
//	private void setFileCompleteStatus() {
//		if( !currentFile.getSource().canSplit() // File goes as a whole
//				|| (!currentFile.morePagesForward(true) ) ) { // or no pages left
//			// && !currentFile.morePagesBack(true) // if we don't want to ignore skipped pages
//			if( !isReopened() )
//				openFileList.markFileComplete(currentFile);
//		}
//	}
	
	private void setFileCompleteStatus() {
		if( !currentFile.getSource().canSplit() // File goes as a whole
				|| currentFile.isFileComplete() ) { // or no pages left
			if( !isReopened() )
				openFileList.markFileComplete(currentFile);
		}
	}

	
	/**
	 * Add the currently displayed page to model retained 
	 * from previous page and open controls for further editing
	 * @throws IOException 
	 */
	private void doAddToPrevious() {
		try {
			dlg.bAddToPrevious.setVisible(false);
			int iCurrentPage = currentFile.getCurrentPageNo();
			currentFile.addPageToCurrent(iCurrentPage); // add the new page.  Is the old page marked complete? No!
			byte[] xmlBytes = saveQueue.pop();   // retrieve the previously "saved" file
			StdeCviXmlModel model = new StdeCviXmlModel(xmlBytes);
			byte[] bSavedFile = model.getPDFAttachmentBytes();
			currentFile.prependFile(bSavedFile); 
			populateFromStdXml( model ); // populate the form
			sPrevCVINo = null;  // Disable duplicate check because it will be the saved one.
			viewer.viewPage(iCurrentPage); 
			viewer.updatePdfDisplay();

		} catch (SourceFileException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	public String getViewerTitle() { return viewerTitle; }

	/**
	 * Use OpenFiles controller to set the file/page counter
	 */
	private void updateCounterPanel() {
		boolean morePagesBack = currentFile.morePagesBack(false);
		boolean morePagesForward = currentFile.morePagesForward(false);
		dlg.pCounters.setPageBackEnabled(morePagesBack);
		dlg.pCounters.setPageForwardEnabled(morePagesForward);
		dlg.pCounters.setFileBackEnabled(openFileList.moreFilesBack(false));
		dlg.pCounters.setFileForwardEnabled(openFileList.moreFilesForward(false));
		dlg.pCounters.setFile(openFileList.getCurrentFileNo());
		dlg.pCounters.setFiles(openFileList.getFileCount());
		dlg.pCounters.setPage(openFileList.getCurrentFile().getCurrentPageNo());
		dlg.pCounters.setPages(openFileList.getCurrentFile().getPageCount());
		if( morePagesBack || morePagesForward )
			dlg.bGotoPage.setEnabled(true);
		else
			dlg.bGotoPage.setEnabled(false);	
		// if navigated off the currentFile, disable editing
		dlg.setFormEditable( openFileList.getCurrentFile() == currentFile );
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
		
	/**
	 * Move dialog to the last saved file.
	 * Simple utility for quick corrections.
	 * @param bLastPage
	 * @throws SourceFileException 
	 */
	private void doEditLast() {
		try {
			bInEditLast = true;
			if( saveQueue.hasFileInQueue() )
				saveQueue.flush();
			else if (sLastSaved != null )
				doEditLast2(sLastSaved);
			else
				MessageDialog.showMessage(dlg, "Civet Error: No File Saved", "No file found in save queue.");
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	private void doEditLast2 (String sFilePath) {
		bInEditLast = false;
		dlg.bAddToPrevious.setVisible(false);
		try {
			ArrayList<File> aFiles = new ArrayList<File>();
			File fFile = new File( sFilePath );		
			aFiles.add(fFile);
			@SuppressWarnings("unused")
			CivetEditDialog dlg2 = new CivetEditDialog( this.parent, aFiles, 20 );
		} catch (SourceFileException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
	}
	
	
	private void checkZipcode(JTextField jtfZip) {
		if( jtfZip == null ) 
			return;
		String sZip = jtfZip.getText().trim();
		if( sZip == null || sZip.trim().length() == 0 )
			return;
		sZip = sZip.trim();
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
		boolean bRet = false;
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
			MessageDialog.showMessage(dlg, "Civet Warning: Predated", "Warning: Issue data later than received date.");
		}
	}

	private void cancel() {
		if( formEditListener.isChanged() || YesNoDialog.ask(dlg, "Civet: Close", "Close without saving?") ) {
			doCleanup();
			dlg.setVisible(false);
		}
	}
	
	/**
	 * The queue saves files as they are pushed out of the top spot but also when we 
	 * explicitly call flush() during cleanup.  Only do the post flush stuff when
	 * in cleanup.
	 */
	private void doCleanup() {
		bInCleanup = true;
		saveQueue.flush();
	}
	
	/**
	 * Called each time the Queue saves a file.  Only respond to the final save
	 * to move the saved files and refresh the inbox.
	 */
	public void saveComplete(String sFilePath) {
		sLastSaved = sFilePath;
		if( bInEditLast ) {
			doEditLast2(sFilePath);
		}
		if( bInCleanup) {
			moveCompletedFiles();
			CivetInboxController.getCivetInboxController().refreshTables();
			bInCleanup = false;
		}
		prog.setVisible(false);
		setupFirstControl();
	}

	public boolean isReopened() {
		boolean bRet = false;
		String sInDir = CivetConfig.getInputDirPath();
		File fCurrentFile = currentFile.getSource().getSourceFile();
		String sCurrentDir = fCurrentFile.getParent();
		bRet = ( !sCurrentDir.equalsIgnoreCase(sInDir) );
		return bRet;
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
	private void gotoPage( int iPage ) {
		try {
			currentFile.gotoPageNo(iPage);
			updateFilePage();
		} catch (SourceFileException e) {
			logger.error(e);
		}
	}
	
	public void setPage( int iPage ) {
		dlg.pCounters.setPage(iPage);
	}

	private void setPages( int iPages ) {
		dlg.pCounters.setPages(iPages);
	}
	
	private void setFile( int iFileNo ) {
		dlg.pCounters.setFile(iFileNo); // currentFiles is 0 indexed array
	}

	private void setFiles( int iFiles ) {
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

	private void jtfOtherName_focusLost(FocusEvent e) {
		String sOtherName = dlg.jtfOtherName.getText();	
		if( sOtherName == null ) return;
		int iLen = sOtherName.trim().length();
		String sOtherStateID = null;
		String sOtherPIN = null;
		if( iLen == 8 ) sOtherStateID = sOtherName.toUpperCase();
		else if( iLen == 8 ) sOtherPIN = sOtherName.toUpperCase();
		else return;
		try {
			if( PremCheckSum.isValidPIN(sOtherPIN) || PremCheckSum.isValidPIN(sOtherStateID) ) {
				UsaHerdsLookupPrems model = new UsaHerdsLookupPrems(sOtherStateID, sOtherPIN);
				if( model != null ) {
					if( model.first() )
						if( model.next() ) {
							dlg.jtfOtherName.setText(model.getPremName());
							dlg.jtfOtherAddress.setText(model.getAddress());
							dlg.jtfOtherCity.setText(model.getCity());
							dlg.jtfOtherZip.setText(model.getZipCode());
							dlg.cbOtherCounty.setSelectedItem(model.getCounty());
							dlg.jtfPhone.requestFocus();
						}
				}
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			logger.error(e1);
		}
		
	}

	private void jtfThisPIN_focusLost(FocusEvent e) {
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

	private void jtfPhone_focusLost(FocusEvent e) {
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

	private void jtfAddrCity_focusLost(FocusEvent e) {
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
	
	private boolean hasData() {
		if( dlg.jtfCVINo.getText().trim().length() > 0 ) return true;
		if( aSpecies.size() > 0 ) return true; 
		if( dlg.cbOtherState.getSelectedIndex() > 0 ) return true;
		if( dlg.jtfOtherName.getText().trim().length() > 0 ) return true;
		if( dlg.jtfOtherAddress.getText().trim().length() > 0 ) return true;
		if( dlg.jtfOtherCity.getText().trim().length() > 0 ) return true;
		if( dlg.cbOtherCounty.getSelectedIndex() > 0 ) return true;
		if( dlg.jtfOtherZip.getText().trim().length() > 0 ) return true;
		if( dlg.jtfThisPIN.getText().trim().length() > 0 ) return true;
		if( dlg.jtfThisName.getText().trim().length() > 0 ) return true;
		if( dlg.jtfPhone.getText().trim().length() > 0 ) return true;
		if( dlg.jtfAddress.getText().trim().length() > 0 ) return true;
		if( dlg.jtfThisCity.getText().trim().length() > 0 ) return true;
		if( dlg.cbThisCounty.getSelectedIndex() > 0  ) return true;
		if( dlg.jtfZip.getText().trim().length() > 0 ) return true;
		if( dlg.jtfDateInspected.getText().trim().length() > 0 ) return true;
		if( dlg.jtfDateReceived.getText().trim().length() > 0 ) return true;
		if( dlg.jtfCVINo.getText().trim().length() > 0 ) return true;
		if( dlg.cbSpecies.getSelectedIndex() > 0  ) return true;
		if( dlg.jtfNumber.getText().trim().length() > 0 ) return true;
		if( dlg.cbIssuedBy.getSelectedIndex() > 0  ) return true;
		if( dlg.jtfIssuedBy.getText().trim().length() > 0 ) return true;
		return false;
	}

	/**
	 * Clearing the form is complicated by various modes and settings.
	 */
	private void clearForm() {
		bSppEntered = false;
		bInSppChangeByCode = true;
		formEditListener.clear();
		// Always start with a blank search box.
		dlg.jtfThisPIN.getSearchDialog().clear(); 
		// Don't just clear() existing array because it has been passed by reference to saveThread.
		aSpecies = new ArrayList<SpeciesRecord>(); 
		// clear the form and select main map if the check box is not checked or we just did an XFA
		if( !dlg.ckSticky.isSelected() ) {  // need to restore logic for file after XFA.
			traversal.selectMainMap();
			dlg.cbOtherState.setSelectedKey(-1);
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
			dlg.jtfCVINo.setText("");  // Pam really wants this sticky!
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
		}
		if(dlg.rbExport.isSelected()) {
			dlg.lIssuedBy.setVisible(true);
			dlg.lIssuedBy.setFont(new Font("Tahoma", Font.BOLD, 11));
			dlg.cbIssuedBy.setVisible(true);
			dlg.jtfIssuedBy.setVisible(false);
			dlg.ckAllVets.setVisible(true);
		}
		if(dlg.rbImport.isSelected()) {
			dlg.lIssuedBy.setVisible(true);
			dlg.lIssuedBy.setFont(new Font("Tahoma", Font.PLAIN, 11));
			dlg.cbIssuedBy.setVisible(false);
			dlg.jtfIssuedBy.setVisible(true);
			dlg.ckAllVets.setVisible(false);
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
	private void populateFromStdXml( StdeCviXmlModel xStd ) {
		if( xStd == null ) {
			logger.error(new Exception("populateFromStdXml called with null StdeCviXml"));
			return;
		}
		else {
			try {
				String sCertNbr = xStd.getCertificateNumber();
				String sSource = xStd.getCertificateNumberSource();
				if( sSource != null && sSource.trim().length() > 0 )
					sCertNbr = sSource + ":" + sCertNbr;
				if( CertificateNbrLookup.certficateNbrExists(sCertNbr) ) {
					MessageDialog.showMessage(dlg, "Civet Warning", "Certificate " + sCertNbr + " has already been uploaded to HERDS");
				}
				bSppEntered = true;
				bInSppChangeByCode = true;
				String sOriginState = xStd.getOrigin().address.state;
				if( sOriginState != null ) {
					sOriginState = States.getState(sOriginState);
					Premises pOrigin = xStd.getOrigin();
					if( pOrigin == null ) {
						MessageDialog.showMessage(dlg, "Civet Error", "Data File lacks Origin information\nEnter manually");
						return;
					}
					Premises pDestination = xStd.getDestination();	
					if( pDestination == null ) {
						MessageDialog.showMessage(dlg, "Civet Error", "Data File lacks Destination information\nEnter manually");
						return;
					}
					if( sOriginState != null && sOriginState.equalsIgnoreCase(CivetConfig.getHomeState()) ) {
						dlg.setImport(false);
						dlg.rbExport.setSelected(true);
						String sOtherState = States.getState(pDestination.address.state);
						dlg.cbOtherState.setSelectedValue(sOtherState);
						refreshOtherCounties();
						String sOtherName = getBestNameForPremises( pDestination );
						dlg.jtfOtherName.setText(sOtherName);
						dlg.jtfOtherAddress.setText(pDestination.address.line1);
						dlg.jtfOtherCity.setText(pDestination.address.town);
						String sOtherStateCode = dlg.cbOtherState.getSelectedCode();
						String sOtherCountyIn = pDestination.address.county;
						String sOtherZip = pDestination.address.zip;
						String sOtherHerdsCounty = getHerdsCounty( sOtherStateCode, sOtherCountyIn, sOtherZip );
						dlg.cbOtherCounty.setSelectedItem(sOtherHerdsCounty);
						dlg.jtfOtherZip.setText(sOtherZip);
						String sThisName = getBestNameForPremises( pOrigin );
						dlg.jtfThisName.setText(sThisName);
						dlg.jtfThisPIN.setText(pOrigin.premid);
						dlg.jtfPhone.setText(pOrigin.personPhone);
						dlg.jtfAddress.setText(pOrigin.address.line1);
						dlg.jtfThisCity.setText(pOrigin.address.town);
						String sThisStateCode = CivetConfig.getHomeStateAbbr();
						String sThisCountyIn = pOrigin.address.county;
						String sThisZip = pOrigin.address.zip;
						String sThisHerdsCounty = getHerdsCounty( sThisStateCode, sThisCountyIn, sThisZip );
						dlg.cbThisCounty.setSelectedItem(sThisHerdsCounty);
						dlg.jtfZip.setText(sThisZip);
						String sNAN = xStd.getVet().nationalAccreditationNumber;
						if( sNAN != null && sNAN.trim().length() > 0 ) {
							VetLookup vet = new VetLookup( sNAN );
							int iVetKey = vet.getKey();
							if( vet != null ) {
								if( !dlg.cbIssuedBy.hasKey( iVetKey ) ) { // Small animal vet?
									doShowAllVets(false);  // this is a toggle false means it wasn't NOW checked.
								}
								dlg.cbIssuedBy.setSelectedKey( iVetKey );
								if( !dlg.cbIssuedBy.hasKey( iVetKey ) ) { 
									MessageDialog.showMessage(dlg, "Civet Unknown Vet", 
											"HERDS doesn't seem to have a Veterinarian record for " + vet.getFormattedName() 
											+ "\n Update and come back to this CVI.");
								}
							}
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
							if( vet.isUniqueMatch() ) {
								dlg.cbIssuedBy.setSelectedKey(vet.getKey());
							}
						}
					}
					else {
						dlg.setImport(true);
						dlg.rbImport.setSelected(true);
						String sOtherState = States.getState(pOrigin.address.state);
						dlg.cbOtherState.setSelectedValue(sOtherState);
						String sOtherName = getBestNameForPremises( pOrigin );
						//					cbOtherCounty.setText(xStd.getOriginPremId());
						dlg.jtfOtherName.setText(sOtherName);
						dlg.jtfOtherAddress.setText(pOrigin.address.line1);
						dlg.jtfOtherCity.setText(pOrigin.address.town);
						refreshOtherCounties();
						String sOtherStateCode = dlg.cbOtherState.getSelectedCode();
						String sOtherCountyIn = pOrigin.address.county;
						String sOtherZip = pOrigin.address.zip;
						if( sOtherZip != null )
							sOtherZip = sOtherZip.trim();
						String sOtherHerdsCounty = getHerdsCounty( sOtherStateCode, sOtherCountyIn, sOtherZip );
						dlg.jtfOtherZip.setText(sOtherZip);
						dlg.cbOtherCounty.setSelectedItem(sOtherHerdsCounty);
						String sThisName = getBestNameForPremises( pDestination );
						dlg.jtfThisName.setText(sThisName);
						dlg.jtfThisPIN.setText(pDestination.premid);
						dlg.jtfPhone.setText(pDestination.personPhone);
						dlg.jtfAddress.setText(pDestination.address.line1);
						dlg.jtfThisCity.setText(pDestination.address.town);
						String sThisState = CivetConfig.getHomeStateAbbr();
						String sThisCountyIn = pDestination.address.county;
						String sThisZip = pDestination.address.zip;
						if( sThisZip != null )
							sThisZip = sThisZip.trim();
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
					// Species groups and animals are a mess!!!
					// Species multiples are an issue
					loadSpeciesFromStdXml(xStd);
					// Load IDs
					idListModel = new AnimalIDListTableModel(xStd);
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
						aErrorKeys = new ArrayList<String>();
						dlg.lError.setVisible(false);					
					}
					if( dlg.jtfDateReceived.getDate() == null ) {
						if( CivetConfig.isDefaultReceivedDate() ) 
							dlg.jtfDateReceived.setDate(currentFile.getLastAccessDate());
						else
							dlg.jtfDateReceived.setText("");
					}
				}
				bInSppChangeByCode = false;
			} catch( Exception e ) {
				logger.error("Unexpected error loading from XML standard document", e);
			}
		}
	}
	
	private String getBestNameForPremises( Premises prem ) {
		String sRet = null;
		if( prem != null ) {
			if( prem.premName != null && prem.premName.trim().length() > 0 ) {
				sRet = prem.premName;
			}
			else {
				NameParts nameParts = prem.personNameParts;
				if( nameParts != null && 
						( (  nameParts.firstName != null && nameParts.firstName.trim().length() > 0 ) ||
					      ( nameParts.lastName != null && nameParts.lastName.trim().length() > 0 ) ) ) {
					sRet = nameParts.firstName + " " + nameParts.lastName;
				}
				else if( prem.personName != null ){
					sRet = prem.personName;
				}
			}
		}
		return sRet;
	}
	
	private String getHerdsCounty( String sStateCode, String sCounty, String sZip ) {
		String sRet = Counties.getHerdsCounty(sStateCode, sCounty);
		if( sRet == null && sZip != null && sZip.trim().length() >= 5 && !sZip.equals("00000") ) {
				try {
					String sZipCounty = CountyUtils.getCounty(sZip);
					sRet = Counties.getHerdsCounty(sStateCode, sZipCounty);	
				} catch (IOException e) {
					logger.error(e);
				}
		}
		return sRet;
	}
	
//	private void loadIDListFromStdXml(StdeCviXmlModel std) {
//		idListModel = new AnimalIDListTableModel(std);
//		ArrayList<Animal> animals = std.getAnimals();
//		for( Animal animal : animals) {
//			
//		}
//	}
//
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
			if( ! idListModel.contains(animal))
				idListModel.addRow(animal.speciesCode.code, animal.getBestID());
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
	
	private void doAddIDs() {
		if( ( dlg.cbSpecies.getSelectedCode() == null || dlg.cbSpecies.getSelectedCode().trim().length() == 0 ) && aSpecies.size() == 0 ) {
			MessageDialog.showMessage(dlg, "Civet Error", "Species must be added before IDs" );
		}
		else {
			updateSpeciesList(false);
			AddAnimalsDialog dlg = new AddAnimalsDialog( aSpecies, idListModel );
			dlg.setModal(true);
			dlg.setVisible(true);
			setActiveSpecies( dlg.getSelectedSpecies() );
		}
	}
	
	private ProgressDialog prog = null;

	/**
	 * Default save action for currently loaded model and form.
	 */
	private void doSave() {
		try {
			if( prog == null ) {
				// Note running on dispatch thread so won't update window contents, just frame.
				prog = new ProgressDialog(dlg, "Civet: Saving Page", "Saving Page");  
			}
			prog.setVisible(true);
			// If save() finds errors be sure form is editable so they can be corrected.
			// Avoid stray input as we save.
			dlg.setFormEditable( false );
			currentFile.setCurrentPagesDone();
//			cStartingComponentFocus = null;
			// For multi-page PDF this creates a new OpenFile from the current pages.  All others just self.
			// This is the root of all evil.  The logic should not be File Save but FilePagesSave.  For
			// non-multi CVI PDF files pages will be all.  
			OpenFile fileToSave = currentFile;
			currentFile = fileToSave.newOpenFileFromSource();
			openFileList.replaceFile( fileToSave, currentFile ); 
			setFileCompleteStatus();
			if( save(fileToSave) ) {  // Saved, no errors that require changes on current page
				if( navigateToNextPage() ) {  // Found another page to edit
					dlg.setFormEditable( true );
				}
				else {  // No more pages to edit, we're done
					doCleanup();
					dlg.setVisible(false);
					dlg.dispose();
				}
			}
			else { // Release this page for further editing
				dlg.setFormEditable(true);
			}

		} catch (SourceFileException e) {
			String sCVINo = dlg.jtfCVINo.getText();
			MessageDialog.showMessage(getDialog(), "Civet Error", "Failed to save Certificate number " + sCVINo);
			logger.error(e);
		}
	}

	/** 
	 * Specify the OpenFile and model to save for use in split files.
	 * @throws SourceFileException 
	 */
	private boolean save( OpenFile fileToSave ) throws SourceFileException {
		String sCVINo = dlg.jtfCVINo.getText();
		String sCVINoSource = fileToSave.getSource().getSystem();
		if( !bReOpened && sCVINo.equalsIgnoreCase(sPrevCVINo) ) {
			MessageDialog.showMessage(dlg, "Civet Error", "Certificate number " + sCVINo + " hasn't changed since last save");
			dlg.jtfCVINo.requestFocus();
			dlg.setFormEditable(true);
			if( prog != null ) {
				prog.setVisible(false);
			}
			return false;
		}
		String sOriginStateCode;
		if( dlg.rbImport.isSelected() )
			sOriginStateCode = States.getStateCode(dlg.cbOtherState.getSelectedValue());
		else 
			sOriginStateCode = CivetConfig.getHomeStateAbbr();
		if( sCVINoSource != null && sCVINoSource.equals("Paper") )
			sCVINoSource = sOriginStateCode + " Paper";
		if( !bReOpened && CertificateNbrLookup.certficateNbrExists(sCVINo, sCVINoSource ) ) {
			MessageDialog.showMessage(dlg, "Civet Error", "Certificate number " + sCVINo + " already exists");
			dlg.jtfCVINo.requestFocus();
			dlg.setFormEditable(true);
			if( prog != null ) {
				prog.setVisible(false);
			}
			return false;
		}
		bReOpened = false;
		// Collect up all values needed
		fileToSave.getModel().checkExpiration();  // Set expiration date if not in data file already.
		fileToSave.getModel().checkQuantity();  // Set expiration date if not in data file already.
		String sInvalidID = fileToSave.getModel().checkAnimalIDTypes();
		if( sInvalidID != null ) {
			MessageDialog.showMessage(dlg, "Civet Warning", "Animal ID " + sInvalidID
					+ " in certificate " + sCVINo + " is not valid for its type.\nChanged to 'OtherOfficialID'");
		}
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
		if(sOtherZipcode != null) sOtherZipcode = sOtherZipcode.trim();
//		String sOtherPIN = null; //dlg.jtfOtherPIN.getText();
		String sThisPremisesId = dlg.jtfThisPIN.getText();
		if( sThisPremisesId != null && sThisPremisesId.trim().length() != 7 && CivetConfig.hasBrokenLIDs() && !bLidFromHerds )
			sThisPremisesId = null;
		String sPhone = dlg.jtfPhone.getText();
		String sThisName = dlg.jtfThisName.getText();
		String sStreetAddress = dlg.jtfAddress.getText();
		String sCity = dlg.jtfThisCity.getText();
		String sZipcode = dlg.jtfZip.getText();
		if( sZipcode != null ) sZipcode = sZipcode.trim();
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
			dlg.setFormEditable(true);
			if( prog != null ) {
				prog.setVisible(false);
			}
			return false;
		}
		else if ( bInbound && ( sIssuedByName == null || sIssuedByName.trim().length() == 0 ) ) {
			sIssuedByName = "Out of State Vet";
		}
		updateSpeciesList(false);
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
			dlg.setFormEditable(true);
			if( prog != null ) {
				prog.setVisible(false);
			}
			return false;
		}

		// Precondition: PDF, Animals and Errors already in model.
		// NOTE!!!!  model is not thread safe at this point.  
		byte[] xmlBytes = buildXml( fileToSave.getModel(), bInbound, bDataFile, 
				aSpecies, sOtherStateCode,
				sOtherName, sOtherAddress, sOtherCity, sOtherCounty, sOtherZipcode, //sOtherPIN,
				sThisPremisesId, sThisName, sPhone,
				sStreetAddress, sCity, sThisCounty, sZipcode,
				dDateIssued, dDateReceived, iIssuedByKey, sIssuedByName, sCVINo, sCVINoSource,
				sMovementPurpose);
		sPrevCVINo = sCVINo;
		saveQueue.push(xmlBytes);
		return true;
	}
	
	/**
	 * Navigate to the next page to edit
	 * Here we ask for only incomplete pages.  (Nav bar steps through all pages and files).
	 * TODO Decide if the next page MIGHT be part of the just saved CVI and offer correct choices.
	 * @return boolean true if incomplete page found
	 */
	private boolean navigateToNextPage() {
		try {
			// Only on multipage PDF do we page forward on save
			if( currentFile.getSource().canSplit() && currentFile.morePagesForward(true) ) {
				currentFile.pageForward(true);
				updateFilePage(); 
			}
//			else if( currentFile.getSource().canSplit() && currentFile.morePagesBack(true) ) {
//				currentFile.pageBackward(true);
//				updateFilePage(); 
//			}
			// Backup to get skipped pages?  Not currently.
			else if ( openFileList.moreFilesForward(true) || openFileList.moreFilesBack(true) ) {
				currentFile = openFileList.nextFile(true);
				setupNewFilePage();
			}
			else {
				return false;
			}
		} catch (SourceFileException e) {
			logger.error("Failure to read file " + currentFile.getSource().getFileName(), e);
		}
		prog.setVisible(false);
		return true;
	}
	
	private void moveCompletedFiles() {
		int iFiles = openFileList.moveCompleteFiles();
		String sDirOut = CivetConfig.getOutputDirPath();
    	if( iFiles > 0 )
    		MessageDialog.showMessage(dlg, "Civet Complete", iFiles + " original files moved to " + sDirOut +
    				"\nProcessed files ready to submit to USAHERDS.");
	}

	private void minimizeAll() {
		dlg.setExtendedState(Frame.ICONIFIED);
		if( parent instanceof Frame )
			((Frame)parent).setExtendedState(Frame.ICONIFIED);
	}

	private void rbExport_actionPerformed(ActionEvent e) {
		if( hasData() ) {
			YesNoDialog yn = new YesNoDialog( dlg, "Civet: Reverse", "All data will be lost.\nDo you want to reverse direction?");
			yn.setVisible(true);
			boolean bOK = yn.getAnswer();
			if( !bOK ) {
				if( isInbound() )
					dlg.rbImport.setSelected(true);
				else
					dlg.rbInState.setSelected(true);
				return;
			}
			clearForm();
			currentFile.getModel().clear();
		}
		dlg.setImport( false );
		dlg.cbOtherState.setSelectedIndex(0);
		dlg.cbOtherState.setEnabled(true);
		Component c = traversal.getComponentByName(traversal.getProperty("pFirstControl"));
		if( c != null)
			c.requestFocus();
	}

	private void rbImport_actionPerformed(ActionEvent e) {
		if( hasData() ) {
			YesNoDialog yn = new YesNoDialog( dlg, "Civet: Reverse", "All data will be lost.\nDo you want to reverse direction?");
			yn.setVisible(true);
			boolean bOK = yn.getAnswer();
			if( !bOK ) { 
				String sOtherState = dlg.cbOtherState.getSelectedCode();
				String sHomeState = CivetConfig.getHomeStateAbbr();
				if( sHomeState.equals(sOtherState) )
					dlg.rbInState.setSelected(true);
				else
					dlg.rbExport.setSelected(true);
				return;
			}
			clearForm();
			currentFile.getModel().clear();
		}
		dlg.setImport( true );
		dlg.cbOtherState.setSelectedIndex(0);
		dlg.cbOtherState.setEnabled(true);
		Component c = traversal.getComponentByName(traversal.getProperty("pFirstControl"));
		if( c != null)
			c.requestFocus();
	}

	private void rbInState_actionPerformed(ActionEvent e) {
		if( hasData() ) {
			YesNoDialog yn = new YesNoDialog( dlg, "Civet: Reverse", "All data will be lost.\nDo you want to reverse direction?");
			yn.setVisible(true);
			boolean bOK = yn.getAnswer();
			if( !bOK ){
				if( isInbound() )
					dlg.rbImport.setSelected(true);
				else
					dlg.rbExport.setSelected(true);
				return;
			}
			clearForm();
			currentFile.getModel().clear();
		}
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

	private void pdfView() {
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
	
	private void pdfViewFile() {
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
	private void changeSpecies( String sPreviousSpecies, String sNewSpecies ) {
		// Species has changed.  Change Previous to New where ever if exists.
		String sOldCode = SpeciesLookup.getCodeForName(sPreviousSpecies);
		String sNewCode = SpeciesLookup.getCodeForName(sNewSpecies);
		mSpeciesChanges.put(sPreviousSpecies, sNewCode);
		for( SpeciesRecord sr : aSpecies ) {
			if(sOldCode.equals(sr.sSpeciesCode))
				sr.sSpeciesCode = sNewCode;
		}
		for( AnimalIDRecord ar : idListModel.getRows() ) {
			if(sPreviousSpecies.equals(ar.animal.speciesCode.code))
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
		CVIErrorDialog dlgErrorDialog = new CVIErrorDialog( dlg, aErrorKeys, sErrorNotes );
		dlgErrorDialog.setModal(true);
		dlgErrorDialog.setVisible(true);
		if( dlgErrorDialog.isExitOK() ) {
			sErrorNotes = dlgErrorDialog.getNotes();
			if( sErrorNotes != null && sErrorNotes.trim().length() > 0 && aErrorKeys.size() == 0 ) {
				MessageDialog.showMessage(dlg, "Civet Error: No Error Checked", "An error message was added by no code checked.");
			}
			currentFile.getModel().getMetaData().setErrorNote(sErrorNotes);
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
			if( aErrorKeys.size() > 0 && !currentFile.getModel().hasErrors() ) {
				MessageDialog.showMessage(dlg, "Civet Error", "Failed to save errors to data model");
			}
		}
	}
	
	private byte[] buildXml( StdeCviXmlModel model,  
			boolean bImport, boolean bIsDataFile, ArrayList<SpeciesRecord> aSpecies, 
			String sOtherStateCode, String sOtherName, String sOtherAddress, String sOtherCity, 
			String sOtherCounty, String sOtherZipcode, //String sOtherPIN,
			String sThisPIN, String sThisName, String sPhone,
			String sThisAddress, String sThisCity, String sThisCounty, String sZipcode,
			java.util.Date dDateIssued, java.util.Date dDateReceived, 
			Integer iIssuedByKey, String sIssuedByName, String sCVINo, String sCVINoSource,
			String sMovementPurpose) {
		byte[] baRet = null;
		if( model == null ) {
			logger.error("Null model in buildXML");
			model = new StdeCviXmlModel();
		}
		String sOriginStateCode;
		String sOriginPIN = null;
		String sOriginName ;
		String sOriginAddress;
		String sOriginCity;
		String sOriginCounty;
		String sOriginZipCode;
		String sOriginPhone;
		String sDestinationStateCode;
		String sDestinationPIN = null;
		String sDestinationName;
		String sDestinationAddress;
		String sDestinationCity;
		String sDestinationCounty;
		String sDestinationZipCode;
		String sDestinationPhone;
		PurposeLookup purpose = new PurposeLookup();
		String sStdPurpose = purpose.getCodeForValue(sMovementPurpose);
		if( sStdPurpose == null || sStdPurpose.trim().length() == 0 )
			sStdPurpose = "other";
		if( bImport ) {
			sOriginPIN = null;
			sOriginPhone = null;
			sOriginName = sOtherName;
			sOriginAddress = sOtherAddress;
			sOriginStateCode = sOtherStateCode;
			sOriginCity = sOtherCity;
			sOriginCounty = sOtherCounty;
			sOriginZipCode = sOtherZipcode;
			if( model != null && model.getOrigin() != null ) {
				sOriginPIN = model.getOrigin().premid;
				sOriginPhone = model.getOrigin().personPhone;
			}
			sDestinationPIN = sThisPIN;
			sDestinationName = sThisName;
			sDestinationAddress = sThisAddress;
			sDestinationCity = sThisCity;
			sDestinationCounty = sThisCounty;
			sDestinationStateCode = CivetConfig.getHomeStateAbbr();
			sDestinationZipCode = sZipcode;
			sDestinationPhone = sPhone;
		}
		else {
			sOriginPIN = sThisPIN;
			sOriginName = sThisName;
			sOriginAddress = sThisAddress;
			sOriginStateCode = CivetConfig.getHomeStateAbbr();
			sOriginCity = sThisCity;
			sOriginCounty = sThisCounty;
			sOriginZipCode = sZipcode;
			sOriginPhone = sPhone;
			if( model != null && model.getDestination() != null )
				sDestinationPIN = model.getDestination().premid;
			sDestinationName = sOtherName;
			sDestinationAddress = sOtherAddress;
			sDestinationCity = sOtherCity;
			sDestinationCounty = sOtherCounty;
			sDestinationStateCode = sOtherStateCode;
			sDestinationZipCode = sOtherZipcode;
			sDestinationPhone = null;
		}
		// Make sure spelling matches HERDS if possible
		sOriginCounty = Counties.getHerdsCounty(sOriginStateCode, sOriginCounty);
		sDestinationCounty = Counties.getHerdsCounty(sDestinationStateCode, sDestinationCounty); 
		model.setCertificateNumber(sCVINo);
		model.setIssueDate(dDateIssued);
		if( !bIsDataFile ) {  // Don't override vet that signed XFA or mCVI or V2 document
			if( bImport ) {
				model.setVet(sIssuedByName);
			}
			else {
				VetLookup vetLookup = new VetLookup( iIssuedByKey );
				NameParts parts = new NameParts(null, vetLookup.getFirstName(), null, vetLookup.getLastName(), null );
				Address addr = new Address(vetLookup.getAddress(), null, vetLookup.getCity(), null, 
						vetLookup.getState(), vetLookup.getZipCode(), null, null, null);
				Person person = new Person(parts, vetLookup.getPhoneDigits(), null );
				Veterinarian vet = new Veterinarian(person, addr, CivetConfig.getHomeStateAbbr(),
						vetLookup.getLicenseNo(), vetLookup.getNAN());
				model.setVet( vet );
			}
			model.setPurpose(sStdPurpose);
		} // End if !bXFA
		// Expiration date will be set automatically from getXML();
		// We don't enter the person name, normally  or add logic to tell prem name from person name.

		model.setOrigin(sOriginPIN, sOriginName, sOriginPhone, sOriginAddress, sOriginCity, sOriginCounty, sOriginStateCode, sOriginZipCode);
		model.setDestination(sDestinationPIN, sDestinationName,  
					sDestinationPhone, sDestinationAddress, sDestinationCity, sDestinationCounty, 
					sDestinationStateCode, sDestinationZipCode);
		// By suggestion of Mitzy at CAI DO NOT populate Consignor or Consignee unless different!
		
		// Add animals and groups.  This logic is tortured!
		// This could be greatly improved to better coordinate with CO/KS list of animals and the standard's group concept.
		// Precondition:  The model provided already has the species list populated with Animals.
		// Use species list to calculate the number of unidentified animals and build groups accordingly.
		// Precondition:  Individually identified animals were added when the AddIdentifiers dialog closed.
		ArrayList<Animal> animals = model.getAnimals();
		for( AnimalIDRecord r : idListModel.getRows() ) {
			Animal newAnimal = r.animal;
			if( !animals.contains(newAnimal) ) {
				model.addAnimal(newAnimal);
				animals.add(newAnimal);
			}
		}
		// We don't collect separate date inspected for manually entered IDs.
		for( Animal animal : animals ) {
			if(animal.inspectionDate == null || animal.inspectionDate.trim().length() == 0 ) {
				String sDateIssued = StdeCviXmlModel.getDateFormat().format(dDateIssued);
				animal.inspectionDate = sDateIssued;
				model.editAnimal(animal);
			}
		}
		ArrayList<GroupLot> groups = model.getGroups();
		for( SpeciesRecord sr : aSpecies ) {
			// Only add group lot if not officially IDd so count ids and subtract
			String sSpeciesCode = sr.sSpeciesCode;
			int iNumOfSpp = sr.iNumber;
			int iCountIds = 0;
			if( animals != null ) {
				for( Animal animal : animals ) {
					if(  animal.speciesCode.code != null && animal.speciesCode.code.equals(sSpeciesCode) ) {
						iCountIds++;
					}
				}
			}
			boolean bFound = false;
			for( GroupLot group : groups ) {
				if( group.speciesCode.code != null && group.speciesCode.code.equals(sSpeciesCode) ) {
					bFound = true;
					if(iCountIds == iNumOfSpp) {
						model.removeGroupLot(group);
					}
					else if( iCountIds > iNumOfSpp ) {
						MessageDialog.showMessage(dlg, "Civet Error: Animal Count", "Number of tags " + iCountIds + " > number of animals " + iNumOfSpp + " for species " + sSpeciesCode);
						logger.error("Number of tags " + iCountIds + " > number of animals " + iNumOfSpp + " for species " + sSpeciesCode);
						model.removeGroupLot(group);
						sr.iNumber = iCountIds;
					}
					else {
						Integer iNumUntagged = iNumOfSpp - iCountIds;
						group.setQuantity(iNumUntagged.doubleValue());
						model.addOrEditGroupLot(group);
					}
				}
			}
			if( !bFound && iCountIds < iNumOfSpp ) {
				Integer iNumUntagged = iNumOfSpp - iCountIds;
				GroupLot group = new GroupLot(sSpeciesCode, iNumUntagged.doubleValue());
				model.addOrEditGroupLot(group);
			}
		}
//		Precondition model contains the current page or pages in the attachment
//      Precondition model contains metadata for errors saved from the add errors dialog.
		model.setDefaultAnimalInspectionDates(dDateIssued);
		model.setBureauReceiptDate(dDateReceived);
		if( sCVINoSource != null && sCVINoSource.equals("Paper") )
			sCVINoSource = sOriginStateCode + " Paper";
		model.setCertificateNumberSource(sCVINoSource);
		baRet = model.getXMLBytes();
		return baRet;
	}

}
