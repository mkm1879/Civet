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

import edu.clemson.lph.civet.lookup.States;

public class IDTypeGuesser {
	public static HashMap<String, String> mTypeMap = new HashMap<String, String>();
	
	static {
		mTypeMap.put("^840\\d{12}$", "N840RFID");
		mTypeMap.put("^(USA|usa)\\d{12}$", "AMID");
		mTypeMap.put("^\\d{2}[a-zA-Z]{3}\\d{4}$", "NUES9");
//		mTypeMap.put("^\\d{2}[a-zA-Z]{2}\\d{4}$", "NUES8");
		mTypeMap.put("^\\d{2}[a-zA-Z]{2}\\d{4}$", "BT");
		mTypeMap.put("^[a-zA-Z0-9]{7}$", "NPIN");
		mTypeMap.put("^[a-zA-Z ',]+$", "NAME");
		mTypeMap.put("^SC\\d{8}$", "SGFLID");
		mTypeMap.put("^SCA\\d+$", "SGFLID");
		mTypeMap.put("^[a-zA-Z]{2}[a-zA-Z0-9]{2,3} *[a-zA-Z][0-9oO]{2,3}$", "TAT");
	}

	private IDTypeGuesser() {
		// TODO Auto-generated constructor stub
	}
	
	public static String getTagType( String sTag ) {
		String sRet = "UN";
		for( String sRegex : mTypeMap.keySet() ) {
			Pattern pattern = Pattern.compile(sRegex);
			Matcher matcher = pattern.matcher(sTag.trim());
		    if( matcher.find() ) {
		    	String sType = mTypeMap.get(sRegex);
		    	try {
					if( sType.equals("NPIN") && !PremCheckSum.isValid(sTag.toUpperCase()) ) {
						sRet = "UN";
						break;
					}
					else if( sType.equals("TAT") && States.getState( sTag.substring(0,2) ) == null ) {
						sRet = "UN";
						break;
					}
					sRet = sType;
				} catch (Exception e) {
					sRet = "UN";
				}
		    }	

		}
		return sRet;
	}


}
