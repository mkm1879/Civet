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
import edu.clemson.lph.civet.lookup.VetLookup;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.webservice.CivetWebServices;
import edu.clemson.lph.civet.xml.CviMetaDataXml;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.civet.xml.elements.Address;
import edu.clemson.lph.civet.xml.elements.GroupLot;
import edu.clemson.lph.civet.xml.elements.NameParts;
import edu.clemson.lph.civet.xml.elements.Person;
import edu.clemson.lph.civet.xml.elements.Premises;
import edu.clemson.lph.civet.xml.elements.Veterinarian;
import edu.clemson.lph.dialogs.*;
import edu.clemson.lph.utils.PremCheckSum;

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

import javax.swing.*;

import org.apache.log4j.*;

public class BulkLoadSwineMovementCSV implements AddOn {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	JFrame fParent = null;
	private static final boolean bRequireBothPINs = true;
	private String sCVINbrSource = CviMetaDataXml.CVI_SRC_SWINE;
	public final static String sProgMessage = "Loading CVI: ";

	
	public BulkLoadSwineMovementCSV() {
	}
	
	public void doImportCSV(Window parent) {
		if( parent instanceof JFrame ) 
			fParent = (JFrame)parent;
		String sFilePath = null;
	    JFileChooser fc = new JFileChooser();
		Action details = fc.getActionMap().get("viewTypeDetails");
		details.actionPerformed(null);		
		fc.setDialogTitle("Civet: Open Swine CSV File");
	    fc.setCurrentDirectory(new File(CivetConfig.getBulkLoadDirPath()));
	    fc.setDialogTitle("Open Swine Movement Permit CSV File");
	    fc.setFileFilter( new CSVFilter() );
	    int returnVal = fc.showOpenDialog(fParent);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
	      File file = fc.getSelectedFile();
	      sFilePath = file.getAbsolutePath();
	    }
	    else {
	    	return;
	    }
		importSwineCSVFile( sFilePath, fParent );

	}
	
	private void importSwineCSVFile( String sFilePath, JFrame fParent ) {
		ProgressDialog prog = new ProgressDialog(fParent, "Civet", sProgMessage );
		prog.setAuto(true);
		prog.setVisible(true);
		TWorkCSV tWork = new TWorkCSV( prog, sFilePath, fParent );
		tWork.start();
	}

	// Also create TWorkAddSpecies and TWorkAddPage
	private class TWorkCSV extends Thread implements ThreadCancelListener {
		private String sFilePath;
		private ProgressDialog prog;
		private JFrame fParent;
		private CivetWebServices service;
		private volatile boolean bCanceled = false;
		
		private TWorkCSV( ProgressDialog prog, String sFilePath, JFrame fParent ) {
			this.prog = prog;
			this.sFilePath = sFilePath;
			this.fParent = fParent;
			service = new CivetWebServices();
			prog.setCancelListener(this);
		}
		
		@Override
		public void cancelThread() {
			bCanceled = true;
			interrupt();
			service = new CivetWebServices();  // Why is this here?
		}

		
		public void run() {
			// Create CSVDataFile object from CSV file
			CSVDataFile data;
			try {
				data = new CSVDataFile( sFilePath );
				// Iterate over the CSV file
				while( data.nextRow() && !bCanceled ) {
					prog.setMessage(sProgMessage + getCVINumber(data)); 
					String sXML = buildXml( data );
//			System.out.println(sXML);
					// Send it!
					String sRet = service.sendCviXML(sXML);
					if( sRet == null || ( !sRet.trim().startsWith("00") && !sRet.contains("Success") ) ) {
						logger.error( sRet, new Exception("Error submitting swine spreadsheet CVI to USAHERDS: ") );
						MessageDialog.messageLater(fParent, "Civet WS Error", "Error submitting to USAHERDS: " + sRet);
					}
				} 
			}catch (IOException e) {
				logger.error(e);
			}
			catch (Exception ex) {
				ex.printStackTrace();
				logger.error(ex.getMessage() + "\nError parsing CSV file" );
			}
			finally {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						prog.setVisible(false);
						prog.dispose();
					}
				});
			}
		}

	}// end inner class TWorkSave
	
	private String buildXml( CSVDataFile data ) throws IOException {
		StdeCviXmlModel xmlModel = new StdeCviXmlModel();
		StringTokenizer tok = new StringTokenizer(data.getVet(), " ," );
		String sFirst = tok.nextToken();
		String sLast = tok.nextToken();
		VetLookup vetLookup = new VetLookup( sLast, sFirst );
		String sCviNumber = getCVINumber(data);
		xmlModel.setCertificateNumber(sCviNumber);
		xmlModel.setIssueDate(data.getDate());
		if( data.getSourceState().equalsIgnoreCase(CivetConfig.getHomeStateAbbr()) && vetLookup != null ) {
			NameParts parts = new NameParts(null, vetLookup.getFirstName(), null, vetLookup.getLastName(), null );
			Address addr = new Address(vetLookup.getAddress(), null, vetLookup.getCity(), null, 
					vetLookup.getState(), vetLookup.getZipCode(), null, null, null);
			Person person = new Person(parts, vetLookup.getPhoneDigits(), null );
			Veterinarian veterinarian = new Veterinarian(person, addr, CivetConfig.getHomeStateAbbr(),
					vetLookup.getLicenseNo(), vetLookup.getNAN());
			xmlModel.setVet( veterinarian );
		}
		else {
			xmlModel.setVet(data.getVet());
		}
		// Expiration date will be set automatically from getXML();
		xmlModel.setPurpose("Feeding to slaughter");
		// We don't enter the person name, normally  or add logic to tell prem name from person name.
		Premises pOrigin = new Premises(data.getSourcePin(), data.getSourceFarm(), data.getSourceState()); 
		xmlModel.setOrigin( pOrigin );
		Premises pDestination = new Premises(data.getDestPin(), data.getDestFarm(), data.getDestState());
		xmlModel.setDestination(pDestination);

		Double dNum = new Double(data.getNumber());  // null reference risk?
		String sSpecies = "POR";
		String sAge = data.getAge();
		String sGender = data.getSex();
		if( sGender == null )
			sGender = "Gender Unknown";
		GroupLot group = new GroupLot(sSpecies, null, sGender, dNum);  //(String speciesCode, String sBreed, String sGender, Double quantity 
		group.age = sAge;
		xmlModel.addGroupLot(group);
		CviMetaDataXml metaData = new CviMetaDataXml();
		metaData.setCertificateNbr(sCviNumber);
		metaData.setBureauReceiptDate(data.getSavedDate());
		metaData.setErrorNote("Swine Bulk Spreadsheet");
		metaData.setCVINumberSource(sCVINbrSource);
//	System.out.println(metaData.getXmlString());
		xmlModel.addOrUpdateMetadataAttachment(metaData);
		return xmlModel.getXMLString();
	}

	private String getCVINumber( CSVDataFile data ) {
		String sRet = null;
		String sDate = (new SimpleDateFormat( "MMddyyyy")).format( data.getDate() );
		String sCompany = data.getCompany();
		
		boolean bValid = true;
		String sSourcePIN = data.getSourcePin();
		try {
			if( !PremCheckSum.isValid(sSourcePIN) ) {
				sSourcePIN = null;
			}
		} catch (Exception e1) {
			sSourcePIN = null;
		}
		String sDestinationPIN = data.getDestPin();
		try {
			if( !PremCheckSum.isValid(sDestinationPIN) ) {
				sDestinationPIN = null;
			}
		} catch (Exception e1) {
			sDestinationPIN = null;
		}
		if( !bValid && bRequireBothPINs ) {
			MessageDialog.messageLater(fParent, "Civet: Missing PIN",
					"Both source and destination PINs are required for bulk shipment records.\n" +
							"Source PIN = " + sSourcePIN + "\n" +
							"Dest PIN = " + sDestinationPIN + "\n" +
							"Ship Date = " + sDate );
		}
		else {
			sRet = sCompany + sSourcePIN + sDestinationPIN + sDate;
		}
		return sRet;
	}


	@Override
	public String getMenuText() {
		return "Import Swine Movement CSV File";
	}

	@Override
	public void execute(Window parent) {
		doImportCSV(parent);
	}

}
