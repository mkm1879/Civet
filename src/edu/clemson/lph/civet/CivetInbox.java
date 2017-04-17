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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;

import edu.clemson.lph.dialogs.*;
import edu.clemson.lph.mailman.MailMan;
import edu.clemson.lph.utils.Validator;
import edu.clemson.lph.civet.files.EmailFilesTableModel;
import edu.clemson.lph.civet.files.DateCellRenderer;
import edu.clemson.lph.civet.files.FilesTableModel;
import edu.clemson.lph.civet.files.SourceFilesTableModel;
import edu.clemson.lph.civet.files.StdXMLFilesTableModel;
import edu.clemson.lph.civet.lookup.LookupFilesGenerator;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.prefs.ConfigDialog;
import edu.clemson.lph.civet.vsps.VspsCviFile;

@SuppressWarnings("serial")
public class CivetInbox extends JFrame {
	public static final String VERSION = "3.18d";
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	static {
	     logger.setLevel(CivetConfig.getLogLevel());
	}
	private JPanel contentPane;
	private JMenuBar menuBar1 = new JMenuBar();
	private ImageIcon appIcon;
	// Empty for now, this label holds the place for future messages and makes the bar the right dimensions
	private JLabel statusBar = new JLabel();
	private BorderLayout borderLayout1 = new BorderLayout();
	private JPanel mainPanel = new JPanel();
	private JPanel statusPanel = new JPanel();
	private BorderLayout borderLayout2 = new BorderLayout();
	private JTable tblInBox = new JTable();
	private FilesTableModel currentModel = null;
	private final JScrollPane scrollPane = new JScrollPane();
	private String sTitleBase = "Civet: Certificate of Veterinary Inspection Document Management System for USAHERDS -- ";

	JPanel jpCount = new JPanel();

	JToolBar pButtons = new JToolBar();
	JButton bOpen = new JButton();
	JButton bOpenAll = new JButton();
	JMenu menuFile = new JMenu();
	JMenuItem menuItemEditPrefs = new JMenuItem();
	JMenuItem menuItemFileOpen = new JMenuItem();
	JMenuItem menuItemFileOpenAll = new JMenuItem();
	JMenuItem menuItemFileExit = new JMenuItem();
	FlowLayout flowLayout1 = new FlowLayout();
	JMenu menuView = new JMenu();
	JMenuItem menuItemViewNew = new JMenuItem();
	JMenuItem menuItemViewOutbound = new JMenuItem();
	JMenuItem menuItemViewInbound = new JMenuItem();
	JMenuItem menuItemViewUnsentOutbound = new JMenuItem();
	JMenuItem menuItemViewUnsentInboundErrors = new JMenuItem();
	JMenuItem menuItemViewToBeFiled = new JMenuItem();
	JMenuItem menuItemViewRefresh = new JMenuItem();
	JMenuItem menuItemEditUnsend = new JMenuItem();
	private JMenu mnSend;
	private JMenuItem menuItemSubmitSelectedCVIs;
	private JMenuItem menuItemSubmitAllCVIs;
	private JMenuItem menuItemSendOutboundCVIs;
	private JMenuItem menuItemSendInboundErrors;
	JMenu menuExperimental = new JMenu();
	JMenu menuAddOns = new JMenu();
	private JMenuItem menuItemVSPS = new JMenuItem();
	JMenu menuHelp = new JMenu();
	JMenuItem menuItemAbout = new JMenuItem();
	private JMenuItem menuItemSendLog;


	// for design time only remove before distribution
	public CivetInbox() {
		try {
			initGui();
			if( !LookupFilesGenerator.isNewThisSession() ) {
				MessageDialog.showMessage(this, "Civet: Error", "Lookup files not initialized.  Running off-line.\n");
				CivetConfig.setStandAlone(true);
			}
		} catch( Throwable t ) {
			logger.error("Unexpected error in initGui", t);
		}
	}
	
