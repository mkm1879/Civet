package edu.clemson.lph.civet.inbox;
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
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;

@SuppressWarnings("serial")
public class SourceFilesTableModel extends FilesTableModel {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());

	public SourceFilesTableModel(File fDirectory) {
		super(fDirectory, new FileFilter() {
			@Override
			public boolean accept(File arg0) {
				String sName = arg0.getName();
				if( sName.toLowerCase().endsWith("pdf") ) return true;
				if( sName.toLowerCase().endsWith("jpg") ) return true;
				if( sName.toLowerCase().endsWith("jpeg") ) return true;
				if( sName.toLowerCase().endsWith("bmp") ) return true;
				if( sName.toLowerCase().endsWith("gif") ) return true;
				if( sName.toLowerCase().endsWith("png") ) return true;
				if( sName.toLowerCase().endsWith("cvi") ) return true;
				return false;
			}
		});
	}

	@Override
	public int getColumnCount() {
		return 3;
	}
	
	public ArrayList<File> getSelectedFiles(JTable tblParent) {
		ArrayList<File> aRet = new ArrayList<File>();
		int aSelected[] = tblParent.getSelectedRows();
		for( int iSelectedRow : aSelected ) {
			int iRow = tblParent.convertRowIndexToModel(iSelectedRow);
			if( iRow >= 0 ) {
				File fNext = allFiles.get(iRow);
				aRet.add(fNext);
			}
		}
		return aRet;
	}

	@Override
	public Object getValueAt(int iRow, int iColumn) {
		if( allFiles == null || iRow < 0 || iRow >= allFiles.size() )
			return null;
		else {
			File fThis = allFiles.get(iRow);
			switch( iColumn ) {
			case 0: return fThis.getName();
			case 1: long lLastSave = fThis.lastModified();
				java.util.Date dLastSave = new java.util.Date( lLastSave );
				return dLastSave;
			case 2: 
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
	public Class<?> getColumnClass(int iColumn) {
		switch( iColumn ) {
		case 1: return java.util.Date.class;
		default: return String.class;
		}
	}

	@Override
	public String getColumnName(int iColumn) {
		switch( iColumn ) {
		case 0: return "File Name";
		case 1: return "Last Saved";
		case 2: return "File Size";
		default: return "Error";
		}
	}
	
	@Override
	public TableRowSorter<FilesTableModel> getSorter() {
		TableRowSorter<FilesTableModel> sorter = new TableRowSorter<FilesTableModel>( this );
		sorter.setComparator(1, new DateCellComparator());
		return sorter;
	}


}
