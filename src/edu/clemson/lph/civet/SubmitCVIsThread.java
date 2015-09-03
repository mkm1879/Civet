package edu.clemson.lph.civet;
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
import org.apache.log4j.PropertyConfigurator;

import edu.clemson.lph.civet.lookup.CertificateNbrLookup;
import edu.clemson.lph.civet.webservice.CivetWebServices;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.ProgressDialog;
import edu.clemson.lph.utils.FileUtils;

public class SubmitCVIsThread extends Thread {
	protected static final Logger logger = Logger.getLogger(Civet.class.getName());
	static {
	     logger.setLevel(CivetConfig.getLogLevel());
	}
	private CivetWebServices service = null;
	public SubmitCVIsThread(CivetInbox parent, List<File> files) {
		this.parent = parent;
		this.allFiles = files;
		sOutPath = CivetConfig.getOutputDirPath();
		prog = new ProgressDialog(parent, sProgTitle, sProgPrompt );
		prog.setAuto(true);
		prog.setVisible(true);
		service = new CivetWebServices();
	}
	protected List<File> allFiles = null;
	protected String sCurrentFilePath = "";
	protected ProgressDialog prog;
	protected String sProgTitle = "Civet: Processing File";
	protected String sProgPrompt = "File: ";
	protected String sOutPath;
	CivetInbox parent = null;
	
	@Override
	public void run() {
		try {
			for( File fThis : allFiles ) {
				final String sName = fThis.getName();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						prog.setMessage(sProgPrompt + sName);
					}
				});		
				sCurrentFilePath = fThis.getAbsolutePath();
				processFile( fThis );
				if( CivetConfig.isSaveCopies() ) {
					File fOut = new File( sOutPath + sName );
	    			if( fOut.exists() ) {
	    				MessageDialog.messageLater(parent, "Civet Error", fOut.getAbsolutePath() + " already exists in OutBox.\n" +
	    							"Check that it really is a duplicate and manually delete.");
	    				String sOutPath = fOut.getAbsolutePath();
	    				sOutPath = FileUtils.incrementFileName(sOutPath);
	    				fOut = new File( sOutPath );
	    			}
	    			else {
	    				boolean success = fThis.renameTo(fOut);
	    				if (!success) {
	    					MessageDialog.messageLater(parent, "Civet Error", "Could not move " + fThis.getAbsolutePath() + " to " + fOut.getAbsolutePath() );
	    				}
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
		doLater();
	}

	private void processFile(File fThis) {
		try {
			String sXML = FileUtils.readTextFile(fThis);
			String sCertNbr = getCertNbr( sXML );
			// Check but don't add yet.
			if( CertificateNbrLookup.certficateNbrExists(sCertNbr) ) {
				MessageDialog.messageLater(parent, "Civet Error", "Certificate Number " + sCertNbr + " already exists.\n" +
						"Resolve conflict and try again.");
				return;
			}
			String sRet = service.sendCviXML(sXML);
			final String sReturn = sRet;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					logger.info("Return code from Civet WS: " + sReturn);
				}
			});		
			// If successfully sent, record the number in CertNbrs.txt
			if( sRet != null && !sRet.toLowerCase().contains("error") && sRet.startsWith("00") ) {
				if( !CertificateNbrLookup.addCertificateNbr(sCertNbr) ) {
					MessageDialog.messageLater(parent, "Civet Error", "Certificate Number " + sCertNbr + " Added twice.\n" +
							"Please report to developer.");
					return;
				}
			}
			else {
				throw new Exception( "Error from web service\n" + sRet);
			}
		} catch (final Exception e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					logger.error("Error in uploading file to USAHERDS", e);
					if( e.getMessage().contains("There was an exception running the extensions") ) {
						MessageDialog.showMessage(null, "Civet Error", "Error Uploading.\nCheck the size of your scanned PDFs");
					}
				}
			});		
		} 
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
