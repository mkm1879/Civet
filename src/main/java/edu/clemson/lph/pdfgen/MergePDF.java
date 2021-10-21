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
import java.awt.Frame;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.clemson.lph.logging.Logger;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import edu.clemson.lph.civet.Civet;


public class MergePDF {
      private static Logger logger = Logger.getLogger();

	public static void testMerge() {
		File fDir = new File( "." );
		JFileChooser open = new JFileChooser( fDir );
		open.setFileSelectionMode(JFileChooser.FILES_ONLY);
		open.setFileFilter(new FileNameExtensionFilter(
				"JPG & PDF Files", "jpg", "pdf", "jpeg"));
		open.setMultiSelectionEnabled(true);

		int resultOfFileSelect = JFileChooser.ERROR_OPTION;
		while(resultOfFileSelect==JFileChooser.ERROR_OPTION){

			resultOfFileSelect = open.showOpenDialog((Frame)null);

			if(resultOfFileSelect==JFileChooser.ERROR_OPTION) {
				logger.error("File choose error");
			}

			if(resultOfFileSelect==JFileChooser.APPROVE_OPTION){
				try {
					byte docBytes[] = null;
					File fList[] = open.getSelectedFiles();
					if( fList != null && fList.length > 0 ) {
						for( File fThis : fList ) {
							// TODO assemble pdfs and/or jpegs into a single PDF to process like the rest
							if( fThis.getName().toLowerCase().endsWith("pdf") ) {
								// Add a pdf to the Document
								docBytes = MergePDF.appendPDFtoPDF( docBytes, fThis ); 
							}
							else {
								// Only pdf and jpg allowed so must be a jpg (weak)
								// Add a jpg to the Document
								docBytes = MergePDF.appendJPGPagetoPDF( docBytes, fThis );
							}
						}
					}
					FileOutputStream fsOut = new FileOutputStream( "MergeTest.pdf" );
					if( fsOut != null ) {
						if( docBytes != null ) 
							fsOut.write(docBytes);
						fsOut.close();
					}
				} catch( IOException ioe ) {
					logger.error(ioe);					
				}
			}
		}
	}


	public static byte[] appendJPGPagetoPDF( byte destBytes[], File fJPEGAdditional ) throws IOException {
		FileInputStream fsIn = new FileInputStream( fJPEGAdditional );
		ByteArrayInputStream baisAdd = null;
		ByteArrayOutputStream baosOut = null;
		byte[] aOut = {};
		try {
			byte jpgBytes[] = new byte[(int)fJPEGAdditional.length()];
			fsIn.read(jpgBytes);
			byte addBytes[] = jpgToPdfBytes( jpgBytes );
			baisAdd = new ByteArrayInputStream( addBytes );
			ArrayList<InputStream> lInputs = new ArrayList<InputStream>();
			if( destBytes != null ) {
				ByteArrayInputStream fisDest = new ByteArrayInputStream( destBytes );
				lInputs.add(fisDest);
			}
			lInputs.add(baisAdd);
			baosOut = new ByteArrayOutputStream();
			concatPDFs( lInputs, baosOut, true);
			aOut = baosOut.toByteArray();
		} catch( IOException ioe ) {
			throw ioe;
		} finally {
			fsIn.close();
			if( baisAdd != null ) baisAdd.close();
			if( baosOut != null ) baosOut.close();
		}
		return aOut;
	}

	public static byte[] appendPDFtoPDF( byte destBytes[], File fJPEGAdditional ) throws IOException {
		FileInputStream fsIn = null;
		byte addBytes[] = new byte[(int)fJPEGAdditional.length()];
		ByteArrayInputStream fisAdd = null;
		ByteArrayOutputStream baosOut = null;
		byte[] aOut = {};
		try {
			fsIn = new FileInputStream( fJPEGAdditional );
			fsIn.read(addBytes);
			fisAdd = new ByteArrayInputStream( addBytes );
			ArrayList<InputStream> lInputs = new ArrayList<InputStream>();
			if( destBytes != null ) {
				ByteArrayInputStream fisDest = new ByteArrayInputStream( destBytes );
				lInputs.add(fisDest);
			}
			lInputs.add(fisAdd);
			baosOut = new ByteArrayOutputStream();
			concatPDFs( lInputs, baosOut, true);
			aOut = baosOut.toByteArray();
		} catch( IOException ioe ) {
			throw ioe;
		} finally {
			if( fsIn != null ) fsIn.close();
			if( fisAdd != null ) fisAdd.close();
			if( baosOut != null ) baosOut.close();
		}
		return aOut;
	}


