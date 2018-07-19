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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.utils.CSVWriter;
import edu.clemson.lph.utils.LabeledCSVParser;
import edu.clemson.lph.utils.XMLUtility;

/**
 * Outer wrapper for pulling veterinarian list from USAHERDS into CSV lookup table 
 * TODO Modify columns and parameters as the definition of the vet list is updated.
 * TODO Renormalize the data by combining rows with the same key pulling in the various 
 * NVAP and State License data.
 * Logic Stack
 * WSDL At: http://corp-hbg-04.compaid.com/USAHERDS/webservice/externalmessages.asmx (edited)
 * Generate Apache Axis2 client code for ADB binding. 
 * 		E:\JavaLib\axis2-1.6.2\bin\wsdl2java -S src -p com.cai.webservice -or -uri CivetExternalMessages.wsdl
 * 	 public class ExternalMessagesStub (Generated Code)
 *      public class CivetWebServices (Thin wrapper adds addresses usernames and passwords from CivetConfig)
 *      		public Document getCivetPremises( String sStatePremID, String sFedPremID, 
 *					String sAddress, String sCity, String sStateCode, String sZipCode, 
 *					String sCounty, String sCountry, String sPhone, String sClassType  ) (Returns DOM of returned XML)
 *         public class UsaHerdsLookupVets (Provides next()/get() Interface and populates cache CSV file)
 * @author mmarti5
 *
 */
public class UsaHerdsLookupVets {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	static {
		// BasicConfigurator replaced with PropertyConfigurator.
	     PropertyConfigurator.configure("CivetConfig.txt");
	     logger.setLevel(Level.ERROR);
	}
	// Choice of type for full list.
	public final static int NVAP_LEVEL2 = 2; 
	public final static int NVAP_ANY = 1; 
	public final static int HERDS_ANY = 0; 
	
	
	private ArrayList<WebServiceVetRow> rows;
	private int iCurrentRow;
	private WebServiceVetRow currentRow;

	/**
	 * Get a complete list of vets at the specified accreditation level
	 * @param iFilterType one of the three enumerated values 
	 * 	NVAP_LEVEL2 = 2; 
	 * 	NVAP_ANY = 1; 
	 * 	HERDS_ANY = 0; 
	 * @throws WebServiceException 
	 */
	public UsaHerdsLookupVets( boolean bAccredOnly ) throws WebServiceException {
		String sVetFile = CivetConfig.getVetTableFile();
		CivetWebServices service = CivetWebServiceFactory.getService();
		Document doc = service.getCivetVets( null, null, null, null, bAccredOnly);
		if( doc != null )
			populateRows(doc);
		else
			populateRows(sVetFile);  //"VetTable.csv");
		iCurrentRow = -1;
	}
	
	public UsaHerdsLookupVets(String sLastName, String sFirstName, 
			String sAddress, String sCity, String sStateCode, String sZipCode, 
			String sPhone, String sNan, String sLicNbr) throws WebServiceException {
		CivetWebServices service = CivetWebServiceFactory.getService();
		Document doc = service.getCivetVets(sLastName, sFirstName, 
				sAddress, sCity, sStateCode, sZipCode, 
				sPhone, sNan, sLicNbr );
		populateRows(doc);
		iCurrentRow = -1;
		
	}

	/**
	 * Construct from an already run search
	 * @param doc
	 */
	public UsaHerdsLookupVets( Document doc ) {
		populateRows(doc);
		iCurrentRow = -1;
	}
	
	public void clear() {
		rows.clear();
		iCurrentRow = -1;
	}
	
	public void generateLookupTable(String sFileName) {
		String[] aColNames = {"VetKey","FormattedName", "FirstName","LastName","Address","City","State","ZipCode","Phone",
				              "LicNo","NAN","NANLevel","NANStatus"};

		CSVWriter writer = new CSVWriter();
		try {
			writer.setHeader(aColNames);
			if( first() ) {
				while( next() ) {
					ArrayList<Object> aRow = new ArrayList<Object>();
					aRow.add(getKeyValue());
					aRow.add(getFormattedName());
					aRow.add(getFirstName());
					aRow.add(getLastName());
					aRow.add(getAddress1());
					aRow.add(getCity());
					aRow.add(getStateCode());
					aRow.add(getZipCode());
					aRow.add(getPhone());
					aRow.add(getStateLicense());
					aRow.add(getNan());
					aRow.add(getNanLevel());
					aRow.add(getNanStatus());
					writer.addRow(aRow);
				}
				writer.write(sFileName);
			}
		} catch (DataFormatException e) {
			logger.error("Format error in header of " + sFileName, e);
		} catch (FileNotFoundException e) {
			logger.error("Could not find file " + sFileName, e);
		}

	}
	
