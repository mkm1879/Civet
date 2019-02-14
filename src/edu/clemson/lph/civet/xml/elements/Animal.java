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

import java.util.ArrayList;

import org.w3c.dom.Element;

public class Animal {
	public Element eAnimal;
	public SpeciesCode speciesCode;
	public ArrayList<AnimalTag> animalTags;
	// Ignore Test unless received in source file
	// Ignore Vaccination unless received in source file
	public String age;
	public String breed;
	public String sex;
	// Ingnore SexDetail unless received in source file
	public String inspectionDate;

	public Animal(Element eAnimal, SpeciesCode speciesCode, ArrayList<AnimalTag> animalTags,  String age, String breed,
			String sex, String inspectionDate ) {
		this.eAnimal = eAnimal;
		this.speciesCode = speciesCode;
		if( animalTags != null )
			this.animalTags = animalTags;
		else
			this.animalTags = new ArrayList<AnimalTag>();
		this.age = age;
		this.breed = breed;
		this.sex = sex;
		this.inspectionDate = inspectionDate;
	}
	
	public Animal(SpeciesCode speciesCode, ArrayList<AnimalTag> animalTags,  String age, String breed,
			String sex, String inspectionDate ) {
		this.eAnimal = null;
		this.speciesCode = speciesCode;
		if( animalTags != null )
			this.animalTags = animalTags;
		else
			this.animalTags = new ArrayList<AnimalTag>();
		this.age = age;
		this.breed = breed;
		this.sex = sex;
		this.inspectionDate = inspectionDate;
	}
	
	public Animal(String sSpeciesCode, String sTag ) {
		this.speciesCode = new SpeciesCode( sSpeciesCode );
		animalTags = new ArrayList<AnimalTag>();
		animalTags.add( new AnimalTag(sTag) );
		this.age = null;
		this.breed = null;
		this.sex = null;
		this.inspectionDate = null;		
	}
	
	public Animal(String sSpeciesCode, AnimalTag.Types type, String sTag ) {
		this.speciesCode = new SpeciesCode( sSpeciesCode );
		animalTags = new ArrayList<AnimalTag>();
		animalTags.add( new AnimalTag(type, sTag) );
		this.age = null;
		this.breed = null;
		this.sex = null;
		this.inspectionDate = null;		
	}

	public Animal(SpeciesCode speciesCode, String sTag ) {
		eAnimal = null;
		this.speciesCode = speciesCode;
		animalTags = new ArrayList<AnimalTag>();
		animalTags.add( new AnimalTag(sTag) );
		this.age = null;
		this.breed = null;
		this.sex = null;
		this.inspectionDate = null;
	}
	
	public Animal(SpeciesCode speciesCode, AnimalTag.Types type, String sTag ) {
		eAnimal = null;
		this.speciesCode = speciesCode;
		animalTags = new ArrayList<AnimalTag>();
		animalTags.add( new AnimalTag(type, sTag) );
		this.age = null;
		this.breed = null;
		this.sex = null;
		this.inspectionDate = null;
	}

	public Animal(SpeciesCode speciesCode, ArrayList<AnimalTag> aTags ) {
		eAnimal = null;
		this.speciesCode = speciesCode;
		animalTags = aTags;
		this.age = null;
		this.breed = null;
		this.sex = null;
		this.inspectionDate = null;
	}
	
	@Override
	public Animal clone() {		
		SpeciesCode newSpp = new SpeciesCode(speciesCode.isStandardCode, speciesCode.code, speciesCode.text);
		ArrayList<AnimalTag> newTags = new ArrayList<AnimalTag>();
		newTags.addAll(animalTags);
		Animal newAnimal = new Animal(newSpp, newTags);
		newAnimal.eAnimal = eAnimal;
		newAnimal.age = age;
		newAnimal.breed = breed;
		newAnimal.sex = sex;
		newAnimal.inspectionDate = inspectionDate;
		return newAnimal;
	}

	/**
	 * Used to allow update of existing animal record pulled from XML
	 */
	@Override
	public boolean equals( Object object ) {
		boolean bRet = false;
		// If pulled from source XML use the element it came from as identity
		if( eAnimal != null && object instanceof Animal ) {
			Element eAnimal2 = ((Animal)object).eAnimal;
			bRet = (eAnimal2 != null && eAnimal2 == eAnimal);  // Intentionally testing identity of objects
		} // End xml animals
		// For animals created here use speciesCode and tag value.  We only assign one so the looping is kind of silly.
		else if( eAnimal == null && object instanceof Animal ) {
			Animal animal2 = (Animal)object;
			if (animal2.eAnimal == null && animal2.speciesCode.equals(speciesCode) ) {
				if( animal2.animalTags != null && animalTags != null ) {
					for( AnimalTag tag : animalTags ) {
						String sTag = tag.value;
						for( AnimalTag tag2 :animal2.animalTags ) {
							String sTag2 = tag2.value;
							if( sTag2 != null && sTag2.equals(sTag) ) {
								bRet = true;
								break;
							}
						} // End for each of the other animal's tags
					} // End for each of my tags
				} // End if they both have tags
			} // End if neither is from XML and same species
		} // End non-xml animals
		return bRet;
	}
	
	public String getFirstOfficialID() {
		String sRet = null;
		for( AnimalTag tag : animalTags ) {
			if( tag.isOfficial() ) {
				sRet = tag.value;
				break;
			}
		}
		return sRet;
	}
	
	public String getBestID() {
		String sRet = getFirstOfficialID();
//		Try for first official but use any we can find.
		if( sRet == null ) {
			for( AnimalTag tag : animalTags ) {
				if( tag.value != null ) {
					sRet = tag.value;
					break;
				}
			}

		}
		return sRet;
	}
}