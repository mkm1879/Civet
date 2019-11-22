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

public class NameParts {
	public String businessName;
	public String firstName;
	public String middleName;
	public String lastName;
	public String otherName;
	
	public NameParts( String businessName, String firstName, String middleName, String lastName, String otherName ) {
		this.businessName = businessName;
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.otherName = otherName;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if( businessName != null && businessName.trim().length() > 0 ) {
			sb.append( businessName );
			sb.append(", ");
		}
		if( lastName != null && lastName.trim().length() > 0 ) {
			sb.append( lastName );
			sb.append(", ");
		}
		if( firstName != null && firstName.trim().length() > 0 ) {
			sb.append( firstName );
			sb.append(", ");
		}
		if( middleName != null && middleName.trim().length() > 0 ) {
			sb.append( middleName );
			sb.append(", ");
		}
		if( otherName != null && otherName.trim().length() > 0 ) {
			sb.append( otherName );
			sb.append(", ");
		}
		String sRet = sb.toString();
		if(sRet != null && sRet.trim().length() > 2 && sRet.trim().endsWith(", ") )
			sRet = sRet.substring(0, sRet.length() - 2);
		return sRet;
	}
}