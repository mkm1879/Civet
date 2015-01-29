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
/**
 * <p>Title: Animal ID Tuple</p>
 * <p>Description: Test Tools to Add PDF Image manipulation to SCPrem</p>
 * <p>Copyright: Copyright (c) 2010</p>
 * <p>Company: LPH</p>
 * @author MKM
 * @version 1.0
 */

public class AnimalIDRecord {
	static int iMaxID = 0;
	public int iRowID;
	public String sSpeciesCode;
	public String sSpecies;
	public String sTag;
	
	public AnimalIDRecord( String sSpeciesCode, String sSpecies, String sTag ) {
		this.iRowID = iMaxID++;
		this.sSpeciesCode = sSpeciesCode; 
		this.sSpecies = sSpecies;
		this.sTag = sTag;
	}

	@Override
	public boolean equals( Object o ) {
		if( !( o instanceof AnimalIDRecord ) ) return false;
		AnimalIDRecord rOther = (AnimalIDRecord)o;
		return ( rOther.sSpeciesCode == this.sSpeciesCode && rOther.sTag.equals(sTag) );
	}

	@Override
	public int hashCode() {
		return sSpeciesCode.hashCode();
	}
}
