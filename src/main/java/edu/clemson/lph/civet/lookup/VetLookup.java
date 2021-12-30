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
import java.util.TreeSet;

import javax.swing.event.TableModelListener;

import edu.clemson.lph.logging.Logger;


import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.webservice.UsaHerdsLookupVets;
import edu.clemson.lph.civet.webservice.WebServiceException;
import edu.clemson.lph.db.DBComboBoxModel;
import edu.clemson.lph.db.DBTableSource;
import edu.clemson.lph.utils.LabeledCSVParser;
import edu.clemson.lph.utils.StringUtils;

@SuppressWarnings("serial")
public class VetLookup extends DBComboBoxModel implements DBTableSource, javax.swing.table.TableModel  {
      private static Logger logger = Logger.getLogger();
	private static ArrayList<Vet> allRows = null;
	private static HashMap<String, Vet> vetNameMap = null;
	private static HashMap<String, Vet> vetNANMap = null;
	private static HashMap<Integer, Vet> vetKeyMap = null;
	private boolean bLevel2Check = false;
	private boolean bExpCheck = false;
	private ArrayList<String> lSearchColumns;
	private ArrayList<ArrayList<Object>> lSearchRows;
	
	// Filtered list of vets matching search criteria
	// Populates TableModel
	private ArrayList<Vet> rows;

	private Vet vet = null;
	/**
	 * Table Model member variables
	 */
	private ArrayList<TableModelListener> listeners = new ArrayList<TableModelListener>();

	/**
	 * Default constructor assumes existence of a VetTable in CivetConfig and will use that 
	 * for all lookups from this object.  Including its function as a DBComboBoxModel 
	 * and SearchTableSource based on iKey
	 */
	public VetLookup() {
		if( vetNameMap == null || vetNANMap == null || vetKeyMap == null )
			readVetTable();
		vet = vetKeyMap.get(1);			
	}

	public VetLookup( int iVetKey ) {
		if( vetNameMap == null || vetNANMap == null || vetKeyMap == null )
			readVetTable();
		vet = vetKeyMap.get(iVetKey);	
	}
	
	public VetLookup( String sNAN ) {
		if( vetNameMap == null || vetNANMap == null || vetKeyMap == null )
			readVetTable();
		vet = vetNANMap.get(sNAN);
	}
	
	public VetLookup(String sLastName, String sFirstName, 
			String sAddress, String sCity, String sStateCode, String sZipCode, 
			String sPhone, String sNan, String sLicNbr) {
		if( vetNameMap == null || vetNANMap == null || vetKeyMap == null )
			readVetTable();
		if( rows == null )
			rows = new ArrayList<Vet>();
		else
			rows.clear();
		for( Vet v : allRows ) {
			if( bMatch( v, sLastName, sFirstName, sAddress, sCity, sStateCode, sZipCode, 
						sPhone, sNan, sLicNbr) ) {
				rows.add( v );
			}
		}
	}
	
	public VetLookup(String sLastName, String sFirstName) {
		if( vetNameMap == null || vetNANMap == null || vetKeyMap == null )
			readVetTable();
		if( rows == null )
			rows = new ArrayList<Vet>();
		else
			rows.clear();
		for( Vet v : allRows ) {
			if( bMatch( v, sLastName, sFirstName, null, null, null, null, 
						null, null, null) ) {
				rows.add( v );
			}
		}
		if( rows != null && rows.size() > 0 )
			vet = rows.get(0);
		else
			vet = null;
	}

