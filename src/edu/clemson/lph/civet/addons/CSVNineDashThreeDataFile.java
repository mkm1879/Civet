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
import edu.clemson.lph.civet.CivetConfig;
import edu.clemson.lph.utils.LabeledCSVParser;

public class CSVNineDashThreeDataFile {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private ArrayList<HashMap<String,String>> aaValues = null;
	private int iCurrentRow;
	private int iNumRows;
	private String sHomeState;
	private Date dSaved;
	private final static int MAX_COLS = 21;

	/**
	 * Abstraction of a CSV File with bulk load records.  First row is headers that must match one
	 * of the cases in the various getters.  
	 * @param sFileName Fully qualified filename for the CSV file
	 * @throws FileNotFoundException If the file is not found
	 * @throws IOException If the file cannot be parsed as a CSV file
	 */
	public CSVNineDashThreeDataFile( String sFileName ) throws FileNotFoundException, IOException {
		sHomeState = CivetConfig.getHomeStateAbbr();
		ArrayList<String> aKeys = new ArrayList<String>();
		aaValues = new ArrayList<HashMap<String,String>>();
		File f = new File( sFileName );
		dSaved = new java.util.Date( f.lastModified() );

		FileReader fr = new FileReader( f );
		LabeledCSVParser parser = new LabeledCSVParser(fr);
		int iField = 0;
		for( String sField : parser.getLabels() ) {
			if( iField++ > MAX_COLS ) break;
			if(sField != null) {
				sField = sField.toUpperCase();
				aKeys.add(sField);
			}
		}
		iNumRows = 0;
		int iLine = 0;
		List<String> aLine = parser.getNext();
		while( aLine != null ) {
			iLine++;
			if( aLine.size() < aKeys.size() ) {
				System.err.println("Line " + iLine + " does not contain the right number of fields");
				logger.error("Line " + iLine + " does not contain the right number of fields");
				return;
			}
			HashMap<String,String> rowData = new HashMap<String,String>();
			boolean bSomeData = false;
			iField = 0;
			for( String sVal : aLine ) {
				if( iField > MAX_COLS ) break;				
				if( sVal.trim().length() > 0 ) bSomeData = true;
				rowData.put(aKeys.get(iField++), sVal);
			}
			if( bSomeData ) {
//				for( String sKey : aKeys )
//					System.out.print(rowData.get(sKey.toUpperCase())+",");
//				System.out.println();
				aaValues.add(rowData);
				iNumRows++;
			}
			aLine = parser.getNext();
		}
		iCurrentRow = -1;
		fr.close();
	}

	/**
	 * Read the next row of data if any
	 * @return true if current pointer is on valid data
	 */
	public boolean nextRow() {
		++iCurrentRow;
		return iCurrentRow < iNumRows;
	}
	
	public int size() {
		return aaValues.size();
	}
	
	public String printRow() {
		StringBuffer sb = new StringBuffer();
		HashMap<String,String> aRow = aaValues.get(iCurrentRow);
		for( String sKey : aRow.keySet() ) {
			String sVal = aRow.get(sKey.toUpperCase());
			if( sVal == null ) sVal = "";
			sb.append( sKey + "=\"" + sVal + "\",");
		}
		return sb.toString();
	}

	/**
	 * return the string value of cell at current row with header sKey
	 * @param sKey
	 * @return
	 */
	public String get( String sKey ) {
		HashMap<String,String> aRow = aaValues.get(iCurrentRow);
		String sRet = aRow.get(sKey.toUpperCase());
		if( sRet != null && sRet.trim().length() == 0 ) sRet = null;
		return sRet;
	}

	/**
	 * Lookup whether the shipment is to SC vs from here
	 * This is problematic with the variety of files we get and with 
	 * premises unregistered.
	 * @return true if movement from another state to Home State defined in settings, false if from SC to another
	 * state or from intrastate SC to SC.
	 */
	public boolean isInbound() {
		String sSourceState = getConsignorState();
		boolean bInbound = false;
		// Blank consignor state is consistent with SC premises
		if( sSourceState != null ) {
			bInbound = (!sHomeState.equalsIgnoreCase(sSourceState));
		}
		return bInbound;
	}
	
