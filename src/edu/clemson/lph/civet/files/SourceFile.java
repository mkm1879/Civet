/**
 * Copyright Nov 30, 2018 Michael K Martin
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.clemson.lph.civet.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jpedal.exception.PdfException;

import com.itextpdf.text.pdf.PdfReader;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.pdfgen.PDFUtils;
import edu.clemson.lph.pdfgen.PDFViewer;
import edu.clemson.lph.utils.FileUtils;

/**
 * Common functionality of all source file types including ID of type.
 */
public abstract class SourceFile {
	protected static final Logger logger = Logger.getLogger(Civet.class.getName());
	public static enum Types {
		PDF,
		Image,
		CO_KS_PDF,
		mCVI,
		AgView,
		Civet,
		Unknown
	}

	protected String sFilePath = null;
	protected String sFileName = null;
	protected String sDataPath = null;
	// fSource is the original PDF or image
	protected File fSource = null;
	// fData only populated for mCVI and AgView where there is a separate data file.
	protected File fData = null;
	// pdfBytes is the whole file read from disk or converted from image.
	protected byte pdfBytes[] = null;
	protected Types type = null;
	// model will hold the pdf as currently constructed.
	protected StdeCviXmlModel model = null;
	protected Integer iPage = null;
	protected PdfReader iTextPdfReader;

	SourceFile cloneCurrentState() {
		return this;
	}
	
//	private SourceFile clonePdfSource() {
//		SourceFile clone = new PdfSourceFile();
//		clone.sFilePath = sFilePath;
//		clone.sFileName = sFileName;
//		clone.sDataPath = sDataPath;
//		// fSource is the original PDF or image
//		clone.fSource = fSource;
//		// fData only populated for mCVI and AgView where there is a separate data file.
//		clone.fData = fData;
//		// pdfBytes is the whole file read from disk or converted from image.
//		clone.pdfBytes = pdfBytes;
//		clone.type = null;
//		// model will hold the pdf as currently constructed.
//		clone.model = new  StdeCviXmlModel( model.getXMLString() );  // Model is a deep copy?
//		clone.model.setOrUpdatePDFAttachment(model.getPDFAttachmentBytes(), model.getPDFAttachmentFilename());
//		clone.model.addOrUpdateMetadataAttachment(model.getMetaData());
//		clone.iPage = iPage;
//		return clone;
//	}
//
	/**
	 * used only by PDFSource in Clone.
	 */
	protected SourceFile() {
		
	}
	
	protected SourceFile( File fFile ) throws SourceFileException {
		if( fFile != null && fFile.exists() && fFile.isFile() ) {
			sFileName = fFile.getName();
			sFilePath = fFile.getAbsolutePath();
			fSource = fFile;
			iPage = 1;
		}
		else {
			logger.error("Attempt to construct source file with no file");
		}
	}
	
	public Types getType() {
		if( type == null ) {
			type = SourceFile.getType(fSource);
		}
		return type;
	}
	
	public File getSourceFile() {
		return fSource;
	}
	
	public File getDataFile() {
		return fData;
	}
	
	public abstract String getSystem();
	
	public java.util.Date getSaveDate() {
		java.util.Date dSaved = null;
		dSaved = new java.util.Date(fSource.lastModified());
		return dSaved;
	}
	
	public abstract StdeCviXmlModel getDataModel();
	
	public Integer getCurrentPageNo() {
		return iPage;
	}
	
//	void viewFile() throws PdfException {
//		byte pdfBytes[] = getPDFBytes();
//		boolean bXFA = PDFUtils.isXFA(pdfBytes);
//		if( bXFA && !CivetConfig.isJPedalXFA() ) {
//			MessageDialog.showMessage(null, "Civet: No XFA", "Civet cannot display CO/KS XFA PDFs without JPedal license");
//			viewer.closePdfFile();
//		}
//		else {
//			viewer.setPdfBytes(pdfBytes, bXFA);
//			viewer.viewPage(iPage);
//		}
//	}
	
	public void gotoPageNo( Integer iPageNo ) {
		if( iTextPdfReader != null && iPageNo >= 1 && iPageNo <= getPageCount() ) {
			this.iPage = iPageNo;
		}
		else {
			logger.error("Attempt to view page " + iPageNo + " of " + getPageCount() + " pages");
		}
	}
	
