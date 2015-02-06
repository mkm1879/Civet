package edu.clemson.lph.civet.xml;
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
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetConfig;
import edu.clemson.lph.utils.IDTypeGuesser;
import edu.clemson.lph.utils.XMLUtility;

public class StdeCviXmlBuilder {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private XMLDocHelper helper;
	private Document doc;
	private Element root;

	public StdeCviXmlBuilder() {
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = db.newDocument();
			doc.setXmlStandalone(true);
			root = doc.createElementNS("http://www.usaha.org/xmlns/ecvi", "eCVI");
			doc.appendChild(root);
			helper = new XMLDocHelper( doc );
		} catch (ParserConfigurationException e) {
			logger.error("Could not set up parser for stdXML", e);
		}
	}

	public StdeCviXmlBuilder(StdeCviXml cviIn) {
		try {
			if( cviIn != null ) {
				doc = cviIn.getDocument();
				doc.setXmlStandalone(true);
				helper = cviIn.getHelper();
				root = cviIn.getRoot();
			}
			if( doc == null ) {
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				doc = db.newDocument();
				doc.setXmlStandalone(true);
				root = doc.createElementNS("http://www.usaha.org/xmlns/ecvi", "eCVI");
				doc.appendChild(root);
				helper = new XMLDocHelper( doc );
			}
		} catch (ParserConfigurationException e) {
			logger.error("Could not set up parser for stdXML", e);
		}
	}

	public StdeCviXmlBuilder( Document doc) {
		this.doc = doc;
		doc.setXmlStandalone(true);
		root = doc.getDocumentElement();
		helper = new XMLDocHelper( doc );
	}

	public void setCviNumber( String sCVINo ) {
		root.setAttribute("CviNumber", sCVINo);
	}
	
	private boolean isValidDoc() {
		boolean bRet = true;
		if( doc == null ) {
			logger.error("Call made against StdeCVIXmlBuilder with null Document");
			bRet = false;
		}
		if( root == null ) {
			logger.error("Call made against StdeCVIXmlBuilder with null root Element");
			bRet = false;
		}
		return bRet;
	}
	
	/**
	 * Set issue and expiration dates based on configured validity period
	 * NOTE: this is not quite right.  Expiration should be 30 after the earliest
	 * inspected date!  So see checkExpiration() call before printing XML
	 * @param dIssued
	 */
	public void setIssueDate( java.util.Date dIssued ) {
		if( isValidDoc() && dIssued != null) {
			String sDate = dateFormat.format(dIssued);
			int iValidDays = CivetConfig.getCviValidDays();
			if( sDate !=  null && iValidDays > 0 ) {
				root.setAttribute("IssueDate", sDate);		
				Calendar cal = Calendar.getInstance();
				cal.setTime(dIssued);
				cal.add(Calendar.DATE, iValidDays);
				String sExp = dateFormat.format(cal.getTime());
				if( sExp != null && sExp.trim().length() > 0 )
					root.setAttribute("ExpirationDate", sExp);
			}
		}
	}
	
	public Element setVet( String sName, String sLicNo, String sNAN, String sPhone ) {
		if( !isValidDoc() )
			return null;
		Element eVet = null;
		if( sName != null && sName.trim().length() > 0 ) {
			eVet = childElementByName(root,"Veterinarian");
			if( eVet == null ) {
				eVet = doc.createElement("Veterinarian");
				Element after = childElementByNames(root,"MovementPurpose,Origin,Destination,Consignor,Consignee,Accessions,Animal,GroupLot,Attachment");
				if( after != null )
					root.insertBefore(eVet, after);
				else
					root.appendChild(eVet);
			}
			if( sLicNo != null && sLicNo.trim().length() > 0 )
				eVet.setAttribute("LicenseNumber", sLicNo);
			if( sNAN != null && sNAN.trim().length() > 0 )
				eVet.setAttribute("NationalAccreditationNumber", sNAN);
			Element person = childElementByName(eVet,"Person");
			if( person == null ) {
				person = doc.createElement("Person");
				eVet.appendChild(person);
			}
			Element name = childElementByName(person, "Name");
			if( name == null ) {
				name = doc.createElement("Name");
				person.appendChild(name);
			}
			name.setTextContent(sName);
			if( sPhone != null && sPhone.trim().length() > 0 ) {
				Element phone = childElementByName(person, "Phone");
				if( phone == null ) {
					phone = doc.createElement("Phone");
					person.appendChild(phone);
				}
				phone.setAttribute("Type", "Unknown");
				phone.setAttribute("Number", sPhone);
			}	
		}
		return eVet;
	}
	
	public Element setVet( String sName ) {
		Element eVet = null;
		if( sName == null || sName.trim().length() == 0 )
			sName = "Not Entered";
		if( isValidDoc() && sName != null && sName.trim().length() > 0 ) {
			eVet = doc.createElement("Veterinarian");
			Node after = childNodeByNames(root,"MovementPurpose,Origin,Destination,Consignor,Consignee,Accessions,Animal,GroupLot,Attachment");
			root.insertBefore(eVet, after);
			Element person = doc.createElement("Person");
			eVet.appendChild(person);
			Element name = doc.createElement("Name");
			person.appendChild(name);
			name.setTextContent(sName);
		}
		return eVet;
	}


	private Element getAddress( Element e, String sStreetIn, String sCityIn, String sStateIn, String sZipIn ) {
		if( e == null ) return null;
		ArrayList<Element> eAddresses = XMLUtility.listChildElementsByName(e, "Address");
		for( Element eAddress : eAddresses ) {
			String sStreet = null;
			String sCity = null;
			String sState = null;
			String sZip = null;
			Element eStreet = childElementByName(eAddress, "Line1");
			if( eStreet != null ) sStreet = eStreet.getTextContent();
			Element eCity = childElementByName(eAddress, "Town");
			if( eCity != null ) sCity = eCity.getTextContent();
			Element eState = childElementByName(eAddress, "State");
			if( eState != null ) sState = eState.getTextContent();
			Element eZip = childElementByName(eAddress, "Zip");
			if( eZip != null ) sZip = eZip.getTextContent();
			if( ((sStreetIn == null && sStreet == null ) || (sStreetIn != null && sStreetIn.equals(sStreet))) 
					||
				((sCityIn == null && sCity == null ) || (sCityIn != null && sCityIn.equals(sCity))) 
					||
				((sStateIn == null && sState == null ) || (sStateIn != null && sStateIn.equals(sState))) 
					||
				((sZipIn == null && sZip == null ) || (sZipIn != null && sZipIn.equals(sZip))) ) {
				
				return eAddress;
			}
		}
		return null;
	}

	/**
	 * Used to add address to Veterinarian, Origin and Destination
	 * @param e
	 * @param sStreet
	 * @param sCity
	 * @param sState
	 * @param sZip
	 * @return
	 */
	public Element setAddress( Element e, String sStreet, String sCity, String sState, String sZip ) {
		if( !isValidDoc() )
			return null;
		Element address = null;
		if( e != null ) {
			address = getAddress(e, sStreet, sCity, sState, sZip );
			if( address != null ) 
				return address;
			address = childElementByName(e,"Address");
			if( address == null ) {
				String sElement = e.getTagName();
				Node nPerson = null;
				if( !"Veterinarian".equals(sElement) ) {
					nPerson = childNodeByName( e, "Person");
				}
				address = doc.createElement("Address");
				e.insertBefore(address, nPerson);
			}
			Node line1 = childNodeByName( address, "Line1");
			if( line1 == null ) {
				line1 = doc.createElement("Line1");
				address.appendChild(line1);
			}
			if( sStreet != null ) {
				line1.setTextContent(sStreet.trim());
			}
			else {
				line1.setTextContent("");
			}
			Node town = childNodeByName( address, "Town");
			if( town == null ) {
				town = doc.createElement("Town");
				address.appendChild(town);
			}
			if( sCity != null ) {
				town.setTextContent(sCity.trim());
			}
			else {
				town.setTextContent("Not Provided");
			}
			Node state = childNodeByName( address, "State");
			if( state == null ) {
				state = doc.createElement("State");
				address.appendChild(state);
			}
			if( sState != null ) {
				state.setTextContent(sState.trim());
			}
			else {
				logger.error("Attempt to add address with no state.", new Exception());
				state.setTextContent("ERROR");				
			}
			Node zip = childNodeByName( address, "ZIP" );
			if( zip == null ) {
				zip = doc.createElement("ZIP");
				address.appendChild(zip);
			}
			if( sZip != null && sZip.trim().length() > 0 ) {		
				zip.setTextContent(sZip.trim());
			}
			else {
				zip.setTextContent("00000");
			}
			Node country = childNodeByName( address, "Country" );
			if( country == null ) {
				country = doc.createElement("Country");
				address.appendChild(country);
			}
			country.setTextContent("USA");
		}
		return address;
	}
	
	public void setPurpose( String sPurpose ) {
		if( isValidDoc() && sPurpose != null && sPurpose.trim().length() > 0 ) {
			Element purposes = childElementByName( root, "MovementPurposes" );
			if( purposes == null) {
				purposes = doc.createElement("MovementPurposes");
				Node after = childNodeByNames(root,"Origin,Destination,Consignor,Consignee,Accessions,Animal,GroupLot,Attachment");
				root.insertBefore(purposes, after);
			}
			Element purpose = childElementByName(purposes, "MovementPurpose");
			if( purpose == null ) {
				purpose = doc.createElement("MovementPurpose");
				purposes.appendChild(purpose);
			}
			purpose.setTextContent(sPurpose.toLowerCase());
		}
	}

	public Element setOrigin( String sPIN, String sPremName, String sName, String sPhone ) {
		if( !isValidDoc() )
			return null;
		Element origin = null;
		origin = childElementByName(root,"Origin");
		if( origin == null ) {
			origin = doc.createElement("Origin");
			Node after = childNodeByNames(root,"Destination,Consignor,Consignee,Accessions,Animal,GroupLot,Attachment");
			root.insertBefore(origin,after);
			if( sPIN != null && sPIN.trim().length() > 0 ) {
				Element pin = doc.createElement("PremId");
				origin.appendChild(pin);
				pin.setTextContent(sPIN);
			}
			if( sPremName != null && sPremName.trim().length() > 0 ) {
				Element premName = doc.createElement("PremName");
				origin.appendChild(premName);
				premName.setTextContent(sPremName);
			}
			Element person = doc.createElement("Person");
			origin.appendChild(person);
			Element name = doc.createElement("Name");
			person.appendChild(name);
			name.setTextContent(sName);
			if( sPhone != null && sPhone.trim().length() > 0 ) {
				Element phone = doc.createElement("Phone");
				person.appendChild(phone);
				phone.setAttribute("Type", "Unknown");
				phone.setAttribute("Number", sPhone);
			}	
		}
		return origin;
	}
	
	public Element setDestination( String sPIN, String sPremName, String sName, String sPhone ) {
		if( !isValidDoc() )
			return null;
		Element destination = null;
		destination = childElementByName(root,"Destination");
		if( destination == null ) {
			destination = doc.createElement("Destination");
			Node after = childNodeByNames(root,"Consignor,Consignee,Accessions,Animal,GroupLot,Attachment");
			root.insertBefore(destination, after);
			if( sPIN != null && sPIN.trim().length() > 0 ) {
				Element pin = doc.createElement("PremId");
				destination.appendChild(pin);
				pin.setTextContent(sPIN);
			}
			if( sPremName != null && sPremName.trim().length() > 0 ) {
				Element premName = doc.createElement("PremName");
				destination.appendChild(premName);
				premName.setTextContent(sPremName);
			}
			Element person = doc.createElement("Person");
			destination.appendChild(person);
			Element name = doc.createElement("Name");
			person.appendChild(name);
			name.setTextContent(sName);
			if( sPhone != null && sPhone.trim().length() > 0 ) {
				Element phone = doc.createElement("Phone");
				person.appendChild(phone);
				phone.setAttribute("Type", "Unknown");
				phone.setAttribute("Number", sPhone);
			}	
		}
		return destination;
	}
	
	public Element setConsignor( String sPremName, String sName, String sPhone ) {
		if( !isValidDoc() )
			return null;
		Element consignor = null;
		consignor = childElementByName(root,"Consignor");
		if( consignor == null ) {
			consignor = doc.createElement("Consignor");
			Node after = childNodeByNames(root,"Consignee,Accessions,Animal,GroupLot,Attachment");
			root.insertBefore(consignor, after);
			Element premName = doc.createElement("PremName");
			consignor.appendChild(premName);
			premName.setTextContent(sPremName);
			Element person = doc.createElement("Person");
			consignor.appendChild(person);
			Element name = doc.createElement("Name");
			person.appendChild(name);
			name.setTextContent(sName);
			if( sPhone != null && sPhone.trim().length() > 0 ) {
				Element phone = doc.createElement("Phone");
				person.appendChild(phone);
				phone.setAttribute("Type", "Unknown");
				phone.setAttribute("Number", sPhone);
			}	
		}
		return consignor;
	}
	
	public Element setConsignee( String sPremName, String sName, String sPhone ) {
		if( isValidDoc() )
			return null;
		Element consignee = null;
		consignee = childElementByName(root,"Consignee");
		if( consignee == null ) {
			consignee = doc.createElement("Consignee");
			Node after = childNodeByNames(root,"Accessions,Animal,GroupLot,Attachment");
			root.insertBefore(consignee, after);
			Element premName = doc.createElement("PremName");
			consignee.appendChild(premName);
			premName.setTextContent(sPremName);
			Element person = doc.createElement("Person");
			consignee.appendChild(person);
			Element name = doc.createElement("Name");
			person.appendChild(name);
			name.setTextContent(sName);
			if( sPhone != null && sPhone.trim().length() > 0 ) {
				Element phone = doc.createElement("Phone");
				person.appendChild(phone);
				phone.setAttribute("Type", "Unknown");
				phone.setAttribute("Number", sPhone);
			}	
		}
		return consignee;
	}

	private Element getAnimalByTag( String sNumber ) {
		if( sNumber == null ) return null;
		ArrayList<Element> eAnimals = XMLUtility.listChildElementsByName(root, "Animal");
		for( Element eAnimal : eAnimals) {
			Element eTag = getAnimalTagByNumber( eAnimal, sNumber );
			if( eTag != null )
				return eAnimal;
		}
		return null;
	}
	
	private Element getAnimalTagByNumber( Element eAnimal, String sNumber ) {
		ArrayList<Element> eAnimalTags = XMLUtility.listChildElementsByName(eAnimal,"AnimalTag");
		for( Element eAnimalTag : eAnimalTags ) {
			if( eAnimalTag != null ) {
				String sExistingNumber = eAnimalTag.getAttribute("Number");
				if( sNumber.equals(sExistingNumber) ) {
					return eAnimalTag;
				}
			}
		}
		return null;
	}
	
	public Element addAnimal( String sSpecies, java.util.Date dInspectionDate, String sBreed, String sAge, String sSex, 
								String sTagType, String sTagNumber ) {
		if( !isValidDoc() ) 
			return null;
		Element animal = getAnimalByTag( sTagNumber );
		if(animal == null ) {
			animal = doc.createElement("Animal");
			Node after = childNodeByNames(root,"GroupLot,Attachment");
			root.insertBefore(animal,after);
			animal.setAttribute("SpeciesCode", sSpecies);
			String sInspectionDate = dateFormat.format(dInspectionDate);
			animal.setAttribute("InspectionDate", sInspectionDate);
			if( sAge != null && sAge.trim().length() > 0 )
				animal.setAttribute("Age", sAge);
			if( sSex != null && sSex.trim().length() > 0 )
				animal.setAttribute("Sex", sSex);
			if( sTagNumber != null && sTagNumber.trim().length() > 0 )
				addAnimalTag( animal, sTagType, sTagNumber );
		}
		return animal;
	}
	
	public void addAnimalTag( Element animal, String sType, String sNumber ) {
		if( animal == null || sNumber == null ) return;
		Element tag = getAnimalTagByNumber( animal, sNumber );
		if( tag == null ) {
				
		tag = doc.createElement("AnimalTag");
		if( isValidDoc() && animal != null ) {
			animal.appendChild(tag);
			if( sType == null || sType.trim().length() == 0 ) {
				sType = IDTypeGuesser.getTagType(sNumber);
			}
			tag.setAttribute("Type", sType);
			tag.setAttribute("Number", sNumber);
		}
		}
	}
	
	public Element addGroup( int iNum, String sDescription, String sSpecies, String sAge, String sSex ) {
		Element group = null;
		if( isValidDoc() && iNum > 0 ) {
			group = doc.createElement("GroupLot");
			Node after = childNodeByName(root,"Attachment");
			root.insertBefore(group,after);
			group.setAttribute("Quantity", Integer.toString(iNum));
			if( (sDescription == null || sDescription.trim().length() == 0) && sSpecies != null && sSpecies.trim().length() > 0 )
				sDescription = sSpecies;
			group.setAttribute("Description", sDescription);
			if( sSpecies != null && sSpecies.trim().length() > 0 )
				group.setAttribute("SpeciesCode", sSpecies);
			if( sAge != null && sAge.trim().length() > 0 )
				group.setAttribute("Age", sAge);
			if( sSex != null && sSex.trim().length() > 0 )
				group.setAttribute("Sex", sSex);
		}
		return group;
	}
	
	public void addPDFAttachement( byte[] pdfBytes, String sFileName ) {
		if( isValidDoc() && pdfBytes != null && pdfBytes.length > 0 ) {
			String sPDF64 = new String(Base64.encodeBase64(pdfBytes));
			try {
				Element attach = doc.createElement("Attachment");
				root.appendChild(attach);
				attach.setAttribute("DocType", "PDF CVI");
				attach.setAttribute("MimeType", "application/pdf");
				attach.setAttribute("Filename", sFileName);
				Element payload = doc.createElement("Payload");
				attach.appendChild(payload);
				payload.setTextContent(sPDF64);
			} catch ( Exception e) {
				logger.error("Should not see this error for unsupported encoding", e);
			}
		}
	}
	
	public void addMetadataAttachement( CviMetaDataXml metaData ) {
		String sXML = metaData.getXmlString();
		try {
			byte[] xmlBytes = sXML.getBytes("UTF-8");
			if( isValidDoc() && xmlBytes != null && xmlBytes.length > 0 ) {
				String sMetadata64 = javax.xml.bind.DatatypeConverter.printBase64Binary(xmlBytes);
				Element attach = doc.createElement("Attachment");
				root.appendChild(attach);
				attach.setAttribute("DocType", "Other");
				attach.setAttribute("MimeType", "text/xml");
				attach.setAttribute("Filename", "CviMetadata.xml");
				Element payload = doc.createElement("Payload");
				attach.appendChild(payload);
				payload.setTextContent(sMetadata64);
			}
		} catch (UnsupportedEncodingException e1) {
			logger.error(e1);
		} catch ( Exception e) {
			logger.error("Should not see this error for unsupported encoding", e);
		}
	}
	
	private void checkExpiration() {
		String sExp = root.getAttribute("ExpirationDate");
		java.util.Date dExp = null;
		int iValidDays = CivetConfig.getCviValidDays();
		Calendar cal = Calendar.getInstance();
		boolean bSet = false;
		try {
			dExp = dateFormat.parse(sExp);
		} catch (ParseException e) {
			dExp = null;
		}
		NodeList nl = root.getElementsByTagName("Animal");
		for( int i = 0; i < nl.getLength(); i++ ) {
			Element e = (Element)nl.item(i);
			String sInsp = e.getAttribute("InspectionDate");
			if( sInsp != null && sInsp.trim().length() > 0 ) {
				java.util.Date dInsp = null;
				try {
					dInsp = dateFormat.parse(sInsp);
					if( dInsp != null ) {
						cal.setTime(dInsp);
						cal.add(Calendar.DATE, iValidDays);
						if( dExp == null  || dExp.compareTo(cal.getTime()) > 0 ) {
							dExp = cal.getTime();
							bSet = true;
						}
							
					}
				} catch (ParseException pe) {
					dExp = null;
				}
			}
		}
		if( bSet ) {
			String sNewExp = dateFormat.format(dExp);
			root.setAttribute("ExpirationDate", sNewExp);
		}
	}
	
	public String getXMLString() {
		checkExpiration();
		return helper.getXMLString();
	}
	
	private Node childNodeByName( Element n, String sName ) {
		Node nChild = null;
		if( n != null ) {
			NodeList nl = n.getElementsByTagName(sName);
			for( int i = 0; i < nl.getLength(); i++ ) {
				Node nNext = nl.item(i);
				if( nNext instanceof Element ) {
					nChild = nNext;
					break;
				}
			}
		}
		return nChild;
	}
	
	private Node childNodeByNames( Element n, String sNames ) {
		Node nChild = null;
		if( n != null ) {
			StringTokenizer tok = new StringTokenizer(sNames, ",");
			while( nChild == null && tok.hasMoreTokens() ) {
				String sName = tok.nextToken();
				NodeList nl = n.getElementsByTagName(sName);
				for( int i = 0; i < nl.getLength(); i++ ) {
					Node nNext = nl.item(i);
					if( nNext instanceof Element ) {
						nChild = nNext;
						break;
					}
				}
			}
		}
		return nChild;
	}
	
	private Element childElementByName( Element n, String sName ) {
		Element dChild = null;
		if( n != null ) {
			NodeList nl = n.getElementsByTagName(sName);
			for( int i = 0; i < nl.getLength(); i++ ) {
				Node nNext = nl.item(i);
				if( nNext instanceof Element ) {
					dChild = (Element)nNext;
					break;
				}
			}
		}
		return dChild;
	}
	
	private Element childElementByNames( Element n, String sNames ) {
		Element eChild = null;
		if( n != null ) {
			StringTokenizer tok = new StringTokenizer(sNames, ",");
			while( eChild == null && tok.hasMoreTokens() ) {
				String sName = tok.nextToken();
				NodeList nl = n.getElementsByTagName(sName);
				for( int i = 0; i < nl.getLength(); i++ ) {
					Node nNext = nl.item(i);
					if( nNext instanceof Element ) {
						eChild = (Element)nNext;
						break;
					}
				}
			}
		}
		return eChild;
	}

}
