package edu.clemson.lph.pdfgen;

import edu.clemson.lph.logging.Logger;
import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.objects.PdfPageData;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.QuestionDialog;

/**
 * This class will contain the viewer displayed in CivetEditDialog.  That viewer will contain the entire 
 * pdf for the current OpenFile.  Paging consists of setting at least setPage() and calling updatePdfDisplay().
 */
public class PDFViewer {
      private static Logger logger = Logger.getLogger();
	/** Data behind the GUI **/
	/**the actual JPanel/decoder object*/
	private PdfDecoder pdfDecoder;
	private byte pdfBytes[];
	private int iRotation = 0;
	private float scale = 1.0f;
	private int iPageNo = 1;
	private boolean bXFA = false;

	public PDFViewer() {
		pdfDecoder = new PdfDecoder();
	}
	
	/**
	 * Pass in bytes to read and to save checking twice the XFA status
	 * @param pdfBytes
	 * @param bXFA
	 * @throws PdfException
	 */
	public void setPdfBytes( byte pdfBytes[], boolean bXFA ) throws PdfException {
		this.pdfBytes = pdfBytes;
		pdfDecoder.openPdfArray(pdfBytes);
		this.bXFA = bXFA;
		checkEncryption();
	}

	/** 
	 * The PDF and metadata are in the contained JPedal pdfDecoder object
	 * @return
	 */
	public PdfDecoder getPdfDecoder() { return pdfDecoder; }

	/**
	 * Rotation of current page added to page rotation encoded in PDF
	 * which may or may not indicate upright view depending on scanner.
	 * @param iRelativeRotation
	 */
	public void setRotation( int iRelativeRotation ) { 
		PdfPageData pd = pdfDecoder.getPdfPageData();
		int iPageRotation = pd.getRotation(iPageNo);
		this.iRotation = ( iPageRotation + iRelativeRotation ) % 360; 
	}
	
	/**
	 * Rotation change from current rotation in integer degrees
	 * @param iRotationChange
	 */
	public void alterRotation( int iRotationChange ) { 
		this.iRotation = ( this.iRotation + iRotationChange ) % 360; 
	}
	
	public int getRotation() {
		return iRotation;
	}
	
	public int getPageCount() {
		return pdfDecoder.getPageCount();
	}
	
	private void setPage( int iPage ) {
		this.iPageNo = iPage;
	}
	
	public int getPage() {
		return iPageNo; 
	}
	
	public void alterScale( float fScaleChange ) {
		this.scale = this.scale * fScaleChange;
	}
	public void setScale( float fScale ) {
		this.scale = fScale;
	}
	
	public float getScale() {
		return scale;
	}
	
	public void viewPage( int iPageNo ) {
		setPage(iPageNo);
		boolean bJPedal = CivetConfig.isJPedalXFA();
		if( bXFA && !bJPedal && CivetConfig.isAutoOpenPdf() ) {
				PDFOpener opener = new PDFOpener(null);
				opener.openPDFContentInAcrobat(pdfBytes);
		}
		else if( bXFA && !bJPedal ) {
			MessageDialog.showMessage(null, "Civet: No XFA", "Civet cannot display CO/KS XFA PDFs without JPedal license");
			closePdfFile();
		}
		else {
			try {
				pdfDecoder.decodePage(iPageNo);
				updatePdfDisplay();
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	/**
	 * Latest instructions from JPedal on how to refresh the PDF display properly.
	 */
	public void updatePdfDisplay() {
		pdfDecoder.setPageParameters(scale, iPageNo, iRotation); 
		 //values scaling (1=100%). page number, rotation + 180
		pdfDecoder.waitForDecodingToFinish();
		pdfDecoder.invalidate();
		pdfDecoder.updateUI();
		pdfDecoder.validate();
	}
	
	public void closePdfFile() {
		pdfDecoder.closePdfFile();
	}

	/**
	 * check if encryption present and acertain password, return true if content accessable
	 * This is poorly implemented but I don't think anyone ever encounters an encrypted CVI PDF.
	 * @throws PdfException 
	 */
	private boolean checkEncryption() throws PdfException {
		//    check if file is encrypted
		if(pdfDecoder.isEncrypted()){
			//if file has a null password it will have been decoded and isFileViewable will return true
			while(!pdfDecoder.isFileViewable()) {
				String password = QuestionDialog.askWait(null, "PDF Encrypted", "Please enter password");
				/** try and reopen with new password */
				if (password != null)
					pdfDecoder.setEncryptionPassword(password);
			}
			return true;
		}
		//if not encrypted return true
		return true;
	}
	

}
