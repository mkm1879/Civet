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

import javax.swing.event.TableModelListener;


import edu.clemson.lph.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.utils.XMLUtility;

/**
 * Outer wrapper for pulling veterinarian list from USAHERDS into CSV lookup table 
 * TODO Make convenience constructors for specific instances.
 * TODO Implement DBTableModel interface
 * Logic Stack
 * WSDL At: http://corp-hbg-04.compaid.com/USAHERDS/webservice/externalmessages.asmx (edited)
 * Generate Apache Axis2 client code for ADB binding. 
 * 		E:\JavaLib\axis2-1.6.2\bin\wsdl2java -S src -p com.cai.webservice -or -uri CivetExternalMessages.wsdl
 * 	 public class ExternalMessagesStub (Generated Code)
 *      public class CivetWebServices (Thin wrapper adds addresses usernames and passwords from CivetConfig)
 *      		public Document getCivetPremises( String sStatePremID, String sFedPremID, 
 *					String sAddress, String sCity, String sStateCode, String sZipCode, 
 *					String sCounty, String sCountry, String sPhone, String sClassType  ) (Returns DOM of returned XML)
 *         public class UsaHerdsLookupPrems (Provides next()/get() Interface and implements DBTableModel)
 * @author mmarti5
 *
 */
public class UsaHerdsLookupPrems implements javax.swing.table.TableModel, PremisesTableModel {
      private static Logger logger = Logger.getLogger();
	
	private ArrayList<WebServicePremisesRow> rows;
	private int iCurrentRow;
	private WebServicePremisesRow currentRow;
	
	/**
	 * Table Model member variables
	 */
	private ArrayList<TableModelListener> listeners = new ArrayList<TableModelListener>();

	/**
	 * Really test only
	 * TODO Update Parameter list, etc.
	 * @throws WebServiceException 
	 */
	public UsaHerdsLookupPrems() throws WebServiceException {
		CivetWebServices service = new CivetWebServices();
		Document doc = service.getCivetPremises(null, null, null, null, "CO", null, null, null, null, null);
//		System.out.println(XMLUtility.domToString(doc));
		populateRows(doc);
		iCurrentRow = -1;
	}
	
	/**
	 * Get lookup with any or all parameters
	 * Used by PremisesSearch Dialog
	 * @param sStatePremID
	 * @param sFedPremID
	 * @param sAddress
	 * @param sCity
	 * @param sStateCode
	 * @param sZipCode
	 * @param sCounty
	 * @param sCountry
	 * @param sPhone
	 * @param sClassType
	 * @throws WebServiceException 
	 */
	public UsaHerdsLookupPrems( String sStatePremID, String sFedPremID, 
			String sAddress1, String sCity, String sStateCode, String sZipCode, 
			String sCounty, String sCountry, String sPhone, String sClassType ) throws WebServiceException {
		CivetWebServices service = new CivetWebServices();
		Document doc = service.getCivetPremises(sStatePremID, sFedPremID, 
				 sAddress1, sCity, sStateCode, sZipCode, 
				 sCounty, sCountry, sPhone, sClassType);
		populateRows(doc);
		iCurrentRow = -1;
	}

	/**
	 * For PhoneField focus lost
	 * @param sPhone
	 * @throws WebServiceException 
	 */
	public UsaHerdsLookupPrems( String sPhone ) throws WebServiceException {
		CivetWebServices service = new CivetWebServices();
		Document doc = service.getCivetPremises(null, null,null,null,null, null,null,null, sPhone, null);
		populateRows(doc);
		iCurrentRow = -1;
	}

	/**
	 * For looking up specific premises
	 * @param sStatePremisesId
	 * @param sFedPremisesId
	 * @throws WebServiceException 
	 */
	public UsaHerdsLookupPrems( String sStatePremisesId, String sFedPremisesId ) throws WebServiceException {
		CivetWebServices service = new CivetWebServices();
		Document doc = service.getCivetPremises(sStatePremisesId, sFedPremisesId,null,null,null, null,null,null, null, null);
		populateRows(doc);
		iCurrentRow = -1;
	}

