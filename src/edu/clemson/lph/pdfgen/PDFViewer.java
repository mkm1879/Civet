package edu.clemson.lph.pdfgen;

import org.apache.log4j.Logger;
import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.objects.PdfPageData;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.dialogs.QuestionDialog;

/**
 * This class will contain the viewer displayed in CivetEditDialog.  That viewer will contain the entire 
 * pdf for the current OpenFile.  Paging consists of setting at least setPage() and calling updatePdfDisplay().
 */
public class PDFViewer {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	/** Data behind the GUI **/
	/**the actual JPanel/decoder object*/
	PdfDecoder pdfDecoder;
	byte pdfBytes[];
	int iRotation = 0;
	float scale = 1.0f;
	int iPageNo = 1;

	public PDFViewer() {
		pdfDecoder = new PdfDecoder();
	}
	
	public void setPdfBytes( byte pdfBytes[] ) throws PdfException {
		this.pdfBytes = pdfBytes;
		pdfDecoder.openPdfArray(pdfBytes);
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
	
	public int getRotation() {
		return iRotation;
	}
	
	public int getPageCount() {
		return pdfDecoder.getPageCount();
	}
	
	public void setPage( int iPage ) {
		this.iPageNo = iPage;
	}
	
	public int getPage() {
		return iPageNo;
	}
	
	public void setScale( float fScale ) {
		this.scale = fScale;
	}
	
	public double getScale() {
		return scale;
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

	/**
	 * check if encryption present and acertain password, return true if content accessable
	 */
	private boolean checkEncryption() {
		//    check if file is encrypted
		if(pdfDecoder.isEncrypted()){
			//if file has a null password it will have been decoded and isFileViewable will return true
			while(!pdfDecoder.isFileViewable()) {
				String password = QuestionDialog.askWait(null, "PDF Encrypted", "Please enter password");
				/** try and reopen with new password */
				if (password != null) {
					try {
						pdfDecoder.setEncryptionPassword(password);
					} catch (PdfException e) {
						logger.error(e.getMessage() + "\nError opening encrypted PDF file");  //To change body of catch statement use File | Settings | File Templates.
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
