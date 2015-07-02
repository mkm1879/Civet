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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.utils.LabeledCSVParser;

public class VspsCviAnimal {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private List<String> aCols;
	private LabeledCSVParser parser;
	private DateFormat df = new SimpleDateFormat( "dd-MMM-yy");
	private DateFormat df2 = new SimpleDateFormat( "d-MMM-yy");
	private String sFirstOfficial = null;
	private String sFirstOfficialType = null;
	private boolean bCheckedIds = false;
	
	VspsCviAnimal( List<String> aColsIn, LabeledCSVParser parserIn ) {
		aCols = aColsIn;
		parser = parserIn;
	}
	
	public String getSpecies() throws IOException {
		int iCol = parser.getLabelIdx("Species");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}
	
	public String getPurpose() throws IOException {
		int iCol = parser.getLabelIdx("Purpose");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}
	
	public String getType() throws IOException {
		int iCol = parser.getLabelIdx("Type");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}
	
	public Integer getCount() throws IOException {
		Integer iRet = null;
		int iCol = parser.getLabelIdx("Count");
		try {
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			iRet =  Integer.parseInt(aCols.get(iCol));
		} catch( NumberFormatException nfe ) {
			for( String sCol : aCols ) 
				logger.error( sCol );
			logger.error(aCols.get(iCol) + " cannot be read as int");
		}			
		return iRet;
	}
	
	public String getBreed() throws IOException {
		int iCol = parser.getLabelIdx("Breed");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}
	
	public String getMinAge() throws IOException {
		int iCol = parser.getLabelIdx("Min Age");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}
	
	public String getMaxAge() throws IOException {
		int iCol = parser.getLabelIdx("Max Age");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}
	
	public java.util.Date getDateOfBirth() throws IOException {
		int iCol = parser.getLabelIdx("Date of Birth");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else {
			String sDate = aCols.get(iCol);
			try {
				return df.parse(sDate);
			} catch (ParseException e) {
				try {
					return df2.parse(sDate);
				} catch (ParseException e2) {
					logger.error(e2);
					return null;
				}
			}
		}
	}
	
	public String getGender() throws IOException {
		int iCol = parser.getLabelIdx("Gender");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}
	
	private boolean isOfficialId( String sId ) {
		boolean bRet = false;
		if( sId == null ) return bRet;
		// 840 tag
		if( sId.trim().length() == 15 && sId.trim().startsWith("840"))
			return true;
		// Manufacturer RFID tag
		if( sId.trim().length() == 15 && sId.trim().startsWith("9"))
			return true;
		// USA tag
		if( sId.trim().startsWith("USA") ) {
			for( int i = 3; i < sId.trim().length(); i++ ) {
				if( !Character.isDigit(sId.trim().charAt(i)) ) {
					return false;
				}
			}
			return true;
		}
		// NUES tag numeric prefix
		if( sId.trim().length() == 9 && Character.isDigit(sId.charAt(0)) && Character.isDigit(sId.charAt(1))
				&& Character.isLetter(sId.charAt(2)) && Character.isLetter(sId.charAt(4)) && Character.isLetter(sId.charAt(4))  
				&& Character.isDigit(sId.charAt(5)) && Character.isDigit(sId.charAt(6)) 
				&& Character.isDigit(sId.charAt(7))  && Character.isDigit(sId.charAt(8)) )
			return true;
		// NUES tag alpha prefix
		if( sId.trim().length() == 9 && Character.isLetter(sId.charAt(0)) && Character.isLetter(sId.charAt(1))
				&& Character.isLetter(sId.charAt(2)) && Character.isLetter(sId.charAt(4)) && Character.isLetter(sId.charAt(4))  
				&& Character.isDigit(sId.charAt(5)) && Character.isDigit(sId.charAt(6)) 
				&& Character.isDigit(sId.charAt(7))  && Character.isDigit(sId.charAt(8)) )
			return true;
		return bRet;
	}
	
	private boolean isOfficialIdType( String sIdType ) {
		boolean bRet = false;
		if( sIdType == null || sIdType.trim().length() == 0 )
			return false;
		if( sIdType.trim().startsWith("Flock ID Eartag") )
			return true;
		if( sIdType.trim().startsWith("Off Scrapie") )
			return true;
		if( sIdType.trim().startsWith("Reg/SFCP") )
			return true;
		if( sIdType.trim().startsWith("USDA") )
			return true;
		if( sIdType.trim().startsWith("Registered Name") )
			return true;
		return bRet;
	}
	
	/**
	 * Look for Id that fits a common official id pattern.
	 * @return one Id string or null if nothing "looks" like an official id
	 * @throws IOException
	 */
	public String getFirstOfficialId() throws IOException {
		checkIds();
		return sFirstOfficial;
	}
	
	/**
	 * Look for Id that fits a common official id pattern.
	 * @return one Id string or null if nothing "looks" like an official id
	 * @throws IOException
	 */
	public String getFirstOfficialIdType() throws IOException {
		checkIds();
		return sFirstOfficialType;
	}
	
	/**
	 * Look for Id that fits a common official id pattern.
	 * Store the ID in sFirstOfficial and type in sFirstOfficialType
	 * @throws IOException
	 */
	private void checkIds()  throws IOException {
		if( !bCheckedIds ) {
			for( int i = 1; i <= 5; i++ ) {
				String sIdType = getIdentifierType(i);
				String sId = getIdentifier(i);
				if( ( sIdType != null && isOfficialIdType(sIdType) ) || ( sId != null && isOfficialId( sId ) ) ) { 
					sFirstOfficial = sId.trim();
					if( "USDA Metal Tag".equalsIgnoreCase(sIdType) ) {
						if( sId.trim().length() == 9 ) 
							sIdType = "NUES9";
						else if( sId.trim().length() == 8 )
							sIdType = "NUES8";
					}
					sFirstOfficialType = sIdType;
					break;
				}
			}
			bCheckedIds = true;
		}
	}

	
	/**
	 * Look for Id that fits a common official id pattern.
	 * @return one Id string or null if nothing "looks" like an official id
	 * @throws IOException
	 */
	public String getFirstOtherId() throws IOException {
		String sRet = null;
		String sFirstOfficial = getFirstOfficialId();
		for( int i = 1; i <= 5; i++ ) {
			String sId = getIdentifier(i);
			if( sId != null && sId.trim().length() > 0 && (sFirstOfficial == null || !sFirstOfficial.equals(sId)) ) {
				sRet = sId.trim();
				break;
			}
		}
		return sRet;
	}

	
	public String getIdentifierType( int iIdNum) throws IOException {
		if( iIdNum < 1 || iIdNum > 5)
			return null;
		int iCol = parser.getLabelIdx("Identifier " + iIdNum + " Type");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}
	
	public String getIdentifier( int iIdNum ) throws IOException {
		if( iIdNum < 1 || iIdNum > 5)
			return null;
		int iCol = parser.getLabelIdx("Identifier " + iIdNum);
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else {
			String sId = aCols.get(iCol);
			if( "RFID".equalsIgnoreCase(getIdentifierType(iIdNum)) ) {
				sId = digitsOnly( sId );
			}
			return sId;
		}
	}
	
	public String digitsOnly( String sIn ) {
		StringBuffer sb = new StringBuffer();
		char cNext;
		for( int i = 0; i < sIn.length(); i++ ) {
			cNext = sIn.charAt(i);
			if( Character.isDigit(cNext) ) {
				sb.append(cNext);
			}
		}
		return sb.toString();
	}

}
