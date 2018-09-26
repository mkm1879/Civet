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
import edu.clemson.lph.utils.XMLUtility;

public class CviMetaDataXml {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	public static final String CVI_SRC_CIVET = "Civet";
	public static final String CVI_SRC_VSPS = "VSPS";
	public static final String CVI_SRC_SWINE = "SWINE_SS";
	public static final String CVI_SRC_9dash3 = "9_Dash_3";
	public static final String CVI_SRC_PERMIT = "Permit_Form";
	private Document doc = null;
	
	public CviMetaDataXml() {
//		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = SafeDocBuilder.getSafeDocBuilder(); //docFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();
			doc.setXmlStandalone(true);
			Element rootElement = doc.createElement("cviMetaData");
			doc.appendChild(rootElement);
		} catch (Exception e ) {
			logger.error(e);
//		} catch (ParserConfigurationException e) {
//			logger.error(e);
		}
	}
	
	public CviMetaDataXml(String sBase64) {
		byte[] bytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(sBase64);

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = SafeDocBuilder.getSafeDocBuilder(); //docFactory.newDocumentBuilder();
			String sXML = new String(bytes, "UTF-8");
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(sXML));
			doc = docBuilder.parse(is);
			doc.setXmlStandalone(true);
//		} catch (ParserConfigurationException e) {
//			logger.error(e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		} catch (SAXException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	public void setCertificateNbr( String sCertNbr ) {
		if( doc == null ) {
			logger.error(new Exception("setCertificateNbr called with null Document"));
			return;
		}
		if( sCertNbr == null ) {
			logger.error(new Exception("setCertificateNbr called with null value in sCertNbr"));
			return;
		}
		Element rootE = doc.getDocumentElement();
		if( rootE == null ) {
			logger.error(new Exception("setCertificateNbr could not find document element"));
			return;
		}
		rootE.setAttribute("certificateNbr", sCertNbr);
		
	}
	
	public void setBureauReceiptDate( java.util.Date dReceived ) {
		String sDateReceived = XMLUtility.dateToXmlDate( dReceived );
		if( sDateReceived == null ) return;
		if( doc == null ) {
			logger.error(new Exception("setBureauReceiptDate called with null Document"));
			return;
		}
		Element rootE = doc.getDocumentElement();
		if( rootE == null ) {
			logger.error(new Exception("setBureauReceiptDate could not find document element"));		
			return;
		}
		Element eDateReceived = XMLUtility.findFirstChildElementByName(rootE, "bureauReceiptDate");
		if( eDateReceived == null ) {
			eDateReceived = doc.createElement("bureauReceiptDate");
			Element eErrors = XMLUtility.findFirstChildElementByName(rootE, "errors");
			if( eErrors != null )
				rootE.insertBefore(eDateReceived, eErrors);
			else
				rootE.appendChild(eDateReceived);
		}
		eDateReceived.setTextContent(sDateReceived);
	}
	
	public java.util.Date getBureauReceiptDate() {
		java.util.Date dRet = null;
		if( doc == null ) {
			logger.error(new Exception("getBureauReceiptDate called with null Document"));
			return null;
		}
		Element rootE = doc.getDocumentElement();
		if( rootE == null ) {
			logger.error(new Exception("getBureauReceiptDate could not find document element"));		
			return null;
		}
		Element eDateReceived = XMLUtility.findFirstChildElementByName(rootE, "bureauReceiptDate");
		if( eDateReceived != null ) {
			String sDate = eDateReceived.getTextContent();
			dRet = XMLUtility.xmlDateToDate( sDate );
		}
		return dRet;
	}
	
	public void addErrors( ArrayList<String> aShortNames ) {
		for( String sShortName : aShortNames )
			addError( sShortName );
	}
	
	public void addError( String sShortName ) {
		if( doc == null ) {
			logger.error(new Exception("addError called with null Document"));
			return;
		}
		if( sShortName == null  ) {
			logger.error(new Exception("addError called with null ShortName "));
			return;
		}
		Element rootE = doc.getDocumentElement();
		if( rootE == null ) {
			logger.error(new Exception("addError could not find document element"));		
			return;
		}
		Element eErrors = XMLUtility.findFirstChildElementByName(rootE, "errors");
		ArrayList<Element> nErrorList = null;
		if( eErrors == null ) {
			eErrors = doc.createElement("errors");
			rootE.appendChild(eErrors);
		}
		else {
			nErrorList = XMLUtility.listChildElementsByName(eErrors, "err");
		}
		if( nErrorList != null ) {
			for( Node nThisError : nErrorList ) {
				if( nThisError.getNodeType() == Node.ELEMENT_NODE && nThisError.getNodeName().equals("err") ) {
					String sValue = nThisError.getTextContent();
					if( sShortName.equalsIgnoreCase(sValue) ) {
						logger.error( new Exception("Error key " + sValue + " already entered") );
						return;
					}
				}
			}
		}	
		Element eNewError = doc.createElement("err");
		eNewError.setTextContent(sShortName);
		eErrors.appendChild(eNewError);
	}
		
	public boolean hasErrors() {
		boolean bRet = false;
		if( doc == null ) {
			logger.error(new Exception("hasErrors called with null Document"));
			return false;
		}
		Element rootE = doc.getDocumentElement();
		if( rootE == null ) {
			logger.error(new Exception("hasErrors could not find document element"));		
			return false;
		}
		Element eErrors = XMLUtility.findFirstChildElementByName(rootE, "errors");
		if( eErrors != null ) {
			NodeList nl = eErrors.getChildNodes();
			for( int i = 0; i < nl.getLength(); i++ ) {
				if( nl.item(i).getNodeType() == Node.ELEMENT_NODE ) {
					bRet = true;
					break;
				}
			}
		}
		return bRet;
	}
	
	public ArrayList<String> listErrors() {
		ArrayList<String> aErrors = new ArrayList<String>();
		if( doc == null ) {
			logger.error(new Exception("hasErrors called with null Document"));
			return null;
		}
		Element rootE = doc.getDocumentElement();
		if( rootE == null ) {
			logger.error(new Exception("hasErrors could not find document element"));		
			return null;
		}
		Element eErrors = XMLUtility.findFirstChildElementByName(rootE, "errors");
		if( eErrors != null ) {
			NodeList nl = eErrors.getChildNodes();
			for( int i = 0; i < nl.getLength(); i++ ) {
				if( nl.item(i).getNodeType() == Node.ELEMENT_NODE ) {
					String sErr = nl.item(i).getTextContent();
					aErrors.add(sErr);
				}
			}
		}
		return aErrors;
	}
	
	public String getErrorsString() {
		ArrayList<String> aErrors = listErrors();
		if( aErrors != null && aErrors.size() > 0 ) {
			StringBuffer sb = new StringBuffer();
			int i = 0;
			for( String sErr : aErrors ) {
				sb.append(sErr);
				if( i < aErrors.size() )
					sb.append(',');
				i++;
			}
			return sb.toString();
		}
		else 
			return null;
	}
	
	public void setErrorNote( String sNote ) {
		if( sNote == null || sNote.trim().length() == 0 ) return;
		if( doc == null ) {
			logger.error(new Exception("setErrorNote called with null Document"));
			return;
		}
		Element rootE = doc.getDocumentElement();
		if( rootE == null ) {
			logger.error(new Exception("setErrorNote could not find document element"));		
			return;
		}
		Element eErrorNote = XMLUtility.findFirstChildElementByName(rootE, "errorNote");
		if( eErrorNote == null ) {
			eErrorNote = doc.createElement("errorNote");
			rootE.appendChild(eErrorNote);
		}
		eErrorNote.setTextContent(sNote);
	}
	
	public String getErrorNote() {
		String sRet = null;
		if( doc == null ) {
			logger.error(new Exception("getErrorNote called with null Document"));
			return null;
		}
		Element rootE = doc.getDocumentElement();
		if( rootE == null ) {
			logger.error(new Exception("getErrorNote could not find document element"));		
			return null;
		}
		Element eNote = XMLUtility.findFirstChildElementByName(rootE, "errorNote");
		if( eNote != null )
			sRet = eNote.getTextContent();
		return sRet;
		
	}

	
	public void setCVINumberSource( String sCVINumberSource ) {
		if( sCVINumberSource == null || sCVINumberSource.trim().length() == 0 ) return;
		if( doc == null ) {
			logger.error(new Exception("setCVINumberSource called with null Document"));
			return;
		}
		Element rootE = doc.getDocumentElement();
		if( rootE == null ) {
			logger.error(new Exception("setCVINumberSource could not find document element"));		
			return;
		}
		Element eCVINumberSource = XMLUtility.findFirstChildElementByName(rootE, "cviNumberSource");
		if( eCVINumberSource == null ) {
			eCVINumberSource = doc.createElement("cviNumberSource");
			rootE.appendChild(eCVINumberSource);
		}
		eCVINumberSource.setTextContent(sCVINumberSource);
	}
	
	public String getCVINumberSource() {
		String sRet = null;
		if( doc == null ) {
			logger.error(new Exception("setCVINumberSource called with null Document"));
			return null;
		}
		Element rootE = doc.getDocumentElement();
		if( rootE == null ) {
			logger.error(new Exception("setCVINumberSource could not find document element"));		
			return null;
		}
		Element eCVINumberSource = XMLUtility.findFirstChildElementByName(rootE, "cviNumberSource");
		if( eCVINumberSource != null )
			sRet = eCVINumberSource.getTextContent();
		return sRet;
		
	}

	
	public Document getDocument() {
		return doc;
	}
	
	public String getXmlString() {
		if( doc == null ) return null;
		return XMLUtility.domToString(doc);
	}

}
