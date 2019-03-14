package edu.clemson.lph.civet.lookup;
import java.io.FileNotFoundException;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.DataFormatException;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.db.DBTableSource;
import edu.clemson.lph.utils.CSVWriter;
import edu.clemson.lph.utils.LabeledCSVParser;
import edu.clemson.lph.utils.StringComparator;

public class StateVetLookup implements DBTableSource {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private static HashMap<String, StateVet> vetStateMap = null;
	private static HashMap<String, String> stateStateCodeMap = null;
	private static ArrayList<String> lSearchColumns = null;
	private static ArrayList<ArrayList<Object>> lSearchRows;
	private StateVet vet = null;
	
	static {
		lSearchColumns = new ArrayList<String>();
		lSearchColumns.add("Prefix");
		lSearchColumns.add("FirstName");
		lSearchColumns.add("LastName");
		lSearchColumns.add("Address");
		lSearchColumns.add("City");
		lSearchColumns.add("StateCode");
		lSearchColumns.add("State");
		lSearchColumns.add("ZipCode");
		lSearchColumns.add("Email");
		lSearchColumns.add("CVIEmail");
		lSearchColumns.add("CVIErrorEmail");
		lSearchColumns.add("FileType");
	}

	public StateVetLookup() {
		if( vetStateMap == null )
			readStateVetTable();
		vet = vetStateMap.get(CivetConfig.getHomeState());
	}
//	
//	public static void main( String sArgs[] ) {
//		PropertyConfigurator.configure("CivetConfig.txt");
//		// Fail now so config file and required files can be fixed before work is done.
//		CivetConfig.checkAllConfig();
//		StateVetLookup lu = new StateVetLookup( "TN" );
//		System.out.println(lu.getFileType() );
//	}

	public StateVetLookup( String sStateCode ) {
		if( vetStateMap == null )
			readStateVetTable();
		if( sStateCode == null ) return;
		// Check to see if we have a code or a state name look up code if name.
		if( sStateCode.trim().length() > 2 ) {
			sStateCode = States.getStateCode(sStateCode);
		}
		vet = vetStateMap.get(sStateCode);
	}
	
	public static String getCodeForState( String sState ) {
		String sStateCode = stateStateCodeMap.get(sState);
		return sStateCode;
	}
	
//	public StateVet getStateVet( String sState ) {
//		String sStateCode = stateStateCodeMap.get(sState);
//		StateVet vet = vetStateMap.get(sStateCode);
//		return vet;
//	}
	
	public String getPrefix() {
		if( vet == null ) return null;
		return vet.sPrefix;
	}
	
	public String getName() {
		if( vet == null ) return null;
		String sFullName = vet.sPrefix + " " + vet.sFirstName + " " + vet.sLastName;
		return sFullName;
	}
	
	public String getFirstName() {
		if( vet == null ) return null;
		return vet.sFirstName;
	}
	
	public String getLastName() {
		if( vet == null ) return null;
		return vet.sLastName;
	}
	public String getAddress() {
		if( vet == null ) return null;
		return vet.sAddress;
	}
	
	public String getCity() {
		if( vet == null ) return null;
		return vet.sCity;
	}
	
	public String getStateCode() {
		if( vet == null ) return null;
		return vet.sStateCode;
	}
	
	public String getState() {
		if( vet == null ) return null;
		return vet.sState;
	}
	
	public String getZipCode() {
		if( vet == null ) return null;
		return vet.sZipCode;
	}
	
	public String getEmail() {
		if( vet == null ) return null;
		return vet.sEmail;
	}
	
	public String getCVIEmail() {
		if( vet == null ) return null;
		String sRet = null;
		sRet = vet.sCVIEmail;
		if( sRet == null || sRet.trim().length() == 0 )
			sRet = vet.sEmail;
		return sRet;
	}
	
	public String getCVIErrorEmail() {
		if( vet == null ) return null;
		String sRet = vet.sCVIErrorEmail;
		if( sRet == null || sRet.trim().length() == 0 ) 
			sRet = vet.sCVIEmail;
		if( sRet == null || sRet.trim().length() == 0 )
			sRet = vet.sEmail;
		return sRet;
	}
	
	public String getFileType() {
		if( vet == null ) return null;
		return vet.sFileType;
	}
	
