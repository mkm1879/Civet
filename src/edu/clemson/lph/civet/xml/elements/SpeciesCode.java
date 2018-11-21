/*
Copyright 2014-2018 Michael K Martin

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

package edu.clemson.lph.civet.xml.elements;
public class SpeciesCode {
	public boolean isStandardCode;
	public String code;
	public String text;
	
	public SpeciesCode(boolean isStandardCode, String code, String text) {
		this.isStandardCode = isStandardCode;
		this.code = code;
		this.text = text;
	}
	
	@Override
	public String toString() {
		String sRet = "";
		if( code != null && code.trim().length() > 0 )
			sRet = code;
		if( text != null && text.trim().length() > 0 )
			sRet = sRet + " " + text;
		return sRet;
	}
}