	public static byte[] appendPDFtoPDF( byte destBytes[], byte addBytes[] ) throws IOException {
		FileInputStream fsIn = null;
		ByteArrayInputStream fisAdd = null;
		ByteArrayOutputStream baosOut = null;
		byte[] aOut = {};
		try {
			fisAdd = new ByteArrayInputStream( addBytes );
			ArrayList<InputStream> lInputs = new ArrayList<InputStream>();
			if( destBytes != null ) {
				ByteArrayInputStream fisDest = new ByteArrayInputStream( destBytes );
				lInputs.add(fisDest);
			}
			lInputs.add(fisAdd);
			baosOut = new ByteArrayOutputStream();
			concatPDFs( lInputs, baosOut, true);
			aOut = baosOut.toByteArray();
		} finally {
			if( fsIn != null ) fsIn.close();
			if( fisAdd != null ) fisAdd.close();
			if( baosOut != null ) baosOut.close();
		}
		return aOut;
	}


	public static void appendJPGPagetoPDF( File fDestination, File fJPEGAdditional ) throws IOException {
		FileInputStream fsDest = null;
		FileInputStream fsIn = null;
		FileOutputStream fsOut = null;
		ByteArrayInputStream baisDest = null;
		ByteArrayInputStream baisAdd = null;
		try {
			fsDest = new FileInputStream( fDestination );
			fsIn = new FileInputStream( fJPEGAdditional );
			byte jpgBytes[] = new byte[(int)fJPEGAdditional.length()];
			fsIn.read(jpgBytes);
			byte addBytes[] = jpgToPdfBytes( jpgBytes );
			byte destBytes[] = new byte[(int)fDestination.length()];
			fsDest.read(destBytes);
			baisDest = new ByteArrayInputStream( destBytes );
			baisAdd = new ByteArrayInputStream( addBytes );
			ArrayList<InputStream> lInputs = new ArrayList<InputStream>();
			lInputs.add(baisDest);
			lInputs.add(baisAdd);
			fsOut = new FileOutputStream( fDestination );
			concatPDFs( lInputs, fsOut, true);
		} catch( IOException ioe ) {
			throw ioe;
		} finally {
			if( fsDest != null ) fsDest.close();
			if( fsIn != null ) fsIn.close();
			if( fsOut != null ) fsOut.close();
			if( baisDest != null ) baisDest.close();
			if( baisAdd != null ) baisAdd.close();
		}
	}
	/**
	 * This method is very specific to JPG images of CVIs.  Assumes them to be letter sized
	 * and landscape orientation.  Other sizes will be scaled to fit.
	 * @param jpgBytes byte[] with the contents of a jpg file.
	 * @return
	 */
	public static byte[] jpgToPdfBytes( byte jpgBytes[] ) {
		try {
			Image jpgImage = new Jpeg( jpgBytes );
			return imageToPdfBytes( jpgImage );
		}
		catch (IOException e) {
			logger.error(e);
		} catch (BadElementException e) {
			logger.error(e);
		}
		return null;
	}

	/**
	 * This method is very specific to PNG images of CVIs.  Assumes them to be letter sized
	 * and landscape orientation.  Other sizes will be scaled to fit.
	 * @param pngBytes byte[] with the contents of a png file.
	 * @return
	 */
	public static byte[] pngToPdfBytes( byte pngBytes[] ) {
		try {
			Image pngImage = com.itextpdf.text.pdf.codec.PngImage.getImage(pngBytes);
			return imageToPdfBytes( pngImage );
		}
		catch (IOException e) {
			logger.error(e);
		}
		return null;
	}

