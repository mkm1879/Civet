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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.xml.elements.*;
import edu.clemson.lph.controls.PhoneField;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.utils.AnimalIDUtils;
import edu.clemson.lph.utils.FileUtils;
import edu.clemson.lph.utils.XMLUtility;

// TODO Refactor rename to StdXmlDataModel
public class StdeCviXmlModel {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	public static final String sDateFormat = "yyyy-MM-dd";
	private static final String allElements = "Veterinarian,MovementPurposes,Origin,Destination,Consignor,Consignee," +
	                                   "Carrier,TransportMode,Accessions,Animal,GroupLot,Statements,Attachment," +
			                           "MiscAttribute,Binary";
	private XMLDocHelper helper;
	private StdeCviBinaries binaries;
	private CviMetaDataXml metaData;
	
	/**
	 * This constructs a list of elements before which an inserted new element should appear by
	 * listing the first element that might follow.
	 * Should this do a little more work and let you list the actual element rather than the one
	 * following?
	 * @param sElement
	 * @return
	 */
	static String getFollowingElementList(String sElement) {
		String sRet = sElement;
		int iIndex = allElements.indexOf(sRet);
		if( iIndex > 0 ) 
			sRet = allElements.substring(iIndex);
		return sRet;
	}
	
	public static SimpleDateFormat getDateFormat() {
		return new SimpleDateFormat( sDateFormat );
	}
	

	/**
	 * Create an empty XML document.
	 */
	public StdeCviXmlModel() {
		try {
			DocumentBuilder db = SafeDocBuilder.getSafeDocBuilder(); //DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.newDocument();
			doc.setXmlStandalone(true);
			Element root = doc.createElementNS("http://www.usaha.org/xmlns/ecvi2", "eCVI");
			doc.appendChild(root);
			helper = new XMLDocHelper( doc, root );
			binaries = new StdeCviBinaries( helper );
			metaData = new CviMetaDataXml();
		} catch (Exception e ) {
			logger.error(e);
		}
	}

	/** 
	 * Create an XML document from the raw DOM object.  
	 * @param doc
	 */
//	public StdeCviXmlModel( Document doc ) {
//		doc.setXmlStandalone(true);
//		Element root = doc.getDocumentElement();
//		helper = new XMLDocHelper( doc, root );
//		binaries = new StdeCviBinaries( helper );
//		metaData = binaries.getMetaData();
//	}
	

