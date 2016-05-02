package edu.clemson.lph.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CountyUtils {
	//TODO Replace with call to Prefs once operational.
	static private String sZipFile = ".\\ZipTable.csv";
	static private LabeledCSVParser parser;
	static private HashMap<String,String> zipMap;
	static private HashMap<String,ArrayList<String>> stateCountiesMap;
	
	public CountyUtils() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		try {
			System.out.println( CountyUtils.getCounty("29115-6909") );
			ArrayList<String>aCounties = CountyUtils.listCounties("SC");
			int i = 0;
			for( String sCounty : aCounties ) {
//				System.out.println(sCounty);
				i++;
			}
			System.out.println(i);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static String getCounty( String sZip ) throws IOException {
		String sRet = null;
		if( sZip == null || sZip.trim().length() < 5 ) return null;
		if( sZip.length() > 5 )
			sZip = sZip.trim().substring(0,5);
		if( zipMap == null )
			loadZips();
		sRet = zipMap.get(sZip);
		return sRet;
	}
	
	public static ArrayList<String> listCounties( String sState) throws IOException {
		ArrayList<String> aRet = null;
		if( sState == null || sState.trim().length() < 2 ) return null;
		if( sState.length() > 2 )
			sState = sState.trim().substring(0,2);
		if( stateCountiesMap == null )
			loadZips();
		aRet = stateCountiesMap.get(sState);
		return aRet;
	}
	
	private static void loadZips() throws IOException {
		parser = new LabeledCSVParser(sZipFile);
		zipMap = new HashMap<String,String>();
		stateCountiesMap = new HashMap<String,ArrayList<String>>();
		List<String> row = parser.getNext();
		while( row != null ) {
			if( row.size() < 3 ) {
				throw new IOException( "Invalid row in " + sZipFile);
			}
			String sZip = row.get(0);
			String sState = row.get(1);
			String sCounty = row.get(2);
			zipMap.put(sZip, sCounty);
			ArrayList<String>aCounties = stateCountiesMap.get(sState);
			if( aCounties == null ) {
				aCounties = new ArrayList<String>();
				stateCountiesMap.put(sState, aCounties);
			}
			if( !aCounties.contains(sCounty) )
				aCounties.add(sCounty);
			row = parser.getNext();
		}
	}

}
