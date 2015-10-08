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
import javax.swing.JTextField;
import javax.swing.text.Document;

import java.awt.event.*;
import java.util.*;

import edu.clemson.lph.db.*;
import edu.clemson.lph.dialogs.DBSearchDialog;

import java.awt.*;


@SuppressWarnings("serial")
public class DBSearchTextField extends JTextField {
//	private static final Logger logger = Logger.getLogger(Civet.class.getName());
//	private String sQuery = null;
	private String sSearchQuery;
	private String sSearchTitle = "Search";
	private String sLookupQuery = null;
	private boolean bHideCode = false;
	private ArrayList<DBSearchTextFieldListener> lListeners = null;
	private DatabaseConnectionFactory factory = null;
	private String sValue = null;
	private int iValue = -1;
	private boolean bInSearch = false;
	private boolean bChanged = false;
	private int deltaX = 0;
	private int deltaY = 0;
	private DBSearchTextFieldModel model;

	public DBSearchTextField() {
		init();
	}

	public DBSearchTextField(int p0) {
		super(p0);
		init();
	}

	public DBSearchTextField(String p0) {
		super(p0);
		init();
	}

	public DBSearchTextField(String p0, int p1) {
		super(p0, p1);
		init();
	}

	public DBSearchTextField(Document p0, String p1, int p2) {
		super(p0, p1, p2);
		init();
	}

	private void init() {
		try {
			jbInit();
			// Temporary test code
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				this_keyPressed(e);
			}
			public void keyTyped(KeyEvent e) {
				//        e.consume();
			}
		});
		if( factory != null ) {
			super.setText("CTRL-F To FIND and set value");
			this.setToolTipText("CTRL-F To FIND and set value");
		}
		else {
			super.setText("");
			this.setToolTipText("");
		}
		
	}

	public void setDatabaseConnectionFactory( DatabaseConnectionFactory factory ) { 
		this.factory = factory; 
		if( factory != null ) {
			super.setText("CTRL-F To FIND and set value");
			this.setToolTipText("CTRL-F To FIND and set value");
		}
		else {
			super.setText("");
			this.setToolTipText("");
		}
	}
	public DatabaseConnectionFactory getDatabaseConnectionFactory() { return factory; }
	
	public void setModel( DBSearchTextFieldModel model ) {
		this.model = model;
		setDatabaseConnectionFactory( model.getFactory() );
		setSearchQuery( model.getSearchQuery() );
	}
	/**
	 * Query to populate search table.  First column to be integer db value.
	 * @param sSearchQuery
	 */
	public void setSearchQuery( String sSearchQuery ) { this.sSearchQuery = sSearchQuery; }
	public void setSearchTitle( String sSearchTitle ) { this.sSearchTitle = sSearchTitle; }
	public void setHideCode( boolean bHideCode ) { this.bHideCode = bHideCode; }
	public boolean isInSearch() { return bInSearch; }
	public void setDeltas( int deltaX, int deltaY ) {
		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}

	public void addListener( DBSearchTextFieldListener listener ) {
		if( lListeners == null ) lListeners = new ArrayList<DBSearchTextFieldListener>();
		lListeners.add( listener );
	}

	/** Refill the text with the same value.  Used after underlying data may have changed without changing key **/
	public void refresh() {
		setText( sValue );
	}

	// This method ensures that only valid values can populate this field
	@Override
	public void setText( String sText ) {
		if( sText == null || sText.trim().length() == 0 || "CTRL-F To FIND and set value".equals(sText) ) {
			this.sValue = null;
			this.iValue = -1;
			if( model != null && model.isConnected() )
				super.setText("CTRL-F To FIND and set value");
		}
		else {
			this.sValue = sText;
			if( sLookupQuery != null ) {
				iValue = model.lookup( sValue );
			}
			super.setText(sText);
		}
	}

	public void setValue( int iValue ) {
		this.iValue = iValue;
		String sVal = Integer.toString( iValue );
		if( model != null && model.isConnected() ) 
			sVal = model.lookup( iValue );
		else 
			sVal = "";
		super.setText(sVal);
		sValue = sVal;
	}

	public String getText() {
		return sValue;
	}

	public String getDisplayText() {
		return super.getText();
	}

	public int getValue() {
		if( bChanged ) {
			sValue = super.getText();
			iValue = model.lookup( sValue );
			bChanged = false;
		}
		return iValue;
	}

	void this_keyPressed(KeyEvent e) {
		// Could just look for numerical value of code == 70 but that might be less readable.
		if( ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && "F".equals(KeyEvent.getKeyText(e.getKeyCode())) ) {
			// Search is just to complicated to generically outsource to the model.  
			// We assume a live db connection here.
			if( sSearchQuery != null && factory != null ) {
				bInSearch = true;
				DBSearchDialog dlg = null;
				Window wParent = getWindowParent();
				if( wParent != null ) {
					dlg = new DBSearchDialog( wParent,
							sSearchTitle, true, getDatabaseConnectionFactory(), sSearchQuery);
					dlg.setHideCode(bHideCode);
					dlg.setDeltas(deltaX, deltaY);
					dlg.center();
					dlg.setVisible(true);
					if (dlg.exitOK()) {
						int iKey = dlg.getSelectedKey();
						this.setValue(iKey);
						// Don't bother to lookup again.
						bChanged = false;
					}
				}
				bInSearch = false;
			}
		}
		else if( ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && "N".equals(KeyEvent.getKeyText(e.getKeyCode())) ) {
			if( lListeners != null ) {
				for( Iterator<DBSearchTextFieldListener> iter = lListeners.iterator(); iter.hasNext(); ) {
					DBSearchTextFieldListener listener = iter.next();
					listener.newItem(this);
				}
			}
		}
		else if( ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) && "E".equals(KeyEvent.getKeyText(e.getKeyCode())) ) {
			if( lListeners != null ) {
				for( Iterator<DBSearchTextFieldListener> iter = lListeners.iterator(); iter.hasNext(); ) {
					DBSearchTextFieldListener listener = iter.next();
					listener.editItem(this);
				}
			}
		}
		else if( e.getKeyCode() == KeyEvent.VK_DELETE ) {
			setText( null );
			bChanged = true;
		}
		else
			bChanged = true;
	}

	public Window getWindowParent() {
		Container parent = this.getParent();
		Window wParent = null;
		while (! (parent instanceof Window) ) {
			if (parent == null) return wParent; // I give up.  Where am I?
			parent = parent.getParent();
		}
		wParent = (Window) parent;
		return wParent;
	}

}// End Class DBSearchTextField