	/** 
	 * Create an XML document from a File.  
	 * @param doc
	 */
	public StdeCviXmlModel( File fXML ) {
		try {
			byte[] xmlBytes = FileUtils.readUTF8File(fXML);
			createModel(xmlBytes);		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
	}

	
	/** 
	 * Create an XML document from the raw XML string.  
	 * @param doc
	 */
	public StdeCviXmlModel( byte[] xmlBytes ) {
		createModel(xmlBytes);
	}
	
	/**
	 * Destroy all data and start clean!
	 * But leave attachment.  
	 */
	public void clear() {
		byte[] pdfBytes = getPDFAttachmentBytes();
		String sFileName = getPDFAttachmentFilename();
		DocumentBuilder db = SafeDocBuilder.getSafeDocBuilder(); //DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = db.newDocument();
		doc.setXmlStandalone(true);
		Element root = doc.createElementNS("http://www.usaha.org/xmlns/ecvi2", "eCVI");
		doc.appendChild(root);
		helper = new XMLDocHelper( doc, root );
		binaries = new StdeCviBinaries( helper );
		binaries.setOrUpdatePDFAttachment(pdfBytes, sFileName);
		metaData = new CviMetaDataXml();
	}

	/** 
	 * Create an XML document from the raw XML string.  
	 * @param doc
	 */
	public void createModel( byte[] xmlBytes ) {
		try {
			Document doc = null;
			Element root = null;
			if( xmlBytes != null ) {
				DocumentBuilder db = SafeDocBuilder.getSafeDocBuilder(); 
//				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//				byte[] xmlBytes = sXML.getBytes();
				InputStream is = new ByteArrayInputStream( xmlBytes );
				doc = db.parse(is);
				doc.setXmlStandalone(true);
				root = doc.getDocumentElement();
			}
			helper = new XMLDocHelper( doc, root );
			binaries = new StdeCviBinaries( helper );
		} catch (Exception e ) {
			logger.error(e);
		}
	}

	private boolean isValidDoc() {
		return helper != null && helper.isInitialized();
	}
	
	/**
	 * Set issue and expiration dates based on configured validity period
	 * NOTE: this is not quite right.  Expiration should be 30 after the earliest
	 * inspected date!  So see checkExpiration() call before printing XML
	 * @param dIssued
	 */
	public void setIssueDate( java.util.Date dIssued ) {
		if( isValidDoc() && dIssued != null) {
			String sDate = getDateFormat().format(dIssued);
			int iValidDays = CivetConfig.getCviValidDays();
			if( sDate !=  null && iValidDays > 0 ) {
				helper.setAttribute(helper.getRootElement(), "IssueDate", sDate);		
				Calendar cal = Calendar.getInstance();
				cal.setTime(dIssued);
				cal.add(Calendar.DATE, iValidDays);
				String sExp = getDateFormat().format(cal.getTime());
				if( sExp != null && sExp.trim().length() > 0 )
					helper.setAttribute(helper.getRootElement(), "ExpirationDate", sExp);
			}
		}
	}
	
	/**
	 * Set the vet from just a name string (Imports)
	 * @param sName
	 * @return
	 */
	public Element setVet( String sName ) {
		Element eVet = getVetElement();
		if( eVet != null )
			helper.removeElement(eVet);
		Person vetPerson = new Person(sName, null, null);
		Veterinarian vet = new Veterinarian(vetPerson, null, null, null, null );
		return setVet(vet);
	}
	
	public Element setVet( Veterinarian vet ) {
		Element eVet = null;
		if( !isValidDoc() )
			return null;
		if( vet.person != null ) {
			String sAfter = getFollowingElementList("MovementPurposes");
			eVet = helper.getOrInsertElementBefore("Veterinarian", sAfter);
			if( vet.licenseNumber != null && vet.licenseNumber.trim().length() > 0 )
				helper.setAttribute(eVet, "LicenseNumber", vet.licenseNumber);
			if( vet.nationalAccreditationNumber  != null && vet.nationalAccreditationNumber .trim().length() > 0 )
				helper.setAttribute(eVet, "NationalAccreditationNumber", vet.nationalAccreditationNumber );
			if( vet.address != null && ( vet.address.state == null || vet.address.state.trim().length() == 0 ) ) {
				String sOriginState = getOriginStateCode();
				if( sOriginState != null && sOriginState.trim().length() > 0 )
					vet.address.state = sOriginState;
				else // Things are really a mess use an obscure state code to get past schema
					vet.address.state = "AA";
				// Actually write to the XML
				Element eAddr = helper.getOrAppendChild(eVet, "Address");
				Element eState = helper.getOrAppendChild(eAddr, "State");
				eState.setTextContent(vet.address.state);
			}
			Element person = helper.getOrAppendChild(eVet,"Person");
			if (vet.person.nameParts != null) {  
				Element eName = helper.getOrAppendChild(person, "Name");
				if( eName != null)
					helper.removeElement(eName);
				Element eNameParts = helper.getOrAppendChild(person, "NameParts");
				sAfter = "LastName";
				if( vet.person.nameParts.firstName != null && vet.person.nameParts.firstName.trim().length() > 0 ) {
					Element eFirstName = helper.getOrInsertElementBefore(eNameParts, "FirstName", sAfter);
					eFirstName.setTextContent(vet.person.nameParts.firstName);
				}
				if( vet.person.nameParts.lastName != null && vet.person.nameParts.lastName.trim().length() > 0 ) {
					Element eLastName = helper.getOrAppendChild(eNameParts, "LastName");
					eLastName.setTextContent(vet.person.nameParts.lastName);
				}
			}
			else if( vet.person.name != null && vet.person.name.length() > 0 ) {
				Element eName = helper.getOrAppendChild(person, "Name");
				eName.setTextContent(vet.person.name);
			}

			if( vet.person.phone != null && vet.person.phone.trim().length() > 0 ) {
				Element phone = helper.getOrAppendChild(person, "Phone");
				helper.setAttribute(phone, "Type", "Unknown");
				helper.setAttribute(phone, "Number", vet.person.phone);
			}	
		}
		return eVet;
	}
	
	
	public Element getVetElement() {
		Element eVet = helper.getElementByName("Veterinarian");
		return eVet;
	}
	
	public Veterinarian getVet() {
		Veterinarian vet = null;
		Element eVet = helper.getElementByName("Veterinarian");
		if( eVet != null ) {
			Address address = null;
			Element eAddress = helper.getChildElementByName(eVet, "Address");
			if( eAddress != null ) {
				address = getAddress(eAddress);
			}
			String sLicenseState = eVet.getAttribute("LicenseState");
			if( sLicenseState == null || sLicenseState.trim().length() == 0 ) {
				String sOriginState = getOriginStateCode();
				if( sOriginState != null && sOriginState.trim().length() > 0 )
					sLicenseState = sOriginState;
				else // Things are really a mess use an obscure state code to get past schema
					sLicenseState = "AA";
			}
			String sLicenseNumber = eVet.getAttribute("LicenseNumber");
			String sNationalAccreditationNumber = eVet.getAttribute("NationalAccreditationNumber");
			Person vetPerson = getPerson(eVet);
			vet = new Veterinarian( vetPerson, address, 
							sLicenseState, sLicenseNumber, sNationalAccreditationNumber);
		}
		return vet;
	}

	/**
	 * Used to add address to Veterinarian, Origin and Destination
	 * Removes child elements if value is not null by zero length
	 * @param e
	 * @param sStreet
	 * @param sCity
	 * @param sState
	 * @param sZip
	 * @return
	 */
	private Element setAddress( Element e, Address addr ) {
		if( !isValidDoc() )
			return null;
		Element address = null;
		if( e != null ) {
			String sAfter = null;
			address = helper.getOrAppendChild(e, "Address" );
			if( addr.line1 != null && addr.line1.trim().length() > 0 ) {
				sAfter = "Line2,Town,County,State,ZIP,Country,GeoPoint";
				Element line1 = helper.getOrInsertElementBefore( address, "Line1", sAfter);
				line1.setTextContent(addr.line1.trim());
			} 
			else if( addr.line1 != null && addr.line1.trim().length() == 0 ) {
				helper.removeChild(address, "Line1");
			}
			if( addr.town != null && addr.town.trim().length() > 0 ) {
				sAfter = "County,State,ZIP,Country,GeoPoint";
				Element town = helper.getOrInsertElementBefore( address, "Town", sAfter);
				town.setTextContent(addr.town.trim());
			}
			else if( addr.town != null && addr.town.trim().length() == 0 ) {
				helper.removeChild( address, "Town" );
			}
			sAfter = "ZIP,Country,GeoPoint";
			Element state = helper.getOrInsertElementBefore( address, "State", sAfter);
			if( addr.state != null ) {
				state.setTextContent(addr.state.trim());
			}
			else {
				logger.error("Attempt to add address with no state.", new Exception());
				state.setTextContent("");				
			}
			if( addr.county != null && addr.county.trim().length() >= 3 ) {
				sAfter = "State,ZIP,Country,GeoPoint";
				Element county = helper.getOrInsertElementBefore( address, "County", sAfter);
				county.setTextContent(addr.county.trim());
			}
			else if( addr.county != null && addr.county.trim().length() == 0 ) {
				helper.removeChild(address, "County");
			}
			if( addr.zip != null && addr.zip.trim().length() > 0 ) {
				sAfter = "Country,GeoPoint";
				Element zip = helper.getOrInsertElementBefore( address, "ZIP", sAfter);
				zip.setTextContent(addr.zip);
			}
			else {
				helper.removeChild(address, "ZIP");
			}
		}
		return address;
	}
	
	/**
	 * Assume single purpose even though standard supports multiple.
	 * @param sPurpose
	 */
	public void setPurpose( String sPurpose ) {
		if( isValidDoc() && sPurpose != null && sPurpose.trim().length() > 0 ) {
			String sAfter = getFollowingElementList("Origin");
			Element purposes = helper.getOrInsertElementBefore( "MovementPurposes", sAfter );
			Element purpose = helper.getOrAppendChild(purposes, "MovementPurpose");
			purpose.setTextContent(sPurpose);
		}
	}
	
	/**
	 * Get just the first movement purpose.
	 * @return
	 */
	public String getPurpose() {
		String sRet = null;
		String sPath = "MovementPurposes/MovementPurpose";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}
	
	/**
	 * Set values in the Premises record for the element sElementName with values listed in String params.
	 * Do not overwrite with null values, but do overwrite changes. 
	 * For convenience, these are the values with fields in the Edit Dialog.
	 * @param sElement
	 * @param sPIN
	 * @param sPremName
	 * @param sPhone
	 * @param sAddressLine1
	 * @param sCity
	 * @param sCounty
	 * @param sStateCode
	 * @param sZipCode
	 * @return
	 */
	public Element setPremises(String sElement,  String sPIN, String sPremName, String sPhone, 
			String sAddressLine1, String sCity, String sCounty, String sStateCode, String sZipCode) {
		Address address = new Address(sAddressLine1, null, sCity, sCounty, sStateCode, sZipCode, "USA",
				null, null);
		Premises prem = new Premises(sPIN, sPremName, address, (String)null, sPhone, null );
		return setPremises( prem, sElement);
	}

	/**
	 * Set values in the Premises record for the element sElementName with values in prem
	 * Do not overwrite with null values, but do overwrite changes.
	 * @param prem
	 * @param sElementName
	 * @return
	 */
	public Element setPremises( Premises prem, String sElement ) {
		Element ePremises = helper.getElementByName(sElement);
		if( ePremises != null ) {
			updatePremises( ePremises, prem );
		}
		else {
			ePremises = addPremises( prem, sElement );
		}
		return ePremises;
	}

	/**
	 * Update values from prem input but do NOT remove existing values
	 * @param ePremises Existing Premises element
	 * @param prem Premises object with data to write
	 * @return same ePremises element with updated data
	 */
	private Element updatePremises( Element ePremises, Premises prem ) {
		String sAfter = null;
		if( !isValidDoc() )
			return null;
		if( prem.premid  != null && prem.premid.trim().length() > 0 ) {
			sAfter = "PremName,Address,StateZoneOrAreaStatus,HerdOrFlockStatus,Person";
			Element pin = helper.getOrInsertElementBefore(ePremises, "PremId", sAfter);
			pin.setTextContent(prem.premid);
		}
		if( prem.premName != null && prem.premName.trim().length() > 0 ) {
			sAfter = "Address,StateZoneOrAreaStatus,HerdOrFlockStatus,Person";
			Element premName = helper.getOrInsertElementBefore(ePremises,"PremName",sAfter);
			premName.setTextContent(prem.premName);
		}
		setAddress( ePremises, prem.address );
		if( (prem.personName != null && prem.personName.trim().length() > 0 ) 
				|| (prem.personNameParts != null) ) {
			Element person = helper.getOrAppendChild(ePremises,"Person");
			// Careful of choice between name and name parts
			if( prem.personNameParts != null ) {  // This condition will only occur when prem
				// was populated by incoming data and thus should not have Name
				Element ePersonName = helper.getChildElementByName(person, "Name");
				if( ePersonName != null ) {
					logger.error( "Attempt to update Name with NameParts in " + ePremises.getTagName() );
					return ePremises;
				}
				sAfter = "Name,Phone,Email";
				Element nameParts = helper.getOrInsertElementBefore(person,"NameParts",sAfter);
				if( prem.personNameParts.businessName != null && prem.personNameParts.businessName.trim().length() > 0 ) {
					sAfter = "OtherName,MiddleName,LastName,OtherName";
					Element businessName = helper.getOrInsertElementBefore(nameParts,"BusinessName",sAfter);
					businessName.setTextContent(prem.personNameParts.businessName);
				}
				if( prem.personNameParts.firstName != null && prem.personNameParts.firstName.trim().length() > 0 ) {
					sAfter = "MiddleName,LastName,OtherName";
					Element firstName = helper.getOrInsertElementBefore(nameParts,"OtherName",sAfter);
					firstName.setTextContent(prem.personNameParts.firstName);
				}
				if( prem.personNameParts.middleName != null && prem.personNameParts.middleName.trim().length() > 0 ) {
					sAfter = "LastName,OtherName";
					Element middleName = helper.getOrInsertElementBefore(nameParts,"MiddleName",sAfter);
					middleName.setTextContent(prem.personNameParts.middleName);
				}
				if( prem.personNameParts.lastName != null && prem.personNameParts.lastName.trim().length() > 0 ) {
					sAfter = "OtherName";
					Element lastName = helper.getOrInsertElementBefore(nameParts,"LastName",sAfter);
					lastName.setTextContent(prem.personNameParts.lastName);
				}
			}			
			else if( (prem.personName != null && prem.personName.trim().length() > 0) ||
					(prem.personPhone != null && prem.personPhone.trim().length() > 0) ) {
				Element ePersonNameParts = helper.getChildElementByName(person, "NameParts");
				if( ePersonNameParts != null ) {
					logger.error( "Attempt to update NameParts with Name in " + ePremises.getTagName() );
					return ePremises;
				}
				if( (prem.personName == null || prem.personName.trim().length() == 0) ) {
					if(prem.premName != null && prem.premName.trim().length() > 0 )
						prem.personName = prem.premName;
					else 
						prem.personName = "Not provided";
				}
				sAfter = "Phone,Email";
				Element name = helper.getOrInsertElementBefore(person,"Name",sAfter);
				name.setTextContent(prem.personName);
			}
			if( prem.personPhone != null && checkPhoneLength(prem.personPhone) ) {
				sAfter = "Email";
				Element phone = helper.getOrInsertElementBefore(ePremises,"Phone",sAfter);
				person.appendChild(phone);
				helper.setAttribute(phone, "Type", "Unknown");
				helper.setAttribute(phone, "Number", prem.personPhone);
			}	
		}
		return ePremises;
	}

	/**
	 * Set values in the Premises record for the element sElementName with values in prem
	 * @param prem
	 * @param sElementName
	 * @return
	 */
	private Element addPremises( Premises prem, String sElementName ) {
		if( !isValidDoc() )
			return null;
		String sAfter = ("Origin".equals(sElementName)?getFollowingElementList("Destination"):getFollowingElementList("Consignor") );
		Element ePremises = helper.getOrInsertElementBefore(sElementName, sAfter);
		return updatePremises(ePremises, prem);
	}

	
	private boolean checkPhoneLength( String sPhone ) {
		boolean bRet = true;
		if( sPhone == null ) return false;
		int iLenPhoneDigits = PhoneField.formatDigitsOnly(sPhone).trim().length();
		if( sPhone.trim().length() > 0 && iLenPhoneDigits != 10 ) {
			MessageDialog.messageLater(null, "Civet Error: Phone format", "CVI standard requires ten digit phone");
			bRet = false;
		}
		return bRet;
	}
	
	private Premises getPremises( String sElementName ) {
		Premises prem = null;
		Element ePremises = helper.getElementByName(sElementName);
		if( ePremises == null )
			return new Premises();
		String premid = null;
		Element ePIN = helper.getChildElementByName(ePremises, "PremId");
		if( ePIN != null )
			premid = ePIN.getTextContent();
		String premName = null;
		Element ePremName = helper.getChildElementByName(ePremises, "PremName");
		if( ePremName != null )
			premName = ePremName.getTextContent();
		Address address = getAddress(ePremises);
		Element ePerson = helper.getChildElementByName(ePremises, "Person");
		if(ePerson != null ) {
			String phone = null;
			Element ePhone = helper.getChildElementByName(ePerson, "Phone");
			if( ePhone != null )
				phone = ePhone.getAttribute("Number");
			String email = null;
			Element eEmail = helper.getChildElementByName(ePerson, "Email");
			if( eEmail != null )
				email = eEmail.getAttribute("Address");
			Element eNameParts = helper.getChildElementByName(ePerson, "NameParts");
			if( eNameParts != null ) {
				String businessName = eNameParts.getAttribute("BusinessName");
				String firstName = eNameParts.getAttribute("OtherName");
				String middleName = eNameParts.getAttribute("MiddleName");
				String lastName = eNameParts.getAttribute("LastName");
				String otherName = eNameParts.getAttribute("OtherName");
				NameParts parts = new NameParts( businessName, firstName, middleName, lastName, otherName );
				prem = new Premises(premid, premName, address, parts, phone, email);

			} else {
				Element eName = helper.getChildElementByName(ePerson, "Name");
				if( eName != null ) {
					String name = eName.getTextContent();
					prem = new Premises(premid, premName, address, name, phone, email);
				}
			}
		}
		else {
			prem = new Premises(premid, premName, address);
		}
		return prem;
	}
	
	private Address getAddress( Element eAddress ) {
		String line1 = null;
		Element eLine1 = helper.getChildElementByName(eAddress, "Line1");
		if( eLine1 != null)
			line1 = eLine1.getTextContent();
		String line2 = null;
		Element eLine2 = helper.getChildElementByName(eAddress, "Line2");
		if( eLine2 != null )
			line2 = eLine2.getTextContent();
		String town = null;
		Element eTown = helper.getChildElementByName(eAddress, "Town");
		if( eTown != null )
			town = eTown.getTextContent();
		String county = null;
		Element eCounty = helper.getChildElementByName(eAddress, "County");
		if( eCounty != null )
			county = eCounty.getTextContent();
		String state = null;
		Element eState = helper.getChildElementByName(eAddress, "State");
		if( eState != null )
			state = eState.getTextContent();
		String zip = null;
		Element eZIP = helper.getChildElementByName(eAddress, "ZIP");
		if( eZIP != null )
			zip = eZIP.getTextContent();
		String country = null;
		Element eCountry = helper.getChildElementByName(eAddress, "Country");
		if( eCountry != null )
			country = eCountry.getTextContent();
		Double latitude = null;
		Double longitude = null;
		Element eGeoPoint = helper.getChildElementByName(eAddress, "GeoPoint");
		if( eGeoPoint != null ) {
			String sLatitude = eGeoPoint.getAttribute("Latitude");
			latitude = Double.parseDouble(sLatitude);
			String sLongitude = eGeoPoint.getAttribute("Longitude");
			longitude = Double.parseDouble(sLongitude);
		}
		Address address = new Address(line1, line2, town, county, state, zip, country,
				latitude, longitude);
		return address;
	}

	//	public Element setPremises(String sElement,  String sPIN, String sPremName,  
	//		String sAddressLine1, String sCity, String sCounty, String sStateCode, String sZipCode) {

	public Element setOrigin(String sOriginPIN, String sOriginName, String sOriginPhone, String sOriginAddress, 
			String sOriginCity, String sOriginCounty, String sOriginStateCode, String sOriginZipCode) {
		return setPremises( "Origin", sOriginPIN, sOriginName, sOriginPhone, sOriginAddress, 
			sOriginCity, sOriginCounty, sOriginStateCode, sOriginZipCode);
	}

	public Element setOrigin( Premises premises ) {
		return setPremises( premises, "Origin");
	}
	
	/** 
	 * Simple convenience method to encapsulate lots of null checks.
	 * @return
	 */
	public String getOriginState() {
		String sRet = null;
		Premises pOrigin = getOrigin();
		if( pOrigin != null ) {
			Address address = pOrigin.address;
			if( address != null ) {
				sRet = address.state;
			}
		}
		return sRet;
	}
	
	public Premises getOrigin() {
		return getPremises("Origin");
	}

	public Element setDestination(String sOriginPIN, String sOriginName, String sOriginPhone, String sOriginAddress, 
			String sOriginCity, String sOriginCounty, String sOriginStateCode, String sOriginZipCode) {
		return setPremises( "Destination", sOriginPIN, sOriginName, sOriginPhone, sOriginAddress, 
			sOriginCity, sOriginCounty, sOriginStateCode, sOriginZipCode);
	}

	public Element setDestination( Premises premises ) {
		return setPremises( premises, "Destination");
	}
	
	/** 
	 * Simple convenience method to encapsulate lots of null checks.
	 * @return
	 */
	public String getDestinationState() {
		String sRet = null;
		Premises pDestination = getOrigin();
		if( pDestination != null ) {
			Address address = pDestination.address;
			if( address != null ) {
				sRet = address.state;
			}
		}
		return sRet;
	}
	
	public Premises getDestination() {
		return getPremises("Destination");
	}
	
	private Element setContact( Contact cont, String sElementName ) {
		if( !isValidDoc() )
			return null;
		String sAfter = ("Consignee".equals(sElementName)?getFollowingElementList("Carrier"):getFollowingElementList("Consignee") );
		Element eContact = helper.getOrInsertElementBefore(sElementName, sAfter);
		if( cont.address  != null ) {
			sAfter = "Person";
			Element address = helper.getOrInsertElementBefore(eContact, "Address", sAfter);
			eContact.appendChild(address);
			setAddress(address, cont.address);
		}
		Element ePerson = helper.getOrAppendChild(eContact,"PremName");
		sAfter = "Phone,Email";
		// Careful of choice between name and name parts
		if( cont.person.name != null && cont.person.name.trim().length() > 0 ) {
			helper.removeChild(ePerson, "NameParts");
			sAfter = "Phone,Email";
			Element name = helper.getOrInsertElementBefore(ePerson,"Name",sAfter);
			name.setTextContent(cont.person.name);
		}
		else if( cont.person.nameParts != null ) {
			helper.removeChild(ePerson, "Name");  // If we have parts DO NOT include single name
			sAfter = "Name,Phone,Email";
			Element nameParts = helper.getOrInsertElementBefore(ePerson,"NameParts",sAfter);
			if( cont.person.nameParts.businessName != null && cont.person.nameParts.businessName.trim().length() > 0 ) {
				sAfter = "OtherName,MiddleName,LastName,OtherName";
				Element businessName = helper.getOrInsertElementBefore(nameParts,"BusinessName",sAfter);
				businessName.setTextContent(cont.person.nameParts.businessName);
			}
			if( cont.person.nameParts.firstName != null && cont.person.nameParts.firstName.trim().length() > 0 ) {
				sAfter = "MiddleName,LastName,OtherName";
				Element firstName = helper.getOrInsertElementBefore(nameParts,"OtherName",sAfter);
				firstName.setTextContent(cont.person.nameParts.firstName);
			}
			if( cont.person.nameParts.middleName != null && cont.person.nameParts.middleName.trim().length() > 0 ) {
				sAfter = "LastName,OtherName";
				Element middleName = helper.getOrInsertElementBefore(nameParts,"MiddleName",sAfter);
				middleName.setTextContent(cont.person.nameParts.middleName);
			}
			if( cont.person.nameParts.lastName != null && cont.person.nameParts.lastName.trim().length() > 0 ) {
				sAfter = "OtherName";
				Element lastName = helper.getOrInsertElementBefore(nameParts,"LastName",sAfter);
				lastName.setTextContent(cont.person.nameParts.lastName);
			}
		}			
		if( cont.person.phone != null && checkPhoneLength(cont.person.phone) ) {
			sAfter = "Email";
			Element phone = helper.getOrInsertElementBefore(eContact,"Phone",sAfter);
			ePerson.appendChild(phone);
			helper.setAttribute(phone, "Type", "Unknown");
			helper.setAttribute(phone, "Number", cont.person.phone);
		}	
		if( cont.person.email != null && cont.person.email.trim().length() > 0 ) {
			Element email = helper.getOrAppendChild(eContact,"Email");
			ePerson.appendChild(email);
			helper.setAttribute(email, "Address", cont.person.email);
		}	
		return eContact;
	}
	
	public Element setConsignor( Contact contact ) {
		return setContact( contact, "Consignor");
	}

	public Element setConsignee( Contact contact ) {
		return setContact( contact, "Consignee");
	}
	
	private Contact getContact( String sElementName ) {
		Contact contact = null;
		Element eContact = helper.getElementByName( sElementName );
		if( eContact != null ) {
			Address address = null;
			Element eAddress = helper.getChildElementByName(eContact, "Address");
			if( eAddress != null ) {
				address = getAddress(eAddress);
			}
			Person person = getPerson( eContact );
			contact = new Contact( address, person );
		}
		return contact;
	}
	
	private Person getPerson( Element eElement ) {
		Person person = null;
		Element ePerson = helper.getChildElementByName(eElement, "Person");
		if( ePerson != null ) {
			Element ePhone = helper.getChildElementByName(ePerson, "Phone");
			String sPhone = null;
			if( ePhone != null ) {
				sPhone = ePhone.getAttribute("Number");
			}
			Element eEmail = helper.getChildElementByName(ePerson, "Email");
			String sEmail = null;
			if( eEmail != null ) {
				sEmail = eEmail.getAttribute("Address");
			}
			Element eName = helper.getChildElementByName(ePerson, "Name");
			Element eNameParts = helper.getChildElementByName(ePerson, "NameParts");
			if( eName != null ) {
				String sName = eName.getTextContent();
				person = new Person( sName, sPhone, sEmail );
			}
			else if( eNameParts != null ) {
				Element eBusinessName = helper.getChildElementByName(eNameParts,"BusinessName");
				String sBusinessName = null;
				if( eBusinessName != null ) {
					sBusinessName = eBusinessName.getTextContent();
				}
				Element eFirstName = helper.getChildElementByName(eNameParts,"FirstName");
				String sFirstName = null;
				if( eFirstName != null ) {
					sFirstName = eFirstName.getTextContent();
				}
				Element eMiddleName = helper.getChildElementByName(eNameParts,"MiddleName");
				String sMiddleName = null;
				if( eMiddleName != null ) {
					sMiddleName = eMiddleName.getTextContent();
				}
				Element eLastName = helper.getChildElementByName(eNameParts,"LastName");
				String sLastName = null;
				if( eLastName != null ) {
					sLastName = eLastName.getTextContent();
				}
				Element eOtherName = helper.getChildElementByName(eNameParts,"OtherName");
				String sOtherName = null;
				if( eOtherName != null ) {
					sOtherName = eOtherName.getTextContent();
				}
				NameParts nameParts = new NameParts( sBusinessName, sFirstName, sMiddleName, sLastName, sOtherName);
				person = new Person( nameParts, sPhone, sEmail );
			}			
		}
		return person;
	}
	
	public Contact getConsignor() {
		return getContact("Consignor");
	}

	public Contact getConsignee() {
		return getContact("Consignee");
	}

	public void clearAnimals() {
		ArrayList<Element> aAnimals = helper.getElementsByName("Animal");
		if( aAnimals != null ) {
			for( Element eAnimal : aAnimals) {
				helper.removeElement(eAnimal);
			}
		}
	}
	
	/**
	 * Remove an animal that already exists in XML as element eAnimal.
	 * @param animalData
	 */
	public void removeAnimal( Animal animalData ) {
		Element eAnimal = animalData.eAnimal;
		if( eAnimal != null )
			helper.removeElement(eAnimal);
	}
	
	public Animal findOrAddAnimal( String sSpeciesCode, String sTag ) {
		Animal animal = null;
		for( Animal aIn : getAnimals() ) {
			if( aIn.speciesCode.code.equals(sSpeciesCode) ) {
				animal = aIn;
				break;
			}
		}
		if( animal == null ) {
			animal = new Animal(sSpeciesCode, sTag );
			Element eAnimal = addAnimal( animal );
			animal.eAnimal = eAnimal;
		}
		return animal;
	}

	public Element addAnimal( Animal animalData ) {
		if( !isValidDoc() ) 
			return null;
		String sAfter = getFollowingElementList("GroupLot");
		Element eAnimal = helper.insertElementBefore("Animal", sAfter);
		if( animalData.speciesCode.isStandardCode ) {
			Element speciesCode = helper.appendChild(eAnimal, "SpeciesCode");
			helper.setAttribute(speciesCode, "Code", animalData.speciesCode.code);
		}
		else {
			Element speciesOther = helper.appendChild(eAnimal, "SpeciesOther");
			helper.setAttribute(speciesOther, "Code", animalData.speciesCode.code);
			helper.setAttribute(speciesOther, "Text", animalData.speciesCode.text);
		}
		Element animalTags = helper.appendChild(eAnimal, "AnimalTags");
		for( AnimalTag tag : animalData.animalTags ) {
			switch( tag.type ) {
			case AIN:
			case MfrRFID:
			case NUES9:
			case NUES8:
			case ManagementID:
				addIDNumber(animalTags, tag);
				break; 
			case OtherOfficialID:
				addOtherOfficialID(animalTags, tag);
				break;
			case BrandImage:
				addBrandImage(animalTags, tag);
				break; 
			case EquineDescription:
				addEquineDescription(animalTags, tag);
				break; 
			case EquinePhotographs:
				addEquinePhotographs(animalTags, tag);
				break; 
			default:
				logger.error("Unexpected tag " + tag.toString() );
			}

		}
		String sAge = animalData.age;
		if( sAge != null && sAge.trim().length() > 0 )
			helper.setAttribute(eAnimal, "Age", sAge);
		String sBreed = animalData.breed;
		if( sBreed != null && sBreed.trim().length() > 0 )
			helper.setAttribute(eAnimal, "Breed", sBreed);
		String sSex = animalData.sex;
		if( sSex != null && sSex.trim().length() > 0 )
			helper.setAttribute(eAnimal, "Sex", sSex);
		String sInsp = animalData.inspectionDate;
		// Required attribute, guess.
		if(sInsp == null ) {
			java.util.Date dIssue = getIssueDate();
			if( dIssue != null )
				sInsp = getDateFormat().format(getIssueDate());
			// Still might be blank but will get set at save time.
		}
		helper.setAttribute(eAnimal, "InspectionDate", sInsp);
		return eAnimal;
	}
	
	/**
	 * Convenience method to list all species in a cert.
	 * @return
	 */
	public String getSpeciesCodes() {
		StringBuffer sb = new StringBuffer();
		HashSet<String> codes = new HashSet<String>();
		for( Animal a : getAnimals() ) {
			String sCode = a.speciesCode.code;
			if( !codes.contains(sCode) )
				codes.add(sCode);
		}
		for( GroupLot g : getGroups() ) {
			String sCode = g.speciesCode.code;
			if( !codes.contains(sCode) )
				codes.add(sCode);
		}
		boolean bFirst = true;
		for( String s : codes ) {
			if( !bFirst )
				sb.append(", ");
			sb.append(s);
			bFirst = false;
		}
		return sb.toString();
	}
	
	public ArrayList<Animal> getAnimals() {
		ArrayList<Animal> animals = new ArrayList<Animal>();
		ArrayList<Element> aAnimals = helper.getElementsByName("Animal");
		if( aAnimals != null ) {
			for( Element eAnimal : aAnimals ) {
				SpeciesCode speciesCode = getSpeciesCode( eAnimal );
				ArrayList<AnimalTag> animalTags = getAnimalTags( eAnimal );
				String age = eAnimal.getAttribute( "Age");
				String breed = eAnimal.getAttribute( "Breed");
				String sex = eAnimal.getAttribute( "Sex");
				String inspectionDate = eAnimal.getAttribute( "InspectionDate");
				// populate above variables.
				Animal animal = new Animal(eAnimal, speciesCode, animalTags, age, breed, sex, inspectionDate);
				animals.add(animal);
			}
		}
		return animals;
	}
	
	/**
	 * Call late in editing to fill in any missing animal inspection dates.
	 * @param dInsp
	 */
	public void setDefaultAnimalInspectionDates( java.util.Date dInsp ) {
		for( Animal animal : getAnimals() ) {
			if( animal.inspectionDate == null ) {
				Element eAnimal = animal.eAnimal;
				String sInsp = getDateFormat().format(dInsp);
				helper.setAttribute(eAnimal, "InspectionDate", sInsp);
			}			
		}
	}

	public void editAnimal( Animal animalData ) {
		if( !isValidDoc() ) 
			return;
		Element eAnimal = animalData.eAnimal;
		if( animalData.speciesCode.isStandardCode ) {
			Element speciesCode = helper.getChildElementByName(eAnimal, "SpeciesCode");
			if( speciesCode == null ) {
				speciesCode = helper.appendChild(eAnimal, "SpeciesCode");
				Element speciesOther = helper.getChildElementByName(eAnimal, "SpeciesOther"); 
				if( speciesOther != null )
					helper.removeElement(speciesOther);
			}
			helper.setAttribute(speciesCode, "Code", animalData.speciesCode.code);
			helper.setAttribute(speciesCode, "Text", animalData.speciesCode.text);
		}
		else {
			Element speciesOther = helper.getChildElementByName(eAnimal, "SpeciesOther");
			if( speciesOther == null ) {
				speciesOther = helper.appendChild(eAnimal, "SpeciesOther");
				Element speciesCode = helper.getChildElementByName(eAnimal, "SpeciesCode"); 
				if( speciesCode != null )
					helper.removeElement(speciesCode);
			}
			helper.setAttribute(speciesOther, "Code", animalData.speciesCode.code);
			helper.setAttribute(speciesOther, "Text", animalData.speciesCode.text);
		}
		Element animalTags = helper.getChildElementByName(eAnimal, "AnimalTags");
		if( animalTags == null ) {
			animalTags = helper.appendChild(eAnimal, "AnimalTags");
		}
		for( AnimalTag tag : animalData.animalTags ) {
			if( getTag(eAnimal, tag.value) == null ) {
				switch( tag.type ) {
				case AIN:
				case MfrRFID:
				case NUES9:
				case NUES8:
				case OtherOfficialID:
				case ManagementID:
					addIDNumber(animalTags, tag);
					break; 
				case BrandImage:
					addBrandImage(animalTags, tag);
					break; 
				case EquineDescription:
					addEquineDescription(animalTags, tag);
					break; 
				case EquinePhotographs:
					addEquinePhotographs(animalTags, tag);
					break; 
				default:
					logger.error("Unexpected tag " + tag.toString() );
				}
			}
		}
		String sAge = animalData.age;
		if( sAge != null && sAge.trim().length() > 0 )
			helper.setAttribute(eAnimal, "Age", sAge);
		String sBreed = animalData.breed;
		if( sBreed != null && sBreed.trim().length() > 0 )
			helper.setAttribute(eAnimal, "Breed", sBreed);
		String sSex = animalData.sex;
		if( sSex != null && sSex.trim().length() > 0 )
			helper.setAttribute(eAnimal, "Sex", sSex);
		helper.setAttribute(eAnimal, "InspectionDate", animalData.inspectionDate);
	}
	
	public Element getTag( Element eAnimal, String sTagNum ) {
		Element eTag = null;
		eTag = helper.getElementByPathAndAttribute(eAnimal, "AnimalTags/AIN", "Number", sTagNum);
		if( eTag == null )
			eTag = helper.getElementByPathAndAttribute(eAnimal, "AnimalTags/MfrRFID", "Number", sTagNum);
		if( eTag == null )
			eTag = helper.getElementByPathAndAttribute(eAnimal, "AnimalTags/NUES9", "Number", sTagNum);
		if( eTag == null )
			eTag = helper.getElementByPathAndAttribute(eAnimal, "AnimalTags/NUES8", "Number", sTagNum);
		if( eTag == null )
			eTag = helper.getElementByPathAndAttribute(eAnimal, "AnimalTags/OtherOfficialID", "Number", sTagNum);
		if( eTag == null )
			eTag = helper.getElementByPathAndAttribute(eAnimal, "AnimalTags/ManagementID", "Number", sTagNum);
		if( eTag == null )
			eTag = helper.getElementByPathAndAttribute(eAnimal, "AnimalTags/EquineDescription", "Name", sTagNum);
		return eTag;
	}

	public ArrayList<GroupLot> getGroups() {
		ArrayList<GroupLot> groups = new ArrayList<GroupLot>();
		ArrayList<Element> aGroups = helper.getElementsByName("GroupLot");
		if( aGroups != null ) {
			for( Element eGroup : aGroups ) {
				SpeciesCode speciesCode = getSpeciesCode( eGroup );
				String groupLotId = null;
				Element eGroupLotId = helper.getChildElementByName(eGroup, "GroupLotID");
				if( eGroupLotId != null )
				 	groupLotId = eGroupLotId.getTextContent();
				String sQuantity = eGroup.getAttribute("Quantity");
				Double quantity = null;
				try {
					quantity = Double.parseDouble(sQuantity);
				} catch( NumberFormatException nfe ) {
					logger.error("Could not parse " + sQuantity + " as a Double in CVI " + getCertificateNumber() );
					quantity = 1.0;  // What other default makes sense?
				}
				String unit = eGroup.getAttribute("Unit");
				String age = eGroup.getAttribute( "Age");
				String breed = eGroup.getAttribute( "Breed");
				String sex = eGroup.getAttribute( "Sex");
				String description = eGroup.getAttribute( "Description");
				// populate above variables.
				GroupLot group = new GroupLot( eGroup, speciesCode, groupLotId, quantity, unit, age, breed, sex, description);
				groups.add(group);
			}
		}
		return groups;
	}

	
	private SpeciesCode getSpeciesCode( Element eAnimal ) {
		boolean bIsStandard = false;
		String sCode = null;
		String sText = null;
		Element eCode = helper.getChildElementByName(eAnimal, "SpeciesCode");
		if( eCode != null ) {
			bIsStandard = true;
			sCode = eCode.getAttribute("Code");
			sText = eCode.getAttribute("Text");
		}
		else {
			eCode = helper.getChildElementByName(eAnimal, "SpeciesOther");
			if( eCode != null ) {
				bIsStandard = false;
				sCode = eCode.getAttribute("Code");
				sText = eCode.getAttribute("Text");
			}
			
		}
		SpeciesCode code = new SpeciesCode(bIsStandard, sCode, sText);
		return code;
	}
	
	private ArrayList<AnimalTag> getAnimalTags( Element eAnimal ) {
		ArrayList<AnimalTag> animalTags = new ArrayList<AnimalTag>();
		Element eAnimalTags = helper.getChildElementByName(eAnimal, "AnimalTags");
		if( eAnimalTags != null ) {
			ArrayList<Element> aAnimalTags = helper.getChildElements(eAnimalTags);
			for( Element e : aAnimalTags ) {
				AnimalTag tag;
				String sType = e.getTagName();
				if( sType.equals("AIN") ) {
					tag = new AnimalTag( AnimalTag.Types.AIN, e.getAttribute("Number") );
				} else if( sType.equals("MfrRFID") ) {
					tag = new AnimalTag( AnimalTag.Types.MfrRFID, e.getAttribute("Number") );				
				} else if( sType.equals("NUES9") ) {
					tag = new AnimalTag( AnimalTag.Types.NUES9, e.getAttribute("Number") );
				} else if( sType.equals("NUES8") ) {
					tag = new AnimalTag( AnimalTag.Types.NUES8, e.getAttribute("Number") );
				} else if( sType.equals("OtherOfficialID") ) {
					tag = new AnimalTag( AnimalTag.Types.OtherOfficialID, e.getAttribute("Type"), e.getAttribute("Number") );
				} else if( sType.equals("ManagementID") ) {
					tag = new AnimalTag( AnimalTag.Types.ManagementID, e.getAttribute("Number") );
				} else if( sType.equals("BrandImage") ) {
					tag = makeBrandImage( e );
				} else if( sType.equals("EquineDescription") ) {
					tag = makeEquineDescription( e );
				} else if( sType.equals("EquinePhotographs") ) {
					tag = makeEquinePhotographs( e );
				} else {
					tag = new AnimalTag( AnimalTag.Types.ManagementID, e.getAttribute("Number") );
				}	
				animalTags.add(tag);
			}
		}
		return animalTags;
	}
	
	private AnimalTag makeBrandImage( Element e ) {
		String ref = e.getAttribute("BrandImageRef"); 
		String description = e.getAttribute("Description");
		BrandImage image = new BrandImage( ref, description );
		return new AnimalTag( image );
	}

	private AnimalTag makeEquineDescription( Element e ) {
		String name = e.getAttribute("Name"); 
		String description = e.getAttribute("Description");
		EquineDescription desc = new EquineDescription( name, description );
		return new AnimalTag( desc );
	}

	private AnimalTag makeEquinePhotographs( Element e ) {
		String view1 = null;
		String view2 = null;
		String view3 = null;
		ArrayList<Element> ePhotos = helper.getChildElements(e);
		int i = 1;
		for( Element ePhoto : ePhotos ) {
			String sView = ePhoto.getAttribute("View");
			if( i == 1 ) view1 = sView;
			else if ( i == 2 ) view2 = sView;
			else if ( i == 3 ) view3 = sView;
			i++;
		}
		EquinePhotographs photos = new EquinePhotographs(view1, view2, view3 );
		return new AnimalTag( photos );
	}

	
	private void addIDNumber(Element animal, AnimalTag tag) {
		String sElementName = tag.getElementName();
		String sAfter = "Test,Vaccination";
		Element eTag = helper.insertElementBefore(animal, sElementName, sAfter);
		helper.setAttribute(eTag, "Number", tag.value);
	}
	
	private void addOtherOfficialID(Element animal, AnimalTag tag) {
		String sElementName = "OtherOfficialID";
		String sAfter = "Test,Vaccination";
		Element eTag = helper.insertElementBefore(animal, sElementName, sAfter);
		helper.setAttribute(eTag, "Type", tag.otherType);
		helper.setAttribute(eTag, "Number", tag.value);
	}
	
	private void addBrandImage(Element animalTags, AnimalTag tag) {
		Element eTag = helper.appendChild(animalTags, "BrandImage");
		helper.setAttribute(eTag, "BrandImageRef", findImageRef(tag));
		helper.setAttribute(eTag, "Description", tag.value);
	}
	
	private void addEquineDescription(Element animalTags, AnimalTag tag) {
		Element eTag = helper.appendChild(animalTags, "EquineDescription");
		helper.setAttribute(eTag, "Name", tag.description.name);
		helper.setAttribute(eTag, "Description", tag.description.description);
	}
	
	private void addEquinePhotographs(Element animalTags, AnimalTag tag) {
		Element eTag = helper.appendChild(animalTags, "EquinePhotographs");
		for( String sView : tag.photographs.views ) {
			helper.setAttribute(eTag, "ImageRef", findImageRef(tag, sView));
			helper.setAttribute(eTag, "View", sView);	
		}
	}

	private String findImageRef(AnimalTag tag) {
		String sRet = "REF1";
		// TODO Implement for real.
		return sRet;
	}
	
	private String findImageRef(AnimalTag tag, String sView) {
		String sRet = "REF2";
		// TODO Implement for real.
		return sRet;
	}
	
	/*
	 * TODO Deal with ANY changes to an existing Animal or Group.
	public void updateSpecies( String sPreviousSpecies, String sNewSpecies ) {
		if( !isValidDoc() ) 
			return;
		ArrayList<Element> eAnimals = XMLUtility.listChildElementsByName(root, "Animal");
		for( Element eAnimal : eAnimals) {
			String sExistingCode = eAnimal.getAttribute("SpeciesCode");
			if( sExistingCode != null && sExistingCode.equals(sPreviousSpecies) ) {
				eAnimal.helper.setAttribute("SpeciesCode", sNewSpecies);
			}
		}
		ArrayList<Element> eGroups = XMLUtility.listChildElementsByName(root, "GroupLot");
		for( Element eGroup : eGroups) {
			String sExistingCode = eGroup.getAttribute("SpeciesCode");
			if( sExistingCode != null && sExistingCode.equals(sPreviousSpecies) ) {
				eGroup.helper.setAttribute("SpeciesCode", sNewSpecies);
			}
		}
		return;
	}
*/
	public void clearGroupLots() {
		ArrayList<Element> eAnimals = helper.getElementsByName("Animal");
		for( Element eAnimal : eAnimals) {
			helper.removeElement(eAnimal);
		}
	}
	
	public Element addGroupLot( GroupLot group ) {
		if( !isValidDoc() ) 
			return null;
		String sAfter = getFollowingElementList("Statements");
		Element groupLot = helper.insertElementBefore("GroupLot", sAfter);
		if( group.speciesCode.isStandardCode ) {
			Element speciesCode = helper.appendChild(groupLot, "SpeciesCode");
			helper.setAttribute(speciesCode, "Code", group.speciesCode.code);
			helper.setAttribute(speciesCode, "Text", group.speciesCode.text);
		}
		else {
			Element speciesOther = helper.appendChild(groupLot, "SpeciesOther");
			helper.setAttribute(speciesOther, "Code", group.speciesCode.code);
			helper.setAttribute(speciesOther, "Text", group.speciesCode.text);
		}
		sAfter = "Test, Vaccination";
		// ID is an Element because it can repeat.  I only implement one.
		String sId = group.groupLotId;
		if( sId != null && sId.trim().length() > 0 ) {
			Element groupId = helper.insertElementBefore(groupLot, "GroupLotID", sAfter);
			groupId.setTextContent(sId);
		}
		Double quantity = group.quantity;
		if( quantity != null )
			helper.setAttribute(groupLot, "Quantity", quantity.toString());
		String sUnit = group.unit;
		if( sUnit != null && sUnit.trim().length() > 0 )
			helper.setAttribute(groupLot, "Unit", sUnit);
		String sAge = group.age;
		if( sAge != null && sAge.trim().length() > 0 )
			helper.setAttribute(groupLot, "Age", sAge);
		String sBreed = group.breed;
		if( sBreed != null && sBreed.trim().length() > 0 )
			helper.setAttribute(groupLot, "Breed", sBreed);
		String sSex = group.sex;
		if( sSex != null && sSex.trim().length() > 0 )
			helper.setAttribute(groupLot, "Sex", sSex);
		String sDescription = group.description;
		if( sDescription != null && sDescription.trim().length() > 0 )
			helper.setAttribute(groupLot, "Description", sDescription);
		return groupLot;
	}
	
	public boolean hasGroup( String sSpecies ) {
		boolean bRet = false;
		if( isValidDoc() && sSpecies != null && sSpecies.trim().length() > 0 ) {
			ArrayList<Element> groups = helper.getElementsByName("GroupLot");
			for( Element group : groups ) {
					String sSpeciesCode = group.getAttribute("SpeciesCode");
					if( sSpecies.equalsIgnoreCase(sSpeciesCode) ) {
						bRet = true;
						break;
					}
			}
		}
		return bRet;
	}
	
	public void removeGroupLot( GroupLot group ) {
		helper.removeElement(group.eGroupLot);
	}
	
	public void addOrEditGroupLot( GroupLot group ) {
		if( !isValidDoc() ) 
			return;
		String sAfter = getFollowingElementList("Statements");
		Element eGroupLot = group.eGroupLot;
		if( eGroupLot == null ) {
			eGroupLot = helper.insertElementBefore("GroupLot", sAfter);
		}
		if( group.speciesCode.isStandardCode ) {
			Element speciesCode = helper.getChildElementByName(eGroupLot, "SpeciesCode");
			if( speciesCode == null ) {
				speciesCode = helper.appendChild(eGroupLot, "SpeciesCode");
				Element speciesOther = helper.getChildElementByName(eGroupLot, "SpeciesOther");
				if( speciesOther != null)
					helper.removeElement(speciesOther);
			}
			helper.setAttribute(speciesCode, "Code", group.speciesCode.code);
			helper.setAttribute(speciesCode, "Text",  group.speciesCode.text);
		}
		else {
			Element speciesOther = helper.getChildElementByName(eGroupLot, "SpeciesOther");
			if( speciesOther == null ) {
				speciesOther = helper.appendChild(eGroupLot, "SpeciesOther");
				Element speciesCode = helper.getChildElementByName(eGroupLot, "SpeciesCode");
				if( speciesCode != null)
					helper.removeElement(speciesCode);
			}
			helper.setAttribute(speciesOther, "Code", group.speciesCode.code);
			helper.setAttribute(speciesOther, "Text", group.speciesCode.text);
		}
		sAfter = "Test, Vaccination";
		// ID is an Element because it can repeat.  I only implement one.
		String sId = group.groupLotId;
		if( sId != null && sId.trim().length() > 0 ) {
			Element groupId = helper.insertElementBefore(eGroupLot, "GroupLotID", sAfter);
			groupId.setTextContent(sId);
		}
		Double quantity = group.quantity;
		if( quantity != null ) {
			// Civet only deals with animals so no fraction part.  "200.0" would be confusing.
			String sQuant = String.format("%1$.0f", quantity); 
			helper.setAttribute(eGroupLot, "Quantity", sQuant);
		}
		String sUnit = group.unit;
		if( sUnit != null && sUnit.trim().length() > 0 )
			helper.setAttribute(eGroupLot, "Unit", sUnit);
		String sAge = group.age;
		if( sAge != null && sAge.trim().length() > 0 )
			helper.setAttribute(eGroupLot, "Age", sAge);
		String sBreed = group.breed;
		if( sBreed != null && sBreed.trim().length() > 0 )
			helper.setAttribute(eGroupLot, "Breed", sBreed);
		String sSex = group.sex;
		if( sSex != null && sSex.trim().length() > 0 )
			helper.setAttribute(eGroupLot, "Sex", sSex);
		String sDescription = group.description;
		if( sDescription != null && sDescription.trim().length() > 0 )
			helper.setAttribute(eGroupLot, "Description", sDescription);
		return;
	}

	public void setOrUpdatePDFAttachment( byte[] pdfBytes, String sFileName ) {
		sFileName = FileUtils.replaceInvalidFileNameChars(sFileName);
		if( isValidDoc() && pdfBytes != null && pdfBytes.length > 0 ) {
			binaries.setOrUpdatePDFAttachment(pdfBytes, sFileName);
		}
	}
	
	public void removePDFAttachment() {
		binaries.removePDFAttachment();
	}
//	
//	private void setPDFAttachment( byte[] pdfBytes, String sFileName ) {
//		sFileName = FileUtils.replaceInvalidFileNameChars(sFileName);
//		if( isValidDoc() && pdfBytes != null && pdfBytes.length > 0 ) {
//			binaries.setPDFAttachment(pdfBytes, sFileName);
//		}
//	}
	
	public byte[] getPDFAttachmentBytes() {
		return binaries.getPDFAttachmentBytes();
	}
	
	public boolean hasPDFAttachment() {
		return binaries.hasPDFAttachment();
	}
	
	public String getPDFAttachmentFilename() {
		return binaries.getPDFAttachmentFilename();
	}
	
	public void addOrUpdateMetadataAttachment( CviMetaDataXml metaData ) {
		binaries.addOrUpdateMetadata(metaData);
	}
	
	/**
	 * Be sure to call from save().  Was done here in v4.
	 */
	public void checkExpiration() {
		String sExp = helper.getAttributeByPath("/eCVI", "ExpirationDate");
		java.util.Date dExp = null;
		int iValidDays = CivetConfig.getCviValidDays();
		Calendar cal = Calendar.getInstance();
		boolean bSet = false;
		try {
			dExp = getDateFormat().parse(sExp);
			return;  // Already have valid expDate
		} catch (ParseException e) {
			dExp = null;
		}
		ArrayList<String> aInspDates = helper.listAttributesByPath( "Animal", "InspectionDate" );
		for( String sInsp : aInspDates ) {
			if( sInsp != null && sInsp.trim().length() > 0 ) {
				java.util.Date dInsp = null;
				try {
					dInsp = getDateFormat().parse(sInsp);
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
			String sNewExp = getDateFormat().format(dExp);
			helper.setAttributeByPath("/eCVI", "ExpirationDate", sNewExp);
		}
	}
	
	public void checkQuantity() {
		ArrayList<Element> aGroups = helper.getElementsByName("GroupLot");
		for( Element eGroup : aGroups ) {
			String sQuantity = eGroup.getAttribute("Quantity");
			if( sQuantity.indexOf(',') >= 0 ) {
				sQuantity = sQuantity.replace(",","");
				eGroup.setAttribute("Quantity", sQuantity);
			}
		}
	}
	
	public String checkAnimalIDTypes() {
		String sRet = null;
		
		// For each Animal 
		ArrayList<Animal> aAnimals = getAnimals();
		for( Animal animal : aAnimals ) {
			ArrayList<AnimalTag> aTags = animal.animalTags;
			for( AnimalTag tag : aTags ) {
				switch( tag.type ) {
				case AIN:
				case MfrRFID:
					if( !AnimalIDUtils.isValid(tag.value, tag.getElementName())) {
						if( sRet == null ) sRet = tag.value;
						else sRet = sRet + ", " + tag.value;
						String[] tagRet = AnimalIDUtils.getIDandType(tag.value);
						if( tagRet[1] != null && (tagRet[1].startsWith("Short") || tagRet[1].startsWith("Long") ) )
							fixAnimalTag( animal, tag );
					}
					break;
				case NUES9:
				case NUES8:
					if( !AnimalIDUtils.isValid(tag.value, tag.getElementName())) {
						fixAnimalTag( animal, tag );
						if( sRet == null ) sRet = tag.value;
						else sRet = sRet + ", " + tag.value;
					}
					break;
				default:
					break;  // The other types either don't have regexes or are too complicated to fix
				}
			}
		}
		return sRet;
	}
	
	private void fixAnimalTag( Animal animal, AnimalTag tag ) {
		Element eAnimal = animal.eAnimal;
		Element eAnimalTags = helper.getChildElementByName(eAnimal, "AnimalTags");
		helper.removeChild(eAnimalTags, tag.getElementName());
		Element eNewTag = helper.appendChild(eAnimalTags, "OtherOfficialID");
		eNewTag.setAttribute("Type", "OTHER");
		if( tag.value == null || tag.value.trim().length() == 0 ) 
			tag.value = "BLANK";
		eNewTag.setAttribute("Number", tag.value);
	}
	
	/**
	 * If we have included "Lids"--really USAHERDS local Identifiers--remove before sending to another state.
	 * This will be a critical issue if we need to function with LID states and start getting them on paper
	 * or in CO/KS eCvi or IIAD iCvi form.
	 */
	public void purgeLids() {
		String sPin = null;
		String sPath = null;
		// Destination
		sPath = "Destination/PremId";
		sPin = helper.getElementTextByPath(sPath);
		if( sPin != null && sPin.trim().length() != 7 ) {
			helper.removeElementByPath(sPath);
		}
		// Origin	
		sPath = "Origin/PremId";
		sPin = helper.getElementTextByPath(sPath);
		if( sPin != null && sPin.trim().length() != 7 ) {
			helper.removeElementByPath(sPath);
		}
	}
	
	public String getCertificateNumber() {
		String sRet = null;
		String sPath = ".";
		String sAttr = "CviNumber";
		sRet = helper.getAttributeByPath(sPath,sAttr);
		return sRet;
	}
	
	public String getCertificateNumberSource() {
		String sRet = null;
		String sPath = ".";
		String sAttr = "CviNumberIssuedBy";
		sRet = helper.getAttributeByPath(sPath,sAttr);
		return sRet;
	}
	
	public void setCertificateNumber(String sCertificateNbr) {
		String sPath = ".";
		String sAttr = "CviNumber";
		helper.setAttributeByPath(sPath, sAttr, sCertificateNbr);
		CviMetaDataXml meta = getMetaData();
		meta.setCertificateNbr(sCertificateNbr);
		binaries.addOrUpdateMetadata(meta);
	}
	
	public java.util.Date getIssueDate() {
		java.util.Date dRet = null;
		String sPath = ".";
		String sAttr = "IssueDate";
		String sDate = helper.getAttributeByPath(sPath,sAttr);
		if( sDate != null && sDate.trim().length() > 0 )
			dRet = XMLUtility.xmlDateToDate(sDate);
		return dRet;
	}

	// The following are convenience methods to pull elements from the metadata attachment.
	
	public CviMetaDataXml getMetaData() {
		if( metaData == null ) {
			metaData = binaries.getMetaData();
			if( metaData == null ) {
				metaData = new CviMetaDataXml();
				binaries.addOrUpdateMetadata(metaData);
			}
		}
		return metaData;
	}
	
	public void setCertificateNumberSource(String sCVINbrSource) {
		String sPath = ".";
		String sAttr = "CviNumberIssuedBy";
		String sExistingSource = helper.getAttributeByPath(sPath, sAttr);
		if( sExistingSource == null || sExistingSource.trim().length() == 0 )
			helper.setAttributeByPath(sPath, sAttr, sCVINbrSource);
	}
	
	public java.util.Date getBureauReceiptDate() {
		java.util.Date dRet = null;
		CviMetaDataXml meta = getMetaData();
		if( meta != null )
			dRet = meta.getBureauReceiptDate();
		return dRet;
	}
	
	public void setBureauReceiptDate( java.util.Date dReceived ) {
		CviMetaDataXml meta = getMetaData();
		meta.setBureauReceiptDate(dReceived);
		binaries.addOrUpdateMetadata(meta);
	}
	
	public String getOriginStateCode() {
		String sRet = null;
		Premises pOrigin = getOrigin();
		if( pOrigin != null ) {
			Address addrOrigin = pOrigin.address;
			if( addrOrigin != null && addrOrigin.state != null ) {
				sRet = addrOrigin.state;
			}
		}
		return sRet;
	}
	
	public String getDestinationStateCode() {
		String sRet = null;
		Premises pDest = getDestination();
		if( pDest != null ) {
			Address addrDest = pDest.address;
			if( addrDest != null && addrDest.state != null ) {
				sRet = addrDest.state;
			}
		}
		return sRet;
	}
	
	public boolean isExport() {
		boolean bRet = false;
		Premises pDest = getDestination();
		if( pDest != null ) {
			Address addrDest = pDest.address;
			if( addrDest != null ) {
				String sHomeState = CivetConfig.getHomeStateAbbr();
				String sDestState = addrDest.state;
				if( sDestState != null && !sDestState.equalsIgnoreCase(sHomeState) ) 
					bRet = true;
			}
		}
		return bRet;
	}
	
	public boolean hasErrors() {
		boolean bRet = false;
		CviMetaDataXml meta = getMetaData();
		if( meta != null )
			return meta.hasErrors();
		return bRet;
	}
	
	public String getErrorsString() {
		String sRet = null;
		CviMetaDataXml meta = getMetaData();
		if( meta != null )
			return meta.getErrorsString();
		return sRet;
	}

	public ArrayList<String> listErrors() {
		CviMetaDataXml meta = getMetaData();
		if( meta != null )
			return meta.listErrors();
		return null;
	}
	
	public String getXMLString() {
		return helper.getXMLString();
	}
	
	public byte[] getXMLBytes() {
		return helper.getXMLBytes();
	}
	
	public byte[] getNoAttachmentXmlBytes() {
		byte[] aRet = null;
		byte[] pdfBytes = getPDFAttachmentBytes();
		String sPdfFileName = getPDFAttachmentFilename();
		removePDFAttachment();
		String sRet = helper.getXMLString();
		aRet = sRet.getBytes();
		setOrUpdatePDFAttachment(pdfBytes, sPdfFileName);
		return aRet;
	}
	

}
