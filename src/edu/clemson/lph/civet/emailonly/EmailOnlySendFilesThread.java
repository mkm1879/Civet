package edu.clemson.lph.civet.emailonly;
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
import javax.swing.SwingUtilities;

import org.apache.log4j.*;

import edu.clemson.lph.mailman.*;
import edu.clemson.lph.utils.FileUtils;
import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.lookup.StateVetLookup;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.dialogs.*;

class EmailOnlySendFilesThread extends Thread {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private ProgressDialog prog;
	private EmailOnlyDialog parent;
	private HashMap<String, ArrayList<File>> mStateMap;
	private static String sEmailOnlyMessage = null;
	private StateVetLookup stateVet = null;
	private ArrayList<File> aSentCVIFiles = new ArrayList<File>();
	private String sCurrentEmailError = "";
	
	EmailOnlySendFilesThread( EmailOnlyDialog parent, ProgressDialog prog ) {
		this.parent = parent;
		this.prog = prog;
		mStateMap = new HashMap<String, ArrayList<File>>();
	}

	public void run() {
		String sEmailOutDir = CivetConfig.getEmailOnlySendPath();
		int iFiles = 0;
		int iUnsent = 0;
		try {
			File fEmailOutDir = new File( sEmailOutDir );
			if( fEmailOutDir == null || !fEmailOutDir.isDirectory() ) {
				throw new Exception(sEmailOutDir + " is not a directory");
			}
			// Go Through all Files and sort by destination state into map
			File aEmailOutFiles[] = fEmailOutDir.listFiles();
			if( aEmailOutFiles != null ) {
				for( File fNext : aEmailOutFiles ) {
					if( fNext != null && fNext.exists() && fNext.isFile() ) {
						String sName = fNext.getName();
						String sParts[] = sName.split("\\_");
						if( sParts.length < 4 ) {
							MessageDialog.showMessage(parent, "Civet: Email only error", sName + " is not configured as email only file.");
							continue;
						}
						String sToState = sParts[2];
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
						MessageDialog.showMessage(parent, "Civet: Error", "No Email only files found to send");
					}
				});
				return;
			}
			// Now state by state process them.
			for( String sState : mStateMap.keySet() ) {
				stateVet = new StateVetLookup( sState );
				String sCurrentEmail = stateVet.getCVIEmail(); 
				ArrayList<File> aCVIsIn = mStateMap.get(sState);
				ArrayList<File> aCVIFilesOut = new ArrayList<File>();
				long lAttachmentsSize = 0;
				int iPart = 1;
				int iPdf = 1; // count to bail on last one.
				for( File fNext : aCVIsIn ) {
					byte[] pdfBytes = FileUtils.readBinaryFile(fNext);
					if( pdfBytes == null || pdfBytes.length < 1 )
						throw new Exception("Empty PDF attachment in send email only");
					// Populate this for use in PdfGen/lookupCode
					aCVIFilesOut.add(fNext);
					lAttachmentsSize += pdfBytes.length;
					// Three reasons to pack up and send.  #, Total Size, No more.
					iPdf++;
					if( aCVIFilesOut.size() >= 5 || lAttachmentsSize > CivetConfig.getMaxAttachSize() || iPdf >= aCVIsIn.size() ) {					
						if( sCurrentEmail == null || !sCurrentEmail.contains("@") ) {
							MessageDialog.showMessage(prog.getWindowParent(), "Civet: Email", "No email address for state " +
									sState + " be sure to mail physical copies");
							aSentCVIFiles.addAll(aCVIFilesOut);
							iUnsent += aCVIFilesOut.size();
						}
						else {
								if( sendEmailOnlyPackage( sCurrentEmail, sState, aCVIFilesOut, iPart ) ) {
									aSentCVIFiles.addAll(aCVIFilesOut);
									iPart++;
								}
								else {
									String sAddress = CivetConfig.getEmailTestTo();
									if( sAddress == null ) sAddress = sCurrentEmail;
									MessageDialog.showMessage(prog.getWindowParent(), "Civet: Message Failed",
											"EMail Failed to " + sState + " at " + sAddress + "\n" + sCurrentEmailError);
									sCurrentEmailError = "";
									// How to bail out gracefully on fatal error?
								}
						}
						aCVIFilesOut.clear();
						lAttachmentsSize = 0;
					} // end if we need to send now
				} // end for each PDF
			} // end for each state
			for( File f : aSentCVIFiles ) {
				f.delete();  // antivirus kills here somewhere
			}
			if( aSentCVIFiles.size() > 0 ) {
				StringBuffer sb = new StringBuffer();
				for( String sState : mStateMap.keySet() ) {
					sb.append(sState);
					sb.append(", ");
				}
				String sStateList = sb.toString();
				sStateList = sStateList.substring(0, sStateList.length() -2 );
				MessageDialog.messageLater( prog.getWindowParent(), "Civet: Messages Sent", 
						"Successfully sent " + (aSentCVIFiles.size() - iUnsent) + " CVIs to\n"
								+ sStateList );
			}
			File aFilesLeft[] = fEmailOutDir.listFiles();
			if( aFilesLeft.length > 0 ) {
				MessageDialog.showMessage(parent, "Civet Error: Remaining Files", "Files remaining in " + sEmailOutDir);
			}
		} catch (javax.mail.AuthenticationFailedException eAuth) {
			MailMan.setUserID(null);
			MailMan.setPassword(null);
			logger.error(eAuth.getMessage() + "\nEmail Authentication Error");
			MessageDialog.showMessage(prog.getWindowParent(), "Civet: Email Error", "Email server userID/password incorrect"
					+ "\nEmail Host: " + CivetConfig.getSmtpHost() );
		} catch (javax.mail.MessagingException e) {
			logger.error(e.getMessage() + "\nEmail Connection Error");
		} catch (Exception ex) {
			logger.error("Error Sending Email", ex );
		} 
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				prog.setVisible(false);
				prog.dispose();
			}
		});
	}

	private boolean sendEmailOnlyPackage( String sEmail, String sState, ArrayList<File> aFilesIn, int iPart ) 
			                                		 throws AuthenticationFailedException, Exception {
		boolean bRet = false;
		if( sEmailOnlyMessage == null ) {
			try {
				File fIn = new File( CivetConfig.getEmailOnlyEmailTemplate() );
				sEmailOnlyMessage = FileUtils.readTextFile( fIn );
			} catch (FileNotFoundException fnf) {
				MessageDialog.messageLater(prog.getWindowParent(), "Civet: Template File Missing",
                                       "Cannot find template file " + CivetConfig.getEmailOnlyEmailTemplate());
				return false;
			}
		}
		try {
			// For testing, replace real destination with test email
			String sTestEmail = CivetConfig.getEmailTestTo();
			if ( sTestEmail != null && sTestEmail.trim().length() > 0 )
				sEmail = sTestEmail;  
			ArrayList<MIMEFile> aFiles = new ArrayList<MIMEFile>();
			for( File fNext : aFilesIn ) {
				byte[] pdfBytes = FileUtils.readBinaryFile(fNext);
				aFiles.add(new MIMEFile( fNext.getName(),"application/pdf",pdfBytes));
			}
			String sFileCopyAddress = CivetConfig.getEmailCopyTo();
			String sHomeState = CivetConfig.getHomeState();
			String sSubject = "CVIs from " + sHomeState + " to " + sState;
			if( iPart > 1 )
				sSubject += " ( Part " + iPart + ")";
			bRet = MailMan.sendIt(sEmail, sFileCopyAddress, 
					sSubject,
					sEmailOnlyMessage, aFiles);
		} catch (AuthenticationFailedException e1) {
			throw( e1 );
		} catch (MailException me) {
			sCurrentEmailError = me.getMessage();
			logger.error(me.getMessage() + "\nInvalid MIMEFile Specification\n" + me.getMessage() );
			bRet = false;
		}  catch( Exception e ) {
			sCurrentEmailError = e.getMessage();
			logger.error("Could not send email only ", e);
			bRet = false;
		}
		return bRet;
	}

}// End class EmailOnlySendFilesThread


