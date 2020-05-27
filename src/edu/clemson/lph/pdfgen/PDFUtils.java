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

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.XfaForm;

import edu.clemson.lph.civet.Civet;

public class PDFUtils {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());

	public PDFUtils() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Given an array of bytes from a PDF determine whether at least the first page can be extracted
	 * by iText;
	 * @param byte[] data to test parse
	 * @return boolean
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
	
	/**
	 * Given an array of bytes from a PDF extract one page using iText;
	 * @param byte[] data to test parse
	 * @return byte[]
	 */
	public static byte[] extractPage(byte[] pdfDataIn, int iPage) {
		byte[] bRet = null;
		ByteArrayOutputStream baOut = new ByteArrayOutputStream();
		try {
			PdfReader reader = new PdfReader(pdfDataIn);
			com.itextpdf.text.Document document = new com.itextpdf.text.Document();
			PdfCopy writer = new PdfCopy(document, baOut);
			document.open();
			PdfImportedPage pip = writer.getImportedPage(reader, iPage);
			writer.addPage(pip);
			document.close();
			byte[] pdfDataOut = baOut.toByteArray();
			int iLen = pdfDataOut.length;
			if( iLen == 0 ) 
				bRet = null;
		} catch( IOException ioe ) {
			logger.error(ioe.getMessage() + "\nIO error extracting pages to byte array\n");
			bRet = null;
		} catch( DocumentException de ) {
			logger.error(de.getMessage() + "\nDocument error extracting pages to byte array");
			bRet = null;
		}
		return bRet;
	}// End decode pages to new PDF
		
	/**
	 * Use iText 5.x to determine whether a PDF contains an XFA form.
	 * @param pdfDataIn
	 * @return
	 */
	public static boolean isXFA(byte[] pdfDataIn) {
		boolean bRet = false;
		if( pdfDataIn == null || pdfDataIn.length == 0 ) return false;
		PdfReader reader;
		try {
			reader = new PdfReader(pdfDataIn);
			XfaForm form = new XfaForm(reader);
			bRet = form.isXfaPresent();
			if( bRet ) {
				// Check again that this is really an XFA data file
				Node xmlNode = form.getDatasetsNode();
				if( "xfa:datasets".equals(xmlNode.getNodeName()) ) {
					Node nData = xmlNode.getFirstChild();
					if( !"xfa:data".equals(nData.getNodeName()) ) {
						bRet = false;
					}
				}
			}
		} catch (IOException e) {
			logger.error(e);
			bRet = false;
		} catch (ParserConfigurationException e) {
			logger.error(e);
			bRet = false;
		} catch (SAXException e) {
			logger.error(e);
			bRet = false;
		}
		return bRet;
	}
	
	public static Node getXFADataNode(byte[] pdfDataIn) {
		Node nData = null;
		try {
			PdfReader reader = new PdfReader(pdfDataIn);
			XfaForm form = new XfaForm(reader);
			Node xmlNode = form.getDatasetsNode();
			if( "xfa:datasets".equals(xmlNode.getNodeName()) ) {
				nData = xmlNode.getFirstChild();
				if( !"xfa:data".equals(nData.getNodeName()) ) {
					System.err.println(nData.getNodeName());
					nData = null;
				}
			}
			else
				System.err.println(xmlNode.getNodeName() );
		} catch (IOException e) {
			logger.error(e);
		} catch (ParserConfigurationException e) {
			logger.error(e);
		} catch (SAXException e) {
			logger.error(e);
		}
		
		return nData;
	}

}
