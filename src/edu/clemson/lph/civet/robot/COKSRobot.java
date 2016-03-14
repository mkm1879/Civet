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
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.webservice.CivetWebServices;
import edu.clemson.lph.civet.xml.CoKsXML;
import edu.clemson.lph.civet.xml.CviMetaDataXml;
import edu.clemson.lph.civet.xml.StdeCviXml;
import edu.clemson.lph.pdfgen.PDFUtils;
import edu.clemson.lph.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
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
			File fXML = new File( xmlDir, file.getName() + ".cvi" );
			try {
				byte[] pdfBytes = FileUtils.readBinaryFile( file );
				if( PDFUtils.isXFA(pdfBytes) ) {
					Node xmlNode = PDFUtils.getXFADataNode(pdfBytes);
					CoKsXML coks = new CoKsXML( xmlNode );
					String sXML = null;
					if( bStd ) {
						StdeCviXml stdXml = coks.getStdeCviXml();
						stdXml.setOriginalCVI( pdfBytes, file.getName() );
						CviMetaDataXml metaData = new CviMetaDataXml();
						metaData.setBureauReceiptDate( now );
						stdXml.addMetadataAttachement( metaData );
						sXML = stdXml.getXMLString();
						if( bLoggedIn ) {
							try {
								CivetWebServices herds = new CivetWebServices();
								String sRet = herds.sendCviXML(sXML);
								if( !sRet.startsWith(CivetWebServices.CVI_SUCCESS_MESSAGE) ) {
									saveToXml(fXML, sXML);
									logger.error("Could not submit to HERDS\nSaving to Robot XML folder");
									logger.info("Processed File: " + file.getName() + " to: " + fXML.getName() );
									bLoggedIn = false;
								}
								logger.info("Processed File: " + file.getName() + " with CVI: " + stdXml.getCertificateNumber() +
										" issued in " + stdXml.getOriginState() );
							} catch( RemoteException re ) {
								saveToXml(fXML, sXML);
								logger.error("Could not submit to HERDS\nSaving to Robot XML folder", re);
								logger.info("Processed File: " + file.getName() + " to: " + fXML.getName() );
								bLoggedIn = false;
							}
						}
						else {
							saveToXml(fXML, sXML);
							logger.info("Processed File: " + file.getName() + " with CVI: " + stdXml.getCertificateNumber() +
									" issued in " + stdXml.getOriginState() );
						}
					}
					else {
						sXML = coks.toAcrobatXMLString();
						saveToXml(fXML, sXML);
						logger.info("Processed File: " + file.getName() + " to: " + fXML.getName() );
					}
				}
				else {
					logger.error( "File " + file.getName() + " was not a CO/KS XFA form");
				}
				// Move Completed File to the compDir
				File fPathOut = new File( compDir.getAbsolutePath() );
				File fout = new File( fPathOut, file.getName() );
				if( fout.exists() ) {
					String sFOutPath = FileUtils.incrementFileName(fout.getAbsolutePath());
					fout = new File( sFOutPath );
				}
				file.renameTo(fout);
			} catch (IOException e) {
				logger.error("Error writing xml file " + fXML.getAbsolutePath(), e);
			} catch (Exception e) {
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
