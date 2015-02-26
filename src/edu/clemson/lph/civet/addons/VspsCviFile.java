package edu.clemson.lph.civet.addons;
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
import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.clemson.lph.civet.AddOn;
import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetConfig;
import edu.clemson.lph.db.DatabaseConnectionFactory;
import edu.clemson.lph.utils.LabeledCSVParser;

public class VspsCviFile implements AddOn {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	
	private LabeledCSVParser parser = null;
	private List<String> aCols = null;
	private DatabaseConnectionFactory factory = null;


	/**
	 * 
	 */
	public VspsCviFile() {
	}

	// To be called from CVIHerdsFiler frame
	public void importVspsFile(Window parent) {
		File fDir = new File( "E:\\EclipseJava" );
		JFileChooser open = new JFileChooser( fDir );
		open.setDialogTitle("Civet: Open PDF File");
		open.setFileSelectionMode(JFileChooser.FILES_ONLY);
		open.setFileFilter(new FileNameExtensionFilter(
		        "CSV Files", "csv"));
		open.setMultiSelectionEnabled(false);
		int resultOfFileSelect = open.showOpenDialog(parent);
		if(resultOfFileSelect==JFileChooser.APPROVE_OPTION){
			File fIn = open.getSelectedFile();
			saveme(parent, factory, fIn);
//		    vsps.printme();
		}

	}
	
	private void saveme(Window parent, DatabaseConnectionFactory factory, File fIn) {
		try {
			CSVParser parserIn = new CSVParser( new FileReader(fIn), CSVFormat.EXCEL );
			parser = new LabeledCSVParser( parserIn );
			aCols = parser.getNext();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage() + "\nCould not read file: " + fIn.getName() );
		} catch (IOException e) {
			logger.error(e.getMessage() + "\nCould not read file: " + fIn.getName() );
		}
		InsertVspsCviThread thread = new InsertVspsCviThread( parent, factory, this );
		thread.start();
	}
	
	public static void main( String args[] ) {
		// BasicConfigurator replaced with PropertyConfigurator.
		PropertyConfigurator.configure("CivetConfig.txt");
		// Fail now so config file and required files can be fixed before work is done.
		CivetConfig.checkAllConfig();
		logger.setLevel(CivetConfig.getLogLevel());
		VspsCviFile me = new VspsCviFile();
		me.printme(new File("cviExport_VSPS_ComplexRemarks.csv"));
	}
	
	/**
	 * Test only
	 */
	@SuppressWarnings("unused")
	private void printme(File fIn) {
		try {
			CSVParser parserIn = new CSVParser( new FileReader(fIn), CSVFormat.EXCEL );
			parser = new LabeledCSVParser( parserIn );
			aCols = parser.getNext();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage() + "\nCould not read file: " + fIn.getName() );
		} catch (IOException e) {
			logger.error(e.getMessage() + "\nCould not read file: " + fIn.getName() );
		}
		VspsCvi cvi;
		try {
			while( (cvi = nextCVI() ) != null ) {
				if( cvi.getStatus().equals("SAVED") )
					continue;
				VspsCviEntity orig = cvi.getOrigin();
				VspsCviEntity dest = cvi.getDestination();
				System.out.println( cvi.getCVINumber() + " created: " + cvi.getCreateDate() );
				System.out.println( "  origin = " + orig.getName()+" "+ orig.getPhone()+" "+ orig.getAddress1() );
				System.out.println( "  destination = " + dest.getName()+" "+ dest.getPhone()+" "+ dest.getAddress1());
				System.out.println( cvi.getOriginState() + " " + orig.getState() );
				System.out.println( cvi.getVeterinarianName() + ": " + cvi.getVetFirstName() + " " + cvi.getVetLastName() );
				System.out.println( cvi.getAnimals().size() + " Animals in CVI");
				System.out.println(cvi.getRemarks());
				for( List<String> aKey : cvi.getSpecies().keySet() ) {
					Integer iCount = cvi.getSpecies().get(aKey);
					System.out.println( iCount + " " + aKey.get(0) + " (" + aKey.get(1) + ")" );
				}
				for( VspsCviAnimal animal : cvi.getAnimals() ) {
					System.out.println( "\t" + animal.getSpecies() + " " + animal.getBreed() + " " + animal.getGender()+ " " + animal.getDateOfBirth());
					for( int i = 1; i <= 5; i++ ) {
						String sIdType = animal.getIdentifierType(i);
						if( sIdType != null )
							System.out.println( "\t\t" + sIdType + " = " + animal.getIdentifier(i));
					}
				}
			}
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	public VspsCvi nextCVI() throws IOException {
		if( aCols == null ) return null;
		VspsCvi thisCVI = new VspsCvi( aCols, parser );
		String sCVINumber = thisCVI.getCVINumber();
		String sNextCVINumber = sCVINumber;		
		do {
			VspsCviAnimal animal = new VspsCviAnimal( aCols, parser );
			thisCVI.addCVIAnimal(animal);
			aCols = parser.getNext();
			if( aCols == null ) break;
			sNextCVINumber = aCols.get(parser.getLabelIdx("Certificate Number"));
		} while( sCVINumber.equals(sNextCVINumber) );
		parser.back();
		return thisCVI;
	}

	@Override
	public String getMenuText() {
		return "Import VSPS eCVI CSV File";
	}

	@Override
	public void execute(Window parent) {
		if( factory == null )
			factory = InitAddOns.getFactory();
		importVspsFile(parent);	}

}
