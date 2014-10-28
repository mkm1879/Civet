package edu.clemson.lph.civet.xml;
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
import java.util.HashMap;

public class PurposeTranslator {
	public static HashMap<String, String> mHerds2Std = new HashMap<String, String>();
	public static HashMap<String, String> mStd2Herds = new HashMap<String, String>();
	
	static {
		mHerds2Std.put("Sale", "sale");
		mHerds2Std.put("Interstate", "other");
		mHerds2Std.put("Show/Exhibition", "show");
		mHerds2Std.put("Slaughter", "slaughter");
		mHerds2Std.put("Other", "other");
		mHerds2Std.put("Breeding", "breeding");
		mHerds2Std.put("Feeding", "feeding");
		mHerds2Std.put("Group Lot", "other");
		mHerds2Std.put("Veterinary Treatment", "medicalTreatment");
		mHerds2Std.put("Training", "training");
		mHerds2Std.put("Racing", "race");
		mHerds2Std.put("Rodeo", "race");
		mHerds2Std.put("Unknown", "other");	
		
        mStd2Herds.put("show", "Show/Exhibition");
        mStd2Herds.put("race", "Racing");
        mStd2Herds.put("rodeo", "Rodeo");
        mStd2Herds.put("sale", "Sale");
        mStd2Herds.put("pet", "Other");
        mStd2Herds.put("breeding", "Breeding");
        mStd2Herds.put("feeding", "Feeding");
        mStd2Herds.put("grazing", "Other");
        mStd2Herds.put("training", "Training");
        mStd2Herds.put("slaughter", "Slaughter");
        mStd2Herds.put("medicalTreatment", "Veterinary Treatment");
        mStd2Herds.put("other", "Other");
	}
	
	public static String herds2Std( String sHerds ) {
		String sRet = null;
		sRet = mHerds2Std.get(sHerds);
		if( sRet == null ) sRet = "other";
		return sRet;
	}
	public static String std2Herds( String sStd ) {
		String sRet = null;
		sRet = mStd2Herds.get(sStd);
		if( sRet == null ) sRet = "Unknown";
		return sRet;
	}

	private PurposeTranslator() {
		// TODO Auto-generated constructor stub
	}

}