	public ArrayList<String> listStates() {
		ArrayList<String> aStates = new ArrayList<String>();
		for( String sKey : vetStateMap.keySet() ) {
			StateVet v = vetStateMap.get(sKey);
			if( v != null ) {
				aStates.add(v.sState );
			}
		}
		aStates.sort(new StringComparator());
		return aStates;
	}
	
	
	public void setPrefix(String sPrefix) {
		if( vet == null ) return;
		vet.sPrefix = sPrefix;
	}
	
	public void setFirstName(String sFirstName) {
		if( vet == null ) return;
		vet.sFirstName = sFirstName;
	}
	
	public void setLastName(String sLastName) {
		if( vet == null ) return;
		vet.sLastName = sLastName;
	}
	public void setAddress(String sAddress) {
		if( vet == null ) return;
		vet.sAddress = sAddress;
	}
	
	public void setCity(String sCity) {
		if( vet == null ) return;
		vet.sCity = sCity;
	}
	
	public void setStateCode(String sStateCode) {
		if( vet == null ) return;
		vet.sStateCode = sStateCode;
	}
	
	public void setState(String sState) {
		if( vet == null ) return;
		vet.sState = sState;
	}
	
	public void setZipCode(String sZipCode) {
		if( vet == null ) return;
		vet.sZipCode = sZipCode;
	}
	
	public void setEmail(String sEmail) {
		if( vet == null ) return;
		vet.sEmail = sEmail;
	}
	
	public void setCVIEmail(String sCVIEmail) {
		if( vet == null ) return;
		if( sCVIEmail == null || (sCVIEmail.trim().length() == 0) )
			 vet.sCVIEmail = "";
		else
			vet.sCVIEmail = sCVIEmail;
	}
	
	public void setCVIErrorEmail(String sCVIErrorEmail) {
		if( vet == null ) return;
		if( sCVIErrorEmail == null || (sCVIErrorEmail.trim().length() == 0) )
			 vet.sCVIErrorEmail = "";
		else
			vet.sCVIErrorEmail = sCVIErrorEmail;
	}
	
	public void setFileType(String sFileType) {
		if( vet == null ) return;
		vet.sFileType = sFileType;
	}
	
	public void doSave() {
		writeStateVetTable();
	}
	
	private void readStateVetTable() {
		synchronized( lSearchColumns ) {  // Why?
			String sStateVetFile = CivetConfig.getStateVetTableFile();
			try {
				LabeledCSVParser parser = new LabeledCSVParser(sStateVetFile);
				parser.sort( parser.getLabelIdx("Name") );
				List<String> line = parser.getNext();
				vetStateMap = new HashMap<String, StateVet>();
				stateStateCodeMap = new HashMap<String, String>();
				lSearchRows = new ArrayList<ArrayList<Object>>();

				while( line != null ) {
					String sVetPrefix = line.get( parser.getLabelIdx( "Prefix" ) );
					String sVetFirstName = line.get( parser.getLabelIdx( "FName" ) );
					String sVetLastName = line.get( parser.getLabelIdx( "LName" ) );
					String sVetAddress = line.get( parser.getLabelIdx( "MailAddress" ) );
					String sVetCity = line.get( parser.getLabelIdx( "Mail City" ) );
					String sVetStateCode = line.get( parser.getLabelIdx( "MailState" ) );
					String sVetState = line.get( parser.getLabelIdx( "State" ) );
					String sVetZipCode = line.get( parser.getLabelIdx( "MailZip" ) );
					String sVetEmail = line.get( parser.getLabelIdx( "Email" ) );
					sVetEmail = sVetEmail.replace(';', ',');
					String sVetCVIEmail = line.get( parser.getLabelIdx( "CVIEmail" ) );
					sVetCVIEmail = sVetCVIEmail.replace(';', ',');
					String sVetCVIErrorEmail = line.get( parser.getLabelIdx( "CVIErrorEmail" ) );
					sVetCVIErrorEmail = sVetCVIErrorEmail.replace(';', ',');
					String sVetFileType = line.get( parser.getLabelIdx( "FileType" ) );
					StateVet vet = new StateVet(sVetPrefix,sVetFirstName,sVetLastName,sVetAddress,sVetCity,
							sVetStateCode,sVetState,sVetZipCode,sVetEmail,sVetCVIEmail,sVetCVIErrorEmail,sVetFileType);
					vetStateMap.put(sVetStateCode, vet);
					stateStateCodeMap.put(sVetState,sVetStateCode);
					ArrayList<Object> aRow = new ArrayList<Object>();
					aRow.add(sVetPrefix);
					aRow.add(sVetFirstName);
					aRow.add(sVetLastName);
					aRow.add(sVetAddress);
					aRow.add(sVetCity);
					aRow.add(sVetStateCode);
					aRow.add(sVetState);
					aRow.add(sVetZipCode);
					aRow.add(sVetEmail);
					aRow.add(sVetCVIEmail);
					aRow.add(sVetCVIErrorEmail);
					aRow.add(sVetFileType);
					lSearchRows.add(aRow);
					line = parser.getNext();
				}
			} catch (IOException e) {
				logger.error("Failed to read Vet Table", e);
			}
		}
	}
	
