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
import java.util.List;

import javax.swing.SwingUtilities;

import edu.clemson.lph.civet.webservice.CivetWebServices;

public class SubmitCVIsThread extends ProcessFilesThread {
	private CivetWebServices service = null;
	public SubmitCVIsThread(CivetInbox parent, List<File> files) {
		super(parent, files);
		service = new CivetWebServices();
	}

	@Override
	protected void processFile(File fThis) {
		try {
			BufferedReader reader = new BufferedReader( new FileReader( fThis ) );
			StringBuffer sb = new StringBuffer();
			String sLine = reader.readLine();
			while( sLine != null ) {
				sb.append(sLine);
				sLine = reader.readLine();
			}
			reader.close();
			String sXML = sb.toString();
			String sRet = service.sendCviXML(sXML);
	System.out.println( sRet );
			if( sRet == null || sRet.toLowerCase().contains("error") )
				throw new Exception( "Error from web service\n" + sRet);
			
		} catch (final Exception e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					logger.error("Error in process file", e);
				}
			});		
		}
		
	}

	@Override
	protected void doLater() {
		// TODO Auto-generated method stub

	}

}
