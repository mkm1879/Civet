package edu.clemson.lph.civet.addons;
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
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.utils.LabeledCSVParser;


public class VspsCvi {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private LabeledCSVParser parser = null;
	private List<String> aCols;
	private List<VspsCviAnimal> lAnimals;
	private int iCurrent = 0;
	private DateFormat df = new SimpleDateFormat( "dd-MMM-yyyy");

	VspsCvi( List<String> aColsIn, LabeledCSVParser parserIn ) {
		aCols = aColsIn;
		parser = parserIn;
		lAnimals = new ArrayList<VspsCviAnimal>();
	}
	
	void addCVIAnimal( VspsCviAnimal aIn ) {
		lAnimals.add(aIn);
	}
	
	public List<VspsCviAnimal> getAnimals() {
		return lAnimals;
	}
	
	public HashMap<List<String>, Integer> getSpecies() throws IOException {
		HashMap<List<String>, Integer> map = new HashMap<List<String>, Integer>();
		for( VspsCviAnimal animal : lAnimals ) {
			Integer iCount = animal.getCount();
			if( iCount == null ) 
				iCount = 1;
			String sSpecies = animal.getSpecies();
			String sGender = animal.getGender();
			if( "Intact Male".equals(sGender) )
				sGender = "Male";
			List<String> aKey = new ArrayList<String>();
			aKey.add(sSpecies);
			aKey.add(sGender);
			Integer iOldCount = map.get(aKey);
			if( iOldCount == null ) iOldCount = 0;
			map.put(aKey, iOldCount += iCount );
		}
		return map;
	}
	
	public java.util.Date getCreateDate() throws IOException {
		int iCol = parser.getLabelIdx("Create Date");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else {
			String sDate = aCols.get(iCol);
			try {
				return df.parse(sDate);
			} catch (ParseException e) {
				logger.error(e);
				return null;
			}
		}
	}

	public String getStatus() throws IOException {
		int iCol = parser.getLabelIdx("Status");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}

	public String getCVINumber() throws IOException {
		int iCol = parser.getLabelIdx("Certificate Number");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}
	
	public String getOriginState() throws IOException {
		int iCol = 4; // Need magic number because column label repeats in data file
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}
	
	public String getDestinationState() throws IOException {
		int iCol = 5; // Need magic number because column label repeats in data file
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}
	
	public java.util.Date getInspectionDate() throws IOException {
		int iCol = parser.getLabelIdx("Inspection Date");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else {
			String sDate = aCols.get(iCol);
			try {
				return df.parse(sDate);
			} catch (ParseException e) {
				logger.error(e);
				return null;
			}
		}
	}
	
	public String getPermitNumber() throws IOException {
		int iCol = parser.getLabelIdx("Permit Number");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}
	
	public String getVeterinarianName() throws IOException {
		int iCol = parser.getLabelIdx("Veterinarian Name");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}
	
	public String getVetFirstName() throws IOException {
		String sVetName = getVeterinarianName();
		if( sVetName == null ) return null;
		return sVetName.substring(0,sVetName.indexOf(' '));
	}
	
	public String getVetLastName() throws IOException {
		String sVetName = getVeterinarianName();
		if( sVetName == null ) return null;
		return sVetName.substring(sVetName.lastIndexOf(' ')+1);
	}
	
	public java.util.Date getEstimatedShipDate() throws IOException {
		int iCol = parser.getLabelIdx("Estimated Ship Date");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else {
			String sDate = aCols.get(iCol);
			try {
				return df.parse(sDate);
			} catch (ParseException e) {
				logger.error(e);
				return null;
			}
		}
	}
	
	public String getTransportationMode() throws IOException {
		int iCol = parser.getLabelIdx("Transportation Mode");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}	
	
	public String getRemarks() throws IOException {
		int iCol = parser.getLabelIdx("Remarks");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}
	
	public String getPurpose() throws IOException {
		String sRet = "Interstate";
		int iCol = parser.getLabelIdx("Purpose");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else {
			String sPurpose = aCols.get(iCol);
			if( "Transit".equals(sPurpose) )
				sRet = "Interstate";
			else 
				sRet = sPurpose;
		}
		return sRet; 
	}
	
	public String getAnimalUnitType() throws IOException {
		int iCol = parser.getLabelIdx("Animal Unit Type");
		if( iCol < 0 || iCol >= aCols.size() )
			return null;
		else if( aCols.get(iCol).trim().length() == 0 )
			return null;
		else
			return  aCols.get(iCol);
	}
	
	public VspsCviEntity getOrigin() {
		return new VspsCviEntity( aCols, parser, "Origin");
	}
	
	public VspsCviEntity getDestination() {
		return new VspsCviEntity( aCols, parser, "Destination");
	}
	
	public VspsCviEntity getConsignee() {
		return new VspsCviEntity( aCols, parser, "Consignee");
	}
	
	public VspsCviEntity getConsignor() {
		return new VspsCviEntity( aCols, parser, "Consignor");
	}
	
	public VspsCviEntity getCarrier() {
		return new VspsCviEntity( aCols, parser, "Carrier");
	}

	
	public VspsCviAnimal nextAnimal() {
		if( iCurrent >= lAnimals.size() )
			return null;
		else
			return lAnimals.get(iCurrent++);
	}

}