	/**
	 * Replace " with ' in returned text so it doesn't foul up quoting in CSV
	 * @param field
	 * @return
	 */
	private String getEscapedText( Node field ) {
		String sContent = field.getTextContent();
		if( sContent != null )
			sContent = sContent.replace('\"', '\'');
		return sContent;
	}
	
	private void populateRows( Document doc ) {
		rows = new ArrayList<WebServiceVetRow>();
		Node root = XMLUtility.findFirstChildElementByName( doc.getDocumentElement(), "vetList" );
		NodeList types = root.getChildNodes();
		for( int i = 0; i < types.getLength(); i++ ) {
			Node vet = types.item(i);
			if( vet.getNodeType() == Node.ELEMENT_NODE ) {
				NamedNodeMap vetAttrs = vet.getAttributes();
				Node nKey = vetAttrs.getNamedItem("vetkey");
				Integer iKeyValue = null;
				if( nKey != null ) {
					String sKey = nKey.getNodeValue();
					iKeyValue = Integer.parseInt(sKey);
				}
			
				NodeList fields = vet.getChildNodes();
				String sFormattedName = null;
				String sFirstName = null;
				String sLastName = null;
				String sAddress1 = null;
				String sAddress2 = null;
				String sCity = null;
				String sStateCode = null;
				String sZipCode = null;
				String sCounty = null;
				String sCountry = null;
				String sPhone = null;
				String sFax = null;
				String sStateLicense = null;
				String sNan = null;
				String sNanLevel = null;
				String sNanStatus = null;
				for( int j = 0; j < fields.getLength(); j++ ) {
					Node field = fields.item(j);
					if( field.getNodeType() == Node.ELEMENT_NODE ) {
						if( "formattedName".equals(field.getNodeName() ) )
							sFormattedName = getEscapedText( field );
						else if( "firstName".equals(field.getNodeName() ) )
							sFirstName = getEscapedText( field );
						else if( "lastName".equals(field.getNodeName() ) )
							sLastName = getEscapedText( field );
						else if( "address1".equals(field.getNodeName() ) )
							sAddress1 = getEscapedText( field );
						else if( "address2".equals(field.getNodeName() ) )
							sAddress2 = getEscapedText( field );
						else if( "city".equals(field.getNodeName() ) )
							sCity = getEscapedText( field );
						else if( "stateCode".equals(field.getNodeName() ) )
							sStateCode = getEscapedText( field );
						else if( "zipCode".equals(field.getNodeName() ) )
							sZipCode = getEscapedText( field );
						else if( "county".equals(field.getNodeName() ) )
							sCounty = getEscapedText( field );
						else if( "country".equals(field.getNodeName() ) )
							sCountry = getEscapedText( field );
						else if( "formattedPhone".equals(field.getNodeName() ) ) {
							sPhone = getEscapedText( field );
							sPhone = sPhone.replace('\n', ' ');
						}

						else if( "fax".equals(field.getNodeName() ) )
							sFax = field.getTextContent();
						else if( "certs".equals(field.getNodeName() ) ) {
							NodeList certs = field.getChildNodes();
							for( int k = 0; k < certs.getLength(); k++ ) {
								Node cert = certs.item(k);
								String sCertType = null;
								String sCertNbr = null;
								String status = null;
								if( cert.getNodeType() == Node.ELEMENT_NODE && "cert".equals(cert.getNodeName() ) ) {
									NodeList certFields = cert.getChildNodes();
									for( int m = 0; m < certFields.getLength(); m++ ) {
										Node certField = certFields.item(m);
										if( certField.getNodeType() == Node.ELEMENT_NODE ) {
											if( "certType".equals(certField.getNodeName() ) )
												sCertType = certField.getTextContent();
											else if( "certNbr".equals(certField.getNodeName() ) )
												sCertNbr = certField.getTextContent();
											else if( "status".equals(certField.getNodeName() ) )
												status = certField.getTextContent();
										}
									}
									if( "State License".equals(sCertType) && "Active".equals(status) ) {
										sStateLicense = sCertNbr;
									}
									if( "USDA Level II Accreditation".equals(sCertType) 
											&& ( "Active".equals(status) || sNan == null ) ) {
										sNan = sCertNbr;
										sNanLevel = "2";
										sNanStatus = status;
									}
									// Don't overwrite active level 2 with level 1
									if( "USDA Level I Accreditation".equals(sCertType) 
											&& ( sNan == null || ( "Active".equals(status) && "Inactive".equals(sNanStatus) ) ) ) {
										sNan = sCertNbr;
										sNanLevel = "1";
										sNanStatus = status;
									}
								}
							}
						}
					}
				}
				WebServiceVetRow row = new WebServiceVetRow(iKeyValue, sFormattedName, sFirstName, sLastName, 
																  sAddress1, sAddress2, sCity, sStateCode, sZipCode, 
																  sCounty, sCountry, sPhone, sFax,
																  sStateLicense, sNan, sNanLevel, sNanStatus );
				rows.add(row);
			}
		}
	}
	
