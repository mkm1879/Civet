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
import javax.swing.*;
import javax.swing.table.*;

import java.awt.*;

import edu.clemson.lph.logging.Logger;


import edu.clemson.lph.db.DBTableModel;
import edu.clemson.lph.db.DBTableSource;
import edu.clemson.lph.db.DatabaseConnectionFactory;
import edu.clemson.lph.db.ThreadListener;


@SuppressWarnings("serial")
public class DBTable extends JTable implements ThreadListener {
      private static Logger logger = Logger.getLogger();
	private Thread refreshThread = null;
	private DBTableModel model;
	
	public DBTable() {
		super();
		model = new DBTableModel();
		model.addThreadListener(this);
		setRowSelectionAllowed(true);
		setAutoCreateRowSorter(true);
	}
	
	public DBTable( DBTableModel model ) {
		super();
		this.model = model;
		setModel(model);
		model.addThreadListener(this);
		setRowSelectionAllowed(true);
	}
	
	public void setDBModel( DBTableModel model ) {
		this.model = model;
		setModel(model);
		model.addThreadListener(this);
		setRowSelectionAllowed(true);
		setAutoCreateRowSorter(true);
	}
	
	public void setDatabaseConnectionFactory( DatabaseConnectionFactory factory ) {
		model.setDBConnectionFactory(factory);
	}
	
	public void setQuery( String sQuery ) {
		model.setQuery(sQuery);
	}
	
	public int getSortedColumn() {
		int iRet = -1;
		try {
		iRet = getRowSorter().getSortKeys().get(0).getColumn();
		} catch( Exception e ) {
			iRet = -1;
		}
		return iRet;
	}
	
    public boolean search( String target ) {
    	int iCol = getSortedColumn();
    	// Should really just subtract hidden columns less than the sorted column!
    	int iFound = -1;
    	if( iCol >= 0 ) {
    		String sTarget = target.toUpperCase();
    		int iMaxRow = getRowCount() -1;
    		int iCurrentRow = getSelectedRow();  // Here we WANT the displayed row # not model row #
    		if( iCurrentRow < 0 )
    			iFound = searchRecursive( sTarget, iCol, 0, iMaxRow );
    		else {
    			String sCurrent = (String)getValueAt(iCurrentRow, iCol);
    			if( sCurrent != null ) {
    				sCurrent = sCurrent.toUpperCase();
    				if( sCurrent.startsWith(sTarget) ) {
    					iFound = iCurrentRow;
    				}
    				else if( sTarget.compareTo( sCurrent ) < 0 ) {
    					iFound = searchRecursive( sTarget, iCol, 0, iCurrentRow );
    				}
    				else {
    					iFound = searchRecursive( sTarget, iCol, iCurrentRow, iMaxRow );
    				}
    			}
    			else {
					iFound = searchRecursive( sTarget, iCol, iCurrentRow, iMaxRow );
    			}
    		}
    		if( iFound >= 0 ) {
    			// Kluge to make sure we found the FIRST match to our search string.
    			String sPrev = (String)getValueAt(iFound-1, iCol);
    			while( sPrev != null && sPrev.toUpperCase().startsWith(sTarget) ) {
    				iFound--;
    				sPrev = (String)getValueAt(iFound-1, iCol);
    			}
				setRowSelectionInterval(iFound, iFound);
				showSearchResults(iFound, iCol);
				return true;
    		}
    		// Previously used Brute force linear search.  Really wasn't too slow!
//    		for(int row = 0; row < getRowCount(); row++) {
//    			String next = (String)getValueAt(row, iCol);
//    			if( next != null ) next = next.toUpperCase();
//    			if(next.startsWith(sTarget))
//    			{
//    				setRowSelectionInterval(row, row);
//    				showSearchResults(row, iCol);
//    				return true;
//    			}
//    		}
    	}
    	return false;
    }
    
