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

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.jpedal.PdfDecoder;
import org.jpedal.objects.PdfPageData;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.clemson.lph.civet.lookup.CertificateNbrLookup;
import edu.clemson.lph.civet.lookup.Counties;
import edu.clemson.lph.civet.lookup.ErrorTypeLookup;
import edu.clemson.lph.civet.lookup.LocalPremisesTableModel;
import edu.clemson.lph.civet.lookup.PurposeLookup;
import edu.clemson.lph.civet.lookup.SpeciesLookup;
import edu.clemson.lph.civet.lookup.States;
import edu.clemson.lph.civet.lookup.VetLookup;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.threads.AddPageToCviThread;
import edu.clemson.lph.civet.threads.OpenFileThread;
import edu.clemson.lph.civet.threads.SaveCVIThread;
import edu.clemson.lph.civet.webservice.PremisesSearchDialog;
import edu.clemson.lph.civet.webservice.PremisesTableModel;
import edu.clemson.lph.civet.webservice.UsaHerdsLookupPrems;
import edu.clemson.lph.civet.webservice.VetSearchDialog;
import edu.clemson.lph.civet.webservice.WebServiceException;
import edu.clemson.lph.civet.xml.CoKsXML;
import edu.clemson.lph.civet.xml.CviMetaDataXml;
import edu.clemson.lph.civet.xml.StdeCviXml;
import edu.clemson.lph.controls.ComboBoxSelectionListener;
import edu.clemson.lph.controls.DBComboBox;
import edu.clemson.lph.controls.DBNumericField;
import edu.clemson.lph.controls.DBSearchComboBox;
import edu.clemson.lph.controls.DateField;
import edu.clemson.lph.controls.PhoneField;
import edu.clemson.lph.controls.PinField;
import edu.clemson.lph.controls.SearchTextField;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.OneLineQuestionDialog;
import edu.clemson.lph.dialogs.YesNoDialog;
import edu.clemson.lph.pdfgen.PDFOpener;
import edu.clemson.lph.pdfgen.PDFUtils;
import edu.clemson.lph.utils.CountyUtils;
import edu.clemson.lph.utils.FileUtils;
import edu.clemson.lph.utils.PremCheckSum;

import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;


@SuppressWarnings("serial")
public final class CivetEditDialogController {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private CivetEditDialog dlg;
	private String viewerTitle="Civet: ";
	private Window parent = null;

	private CVIFileController controller;

	ArrayList<SpeciesRecord> aSpecies = new ArrayList<SpeciesRecord>();
	HashMap<String,String> mSpeciesChanges;
	AnimalIDListTableModel idListModel = new AnimalIDListTableModel();
	ArrayList<String> aErrorKeys;
	String sErrorNotes;
	
	private String sPriorPhone;
	private String sPriorAddress;
	private String sPriorCity;
	private boolean bLidFromHerds = false;
	
	private PremisesSearchDialog premSearch = new PremisesSearchDialog();

	boolean bGotoLast = false; // Flag to open thread to goto last page when finished loading.
	private String sPrevCVINo;
	private boolean bInEditLast;
	private String sPreviousSpecies;
	private boolean bSppEntered = false;
	private boolean bInClearForm = false;
	boolean bInSearch = false;

	private String sDefaultPurpose;
	
	CivetEditOrderTraversalPolicy traversal;

	VetLookup vetLookup;


	/**
	 * construct an empty pdf viewer and pop up the open window
	 * @wbp.parser.constructor
	 */
	public CivetEditDialogController( Window parent ){
		this.parent = parent;
		dlg = new CivetEditDialog( parent );
		dlg.setController(this);
		controller = new CVIFileController( this ); 
		initializeDBComponents();
		initializeActionHandlers();
		setAllFocus();
		setTraversal();
	}
	
	public CivetEditDialog getDialog() {
		return dlg;
	}
	
	public CivetEditDialog getDialogParent() {
		return dlg.getDialogParent();
	}
	
	public CVIFileController getFileController() {
		return controller;
	}
	
