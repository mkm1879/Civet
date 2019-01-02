package edu.clemson.lph.civet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.clemson.lph.civet.emailonly.EmailOnlyDialog;
import edu.clemson.lph.civet.files.SourceFileException;
import edu.clemson.lph.civet.inbox.EmailFilesTableModel;
import edu.clemson.lph.civet.inbox.FilesTableModel;
import edu.clemson.lph.civet.inbox.SourceFilesTableModel;
import edu.clemson.lph.civet.inbox.StdXMLFilesTableModel;
import edu.clemson.lph.civet.lookup.StateVetTableEditDlg;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.prefs.ConfigDialog;
import edu.clemson.lph.civet.threads.SendInboundErrorsEmailThread;
import edu.clemson.lph.civet.threads.SendOutboundCVIEmailThread;
import edu.clemson.lph.civet.threads.SubmitCVIsThread;
import edu.clemson.lph.civet.vsps.VspsCviFile;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.ProgressDialog;
import edu.clemson.lph.dialogs.QuestionDialog;
import edu.clemson.lph.mailman.MailMan;
import edu.clemson.lph.utils.Validator;

public class CivetInboxController {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private CivetInbox inbox = null;

	public CivetInboxController(CivetInbox inbox) {
		this.inbox = inbox;
		initializeActionHandlers();
		viewNew();
		inbox.setVisible( true );


	}

	/**
	 * Testing only
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure("CivetConfig.txt");
		// Fail now so config file and required files can be fixed before work is done.
		CivetConfig.checkAllConfig();
		logger.setLevel(CivetConfig.getLogLevel());
		CivetInbox.VERSION = "Test Inbox Controller";
		logger.info("Civet running build: " + CivetInbox.VERSION);
		// TODO Auto-generated method stub
		CivetInboxController me = new CivetInboxController(new CivetInbox());
		me.selectFiles();
	}
	
	/**
	 * Called from constructor to isolate all the add****Handler verbosity
	 */
	private void initializeActionHandlers() {
		inbox.bOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doOpenFiles();
			}
		});
		inbox.menuItemEditPrefs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doEditPrefs();
			}
		});
		inbox.menuItemEditStateVets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doEditStateVets();
			}
		});
		inbox.menuItemFileOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doOpenFiles();
			}
		});
		inbox.bOpenAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doOpenAll();
			}
		});
		inbox.menuItemFileOpenAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doOpenAll();
			}
		});
		inbox.menuItemViewNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				viewNew();
			}
		});
		inbox.menuItemViewOutbound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
//				menuItemViewOutbound_actionPerformed(arg0);
			}
		});
		inbox.menuItemViewInbound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
