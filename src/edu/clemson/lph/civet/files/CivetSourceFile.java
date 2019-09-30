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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

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
public class CivetSourceFile extends SourceFile {
	
	public CivetSourceFile( File fFile ) throws SourceFileException {
		super(fFile);
		type = Types.Civet;
		if( fSource != null && fSource.exists() && fSource.isFile() ) {
			try {
				String sStdXml = FileUtils.readTextFile(fSource);
				if( isV1(sStdXml) ) {
					sStdXml = makeV2( sStdXml );
				}
				model = new StdeCviXmlModel(sStdXml);
				pdfBytes = model.getPDFAttachmentBytes();
				if( pdfBytes == null ) {
					System.err.println("No PDF in CivetFile");
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

	
	private boolean isV1(String sStdXml) {
		boolean bRet = false;
		int iV1 = sStdXml.indexOf("xmlns=\"http://www.usaha.org/xmlns/ecvi\"");
		if( iV1 >= 0 )
			bRet = true;
		return bRet;
	}

	private String makeV2(String sStdXmlV1) {
		String sRet = null;
		String sXSLT = "eCVI1_to_eCVI2.xsl";
		try {
			File fTransform = new File(sXSLT);
			InputStream isXLT = new FileInputStream(fTransform);
//			InputStream isXLT = getClass().getResourceAsStream("../res/eCVI1_to_eCVI2.xsl");
			StringReader sourceReader = new StringReader( sStdXmlV1 );
			ByteArrayOutputStream baosDest = new ByteArrayOutputStream();
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer;
			transformer = tFactory.newTransformer(new StreamSource(isXLT));
			transformer.transform(new StreamSource(sourceReader),
					new StreamResult(baosDest));
			sRet = new String( baosDest.toByteArray(), "UTF-8" );
			sRet = postProcessv2(sRet);
		} catch ( TransformerException e) {
			logger.error("Failed to transform XML with XSLT: \"eCVI1_to_eCVI2.xsl\"", e);
		} catch (UnsupportedEncodingException e) {
			logger.error("Should not see this unsupported encoding", e);
		} catch (FileNotFoundException e) {
			logger.error("Could not find XSLT: \"eCVI1_to_eCVI2.xsl\"", e);
		}
		return sRet;
	}

	public static boolean isCivet( File fFile ) {
		boolean bRet = false;
		String sName = fFile.getName();
		if( sName.toLowerCase().endsWith(".cvi") ) {
			bRet = true;
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
				String sStdXml = FileUtils.readTextFile(fSource);
				if( sStdXml != null )
					model = new StdeCviXmlModel( sStdXml );
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
	
	/**
	 * The Java transform labels every element with its own namespace declaration!
	 * this removes that because the whole thing is in ecvi2
	 * @param sV2  The output of the java transform.
	 * @return
	 */
	private String postProcessv2( String sV2 ) {
		String sRet = null;
		String prePattern = "<ns[0-9]+:";
		String prePattern2 = "</ns[0-9]+:";
		String postPattern = "xmlns:ns[0-9]+=\"http://www.usaha.org/xmlns/ecvi2\"";
		sRet = sV2.replaceAll(prePattern, "<");
		sRet = sRet.replaceAll(prePattern2, "</");
		sRet = sRet.replaceAll(postPattern, "");
		return sRet;
	}

	@Override
	public String getSystem() {
		return "CivetData";
	}

}