	/**
	 * Create the frame.
	 */
	private void initGui() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		appIcon = new ImageIcon(getClass().getResource("res/civet32.png"));
		this.setIconImage(appIcon.getImage());
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(borderLayout1);
		this.setSize(new Dimension(813, 404));
		this.setExtendedState(Frame.MAXIMIZED_BOTH);
		menuFile.setText("File");
		bOpen.setIcon(new ImageIcon(getClass().getResource("res/open.png")));
		bOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doOpenFiles();
			}
		});
		bOpen.setToolTipText("Open selected files or browse for files");
		
		menuItemEditPrefs.setText("Edit Preferences in CivetConfig.txt");
		menuItemEditPrefs.setToolTipText("Open selected files or browse for files");
		menuItemEditPrefs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doEditPrefs();
			}
		});
		menuFile.add(menuItemEditPrefs);
		
		menuItemFileOpen.setText("Open File(s)");
		menuItemFileOpen.setToolTipText("Open selected files or browse for files");
		menuItemFileOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doOpenFiles();
			}
		});
		menuFile.add(menuItemFileOpen);
		
		bOpenAll.setIcon(new ImageIcon(getClass().getResource("res/openmulti.png")));
		bOpenAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doOpenAll();
			}
		});
		bOpenAll.setToolTipText("Open all files in Civet Inbox");
		menuItemFileOpenAll.setText("Open All Files");
		menuItemFileOpenAll.setToolTipText("Open all files in Civet Inbox");
		menuItemFileOpenAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doOpenAll();
			}
		});
		menuFile.add(menuItemFileOpenAll);
		
		statusBar.setText(" ");
		jpCount.setLayout(new FlowLayout());
		menuView.setText("View");
		menuView.setEnabled(true);
		
		menuItemViewNew.setText("To Be Entered");
		menuItemViewNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				viewNew();
			}
		});
		menuView.add(menuItemViewNew);
		
		menuItemViewOutbound.setText("Outbound");
		menuItemViewOutbound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
//				menuItemViewOutbound_actionPerformed(arg0);
			}
		});
//		menuView.add(menuItemViewOutbound);
		menuItemViewInbound.setText("Inbound");
		menuItemViewInbound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
//				menuItemViewInbound_actionPerformed(arg0);
			}
		});
