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
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import edu.clemson.lph.dialogs.MessageDialog;

@SuppressWarnings("serial")
public class AnimalIDListTableModel extends AbstractTableModel {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private ArrayList<AnimalIDRecord> rows;
	private ArrayList<AnimalIDRecord> savedRows;
	
	public AnimalIDListTableModel() {
		rows = new ArrayList<AnimalIDRecord>();
	}
	
	public AnimalIDListTableModel( ArrayList<AnimalIDRecord> rowsIn ) {
		if( rowsIn != null )
			rows = rowsIn;
		else
			rows = new ArrayList<AnimalIDRecord>();
	}
	
	public void clear() {
		rows.clear();
		if( savedRows != null )
			savedRows.clear();
	}
	
	public void saveState() {
		savedRows = new ArrayList<AnimalIDRecord>();
		for( AnimalIDRecord r : rows )
			savedRows.add(r);
	}
	
	public void restoreState() {
		if( savedRows == null )
			logger.error("Attempt to restore AnimalIDListTableModel without saved state");
		else {
			rows.clear();
			for( AnimalIDRecord r : savedRows )
				rows.add(r);
		}
	}
	
	public void addRow( AnimalIDRecord rowIn ) {
		if( rows.contains(rowIn) ) {
			MessageDialog.showMessage(null, "Civet: Duplicate ID", "ID " + rowIn.sTag + 
					                        " is already in the list for species " + rowIn.sSpecies);
		}
		else {
			rows.add(rowIn);
			fireTableDataChanged();
		}
	}
	
	public void addRow( String sSpeciesCode, String sSpecies, String sTag ) {
		rows.add( new AnimalIDRecord( sSpeciesCode, sSpecies, sTag ) );
		fireTableDataChanged();
	}
	
	public void deleteRow( int iRowID ) {
		AnimalIDRecord rowToDel = null;
		for( AnimalIDRecord row : rows) {
			if( row.iRowID == iRowID ) {
				rowToDel = row;
				break;
			}
		}
		if( rowToDel != null ) {
			rows.remove(rowToDel);
			fireTableDataChanged();
		}
	}
	
	public void deleteRow( AnimalIDRecord rowToDel ) {
		rows.remove(rowToDel);
		fireTableDataChanged();
	}
	
	public void deleteRows( int tableRows[] ) {
		ArrayList<AnimalIDRecord> aDelRows = new ArrayList<AnimalIDRecord>();
		for( int i = 0; i < tableRows.length; i++ ) {
			aDelRows.add(rows.get(tableRows[i]));
		}
		for( AnimalIDRecord r : aDelRows )
			deleteRow( r );
	}
	
	public final ArrayList<AnimalIDRecord> getRows() {
		final ArrayList<AnimalIDRecord> rowsOut = rows;
		return rowsOut;
	}
	
	public final ArrayList<AnimalIDRecord> cloneRows() {
		final ArrayList<AnimalIDRecord> rowsOut = new ArrayList<AnimalIDRecord>();
		for( AnimalIDRecord record : rows ) {
			rowsOut.add(record);
		}
		return rowsOut;
	}
	
	@Override
	public String getColumnName( int arg0 ) {
		if( arg0 == 0 )
			return "Row";
		else if( arg0 == 1 )
			return "Species";
		else if( arg0 == 2 )
			return "Animal ID";
		else 
			return null;
	}
	
	@Override
	public Class<?> getColumnClass( int column ) {
		switch (column) {
		case 0:
			return Integer.class;
		case 1:
			return String.class;
		case 2:
			return String.class;
		default:
			return String.class;
		}
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public Object getValueAt(int iRow, int iCol) {
		if( rows != null &&  iCol >=0 && iCol <= 2 && iRow >=0 && iRow < rows.size() ) {
			if( iCol == 0 )
				return iRow + 1; // Integer.toString(iRow + 1);
			if( iCol == 1 )
				return rows.get(iRow).sSpecies;
			if( iCol == 2)
				return rows.get(iRow).sTag;
			else
				return null;
		}
		else
			return null;
	}

	public String getSpeciesCodeAt(int iRow) {
		if( iRow >=0 && iRow < rows.size() ) {
			return rows.get(iRow).sSpeciesCode;
		}
		else
			return null;
	}

	public int getRowIDAt(int iRow) {
		if( iRow >=0 && iRow < rows.size() ) {
			return rows.get(iRow).iRowID;
		}
		else
			return -1;
	}
}
