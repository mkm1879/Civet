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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.xml.StdeCviXml;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.ProgressDialog;
import edu.clemson.lph.pdfgen.MergePDF;
import edu.clemson.lph.utils.FileUtils;

public class AddPageToCviThread extends Thread {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private ProgressDialog prog;
	private CivetEditDialog dlg;
	private File currentFile;
	private byte[] rawPdfBytes;
	private byte[] pageBytes;
	private StdeCviXml stdXml = null;
	
	public AddPageToCviThread(CivetEditDialog dlg, File fAddToFile, byte[] pageBytes) {
		this.dlg = dlg;
		this.currentFile = fAddToFile;
		this.pageBytes = pageBytes;
		prog = new ProgressDialog(dlg, "Civet", "Opening CVI File " + this.currentFile.getName() );
		prog.setAuto(true);
		prog.setVisible(true);
	}
	
	public void run() {
		byte[] fileBytes;
		try {
			fileBytes = FileUtils.readBinaryFile( currentFile.getAbsolutePath() );
			String sXml = new String( fileBytes, "UTF-8" );
			stdXml = new StdeCviXml( sXml );
			rawPdfBytes = stdXml.getOriginalCVI();
			String sFileName = stdXml.getOriginalCVIFileName();
			ByteArrayInputStream basFirst = new ByteArrayInputStream( rawPdfBytes );
			ByteArrayInputStream basSecond = new ByteArrayInputStream( pageBytes );
			ArrayList<InputStream> aInputs = new ArrayList<InputStream>();
			aInputs.add(basFirst);
			aInputs.add(basSecond);
			ByteArrayOutputStream baOut = new ByteArrayOutputStream();
			MergePDF.concatPDFs(aInputs, baOut, true);
			rawPdfBytes = baOut.toByteArray();
			stdXml.setOriginalCVI(rawPdfBytes, sFileName);
			sXml = stdXml.getXMLString();
			saveXml(sXml, currentFile);
			File fEmailOut = new File( CivetConfig.getEmailOutDirPath() + currentFile.getName() );
			if( fEmailOut.exists() )
				saveXml(sXml, fEmailOut);
			File fEmailErr = new File( CivetConfig.getEmailErrorsDirPath() + currentFile.getName() );
			if( fEmailErr.exists() )
				saveXml(sXml, fEmailErr);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				dlg.saveComplete();
				prog.setVisible(false);
				prog.dispose();
			}
		});
	}
	
	private void saveXml(String sStdXml, File fOut) {
		try {
			PrintWriter pw = new PrintWriter( new FileOutputStream( fOut ) );
			pw.print(sStdXml);
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			logger.error("Could not save " + fOut.getAbsolutePath(), e);
			MessageDialog.messageLater(dlg, "Civet Error: File Save", "Could not save file\n " + fOut.getAbsolutePath() );
			return;
		}
	}

}
