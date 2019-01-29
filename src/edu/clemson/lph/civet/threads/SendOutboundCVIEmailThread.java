package edu.clemson.lph.civet.threads;
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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.swing.SwingUtilities;

import org.apache.log4j.*;

import edu.clemson.lph.mailman.*;
import edu.clemson.lph.pdfgen.PDFOpener;
import edu.clemson.lph.pdfgen.PDFUtils;
import edu.clemson.lph.utils.FileUtils;
import edu.clemson.lph.civet.CVIFileController;
import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetInbox;
import edu.clemson.lph.civet.lookup.StateVetLookup;
import edu.clemson.lph.civet.lookup.States;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.xml.StdeCviXmlV1;
import edu.clemson.lph.dialogs.*;

public class SendOutboundCVIEmailThread extends Thread {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private static String sOutBoundCVIMessage = null;
	private ProgressDialog prog;
	private CivetInbox parent;
	private HashMap<String, ArrayList<File>> mStateMap;
	private ArrayList<File> aSentCVIFiles;
	private String sCurrentEmailError = "";
	
	public SendOutboundCVIEmailThread( CivetInbox parent, ProgressDialog prog ) {
		this.parent = parent;
		this.prog = prog;
		mStateMap = new HashMap<String, ArrayList<File>>();
	}
	void pdfView( byte pdfBytes[] ) {
		PDFOpener opener = new PDFOpener(parent);
		opener.openPDFContentInAcrobat(pdfBytes);
	}

