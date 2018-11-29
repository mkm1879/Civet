package edu.clemson.lph.civet.xml;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringBufferInputStream;
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

import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.xml.elements.*;
import edu.clemson.lph.civet.xml.elements.AnimalTag.Types;
import edu.clemson.lph.controls.PhoneField;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.utils.FileUtils;
import edu.clemson.lph.utils.IDTypeGuesser;
import edu.clemson.lph.utils.XMLUtility;

// TODO Refactor rename to StdXmlDataModel
public class StdeCviXmlBuilder {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final String allElements = "Veterinarian,MovementPurpose,Origin,Destination,Consignor,Consignee," +
	                                   "Carrier,TransportMode,Accessions,Animal,GroupLot,Statements,Attachment," +
			                           "MiscAttribute,Binary";
	private XMLDocHelper helper;
	private StdeCviBinaries binaries;
	
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
	
	public static void main( String args[] ) {
	     PropertyConfigurator.configure("CivetConfig.txt");
	     logger.setLevel(Level.INFO);
		String sXML;
		try {
			sXML = FileUtils.readTextFile(new java.io.File( "./std2samples/sample2.xml") );
			StdeCviXmlBuilder builder = new StdeCviXmlBuilder( sXML );
			System.out.println(builder.getCertificateNumber());
			System.out.println(builder.getIssueDate());
			System.out.println("Vet");
			Veterinarian vet = builder.getVet();
			System.out.println(vet.nationalAccreditationNumber);
			System.out.println(vet.person.name);
			System.out.println(builder.getPurpose());
			Premises origin = builder.getOrigin();
			System.out.println("Origin");
			System.out.println(origin.address.line1);
			System.out.println(origin.address.line2);
			System.out.println(origin.address.zip);
			System.out.println(origin.address.county);
			System.out.println("Consignor");
			Contact consignor = builder.getConsignor();
			System.out.println(consignor.addressBlock);
			System.out.println(consignor.person.phone);
			System.out.println("Animals: ");
			ArrayList<Animal> aAnimals = builder.getAnimals();
			int i = 1;
			for( Animal a : aAnimals ) {
				System.out.println(a.speciesCode.code);
				for( AnimalTag t : a.animalTags ) {
					System.out.println(t.value);
					System.out.println(t.type.toString());
					if( i++ == 3 ) {
						a.speciesCode.text = "A Horse of Course";
						builder.editAnimal(a);
					}
				}
			}
			System.out.println("Groups: ");
			ArrayList<GroupLot> aGroups = builder.getGroups();
			for( GroupLot g : aGroups ) {
				System.out.println(g.description);
				System.out.println(g.speciesCode.code);
				System.out.println(g.age);
				g.speciesCode.text = null;
				builder.editGroupLot(g);
			}
			CviMetaDataXml meta = new CviMetaDataXml();
			meta.addError("NID");
			meta.setBureauReceiptDate(new java.util.Date());
			meta.setErrorNote("This is quite an error");
			builder.addOrUpdateMetadataAttachement(meta);
			FileUtils.writeTextFile(builder.getXMLString(), "TestBuilder.xml");

			byte pdfBytes[] = builder.getPDFAttachmentBytes();
			if( pdfBytes != null )
				FileUtils.writeBinaryFile(pdfBytes, builder.getPDFAttachmentFilename());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Create an empty XML document.
	 */
	public StdeCviXmlBuilder() {
		try {
			DocumentBuilder db = SafeDocBuilder.getSafeDocBuilder(); //DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.newDocument();
			doc.setXmlStandalone(true);
			Element root = doc.createElementNS("http://www.usaha.org/xmlns/ecvi2", "eCVI");
			doc.appendChild(root);
			helper = new XMLDocHelper( doc, root );
			binaries = new StdeCviBinaries( helper );
		} catch (Exception e ) {
			logger.error(e);
		}
	}

	/**
	 * Create an XML document from an existing wrapper.  Will this ever apply.
	 * @param cviIn
	 */
	public StdeCviXmlBuilder(StdeCviXml cviIn) {
		try {
			Document doc = null;
			Element root = null;
			if( cviIn != null ) {
				doc = cviIn.getDocument();
				doc.setXmlStandalone(true);
				root = cviIn.getRoot();
			}
			if( doc == null ) {
				DocumentBuilder db = SafeDocBuilder.getSafeDocBuilder(); //DocumentBuilderFactory.newInstance().newDocumentBuilder();
				doc = db.newDocument();
				doc.setXmlStandalone(true);
				root = doc.createElementNS("http://www.usaha.org/xmlns/ecvi2", "eCVI");
				doc.appendChild(root);
			}
			helper = new XMLDocHelper( doc, root );
			binaries = new StdeCviBinaries( helper );
		} catch (Exception e ) {
			logger.error(e);
		}
	}

	/** 
	 * Create an XML document from the raw DOM object.  
	 * @param doc
	 */
	public StdeCviXmlBuilder( Document doc ) {
		doc.setXmlStandalone(true);
		Element root = doc.getDocumentElement();
		helper = new XMLDocHelper( doc, root );
		binaries = new StdeCviBinaries( helper );
	}

	/** 
	 * Create an XML document from the raw XML string.  
	 * @param doc
	 */
	public StdeCviXmlBuilder( String sXML ) {
		try {
			Document doc = null;
			Element root = null;
			if( sXML != null ) {
				DocumentBuilder db = SafeDocBuilder.getSafeDocBuilder(); //DocumentBuilderFactory.newInstance().newDocumentBuilder();
				byte[] xmlBytes = sXML.getBytes();
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

	public void setCviNumber( String sCVINo ) {
		helper.setAttribute(helper.getRootElement(), "CviNumber", sCVINo);
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
			String sDate = dateFormat.format(dIssued);
			int iValidDays = CivetConfig.getCviValidDays();
			if( sDate !=  null && iValidDays > 0 ) {
				helper.setAttribute(helper.getRootElement(), "IssueDate", sDate);		
				Calendar cal = Calendar.getInstance();
				cal.setTime(dIssued);
				cal.add(Calendar.DATE, iValidDays);
				String sExp = dateFormat.format(cal.getTime());
				if( sExp != null && sExp.trim().length() > 0 )
					helper.setAttribute(helper.getRootElement(), "ExpirationDate", sExp);
			}
		}
	}
	
	public Element setVet( Veterinarian vet ) {
		Element eVet = null;
		if( !isValidDoc() )
			return null;
		if( vet.person.name != null && vet.person.name.trim().length() > 0 ) {
			String sAfter = getFollowingElementList("MovementPurposes");
			eVet = helper.getOrInsertElementBefore("Veterinarian", sAfter);
			if( vet.licenseNumber != null && vet.licenseNumber.trim().length() > 0 )
				helper.setAttribute(eVet, "LicenseNumber", vet.licenseNumber);
			if( vet.nationalAccreditationNumber  != null && vet.nationalAccreditationNumber .trim().length() > 0 )
				helper.setAttribute(eVet, "NationalAccreditationNumber", vet.nationalAccreditationNumber );
			Element person = helper.getOrAppendChild(eVet,"Person");
			Element name = helper.getOrAppendChild(person, "Name");
			name.setTextContent(vet.person.name);
			if( vet.person.phone != null && vet.person.phone.trim().length() > 0 ) {
				Element phone = helper.getOrAppendChild(person, "Phone");
				helper.setAttribute(phone, "Type", "Unknown");
				helper.setAttribute(phone, "Number", vet.person.phone);
			}	
		}
		return eVet;
	}
	
	public Veterinarian getVet() {
		Veterinarian vet = null;
		Element eVet = helper.getElementByName("Veterinarian");
		if( eVet != null ) {
			String sAddress = null;
			Element eAddressBlock = helper.getChildElementByName(eVet, "AddressBlock");
			if( eAddressBlock != null ) {
				sAddress = eAddressBlock.getTextContent();
			}
			String sLicenseState = eVet.getAttribute("LicenseState");
			String sLicenseNumber = eVet.getAttribute("LicenseNumber");
			String sNationalAccreditationNumber = eVet.getAttribute("NationalAccreditationNumber");
			Person vetPerson = getPerson(eVet);
			vet = new Veterinarian( vetPerson, sAddress, 
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
			address = helper.getOrAppendChild(e, "Adddress" );
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
			if( addr.zip != null ) {
				sAfter = "Country,GeoPoint";
				Element zip = helper.getOrInsertElementBefore( address, "ZIP", sAfter);
				address.appendChild(zip);
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
		String sPath = "//MovementPurposes/MovementPurpose";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}

	private Element setPremises( Premises prem, String sElementName ) {
		if( !isValidDoc() )
			return null;
		String sAfter = ("Origin".equals(sElementName)?getFollowingElementList("Destination"):getFollowingElementList("Consignor") );
		Element premises = helper.getOrInsertElementBefore(sElementName, sAfter);
		if( prem.premid  != null && prem.premid.trim().length() > 0 ) {
			sAfter = "PremName,Address,StateZoneOrAreaStatus,HerdOrFlockStatus,Person";
			Element pin = helper.getOrInsertElementBefore(premises, "PremId", sAfter);
			premises.appendChild(pin);
			pin.setTextContent(prem.premid);
		}
		if( prem.premName != null && prem.premName.trim().length() > 0 ) {
			sAfter = "Address,StateZoneOrAreaStatus,HerdOrFlockStatus,Person";

			Element premName = helper.getOrInsertElementBefore(premises,"PremName",sAfter);
			premises.appendChild(premName);
			premName.setTextContent(prem.premName);
		}
		setAddress( premises, prem.address );
		Element person = helper.getOrAppendChild(premises,"PremName");
		sAfter = "Phone,Email";
		// Careful of choice between name and name parts
		if( prem.personName != null && prem.personName.trim().length() > 0 ) {
			helper.removeChild(person, "NameParts");
			sAfter = "Phone,Email";
			Element name = helper.getOrInsertElementBefore(person,"Name",sAfter);
			name.setTextContent(prem.personName);
		}
		else if( prem.personNameParts != null ) {
			helper.removeChild(person, "Name");  // If we have parts DO NOT include single name
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
		if( prem.personPhone != null && checkPhoneLength(prem.personPhone) ) {
			sAfter = "Email";
			Element phone = helper.getOrInsertElementBefore(premises,"Phone",sAfter);
			person.appendChild(phone);
			helper.setAttribute(phone, "Type", "Unknown");
			helper.setAttribute(phone, "Number", prem.personPhone);
		}	
		return premises;
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
		return prem;
	}
	
	private Address getAddress( Element ePremises ) {
		String line1 = null;
		Element eLine1 = helper.getChildElementByName(ePremises, "Line1");
		if( eLine1 != null)
			line1 = eLine1.getTextContent();
		String line2 = null;
		Element eLine2 = helper.getChildElementByName(ePremises, "Line2");
		if( eLine2 != null )
			line2 = eLine2.getTextContent();
		String town = null;
		Element eTown = helper.getChildElementByName(ePremises, "Town");
		if( eTown != null )
			town = eTown.getTextContent();
		String county = null;
		Element eCounty = helper.getChildElementByName(ePremises, "County");
		if( eCounty != null )
			county = eCounty.getTextContent();
		String state = null;
		Element eState = helper.getChildElementByName(ePremises, "State");
		if( eState != null )
			state = eState.getTextContent();
		String zip = null;
		Element eZIP = helper.getChildElementByName(ePremises, "ZIP");
		if( eZIP != null )
			zip = eZIP.getTextContent();
		String country = null;
		Element eCountry = helper.getChildElementByName(ePremises, "Country");
		if( eCountry != null )
			country = eCountry.getTextContent();
		Double latitude = null;
		Double longitude = null;
		Element eGeoPoint = helper.getChildElementByName(ePremises, "GeoPoint");
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
	

	public Element setOrigin( Premises premises ) {
		return setPremises( premises, "Origin");
	}
	
	public Premises getOrigin() {
		return getPremises("Origin");
	}

	public Element setDestination( Premises premises ) {
		return setPremises( premises, "Destination");
	}
	
	public Premises getDestination() {
		return getPremises("Destination");
	}
	
	private Element setContact( Contact cont, String sElementName ) {
		if( !isValidDoc() )
			return null;
		String sAfter = ("Consignee".equals(sElementName)?getFollowingElementList("Carrier"):getFollowingElementList("Consignee") );
		Element eContact = helper.getOrInsertElementBefore(sElementName, sAfter);
		if( cont.addressBlock  != null && cont.addressBlock.trim().length() > 0 ) {
			sAfter = "Person";
			Element pin = helper.getOrInsertElementBefore(eContact, "AddressBlock", sAfter);
			eContact.appendChild(pin);
			pin.setTextContent(cont.addressBlock);
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
			String sAddressBlock = null;
			Element eAddressBlock = helper.getChildElementByName(eContact, "AddressBlock");
			if( eAddressBlock != null ) {
				sAddressBlock = eAddressBlock.getTextContent();
			}
			Person person = getPerson( eContact );
			contact = new Contact( sAddressBlock, person );
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
				NameParts nameParts = new NameParts( sBusinessName, sOtherName, sMiddleName, sLastName, sOtherName);
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
		for( AnimalTag tag : animalData.animalTags ) {
			switch( tag.type ) {
			case AIN:
			case MfrRFID:
			case NUES9:
			case NUES8:
			case OtherOfficialID:
			case ManagementID:
				addIDNumber(eAnimal, tag);
				break; 
			case BrandImage:
				addBrandImage(eAnimal, tag);
				break; 
			case EquineDescription:
				addEquineDescription(eAnimal, tag);
				break; 
			case EquinePhotographs:
				addEquinePhotographs(eAnimal, tag);
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
		helper.setAttribute(eAnimal, "InspectionDate", animalData.inspectionDate);
		return eAnimal;
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
		for( AnimalTag tag : animalData.animalTags ) {
			switch( tag.type ) {
			case AIN:
			case MfrRFID:
			case NUES9:
			case NUES8:
			case OtherOfficialID:
			case ManagementID:
				addIDNumber(eAnimal, tag);
				break; 
			case BrandImage:
				addBrandImage(eAnimal, tag);
				break; 
			case EquineDescription:
				addEquineDescription(eAnimal, tag);
				break; 
			case EquinePhotographs:
				addEquinePhotographs(eAnimal, tag);
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
		helper.setAttribute(eAnimal, "InspectionDate", animalData.inspectionDate);
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
				Double quantity = Double.parseDouble(sQuantity);
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
	
	private void addBrandImage(Element animal, AnimalTag tag) {
		String sAfter = "Test,Vaccination";
		Element eTag = helper.insertElementBefore(animal, "BrandImage", sAfter);
		helper.setAttribute(eTag, "BrandImageRef", findImageRef(tag));
		helper.setAttribute(eTag, "Description", tag.value);
	}
	
	private void addEquineDescription(Element animal, AnimalTag tag) {
		String sAfter = "Test,Vaccination";
		Element eTag = helper.insertElementBefore(animal, "EquineDescription", sAfter);
		helper.setAttribute(eTag, "Name", tag.description.name);
		helper.setAttribute(eTag, "Description", tag.description.description);
	}
	
	private void addEquinePhotographs(Element animal, AnimalTag tag) {
		String sAfter = "Test,Vaccination";
		Element eTag = helper.insertElementBefore(animal, "EquinePhotographs", sAfter);
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
	
	public void editGroupLot( GroupLot group ) {
		if( !isValidDoc() ) 
			return;
		String sAfter = getFollowingElementList("Statements");
		Element eGroupLot = group.eGroupLot;
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
		if( quantity != null )
			helper.setAttribute(eGroupLot, "Quantity", quantity.toString());
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
	
	public void addPDFAttachement( byte[] pdfBytes, String sFileName ) {
		if( isValidDoc() && pdfBytes != null && pdfBytes.length > 0 && !attachmentExists(sFileName)) {
			binaries.addPDFAttachment(pdfBytes, sFileName);
		}
	}
	
	public byte[] getPDFAttachmentBytes() {
		return binaries.getPDFAttachmentBytes();
	}
	
	public String getPDFAttachmentFilename() {
		return binaries.getPDFAttachmentFilename();
	}
	
	/**
	 * Just make this logic a little clearer
	 * @param sFileName
	 * @return
	 */
	private boolean attachmentExists( String sFileName ) {
		boolean bRet = false;
		Element eAttach = binaries.getAttachment(sFileName);
		bRet = ( eAttach != null );
		return bRet;
	}
	
	public void addOrUpdateMetadataAttachement( CviMetaDataXml metaData ) {
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
			dExp = dateFormat.parse(sExp);
		} catch (ParseException e) {
			dExp = null;
		}
		ArrayList<String> aInspDates = helper.listAttributesByPath( "/eCVI/Animal", "InspectionDate" );
		for( String sInsp : aInspDates ) {
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
			helper.setAttributeByPath("/eCVI", "ExpirationDate", sNewExp);
		}
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
		sPath = "/eCVI/Destination/PremId";
		sPin = helper.getElementTextByPath(sPath);
		if( sPin != null && sPin.trim().length() != 7 ) {
			helper.removeElementByPath(sPath);
		}
		// Origin	
		sPath = "/eCVI/Origin/PremId";
		sPin = helper.getElementTextByPath(sPath);
		if( sPin != null && sPin.trim().length() != 7 ) {
			helper.removeElementByPath(sPath);
		}
	}
	
	public String getCertificateNumber() {
		String sRet = null;
		String sPath = "/eCVI";
		String sAttr = "CviNumber";
		sRet = helper.getAttributeByPath(sPath,sAttr);
		return sRet;
	}
	
	public java.util.Date getIssueDate() {
		java.util.Date dRet = null;
		String sPath = "/eCVI";
		String sAttr = "IssueDate";
		String sDate = helper.getAttributeByPath(sPath,sAttr);
		dRet = XMLUtility.xmlDateToDate(sDate);
		return dRet;
	}
	
	public CviMetaDataXml getMetaData() {
		CviMetaDataXml mRet = binaries.getMetaData();
		return mRet;
	}
	
	// The following are convenience methods to pull elements from the metadata attachment.
	public java.util.Date getBureauReceiptDate() {
		java.util.Date dRet = null;
		CviMetaDataXml meta = getMetaData();
		if( meta != null )
			return meta.getBureauReceiptDate();
		return dRet;
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
	

}
