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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;

/**
 * @author mmarti5
 * NOTE:  Missing features:  "" to escape the " character is not implemented as used by Excel
 *                           New line character in side " quotes is not supported as used by Excel
 *
 */
public class CSVParserWrapper {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private char cSepChar = ',';
	private char cQuoteChar = '"';
	
	private BufferedReader reader = null;
	
	protected ArrayList<List<String>> aRows = new ArrayList<List<String>>();
	protected int iRows = -1;
	protected int iCurrent = 0;
	
	public CSVParserWrapper(String sFileName) throws IOException {
		reader = new BufferedReader( new FileReader( sFileName ) );
		readLines();
	}
	public CSVParserWrapper(File file) throws IOException {
		reader = new BufferedReader( new FileReader( file ) );
		readLines();
	}
	public CSVParserWrapper(InputStream isIn) throws IOException {
		reader = new BufferedReader( new InputStreamReader( isIn ) );
		readLines();
	}
	public CSVParserWrapper(Reader rIn) throws IOException {
		reader = new BufferedReader( rIn );
		readLines();
	}
	public CSVParserWrapper(CSVParser pIn) throws IOException {
		if( pIn == null ) return;
		try {
			for( CSVRecord r : pIn.getRecords() ) {
				List<String> aRow = new ArrayList<String>();
				for( int i = 0; i < r.size(); i++ ) {
					String sField = r.get(i);
					aRow.add(sField);
				}
				aRows.add(aRow);
			}
			iRows = aRows.size();
			iCurrent = 1;
		}
		finally {
			pIn.close();
		}
	}

	
	public void setSepChar( char c ) {
		cSepChar = c;
	}
	
	public void setQuoteChar( char c ) {
		cQuoteChar = c;
	}
	
	public List<String> getHeader() {
		if( aRows != null && iRows > 0 ) 
			return aRows.get(0);
		else
			return null;
	}
	
	/**
	 * Convenience method to sort alphabetically on specified column
	 * @param iCol column to sort (see LabeledCSVParser.getLabelIdx( ColumnLabel ) )
	 */
	public void sort( int iCol ) {
		sort( iCol, false, false);
	}
	
	/**
	 * Sort everything including the top row
	 * @param iCol  column to sort (see LabeledCSVParser.getLabelIdx( ColumnLabel ) )
	 * @param bNumbericCompare set to true to parse column to an number before comparing
	 */
	protected void sort( int iCol, boolean bNumericCompare, boolean bUniqueSort ) {
//		final ArrayList<List<String>> aRowHold = (ArrayList<List<String>>)aRows.clone(); 
		final int iSortCol = iCol;
		final boolean bUnique = bUniqueSort;
		
		final boolean bNumeric = bNumericCompare;
		if( aRows == null || iRows <= 0 || iCol < 0 || iCol >= aRows.get(0).size() ) 
			return;
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
						iRet = Double.compare(d0, d1);
					else if( d0 != null && d1 == null )
						iRet = -1;
					else if( d0 == null && d1 != null )
						iRet = 1;
					else
						iRet = 0;
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
		for( List<String> lRow : aRows )
			setRows.add(lRow);
		aRows.clear();
		for( List<String> lRow : setRows ) {
			aRows.add(lRow);
		}
		iRows = aRows.size();
	}
	
	public boolean back() {
		if( aRows != null && iCurrent > 0 ) {
			iCurrent--;
			return true;
		}
		else
			return false;
	}
	
	public void reset() {
		if(iRows >= 1)
			iCurrent = 1;
		else 
			iCurrent = 0;
	}
	
	/**
	 * Better version of getting next row
	 * @return
	 */
	public List<String> getNext() {
		if( aRows != null && iRows > iCurrent ) 
			return aRows.get(iCurrent++);
		else
			return null;
	}	
	
	public List<String> getCurrent() {
		if( aRows != null && iRows > iCurrent ) 
			return aRows.get(iCurrent);
		else
			return null;
	}	

	
	public void close() {
		// Ignore only resources are Java objects to G.C.
	}
	
