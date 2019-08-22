package edu.clemson.lph.civet.addons.swinehealthplans;
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
import edu.clemson.lph.civet.lookup.States;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.dialogs.MessageDialog;
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
	private CactusVets cactusVets = null;
	private SHPColumnMaps colMaps = null;

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
		colMaps = new SHPColumnMaps();
		cactusVets = new CactusVets();
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
			sCompany = "UN_";
		}
		// Exception case
		if( sFileName.toUpperCase().contains("SE HEALTH") ) {
			sCompany = "CACTUS";
		}
		if( sFileName.toUpperCase().contains("PURVIS") ) {
			sCompany = "PURVIS";
		}
		if( sCompany.equals("UN_") && sFileName.startsWith("IN") ) {
			sCompany="TDM";
		}
		if( sCompany.equals("Murphy Brown") ) {
			sCompany="MB";
		}
		if( sCompany.equals("Prestage") ) {
			sCompany="PR";
		}
		FileReader fr = new FileReader( f );
		CSVParserWrapper parser = new CSVParserWrapper(fr);
		for( String sField : parser.getHeader() ) {
			if(sField != null) {
				sField = sField.replaceAll("[^a-zA-Z0-9# ]", "");
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
	private String get( String sField ) {
		ArrayList<String> aKeys = colMaps.getMappedHeaders(sField.toUpperCase());
		if( aKeys == null ) {
			logger.error( "Column " + sField + " not found in SwineHealthPlanColumnMaps.csv");
			return null;
		}
		HashMap<String,String> aRow = aaValues.get(iCurrentRow);
		String sRet = null;
		for( String sKey : aKeys ) {
			sRet = aRow.get(sKey);
			if( sRet != null ) {
				if( sRet.trim().length() == 0 )
					sRet = null;
				else
					break;
			}
		}
		return sRet;
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
		String sRet = get( "SourceState" );
		if( sRet != null && sRet.trim().length() > 2 )
			sRet = States.getStateCode(sRet);
		return sRet;
	}
	
	public String getSourcePin() {
		String sRet = get( "SourcePin" );
		return sRet;
	}
	
	public String getSourceFarm() {
		String sRet = get( "SourceFarm" );
		return sRet;
	}
	
	public String getDestFarm() {
		String sRet = get( "DestFarm" );
		return sRet;
	}

	public String getDestPin() {
		String sRet = get( "DestPin" );
		return sRet;
	}
	
	public String getDestState() {
		String sRet = get( "DestState" );
		if( sRet != null && sRet.trim().length() > 2 )
			sRet = States.getStateCode(sRet);
		return sRet;
	}
	
	public String getVet() {
		String sRet = get( "Vet" );
		if( sRet == null && getCompany().equals("CACTUS") ) sRet = cactusVets.getVetNameForPin(getSourcePin());
		if( sRet == null && getCompany().equals("CACTUS") ) sRet = "Peter Schneider";
		if( sRet == null && getCompany().equals("CFF") ) sRet = "Peter Schneider";
		return sRet;
	}

	public int getNumber() {
		String sNumber = get( "Number" );
		int iNumber = -1;
		try {
		iNumber = Integer.parseInt(sNumber);
		}
		catch( NumberFormatException nfe ) {
			logger.error( "Cannot parse " + sNumber + " as an Integer" );
		}
		return iNumber;
	}
	
	public String getSex() {
		String sSexOut = "Gender Unknown";
		String sSexIn = get("Sex");
		if( sSexIn != null ) {
			if( "Gilts".equals(sSexIn) || "Sows".equals(sSexIn) )
				sSexOut = "Female";
			else if( "Mixed".equals(sSexIn) )
				sSexOut = "Other";
			else if( "Barrows".equals(sSexIn) || "Boars".equals(sSexIn) )
				sSexOut = "Male";
		}
		return sSexOut;
	}
	
	public String getAge() {
		String sAgeOut = null;
		String sAgeNum = null;
		String sAgeUnits = null;
		String sAgeIn = get("Age");
		if( sAgeIn != null ) {
			if( sAgeIn.toLowerCase().endsWith("days") )
				sAgeUnits = "d";
			else if( sAgeIn.toLowerCase().endsWith("weeks") )
				sAgeUnits = "wk";
			else if( sAgeIn.toLowerCase().endsWith("week") )
				sAgeUnits = "wk";
			else if( sAgeIn.toLowerCase().endsWith("months") )
				sAgeUnits = "mo";
			else if( sAgeIn.toLowerCase().endsWith("years") )
				sAgeUnits = "a";
			sAgeNum = sAgeIn.substring(0,sAgeIn.indexOf(' '));
			try {
				int iAgeNum = Integer.parseInt(sAgeNum);
				sAgeOut = convertAgeUnits( iAgeNum, sAgeUnits );
			} catch ( NumberFormatException nfe ) {
				logger.error("Invalid age: " + sAgeIn);
			}
		}
		return sAgeOut;
	}
	
	private String convertAgeUnits( int iAgeNum, String sAgeUnits ) {
		if( iAgeNum > 99 ) {
			if( "d".equals(sAgeUnits) ) {
				sAgeUnits = "mo";
				iAgeNum = iAgeNum / 30;
			}
			else if( "wk".equals(sAgeUnits) ) {
				sAgeUnits = "a";
				iAgeNum = iAgeNum / 52;
			}
			// yikes!  old pig
			else if( "mo".equals(sAgeUnits) ) {
				sAgeUnits = "a";
				iAgeNum = iAgeNum / 12;
			}
		}
		return iAgeNum + sAgeUnits;
	}

	public java.util.Date getDate() {
		String sDate = get( "Date" );
		java.util.Date dRet = null;
		if( sDate != null ) {
			int iLastSlash = sDate.lastIndexOf('/');
			if( iLastSlash >= 0 ) {
				String sYear = sDate.substring(iLastSlash);
		
				SimpleDateFormat df = new SimpleDateFormat( "M/d/yy");
				if( sYear.trim().length() > 2 ) df = new SimpleDateFormat( "M/d/yyyy");
				try {
					dRet = df.parse(sDate);
				} catch (ParseException e) {
					MessageDialog.showMessage( null, "Civet Error: Invalid Date", "Invalid date format " + sDate + " in data file");
					logger.error( "Cannot parse " + sDate + " as a date" );
				}
			}
		}
		return dRet;
	}


}// class CSVFile