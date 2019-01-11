package edu.clemson.lph.utils;
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
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import edu.clemson.lph.civet.xml.elements.AnimalTag;

public class IDTypeGuesser {
	private static HashMap<String, AnimalTag.Types> mTypeMap = new HashMap<String, AnimalTag.Types>();

	static {
		mTypeMap.put("^840\\d{11,13}$", AnimalTag.Types.AIN); // Make fuzzy on length
		mTypeMap.put("^\\d{2}[a-zA-Z]{3}\\d{4}$",  AnimalTag.Types.NUES9);
		mTypeMap.put("^\\d{2}[a-zA-Z]{2}\\d{4}$", AnimalTag.Types.NUES8);
		mTypeMap.put("((9[0-8]\\d)|(9\\d[0-8])|(124)|(484))\\d{12}", AnimalTag.Types.MfrRFID);
		mTypeMap.put("^SC\\d{8}$", AnimalTag.Types.OtherOfficialID);
		mTypeMap.put("^SCA\\d+$", AnimalTag.Types.OtherOfficialID);
	}

	private IDTypeGuesser() {
		// TODO Auto-generated constructor stub
	}
	
	public static AnimalTag.Types getTagType( String sTag ) {
		return getTagType( sTag, false);
	}
	
	public static AnimalTag.Types getTagType( String sTag, boolean bDefaultOfficial ) {
		AnimalTag.Types type = AnimalTag.Types.ManagementID;
		if( bDefaultOfficial) type = AnimalTag.Types.OtherOfficialID;
		if( sTag != null && sTag.trim().length() > 0 ) {
			for( String sRegex : mTypeMap.keySet() ) {
				Pattern pattern = Pattern.compile(sRegex);
				Matcher matcher = pattern.matcher(sTag.trim());
				if( matcher.find() ) {
					type = mTypeMap.get(sRegex);
				}	
			}
		}
		return type;
	}


}