	/**
	 * This is misnamed now that we have no actual database connection.
	 * The idea is to isolate those actions that interfere with GUI design.
	 * Now mostly pulled from lookup tables filled via web service.
	 */
	private void initializeDBComponents() {
		if( !Beans.isDesignTime() ) {
			try {
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
				dlg.make90Percent();
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
				dlg.updatePdfDisplay();
			}
		});
		dlg.bBigger.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dlg.fScale = dlg.fScale * 1.1f;
				dlg.updatePdfDisplay();
			}
		});
		dlg.bSmaller.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dlg.fScale = dlg.fScale / 1.1f;
				dlg.updatePdfDisplay();
			}
		});
		dlg.bRotate.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dlg.iRotation = (dlg.iRotation + 90) % 360;
				dlg.updatePdfDisplay();
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
					controller.fileBackward();
				}
			});
			dlg.pCounters.bPageBack.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					controller.pageBack();
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
					controller.pageForward();
				}
			});
			dlg.pCounters.bFileForward.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					controller.fileForward();
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
				if(bInClearForm)
					return;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						doCheckSpeciesChange1();
					}
				});
				
			}
		});
		dlg.cbSpecies.addItemListener( new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if(bInClearForm)
					return;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if( !bInClearForm )
							doCheckSpeciesChange2();
					}
				});
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
		dlg.bAddToLast.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doAddToLast();
			}
		});
		dlg.bAddIDs.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if( ( dlg.cbSpecies.getSelectedCode() == null || dlg.cbSpecies.getSelectedCode().trim().length() == 0 ) && aSpecies.size() == 0 ) {
					MessageDialog.showMessage(dlg, "Civet Error", "Species must be added before IDs" );
				}
				else {
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
						SpeciesLookup spp = new SpeciesLookup( sSp, true );
						String sCode = spp.getSpeciesCode();
						hSpecies.put(sCode,sSp);
					}
					AddAnimalsDialog dlg = new AddAnimalsDialog( hSpecies, idListModel );
					dlg.setModal(true);
					dlg.setVisible(true);
					setActiveSpecies( dlg.getSelectedSpecies() );
				}
			}
		});
		dlg.bSave.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSave();
			}
		});

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
	
	void setupForm( String sFileName, int iPageNo, int iPagesInFile, int iFileNo, int iFiles, boolean bPageComplete ) {
		dlg.setTitle(getViewerTitle()+sFileName);
		setPage(iPageNo);
		setPages(iPagesInFile);
		setFile(iFileNo);
		setFiles(iFiles);
		dlg.setFormEditable(!bPageComplete && dlg.iMode != CivetEditDialog.VIEW_MODE);
		updateCounterPanel();
		setupSaveButtons();	
	}

	void setupSaveButtons() {
		if( isReopened() ) {
			dlg.bAddToLast.setVisible(false);			
		}
		else if( controller.isXFADocument() || controller.isLastSavedXFA()) {
			dlg.bAddToLast.setVisible(false);			
		}
		else if( controller.getLastSavedFile() == null ){
			dlg.bAddToLast.setVisible(false);
		}
		else if( dlg.iMode != CivetEditDialog.PDF_MODE ) {
			dlg.bAddToLast.setVisible(false);
		}
		else {
			dlg.bAddToLast.setVisible(true);
		}
		if( controller.hasLastSaved() ) {
			dlg.bEditLast.setEnabled(true);
		}
		else {
			dlg.bEditLast.setEnabled(false);
		}
	}
	
	public String getViewerTitle() { return viewerTitle; }

	/**
	 * Use OpenFiles controller to set the file/page counter
	 */
	public void updateCounterPanel() {
		boolean morePagesBack = controller.morePagesBack();
		boolean morePagesForward = controller.morePagesForward();
		dlg.pCounters.setPageBackEnabled(morePagesBack);
		dlg.pCounters.setPageForwardEnabled(morePagesForward);
		dlg.pCounters.setFileBackEnabled(controller.moreFilesBack());
		dlg.pCounters.setFileForwardEnabled(controller.moreFilesForward());
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
	
	public void setInEditLast( boolean bInEditLast ) {
		this.bInEditLast = bInEditLast;
	}
	
	public boolean isInEditLast() {
		return bInEditLast;
	}
		
	/**
	 * WARNING Deal with lost data if clicked during edit.
	 * @param bLastPage
	 */
	void doEditLast(boolean bLastPage) {
		File fLast = controller.getLastSavedFile();
		File aFiles[] = new File[1];
		aFiles[0] = fLast;
		CivetEditDialogController dlgLast = new CivetEditDialogController(dlg);
		dlgLast.setInEditLast(true);
		if( bLastPage ) {
			dlgLast.bGotoLast = true;
		}
		dlgLast.openFiles(aFiles, false);
		if( bInEditLast ) {
			dlg.setVisible(false);
		}
	}
	
	void doAddToLast() {
		controller.addCurrentPage();
		if( controller.isXFADocument() ) {
			MessageDialog.showMessage(dlg, "Civet: Error", "PDF Form Files are not save as pages.");
			return;
		}
		byte[] bExtractedPageBytes = controller.extractPagesToNewPDF();
		File fLastSavedFile = controller.getLastSavedFile();
		updateSpeciesList(false);
		AddPageToCviThread addThread = new AddPageToCviThread( dlg, fLastSavedFile, bExtractedPageBytes );
		addThread.start();
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
		bInClearForm = true;
		updateSpeciesList(false);
		dlg.cbSpecies.setSelectedItem(sSpecies);
		String sSpeciesCode = dlg.cbSpecies.getSelectedCode();
		for( SpeciesRecord r : aSpecies ) {
			if( r.sSpeciesCode.equals(sSpeciesCode) ) {
				dlg.jtfNumber.setText(Integer.toString(r.iNumber) );
			}
		}
		bInClearForm = false;
	}

	/**
	 * Open previously selected files.
	 * @param selectedFiles
	 */
	public void openFiles(File selectedFiles[], boolean bViewOnly ) {
		controller.setCurrentFiles(selectedFiles, bViewOnly);
	}

	/**
	 * opens a chooser and allows user to select One or more pdf or jpg files and opens a pdf created from them.
	 */
	public void selectFiles() {
		dlg.setRotation( CivetConfig.getRotation() );
		File fDir = new File( CivetConfig.getInputDirPath() );
		JFileChooser open = new JFileChooser( fDir );
		open.setDialogTitle("Civet: Open multiple PDF and Image Files");
		open.setFileSelectionMode(JFileChooser.FILES_ONLY);
		open.setFileFilter(new FileNameExtensionFilter(
		        "Image, PDF, and Civet Files", "jpg", "png", "pdf", "jpeg", "gif", "bmp", "cvi"));
		open.setMultiSelectionEnabled(true);

		int resultOfFileSelect = JFileChooser.ERROR_OPTION;
		while(resultOfFileSelect==JFileChooser.ERROR_OPTION){

			resultOfFileSelect = open.showOpenDialog(dlg);

			if(resultOfFileSelect==JFileChooser.ERROR_OPTION) {
				logger.error("JFileChooser error");
			}

			if(resultOfFileSelect==JFileChooser.APPROVE_OPTION){
				File selectedFiles[] = open.getSelectedFiles();
				openFiles(selectedFiles, false);
			}
		}
	}
	
	public int getPageNo() { return controller.getCurrentPageNo(); }
	
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
		if( dlg.iMode != CivetEditDialog.VIEW_MODE ) {
			if( YesNoDialog.ask(dlg, "Civet: Close", "Close without saving?") ) {
				doCleanup();
				dlg.setVisible(false);
			}
		}
	}
	
	private void doCleanup() {
		LocalPremisesTableModel.saveData();
		//TODO Add logic to handle partially complete file or file list.
		File[] completeFiles = controller.getCompleteFiles();
    	moveCompleteFiles( completeFiles );
    	if( parent instanceof CivetInbox) {
    		((CivetInbox)parent).refreshTables();
    	}
	}
	
	// Probably fast enough to leave in event dispatch thread
	// but not good practice.
	// May be too slow if moving to different drive instead of just renaming to new folder.
	public void allFilesDone() {
		dlg.pdfDecoder.closePdfFile();
	    if( !isReopened() ) {
	    	File completeFiles[] = controller.getCompleteFiles();
	    	moveCompleteFiles( completeFiles );
	    }
	    if( parent instanceof CivetInbox) {
			((CivetInbox)parent).refreshTables();
		}
	    dlg.setVisible(false);
	}
	
	private void moveCompleteFiles( File[] completeFiles ) {
    	// Destination for files 
		File dirIn =  new File(CivetConfig.getInputDirPath());
		String sDirIn = dirIn.getAbsolutePath();
    	File dir = new File(CivetConfig.getOutputDirPath());
    	int iFiles = 0;
    	for( File fCurrent : completeFiles ) {
    		// Don't move opened and saved files waiting to upload.
    		if( fCurrent.getAbsolutePath().startsWith(sDirIn) ) {
    			// Move file to new directory
    			File fNew = new File(dir, fCurrent.getName());
    			if( fNew.exists() ) {
    				MessageDialog.showMessage(dlg, "Civet Error", fNew.getAbsolutePath() + " already exists in OutBox.\n" +
    							"Check that it really is a duplicate and manually delete.");
    				String sOutPath = fNew.getAbsolutePath();
    				sOutPath = FileUtils.incrementFileName(sOutPath);
    				fNew = new File( sOutPath );
    			}
    			boolean success = fCurrent.renameTo(fNew);
    			if (!success) {
    				MessageDialog.showMessage(dlg, "Civet Error", "Could not move " + fCurrent.getAbsolutePath() + " to " + fNew.getAbsolutePath() );
    			}
    			else {
    				iFiles++; 
    			}
    			String sMCviDataFile = CVIFileController.getMCviDataFilename(fCurrent);
    			File fMCviDataFile = new File( sMCviDataFile );
    			if(fMCviDataFile.exists() ) {
    				File fMovedFile = new File( dir, fMCviDataFile.getName() );
    				if( fMovedFile.exists() ) {
    					MessageDialog.showMessage(dlg, "Civet Error", fMovedFile.getAbsolutePath() + " already exists in OutBox.\n" +
    							"Check that it really is a duplicate and manually delete.");
    					String sOutPath = fNew.getAbsolutePath();
    					sOutPath = FileUtils.incrementFileName(sOutPath);
    					fNew = new File( sOutPath );
    				}
    				success = fMCviDataFile.renameTo(fMovedFile);
    				if (!success) {
    					MessageDialog.showMessage(dlg, "Civet Error", "Could not move " + fMCviDataFile.getAbsolutePath() + " to " + fMovedFile.getAbsolutePath() );
    				}
    			}
    		}
    	}
    	if( iFiles > 0 )
    		MessageDialog.showMessage(dlg, "Civet Complete", iFiles + " files ready to submit to USAHERDS.\n Original files moved to " + dir.getPath() );

	}

	public boolean isReopened() {
		String sCurrentPath = controller.getCurrentFilePath();
		String sCurrentDir = sCurrentPath.substring(0,sCurrentPath.lastIndexOf('\\')+1);
		// This is a huge kluge to detect that we are reopening a toBeFiled stdXML
	    return( sCurrentDir.equalsIgnoreCase(CivetConfig.getToFileDirPath()) );
	}

	void doPickPage( MouseEvent e ) {
		if( e.getClickCount() == 2 ) {
			doGotoPage();
		}
	}
	
	// Callbacks for Threads to set values in counter panel.
	void setPage( int iPageNo ) {
		dlg.pCounters.setPage(iPageNo);
		dlg.iPageNo = iPageNo;
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
		controller.setPage(iPage);
	}
	
	void gotoLastPage() {
		int iPages = controller.getNumberOfPages();
		controller.setPage(iPages);
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
	private void clearForm() {
		bSppEntered = false;
		bInClearForm = true;
		// Always start with a blank search box.
		dlg.jtfThisPIN.getSearchDialog().clear(); 
		// clear the form and select main map if the check box is not checked or we just did an XFA
		if( !dlg.ckSticky.isSelected() || (controller.isLastSavedXFA() && getPageNo() == 1) ){
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
		idListModel.clear();
		aErrorKeys = new ArrayList<String>();
		sErrorNotes = null;
		aSpecies = new ArrayList<SpeciesRecord>();
		dlg.lError.setVisible(false);
		dlg.bMultiSpecies = false;
		bInClearForm = false;
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
		dlg.setMode(CivetEditDialog.XML_MODE);
		OpenFileThread thread = new OpenFileThread( dlg, xStd );
		thread.start();
	}

	/**
	 * Populate dialog form from standard eCVI XML
	 * @param xStd StdeCviXml object representation of a CVI
	 */
	public void populateFromStdXml( StdeCviXml xStd ) {
		if( xStd == null ) {
			logger.error(new Exception("populateFromStdXml called with null StdeCviXml"));
			return;
		}
		else {
			try {
				bSppEntered = true;
				bInClearForm = true;
				String sOriginState = xStd.getOriginState();
				if( sOriginState != null ) {
					sOriginState = States.getState(sOriginState);
					if( sOriginState != null && sOriginState.equalsIgnoreCase(CivetConfig.getHomeState()) ) {
						dlg.setImport(false);
						dlg.rbExport.setSelected(true);
						String sOtherState = States.getState(xStd.getDestinationState());
						dlg.cbOtherState.setSelectedValue(sOtherState);
						refreshOtherCounties();
						//					jtfOtherPIN.setText("");
						// For display purposes only!  Display person name if no prem name.
						String sOtherName = xStd.getDestinationPremName();
						if( sOtherName == null || sOtherName.trim().length() == 0 )
							sOtherName = xStd.getDestinationPersonName();
						//					jtfOtherPIN.setText(xStd.getDestinationPremId());
						dlg.jtfOtherName.setText(sOtherName);
						dlg.jtfOtherAddress.setText(xStd.getDestinationStreet());
						dlg.jtfOtherCity.setText(xStd.getDestinationCity());
						String sOtherStateCode = dlg.cbOtherState.getSelectedCode();
						String sOtherCountyIn = xStd.getDestinationCounty();
						String sOtherZip = xStd.getDestinationZip();
						String sOtherHerdsCounty = getHerdsCounty( sOtherStateCode, sOtherCountyIn, sOtherZip );
						dlg.cbOtherCounty.setSelectedItem(sOtherHerdsCounty);
						dlg.jtfOtherZip.setText(xStd.getDestinationZip());
						dlg.jtfThisPIN.setText("");
						String sThisName = xStd.getOriginPremName();
						if( sThisName == null || sThisName.trim().length() == 0 )
							sThisName = xStd.getOriginPersonName();
						dlg.jtfThisPIN.setText(xStd.getOriginPremId());
						dlg.jtfThisName.setText(sThisName);
						dlg.jtfPhone.setText(xStd.getOriginPhone());
						dlg.jtfAddress.setText(xStd.getOriginStreet());
						dlg.jtfThisCity.setText(xStd.getOriginCity());
						String sThisStateCode = CivetConfig.getHomeStateAbbr();
						String sThisCountyIn = xStd.getOriginCounty();
						String sThisZip = xStd.getOriginZip();
						String sThisHerdsCounty = getHerdsCounty( sThisStateCode, sThisCountyIn, sThisZip );
						dlg.cbThisCounty.setSelectedItem(sThisHerdsCounty);
						dlg.jtfZip.setText(xStd.getOriginZip());
						String sNAN = xStd.getVetNAN();
						if( sNAN != null && sNAN.trim().length() > 0 ) {
							VetLookup vet = new VetLookup( sNAN );
							dlg.cbIssuedBy.setSelectedKey(vet.getKey());
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
								dlg.cbIssuedBy.setSelectedKey(vet.getKey());
						}
					}
					else {
						dlg.setImport(true);
						dlg.rbImport.setSelected(true);
						String sOtherState = States.getState(xStd.getOriginState());
						dlg.cbOtherState.setSelectedValue(sOtherState);
						//					jtfOtherPIN.setText("");
						// For display purposes only!  Display person name if no prem name.
						String sOtherName = xStd.getOriginPremName();
						if( sOtherName == null || sOtherName.trim().length() == 0 )
							sOtherName = xStd.getOriginPersonName();
						//					cbOtherCounty.setText(xStd.getOriginPremId());
						dlg.jtfOtherName.setText(sOtherName);
						dlg.jtfOtherAddress.setText(xStd.getOriginStreet());
						dlg.jtfOtherCity.setText(xStd.getOriginCity());
						refreshOtherCounties();
						String sOtherStateCode = dlg.cbOtherState.getSelectedCode();
						String sOtherCountyIn = xStd.getOriginCounty();
						String sOtherZip = xStd.getOriginZip();
						String sOtherHerdsCounty = getHerdsCounty( sOtherStateCode, sOtherCountyIn, sOtherZip );
						dlg.jtfOtherZip.setText(sOtherZip);
						dlg.cbOtherCounty.setSelectedItem(sOtherHerdsCounty);
						dlg.jtfThisPIN.setText("");
						String sThisName = xStd.getDestinationPremName();
						if( sThisName == null || sThisName.trim().length() == 0 )
							sThisName = xStd.getDestinationPersonName();
						dlg.jtfThisPIN.setText(xStd.getDestinationPremId());
						dlg.jtfThisName.setText(sThisName);
						dlg.jtfPhone.setText(xStd.getDestinationPhone());
						dlg.jtfAddress.setText(xStd.getDestinationStreet());
						dlg.jtfThisCity.setText(xStd.getDestinationCity());
						String sThisState = CivetConfig.getHomeStateAbbr();
						String sThisCountyIn = xStd.getDestinationCounty();
						String sThisZip = xStd.getDestinationZip();
						String sThisHerdsCounty = getHerdsCounty( sThisState, sThisCountyIn, sThisZip);
						dlg.cbThisCounty.setSelectedItem(sThisHerdsCounty);
						dlg.jtfZip.setText(xStd.getDestinationZip());
						String sVetName = xStd.getVetName();
						dlg.jtfIssuedBy.setText(sVetName);

					}
					java.util.Date dIssueDate = xStd.getIssueDate();
					dlg.jtfDateInspected.setDate(dIssueDate);
					dlg.jtfCVINo.setText(xStd.getCertificateNumber());
					// Species multiples are an issue
					loadSpeciesFromStdXml(xStd);
					// Overly simplistic.  Only works if spelling matches
					String sPurposeCode = xStd.getMovementPurpose();
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
					Component c = traversal.getComponentByName(traversal.getProperty("pPDFLoaded"));
					if( c != null)
						c.requestFocus();
				}
				bInClearForm = false;
			} catch( Exception e ) {
				logger.error("Unexpected error loading from XML standard document", e);
			}
		}
	}
	
	private String getHerdsCounty( String sStateCode, String sCounty, String sZip ) {
		String sRet = Counties.getHerdsCounty(sStateCode, sCounty);
		if( sRet == null && sZip.trim().length() >= 5 && !sZip.equals("00000") ) {
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
	private void loadSpeciesFromStdXml(StdeCviXml std) {
		aSpecies = new ArrayList<SpeciesRecord>();
		ArrayList<String> aBadSpecies = new ArrayList<String>();
		NodeList animals = std.listAnimals();
		String sSpeciesCode = null;
		String sSpeciesName = null;
		for( int i = 0; i < animals.getLength(); i++ ) {
			sSpeciesCode = std.getSpeciesCode(animals.item(i));
			String sAnimalID = std.getAnimalID(animals.item(i));
			if( sAnimalID == null || sAnimalID.trim().length() == 0 ) 
				sAnimalID = "Not provided";  // This will flag as individual animal record in XML
			boolean bSet = false;
			SpeciesLookup sppLookup = null;
			if( aBadSpecies == null || !aBadSpecies.contains(sSpeciesCode) )
				sppLookup = new SpeciesLookup( sSpeciesCode );
			if( sppLookup == null || sppLookup.getSpeciesName() == null ) {
				aBadSpecies.add(sSpeciesCode);
				sSpeciesCode = "OTH";
				sSpeciesName = "Other";
				//continue;  // Without a valid code, we cannot create a valid animal.
				// TODO: Replace giving up with a dialog to select species?
			}
			else {
				sSpeciesName = sppLookup.getSpeciesName();
			}
			if( sSpeciesCode != null && sAnimalID != null && sAnimalID.trim().length() > 0 ) {
				idListModel.addRow(sSpeciesCode, sSpeciesName, sAnimalID);
			}
			if( aSpecies.size() > 0 ) {
				for( SpeciesRecord r : aSpecies ) {
					if( r.sSpeciesCode.equals(sSpeciesCode) ) {
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
		NodeList groups = std.listGroups();
		for( int i = 0; i < groups.getLength(); i++ ) {
			sSpeciesCode = std.getSpeciesCode(groups.item(i));
			int iQuantity = std.getQuantity(groups.item(i));
			boolean bSet = false;
			SpeciesLookup sppLookup = null;
			if( aBadSpecies == null || !aBadSpecies.contains(sSpeciesCode) )
				sppLookup = new SpeciesLookup( sSpeciesCode );
			if( sppLookup == null || sppLookup.getSpeciesName() == null ) {
				aBadSpecies.add(sSpeciesCode);
				sSpeciesCode = "OTH";
				sSpeciesName = "Other";
				//continue;  // Without a valid code, we cannot create a valid animal.
				// TODO: Replace giving up with a dialog to select species?
			}
			else {
				sSpeciesName = sppLookup.getSpeciesName();
			}
			if( aSpecies.size() > 0 ) {
				for( SpeciesRecord r : aSpecies ) {
					if( r.sSpeciesCode.equals(sSpeciesCode) ) {
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
	
	
	/**
	 * Convert CO/KS xml supplied with mCVI PDF to standard and extract data from there.
	 * "Virtual" parameter PDF File loaded in pdfDecoder.
	 */
	void populateFromMCvi(String sDataFile) {
		File fDataFile = new File( sDataFile );
		try {
			String sDataXml = FileUtils.readTextFile(fDataFile);
			// Check to see if this is a version 2 schema file
			int iV2Loc = sDataXml.indexOf("http://www.usaha.org/xmlns/ecvi2");
			if( iV2Loc > 0 && iV2Loc < 200 ) {
				StdeCviXml stde = new StdeCviXml(sDataXml, 2);
				populateFromStdXml(stde);
//				controller.setStdXml(stde);
			}
			else {
				CoKsXML coks = new CoKsXML( sDataXml );
				populateFromCoKs(coks);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
	}
	/**
	 * Convert CO/KS xml to standard and extract data from there.
	 * "Virtual" parameter PDF File loaded in pdfDecoder.
	 */
	void populateFromPDF() {
		// This is always true.  Waste of cycles.
		if( controller.isXFADocument() ) {
			byte[] pdfBytes = controller.getCurrentPdfBytes();
			Node xmlNode = PDFUtils.getXFADataNode(pdfBytes);
			if( xmlNode == null ) {
				MessageDialog.messageWait(dlg, "Civet Error:", "Could not extract CO/KS data node from XFA PDF");		
				return;
			}
			CoKsXML coks = new CoKsXML( xmlNode );
			populateFromCoKs(coks);

		}
	}

	public void populateFromCoKs(CoKsXML coks) {
		bInClearForm = true;
		StdeCviXml std = coks.getStdeCviXml();
		if( std == null ) {
			MessageDialog.showMessage(dlg, "Civet Error:", "Could not convert PDF content to USAHA Standard XML using XSLT\n" +
					CivetConfig.getCoKsXSLTFile());
		}
		else {
			String sPurposeCode = std.getMovementPurpose();
			if( sPurposeCode != null && sPurposeCode.equalsIgnoreCase("other") ) {
				String sMsg = "Importing Purpose OTHER. \nUpdate in database later if possible.\nPurpose in PDF: ";
				String sPurpose =  coks.getPurposeCode();
					sMsg = sMsg + sPurpose;
				sMsg = sMsg.substring(0,sMsg.length());
				MessageDialog.showMessage(null, "Civet Warning: Other Purpose", sMsg );					
			}
			String sSppCodes = std.getSpeciesCodes();
			if( sSppCodes != null && sSppCodes.contains("OTH") ) {
				String sMsg = "Importing Species OTHER. \nUpdate in database later if possible.\nSpecies Codes in PDF: ";
				for( String sSpp : coks.listSpeciesCodes() )
					sMsg = sMsg + sSpp + ", ";
				sMsg = sMsg.substring(0,sMsg.length()-2);
				MessageDialog.showMessage(null, "Civet Warning: Other Species", sMsg );
			}
			populateFromStdXml( std );
			if( dlg.jtfDateReceived.getDate() == null && CivetConfig.isDefaultReceivedDate() ) {
				dlg.jtfDateReceived.setDate(new java.util.Date());
			}
		}
		bInClearForm = false;
	}
	

	
	private void doSave() {
		String sCVINo = dlg.jtfCVINo.getText();
		if( sCVINo.equalsIgnoreCase(sPrevCVINo) ) {
			MessageDialog.showMessage(dlg, "Civet Error", "Certificate number " + sCVINo + " hasn't changed since last save");
			dlg.jtfCVINo.requestFocus();
			return;
		}
		if( CertificateNbrLookup.certficateNbrExists(dlg.jtfCVINo.getText()) ) {
			MessageDialog.showMessage(dlg, "Civet Error", "Certificate number " + sCVINo + " already exists");
			dlg.jtfCVINo.requestFocus();
			return;
		}
		// If save() finds errors be sure form is editable so they can be corrected.
		if( !save() )
			dlg.setFormEditable( true );
	}

	/** 
	 * Gather values from dialog controls and dispatch to appropriate insert or update thread.
	 */
	boolean save() {
		controller.addCurrentPage();
		// Collect up all values needed
		dlg.setImport(dlg.rbImport.isSelected());
		boolean bInbound = dlg.rbImport.isSelected();
		boolean bXFA = false;
		boolean bAgView = false;
		String sOtherState = dlg.cbOtherState.getSelectedValue();
		String sOtherStateCode = States.getStateCode(sOtherState);
		String sOtherName = dlg.jtfOtherName.getText();
		String sOtherAddress = dlg.jtfOtherAddress.getText();
		String sOtherCity = dlg.jtfOtherCity.getText();
		String sOtherCounty = (String)dlg.cbOtherCounty.getSelectedItem();
		String sOtherZipcode = dlg.jtfOtherZip.getText();
		String sOtherPIN = null; //dlg.jtfOtherPIN.getText();
		// Only save actual PINs for other state and only save PINs or HERDS State PremIds (they aren't really LIDS!)
		// if not 
//		if( sOtherPIN != null && sOtherPIN.trim().length() != 7 && CivetConfig.hasBrokenLIDs() )
//			sOtherPIN = null;
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
		String sCVINo = dlg.jtfCVINo.getText();
		updateSpeciesList(false);
		StdeCviXml stdXml = controller.getStdXml();
		if( controller.isXFADocument() ) {
			byte[] pdfBytes = controller.getCurrentPdfBytes();
			Node xmlNode = PDFUtils.getXFADataNode(pdfBytes);
			CoKsXML coks = new CoKsXML( xmlNode );
			stdXml = coks.getStdeCviXml();
			bXFA = true;  // this will prevent overwriting some values
				// Should really disable those controls.  But some want to override!
		}
		if( controller.isAgViewDocument() ) {
			bAgView = true;
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
			MessageDialog.showMessage(dlg, "Civet Error", "One or more required fields:" + sFields + " are empty");
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
			// Bytes and Name came from stdXML attachment
			if( sAttachmentFileName != null ) {
				bAttachmentBytes = stdXml.getOriginalCVI();
			}
			// File without name set is an XFA XML from which we extracted data for stdXML
			else { 
				fAttachmentFile = controller.getCurrentFile();
			}
		}
		else {
			// Bytes without file or name is page(s) from multipage
			if( controller.isPageable() ) {
				bAttachmentBytes = controller.extractPagesToNewPDF();
				// leave filename to save thread
			}
			// File without name or bytes is single CVI PDF or image just send PDF bytes 
			else {
				bAttachmentBytes = controller.getCurrentPdfBytes();
			}
		}
		String sOpenedAsFileName = controller.getCurrentFileName();
		SaveCVIThread thread = new SaveCVIThread(dlg, stdXml, sOpenedAsFileName, bAttachmentBytes,
				sAttachmentFileName, fAttachmentFile, bInbound, bXFA, bAgView, sOtherStateCode,
				sOtherName, sOtherAddress, sOtherCity, sOtherCounty, sOtherZipcode, sOtherPIN,
				sThisPremisesId, sThisName, sPhone,
				sStreetAddress, sCity, sThisCounty, sZipcode,
				dDateIssued, dDateReceived, iIssuedByKey, sIssuedByName, sCVINo,
				sMovementPurpose,
				aSpecies,
				mSpeciesChanges,
				aErrorKeys, sErrorNotes,
				idListModel.getRows()	);
		// Messy way of checking for rapid fire duplicate entry
		sPrevCVINo = sCVINo;
		thread.start();
		return true;
	}
	
	private void deleteFile( String sFileName ) {
		File fToFile = new File( CivetConfig.getToFileDirPath() + sFileName );
		File fToEmailOut = new File( CivetConfig.getEmailOutDirPath() + sFileName );
		File fToEmailErr = new File( CivetConfig.getEmailErrorsDirPath() + sFileName );
		if( fToFile.exists() )
			fToFile.delete();
		if( fToEmailOut.exists() )
			fToEmailOut.delete();
		if( fToEmailErr.exists() )
			fToEmailErr.delete();
	}
		
	public void saveComplete() {
		controller.setCurrentPagesComplete();
		controller.clearCurrentPages();
		clearForm();
		if( dlg.iMode == CivetEditDialog.XML_MODE ) {
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
		if( controller.isPageable() & !controller.isXFADocument() ) {
			opener.openPageContentInAcrobat(controller.getCurrentFilePath(), controller.getCurrentPageNo());
		}
		// Otherwise use the content converted to PDF earlier
		else {
			opener.openPDFContentInAcrobat(controller.getCurrentPdfBytes());
		}
	}
	

	void pdfView( byte pdfBytes[] ) {
		PDFOpener opener = new PDFOpener(dlg);
		opener.openPDFContentInAcrobat(pdfBytes);
	}

	
	protected void pdfViewFile() {
		PDFOpener opener = new PDFOpener(dlg);
		opener.openPDFContentInAcrobat(controller.getCurrentPdfBytes());
	}

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
				if( sr.sSpeciesCode.equals(sSpeciesCode) ) {
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
		sPreviousSpecies = dlg.cbSpecies.getSelectedValue();
	}
	
	/** This pair of handlers is a complicated way to detect if the selected species
    has been changed from a previously recorded value.  Most of the logic is
    simply NOT triggering a change for harmless things like clearing the form.
    
    If the change is confirmed, perform the change.
	*/	
	private void doCheckSpeciesChange2() {
		{
			bInClearForm = true;
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
			bInClearForm = false;
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
		String sPreviousCode = (new SpeciesLookup(sPreviousSpecies, true)).getSpeciesCode();
		String sNewCode = (new SpeciesLookup(sNewSpecies, true)).getSpeciesCode();
		System.out.println( "Old: " + sPreviousCode + " to " + sNewCode );
		mSpeciesChanges.put(sPreviousCode, sNewCode);
		for( SpeciesRecord sr : aSpecies ) {
			if(sPreviousCode.equals(sr.sSpeciesCode))
				sr.sSpeciesCode = sNewCode;
		}
		for( AnimalIDRecord ar : idListModel.getRows() ) {
			if(sPreviousCode.equals(ar.sSpeciesCode))
				ar.sSpeciesCode = sNewCode;
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
		dlgErrorDialog.setVisible(true);
		if( dlgErrorDialog.isExitOK() ) {
			sErrorNotes = dlgErrorDialog.getNotes();
			logger.info( "Error note: " + sErrorNotes);
//			for( String sError : aErrorKeys ) {
//				logger.info("Error key " + sError + " noted");
//			}
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
