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

public class GroupLot {
	public SpeciesCode speciesCode;
	public String groupLotId;
	// Ignore Test unless received in source file
	// Ignore Vaccination unless received in source file
	public Double quantity;
	public String unit;
	public String age;
	public String breed;
	public String sex;
	// Ingnore SexDetail unless received in source file
	public String description;
	
	public GroupLot(SpeciesCode speciesCode, String groupLotId, Double quantity, String unit, 
				String age, String breed, String sex, String description ) {
		this.speciesCode = speciesCode;
		this.groupLotId = groupLotId;
		this.quantity = quantity;
		this.unit = unit;
		this.age = age;
		this.breed = breed;
		this.sex = sex;
		this.description = description;
	}
}