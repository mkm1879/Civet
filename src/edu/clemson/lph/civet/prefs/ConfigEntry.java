package edu.clemson.lph.civet.prefs;

public class ConfigEntry {
	String sName;
	String sValue;
	String sType;
	String sHelp;
	
	public ConfigEntry(String sName, String sValue,	String sType, String sHelp) {
		this.sName = sName;
		this.sValue = sValue;
		this.sType = sType;
		this.sHelp = sHelp;
	}

}
