package edu.clemson.lph.utils;
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
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

/**
 * Gathering place for various type conversions and encodings.
 * @author mmarti5
 *
 */
public class Encodings  {

	/**
	 * Display the value of a byte in Hexadecimal
	 * @param b
	 * @return one character String
	 */
	static public String byteToHex(byte b) {
		// Returns hex String representation of byte b
		char hexDigit[] = {
				'0', '1', '2', '3', '4', '5', '6', '7',
				'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
		};
		char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
		return new String(array);
	}

	/**
	 * Display the value of a char as a Hexadecimal pair
	 * @param c
	 * @return two character String
	 */
	static public String charToHex(char c) {
		// Returns hex String representation of char c
		byte hi = (byte) (c >>> 8);
		byte lo = (byte) (c & 0xff);
		return byteToHex(hi) + byteToHex(lo);
	}

	/**
	 * Convert a String--normally XML--into Windows .Net format string (UTF-16 with Little Endian byte order)
	 * encoded in base64 binary String format.  Used to populate base64Binary fields in .Net generated SOAP 
	 * services.  Edit the WSDL prior to code generation to accept a string instead of a base64Binary type
	 * because SOAP would generate that in UTF-8.
	 * @param sXML
	 * @return String in base64
	 */
	public static String getBase64Utf16Le( String sXML ) {
		String sBase64 = null;
		byte[] bytes;
		try {
			bytes = sXML.getBytes("UTF-16LE");  // Tested, specifying little endian really is essential.
			byte[] base64Bytes = Base64.encodeBase64(bytes);
			sBase64 = new String(base64Bytes);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sBase64;
	}

	/**
	 * Convert a String--normally XML--into normal base64Binary format string (UTF-8).
	 * Used for most cases of base64 encoding of content to include in SOAP or other XML 
	 * content.
	 * @param sXML
	 * @return String in base64
	 */
	public static String getBase64Utf8( String sXML ) {
		String sBase64 = null;
		byte[] bytes;
		try {
			bytes = sXML.getBytes("UTF-8");
			byte[] base64Bytes = Base64.encodeBase64(bytes);
			sBase64 = new String(base64Bytes);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sBase64;
	}

} // class
