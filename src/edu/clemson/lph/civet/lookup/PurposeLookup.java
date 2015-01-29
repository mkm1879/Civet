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
import edu.clemson.lph.utils.LabeledCSVParser;

@SuppressWarnings("serial")
public class PurposeLookup extends DBComboBoxModel implements DBTableSource {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private HashMap<String, Purpose> purposeNameMap = null;
	private HashMap<Integer, Purpose> purposeKeyMap = null;
	private Purpose purpose = null;
	private ArrayList<String> lSearchColumns;
	private ArrayList<ArrayList<Object>> lSearchRows;

	/**
	 * Default constructor assumes existence of a PurposeTable in CivetConfig and will use that 
	 * for all lookups from this object.  Including its function as a DBComboBoxModel based on iKey
	 */
	public PurposeLookup() {
		if( purposeNameMap == null || purposeKeyMap == null )
			readPurposeTable();		
	}
	
	public PurposeLookup( int iPurposeKey ) {
		if( purposeNameMap == null || purposeKeyMap == null )
			readPurposeTable();
		purpose = purposeKeyMap.get(iPurposeKey);
	}
	
	public PurposeLookup( String sPurposeName ) {
		if( purposeNameMap == null || purposeKeyMap == null )
			readPurposeTable();
		purpose = purposeNameMap.get(sPurposeName);
	}
	
	public Integer getKey() {
		return purpose.iPurposeKey;
	}
	
	public String getUSAHACode() {
		return purpose.sUSAHACode;
	}

	public String getPurposeName() {
		return purpose.sPurposeName;
	}
	
	public Integer getDisplaySequence() {
		return purpose.iDisplaySequence;
	}
	
	private void readPurposeTable() {
		String sPurposeFile = CivetConfig.getPurposeTableFile();
		try {
			LabeledCSVParser parser = new LabeledCSVParser(sPurposeFile);
			// This is a numeric, non-unique sort
			parser.sort( parser.getLabelIdx("DisplaySequence"), true, false );
			purposeNameMap = new HashMap<String, Purpose>();
			purposeKeyMap = new HashMap<Integer, Purpose>();
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
				 String sPurposeKey = line.get( parser.getLabelIdx( "CVIPurposeTypeKey" ) );
				 int iPurposeKey = Integer.parseInt(sPurposeKey);
				 String sUSAHACode = line.get( parser.getLabelIdx( "USAHACode" ) );
				 String sPurposeName = line.get( parser.getLabelIdx( "Description" ) );
				 String sDisplaySequence = line.get( parser.getLabelIdx( "DisplaySequence" ) );
				 int iDisplaySequence = Integer.parseInt(sDisplaySequence);
				 Purpose purpose = new Purpose(iPurposeKey, sUSAHACode, sPurposeName, iDisplaySequence);
				 purposeNameMap.put(sPurposeName, purpose);
				 purposeKeyMap.put(iPurposeKey, purpose);
				 super.addElement(sPurposeName);
				 hValuesKeys.put(sPurposeName, iPurposeKey);
				 hKeysValues.put(iPurposeKey, sPurposeName);
				 ArrayList<Object> aRow = new ArrayList<Object>();
				 aRow.add(Integer.toString(iPurposeKey));
				 aRow.add(sPurposeName);
				 aRow.add(iDisplaySequence);
				 lSearchRows.add(aRow);
				 line = parser.getNext();
			}
		} catch (IOException e) {
			logger.error(sPurposeFile + "\nFailed to read Purpose Table", e);
		}
	}
	
	private class Purpose {
		public Integer iPurposeKey;
		public String sUSAHACode;
		public String sPurposeName;
		public Integer iDisplaySequence;
		
		public Purpose( int iPurposeKey, String sUSAHACode, String sPurposeName, int iDisplaySequence ) {
			this.iPurposeKey = iPurposeKey;
			this.sUSAHACode = sUSAHACode;
			this.sPurposeName = sPurposeName;
			this.iDisplaySequence = iDisplaySequence;
		}
	}
	
	@Override
	public void refresh() {
		readPurposeTable();
	}

	@Override
	public ArrayList<String> getColumnNames() {
		if( lSearchColumns == null ) {
			lSearchColumns = new ArrayList<String>();
			lSearchColumns.add("PurposeKey");
			lSearchColumns.add("PurposeCode");
			lSearchColumns.add("PurposeName");
		}
		return lSearchColumns;
	}

	@Override
	public ArrayList<ArrayList<Object>> getRows() {
		return lSearchRows;
	}

}

