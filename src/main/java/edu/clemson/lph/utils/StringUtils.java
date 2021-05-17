package edu.clemson.lph.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class StringUtils {
	public static String toTitleCase( String sIn ) {
		StringBuffer sb = new StringBuffer();
		boolean bFirst = true;
		for( int i = 0; i < sIn.length(); i++ ) {
			char cNext = sIn.charAt(i);
			if( bFirst ) cNext = Character.toUpperCase(cNext);
			else cNext = Character.toLowerCase(cNext);
			if( Character.isWhitespace(cNext) ) bFirst = true;
			else bFirst = false;
			sb.append(cNext);
		}
		return sb.toString();
	}

	public static List<String> getStringLines( String sInput ) throws Exception {
		List<String> lRet = new ArrayList<String>();
		if( sInput != null ) {
			BufferedReader rdr = new BufferedReader(new StringReader(sInput));
			try {
				for (String line = rdr.readLine(); line != null; line = rdr.readLine()) {
				    lRet.add(line);
				}
			} catch (IOException e) {
				throw( e );
			} finally {
				if( rdr != null )
				try {
					rdr.close();
				} catch (IOException e) {
					throw(e);
				}
			}
		}
		return lRet;
	}
	
	public static boolean wildCardMatches( String sInput, String sPattern ) {
		boolean bRet = false;
		String sRegex = sPattern.replace(".","\\.").replace("*", ".*").replace("?", ".");
		Pattern p = Pattern.compile(sRegex);
		Matcher matcher = p.matcher(sInput);
        bRet = matcher.matches();
		return bRet;
	}
}