	/**
	 * Hope this can be temporary
	 * @param sFileName
	 */
	private void populateRows( String sFileName ) {
		rows = new ArrayList<WebServiceVetRow>();
		try {
			LabeledCSVParser parser = new LabeledCSVParser( sFileName );
			int i = 0;
			do {
				try {
				WebServiceVetRow row = new WebServiceVetRow(
						Integer.parseInt(parser.getValue("VetKey")),
						parser.getValue("FormattedName"),
						parser.getValue("FirstName"),
						parser.getValue("LastName"),
						parser.getValue("Address"), "",
						parser.getValue("City"),
						parser.getValue("State"),
						parser.getValue("ZipCode"), "", "", 
						parser.getValue("Phone"), "",
						parser.getValue("LicNo"),
						parser.getValue("NAN"),
						parser.getValue("NANLevel"),
						parser.getValue("NANStatus") );
				rows.add(row);
				if( i++ < 2 ) {
					for(String s : parser.getCurrent() ) {
						System.out.print(s + ", ");
					}
				}
				} catch( NumberFormatException nfe ) {
					logger.error("Bad key " + parser.getValue("VetKey") + " " + parser.getValue("FormattedName"));
					// Press on
				}
			} while( parser.getNext() != null );
			parser.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
		
	}

	
	public boolean first() {
		if( rows == null || rows.size() == 0 ) return false;
		iCurrentRow = -1;
		return true;
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
	
	public Integer getKeyValue() {
		if( currentRow == null ) return null;
		return currentRow.iKeyValue;
	}

	public String getFormattedName() {
		if( currentRow == null) return null;
		return currentRow.sFormattedName;
	}
	
	public String getFirstName() {
		if( currentRow == null) return null;
		return currentRow.sFirstName;
	}
	
	public String getLastName() {
		if( currentRow == null) return null;
		return currentRow.sLastName;
	}
	
	public String getAddress1() {
		if( currentRow == null) return null;
		return currentRow.sAddress1;
	}
	
	public String getAddress2() {
		if( currentRow == null) return null;
		return currentRow.sAddress2;
	}
	
	public String getCity() {
		if( currentRow == null) return null;
		return currentRow.sCity;
	}
	
	public String getStateCode() {
		if( currentRow == null) return null;
		return currentRow.sStateCode;
	}
	
	public String getZipCode() {
		if( currentRow == null) return null;
		return currentRow.sZipCode;
	}
	
	public String getCounty() {
		if( currentRow == null) return null;
		return currentRow.sCounty;
	}
	
	public String getCountry() {
		if( currentRow == null) return null;
		return currentRow.sCountry;
	}
	
	public String getPhone() {
		if( currentRow == null) return null;
		return currentRow.sPhone;
	}
	
	public String getFax() {
		if( currentRow == null) return null;
		return currentRow.sFax;
	}
	
	public String getStateLicense() {
		if( currentRow == null) return null;
		return currentRow.sStateLicense;
	}
	public String getNan() {
		if( currentRow == null) return null;
		return currentRow.sNan;
	}
	public String getNanLevel() {
		if( currentRow == null) return null;
		return currentRow.sNanLevel;
	}
	public String getNanStatus() {
		if( currentRow == null) return null;
		return currentRow.sNanStatus;
	}


	private static class WebServiceVetRow {
		public Integer iKeyValue;
		public String sFormattedName;
		public String sFirstName;
		public String sLastName;
		public String sAddress1;
		public String sAddress2;
		public String sCity;
		public String sStateCode;
		public String sZipCode;
		public String sCounty;
		public String sCountry;
		public String sPhone;
		public String sFax;
		public String sStateLicense;
		public String sNan;
		public String sNanLevel;
		public String sNanStatus;
		
		public  WebServiceVetRow( Integer iKeyValue, String sFormattedName, String sFirstName, String sLastName, 
				                     String sAddress1, String sAddress2, String sCity, String sStateCode, String sZipCode, 
				                     String sCounty, String sCountry, String sPhone, String sFax,
				                     String sStateLicense, String sNan, String sNanLevel, String sNanStatus ) {
			this.iKeyValue = iKeyValue;
			this.sFormattedName = sFormattedName;
			this.sFirstName = sFirstName;
			this.sLastName = sLastName;
			this.sAddress1 = sAddress1;
			this.sAddress2 = sAddress2;
			this.sCity = sCity;
			this.sStateCode = sStateCode;
			this.sZipCode = sZipCode;
			this.sCounty = sCounty;
			this.sCountry = sCountry;
			this.sPhone = sPhone;
			this.sFax = sFax;
			this.sStateLicense = sStateLicense;
			this.sNan = sNan;
			this.sNanLevel = sNanLevel;
			this.sNanStatus = sNanStatus;
		}
	}
	
}

