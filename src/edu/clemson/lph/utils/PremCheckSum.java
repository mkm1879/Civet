package edu.clemson.lph.utils;

import java.util.Random;

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
  
  /**
   * main only present for unit testing
   * This version does a complex round robin looking for collisions.  
   * The algorithm works remarkably well.
   * @param args String[]
   */
  public static void main(String[] args) {
    StringBuffer sb = new StringBuffer();
    Random rand = new Random();
    // Generate 1000 ID strings and calculate a valid checksum for each
    for( int iRound = 0; iRound < 1000; iRound++ ) {
      sb.setLength(0);
      for( int iChar = 0; iChar < 6; iChar++ ) {
        int iRand = rand.nextInt() % 36;
        if( iRand < 0 ) iRand *= -1;
        char cRand = char36[iRand];
        sb.append( cRand );
      }
      String sID = sb.toString();
      char cCheck = '0';
      int iCheck = 0;
      // Code will throw an exception if we use any invalid characters.
      try {
        cCheck = getChecksum(sID);
        iCheck = lookup( cCheck );
        // sPremID is our valid PremID
        String sPremID = sID + cCheck;
        // It would be weird to fail this one since we generated the checksum.
        if( !isValid(sPremID) ) {
          System.err.println( sPremID + " is not valid and should be." );
        }
        // The only normal output is the list of valid IDs
        System.out.println("Identifier with checksum = " + sPremID +
                           (isValid(sPremID) ? " is " : " is not ") + "valid");
        // Check for any one-character error that validates.  Should be none.
        for( int i = 0; i < 6; i++ ) {
          for (int j = 0; j < 36; j++) {
              StringBuffer sb2 = new StringBuffer( sPremID );
              if( char36[j] != sb2.charAt(i) ) {
                sb2.setCharAt(i, char36[j]);
                String sPremID2 = sb2.toString();
                if (isValid(sPremID2)) {
                  System.out.println("    One char typo " + sPremID2 +
                                     " is valid and shouldn't be.");
                }
                // Check for any two-character error that validates. Should be none.
                for (int k = 0; k < 6; k++) {
                  for (int l = 0; l < 36; l++) {
                    StringBuffer sb3 = new StringBuffer(sPremID);
                    if (l != j && char36[l] != sb3.charAt(k)) {
                      sb3.setCharAt(k, char36[l]);
                      String sPremID3 = sb3.toString();
                      if (isValid(sPremID3)) {
                        System.out.println("   Two char typo " + sPremID3 +
                                           " is valid and shouldn't be.");
                      }
                    }
                  } // End for each "typo"
                } // End for each character to change with "typo"

              }
          } // End for each "typo"
        } // End for each character to change with "typo"
        // Does any other checksum validate?  Should never.
        for( int j = 0; j < 36; j++ ) {
          if( j != iCheck ) {
            String sPremID3 = sID + char36[j];
            if (isValid(sPremID3)) {
              System.err.println(sPremID3 + " is valid and shouldn't be.");
            }
          }
        } // End for each invalid checksum
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }// End for each round
  }
  /**
   * main only present for unit testing
   * @param args String[]
   */
  public static void main2(String[] args) {
    String sID = "A234SEY";
    String sID2 = "A234SEZ";
    String sID3 = "003OR12";
    String sID4 = "003OR13";
    String sID5 = "000N5H0";
    String sID6 = "000N5HZ";
    String sID7 = "003E2UN";
    String sID8 = "0030R12";
    String sID9 = "SC00015";
    try {
      System.out.println("Lookup M = " + lookup( 'M' ) );
      System.out.println("Identifier with checksum = " + sID + (isValid( sID ) ? " is " : " is not ") + "valid");
      System.out.println("Identifier with checksum = " + sID2 + (isValid( sID2 ) ? " is " : " is not ") + "valid");
      System.out.println("Identifier with checksum = " + sID3 + (isValid( sID3 ) ? " is " : " is not ") + "valid");
      System.out.println("Identifier with checksum = " + sID4 + (isValid( sID4 ) ? " is " : " is not ") + "valid");
      System.out.println("Identifier with checksum = " + sID5 + (isValid( sID5 ) ? " is " : " is not ") + "valid");
      System.out.println("Identifier with checksum = " + sID6 + (isValid( sID6 ) ? " is " : " is not ") + "valid");
      System.out.println("Identifier " + sID7 + " is a typo of " + findTypo( sID7 ) );
      System.out.println("Identifier " + sID8 + " is a typo of " + findTypo( sID8 ) );
      System.out.println("Identifier with checksum = " + sID9 + (isValid( sID ) ? " is " : " is not ") + "valid");
    }catch( Exception e ) {
      e.printStackTrace();
    }
  } // End test main

} // End class PremCheckSum
