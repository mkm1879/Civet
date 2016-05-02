package edu.clemson.lph.civet.addons;
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
import edu.clemson.lph.civet.AddOn;
import edu.clemson.lph.civet.CSVFilter;
import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.lookup.SpeciesLookup;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.webservice.CivetWebServices;
import edu.clemson.lph.civet.xml.CviMetaDataXml;
import edu.clemson.lph.civet.xml.StdeCviXml;
import edu.clemson.lph.civet.xml.StdeCviXmlBuilder;
import edu.clemson.lph.db.*;
import edu.clemson.lph.dialogs.*;
import edu.clemson.lph.utils.PremCheckSum;
import edu.clemson.lph.utils.StringUtils;

import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.*;

import org.apache.log4j.*;
import org.w3c.dom.Element;

public class BulkLoadNineDashThreeCSV implements ThreadListener, AddOn {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private String sCVINbrSource = CviMetaDataXml.CVI_SRC_9dash3;
	private static final String sProgMsg = "Loading 9-3: ";
	private static final OpenOption[] CREATE_OR_APPEND = new OpenOption[] { StandardOpenOption.APPEND, StandardOpenOption.CREATE };
	private DatabaseConnectionFactory factory;
	
	public BulkLoadNineDashThreeCSV() {
	}

	public void import93CSV( Window parent ) {
		if( factory == null )
			factory = InitAddOns.getFactory();
		String sFilePath = null;
	    JFileChooser fc = new JFileChooser();
	    fc.setCurrentDirectory(new File(CivetConfig.getNineDashThreeLoadDirPath() ));
	    fc.setDialogTitle("Open NPIP 9-3 CSV File");
	    fc.setFileFilter( new CSVFilter() );
	    int returnVal = fc.showOpenDialog(parent);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
	      File file = fc.getSelectedFile();
	      sFilePath = file.getAbsolutePath();
	    }
	    else {
	    	return;
	    }
		importNineDashThreeCSVFile( parent, sFilePath );

	}
	
	private void importNineDashThreeCSVFile( Window parent, String sFilePath ) {
		ProgressDialog prog = new ProgressDialog(parent, "Civet", sProgMsg);
		prog.setAuto(true);
		prog.setVisible(true);
		TWork93CSV tWork = new TWork93CSV( prog, factory, sFilePath, parent );
		tWork.start();
	}

	// Also create TWorkAddSpecies and TWorkAddPage
	class TWork93CSV extends Thread {
		String sFilePath;
		ProgressDialog prog;
		Window parent;
		CivetWebServices service;
		DatabaseConnectionFactory factory;

		
		public TWork93CSV( ProgressDialog prog, DatabaseConnectionFactory factory, String sFilePath, Window parent ) {
			this.prog = prog;
			this.sFilePath = sFilePath;
			this.parent = parent;
			this.factory = factory;
			service = new CivetWebServices();
		}
		
		private String formatMessage( int iRow, int iMax ) {
			String sOut = String.format("Record %d of %d imported", iRow, iMax);
			return sOut;
		}
		
		public void run() {
			// Create CSVNineDashThreeDataFile object from CSV file
			CSVNineDashThreeDataFile data;
//			boolean bSeenBadProduct = false;
			try {
				File fData = new File(sFilePath);
				String sFileName = fData.getName();
				prog.setTitle("Civet 9-3: " + sFileName);
				data = new CSVNineDashThreeDataFile( sFilePath );
				int iMax = data.size();
				prog.setMax(iMax);
				int iRow = 0;
				prog.setValue(iRow);
				prog.setMessage( formatMessage( iRow, iMax) );
					// Iterate over the CSV file
					while( data.nextRow() ) {
						// Let USAHERDS catch up.
						try {
							Thread.sleep(500L);
						} catch (InterruptedException e1) { }
						prog.setMessage(sProgMsg + data.getCVINumber() );
						if( cviExists( data.getCVINumber(), data.getConsignorState() )) {
							try {
								String sLineOut = data.getCVINumber() + " from " + data.getConsignorState() + " already exists\r\n";
							    Files.write(Paths.get("Duplicates.txt"), sLineOut.getBytes(), CREATE_OR_APPEND);
							}catch (IOException e) {
							    logger.error(e);
							}
							continue;
						}
						if( !("live animal".equalsIgnoreCase( data.getProduct() )) ) {
							try {
								String sLineOut = data.getCVINumber() + ", " + data.getProduct() + "\r\n";
							    Files.write(Paths.get("NonAnimalCVIs.txt"), sLineOut.getBytes(), CREATE_OR_APPEND);
							}catch (IOException e) {
								 logger.error(e);
							}
						}
						String sXML = buildXml( data );
//			System.out.println(sXML);
						// Send it!
						String sRet = null;
						int iTries = 0;
						while( iTries < 3 ) {
							try { 
								sRet = service.sendCviXML(sXML);
								iTries = 4;
							} catch( Exception e ) {
								if( e.getMessage().contains("timed out")) {
									try {
										logger.info("Sleeping after timeout");
										sleep(2000L);
									} catch (InterruptedException e1) { }
									iTries++;
								}
								else {
									sRet = null;
									iTries = 4;
								}
							}
						}
						if( sRet == null || !sRet.trim().startsWith("00") ) {
							logger.error( sRet, new Exception("Error submitting NPIP 9-3 spreadsheet CVI to USAHERDS: " +
														data.getCVINumber() ) );
							logger.error(sXML);
							MessageDialog.messageLater(parent, "Civet WS Error", "Error submitting to USAHERDS: " + 
														data.getCVINumber() + "\n" + sRet);
						}

					} // Next Row

			} catch (IOException e2) {
				// File Error on the data file to be read
				logger.error(e2);
				exitThread(false);
			}
			exitThread(true);
		}

		private void exitThread( boolean bSuccess ) {
			final boolean bDone = bSuccess;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					prog.setVisible(false);
					prog.dispose();
					if( bDone )
						onThreadComplete(TWork93CSV.this);
				}
			});
		}
		
		private String buildXml( CSVNineDashThreeDataFile data ) throws IOException {
			StdeCviXmlBuilder xmlBuilder = new StdeCviXmlBuilder();
			String sCVINumber = data.getCVINumber();
			String sSpecies = data.getSpecies();
			String sSpeciesCode = null;
			if( sSpecies == null || sSpecies.trim().equalsIgnoreCase("Poultry") ) {
				sSpeciesCode = "OTH";
			}
			else if( sSpecies.trim().length() == 3 ) {
				sSpeciesCode = sSpecies;
			}
			else {
				SpeciesLookup spLu = new SpeciesLookup( StringUtils.toTitleCase(sSpecies.trim()), true );
				sSpeciesCode = spLu.getSpeciesCode();
				if( sSpeciesCode.equals("ERROR") ) {
					sSpeciesCode = "POU";
				}
			}
			String sConsignorState = data.getConsignorState();
			String sSourcePIN = data.getConsignorPIN();
			try {
				if( !PremCheckSum.isValid(sSourcePIN) ) {
					sSourcePIN = null;
				}
			} catch (Exception e1) {
				sSourcePIN = null;
			}
			if( sSourcePIN != null && sSourcePIN.trim().length() == 8 ) {
				sConsignorState = sSourcePIN.substring(0, 2);
			}
			else if( sConsignorState == null || sConsignorState.trim().length() == 0 )
				sConsignorState = CivetConfig.getHomeStateAbbr();
			
			String sConsigneeState = data.getConsigneeState();
			String sDestinationPIN = data.getConsigneePIN();
			try {
				if( !PremCheckSum.isValid(sDestinationPIN) ) {
					sDestinationPIN = null;
				}
			} catch (Exception e1) {
				sDestinationPIN = null;
			}
			if( sDestinationPIN != null && sDestinationPIN.trim().length() == 8 ) {
				sConsigneeState = sDestinationPIN.substring(0, 2);
			}
			else if( sConsigneeState == null || sConsigneeState.trim().length() == 0 )
				sConsigneeState = CivetConfig.getHomeStateAbbr();
			xmlBuilder.setCviNumber(sCVINumber);
			xmlBuilder.setIssueDate(data.getInspectionDate());
			xmlBuilder.setVet("NPIP");
			// Expiration date will be set automatically from getXML();
			xmlBuilder.setPurpose("other");
			// We don't enter the person name, normally  or add logic to tell prem name from person name.
			Element origin = xmlBuilder.setOrigin(sSourcePIN, data.getConsignorBusiness(), data.getConsignorName(), null );
			xmlBuilder.setAddress(origin, data.getConsignorStreet(), data.getConsignorCity(), null, sConsignorState, data.getConsignorZip());
			Element destination = xmlBuilder.setDestination(sDestinationPIN, data.getConsigneeBusiness(), data.getConsigneeName(), null );
			xmlBuilder.setAddress(destination, data.getConsigneeStreet(), data.getConsigneeCity(), null, sConsigneeState, data.getConsigneeZip());

			Integer iNum = data.getAnimalCount();
			if( iNum == null ) {
				logger.error("Missing Animal Count in " + sCVINumber);
				iNum = 1;
			}
			String sGender = "Gender Unknown";
			int iNumTags = 0;
			if( data.hasTags() ) {
				for( String sID : data.listTagIds() ) {
					xmlBuilder.addAnimal(sSpeciesCode, data.getInspectionDate(), null, null, sGender, "OTH", sID);
					iNumTags++;
				}
			}
			if( iNumTags < iNum ) {
				xmlBuilder.addGroup(iNum - iNumTags, "Poultry Lot Under NPIP 9-3", sSpeciesCode, null, sGender);
			}
			CviMetaDataXml metaData = new CviMetaDataXml();
			metaData.setCertificateNbr(sCVINumber);
			metaData.setBureauReceiptDate(data.getSavedDate());
			metaData.setErrorNote("NPIP 9-3 Form Spreadsheet");
			metaData.setCVINumberSource(sCVINbrSource);
//		System.out.println(metaData.getXmlString());
			xmlBuilder.addMetadataAttachement(metaData);
			return xmlBuilder.getXMLString();
		}

		private boolean cviExists( String sCVINbr, String sState ) {
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
	
	}// end inner class TWorkSave
	


	@Override
	public void onThreadComplete( Thread thread ) {
		// TODO Auto-generated method stub
		// Do what needs to be done in GUI thread.
		if( thread instanceof TWork93CSV ) {
			System.out.println("Import Complete");
			//System.exit(1);
		}
	}

	@Override
	public String getMenuText() {
		return "Import NPIP 9-3 CSV File";
	}

	@Override
	public void execute(Window parent) {
		import93CSV(parent);
	}

}

