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
import edu.clemson.lph.pdfgen.PDFViewer;
import edu.clemson.lph.utils.FileUtils;

/**
 * 
 */
class PdfSourceFile extends SourceFile {
	
	/**
	 * Used only in cloneCurrentState
	 */
	protected PdfSourceFile() {
		
	}
	
	PdfSourceFile( File fFile, PDFViewer viewer ) throws SourceFileException {
		super(fFile, viewer);
		type = Types.PDF;
		try {
			pdfBytes = FileUtils.readBinaryFile(fSource);
		} catch (Exception e) {
			throw new SourceFileException(e);
		}
		model = new StdeCviXmlModel();
		model.setOrUpdatePDFAttachment(getPDFBytes(iPage), fSource.getName());
		viewer.alterRotation(180);  // Our scanned PDFs are off.
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
	@Override
	public void gotoPageNo( Integer iPageNo ) {
		this.iPage = iPageNo;
		model = new StdeCviXmlModel();
		byte pdfPageBytes[] = getPDFBytes(iPage);
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
		if( pdfBytes != null ) {
			ByteArrayOutputStream baOut = new ByteArrayOutputStream();
			try {
				PdfReader reader = new PdfReader(pdfBytes);
				com.itextpdf.text.Document document = new com.itextpdf.text.Document();
				PdfCopy writer = new PdfCopy(document, baOut);
				document.open();
				for( Integer iPage : aPages ) {
					PdfImportedPage pip = writer.getImportedPage(reader, iPage);
					writer.addPage(pip);
				}
				document.close();
				bOut = baOut.toByteArray();
				int iLen = bOut.length;
				if( iLen ==  0 ) 
					bOut = null;
			} catch( IOException ioe ) {
				logger.error(ioe.getMessage() + "\nIO error extracting pages to byte array\n");
				bOut = null;
			} catch( DocumentException de ) {
				logger.error(de.getMessage() + "\nDocument error extracting pages to byte array");
				bOut = null;
			}
		}
		return bOut;
	}

	@Override
	public String getSystem() {
		return "Paper";
	}

}
