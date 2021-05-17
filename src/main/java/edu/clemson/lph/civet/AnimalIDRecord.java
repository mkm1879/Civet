package edu.clemson.lph.civet;

import edu.clemson.lph.civet.xml.elements.Animal;

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
	private static int iMaxID = 0;
	int iRowID;
	Animal animal;
	
	public AnimalIDRecord( String sSpeciesCode, String sSpecies, String sTag ) {
		this.iRowID = iMaxID++;
		animal = new Animal( sSpeciesCode, sTag );
	}

	public AnimalIDRecord( Animal animal ) {
		this.iRowID = iMaxID++;
		this.animal = animal;
	}

	@Override
	public boolean equals( Object o ) {
		if( o == null ) return false;
		if( !( o instanceof AnimalIDRecord ) ) return false;
		AnimalIDRecord rOther = (AnimalIDRecord)o;
		if( rOther.animal == this.animal )  // Test identity of animals Objects
			return true;
		if( rOther.animal == null && this.animal != null)
			return false;
		if( rOther.animal != null && this.animal == null)
			return false;
		if( rOther.animal == null || this.animal == null)
			return false;
		if( rOther.animal.getFirstOfficialID() == null || this.animal.getFirstOfficialID() == null )
			return false;
		if( rOther.animal.speciesCode.code == null || this.animal.speciesCode.code == null )
			return false;
		// Or test equality of values. 
		return ( rOther.animal.speciesCode.code == this.animal.speciesCode.code && 
				rOther.animal.getFirstOfficialID().equals(this.animal.getFirstOfficialID() ) );
	}

	@Override
	public int hashCode() {
		String sAll = animal.speciesCode.code + animal.getFirstOfficialID();
		return sAll.hashCode();
	}
}
