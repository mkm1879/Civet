package edu.clemson.lph.civet.poultry;

import java.awt.Window;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import edu.clemson.lph.logging.Logger;

import edu.clemson.lph.civet.AnimalIDRecord;
import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.SpeciesRecord;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.webservice.CivetWebServices;
import edu.clemson.lph.civet.xml.CviMetaDataXml;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.civet.xml.elements.Address;
import edu.clemson.lph.civet.xml.elements.Animal;
import edu.clemson.lph.civet.xml.elements.GroupLot;
import edu.clemson.lph.civet.xml.elements.Premises;
import edu.clemson.lph.civet.xml.elements.Product;
import edu.clemson.lph.db.DatabaseConnectionFactory;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.utils.FileUtils;
import edu.clemson.lph.utils.PremCheckSum;

public class SubmitNineDashThreeThread extends Thread {
      private static Logger logger = Logger.getLogger();
//	private static final OpenOption[] CREATE_OR_APPEND = new OpenOption[] { StandardOpenOption.APPEND, StandardOpenOption.CREATE };
//	private static final String sProgMsg = "Loading 9-3: ";
	
	Window parent;
	CivetWebServices service;
	DatabaseConnectionFactory factory;
	private StdeCviXmlModel xmlModel;
	private String sCVINo;
	private java.util.Date dDateIssued;
	private String sProduct;
	private ArrayList<SpeciesRecord> aSpecies;
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
				StdeCviXmlModel xmlModel,
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
		this.xmlModel = xmlModel;
		this.sCVINo = sCVINo; 
		this.dDateIssued = dDateIssued; 
		this.sProduct = sProduct; 
		this.aSpecies = aSpecies; 
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
			byte[] baXML = buildXml();
			// Save it just in case.
			String sFileName = "./CivetOutbox/" + sCVINo + ".xml";
			FileUtils.writeUTF8File(baXML, sFileName);
			// Send it!
			String sRet = null;
			int iTries = 0;
			while( iTries < 3 ) {
				try { 
					sRet = service.sendCviXML(baXML);
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
			if( sRet == null || ( !sRet.trim().startsWith("00") && !sRet.contains("Success") ) ) {
				if( sRet != null && sRet.contains("99,Timeout expired")) {
					logger.info("Time out CVI Number: " + sCVINo );
				}
				else {
					sRet = "Transmission level error\n" + sRet;
					logger.error( sRet, new Exception("Error submitting NPIP 9-3 spreadsheet CVI to USAHERDS: " +
							sCVINo ) );
					logger.error(new String(baXML, StandardCharsets.UTF_8));
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
	
	private byte[] buildXml() throws IOException {
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
		xmlModel.setCertificateNumber(sCVINo);
		xmlModel.setIssueDate(dDateIssued);
		xmlModel.setVet("NPIP");
		// Expiration date will be set automatically from getXML();
		xmlModel.setPurpose("Sale");

		Address aOrigin = new Address(sOriginAddress, null, sOriginCity, sOriginCounty, sOriginStateCode, sOriginZipCode,
				"USA", null, null);
		Premises pOrigin = new Premises(sOriginPIN, sOriginBusiness, aOrigin); 
		pOrigin.personName = sOriginName;
		xmlModel.setOrigin( pOrigin );
		Address aDestination = new Address(sDestinationAddress, null, sDestinationCity, sDestinationCounty, sDestinationStateCode, sDestinationZipCode,
				"USA", null, null);
		Premises pDestination = new Premises(sDestinationPIN, sDestinationBusiness, aDestination); 
		pDestination.personName = sDestinationName;
		xmlModel.setDestination(pDestination);
		if( "Eggs".equalsIgnoreCase(sProduct) ) {
			// Should never have IDs for Eggs!
			for( SpeciesRecord sr : aSpecies ) {
				Product product = new Product(sr.sSpeciesCode, "Eggs", Double.valueOf(sr.iNumber));
				xmlModel.addProduct(product);
			}
		}
		else {
			ArrayList<Animal> animals = xmlModel.getAnimals();
			for( Animal animal : animals ) {
				String sDateIssued = StdeCviXmlModel.getDateFormat().format(dDateIssued);
				animal.inspectionDate = sDateIssued;
				xmlModel.editAnimal(animal);
			}
			ArrayList<GroupLot> groups = xmlModel.getGroups();
			for( SpeciesRecord sr : aSpecies ) {
				// Only add group lot if not officially IDd so count ids and subtract
				String sSpeciesCode = sr.sSpeciesCode;
				int iNumOfSpp = sr.iNumber;
				int iCountIds = 0;
				if( animals != null ) {
					for( Animal animal : animals ) {
						if(  animal.speciesCode.code != null && animal.speciesCode.code.equals(sSpeciesCode) ) {
							iCountIds++;
						}
					}
				}
				boolean bFound = false;
				if( groups != null ) {
					for( GroupLot group : groups ) {
						if( group.speciesCode.code != null && group.speciesCode.code.equals(sSpeciesCode) ) {
							bFound = true;
							if(iCountIds == iNumOfSpp) {
								xmlModel.removeGroupLot(group);
							}
							else if( iCountIds > iNumOfSpp ) {
								logger.error("Number of tags " + iCountIds + " > number of animals " + iNumOfSpp + " for species " + sSpeciesCode);
								// Leave things alone.
							}
							else if( iCountIds != iNumOfSpp ) {
								Integer iNumUntagged = iNumOfSpp - iCountIds;
								group.setQuantity(iNumUntagged.doubleValue());
								xmlModel.addOrEditGroupLot(group);
							}
						}
					}
				}
				if( !bFound ) {
					Integer iNumUntagged = iNumOfSpp - iCountIds;
					GroupLot group = new GroupLot(sSpeciesCode, iNumUntagged.doubleValue());
					xmlModel.addOrEditGroupLot(group);
				}
			}
			for( GroupLot group : xmlModel.getGroups() ) {
				if( group.quantity <= 0 )
					xmlModel.removeGroupLot(group);
			}
		}
		
		CviMetaDataXml metaData = new CviMetaDataXml();
		metaData.setCertificateNbr(sCVINo);
		metaData.setBureauReceiptDate((new java.util.Date()));
		metaData.setErrorNote("NPIP 9-3 Dialog entry");
		metaData.setCVINumberSource("NPIP9_3");
		xmlModel.addOrUpdateMetadataAttachment(metaData);
		return xmlModel.getXMLBytes();
	}

}