//				menuItemViewInbound_actionPerformed(arg0);
			}
		});
		inbox.menuItemViewUnsentOutbound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				viewUnsentOutbound();
			}
		});
		inbox.menuItemViewUnsentInboundErrors.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				viewUnsentInboundErrors();
			}
		});
		inbox.menuItemViewToBeFiled.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				viewToBeFiled();
			}
		});
		inbox.menuItemViewRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				inbox.currentModel.refresh();
			}
		});
		inbox.menuItemAbout.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String sMsg = "Civet: CVI Management Utility\nVersion: " + inbox.VERSION;
				MessageDialog.showMessage(inbox, "About Civet", sMsg);
			}
		});
		inbox.menuItemSendLog.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doMailLogFile();
			}
		});
		inbox.tblInBox.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if( e.getClickCount() == 2 ) {
					doOpenFiles();
				}
			}
		});
		inbox.menuItemFileExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				inbox.setVisible(false);
				inbox.dispose();
				inbox.dispatchEvent(new WindowEvent(inbox, WindowEvent.WINDOW_CLOSING));
			}
		});
		inbox.menuItemSendOutboundCVIs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sendOutboundCVIs();
			}
		});
		inbox.menuItemSendInboundErrors.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sendInboundErrors();
			}
		});
		inbox.menuItemSubmitSelectedCVIs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doSubmitSelected();
			}
		});
		inbox.menuItemSubmitAllCVIs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doSubmitAll();
			}
		});
		inbox.menuItemVSPS.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				VspsCviFile me = new VspsCviFile();
				me.importVspsFile(inbox);
			}
		});
		inbox.menuEmailOnly.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				EmailOnlyDialog dialog = new EmailOnlyDialog(inbox);
				dialog.selectFiles();
				dialog.setVisible(true);
			}
		});

	}

	/**
	 * opens a chooser and allows user to select One or more pdf or jpg files and opens a pdf created from them.
	 */
	public void selectFiles() {
		File fDir = new File( CivetConfig.getInputDirPath() );
		JFileChooser open = new JFileChooser( fDir );
		open.setDialogTitle("Civet: Open multiple PDF and Image Files");
		open.setFileSelectionMode(JFileChooser.FILES_ONLY);
		open.setFileFilter(new FileNameExtensionFilter(
		        "Image, PDF, and Civet Files", "jpg", "png", "pdf", "jpeg", "gif", "bmp", "cvi"));
		open.setMultiSelectionEnabled(true);

		int resultOfFileSelect = JFileChooser.ERROR_OPTION;
		while(resultOfFileSelect==JFileChooser.ERROR_OPTION){

			resultOfFileSelect = open.showOpenDialog(inbox);

			if(resultOfFileSelect==JFileChooser.ERROR_OPTION) {
				logger.error("JFileChooser error");
			}

			if(resultOfFileSelect==JFileChooser.APPROVE_OPTION){
				File selectedFiles[] = open.getSelectedFiles();
				openFiles(selectedFiles, false);
			}
		}
	}
	
	/**
	 * Open previously selected files.
	 * @param selectedFiles
	 * @throws SourceFileException 
	 */
	public void openFiles(File selectedFiles[], boolean bViewOnly )  {
		try {
			ArrayList<File> files = new ArrayList<File>();
			for( int i = 0; i < selectedFiles.length; i++ )
				files.add(selectedFiles[i]);
			CivetEditDialog dlg = new CivetEditDialog( inbox, files);
			dlg.setVisible(true);
		} catch( SourceFileException e ) {
			logger.error("Failed to open files", e);
			MessageDialog.showMessage(inbox, "Civet Error:", e.getMessage());
		}
	}

	private void doOpenFiles() {
		try {
			FilesTableModel model = (FilesTableModel)inbox.tblInBox.getModel();
			boolean bView = ( model instanceof EmailFilesTableModel );
			ArrayList<File> files = model.getSelectedFiles(inbox.tblInBox);
			CivetEditDialog dlg = new CivetEditDialog(inbox, files);
			dlg.setVisible(true);
		} catch( SourceFileException e ) {
			logger.error("Failed to open files", e);
			MessageDialog.showMessage(inbox, "Civet Error:", e.getMessage());
		}
	}
	
	private void doEditPrefs() {
		ConfigDialog dlg = new ConfigDialog();
		dlg.setVisible(true);
	}
	
	private void doEditStateVets() {
		StateVetTableEditDlg dlg = new StateVetTableEditDlg();
		dlg.setVisible(true);
	}

	private void doOpenAll() {
		try {
			FilesTableModel model = (FilesTableModel)inbox.tblInBox.getModel();
			boolean bView = (model instanceof EmailFilesTableModel );
			ArrayList<File> files = model.getAllFiles();
			CivetEditDialog dlg = new CivetEditDialog(inbox, files);
			dlg.setVisible(true);
		} catch( SourceFileException e ) {
			logger.error("Failed to open files", e);
			MessageDialog.showMessage(inbox, "Civet Error:", e.getMessage());
		}
	}
	
	private void doSubmitAll() {
		// TODO Implement with new logic
		
//		if( !inbox.currentModel.getAbsolutePath().equals(CivetConfig.getToFileDirPath()) )
//			currentModel = new StdXMLFilesTableModel( new File( CivetConfig.getToFileDirPath() ) );
//		List<File> aStdXmlFiles = currentModel.getAllFiles();
//		if( validateCVIs( aStdXmlFiles ) ) {
//			SubmitCVIsThread t = new SubmitCVIsThread( this, aStdXmlFiles );
//			t.start();
//		}
	}
	
	private void doSubmitSelected() {
		if( !inbox.currentModel.getAbsolutePath().equals(CivetConfig.getToFileDirPath()) )
			inbox.currentModel = new StdXMLFilesTableModel( new File( CivetConfig.getToFileDirPath() ) );
		ArrayList<File> aStdXmlFiles = inbox.currentModel.getSelectedFiles(inbox.tblInBox);
		if( validateCVIs( aStdXmlFiles ) ) {
			SubmitCVIsThread t = new SubmitCVIsThread( inbox, aStdXmlFiles );
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
						MessageDialog.showMessage(inbox, "Civet: Error", "Output file " 
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
		ProgressDialog prog = new ProgressDialog(inbox, "Civet", "Emailing Outbound CVIs");
		prog.setAuto(true);
		prog.setVisible(true);
		SendOutboundCVIEmailThread tThread = new SendOutboundCVIEmailThread(inbox, prog);
		tThread.start();
	}

	void sendInboundErrors() {
		if( !CivetConfig.initEmail(true) )
			return;
		ProgressDialog prog = new ProgressDialog(inbox, "Civet", "Emailing Inbound Errors");
		prog.setAuto(true);
		prog.setVisible(true);
		SendInboundErrorsEmailThread tThread = new SendInboundErrorsEmailThread(inbox, prog);
		tThread.start();
	}

	private void viewUnsentOutbound() {
		inbox.setTitle(inbox.sTitleBase + "Outbound CVIs to Email");
		inbox.currentModel = new EmailFilesTableModel( new File( CivetConfig.getEmailOutDirPath() ) );
		refreshTables();
		inbox.menuItemSubmitSelectedCVIs.setEnabled(false);
		inbox.menuItemSubmitAllCVIs.setEnabled(false);
		if( inbox.currentModel.getRowCount() > 0 )
			inbox.menuItemSendOutboundCVIs.setEnabled(true);	
		else
			inbox.menuItemSendOutboundCVIs.setEnabled(false);
		inbox.menuItemSendInboundErrors.setEnabled(false);			
	}

	private void viewUnsentInboundErrors() {
		inbox.setTitle(inbox.sTitleBase + "Inbound Error CVIs to Email");
		inbox.currentModel = new EmailFilesTableModel( new File( CivetConfig.getEmailErrorsDirPath() ) );
		refreshTables();
		inbox.menuItemSubmitSelectedCVIs.setEnabled(false);
		inbox.menuItemSubmitAllCVIs.setEnabled(false);
		inbox.menuItemSendOutboundCVIs.setEnabled(false);			
		if( inbox.currentModel.getRowCount() > 0 )
			inbox.menuItemSendInboundErrors.setEnabled(true);	
		else
			inbox.menuItemSendInboundErrors.setEnabled(false);			
	}

	private void viewToBeFiled() {
		inbox.setTitle(inbox.sTitleBase + "CVI Data Ready to Submit to USAHERDS");
		inbox.currentModel = new StdXMLFilesTableModel( new File( CivetConfig.getToFileDirPath() ) );
		refreshTables();
		if( inbox.currentModel.getRowCount() > 0 && !CivetConfig.isStandAlone() ) {
			inbox.menuItemSubmitSelectedCVIs.setEnabled(true);
			inbox.menuItemSubmitAllCVIs.setEnabled(true);
		}
		else {
			inbox.menuItemSubmitSelectedCVIs.setEnabled(false);
			inbox.menuItemSubmitAllCVIs.setEnabled(false);			
		}
		inbox.menuItemSendOutboundCVIs.setEnabled(false);			
		inbox.menuItemSendInboundErrors.setEnabled(false);			
	}

	private void viewNew() {
		inbox.setTitle(inbox.sTitleBase + "Source Files Ready to Enter");
		inbox.currentModel = new SourceFilesTableModel( new File( CivetConfig.getInputDirPath() ) );
		refreshTables();
		inbox.menuItemSubmitSelectedCVIs.setEnabled(false);
		inbox.menuItemSubmitAllCVIs.setEnabled(false);
		inbox.menuItemSendOutboundCVIs.setEnabled(false);			
		inbox.menuItemSendInboundErrors.setEnabled(false);			
	}
	
	private void doMailLogFile() {
		File fLog = new File( "Civet.log");
		if( fLog.exists() ) {
			String sMsg = QuestionDialog.ask(inbox, "Civet Send Log", "Describe your problem:");
			if( !CivetConfig.initEmail(true) )
				return;
			try {
				if( MailMan.sendIt("mmarti5@clemson.edu", null, "Civet Error Log", 
						sMsg, fLog.getAbsolutePath()) ) {
					MessageDialog.showMessage(inbox, "Civet Log Sent:", "You can delete the Civet.log file after you exit if you like.");
				}
				else {
					MessageDialog.showMessage(inbox, "Civet Error:", "Failed to send Civet.log.  Please email manually.");
				}
			} catch (AuthenticationFailedException e) {
				MessageDialog.showMessage(inbox, "Civet Error:", "Email login failed");
				MailMan.setDefaultUserID( null );
				MailMan.setDefaultPassword( null );
				logger.error(e);
			} catch (MessagingException e) {
				MessageDialog.showMessage(inbox, "Civet Error:", "Failed to send Civet.log.  Please email manually.");
				logger.error(e);
			}
		}
		else {
			MessageDialog.showMessage(inbox, "Civet Error:", "Cannot find Civet.log.  Please email manually.");
		}
	}

	public void refreshTables() {
		if( inbox.currentModel != null ) {
			inbox.currentModel.refresh();
			inbox.tblInBox.setModel(inbox.currentModel);
			inbox.tblInBox.setRowSorter( inbox.currentModel.getSorter() );
			// Set items that apply in any view
			if( inbox.currentModel.getRowCount() == 0 ) {
				inbox.bOpen.setEnabled(false);
				inbox.menuItemFileOpen.setEnabled(false);
				inbox.bOpenAll.setEnabled(false);
				inbox.menuItemFileOpenAll.setEnabled(false);
			}
			else {
				inbox.bOpen.setEnabled(true);
				inbox.menuItemFileOpen.setEnabled(true);
				inbox.bOpenAll.setEnabled(true);
				inbox.menuItemFileOpenAll.setEnabled(true);
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
	

}
