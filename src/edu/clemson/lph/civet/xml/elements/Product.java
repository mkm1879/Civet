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

import org.w3c.dom.Element;


public class Product {
	public Element eProduct;
	public SpeciesCode speciesCode;
	public String productId;
	// Ignore Test unless received in source file
	// Ignore Vaccination unless received in source file
	public String productType;
	public Double quantity;
	public String unit;
	public String age;
	public String breed;
	public String sex;
	// Ingnore SexDetail unless received in source file
	public String description;

	public Product(Element eProduct, SpeciesCode speciesCode, String productId, String productType, 
			Double quantity, String unit, String age, String breed, String sex, String description ) {
		this.eProduct = eProduct;
		this.speciesCode = speciesCode;
		this.productId = productId;
		this.quantity = quantity;
		if( this.quantity == null ) this.quantity = 1.0;
		this.unit = unit;
		this.age = age;
		this.productType = productType;
		this.breed = breed;
		this.sex = sex;
		if( description != null && description.trim().length() > 0 )
			this.description = description;
		else {
			String sSpecies = this.speciesCode.text;
			Long iQuant = (Long) Math.round(quantity); 
			this.description = iQuant.toString() + " " + sSpecies + " " + productType;
		}
	}

	public Product(SpeciesCode speciesCode, String productType, Double quantity ) {
		this.eProduct = null;
		this.speciesCode = speciesCode;
		this.productId = null;
		this.productType = productType;
		this.quantity = quantity;
		this.unit = null;
		this.age = null;
		this.breed = null;
		this.sex = null;
		String sSpecies = "OTH";
		if( speciesCode != null )
			sSpecies = this.speciesCode.text;
		Integer iQuant = quantity.intValue(); 
		description = iQuant.toString() + " " + sSpecies + " " + productType;
	}

	public Product(String speciesCode, String productType, Double quantity ) {
		this.eProduct = null;
		this.speciesCode = new SpeciesCode(speciesCode);
		this.productId = null;
		this.productType = productType;
		this.quantity = quantity;
		this.unit = null;
		this.age = null;
		this.breed = null;
		this.sex = null;
		String sSpecies = "OTH";
		if( speciesCode != null )
			sSpecies = this.speciesCode.text;
		Integer iQuant = quantity.intValue(); 
		description = iQuant.toString() + " " + sSpecies + " " + productType;
	}

	public Product(String speciesCode, String sBreed, String sGender, String productType, Double quantity ) {
		this.eProduct = null;
		this.speciesCode = new SpeciesCode(speciesCode);
		this.productId = null;
		this.productType = productType;
		this.quantity = quantity;
		this.unit = null;
		this.age = null;
		this.breed = sBreed;
		this.sex = sGender;
		String sSpecies = "OTH";
		if( speciesCode != null )
			sSpecies = this.speciesCode.text;
		Integer iQuant = quantity.intValue(); 
		description = iQuant.toString() + " " + sSpecies + " " + productType;
	}
	
	public void setQuantity( Double quantity ) {
		this.quantity = quantity;
		String sSpecies = speciesCode.text;
		Long iQuant = (Long) Math.round(quantity); 
		this.description = "Group of " + iQuant.toString() + " " + sSpecies + " " + productType;
	}
}