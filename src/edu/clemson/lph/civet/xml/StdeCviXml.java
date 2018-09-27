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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.lookup.Counties;
import edu.clemson.lph.utils.XMLUtility;
/**
 * Note: kind of weird different style here than in other XML.  Someday maybe make them match.
 * @author mmarti5
 *
 */
public class StdeCviXml {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private XMLDocHelper helper;

	public StdeCviXml( Node rootNode ) {
		if( rootNode != null ) {
			Document doc = rootNode.getOwnerDocument();
			helper = new XMLDocHelper( doc );
		}
	}
	
	public StdeCviXml( File fXML ) {
		try {
			BufferedReader reader = new BufferedReader( new FileReader( fXML ) );
			StringBuffer sb = new StringBuffer();
			String sLine = reader.readLine();
			while( sLine != null ) {
				sb.append(sLine);
				sLine = reader.readLine();
			}
			reader.close();
			String sXML = sb.toString();
			// Check to see if this is a version 2 schema file
			int iV2Loc = sXML.indexOf("http://www.usaha.org/xmlns/ecvi2");
			int iVersion = 1;
			if( iV2Loc > 0 && iV2Loc < 200 )
				iVersion = 2;
			buildStdeCviXml( sXML, iVersion );
		
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public StdeCviXml( String sStdXML, int iVersion ) {
		buildStdeCviXml( sStdXML, iVersion );
	}

	public StdeCviXml( String sStdXML ) {
		buildStdeCviXml( sStdXML, 1 );
	}
	
	public Document getDocument() {
		if( helper != null )
			return helper.getDocument();
		else
			return null;
	}
	
	public Element getRoot() {
		if( getDocument() == null ) return null;
		return getDocument().getDocumentElement();
	}
	
	public XMLDocHelper getHelper() {
		return helper;
	}
	
	private void buildStdeCviXml( String sStdXML, int iVersion ) {
		if( iVersion == 2 ) {
			sStdXML = V2Transform.convertToV1( sStdXML );
		}
		try {
			DocumentBuilder db = SafeDocBuilder.getSafeDocBuilder(); //DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(sStdXML));
			Document doc = db.parse(is);
			doc.setXmlStandalone(true);
			helper = new XMLDocHelper( doc );
		} catch (SAXException e) {
			logger.error("Could not parse stdXML", e);;
		} catch (IOException e) {
			logger.error("Could not read stdXML", e);;
//		} catch (ParserConfigurationException e) {
//			logger.error("Could not set up parser for stdXML", e);;
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
		// Note: currently above returns "" if the element does not exist so 
		// we attempt to remove non-existent node.  Null check catches in removeElementByPath 
		// but this is ugly.
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
	
	public String getMovementPurpose() {
		String sRet = null;
		String sPath = "//MovementPurpose";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}

	public String getSpeciesCode(Node nAnimal) {
		String sRet = null;
		String sPath = "";
		String sAttr = "SpeciesCode";
		sRet = helper.getAttributeByPath(nAnimal, sPath,sAttr);
		return sRet;
	}
	
	public int getQuantity(Node nGroup) {
		int iRet = -1;
		String sRet = null;
		String sPath = "";
		String sAttr = "Quantity";
		sRet = helper.getAttributeByPath(nGroup, sPath,sAttr);
		try {
			float fRet = Float.parseFloat(sRet);
			iRet = (int)fRet;
		} catch( NumberFormatException nfe ) {
			logger.error("Failed to parse " + sRet, nfe);
		}
		return iRet;
	}

	public String getSpeciesCodes() {
		NodeList animals = listAnimals();
		NodeList groups = listGroups();
		ArrayList<String> aSpp = new ArrayList<String>();
		if( animals != null ) {
			for( int i = 0; i < animals.getLength(); i++ ) {
				Node n = animals.item(i);
				String sSp = getSpeciesCode(n);
				if( !aSpp.contains(sSp) )
					aSpp.add(sSp);
			}
		}
		if( groups != null ) {
			for( int j = 0; j < groups.getLength(); j++ ) {
				Node n = groups.item(j);
				String sSp = getSpeciesCode(n);
				if( !aSpp.contains(sSp) )
					aSpp.add(sSp);
			}
		}
		int i = 0;
		StringBuffer sb = new StringBuffer();
		for( String sSp : aSpp ) {
			if( i++ > 0 )
				sb.append(", ");
			sb.append(sSp);
		}
		return sb.toString();
	}
	
	public void updateSpeciesCodes( String sPreviousCode, String sNewCode ) {
		NodeList animals = listAnimals();
		NodeList groups = listGroups();
		if( animals != null ) {
			for( int i = 0; i < animals.getLength(); i++ ) {
				Node n = animals.item(i);
				String sSp = getSpeciesCode(n);
				if( sPreviousCode.equals(sSp) )
					setSpeciesCode(n, sNewCode);
			}
		}
		if( groups != null ) {
			for( int j = 0; j < groups.getLength(); j++ ) {
				Node n = groups.item(j);
				String sSp = getSpeciesCode(n);
				if( sPreviousCode.equals(sSp) )
					setSpeciesCode(n, sNewCode);
			}
		}
		return;
	}

	public void setSpeciesCode(Node nAnimal, String sNewCode) {
		String sPath = "";
		String sAttr = "SpeciesCode";
		helper.updateAttribute(nAnimal, sPath, sAttr);
		return;
	}
	

	public String getAnimalID(Node nAnimal) {
		String sRet = null;
		String sPath = "AnimalTag";
		String sAttr = "Number";
		sRet = helper.getAttributeByPath(nAnimal, sPath,sAttr);
		return sRet;
	}
	
	public String getOriginPremName() {
		String sRet = null;
		String sPath = "/eCVI/Origin/PremName";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}
	
	public String getOriginPremId() {
		String sRet = null;
		String sPath = "/eCVI/Origin/PremId";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}
	
	public String getOriginPersonName() {
		String sRet = null;
		String sPath = "/eCVI/Origin/Person/Name";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}
	
	public String getOriginPhone() {
		String sRet = null;
		String sPath = "/eCVI/Origin/Person/Phone";
		String sAttr = "Number";
		sRet = helper.getAttributeByPath(sPath,sAttr);
		return sRet;
	}


	public String getOriginStreet() {
		String sRet = null;
		String sPath = "/eCVI/Origin/Address/Line1";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}
	
	public String getOriginCity() {
		String sRet = null;
		String sPath = "/eCVI/Origin/Address/Town";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}
	
	public String getOriginCounty() {
		String sRet = null;
		String sPath = "/eCVI/Origin/Address/County";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}
	
	public String validateHerdsOriginCounty() {
		String sRet = null;
		String sStatePath = "/eCVI/Origin/Address/State";
		String sState = helper.getElementTextByPath(sStatePath);
		String sCountyPath = "/eCVI/Origin/Address/County";
		String sOriginalCounty = helper.getElementTextByPath(sCountyPath);
		if( sOriginalCounty != null ) {
			sRet = Counties.getHerdsCounty(sState, sOriginalCounty);
			if( !sOriginalCounty.equalsIgnoreCase(sRet) ) {
				if( sRet != null && sRet.trim().length() > 3 )
					helper.updateElementByPath( sCountyPath, sRet);
				else
					helper.removeElementByPath( sCountyPath );
			}
		}
		return sRet;
	}
	
	public String getOriginState() {
		String sRet = null;
		String sPath = "/eCVI/Origin/Address/State";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}
	
	public String getOriginZip() {
		String sRet = null;
		String sPath = "/eCVI/Origin/Address/ZIP";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}
	
	public String getOriginCountry() {
		String sRet = null;
		String sPath = "/eCVI/Origin/Address/Country";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}
//
	
	public String getDestinationPremName() {
		String sRet = null;
		String sPath = "/eCVI/Destination/PremName";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}
	
	public String getDestinationPremId() {
		String sRet = null;
		String sPath = "/eCVI/Destination/PremId";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}
	
	public String getDestinationPersonName() {
		String sRet = null;
		String sPath = "/eCVI/Destination/Person/Name";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}
	
	public String getDestinationPhone() {
		String sRet = null;
		String sPath = "/eCVI/Destination/Person/Phone";
		String sAttr = "Number";
		sRet = helper.getAttributeByPath(sPath,sAttr);
		return sRet;
	}
	
	public String getDestinationStreet() {
		String sRet = null;
		String sPath = "/eCVI/Destination/Address/Line1";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}
	
	public String getDestinationCity() {
		String sRet = null;
		String sPath = "/eCVI/Destination/Address/Town";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}
	
	public String getDestinationCounty() {
		String sRet = null;
		String sPath = "/eCVI/Destination/Address/County";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}
	
	public String validateHerdsDestinationCounty() {
		String sRet = null;
		String sStatePath = "/eCVI/Destination/Address/State";
		String sState = helper.getElementTextByPath(sStatePath);
		String sCountyPath = "/eCVI/Destination/Address/County";
		String sOriginalCounty = helper.getElementTextByPath(sCountyPath);
		sRet = Counties.getHerdsCounty(sState, sOriginalCounty);
		if( sOriginalCounty != null ) {
			sRet = Counties.getHerdsCounty(sState, sOriginalCounty);
			if( !sOriginalCounty.equalsIgnoreCase(sRet) ) {
				if( sRet != null && sRet.trim().length() > 3 )
					helper.updateElementByPath( sCountyPath, sRet);
				else
					helper.removeElementByPath( sCountyPath );
			}
		}
		return sRet;
	}
	
	public String getDestinationState() {
		String sRet = null;
		String sPath = "/eCVI/Destination/Address/State";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}
	
	public String getDestinationZip() {
		String sRet = null;
		String sPath = "/eCVI/Destination/Address/ZIP";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}
	
	public String getDestinationCountry() {
		String sRet = null;
		String sPath = "/eCVI/Destination/Address/Country";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}

	public String getVetName() {
		String sRet = null;
		String sPath = "/eCVI/Veterinarian/Person/Name";
		sRet = helper.getElementTextByPath(sPath);
		return sRet;
	}

	public String getVetLicNo() {
		String sRet = null;
		String sPath = "/eCVI/Veterinarian";
		String sAttr = "LicenseNumber";
		sRet = helper.getAttributeByPath(sPath, sAttr);
		return sRet;
	}

	public String getVetNAN() {
		String sRet = null;
		String sPath = "/eCVI/Veterinarian";
		String sAttr = "NationalAccreditationNumber";
		sRet = helper.getAttributeByPath(sPath, sAttr);
		return sRet;
	}
	
	public String getVetPhone() {
		String sRet = null;
		sRet = helper.getAttributeByPath( "/eCVI/Veterinarian/Person/Phone", "Number");
		return sRet;
	}
	
	public NodeList listAnimals() {
		return helper.getNodeListByPath( "/eCVI/Animal" );
	}
	
	public NodeList listGroups() {
		return helper.getNodeListByPath( "/eCVI/GroupLot" );
	}
	
	public void setOriginalCVI( byte[] pdfBytes, String sFileName ) {
		if( pdfBytes != null && pdfBytes.length > 0 ) {
			Document doc = helper.getDocument();
			if( doc == null ) {
				logger.error("Missing doc element", new Exception("Bad XMLDocumentHelper"));
				return;
			}
			String sPDF64 = javax.xml.bind.DatatypeConverter.printBase64Binary(pdfBytes);
			try {
				Element root = doc.getDocumentElement();
				if( root == null ) {
					logger.error("Missing root element", new Exception("Bad XMLDocumentHelper"));
					return;
				}
				Element attach = helper.getElementByPathAndAttribute( "/eCVI/Attachment", "DocType", "PDF CVI");
				if( attach == null ) {
					attach = doc.createElement("Attachment");
					root.appendChild(attach);
					attach.setAttribute("DocType", "PDF CVI");
					attach.setAttribute("MimeType", "application/pdf");
					attach.setAttribute("Filename", sFileName);
					Element payload = doc.createElement("Payload");
					payload.setTextContent(sPDF64);
					attach.appendChild(payload);
				}
				else {
					Element payload = helper.getElementByPath(attach, "Payload");
					if( payload == null ) {
						payload = doc.createElement("Payload");
						payload.setTextContent(sPDF64);
						attach.appendChild(payload);
					}
					else {
						payload.setTextContent(sPDF64);
					}
				}
			} catch ( Exception e) {
				logger.error("Should not see this error for unsupported encoding", e);
			}
		}
	}
	
	public byte[] getOriginalCVI() {
		byte[] bytes = null;
		Element e = helper.getElementByPathAndAttribute( "/eCVI/Attachment", "DocType", "PDF CVI");
		if( e != null ) {
			Element payload = helper.getElementByPath(e, "Payload");
			if( payload != null ) {
				String sBase64 = payload.getTextContent();
				bytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(sBase64);
			}
		}
		return bytes;
	}
	
	public String getOriginalCVIFileName() {
		String sRet = null;
		Element e = helper.getElementByPathAndAttribute( "/eCVI/Attachment", "DocType", "PDF CVI");
		if( e != null )
			sRet = e.getAttribute("Filename");
		return sRet;
	}
	
	public void addMetadataAttachement( CviMetaDataXml metaData ) {
		String sXML = metaData.getXmlString();
		try {
			byte[] xmlBytes = sXML.getBytes("UTF-8");
			if( xmlBytes != null && xmlBytes.length > 0 ) {
				Document doc = helper.getDocument();
				if( doc == null ) {
					logger.error("Missing doc element", new Exception("Bad XMLDocumentHelper"));
					return;
				}
				String sMetadata64 = javax.xml.bind.DatatypeConverter.printBase64Binary(xmlBytes);
				Element root = doc.getDocumentElement();
				if( root == null ) {
					logger.error("Missing root element", new Exception("Bad XMLDocumentHelper"));
					return;
				}
				Element attach = helper.getElementByPathAndAttribute( "/eCVI/Attachment", "Filename", "CviMetadata.xml");
				if( attach == null ) {
					attach = doc.createElement("Attachment");
					root.appendChild(attach);
					attach.setAttribute("DocType", "Other");
					attach.setAttribute("MimeType", "text/xml");
					attach.setAttribute("Filename", "CviMetadata.xml");
				}
				Element payload = helper.getElementByPath( attach, "Payload" );
				if( payload == null ) {
					payload = doc.createElement("Payload");
					attach.appendChild(payload);
				}
				payload.setTextContent(sMetadata64);
			}
		} catch (UnsupportedEncodingException e1) {
			logger.error(e1);
		} catch ( Exception e) {
			logger.error("Should not see this error for unsupported encoding", e);
		}
	}
	
	public CviMetaDataXml getMetaData() {
		CviMetaDataXml mRet = null;
		Element e = helper.getElementByPathAndAttribute( "/eCVI/Attachment", "Filename", "CviMetadata.xml");
		if( e != null ) {
			String sBase64 = e.getTextContent();
			mRet = new CviMetaDataXml( sBase64 );
		}
		return mRet;
	}
	
	public String getXMLString() {
		return helper.getXMLString();
	}


} // End class StdeCviXml
