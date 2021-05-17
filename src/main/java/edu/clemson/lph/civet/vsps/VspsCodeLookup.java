package edu.clemson.lph.civet.vsps;

import java.util.HashMap;

public class VspsCodeLookup {
	private static HashMap<String, String> hSpCodeLu = new HashMap<String, String>();
	private static HashMap<String, String> hGenderCodeLu = new HashMap<String, String>();
	private static HashMap<String, String> hPurposeCodeLu = new HashMap<String, String>();

	static {
	hSpCodeLu.put("Cattle", "BOV");
	hSpCodeLu.put("Not Applicable", "NA");
	hSpCodeLu.put("Bovine", "BOV");
	hSpCodeLu.put("Equine", "EQU");
	hSpCodeLu.put("Swine", "POR");
	hSpCodeLu.put("Porcine", "POR");
	hSpCodeLu.put("Goat", "CAP");
	hSpCodeLu.put("Caprine", "CAP");
	hSpCodeLu.put("Cervidae", "CER");
	hSpCodeLu.put("Deer", "DEE");
	hSpCodeLu.put("Duck", "DUC");
	hSpCodeLu.put("Rabbit", "LAG");
	hSpCodeLu.put("Sheep", "OVI");
	hSpCodeLu.put("Ovine", "OVI");
	hSpCodeLu.put("Turkey", "TUR");
	hSpCodeLu.put("Chicken", "CHI");
	hSpCodeLu.put("Avian", "AVI");
	hSpCodeLu.put("Camelid", "CAM");
	hSpCodeLu.put("White-Tailed Deer", "WTD");
	hSpCodeLu.put("Elk", "ELK");
	hSpCodeLu.put("Other", "OTH");
	hSpCodeLu.put("Poultry", "POU");
	hSpCodeLu.put("Pigeon", "PGN");
	hSpCodeLu.put("Cattle Beef", "BEF");
	// Gender
	hGenderCodeLu.put("Intact Male", "Male");
	hGenderCodeLu.put("Castrated Male", "Neutered Male");
	hGenderCodeLu.put("Female", "Female");
	hGenderCodeLu.put("Spayed Female", "Spayed Female");
	// Purposes
	hPurposeCodeLu.put("Breeding",  "Breeding");
	hPurposeCodeLu.put("Competition",  "Competition"); 
	hPurposeCodeLu.put("Feeding",  "Feeding to condition");
	hPurposeCodeLu.put("Finishing",  "Feeding to slaughter");
	hPurposeCodeLu.put("Medical Treatment",  "Medical Treatment");
	hPurposeCodeLu.put("Pleasure",  "Companion Animal");
	hPurposeCodeLu.put("Production",  "Other");
	hPurposeCodeLu.put("Race Track",  "Racing");
	hPurposeCodeLu.put("Rodeo",  "Exhibition/Show/Rodeo");
	hPurposeCodeLu.put("Sale",  "Sale");
	hPurposeCodeLu.put("Show/Exhibition",  "Exhibition/Show/Rodeo");
	hPurposeCodeLu.put("Transit",  "Personal Travel/Transit");

	}
	
	public static String getSpCode( String sSpecies ) {
		String sRet;
		sRet = hSpCodeLu.get(sSpecies);
		if( sRet == null ) sRet = "OTH";
		return sRet;
	}
	
	public static String getGenderCode( String sGender ) {
		String sRet;
		sRet = hGenderCodeLu.get(sGender);
		if( sRet == null ) sRet = "Gender Unknown";
		return sRet;
	}
	
	public static String getPurposeCode( String sPurpose ) {
		String sRet;
		sRet = hPurposeCodeLu.get(sPurpose);
		if( sRet == null ) sRet = "Other";
		return sRet;
	}
}
