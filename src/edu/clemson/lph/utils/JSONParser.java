package edu.clemson.lph.utils;

/*
Copyright 2016 Michael K Martin

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

import java.io.Reader;
import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;


public class JSONParser {
	private static String sTest = "{\"access_token\":\"CUw-XhlRM--wwejuEo7qDtq91u_bmvZwqBCxieb1_m4P7b2xz0JtoBr1jeCVXaZLfAZwaOufPI1OeDJ3smhKCRCasxGfe34dNjXOs8laahVNa9UrlzHlURDo95joIiTYCX6PXCjxtbgS7GVu9tSpemo7rnm_6MIZZjxTDiZ0EmpjyycM9QPi6VgWRA3Jz23Gh-vvQ55Mv3WavrMInw8HZdmaOrs0jLu-CiF8Akp0EeXwVkhrbbuIL8ToWhG9xBR4UfbxFgQMdWfjpR2BRupeXMtnrpJsbEptu65e3_eU7bV53bSEJg9PMcSbVTgMLaXCLILsVIS1jS4MMwKquz9_V-kmsN93VxxRwht8SddVo52vsWf1Ec7SN51hL3GfKwNbvB_XmW0XfsP0yG539kShtg\",\"token_type\":\"bearer\",\"expires_in\":7199}";
	private JsonObject obj = null;
	
	public JSONParser( String sJSON ) {
		Reader reader = new StringReader(sJSON);
		JsonReader rdr = Json.createReader(reader);
		obj = rdr.readObject();
	}
	
	public void print() {
		for( String sKey : obj.keySet() ) {
			System.out.println( sKey + " = " + get(sKey) );
		}
		System.out.println( "One more second is " + (getInt("expires_in")+1));
// Save this to remind myself how to process arrays.
//		for (JsonObject result : results.getValuesAs(JsonObject.class)) {
//		     System.out.print(result.getJsonObject("from").getString("name"));
//		     System.out.print(": ");
//		    System.out.println(result.getString("message", ""));
//		    System.out.println("-----------");
//		}
	}
	
	public String get( String sKey ) {
		String sRet = null;
		// This is a stupid work-around.  
		try {
			sRet = obj.getString(sKey);
		} catch( ClassCastException e ) {
			JsonNumber nValue = obj.getJsonNumber(sKey);
			int iValue = nValue.intValue();
			sRet = Integer.toString(iValue);
		}
		return sRet;
	}

	public int getInt( String sKey ) {
		int iRet = -1;
		// This is a stupid work-around.  
		try {
			JsonNumber nValue = obj.getJsonNumber(sKey);
			iRet = nValue.intValue();
		} catch( ClassCastException e ) {
			e.printStackTrace(); 
		}
		return iRet;
	}

	public static void main(String[] args) {
		JSONParser me = new JSONParser(sTest);
		me.print();
	}

}