	private void writeStateVetTable() {
		synchronized( lSearchColumns ) {
			updateSearchRows();
			CSVWriter w = new CSVWriter();
			try {
				w.setHeader(new String[] {"Prefix","FName","LName","MailAddress","Mail City","MailState","State","MailZip",
							"Email","CVIEmail","CVIErrorEmail","FileType"} );
				for( ArrayList<Object> aRow  : lSearchRows ) {
					w.addRow(aRow);
				}
				w.write("StateVetTable");
			} catch (DataFormatException e) {
				logger.error("Failed to format header", e);
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				logger.error("Failed write file", e);
				e.printStackTrace();
			}
			
		}
	}
	
	private void updateSearchRows() {
		lSearchRows.clear();
		for( String sState : vetStateMap.keySet() ) {
			StateVet vet = vetStateMap.get(sState);
			ArrayList<Object> aRow = new ArrayList<Object>();
			aRow.add(vet.sPrefix);
			aRow.add(vet.sFirstName);
			aRow.add(vet.sLastName);
			aRow.add(vet.sAddress);
			aRow.add(vet.sCity);
			aRow.add(vet.sStateCode);
			aRow.add(vet.sState);
			aRow.add(vet.sZipCode);
			aRow.add(vet.sEmail);
			aRow.add(vet.sCVIEmail);
			aRow.add(vet.sCVIErrorEmail);
			aRow.add(vet.sFileType);
			lSearchRows.sort(new java.util.Comparator<ArrayList<Object>> () {
				@Override
				public int compare(ArrayList<Object> arg0, ArrayList<Object> arg1) {
					if( arg0 == null && arg1 == null ) return 0;
					if( arg0 != null && arg1 == null ) return -1;
					if( arg0 == null && arg1 != null ) return 1;
					if( arg0 == null || arg1 == null ) return 0;
					
					String sState0 = (String)arg0.get(5);
					String sState1 = (String)arg1.get(5);
					
					if( sState0 == null && sState1 == null ) return 0;
					if( ( sState0 == null || sState0.trim().length() == 0 ) && ( sState1 == null || sState1.trim().length() == 0 ) ) return 0;
					if( ( sState0 != null && sState0.trim().length() > 0 ) && ( sState1 == null || sState1.trim().length() == 0 ) ) return -1;
					if( ( sState1 != null && sState1.trim().length() > 0 ) && ( sState0 == null || sState0.trim().length() == 0 ) ) return 1;
					if( sState0 == null || sState1 == null ) return 0;
					return sState0.compareTo(sState1);
				}
			});
			lSearchRows.add(aRow);
		}
		
	}
	
	private static class StateVet {
		public String sPrefix;
		public String sFirstName;
		public String sLastName;
		public String sAddress;
		public String sCity;
		public String sStateCode;
		public String sState;
		public String sZipCode;
		public String sEmail;
		public String sCVIEmail;
		public String sCVIErrorEmail;
		public String sFileType;
		
		public StateVet( String sVetPrefix, String sVetFirstName, String sVetLastName, 
				    String sVetAddress, String sVetCity, String sVetStateCode, String sVetState, 
				    String sVetZipCode, String sVetEmail, String sVetCVIEmail, String sVetCVIErrorEmail, String sFileType ) {
			this.sPrefix = sVetPrefix;
			this.sFirstName = sVetFirstName;
			this.sLastName = sVetLastName;
			this.sAddress = sVetAddress;
			this.sCity = sVetCity;
			this.sStateCode = sVetStateCode;
			this.sState = sVetState;
			this.sZipCode = sVetZipCode;
			this.sEmail = sVetEmail;
			this.sCVIEmail = sVetCVIEmail;
			this.sCVIErrorEmail = sVetCVIErrorEmail;
			this.sFileType = sFileType;
		}
	}

	@Override
	public ArrayList<String> getColumnNames() {
		return lSearchColumns;
	}

	@Override
	public ArrayList<ArrayList<Object>> getRows() {
		return lSearchRows;
	}


}