	public Integer getPageCount() {
		Integer iRet = null;
		if( iTextPdfReader != null ) {
			iRet = iTextPdfReader.getNumberOfPages();
		}
		else {
			iRet = 1;
		}
		return iRet;
	}
	
	/**
	 * Overridden by file types (Plain PDF) that need default rotation set in config file
	 * @return
	 */
	public int getRotation() {
		return 0;
	}
	
	public byte[] getPDFBytes() {
		return pdfBytes;
	}
	
	public byte[] getPDFBytes( int iPageNo ) {
		logger.error("getPDFBytes(iPage) called on non-splitable source " + sFilePath);
		return getPDFBytes();  // just send it all
	}
	
	public byte[] getPDFBytes(ArrayList<Integer> aPages) {
		logger.error("getPDFBytes(Array) called on non-splitable source " + sFilePath);
		return getPDFBytes();  // just send it all
	}
	
	public abstract boolean canSplit();
	
	public boolean isXFA() {
		return false;
	}
	public boolean isDataFile() {
		return false;
	}
	public String getFileName() {
		return sFileName;
	}
	public void setFileName( String sFileName) {
		this.sFileName = sFileName;
	}
	public String getFilePath() {
		return sFilePath;
	}
	public void setFilePath( String sFilePath ) {
		this.sFilePath = sFilePath;
	}
	
	/**
	 * Move source file and if subclass includes one, the data file to the specified directory.
	 * @param fDir
	 * @return true if moved successfully.
	 */
	public boolean moveToDirectory( File fDir ) {
		boolean bRet = false;
		File fNew = new File(fDir, getFileName());
		if( fNew.exists() ) {
			String sOutPath = fNew.getAbsolutePath();
			sOutPath = FileUtils.incrementFileName(sOutPath);
			logger.error(fNew.getName() + " already exists in " + fDir.getAbsolutePath() + "\n" +
					"Saving as " + sOutPath);
			fNew = new File( sOutPath );
		}
		bRet = fSource.renameTo(fNew);
		if (!bRet) {
			logger.error("Could not move " + getFilePath() + " to " + fNew.getAbsolutePath() );
		}
		return bRet;
	}
	public void addPageToCurrent( Integer iPage ) throws SourceFileException {
		throw new SourceFileException("Attempt to add page to unsplittable file.");
	}
	/**
	 * This is the factory method that constructs the appropriate subclass object.
	 * @param sPath Full path to the source file being "opened".
	 * @return
	 */
	static SourceFile readSourceFile( File fFile ) throws SourceFileException {
		SourceFile sourceFile = null;
		SourceFile.Types type = SourceFile.getType(fFile);
		switch( type ) {
		case PDF:
			sourceFile = new PdfSourceFile( fFile );
			break;
		case Image:
			sourceFile = new ImageSourceFile( fFile );
			break;			
		case CO_KS_PDF:
			sourceFile = new CoKsSourceFile( fFile );
			break;
		case mCVI:
			sourceFile = new MCviSourceFile( fFile );
			break;
		case AgView:
			sourceFile = new AgViewSourceFile( fFile );
			break;
		case Civet:
			sourceFile = new CivetSourceFile( fFile );
			break;
		case Unknown:
			logger.error("Unknown file type " + fFile);
		default:
			logger.error("Unknown file type " + fFile);
		}
		sourceFile.setFileName( fFile.getName() );
		sourceFile.setFilePath( fFile.getAbsolutePath() );
		return sourceFile;
	}
	
	/**
	 * This is a little awkward but allows each type to ID itself.
	 * @param sFilePath
	 * @return
	 */
	private static Types getType( File fFile ) {
		Types type = SourceFile.Types.Unknown;
		if( PdfSourceFile.isPDF(fFile) ) {
			if( CoKsSourceFile.isCoKs(fFile) ) {
				type = Types.CO_KS_PDF;
			}
			else if( AgViewSourceFile.isAgView(fFile) ) {
				type = Types.AgView;
			}
			else if( MCviSourceFile.isMCvi(fFile) ) {
				type = Types.mCVI;
			}
			else {
				type = Types.PDF;
			}
		}
		else if( ImageSourceFile.isImage(fFile) ) {
			type = Types.Image;
		}
		else if( CivetSourceFile.isCivet(fFile) ) {
			type = Types.Civet;
		}
		else {
			type = Types.Unknown;
		}
		return type;
	}

}
