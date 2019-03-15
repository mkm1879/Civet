package edu.clemson.lph.civet.prefs;

class ConfigEntry {
	String sName;
	boolean bMandatory;
	String sValue;
	String sType;
	String sHelp;
	
	ConfigEntry(String sName, boolean bMandatory, String sValue,	String sType, String sHelp) {
		this.sName = sName;
		this.bMandatory = bMandatory;
		this.sValue = sValue;
		this.sType = sType;
		this.sHelp = sHelp;
	}

}
