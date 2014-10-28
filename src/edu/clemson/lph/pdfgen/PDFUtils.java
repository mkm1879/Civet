package edu.clemson.lph.pdfgen;
/*
Copyright 2014 Michael K Martin

This file is part of Civet.

Civet is free software: you can redistribute it and/or modify
it under the terms of the Lesser GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Civet is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the Lesser GNU General Public License
along with Civet.  If not, see <http://www.gnu.org/licenses/>.
*/
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PRAcroForm;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;

import edu.clemson.lph.civet.Civet;

public class PDFUtils {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());

	public PDFUtils() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Given an array of bytes from a PDF determine whether at least the first page can be extracted
	 * by iText;
	 * @param byte[] data to test parse
	 * @return byte[]
	 */
	public static boolean canExtractPages(byte[] pdfDataIn) {
		boolean bRet = false;
		ByteArrayOutputStream baOut = new ByteArrayOutputStream();
		try {
			PdfReader reader = new PdfReader(pdfDataIn);
			com.itextpdf.text.Document document = new com.itextpdf.text.Document();
			PdfCopy writer = new PdfCopy(document, baOut);
			document.open();
			PdfImportedPage pip = writer.getImportedPage(reader, 1);
			writer.addPage(pip);
			PRAcroForm form = reader.getAcroForm();
			if (form != null) {
				writer.copyAcroForm(reader);
			}
			document.close();
			byte[] pdfDataOut = baOut.toByteArray();
			int iLen = pdfDataOut.length;
			if( iLen > 0 ) 
				bRet = true;
		} catch( IOException ioe ) {
			logger.error(ioe.getMessage() + "\nIO error extracting pages to byte array\n");
			bRet = false;
		} catch( DocumentException de ) {
			logger.error(de.getMessage() + "\nDocument error extracting pages to byte array");
			bRet = false;
		}
		return bRet;
	}// End decode pages to new PDF

}
