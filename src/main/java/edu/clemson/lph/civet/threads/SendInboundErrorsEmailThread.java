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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.mail.AuthenticationFailedException;
import javax.swing.SwingUtilities;

import edu.clemson.lph.logging.Logger;

import edu.clemson.lph.mailman.*;
import edu.clemson.lph.pdfgen.CodeSource;
import edu.clemson.lph.pdfgen.PDFGen;
import edu.clemson.lph.utils.FileUtils;
import edu.clemson.lph.utils.StringUtils;

import edu.clemson.lph.civet.CivetInbox;
import edu.clemson.lph.civet.lookup.ErrorTypeLookup;
import edu.clemson.lph.civet.lookup.StateVetLookup;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.xml.CviMetaDataXml;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.dialogs.*;

public 
class SendInboundErrorsEmailThread extends Thread implements CodeSource {
      private static Logger logger = Logger.getLogger();
	private ProgressDialog prog;
	private CivetInbox parent;
	private HashMap<String, ArrayList<File>> mStateMap;
	private static String sInBoundCVIErrorMessage = null;
	private StateVetLookup stateVet = null;
	private String sCurrentState = null;
	private CviMetaDataXml cviMetaData = null;
	private String sCurrentCVINumber;
	private ArrayList<File> aSentCVIFiles;
	private String sCurrentEmailError = "";
	
	public SendInboundErrorsEmailThread( CivetInbox parent, ProgressDialog prog ) {
		this.parent = parent;
		this.prog = prog;
		mStateMap = new HashMap<String, ArrayList<File>>();
	}