	/**
	 * Legacy version returns array of strings to mimic older library.
	 * @return
	 */
	public String[] getLine() {
		if( aRows != null && iRows > iCurrent ) {
			List<String> aLine = aRows.get(iCurrent++);
			if( aLine == null  ) return null;
			return aLine.toArray(new String[0]);
		}
		else
			return null;
	}
	
	private void readLines() throws IOException {
		String sAllLines = stripNewLines(readReaderAsString(reader));
		reader = new BufferedReader( new StringReader( sAllLines ) );
		String sLine = reader.readLine();
		while( sLine != null ) {
			ArrayList<String> aFields = readLine( sLine );
			if( aRows.size() > 0 && aFields.size() != aRows.get(0).size() ) {
				logger.error(sLine + '\n' + aFields.size() + " != " + aRows.get(0).size());
			}
			aRows.add(aFields);
			sLine = reader.readLine();
		}
		iRows = aRows.size();
		iCurrent = 1;
		reader.close();
	}
		
	private ArrayList<String> readLine( String sLine ) throws IOException {
		StringBuffer sb = new StringBuffer();
		ArrayList<String> aFields = new ArrayList<String>();
		boolean bInQuote = false;
		boolean bAfterQuote = false;
		for( int i = 0; i < sLine.trim().length(); i++ ) {
			char c = sLine.charAt(i);
			if( c == cQuoteChar && !bInQuote ) {
				bInQuote = true;
			}
			else if( bAfterQuote && c != cSepChar ) {
				// Ignore
			}
			else if( c == cQuoteChar && bInQuote ) {
				bInQuote = false;
				bAfterQuote = true;
			}
			else if( !bInQuote && c == cSepChar ) {
				bInQuote = false;
				bAfterQuote = false;
				aFields.add(sb.toString());	
				sb.setLength(0);
			}
			else {
				sb.append(c);
			}
		}
		aFields.add(sb.toString());
		return aFields;
	}
	
	private static String stripNewLines( String sInput ) {
		StringBuffer sb = new StringBuffer();
		int iQuoteCount = 0;
		char cLast = '\0';
		char cThis = '\0';
		for( int i = 0; i < sInput.length(); i++ ) {
			cThis = sInput.charAt(i);
			if( cThis == '\"' ) {
				if( cLast != '\"' )
					iQuoteCount++;
				else
					iQuoteCount--;
			}
			if( cThis == '\n' ) {
				if( (iQuoteCount % 2) > 0 ) {
//					System.err.println("Removed new line after " + iQuoteCount + " quotes");
//					new Exception("Removed new line after " + iQuoteCount + " quotes").printStackTrace();
//					System.exit(1);
					cThis = ' ';
				}
				else {
					iQuoteCount = 0;
				}
			}
			sb.append(cThis);
			cLast = cThis;
		}
		return sb.toString();
	}

// Replaced by preprocessing files.  This never really worked.
//	private static void stripNewLines( File fIn, File fOut ) {
//		FileReader rIn = null;
//		FileWriter wOut = null;
//		try {
//			rIn = new FileReader( fIn );
//			wOut = new FileWriter( fOut );
//			int iQuoteCount = 0;
//			char cLast = '\0';
//			char cThis = '\0';
//			int iThis = rIn.read();
//			while( iThis >= 0 ) {
//				cThis = (char)iThis;
//				if( cThis == '\"' ) {
//					iQuoteCount++;
//				}
//				if( cThis == '\n' || cThis == '\r'  ) {
//					if( (iQuoteCount % 2) > 0 ) {
//						//					System.err.println("Removed new line after " + iQuoteCount + " quotes");
//						//					new Exception("Removed new line after " + iQuoteCount + " quotes").printStackTrace();
//						//					System.exit(1);
//						cThis = ' ';
//					}
//					else {
//						iQuoteCount = 0;
//					}
//				}
//				wOut.append(cThis);
//				cLast = cThis;
//				iThis = rIn.read();
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			logger.error(e);
//		} finally {
//			try {
//			if( rIn != null )
//				rIn.close();
//			if( wOut != null ) {
//				wOut.flush();
//				wOut.close();
//			}
//			} catch( IOException e ) {
//				logger.error(e);
//			}
//		}
//	}
//
	
	private String readReaderAsString(BufferedReader reader) throws IOException {
        StringBuffer fileData = new StringBuffer();
         char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

}
