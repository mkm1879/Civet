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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;

import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.pdfgen.MergePDF;
import edu.clemson.lph.utils.FileUtils;

/**
 * 
 */
class PdfSourceFile extends SourceFile {
	private byte[] wholeFileBytes = null;
	/**
	 * Used only in cloneCurrentState
	 */
	protected PdfSourceFile() {
		
	}
	
	/**
	 * Scanned paper PDF loaded with an empty data model with just the 
	 * first page of the source PDF file as its attachemnt.
	 * @param fFile
	 * @throws SourceFileException
	 */
	PdfSourceFile( File fFile ) throws SourceFileException {
		super(fFile);
		// iPage = 1 from super 
		type = Types.PDF;
		try {
			wholeFileBytes = FileUtils.readBinaryFile(fSource);
			pdfBytes = wholeFileBytes;
			FileUtils.writeBinaryFile(pdfBytes, "Original_" + fSource.getName());
			iTextPdfReader = new PdfReader(pdfBytes);
		} catch (Exception e) {
			throw new SourceFileException(e);
		}
		model = new StdeCviXmlModel();
		model.setOrUpdatePDFAttachment(getPDFBytes(iPage), fSource.getName());
	}
	
	/**
	 * Scanned paper PDF loaded with an empty data model with just the 
	 * first page of the source PDF file as its attachemnt.
	 * @param fFile
	 * @throws SourceFileException
	 */
	PdfSourceFile( byte[] fileBytes, File fFile, Integer iPage ) throws SourceFileException {
		super();
		if( fFile != null && fFile.exists() && fFile.isFile() ) {
			sFileName = fFile.getName();
			sFilePath = fFile.getAbsolutePath();
			fSource = fFile;
			this.iPage = iPage;
		}
		else {
			logger.error("Attempt to construct source file with no file");
		}
		type = Types.PDF;
		try {
			wholeFileBytes = fileBytes;
			pdfBytes = wholeFileBytes;
			iTextPdfReader = new PdfReader(pdfBytes);
		} catch (Exception e) {
			throw new SourceFileException(e);
		}
		model = new StdeCviXmlModel();
		model.setOrUpdatePDFAttachment(getPDFBytes(iPage), fSource.getName());
	}
	
	/**
	 * All we assert here is that this is a PDF file, sub types do the rest.
	 * Rule out CO/KS, mCVI, or AgView before instantiating as this.
	 * @param sPath
	 * @return
	 */
	static boolean isPDF( File fFile ) {
		boolean bRet = false;
		String sName = fFile.getName();
		if( sName.toLowerCase().endsWith(".pdf") ) {
			bRet = true;
		}
		return bRet;
	}

	/** 
	 * Being here we know we are a PDF and if more than one page, pageable.
	 * Because XFA is a different class, we can split.
	 * @return
	 */
	@Override
	public boolean canSplit() {
		return true;
	}
	
	/**
	 * Create another representation of the whole file set to the current page
	 * ready to move on.
	 */
	@Override
	SourceFile newSourceFromSameSourceFile() {
		PdfSourceFile clone = null;
		try {
			clone = new PdfSourceFile(wholeFileBytes, fSource, iPage);
		} catch( SourceFileException e ) {
			logger.error(e);
		}
		return clone;
	}
	
	/**
	 * By convention, the data model holds only pages in the current PDF
	 * For ordinary PDF files this is a subset of the whole file.
	 * Here we add a new page to the data model from the source.
	 */
	@Override
	public void addPageToCurrent( Integer iPage ) throws SourceFileException {
		this.iPage = iPage;
		byte pdfBytesCurrent[] = model.getPDFAttachmentBytes();
		byte pdfPageBytes[] = getPDFBytes(iPage);  // extract pages from original full pdf
		try {
			byte pdfCombined[] = MergePDF.appendPDFtoPDF(pdfBytesCurrent, pdfPageBytes);
			if( pdfCombined == null ) {
				logger.error("null pdfCombined in addPageToCurrent(" + iPage + ") of " + getPageCount() + " pages");
			}
			String sFileName = model.getPDFAttachmentFilename();
			model.setOrUpdatePDFAttachment(pdfCombined, sFileName);
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	/**
	 * By convention, the data model holds only pages in the current PDF
	 * For ordinary PDF files this is a subset of the whole file.
	 */
	public void gotoPageNo( Integer iPageNo ) {
		this.iPage = iPageNo;
		model = new StdeCviXmlModel();
		byte pdfPageBytes[] = getPDFBytes(iPage);
		if( pdfPageBytes == null ) {
			logger.error("null page bytes in gotoPageNo(" + iPageNo + ") of " + getPageCount() + " pages");
		}
		model.setOrUpdatePDFAttachment(pdfPageBytes, fSource.getName());
	}
	

	/**
	 * The Data Model contains only the current CVI Pages.  The Decoder contains all.
	 */
	@Override
	public StdeCviXmlModel getDataModel() {
		if( model == null ) {
			model = new StdeCviXmlModel();
			byte pageBytes[] = getPDFBytes(1);
			model.setOrUpdatePDFAttachment(pageBytes, fSource.getName());
		}
		return model;
	}
	
	
	/**
	 * Overridden by file types (Plain PDF) that need default rotation set in config file
	 * @return
	 */
	@Override
	public int getRotation() {
		return CivetConfig.getRotation();
	}


	@Override
	public byte[] getPDFBytes() {
		if( pdfBytes == null ) {
			try {
				logger.info("Reading PDF file outside of constructor");
				pdfBytes = FileUtils.readBinaryFile(fSource);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("Failed to read file " + sFilePath, e);
			}
		}
		return pdfBytes;
	}

	@Override
	public byte[] getPDFBytes(int iPageNo) {
		byte bOut[] = null;
		ArrayList<Integer> aPages = new ArrayList<Integer>();
		aPages.add(iPageNo);
		bOut = getPDFBytes(aPages);
		return bOut;
	}

	@Override
	public byte[] getPDFBytes(ArrayList<Integer> aPages) {
		byte bOut[] = null;
		if( pdfBytes == null ) {
			pdfBytes = getPDFBytes();
			if( pdfBytes == null ) {
				logger.error("Unable to page getPDFBytes for " + getFileName());
				return null;
			}
		}
		ByteArrayOutputStream baOut = new ByteArrayOutputStream();
		try {
			com.itextpdf.text.Document newDocument = new com.itextpdf.text.Document();
			PdfCopy writer = new PdfCopy(newDocument, baOut);
			newDocument.open();
			for( Integer iPage : aPages ) {
				PdfImportedPage pip = writer.getImportedPage(iTextPdfReader, iPage);
				writer.addPage(pip);
			}
			newDocument.close();
			bOut = baOut.toByteArray();
			writer.close();
			int iLen = bOut.length;
			if( iLen ==  0 ) {
				logger.error("No bytes from file " + getFileName() + " page " + iPage );
				bOut = null;
			}
		} catch( IOException ioe ) {
			logger.error(ioe.getMessage() + "\nIO error extracting pages to byte array\n");
			bOut = null;
		} catch( DocumentException de ) {
			logger.error(de.getMessage() + "\nDocument error extracting pages to byte array");
			bOut = null;
		}
		return bOut;
	}

	@Override
	public String getSystem() {
		return "Paper";
	}

}
