package edu.clemson.lph.civet.vsps;
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
import java.util.Map;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.lookup.VetLookup;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.webservice.CivetWebServices;
import edu.clemson.lph.civet.xml.CviMetaDataXml;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.civet.xml.elements.Address;
import edu.clemson.lph.civet.xml.elements.Animal;
import edu.clemson.lph.civet.xml.elements.AnimalTag;
import edu.clemson.lph.civet.xml.elements.GroupLot;
import edu.clemson.lph.civet.xml.elements.NameParts;
import edu.clemson.lph.civet.xml.elements.Person;
import edu.clemson.lph.civet.xml.elements.SpeciesCode;
import edu.clemson.lph.civet.xml.elements.Veterinarian;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.ProgressDialog;
import edu.clemson.lph.dialogs.ThreadCancelListener;
import edu.clemson.lph.utils.FileUtils;

class InsertVspsCviThread extends Thread implements ThreadCancelListener {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private Window parent;
	private VspsCviFile cviFile;
	private ProgressDialog prog;
	private String sCVINbrSource = CviMetaDataXml.CVI_SRC_VSPS;
	private CivetWebServices service = null;
	private String sProgMsg = "Processing VSPS CVI: ";
	private volatile boolean bCanceled = false;

	InsertVspsCviThread(Window parent, VspsCviFile cviFile ) {
		this.parent = parent;
		this.cviFile = cviFile;
		prog = new ProgressDialog(parent, "Civet: VSPS Import", sProgMsg);
		prog.setAuto(true);
		prog.setCancelListener(this);
		prog.setVisible(true);
		service = new CivetWebServices();
	}
	
	@Override
	public void cancelThread() {
		bCanceled = true;
		interrupt();
	}

