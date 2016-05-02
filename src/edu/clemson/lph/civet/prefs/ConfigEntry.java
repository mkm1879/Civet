package edu.clemson.lph.civet.prefs;

public class ConfigEntry {
	String sName;
	boolean bMandatory;
	String sValue;
	String sType;
	String sHelp;
	
	public ConfigEntry(String sName, boolean bMandatory, String sValue,	String sType, String sHelp) {
		this.sName = sName;
		this.bMandatory = bMandatory;
		this.sValue = sValue;
		this.sType = sType;
		this.sHelp = sHelp;
	}

}
