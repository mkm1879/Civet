package edu.clemson.lph.civet.addons;
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
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.utils.LabeledCSVParser;


public class VspsCviEntity {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private String[] aEntityNames = {"Origin","Destination","Consignee","Consignor","Carrier"};
	public List<String> lEntityNames = Arrays.asList(aEntityNames);
	static {
		PropertyConfigurator.configure("CivetConfig.txt");
		logger.setLevel(Level.ERROR);
	}
	private LabeledCSVParser parser = null;
	private List<String> aCols;
	private int iDelta;

	public VspsCviEntity( List<String> aColsIn, LabeledCSVParser parserIn, String sEntityType ) {
		aCols = aColsIn; 
		parser = parserIn;
		try {
			int iEntitySize = parser.getLabelIdx("Destination Business Name") - parser.getLabelIdx("Origin Business Name");
			int iEntityNo = lEntityNames.indexOf(sEntityType);
			iDelta = iEntitySize * iEntityNo;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getBusiness() throws IOException {
		int iCol = iDelta + parser.getLabelIdx("Origin Business Name");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}

	public String getName() throws IOException {
		int iCol = iDelta + parser.getLabelIdx("Origin Name");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}

	public String getPremisesId() throws IOException {
		int iCol = iDelta + parser.getLabelIdx("Origin Premises Id");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}

	public String getAddress1() throws IOException {
		int iCol = iDelta + parser.getLabelIdx("Origin Address 1");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}

	public String getAddress2() throws IOException {
		int iCol = iDelta + parser.getLabelIdx("Origin Address 2");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}

	public String getCity() throws IOException {
		int iCol = iDelta + parser.getLabelIdx("Origin City");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}

	public String getPostalCode() throws IOException {
		int iCol = iDelta + parser.getLabelIdx("Origin Postal Code");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}

	public String getCounty() throws IOException {
		int iCol = iDelta + parser.getLabelIdx("Origin County");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}

	public String getState() throws IOException {
		int iCol = iDelta + 18; // Need magic number because column label is repeated.
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}

	public String getCountry() throws IOException {
		int iCol = iDelta + parser.getLabelIdx("Origin Country");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}

	public String getEmail() throws IOException {
		int iCol = iDelta + parser.getLabelIdx("Origin E-mail");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}

	public String getPhone() throws IOException {
		int iCol = iDelta + parser.getLabelIdx("Origin Phone");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}
	
	public String getPhoneDigits() throws IOException {
		String sRet = null;
		String sPhone = getPhone();
		if( sPhone != null && sPhone.trim().length() >= 10 ) {
			StringBuffer sb = new StringBuffer();
			for( int i = 0; i < sPhone.length(); i++ ) {
				char cNext = sPhone.charAt(i);
				if( Character.isDigit(cNext) )
					sb.append(cNext);
			}
			sRet = sb.toString();
		}
		return sRet;
	}

	public String getFax() throws IOException {
		int iCol = iDelta + parser.getLabelIdx("Origin Fax");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}

	public String getCell() throws IOException {
		int iCol = iDelta + parser.getLabelIdx("Origin Cell");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}


}