//		menuView.add(menuItemViewInbound);
		
		menuItemViewUnsentOutbound.setText("Unsent Outbound CVIs");
		menuItemViewUnsentOutbound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				viewUnsentOutbound();
			}
		});
		menuView.add(menuItemViewUnsentOutbound);
		
		menuItemViewUnsentInboundErrors.setText("Unsent Inbound Error CVIs");
		menuItemViewUnsentInboundErrors.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				viewUnsentInboundErrors();
			}
		});
		menuView.add(menuItemViewUnsentInboundErrors);

		menuItemViewToBeFiled.setText("Entered CVIs Ready to Upload to HERDS");
		menuItemViewToBeFiled.setEnabled(true);
		menuItemViewToBeFiled.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				viewToBeFiled();
			}
		});
		menuView.add(menuItemViewToBeFiled);

		menuItemViewRefresh.setText("Refresh");
		menuItemViewRefresh.setEnabled(true);
		menuItemViewRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				currentModel.refresh();
			}
		});
		menuView.add(menuItemViewRefresh);
		
		menuHelp.setText("Help");
		menuItemAbout.setText("About Civet");
		menuItemAbout.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String sMsg = "Civet: CVI Management Utility\nVersion: " + VERSION;
				MessageDialog.showMessage(CivetInbox.this, "About Civet", sMsg);
			}
		});
		menuHelp.add(menuItemAbout);
		
		menuItemSendLog = new JMenuItem();
		menuItemSendLog.setText("Send Civet Error Log");
		menuItemSendLog.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doMailLogFile();
			}
		});
		menuHelp.add(menuItemSendLog);

		this.setJMenuBar(menuBar1);
		statusPanel.setLayout(new FlowLayout());
		mainPanel.setLayout(borderLayout2);
		tblInBox.setDefaultRenderer(java.util.Date.class, new DateCellRenderer() );
		tblInBox.setAutoCreateRowSorter(true);
		tblInBox.setRowSelectionAllowed(true);
		tblInBox.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if( e.getClickCount() == 2 ) {
					doOpenFiles();
				}
			}
		});
		contentPane.setPreferredSize(new Dimension(640, 480));
		contentPane.add(mainPanel, BorderLayout.CENTER);
		mainPanel.add(scrollPane,  BorderLayout.CENTER);
		scrollPane.setViewportView(tblInBox);
		mainPanel.add(jpCount, BorderLayout.NORTH);
		contentPane.add(statusPanel, BorderLayout.SOUTH);
		statusPanel.add(statusBar);
		contentPane.add(pButtons, BorderLayout.NORTH);
		pButtons.add(bOpen);
		pButtons.add(bOpenAll);
		menuBar1.add(menuFile);
		menuBar1.add(menuView);
		menuItemFileExit.setText("Exit");
		menuItemFileExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
				dispose();
				CivetInbox.this.dispatchEvent(new WindowEvent(CivetInbox.this, WindowEvent.WINDOW_CLOSING));
			}
		});
		menuFile.add(menuItemFileExit);

		mnSend = new JMenu("Send");
		mnSend.setEnabled(true);
		menuBar1.add(mnSend);

		menuItemSendOutboundCVIs = new JMenuItem("Send Outbound CVIs Email");
		menuItemSendOutboundCVIs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sendOutboundCVIs();
			}
		});
		mnSend.add(menuItemSendOutboundCVIs);


		menuItemSendInboundErrors = new JMenuItem("Send Inbound Error Letters Email");
		menuItemSendInboundErrors.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sendInboundErrors();
			}
		});
		mnSend.add(menuItemSendInboundErrors);
		
		menuItemSubmitSelectedCVIs = new JMenuItem("Submit Selected CVIs to USAHERDS");
		menuItemSubmitSelectedCVIs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doSubmitSelected();
			}
		});
		mnSend.add(menuItemSubmitSelectedCVIs);

		menuItemSubmitAllCVIs = new JMenuItem("Submit All CVIs to USAHERDS");
		menuItemSubmitAllCVIs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doSubmitAll();
			}
		});
		mnSend.add(menuItemSubmitAllCVIs);

		// Only include add ons in local version
		// This is a kluge until I figure out how to package add ons differently for local use
		// in a better build cycle.
		if( VERSION.toLowerCase().endsWith("local") ) {
			menuAddOns = new JMenu( "Add Ons");
			menuBar1.add(menuAddOns);
			// Once we set it up right, the package edu.clemson.lph.civet.addons will
			// be in a separate Jar file.  It will be empty by default and populated with 
			// Clemson DB stuff for us.
			AddOnLoader.populateMenu(this, menuAddOns);
		}
		
		menuExperimental.setText("Experimental");
		menuItemVSPS.setText("Import VSPS eCVI CSV File");
		menuItemVSPS.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				VspsCviFile me = new VspsCviFile();
				me.importVspsFile(CivetInbox.this);
			}
		});
		menuExperimental.add(menuItemVSPS);
		menuBar1.add(menuExperimental);
		
		menuBar1.add(menuHelp);
		viewNew();
		setVisible( true );

	}
	
	private void doMailLogFile() {
		File fLog = new File( "Civet.log");
		if( fLog.exists() ) {
			String sMsg = QuestionDialog.ask(this, "Civet Send Log", "Describe your problem:");
			if( !CivetConfig.initEmail(true) )
				return;
			try {
				if( MailMan.sendIt("mmarti5@clemson.edu", null, "Civet Error Log", 
						sMsg, fLog.getAbsolutePath()) ) {
					MessageDialog.showMessage(this, "Civet Log Sent:", "You can delete the Civet.log file after you exit if you like.");
				}
				else {
					MessageDialog.showMessage(this, "Civet Error:", "Failed to send Civet.log.  Please email manually.");
				}
			} catch (AuthenticationFailedException e) {
				MessageDialog.showMessage(this, "Civet Error:", "Email login failed");
				logger.error(e);
			} catch (MessagingException e) {
				MessageDialog.showMessage(this, "Civet Error:", "Failed to send Civet.log.  Please email manually.");
				logger.error(e);
			}
		}
		else {
			MessageDialog.showMessage(this, "Civet Error:", "Cannot find Civet.log.  Please email manually.");
		}
	}

	void refreshTables() {
		if( currentModel != null ) {
			currentModel.refresh();
			tblInBox.setModel(currentModel);
			tblInBox.setRowSorter( currentModel.getSorter() );
			// Set items that apply in any view
			if( currentModel.getRowCount() == 0 ) {
				bOpen.setEnabled(false);
				menuItemFileOpen.setEnabled(false);
				bOpenAll.setEnabled(false);
				menuItemFileOpenAll.setEnabled(false);
			}
			else {
				bOpen.setEnabled(true);
				menuItemFileOpen.setEnabled(true);
				bOpenAll.setEnabled(true);
				menuItemFileOpenAll.setEnabled(true);
			}
		}
	}

