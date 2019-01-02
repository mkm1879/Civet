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
public class AnimalTag {
	public static enum Types {
		AIN, 
		MfrRFID, 
		NUES9, 
		NUES8, 
		OtherOfficialID, 
		ManagementID, 
		BrandImage, 
		EquineDescription, 
		EquinePhotographs 
	}

	public Types type;
	public String otherType;
	public String value;
	public EquineDescription description;
	public EquinePhotographs photographs;
	public BrandImage brand;
	
	public AnimalTag(Types type, String number) {
		this.type = type;
		this.value = number;
	}
	
	public AnimalTag(Types type, String otherType, String number) {
		this.type = type;
		this.otherType = otherType;
		this.value = number;
	}
	
	public AnimalTag(EquineDescription description) {
		this.type = Types.EquineDescription;
		this.value = description.toString();
		this.description = description;
		value = description.description;
	}
	
	public AnimalTag(EquinePhotographs photographs) {
		this.type = Types.EquinePhotographs;
		this.value = "See attached photos: " + photographs.toString();
		this.photographs = photographs;
	}
	
	public AnimalTag(BrandImage brand)  {
		this.type = Types.BrandImage;
		this.value = "See attached image: " + brand.toString();
		this.brand = brand;
	}
	
	public String getElementName() {
		String sRet = null;
		switch( type ) {
		case AIN:
			sRet = "AIN";
			break;
		case MfrRFID:
			sRet = "MfrRFID";
			break; 
		case NUES9:
			sRet = "NUES9";
			break; 
		case NUES8:
			sRet = "NUES8";
			break; 
		case OtherOfficialID:
			sRet = "OtherOfficialID";
			break; 
		case ManagementID:
			sRet = "ManagementID";
			break; 
		case BrandImage:
			sRet = "BrandImage";
			break; 
		case EquineDescription:
			sRet = "EquineDescription";
			break; 
		case EquinePhotographs:
			sRet = "EquinePhotographs";
			break; 
		default:
			sRet = "ManagementID";  // Give up if I can't classify it
		}
		return sRet;
	}
	
	
	public boolean isOfficial() {
		boolean bRet = false;
		switch( type ) {
		case AIN:
		case MfrRFID:
		case NUES9:
		case NUES8:
		case OtherOfficialID:
			bRet = true;
			break; 
		default:
			bRet = false;
		}
		return bRet;
	}
	
	@Override
	public String toString() {
		return value;
	}
}