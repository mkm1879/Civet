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
import java.io.StringWriter;
import java.rmi.RemoteException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import com.cai.webservice.ExternalMessagesStub;
import com.cai.webservice.ExternalMessagesStub.AddExternalMessage;
import com.cai.webservice.ExternalMessagesStub.AddExternalMessageResponse;
import com.cai.webservice.ExternalMessagesStub.CivetGetLookup;
import com.cai.webservice.ExternalMessagesStub.CivetGetLookupResponse;
import com.cai.webservice.ExternalMessagesStub.CivetGetPremises;
import com.cai.webservice.ExternalMessagesStub.CivetGetPremisesResponse;
import com.cai.webservice.ExternalMessagesStub.CivetGetVets;
import com.cai.webservice.ExternalMessagesStub.CivetGetVetsResponse;

import org.w3c.dom.Document;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetConfig;
import edu.clemson.lph.utils.Encodings;
import edu.clemson.lph.utils.XMLUtility;

/**
 * Wrapper around generated ExternalMessagesStub produced from CivetExternalMessages.wsdl
 * E:\JavaLib\axis2-1.6.2\bin\wsdl2java -S src -p com.cai.webservice -or -uri CivetExternalMessages.wsdl
 * NOTE: this WSDL file is a heavily edited copy of the one published at
 * http://corp-hbg-04.compaid.com/usaherds/WebService/ExternalMessages.asmx?WSDL
 * Change input parameters from base64Binary to string, then base64 encode manually.  I also removed a large number
 * of message types and methods not used by Civet although that was just to make it easier to read 
 * during implementation.
 * @author mmarti5
 *
 */
public class CivetWebServices {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	static {
		// BasicConfigurator replaced with PropertyConfigurator.
	     PropertyConfigurator.configure("CivetConfig.txt");
	     logger.setLevel(Level.ERROR);
	}
	public static final String CVI_UPLOAD_MESSAGE_TYPE = "Civet XML";
	public static final String CVI_UPLOAD_SOURCE = "Civet";
	public static final String CVI_SUCCESS_MESSAGE = "00,Successful";

