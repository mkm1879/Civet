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
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import edu.clemson.lph.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.xml.elements.AnimalTag;
import edu.clemson.lph.utils.IDTypeGuesser;
import edu.clemson.lph.utils.XMLUtility;

public class CoKsXML {
      private static Logger logger = Logger.getLogger();
	private Node xmlNode = null;
	private String xmlString = null;

	/**
	 * Create a CoKsXML from the document Node returned by iText extraction of XFA data
	 * @param xmlNode
	 */
	public CoKsXML( Node xmlNode ) {
		this.xmlNode = xmlNode;
		// To match what is returned by JPedal we leave off the xml declaration.
		xmlString = nodeToString( xmlNode, false );
	}
	
	public CoKsXML(String sDataXml) {
		xmlString = sDataXml;
		try {
			Document dom = XMLUtility.stringToDom(sDataXml);
			xmlNode = dom.getDocumentElement();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
		
	}

	public String getVersion() {
		String sRet = null;
		Document doc = null;
		Node nEcvi = null;
		if( xmlString != null && xmlNode == null) {
			try {
				DocumentBuilder db = SafeDocBuilder.getSafeDocBuilder();  //DocumentBuilderFactory.newInstance().newDocumentBuilder();
				InputSource is = new InputSource();
				// Move namespace definition to each of the header nodes because we are losing the XFA document node later.
				String sStrip = xmlString.replaceAll(" xfa:dataNode=\"dataGroup\"", " xmlns:xfa=\"http://www.xfa.org/schema/xfa-data/1.0/\"\nxfa:dataNode=\"dataGroup\"");
				is.setCharacterStream(new StringReader(sStrip));
				doc = db.parse(is);
				NodeList nl = doc.getElementsByTagName("eCVI");
				if( nl.getLength() == 1 ) {
					nEcvi = nl.item(0);
					sRet = nodeToString(nEcvi, false);
				}
				else {
					return null;
				}
			} catch (SAXException e) {
				logger.error("Failed to parse XML\n" + xmlString, e);
			} catch (IOException e) {
				logger.error("Failed to read XML\n" + xmlString, e);
//			} catch (ParserConfigurationException e) {
//				logger.error("Failed to configure XML parser", e);
			}
		}
		else {
			nEcvi = XMLUtility.findFirstChildElementByName(xmlNode, "eCVI");
			if( nEcvi == null ) 
				return null;
		}
		Node nVer = XMLUtility.findFirstChildElementByName(nEcvi, "version");
		if( nVer != null )
			sRet = nVer.getTextContent();
		return sRet;
		
	}
	
	/**
	 * returns the XFA dataset document as is
	 * @return XML String
	 */
	@Override
	public String toString() {
		if( xmlString != null )
			return xmlString;
		else 
			return super.toString();
	}
	
	/**
	 * Returns the XFA data as formatted by Acrobat's export XML with eCVI as the document element.
	 * Omits the xfa:dataset and xfa:data elements and puts definition of xfa namespace in 
	 * datagroup attributes that use it.
	 * @return XML String
	 */
	public String toAcrobatXMLString() {
		String sRet = null;
		Document doc = null;
		Node nEcvi = null;
		if( xmlString == null && xmlNode != null) {
			xmlString = nodeToString( xmlNode, true );
		}
		if( xmlString == null )
			return null;
		try {
			DocumentBuilder db = SafeDocBuilder.getSafeDocBuilder(); //DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			// Move namespace definition to each of the header nodes because we are losing the XFA document node later.
			String sStrip = xmlString.replaceAll(" xfa:dataNode=\"dataGroup\"", " xmlns:xfa=\"http://www.xfa.org/schema/xfa-data/1.0/\"\nxfa:dataNode=\"dataGroup\"");
			is.setCharacterStream(new StringReader(sStrip));
			doc = db.parse(is);
			doc.setXmlStandalone(true);
		} catch (SAXException e) {
			logger.error("Failed to parse XML\n" + xmlString, e);
		} catch (IOException e) {
			logger.error("Failed to read XML\n" + xmlString, e);
//		} catch (ParserConfigurationException e) {
//			logger.error("Failed to configure XML parser", e);
		}
		NodeList nl = doc.getElementsByTagName("eCVI");
		if( nl.getLength() == 1 ) {
			nEcvi = nl.item(0);
			sRet = nodeToString(nEcvi, false);
		}
		return sRet;
	}
	
	
	private String nodeToString(Node node, boolean bOmitDeclaration) {
		String sOmit = bOmitDeclaration ? "yes" : "no";
		StringWriter sw = new StringWriter();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, sOmit);
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException te) {
			logger.error("nodeToString Transformer Exception", te);
		}
		return sw.toString();
	}

	public String toStdXMLString() {
		String sRet = null;
		String sXSLT = CivetConfig.getCoKsXSLTFile();
		String sAcrobatXML = toAcrobatXMLString();
		try {
		    FileReader xsltReader = new FileReader( sXSLT );
		    StringReader sourceReader = new StringReader( sAcrobatXML );
		    ByteArrayOutputStream baosDest = new ByteArrayOutputStream();
			TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer;
				transformer = tFactory.newTransformer(new StreamSource(xsltReader));
				transformer.transform(new StreamSource(sourceReader),
						new StreamResult(baosDest));
				sRet = new String( baosDest.toByteArray(), "UTF-8" );
			} catch ( TransformerException e) {
				logger.error("Failed to transform XML with XSLT: " + sXSLT, e);
			} catch (UnsupportedEncodingException e) {
				logger.error("Should not see this unsupported encoding", e);
			} catch (FileNotFoundException e) {
				logger.error("Could not find XSLT: " + sXSLT, e);
			}
		sRet = postProcessStdXML( sRet );
 		return sRet;
	}
	
	public Node toStdXMLNode() {
		Node nRet = null;
		String sStdXML = toStdXMLString();
		if( sStdXML != null ) {
			try {
				DocumentBuilder db = SafeDocBuilder.getSafeDocBuilder(); //DocumentBuilderFactory.newInstance().newDocumentBuilder();
				InputSource is = new InputSource();
				is.setCharacterStream(new StringReader(sStdXML));
				Document doc = db.parse(is);
				doc.setXmlStandalone(true);
				nRet = doc.getDocumentElement();	
			} catch (SAXException e) {
				logger.error("Failed to parse XML\n" + sStdXML, e);
			} catch (IOException e) {
				logger.error("Failed to read XML\n" + sStdXML, e);
//			} catch (ParserConfigurationException e) {
//				logger.error("Failed to setup XML parser", e);;
			}
		}
		return nRet;
	}
	
	private String postProcessStdXML( String sStdXML ) {
		Document doc = null;
		if( sStdXML != null ) {
			try {
				DocumentBuilder db = SafeDocBuilder.getSafeDocBuilder();  //DocumentBuilderFactory.newInstance().newDocumentBuilder();
				InputSource is = new InputSource();
				is.setCharacterStream(new StringReader(sStdXML));
				doc = db.parse(is);
				doc.setXmlStandalone(true);
			} catch (SAXException e) {
				logger.error("Failed to parse XML\n" + sStdXML, e);
				return null;
			} catch (IOException e) {
				logger.error("Failed to read XML\n" + sStdXML, e);
				return null;
//			} catch (ParserConfigurationException e) {
//				logger.error("Failed to setup XML parser", e);
//				return null;
			}
		}
		return postProcessStdXML( doc );
	}
	
	private String postProcessStdXML( Document doc ) {
		if( doc == null ) return null;
		XMLDocHelper helper = new XMLDocHelper( doc );
		NodeList nlAnimalTags = helper.getNodeListByPath("//AnimalTag");
		if( nlAnimalTags != null ) {
			for( int i = 0; i < nlAnimalTags.getLength(); i++ ) {
				Element eTag = (Element)nlAnimalTags.item(i);
				String sTag = eTag.getAttribute("Number");
				String sType = eTag.getAttribute("Type");
				if( sType == null || sType.trim().length() == 0 || sType.equalsIgnoreCase("UN") ) {
					sType = AnimalTag.getElementName(IDTypeGuesser.getTagType(sTag, true));
					eTag.setAttribute("Type", sType);
				}
			}
		}
		return helper.getXMLString();
	}
	
	public ArrayList<String> listSpeciesCodes() {
		ArrayList<String> aRet = new ArrayList<String>();
		String sXML = toAcrobatXMLString();
		int iStart = sXML.indexOf("<spp>");
		while( iStart > 0 ) {
			int iEnd = sXML.indexOf("</spp>", iStart+5);
			String sSpp = sXML.substring(iStart+5, iEnd);
			aRet.add(sSpp);
			iStart = sXML.indexOf("<spp>", iEnd + 5);
		}
		return aRet;
	}
	
	public String getPurposeCode() {
		String sRet = null;
		String sXML = toAcrobatXMLString();
		int iStart = sXML.indexOf("<purpose>");
		if( iStart > 0 ) {
			int iEnd = sXML.indexOf("</purpose>", iStart+5);
			sRet = sXML.substring(iStart+9, iEnd);
		}
		return sRet;
	}
	

}