	/**
	 * For Address, City, and ZipCode field focus lost
	 * @param sAddress
	 * @param sCity
	 * @param sStateCode
	 * @param sZipCode
	 * @throws WebServiceException 
	 */
	public UsaHerdsLookupPrems( String sAddress, String sCity, String sStateCode, String sZipCode ) throws WebServiceException {
		CivetWebServices service = new CivetWebServices();
		Document doc = service.getCivetPremises(null, null, sAddress, sCity, sStateCode, sZipCode,null,null, null, null);
		populateRows(doc);
		iCurrentRow = -1;
	}

	/**
	 * Construct from an already run search
	 * @param doc
	 */
	public UsaHerdsLookupPrems( Document doc ) {
		populateRows(doc);
		iCurrentRow = -1;
	}
	
	/* (non-Javadoc)
	 * @see edu.clemson.lph.civet.webservice.PremisesTableModel#clear()
	 */
	@Override
	public void clear() {
		rows.clear();
		iCurrentRow = -1;
	}
	
	private void populateRows( Document doc ) {
		rows = new ArrayList<WebServicePremisesRow>();
		Node root = XMLUtility.findFirstChildElementByName( doc.getDocumentElement(), "premiseList" );
		// TODO add test
		NodeList types = root.getChildNodes();
		for( int i = 0; i < types.getLength(); i++ ) {
			Node vet = types.item(i);
			if( vet.getNodeType() == Node.ELEMENT_NODE ) {
				NamedNodeMap vetAttrs = vet.getAttributes();
				Node nKey = vetAttrs.getNamedItem("premisekey");
				Integer iKeyValue = null;
				if( nKey != null ) {
					String sKey = nKey.getNodeValue();
					iKeyValue = Integer.parseInt(sKey);
				}
			
				NodeList fields = vet.getChildNodes();
				String sStatePremID = null;
				String sFedPremID = null;
				String sPremName = null;
				String sAddress1 = null;
				String sAddress2 = null;
				String sCity = null;
				String sStateCode = null;
				String sZipCode = null;
				String sCounty = null;
				String sCountry = null;
				String sPhone = null;
				String sFax = null;
				String sGeoSource = null;
				String sGeoLat = null;
				String sGeoLong = null;
				String sClassType = null;
				for( int j = 0; j < fields.getLength(); j++ ) {
					Node field = fields.item(j);
					if( field.getNodeType() == Node.ELEMENT_NODE ) {
						if( "statePremID".equals(field.getNodeName() ) )
							sStatePremID = field.getTextContent();
						else if( "fedPremID".equals(field.getNodeName() ) )
							sFedPremID = field.getTextContent();
						else if( "premName".equals(field.getNodeName() ) )
							sPremName = field.getTextContent();
						else if( "address1".equals(field.getNodeName() ) )
							sAddress1 = field.getTextContent();
						else if( "address2".equals(field.getNodeName() ) )
							sAddress2 = field.getTextContent();
						else if( "city".equals(field.getNodeName() ) )
							sCity = field.getTextContent();
						else if( "stateCode".equals(field.getNodeName() ) )
							sStateCode = field.getTextContent();
						else if( "zipCode".equals(field.getNodeName() ) ) {
							sZipCode = field.getTextContent();
							if( sZipCode != null )
								sZipCode = sZipCode.trim();
						}
						else if( "county".equals(field.getNodeName() ) )
							sCounty = field.getTextContent();
						else if( "country".equals(field.getNodeName() ) )
							sCountry = field.getTextContent();
						else if( "phone".equals(field.getNodeName() ) )
							sPhone = field.getTextContent();
						else if( "fax".equals(field.getNodeName() ) )
							sFax = field.getTextContent();
						else if( "classType".equals(field.getNodeName() ) )
							sClassType = field.getTextContent();
						else if( "fax".equals(field.getNodeName() ) )
							sGeoSource = field.getTextContent();
						else if( "fax".equals(field.getNodeName() ) )
							sGeoLat = field.getTextContent();
						else if( "fax".equals(field.getNodeName() ) )
							sGeoLong = field.getTextContent();
					}
				}
				WebServicePremisesRow row = new WebServicePremisesRow(iKeyValue, sStatePremID, sFedPremID, sPremName, 
																  sAddress1, sAddress2, sCity, sStateCode, sZipCode, 
																  sCounty, sCountry, sPhone, sFax, sClassType,
																  sGeoSource, sGeoLat, sGeoLong );
				rows.add(row);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.clemson.lph.civet.webservice.PremisesTableModel#first()
	 */
	@Override
	public boolean first() {
		if( rows == null || rows.size() == 0 ) return false;
		iCurrentRow = -1;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see edu.clemson.lph.civet.webservice.PremisesTableModel#next()
	 */
	@Override
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
	
	@Override
	public String getPremNameAt( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sPremName;
	}
	
	public String getPremIdAt( int iRow ) {
		String sPremId = getFederalIdAt(iRow);
		if( sPremId == null || sPremId.trim().length() == 0 )
			sPremId = getStateIdAt(iRow);
		return sPremId;
	}
	
	public String getPremClassAt( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sClassType;
	}
	
	private String getStateIdAt( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sStatePremID;
	}
	
	private String getFederalIdAt( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sFedPremID;
	}

	/* (non-Javadoc)
	 * @see edu.clemson.lph.civet.webservice.PremisesTableModel#getPhoneAt(int)
	 */
	@Override
	public String getPhoneAt( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sPhone;
	}

	/* (non-Javadoc)
	 * @see edu.clemson.lph.civet.webservice.PremisesTableModel#getAddressAt(int)
	 */
	@Override
	public String getAddressAt( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sAddress;
	}

	/* (non-Javadoc)
	 * @see edu.clemson.lph.civet.webservice.PremisesTableModel#getCityAt(int)
	 */
	@Override
	public String getCityAt( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sCity;
	}

	/* (non-Javadoc)
	 * @see edu.clemson.lph.civet.webservice.PremisesTableModel#getStateCodeAt(int)
	 */
	@Override
	public String getStateCodeAt( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sStateCode;
	}

	public String getCountyAt( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sCounty;
	}

	/* (non-Javadoc)
	 * @see edu.clemson.lph.civet.webservice.PremisesTableModel#getZipCodeAt(int)
	 */
	@Override
	public String getZipCodeAt( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sZipCode;
	}
	
	public Integer getKeyValue() {
		if( currentRow == null ) return null;
		return currentRow.iKeyValue;
	}
	
	public String getPremId() {
		String sPremId = getFedPremId();
		if( sPremId == null || sPremId.trim().length() == 0 )
			sPremId = getStatePremId();
		return sPremId;
	}

	private String getStatePremId() {
		if( currentRow == null) return null;
		return currentRow.sStatePremID;
	}
	
	public String getPremClass() {
		if( currentRow == null) return null;
		return currentRow.sClassType;
	}
	
	private String getFedPremId() {
		if( currentRow == null) return null;
		return currentRow.sFedPremID;
	}
	
	public String getPremName() {
		if( currentRow == null) return null;
		return currentRow.sPremName;
	}
	
	public String getAddress() {
		if( currentRow == null) return null;
		return currentRow.sAddress;
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

	public String getClassType() {
		if( currentRow == null) return null;
		return currentRow.sClassType;
	}

	public String getGeoSource() {
		if( currentRow == null) return null;
		return currentRow.sGeoSource;
	}

	public String getGeoLat() {
		if( currentRow == null) return null;
		return currentRow.sGeoLat;
	}

	public String getGeoLong() {
		if( currentRow == null) return null;
		return currentRow.sGeoLong;
	}

	private static class WebServicePremisesRow {
		public Integer iKeyValue;
		public String sStatePremID;
		public String sFedPremID;
		public String sPremName;
		public String sAddress;
		public String sCity;
		public String sStateCode;
		public String sZipCode;
		public String sCounty;
		public String sCountry;
		public String sPhone;
		public String sFax;
		public String sClassType;
		public String sGeoSource;
		public String sGeoLat;
		public String sGeoLong;
		
		public  WebServicePremisesRow( Integer iKeyValue, String sStatePremID, String sFedPremID, String sPremName, 
				String sAddress1, String sAddress2, String sCity, String sStateCode, String sZipCode, 
				String sCounty, String sCountry, String sPhone, String sFax, String sClassType,
				String sGeoSource, String sGeoLat, String sGeoLong ) {
			this.iKeyValue = iKeyValue;
			this.sStatePremID = sStatePremID;
			this.sFedPremID = sFedPremID;
			this.sPremName = sPremName;
			this.sAddress = sAddress1;
			this.sCity = sCity;
			this.sStateCode = sStateCode;
			this.sZipCode = sZipCode;
			this.sCounty = sCounty;
			this.sCountry = sCountry;
			this.sPhone = sPhone;
			this.sFax = sFax;
			this.sClassType = sClassType;
			this.sGeoSource = sGeoSource;
			this.sGeoLat = sGeoLat;
			this.sGeoLong = sGeoLat;
		}
	}

	// Table Model methods.
	
	@Override
	public int getColumnCount() {
		return 7;
	}

	@Override
	public String getColumnName(int columnIndex) {
		String sRet = null;
		switch( columnIndex ) {
		case 0: sRet =  "LID"; break;
		case 1: sRet =  "PIN"; break;
		case 2: sRet =  "Name"; break;
		case 3: sRet =  "Address"; break;
		case 4: sRet =  "City"; break;
		case 5: sRet =  "County"; break;
		case 6: sRet =  "Classification"; break;
		default: logger.error("Index out of bounds: " + columnIndex);
		}
		return sRet;
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object oRet = null;
		if( rows.size() == 0 )
			return oRet;
		try {
			WebServicePremisesRow row = rows.get(rowIndex);
			switch( columnIndex ) {
			case 0: oRet =  row.sStatePremID; break;
			case 1: oRet =  row.sFedPremID; break;
			case 2: oRet =  row.sPremName; break;
			case 3: oRet =  row.sAddress; break;
			case 4: oRet =  row.sCity; break;
			case 5: oRet =  row.sCounty; break;
			case 6: oRet =  row.sClassType; break;
			default: logger.error("Index out of bounds: " + columnIndex);
			}
		} catch( IndexOutOfBoundsException e ) {
//			MessageDialog.showMessage(null, "Civet Error: Silent Bug", "Please make a note of what you just did.  It will help find the source of a silent bug.");
			logger.error("Index out of bounds: " + columnIndex, e);
		}
		return oRet;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if( rows.size() == 0 )
			return;
		try {
			WebServicePremisesRow row = rows.get(rowIndex);
			switch( columnIndex ) {
			case 0: row.sStatePremID = (String)aValue; break;
			case 1: row.sFedPremID = (String)aValue; break;
			case 2: row.sPremName = (String)aValue; break;
			case 3: row.sAddress = (String)aValue; break;
			case 4: row.sCity = (String)aValue; break;
			case 5: row.sCounty = (String)aValue; break;
			case 6: row.sClassType = (String)aValue; break;
			default: logger.error("Index out of bounds: " + columnIndex);
			}
		} catch( IndexOutOfBoundsException e ) {
			logger.error("Index out of bounds: " + columnIndex, e);
		}
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

}

