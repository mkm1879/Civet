package edu.clemson.lph.civet.robot;
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
import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetInbox;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.webservice.CivetWebServiceFactory;
import edu.clemson.lph.civet.webservice.CivetWebServices;
import edu.clemson.lph.civet.webservice.CivetWebServicesNew;
import edu.clemson.lph.civet.xml.CoKsXML;
import edu.clemson.lph.civet.xml.CviMetaDataXml;
import edu.clemson.lph.civet.xml.StdeCviXml;
import edu.clemson.lph.civet.xml.V2Transform;
import edu.clemson.lph.pdfgen.PDFUtils;
import edu.clemson.lph.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Node;


public class COKSRobot extends Thread {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private File inDir;
	private File compDir;
	private File xmlDir;
	private boolean bStd;
	private boolean bCancel = false;
	private boolean bLoggedIn = false;
	
	public COKSRobot() throws IOException {
		inDir = new File( CivetConfig.getRobotInputPath() );
		if( !inDir.isDirectory() )
			throw new IOException( "Robot Input Path is not a directory\n" + inDir.getAbsolutePath());
		compDir = new File( CivetConfig.getRobotCompleteOutPath() );
		if( !compDir.isDirectory() )
			throw new IOException( "Robot Complete Path is not a directory\n" + compDir.getAbsolutePath());
		xmlDir = new File( CivetConfig.getRobotXMLOutPath() );
		if( !xmlDir.isDirectory() )
			throw new IOException( "Robot XML Path is not a directory\n" + xmlDir.getAbsolutePath());
		String sFormat = CivetConfig.getRobotOutputFormat();
		if( CivetConfig.getHERDSUserName() != null && CivetConfig.getHERDSPassword() != null )
			bLoggedIn = true;
		if( sFormat.equals("STD") ) 
			bStd = true;
		else if( sFormat.equals("ADOBE") )
			bStd = false;
		else
			throw new RuntimeException("Robot mode needs robotOutputFormat set in CivetConfig.txt to either STD or ADOBE");

	}
	
	public void cancel() {
		bCancel = true;
	}
	
	private boolean isMCvi( File f ) {
		boolean bRet = false;
		String sDataFile = FileUtils.getRoot(f.getAbsolutePath()) + ".xml";
		System.out.println(sDataFile);
		File fData = new File(sDataFile);
		if( fData.exists() && fData.isFile() )
			bRet = true;
		return bRet;
	}
	
	public static void main( String args[] ) {
		PropertyConfigurator.configure("CivetConfig.txt");
		CivetConfig.checkAllConfig();
		try {
			COKSRobot me = new COKSRobot();
			File f = new File("../../Documents/CivetTest/CivetInBox/99-1117582044-1536259117.pdf");
			System.out.println(me.isMCvi(f));
		} catch (IOException e) {
			e.printStackTrace();;
		}
		
	}
	
