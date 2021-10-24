package edu.clemson.lph.civet.webservice;
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
import java.util.ArrayList;

import edu.clemson.lph.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.utils.XMLUtility;

/**
 * 
 * Logic Stack
 * WSDL At: http://corp-hbg-04.compaid.com/USAHERDS/webservice/externalmessages.asmx (edited)
 * Generate Apache Axis2 client code for ADB binding. 
 * 		E:\JavaLib\axis2-1.6.2\bin\wsdl2java -S src -p com.cai.webservice -or -uri CivetExternalMessages.wsdl
 * 	 public class ExternalMessagesStub (Generated Code)
 *      public class CivetWebServices (Thin wrapper adds addresses usernames and passwords from CivetConfig)
 *      		public Document getCivetPremises( String sStatePremID, String sFedPremID, 
 *					String sAddress, String sCity, String sStateCode, String sZipCode, 
 *					String sCounty, String sCountry, String sPhone, String sClassType  ) (Returns DOM of returned XML)
 *         public class UsaHerdsWebServiceLookup (Provides next()/get() Interface and populates cache CSV files)
 * @author mmarti5
 *
 */
public class UsaHerdsWebServiceLookup {
      private static Logger logger = Logger.getLogger();
	static {
	     logger.setLevel(CivetConfig.getLogLevel());
	}
	// Here or in the CivetWebServices class have the list of lookup types as constants.
	public final static String LOOKUP_ERRORS = "CVIErrorTypes"; 
	public final static String LOOKUP_SPECIES = "CVISpeciesTypes"; 
	
	
	private ArrayList<WebServiceLookupRow> rows;
	private int iCurrentRow;
	private WebServiceLookupRow currentRow;

	public UsaHerdsWebServiceLookup( String sLookupType ) throws WebServiceException {
		CivetWebServices service = new CivetWebServices();
		Document doc = service.getCivetLookupDocument(sLookupType);
		if( doc == null ) {
			logger.error("Failed Lookup, null document", new Exception());
			return;
		}
		populateRows(doc);
		iCurrentRow = -1;
	}

	public UsaHerdsWebServiceLookup( Document doc ) {
		if( doc == null ) {
			logger.error("Failed Lookup, null document", new Exception());
			return;
		}
		populateRows(doc);
		iCurrentRow = -1;
	}

	private void populateRows( Document doc ) {
		rows = new ArrayList<WebServiceLookupRow>();
		Node root = XMLUtility.findFirstChildElementByName( doc.getDocumentElement(), "LookupTypes" );
		if( root == null ) {
			logger.error("Failed Lookup, null root", new Exception());
			return;
		}
//System.out.println(XMLUtility.domToString(doc));
		NodeList types = root.getChildNodes();
		for( int i = 0; i < types.getLength(); i++ ) {
			Node type = types.item(i);
			if( type.getNodeType() == Node.ELEMENT_NODE ) {
				NodeList fields = type.getChildNodes();
				Integer iKeyValue = null;
				String sDescription = null;
				String sUSDADescription = null;
				Integer iDisplaySequence = null;
				String sMappedValue = null;
				String sShortName = null;
				for( int j = 0; j < fields.getLength(); j++ ) {
					Node field = fields.item(j);
					if( field.getNodeType() == Node.ELEMENT_NODE ) {
						if( "KeyValue".equals(field.getNodeName() ) ) {
							String sKey = field.getTextContent();
							iKeyValue = Integer.parseInt(sKey);
						}
						else if( "Description".equals(field.getNodeName() ) ) {
							sDescription = field.getTextContent();
						}
						else if( "USDADescription".equals(field.getNodeName() ) ) {
							sUSDADescription = field.getTextContent();
						}
						else if( "DisplaySequence".equals(field.getNodeName() ) ) {
							String sDisplaySequence = field.getTextContent();
							iDisplaySequence = Integer.parseInt(sDisplaySequence);
						}
						else if( "USDACode".equals(field.getNodeName() ) ) {
							sMappedValue = field.getTextContent();
						}
						else if( "USAHACode".equals(field.getNodeName() ) ) {
							sMappedValue = field.getTextContent();
						}
						else if( "ShortName".equals(field.getNodeName() ) ) {
							sShortName = field.getTextContent();
						}
					}
				}
				WebServiceLookupRow row = new WebServiceLookupRow( iKeyValue, sDescription, sUSDADescription,
						                                           iDisplaySequence, sMappedValue, sShortName );
				rows.add(row);
			}
		}
		return;
	}
	
	public boolean next() {
		boolean bRet = false;
		iCurrentRow++;
		if( iCurrentRow < rows.size() && rows.size() > 0 ) {
			currentRow = rows.get(iCurrentRow);
			bRet = true;
		}
		else {
			currentRow = null;
			bRet = false;
		}
		return bRet;
	}
	
	public void reset() {
		iCurrentRow = -1;
	}
	
	public Integer getKeyValue() {
		if( currentRow == null ) return null;
		return currentRow.iKeyValue;
	}
	
	public String getDescription() {
		if( currentRow == null ) return null;
		return currentRow.sDescription;
	}
	
	public String getUSDADescription() {
		if( currentRow == null ) return null;
		return currentRow.sUSDADescription;
	}
	
	public String getMappedValue() {
		if( currentRow == null ) return null;
		return currentRow.sMappedValue;
	}
	
	public String getShortName() {
		if( currentRow == null ) return null;
		return currentRow.sShortName;
	}
	
	public Integer getDisplaySequence() {
		if( currentRow == null ) return null;
		return currentRow.iDisplaySequence;
	}
	
	private static class WebServiceLookupRow {
		public Integer iKeyValue;
		public String sDescription;
		public String sUSDADescription;
		public Integer iDisplaySequence;
		public String sMappedValue;
		public String sShortName;
		public  WebServiceLookupRow( Integer iKey, String sDesc, String sUSDADesc, Integer iDisp, String sMapped, String sShortNm ) {
			iKeyValue = iKey;
			sDescription = sDesc;
			sUSDADescription = sUSDADesc;
			iDisplaySequence = iDisp;
			sMappedValue = sMapped;
			sShortName = sShortNm;
		}
	}

}
