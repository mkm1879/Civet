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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.xml.StdeCviXmlV1;

@SuppressWarnings("serial")
public class StdXMLFilesTableModel extends FilesTableModel {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
//	private ArrayList<StdeCviXml> aRows = new ArrayList<StdeCviXml>();
	private ArrayList<String[]> aRows = new ArrayList<String[]>();
	private SimpleDateFormat df = new SimpleDateFormat( "MMM d, yyyy");

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
			StdeCviXmlV1 thisStd = new StdeCviXmlV1( f );
			String aRow[] = new String[8];
			aRow[0] = thisStd.getCertificateNumber();
			aRow[1] = thisStd.getOriginState();
			aRow[2] = thisStd.getDestinationState();
			aRow[3] = thisStd.getSpeciesCodes();
			aRow[4] = thisStd.getVetName();
			aRow[5] = df.format(thisStd.getIssueDate());
			aRow[6] = df.format(thisStd.getBureauReceiptDate());
			String sErrors = thisStd.getErrorsString();
			if( sErrors == null ) 
				sErrors = "None";
			aRow[7]= sErrors;
			aRows.add(aRow);	
			//			aRows.add(thisStd);
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
		if( aRows == null || iRow < 0 || iRow >= aRows.size() )
			return null;
		else {
			String aRow[] = aRows.get(iRow);
			if( iColumn >= 0 && iColumn <= 7 && iColumn != 5 && iColumn != 6)
				return aRow[iColumn];
			else if( iColumn == 5 || iColumn == 6 ) {
				try {
					return df.parse(aRow[iColumn]);
				} catch (ParseException e) {
					logger.error(e);
					return "";
				}
			}
			else
				return "Error";
		}
	}

	@Override
	public void refresh() {
		aRows.clear();
		readFiles();
		buildDocuments();
		this.fireTableDataChanged();		
	}


} // End class StdXMLFileTableModel
