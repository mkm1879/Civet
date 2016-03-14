package edu.clemson.lph.civet.addons;
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
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.utils.CSVParserWrapper;


/**
 * @author mmarti5
 * The CSVFile class represents the raw data file being imported.
 * Need for commas in fields such as dates made me go looking for
 * a more robust CSV file parser.  This version uses the Ostermiller
 * Java Utilities CSVParserWrapper class.
 * Note: Company name must be first part of filename followed by an underscore character.
 */
public class CSVDataFile {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private ArrayList<HashMap<String,String>> aaValues = null;
	private int iCurrentRow;
	private int iNumRows;
	private boolean bRowInbound;
	private String sHomeState;
	private String sCompany = null;
	private java.util.Date dSaved = null;
// Normal Column Order
//	private final static String[] aColNames = { "Date", "SourceFarm", "SourcePIN", "SourceState", 
//		                                     "Vet", "DestFarm", "DestPin", "DestState", "Number" };

	/**
	 * Abstraction of a CSV File with bulk load records.  First row is headers that must match one
	 * of the cases in the various getters.  
	 * @param sFileName Fully qualified filename for the CSV file
	 * @throws FileNotFoundException If the file is not found
	 * @throws IOException If the file cannot be parsed as a CSV file
	 */
	public CSVDataFile( String sFileName ) throws FileNotFoundException, IOException {
		sHomeState = CivetConfig.getHomeStateAbbr();
		ArrayList<String> aKeys = new ArrayList<String>();
		aaValues = new ArrayList<HashMap<String,String>>();
		File f = new File( sFileName );
		dSaved = new java.util.Date( f.lastModified() );
		int iUnderscore = f.getName().indexOf('_');
		if( iUnderscore > 0 ) {
			sCompany = f.getName().substring(0, iUnderscore);
		}
		else {
			sCompany = "UN";
		}
		FileReader fr = new FileReader( f );
		CSVParserWrapper parser = new CSVParserWrapper(fr);
		for( String sField : parser.getHeader() ) {
			if(sField != null) {
				sField = sField.toUpperCase();
				if( aKeys.contains(sField) ) {
					// So we match the first of any given column name
					sField = sField + "(+)";
				}
				aKeys.add(sField);
			}
		}
		iNumRows = 0;
		int iLine = 0;
		List<String> aLine = parser.getNext();
		while( aLine != null ) {
			iLine++;
			if( aLine.size() != aKeys.size() ) {
				logger.error("Line " + iLine + " does not contain the right number of fields");
				return;
			}
			HashMap<String,String> rowData = new HashMap<String,String>();
			boolean bSomeData = false;
			int iField = 0;
			for( String sVal : aLine ) {
				if( sVal.trim().length() > 0 ) bSomeData = true;
				rowData.put(aKeys.get(iField++), sVal);
			}
			if( bSomeData ) {
				aaValues.add(rowData);
				iNumRows++;
			}
			aLine = parser.getNext();
		}
		iCurrentRow = -1;
	}

	/**
	 * Read the next row of data if any
	 * @return true if current pointer is on valid data
	 */
	public boolean nextRow() {
		++iCurrentRow;
		if( iCurrentRow < iNumRows ) {
			bRowInbound = isInbound();
		}
		return iCurrentRow < iNumRows;
	}

	/**
	 * return the string value of cell at current row with header sKey
	 * @param sKey
	 * @return
	 */
	public String get( String sKey ) {
		HashMap<String,String> aRow = aaValues.get(iCurrentRow);
		String sRet = aRow.get(sKey);
		if( sRet != null && sRet.trim().length() == 0 ) sRet = null;
		return aRow.get(sKey);
	}
	
	public java.util.Date getSavedDate() {
		return dSaved;
	}

	/**
	 * Lookup whether the shipment is to SC vs from here
	 * This is problematic with the variety of files we get and with 
	 * premises unregistered.
	 * @return true if movement from another state to Home State defined in settings, false if from SC to another
	 * state or from intrastate SC to SC.
	 */
	public boolean isInbound() {
		String sSourceState = getSourceState();
		boolean bInbound = false;
		if( sSourceState != null ) {
			bInbound = (!sHomeState.equalsIgnoreCase(sSourceState));
		}
		return bInbound;
	}
	
	public String getCompany() {
		return sCompany;
	}

	public String getOtherState() {
		String sSourceState = getSourceState();
		String sDestState = getDestState();
		if( sSourceState == null || sHomeState.equalsIgnoreCase(sSourceState) )
			return sDestState;
		else if( sDestState == null || sHomeState.equalsIgnoreCase(sDestState) )
			return sSourceState;
		else
			return null;
	}

	public String getOtherPin() {
		if( bRowInbound ) {
			return getSourcePin();
		}
		else {
			return getDestPin();
		}
	}

	public String getThisPin() {
		if( bRowInbound ) {
			return getDestPin();
		}
		else {
			return getSourcePin();
		}
	}
	
