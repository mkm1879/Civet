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

import edu.clemson.lph.logging.Logger;


import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.db.DBComboBoxModel;
import edu.clemson.lph.db.DBTableSource;
import edu.clemson.lph.utils.FileUtils;
import edu.clemson.lph.utils.LabeledCSVParser;
import edu.clemson.lph.utils.StringComparator;

/**
 * This class serves two distinct functions both based on the same value-set of species codes and names
 * First it is a data model for the custom DBComboBox that populates the Species choice
 * Second it is a general purpose code to name and name to code lookup that can be accessed statically.
 */
@SuppressWarnings("serial")
public class SpeciesLookup extends DBComboBoxModel implements DBTableSource {
      private static Logger logger = Logger.getLogger();
	private static HashMap<String, Spp> code2Spp;
	private static HashMap<String, Spp> name2Spp;
	private static SpeciesLookup me = null;
	// TODO: Once we dump the DB stuff entirely, get rid of temporary member variables and return from Spp directly.
	private ArrayList<String> lSearchColumns;
	private ArrayList<ArrayList<Object>> lSearchRows;

	/**
	 * Default constructor assumes existence of a SppTable in CivetConfig and will use that 
	 * for all lookups from this object. 
	 * 
	 */
	public SpeciesLookup() {
		if( code2Spp == null || name2Spp == null ) {
			readSppTable();		
//			for( String sCode : code2Spp.keySet() ) {
//				Spp species = code2Spp.get(sCode);
//				System.out.println(sCode + ": " + species.sSppName + ", " + species.bStd );
//			}
//			for( String sName : name2Spp.keySet() ) {
//				Spp species = name2Spp.get(sName);
//				System.out.println(sName + ": " + species.sSppCode + ", " + species.bStd );
//			}
		}
		me = this;
		
	}
	
	private void readSppTable() {
		StandardSppCodes stdSpp = new StandardSppCodes();
		code2Spp = new HashMap<String, Spp>();
		name2Spp = new HashMap<String, Spp>();
		String sSppFile = CivetConfig.getSppTableFile();
		try {
			// Clear the keys from parent even though ignored here.
			hValuesKeys.clear();
			hKeysValues.clear();
			hValuesCodes.clear();
			hCodesValues.clear();
			super.removeAllElements();
			// inherited from DBComboBoxModel need to add blank row if requested.
			if( bBlank ) {
				super.addElement("");
				hValuesKeys.put("", -1);
				hKeysValues.put(-1, "");
			}
			lSearchRows = new ArrayList<ArrayList<Object>>();
			LabeledCSVParser parser = new LabeledCSVParser(sSppFile);
// May want to sort by code rather than string
//			parser.sort( parser.getLabelIdx("USDACode") );
			parser.sort( parser.getLabelIdx("Description") );
			List<String> line = parser.getNext();
			// Walk through the file we got from HERDS and check each to see if it is official
			while( line != null ) {
				boolean bStd = false;
				boolean bHerds = true;
				 String sSppCode = line.get( parser.getLabelIdx( "USDACode" ) );
				 if( sSppCode == null || sSppCode.trim().length() == 0 ) {  // No code, 
					 line = parser.getNext();
					 continue;
					 // don't bother with species with no code
				 }
				 String sSppName = line.get( parser.getLabelIdx( "Description" ) );
				 String sStdName = line.get( parser.getLabelIdx( "USDADescription" ) );
				 if( stdSpp.getStdCodes().contains(sSppCode) ) {
					 bStd = true;
					 sStdName = stdSpp.getNameForCode(sSppCode);
				 }
				 // NOW construct an Spp record for the map that includes standard code or other code flag.
				 Spp spp = new Spp(sSppCode, sSppName, bStd, bHerds);
				 code2Spp.put(sSppCode, spp);
				 name2Spp.put(sSppName, spp);
				 name2Spp.put(sStdName, spp);
				 // These two Hashmaps are used by GUI components.
				 hValuesCodes.put(sSppName, sSppCode);
				 hCodesValues.put(sSppCode, sSppName);
				 // These rows allow searching on either HERDS description or standard name (or USDA Desc if non standard).
				 addSearchRow(sSppCode, sSppName, sStdName );
				 line = parser.getNext();
			}
			// Now add the codes that ARE in the standard but aren't in HERDS
			StringBuffer sbBadSpp = new StringBuffer();
			sbBadSpp.append("Species codes in Standard but not USAHERDS:\n");
			for( String sCode : stdSpp.getStdCodes() ) {
				Spp spp = code2Spp.get(sCode);
				if( spp == null) {
					String sSppName = stdSpp.getNameForCode(sCode);
					spp = new Spp(sCode, sSppName, true, false);
					code2Spp.put(sCode, spp);
					name2Spp.put(sSppName, spp);
					 // These two Hashmaps are used by GUI components.
					 hValuesCodes.put(sSppName, sCode);
					 hCodesValues.put(sCode, sSppName);
					addSearchRow(sCode, sSppName, sSppName );
					sbBadSpp.append(sCode);
					sbBadSpp.append('\n');
				}
				FileUtils.writeTextFile(sbBadSpp.toString(), "BadSpp.txt", false);
			}
			// Populate combo box with sorted names
			ArrayList<String> aNames = new ArrayList<String>();
			for( String sName : hValuesCodes.keySet() ) 
				aNames.add(sName);
			aNames.sort( new StringComparator() );
			for( String sName : aNames )
				super.addElement(sName);

		} catch (IOException e) {
			logger.error("Failed to read Species Table", e);
		}
	}
	
