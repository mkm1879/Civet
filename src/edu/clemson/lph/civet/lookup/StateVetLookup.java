package edu.clemson.lph.civet.lookup;
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

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetConfig;
import edu.clemson.lph.db.DBTableSource;
import edu.clemson.lph.utils.LabeledCSVParser;

public class StateVetLookup implements DBTableSource {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private static HashMap<String, StateVet> vetStateMap = null;
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
		lSearchColumns.add("FileType");
	}

	public StateVetLookup() {
		if( vetStateMap == null )
			readStateVetTable();
		vet = vetStateMap.get(CivetConfig.getHomeState());
	}

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
	
	public String getPrefix() {
		return vet.sPrefix;
	}
	
	public String getName() {
		String sFullName = vet.sPrefix + " " + vet.sFirstName + " " + vet.sLastName;
		return sFullName;
	}
	
	public String getFirstName() {
		return vet.sFirstName;
	}
	
	public String getLastName() {
		return vet.sLastName;
	}
	public String getAddress() {
		return vet.sAddress;
	}
	
	public String getCity() {
		return vet.sCity;
	}
	
	public String getStateCode() {
		return vet.sStateCode;
	}
	
	public String getState() {
		return vet.sState;
	}
	
	public String getZipCode() {
		return vet.sZipCode;
	}
	
	public String getEmail() {
		return vet.sEmail;
	}
	
	public String getCVIEmail() {
		return vet.sCVIEmail;
	}
	
	public String getFileType() {
		return vet.sFileType;
	}
	
	private void readStateVetTable() {
		synchronized( lSearchColumns ) {
			String sStateVetFile = CivetConfig.getStateVetTableFile();
			try {
				LabeledCSVParser parser = new LabeledCSVParser(sStateVetFile);
				parser.sort( parser.getLabelIdx("Name") );
				List<String> line = parser.getNext();
				vetStateMap = new HashMap<String, StateVet>();
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
					String sVetCVIEmail = line.get( parser.getLabelIdx( "CVIEmail" ) );
					String sVetFileType = line.get( parser.getLabelIdx( "FileType" ) );
					StateVet vet = new StateVet(sVetPrefix,sVetFirstName,sVetLastName,sVetAddress,sVetCity,
							sVetStateCode,sVetState,sVetZipCode,sVetEmail,sVetCVIEmail,sVetFileType);
					vetStateMap.put(sVetStateCode, vet);
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
					aRow.add(sVetFileType);
					lSearchRows.add(aRow);
					line = parser.getNext();
				}
			} catch (IOException e) {
				logger.error("Failed to read Vet Table", e);
			}
		}
	}
	
	private class StateVet {
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
		public String sFileType;
		
		public StateVet( String sVetPrefix, String sVetFirstName, String sVetLastName, 
				    String sVetAddress, String sVetCity, String sVetStateCode, String sVetState, 
				    String sVetZipCode, String sVetEmail, String sVetCVIEmail, String sFileType ) {
			this.sFirstName = sVetFirstName;
			this.sLastName = sVetLastName;
			this.sAddress = sVetAddress;
			this.sCity = sVetCity;
			this.sStateCode = sVetStateCode;
			this.sState = sVetState;
			this.sZipCode = sVetZipCode;
			this.sEmail = sVetEmail;
			this.sCVIEmail = sVetCVIEmail;
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

