package edu.clemson.lph.controls;
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
import java.awt.event.*;
import java.awt.*;
import java.util.*;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.db.*;
import edu.clemson.lph.dialogs.DBSearchDialog;
import edu.clemson.lph.dialogs.SearchDialog;

import org.apache.log4j.*;


@SuppressWarnings("serial")
public class DBSearchComboBox extends DBComboBox {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private String sSearchQuery;
	private String sSearchTitle = "Search";
	private SearchDialog<Integer> dSearchDialog = null;
	private boolean bHideCode = false;
	private ArrayList<DBSearchComboBoxListener> lListeners = null;
	private int deltaX = 0;
	private int deltaY = 0;
	private DBTableSource searchSource;

	public DBSearchComboBox() {
		try {
			jbInit();
		}
		catch(Exception e) {
			logger.error(e.getMessage() + "\nError in UNIDENTIFIED" );
		}
	}

	public DBSearchComboBox(DatabaseConnectionFactory factory, String sQuery) throws Exception {
		super(factory, sQuery);
	}

	public DBSearchComboBox(DatabaseConnectionFactory factory, String sQuery, String sSearchQuery) throws Exception {
		super(factory, sQuery);
		this.sSearchQuery = sSearchQuery;
	}

	public void setSearchQuery( String sSearchQuery ) {
		this.sSearchQuery = sSearchQuery;
	}
	
	public void setSearchSource(DBTableSource source) {
		this.searchSource = source;
	}
	
	public void setSearchDialog(SearchDialog<Integer> dlg) {
		this.dSearchDialog = dlg;
	}

	public String getSearchQuery() { return sSearchQuery; }

	public void setSearchTitle( String sSearchTitle ) {
		this.sSearchTitle = sSearchTitle;
	}
	public void setDeltas( int deltaX, int deltaY ) {
		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}

	public String getSearchTitle() { return sSearchTitle; }
	public void setHideCode( boolean bHideCode ) { this.bHideCode = bHideCode; }

	public void addListener( DBSearchComboBoxListener listener ) {
		if( lListeners == null ) lListeners = new ArrayList<DBSearchComboBoxListener>();
		lListeners.add( listener );
	}

	private void jbInit() throws Exception {
		this.addKeyListener( new KeyAdapter() {
			@Override
			public void keyPressed( KeyEvent e ) {
				try {
					SearchDialog<Integer> dSearch = null;
					if( ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && "F".equals(KeyEvent.getKeyText(e.getKeyCode())) ) {
						if( dSearchDialog != null ) {
							dSearch = dSearchDialog;
						}
						else if( sSearchQuery != null || searchSource != null ) {
							DBSearchDialog dlg = null;
							Window wParent = getWindowParent();
							if( wParent != null ) {
								if( sSearchQuery != null ) {
									dlg = new DBSearchDialog( wParent,
											sSearchTitle, true, DBSearchComboBox.super.getDatabaseConnectionFactory(), sSearchQuery);
								}
								else if( searchSource != null ) {
									dlg = new DBSearchDialog( wParent, sSearchTitle, true, searchSource );
								}
								else {
									logger.error("SearchDialog initialized with no source", new Exception("Civet Error"));
									return;
								}
								dlg.setHideCode(bHideCode);
								dSearch = dlg;
							}
						}
						if( dSearch != null ) {
							dSearch.setDeltas(deltaX, deltaY);
							dSearch.setVisible(true);
							if (dSearch.exitOK()) {
								int iKey = dSearch.getSelectedKey();
								DBSearchComboBox.super.setSelectedKey(iKey);
							}
						}
					}
				} catch( Throwable t ) {
					logger.error("Unusual Error in Ctrl F", t);
				}
			}
		});
	}

	public Window getWindowParent() {
		Container parent = this.getParent();
		Window wParent = null;
		while (! (parent instanceof Window) ) {
			if (parent == null) return wParent; // I give up.  Where am I?
			parent = parent.getParent();
		}
		if (parent instanceof Window) wParent = (Window) parent;
		return wParent;
	}



}// End class DBSearchComboBox
