package edu.clemson.lph.dialogs;
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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.clemson.lph.logging.Logger;



import edu.clemson.lph.controls.DBTable;
import edu.clemson.lph.db.DatabaseConnectionFactory;
import edu.clemson.lph.db.ThreadListener;


@SuppressWarnings("serial")
class ListPickerDialog extends JDialog {
      private static Logger logger = Logger.getLogger();
	private int iKey = -1;
	private DatabaseConnectionFactory factory = null;
	private boolean bHideCode = true;
	private boolean bLink = false;
	
	private JPanel jpMain = new JPanel();
	private BorderLayout borderLayout1 = new BorderLayout();
	private JPanel jpClient = new JPanel();
	private JPanel jpButtons = new JPanel();
	private JButton jbNone = new JButton();
	private JButton jbUse = new JButton();
	private BorderLayout borderLayout2 = new BorderLayout();
	private DBTable tblSearch = new DBTable();
	private JScrollPane scrollPane = new JScrollPane();
	private ProgressDialog prog;
	private Runnable runOnSelect;
	
	private ListPickerDialog(Window parent, String title, boolean modal, DatabaseConnectionFactory factory, String sQuery) {
		super(parent);
		setModal(modal);
		setTitle(title);
		prog = new ProgressDialog( parent, "Civet", "Populating Pick List");
		prog.setAuto(true);
		prog.setVisible(true);
		this.factory = factory;
		try {
			initializeDisplay();
			pack();
			if( sQuery != null && sQuery.trim().length() > 0 )
				tblSearch.setQuery(sQuery);
		}
		catch(Exception ex) {
			logger.error(ex.getMessage() + "\nError in UNIDENTIFIED");
		}
	}

	public ListPickerDialog() {
		this((Window)null, "", false, null, "");
	}

	public void setHideCode( boolean bHideCode ) { this.bHideCode = bHideCode; }

	public int getSelectedKey() { return iKey; }
	public void setSelectedKey( int iKey ) { this.iKey = iKey; }

	public void setVisible( boolean bShow ) {
		if( bShow ) {
			if (bHideCode) {
				tblSearch.hideFirstColumn();
			}
			if (iKey >= 0)
				tblSearch.selectByKey(iKey);
			tblSearch.refresh();
			DialogHelper.center( this );
		}
		else
			super.setVisible(bShow);
	}

	public boolean isLinkSelected() { return bLink; }

	public void setQuery( String sQuery ) throws Exception {
		tblSearch.setQuery(sQuery);
	}
	
	public void setRunOnSelect( Runnable runnable ) {
		runOnSelect = runnable;
	}

	private void initializeDisplay() throws Exception {
		jpMain.setLayout(borderLayout1);
		jpButtons.setAlignmentX((float) 0.5);
		jbNone.setRequestFocusEnabled(true);
		jbNone.setText("No Match, Use Existing");
		jbNone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				bLink = false;
				iKey = -1;
				setVisible(false);
			}
		});
		jbUse.setRequestFocusEnabled(true);
		jbUse.setEnabled(false);
		jbUse.setText("Use This");
		jbUse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				iKey = tblSearch.getSelectedKey();
				runOnSelect.run();
				setVisible(false);
			}
		});
		jpClient.setLayout(borderLayout2);
		tblSearch.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if( tblSearch.getSelectedKey() > 0 ) {
					if( e.getClickCount() == 2 ) {
						bLink = false;
						iKey = tblSearch.getSelectedKey();
						runOnSelect.run();
						setVisible(false);
					}
					else
						jbUse.setEnabled(true);
				}
				else {
					jbUse.setEnabled(false);
				}
			}
		});
		tblSearch.setPreferredSize(new Dimension(450,300));
		tblSearch.setDatabaseConnectionFactory( factory );
		tblSearch.addThreadListener( new ThreadListener() {
			@Override
			public void onThreadComplete(Thread thread) {
				prog.setVisible(false);
				prog.dispose();
				int iRows = tblSearch.getRowCount();
				if( iRows > 0 ) {
					ListPickerDialog.super.setVisible(true);
				}
				else {
					setVisible(false);
				}
			}
		});
		getContentPane().add(jpMain);
		jpMain.add(jpClient, BorderLayout.CENTER);
		jpMain.add(jpButtons,  BorderLayout.SOUTH);
		jpButtons.add(jbNone, null);
		jpButtons.add(jbUse, null);
		jpClient.add(scrollPane,  BorderLayout.CENTER);
		scrollPane.setViewportView(tblSearch);
	}

} // End ListPremisesByPhoneDialog
