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

import edu.clemson.lph.civet.lookup.SpeciesLookup;

public class SpeciesCode {
	public boolean isStandardCode;
	public String code;
	public String text;
	
	public SpeciesCode(boolean isStandardCode, String code, String text) {
		this.isStandardCode = isStandardCode;
		this.code = code;
		this.text = text;
	}
	
	public SpeciesCode(String code, String text) {
		boolean bStd = SpeciesLookup.isCodeStandard( code );
		this.isStandardCode = bStd;
		this.code = code;
		this.text = text;
	}
	
	public SpeciesCode(String code) {
		boolean bStd = SpeciesLookup.isCodeStandard( code );
		this.isStandardCode = bStd;
		this.code = code;
		this.text = SpeciesLookup.getNameForCode(code);
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
	
	@Override
	public boolean equals(Object o) {
		boolean bRet = false;
		if( o instanceof SpeciesCode ) {
			SpeciesCode scOther = (SpeciesCode)o;
			if( code.equals(scOther.code) )
				bRet = true;
		}
		return bRet;
	}
	
}