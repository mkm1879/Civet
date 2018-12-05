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
import java.io.IOException;
import java.util.ArrayList;

import org.jpedal.PdfDecoder;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;

import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.pdfgen.PDFUtils;
import edu.clemson.lph.utils.FileUtils;

/**
 * 
 */
public class PdfSourceFile extends SourceFile {
	private int iPages = 0;
	
	public PdfSourceFile( String sPath ) throws SourceFileException {
		super(sPath);
		type = Types.PDF;
		if( fSource == null || !fSource.exists() )
			logger.error("File " + sPath + " does not exist");
		pdfDecoder = new PdfDecoder();
		try {
			pdfBytes = FileUtils.readBinaryFile(fSource);
			byte pageBytes[] = getPDFBytes();
			pdfDecoder.openPdfArray(pageBytes);
			if( PDFUtils.isXFA(pdfBytes) ) 
				logger.error("File " + fSource.getName() + " looks like XFA but created as ordinary PDF");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Could not open PDF file " + fSource.getName(), e);
		}
		model = new StdeCviXmlModel();
		model.addPDFAttachement(getPDFBytes(1), fSource.getName());
	}
	
	/**
	 * All we assert here is that this is a PDF file, sub types do the rest.
	 * Rule out CO/KS, mCVI, or AgView before instantiating as this.
	 * @param sPath
	 * @return
	 */
	public static boolean isPDF( String sPath ) {
		boolean bRet = false;
		if( sPath.toLowerCase().endsWith(".pdf") ) {
			bRet = true;
		}
		return bRet;
	}
	
	/** 
	 * Being here we know we are a PDF and if more than one page, pageable.
	 * @return
	 */
	@Override
	public boolean isPageable() {
		boolean bRet = false;
		if( pdfDecoder != null && getPages() > 1 )
			bRet = true;
		return bRet;
	}
	
	/** 
	 * Being here we know we are a PDF and if more than one page, pageable.
	 * Because XFA is a different class, we can split.
	 * @return
	 */
	@Override
	public boolean canSplit() {
		boolean bRet = false;
		if( pdfDecoder != null && getPages() > 1 )
			bRet = true;
		return bRet;
	}

	/**
	 * The Data Model contains only the current CVI Pages.  The Decoder contains all.
	 */
	@Override
	public StdeCviXmlModel getDataModel() {
		StdeCviXmlModel model = new StdeCviXmlModel();
		ArrayList<Integer> aPages = new ArrayList<Integer>();
		aPages.add(1);
		byte pageBytes[] = getPDFBytes(aPages);
		model.addPDFAttachement(pageBytes, fSource.getName());
		return model;
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
		if( pdfBytes != null ) {
			ByteArrayOutputStream baOut = new ByteArrayOutputStream();
			try {
				PdfReader reader = new PdfReader(pdfBytes);
				com.itextpdf.text.Document document = new com.itextpdf.text.Document();
				PdfCopy writer = new PdfCopy(document, baOut);
				document.open();
				PdfImportedPage pip = writer.getImportedPage(reader, iPageNo);
				writer.addPage(pip);
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
	public int getPages() {
		if( pdfDecoder != null ) {
			iPages = pdfDecoder.getPageCount();
		}
		return iPages;
	}
	

}
