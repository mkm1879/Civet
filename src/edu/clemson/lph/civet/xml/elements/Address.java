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

import java.util.StringTokenizer;

public class Address {
	public String line1; 
	public String line2; 
	public String town; 
	public String county;
	public String state; 
	public String zip; 
	public String country;
	public Double latitude; 
	public Double longitude;
	
//	/**
//	 * Convert from AddressBlock to Address.  Only works if each field is , delimited.  Needs work.
//	 * @param addressBlock
//	 */
//	public Address(String addressBlock) {
//		StringTokenizer tok = new StringTokenizer(addressBlock, ", ");
//		line1 = tok.nextToken(); 
//		line2 = tok.nextToken(); 
//		town = tok.nextToken(); 
//		county = tok.nextToken();
//		state = tok.nextToken(); 
//		zip = tok.nextToken(); 
//		country = tok.nextToken();
//		String sLatitude = tok.nextToken(); 
//		String sLongitude = tok.nextToken();
//		try {
//			latitude = Double.parseDouble(sLatitude);
//			longitude = Double.parseDouble(sLongitude);
//		} catch ( NumberFormatException nfe ) {
//			latitude = null;
//			longitude = null;
//		}
//
//	}
	
	public Address(String line1, String line2, String town, String county, String state, String zip, String country,
			Double latitude, Double longitude) {
		this.line1 = line1;
		this.line2 = line2;
		this.town = town;
		this.county = county;
		this.state = state;
		this.zip = zip;
		this.country = country;
		this.latitude = latitude;
		this.longitude = longitude;
	}
}