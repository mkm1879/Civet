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
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.event.TableModelListener;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetConfig;
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
 *					String sAddress1, String sCity, String sStateCode, String sZipCode, 
 *					String sCounty, String sCountry, String sPhone, String sClassType  ) (Returns DOM of returned XML)
 *         public class UsaHerdsLookupPrems (Provides next()/get() Interface and implements DBTableModel)
 * @author mmarti5
 *
 */
public class UsaHerdsLookupPrems implements javax.swing.table.TableModel {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	static {
		// BasicConfigurator replaced with PropertyConfigurator.
	     PropertyConfigurator.configure("CivetConfig.txt");
	     logger.setLevel(Level.ERROR);
	}
	
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
		System.out.println(XMLUtility.domToString(doc));
		populateRows(doc);
		iCurrentRow = -1;
	}
	
	/**
	 * Get lookup with any or all parameters
	 * Used by PremisesSearch Dialog
	 * @param sStatePremID
	 * @param sFedPremID
	 * @param sAddress1
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
	
	public static void main( String args[] ) {
		CivetConfig.setHERDSUserName("civet");
		CivetConfig.setHERDSPassword("civet#2014");
		CivetWebServices service = new CivetWebServices();
		Document doc;
		try {
			doc = service.getCivetPremises(null, null, null, null, "CA", null, null, null, "*3*", null);
			FileOutputStream fOut = new FileOutputStream(new File("PremisesCA3.xml"));
			PrintWriter pw = new PrintWriter( fOut );
			pw.write(XMLUtility.domToString(doc));
			pw.close();
			fOut.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
//		UsaHerdsLookupPrems me;
//		try {
//			me = new UsaHerdsLookupPrems();
//			while( me.next() ) {
//				System.out.println( String.format("%d | %s | %s| %s", me.getKeyValue(), me.getStatePremId(), me.getFedPremId(), me.getAddress1() ));
//			}
//		} catch (WebServiceException e) {
//			// TODO Auto-generated catch block
//			logger.error(e);
//		}
 	}
	
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
						else if( "zipCode".equals(field.getNodeName() ) )
							sZipCode = field.getTextContent();
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
	
	public String getPremNameAt( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sPremName;
	}
	
	public String getStateIdAt( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sStatePremID;
	}
	
	public String getFederalIdAt( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sFedPremID;
	}

	public String getPhoneAt( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sPhone;
	}

	public String getAddress1At( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sAddress1;
	}

	public String getAddress2At( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sAddress2;
	}

	public String getCityAt( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sCity;
	}

	public String getStateCodeAt( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sStateCode;
	}

	public String getCountyAt( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sCounty;
	}

	public String getZipCodeAt( int iRow ) {
		WebServicePremisesRow row = rows.get(iRow);
		return row.sZipCode;
	}
	
	public Integer getKeyValue() {
		if( currentRow == null ) return null;
		return currentRow.iKeyValue;
	}

	public String getStatePremId() {
		if( currentRow == null) return null;
		return currentRow.sStatePremID;
	}
	
	public String getFedPremId() {
		if( currentRow == null) return null;
		return currentRow.sFedPremID;
	}
	
	public String getPremName() {
		if( currentRow == null) return null;
		return currentRow.sPremName;
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

	private class WebServicePremisesRow {
		public Integer iKeyValue;
		public String sStatePremID;
		public String sFedPremID;
		public String sPremName;
		public String sAddress1;
		public String sAddress2;
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
			this.sAddress1 = sAddress1;
			this.sAddress2 = sAddress2;
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
		return 6;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch( columnIndex ) {
		case 0: return "LID";
		case 1: return "PIN";
		case 2: return "Name";
		case 3: return "Address";
		case 4: return "City";
		case 5: return "County";
		}
		return null;
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		WebServicePremisesRow row = rows.get(rowIndex);
		switch( columnIndex ) {
		case 0: return row.sStatePremID;
		case 1: return row.sFedPremID;
		case 2: return row.sPremName;
		case 3: return row.sAddress1;
		case 4: return row.sCity;
		case 5: return row.sCounty;
		}
		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		WebServicePremisesRow row = rows.get(rowIndex);
		switch( columnIndex ) {
		case 0: row.sStatePremID = (String)aValue;
		case 1: row.sFedPremID = (String)aValue;
		case 2: row.sPremName = (String)aValue;
		case 3: row.sAddress1 = (String)aValue;
		case 4: row.sCity = (String)aValue;
		case 5: row.sCounty = (String)aValue;
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

