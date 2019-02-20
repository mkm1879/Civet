package edu.clemson.lph.civet.threads;
/*
Copyright 2014 - 2019 Michael K Martin

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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetEditDialog;
import edu.clemson.lph.civet.CivetEditDialogController;
import edu.clemson.lph.civet.SpeciesRecord;
import edu.clemson.lph.civet.lookup.Counties;
import edu.clemson.lph.civet.lookup.LocalPremisesTableModel;
import edu.clemson.lph.civet.lookup.PremisesLocalStore;
import edu.clemson.lph.civet.lookup.PurposeLookup;
import edu.clemson.lph.civet.lookup.VetLookup;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.xml.CviMetaDataXml;
import edu.clemson.lph.civet.xml.elements.*;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.ProgressDialog;
import edu.clemson.lph.utils.FileUtils;

public class SaveCVIThread extends Thread {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private String sCVINbrSource = CviMetaDataXml.CVI_SRC_CIVET;
	private CivetEditDialogController controller;
	private ProgressDialog prog;
	private StdeCviXmlModel model = null;
	private String sXmlFileName;
	private boolean bImport;
	private String sOriginStateCode;
	private String sOriginPIN;
	private String sOriginName ;
	private String sOriginAddress;
	private String sOriginCity;
	private String sOriginCounty;
	private String sOriginZipCode;
	private String sOriginPhone;
	private String sDestinationStateCode;
	private String sDestinationPIN;
	private String sDestinationName;
	private String sDestinationAddress;
	private String sDestinationCity;
	private String sDestinationCounty;
	private String sDestinationZipCode;
	private String sDestinationPhone;
	private java.util.Date dDateIssued;
	private java.util.Date dDateReceived;
	private ArrayList<SpeciesRecord> aSpecies;
	
	private String sStdPurpose;
	private Integer iIssuedByKey;
	private String sIssuedByName;
	private String sCVINo;
	private boolean bNoEmail;
	private boolean bCancel = false;
	private boolean bIsDataFile = false;

	/**
	 * Pass in the values from each of the GUI controls with single values.
	 * @param dlgController To call back to the controller.
	 * @param model  The XML Data Model with Animals, Errors, Attachments, etc. in place
	 * @param bImport  Direction so controls are assigned to the right elements
	 * @param bIsDataFile  Lets us know whether to worry about existing data
	 * @param aSpecies  Used to calculate the number of anonymous groups to add
	 * @param sOtherStateCode
	 * @param sOtherName
	 * @param sOtherAddress
	 * @param sOtherCity
	 * @param sOtherCounty
	 * @param sOtherZipcode
	 * @param sOtherPIN
	 * @param sThisPIN
	 * @param sThisName
	 * @param sPhone
	 * @param sThisAddress
	 * @param sThisCity
	 * @param sThisCounty
	 * @param sZipcode
	 * @param dDateIssued
	 * @param dDateReceived
	 * @param iIssuedByKey   Value from picklist of in state vets.
	 * @param sIssuedByName  Name of other state vets if entered.
	 * @param sCVINo
	 * @param sMovementPurpose
	 */
	public SaveCVIThread(CivetEditDialogController dlgController, StdeCviXmlModel model,  
			boolean bImport, boolean bIsDataFile, ArrayList<SpeciesRecord> aSpecies, 
			String sOtherStateCode, String sOtherName, String sOtherAddress, String sOtherCity, 
			String sOtherCounty, String sOtherZipcode, String sOtherPIN,
			String sThisPIN, String sThisName, String sPhone,
			String sThisAddress, String sThisCity, String sThisCounty, String sZipcode,
			java.util.Date dDateIssued, java.util.Date dDateReceived, Integer iIssuedByKey, String sIssuedByName, String sCVINo,
			String sMovementPurpose) {
		this.controller = dlgController;
		prog = new ProgressDialog(dlgController.getDialog(), "Civet", "Saving CVI");
		prog.setAuto(true);
		prog.setVisible(true);
		this.bImport = bImport;
		this.bIsDataFile = bIsDataFile;
		this.model = model;
		this.aSpecies = aSpecies;
		this.dDateIssued = dDateIssued;
		this.dDateReceived = dDateReceived;
		this.iIssuedByKey = iIssuedByKey;
		this.sIssuedByName = sIssuedByName;
		PurposeLookup purpose = new PurposeLookup();
		this.sStdPurpose = purpose.getCodeForValue(sMovementPurpose);
		if( sStdPurpose == null || sStdPurpose.trim().length() == 0 )
			sStdPurpose = "Other";
		this.sCVINo = sCVINo;
		if( bImport ) {
			this.sOriginPIN = sOtherPIN;
			this.sOriginName = sOtherName;
			this.sOriginAddress = sOtherAddress;
			this.sOriginStateCode = sOtherStateCode;
			this.sOriginCity = sOtherCity;
			this.sOriginCounty = sOtherCounty;
			this.sOriginZipCode = sOtherZipcode;
			this.sOriginPhone = null;
			this.sDestinationPIN = sThisPIN;
			this.sDestinationName = sThisName;
			this.sDestinationAddress = sThisAddress;
			this.sDestinationCity = sThisCity;
			this.sDestinationCounty = sThisCounty;
			this.sDestinationStateCode = CivetConfig.getHomeStateAbbr();
			this.sDestinationZipCode = sZipcode;
			this.sDestinationPhone = sPhone;
		}
		else {
			this.sOriginPIN = sThisPIN;
			this.sOriginName = sThisName;
			this.sOriginAddress = sThisAddress;
			this.sOriginStateCode = CivetConfig.getHomeStateAbbr();
			this.sOriginCity = sThisCity;
			this.sOriginCounty = sThisCounty;
			this.sOriginZipCode = sZipcode;
			this.sOriginPhone = sPhone;
			this.sDestinationPIN = sOtherPIN;
			this.sDestinationName = sOtherName;
			this.sDestinationAddress = sOtherAddress;
			this.sDestinationCity = sOtherCity;
			this.sDestinationCounty = sOtherCounty;
			this.sDestinationStateCode = sOtherStateCode;
			this.sDestinationZipCode = sOtherZipcode;
			this.sDestinationPhone = null;
		}
	}


	@Override
	public void run() {
		
		try {
			setUpFileNamesAndContent();
			if( bCancel ) return;
			String sXml = buildXml();
			saveXml( sXml );
			if( bCancel ) return;
			if( !bNoEmail )
				saveEmail( sXml );
			if( CivetConfig.isStandAlone() ) {
				saveLocalPremData();
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex);
		}
	    finally {
	    	
	    }
	}

	private void saveLocalPremData() {
		PremisesLocalStore dataStore = LocalPremisesTableModel.getLocalStore();
		if( bImport )
			dataStore.addPremises(  sDestinationPIN,  sDestinationName,  sDestinationAddress,  sDestinationCity,			
					sDestinationCounty, sDestinationStateCode,  sDestinationZipCode,  sDestinationPhone );
		else
			dataStore.addPremises(  sOriginPIN,  sOriginName,  sOriginAddress,  sOriginCity,			
					sOriginCounty, sOriginStateCode,  sOriginZipCode,  sOriginPhone );
	}

	/**
	 * This is much simplified by new model.
	 */
	private void setUpFileNamesAndContent() {
		sXmlFileName = "CVI_" + sOriginStateCode + "_To_" + sDestinationStateCode + "_" + sCVINo + ".cvi";
		sXmlFileName = FileUtils.replaceInvalidFileNameChars(sXmlFileName);	
		String sOriginalFileName = controller.getOriginalFileName();
		checkExistingFiles( sOriginalFileName, sXmlFileName );
	}
	
	/**
	 * Look for existing .cvi files whose names will change due to state or cvi number edits
	 * Delete any existing.
	 * @param sExisting filename  (just name)
	 * @param sNew filename  (just name)
	 */
	private void checkExistingFiles(String sExisting, String sNew) {
		// Don't waste time if not editing an existing file
		if( sExisting != null && sExisting.toLowerCase().endsWith(".cvi") && !sExisting.equalsIgnoreCase(sNew) ) {
			// Appears we opened and changed an existing .cvi file need to delete existing before saving.
			String sEmailOutDir = CivetConfig.getEmailOutDirPath();
			String sEmailErrorsDir = CivetConfig.getEmailErrorsDirPath();
			String sToFileDir = CivetConfig.getToFileDirPath();
			File fEmailOut = new File ( sEmailOutDir + sExisting );
			File fEmailErrors = new File ( sEmailErrorsDir + sExisting );
			File fToFile = new File ( sToFileDir + sExisting );
			try { 
				if( fEmailOut.exists() && fEmailOut.isFile() )
					fEmailOut.delete();
			} catch( Exception e ) {
				logger.error("Could not delete file " + fEmailOut, e);
			}
			try { 
				if( fEmailErrors.exists() && fEmailErrors.isFile() )
					fEmailErrors.delete();
			} catch( Exception e ) {
				logger.error("Could not delete file " + fEmailErrors, e);
			}
			try { 
				if( fToFile.exists() && fToFile.isFile() )
					fToFile.delete();
			} catch( Exception e ) {
				logger.error("Could not delete file " + fToFile, e);
			}
		}
	}


	private String buildXml() {
		// Make sure spelling matches HERDS if possible
		sOriginCounty = Counties.getHerdsCounty(sOriginStateCode, sOriginCounty);
		sDestinationCounty = Counties.getHerdsCounty(sDestinationStateCode, sDestinationCounty); 
		model.setCviNumber(sCVINo);
		model.setIssueDate(dDateIssued);
		if( !bIsDataFile ) {  // Don't override vet that signed XFA or mCVI or V2 document
			if( bImport ) {
				model.setVet(sIssuedByName);
			}
			else {
				VetLookup vetLookup = new VetLookup( iIssuedByKey );
				NameParts parts = new NameParts(null, vetLookup.getFirstName(), null, vetLookup.getLastName(), null );
				AddressBlock addr = new AddressBlock(vetLookup.getAddress(), null, vetLookup.getCity(), null, 
						vetLookup.getState(), vetLookup.getZipCode(), null, null, null);
				Person person = new Person(parts, vetLookup.getPhoneDigits(), null );
				Veterinarian vet = new Veterinarian(person, addr.toString(), CivetConfig.getHomeStateAbbr(),
						vetLookup.getLicenseNo(), vetLookup.getNAN());
				model.setVet( vet );
			}
			model.setPurpose(sStdPurpose);
		} // End if !bXFA
		// Expiration date will be set automatically from getXML();
		// We don't enter the person name, normally  or add logic to tell prem name from person name.

		model.setOrigin(sOriginPIN, sOriginName, sOriginPhone, sOriginAddress, sOriginCity, sOriginCounty, sOriginStateCode, sOriginZipCode);
		model.setDestination(sDestinationPIN, sDestinationName,  
					sDestinationPhone, sDestinationAddress, sDestinationCity, sDestinationCounty, 
					sDestinationStateCode, sDestinationZipCode);
		// By suggestion of Mitzy at CAI DO NOT populate Consignor or Consignee unless different!
		
		// Add animals and groups.  This logic is tortured!
		// This could be greatly improved to better coordinate with CO/KS list of animals and the standard's group concept.
		// Precondition:  The model provided already has the species list populated with Animals.
		// Use species list to calculate the number of unidentified animals and build groups accordingly.
		// Precondition:  Individually identified animals were added when the AddIdentifiers dialog closed.
		ArrayList<Animal> animals = model.getAnimals();
		ArrayList<GroupLot> groups = model.getGroups();
		for( SpeciesRecord sr : aSpecies ) {
			// Only add group lot if not officially IDd so count ids and subtract
			String sSpeciesCode = sr.sSpeciesCode;
			int iNumOfSpp = sr.iNumber;
			int iCountIds = 0;
			if( animals != null ) {
				for( Animal animal : animals ) {
					if(  animal.speciesCode.code != null && animal.speciesCode.code.equals(sSpeciesCode) ) {
						iCountIds++;
					}
				}
			}
			boolean bFound = false;
			for( GroupLot group : groups ) {
				if( group.speciesCode.code != null && group.speciesCode.code.equals(sSpeciesCode) ) {
					bFound = true;
					if(iCountIds == iNumOfSpp) {
						model.removeGroupLot(group);
					}
					else if( iCountIds > iNumOfSpp ) {
						logger.error("Number of tags " + iCountIds + " > number of animals " + iNumOfSpp + " for species " + sSpeciesCode);
						// Leave things alone.
					}
					else if( iCountIds != iNumOfSpp ) {
						Integer iNumUntagged = iNumOfSpp - iCountIds;
						group.quantity = iNumUntagged.doubleValue();
						model.addOrEditGroupLot(group);
					}
				}
			}
			if( !bFound ) {
				Integer iNumUntagged = iNumOfSpp - iCountIds;
				GroupLot group = new GroupLot(sSpeciesCode, iNumUntagged.doubleValue());
				model.addOrEditGroupLot(group);
			}
		}
//		Precondition model contains the current page or pages in the attachment
//      Precondition model contains metadata for errors saved from the add errors dialog.
		model.setDefaultAnimalInspectionDates(dDateIssued);
		model.setCertificateNumber(sCVINo);
		model.setBureauReceiptDate(dDateReceived);
		model.setCVINumberSource(sCVINbrSource);
		return model.getXMLString();
	}
	
	private void saveXml(String sStdXml) {
		final String sFilePath = CivetConfig.getToFileDirPath() + sXmlFileName;
		final File fileOut = new File(sFilePath);
		try {
			PrintWriter pw = new PrintWriter( new FileOutputStream( fileOut ) );
			pw.print(sStdXml);
			pw.flush();
			pw.close();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					controller.setLastSavedFile(fileOut);
					CivetEditDialog dlgParent = controller.getDialogParent();
					if( dlgParent != null ) {
						dlgParent.getDialogController().setLastSavedFile(fileOut);
					}
				}
			});
		} catch (final FileNotFoundException e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					logger.error("Could not save " + sFilePath, e);
					MessageDialog.showMessage(controller.getDialog(), "Civet Error: File Save", "Could not save file\n " + sFilePath );
					prog.setVisible(false);
					prog.dispose();
				}
			});
			bCancel = true;
			return;
		}
	}
	
	private void saveEmail(String sStdXml) {
		String sFilePath = null;
		if( !bImport ) 
			sFilePath = CivetConfig.getEmailOutDirPath() + sXmlFileName;
		else if( model.hasErrors() ) {
			sFilePath = CivetConfig.getEmailErrorsDirPath() + sXmlFileName;
		}
		if( sFilePath == null ) 
			return;
		File fileOut = new File(sFilePath);
		try {
			PrintWriter pw = new PrintWriter( new FileOutputStream( fileOut ) );
			pw.print(sStdXml);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			logger.error("Could not save " + sFilePath, e);
			MessageDialog.messageLater(controller.getDialog(), "Civet Error: File Save", "Could not save file\n " + sFilePath );
			return;
		} 
	}

	public void setNoEmail() {
		bNoEmail = true;
	}

} // End class SaveCVIThread