	// Tweak these getters to match source files.

	public String getSourceState() {
		String sRet = get( "SourceState".toUpperCase() );
		if( sRet == null ) sRet = get( "Source State".toUpperCase() );
		return sRet;
	}
	
	public String getSourcePin() {
		String sRet = get( "SourcePin".toUpperCase() );
		if( sRet == null ) sRet = get( "Source PIN".toUpperCase() );
		if( sRet == null ) sRet = get( "From PIN".toUpperCase() );
		if( sRet == null ) sRet = get( "FromPIN".toUpperCase() );
		if( sRet == null ) sRet = get( "Source Premises ID".toUpperCase() );
		if( sRet == null ) sRet = get( "Source Premise ID".toUpperCase() );
		if( sRet == null ) sRet = get( "Source Premises".toUpperCase() );
		if( sRet == null ) sRet = get( "Source Premise".toUpperCase() );
		return sRet;
	}
	
	public String getSourceFarm() {
		String sRet = get( "Source".toUpperCase() );
		if( sRet == null ) sRet = get( "SourceFarm".toUpperCase() );
		if( sRet == null ) sRet = get( "Source Farm".toUpperCase() );
		return sRet;
	}
	
	public String getDestFarm() {
		String sRet = get( "Destination".toUpperCase() );
		if( sRet == null ) sRet = get( "Destination Farm".toUpperCase() );
		if( sRet == null ) sRet = get( "DestFarm".toUpperCase() );
		if( sRet == null ) sRet = get( "To Farm".toUpperCase() );
		return sRet;
	}

	public String getDestPin() {
		String sRet = get( "DestPin".toUpperCase() );
		if( sRet == null ) sRet = get( "Dest PIN".toUpperCase() );
		if( sRet == null ) sRet = get( "ToPIN".toUpperCase() );
		if( sRet == null ) sRet = get( "To PIN".toUpperCase() );
		if( sRet == null ) sRet = get( "Dest Premises ID".toUpperCase() );
		if( sRet == null ) sRet = get( "Dest Premise ID".toUpperCase() );
		if( sRet == null ) sRet = get( "Dest Premises".toUpperCase() );
		if( sRet == null ) sRet = get( "Dest Premise".toUpperCase() );
		if( sRet == null ) sRet = get( "DestinationPIN".toUpperCase() );
		if( sRet == null ) sRet = get( "Destination PIN".toUpperCase() );
		if( sRet == null ) sRet = get( "Destination Premises ID".toUpperCase() );
		if( sRet == null ) sRet = get( "Destination Premise ID".toUpperCase() );
		if( sRet == null ) sRet = get( "Destination Premises".toUpperCase() );
		if( sRet == null ) sRet = get( "Destination Premise".toUpperCase() );
		return sRet;
	}
	
	public String getDestState() {
		String sRet = get( "DestState".toUpperCase() );
		if( sRet == null ) sRet = get( "Dest State".toUpperCase() );
		if( sRet == null ) sRet = get( "DestinationState".toUpperCase() );
		if( sRet == null ) sRet = get( "Destination State".toUpperCase() );
		return sRet;
	}

	public String getThisFarm() {
		String sRet = get( "DestFarm".toUpperCase() );
		if( sRet == null ) sRet = get( "Dest Farm".toUpperCase() );
		if( sRet == null ) sRet = get( "Destination".toUpperCase() );
		if( sRet == null ) sRet = get( "DestinationFarm".toUpperCase() );
		if( sRet == null ) sRet = get( "Destination Farm".toUpperCase() );
		return sRet;
	}
	
	public String getVet() {
		String sRet = get( "Vet".toUpperCase() );
		if( sRet == null ) sRet = get( "Veterinarian".toUpperCase() );
		return sRet;
	}

	public int getNumber() {
		String sNumber = get( "Number".toUpperCase() );
		if( sNumber == null ) sNumber = get( "# Head".toUpperCase() );
		int iNumber = -1;
		try {
		iNumber = Integer.parseInt(sNumber);
		}
		catch( NumberFormatException nfe ) {
			logger.error( "Cannot parse " + sNumber + " as an Integer" );
		}
		return iNumber;
	}

	public java.util.Date getDate() {
		String sDate = get( "Date".toUpperCase() );
		if( sDate == null ) sDate = get( "Date Moved".toUpperCase() );
		if( sDate == null ) sDate = get( "Move Date".toUpperCase() );
		if( sDate == null ) sDate = get( "Movement Date".toUpperCase() );
		java.util.Date dRet = null;
		String sYear = sDate.substring(sDate.lastIndexOf('/'));
		
		SimpleDateFormat df = new SimpleDateFormat( "M/d/yy");
		if( sYear.trim().length() > 2 ) df = new SimpleDateFormat( "M/d/yyyy");
		try {
			dRet = df.parse(sDate);
		} catch (ParseException e) {
			logger.error( "Cannot parse " + sDate + " as a date" );
		}
		return dRet;
	}


}// class CSVFile
