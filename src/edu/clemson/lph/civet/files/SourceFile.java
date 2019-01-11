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
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jpedal.exception.PdfException;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.pdfgen.PDFUtils;
import edu.clemson.lph.pdfgen.PDFViewer;
import edu.clemson.lph.utils.FileUtils;

/**
 * Common functionality of all source file types including ID of type.
 */
public abstract class SourceFile {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
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
	protected PDFViewer viewer = null;



	protected SourceFile( File fFile, PDFViewer viewer ) throws SourceFileException {
		if( fFile != null && fFile.exists() && fFile.isFile() ) {
			sFileName = fFile.getName();
			sFilePath = fFile.getAbsolutePath();
			fSource = fFile;
			this.viewer = viewer;
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
	
	public abstract StdeCviXmlModel getDataModel();
	
	public Integer getCurrentPageNo() {
		return iPage;
	}
	
	public void viewFile() throws PdfException {
		byte pdfBytes[] = getPDFBytes();
		boolean bXFA = PDFUtils.isXFA(pdfBytes);
		viewer.setPdfBytes(pdfBytes, bXFA);
		viewer.viewPage(iPage);
	}
	
	public void gotoPageNo( Integer iPageNo ) {
		this.iPage = iPageNo;
		if( viewer != null && iPageNo >= 1 && iPageNo <= getPageCount() ) {
			viewer.viewPage(iPageNo);
		}
		else {
			logger.error("Attempt to view page " + iPageNo + " of " + getPageCount() + " pages");
		}
	}
	
	public Integer getPageCount() {
		Integer iRet = null;
		if( viewer != null ) {
			iRet = viewer.getPageCount();
		}
		else {
			iRet = 1;
		}
		return iRet;
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
	public StdeCviXmlModel split() throws SourceFileException {
		throw new SourceFileException("Attempt to split unsplittable file.");
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
			logger.error(fNew.getName() + " already exists in " + fDir.getAbsolutePath() + " .\n" +
						"Check that it really is a duplicate and manually delete.");
			String sOutPath = fNew.getAbsolutePath();
			sOutPath = FileUtils.incrementFileName(sOutPath);
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
	public static SourceFile readSourceFile( File fFile, PDFViewer viewer ) throws SourceFileException {
		SourceFile sourceFile = null;
		SourceFile.Types type = SourceFile.getType(fFile);
		switch( type ) {
		case PDF:
			sourceFile = new PdfSourceFile( fFile, viewer );
			break;
		case Image:
			sourceFile = new ImageSourceFile( fFile, viewer );
			break;			
		case CO_KS_PDF:
			sourceFile = new CoKsSourceFile( fFile, viewer );
			break;
		case mCVI:
			sourceFile = new MCviSourceFile( fFile, viewer );
			break;
		case AgView:
			sourceFile = new AgViewSourceFile( fFile, viewer );
			break;
		case Civet:
			sourceFile = new CivetSourceFile( fFile, viewer );
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
	public static Types getType( File fFile ) {
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
