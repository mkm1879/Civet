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

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;

import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.dialogs.QuestionDialog;
import edu.clemson.lph.pdfgen.MergePDF;
import edu.clemson.lph.pdfgen.PDFUtils;
import edu.clemson.lph.utils.FileUtils;

/**
 * 
 */
public class PdfSourceFile extends SourceFile {
	
	public PdfSourceFile( File fFile ) throws SourceFileException {
		super(fFile);
		type = Types.PDF;
		try {
			pdfBytes = FileUtils.readBinaryFile(fSource);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Could not open PDF file " + fSource.getName(), e);
		}
		model = new StdeCviXmlModel();
		model.setPDFAttachment(getPDFBytes(iPage), fSource.getName());
	}
	
	/**
	 * All we assert here is that this is a PDF file, sub types do the rest.
	 * Rule out CO/KS, mCVI, or AgView before instantiating as this.
	 * @param sPath
	 * @return
	 */
	public static boolean isPDF( File fFile ) {
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
	 * Return model from previous page(s) to be saved.
	 * Start new model with the page after the one we were on.
	 */
	@Override
	public StdeCviXmlModel split() throws SourceFileException {
		StdeCviXmlModel newModel = model;
		model = new StdeCviXmlModel();
		iPage++;
		byte pdfPageBytes[] = getPDFBytes(iPage);
		model.setPDFAttachment(pdfPageBytes, fSource.getName());
		return newModel;
	}
	
	@Override
	public void addPageToCurrent( Integer iPage ) throws SourceFileException {
		this.iPage = iPage;
		byte pdfBytesCurrent[] = model.getPDFAttachmentBytes();
		byte pdfPageBytes[] = getPDFBytes(iPage);  // extract pages from original full pdf
		try {
			byte pdfCombined[] = MergePDF.appendPDFtoPDF(pdfBytesCurrent, pdfPageBytes);
			String sFileName = model.getPDFAttachmentFilename();
			model.setPDFAttachment(pdfCombined, sFileName);
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	@Override
	public void setCurrentPage( Integer iPage ) {
		this.iPage = iPage;
		model = new StdeCviXmlModel();
		byte pdfPageBytes[] = getPDFBytes(iPage);
		model.setPDFAttachment(pdfPageBytes, fSource.getName());
	}
	

	/**
	 * The Data Model contains only the current CVI Pages.  The Decoder contains all.
	 */
	@Override
	public StdeCviXmlModel getDataModel() {
		if( model == null ) {
			model = new StdeCviXmlModel();
			byte pageBytes[] = getPDFBytes(1);
			model.setPDFAttachment(pageBytes, fSource.getName());
		}
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
	
}
