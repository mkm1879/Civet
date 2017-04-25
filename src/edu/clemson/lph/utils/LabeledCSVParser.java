package edu.clemson.lph.utils;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.csv.CSVParser;



/**
 * @author mmarti5
 *
 */
public class LabeledCSVParser extends CSVParserWrapper {

	/**
	 * @param reader
	 * @throws IOException 
	 */
	public LabeledCSVParser(Reader reader) throws IOException {
		super(reader);
	}
		public LabeledCSVParser(String sFileName) throws IOException {
		super(sFileName);
	}
	public LabeledCSVParser(File file) throws IOException {
		super(file);
	}
	public LabeledCSVParser(InputStream isIn) throws IOException {
		super(isIn);
	}
	public LabeledCSVParser(CSVParser pIn) throws IOException {
		super(pIn);
	}

	public int getLabelIdx( String sLabel ) throws IOException {
		if( getHeader() == null ) return -1;
		return getHeader().indexOf(sLabel);
	}
	
	public String[] getLabels() {
		List<String> aHeader = getHeader();
		if( aHeader == null ) return null;
		return aHeader.toArray(new String[0]);
	}
	
	public String getValue( String sLabel ) throws IOException {
		String sRet = null;
		int iIdx = getLabelIdx( sLabel );
		if( iIdx < 0 ) return null;
		List<String> row = getCurrent();
		if( row != null )
			sRet = row.get(iIdx);
		return sRet;
	}
	

	/**
	 * Convenience method to sort alphabetically on specified column
	 * @param iCol column to sort (see LabeledCSVParser.getLabelIdx( ColumnLabel ) )
	 */
	@Override
	public void sort( int iCol ) {
		sort( iCol, false, false );
	}
	
	/**
	 * Sort everything but the top row
	 * NOTE: This is a unique sort the values in iCol should be unique or duplicates will be trimmed randomly.
	 * @param iCol  column to sort (see LabeledCSVParser.getLabelIdx( ColumnLabel ) )
	 * @param bNumbericCompare set to true to parse column to an number before comparing
	 */
	@Override
	public void sort( int iCol, boolean bNumericCompare, boolean bUniqueSort ) {
		final int iSortCol = iCol;
		final boolean bNumeric = bNumericCompare;
		final boolean bUnique = bUniqueSort;
//		final ArrayList<List<String>> aRowsHold = (ArrayList<List<String>>)aRows.clone();
		if( aRows == null || iRows <= 1 || iCol < 0 || iCol >= aRows.get(0).size() ) 
			return;
		ArrayList<String> lHeader = (ArrayList<String>)aRows.get(0);
		List<List<String>> lRows = aRows.subList(1, aRows.size());
		Comparator<List<String>> compRows = new Comparator<List<String>>() {
			// Compare Strings in the indicated column.  The only weird part is
			// numeric comparison.  Try casting to Double.  If both fail they are equal
			// if one fails it is GREATER than the other so it sorts later in the list.
			@Override
			public int compare(List<String> arg0, List<String> arg1) {
				int iRet = 0;
				String s0 = arg0.get(iSortCol);
				String s1 = arg1.get(iSortCol);
				if( bNumeric ) {
					Double d0 = null;
					Double d1 = null;
					try {
						d0 = Double.parseDouble(s0);
					} catch( NumberFormatException e ) { }
					try {
						d1 = Double.parseDouble(s1);
					} catch( NumberFormatException e ) { }
					if( d0 != null && d1 != null)
						iRet =  Double.compare(d0, d1);
					else if( d0 != null && d1 == null )
						return -1;
					else if( d0 == null && d1 != null )
						iRet =  1;
					else
						iRet =  0;
				}
				else {
					iRet = s0.compareTo(s1);
				}
				// If the compared column values are equal find SOMETHING different or the set logic 
				// will only include the first row with that value
				if( !bUnique && iRet == 0 ) {
					for( int i = arg0.size() - 1; i >= 0; i-- ) {
						if( i != iSortCol ) {
							String s0a = arg0.get(i);
							String s1a = arg1.get(i); 
							iRet = s0a.compareTo(s1a);
							if( iRet != 0 ) {
								break;
							}
						}
					}
					
				}
				return iRet;
			}
		};
		TreeSet<List<String>> setRows = new TreeSet<List<String>>(compRows);
		for( List<String> lRow : lRows )
			setRows.add(lRow);
		aRows.clear();
		aRows.add(lHeader); 
		for( List<String> lRow : setRows ) {
			aRows.add(lRow);
		}
		iRows = aRows.size();
	}


}