	private boolean bMatch( Vet v, String sLastName, String sFirstName, 
			String sAddress, String sCity, String sStateCode, String sZipCode, 
			String sPhone, String sNan, String sLicNbr ) {
		boolean bRet = true;
		if( sNan != null && sNan.indexOf('*') < 0 && sNan.indexOf('?') < 0 ) 
			sNan = lPadZeros( sNan, 6 );
		// Exact match on NAN or Lic should trump other search logic. Always include these
		if( v.sNAN != null && sNan != null && v.sNAN.equals(sNan) ) 
			return true;
		if( v.sLic != null && sLicNbr != null && v.sLic.equals(sLicNbr) ) 
			return true;
		// Otherwise any mismatch fails
		if( sLastName != null ) sLastName = sLastName.toLowerCase();
		if( sFirstName != null ) sFirstName = sFirstName.toLowerCase();
		if( sAddress != null ) sAddress = sAddress.toLowerCase();
		if( sCity != null ) sCity = sCity.toLowerCase();
		if( sStateCode != null ) sStateCode = sStateCode.toUpperCase();
//		if( v.sLastName != null && sLastName != null && !v.sLastName.toLowerCase().contains(sLastName) ) 
//			return false;
//		if( v.sFirstName != null && sFirstName != null && !v.sFirstName.toLowerCase().contains(sFirstName) ) 
//			return false;
//		if( v.sAddress != null && sAddress != null && !v.sAddress.toLowerCase().contains(sAddress) ) 
//			return false;
//		if( v.sCity != null && sCity != null && !v.sCity.toLowerCase().contains(sCity) ) 
//			return false;
//		if( v.sState != null && sStateCode != null && !v.sState.toUpperCase().contains(sStateCode) ) 
//			return false;
//		if( v.sZipCode != null && sZipCode != null && !StringUtils.wildCardMatches(v.sZipCode,sZipCode) ) 
//			return false;
//		if( !phoneMatch( v.sPhone, sPhone) ) 
//			return false;
//		if( v.sNAN != null && sNan != null && !StringUtils.wildCardMatches(v.sNAN, sNan) ) 
//			return false;
//		if( v.sLic != null && sLicNbr != null && !StringUtils.wildCardMatches(v.sLic, sLicNbr) ) 
//			return false;
		if( bNonMatch(v.sLastName.toLowerCase(), sLastName) ) 
			return false;
		if( bNonMatch(v.sFirstName.toLowerCase(), sFirstName) ) 
			return false;
		if( bNonMatch(v.sAddress.toLowerCase(), sAddress) ) 
			return false;
		if( bNonMatch(v.sCity.toLowerCase(), sCity) ) 
			return false;
		if( bNonMatch(v.sState.toUpperCase(), sStateCode) ) 
			return false;
		if( bNonMatch(v.sZipCode,sZipCode) ) 
			return false;
		// Do both part string, wildcard and truncated search
		if( !phoneMatch( v.sPhone, sPhone) && bNonMatch( v.sPhone, sPhone)  ) 
			return false;
		if( bNonMatch(v.sNAN, sNan) ) 
			return false;
		if( bNonMatch(v.sLic, sLicNbr) ) 
			return false;
		return bRet;
	}
	
	/**
	 * Only actual comparisons that do NOT match result in a true.  True means "do not include this record".
	 * @param sInput
	 * @param sPattern
	 * @return
	 */
	private boolean bNonMatch( String sInput, String sPattern ) {
		boolean bRet = false;
		if( sPattern == null || sPattern.trim().length() == 0 ) 
			return false;
		if( sInput == null || sInput.trim().length() == 0 )
			return true;
		if( sPattern.contains("*") || sPattern.contains("?") ) {
			if( !StringUtils.wildCardMatches(sInput, sPattern) )
				bRet = true;
		}
		else {
			if( !sInput.contains(sPattern) )
				bRet = true;
		}
		return bRet;
	}
	
	private String lPadZeros ( String sIn, int iLen ) {
		StringBuffer sb = new StringBuffer();
		for( int i = sIn.trim().length(); i < iLen; i++ ) {
			sb.append('0');
		}
		sb.append(sIn.trim());
		return sb.toString();
	}
	
	private boolean phoneMatch(String sPhone1, String sPhone2) {
		if( (sPhone1 == null || sPhone1.trim().length() == 0) && (sPhone2 == null || sPhone2.trim().length() == 0) ) return true;
		if( (sPhone1 == null || sPhone1.trim().length() == 0) && (sPhone2 != null && sPhone2.trim().length() > 0) ) return false;
		if( (sPhone1 != null && sPhone1.trim().length() > 0) && (sPhone2 == null || sPhone2.trim().length() == 0) ) return false;
		return digitsOnly( sPhone1 ).contains(digitsOnly( sPhone2 ));
	}
	
