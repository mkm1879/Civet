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
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetInbox;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.db.DBComboBoxModel;
import edu.clemson.lph.db.DBTableSource;
import edu.clemson.lph.utils.FileUtils;
import edu.clemson.lph.utils.LabeledCSVParser;

/**
 * This class serves two distinct functions both based on the same value-set of species codes and names
 * First it is a data model for the custom DBComboBox that populates the Species choice
 * Second it is a general purpose code to name and name to code lookup that can be accessed statically.
 */
@SuppressWarnings("serial")
public class SpeciesLookup extends DBComboBoxModel implements DBTableSource {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private static ArrayList<String> stdCodes;
	private static HashMap<String, String> code2text;
	private static HashMap<String, String> text2code;
	private static HashMap<String, Spp> sppCodeMap = null;
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
		if( sppCodeMap == null )
			readSppTable();		
		me = this;
	}
	
	public static void main( String sArgs[] ) {
		PropertyConfigurator.configure("CivetConfig.txt");
		// Fail now so config file and required files can be fixed before work is done.
		CivetConfig.checkAllConfig();
		logger.setLevel(CivetConfig.getLogLevel());
		CivetInbox.VERSION = "Test Inbox Controller";
		logger.info("Civet running build: " + CivetInbox.VERSION);
//		SpeciesLookup lu = new SpeciesLookup();
		me = new SpeciesLookup();
		Set<String> keys = text2code.keySet();
		for( String s : keys ) {
			String code = text2code.get(s);
			Spp spp = sppCodeMap.get(code);
			System.out.println( s + " : " + spp.sSppCode + " : " + spp.sSppName + " : " + spp.bStd ); //SpeciesLookup.isCodeStandard(spp.sSppCode) );
		}
	}
	
	
	private void readSppTable() {
		sppCodeMap = new HashMap<String, Spp>();
		String sSppFile = CivetConfig.getSppTableFile();
		try {
			populateStdCodes();
			LabeledCSVParser parser = new LabeledCSVParser(sSppFile);
//			parser.sort( parser.getLabelIdx("USDACode") );
			parser.sort( parser.getLabelIdx("Description") );
			lSearchRows = new ArrayList<ArrayList<Object>>();
			// Clear the keys from parent even though ignored here.
			hValuesKeys.clear();
			hKeysValues.clear();
			hValuesCodes.clear();
			hCodesValues.clear();
			super.removeAllElements();
			if( bBlank ) {
				super.addElement("");
				hValuesKeys.put("", -1);
				hKeysValues.put(-1, "");
			}
			List<String> line = parser.getNext();
			// Walk through the file we got from HERDS and check each to see if it is official
			while( line != null ) {
				boolean bStd = false;
				 String sSppCode = line.get( parser.getLabelIdx( "USDACode" ) );
				 if( sSppCode == null || sSppCode.trim().length() == 0 ) {  // No code, 
					 sSppCode = "OTH";
				 }
				 if( sSppCode.trim().length() > 0 && stdCodes.contains(sSppCode) ) {
					 bStd = true;
				 }
				 String sSppName = line.get( parser.getLabelIdx( "Description" ) );
				 // Add code/name pairs from HERDS
				 if( code2text.get(sSppCode) == null ) {
					 text2code.put(sSppName, sSppCode);
					 code2text.put(sSppCode, sSppName);
				 }
				 // Map all the entries with no code to OTH but OTH will return OTHER when run the other way.
				 else if( text2code.get(sSppName) == null && sSppCode.equalsIgnoreCase("OTH") ) {
					 text2code.put(sSppName, sSppCode);					 
				 }
				 else {
					 // Use the standard name if it exists
					 sSppName = code2text.get(sSppCode);
				 }
				 // NOW construct an Spp record for the map that includes standard code or other code flag.
				 Spp spp = new Spp(sSppCode, sSppName, bStd);
				 sppCodeMap.put(sSppCode, spp);
				 super.addElement(sSppName);
				 // These two Hashmaps are used by GUI components.
				 hValuesCodes.put(sSppName, sSppCode);
				 hCodesValues.put(sSppCode, sSppName);
				 // These rows allow searching on either HERDS description or standard name.
				 addSearchRow(sSppCode, sSppName, line.get( parser.getLabelIdx( "USDADescription" ) ) );
				 line = parser.getNext();
			}
			// Now add the codes that ARE in the standard but aren't in HERDS
			StringBuffer sbBadSpp = new StringBuffer();
			for( String sCode : stdCodes ) {
				Spp spp = sppCodeMap.get(sCode);
				if( spp == null) {
					spp = new Spp(sCode, getNameForCode(sCode), true);
					sppCodeMap.put(sCode, spp);
					addSearchRow(sCode, getNameForCode(sCode), getNameForCode(sCode) );
					sbBadSpp.append(sCode);
					sbBadSpp.append('\n');
				}
				FileUtils.writeTextFile(sbBadSpp.toString(), "BadSpp.txt", false);
			}
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
		return code2text.get(sSppCode);
	}
	
	public static String getCodeForName( String sSppName ) {
		if( me == null ) me = new SpeciesLookup();
		return text2code.get(sSppName);
	}
	
	public static boolean isCodeStandard( String sSppCode ) {
		boolean bRet = false;
		if( me == null ) me = new SpeciesLookup();
		Spp spp = sppCodeMap.get(sSppCode);
		if( spp != null ) bRet = spp.bStd;
		return bRet;
	}
	
	private static class Spp {
		public String sSppCode;
		public String sSppName;
		public boolean bStd;
		
		public Spp( String sSppCode, String sSppName, boolean bStd ) {
			this.sSppCode = sSppCode;
			this.sSppName = sSppName;
			this.bStd = bStd;
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
	
	private static void populateStdCodes() {
		text2code = new HashMap<String, String>();
		code2text = new HashMap<String, String>();
		stdCodes = new ArrayList<String>();
		
        stdCodes.add( "AQU" );
        stdCodes.add( "BEF" );
        stdCodes.add( "BIS" );
        stdCodes.add( "BOV" );
        stdCodes.add( "CAM" );
        stdCodes.add( "CAN" );
        stdCodes.add( "CAP" );
        stdCodes.add( "CER" );
        stdCodes.add( "CHI" );
        stdCodes.add( "DAI" );
        stdCodes.add( "EQU" );
        stdCodes.add( "FEL" );
        stdCodes.add( "OVI" );
        stdCodes.add( "POR" );
        stdCodes.add( "TUR" );

        text2code.put( "Aquaculture", "AQU" );
        text2code.put( "Beef", "BEF" );
        text2code.put( "Bison", "BIS" );
        text2code.put( "Bovine (Bison and Cattle) DEPRECATED", "BOV" );
        text2code.put( "Camelid (Alpacas, Llamas, etc.)", "CAM" );
        text2code.put( "Canine", "CAN" );
        text2code.put( "Caprine (Goats)", "CAP" );
        text2code.put( "Cervids", "CER" );
        text2code.put( "Chickens", "CHI" );
        text2code.put( "Dairy", "DAI" );
        text2code.put( "Equine (Horses, Mules, Donkeys, Burros)", "EQU" );
        text2code.put( "Feline", "FEL" );
        text2code.put( "Ovine (Sheep)", "OVI" );
        text2code.put( "Porcine (Swine)", "POR" );
        text2code.put( "Turkeys", "TUR" );

        code2text.put( "AQU", "Aquaculture" );
        code2text.put( "BEF", "Beef" );
        code2text.put( "BIS", "Bison" );
        code2text.put( "BOV", "Bovine (Bison and Cattle) DEPRECATED" );
        code2text.put( "CAM", "Camelid (Alpacas, Llamas, etc.)" );
        code2text.put( "CAN", "Canine" );
        code2text.put( "CAP", "Caprine (Goats)" );
        code2text.put( "CER", "Cervids" );
        code2text.put( "CHI", "Chickens" );
        code2text.put( "DAI", "Dairy" );
        code2text.put( "EQU", "Equine (Horses, Mules, Donkeys, Burros)" );
        code2text.put( "FEL", "Feline" );
        code2text.put( "OVI", "Ovine (Sheep)" );
        code2text.put( "POR", "Porcine (Swine)" );
        code2text.put( "TUR", "Turkeys" );
	}

}

