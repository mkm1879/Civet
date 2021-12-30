package edu.clemson.lph.civet.prefs;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import edu.clemson.lph.logging.Logger;


import edu.clemson.lph.utils.LabeledCSVParser;

class ConfigCsv {
      private static Logger logger = Logger.getLogger();
	private LabeledCSVParser parser;
	private List<List<List<String>>> tabs;
	private List<List<String>> currentTab;
	private List<String> currentRow;
	private int iTab = -1;
	private int iRow = -1;

	ConfigCsv() {
		InputStream isConfigCsv = this.getClass().getResourceAsStream("/edu/clemson/lph/civet/res/CivetConfig.csv");
		try {
			parser = new LabeledCSVParser( isConfigCsv );
			tabs = new ArrayList<List<List<String>>>();
			String sTab = null;
			while( (currentRow = parser.getNext()) != null ) {
				if( currentRow.size() < 1 ) continue;
				String sNextTab = currentRow.get(0);
				if( !sNextTab.equals(sTab) ) {
					sTab = sNextTab;
					currentTab = new ArrayList<List<String>>();
					tabs.add(currentTab);
				}
				currentTab.add(currentRow);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		} finally {
			try {
				isConfigCsv.close();
			} catch (Exception e) {
			}
		}
	}
	
	public String nextTab() {
		String sRet = null;
		iTab++;
		if( iTab >= tabs.size() ) return null;
		currentTab = tabs.get(iTab);
		if( currentTab.size() > 0 ) {
			iRow = 0;
			currentRow = currentTab.get(iRow);
			if( currentRow.size() > 0 )
				sRet = currentRow.get(0);
		}
		iRow = -1;
		return sRet;
	}
	
	public List<String> nextRow() {
		iRow++;
		if( iRow >= currentTab.size() )
			return null;
		currentRow = currentTab.get(iRow);
		return currentRow;
	}
	
	public int getCurrentTabSize() {
		if( currentTab == null ) return -1;
		return currentTab.size();
	}
	
	public String getTab() {
		if( currentRow == null || currentRow.size() < 1 ) return null;
		return currentRow.get(0);
	}
	public boolean isMandatory() {
		if( currentRow == null || currentRow.size() < 2 ) return false;
		String sMandatory = currentRow.get(1);
		return ("Y".equals(sMandatory));
	}
	public String getName() {
		if( currentRow == null || currentRow.size() < 3 ) return null;
		return currentRow.get(2);
	}
	public String getDefault() {
		if( currentRow == null || currentRow.size() < 4 ) return null;
		return currentRow.get(3);
	}
	public String getType() {
		if( currentRow == null || currentRow.size() < 5 ) return null;
		return currentRow.get(4);
	}
	public List<String> getChoices() {
		if( currentRow == null || currentRow.size() < 6 ) return null;
		List<String> lChoices = new ArrayList<String>();
		String sChoices = currentRow.get(5);
		StringTokenizer tok = new StringTokenizer(sChoices, ";");
		while( tok.hasMoreTokens()) {
			String s = tok.nextToken();
			lChoices.add(s);
		}
		return lChoices;
	}
	public String getDescription() {
		if( currentRow == null || currentRow.size() < 7 ) return null;
		return currentRow.get(6);
	}
	public String getHelpText() {
		if( currentRow == null || currentRow.size() < 8 ) return null;
		return currentRow.get(7);
	}
	
	/**
	 * Testing only (and example usage)
	 * @param args
	 */
	public static void main( String args[] ) {
		ConfigCsv me = new ConfigCsv();
		String sTab = null;
		while( (sTab = me.nextTab()) != null ) {
			System.out.println("Tab: " + sTab);
			while( me.nextRow() != null ) {
				System.out.println(me.getName()  +" = "+ me.getDefault() +" ("+ me.getDescription() + ")");
				for( String s : me.getChoices() ) 
					System.out.println( "\tChoice: " + s);
			}
		} 
		
	}

}
