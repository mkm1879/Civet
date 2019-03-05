package edu.clemson.lph.civet.emailonly;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.jpedal.PdfDecoder;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.dialogs.MessageDialog;

public class EmailOnlyFileController {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	
	EmailOnlyDialog dlg;
	/** Data defining PDF and Index state **/
	/**name of current PDF file*/
	String currentFilePath = null;
	String currentFileName = null;
	private File currentFile = null;
	private File currentFiles[] = null;
	/** File and Page Numbers are 1 based.  PageNo used as is in pdfDecoder.decodePage()
	 *  iFileNo is one more than the array index!!!  **/
	private int iFileNo = 1;
	private int iFiles = 0;
	private int iPageNo = 1;
	private int iPages = 0;
	private byte rawPdfBytes[];

	public EmailOnlyFileController(EmailOnlyDialog parent) {
		dlg = parent;
	}
	
	public void setCurrentFiles( File files[] ) {
		currentFiles = files;
		if( currentFiles == null ) {
			logger.error("setCurrentFiles called with null", new Exception("Civet Error"));
			return;
		}
		iFileNo = 1;
		iFiles = currentFiles.length;
		dlg.setFile(iFileNo);
		dlg.setFiles( iFiles );
		if( currentFiles != null && currentFiles.length > 0 ) {
			currentFile = currentFiles[0];
			currentFilePath = currentFile.getAbsolutePath();
			currentFileName = currentFile.getName();
			EMailOnlyOpenFileThread thread = new EMailOnlyOpenFileThread( dlg, currentFilePath );
			thread.start();
		}
	}
	
	public void setCurrentPdfBytes( byte bytes[] ) { rawPdfBytes = bytes; }
	public byte[] getCurrentPdfBytes() { return rawPdfBytes; }
	public File getCurrentFile() { return currentFile; }
	
	public String getCurrentFilePath() { return currentFilePath; }
	public String getCurrentFileName() { return currentFileName; }
	public int getCurrentFileNo() {	return iFileNo;	}
	public int getNumberOfFiles() {	return currentFiles.length;	}
	public int getCurrentPageNo() {	return iPageNo;	}
	public void setNumberOfPages( int iPages ) { this.iPages = iPages; }
	public int getNumberOfPages() {	return iPages; }
	

	/**
	 * Move forward one page
	 */
	public boolean pageForward() {
		if( iPageNo < iPages ) {
			iPageNo++;
			try {
				dlg.getPdfDecoder().decodePage(iPageNo);
				dlg.updatePdfDisplay();
				dlg.setupForm(currentFileName);
			}
			catch (Exception e1) {
				logger.error(e1.getMessage() + "\nError moving forward one page");
				iPageNo--;
				// Now what?  Another try?
			}
			return true;
		}
		else {
			File prevFile = currentFile;
			String sNewPath = CivetConfig.getOutputDirPath();
			if( sNewPath == null || sNewPath.trim().length() == 0 ) {
				MessageDialog.showMessage(dlg, "Civet Error", "Set EmailSendOutputDir preference to use this feature");
				return false;
			}
			File fNewPath = new File(sNewPath);
			if( fNewPath == null || !fNewPath.isDirectory() ) {
				MessageDialog.showMessage(dlg, "Civet Error", "EmailSendOutputDir folder " + sNewPath + " does not exist");
			}
			String sNewName = prevFile.getName();
			File fNew = new File( sNewPath, sNewName );
			try {
				prevFile.renameTo(fNew);
			} catch( Exception e ) {
				e.printStackTrace();
			}
			if( iFileNo < currentFiles.length ) {
				iFileNo++;
				currentFile = currentFiles[iFileNo-1];
				currentFilePath = currentFile.getAbsolutePath();
				currentFileName = currentFile.getName();
				// Would have a race condition if we didn't perform this set BEFORE starting thread
				iPageNo = 1;
				EMailOnlyOpenFileThread thread = new EMailOnlyOpenFileThread( dlg, currentFile );
				thread.start();	
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Callback from open file thread.
	 */
	public void setupFile() {
		PdfDecoder pdfDecoder = dlg.getPdfDecoder();
		try {
			pdfDecoder.decodePage(iPageNo);
			pdfDecoder.setPageParameters(dlg.getScale(),iPageNo,dlg.getRotation()); //values scaling (1=100%). page number
			int iPagesInFile = pdfDecoder.getPageCount();
			setNumberOfPages( iPagesInFile );
			// Pages in this file
			dlg.setupForm(currentFileName);
			dlg.setVisible(true);
			dlg.updatePdfDisplay();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
	}
	
	/**
	 * From the current pdfDecoder, extract the page(s) in aPagesInCurrent to a new pdfData buffer as output stream.
	 * @param aPages int[]
	 * @return byte[]
	 */
	public byte[] extractPagesToNewPDF() {
		ByteArrayOutputStream baOut = new ByteArrayOutputStream();
		try {
			byte[] pdfDataIn = rawPdfBytes;
			PdfReader reader = new PdfReader(pdfDataIn);
			com.itextpdf.text.Document document = new com.itextpdf.text.Document();
			PdfCopy writer = new PdfCopy(document, baOut);
			document.open();
			PdfImportedPage pip = writer.getImportedPage(reader, iPageNo );
			writer.addPage(pip);
			document.close();
		} catch( IOException ioe ) {
			logger.info("IO error extracting pages to byte array", ioe);
			// This is a bug if the exception happens just returning the original pdf
			return rawPdfBytes;
		} catch( DocumentException de ) {
			logger.info(de.getMessage() + "\nDocument error extracting pages to byte array");
			// This is a bug if the exception happens just returning the original pdf
			return rawPdfBytes;
		}
		return baOut.toByteArray();
	}// End decode pages to new PDF




}
