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

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetConfig;
import edu.clemson.lph.db.DBComboBoxModel;
import edu.clemson.lph.db.DBTableSource;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.utils.LabeledCSVParser;

@SuppressWarnings("serial")
public class SpeciesLookup extends DBComboBoxModel implements DBTableSource {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private HashMap<String, Spp> sppNameMap = null;
	private HashMap<String, Spp> sppCodeMap = null;
	// TODO: Once we dump the DB stuff entirely, get rid of temporary member variables and return from Spp directly.
	private String sSpeciesCode = null;
	private String sSpeciesName = null;
	private ArrayList<String> lSearchColumns;
	private ArrayList<ArrayList<Object>> lSearchRows;
	private HashMap<String, String> hStdSpecies = null;

	/**
	 * Default constructor assumes existence of a SppTable in CivetConfig and will use that 
	 * for all lookups from this object.  Including its function as a DBComboBoxModel based on iKey
	 */
	public SpeciesLookup() {
		if( sppCodeMap == null || sppCodeMap == null )
			readSppTable();		
	}
	
	public SpeciesLookup( String sSppCode ) {
		if( sppCodeMap == null || sppCodeMap == null )
			readSppTable();
		Spp spp = sppCodeMap.get(sSppCode);
		if( spp == null ) {
			MessageDialog.messageLater(null, "Civet Error: Missing Species Code", "Species code " + sSppCode + " not found in lookup table.");
			this.sSpeciesName = "Missing Species Code";
			this.sSpeciesCode = sSppCode;
			return;
		}
		this.sSpeciesName = spp.sSppName;
		this.sSpeciesCode = spp.sSppCode;
	}
	
	// Force an overload of another string constructor
	public SpeciesLookup( String sSpeciesValue, boolean bByName ) {
		if( sppCodeMap == null || sppNameMap == null )
			readSppTable();
		Spp spp = sppNameMap.get(sSpeciesValue);
		if( spp == null ) {
			MessageDialog.messageLater(null, "Civet Error: Missing Species Value", "Species name " + sSpeciesValue + " not found in lookup table.");
			this.sSpeciesName = "Missing Species Name";
			this.sSpeciesCode = "ERROR";
			return;
		}
		this.sSpeciesName = sSpeciesValue;
		this.sSpeciesCode = spp.sSppCode;
	}
	
	public Integer getKey() {
		return -1;
	}

	public String getSpeciesName() {
		return sSpeciesName;
	}
	
	public String getSpeciesCode() {
		return sSpeciesCode;
	}
		
	private void readSppTable() {
		String sVetFile = CivetConfig.getSppTableFile();
		populateStdMap();
		try {
			LabeledCSVParser parser = new LabeledCSVParser(sVetFile);
			parser.sort( parser.getLabelIdx("USDACode") );
			sppNameMap = new HashMap<String, Spp>();
			sppCodeMap = new HashMap<String, Spp>();
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
			Set<String> sStdSpp = hStdSpecies.keySet();
			while( line != null ) {
				 String sSppCode = line.get( parser.getLabelIdx( "USDACode" ) );
				 if( sSppCode == null || sSppCode.trim().length() == 0 || !sStdSpp.contains(sSppCode) ) {
					 line = parser.getNext();
					 continue;
				 }
				 String sSppName = line.get( parser.getLabelIdx( "Description" ) );
				 Spp spp = new Spp(sSppCode, sSppName);
				 if( sppNameMap.get(sSppName) == null ) {
					 sppNameMap.put(sSppName, spp);
					 sppCodeMap.put(sSppCode, spp);
					 super.addElement(sSppName);
					 hValuesCodes.put(sSppName, sSppCode);
					 hCodesValues.put(sSppCode, sSppName);
					 ArrayList<Object> aRow = new ArrayList<Object>();
					 aRow.add(sSppCode);
					 aRow.add(sSppName);
					 String sSppUSDAName = line.get( parser.getLabelIdx( "USDADescription" ) );
					 aRow.add(sSppUSDAName);
					 lSearchRows.add(aRow);
				 }
				 line = parser.getNext();
			}
		} catch (IOException e) {
			logger.error("Failed to read Species Table", e);
		}
	}
	
	private class Spp {
		public String sSppCode;
		public String sSppName;
		
		public Spp( String sSppCode, String sSppName ) {
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
			lSearchColumns.add("SpeciesUSDAName");
		}
		return lSearchColumns;
	}

	@Override
	public ArrayList<ArrayList<Object>> getRows() {
		return lSearchRows;
	}
	
	private void populateStdMap() {
		if( hStdSpecies == null ) {
			hStdSpecies = new HashMap<String, String>();
			hStdSpecies.put("AQU", "Aquaculture");
			hStdSpecies.put("AVI", "Avian");
			hStdSpecies.put("BEF", "Beef");
			hStdSpecies.put("BIS", "Bison");
			hStdSpecies.put("BOV", "Bovine (Bison and Cattle)");
			hStdSpecies.put("CAM", "Camelid (Alpacas, Llamas, etc.)");
			hStdSpecies.put("CAN", "Canine");
			hStdSpecies.put("CAP", "Caprine (Goats)");
			hStdSpecies.put("CER", "Cervids");
			hStdSpecies.put("CHI", "Chickens");
			hStdSpecies.put("CLM", "Clams");
			hStdSpecies.put("CRA", "Crawfish");
			hStdSpecies.put("CTF", "Catfish");
			hStdSpecies.put("DAI", "Dairy");
			hStdSpecies.put("DEE", "Deer");
			hStdSpecies.put("DUC", "Ducks");
			hStdSpecies.put("ELK", "Elk");
			hStdSpecies.put("EQU", "Equine (Horses, Mules, Donkeys, Burros)");
			hStdSpecies.put("FEL", "Feline");
			hStdSpecies.put("GEE", "Geese");
			hStdSpecies.put("GUI", "Guineas");
			hStdSpecies.put("MSL", "Mussels");
			hStdSpecies.put("OTH", "Other");
			hStdSpecies.put("OVI", "Ovine (Sheep)");
			hStdSpecies.put("OYS", "Oysters");
			hStdSpecies.put("PGN", "Pigeon");
			hStdSpecies.put("POR", "Porcine (Swine)");
			hStdSpecies.put("QUA", "Quail");
			hStdSpecies.put("RTT", "Ratites (Emus, Ostriches, etc.)");
			hStdSpecies.put("SAL", "Salmon");
			hStdSpecies.put("SBA", "Striped Bass");
			hStdSpecies.put("SHR", "Shrimp");
			hStdSpecies.put("SLP", "Scallops");
			hStdSpecies.put("TIL", "Tilapia");
			hStdSpecies.put("TRO", "Trout");
			hStdSpecies.put("TUR", "Turkeys");
		}
	}

}

