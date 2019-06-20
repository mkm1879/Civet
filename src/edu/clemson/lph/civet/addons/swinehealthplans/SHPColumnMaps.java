package edu.clemson.lph.civet.addons.swinehealthplans;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.utils.CSVParserWrapper;

public class SHPColumnMaps {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	HashMap<String, ArrayList<String>> hMaps = null;

	public SHPColumnMaps() {
		if( hMaps == null )
			readFile();
	}
	
	private void readFile() {
		File fMap = new File("SwineHealthPlanColumnMaps.csv");
		hMaps = new HashMap<String, ArrayList<String>>();
		FileReader fr = null;
		try {
			fr = new FileReader( fMap );
			CSVParserWrapper parser = new CSVParserWrapper(fr);
			List<String> aRow = null;
			int iCol = 0;
			// For each column read all the rows
			for( String sField : parser.getHeader() ) {
				ArrayList<String> aValues = new ArrayList<String>();
				// For each row, if it has a value, add to the list for that header
				while( (aRow = parser.getNext()) != null ) {
					String sValue = aRow.get(iCol);
					if( sValue != null && sValue.trim().length() > 0 )
						aValues.add(sValue.toUpperCase().trim());
				}
				hMaps.put(sField.toUpperCase().trim(), aValues);
//				System.out.print(sField.toUpperCase() + ": ");
//				for( String sValue : hMaps.get(sField.toUpperCase())) {
//					System.out.print(sValue + ", ");
//				}
//				System.out.println();
				iCol++;
				parser.reset();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error("File SwineHealthPlanColumnMaps.csv not found\n", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Error reading SwineHealthPlanColumnMaps.csv\n", e);
		} finally {
			try {
				if( fr != null )
					fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error(e);
			}
		}
	}
	
	public Set<String> getKeys() {
		return hMaps.keySet();
	}
	
	public ArrayList<String> getMappedHeaders( String sField ) {
		return hMaps.get(sField);
	}
}
