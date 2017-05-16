package edu.clemson.lph.civet.addons;

import java.awt.Window;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import edu.clemson.lph.civet.AnimalIDRecord;
import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.SpeciesRecord;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.webservice.CivetWebServices;
import edu.clemson.lph.civet.xml.CviMetaDataXml;
import edu.clemson.lph.civet.xml.StdeCviXmlBuilder;
import edu.clemson.lph.db.DatabaseConnectionFactory;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.ProgressDialog;
import edu.clemson.lph.utils.FileUtils;
import edu.clemson.lph.utils.PremCheckSum;

public class SubmitNineDashThreeThread extends Thread {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private static final OpenOption[] CREATE_OR_APPEND = new OpenOption[] { StandardOpenOption.APPEND, StandardOpenOption.CREATE };
//	private static final String sProgMsg = "Loading 9-3: ";
	
	Window parent;
	CivetWebServices service;
	DatabaseConnectionFactory factory;
	private String sCVINo;
	private java.util.Date dDateIssued;
	private String sProduct;
	private ArrayList<SpeciesRecord> aSpecies;
	private ArrayList<AnimalIDRecord> aAnimalIDs;
	private String sOriginStateCode;
	private String sOriginPIN;
	private String sOriginBusiness ;
	private String sOriginName ;
	private String sOriginAddress;
	private String sOriginCity;
	private String sOriginCounty;
	private String sOriginZipCode;
	private String sDestinationStateCode;
	private String sDestinationPIN;
	private String sDestinationBusiness;
	private String sDestinationName;
	private String sDestinationAddress;
	private String sDestinationCity;
	private String sDestinationCounty;
	private String sDestinationZipCode;
	

	public SubmitNineDashThreeThread() {
		// TODO Auto-generated constructor stub
	}

	
	public SubmitNineDashThreeThread( DatabaseConnectionFactory factory, Window parent,
			 String sCVINo, 
			 java.util.Date dDateIssued, 
			 String sProduct, 
			 ArrayList<SpeciesRecord> aSpecies, 
			 ArrayList<AnimalIDRecord> aAnimalIDs, 
			 String sOriginStateCode, 
			 String sOriginPIN, 
			 String sOriginBusiness,
			 String sOriginName , 
			 String sOriginAddress, 
			 String sOriginCity, 
			 String sOriginCounty, 
			 String sOriginZipCode, 
			 String sDestinationStateCode, 
			 String sDestinationPIN, 
			 String sDestinationBusiness,
			 String sDestinationName, 
			 String sDestinationAddress, 
			 String sDestinationCity, 
			 String sDestinationCounty, 
			 String sDestinationZipCode  ) {
		this.parent = parent;
		this.factory = factory;
		this.sCVINo = sCVINo; 
		this.dDateIssued = dDateIssued; 
		this.sProduct = sProduct; 
		this.aSpecies = aSpecies; 
		this.aAnimalIDs = aAnimalIDs; 
		this.sOriginStateCode = sOriginStateCode; 
		this.sOriginPIN = sOriginPIN; 
		this.sOriginBusiness  = sOriginBusiness; 
		this.sOriginName  = sOriginName; 
		this.sOriginAddress = sOriginAddress; 
		this.sOriginCity = sOriginCity; 
		this.sOriginCounty = sOriginCounty; 
		this.sOriginZipCode = sOriginZipCode; 
		this.sDestinationStateCode = sDestinationStateCode; 
		this.sDestinationPIN = sDestinationPIN; 
		this.sDestinationBusiness = sDestinationBusiness;
		this.sDestinationName = sDestinationName; 
		this.sDestinationAddress = sDestinationAddress; 
		this.sDestinationCity = sDestinationCity; 
		this.sDestinationCounty = sDestinationCounty; 
		this.sDestinationZipCode = sDestinationZipCode;
		service = new CivetWebServices();
	}
	
	/**
	 * Most of this is duplicate code from the other submission threads.  Should refactor.
	 */
	public void run() {
		try {
			if( cviExists( sCVINo, sOriginStateCode )) {
				try {
					String sLineOut = sCVINo + " from " + sOriginStateCode + " already exists\r\n";
					Files.write(Paths.get("Duplicates.txt"), sLineOut.getBytes(), CREATE_OR_APPEND);
				}catch (IOException e) {
					logger.error(e);
				}
				exitThread(false);
			}
			if( "Eggs".equalsIgnoreCase( sProduct ) ) {
				addToEggCVIs(sCVINo);
			}
			String sXML = buildXml();
					System.out.println(sXML);
			// Send it!
			String sRet = null;
			int iTries = 0;
			while( iTries < 3 ) {
				try { 
					sRet = service.sendCviXML(sXML);
					iTries = 4;
				} catch( Exception e ) {
					if( sRet.contains("99,Timeout expired")) {
						logger.info("Time out CVI Number: " + sCVINo + '\n' + e.getMessage() );
						iTries++;
					}
					else {
						sRet = null;
						break;  // Give up
					}
				}
			}
			if( sRet == null || !sRet.trim().startsWith("00") ) {
				if( sRet.contains("99,Timeout expired")) {
					logger.info("Time out CVI Number: " + sCVINo );
				}
				else {
					sRet = "Transmission level error\n" + sRet;
					logger.error( sRet, new Exception("Error submitting NPIP 9-3 spreadsheet CVI to USAHERDS: " +
							sCVINo ) );
					logger.error(sXML);
					MessageDialog.messageLater(parent, "Civet WS Error", "Error submitting to USAHERDS: " + 
							sCVINo + "\n" + sRet);
				}
			}

		} catch (IOException e2) {
			// File Error on the data file to be read
			logger.error(e2);
			exitThread(false);
		}
		exitThread(true);
	}

