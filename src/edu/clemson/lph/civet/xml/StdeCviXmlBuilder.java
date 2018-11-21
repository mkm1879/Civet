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

import javax.swing.JDialog;
import javax.swing.SwingUtilities;
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
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.xml.elements.*;
import edu.clemson.lph.civet.xml.elements.AnimalTag.Types;
import edu.clemson.lph.controls.PhoneField;
import edu.clemson.lph.dialogs.MessageDialog;
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

	public StdeCviXmlBuilder( Document doc ) {
		doc.setXmlStandalone(true);
		Element root = doc.getDocumentElement();
		helper = new XMLDocHelper( doc, root );
		binaries = new StdeCviBinaries( helper );
	}

	public void setCviNumber( String sCVINo ) {
		helper.setAttribute(null, "CviNumber", sCVINo);
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
				helper.setAttribute(null, "IssueDate", sDate);		
				Calendar cal = Calendar.getInstance();
				cal.setTime(dIssued);
				cal.add(Calendar.DATE, iValidDays);
				String sExp = dateFormat.format(cal.getTime());
				if( sExp != null && sExp.trim().length() > 0 )
					helper.setAttribute(null, "ExpirationDate", sExp);
			}
		}
	}
	
	static String getAfter(String sElement) {
		String sRet = sElement;
		int iIndex = allElements.indexOf(sRet);
		if( iIndex > 0 ) 
			sRet = allElements.substring(iIndex);
		return sRet;
	}
	
	public Element setVet( Veterinarian vet ) {
		Element eVet = null;
		if( !isValidDoc() )
			return null;
		if( vet.personName != null && vet.personName.trim().length() > 0 ) {
			String sAfter = getAfter("MovementPurposes");
			eVet = helper.getOrInsertElementBefore("Veterinarian", sAfter);
			if( vet.licenseNumber != null && vet.licenseNumber.trim().length() > 0 )
				eVet.setAttribute("LicenseNumber", vet.licenseNumber);
			if( vet.nationalAccreditationNumber  != null && vet.nationalAccreditationNumber .trim().length() > 0 )
				eVet.setAttribute("NationalAccreditationNumber", vet.nationalAccreditationNumber );
			Element person = helper.getOrAppendChild(eVet,"Person");
			Element name = helper.getOrAppendChild(person, "Name");
			name.setTextContent(vet.personName);
			if( vet.personPhone != null && vet.personPhone.trim().length() > 0 ) {
				Element phone = helper.getOrAppendChild(person, "Phone");
				phone.setAttribute("Type", "Unknown");
				phone.setAttribute("Number", vet.personPhone);
			}	
		}
		return eVet;
	}
	
	public Veterinarian getVet() {
		Veterinarian vet = null;
		Element eVet = helper.getElementByName("Veterinarian");
		String sLicenseState = eVet.getAttribute("LicenseState");
		String sLicenseNumber = eVet.getAttribute("LicenseNumber");
		String sNationalAccreditationNumber = eVet.getAttribute("NationalAccreditationNumber");
		Element eVetPerson = helper.getChildElementByName(eVet, "Person");
		Element eVetPhone = helper.getChildElementByName(eVetPerson, "Phone");
		String sPhone = eVetPhone.getAttribute("Number");
		Element eVetEmail = helper.getChildElementByName(eVetPerson, "Email");
		String sEmail = eVetEmail.getAttribute("Email");
		Element eAddress = helper.getChildElementByName(eVet, "AddressBlock");
		String sAddress = eAddress.getTextContent();
		Element eVetName = helper.getChildElementByName(eVetPerson, "Name");
		String sName = null;
		if( eVetName != null ) {
			sName = eVetName.getTextContent();
			vet = new Veterinarian(sName, sPhone,  sEmail,  sAddress, 
					sLicenseState,  sLicenseNumber,  sNationalAccreditationNumber);
		}
		else {
			Element eVetNameParts = helper.getChildElementByName(eVetPerson, "NameParts");
			if( eVetNameParts != null ) {
				String sBusinessName = eVetNameParts.getAttribute("BusinessName");
				String sFirstName = eVetNameParts.getAttribute("FirstName");
				String sMiddleName = eVetNameParts.getAttribute("MiddleName");
				String sLastName = eVetNameParts.getAttribute("LastName");
				String sOtherName = eVetNameParts.getAttribute("OtherName");
				NameParts parts = new NameParts( sBusinessName, sFirstName, sMiddleName, sLastName, sOtherName );
				vet = new Veterinarian( parts, sPhone, sEmail, sAddress, 
					      sLicenseState, sLicenseNumber, sNationalAccreditationNumber);
			}
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
			String sAfter = getAfter("Origin");
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
		String sAfter = ("Origin".equals(sElementName)?getAfter("Destination"):getAfter("Consignor") );
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
				sAfter = "FirstName,MiddleName,LastName,OtherName";
				Element businessName = helper.getOrInsertElementBefore(nameParts,"BusinessName",sAfter);
				businessName.setTextContent(prem.personNameParts.businessName);
			}
			if( prem.personNameParts.firstName != null && prem.personNameParts.firstName.trim().length() > 0 ) {
				sAfter = "MiddleName,LastName,OtherName";
				Element firstName = helper.getOrInsertElementBefore(nameParts,"FirstName",sAfter);
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
			phone.setAttribute("Type", "Unknown");
			phone.setAttribute("Number", prem.personPhone);
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
		Element ePIN = helper.getChildElementByName(ePremises, "PremId");
		String premid = ePIN.getTextContent();
		Element ePremName = helper.getChildElementByName(ePremises, "PremName");
		String premName = ePremName.getTextContent();
		Address address = getAddress(ePremises);
		//TODO Implement Premises Person (Name and Email) if needed.
		Element ePerson = helper.getChildElementByName(ePremises, "Person");
		Element ePhone = helper.getChildElementByName(ePerson, "Phone");
		String phone = ePhone.getAttribute("Number");
		Element eEmail = helper.getChildElementByName(ePerson, "Email");
		String email = eEmail.getAttribute("Address");
		Element eNameParts = helper.getChildElementByName(ePerson, "NameParts");
		if( eNameParts != null ) {
			String businessName = eNameParts.getAttribute("BusinessName");
			String firstName = eNameParts.getAttribute("FirstName");
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
		return prem;
	}
	
	private Address getAddress( Element ePremises ) {
		Element eLine1 = helper.getChildElementByName(ePremises, "Line1");
		String line1 = eLine1.getTextContent();
		Element eLine2 = helper.getChildElementByName(ePremises, "Line2");
		String line2 = eLine2.getTextContent();
		Element eTown = helper.getChildElementByName(ePremises, "Town");
		String town = eTown.getTextContent();
		Element eCounty = helper.getChildElementByName(ePremises, "County");
		String county = eCounty.getTextContent();
		Element eState = helper.getChildElementByName(ePremises, "State");
		String state = eState.getTextContent();
		Element eZIP = helper.getChildElementByName(ePremises, "ZIP");
		String zip = eZIP.getTextContent();
		Element eCountry = helper.getChildElementByName(ePremises, "Country");
		String country = eCountry.getTextContent();
		Element eGeoPoint = helper.getChildElementByName(ePremises, "GeoPoint");
		String sLatitude = eGeoPoint.getAttribute("Latitude");
		Double latitude = Double.parseDouble(sLatitude);
		String sLongitude = eGeoPoint.getAttribute("Longitude");
		Double longitude = Double.parseDouble(sLongitude);
		
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
		String sAfter = ("Consignee".equals(sElementName)?getAfter("Carrier"):getAfter("Consignee") );
		Element contact = helper.getOrInsertElementBefore(sElementName, sAfter);
		if( cont.addressBlock  != null && cont.addressBlock.trim().length() > 0 ) {
			sAfter = "Person";
			Element pin = helper.getOrInsertElementBefore(contact, "AddressBlock", sAfter);
			contact.appendChild(pin);
			pin.setTextContent(cont.addressBlock);
		}
		Element person = helper.getOrAppendChild(contact,"PremName");
		sAfter = "Phone,Email";
		// Careful of choice between name and name parts
		if( cont.personName != null && cont.personName.trim().length() > 0 ) {
			helper.removeChild(person, "NameParts");
			sAfter = "Phone,Email";
			Element name = helper.getOrInsertElementBefore(person,"Name",sAfter);
			name.setTextContent(cont.personName);
		}
		else if( cont.personNameParts != null ) {
			helper.removeChild(person, "Name");  // If we have parts DO NOT include single name
			sAfter = "Name,Phone,Email";
			Element nameParts = helper.getOrInsertElementBefore(person,"NameParts",sAfter);
			if( cont.personNameParts.businessName != null && cont.personNameParts.businessName.trim().length() > 0 ) {
				sAfter = "FirstName,MiddleName,LastName,OtherName";
				Element businessName = helper.getOrInsertElementBefore(nameParts,"BusinessName",sAfter);
				businessName.setTextContent(cont.personNameParts.businessName);
			}
			if( cont.personNameParts.firstName != null && cont.personNameParts.firstName.trim().length() > 0 ) {
				sAfter = "MiddleName,LastName,OtherName";
				Element firstName = helper.getOrInsertElementBefore(nameParts,"FirstName",sAfter);
				firstName.setTextContent(cont.personNameParts.firstName);
			}
			if( cont.personNameParts.middleName != null && cont.personNameParts.middleName.trim().length() > 0 ) {
				sAfter = "LastName,OtherName";
				Element middleName = helper.getOrInsertElementBefore(nameParts,"MiddleName",sAfter);
				middleName.setTextContent(cont.personNameParts.middleName);
			}
			if( cont.personNameParts.lastName != null && cont.personNameParts.lastName.trim().length() > 0 ) {
				sAfter = "OtherName";
				Element lastName = helper.getOrInsertElementBefore(nameParts,"LastName",sAfter);
				lastName.setTextContent(cont.personNameParts.lastName);
			}
		}			
		if( cont.personPhone != null && checkPhoneLength(cont.personPhone) ) {
			sAfter = "Email";
			Element phone = helper.getOrInsertElementBefore(contact,"Phone",sAfter);
			person.appendChild(phone);
			phone.setAttribute("Type", "Unknown");
			phone.setAttribute("Number", cont.personPhone);
		}	
		return contact;
	}
	
	public Element setConsignor( Contact contact ) {
		return setContact( contact, "Consignor");
	}

	public Element setConsignee( Contact contact ) {
		return setContact( contact, "Consignee");
	}

	public void clearAnimals() {
		ArrayList<Element> eAnimals = helper.getElementsByName("Animal");
		for( Element eAnimal : eAnimals) {
			helper.removeElement(eAnimal);
		}
	}

	public Element addAnimal( Animal animalData ) {
		if( !isValidDoc() ) 
			return null;
		String sAfter = getAfter("GroupLot");
		Element animal = helper.insertElementBefore("Animal", sAfter);
		if( animalData.speciesCode.isStandardCode ) {
			Element speciesCode = helper.appendChild(animal, "SpeciesCode");
			speciesCode.setAttribute("Code", animalData.speciesCode.code);
		}
		else {
			Element speciesOther = helper.appendChild(animal, "SpeciesOther");
			speciesOther.setAttribute("Code", animalData.speciesCode.code);
			speciesOther.setAttribute("Text", animalData.speciesCode.text);
		}
		for( AnimalTag tag : animalData.animalTags ) {
			switch( tag.type ) {
			case AIN:
			case MfrRFID:
			case NUES9:
			case NUES8:
			case OtherOfficialID:
			case ManagementID:
				addIDNumber(animal, tag);
				break; 
			case BrandImage:
				addBrandImage(animal, tag);
				break; 
			case EquineDescription:
				addEquineDescription(animal, tag);
				break; 
			case EquinePhotographs:
				addEquinePhotographs(animal, tag);
				break; 
			default:
				logger.error("Unexpected tag " + tag.toString() );
			}

		}
		String sAge = animalData.age;
		if( sAge != null && sAge.trim().length() > 0 )
			animal.setAttribute("Age", sAge);
		String sBreed = animalData.breed;
		if( sBreed != null && sBreed.trim().length() > 0 )
			animal.setAttribute("Breed", sBreed);
		String sSex = animalData.sex;
		if( sSex != null && sSex.trim().length() > 0 )
			animal.setAttribute("Sex", sSex);
		animal.setAttribute("InspectionDate", animalData.inspectionDate);
		return animal;
	}
	
	public ArrayList<Animal> getAnimals() {
		ArrayList<Animal> animals = new ArrayList<Animal>();
		ArrayList<Element> aAnimals = helper.getElementsByName("Animal");
		for( Element eAnimal : aAnimals ) {
			SpeciesCode speciesCode = getSpeciesCode( eAnimal );
			ArrayList<AnimalTag> animalTags = getAnimalTags( eAnimal );
			String age = eAnimal.getAttribute( "Age");
			String breed = eAnimal.getAttribute( "Breed");
			String sex = eAnimal.getAttribute( "Sex");
			String inspectionDate = eAnimal.getAttribute( "InspectionDate");
			// populate above variables.
			Animal animal = new Animal(speciesCode, animalTags, age, breed, sex, inspectionDate);
			animals.add(animal);
		}
		return animals;
	}
	

	public ArrayList<GroupLot> getGroups() {
		ArrayList<GroupLot> groups = new ArrayList<GroupLot>();
		ArrayList<Element> aGroups = helper.getElementsByName("GroupLot");
		for( Element eGroup : aGroups ) {
			SpeciesCode speciesCode = getSpeciesCode( eGroup );
			String groupLotId = null;
			Element eGroupLotId = helper.getChildElementByName(eGroup, "GroupLotID");
			String sGroupLotId = eGroupLotId.getTextContent();
			String sQuantity = eGroup.getAttribute("Quantity");
			Double quantity = Double.parseDouble(sQuantity);
			String unit = eGroup.getAttribute("Unit");
			String age = eGroup.getAttribute( "Age");
			String breed = eGroup.getAttribute( "Breed");
			String sex = eGroup.getAttribute( "Sex");
			String description = eGroup.getAttribute( "Description");
			// populate above variables.
			GroupLot group = new GroupLot( speciesCode, groupLotId, quantity, unit, age, breed, sex, description);
			groups.add(group);
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
		ArrayList<Element> aAnimalTags = helper.getChildElements(eAnimalTags);
		for( Element e : aAnimalTags ) {
			AnimalTag tag;
			String sType = e.getLocalName();
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
		eTag.setAttribute("Number", tag.value);
	}
	
	private void addBrandImage(Element animal, AnimalTag tag) {
		String sAfter = "Test,Vaccination";
		Element eTag = helper.insertElementBefore(animal, "BrandImage", sAfter);
		eTag.setAttribute("BrandImageRef", findImageRef(tag));
		eTag.setAttribute("Description", tag.value);
	}
	
	private void addEquineDescription(Element animal, AnimalTag tag) {
		String sAfter = "Test,Vaccination";
		Element eTag = helper.insertElementBefore(animal, "EquineDescription", sAfter);
		eTag.setAttribute("Name", tag.description.name);
		eTag.setAttribute("Description", tag.description.description);
	}
	
	private void addEquinePhotographs(Element animal, AnimalTag tag) {
		String sAfter = "Test,Vaccination";
		Element eTag = helper.insertElementBefore(animal, "EquinePhotographs", sAfter);
		for( String sView : tag.photographs.views ) {
			eTag.setAttribute("ImageRef", findImageRef(tag, sView));
			eTag.setAttribute("View", sView);	
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
				eAnimal.setAttribute("SpeciesCode", sNewSpecies);
			}
		}
		ArrayList<Element> eGroups = XMLUtility.listChildElementsByName(root, "GroupLot");
		for( Element eGroup : eGroups) {
			String sExistingCode = eGroup.getAttribute("SpeciesCode");
			if( sExistingCode != null && sExistingCode.equals(sPreviousSpecies) ) {
				eGroup.setAttribute("SpeciesCode", sNewSpecies);
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
		String sAfter = getAfter("Statements");
		Element groupLot = helper.insertElementBefore("GroupLot", sAfter);
		if( group.speciesCode.isStandardCode ) {
			Element speciesCode = helper.appendChild(groupLot, "SpeciesCode");
			speciesCode.setAttribute("Code", group.speciesCode.code);
		}
		else {
			Element speciesOther = helper.appendChild(groupLot, "SpeciesOther");
			speciesOther.setAttribute("Code", group.speciesCode.code);
			speciesOther.setAttribute("Text", group.speciesCode.text);
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
			groupLot.setAttribute("Quantity", quantity.toString());
		String sUnit = group.unit;
		if( sUnit != null && sUnit.trim().length() > 0 )
			groupLot.setAttribute("Unit", sUnit);
		String sAge = group.age;
		if( sAge != null && sAge.trim().length() > 0 )
			groupLot.setAttribute("Age", sAge);
		String sBreed = group.breed;
		if( sBreed != null && sBreed.trim().length() > 0 )
			groupLot.setAttribute("Breed", sBreed);
		String sSex = group.sex;
		if( sSex != null && sSex.trim().length() > 0 )
			groupLot.setAttribute("Sex", sSex);
		String sDescription = group.description;
		if( sDescription != null && sDescription.trim().length() > 0 )
			groupLot.setAttribute("Description", sDescription);
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
	
	public void addPDFAttachement( byte[] pdfBytes, String sFileName ) {
		if( isValidDoc() && pdfBytes != null && pdfBytes.length > 0 && !attachmentExists(sFileName)) {
			binaries.addPDFAttachment(pdfBytes, sFileName);
		}
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
	
	public void addMetadataAttachement( CviMetaDataXml metaData ) {
		String sXML = metaData.getXmlString();
		try {
			byte[] xmlBytes = sXML.getBytes("UTF-8");
			binaries.addOrUpdateMetadata(xmlBytes);
		} catch (UnsupportedEncodingException e1) {
			logger.error(e1);
		}
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
