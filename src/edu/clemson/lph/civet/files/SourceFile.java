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


	/**
	 * Cheap little unit test
	 * @param args
	 */
	public static void main(String args[] ) {
		PropertyConfigurator.configure("CivetConfig.txt");
		CivetConfig.checkAllConfig();
		try {
		SourceFile source = SourceFile.readSourceFile("Test/AgViewTest.pdf");
		System.out.println(source.getType() + ", " + source.isPageable() + ": " + source.getPages() + ": " + source.canSplit());
		FileUtils.writeBinaryFile(source.getDataModel().getPDFAttachmentBytes(), "AgView.pdf");
		source = SourceFile.readSourceFile("Test/CivetTest.cvi");
		System.out.println(source.getType() + ", " + source.isPageable() + ": " + source.getPages() + ": " + source.canSplit());
		FileUtils.writeBinaryFile(source.getDataModel().getPDFAttachmentBytes(), "Civet.pdf");
		source = SourceFile.readSourceFile("Test/CO_KS_Test1.pdf");
		System.out.println(source.getType() + ", " + source.isPageable() + ": " + source.getPages() + ": " + source.canSplit());
		FileUtils.writeBinaryFile(source.getDataModel().getPDFAttachmentBytes(), "CoKs.pdf");
		source = SourceFile.readSourceFile("Test/ImageTest1.gif");
		System.out.println(source.getType() + ", " + source.isPageable() + ": " + source.getPages() + ": " + source.canSplit());
		FileUtils.writeBinaryFile(source.getDataModel().getPDFAttachmentBytes(), "Gif.pdf");
		source = SourceFile.readSourceFile("Test/ImageTest2.jpg");
		System.out.println(source.getType() + ", " + source.isPageable() + ": " + source.getPages() + ": " + source.canSplit());
		FileUtils.writeBinaryFile(source.getDataModel().getPDFAttachmentBytes(), "Jpg.pdf");
		source = SourceFile.readSourceFile("Test/ImageTest3.PNG");
		System.out.println(source.getType() + ", " + source.isPageable() + ": " + source.getPages() + ": " + source.canSplit());
		FileUtils.writeBinaryFile(source.getDataModel().getPDFAttachmentBytes(), "Png.pdf");
		source = SourceFile.readSourceFile("Test/mCVITest.pdf");
		System.out.println(source.getType() + ", " + source.isPageable() + ": " + source.getPages() + ": " + source.canSplit());
		FileUtils.writeBinaryFile(source.getDataModel().getPDFAttachmentBytes(), "mCVI.pdf");
		source = SourceFile.readSourceFile("Test/PDFTest1.pdf");
		System.out.println(source.getType() + ", " + source.isPageable() + ": " + source.getPages() + ": " + source.canSplit());
		FileUtils.writeBinaryFile(source.getDataModel().getPDFAttachmentBytes(), "Pdf1.pdf");
		source = SourceFile.readSourceFile("Test/PDFTest3.pdf");
		System.out.println(source.getType() + ", " + source.isPageable() + ": " + source.getPages() + ": " + source.canSplit());
		FileUtils.writeBinaryFile(source.getDataModel().getPDFAttachmentBytes(), "Pdf3.pdf");
		} catch( SourceFileException e ) {
			logger.error("Bad Source File", e);
		}
		
	}

	
	protected SourceFile( String sPath ) throws SourceFileException {
		if( sPath != null && sPath.trim().length() > 0 ) {
			sFilePath = sPath;
			fSource = new File( sPath );
			if( fSource == null || !fSource.exists() || ! fSource.isFile() ) {
				logger.error("Cannot find file " + sPath);
			}
		}
		else {
			logger.error("Attempt to construct source file with no path");
		}
	}
	
	public Types getType() {
		if( type == null ) {
			type = SourceFile.getType(sFilePath);
		}
		return type;
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
	public abstract int getPages();

	/**
	 * This is the factory method that constructs the appropriate subclass object.
	 * @param sPath Full path to the source file being "opened".
	 * @return
	 */
	public static SourceFile readSourceFile( String sPath ) throws SourceFileException {
		SourceFile sourceFile = null;
		SourceFile.Types type = SourceFile.getType(sPath);
		switch( type ) {
		case PDF:
			sourceFile = new PdfSourceFile( sPath );
			break;
		case Image:
			sourceFile = new ImageSourceFile( sPath );
			break;			
		case CO_KS_PDF:
			sourceFile = new CoKsSourceFile( sPath );
			break;
		case mCVI:
			sourceFile = new MCviSourceFile( sPath );
			break;
		case AgView:
			sourceFile = new AgViewSourceFile( sPath );
			break;
		case Civet:
			sourceFile = new CivetSourceFile( sPath );
			break;
		case Unknown:
			logger.error("Unknown file type " + sPath);
		default:
			logger.error("Unknown file type " + sPath);
		}
		return sourceFile;
	}
	
	/**
	 * This is a little awkward but allows each type to ID itself.
	 * @param sFilePath
	 * @return
	 */
	public static Types getType( String sFilePath ) {
		Types type = SourceFile.Types.Unknown;
		if( PdfSourceFile.isPDF(sFilePath) ) {
			if( CoKsSourceFile.isCoKs(sFilePath) ) {
				type = Types.CO_KS_PDF;
			}
			else if( AgViewSourceFile.isAgView(sFilePath) ) {
				type = Types.AgView;
			}
			else if( MCviSourceFile.isMCvi(sFilePath) ) {
				type = Types.mCVI;
			}
			else {
				type = Types.PDF;
			}
		}
		else if( ImageSourceFile.isImage(sFilePath) ) {
			type = Types.Image;
		}
		else if( CivetSourceFile.isCivet(sFilePath) ) {
			type = Types.Civet;
		}
		else {
			type = Types.Unknown;
		}
		return type;
	}
}
