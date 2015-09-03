package edu.clemson.lph.utils;

import edu.clemson.lph.civet.CivetConfig;

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
/**
 * <p>Title: PremCheckSum</p>
 * <p>Description: ISO7064 Mod37c36 Checksum For NAIS Prem IDs</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: APHIS, VS</p>
 * @author Michael K Martin
 * @version 1.0
 */

public class PremCheckSum {
  private static char[] char36 = {'0','1','2','3','4','5','6','7','8','9',
                                  'A','B','C','D','E','F','G','H','I','J',
                                  'K','L','M','N','O','P','Q','R','S','T',
                                  'U','V','W','X','Y','Z'};

  /**
   * This class is not instantiable.  Only used as static method container.
   */
  private PremCheckSum() {
  }

  private static int lookup( char cIn ) {
    for( int i = 0; i < char36.length; i++ ) {
      if( char36[i] == cIn ) return i;
    }
    return -1;
  }

  /**
   * Calculate a checksum character based upon the identifier less checksum
   * @param sID String Identifier without checksum
   * @throws Exception If the identifier contains characters other than digits
   * or capital letters.
   * @return char Checksum
   */
  public static char getChecksum( String sID ) throws Exception {
    int pj = 36;
    int sj = 0;
    for( int i = 0; i < sID.length(); i++ ) {
      char cNext = sID.charAt(i);
      int iNext = lookup( cNext );
      if( iNext == -1 ) throw new Exception( "Character " + cNext + " is not valid in ID" );
      sj = pj + iNext;
      sj = sj % 36; if( sj == 0 ) sj = 36;
      pj = ( sj * 2 ) % 37;
    }
    sj = ( 37 - pj ) % 36;
    if( sj < 0 || sj >= char36.length ) throw new Exception( "Invalid numerical result: " + sj );
    return char36[sj];
  }

  /**
   * Check the identifier with checksum for validity.
   * @param sID String Identifier with checksum
   * @throws Exception If the identifier contains characters other than digits
   * or capital letters.
   * @return boolean true if last character is correct checksum.
   */
  public static boolean isValid( String sID ) throws Exception {
    if( sID == null || sID.trim().length() == 0 || sID.startsWith("*") ) return true;  // Ignore temporary numbers
    if( sID.trim().length() < 7 || sID.trim().length() > 8 ) return false;
    if( sID.trim().length() == 8 && !CivetConfig.hasStateIDChecksum() ) return true;  // Ignore State IDs in states without checksums
    if( sID.startsWith("OO") ) return false; // 'O' conflicts with 0 and is not allowed any longer
    char cCheckSum2 = sID.charAt( sID.length() -1 );
    String sID2 = sID.substring( 0, sID.length() - 1 );
    char cCheckSum = getChecksum( sID2 );
    return cCheckSum == cCheckSum2;
  }

  /**
   * Try all iterations of zero to O, one to L, one to I, etc. and return first validated checksum
   * @param sID String string with invalid checksum
   * @return String with valid checksum or null if none found.
   */
  public static String findTypo( String sID ) {
    return findTypo( sID, 0 );
  }

  /**
   * The actual implementation is recursive and private.
   * @param sID String
   * @param iChar int
   * @return String
   */
  private static String findTypo( String sID, int iChar ) {
    try {
      if (sID == null || iChar >= sID.length())return null; // failed to find a valid id
      String sIDOut = findTypo(sID, iChar + 1);
      if (isValid(sID))
        return sID;
      if (sIDOut != null) // Found a valid
        return sIDOut;
      char[] cMatch = {'o','0','L','I','1','z','2',};
      char[][] cSubs = {{'0'},{'o'},{'1','I'},{'1','L'},{'I','L'},{'2'},{'Z'}};
      int iMatchChar = -1;
      for( int i = 0; i < cMatch.length; i++ ) {
        if( sID.charAt(iChar) == Character.toUpperCase( cMatch[i] ) ) {
          iMatchChar = i;
          break;
        }
      }
      if (iMatchChar < 0)
        return null;
      StringBuffer sb = new StringBuffer(sID);
      for( int j = 0; j < cSubs[iMatchChar].length; j++ ) {
        sb.setCharAt(iChar, Character.toUpperCase( cSubs[iMatchChar][j] ) );
        String sSubID = sb.toString();
        if (isValid(sSubID))
          return sSubID;
        String sOut = findTypo(sSubID, iChar + 1);
        if( sOut != null )
          return sOut;
      }
      return null;
    }
    catch( Exception e ) {
      e.printStackTrace();
      return null;
    }

  }
  
  public static void main( String args[] ) {
	  System.out.println( findTypo( "IN73007P") );
  }
  

} // End class PremCheckSum
