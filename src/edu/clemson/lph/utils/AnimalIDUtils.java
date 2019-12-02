package edu.clemson.lph.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnimalIDUtils {
	private static final  String[] aTagPatterns = { 
			"(^((9[0-8]\\d)|(9\\d[0-8])|(124)|(484))\\d{11})($|[^\\d])",
			"(^((9[0-8]\\d)|(9\\d[0-8])|(124)|(484))\\d{12})($|[^\\d])",
			"(^((9[0-8]\\d)|(9\\d[0-8])|(124)|(484))\\d{13})($|[^\\d])",
			"(\\d{2}|MD|MN|NM|NY|US|WY)[A-Z]{3}\\d{4}",
			"((840)\\d{12})($|[^\\d])",
			"((840)\\d{11})($|[^\\d])",
			"((840)\\d{13})($|[^\\d])",
			"((USA)\\d{12})($|[^\\d])",
			"((USA)\\d{11})($|[^\\d])",
			"((USA)\\d{13})($|[^\\d])",
			"\\d{2}[A-Z]{2}\\d{4}"};
	private static final String[] aTagTypes = {"MfrRFID", "Short MfrRFID", "Long MfrRFID",
			"NUES9", "AIN", "Short AIN", "Long AIN", "USA", "Short USA", "Long USA", "NUES8"};

	public AnimalIDUtils() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main( String args[]) {
		String aTag[] = AnimalIDUtils.getIDandType( "microchip # 840123456789012 more");
		System.out.println( aTag[0] + " is a " + aTag[1] );
		System.out.println( AnimalIDUtils.isValid("982123456789012", "MfrRFID" ));
	}
	
	public static String[] getIDandType(String sIDin) {
		String aRet[] = new String[2];
		aRet[0] = sIDin;
		aRet[1] = "Unknown";
		for( int i = 0; i < aTagPatterns.length; i++ ) {
			Pattern pattern = Pattern.compile(aTagPatterns[i]);
			Matcher matcher = pattern.matcher(sIDin);
			if( matcher.find() ) {
				int iStart = matcher.start();
				int iEnd = matcher.end();
				String sID = sIDin.substring(iStart, iEnd);
				aRet[0] = sID;
				aRet[1] = aTagTypes[i];
			}
		}
		return aRet;
	}
	
	public static boolean isValid( String sValue, String sType ) {
		boolean bRet = false;
		int iIndex = -1;
		for( int i = 0; i < aTagTypes.length; i++ ) {
			if( aTagTypes[i].equals(sType) ) {
				iIndex = i;
				break;
			}
		}
		Pattern pattern = Pattern.compile(aTagPatterns[iIndex]);
		Matcher matcher = pattern.matcher(sValue);
		if( matcher.find() ) {
			bRet = true;
		}
		return bRet;
	}

}
