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
import edu.clemson.lph.civet.CivetConfig;
import edu.clemson.lph.civet.xml.CoKsXML;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.objects.acroforms.AcroRenderer;
import org.jpedal.objects.raw.PdfDictionary;
import org.w3c.dom.Node;


public class COKSRobot extends Thread {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private File inDir;
	private File compDir;
	private File xmlDir;
	private boolean bStd;
	private boolean bCancel = false;
	
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
		if( sFormat.equals("STD") ) 
			bStd = true;
		else if( sFormat.equals("ADOBE") )
			bStd = false;
		else
			System.exit(1);

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
		PdfDecoder pdfDecoder = new PdfDecoder();
		for( File file : files ) {
			File fXML = new File( xmlDir, file.getName() + ".xml" );
			try {
				pdfDecoder.openPdfFile(file.getAbsolutePath());
				AcroRenderer rend = pdfDecoder.getFormRenderer();
				if( rend.isXFA() ) {
					Node xmlNode = rend.getXMLContentAsNode(PdfDictionary.XFA_DATASET);
					CoKsXML coks = new CoKsXML( xmlNode );
					String sXML = null;
					if( bStd )
						sXML = coks.toStdXMLString();
					else
						sXML = coks.toAcrobatXMLString();
					FileOutputStream sOut = new FileOutputStream( fXML );
					sOut.write(sXML.getBytes("UTF-8"));
					sOut.flush();
					sOut.close();
					logger.info("Processed File: " + file.getName() + " to: " + fXML.getName() );
				}
				else {
					logger.error( "File " + file.getName() + " was not a CO/KS XFA form");
				}
				pdfDecoder.closePdfFile();
				// Move Completed File to the compDir
				File fPathOut = new File( compDir.getAbsolutePath() );
				File fout = new File( fPathOut, file.getName() );
				file.renameTo(fout);
			} catch (PdfException e) {
				logger.error("Error parsing pdf file " + file.getAbsolutePath() );
			} catch (IOException e) {
				logger.error("Error writing xml file " + fXML.getAbsolutePath() );
			}
		}
	}
	
	@Override
	public void run() {
        while( !bCancel ) {
            synchronized (this) {
              try {
            	  wait(1000);
            	  processFiles();
              }
              catch (InterruptedException ex) {
              }
            }
          }

	}

	public static void main(String[] args) {
	     PropertyConfigurator.configure("CivetConfig.txt");
	     logger.setLevel(Level.ERROR);
		try {
			COKSRobot robbie = new COKSRobot();
			robbie.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
