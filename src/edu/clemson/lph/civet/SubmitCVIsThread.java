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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.swing.SwingUtilities;

import edu.clemson.lph.civet.lookup.CertificateNbrLookup;
import edu.clemson.lph.civet.webservice.CivetWebServices;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.utils.FileUtils;

public class SubmitCVIsThread extends ProcessFilesThread {
	private CivetWebServices service = null;
	public SubmitCVIsThread(CivetInbox parent, List<File> files) {
		super(parent, files);
		service = new CivetWebServices();
	}

	@Override
	protected void processFile(File fThis) {
		try {
			String sXML = FileUtils.readTextFile(fThis);
			String sCertNbr = getCertNbr( sXML );
			if( !CertificateNbrLookup.addCertificateNbr(sCertNbr) ) {
				MessageDialog.messageLater(parent, "Civet Error", "Certificate Number " + sCertNbr + " already exists.\n" +
						"Resolve conflict and try again.");
				return;
			}
			String sRet = service.sendCviXML(sXML);
			if( sRet == null || sRet.toLowerCase().contains("error") )
				throw new Exception( "Error from web service\n" + sRet);
			
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

	@Override
	protected void doLater() {
		// TODO Auto-generated method stub

	}

}