	private void addSearchRow( String code, String name, String usda ) {
		 ArrayList<Object> aRow = new ArrayList<Object>();
		 aRow.add(code);
		 aRow.add(name);
		 aRow.add(usda);
		 lSearchRows.add(aRow);
	}
	
	public static String getNameForCode( String sSppCode ) {
		if( me == null ) me = new SpeciesLookup();
		Spp spp = code2Spp.get(sSppCode);
		if( spp != null ) return spp.sSppName;
		else return null;
	}
	
	public static String getCodeForName( String sSppName ) {
		if( me == null ) me = new SpeciesLookup();
		Spp spp = name2Spp.get(sSppName);
		if( spp != null ) return spp.sSppCode;
		else return null;
	}
	
	public static boolean isCodeStandard( String sSppCode ) {
		boolean bRet = false;
		if( me == null ) me = new SpeciesLookup();
		Spp spp = code2Spp.get(sSppCode);
		if( spp != null ) bRet = spp.bStd;
		return bRet;
	}
	
	public static boolean isCodeInHerds( String sSppCode ) {
		boolean bRet = false;
		if( me == null ) me = new SpeciesLookup();
		Spp spp = code2Spp.get(sSppCode);
		if( spp != null ) bRet = spp.bHerds;
		return bRet;
	}
	
	private static class Spp {
		public String sSppCode;
		public String sSppName;
		public boolean bStd;
		public boolean bHerds;
		
		public Spp( String sSppCode, String sSppName, boolean bStd, boolean bHerds ) {
			this.sSppCode = sSppCode;
			this.sSppName = sSppName;
			this.bStd = bStd;
			this.bHerds = bHerds;
		}
	}
	
	@Override
	public void refresh() {
		readSppTable();
	}

	@Override
	public ArrayList<String> getColumnNames() {
		if( lSearchColumns == null ) {
			lSearchColumns = new ArrayList<String>();
			lSearchColumns.add("SpeciesCode");
			lSearchColumns.add("SpeciesName");
			lSearchColumns.add("SpeciesUSDAName");
		}
		return lSearchColumns;
	}

	@Override
	public ArrayList<ArrayList<Object>> getRows() {
		return lSearchRows;
	}
	

}

