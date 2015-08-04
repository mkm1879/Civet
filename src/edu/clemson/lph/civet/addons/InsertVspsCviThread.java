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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetConfig;
import edu.clemson.lph.civet.lookup.VetLookup;
import edu.clemson.lph.civet.webservice.CivetWebServices;
import edu.clemson.lph.civet.xml.CviMetaDataXml;
import edu.clemson.lph.civet.xml.StdeCviXml;
import edu.clemson.lph.civet.xml.StdeCviXmlBuilder;
import edu.clemson.lph.dialogs.ProgressDialog;

public class InsertVspsCviThread extends Thread {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	Window parent;
	VspsCviFile cviFile;
	StdeCviXml stdXml;
	ProgressDialog prog;
	private String sCVINbrSource = CviMetaDataXml.CVI_SRC_VSPS;
	private CivetWebServices service = null;
	private String sProgMsg = "Processing VSPS CVI: ";


	public InsertVspsCviThread(Window parent, VspsCviFile cviFile ) {
		this.parent = parent;
		this.cviFile = cviFile;
		prog = new ProgressDialog(parent, "Civet: VSPS Import", sProgMsg);
		prog.setAuto(true);
		prog.setVisible(true);
		service = new CivetWebServices();
	}

	@Override
	public void run() {
		VspsCvi cvi;
		try {
			while( (cvi = cviFile.nextCVI() ) != null ) {
				if( cvi.getStatus().equals("SAVED") )  // Ignore Saved but not issued CVIs
					continue;
				if( (cvi.getOrigin() == null || cvi.getOrigin().getState() == null) &&  (cvi.getConsignor() == null || cvi.getConsignor().getState() == null))
					continue;
				if( (cvi.getDestination() == null || cvi.getDestination().getState() == null) &&  (cvi.getConsignee() == null || cvi.getConsignee().getState() == null))
					continue;
				prog.setMessage( sProgMsg + cvi.getCVINumber() );
				String sXML = buildXml ( cvi );
				System.out.println(sXML);
				// Send it!
				service.sendCviXML(sXML);
			}
		} catch (IOException e) {
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
	
	private String buildXml( VspsCvi cvi ) throws IOException {
		StdeCviXmlBuilder xmlBuilder = new StdeCviXmlBuilder(stdXml);
		VetLookup vet = new VetLookup( cvi.getVetLastName(), cvi.getVetFirstName() );
		xmlBuilder.setCviNumber(cvi.getCVINumber());
		xmlBuilder.setIssueDate(cvi.getInspectionDate());
		Element eVet = null;
		if( cvi.getOriginState().equalsIgnoreCase(CivetConfig.getHomeStateAbbr()) && vet != null ) {
			String sVetName = vet.getLastName() + ", " + vet.getFirstName();
			eVet = xmlBuilder.setVet(sVetName, vet.getLicenseNo(), vet.getNAN(), vet.getPhoneDigits());
			xmlBuilder.setAddress(eVet, vet.getAddress(), vet.getCity(), vet.getState(), vet.getZipCode());
		}
		else {
			xmlBuilder.setVet(cvi.getVeterinarianName());
		}
		// Expiration date will be set automatically from getXML();
		xmlBuilder.setPurpose(VspsCodeLookup.getPurposeCode(cvi.getStdPurpose()));
		// We don't enter the person name, normally  or add logic to tell prem name from person name.
		VspsCviEntity origin = cvi.getOrigin();
		Element eOrigin = xmlBuilder.setOrigin(origin.getPremisesId(), origin.getBusiness(), 
				origin.getName(), origin.getPhoneDigits());
		xmlBuilder.setAddress(eOrigin, origin.getAddress1(), origin.getCity(), 
				origin.getState(), origin.getPostalCode());
		VspsCviEntity destination = cvi.getDestination();
		Element eDestination = xmlBuilder.setDestination(destination.getPremisesId(), destination.getBusiness(), 
				destination.getName(), destination.getPhoneDigits());
		xmlBuilder.setAddress(eDestination, destination.getAddress1(), destination.getCity(), 
				destination.getState(), destination.getPostalCode());

		HashMap<List<String>, Integer> hGroups = new HashMap<List<String>, Integer>();
		List<VspsCviAnimal> animals = cvi.getAnimals();
		for( VspsCviAnimal animal : animals ) {
			String sSpecies = VspsCodeLookup.getSpCode(animal.getSpecies());
			String sBreed = animal.getBreed();
			String sGender = VspsCodeLookup.getGenderCode(animal.getGender());
			String sId = animal.getFirstOfficialId();
			String sType = animal.getFirstOfficialIdType(); 
			if( sId != null ) {
				xmlBuilder.addAnimal( sSpecies, cvi.getInspectionDate(), sBreed, null, sGender, sType, sId);
			}
			else {
				List<String> lKey = new ArrayList<String>();
				lKey.add(sSpecies);
				lKey.add(sGender);
				Integer iNum = hGroups.get(lKey);
				if( iNum != null ) {
					iNum++;
					hGroups.put(lKey, iNum);
				}
				else {
					hGroups.put(lKey, 1);
				}
			}
		}
		// Add Groups.
		for( List<String> lKey : hGroups.keySet() ) {
			String sSpecies = lKey.get(0);
			String sGender = lKey.get(1);
			Integer iNum = hGroups.get(lKey);
			xmlBuilder.addGroup(iNum, "Non-identified Animals", sSpecies, null, sGender);
		}
		CviMetaDataXml metaData = new CviMetaDataXml();
		metaData.setCertificateNbr(cvi.getCVINumber());
		metaData.setBureauReceiptDate(cvi.getCreateDate());
		metaData.setErrorNote("VSPS Download");
		metaData.setCVINumberSource(sCVINbrSource);
	System.out.println(metaData.getXmlString());
		xmlBuilder.addMetadataAttachement(metaData);
		return xmlBuilder.getXMLString();
	}

}