    private int searchRecursive( String sTarget, int iCol, int iMinRow, int iMaxRow ) {
    	if( iMinRow > iMaxRow - 10 ) {
			return linearSearch( sTarget, iCol, iMinRow, iMaxRow );
    	}
    	int iCurrentRow = (iMinRow + iMaxRow) / 2;
		String sCurrent = (String)getValueAt(iCurrentRow, iCol);
		if( sCurrent == null && sTarget != null )
			// Null current = current < target
			return searchRecursive( sTarget, iCol, iCurrentRow + 1, iMaxRow );
		if( sCurrent != null ) {
			sCurrent = sCurrent.toUpperCase();
			if( sCurrent.startsWith(sTarget) ) {
				return iCurrentRow;
			}
			else if( sTarget.compareTo( sCurrent ) < 0 ) {
				return searchRecursive( sTarget, iCol, 0, iCurrentRow - 1 );
			}
			else {
				return searchRecursive( sTarget, iCol, iCurrentRow + 1, iMaxRow );
			}
		}
		else {
			return searchRecursive( sTarget, iCol, iCurrentRow + 1, iMaxRow );
		}
    }
    
    public int linearSearch( String sTarget, int iCol, int iMinRow, int iMaxRow ) {
    	for(int row = iMinRow; row < iMaxRow; row++) {
    		String next = (String)getValueAt(row, iCol);
    		// null is never equal so skip to next.
    		if( next != null ) {
    			next = next.toUpperCase();
    			if(next.startsWith(sTarget))
    			{
    				setRowSelectionInterval(row, row);
    				showSearchResults(row, iCol);
    				return row;
    			}
    		}
    	}
    	return -1;
    }
    private void showSearchResults(int row, int col)
    {
        Rectangle r = getCellRect(row, col, false);
        scrollRectToVisible(r);
        repaint();
    }
	
	public void hideFirstColumn() {
		model.hideFirstColumn();
	}
	
	public void selectByKey( int iKey ) {
		if( iKey <= 0 ) {
			return;
		}
		for( int i = 0; i < model.getRowCount(); i++ ) {
			String sRowKey = (String)model.getValueAt(i, 0);
			int iRowVal = Integer.parseInt(sRowKey);
			if( iRowVal == iKey ) {
				this.setRowSelectionInterval(i,i);
				break;
			}
		}
	}
	
	public void selectByValue( String sValue ) {
		if( sValue == null ) {
			return;
		}
		for( int i = 0; i < model.getRowCount(); i++ ) {
			String sRowVal = (String)model.getValueAt(i, 1);
			if( sValue.equals(sRowVal) ) {
				this.setRowSelectionInterval(i,i);
				break;
			}
		}
	}
	
	public void setAutoCreateRowSorter( boolean bSort ) {
		super.setAutoCreateRowSorter(bSort);
	}
	
	public int getSelectedKey() {
		int iRet = -1;
		int iRow = convertRowIndexToModel(getSelectedRow());
		if( iRow >= 0 ) {
			String sKey = (String)model.getKeyAt(iRow);
			try {
			iRet = Integer.parseInt(sKey);
			} catch( NumberFormatException nfe ) {
				logger.error( "Non integer value " + sKey + "in column 0" );
			}
		}
		// TODO find value of first column of selected row and convert to int.
		return iRet;
	}
	
	public String getSelectedValue() {
		String sValue = null;
		int iRow = convertRowIndexToModel(getSelectedRow());
		if( iRow >= 0 ) {
			sValue = (String)model.getValueAt(iRow, 1);
		}
		// TODO find value of first column of selected row and convert to int.
		return sValue;
	}
	
	public void clearQueryParameters() {model.clearQueryParameters(); }
	
	public void setQueryParameter( Integer iIndex, Object oValue ) {
		model.setQueryParameter( iIndex, oValue );
	}

	public void refresh() { refreshThread = model.refresh(); }
	
	public void addThreadListener( ThreadListener listener ) {
		model.addThreadListener(listener);
	}

	@Override
	public void onThreadComplete(Thread thread) {
		if( thread == refreshThread && model.isValid() ) {
			setModel(model);
			refreshThread = null;
		}
	}
	
	private static class DateCellRenderer extends DefaultTableCellRenderer {
        public DateCellRenderer() { super(); }
        public void setValue(Object value) {
            if ( value instanceof java.util.Date ) {
                super.setValue( DateField.dateToText((java.util.Date)value) );
            }
            else
            	super.setValue(value);
        }
    };
	
    public TableCellRenderer getCellRenderer(int row, int column) {
        if (column != 0) {
            return new DateCellRenderer();
        }
        // else...
        return super.getCellRenderer(row, column);
    }

	public void setTableSource(DBTableSource tableSource) {
		model.setSource(tableSource);
	
	}


}// end class DBTable