	public void run() {
		String sEmailOutDir = CivetConfig.getEmailErrorsDirPath();
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
			if( aEmailOutFiles != null ) {
				for( File fNext : aEmailOutFiles ) {
					if( fNext != null && fNext.exists() && fNext.isFile() ) {
						String sName = fNext.getName();
						if( sName.toLowerCase().endsWith(".cvi") ) {
							String sParts[] = sName.split("\\_");
							if( sParts.length < 4 )
								throw new Exception(sName + " is not configured as email file.");
							String sFromState = sParts[1];
							ArrayList<File> aStatePdfs = mStateMap.get(sFromState);
							if( aStatePdfs == null ) {
								aStatePdfs = new ArrayList<File>();
								mStateMap.put(sFromState, aStatePdfs);
							}
							aStatePdfs.add(fNext);
							iFiles++;
						}
					}
				}
			}
			if( iFiles == 0 ) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						prog.setVisible(false);
						prog.dispose();
						MessageDialog.showMessage(parent, "Civet: Error", "No CVI files found to send");
					}
				});
				return;
			}
			// Now state by state process them.
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
				sCurrentState = sState;
				stateVet = new StateVetLookup( sState );
				String sCurrentEmail = stateVet.getCVIErrorEmail(); 
				ArrayList<File> aCVIsIn = mStateMap.get(sState);
				ArrayList<byte[]> aLetterBytes = new ArrayList<byte[]>();
				ArrayList<StdeCviXmlModel> aCVIsOut = new ArrayList<StdeCviXmlModel>();
				ArrayList<File> aCVIFilesOut = new ArrayList<File>();
				long lAttachmentsSize = 0;
				int iPart = 1;
				int iPdf = 1; // count to bail on last one.
				for( File fNext : aCVIsIn ) {
					byte[] xmlBytes = FileUtils.readUTF8File(fNext);
					StdeCviXmlModel stdXml = new StdeCviXmlModel( xmlBytes );
					byte[] pdfBytes = stdXml.getPDFAttachmentBytes();
					if( pdfBytes == null || pdfBytes.length < 1 ) {
						MessageDialog.showMessage(parent, "Civet Error: Missing Attachment", "Missing PDF Attachment");
						throw new Exception("Missing CVI attachment in send email");
					}
					// Populate this for use in PdfGen/lookupCode
					cviMetaData = stdXml.getMetaData();
					sCurrentCVINumber = stdXml.getCertificateNumber();
					if( cviMetaData == null )
						throw new Exception("Null metadata in send errors");
					aCVIsOut.add(stdXml);
					aCVIFilesOut.add(fNext);
					// Generate letter
					PDFGen pdfGenerator = new PDFGen();
					pdfGenerator.setCodeSource(this);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					pdfGenerator.printDoc(CivetConfig.getImportErrorsLetterTemplate(), bos);
					byte[] letterBytes = bos.toByteArray(); 
					aLetterBytes.add(letterBytes);
					lAttachmentsSize += pdfBytes.length + letterBytes.length;
					// Three reasons to pack up and send.  #, Total Size, No more.
					iPdf++;
					if( aCVIsOut.size() >= 5 || lAttachmentsSize > CivetConfig.getMaxAttachSize() || iPdf >= aCVIsIn.size() ) {					
						if( sCurrentEmail == null || !sCurrentEmail.contains("@") ) {
							MessageDialog.showMessage(prog.getWindowParent(), "Civet: Email", "No email address for state " +
									sState + " be sure to mail physical copies");
							aSentCVIFiles.addAll(aCVIFilesOut);
							iUnsent += aCVIFilesOut.size();
						}
						else {
							// This will throw AuthenticationFailedException if User/Password incorrect.
								if( sendInboundErrorPackage( sCurrentEmail, sState, aCVIsOut, aLetterBytes, iPart ) ) {
									aSentCVIFiles.addAll(aCVIFilesOut);
									iPart++;
								}
								else {
									String sAddress = CivetConfig.getEmailTestTo();
									if( sAddress == null ) sAddress = sCurrentEmail;
									MessageDialog.showMessage(prog.getWindowParent(), "Civet: Message Failed",
											"EMail Failed to " + sState + " at " + sAddress + "\n" + sCurrentEmailError);
									sCurrentEmailError = "";
									iUnsent += aCVIFilesOut.size();
								}
						}
						aCVIFilesOut.clear();
						aCVIsOut.clear();
						aLetterBytes.clear();
						lAttachmentsSize = 0;
					} // end if we need to send now
				} // end for each PDF
				for( File f : aSentCVIFiles ) {
					f.delete();
				}
				if( aSentCVIFiles.size() > 0 ) {
					sbCVICounts.append("\n     " + (aSentCVIFiles.size() - iUnsent) + " CVIs to " + sState);
				}
			} // end for each state
			if( sbCVICounts != null && sbCVICounts.toString().trim().length() > 0 ) {
				MessageDialog.messageLater( prog.getWindowParent(), "Civet: Messages Sent", 
						"Successfully sent: " + sbCVICounts.toString() );
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
		} finally {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					parent.refreshTables();
					prog.setVisible(false);
					prog.dispose();
				}
			});
		}
	}

	private boolean sendInboundErrorPackage( String sEmail, String sState, ArrayList<StdeCviXmlModel> aStdCvis, 
			                                 ArrayList<byte[]> aLetterBytes, int iPart ) 
			                                		 throws AuthenticationFailedException, Exception {
		boolean bRet = false;
		if( aLetterBytes.size() != aStdCvis.size() )
			throw new Exception( "Missmatched letters with CVIs in send errors");
		if( sInBoundCVIErrorMessage == null ) {
			try {
				File fIn = new File( CivetConfig.getImportErrorsEmailTemplate() );
				sInBoundCVIErrorMessage = FileUtils.readTextFile( fIn );
			} catch (FileNotFoundException fnf) {
				MessageDialog.messageLater(prog.getWindowParent(), "Civet: Template File Missing",
                                       "Cannot find template file " + CivetConfig.getImportErrorsEmailTemplate());
				return false;
			}
		}
		try {
			// For testing, replace real destination with test email
			String sTestEmail = CivetConfig.getEmailTestTo();
			if ( sTestEmail != null && sTestEmail.trim().length() > 0 )
				sEmail = sTestEmail;  
			ArrayList<MIMEFile> aFiles = new ArrayList<MIMEFile>();
			for( int i = 0; i < aStdCvis.size(); i++ ) {
				StdeCviXmlModel stdXml = aStdCvis.get(i);
				byte letterBytes[] = aLetterBytes.get(i);
				byte pdfBytes[] = stdXml.getPDFAttachmentBytes();
				String sCVINo = stdXml.getCertificateNumber();
				// Attach PDF
				aFiles.add(new MIMEFile( sCVINo + "_Cover.pdf","application/pdf",letterBytes));
				aFiles.add(new MIMEFile(sCVINo + "_Not_Accepted.pdf","application/pdf",pdfBytes));
			}
			String sFileCopyAddress = CivetConfig.getEmailCopyTo();
			String sHomeState = CivetConfig.getHomeState();
			bRet = MailMan.sendIt(sEmail, sFileCopyAddress, 
					"CVIs With Errors From " + sState + " to " + sHomeState + (iPart>1?" Part " + iPart:""),
					sInBoundCVIErrorMessage, aFiles);
		} catch (javax.mail.AuthenticationFailedException eAuth) {
			throw eAuth;
		} catch (MailException me) {
			sCurrentEmailError = me.getMessage();
			logger.error(me.getMessage() + "\nInvalid MIMEFile Specification\n" + me.getMessage() );
			bRet = false;
		}  catch( Exception e ) {
			sCurrentEmailError = e.getMessage();
			logger.error("Could not send error ", e);
			bRet = false;
		}
		return bRet;
	}
	
	
	public String lookupCode(String sCode) {
		// Either of these should be impossible
		if( sCode == null ) return null;
		if( sCurrentState == null ) return null;
		// First look for any class member values, easy.
		if( sCode.equals("OtherState") ) return sCurrentState;
		if( sCode.equals("CVINumber") ) {
			if( sCurrentCVINumber == null ) sCurrentCVINumber = "Not Available";
			return sCurrentCVINumber;
		}
		if( sCode.equals("Date") ) {
			GregorianCalendar cal = new GregorianCalendar();
			SimpleDateFormat df = new SimpleDateFormat("MMMM dd, yyyy");
			String sDate = df.format(cal.getTime());
			return sDate;
		}
		// First look for the generic items.
        ArrayList<String> codes = new ArrayList<String>( Arrays.asList("OtherStateVet", "StateVetLName", "Address",
            	"City", "StateCode", "ZipCode", "StateName"));
        if( codes.contains(sCode) ) {
        	if( "OtherStateVet".equals(sCode) ) {
        		String prefix = stateVet.getPrefix();
        		if( prefix == null || prefix.trim().length() == 0 ) 
        			prefix = "";
        		else
        			prefix = prefix + " ";
        		String lName = stateVet.getLastName();
        		String fName = stateVet.getFirstName();
        		return  prefix + fName + " " + lName;
        	}
        	else if( "StateVetLName".equals(sCode) ) {
        		return stateVet.getLastName();
        	}
        	else if( "Address".equals(sCode) ) {
        		return stateVet.getAddress();
        	}
        	else if( "City".equals(sCode) ) {
        		return stateVet.getCity();
        	}
        	else if( "ZipCode".equals(sCode) ) {
        		return stateVet.getZipCode();
        	}
        	else if( "StateName".equals(sCode) ) {
        		String sState = stateVet.getState();
        		sState = StringUtils.toTitleCase( sState );
        		return sState;
        	}
        	else if( "StateCode".equals(sCode) ) {
        		return stateVet.getStateCode();
        	}
        }
		if( "Errors".equals(sCode) ) {
			StringBuilder sb = new StringBuilder();
			ArrayList<String> aErrors = cviMetaData.listErrors();
			for( String sError : aErrors ) {
				String sReason = ErrorTypeLookup.getDescriptionForErrorKey(sError);
				String sNotes = cviMetaData.getErrorNote();
				sb.append("  " + sReason);
				if( "Other".equalsIgnoreCase(sReason) )
					sb.append(": " + sNotes);
				sb.append("\n");
			}
			return sb.toString();
		}
		return null;
	}



}// End class SendOutboundCVIEmailThread


