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
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;

@SuppressWarnings("serial")
public abstract class FilesTableModel extends AbstractTableModel {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	protected boolean bMulti = true;
	protected File fDir;
	protected FileFilter fileFilter = null;
	protected ArrayList<File> allFiles = null;

	public FilesTableModel(File fDirectory ) {
		this.fDir = fDirectory;
		if( !fDir.isDirectory() ) {
			logger.error("File " + fDir.getAbsolutePath() + " is not a directory", new Exception("Directory not found"));
			return;
		}
		readFiles();
	}
	
	public FilesTableModel(File fDirectory, FileFilter ff ) {
		this.fDir = fDirectory;
		if( !fDir.isDirectory() ) {
			logger.error("File " + fDir.getAbsolutePath() + " is not a directory", new Exception("Directory not found"));
			return;
		}
		fileFilter = ff;
		readFiles();
	}
	
	public String getAbsolutePath() {
		return fDir.getAbsolutePath();
	}
	
	protected void readFiles() {
		allFiles = new ArrayList<File>();
		for( File f : fDir.listFiles(fileFilter) ) {
			if( f.isFile() && !f.isHidden() ) 
				allFiles.add(f);
		}
	}
	
	public void setFileFilter( FileFilter ff ) {
		this.fileFilter = ff;
	}
	
	/**
	 * Reread directory list
	 */
	public void refresh() {
		readFiles();
		this.fireTableDataChanged();
	}

	@Override
	public abstract int getColumnCount();
	@Override
	public abstract Object getValueAt(int iRow, int iColumn);
	@Override
	public abstract String getColumnName(int iColumn);
	
	public TableRowSorter<FilesTableModel> getSorter() {
		TableRowSorter<FilesTableModel> sorter = new TableRowSorter<FilesTableModel>( this );
		return sorter;
	}

	@Override
	public int getRowCount() {
		if( allFiles == null )
			return 0;
		else
			return allFiles.size();
	}

	public ArrayList<File> getAllFiles() {
		return allFiles;
	}

	public File getSelectedFile( JTable tblParent ) {
		if( allFiles == null || tblParent == null )
			return null;
		File fRet = null;
		int iRow = tblParent.convertRowIndexToModel(tblParent.getSelectedRow());
		if( iRow >= 0 )
			fRet = allFiles.get(iRow);
		return fRet;
	}

	public ArrayList<File> getSelectedFiles(JTable tblParent ) {
		if( allFiles == null || tblParent == null )
			return null;
		ArrayList<File> afRet = new ArrayList<File>();
		int iRows[] = tblParent.getSelectedRows();
		for( int i = 0; i < iRows.length; i++ ) {
			int iRow = tblParent.convertRowIndexToModel(iRows[i]);
			if( iRow >= 0 ) {
				File fRet = allFiles.get(iRow);
				afRet.add(fRet);
			}
		}
		return afRet;
	}

	public void setMultiSelect(boolean bMultiSelect) {
		this.bMulti = bMultiSelect;
	}

	public boolean isMultiSelect() {
		return bMulti;
	}

}