	private void processFiles() {
		File[] files = inDir.listFiles( new FileFilter() {
			@Override
			public boolean accept(File arg0) {
				if( arg0.isFile() && arg0.getName().toLowerCase().endsWith(".pdf") )
					return true;
				else
					return false;
			}
		});
		java.util.Date now = new java.util.Date();
		for( File file : files ) {
			File fXMLOut = new File( xmlDir, file.getName() + ".cvi" );
			File fDataFile = null;
			try {
				byte[] pdfBytes = FileUtils.readBinaryFile( file );
				boolean bXFA = PDFUtils.isXFA(pdfBytes);
				boolean bMCvi = isMCvi(file);
				String sStdXML = null;
				String sAdobeXML = null;
				StdeCviXml stdXml = null;
				if( bXFA || bMCvi ) {
					if( bXFA ) {
						Node xmlNode = PDFUtils.getXFADataNode(pdfBytes);
						CoKsXML coks = new CoKsXML( xmlNode );
						stdXml = coks.getStdeCviXml();
						stdXml.setOriginalCVI( pdfBytes, file.getName() );
						CviMetaDataXml metaData = new CviMetaDataXml();
						metaData.setBureauReceiptDate( now );
						stdXml.addMetadataAttachement( metaData );
						if( bStd )
							sStdXML = stdXml.getXMLString();
						else
							sAdobeXML = coks.toAcrobatXMLString();
					}
					else if( bMCvi ) {
						String sDataFile = FileUtils.getRoot(file.getAbsolutePath()) + ".xml";
						fDataFile =  new File(sDataFile);
						String sV2XML = FileUtils.readTextFile(fDataFile);
						sStdXML = V2Transform.convertToV1(sV2XML);
						stdXml = new StdeCviXml( sStdXML );
						stdXml.setOriginalCVI( pdfBytes, file.getName() );
						CviMetaDataXml metaData = new CviMetaDataXml();
						metaData.setBureauReceiptDate( now );
						stdXml.addMetadataAttachement( metaData );
						sStdXML = stdXml.getXMLString();
					}
					if( bStd ) {
						if( bLoggedIn ) {
							try {
								CivetWebServices service = CivetWebServiceFactory.getService();
								String sRet = service.sendCviXML(sStdXML);
								if( !sRet.contains(service.getSuccessMessage()) ) {
									saveToXml(fXMLOut, sStdXML);
									logger.error("Could not submit to HERDS\nSaving to Robot XML folder");
									logger.info("Processed File: " + file.getName() + " to: " + fXMLOut.getName() );
									bLoggedIn = false;
								}
								logger.info("Processed File: " + file.getName() + " with CVI: " + stdXml.getCertificateNumber() +
										" issued in " + stdXml.getOriginState() );
							} catch( RemoteException re ) {
								saveToXml(fXMLOut, sStdXML);
								logger.error("Could not submit to HERDS\nSaving to Robot XML folder", re);
								logger.info("Processed File: " + file.getName() + " to: " + fXMLOut.getName() );
								bLoggedIn = false;
							}
						}
						else {
							saveToXml(fXMLOut, sStdXML);
							logger.info("Processed File: " + file.getName() + " with CVI: " + stdXml.getCertificateNumber() +
									" issued in " + stdXml.getOriginState() );
						}
					}
					else {
						saveToXml(fXMLOut, sAdobeXML);
						logger.info("Processed File: " + file.getName() + " to: " + fXMLOut.getName() );
					}
				}
				else {
					logger.error( "File " + file.getName() + " was not a CO/KS XFA or mCVI form");
				}
				// Move Completed File to the compDir
				File fPathOut = new File( compDir.getAbsolutePath() );
				File fout = new File( fPathOut, fDataFile.getName() );
				if( fout.exists() ) {
					String sFOutPath = FileUtils.incrementFileName(fout.getAbsolutePath());
					fout = new File( sFOutPath );
				}
				file.renameTo(fout);
				if( bMCvi ) {
					File fDataOut = new File( fPathOut, file.getName() );
					if( fDataOut.exists() ) {
						String sFOutPath = FileUtils.incrementFileName(fDataOut.getAbsolutePath());
						fDataOut = new File( sFOutPath );
					}
					fDataFile.renameTo(fDataOut);
				}
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("Error writing xml file " + fXMLOut.getAbsolutePath(), e);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Error reading bytes from file " + file.getAbsolutePath(), e);
			}
		}
	}
	
	private void saveToXml(File fXML, String sXML) throws UnsupportedEncodingException, IOException {
		FileOutputStream sOut = new FileOutputStream( fXML );
		sOut.write(sXML.getBytes("UTF-8"));
		sOut.flush();
		sOut.close();
	}
	
	@Override
	public void run() {
		long lWait = CivetConfig.getRobotWaitSeconds() * 1000;
        while( !bCancel ) {
            synchronized (this) {
              try {
            	  wait(lWait);
            	  processFiles();
              }
              catch (InterruptedException ex) {
              }
            }
        }
	}

}
