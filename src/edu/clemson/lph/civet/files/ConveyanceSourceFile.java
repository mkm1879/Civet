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
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;

import edu.clemson.lph.civet.prefs.CivetConfig;
import javax.xml.parsers.DocumentBuilder;
import edu.clemson.lph.civet.xml.SafeDocBuilder;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.pdfgen.PDFUtils;
import edu.clemson.lph.utils.FileUtils;
import edu.clemson.lph.utils.Validator; 

/**
 * 
 */
public class ConveyanceSourceFile extends SourceFile {
	static {
		// BasicConfigurator replaced with PropertyConfigurator.
	     PropertyConfigurator.configure("CivetConfig.txt");
	     logger.setLevel(Level.INFO);
	}
	
	public ConveyanceSourceFile( File fFile ) throws SourceFileException {
		super(fFile);
		type = Types.Conveyance;
		if( fSource == null || !fSource.exists() ) {
			throw new SourceFileException("File " + sFilePath + " does not exist");
		}
		try {
			pdfBytes = FileUtils.readBinaryFile(fFile);
			iTextPdfReader = new PdfReader(pdfBytes);
		} catch (Exception e) {
			throw new SourceFileException(e);
		}
		String sAcrobatXML = getXML();
		String sStdXML = toStdXMLString( sAcrobatXML);
		
		model = new StdeCviXmlModel(sStdXML);
		model.setOrUpdatePDFAttachment(pdfBytes, fSource.getName());
	}
	
	private String getXML() {
		StringBuffer sbRet = new StringBuffer();
		final AcroFields fields = iTextPdfReader.getAcroFields();
		final Map<String, Object> values = new HashMap<>();
		sbRet.append("<fields xmlns:xfdf=\"http://ns.adobe.com/xfdf-transition/\">");
		for (String fieldName : (Set<String>) fields.getFields().keySet()) {
			String sXmlField = fieldName.replaceAll(" ", "").replaceAll("#", "Num").replaceAll("/", "_");
			sbRet.append("<" + sXmlField + ">" + fields.getField(fieldName) + "</" + sXmlField + ">" );
		    values.put(fieldName, fields.getField(fieldName));
		}
		sbRet.append("</fields>");
		String sRet = sbRet.toString();
		return sRet;
	}

	/**
	 * This is very much a hack but needed to QUICKLY estimate if this is XFA
	 * and therefore probably CO/KS eCVI.
	 * More deterministic would be to use PDFUtils.isXFA() but that reads 
	 * the whole file.
	 * @param sPath
	 * @return
	 */
	public static boolean isConveyance( File fFile ) {
		boolean bRet = false;
		if( fFile == null ) return false;
		String sName = fFile.getName();
		if( sName.toLowerCase().endsWith("for-CIVET.pdf") ) {
			if( fFile.exists() && fFile.isFile() ) {
				String sTop;
				try {
					sTop = FileUtils.readTextFile( fFile, 4 );
					int iLoc = sTop.indexOf("<</Filter/FlateDecode");
					int iLoc2 = sTop.indexOf("PDF-1.6");
					if( iLoc >= 0 && iLoc2 >= 0 ) {
						// is a fancy PDF but not sure it is not XFA 
						byte pdfBytes[] = FileUtils.readBinaryFile(fFile);
						if( !PDFUtils.isXFA(pdfBytes) ) {
							bRet = true;
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error(e);
				}
			}
		}
		return bRet;
	}

	@Override
	public StdeCviXmlModel getDataModel() {
		if( model == null ) {
			try { 
				if( fData != null && fData.exists() && fData.isFile() ) {
					String sAcrobatXml = getXML();
					String sStdXml = toStdXMLString( sAcrobatXml );
					model = new StdeCviXmlModel(sStdXml);
					String sInvalidID = model.checkAnimalIDTypes();
					if( sInvalidID != null ) {
						MessageDialog.showMessage(null, "Civet Warning", "Animal ID " + sInvalidID
								+ " in certificate " + model.getCertificateNumber() + " is not valid for its type.\nChanged to 'OtherOfficialID'");
						sStdXml = model.getXMLString();
					}
					if( !isValidCVI(sStdXml) ) {
						FileUtils.writeTextFile(sStdXml, "FailedTransform" + fData.getName());
						throw new SourceFileException( "Failed to convert Conveyance Source\n"
								+ super.sFileName + "\nFix manually and try again");
					}
					byte pdfBytes[] = getPDFBytes();
					model.setOrUpdatePDFAttachment(pdfBytes, fSource.getName());
				}
				else {
					logger.error("Cannot find data file " + sDataPath);
				}
			} catch( Exception e ) {
				logger.error("Failed to read data file " + sDataPath);
			}
		}
		return model;
	}
	
	private boolean isValidCVI( String sXml ) {
		boolean bRet = true;
		String sSchemaPath = CivetConfig.getSchemaFile();
		if( sSchemaPath != null && sSchemaPath.trim().length() > 0 ) {
			Validator v = new Validator(sSchemaPath);
			if( v != null ) {
				if( !v.isValidXMLString(sXml) ) {
					MessageDialog.showMessage(null, "Civet: Error", "Failed translation of XFA data\n"
							+ v.getLastError() 
							+ "\nFix manually and try again" );
					logger.error("Civet: Error Failed translation of AgView AddressBlock\n");
					bRet = false;
				}
			}
		}
		return bRet;
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
	public boolean isXFA() {
		return false;
	}
	
	@Override
	public boolean isDataFile() {
		return true;
	}

	@Override
	public boolean canSplit() {
		// Never split XFA PDF
		return false;
	}
	
	private static String toStdXMLString( String sAcrobatXml ) {
		String sRet = null;
		String sXSLT = CivetConfig.getConveyanceXSLTFile();
		try {
			File fTransform = new File(sXSLT);
			InputStream isXLT = new FileInputStream(fTransform);
//			InputStream isXLT = getClass().getResourceAsStream("../res/Conveyance_to_Standard2.xsl");
		    StringReader sourceReader = new StringReader( sAcrobatXml );
		    ByteArrayOutputStream baosDest = new ByteArrayOutputStream();
			TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer;
            transformer = tFactory.newTransformer(new StreamSource(isXLT));
				transformer.transform(new StreamSource(sourceReader),
						new StreamResult(baosDest));
				sRet = new String( baosDest.toByteArray(), "UTF-8" );
			} catch ( TransformerException e) {
				logger.error("Failed to transform XML with XSLT: " + sXSLT, e);
			} catch (UnsupportedEncodingException e) {
				logger.error("Should not see this unsupported encoding", e);
			} catch (FileNotFoundException e) {
				logger.error("Could not find transform file " + sXSLT, e);
			}
 		return sRet;
	}

	@Override
	public String getSystem() {
		return "Conveyance Document";
	}

}