	/**
	 * For future use.
	 * @param bSuccess
	 */
	private void exitThread( boolean bSuccess ) {
//		final boolean bDone = bSuccess;
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				prog.setVisible(false);
//				prog.dispose();
//				if( bDone )
//					;
//			}
//		});
	}
	
	private String buildXml() throws IOException {
		StdeCviXmlBuilder xmlBuilder = new StdeCviXmlBuilder();
		try {
			if( !PremCheckSum.isValid(sOriginPIN) ) {
				sOriginPIN = null;
			}
		} catch (Exception e1) {
			sOriginPIN = null;
		}
		if( sOriginPIN != null && sOriginPIN.trim().length() == 8 ) {
			sOriginStateCode = sOriginPIN.substring(0, 2);
		}
		else if( sOriginStateCode == null || sOriginStateCode.trim().length() == 0 )
			sOriginStateCode = CivetConfig.getHomeStateAbbr();
		
		try {
			if( !PremCheckSum.isValid(sDestinationPIN) ) {
				sDestinationPIN = null;
			}
		} catch (Exception e1) {
			sDestinationPIN = null;
		}
		if( sDestinationPIN != null && sDestinationPIN.trim().length() == 8 ) {
			sDestinationStateCode = sDestinationPIN.substring(0, 2);
		}
		else if( sDestinationStateCode == null || sDestinationStateCode.trim().length() == 0 )
			sDestinationStateCode = CivetConfig.getHomeStateAbbr();
		xmlBuilder.setCviNumber(sCVINo);
		xmlBuilder.setIssueDate(dDateIssued);
		xmlBuilder.setVet("NPIP");
		// Expiration date will be set automatically from getXML();
		xmlBuilder.setPurpose("other");
		// We don't enter the person name, normally  or add logic to tell prem name from person name.
		Element origin = xmlBuilder.setOrigin(sOriginPIN, sOriginBusiness, sOriginName, null );
		xmlBuilder.setAddress(origin, sOriginAddress, sOriginCity, sOriginCounty, sOriginStateCode, sOriginZipCode);
		Element destination = xmlBuilder.setDestination(sDestinationPIN, sDestinationBusiness, sDestinationName, null );
		xmlBuilder.setAddress(destination, sDestinationAddress, sDestinationCity, sDestinationCounty, 
					sDestinationStateCode, sDestinationZipCode);
		
		for( SpeciesRecord sr : aSpecies ) {
			String sSpeciesCode = sr.sSpeciesCode;
			Integer iNum = sr.iNumber;
			int iNumTags = 0;
			String sGender = "Gender Unknown";
			for( AnimalIDRecord ar : aAnimalIDs ) {
				if( ar.sSpeciesCode.equals( sSpeciesCode ) ) {
					xmlBuilder.addAnimal(sSpeciesCode, dDateIssued, null, null, sGender, "OTH", ar.sTag);
					iNumTags++;
				}
			}
			if( iNumTags < iNum ) {
				xmlBuilder.addGroup(iNum - iNumTags, "Poultry Lot Under NPIP 9-3", sSpeciesCode, null, sGender);
			}		
		}
		CviMetaDataXml metaData = new CviMetaDataXml();
		metaData.setCertificateNbr(sCVINo);
		metaData.setBureauReceiptDate((new java.util.Date()));
		metaData.setErrorNote("NPIP 9-3 Form Spreadsheet");
		metaData.setCVINumberSource("NPIP9_3");
//	System.out.println(metaData.getXmlString());
		xmlBuilder.addMetadataAttachement(metaData);
		return xmlBuilder.getXMLString();
	}

	/**
	 * Duplicate checking is only available for local SC use with direct DB access.
	 * @param sCVINbr
	 * @param sState
	 * @return
	 */
	private boolean cviExists( String sCVINbr, String sState ) {
		if( factory == null ) return false;
		boolean bRet = false;
		// NOTE: There is no generator for this now.  Make one in AddOns and distribute to other states????
		String sQuery = "SELECT * FROM USAHERDS.dbo.CVIs c \n" +
		                "JOIN USAHERDS.dbo.States s ON s.StateKey = c.StateKey \n" +
				        "WHERE c.CertificateNbr = ? AND s.StateCode = ?";
		Connection conn = factory.makeDBConnection();
		try {
			PreparedStatement ps = conn.prepareStatement(sQuery);
			ps.setString(1, sCVINbr);
			ps.setString(2,  sState);
			ResultSet rs = ps.executeQuery();
			if( rs.next() ) {
				bRet = true;
			}
		} catch( SQLException e ) {
			logger.error("Error in query: " + sQuery, e);
		} finally {
			try {
				if( conn != null && !conn.isClosed() )
					conn.close();
			} catch( Exception e2 ) {
			}
		}
		return bRet;
	}
	
	private boolean addToEggCVIs( String sCVINbr ) {
		boolean bRet = false;
		if( factory != null ) {
			String sQuery = "INSERT INTO USAHERDS_LPH.dbo.EggCVIs VALUES( ? )";
			Connection conn = factory.makeDBConnection();
			try {
				PreparedStatement ps = conn.prepareStatement(sQuery);
				ps.setString(1, sCVINbr);
				int iRet = ps.executeUpdate();
				if( iRet > 0 ) {
					bRet = true;
				}
			} catch( SQLException e ) {
				logger.error("Error in query: " + sQuery, e);
			} finally {
				try {
					if( conn != null && !conn.isClosed() )
						conn.close();
				} catch( Exception e2 ) {
				}
			}
		}
		else {
			FileUtils.writeTextFile(sCVINbr + "\n", "EggCVIs.txt", true);
		}
		return bRet;
	}





}