	private String digitsOnly( String sPhone ) {
		if( sPhone == null || sPhone.trim().length() == 0 )
			return null;
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < sPhone.length(); i++ ) {
			char cNext = sPhone.charAt(i);
			if( Character.isDigit(cNext) ) {
				sb.append(cNext);
			}
		}
		return sb.toString();
	}

	public static void generateLookupTable(String sFileName, boolean bAccredCheck) throws WebServiceException {
		UsaHerdsLookupVets me = new UsaHerdsLookupVets(bAccredCheck);
		me.generateLookupTable( sFileName );
	}	
	
	public void setLevel2Check( boolean bLevel2Check ) {
		this.bLevel2Check = bLevel2Check;
	}
	
	public boolean isLevel2Check() {
		return bLevel2Check;
	}
	
	public void setExpCheck( boolean bExpCheck ) {
		this.bExpCheck = bExpCheck;
	}
	
	public boolean isExpCheck() {
		return bExpCheck;
	}
	
	public boolean isUniqueMatch() {
		return (rows != null && rows.size() == 1 );
	}
	
	public int getKey() {
		if( vet == null ) return -1;
		return vet.iVetKey;
	}
	
	public int getKeyAt(int iModelIndex ) {
		Vet vet = rows.get(iModelIndex);
		return vet.iVetKey;
	}


	public String getVetFormattedName() {
		if( vet == null ) return null;
		return vet.sFormattedName;
	}
	
	public String getFormattedNameAt(int iModelIndex ) {
		Vet vet = rows.get(iModelIndex);
		return vet.sFormattedName;
	}
	
	public String getFormattedName() {
		if( vet == null ) return null;
		if( vet.sFormattedName != null && vet.sFormattedName.trim().length() > 0 )
			return vet.sFormattedName;
		else
			return vet.sLastName + ", " + vet.sFirstName;
	}
	
	public String getFirstName() {
		if( vet == null ) return null;
		return vet.sFirstName;
	}
	
	public String getLastName() {
		if( vet == null ) return null;
		return vet.sLastName;
	}
	
	public String getLicenseNo() {
		if( vet == null ) return null;
		return vet.sLic;
	}
	
	public String getNAN() {
		if( vet == null ) return null;
		return vet.sNAN;
	}
	
	public Integer getNANLevel() {
		if( vet == null ) return null;
		return vet.iNANLevel;
	}
	
	public String getNANStatus() {
		if( vet == null ) return null;
		return vet.sNANStatus;
	}
	
	public String getPhone() {
		if( vet == null ) return null;
		return vet.sPhone;
	}

	public String getPhoneDigits() {
		String sDigits = digitsOnly( getPhone() );
		if( sDigits != null && sDigits.length() > 10 )
			sDigits = sDigits.substring(0,10);
		return sDigits;
	}
	
	public String getAddress() {
		if( vet == null ) return null;
		return vet.sAddress;
	}
	
	public String getCity() {
		if( vet == null ) return null;
		return vet.sCity;
	}
	
	public String getState() {
		if( vet == null ) return null;
		return vet.sState;
	}
	
	public String getZipCode() {
		if( vet == null ) return null;
		return vet.sZipCode;
	}
	
	private synchronized void readVetTable() {
		if( vetNameMap == null || vetNANMap == null || vetKeyMap == null ) {
			String sVetFile = CivetConfig.getVetTableFile();
			try {
				LabeledCSVParser parser = new LabeledCSVParser(sVetFile);
				parser.sort( parser.getLabelIdx("Name") );
				List<String> line = parser.getNext();
				vetNameMap = new HashMap<String, Vet>();
				vetNANMap = new HashMap<String, Vet>();
				vetKeyMap = new HashMap<Integer, Vet>();
				allRows = new ArrayList<Vet>();
				lSearchRows = new ArrayList<ArrayList<Object>>();
				hValuesKeys.clear();
				hKeysValues.clear();
				super.removeAllElements();
				if( bBlank ) {
					super.addElement("");
					hValuesKeys.put("", -1);
					hKeysValues.put(-1, "");
				}

				while( line != null ) {
					String sVetKey = line.get( parser.getLabelIdx( "VetKey" ) );
					int iVetKey = Integer.parseInt(sVetKey);
					String sVetFormattedName = line.get( parser.getLabelIdx( "FormattedName" ) );
					String sVetFirstName = line.get( parser.getLabelIdx( "FirstName" ) );
					String sVetLastName = line.get( parser.getLabelIdx( "LastName" ) );
					String sVetAddress = line.get( parser.getLabelIdx( "Address" ) );
					String sVetCity = line.get( parser.getLabelIdx( "City" ) );
					String sVetState = line.get( parser.getLabelIdx( "State" ) );
					String sVetZip = line.get( parser.getLabelIdx( "ZipCode" ) );
					String sVetPhone = line.get( parser.getLabelIdx( "Phone" ) );
					String sVetLic = line.get( parser.getLabelIdx( "LicNo" ) );
					String sVetNAN = line.get( parser.getLabelIdx( "NAN" ) );
					if(sVetNAN != null && sVetNAN.trim().length() < 6 )
						sVetNAN = lPadZeros( sVetNAN, 6 );
					String sNANLevel = line.get( parser.getLabelIdx( "NANLevel") );
					int iNANLevel = 0;
					if( sNANLevel != null && sNANLevel.trim().length() > 0 ) {
						try {
							iNANLevel = Integer.parseInt(sNANLevel);
						} catch( NumberFormatException nfe ) {
							iNANLevel = 0;
						}
					}	
					String sNANStatus = line.get( parser.getLabelIdx( "NANStatus") );
					Vet vet = new Vet(iVetKey,sVetFormattedName, sVetFirstName,sVetLastName,sVetAddress,sVetCity,
							sVetState,sVetZip,sVetPhone, sVetLic,sVetNAN,iNANLevel,sNANStatus);
					String sVetFullName = sVetLastName + ", " + sVetFirstName;
					vetNameMap.put(sVetFullName, vet);
					vetNANMap.put(sVetNAN, vet);
					vetKeyMap.put(iVetKey, vet);
					allRows.add(vet);
					// Below can be deleted later.
					ArrayList<Object> aRow = new ArrayList<Object>();
					aRow.add(Integer.toString(iVetKey));
					aRow.add(sVetFirstName);
					aRow.add(sVetLastName);
					aRow.add(sVetAddress);
					aRow.add(sVetCity);
					aRow.add(sVetState);
					aRow.add(sVetZip);
					aRow.add(sVetPhone);
					aRow.add(sVetLic);
					aRow.add(sVetNAN);
					aRow.add(sNANLevel);
					lSearchRows.add(aRow);
					// To here
					line = parser.getNext();
				}
			} catch (IOException e) {
				logger.error(sVetFile + "\nFailed to read Vet Table", e);;
			}
		}
	}
	
	private void updateCbModel() {
		hValuesKeys.clear();
		hKeysValues.clear();
		super.removeAllElements();
		if( bBlank ) {
			super.addElement("");
			hValuesKeys.put("", -1);
			hKeysValues.put(-1, "");
		}
		TreeSet<String> setVetNames = new TreeSet<String>(vetNameMap.keySet());
		for( String sVetName : setVetNames ) {
			 int iVetKey = vetNameMap.get(sVetName).iVetKey;
			 int iNANLevel = vetNameMap.get(sVetName).iNANLevel;
			 if( CivetConfig.isCheckAccredStatus() ) {
				 String sNANStatus = vetNameMap.get(sVetName).sNANStatus;
				 if( bLevel2Check && !"Active".equalsIgnoreCase(sNANStatus) ) {
					 continue;
				 }
			 }
			 if( bLevel2Check && iNANLevel < 2 ) {
				 continue;
			 }
			 super.addElement(sVetName);
			 hValuesKeys.put(sVetName, iVetKey);
			 hKeysValues.put(iVetKey, sVetName);
		}
	}
	
	private static class Vet {
		public Integer iVetKey;
		public String sFormattedName;
		public String sFirstName;
		public String sLastName;
		public String sAddress;
		public String sCity;
		public String sState;
		public String sZipCode;
		public String sPhone;
		public String sLic;
		public String sNAN;
		public int iNANLevel;
		public String sNANStatus;
		
		public Vet( int iVetKey, String sFormattedName, String sVetFirstName, String sVetLastName, 
				    String sVetAddress, String sVetCity, String sVetState, 
				    String sVetZip, String sVetPhone, String sVetLic, 
				    String sVetNAN, int iVetNANLevel, String sNANStatus ) {
			this.iVetKey = iVetKey;
			this.sFormattedName = sFormattedName;
			this.sFirstName = sVetFirstName;
			this.sLastName = sVetLastName;
			this.sAddress = sVetAddress;
			this.sCity = sVetCity;
			this.sState = sVetState;
			this.sZipCode = sVetZip;
			this.sPhone = sVetPhone;
			this.sLic = sVetLic;
			this.sNAN = sVetNAN;
			this.iNANLevel = iVetNANLevel;
			this.sNANStatus = sNANStatus;
		}
	}
	
	@Override
	public void refresh() {
		readVetTable();
		updateCbModel();
	}

	@Override
	public ArrayList<String> getColumnNames() {
		if( lSearchColumns == null ) {
			lSearchColumns = new ArrayList<String>();
			lSearchColumns.add("VetKey");
			lSearchColumns.add("FirstName");
			lSearchColumns.add("LastName");
			lSearchColumns.add("PracticeName");
			lSearchColumns.add("Address");
			lSearchColumns.add("City");
			lSearchColumns.add("State");
			lSearchColumns.add("ZipCode");
			lSearchColumns.add("Phone");
			lSearchColumns.add("LicNo");
			lSearchColumns.add("NAN");
			lSearchColumns.add("NANLevel");
			lSearchColumns.add("NANExp");
		}
		return lSearchColumns;
	}

	@Override
	public ArrayList<ArrayList<Object>> getRows() {
		return lSearchRows;
	}
	
	
	public void clear() {
		rows.clear();
	}
	// TableModel Methods
	
	@Override
	public int getColumnCount() {
		return 9;
	}

	@Override
	public String getColumnName(int columnIndex) {
		String sRet = null;
		switch( columnIndex ) {
		case 0: sRet =  "VetKey"; break;
		case 1: sRet =  "LicNbr"; break;
		case 2: sRet =  "NAN #"; break;
		case 3: sRet =  "NAN Level"; break;
		case 4: sRet =  "LastName"; break;
		case 5: sRet =  "FirstName"; break;
		case 6: sRet =  "Address"; break;
		case 7: sRet =  "City"; break;
		case 8: sRet =  "State"; break;
		default: logger.error("Index out of bounds: " + columnIndex);
		}
		return sRet;
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object oRet = null;
		Vet row = rows.get(rowIndex);
		switch( columnIndex ) {
		case 0: oRet =  row.iVetKey; break;
		case 1: oRet =  row.sLic; break;
		case 2: oRet =  row.sNAN; break;
		case 3: oRet =  row.iNANLevel; break;
		case 4: oRet =  row.sLastName; break;
		case 5: oRet =  row.sFirstName; break;
		case 6: oRet =  row.sAddress; break;
		case 7: oRet =  row.sCity; break;
		case 8: oRet =  row.sState; break;
		default: logger.error("Index out of bounds: " + columnIndex);
		}
		return oRet;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Vet row = rows.get(rowIndex);
		switch( columnIndex ) {
		case 0:  row.iVetKey = (Integer)aValue; break;
		case 1:  row.sLic = (String)aValue; break;
		case 2:  row.sNAN = (String)aValue; break;
		case 3:  row.iNANLevel = (Integer)aValue; break;
		case 4:  row.sLastName = (String)aValue; break;
		case 5:  row.sFirstName = (String)aValue; break;
		case 6:  row.sAddress = (String)aValue; break;
		case 7:  row.sCity = (String)aValue; break;
		case 8:  row.sState = (String)aValue; break;
		default: logger.error("Index out of bounds: " + columnIndex);
		}
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}


}