	public String getCVINumber() {
		String sCVINumber = get("CVINumber");
		return sCVINumber;
	}
	public java.util.Date getInspectionDate() {
		String sDate = get( "InspectionDate".toUpperCase() );
		java.util.Date dRet = null;
		if( sDate == null || sDate.trim().length() == 0 ) return dRet;
		
		SimpleDateFormat df = new SimpleDateFormat( "MM/dd/yy");
		SimpleDateFormat df2 = new SimpleDateFormat( "MM/dd/yyyy");
		try {
			dRet = df.parse(sDate);
		} catch (ParseException e) {
			try {
				dRet = df2.parse(sDate);
			} catch (ParseException e2) {
				logger.error( "Cannot parse " + sDate + " as a date" );
			}
		}
		return dRet;
	}
	
	public java.util.Date getSavedDate() {
		return dSaved;
	}
	
	public String getConsigneePIN() {
		return get("ConsigneePIN");
	}
	public String getConsigneeBusiness() {
		return get("ConsigneeBusiness");
	}
	public String getConsigneeName() {
		return get("ConsigneeName");
	}
	public String getConsigneeStreet() {
		return get("ConsigneeStreet");
	}
	public String getConsigneeCity() {
		return get("ConsigneeCity");
	}
	public String getConsigneeState() {
		String sState = get("ConsigneeState");
		if( sState != null && sState.trim().length() == 2 )
			return sState.trim().toUpperCase();
		else
			return null;
	}
	public String getConsigneeCountry() {
		return get("ConsigneeCountry");
	}
	public String getConsigneeZip() {
		return formatZip( get("ConsigneeZip") );
	}
	public String getConsignorPIN() {
		return get("ConsignorPIN");
	}
	public String getConsignorBusiness() {
		return get("ConsignorBusiness");
	}
	public String getConsignorName() {
		return get("ConsignorName");
	}
	public String getConsignorStreet() {
		return get("ConsignorStreet");
	}
	public String getConsignorCity() {
		return get("ConsignorCity");
	}
	public String getConsignorState() {
		String sState = get("ConsignorState");
		if( sState != null && sState.trim().length() == 2 )
			return sState.trim().toUpperCase();
		else
			return null;
	}
	public String getConsignorZip() {
		return formatZip( get("ConsignorZip") );
	}
	public String getConsignorCountry() {
		return get("ConsignorCountry");
	}
	public Integer getAnimalCount() {
		Integer iRet = null;
		String sAnimalCount = get("AnimalCount");
		try {
			iRet = Integer.parseInt(sAnimalCount);
		} catch( NumberFormatException nfe ) {
			iRet = null;
		}
		return iRet;
	}
	public String getSpecies() {
		return get("Species");
	}
	public String getProduct() {
		return get("Product");
	}
	
	public boolean hasTags() {
		String sTags = get("TagID");
		if( sTags != null && sTags.trim().length() > 0 )
			return true;
		else
			return false;	
	}
	
	public List<String> listTagIds() {
		ArrayList<String> tagList;
		String sTags = get("TagID");
		if( sTags != null && sTags.trim().length() > 0 ) {
			tagList = new ArrayList<String>();
			StringTokenizer tok = new StringTokenizer(sTags, ",");
			while( tok.hasMoreTokens() ) {
				String sTag = tok.nextToken();
				tagList.add(sTag.trim());
			}
		}
		else {
			tagList = null;
		}
		return tagList;
	}

	private static String formatZip( String sZipIn ) {
		String sRet = sZipIn;
		if( sZipIn == null || sZipIn.trim().length() == 0 ) return sRet;
		while( !Character.isDigit(sRet.charAt(0)) ) {
			sRet = sRet.substring(1);
		}
		while( sRet.trim().length() < 5 ) {
			sRet = '0' + sRet;
		}
		return sRet;
	}
}// class CSVFile
