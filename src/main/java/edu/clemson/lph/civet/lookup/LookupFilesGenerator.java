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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import edu.clemson.lph.logging.Logger;


import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.webservice.UsaHerdsLookupVets;
import edu.clemson.lph.civet.webservice.UsaHerdsWebServiceLookup;
import edu.clemson.lph.civet.webservice.WebServiceException;
import edu.clemson.lph.utils.CSVWriter;

public class LookupFilesGenerator {
      private static Logger logger = Logger.getLogger();
	private static boolean bNewThisSession = false;

	public LookupFilesGenerator() {
	}
	
	public void generateAllLookups() throws WebServiceException {
		generateAllLookups( (java.awt.Window)null );
	}
		
	public synchronized void generateAllLookups(java.awt.Window parent) {
		try {
			new CertificateNbrLookup();  // Build Set from File
			generateSpeciesFile();
			generateErrorTypesFile();
			generateVetTableFile();
			bNewThisSession = true;
		} catch ( WebServiceException e) {
			logger.error("Error in generateAllLookups thread", e);
		}
	}
	
	public static synchronized boolean checkLookupFiles() {
		boolean bRet = true;
		String sName = CivetConfig.getSppTableFile();
		File f = new File( sName );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "Species lookup table: " + sName + " does not exist or is not a file");
			bRet = false;
		}
		sName = CivetConfig.getErrorTypeTableFile();
		f = new File( sName );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "Error type lookup table: " + sName + " does not exist or is not a file");
			bRet = false;
		}
		sName = CivetConfig.getStateVetTableFile();
		f = new File( sName );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "State vet lookup table: " + sName + " does not exist or is not a file");
			bRet = false;
		}
		return bRet;
	}
	
	public static boolean isNewThisSession() {
		return bNewThisSession;
	}
	
	/**
	 * This takes as long to run as just downloading the files does!
	 * @param sFilePath
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean isCurrent( String sFilePath ) {
		File f = new File( sFilePath );
		long lModTime = f.lastModified();
		long lNow = new java.util.Date().getTime();
		long lMSecsOld = lNow - lModTime;
		// TODO add configuration for how long to age lookup tables.
		return lMSecsOld < 1000 * 60 * 60;
	}
	
	public void generateSpeciesFile() throws WebServiceException {
//		if( isCurrent( CivetConfig.getSppTableFile() ) ) return;
		UsaHerdsWebServiceLookup speciesLookup = new UsaHerdsWebServiceLookup(UsaHerdsWebServiceLookup.LOOKUP_SPECIES);
		String sName = CivetConfig.getSppTableFile();
		ArrayList<String> aColNames = new ArrayList<String>();
		aColNames.add("AnimalClassHierarchyKey");
		aColNames.add("Description");
		aColNames.add("USDADescription");
		aColNames.add("USDACode");
		generateSpeciesLookup( sName, speciesLookup, aColNames );
		
	}
	

	public void generateErrorTypesFile() throws WebServiceException {
//		if( isCurrent( CivetConfig.getErrorTypeTableFile() ) ) return;
		UsaHerdsWebServiceLookup lookup = new UsaHerdsWebServiceLookup(UsaHerdsWebServiceLookup.LOOKUP_ERRORS);
		String sName = CivetConfig.getErrorTypeTableFile();
		ArrayList<String> aColNames = new ArrayList<String>();
		aColNames.add("ShortName");
		aColNames.add("Description");
		aColNames.add("DisplaySequence");
		CSVWriter writer = new CSVWriter();
		try {
			writer.setHeader(aColNames);
			while( lookup.next() ) {
				ArrayList<Object> aValues = new ArrayList<Object>();
				aValues.add(lookup.getShortName());
				aValues.add(lookup.getDescription());
				aValues.add(lookup.getDisplaySequence());
				if(aColNames.size() == 4)
					aValues.add(lookup.getMappedValue());
				for( int i = 4; i < aColNames.size(); i++ ) 
					aValues.add("");
				writer.addRow(aValues);
			}
			writer.write(sName);
		} catch (FileNotFoundException e) {
			logger.error("Could not find output file " + sName + ".csv", e);
		} catch (DataFormatException e) {
			logger.error("Rows returned by query not equal size", e);;
		} 
	}

	
	// NOTE: StateVetTable generator pulls directly from USAHERDS using SC logic and db connection
	// As such, it has been moved to the AddOns package in StateVetLookupGenerator class.  This 
	// AddOn will be called in SC and the resulting CSV file distributed to user states.
	
	public void generateVetTableFile() throws WebServiceException {
//		if( isCurrent( CivetConfig.getVetTableFile() ) ) return;
		// For now pull all vets.  Later base on a CivetConfig value.
		UsaHerdsLookupVets vets = new UsaHerdsLookupVets(false);
		vets.generateLookupTable(CivetConfig.getVetTableFile());
	}
	
	
	private void generateSpeciesLookup( String sName, UsaHerdsWebServiceLookup lookup, ArrayList<String> aColNames ) {
		CSVWriter writer = new CSVWriter();
		HashMap<String, Integer> hCodeKeys = new HashMap<String, Integer>();
		try {
			writer.setHeader(aColNames);
			while( lookup.next() ) {
				String sCode = lookup.getMappedValue();
				Integer iKey = lookup.getKeyValue();
				Integer iLastKey = hCodeKeys.get(sCode);
				if( iLastKey == null || iLastKey > iKey ) {
					hCodeKeys.put(sCode, iKey);
				}
			}
			lookup.reset();
			while( lookup.next() ) {
				String sCode = lookup.getMappedValue();
				Integer iKey = lookup.getKeyValue();
				Integer iFirstKey = hCodeKeys.get(sCode);
				if( iKey.equals( iFirstKey ) ) {
					ArrayList<Object> aValues = new ArrayList<Object>();
					aValues.add(lookup.getKeyValue());
					aValues.add(lookup.getDescription());
					aValues.add(lookup.getUSDADescription());
					if(aColNames.size() == 4)
						aValues.add(lookup.getMappedValue());
					for( int i = 4; i < aColNames.size(); i++ ) 
						aValues.add("");
					writer.addRow(aValues);
				}

			}
			writer.write(sName);
		} catch (FileNotFoundException e) {
			logger.error("Could not find output file " + sName + ".csv", e);
		} catch (DataFormatException e) {
			logger.error("Rows returned by query not equal size", e);;
		} 
	}
}
