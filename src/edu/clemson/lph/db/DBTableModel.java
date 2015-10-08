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
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.table.*;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.dialogs.MessageDialog;

@SuppressWarnings("serial")
public class DBTableModel extends AbstractTableModel implements ThreadListener {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private String sQuery = null;
	private DBTableSource dbTableSource = null;
	private ArrayList<ThreadListener> listeners = new ArrayList<ThreadListener>();
	private ArrayList<String> aColNames = new ArrayList<String>();
	private ArrayList<String> aNewColNames = new ArrayList<String>();
	private ArrayList<ArrayList<Object>> aRows = new ArrayList<ArrayList<Object>>();
	private ArrayList<ArrayList<Object>> aNewRows = new ArrayList<ArrayList<Object>>();
	private HashMap<Integer, Object> hParameters = new HashMap<Integer, Object>();
	// TODO Add provision for query parameters
	private DatabaseConnectionFactory mFactory; 
	private JFrame parent = null;
	private boolean bValid = false;
	private boolean bHideFirst = false;
	
	public DBTableModel() {
		listeners.add(this);
	}

	public DBTableModel(JFrame parent) {
		this.parent = parent;
		listeners.add(this);
	}

	public void setDBConnectionFactory( DatabaseConnectionFactory factory ) {
		mFactory = factory;
	}
	public void setQuery( String query ) {
		sQuery = query;
	}
	
	public void clearQueryParameters() {hParameters.clear(); }
	
	public void setQueryParameter( Integer iIndex, Object oValue ) {
		hParameters.put(iIndex, oValue);
	}
	
	public void setSource( DBTableSource source ) {
		this.dbTableSource = source;
	}
	
	public void addThreadListener( ThreadListener listener ) {
		listeners.add(listener);
	}
	
	public void hideFirstColumn() {
		hideFirstColumn(true);
	}
	public void hideFirstColumn(boolean bHide) {
		this.bHideFirst = bHide;
	}
	
	public boolean isValid() { return bValid; }
	
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return aRows.size();
	}

	@Override
	public int getColumnCount() {
		int iCols = aColNames.size();
		if( bHideFirst ) iCols--;
		return iCols;
	}
	
	public String getColumnName( int columnIndex ) {
		if( bHideFirst && columnIndex < aColNames.size() -1 )
			columnIndex++;
		if( columnIndex < 0 || columnIndex >= aColNames.size() ) return null;
		return aColNames.get(columnIndex);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if( bHideFirst && columnIndex < aColNames.size() -1 )
			columnIndex++;
		if( rowIndex < 0 || rowIndex >= aRows.size() || columnIndex < 0 || columnIndex >= aRows.get(rowIndex).size() ) 
			return null;
		return aRows.get(rowIndex).get(columnIndex);
	}

	public Object getKeyAt(int rowIndex) {
		return aRows.get(rowIndex).get(0);
	}
	
	public boolean isCellEditable(int row, int col) {
        return false;
    }
	
	public Thread refresh() {
		DoWork work = new DoWork();
		work.start();
		return work;
	}
	
	class DoWork extends Thread {
		private void doReadTableSource() {
			aNewRows.clear();
			aNewColNames.clear();
			aNewColNames = dbTableSource.getColumnNames();
			aNewRows = dbTableSource.getRows();
		}
		
		private void doReadDatabase() {
			Connection conn = mFactory.makeDBConnection();
			if( conn == null ) {
				MessageDialog.messageLater(parent, "SQL Error", "Cannot conncet to database on " + mFactory.getDatabaseServerName() );
				return;
			}
			aNewRows.clear();
			aNewColNames.clear();
			try {
				PreparedStatement ps = conn.prepareStatement(sQuery);
				addParameters( ps, hParameters );
				ResultSet rs = ps.executeQuery();
				ResultSetMetaData meta = rs.getMetaData();
				for( int i = 1; i <= meta.getColumnCount(); i++ ) {
					aNewColNames.add(meta.getColumnName(i));
				}
				while( rs.next() ) {
					ArrayList<Object> row = new ArrayList<Object>();
					for( int i = 1; i <= meta.getColumnCount(); i++ ) {
						int iType = meta.getColumnType(i);
						// Force dates to be user friendly when displayed in tables.
						// Note, this will be an issue if I ever need to display the times.
						if( iType == java.sql.Types.TIMESTAMP ) {
							row.add( (Object)rs.getDate(i) );
						}
						else {
							row.add( (Object)rs.getString(i));
						}
					}
					aNewRows.add(row);
				}
			} catch (SQLException e) {
				logger.error("Error in query\n" + e.getMessage() + "\n" + sQuery);
				MessageDialog.messageLater(parent, "SQL Error", "Error in query\n" + e.getMessage() + "\n" + sQuery );
			}
			finally {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error(e);
				}
			}			
		}

		@Override
		public void run() {
			if( dbTableSource != null ) {
				doReadTableSource();
			}
			else {
				doReadDatabase();
			}
			// Then let GUI update itself in main thread.
			SwingUtilities.invokeLater( new Runnable() {
				@Override
				public void run() {
					for( ThreadListener listener : listeners ) {
						listener.onThreadComplete( DoWork.this );
					}
					DBTableModel.this.fireTableStructureChanged();
				}
			});
		}
	}
	
	private void addParameters( PreparedStatement ps, HashMap<Integer, Object> hParameters ) 
			throws SQLException {
		for( Map.Entry<Integer, Object> entry : hParameters.entrySet() ) {
			Integer iIndex = entry.getKey();
			Object oValue = entry.getValue();
			if( oValue == null )
				logger.error( "Null value in DBTableModel parameter ");
			else if( oValue instanceof String ) 
				ps.setString(iIndex, (String)oValue);
			else if( oValue instanceof Integer ) 
				ps.setInt(iIndex, (Integer)oValue);
			else if( oValue instanceof java.util.Date ) 
				ps.setDate(iIndex, new java.sql.Date(((java.util.Date)oValue).getTime()));
			else if( oValue instanceof Float ) 
				ps.setFloat(iIndex, (Float)oValue);
			else if( oValue instanceof Double ) 
				ps.setDouble(iIndex, (Double)oValue);
			else 
				logger.error( "Unexpected object type " + oValue.getClass().getName() );
		}
	}

	@Override
	public void onThreadComplete(Thread thread) {
		ArrayList<String> aTempColNames = aColNames;
		aColNames = aNewColNames;
		aNewColNames = aTempColNames;
		ArrayList<ArrayList<Object>> aTempRows = aRows;
		aRows = aNewRows;
		aNewRows = aTempRows;
		bValid = aRows.size() > 0;
	};

}
