package edu.clemson.lph.civet.webservice;

import java.rmi.RemoteException;

import org.w3c.dom.Document;

public interface CivetWebServices {
	public static final String CVI_UPLOAD_MESSAGE_TYPE = "Civet XML";
	public static final String CVI_UPLOAD_SOURCE = "Civet";

	/**
	 * Insert a CVI in USAHA standard XML format.
	 * @return String message from server.
	 * @throws RemoteException 
	 */
	public abstract String sendCviXML(String sXML) throws RemoteException;
	
	public abstract String getSuccessMessage();

	/**
	 * Get a named Lookup table as a while( next() ) get...() style list of 
	 * three fields getKeyValue, getDescription, and getDisplaySequence.
	 * @param sLookupType One of the types listed as constants
	 *  LOOKUP_ERRORS 
	 *  LOOKUP_SPECIES 
	 *  LOOKUP_PURPOSES 
	 * @return UsaHerdsWebServiceLookup list.
	 */
	public abstract UsaHerdsWebServiceLookup getCivetLookup(String sLookupType)
			throws WebServiceException;

	/**
	 * Get a named Lookup table
	 * @param sLookupType One of the types listed as constants
	 *  LOOKUP_ERRORS 
	 *  LOOKUP_SPECIES 
	 *  LOOKUP_PURPOSES 
	 * @return XML DOM Document object with list of values.
	 */
	public abstract Document getCivetLookupDocument(String sLookupType)
			throws WebServiceException;

	/**
	 * Lookup premises based on various parameters
	 * @param sPhone
	 * @param sStateCode
	 * @param sPremId
	 * @return DOM Document of Premises List
	 */
	public abstract Document getCivetPremises(String sStatePremID,
			String sFedPremID, String sAddress, String sCity,
			String sStateCode, String sZipCode, String sCounty,
			String sCountry, String sPhone, String sClassType)
			throws WebServiceException;

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
	public abstract Document getCivetVets(String sLastName, String sFirstName,
			String sAddress, String sCity, String sStateCode, String sZipCode,
			String sPhone, String sNan, String sLicNbr)
			throws WebServiceException;

	/**
	 * Lookup vets based on various parameters
	 * @param sFirstName
	 * @param sLastName
	 * @param sPhone
	 * @param sStateCode
	 * @return DOM Document of Vet List
	 * TODO do I use this?
	 */
	public abstract Document getCivetVets(String sFirstName, String sLastName,
			String sPhone, String sStateCode, boolean bAccredOnly)
			throws WebServiceException;

}