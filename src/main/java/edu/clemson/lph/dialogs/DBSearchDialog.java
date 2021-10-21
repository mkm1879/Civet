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

import java.awt.*;
import javax.swing.*;
import edu.clemson.lph.logging.Logger;
import edu.clemson.lph.db.*;
import java.awt.event.*;
import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.controls.*;

/**
 * <p>Title: SC Prem ID</p>
 * <p>Description: SC Premises Identification Data Hub</p>
 * <p>Copyright: Copyright (c) 2004-2012</p>
 * <p>Company: Clemson Livestock Poultry Health</p>
 * @author Michael K. Martin
 * @version 1.0
 */

@SuppressWarnings("serial")
public class DBSearchDialog extends JDialog implements SearchDialog<Integer> {
      private static Logger logger = Logger.getLogger();
	private int deltaX = 0;
	private int deltaY = 0;
	private String sSearchQuery;
	private int iKey = -1;
	private String sValue = null;
	private DatabaseConnectionFactory factory = null;
	private boolean bOK = false;
	private boolean bHideCode = false;

	private JPanel jpMain = new JPanel();
	private BorderLayout borderLayout1 = new BorderLayout();
	private JPanel jpClient = new JPanel();
	private JPanel jpButtons = new JPanel();
	private JButton jbOK = new JButton();
	private JButton jbCancel = new JButton();
	private BorderLayout borderLayout2 = new BorderLayout();
	private DBTable tblSearch = new DBTable();
	private final JButton btnNone = new JButton("None");
	private final JScrollPane scrollPane = new JScrollPane();
	private final JPanel panel = new JPanel();
	private final JLabel lblSearch = new JLabel("Search For:");
	private final JTextField jtfSearchFor = new JTextField();
	private final JButton btnSearch = new JButton("Search");
	private DBTableSource searchSource;

	public DBSearchDialog(Window parent, String title, boolean modal, DatabaseConnectionFactory factory, String sSearchQuery) {
		super(parent, title);
		this.sSearchQuery = sSearchQuery;
		this.factory = factory;
		this.setModal(modal);
		try {
			jbInit();
			pack();
		}
		catch(Exception ex) {
			logger.error(ex.getMessage() + "\nError in UNIDENTIFIED");
		}
	}

	public DBSearchDialog() {
		this((Frame)null, "", false, null, "");
	}

	public DBSearchDialog(Window parent, String title, boolean modal,
			DBTableSource searchSource) {
		super(parent, title);
		this.searchSource = searchSource;
		this.setModal(modal);
		try {
			jbInit();
			pack();
		}
		catch(Exception ex) {
			logger.error(ex.getMessage() + "\nError in UNIDENTIFIED");
		}
	}

	public void setDeltas( int deltaX, int deltaY ) {
		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}
	
	public void clear() {
		// TODO override in specific class.
	}

	public void center() {
		//Center the window
		boolean bSmall = false;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize();
		if( frameSize.height > screenSize.height ) {
			frameSize.height = screenSize.height;
			bSmall = true;
		}
		if( frameSize.width > screenSize.width ) {
			frameSize.width = screenSize.width;
			bSmall = true;
		}
		if( bSmall ) {
			setLocation( (screenSize.width - frameSize.width) / 2, 0);
		}
		else {
			setLocation( deltaX + (screenSize.width - frameSize.width) / 2,
					deltaY + (screenSize.height - frameSize.height) / 2);
		}
	}

	public void setHideCode( boolean bHideCode ) { this.bHideCode = bHideCode; }

	public boolean exitOK() { return bOK; }
	public Integer getSelectedKey() { return iKey; }
	public void setSelectedKey( int iKey ) { this.iKey = iKey; }
	public String getSelectedValue() { return sValue; }

	public void setVisible( boolean bShow ) {
		if( bShow ) {
			if ( bHideCode ) {
				tblSearch.hideFirstColumn();
			}
			if (iKey >= 0)
				tblSearch.selectByKey(iKey);
		}
		super.setVisible(bShow);
	}

	private void jbInit() throws Exception {
		try {
			jpMain.setLayout(borderLayout1);
			jpButtons.setAlignmentX((float) 0.5);
			jbOK.setRequestFocusEnabled(true);
			jbOK.setPreferredSize(new Dimension(75, 25));
			jbOK.setText("OK");
			jbOK.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed( ActionEvent e ) {
					bOK = true;
					iKey = tblSearch.getSelectedKey();
					sValue = tblSearch.getSelectedValue();
					setVisible(false);
				}
			});
			jbCancel.setPreferredSize(new Dimension(75, 25));
			jbCancel.setText("Cancel");
			jbCancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed( ActionEvent e ) {
					bOK = false;
					iKey = -1;
					sValue = null;
					setVisible(false);
				}			
			});
			jpClient.setLayout(borderLayout2);
			getContentPane().add(jpMain);
			jpMain.add(jpClient, BorderLayout.CENTER);
			jpMain.add(jpButtons,  BorderLayout.SOUTH);
			btnNone.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					bOK = true;
					iKey = -1;
					sValue = null;
					setVisible(false);
				}
			});
			btnNone.setPreferredSize(new Dimension(75, 25));
			jpButtons.add(btnNone);
			jpButtons.add(jbCancel, null);
			jpButtons.add(jbOK, null);
			tblSearch.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if( e.getClickCount() == 2 ) {
						bOK = true;
						iKey = tblSearch.getSelectedKey();
						sValue = tblSearch.getSelectedValue();
						setVisible(false);
					}
				}

			});
			if( sSearchQuery != null ) {
				tblSearch.setDatabaseConnectionFactory(factory);
				tblSearch.setQuery(sSearchQuery);
			}
			else if ( searchSource != null ) {
				tblSearch.setTableSource( searchSource );
			}
			tblSearch.setFillsViewportHeight(true);
			tblSearch.refresh();
			jpClient.add(scrollPane, BorderLayout.CENTER);
			scrollPane.setViewportView(tblSearch);

			jpClient.add(panel, BorderLayout.NORTH);
			panel.add(lblSearch);
			panel.add(jtfSearchFor);
			panel.add(btnSearch);
			jtfSearchFor.setColumns(30);
			jtfSearchFor.addKeyListener( new KeyAdapter() {
				public void keyTyped(KeyEvent e) {
					String sSearch = jtfSearchFor.getText();
					char c = e.getKeyChar();
					if (c == 0x08) {
						if (sSearch.length() < 1) {
							sSearch = "";
							jtfSearchFor.setForeground(Color.RED);
						}
					}
					else if ( Character.getNumericValue(c) > 0 ) {
						sSearch = sSearch + c;
					}
					if( sSearch.length() > 0 ) {
						if( tblSearch.search(sSearch) ) {
							jtfSearchFor.setForeground(Color.BLACK);
						}
						else {
							jtfSearchFor.setForeground(Color.RED);
						}
					}
				}
			});
			btnSearch.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					tblSearch.search(jtfSearchFor.getText());
				}
			});
		} catch( Throwable t ) {
			logger.error("Unusual error in DBSearchDialog.jbInit()", t);
		}
	}

}// End class DBSearchDialog



