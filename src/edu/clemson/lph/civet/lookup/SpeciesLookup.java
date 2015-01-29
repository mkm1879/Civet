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
import edu.clemson.lph.db.DBComboBoxModel;
import edu.clemson.lph.db.DBTableSource;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.utils.LabeledCSVParser;

@SuppressWarnings("serial")
public class SpeciesLookup extends DBComboBoxModel implements DBTableSource {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private Integer iSpeciesKey = -1;
	private HashMap<String, Spp> sppNameMap = null;
	private HashMap<String, Spp> sppCodeMap = null;
	private HashMap<Integer, Spp> sppKeyMap = null;
	// TODO: Once we dump the DB stuff entirely, get rid of temporary member variables and return from Spp directly.
	private String sSpeciesCode = null;
	private String sSpeciesName = null;
	private ArrayList<String> lSearchColumns;
	private ArrayList<ArrayList<Object>> lSearchRows;

	/**
	 * Default constructor assumes existence of a SppTable in CivetConfig and will use that 
	 * for all lookups from this object.  Including its function as a DBComboBoxModel based on iKey
	 */
	public SpeciesLookup() {
		if( sppCodeMap == null || sppKeyMap == null )
			readSppTable();		
	}
	
	public SpeciesLookup( String sSppCode ) {
		if( sppCodeMap == null || sppKeyMap == null )
			readSppTable();
		Spp spp = sppCodeMap.get(sSppCode);
		if( spp == null ) {
			MessageDialog.messageLater(null, "Civet Error: Missing Species Code", "Species code " + sSppCode + " not found in lookup table.");
			this.iSpeciesKey = -1;
			this.sSpeciesName = "Missing Species Code";
			this.sSpeciesCode = sSppCode;
			return;
		}
		this.iSpeciesKey = spp.iSppKey;
		this.sSpeciesName = spp.sSppName;
		this.sSpeciesCode = spp.sSppCode;
	}
	
	public SpeciesLookup( int iSppKey ) {
		if( sppCodeMap == null || sppKeyMap == null )
			readSppTable();
		Spp spp = sppKeyMap.get(iSppKey);
		if( spp == null ) {
			MessageDialog.messageLater(null, "Civet Error: Missing Species Key", "Species key " + iSppKey + " not found in lookup table.");
			this.iSpeciesKey = iSppKey;
			this.sSpeciesName = "Missing Species Code";
			this.sSpeciesCode = "ERROR";
			return;
		}
		this.iSpeciesKey = spp.iSppKey;
		this.sSpeciesName = spp.sSppName;
		this.sSpeciesCode = spp.sSppCode;
	}
	
	public Integer getKey() {
		return iSpeciesKey;
	}

	public String getSpeciesName() {
		return sSpeciesName;
	}
	
	public String getSpeciesCode() {
		return sSpeciesCode;
	}
		
	private void readSppTable() {
		String sVetFile = CivetConfig.getSppTableFile();
		try {
			LabeledCSVParser parser = new LabeledCSVParser(sVetFile);
			parser.sort( parser.getLabelIdx("USDACode") );
			sppNameMap = new HashMap<String, Spp>();
			sppCodeMap = new HashMap<String, Spp>();
			sppKeyMap = new HashMap<Integer, Spp>();
			lSearchRows = new ArrayList<ArrayList<Object>>();
			hValuesKeys.clear();
			hKeysValues.clear();
			super.removeAllElements();
			if( bBlank ) {
				super.addElement("");
				hValuesKeys.put("", -1);
				hKeysValues.put(-1, "");
			}
			List<String> line = parser.getNext();
			while( line != null ) {
				 String sSppKey = line.get( parser.getLabelIdx( "AnimalClassHierarchyKey" ) );
				 int iSppKey = Integer.parseInt(sSppKey);
				 String sSppCode = line.get( parser.getLabelIdx( "USDACode" ) );
				 if( sSppCode == null || sSppCode.trim().length() == 0 ) {
					 line = parser.getNext();
					 continue;
				 }
				 String sSppName = line.get( parser.getLabelIdx( "USDADescription" ) );
				 Spp spp = new Spp(iSppKey, sSppCode, sSppName);
				 if( sppNameMap.get(sSppName) == null ) {
					 sppNameMap.put(sSppName, spp);
					 sppKeyMap.put(iSppKey, spp);
					 sppCodeMap.put(sSppCode, spp);
					 super.addElement(sSppName);
					 hValuesKeys.put(sSppName, iSppKey);
					 hKeysValues.put(iSppKey, sSppName);
					 hValuesCodes.put(sSppName,  sSppCode);
					 ArrayList<Object> aRow = new ArrayList<Object>();
					 aRow.add(Integer.toString(iSppKey));
					 aRow.add(sSppName);
					 lSearchRows.add(aRow);
				 }
				 line = parser.getNext();
			}
		} catch (IOException e) {
			logger.error("Failed to read Species Table", e);
		}
	}
	
	private class Spp {
		public Integer iSppKey;
		public String sSppCode;
		public String sSppName;
		
		public Spp( int iSppKey, String sSppCode, String sSppName ) {
			this.iSppKey = iSppKey;
			this.sSppCode = sSppCode;
			this.sSppName = sSppName;
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
			lSearchColumns.add("AnimalClassHierarchyKey");
			lSearchColumns.add("SpeciesCode");
			lSearchColumns.add("SpeciesName");
		}
		return lSearchColumns;
	}

	@Override
	public ArrayList<ArrayList<Object>> getRows() {
		return lSearchRows;
	}

}