	public void run() {
		String sEmailOutDir = CivetConfig.getEmailOutDirPath();
		StringBuffer sbCVICounts = new StringBuffer();
		int iFiles = 0;
		int iUnsent = 0;
		try {
			File fEmailOutDir = new File( sEmailOutDir );
			if( fEmailOutDir == null || !fEmailOutDir.isDirectory() ) {
				throw new Exception(sEmailOutDir + " is not a directory");
			}
			// Go Through all Files and sort by destination state into map
			File aEmailOutFiles[] = fEmailOutDir.listFiles();
			for( File fNext : aEmailOutFiles ) {
				if( fNext != null && fNext.exists() && fNext.isFile() ) {
					String sName = fNext.getName();
					if( sName.toLowerCase().endsWith(".cvi") ) {
						String sParts[] = sName.split("\\_");
						if( sParts.length < 4 )
							throw new Exception(sName + " is not configured as email file.");
						String sToState = sParts[3];
						ArrayList<File> aStatePdfs = mStateMap.get(sToState);
						if( aStatePdfs == null ) {
							aStatePdfs = new ArrayList<File>();
							mStateMap.put(sToState, aStatePdfs);
						}
						aStatePdfs.add(fNext);
						iFiles++;
					}
				}
			}
			if( iFiles == 0 ) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						prog.setVisible(false);
						prog.dispose();
						MessageDialog.showMessage(parent, "Civet: Error", "No files found to send");
					}
				});
				return;
			}
			ArrayList<String> aStates = new ArrayList<String>();
			for( String sState : mStateMap.keySet() ) {
				aStates.add(sState);
			}
			aStates.sort(new java.util.Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub
					return o1.compareTo(o2);
				}
			});
			for( String sState : aStates ) {
				aSentCVIFiles = new ArrayList<File>();
				StateVetLookup stateVet = null;
				String sCurrentEmail = null; 
				String sCurrentFileType = null;
				ArrayList<File> aCVIsIn = null;
				try {
					stateVet = new StateVetLookup( sState );
					sCurrentEmail = stateVet.getCVIEmail(); 
					sCurrentFileType = stateVet.getFileType();
					aCVIsIn = mStateMap.get(sState);
				} catch( Exception e ) {
					logger.error("Error getting destination info for state: " + sState, e);
				}
				ArrayList<File> aCVIFilesOut = new ArrayList<File>();
				ArrayList<StdeCviXmlV1> aCVIsOut = new ArrayList<StdeCviXmlV1>();
				long lPDFSize = 0;
				int iPart = 1;
				int iPdf = 0; // count to bail on last one.
				for( File fNext : aCVIsIn ) {
					String sXml = FileUtils.readTextFile(fNext);
					StdeCviXmlV1 stdXml = new StdeCviXmlV1( sXml );
					byte[] pdfBytes = stdXml.getOriginalCVI();
					if( pdfBytes == null || pdfBytes.length < 1 )
						throw new Exception("Missing CVI attachment in send email");
					aCVIsOut.add(stdXml);
					aCVIFilesOut.add(fNext);
					lPDFSize += pdfBytes.length;
					// Three reasons to pack up and send.  #, Total Size, No more.
					iPdf++;
					if( aCVIsOut.size() >= 5 || lPDFSize > CivetConfig.getMaxAttachSize() || iPdf >= aCVIsIn.size() ) {					
						if( sCurrentEmail == null || !sCurrentEmail.contains("@") ) {
							MessageDialog.messageWait(prog.getWindowParent(), "Civet: Email", "No email address for state " +
									sState + " be sure to mail physical copies");
							aSentCVIFiles.addAll(aCVIFilesOut); // Assume they where sent manually.
							iUnsent += aCVIFilesOut.size();
						}
						else {
								if( sendOutboundCVIPackage( sCurrentEmail, sState, aCVIsOut, sCurrentFileType, iPart ) ) {
									aSentCVIFiles.addAll(aCVIFilesOut); 
									iPart++;
								}
								else {
									String sAddress = CivetConfig.getEmailTestTo();
									if( sAddress == null ) sAddress = sCurrentEmail;
									MessageDialog.messageWait(prog.getWindowParent(), "Civet: Message Failed",
											"EMail Failed to " + sState + " at " + sAddress + "\n" + sCurrentEmailError );
									sCurrentEmailError = "";
									iUnsent += aCVIFilesOut.size();
								}
						}
						aCVIFilesOut.clear();
						aCVIsOut.clear();
						lPDFSize = 0;
					} // end if we need to send now
				} // end for each PDF
				for( File f : aSentCVIFiles ) {
					f.delete();
				}
				if( aSentCVIFiles.size() > 0 ) {
					sbCVICounts.append("\n     " + (aSentCVIFiles.size() - iUnsent) + " CVIs to " + sState);
				}
			} // end for each state
			MessageDialog.messageLater( prog.getWindowParent(), "Civet: Messages Sent", 
					"Successfully sent: " + sbCVICounts.toString() );
		} catch (AuthenticationFailedException e) {
			logger.error(e.getMessage() + "\nEmail Authentication Error");
			MailMan.setDefaultPassword(null);
			MailMan.setDefaultUserID(null);
		} catch (javax.mail.MessagingException e) {
			logger.error(e.getMessage() + "\nEmail Connection Error");
		} catch (Exception ex) {
			logger.error("Error Sending Email", ex );
		} 
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				parent.refreshTables();
				prog.setVisible(false);
				prog.dispose();
			}
		});
	}

	private boolean sendOutboundCVIPackage( String sEmail, String sState, 
				ArrayList<StdeCviXmlV1> aCVIs, String sCurrentFileType, int iPart ) throws AuthenticationFailedException {
		boolean bRet = false;
		String sTemplateFile = null;
		if( sOutBoundCVIMessage == null ) {
			try {
				sTemplateFile = CivetConfig.getExportMailTemplate();
				File fIn = new File( sTemplateFile );
				sOutBoundCVIMessage = FileUtils.readTextFile( fIn );
			} catch (FileNotFoundException fnf) {
				MessageDialog.messageLater(prog.getWindowParent(), "Civet: Template File Missing",
                                       "Cannot find template file " + sTemplateFile);
				return false;
			} catch (Exception e) {
				MessageDialog.messageLater(prog.getWindowParent(), "Civet: Template File Error",
                        "Cannot read template file " + sTemplateFile);
				return false;
			}
		}
		String sFileName = null;
		try {
			// For testing, replace real destination with test email
			String sTestEmail = CivetConfig.getEmailTestTo();
			if ( sTestEmail != null && sTestEmail.trim().length() > 0 )
				sEmail = sTestEmail;  
			ArrayList<MIMEFile> aFiles = new ArrayList<MIMEFile>();
			for( StdeCviXmlV1 thisCVI : aCVIs) {
				byte pdfBytes[] = thisCVI.getOriginalCVI();
				boolean bXFA = PDFUtils.isXFA(pdfBytes);
				if( "CVI".equalsIgnoreCase(sCurrentFileType) && !bXFA ) {
					// if the receiving state wants .CVI files we need to do some clean up
					sFileName = thisCVI.getOriginState() + "_To_" + thisCVI.getDestinationState() + 
							"_" + thisCVI.getCertificateNumber() + ".cvi";
					// Remove 8 character State Premises Identifiers that may or may not be LIDs
					if( CivetConfig.hasBrokenLIDs() )
						thisCVI.purgeLids();
					String sThisCvi = thisCVI.getXMLString();
					byte cviBytes[] = sThisCvi.getBytes("UTF-8");
					aFiles.add(new MIMEFile(sFileName,"text/xml",cviBytes));
				}
				else {
					// Otherwise, we extract the original PDF as attached to the .cvi file
					sFileName = thisCVI.getOriginState() + "_To_" + thisCVI.getDestinationState() + 
							"_" + thisCVI.getCertificateNumber() + ".pdf";
					String sCVIFilename = thisCVI.getOriginalCVIFileName();
					MIMEFile mMCviDataFile = getMCviDataFile(sCVIFilename);
					if( mMCviDataFile != null )
						aFiles.add(mMCviDataFile);
					aFiles.add(new MIMEFile(sFileName,"application/pdf",pdfBytes));
				}
			}
			String sFileCopyAddress = CivetConfig.getEmailCopyTo();
			String sHomeState = CivetConfig.getHomeState();
			bRet = MailMan.sendIt(sEmail, sFileCopyAddress,
					 "CVIs From " + sHomeState + " to " + sState + (iPart>1?" Part " + iPart:""),
					sOutBoundCVIMessage, aFiles);
//			logger.info("Email sent to: " + sEmail + " at " + sState + " returned " + bRet);
		} catch (AuthenticationFailedException e1) {
			sCurrentEmailError = e1.getMessage();
			MessageDialog.messageWait( prog.getWindowParent(), "Civet: Invalid UserID/Password", 
					"Authentication failure to Email system:\n" + CivetConfig.getSmtpHost());
			MailMan.setDefaultUserID( null );
			MailMan.setDefaultPassword( null );
			// NOTE: This is still not 100% right.  If an authentication error happens AFTER some have been sent
			// those won't be handled before this except exists the enclosing try.
			throw( e1 );
		} catch (MailException me) {
			logger.error(me.getMessage() + "\nInvalid MIMEFile Specification" );
			sCurrentEmailError = me.getMessage();
			bRet = false;
		} catch (MessagingException e) {
			logger.error("Could not connect to email server", e);
			e.printStackTrace();
			sCurrentEmailError = e.getMessage();
			bRet = false;
		} catch (Exception e) {
			logger.error("Could not send file " + sFileName, e);
			sCurrentEmailError = e.getMessage();
			bRet = false;
		}
		return bRet;
	}
	
	/**
	 * The original filename has been decorated by now so we jump through hoops to see
	 * if the matching datafile exists in outbox.  And pull it out and rename it if it does.
	 * @param sOrginalCVIFilename
	 * @return
	 */
	private MIMEFile getMCviDataFile( String sCVIFilename ) {
		MIMEFile mRet = null;
		try {
			// Check for mCVI data bundle in outbox.
			String sOriginalCVIFilename = sCVIFilename.substring(sCVIFilename.lastIndexOf('_')+1);
			if( sOriginalCVIFilename.contains("(")) {
				sOriginalCVIFilename = sOriginalCVIFilename.substring(0,sOriginalCVIFilename.lastIndexOf('('));
				sOriginalCVIFilename = sOriginalCVIFilename + ".pdf";
				String sStateCode = CivetConfig.getHomeStateAbbr();
				if( !sOriginalCVIFilename.startsWith(sStateCode + "-") )
					sOriginalCVIFilename = sStateCode + "-" + sOriginalCVIFilename;
			}
			File dir = new File(CivetConfig.getOutputDirPath());
			File fCVI = new File(dir, sOriginalCVIFilename);
			String sMCviDataFilename = CVIFileController.getMCviDataFilename(fCVI);
			File fMCviData = new File( sMCviDataFilename);
			if( fMCviData.exists() ) {
				byte mCviDataBytes[] = FileUtils.readBinaryFile(fMCviData);
				sMCviDataFilename = sCVIFilename.substring(0,sCVIFilename.lastIndexOf('.')) + ".XML";
				mRet =  new MIMEFile(sMCviDataFilename, "text/xml", mCviDataBytes);
			}	
		} catch( Exception e ) {
			logger.error(e);
		}
		return mRet;
	}
 
}// End class SendOutboundCVIEmailThread
