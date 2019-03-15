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
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetInbox;
import edu.clemson.lph.civet.lookup.CertificateNbrLookup;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.webservice.CivetWebServices;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.ProgressDialog;
import edu.clemson.lph.dialogs.ThreadCancelListener;
import edu.clemson.lph.utils.FileUtils;

public class SubmitCVIsThread extends Thread implements ThreadCancelListener {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	static {
	     logger.setLevel(CivetConfig.getLogLevel());
	}
	private CivetWebServices service = null;
	private List<File> allFiles = null;
	private String sCurrentFilePath = "";
	private ProgressDialog prog;
	private String sProgTitle = "Civet: Processing File";
	private String sProgPrompt = "File: ";
	private String sOutPath;
	private CivetInbox parent = null;
	private volatile boolean bCanceled = false;
	
	public SubmitCVIsThread(CivetInbox parent, List<File> files) {
		this.parent = parent;
		this.allFiles = files;
		sOutPath = CivetConfig.getOutputDirPath();
		prog = new ProgressDialog(parent, sProgTitle, sProgPrompt );
		prog.setCancelListener(this);
		prog.setAuto(true);
		prog.setVisible(true);
		service = new CivetWebServices();
	}
	
	@Override
	public void cancelThread() {
		bCanceled = true;
		interrupt();
	}

	@Override
	public void run() {
		try {
			for( File fThis : allFiles ) {
				if( bCanceled )
					break;
				final String sName = fThis.getName();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						prog.setMessage(sProgPrompt + sName);
					}
				});		
				sCurrentFilePath = fThis.getAbsolutePath();
				if( !processFile( fThis ) ) {
					continue;
				}
				if( CivetConfig.isSaveCopies() ) {
					File fOut = new File( sOutPath, sName );
	    			while( fOut.exists() ) {
	    				String sOutPath = fOut.getAbsolutePath();
	    				sOutPath = FileUtils.incrementFileName(sOutPath);
	    				fOut = new File( sOutPath );
	    				MessageDialog.showMessage(parent, "Civet Error", fOut.getAbsolutePath() + " already exists in OutBox.\n" +
	    						"Saving as " + sOutPath);
	    			}
    				boolean success = fThis.renameTo(fOut);
    				if (!success) {
    					MessageDialog.showMessage(parent, "Civet Error", "Could not move " + fThis.getAbsolutePath() + " to " + fOut.getAbsolutePath() );
	    			}
				}
				else {
					fThis.delete();
				}
			}
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					doLater();
				}
			});		
		} catch(Exception e){
			logger.error("\nError processing file " + sCurrentFilePath, e );
		}
		finally {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					parent.refreshTables();
					prog.setVisible(false);
				}
			});		
		}
	}

	private boolean processFile(File fThis) {
		boolean bRet = false;
		try {
			String sXML = FileUtils.readTextFile(fThis);
			final String sCertNbr = getCertNbr( sXML );
			// Check but don't add yet.
			if( CertificateNbrLookup.certficateNbrExists(sCertNbr) ) {
				MessageDialog.showMessage(parent, "Civet Error", "Certificate Number " + sCertNbr + " already exists.\n" +
						"Resolve conflict and try again.");
				return false;
			}
			String sRet = service.sendCviXML(sXML);
			final String sReturn = sRet;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					logger.info("Return code from Civet WS: " + sReturn);
				}
			});		
			// If successfully sent, record the number in CertNbrs.txt
			System.out.println( service.getSuccessMessage() );
			if( sRet != null && !sRet.toLowerCase().contains("error") && sRet.contains(service.getSuccessMessage() ) ) {
				bRet = true;
				if( !CertificateNbrLookup.addCertificateNbr(sCertNbr) ) {
					MessageDialog.showMessage(parent, "Civet Error", "Certificate Number " + sCertNbr + " Added twice.\n" +
							"Please report to developer.");
				}
			}
			else {  // Should have thrown an exception but just in case.
				MessageDialog.showMessage(parent, "Civet Error", "Certificate Number " + sCertNbr + " failed to upload.\n See Civet.log");
				bRet = false;
			}
		} catch (final Exception e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					logger.error("Error in uploading file to USAHERDS", e);
					if( e.getMessage().contains("There was an exception running the extensions") ) {
						MessageDialog.showMessage(parent, "Civet Error", "Error Uploading.\nCheck the size of your scanned PDFs");
					}
					if( e.getMessage().contains("Authorization has been denied") ) {
						MessageDialog.showMessage(parent, "Civet Error", "Upload Permission Denied");
					}
					else {
						MessageDialog.showMessage(parent, "Civet Error",  "Certificate failed to upload.\nSee Civet.log");						
					}
				}
			});
			bRet = false;
		} 
		return bRet;
	}
	
	private String getCertNbr( String sXML ) {
		if( sXML == null || sXML.trim().length() == 0 ) return null;
		int iStart = sXML.indexOf("CviNumber=") + 11;
		int iEnd = sXML.substring(iStart).indexOf('\"');
		return sXML.substring(iStart, iStart+iEnd);
	}

	private void doLater() {
		// TODO Auto-generated method stub

	}

}
