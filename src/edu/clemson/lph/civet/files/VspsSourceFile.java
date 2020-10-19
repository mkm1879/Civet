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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.itextpdf.text.pdf.PdfReader;

import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.pdfgen.PDFUtils;
import edu.clemson.lph.utils.FileUtils;

/**
 * 
 */
public class VspsSourceFile extends SourceFile {
	
	public VspsSourceFile( File fFile ) throws SourceFileException {
		super(fFile);
		type = Types.Civet;
		if( fSource != null && fSource.exists() && fSource.isFile() ) {
			try {
				
				byte[] xmlBytes = FileUtils.readUTF8File(fSource);
				model = new StdeCviXmlModel(xmlBytes);
				pdfBytes = model.getPDFAttachmentBytes();
				if( pdfBytes == null ) {
					System.err.println("No PDF in VSPS File");
				}
				try {
					iTextPdfReader = new PdfReader(pdfBytes);
				} catch (FileNotFoundException e) {
					throw new SourceFileException( "Attempt to read non-file " + sFilePath );
				} catch (IOException e) {
					throw new SourceFileException( "Failed to read file " + sFilePath );
				}   
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("Could not open PDF file " + fSource.getName(), e);
			}
		}
		else {
			logger.error("File " + sFilePath + " does not exist");
		}
	}
	
	@Override
	public boolean isXFA() {
		return PDFUtils.isXFA(pdfBytes);
	}


	public static boolean isVsps( File fFile ) {
		boolean bRet = false;
		String sName = fFile.getName();
		if( sName.toLowerCase().endsWith(".cvi") ) {
			String sXML;
			try {
				sXML = FileUtils.readTextFile(fFile,3);
				if( sXML.indexOf("CviNumberIssuedBy=\"VSPS\"") > 0 )
					bRet = true;
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return bRet;
	}
	
	@Override
	public boolean isDataFile() {
		return true;
	}

	@Override
	public StdeCviXmlModel getDataModel() {
		if( model == null ) {
			try {
				byte[] xmlBytes = FileUtils.readUTF8File(fSource);
				if( xmlBytes != null )
					model = new StdeCviXmlModel( xmlBytes );
			} catch (Exception e) {
				logger.error("Failed to read file " + sFilePath, e);
			}
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
		// Std File
		return false;
	}
	
	@Override
	public String getSystem() {
		return "VSPS";
	}

}
