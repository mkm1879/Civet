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

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;

import edu.clemson.lph.civet.xml.StdeCviXml;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.ProgressDialog;
import edu.clemson.lph.dialogs.QuestionDialog;
import edu.clemson.lph.pdfgen.MergePDF;
import edu.clemson.lph.utils.FileUtils;

public class OpenFileThread extends Thread {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private ProgressDialog prog;
	private CivetEditDialog dlg;
	private PdfDecoder pdfDecoder;
	private File currentFile;
	private String sFilePath;
	private byte[] rawPdfBytes;
	private byte[] fileBytes;
	private StdeCviXml stdXml = null;
	private boolean bViewOnly = false;
	
	/**
	 * Read a file by name and open in CivetEditDialog
	 * @param dlg CivetEditDialog that owns this thread
	 * @param currentFilePath
	 */
	public OpenFileThread(CivetEditDialog dlg, String currentFilePath) {
		this.dlg = dlg;
		this.currentFile = new File(currentFilePath);
		this.sFilePath = currentFilePath;
		prog = new ProgressDialog(dlg, "Civet", "Opening CVI File " + this.currentFile.getName() );
		prog.setAuto(true);
		prog.setVisible(true);
		this.pdfDecoder = dlg.getPdfDecoder();
		fileBytes = null;
	}

	/**
	 * Read a file and open in CivetEditDialog
	 * @param dlg CivetEditDialog that owns this thread
	 * @param fCurrentFile
	 */
	public OpenFileThread(CivetEditDialog dlg, File fCurrentFile) {
		this.dlg = dlg;
		this.currentFile = fCurrentFile;
		this.sFilePath = fCurrentFile.getAbsolutePath();
		prog = new ProgressDialog(dlg, "Civet", "Opening CVI File " + this.currentFile.getName() );
		prog.setAuto(true);
		prog.setVisible(true);
		this.pdfDecoder = dlg.getPdfDecoder();
		fileBytes = null;
	}
	
	/**
	 * This constructor is used for files extracted from XML rather than on disk.
	 * @param dlg CivetEditDialog that owns this thread
	 * @param xStd
	 */
	public OpenFileThread(CivetEditDialog dlg, StdeCviXml xStd ) {
		this.dlg = dlg;
		this.fileBytes = xStd.getOriginalCVI();
		this.sFilePath = xStd.getOriginalCVIFileName();
		this.stdXml = xStd;
		if( fileBytes == null || sFilePath == null ) {
			logger.error(new Exception("OpenFileThread called without attached file"));
			return;
		}
		prog = new ProgressDialog(dlg, "Civet", "Opening CVI File " + sFilePath );
		prog.setAuto(true);
		prog.setVisible(true);
		this.pdfDecoder = dlg.getPdfDecoder();
	}
	
	public void setReadOnly( boolean bReadOnly ) {
		bViewOnly = bReadOnly;
	}
	
	@Override
	public void run() {
		// Do all setup here.
		try {
			pdfDecoder = dlg.initPdfDisplay();
			if( fileBytes == null ) {
				fileBytes = FileUtils.readBinaryFile( sFilePath );
			}
			pdfDecoder.closePdfFile();
			// Opening a CVI file 
			if( sFilePath.toLowerCase().endsWith(".xml") || sFilePath.toLowerCase().endsWith(".cvi")) {
				String sXml = new String( fileBytes );
				if( stdXml == null ) 
					stdXml = new StdeCviXml( sXml );
				rawPdfBytes = stdXml.getOriginalCVI();
				pdfDecoder.openPdfArray(rawPdfBytes);
			}
			else if( sFilePath.toLowerCase().endsWith(".pdf") ) {
				rawPdfBytes = fileBytes;
				pdfDecoder.openPdfArray(rawPdfBytes);
				if(!checkEncryption()){
					logger.error("Unable to open encrypted pdf file: " + currentFile.getAbsolutePath() );
					MessageDialog.messageLater(dlg, "PDF Error", "Unable to open encrypted pdf file: " + currentFile.getAbsolutePath());
					prog.setVisible(false);
					prog.dispose();
					return;
				}
			}
			else if( sFilePath.toLowerCase().endsWith(".jpg") || sFilePath.toLowerCase().endsWith(".jpeg") ) {
				rawPdfBytes = MergePDF.jpgToPdfBytes(fileBytes);
				pdfDecoder.openPdfArray(rawPdfBytes);
			}
			else if( sFilePath.toLowerCase().endsWith(".png")  ) {
				rawPdfBytes = MergePDF.pngToPdfBytes(fileBytes);
				pdfDecoder.openPdfArray(rawPdfBytes);
			}
			else if( sFilePath.toLowerCase().endsWith(".bmp")  ) {
				rawPdfBytes = MergePDF.bmpToPdfBytes(fileBytes);
				pdfDecoder.openPdfArray(rawPdfBytes);
			}
			else if( sFilePath.toLowerCase().endsWith(".gif")  ) {
				rawPdfBytes = MergePDF.gifToPdfBytes(fileBytes);
				pdfDecoder.openPdfArray(rawPdfBytes);
			}
		} catch(Exception e){
			logger.error("\nError reading or decoding file " + sFilePath, e );
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					prog.setVisible(false);
					prog.dispose();
					dlg.setVisible(false);
				}
			});
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				dlg.getController().setCurrentPdfBytes( rawPdfBytes );
				dlg.getController().setupFile();
				if( stdXml != null ) {
					dlg.populateFromStdXml(stdXml);
					if( bViewOnly )
						dlg.setMode(CivetEditDialog.VIEW_MODE);
					else
						dlg.setMode(CivetEditDialog.XML_MODE);
				}
				else {
					if( bViewOnly )
						dlg.setMode(CivetEditDialog.VIEW_MODE);
					else
						dlg.setMode(CivetEditDialog.PDF_MODE);
				}
				prog.setVisible(false);
				dlg.toFront();
			}
		});
	}

	/**
	 * check if encryption present and acertain password, return true if content accessable
	 */
	private boolean checkEncryption() {

		//    check if file is encrypted
		if(pdfDecoder.isEncrypted()){

			//if file has a null password it will have been decoded and isFileViewable will return true
			while(!pdfDecoder.isFileViewable()) {
				String password = QuestionDialog.askWait(dlg, "PDF Encrypted", "Please enter password");
				/** try and reopen with new password */
				if (password != null) {
					try {
						pdfDecoder.setEncryptionPassword(password);
					} catch (PdfException e) {
						logger.error(e.getMessage() + "\nError opening encrypted PDF file " + currentFile);  //To change body of catch statement use File | Settings | File Templates.
						return false;
					}
				}
			}
			return true;
		}
		//if not encrypted return true
		return true;
	}


}
