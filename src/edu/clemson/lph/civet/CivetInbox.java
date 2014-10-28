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
import java.io.File;
import java.util.List;

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
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import edu.clemson.lph.dialogs.*;
import edu.clemson.lph.mailman.MailMan;
import edu.clemson.lph.utils.Validator;
import edu.clemson.lph.civet.files.DateCellComparator;
import edu.clemson.lph.civet.files.EmailFilesTableModel;
import edu.clemson.lph.civet.files.DateCellRenderer;
import edu.clemson.lph.civet.files.FilesTableModel;
import edu.clemson.lph.civet.files.SourceFilesTableModel;
import edu.clemson.lph.civet.files.StdXMLFilesTableModel;
import edu.clemson.lph.civet.lookup.LookupFilesGenerator;

@SuppressWarnings("serial")
public class CivetInbox extends JFrame {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
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
	private SourceFilesTableModel sourceModel = null;
	private StdXMLFilesTableModel toFileModel = null;
	private EmailFilesTableModel toEmailOutModel = null;
	private EmailFilesTableModel toEmailErrorsModel = null;
	private FilesTableModel currentModel = null;
	private final JScrollPane scrollPane = new JScrollPane();
	private String sTitleBase = "Civet: Certificate of Veterinary Inspection Document Management System for USAHERDS -- ";

	JPanel jpCount = new JPanel();

	JToolBar pButtons = new JToolBar();
	JButton bOpen = new JButton();
	JButton bOpenAll = new JButton();
	JMenu menuFile = new JMenu();
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
	JMenu menuEdit = new JMenu();
	JMenuItem menuItemEditUnsend = new JMenuItem();
	private JMenu mnSend;
	private JMenuItem menuItemSubmitSelectedCVIs;
	private JMenuItem menuItemSubmitAllCVIs;
	private JMenuItem menuItemSendOutboundCVIs;
	private JMenuItem menuItemSendInboundErrors;
	JMenu menuAddOns = new JMenu();


	// for design time only remove before distribution
	public CivetInbox() {
		try {
			initGui();
			if( !LookupFilesGenerator.isNewThisSession() ) {
				MessageDialog.showMessage(this, "Civet: Error", "Lookup files not initialized.  Running off-line.\n");
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
		initModels();
		menuFile.setText("File");
		bOpen.setIcon(new ImageIcon(getClass().getResource("res/open.png")));
		bOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doOpenFiles();
			}
		});
		bOpen.setToolTipText("Open selected files or browse for files");
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
		menuView.setEnabled(!CivetConfig.isStandAlone());
		
