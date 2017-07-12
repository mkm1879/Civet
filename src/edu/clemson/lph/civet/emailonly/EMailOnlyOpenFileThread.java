/**
 * 
 */
package edu.clemson.lph.civet.emailonly;

import java.io.File;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.xml.StdeCviXml;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.ProgressDialog;
import edu.clemson.lph.dialogs.QuestionDialog;
import edu.clemson.lph.pdfgen.MergePDF;
import edu.clemson.lph.utils.FileUtils;

/**
 * @author mmarti5
 *
 */
public class EMailOnlyOpenFileThread extends Thread {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private ProgressDialog prog;
	private EmailOnlyDialog dlg;
	private PdfDecoder pdfDecoder;
	private File currentFile;
	private String sFilePath;
	private byte[] rawPdfBytes;
	private byte[] fileBytes;
	
	/**
	 * Read a file by name and open in EmailOnlyDialog
	 * @param dlg EmailOnlyDialog that owns this thread
	 * @param currentFilePath
	 */
	public EMailOnlyOpenFileThread(EmailOnlyDialog dlg, String currentFilePath) {
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
	 * Read a file and open in EmailOnlyDialog
	 * @param dlg EmailOnlyDialog that owns this thread
	 * @param fCurrentFile
	 */
	public EMailOnlyOpenFileThread(EmailOnlyDialog dlg, File fCurrentFile) {
		this.dlg = dlg;
		this.currentFile = fCurrentFile;
		this.sFilePath = fCurrentFile.getAbsolutePath();
		prog = new ProgressDialog(dlg, "Civet", "Opening CVI File " + this.currentFile.getName() );
		prog.setAuto(true);
		prog.setVisible(true);
		this.pdfDecoder = dlg.getPdfDecoder();
		fileBytes = null;
	}

	@Override
	public void run() {
		// Do all setup here.
		try {
//			pdfDecoder = dlg.initPdfDisplay();
			if( fileBytes == null ) {
				fileBytes = FileUtils.readBinaryFile( sFilePath );
			}
			pdfDecoder.closePdfFile();
			// Opening a CVI file 
			if( sFilePath.toLowerCase().endsWith(".pdf") ) {
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
