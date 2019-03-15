package edu.clemson.lph.pdfgen;
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
import edu.clemson.lph.utils.FileUtils;

import java.awt.Window;
import java.io.*;

import org.apache.log4j.Logger;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;

public class PDFOpener {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	
	/**
	 * Note: Originally intended to open viewer as child window but that failed.
	 * @param parent
	 */
	public PDFOpener( Window parent ) {
	}

	/**
	 * Opens the current Page in default PDF reader
	 */
	public void openPDFContentInAcrobat( byte[] pdfBytes ) {
		String sFileName = extractRecordToTempPDF( pdfBytes );
		Thread t = new OpenRecordInAcrobat(sFileName);
		t.start();
	}

	/**
	 * Opens the current Page in default PDF reader
	 */
	public void openPDFFileInAcrobat( String sFileName ) {
		File fIn = new File( sFileName );
		File fTemp = FileUtils.copyBinaryFile(fIn);
		Thread t = new OpenRecordInAcrobat(fTemp.getAbsolutePath());
		t.start();
	}

	public String extractRecordToTempPDF(byte[] pdfBytes) {
		String sFileOut = null;
		File fOut = null;
		try {
			fOut = File.createTempFile("TempFileCVI", ".pdf");
			sFileOut = fOut.getAbsolutePath();
			FileOutputStream fsOut = new FileOutputStream( sFileOut );
			fsOut.write(pdfBytes);
			fsOut.close();
		}
		catch (IOException ex1) {
			logger.error(ex1.getMessage() + "\nError writing " + sFileOut + " temporary file");
		}
		return sFileOut;
	}// End decode pages to new PDF

	static class OpenRecordInAcrobat extends Thread {
		String sFileName;
		int iPageNo;
		public OpenRecordInAcrobat( String sFileName ) {
			this.sFileName = sFileName;
		}

		public void run() {
			String sOs = System.getProperty("os.name");
			String sExecutable = CivetConfig.getAcrobatPath();
			// Open with default application using CMD command interpreter to run "start filename.ext"
			// The CMD command interpreter "knows" the associated application.  Just start doesn't work.
			Runtime myRuntime = Runtime.getRuntime();
			if( sOs.toLowerCase().startsWith("mac") ) {
				String sCmd = "Open " + sFileName ;
				try {
					myRuntime.exec(sCmd);
				}
				catch (IOException ex) {
					logger.error(ex.getMessage() + "\nError in UNIDENTIFIED");
				}
			}
			else {
				String sCmd = "cmd /c \"start " + sFileName + " \"";
				if( sExecutable != null ) {
					sCmd = "\"" + sExecutable + "\" \"" + sFileName + "\""; 
				}
				try {
					myRuntime.exec(sCmd);
				}
				catch (IOException ex) {
					logger.error(ex.getMessage() + "\nError in UNIDENTIFIED");
				}
			}
		}
	}// End class OpenRecord

	/**
	 * Opens the current Page in default PDF reader
	 */
	public void openPageContentInAcrobat(String sFileName, int iPageNo) {
		Thread t = new OpenPageInAcrobat(sFileName, iPageNo);
		t.start();
	}

	private static class OpenPageInAcrobat extends Thread {
		private String sFileName;
		private int iPageNo;
		private OpenPageInAcrobat(String sFileName, int iPageNo ) {
			this.sFileName = sFileName;
			this.iPageNo = iPageNo;
		}

		public void run() {
			String sOs = System.getProperty("os.name");
			String sFile = extractPageToTempPDF( sFileName, iPageNo );
			// Open with default application using CMD command interpreter to run "start filename.ext"
			// The CMD command interpreter "knows" the associated application.  Just start doesn't work.
			Runtime myRuntime = Runtime.getRuntime();
			if( sOs.toLowerCase().startsWith("mac") ) {
				String sCmd = "Open " + sFile ;
				try {
					myRuntime.exec(sCmd);
				}
				catch (IOException ex) {
					logger.error(ex.getMessage() + "\nError in UNIDENTIFIED");
				}
			}
			else {
				String sCmd = "cmd /c \"start " + sFile + " \"";
				try {
					myRuntime.exec(sCmd);
				}
				catch (IOException ex) {
					logger.error(ex.getMessage() + "\nError in PDFOpener run()");
				}
			}
		}

		private String extractPageToTempPDF(String sFilePath, int iPage) {
			String sFileOut = null;
			File fOut = null;
			PdfReader reader = null;
			try {
				fOut = File.createTempFile("TempFileCVI", ".pdf");
				sFileOut = fOut.getAbsolutePath();
				reader = new PdfReader(sFilePath);
				com.itextpdf.text.Document document = new com.itextpdf.text.Document();
				PdfCopy writer = null;
				writer = new PdfCopy(document,
						new FileOutputStream(fOut));

				document.open();

				PdfImportedPage pip = writer.getImportedPage(reader, iPageNo);
				writer.addPage(pip);
				document.close();
				writer.close();
			}
			catch (DocumentException ex) {
				logger.error(ex.getMessage() + "\nError in extractPageToTempPDF");
			}
			catch (IOException ex1) {
				logger.error(ex1.getMessage() + "\nError in extractPageToTempPDF");
			}
			finally {
				if( reader != null ) {
					reader.close();
					reader = null;
				}
			}
			return sFileOut;
		}// End decode pages to new PDF

	}// End class OpenPage

}
