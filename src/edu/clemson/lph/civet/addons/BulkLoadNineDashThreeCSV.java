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
import edu.clemson.lph.civet.AddOn;
import edu.clemson.lph.civet.CSVFilter;
import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetConfig;
import edu.clemson.lph.civet.lookup.SpeciesLookup;
import edu.clemson.lph.civet.webservice.CivetWebServices;
import edu.clemson.lph.civet.xml.CviMetaDataXml;
import edu.clemson.lph.civet.xml.StdeCviXml;
import edu.clemson.lph.civet.xml.StdeCviXmlBuilder;
import edu.clemson.lph.db.*;
import edu.clemson.lph.dialogs.*;
import edu.clemson.lph.utils.PremCheckSum;
import edu.clemson.lph.utils.StringUtils;

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.swing.*;

import org.apache.log4j.*;
import org.w3c.dom.Element;

public class BulkLoadNineDashThreeCSV implements ThreadListener, AddOn {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private StdeCviXml stdXml;
	private String sCVINbrSource = CviMetaDataXml.CVI_SRC_9dash3;
	private static final String sProgMsg = "Loading 9-3: ";
	
	public BulkLoadNineDashThreeCSV() {
	}

	public void import93CSV( Window parent ) {
		String sFilePath = "E:\\EclipseJava\\Civet\\NPIP93Data.csv";  //null;
	    JFileChooser fc = new JFileChooser();
	    fc.setCurrentDirectory(new File(CivetConfig.getBulkLoadDirPath()));
	    fc.setDialogTitle("Open NPIP 9-3 CSV File");
	    fc.setFileFilter( new CSVFilter() );
	    int returnVal = fc.showOpenDialog(parent);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
	      File file = fc.getSelectedFile();
	      sFilePath = file.getAbsolutePath();
	    }
	    else {
	    	return;
	    }
		importNineDashThreeCSVFile( parent, sFilePath );

	}
	
	private void importNineDashThreeCSVFile( Window parent, String sFilePath ) {
		ProgressDialog prog = new ProgressDialog(parent, "Civet", sProgMsg);
		prog.setAuto(true);
		prog.setVisible(true);
		TWork93CSV tWork = new TWork93CSV( prog, sFilePath, parent );
		tWork.start();
	}

	// Also create TWorkAddSpecies and TWorkAddPage
	class TWork93CSV extends Thread {
		String sFilePath;
		ProgressDialog prog;
		Window parent;
		CivetWebServices service;
		
		public TWork93CSV( ProgressDialog prog, String sFilePath, Window parent ) {
			this.prog = prog;
			this.sFilePath = sFilePath;
			this.parent = parent;
			service = new CivetWebServices();
		}
		
		private String formatMessage( int iRow, int iMax ) {
			String sOut = String.format("Record %d of %d imported", iRow, iMax);
			return sOut;
		}
		
		public void run() {
			// Create CSVNineDashThreeDataFile object from CSV file
			CSVNineDashThreeDataFile data;
			try {
				data = new CSVNineDashThreeDataFile( sFilePath );
				int iMax = data.size();
				prog.setMax(iMax);
				int iRow = 0;
				prog.setValue(iRow);
				prog.setMessage( formatMessage( iRow, iMax) );
					// Iterate over the CSV file
					while( data.nextRow() ) {
						prog.setMessage(sProgMsg + data.getCVINumber() );
						String sXML = buildXml( data );
			System.out.println(sXML);
						// Send it!
						String sRet = service.sendCviXML(sXML);
						if( sRet == null || !sRet.trim().startsWith("00") ) {
							logger.error( sRet, new Exception("Error submitting NPIP 9-3 spreadsheet CVI to USAHERDS: ") );
							MessageDialog.messageLater(parent, "Civet WS Error", "Error submitting to USAHERDS: " + sRet);
						}

					} // Next Row

			} catch (IOException e2) {
				// File Error on the data file to be read
				logger.error(e2);
				exitThread(false);
			}
			exitThread(true);
		}

		private void exitThread( boolean bSuccess ) {
			final boolean bDone = bSuccess;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					prog.setVisible(false);
					prog.dispose();
					if( bDone )
						onThreadComplete(TWork93CSV.this);
				}
			});
		}
		
		private String buildXml( CSVNineDashThreeDataFile data ) throws IOException {
			StdeCviXmlBuilder xmlBuilder = new StdeCviXmlBuilder(stdXml);
			String sCVINumber = data.getCVINumber();
			String sSpecies = data.getSpecies();
			String sSpeciesCode = null;
			if( sSpecies == null || sSpecies.trim().equalsIgnoreCase("Poultry") ) {
				sSpeciesCode = "OTH";
			}
			else if( sSpecies.trim().length() == 3 ) {
				sSpeciesCode = sSpecies;
			}
			else {
				SpeciesLookup spLu = new SpeciesLookup( StringUtils.toTitleCase(sSpecies.trim()), true );
				sSpeciesCode = spLu.getSpeciesCode();
				if( sSpeciesCode.equals("ERROR") ) {
					sSpeciesCode = "POU";
				}
			}
			String sConsignorState = data.getConsignorState();
			String sSourcePIN = data.getConsignorPIN();
			try {
				if( !PremCheckSum.isValid(sSourcePIN) ) {
					sSourcePIN = null;
				}
			} catch (Exception e1) {
				sSourcePIN = null;
			}
			if( sSourcePIN != null && sSourcePIN.trim().length() == 8 ) {
				sConsignorState = sSourcePIN.substring(0, 2);
			}
			else if( sConsignorState == null || sConsignorState.trim().length() == 0 )
				sConsignorState = CivetConfig.getHomeStateAbbr();
			
			String sConsigneeState = data.getConsigneeState();
			String sDestinationPIN = data.getConsigneePIN();
			try {
				if( !PremCheckSum.isValid(sDestinationPIN) ) {
					sDestinationPIN = null;
				}
			} catch (Exception e1) {
				sDestinationPIN = null;
			}
			if( sDestinationPIN != null && sDestinationPIN.trim().length() == 8 ) {
				sConsigneeState = sDestinationPIN.substring(0, 2);
			}
			else if( sConsigneeState == null || sConsigneeState.trim().length() == 0 )
				sConsigneeState = CivetConfig.getHomeStateAbbr();
			xmlBuilder.setCviNumber(sCVINumber);
			xmlBuilder.setIssueDate(data.getInspectionDate());
			xmlBuilder.setVet("NPIP");
			// Expiration date will be set automatically from getXML();
			xmlBuilder.setPurpose("other");
			// We don't enter the person name, normally  or add logic to tell prem name from person name.
			Element origin = xmlBuilder.setOrigin(sSourcePIN, data.getConsignorName(), sConsignorState );
			xmlBuilder.setAddress(origin, data.getConsignorStreet(), data.getConsignorCity(), sConsignorState, data.getConsignorZip());
			Element destination = xmlBuilder.setDestination(sDestinationPIN, data.getConsigneeName(), sConsigneeState );
			xmlBuilder.setAddress(destination, data.getConsigneeStreet(), data.getConsigneeCity(), sConsigneeState, data.getConsigneeZip());

			int iNum = data.getAnimalCount();
			String sGender = "Gender Unknown";
			xmlBuilder.addGroup(iNum, "Poultry Lot Under NPIP 9-3", sSpeciesCode, null, sGender);
			CviMetaDataXml metaData = new CviMetaDataXml();
			metaData.setCertificateNbr(sCVINumber);
			metaData.setBureauReceiptDate(data.getSavedDate());
			metaData.setErrorNote("NPIP 9-3 Form Spreadsheet");
			metaData.setCVINumberSource(sCVINbrSource);
//		System.out.println(metaData.getXmlString());
			xmlBuilder.addMetadataAttachement(metaData);
			return xmlBuilder.getXMLString();
		}

	
	}// end inner class TWorkSave
	


	@Override
	public void onThreadComplete( Thread thread ) {
		// TODO Auto-generated method stub
		// Do what needs to be done in GUI thread.
		if( thread instanceof TWork93CSV ) {
			System.out.println("Import Complete");
			//System.exit(1);
		}
	}

	@Override
	public String getMenuText() {
		return "Import NPIP 9-3 CSV File";
	}

	@Override
	public void execute(Window parent) {
		import93CSV(parent);
	}

}

