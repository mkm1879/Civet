package edu.clemson.lph.civet;
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

public class IdListGen {
	
	public static ArrayList<String> getIds( String sPrefix, String sFirstID, int iNum ) throws Exception {
		if( "840".equals(sPrefix) || "982".equals(sPrefix) ) {
			return get840Ids( sPrefix, sFirstID, iNum );
		}
		else {
			ArrayList<String> lList = new ArrayList<String>();
			try {
			SplitID split = new SplitID( sFirstID );
			String sFirst = split.getFirstPart(); 
			Long iLast = split.getLastPart();

			int iLen = iLast.toString().length();
			lList.add( sPrefix + sFirstID );
			for( int i = 1; i < iNum; i++ ) {
				Long iNext = iLast + i;
				int iNextLen = iNext.toString().length();
				while( iNextLen > iLen ) {
					if( sFirst.endsWith("0") ) {
						sFirst = sFirst.substring(0, sFirst.length()-1);
						iLen++;
					}
					else {
						throw new Exception("Identifier " + sFirst + iNext.toString() + " does not fit in initial length" );
					}
				}
				String sNextID = sPrefix + sFirst + iNext.toString();
				lList.add(sNextID);
			}
			}catch( NumberFormatException nfe ) {
				throw new NumberFormatException( "Cannot parse '" + sFirstID + "' as ending in a number" );
			}
			return lList;
		}
	}
	
	public static ArrayList<String> get840Ids( String sPrefix, String sFirstID, int iNum ) throws Exception {
		ArrayList<String> lList = new ArrayList<String>();
		lList.add( AddAnimalsDialog.padTag(sPrefix, sFirstID, 15) );
		Long iLast = null;
		try {
			iLast = Long.parseLong(sFirstID);
		} catch( NumberFormatException nfe ) {
			throw new NumberFormatException( "Cannot parse '" + sFirstID + "' as a number" );
		}
		for( int i = 1; i < iNum; i++ ) {
			Long iNext = iLast + i;
			String sNextID = AddAnimalsDialog.padTag(sPrefix, iNext.toString(), 15);
			lList.add(sNextID);
		}
		return lList;
	}

	
	public static String getFirstSplit( String sId ) {
		SplitID split = new SplitID( sId );
		return split.getFirstPart();
	}
	
	public static Long getLastSplit( String sId ) {
		SplitID split = new SplitID( sId );
		return split.getLastPart();
	}
	
	static class SplitID {
		private String sFirstPart = null;
		private Long iLastPart = null;
		
		public SplitID( String sID ) {
			try {
				int iLen = sID.length();
				long iFullID = Long.parseLong(sID);
				iLastPart = iFullID;
				int iNewLen = iLastPart.toString().length();
				sFirstPart = "";
				for( int i = 0; i < iLen - iNewLen; i++ )
					sFirstPart += "0";
				return;
			} catch( NumberFormatException nfe ) {
				// Expected need to split
			}
			for( int i = sID.length() - 2; i >= 0; i-- ) {
				// Can't treat 840 numbers as all integer because of overflow
				if( !Character.isDigit(sID.charAt(i)) || sID.substring(0,i+1).matches("8400*")) {
					if( i < sID.length()-2 && sID.charAt(i+1) == '0' ) {
							i++;
					}
					sFirstPart = sID.substring(0,i+1);
					if( i < sID.length() )
						iLastPart = Long.parseLong(sID.substring(i+1));
					else 
						iLastPart = null;
					break;
				}
			}
		}
		
		public String getFirstPart() { return sFirstPart; }
		public Long getLastPart() { return iLastPart; }
	}

}