	public CivetWebServices() {
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * Insert a CVI in USAHA standard XML format.
	 * @return String message from server.
	 * @throws RemoteException 
	 */
	public String sendCviXML( String sXML ) throws RemoteException {
		String sRet = null;
		String sURL = CivetConfig.getHERDSWebServiceURL();
		ExternalMessagesStub stub = new ExternalMessagesStub(sURL);
		AddExternalMessage proxy = new AddExternalMessage();
		proxy.setUserName(CivetConfig.getHERDSUserName());
		proxy.setPassword(CivetConfig.getHERDSPassword());
		proxy.setMessageType(CVI_UPLOAD_MESSAGE_TYPE);
		proxy.setSource(CVI_UPLOAD_SOURCE);
		String sBase64 = Encodings.getBase64Utf16Le( sXML );
		proxy.setMessageValue(sBase64);
		AddExternalMessageResponse resp = stub.addExternalMessage(proxy);
		sRet = resp.getAddExternalMessageResult();
		return sRet;
	}
	
	/**
	 * Get a named Lookup table as a while( next() ) get...() style list of 
	 * three fields getKeyValue, getDescription, and getDisplaySequence.
	 * @param sLookupType One of the types listed as constants
	 *  LOOKUP_ERRORS 
	 *  LOOKUP_SPECIES 
	 *  LOOKUP_PURPOSES 
	 * @return UsaHerdsWebServiceLookup list.
	 */
	public UsaHerdsWebServiceLookup getCivetLookup( String sLookupType ) throws WebServiceException {
		return new UsaHerdsWebServiceLookup( getCivetLookupDocument( sLookupType ) );
	}

	/**
	 * Get a named Lookup table
	 * @param sLookupType One of the types listed as constants
	 *  LOOKUP_ERRORS 
	 *  LOOKUP_SPECIES 
	 *  LOOKUP_PURPOSES 
	 * @return XML DOM Document object with list of values.
	 */
	public Document getCivetLookupDocument( String sLookupType ) throws WebServiceException {
		Document dOut = null;
		String sURL = null;
		try {
			sURL = CivetConfig.getHERDSWebServiceURL();
			ExternalMessagesStub stub = new ExternalMessagesStub(sURL);
			javax.xml.namespace.QName parentElement = new javax.xml.namespace.QName("urn:civet","civet");
			CivetGetLookup proxy = new CivetGetLookup();
			proxy.setUn(CivetConfig.getHERDSUserName());
			proxy.setPw(CivetConfig.getHERDSPassword());
			proxy.setLookupType(sLookupType);
			CivetGetLookupResponse resp = stub.civetGetLookup(proxy);
			// How to get raw XML result
			XMLOutputFactory output = XMLOutputFactory.newInstance();
			StringWriter swOut = new StringWriter();
			XMLStreamWriter writer = output.createXMLStreamWriter( swOut );
			resp.serialize(parentElement, writer, false);
			writer.flush();
			String sOut = swOut.toString();
			// Debugging output to verify webservice data
//			System.out.println(sOut);
			dOut = XMLUtility.stringToDom(sOut);
			writer.close();
		} catch (Exception e) {
			throw new WebServiceException(e);
		}
		return dOut;
	}

	public static boolean validUSAHERDSCredentials( String sUserName, String sPassword ) throws WebServiceException {
		boolean bRet = true;
		String sURL = null;
		try {
			sURL = CivetConfig.getHERDSWebServiceURL();
			ExternalMessagesStub stub = new ExternalMessagesStub(sURL);
			javax.xml.namespace.QName parentElement = new javax.xml.namespace.QName("urn:civet","civet");
			CivetGetLookup proxy = new CivetGetLookup();
			proxy.setUn(sUserName);
			proxy.setPw(sPassword);
			proxy.setLookupType(UsaHerdsWebServiceLookup.LOOKUP_ERRORS);
			CivetGetLookupResponse resp = stub.civetGetLookup(proxy);
			// How to get raw XML result
			XMLOutputFactory output = XMLOutputFactory.newInstance();
			StringWriter swOut = new StringWriter();
			XMLStreamWriter writer = output.createXMLStreamWriter( swOut );
			resp.serialize(parentElement, writer, false);
			writer.flush();
			String sOut = swOut.toString();
//			System.out.println(sOut);
			if( sOut.contains("Invalid Username/Password"))
				bRet = false;
			writer.close();
		} catch (Exception e) {
			throw new WebServiceException(e);
		}
		return bRet;
	}
	
	/**
	 * Lookup premises based on various parameters
	 * @param sPhone
	 * @param sStateCode
	 * @param sPremId
	 * @return DOM Document of Premises List
	 */
	public Document getCivetPremises( String sStatePremID, String sFedPremID, 
			String sAddress, String sCity, String sStateCode, String sZipCode, 
			String sCounty, String sCountry, String sPhone, String sClassType  ) throws WebServiceException {
		Document dOut = null;
		String sURL = null;
		try {
			sURL = CivetConfig.getHERDSWebServiceURL();
			ExternalMessagesStub stub = new ExternalMessagesStub(sURL);
			javax.xml.namespace.QName parentElement = new javax.xml.namespace.QName("urn:civet","civet");
			CivetGetPremises proxy = new CivetGetPremises();
			proxy.setUn(CivetConfig.getHERDSUserName());
			proxy.setPw(CivetConfig.getHERDSPassword());
			StringBuffer sbParms = new StringBuffer();
			sbParms.append("<parms>");
			if( sStatePremID != null )
				sbParms.append("<premId>" + sStatePremID + "</premId>");
			else if( sFedPremID != null )
				sbParms.append("<premId>" + sFedPremID + "</premId>");
			if( sAddress != null )
				sbParms.append("<address>" + sAddress + "</address>");
			if( sCity != null )
				sbParms.append("<city>" + sCity + "</city>");
			if( sStateCode != null )
				sbParms.append("<stateCode>" + sStateCode + "</stateCode>");
			if( sZipCode != null )
				sbParms.append("<zipCode>" + sZipCode + "</zipCode>");
			if( sCounty != null )
				sbParms.append("<county>" + sCounty + "</county>");
			if( sCountry != null )
				sbParms.append("<country>" + sCountry + "</country>");
			if( sPhone != null )
				sbParms.append("<phone>" + sPhone + "</phone>");
			if( sClassType != null )
				sbParms.append("<classType>" + sClassType + "</classType>");
			sbParms.append("</parms>");
//			System.out.println(sbParms.toString());
			String sBase64 = Encodings.getBase64Utf16Le( sbParms.toString() );
			proxy.setParms(sBase64);
//			System.out.println(proxy.getParms());
//			System.out.println(new String(Base64.decodeBase64(proxy.getParms()),"UTF-16LE"));
			CivetGetPremisesResponse resp = stub.civetGetPremises(proxy);
			// How to get raw XML result
			XMLOutputFactory output = XMLOutputFactory.newInstance();
			StringWriter swOut = new StringWriter();
			XMLStreamWriter writer = output.createXMLStreamWriter( swOut );
			resp.serialize(parentElement, writer, false);
			writer.flush();
			String sOut = swOut.toString();
//			System.out.println(sOut);
			dOut = XMLUtility.stringToDom(sOut);
			writer.close();
		} catch (Exception e) {
			throw new WebServiceException(e);
		}
		return dOut;
	}
	
	/**
	 * Under development.  I'm not sure all these parameters are allowed.
	 * @param sLastName
	 * @param sFirstName
	 * @param sAddress
	 * @param sCity
	 * @param sStateCode
	 * @param sZipCode
	 * @param sPhone
	 * @param sNan
	 * @param sLicNbr
	 * @return
	 */
	public Document getCivetVets( String sLastName, String sFirstName, 
			String sAddress, String sCity, String sStateCode, String sZipCode, 
			String sPhone, String sNan, String sLicNbr) throws WebServiceException {
		Document dOut = null;
		String sURL = null;
		try {
			sURL = CivetConfig.getHERDSWebServiceURL();
			ExternalMessagesStub stub = new ExternalMessagesStub(sURL);
			javax.xml.namespace.QName parentElement = new javax.xml.namespace.QName("urn:civet","civet");
			CivetGetVets proxy = new CivetGetVets();
			proxy.setUn(CivetConfig.getHERDSUserName());
			proxy.setPw(CivetConfig.getHERDSPassword());
			StringBuffer sbParms = new StringBuffer();
			sbParms.append("<parms>");
			if( sFirstName != null )
				sbParms.append("<firstName>" + sFirstName + "</firstName>");
			if( sLastName != null )
				sbParms.append("<lastName>" + sLastName + "</lastName>");
			if( sAddress != null )
				sbParms.append("<address1>" + sAddress + "</address1>");
			if( sCity != null )
				sbParms.append("<city>" + sCity + "</city>");
			if( sStateCode != null )
				sbParms.append("<stateCode>" + sStateCode + "</stateCode>");
			if( sZipCode != null )
				sbParms.append("<zipCode>" + sZipCode + "</zipCode>");
			if( sPhone != null )
				sbParms.append("<phone>" + sPhone + "</phone>");
			if( sNan != null )
				sbParms.append("<nan>" + sNan + "</nan>");
			if( sLicNbr != null )
				sbParms.append("<lic>" + sLicNbr + "</lic>");
			
			sbParms.append("<accreditedOnly>0</accreditedOnly>");
			sbParms.append("</parms>");
			String sBase64 = Encodings.getBase64Utf16Le( sbParms.toString() );
			proxy.setParms(sBase64);
//			System.out.println(proxy.getParms());
//			System.out.println(new String(Base64.decodeBase64(proxy.getParms()),"UTF-16LE"));
			CivetGetVetsResponse resp = stub.civetGetVets(proxy);
			// How to get raw XML result
			XMLOutputFactory output = XMLOutputFactory.newInstance();
			StringWriter swOut = new StringWriter();
			XMLStreamWriter writer = output.createXMLStreamWriter( swOut );
			resp.serialize(parentElement, writer, false);
			writer.flush();
			String sOut = swOut.toString();
//			System.err.println(sOut);
			dOut = XMLUtility.stringToDom(sOut);
//			System.out.println(XMLUtility.printElements(dOut));

			writer.close();
		} catch (Exception e) {
			throw new WebServiceException(e);
		}
		return dOut;
		
	}

	/**
	 * Lookup vets based on various parameters
	 * @param sFirstName
	 * @param sLastName
	 * @param sPhone
	 * @param sStateCode
	 * @return DOM Document of Vet List
	 * TODO do I use this?
	 */
	public Document getCivetVets( String sFirstName, String sLastName, String sPhone, 
			String sStateCode, boolean bAccredOnly  ) throws WebServiceException {
		Document dOut = null;
		String sURL = null;
		try {
			sURL = CivetConfig.getHERDSWebServiceURL();
			ExternalMessagesStub stub = new ExternalMessagesStub(sURL);
			javax.xml.namespace.QName parentElement = new javax.xml.namespace.QName("urn:civet","civet");
			CivetGetVets proxy = new CivetGetVets();
			proxy.setUn(CivetConfig.getHERDSUserName());
			proxy.setPw(CivetConfig.getHERDSPassword());
			StringBuffer sbParms = new StringBuffer();
			sbParms.append("<parms>");
			if( sFirstName != null )
				sbParms.append("<firstName>" + sFirstName + "</firstName>");
			if( sLastName != null )
				sbParms.append("<lastName>" + sLastName + "</lastName>");
			if( sPhone != null )
				sbParms.append("<phone>" + sPhone + "</phone>");
			if( sStateCode != null )
				sbParms.append("<stateCode>" + sStateCode + "</stateCode>");
			if( bAccredOnly )
				sbParms.append("<accreditedOnly>1</accreditedOnly>");
			else
				sbParms.append("<accreditedOnly>0</accreditedOnly>");
			sbParms.append("</parms>");
			String sBase64 = Encodings.getBase64Utf16Le( sbParms.toString() );
			proxy.setParms(sBase64);
			CivetGetVetsResponse resp = stub.civetGetVets(proxy);
			// How to get raw XML result
			XMLOutputFactory output = XMLOutputFactory.newInstance();
			StringWriter swOut = new StringWriter();
			XMLStreamWriter writer = output.createXMLStreamWriter( swOut );
			resp.serialize(parentElement, writer, false);
			writer.flush();
			String sOut = swOut.toString();
//			System.out.println(sOut);
			dOut = XMLUtility.stringToDom(sOut);
//			System.out.println(XMLUtility.printElements(dOut));

			writer.close();
		} catch (Exception e) {
			throw new WebServiceException(e);
		}
		return dOut;
	}

}
