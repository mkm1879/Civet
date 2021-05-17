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
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.utils.LabeledCSVParser;
/**
 * Note: this class has remnants of the old primary key usages.  Should really edit out 
 * references to key.
 *
 */
public class ErrorTypeLookup {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private static HashMap<String, Error> keyErrorMap = null;
	private static HashMap<String, String> descriptionKeyMap = null;
	private static ArrayList<String> aErrorTypes = new ArrayList<String>();

	public ErrorTypeLookup() {
		if( keyErrorMap == null || descriptionKeyMap == null )
			readErrorTypeTable();
	}
	
	public static String getDescriptionForErrorKey( String sShortName ) {
		if( keyErrorMap == null || descriptionKeyMap == null )
			readErrorTypeTable();
		return keyErrorMap.get(sShortName).sDescription;
	}
	
	public static String getShortNameForDescription( String sDescription ) {
		if( keyErrorMap == null || descriptionKeyMap == null )
			readErrorTypeTable();
		return descriptionKeyMap.get(sDescription);
	}
	
	public ArrayList<String> listErrorTypes() {
		if( keyErrorMap == null || descriptionKeyMap == null )
			readErrorTypeTable();
		return aErrorTypes;
	}
	
	private static void readErrorTypeTable() {
		String sErrorTypeFile = CivetConfig.getErrorTypeTableFile();
		try {
			LabeledCSVParser parser = new LabeledCSVParser(sErrorTypeFile);
			parser.sort( parser.getLabelIdx("DisplaySequence"), true, false );
			List<String> line = parser.getNext();
			keyErrorMap = new HashMap<String, Error>();
			descriptionKeyMap = new HashMap<String, String>();

			while( line != null ) {
				 String sShortName = line.get( parser.getLabelIdx( "ShortName" ) );
				 String sDescription = line.get( parser.getLabelIdx( "Description" ) );
				 String sDisplaySequence = line.get( parser.getLabelIdx( "DisplaySequence" ) );
				 Integer iDisplaySequence = null;
				 try {
					 iDisplaySequence = Integer.parseInt(sDisplaySequence);
				 } catch( Exception e ) {
					 iDisplaySequence = 90;
				 }
				 Error error = new Error(sShortName, sDescription, iDisplaySequence);
				 keyErrorMap.put(sShortName, error);
				 descriptionKeyMap.put(sDescription, sShortName);
				 aErrorTypes.add(sDescription);
				 line = parser.getNext();
			}
		} catch (IOException e) {
			logger.error("Failed to read Vet Table", e);
		}
	}
	
	private static class Error {
		@SuppressWarnings("unused")
		public String sShortName;
		public String sDescription;
		@SuppressWarnings("unused")
		public int iDisplaySequence;
		
		public Error( String sShortName, String sDescription, int iDisplaySequence ) {
			this.sShortName = sShortName;
			this.sDescription = sDescription;
			this.iDisplaySequence = iDisplaySequence;
		}
	}

}

