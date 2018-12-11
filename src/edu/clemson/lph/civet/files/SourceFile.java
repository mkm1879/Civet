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
import org.apache.log4j.PropertyConfigurator;
import org.jpedal.PdfDecoder;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetInbox;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
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
	protected String sDataPath = null;
	// fSource is the original PDF or image
	protected File fSource = null;
	// fData only populated for mCVI and AgView where there is a separate data file.
	protected File fData = null;
	// pdfBytes is the whole file read from disk or converted from image.
	protected byte pdfBytes[] = null;
	protected Types type = null;
	// pdfDecoder with full source file included here for access to metadata.
	protected PdfDecoder pdfDecoder = null;
	// model will hold the pdf as currently constructed.
	protected StdeCviXmlModel model = null;
	// Total number of pages reported by decoder
	protected Integer iPages = null;
	// Default for single page formats.
	protected Integer iPage = 1;  


	/**
	 * Cheap little unit test
	 * @param args
	 */
	public static void main(String args[] ) {
		PropertyConfigurator.configure("CivetConfig.txt");
		CivetConfig.checkAllConfig();
		try {
		SourceFile source = SourceFile.readSourceFile(new File("Test/AgViewTest.pdf"));
		System.out.println(source.getType() + ", " + source.isPageable() + ": " + source.getPageCount() + ": " + source.canSplit());
		FileUtils.writeBinaryFile(source.getDataModel().getPDFAttachmentBytes(), "AgView.pdf");
		FileUtils.writeTextFile(source.getDataModel().getXMLString(), "AgView.xml");
		source = SourceFile.readSourceFile(new File("Test/CivetTest.cvi"));
		System.out.println(source.getType() + ", " + source.isPageable() + ": " + source.getPageCount() + ": " + source.canSplit());
		FileUtils.writeBinaryFile(source.getDataModel().getPDFAttachmentBytes(), "Civet.pdf");
		FileUtils.writeTextFile(source.getDataModel().getXMLString(), "Civet.xml");
		source = SourceFile.readSourceFile(new File("Test/CO_KS_Test1.pdf"));
		System.out.println(source.getType() + ", " + source.isPageable() + ": " + source.getPageCount() + ": " + source.canSplit());
		FileUtils.writeBinaryFile(source.getDataModel().getPDFAttachmentBytes(), "CoKs.pdf");
		FileUtils.writeTextFile(source.getDataModel().getXMLString(), "CoKs.xml");
		source = SourceFile.readSourceFile(new File("Test/ImageTest1.gif"));
		System.out.println(source.getType() + ", " + source.isPageable() + ": " + source.getPageCount() + ": " + source.canSplit());
		FileUtils.writeBinaryFile(source.getDataModel().getPDFAttachmentBytes(), "Gif.pdf");
		FileUtils.writeTextFile(source.getDataModel().getXMLString(), "Gif.xml");
		source = SourceFile.readSourceFile(new File("Test/ImageTest2.jpg"));
		System.out.println(source.getType() + ", " + source.isPageable() + ": " + source.getPageCount() + ": " + source.canSplit());
		FileUtils.writeBinaryFile(source.getDataModel().getPDFAttachmentBytes(), "Jpg.pdf");
		FileUtils.writeTextFile(source.getDataModel().getXMLString(), "Jpg.xml");
		source = SourceFile.readSourceFile(new File("Test/ImageTest3.PNG"));
		System.out.println(source.getType() + ", " + source.isPageable() + ": " + source.getPageCount() + ": " + source.canSplit());
		FileUtils.writeBinaryFile(source.getDataModel().getPDFAttachmentBytes(), "Png.pdf");
		FileUtils.writeTextFile(source.getDataModel().getXMLString(), "Png.xml");
		source = SourceFile.readSourceFile(new File("Test/mCVITest.pdf"));
		System.out.println(source.getType() + ", " + source.isPageable() + ": " + source.getPageCount() + ": " + source.canSplit());
		FileUtils.writeBinaryFile(source.getDataModel().getPDFAttachmentBytes(), "mCVI.pdf");
		FileUtils.writeTextFile(source.getDataModel().getXMLString(), "mCVI.xml");
		source = SourceFile.readSourceFile(new File("Test/PDFTest1.pdf"));
		System.out.println(source.getType() + ", " + source.isPageable() + ": " + source.getPageCount() + ": " + source.canSplit());
		FileUtils.writeBinaryFile(source.getDataModel().getPDFAttachmentBytes(), "Pdf1.pdf");
		FileUtils.writeTextFile(source.getDataModel().getXMLString(), "Pdf1.xml");
		source = SourceFile.readSourceFile(new File("Test/PDFTest3.pdf"));
		System.out.println(source.getType() + ", " + source.isPageable() + ": " + source.getPageCount() + ": " + source.canSplit());
		FileUtils.writeBinaryFile(source.getDataModel().getPDFAttachmentBytes(), "Pdf3.pdf");
		FileUtils.writeTextFile(source.getDataModel().getXMLString(), "Pdf4.xml");
		} catch( SourceFileException e ) {
			logger.error("Bad Source File", e);
		}
		
	}

	protected SourceFile( File fFile ) throws SourceFileException {
		if( fFile != null && fFile.exists() && fFile.isFile() ) {
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
	
	public PdfDecoder getPdfDecoder() {
		return pdfDecoder;
	}
	
	public abstract StdeCviXmlModel getDataModel();
	
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
	
	public abstract boolean isPageable();
	public abstract boolean canSplit();
	public StdeCviXmlModel split() throws SourceFileException {
		throw new SourceFileException("Attempt to split unsplittable file.");
	}
	public abstract Integer getPageCount();
	public Integer getCurrentPage() {
		return iPage;
	}
	public void setCurrentPage( Integer iPage ) {
		this.iPage = iPage;
		// subclasses do any required management
	}
	public void addPageToCurrent( Integer iPage ) throws SourceFileException {
		throw new SourceFileException("Attempt to add page to unsplittable file.");
	}
	/**
	 * This is the factory method that constructs the appropriate subclass object.
	 * @param sPath Full path to the source file being "opened".
	 * @return
	 */
	public static SourceFile readSourceFile( File fFile ) throws SourceFileException {
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
