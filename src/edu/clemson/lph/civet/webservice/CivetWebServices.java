package edu.clemson.lph.civet.webservice;
/*
Copyright 2014-16 Michael K Martin

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
import edu.clemson.lph.civet.webservice.HttpGetClient;
import edu.clemson.lph.dialogs.MessageDialog;
import java.rmi.RemoteException;
import org.w3c.dom.Document;
import org.apache.log4j.Logger;
import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;
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
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	static {
	     logger.setLevel(CivetConfig.getLogLevel());
	}
	private static final String CVI_UPLOAD_MESSAGE_TYPE = "Civet XML";
	private static final String CVI_UPLOAD_SOURCE = "Civet";
	private String CVI_SUCCESS_MESSAGE = "Success";
	private static final String LOOKUPS_URL = "/api/Civet/GetLookups/";
	private static final String POST_PREM_URL = "/api/Civet/SearchPremises";
	private static final String POST_VETS_URL = "/api/CIVET/SearchVets";
	private static final String POST_XML_URL = "/api/AddExternalMessages";
	public CivetWebServices() {
		// TODO Auto-generated constructor stub
	}

	
	/* (non-Javadoc)
	 * @see edu.clemson.lph.civet.webservice.CivetWebServices#sendCviXML(java.lang.String)
	 */
	
	public String sendCviXML( String sXML ) throws RemoteException, WebServiceException {
		String sRet = null;
		String sURL = null;
		sURL = CivetConfig.getHERDSWebServiceURL() + POST_XML_URL;
		HttpPostClient postXML = new HttpPostClient();
		postXML.authenticate();
		postXML.addParameter("MessageType", CVI_UPLOAD_MESSAGE_TYPE);
		postXML.addParameter("Source", CVI_UPLOAD_SOURCE );
		postXML.addParameter("Message", sXML);
		if( postXML.getURL(sURL) ) {
			//				System.out.println(postXML.getBody());
			sRet = postXML.getBody();
		}
		else {
			sRet = postXML.getError();
			logger.error("Error in Add External Message " + sURL + '\n' + sRet);
			throw new RemoteException(sRet);
		}

		return sRet;
	}
	
	
	public String getSuccessMessage() {
		return CVI_SUCCESS_MESSAGE;
	}
	
	/* (non-Javadoc)
	 * @see edu.clemson.lph.civet.webservice.CivetWebServices#getCivetLookup(java.lang.String)
	 */
	
	public UsaHerdsWebServiceLookup getCivetLookup( String sLookupType ) throws WebServiceException {
		return new UsaHerdsWebServiceLookup( getCivetLookupDocument( sLookupType ) );
	}

	/* (non-Javadoc)
	 * @see edu.clemson.lph.civet.webservice.CivetWebServices#getCivetLookupDocument(java.lang.String)
	 */
	
	public Document getCivetLookupDocument( String sLookupType ) throws WebServiceException {
		Document dOut = null;
		String sBaseURL = CivetConfig.getHERDSWebServiceURL();
		String sURL = sBaseURL + LOOKUPS_URL + sLookupType;
		try {
			String sOut = null;
			HttpGetClient purpose = new HttpGetClient();
			purpose.authenticate();
			if( purpose.getURL(sURL) ) {
//				System.out.println(purpose.getBody());
				sOut = purpose.getBody();
			}
			else {
				logger.error("Error in Lookup " + sURL + '\n' + purpose.getError());
			}
			dOut = XMLUtility.stringToDom(sOut);
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new WebServiceException(e);
		}
		return dOut;
	}

	public boolean validUSAHERDSCredentials( String sUserName, String sPassword ) throws WebServiceException {
		boolean bRet = true;
		String sURL = CivetConfig.getHERDSWebServiceURL();
		String sToken = UsaHerdsWebServiceAuthentication.getToken(sURL, sUserName, sPassword);
		bRet = (sToken != null);
		if( !bRet )
			MessageDialog.showMessage(null, "Civet: USAHERDS Password Error", "Invalid Username/Password");
		return bRet;
	}
	
	/* (non-Javadoc)
	 * @see edu.clemson.lph.civet.webservice.CivetWebServices#getCivetPremises(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	
	public Document getCivetPremises( String sStatePremID, String sFedPremID, 
			String sAddress, String sCity, String sStateCode, String sZipCode, 
			String sCounty, String sCountry, String sPhone, String sClassType  ) throws WebServiceException {
		Document dOut = null;
		String sURL = null;
		try {
			sURL = CivetConfig.getHERDSWebServiceURL() + POST_PREM_URL;
			HttpPostClient premSearch = new HttpPostClient();
			premSearch.authenticate();
			String sOut = null;
			if( sStatePremID != null )
				premSearch.addParameter("premId",  sStatePremID);
			else if( sFedPremID != null )
				premSearch.addParameter("premId", sFedPremID);
			if( sAddress != null )
				premSearch.addParameter("address", sAddress);
			if( sCity != null )
				premSearch.addParameter("city",sCity);
			if( sStateCode != null )
				premSearch.addParameter("stateCode", sStateCode);
			if( sZipCode != null )
				premSearch.addParameter("zipCode",sZipCode );
			if( sCounty != null )
				premSearch.addParameter("county",sCounty);
			if( sCountry != null )
				premSearch.addParameter("country",sCountry);
			if( sPhone != null )
				premSearch.addParameter("phone", sPhone);
			if( sClassType != null )
				premSearch.addParameter("classType", sClassType);
			if( premSearch.getURL(sURL) ) {
//				System.out.println(premSearch.getBody());
				sOut = premSearch.getBody();
			}
			else {
				logger.error("Error in Lookup Premises " + sURL + '\n' + premSearch.getError());
			}
			dOut = XMLUtility.stringToDom(sOut);
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new WebServiceException(e);
		}
		return dOut;
	}
	
	/* (non-Javadoc)
	 * @see edu.clemson.lph.civet.webservice.CivetWebServices#getCivetVets(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	
	public Document getCivetVets( String sLastName, String sFirstName, 
			String sAddress, String sCity, String sStateCode, String sZipCode, 
			String sPhone, String sNan, String sLicNbr) throws WebServiceException {
		Document dOut = null;
		String sURL = null;
		try {
			sURL = CivetConfig.getHERDSWebServiceURL() + POST_VETS_URL;
			HttpPostClient vetSearch = new HttpPostClient();
			vetSearch.authenticate();
			String sOut = null;
			if( sFirstName != null )
				vetSearch.addParameter("firstName", sFirstName);
			if( sLastName != null )
				vetSearch.addParameter("lastName", sLastName);
			if( sAddress != null )
				vetSearch.addParameter("address1", sAddress);
			if( sCity != null )
				vetSearch.addParameter("city", sCity);
			if( sStateCode != null )
				vetSearch.addParameter("stateCode", sStateCode);
			if( sZipCode != null )
				vetSearch.addParameter("zipCode", sZipCode);
			if( sPhone != null )
				vetSearch.addParameter("phone", sPhone);
			if( sNan != null )
				vetSearch.addParameter("nan", sNan);
			if( sLicNbr != null )
				vetSearch.addParameter("lic", sLicNbr);
			vetSearch.addParameter("accreditedOnly", "false");
			if( vetSearch.getURL(sURL) ) {
//				System.out.println(vetSearch.getBody());
				sOut = vetSearch.getBody();
			}
			else {
				logger.error("Error in Lookup Premises " + sURL + '\n' + vetSearch.getError());
			}
			dOut = XMLUtility.stringToDom(sOut);
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new WebServiceException(e);
		}
		return dOut;
		
	}

	/* (non-Javadoc)
	 * @see edu.clemson.lph.civet.webservice.CivetWebServices#getCivetVets(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	
	public Document getCivetVets( String sFirstName, String sLastName, String sPhone, 
			String sStateCode, boolean bAccredOnly  ) throws WebServiceException {
		Document dOut = null;
		String sURL = null;
		try {
			sURL = CivetConfig.getHERDSWebServiceURL() + POST_VETS_URL;
			HttpPostClient vetSearch = new HttpPostClient();
			vetSearch.authenticate();
			String sOut = null;
			if( sFirstName != null )
				vetSearch.addParameter("firstName", sFirstName);
			if( sLastName != null )
				vetSearch.addParameter("lastName", sLastName);
			if( sPhone != null )
				vetSearch.addParameter("phone", sPhone);
			if( sStateCode != null )
				vetSearch.addParameter("stateCode", sStateCode );
			if( bAccredOnly )
				vetSearch.addParameter("accreditedOnly", "true");
			else
				vetSearch.addParameter("accreditedOnly", "false");
			if( vetSearch.getURL(sURL) ) {
//				System.out.println(vetSearch.getBody());
				sOut = vetSearch.getBody();
			}
			else {
				logger.error("Error in Lookup Vets\n" + sURL + '\n' + vetSearch.getError());
			}
			dOut = XMLUtility.stringToDom(sOut);
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new WebServiceException(e);
		}
		return dOut;
	}

}
