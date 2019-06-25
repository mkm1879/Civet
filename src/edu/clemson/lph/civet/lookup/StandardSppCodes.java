package edu.clemson.lph.civet.lookup;

import java.util.ArrayList;
import java.util.HashMap;

public class StandardSppCodes {
	private ArrayList<String> stdCodes;
//	private HashMap<String, String> text2Spp;
	private HashMap<String, String> code2Spp;

	public StandardSppCodes() {
//		text2Spp = new HashMap<String, String>();
		code2Spp = new HashMap<String, String>();
		stdCodes = new ArrayList<String>();
		
        stdCodes.add( "AQU" );
        stdCodes.add( "BEF" );
        stdCodes.add( "BIS" );
        stdCodes.add( "BOV" );
        stdCodes.add( "CAM" );
        stdCodes.add( "CAN" );
        stdCodes.add( "CAP" );
        stdCodes.add( "CER" );
        stdCodes.add( "CHI" );
        stdCodes.add( "DAI" );
        stdCodes.add( "EQU" );
        stdCodes.add( "FEL" );
        stdCodes.add( "OVI" );
        stdCodes.add( "POR" );
        stdCodes.add( "TUR" );
//
//        text2Spp.put( "Aquaculture", "AQU" );
//        text2Spp.put( "Beef", "BEF" );
//        text2Spp.put( "Bison", "BIS" );
//        text2Spp.put( "Bovine (Bison and Cattle) DEPRECATED", "BOV" );
//        text2Spp.put( "Camelid (Alpacas, Llamas, etc.)", "CAM" );
//        text2Spp.put( "Canine", "CAN" );
//        text2Spp.put( "Caprine (Goats)", "CAP" );
//        text2Spp.put( "Cervids", "CER" );
//        text2Spp.put( "Chickens", "CHI" );
//        text2Spp.put( "Dairy", "DAI" );
//        text2Spp.put( "Equine (Horses, Mules, Donkeys, Burros)", "EQU" );
//        text2Spp.put( "Feline", "FEL" );
//        text2Spp.put( "Ovine (Sheep)", "OVI" );
//        text2Spp.put( "Porcine (Swine)", "POR" );
//        text2Spp.put( "Turkeys", "TUR" );

        code2Spp.put( "AQU", "Aquaculture" );
        code2Spp.put( "BEF", "Beef" );
        code2Spp.put( "BIS", "Bison" );
        code2Spp.put( "BOV", "Bovine (Bison and Cattle) DEPRECATED" );
        code2Spp.put( "CAM", "Camelid (Alpacas, Llamas, etc.)" );
        code2Spp.put( "CAN", "Canine" );
        code2Spp.put( "CAP", "Caprine (Goats)" );
        code2Spp.put( "CER", "Cervids" );
        code2Spp.put( "CHI", "Chickens" );
        code2Spp.put( "DAI", "Dairy" );
        code2Spp.put( "EQU", "Equine (Horses, Mules, Donkeys, Burros)" );
        code2Spp.put( "FEL", "Feline" );
        code2Spp.put( "OVI", "Ovine (Sheep)" );
        code2Spp.put( "POR", "Porcine (Swine)" );
        code2Spp.put( "TUR", "Turkeys" );
	}
	
	public boolean isCodeStandard( String sSppCode ) {
		boolean bRet = false;
		if( stdCodes.contains(sSppCode)  )
			bRet = true;
		return bRet;
	}
	
	public ArrayList<String> getStdCodes() {
		return stdCodes;
	}
	
	public String getNameForCode( String sSpeciesCode ) {
		return code2Spp.get(sSpeciesCode);
	}

}
