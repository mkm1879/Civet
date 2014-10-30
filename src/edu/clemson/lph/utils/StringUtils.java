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


}
