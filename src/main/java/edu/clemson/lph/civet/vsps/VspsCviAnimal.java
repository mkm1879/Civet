package edu.clemson.lph.civet.vsps;
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
import java.util.ArrayList;
import java.util.List;

import edu.clemson.lph.logging.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.xml.elements.AnimalTag;
import edu.clemson.lph.civet.xml.elements.EquineDescription;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.utils.AnimalIDUtils;
import edu.clemson.lph.utils.LabeledCSVParser;

public class VspsCviAnimal {
      private static Logger logger = Logger.getLogger();
	private List<String> aCols;
	private LabeledCSVParser parser;
	private DateFormat df = new SimpleDateFormat( "dd-MMM-yy");
	private DateFormat df2 = new SimpleDateFormat( "d-MMM-yy");
	private ArrayList<AnimalTag> aTags;
	
	VspsCviAnimal( List<String> aColsIn, LabeledCSVParser parserIn ) {
		aCols = aColsIn;
		parser = parserIn;
		readIds();
	}
	
	/**
	 * Look for Id that fits a common official id pattern.
	 * Store the ID in sFirstOfficial and type in sFirstOfficialType
	 * @throws IOException
	 */
	private void readIds() {
		aTags = new ArrayList<AnimalTag>();
		for( int i = 1; i <= 5; i++ ) {
			try {
				AnimalTag tag = null;
				String sIdType = getIdentifierType(i);
				String sId = getIdentifier(i);
				String sOtherType = null;
				if( sId == null || sId.trim().length() == 0 )
					continue;
				String aTag[] = AnimalIDUtils.getIDandType(sId);
				String sTrimmedId = aTag[0];
				String sGuessedType = aTag[1];
// Replace all this with above getIDandType
//				if( sId.startsWith("840") && sId.trim().length() >= 14 && sId.trim().length() <= 16) {
//					sId = sId.trim();
//					tag = new AnimalTag( AnimalTag.Types.AIN, sId);
//				}
//				else if( sId.startsWith("USA") && sId.trim().length() >= 14 && sId.trim().length() <= 16) {
//					sId = sId.trim();
//					tag = new AnimalTag( AnimalTag.Types.OtherOfficialID, "USA", sId) ;
//				}
//				else if( "USDA Metal Tag".equalsIgnoreCase(sIdType) ) {
//					if( sId.trim().length() == 9 ) 
//						tag = new AnimalTag( AnimalTag.Types.NUES9, sId );
//					else if( sId.trim().length() == 8 )
//						tag = new AnimalTag( AnimalTag.Types.NUES8, sId );
//					else {
//						sOtherType = "USDA Metal Tag";
//						tag = new AnimalTag( AnimalTag.Types.OtherOfficialID, sOtherType, sId );
//					}
//				}
				if( "MfrRFID".equals(sGuessedType) ) {
					tag = new AnimalTag( AnimalTag.Types.MfrRFID, sTrimmedId );
				}
				else if( "AIN".equals(sGuessedType) ) {
					tag = new AnimalTag( AnimalTag.Types.AIN, sTrimmedId );
				}
				else if( "NUES9".equals(sGuessedType) ) {
					tag = new AnimalTag( AnimalTag.Types.NUES9, sTrimmedId );
				}
				else if( "NUES8".equals(sGuessedType) && "USDA Metal Tag".equalsIgnoreCase(sIdType) ) {
					tag = new AnimalTag( AnimalTag.Types.NUES8, sTrimmedId );
				}
				else if( sGuessedType.startsWith("Short") ) {
					tag = new AnimalTag( AnimalTag.Types.OtherOfficialID, "OTHER", sTrimmedId );
					MessageDialog.showMessage(null, "VSPS Error", "Invalid (long or short) ID '" + sId + "' found in VSPS file");
				}
				else if( sGuessedType.startsWith("Long") ) {
					tag = new AnimalTag( AnimalTag.Types.OtherOfficialID, "OTHER", sTrimmedId );
					MessageDialog.showMessage(null, "VSPS Error", "Invalid ID '" + sId + "' found in VSPS file");
				}
				else if( sGuessedType.contains("USA") ) {
					tag = new AnimalTag( AnimalTag.Types.OtherOfficialID, "AMID", sTrimmedId );
				}
				else if( "Registered Name of Animal".equalsIgnoreCase(sIdType) ) {
					EquineDescription ed = new EquineDescription( sId, sId );
					tag = new AnimalTag( ed );
				}
				else if( "Tattoo".equalsIgnoreCase(sIdType) ) {
					sOtherType = "TAT";
					tag = new AnimalTag( AnimalTag.Types.OtherOfficialID, sOtherType, sId );
				}
				else if( "Ear Tattoo".equalsIgnoreCase(sIdType) ) {
					sOtherType = "TAT";
					tag = new AnimalTag( AnimalTag.Types.OtherOfficialID, sOtherType, sId );
				}
				else if( "Call Name".equalsIgnoreCase(sIdType) ) {
					sOtherType = "NAME";
					tag = new AnimalTag( AnimalTag.Types.ManagementID, sOtherType, sId );
				}
				else {
					tag = new AnimalTag( sId );
				}
				aTags.add(tag);
			} catch( IOException e ) {
				logger.error("Could not read ID " + i, e);
			}
		}
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

	public ArrayList<AnimalTag> getTags() {
		return aTags;
	}
	
	/**
	 * Used to be sure we put official IDs first.
	 * @return one Id string or null if nothing "looks" like an official id
	 * @throws IOException
	 */
	public AnimalTag getFirstOfficialId() throws IOException {
		AnimalTag tRet = null;
		for( AnimalTag tag : aTags ) {
			if( tag.isOfficial() ) {
				tRet = tag;
				break;
			}
		}
		return tRet;
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
