package edu.clemson.lph.civet.files;
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
import java.io.File;
import java.io.FileFilter;

import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;

@SuppressWarnings("serial")
public class EmailFilesTableModel extends FilesTableModel {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());

	public EmailFilesTableModel(File fDirectory) {
		super(fDirectory, new FileFilter() {
			@Override
			public boolean accept(File arg0) {
				String sName = arg0.getName();
				if( sName.toLowerCase().endsWith("cvi") ) return true;
				return false;
			}
		});
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public Object getValueAt(int iRow, int iColumn) {
		if( allFiles == null || iRow < 0 || iRow >= allFiles.size() )
			return null;
		else {
			File fThis = allFiles.get(iRow);
			switch( iColumn ) {
			case 0: return fThis.getName();
			case 1: return getFromState(fThis);
			case 2: return getToState(fThis);
			case 3: long lLastSave = fThis.lastModified();
				java.util.Date dLastSave = new java.util.Date( lLastSave );
				return dLastSave;
			case 4: 
				long lSize = fThis.length();
				// This isn't totally pretty at this point but leave it to tweak later.
				// Probably better to return as a double and let a cell renderer handle it.
				String sRet = String.format("%,.2f K", lSize/1024.00);
				for( int i = sRet.length(); i < 10; i++ )
					sRet = " " + sRet;
				return sRet;
			default: return null;
			}
		}
	}

	@Override
	public String getColumnName(int iColumn) {
		switch( iColumn ) {
		case 0: return "File Name";
		case 1: return "From State";
		case 2: return "To State";
		case 3: return "Last Saved";
		case 4: return "File Size";
		default: return "Error";
		}
	}
	
	@Override
	public Class<?> getColumnClass(int iColumn) {
		switch( iColumn ) {
		case 3: return java.util.Date.class;
		default: return String.class;
		}
	}
	
	@Override
	public TableRowSorter<FilesTableModel> getSorter() {
		TableRowSorter<FilesTableModel> sorter = new TableRowSorter<FilesTableModel>( this );
		sorter.setComparator(3, new DateCellComparator());
		return sorter;
	}
	
	private String getFromState(File fThis) {
		String sRet = null;
		String sName = fThis.getName();
		String sParts[] = sName.split("\\_");
		if( sParts.length >= 2 )
			sRet = sParts[1];
		return sRet;
	}
	
	private String getToState(File fThis) {
		String sRet = null;
		String sName = fThis.getName();
		String sParts[] = sName.split("\\_");
		if( sParts.length >= 4 )
			sRet = sParts[3];
		return sRet;
	}
	

}
