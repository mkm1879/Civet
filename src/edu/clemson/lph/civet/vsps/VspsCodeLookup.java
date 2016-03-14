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
	hPurposeCodeLu.put("Breeding", "breeding");
	hPurposeCodeLu.put("Competition", "other");
	hPurposeCodeLu.put("Grazing", "grazing");
	hPurposeCodeLu.put("Medical Treatment", "medicalTreatment");
	hPurposeCodeLu.put("Pleasure","other");
	hPurposeCodeLu.put("Race Track", "medicalTreatment");
	hPurposeCodeLu.put("Rodeo", "rodeo");
	hPurposeCodeLu.put("Sale", "sale");
	hPurposeCodeLu.put("Show/Exhibition", "show");
	hPurposeCodeLu.put("Transit", "other");
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
		if( sRet == null ) sRet = "other";
		return sRet;
	}
}
