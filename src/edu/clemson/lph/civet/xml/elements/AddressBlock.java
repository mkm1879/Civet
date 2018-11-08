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
public class AddressBlock {
	public String addressBlock;
	
	public AddressBlock(String addressBlock) {
		this.addressBlock = addressBlock;
	}
	
	/**
	 * Convert address parts to single address block.  Here include ,s for each field.
	 * @param line1
	 * @param line2
	 * @param town
	 * @param county
	 * @param state
	 * @param zip
	 * @param country
	 * @param latitude Double
	 * @param longitude Double
	 */
	public AddressBlock(String line1, String line2, String town, String county, String state, String zip, String country,
			Double latitude, Double longitude) {
		StringBuffer sb = new StringBuffer();
		if( line1 != null && line1.trim().length() > 0 ) sb.append(line1); sb.append(", ");
		if( line2 != null && line2.trim().length() > 0 )  sb.append(line2); sb.append(", ");
		if( town != null && town.trim().length() > 0 ) sb.append(town); sb.append(", ");
		if( county != null && county.trim().length() > 0 ) sb.append(county); sb.append(", ");
		if( state != null && state.trim().length() > 0 ) sb.append(state); sb.append(", ");
		if( zip != null && zip.trim().length() > 0 ) sb.append(zip); sb.append(", ");
		if( country != null && country.trim().length() > 0 )  sb.append(country); sb.append(", ");
		if( latitude != null ) sb.append(latitude.toString() ); sb.append(", ");
		if( longitude != null ) sb.append(longitude.toString() ); sb.append(", ");
		String sRet = sb.toString();
		this.addressBlock = sRet.substring(0, sRet.length() - 2);
	}

	@Override
	public String toString() {
		return addressBlock;
	}
}