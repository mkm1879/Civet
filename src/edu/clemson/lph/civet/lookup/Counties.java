package edu.clemson.lph.civet.lookup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.utils.FileUtils;
import edu.clemson.lph.utils.LabeledCSVParser;

public class Counties  {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private static HashMap<String, ArrayList<String>> counties = null;
	private static HashMap<StatePlusAlias, String> aliasMap = null;
	private ArrayList<String> rows = null;

	public Counties() throws IOException {
			loadCounties();
			loadAliases();
	}
	
	public boolean setState( String sState ) {
		boolean bRet = false;
		if( sState == null || sState.length() != 2 ) {
			rows = new ArrayList<String>();
			bRet = true;
		}
		else {
			rows = counties.get(sState);
			if( rows == null ) 
				logger.error("Invalid value in Counties.setState " + sState );
			else
				bRet = true;
		}
		return bRet;
	}
	
	public static ArrayList<String> getCounties( String sState ) {
		if( counties == null ) {
			try {
				loadCounties();
			} catch (IOException e) {
				logger.error(e);
				return null;
			}
		}
		ArrayList<String> rows = counties.get(sState);
		return rows;
	}
	
	public static boolean isHerdsCounty( String sState, String sCounty ) {
		boolean bRet = false;
		if( counties == null ) {
			try {
				loadCounties();
			} catch (IOException e) {
				logger.error(e);
				return false;
			}
		}
		ArrayList<String> row = counties.get(sState);
		if( row != null ) {
			bRet = row.contains(sCounty);
		}
		return bRet;
	}
	
	public static String getHerdsCounty(String sState, String sCounty) {
		String sRet = null;
		if( sState == null || sCounty == null || sState.trim().length() == 0 || sCounty.trim().length() == 0 )
			return sRet;
		sState = sState.toUpperCase();
		if( sState.length() > 2 )
			sState = States.getStateCode(sState);
		sCounty = sCounty.toUpperCase();
		if( aliasMap == null ) {
			try {
				loadAliases();
			} catch (IOException e) {
				logger.error(e);
				return null;
			}
		}
		if( isHerdsCounty( sState, sCounty ) )
			sRet = sCounty;
		else {
			StatePlusAlias key = new StatePlusAlias( sState, sCounty );
			sRet = aliasMap.get(key);
			if( sRet == null ) {
				String sLine = sState + ", " + sCounty + System.getProperty("line.separator");
				FileUtils.writeTextFile(sLine, "./BadCounties.txt", true);
			}
		}
		return sRet;
	}
	
	
	private static void loadCounties() throws IOException {
		String sCountiesFile = CivetConfig.getCountiesTableFile();
		if( sCountiesFile == null ) 
			throw new IOException( "No Counties File Specified" );
		LabeledCSVParser parser = new LabeledCSVParser(sCountiesFile);
		counties = new HashMap<String, ArrayList<String>>();
		List<String> row = parser.getNext();
		while( row != null ) {
			if( row.size() < 2 ) {
				throw new IOException( "Invalid row in " + sCountiesFile);
			}
			String sState = row.get(0);
			String sCounty = row.get(1);
			ArrayList<String> aCounties = counties.get(sState);
			if( aCounties == null ) {
				aCounties = new ArrayList<String>();
				counties.put(sState, aCounties);
			}
			if( !aCounties.contains(sCounty) )
				aCounties.add(sCounty);
			row = parser.getNext();
		}
	}
	
	private static void loadAliases() throws IOException {
		String sAliasesFile = CivetConfig.getCountyAliasesTableFile();
		if( sAliasesFile == null ) 
			throw new IOException( "No Counties File Specified" );
		LabeledCSVParser parser = new LabeledCSVParser(sAliasesFile);
		aliasMap = new HashMap<StatePlusAlias, String>();
		List<String> row = parser.getNext();
		while( row != null ) {
			if( row.size() < 3 ) {
				throw new IOException( "Invalid row in " + sAliasesFile);
			}
			String sState = row.get(0);
			String sAlias = row.get(1);
			String sCounty = row.get(2);
			StatePlusAlias key = new StatePlusAlias( sState, sAlias );
			aliasMap.put(key, sCounty);
			row = parser.getNext();
		}
	}
	
	public static void main( String args[] ) {
		PropertyConfigurator.configure("CivetConfig.txt");
		CivetConfig.checkAllConfig();
		Counties c = null;
		try {
			c = new Counties();
		} catch (IOException e) {
			logger.error(e);
		}
//		System.out.println(c.getHerdsCounty("FL", "MIAMI-DADE"));
//		if( c != null ) {
//			for( String sCounty : c.getCounties("SC") ) 
//				System.out.println(sCounty);
//			for( String sCounty : c.getCounties("AK") ) 
//				System.out.println(sCounty);
//		}
	}

	static class StatePlusAlias {
		public String sState;
		public String sAlias;
		public StatePlusAlias( String sState, String sAlias ) {
			this.sState = sState;
			this.sAlias = sAlias;
		}
		public boolean equals( Object o ) {
			if( !(o instanceof StatePlusAlias) )
				return false;
			StatePlusAlias other = (StatePlusAlias)o;
			if( other.sAlias == null || other.sState == null ) {
				logger.error("null value in StatePlusAlias");
				return false;  // Should never happen.
			}
			return other.sState.equals(this.sState) && other.sAlias.equals(this.sAlias);
		}
		/**
		 * Note:  This would be implemented as Objects.getHash() but that was only added in Java 1.7
		 * This variant should be a Java 6 safe alternative.
		 */
		public int hashCode() {
			String sStateAlias = sState + sAlias;
			return sStateAlias.hashCode();
		}
	}
}