//	private void doOpenFile( String sFile ) {
//		if( sFile == null || sFile.trim().length() == 0 ) return;
//		CivetEditDialog dlg = new CivetEditDialog( CivetInbox.this );
//		File fFile = new File(sFile);
//		if( !fFile.exists() ) return;
//		String sFileName = fFile.getName();
//		String sInbox = CivetConfig.getInputDirPath();
//		String sMoveTo = sInbox + sFileName;
//		if( !(sMoveTo.equalsIgnoreCase(sFile)) ){
//			File fMoveTo = new File( sMoveTo );
//			fFile.renameTo(fMoveTo);
//			fFile = fMoveTo;
//		}
//		File selectedFiles[] = {fFile};
//		dlg.openFiles(selectedFiles, false);
//	}

	private void doOpenFiles() {
		CivetEditDialog dlg = new CivetEditDialog( CivetInbox.this );
		File selectedFiles[] = {};
		FilesTableModel model = (FilesTableModel)tblInBox.getModel();
		boolean bView = ( model instanceof EmailFilesTableModel );
		List<File> files = model.getSelectedFiles(tblInBox);
			selectedFiles = files.toArray(selectedFiles);
		if( selectedFiles != null && selectedFiles.length > 0 )
			dlg.openFiles(selectedFiles, bView);
		else
			dlg.selectFiles();
	}
	
	private void doEditPrefs() {
		ConfigDialog dlg = new ConfigDialog();
		dlg.setVisible(true);
		
	}

	private void doOpenAll() {
		CivetEditDialog dlg = new CivetEditDialog( CivetInbox.this );
		File allFiles[] = {};
		FilesTableModel model = (FilesTableModel)tblInBox.getModel();
		boolean bView = (model instanceof EmailFilesTableModel );
		List<File> files = model.getAllFiles();
			allFiles = files.toArray(allFiles);
		if( allFiles != null && allFiles.length > 0 )
			dlg.openFiles(allFiles, bView);
	}
	
	private void doSubmitAll() {
		if( !currentModel.getAbsolutePath().equals(CivetConfig.getToFileDirPath()) )
			currentModel = new StdXMLFilesTableModel( new File( CivetConfig.getToFileDirPath() ) );
		List<File> aStdXmlFiles = currentModel.getAllFiles();
		if( validateCVIs( aStdXmlFiles ) ) {
			SubmitCVIsThread t = new SubmitCVIsThread( this, aStdXmlFiles );
			t.start();
		}
	}
	
	private void doSubmitSelected() {
		if( !currentModel.getAbsolutePath().equals(CivetConfig.getToFileDirPath()) )
			currentModel = new StdXMLFilesTableModel( new File( CivetConfig.getToFileDirPath() ) );
		List<File> aStdXmlFiles = currentModel.getSelectedFiles(tblInBox);
		if( validateCVIs( aStdXmlFiles ) ) {
			SubmitCVIsThread t = new SubmitCVIsThread( this, aStdXmlFiles );
			t.start();
		}
	}
	
	private boolean validateCVIs( List<File> cviFiles ) {
		boolean bRet = true;
		String sSchemaPath = CivetConfig.getSchemaFile();
		if( sSchemaPath != null && sSchemaPath.trim().length() > 0 ) {
			Validator v = new Validator(sSchemaPath);
			if( v != null ) {
				for( File f : cviFiles ) {
					if( !v.isValidXMLFile(f.getAbsolutePath() ) ) {
						MessageDialog.showMessage(this, "Civet: Error", "Output file " 
								+ f.getAbsolutePath() + " is not valid based on schema\n"
								+ sSchemaPath + "\n" + v.getLastError() );
						logger.error("Civet: Error\nOutput file " 
								+ f.getAbsolutePath() + " is not valid based on schema\n"
								+ sSchemaPath + "\n" + v.getLastError());
						bRet = false;
						break;
					}
				}
			}
		}
		return bRet;
	}


	void sendOutboundCVIs() {
		if( !CivetConfig.initEmail(true) )
			return;
		ProgressDialog prog = new ProgressDialog(this, "Civet", "Emailing Outbound CVIs");
		prog.setAuto(true);
		prog.setVisible(true);
		SendOutboundCVIEmailThread tThread = new SendOutboundCVIEmailThread(this, prog);
		tThread.start();
	}

	void sendInboundErrors() {
		if( !CivetConfig.initEmail(true) )
			return;
		ProgressDialog prog = new ProgressDialog(this, "Civet", "Emailing Inbound Errors");
		prog.setAuto(true);
		prog.setVisible(true);
		SendInboundErrorsEmailThread tThread = new SendInboundErrorsEmailThread(this, prog);
		tThread.start();
	}

	private void viewUnsentOutbound() {
		this.setTitle(sTitleBase + "Outbound CVIs to Email");
		currentModel = new EmailFilesTableModel( new File( CivetConfig.getEmailOutDirPath() ) );
		refreshTables();
		menuItemSubmitSelectedCVIs.setEnabled(false);
		menuItemSubmitAllCVIs.setEnabled(false);
		if( currentModel.getRowCount() > 0 )
			menuItemSendOutboundCVIs.setEnabled(true);	
		else
			menuItemSendOutboundCVIs.setEnabled(false);
		menuItemSendInboundErrors.setEnabled(false);			
	}

	private void viewUnsentInboundErrors() {
		this.setTitle(sTitleBase + "Inbound Error CVIs to Email");
		currentModel = new EmailFilesTableModel( new File( CivetConfig.getEmailErrorsDirPath() ) );
		refreshTables();
		menuItemSubmitSelectedCVIs.setEnabled(false);
		menuItemSubmitAllCVIs.setEnabled(false);
		menuItemSendOutboundCVIs.setEnabled(false);			
		if( currentModel.getRowCount() > 0 )
			menuItemSendInboundErrors.setEnabled(true);	
		else
			menuItemSendInboundErrors.setEnabled(false);			
	}

	private void viewToBeFiled() {
		this.setTitle(sTitleBase + "CVI Data Ready to Submit to USAHERDS");
		currentModel = new StdXMLFilesTableModel( new File( CivetConfig.getToFileDirPath() ) );
		refreshTables();
		if( currentModel.getRowCount() > 0 && !CivetConfig.isStandAlone() ) {
			menuItemSubmitSelectedCVIs.setEnabled(true);
			menuItemSubmitAllCVIs.setEnabled(true);
		}
		else {
			menuItemSubmitSelectedCVIs.setEnabled(false);
			menuItemSubmitAllCVIs.setEnabled(false);			
		}
		menuItemSendOutboundCVIs.setEnabled(false);			
		menuItemSendInboundErrors.setEnabled(false);			
	}

	private void viewNew() {
		this.setTitle(sTitleBase + "Source Files Ready to Enter");
		currentModel = new SourceFilesTableModel( new File( CivetConfig.getInputDirPath() ) );
		refreshTables();
		menuItemSubmitSelectedCVIs.setEnabled(false);
		menuItemSubmitAllCVIs.setEnabled(false);
		menuItemSendOutboundCVIs.setEnabled(false);			
		menuItemSendInboundErrors.setEnabled(false);			
	}
	

} // End class Civet Inbox
