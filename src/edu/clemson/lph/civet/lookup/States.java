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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import edu.clemson.lph.db.DBComboBoxModel;
import edu.clemson.lph.db.DBTableSource;

@SuppressWarnings("serial")
public class States extends DBComboBoxModel implements DBTableSource {
	private static HashMap<String, String> states = new HashMap<String, String>();
	private static ArrayList<String> colNames = new ArrayList<String>();
	private static ArrayList<ArrayList<Object>> rows = new ArrayList<ArrayList<Object>>();
	

	public States() {
	}
	
	static {
		states.put("AL", "ALABAMA");
		states.put("AK", "ALASKA");
		states.put("AZ", "ARIZONA");
		states.put("AR", "ARKANSAS");
		states.put("CA", "CALIFORNIA");
		states.put("CO", "COLORADO");
		states.put("CT", "CONNECTICUT");
		states.put("DE", "DELAWARE");
		states.put("DC", "DISTRICT OF COLUMBIA");
		states.put("FL", "FLORIDA");
		states.put("GA", "GEORGIA");
		states.put("GU", "GUAM");
		states.put("HI", "HAWAII");
		states.put("ID", "IDAHO");
		states.put("IL", "ILLINOIS");
		states.put("IN", "INDIANA");
		states.put("IA", "IOWA");
		states.put("KS", "KANSAS");
		states.put("KY", "KENTUCKY");
		states.put("LA", "LOUISIANA");
		states.put("ME", "MAINE");
		states.put("MD", "MARYLAND");
		states.put("MA", "MASSACHUSETTS");
		states.put("MI", "MICHIGAN");
		states.put("MN", "MINNESOTA");
		states.put("MS", "MISSISSIPPI");
		states.put("MO", "MISSOURI");
		states.put("MT", "MONTANA");
		states.put("NE", "NEBRASKA");
		states.put("NV", "NEVADA");
		states.put("NH", "NEW HAMPSHIRE");
		states.put("NJ", "NEW JERSEY");
		states.put("NM", "NEW MEXICO");
		states.put("NY", "NEW YORK");
		states.put("NC", "NORTH CAROLINA");
		states.put("ND", "NORTH DAKOTA");
		states.put("OH", "OHIO");
		states.put("OK", "OKLAHOMA");
		states.put("OR", "OREGON");
		states.put("PW", "PALAU");
		states.put("PA", "PENNSYLVANIA");
		states.put("PR", "PUERTO RICO");
		states.put("RI", "RHODE ISLAND");
		states.put("SC", "SOUTH CAROLINA");
		states.put("SD", "SOUTH DAKOTA");
		states.put("TN", "TENNESSEE");
		states.put("TX", "TEXAS");
		states.put("UT", "UTAH");
		states.put("VT", "VERMONT");
		states.put("VI", "VIRGIN ISLANDS OF THE U.S.");
		states.put("VA", "VIRGINIA");
		states.put("WA", "WASHINGTON");
		states.put("WV", "WEST VIRGINIA");
		states.put("WI", "WISCONSIN");
		states.put("WY", "WYOMING");
		colNames.add("StateCode");
		colNames.add("State");
		for( String sCode : states.keySet() ) {
			String sState = states.get(sCode);
			ArrayList<Object> aRow = new ArrayList<Object>();
			aRow.add(sCode);
			aRow.add(sState);
			rows.add(aRow);
		}
		
	}
	
	public static String getState(String sStateCode) {
		String sRet = states.get(sStateCode);
		return sRet;
	}
	
	public static String getStateCode( String sState ) {
		for( String sCode : states.keySet() ) {
			if( states.get(sCode).equals(sState) ) 
				return sCode;
		}
		return null;
	}
	
	@Override
	public void refresh() {
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
		Object aStates[] =  states.keySet().toArray();
		Arrays.sort( aStates );
		for( Object oCode : aStates ) {
			String sCode = (String)oCode;
			String sState = states.get(sCode);
			super.addElement(sState);
			hValuesKeys.put(sState, sCode);
			hKeysValues.put(sCode, sState);
			hValuesCodes.put(sState, sCode);
			hCodesValues.put(sCode, sState);
		}

	}

	@Override
	public ArrayList<String> getColumnNames() {
		return colNames;
	}

	@Override
	public ArrayList<ArrayList<Object>> getRows() {
		return rows;
	}

}
