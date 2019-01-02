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

import org.jpedal.PdfDecoder;

import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.pdfgen.MergePDF;
import edu.clemson.lph.pdfgen.PDFUtils;
import edu.clemson.lph.utils.FileUtils;

/**
 * 
 */
public class ImageSourceFile extends SourceFile {
	
	public ImageSourceFile( File fFile ) throws SourceFileException {
		super(fFile);
		type = Types.Image;
		if( fSource != null && fSource.exists() && fSource.isFile() ) {
			model = new StdeCviXmlModel();
			model.setPDFAttachment(getPDFBytes(), fSource.getName() + ".pdf");			
		}
		else {
			logger.error("File " + sFilePath + " does not exist");
		}
	}

	public static boolean isImage( File fFile ) {
		boolean bRet = false;
		String sName = fFile.getName();
		String sExt = FileUtils.getExt(sName);
		if( sExt != null && sExt.trim().length() > 0 ) {
			if( sExt.toLowerCase().equals(".png") ) bRet = true;
			else if( sExt.toLowerCase().equals(".gif") ) bRet = true;
			else if( sExt.toLowerCase().equals(".bmp") ) bRet = true;
			else if( sExt.toLowerCase().equals(".jpg") ) bRet = true;
			else if( sExt.toLowerCase().equals(".jpeg") ) bRet = true;
		}
		return bRet;
	}

	@Override
	public StdeCviXmlModel getDataModel() {
		if( model == null ) {
			model = new StdeCviXmlModel();
			model.setPDFAttachment(getPDFBytes(), fSource.getName() + ".pdf");			
		}
		return model;
	}

	@Override
	public byte[] getPDFBytes() {
		if( pdfBytes == null ) {
			if( model == null ) {
				model = getDataModel();
			}
			pdfBytes = model.getPDFAttachmentBytes();
		}
		return pdfBytes;
	}

	@Override
	public boolean canSplit() {
		return false;
	}

	private byte[] makePdf() {
		byte rawPdfBytes[] = null;
		byte fileBytes[];
		try {
			fileBytes = FileUtils.readBinaryFile(fSource);
			if( sFilePath.toLowerCase().endsWith(".jpg") || sFilePath.toLowerCase().endsWith(".jpeg") ) {
				rawPdfBytes = MergePDF.jpgToPdfBytes(fileBytes);
			}
			else if( sFilePath.toLowerCase().endsWith(".png")  ) {
				rawPdfBytes = MergePDF.pngToPdfBytes(fileBytes);
			}
			else if( sFilePath.toLowerCase().endsWith(".bmp")  ) {
				rawPdfBytes = MergePDF.bmpToPdfBytes(fileBytes);
			}
			else if( sFilePath.toLowerCase().endsWith(".gif")  ) {
				rawPdfBytes = MergePDF.gifToPdfBytes(fileBytes);
			}
			else {
				logger.error("Unknown file type " + fSource.getName());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
		return rawPdfBytes;
	}
}
