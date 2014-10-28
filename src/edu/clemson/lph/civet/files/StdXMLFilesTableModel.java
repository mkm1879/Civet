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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.xml.StdeCviXml;

@SuppressWarnings("serial")
public class StdXMLFilesTableModel extends FilesTableModel {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private ArrayList<StdeCviXml> xFiles = new ArrayList<StdeCviXml>();

	public StdXMLFilesTableModel(File fDirectory) {
		super(fDirectory, new FileFilter() {
			@Override
			public boolean accept(File arg0) {
				return arg0.getName().toLowerCase().endsWith(".cvi");
			}
		});
		buildDocuments();
	}

	public StdXMLFilesTableModel(File fDirectory, FileFilter ff) {
		super(fDirectory, ff);
		buildDocuments();
	}
	
	private void buildDocuments() {
		for( File f : allFiles ) {
			StdeCviXml thisStd = new StdeCviXml( f );
			xFiles.add(thisStd);
		}
	}
	
	@Override
	public String getColumnName(int iColumn) {
		switch( iColumn ) {
		case 0: return "CVI Number";
		case 1: return "From";
		case 2: return "To";
		case 3: return "Species";
		case 4: return "Vet";
		case 5: return "Date Issued";
		case 6: return "Date Received";
		case 7: return "Errors";
		default: return "Error";
		}
	}
	
	@Override
	public Class<?> getColumnClass(int iColumn) {
		switch( iColumn ) {
		case 5: return java.util.Date.class;
		case 6: return java.util.Date.class;
		default: return String.class;
		}
	}
	
	@Override
	public TableRowSorter<FilesTableModel> getSorter() {
		TableRowSorter<FilesTableModel> sorter = new TableRowSorter<FilesTableModel>( this );
		sorter.setComparator(5, new DateCellComparator());
		sorter.setComparator(6, new DateCellComparator());
		return sorter;
	}

	@Override
	public int getColumnCount() {
		return 8;
	}

	@Override
	public Object getValueAt(int iRow, int iColumn) {
		if( xFiles == null || iRow < 0 || iRow >= xFiles.size() )
			return null;
		else {
			StdeCviXml xThis = xFiles.get(iRow);
			switch( iColumn ) {
			case 0: return xThis.getCertificateNumber();
			case 1: return xThis.getOriginState();
			case 2: return xThis.getDestinationState();
			case 3: return xThis.getSpeciesCodes();
			case 4: return xThis.getVetName();
			case 5: return xThis.getIssueDate();
			case 6: return xThis.getBureauReceiptDate();
			case 7: 
				String sErrors = xThis.getErrorsString();
				if( sErrors == null ) 
					sErrors = "None";
				return sErrors;
			default: return "Error";
			}
		}
	}
	
	public StdeCviXml getSelectedStdXml(JTable tblParent) {
		if( xFiles == null || tblParent == null )
			return null;
		StdeCviXml std = null;
		int iRow = tblParent.convertRowIndexToModel(tblParent.getSelectedRow());
		if( iRow >= 0 )
			std = xFiles.get(iRow);
		return std;	
	}
	
	public List<StdeCviXml> getSelectedStdXmls(JTable tblParent ) {
		if( xFiles == null || tblParent == null )
			return null;
		ArrayList<StdeCviXml> afRet = new ArrayList<StdeCviXml>();
		int iRows[] = tblParent.getSelectedRows();
		for( int i = 0; i < iRows.length; i++ ) {
			int iRow = tblParent.convertRowIndexToModel(iRows[i]);
			if( iRow >= 0 ) {
				StdeCviXml fRet = xFiles.get(iRow);
				afRet.add(fRet);
			}
		}
		return afRet;
	}

	@Override
	public void refresh() {
		xFiles.clear();
		readFiles();
		buildDocuments();
		this.fireTableDataChanged();		
	}


} // End class StdXMLFileTableModel