	@Override
	public void run() {
		VspsCvi cvi;
		try {
			while( (cvi = cviFile.nextCVI() ) != null ) {
				if( bCanceled )
					break;
				if( cvi.getStatus().equals("SAVED") )  // Ignore Saved but not issued CVIs
					continue;
				if( (cvi.getOrigin() == null || cvi.getOrigin().getState() == null) &&  (cvi.getConsignor() == null || cvi.getConsignor().getState() == null))
					continue;
				if( (cvi.getDestination() == null || cvi.getDestination().getState() == null) &&  (cvi.getConsignee() == null || cvi.getConsignee().getState() == null))
					continue;
				prog.setMessage( sProgMsg + cvi.getCVINumber() );
				byte[] baXML = buildXml ( cvi );
//		System.out.println(sXML);
				// Send it!
				String sRet = service.sendCviXML(baXML);
				if( sRet == null || ( !sRet.trim().startsWith("00") && !sRet.contains("Success") ) ) {
					String sCVINbr = cvi.getCVINumber();
					logger.error( new Exception("Error submitting VSPS spreadsheet CVI " + sCVINbr + " to USAHERDS: ") );
					FileUtils.writeUTF8File(baXML, sCVINbr + "_Error.xml");
					MessageDialog.messageLater(parent, "Civet WS Error", "Error submitting CVI " + sCVINbr + " to USAHERDS: " + sRet);
				}
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
	
	private byte[] buildXml( VspsCvi cvi ) throws IOException {
		StdeCviXmlModel xmlModel = new StdeCviXmlModel();
		xmlModel.setCertificateNumber(cvi.getCVINumber());
		xmlModel.setCertificateNumberSource("VSPS");
		xmlModel.setIssueDate(cvi.getInspectionDate());
		@SuppressWarnings("unused")
		Element eVet = null;
		VetLookup vetLookup = new VetLookup( cvi.getVetLastName(), cvi.getVetFirstName() );
		if( cvi.getOriginState().equalsIgnoreCase(CivetConfig.getHomeStateAbbr()) && vetLookup != null ) {
//			String sVetName = vet.getLastName() + ", " + vet.getFirstName();
//			eVet = xmlModel.setVet(sVetName, vet.getLicenseNo(), vet.getNAN(), vet.getPhoneDigits());
//			xmlModel.setAddress(eVet, vet.getAddress(), vet.getCity(), null, vet.getState(), vet.getZipCode());
			NameParts parts = new NameParts(null, vetLookup.getFirstName(), null, vetLookup.getLastName(), null );
			Address addr = new Address(vetLookup.getAddress(), null, vetLookup.getCity(), null, 
					vetLookup.getState(), vetLookup.getZipCode(), null, null, null);
			Person person = new Person(parts, vetLookup.getPhoneDigits(), null );
			Veterinarian vet = new Veterinarian(person, addr, CivetConfig.getHomeStateAbbr(),
					vetLookup.getLicenseNo(), vetLookup.getNAN());
			xmlModel.setVet( vet );
		}
		else {
			xmlModel.setVet(cvi.getVeterinarianName());
		}
		// Expiration date will be set automatically from getXML();
		xmlModel.setPurpose(cvi.getStdPurpose());
		// We don't enter the person name, normally  or add logic to tell prem name from person name.
		VspsCviEntity origin = cvi.getOrigin();
		@SuppressWarnings("unused")
		Element eOrigin = xmlModel.setOrigin( origin.getPremisesId(), origin.getBusiness(), origin.getPhoneDigits(), origin.getAddress1(), 
				origin.getCity(), origin.getCounty(), origin.getState(), origin.getPostalCode() );
		VspsCviEntity destination = cvi.getDestination();
		@SuppressWarnings("unused")
		Element eDestination = xmlModel.setDestination( destination.getPremisesId(), destination.getBusiness(), destination.getPhoneDigits(), destination.getAddress1(), 
				destination.getCity(), destination.getCounty(), destination.getState(), destination.getPostalCode() );

		HashMap<List<String>, Integer> hGroups = new HashMap<List<String>, Integer>();
		List<VspsCviAnimal> animals = cvi.getAnimals();
		for( VspsCviAnimal vspsAnimal : animals ) {
			String sSpecies = VspsCodeLookup.getSpCode(vspsAnimal.getSpecies());
			String sBreed = vspsAnimal.getBreed();
			String sGender = VspsCodeLookup.getGenderCode(vspsAnimal.getGender());
			Integer iCount = vspsAnimal.getCount();
			if( iCount == null )
				iCount = 1;
			ArrayList<AnimalTag> aTags = vspsAnimal.getTags();
			if( iCount == 1 && aTags.size() > 0 && iCount == 1 ) {
				java.util.Date dInspDate = cvi.getInspectionDate();
				String sInspDate = StdeCviXmlModel.getDateFormat().format(dInspDate);
				SpeciesCode scSpecies = new SpeciesCode(sSpecies);
				Animal animal = new Animal(scSpecies, aTags, null, sBreed,
						sGender, sInspDate);
				xmlModel.addAnimal( animal );
			}
			else {
				List<String> lKey = new ArrayList<String>();
				lKey.add(sSpecies);
				lKey.add(sGender);
				lKey.add(sBreed);
				Integer iNum = hGroups.get(lKey);
				if( iNum != null ) {
					iNum += iCount;
					hGroups.put(lKey, iNum);
				}
				else {
					hGroups.put(lKey, iCount);
				}
			}
		}
		// Add Groups.
		for( Map.Entry<List<String>, Integer> entry : hGroups.entrySet() ) {
			List<String> lKey = entry.getKey();
			String sSpecies = lKey.get(0);
			String sGender = lKey.get(1);
			String sBreed = lKey.get(2);
			Integer iNum = entry.getValue();
			GroupLot group = new GroupLot(sSpecies, sBreed, sGender, Double.valueOf(iNum) );
			xmlModel.addGroupLot(group);
		}
		CviMetaDataXml metaData = new CviMetaDataXml();
		metaData.setCertificateNbr(cvi.getCVINumber());
		metaData.setBureauReceiptDate(cvi.getCreateDate());
//		metaData.setErrorNote("VSPS Download");
		metaData.setCVINumberSource(sCVINbrSource);
		xmlModel.addOrUpdateMetadataAttachment(metaData);
		return xmlModel.getXMLBytes();
	}

}