		menuItemViewNew.setText("To Be Entered");
		menuItemViewNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				viewNew();
			}
		});
		menuView.add(menuItemViewNew);
		
		menuItemViewToBeFiled.setText("Entered CVIs Ready to Upload to HERDS");
		menuItemViewToBeFiled.setEnabled(!CivetConfig.isStandAlone());
		menuItemViewToBeFiled.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				viewToBeFiled();
			}
		});
		menuView.add(menuItemViewToBeFiled);

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

		menuItemViewRefresh.setText("Refresh");
		menuItemViewRefresh.setEnabled(!CivetConfig.isStandAlone());
		menuItemViewRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				currentModel.refresh();
			}
		});
		menuView.add(menuItemViewRefresh);

		this.setJMenuBar(menuBar1);
		statusPanel.setLayout(new FlowLayout());
		mainPanel.setLayout(borderLayout2);
		if( !CivetConfig.isStandAlone() ) {
			sourceModel = new SourceFilesTableModel( new File( CivetConfig.getInputDirPath() ) );
			tblInBox.setModel(sourceModel);
			tblInBox.setRowSorter( sourceModel.getSorter() );
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
		}
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
				System.exit(0);
			}
		});
		menuFile.add(menuItemFileExit);

		menuEdit.setText("Edit");
		menuEdit.setEnabled(!CivetConfig.isStandAlone());
		menuBar1.add(menuEdit);


		mnSend = new JMenu("Send");
		mnSend.setEnabled(!CivetConfig.isStandAlone());
		menuBar1.add(mnSend);

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

		menuItemSendOutboundCVIs = new JMenuItem("Send Outbound CVI PDFs Email");
		menuItemSendOutboundCVIs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sendOutboundCVIs();
			}
		});
		mnSend.add(menuItemSendOutboundCVIs);


		menuItemSendInboundErrors = new JMenuItem("Send Inbound Errors Letter PDF");
		menuItemSendInboundErrors.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sendInboundErrors();
			}
		});
		mnSend.add(menuItemSendInboundErrors);
		if( CivetConfig.isStandAlone() ) {
			setVisible(true);
		}
		
		menuAddOns = new JMenu( "Add Ons");
		// Once we set it up right, the package edu.clemson.lph.civet.addons will
		// be in a separate Jar file.  It will be empty by default and populated with 
		// Clemson DB stuff for us.
		AddOnLoader.populateMenu(this, menuAddOns);
		menuBar1.add(menuAddOns);
		viewNew();
		setVisible( true );

	}
	
	void refreshTables() {
		sourceModel.refresh();
		toFileModel.refresh();
		toEmailOutModel.refresh();
		toEmailErrorsModel.refresh();
		setMenuItems();
	}
	
	private void setMenuItems() {
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
		
		// Set items that only appear when viewing specific table
		if( currentModel == toFileModel && toFileModel.getRowCount() > 0 ) {
			menuItemSubmitSelectedCVIs.setEnabled(true);
			menuItemSubmitAllCVIs.setEnabled(true);
		}
		else {
			menuItemSubmitSelectedCVIs.setEnabled(false);
			menuItemSubmitAllCVIs.setEnabled(false);
		}
		if( currentModel == toEmailOutModel && toEmailOutModel.getRowCount() > 0 ) {
			menuItemSendOutboundCVIs.setEnabled(true);
		}
		else {
			menuItemSendOutboundCVIs.setEnabled(false);			
		}
		if( currentModel == toEmailErrorsModel && toEmailErrorsModel.getRowCount() > 0 ) {
			menuItemSendInboundErrors.setEnabled(true);
		}
		else {
			menuItemSendInboundErrors.setEnabled(false);			
		}
	}

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
		List<File> aStdXmlFiles = toFileModel.getAllFiles();
		if( validateCVIs( aStdXmlFiles ) ) {
			SubmitCVIsThread t = new SubmitCVIsThread( this, aStdXmlFiles );
			t.start();
		}
	}
	
	private void doSubmitSelected() {
		List<File> aStdXmlFiles = toFileModel.getSelectedFiles(tblInBox);
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
						bRet = false;
						break;
					}
				}
			}
		}
		return bRet;
	}

	private void initEmail() {
		if(MailMan.getDefaultUserID() == null || MailMan.getDefaultPassword() == null ) {
			TwoLineQuestionDialog ask = new TwoLineQuestionDialog( this, "Civet Email Login:",
					"Email UserID:", "Email Password:", true);
			ask.setPassword(true);
			ask.setVisible(true);
			if( ask.isExitOK() ) {
				MailMan.setDefaultUserID(ask.getAnswerOne());
				MailMan.setDefaultPassword(ask.getAnswerTwo());
				MailMan.setDefaultHost(CivetConfig.getSmtpHost());
				MailMan.setDefaultPort(CivetConfig.getSmtpPortInt());
				String sIsTLS = CivetConfig.getSmtpIsTLS();
				if( "yes".equalsIgnoreCase(sIsTLS) || "true".equalsIgnoreCase(sIsTLS) )
					MailMan.setTLS(true);
				else
					MailMan.setTLS(false);
				MailMan.setDefaultFrom(ask.getAnswerOne() + CivetConfig.getSmtpDomain() );
			}
			else {
				return;
			}
		}
	}

	void sendOutboundCVIs() {
		initEmail();
		ProgressDialog prog = new ProgressDialog(this, "Civet", "Emailing Outbound CVIs");
		prog.setAuto(true);
		prog.setVisible(true);
		SendOutboundCVIEmailThread tThread = new SendOutboundCVIEmailThread(this, prog);
		tThread.start();
	}

	void sendInboundErrors() {
		initEmail();
		ProgressDialog prog = new ProgressDialog(this, "Civet", "Emailing Inbound Errors");
		prog.setAuto(true);
		prog.setVisible(true);
		SendInboundErrorsEmailThread tThread = new SendInboundErrorsEmailThread(this, prog);
		tThread.start();
	}
	
	private void initModels() {
		if( toEmailOutModel == null )
			toEmailOutModel = new EmailFilesTableModel( new File( CivetConfig.getEmailOutDirPath() ) );
		else
			toEmailOutModel.refresh();
		if( toEmailErrorsModel == null )
			toEmailErrorsModel = new EmailFilesTableModel( new File( CivetConfig.getEmailErrorsDirPath() ) );
		else
			toEmailErrorsModel.refresh();
		if( toFileModel == null )
			toFileModel = new StdXMLFilesTableModel( new File( CivetConfig.getToFileDirPath() ) );
		else
			toFileModel.refresh();
		if( toFileModel == null )
			toFileModel = new StdXMLFilesTableModel( new File( CivetConfig.getToFileDirPath() ) );
		else
			toFileModel.refresh();
		if( sourceModel == null )
			sourceModel = new SourceFilesTableModel( new File( CivetConfig.getOutputDirPath() ) );
		else
			sourceModel.refresh();		
	}

	private void viewUnsentOutbound() {
		this.setTitle(sTitleBase + "Outbound CVIs to Email");
		currentModel = toEmailOutModel;
		tblInBox.setModel(toEmailOutModel);
		tblInBox.setRowSorter( toEmailOutModel.getSorter() );
		refreshTables();
	}

	private void viewUnsentInboundErrors() {
		this.setTitle(sTitleBase + "Inbound Error CVIs to Email");
		currentModel = toEmailErrorsModel;
		tblInBox.setModel(toEmailErrorsModel);
		tblInBox.setRowSorter( toEmailErrorsModel.getSorter() );
		refreshTables();
	}

	private void viewToBeFiled() {
		this.setTitle(sTitleBase + "CVI Data Ready to Submit to USAHERDS");
		currentModel = toFileModel;
		refreshTables();
		tblInBox.setModel(toFileModel);
		tblInBox.setRowSorter( toFileModel.getSorter() );
	}

	private void viewNew() {
		this.setTitle(sTitleBase + "Source Files Ready to Enter");
		currentModel = sourceModel;
		tblInBox.setModel(sourceModel);
		tblInBox.setRowSorter( sourceModel.getSorter() );
		refreshTables();
	}
	

} // End class Civet Inbox