	/**
	 * This method is very specific to BMP images of CVIs.  Assumes them to be letter sized
	 * and landscape orientation.  Other sizes will be scaled to fit.
	 * @param bmpBytes byte[] with the contents of a png file.
	 * @return
	 */
	public static byte[] bmpToPdfBytes( byte bmpBytes[] ) {
		try {
			Image bmpImage = com.itextpdf.text.pdf.codec.BmpImage.getImage(bmpBytes);
			return imageToPdfBytes( bmpImage );
		}
		catch (IOException e) {
			logger.error(e);
		}
		return null;
	}

	/**
	 * This method is very specific to GIF images of CVIs.  Assumes them to be letter sized
	 * and landscape orientation.  Other sizes will be scaled to fit.
	 * @param bmpBytes byte[] with the contents of a png file.
	 * @return
	 */
	public static byte[] gifToPdfBytes( byte gifBytes[] ) {
		try {
			com.itextpdf.text.pdf.codec.GifImage gif = new com.itextpdf.text.pdf.codec.GifImage( gifBytes );
			Image gifImage = gif.getImage(1);
			return imageToPdfBytes( gifImage );
		}
		catch (IOException e) {
			logger.error(e);
		}
		return null;
	}

	/**
	 * This version can be used stand alone or called from specific image type methods.
	 * @param Image Java Image object.
	 * @return
	 */
	public static byte[] imageToPdfBytes(Image imageIn ) {
		// new pdf Document
		Rectangle pagesize = PageSize.LETTER.rotate();  // Landscape letter size (11x8.5)
		Document doc = new Document( pagesize, 0f, 0f, 0f, 0f ); // no margins
		ByteArrayOutputStream baosPDF = new ByteArrayOutputStream();
		byte[] aOut = {};
		PdfWriter docWriter = null;
		try {
			docWriter = PdfWriter.getInstance(doc, baosPDF);
			doc.open();
			imageIn.scaleToFit( pagesize.getWidth(), pagesize.getHeight()  );
			doc.add(imageIn);
			doc.close();
			aOut = baosPDF.toByteArray();
		} catch (DocumentException e) {
			logger.error(e);
		}
		finally {
			if( doc != null )
				doc.close();
			if( docWriter != null )
				docWriter.close();
			try {
				baosPDF.close();
			} catch (IOException e) {
				logger.error("Error closing generated pdf", e);
			}
		}
		return aOut;
	}
	
	public static void appendPDFtoPDF( File fDestination, File fAdditional ) throws IOException {
		FileInputStream fsDest = null;
		FileInputStream fsIn = null;
		FileOutputStream fsOut = null;
		ByteArrayInputStream fisDest = null;
		try {
			fsDest = new FileInputStream( fDestination );
			fsIn = new FileInputStream( fAdditional );
			byte destBytes[] = new byte[(int)fDestination.length()];
			fsDest.read(destBytes);
			fisDest = new ByteArrayInputStream( destBytes );
			ArrayList<InputStream> lInputs = new ArrayList<InputStream>();
			lInputs.add(fisDest);
			lInputs.add(fsIn);
			fsOut = new FileOutputStream( fDestination );
			concatPDFs( lInputs, fsOut, true);
		} catch( IOException ioe ) {
			throw ioe;
		} finally {
			if( fsDest != null ) fsDest.close();
			if( fsIn != null ) fsIn.close();
			if( fisDest != null ) fisDest.close();
			if( fsOut != null ) fsOut.close();
		}
	}

	public static void concatPDFs(List<InputStream> pdfInputStreams, OutputStream outputStream, boolean paginate) {
		Document document = new Document();
		try {
			PdfCopy cp = new PdfCopy( document, outputStream );
			document.open();
			Iterator<InputStream> iteratorPDFReader = pdfInputStreams.iterator();

			// Loop through the PDF streams and add to the output.
			while (iteratorPDFReader.hasNext()) {
				InputStream is = iteratorPDFReader.next();
				PdfReader pdfReader = new PdfReader( is );
				int n = pdfReader.getNumberOfPages();
				for (int pageNo = 0; pageNo < n; ) {
					pdfReader.getPageN(pageNo);
					cp.addPage(cp.getImportedPage(pdfReader, ++pageNo));
				}
			}
			document.close();
			outputStream.flush();
			outputStream.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}


}// End utility class MergePDF