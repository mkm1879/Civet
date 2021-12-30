package edu.clemson.lph.db;
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
import javax.swing.DefaultComboBoxModel;

import edu.clemson.lph.logging.Logger;

import java.sql.*;
import java.util.*;


import edu.clemson.lph.controls.*;
import edu.clemson.lph.dialogs.MessageDialog;


@SuppressWarnings("serial")
public class DBComboBoxModel extends DefaultComboBoxModel<String> {
      private static Logger logger = Logger.getLogger();
	private DatabaseConnectionFactory factory;
	private String sQuery;
	protected boolean bBlank = false;
	// Named for input/output
	protected HashMap<String, Object> hValuesKeys = new HashMap<String, Object>();
	protected HashMap<Object, String> hKeysValues = new HashMap<Object, String>();
	protected HashMap<String, String> hValuesCodes = new HashMap<String, String>();
	protected HashMap<String, String> hCodesValues = new HashMap<String, String>();
	protected DBComboBox parent = null;
	private Vector<ComboBoxSelectionListener> listeners = new Vector<ComboBoxSelectionListener>();

	public DBComboBoxModel() {
		super();
	}

	public DBComboBoxModel(DatabaseConnectionFactory factory, String sQuery) {
		super();
		this.factory = factory;
		this.sQuery = sQuery;
	}
	
	public void setDBFactory( DatabaseConnectionFactory factory ) {
		this.factory = factory;
	}

	public void setQuery( String sQuery ) {
		this.sQuery = sQuery;
	}
	
	public void setBlankDefault( boolean bBlank ) {
		this.bBlank = bBlank;
	}

	public void addSelectionChangeListener( ComboBoxSelectionListener listener ) {
		listeners.add( listener );
	}

	public void setParent( DBComboBox parent ) { this.parent = parent; }

	public void setSelectedItem( String sItem ) {
		// Make this case insensitive
		for( String sKey : hValuesKeys.keySet() ) {
			if( sKey != null && sItem != null && sKey.equalsIgnoreCase( sItem ) ) {
				sItem = sKey;
				break;
			}
		}
		String sOld = (String)getSelectedItem();
		// This is not a mistake. Testing identity objects, not equality of values.
		if( (sOld != null && sItem == null) || (sOld == null && sItem != null) || (sOld != null && !sOld.equals(sItem)) ) {
			for( ComboBoxSelectionListener listener: listeners ) {
				listener.selectionChanged( new SelectionChangeEvent( parent, sOld, sItem) );
			}
		}
		super.setSelectedItem(sItem);
	}
	
	public void setSelectedCode( String sCode ) {
		String sItem = hCodesValues.get(sCode);
		String sOld = (String)getSelectedItem();
		// This is not a mistake. Testing identity objects, not equality of values.
		if( (sOld != null && sItem == null) || (sOld == null && sItem != null) || (sOld != null && !sOld.equals(sItem)) ) {
			for( ComboBoxSelectionListener listener: listeners ) {
				listener.selectionChanged( new SelectionChangeEvent( parent, sOld, sItem) );
			}
		}
		super.setSelectedItem(sItem);
	}
	
	public void setSelectedKey( int iKey ) {
		String sItem = hKeysValues.get( iKey );
		setSelectedItem( sItem );
	}
	
	public void setSelectedKey( Object oKey ) {
		String sItem = hKeysValues.get( oKey );
		setSelectedItem( sItem );
	}
	
	public String getSelectedValue() {
		return (String)getSelectedItem();
	}
	
	public int getSelectedKeyInt() {
		Integer iRet = -1;
		String sItem = (String)getSelectedItem();
		Object oRet = hValuesKeys.get(sItem);
		iRet = (Integer)oRet;
		if( iRet == null ) return -1;
		return iRet;
	}
	
	
	public Object getSelectedKey() {
		String sItem = (String)getSelectedItem();
		Object oRet = hValuesKeys.get(sItem);
		return oRet;
	}

	public String getSelectedCode() {
		String sItem = (String)getSelectedItem();
		String sRet = hValuesCodes.get(sItem);
		return sRet;
	}

	public ArrayList<String> getValues() {
		ArrayList<String> aRet = new ArrayList<String>(hValuesKeys.keySet());
		return aRet;
	}
	
	public int getKeyIntForValue( String sValue ) {
		Object oRet = hValuesKeys.get(sValue);
		if( !(oRet instanceof Integer) ) {
			logger.error("getKeyIntForValue called on key type of " + oRet.getClass().getName(), new Exception("Invalid Call") );
			return -1;
		}
		Integer iKey = (Integer)oRet;
		return iKey.intValue();
	}
	
	public Object getKeyForValue( String sValue ) {
		Object oRet = hValuesKeys.get(sValue);
		return oRet;
	}
		
	public String getCodeForValue( String sValue ) {
		String sRet = hValuesCodes.get(sValue);
		return sRet;
	}
	
	public String getValueForKey( int iKey ) {
		String sValue = hKeysValues.get(iKey);
		return sValue;
	}
	
	public String getValueForKey( Object oKey ) {
		String sValue = hKeysValues.get(oKey);
		return sValue;
	}
	
	public String getValueForCode( String sCode ) {
		String sValue = hCodesValues.get(sCode);
		return sValue;
	}
	
	public void refresh() {
		if( (factory == null || sQuery == null) ) {
			logger.error("DBComboBoxModel not initialized", new Exception( "Combo Box Model Exception "));
			return;
		}
		hValuesKeys.clear();
		hKeysValues.clear();
		super.removeAllElements();
		if( bBlank ) {
			super.addElement("");
			hValuesKeys.put("", -1);
			hKeysValues.put(-1, "");
		}
		Connection newConn = factory.makeDBConnection();
		if( newConn == null ) {
			logger.error("Null newConn in openDBRecord");
			MessageDialog.showMessage(parent.getWindowParent(), "Civet: Database Error", "Could not connect to database");
			return;
		}
		try {
			Statement s = newConn.createStatement();
			ResultSet rs = s.executeQuery(sQuery);
			while( rs.next() ) {
				Integer iKey = rs.getInt(1);
				String sValue = rs.getString(2);
				super.addElement(sValue);
				hValuesKeys.put(sValue, iKey);
				hKeysValues.put(iKey, sValue);
			}
		}
		catch (SQLException ex) {
			logger.error(ex.getMessage() + "\nError in query " + sQuery);
		}
		finally {
			try {
				if( newConn != null && !newConn.isClosed() )
					newConn.close();
			} catch( SQLException se ) { 
				logger.error(se.getMessage());
			}
		}
	}
	
}
