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

import org.apache.log4j.*;

import java.sql.*;
import java.util.*;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.controls.*;
import edu.clemson.lph.dialogs.MessageDialog;


@SuppressWarnings("serial")
public class DBComboBoxModel extends DefaultComboBoxModel<String> {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private DatabaseConnectionFactory factory;
	private String sQuery;
	protected boolean bBlank = false;
	protected HashMap<String, Object> hValues = new HashMap<String, Object>();
	protected HashMap<Object, String> hKeys = new HashMap<Object, String>();
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
		for( String sKey : hValues.keySet() ) {
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
	
	public void setSelectedKey( int iKey ) {
		String sItem = hKeys.get( iKey );
		setSelectedItem( sItem );
	}
	
	public void setSelectedKey( Object oKey ) {
		String sItem = hKeys.get( oKey );
		setSelectedItem( sItem );
	}
	
	public String getSelectedValue() {
		return (String)getSelectedItem();
	}
	
	public int getSelectedKeyInt() {
		Integer iRet = -1;
		String sItem = (String)getSelectedItem();
		Object oRet = hValues.get(sItem);
		iRet = (Integer)oRet;
		if( iRet == null ) return -1;
		return iRet;
	}
	
	
	public Object getSelectedKey() {
		String sItem = (String)getSelectedItem();
		Object oRet = hValues.get(sItem);
		return oRet;
	}

	
	public ArrayList<String> getValues() {
		ArrayList<String> aRet = new ArrayList<String>(hValues.keySet());
		return aRet;
	}
	
	public int getKeyIntForValue( String sValue ) {
		Object oRet = hValues.get(sValue);
		if( !(oRet instanceof Integer) ) {
			logger.error("getKeyIntForValue called on key type of " + oRet.getClass().getName(), new Exception("Invalid Call") );
			return -1;
		}
		Integer iKey = (Integer)oRet;
		return iKey.intValue();
	}
	
	public Object getKeyForValue( String sValue ) {
		Object oRet = hValues.get(sValue);
		return oRet;
	}
	
	public String getValueForKey( int iKey ) {
		String sValue = hKeys.get(iKey);
		return sValue;
	}
	
	public String getValueForKey( Object oKey ) {
		String sValue = hKeys.get(oKey);
		return sValue;
	}
	
	public void refresh() {
		if( (factory == null || sQuery == null) ) {
			logger.error("DBComboBoxModel not initialized", new Exception( "Combo Box Model Exception "));
			return;
		}
		hValues.clear();
		hKeys.clear();
		super.removeAllElements();
		if( bBlank ) {
			super.addElement("");
			hValues.put("", -1);
			hKeys.put(-1, "");
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
				hValues.put(sValue, iKey);
				hKeys.put(iKey, sValue);
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
