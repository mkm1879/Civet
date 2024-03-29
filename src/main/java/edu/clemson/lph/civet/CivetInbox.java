package edu.clemson.lph.civet;
/*
Copyright 2014-2018 Michael K Martin

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

import edu.clemson.lph.logging.Logger;

import edu.clemson.lph.dialogs.*;
import edu.clemson.lph.civet.inbox.DateCellRenderer;
import edu.clemson.lph.civet.inbox.FilesTableModel;
import edu.clemson.lph.civet.lookup.LookupFilesGenerator;
import edu.clemson.lph.civet.prefs.CivetConfig;

@SuppressWarnings("serial")
public class CivetInbox extends JFrame {
//	public static String VERSION = "Not Set";
      private static Logger logger = Logger.getLogger();
	static {
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
	JTable tblInBox = new JTable();
	FilesTableModel currentModel = null;
	private final JScrollPane scrollPane = new JScrollPane();
	String sTitleBase = "Civet: Certificate of Veterinary Inspection Document Management System for USAHERDS -- ";

	private JPanel jpCount = new JPanel();

	private JToolBar pButtons = new JToolBar();
	JButton bOpen = new JButton();
	JButton bOpenAll = new JButton();
	private JMenu menuFile = new JMenu();
	JMenuItem menuItemEditPrefs = new JMenuItem();
	JMenuItem menuItemEditStateVets = new JMenuItem();
	JMenuItem menuItemFileOpen = new JMenuItem();
	JMenuItem menuItemFileOpenAll = new JMenuItem();
	JMenuItem menuItemFileExit = new JMenuItem();
	private JMenu menuView = new JMenu();
	JMenuItem menuItemViewNew = new JMenuItem();
	JMenuItem menuItemViewOutbound = new JMenuItem();
	JMenuItem menuItemViewInbound = new JMenuItem();
	JMenuItem menuItemViewUnsentOutbound = new JMenuItem();
	JMenuItem menuItemViewUnsentInboundErrors = new JMenuItem();
	JMenuItem menuItemViewToBeFiled = new JMenuItem();
	JMenuItem menuItemViewRefresh = new JMenuItem();
	private JMenu mnSend;
	JMenuItem menuItemSubmitSelectedCVIs;
	JMenuItem menuItemSubmitAllCVIs;
	JMenuItem menuItemSendOutboundCVIs;
	JMenuItem menuItemSendInboundErrors;
	private JMenu menuExperimental = new JMenu();
	JMenuItem menuItemVSPS = new JMenuItem();
	JMenuItem menuItemVSPSEMS = new JMenuItem();
	JMenuItem menuItemBulk9Dash3 = new JMenuItem();
	JMenuItem menuItem9Dash3Dialog = new JMenuItem();
	private JMenu menuHelp = new JMenu();
	JMenuItem menuItemAbout = new JMenuItem();
	JMenuItem menuItemSendLog;
	JMenuItem menuEmailOnly;
	private CivetInboxController inboxController;


	// for design time only remove before distribution
	public CivetInbox() {
		try {
		    logger.setLevel(CivetConfig.getLogLevel());
			initGui();
			inboxController = new CivetInboxController( this );
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
		logger.info("Starting CivetInBox");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		appIcon = new ImageIcon(getClass().getResource("res/civet32.png"));
		this.setIconImage(appIcon.getImage());
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(borderLayout1);
		this.setSize(new Dimension(813, 404));
		this.setExtendedState(Frame.MAXIMIZED_BOTH);
		menuFile.setText("File");
		bOpen.setIcon(new ImageIcon(getClass().getResource("res/open.png")));
		bOpen.setToolTipText("Open selected files or browse for files");
		
		menuItemEditPrefs.setText("Edit Preferences in CivetConfig.txt");
		menuItemEditPrefs.setToolTipText("Edit Preferences configuration in dialog");
		menuFile.add(menuItemEditPrefs);
		
		menuItemEditStateVets.setText("Edit State Vet Table");
		menuItemEditStateVets.setToolTipText("Edit State Vet Table in dialog");
		menuFile.add(menuItemEditStateVets);
		
		menuItemFileOpen.setText("Open File(s)");
		menuItemFileOpen.setToolTipText("Open selected files or browse for files");
		menuFile.add(menuItemFileOpen);
		
		bOpenAll.setIcon(new ImageIcon(getClass().getResource("res/openmulti.png")));
		bOpenAll.setToolTipText("Open all files in Civet Inbox");
		menuItemFileOpenAll.setText("Open All Files");
		menuItemFileOpenAll.setToolTipText("Open all files in Civet Inbox");
		menuFile.add(menuItemFileOpenAll);
		
		statusBar.setText(" ");
		jpCount.setLayout(new FlowLayout());
		menuView.setText("View");
		menuView.setEnabled(true);
		
		menuItemViewNew.setText("To Be Entered");
		menuView.add(menuItemViewNew);
		
		String sSmtp = CivetConfig.getSmtpHost();
		if( sSmtp != null && !"NONE".equalsIgnoreCase(sSmtp) ) {
			menuItemViewOutbound.setText("Outbound");
			//		menuView.add(menuItemViewOutbound);
			menuItemViewInbound.setText("Inbound");
			//		menuView.add(menuItemViewInbound);

			menuItemViewUnsentOutbound.setText("Unsent Outbound CVIs");
			menuView.add(menuItemViewUnsentOutbound);

			menuItemViewUnsentInboundErrors.setText("Unsent Inbound Error CVIs");
			menuView.add(menuItemViewUnsentInboundErrors);
		}

		menuItemViewToBeFiled.setText("Entered CVIs Ready to Upload to HERDS");
		menuItemViewToBeFiled.setEnabled(true);
		menuView.add(menuItemViewToBeFiled);

		menuItemViewRefresh.setText("Refresh");
		menuItemViewRefresh.setEnabled(true);
		menuView.add(menuItemViewRefresh);
		
		menuHelp.setText("Help");
		menuItemAbout.setText("About Civet");
		menuHelp.add(menuItemAbout);
		
		if( sSmtp != null && !"NONE".equalsIgnoreCase(sSmtp) ) {
			menuItemSendLog = new JMenuItem();
			menuItemSendLog.setText("Send Civet Error Log");
			menuHelp.add(menuItemSendLog);
		}

		this.setJMenuBar(menuBar1);
		statusPanel.setLayout(new FlowLayout());
		mainPanel.setLayout(borderLayout2);
		tblInBox.setDefaultRenderer(java.util.Date.class, new DateCellRenderer() );
		tblInBox.setAutoCreateRowSorter(true);
		tblInBox.setRowSelectionAllowed(true);
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
		menuFile.add(menuItemFileExit);

		mnSend = new JMenu("Send");
		mnSend.setEnabled(true);
		menuBar1.add(mnSend);

		if( sSmtp != null && !"NONE".equalsIgnoreCase(sSmtp) ) {
			menuItemSendOutboundCVIs = new JMenuItem("Send Outbound CVIs Email");
			mnSend.add(menuItemSendOutboundCVIs);

			menuItemSendInboundErrors = new JMenuItem("Send Inbound Error Letters Email");
			mnSend.add(menuItemSendInboundErrors);
		}
		menuItemSubmitSelectedCVIs = new JMenuItem("Submit Selected CVIs to USAHERDS");
		mnSend.add(menuItemSubmitSelectedCVIs);

		menuItemSubmitAllCVIs = new JMenuItem("Submit All CVIs to USAHERDS");
		mnSend.add(menuItemSubmitAllCVIs);

		menuExperimental.setText("Experimental");
		menuItemVSPS.setText("Import VSPS eCVI CSV File");
		menuExperimental.add(menuItemVSPS);
		menuItemVSPSEMS.setText("Import VSPS eCVIs via EMS");
		menuExperimental.add(menuItemVSPSEMS);
		menuItem9Dash3Dialog.setText("Enter NPIP 9-3 Movement Forms");
		menuExperimental.add(menuItem9Dash3Dialog);
		menuItemBulk9Dash3.setText("Import NPIP 9-3 CSV File");
		menuExperimental.add(menuItemBulk9Dash3);

		menuEmailOnly = new JMenuItem("Email Files Only");
		menuExperimental.add(menuEmailOnly);
		menuBar1.add(menuExperimental);
		
		menuBar1.add(menuHelp);
	}
	
	/** 
	 * Lazy convenience method replaces "parent.getController().refreshTables()"
	 */
	public void refreshTables() {
		inboxController.refreshTables();
	}

} // End class Civet Inbox
