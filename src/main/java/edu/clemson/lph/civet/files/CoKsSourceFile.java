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
import java.nio.charset.StandardCharsets;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;



import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
public class CoKsSourceFile extends SourceFile {
	
	public CoKsSourceFile( File fFile ) throws SourceFileException {
		super(fFile);
		type = Types.CO_KS_PDF;
		if( fSource == null || !fSource.exists() ) {
			throw new SourceFileException("File " + sFilePath + " does not exist");
		}
		try {
			pdfBytes = FileUtils.readBinaryFile(fFile);
			iTextPdfReader = new PdfReader(pdfBytes);
		} catch (Exception e) {
			throw new SourceFileException(e);
		}
		String sAcrobatXML = toAcrobatXMLString();
		byte[] baXml = toStdXMLBytes( sAcrobatXML);
		
		model = new StdeCviXmlModel(baXml);
		model.setOrUpdatePDFAttachment(pdfBytes, fSource.getName());
	}

	/**
	 * This is very much a hack but needed to QUICKLY estimate if this is XFA
	 * and therefore probably CO/KS eCVI.
	 * More deterministic would be to use PDFUtils.isXFA() but that reads 
	 * the whole file.
	 * @param sPath
	 * @return
	 */
	public static boolean isCoKs( File fFile ) {
		boolean bRet = false;
		if( fFile == null ) return false;
		String sName = fFile.getName();
		if( sName.toLowerCase().endsWith(".pdf") ) {
			if( fFile.exists() && fFile.isFile() ) {
				String sTop;
				try {
					sTop = FileUtils.readTextFile( fFile, 4 );
					int iLoc = sTop.indexOf("<</Filter/FlateDecode");
					int iLoc2 = sTop.indexOf("PDF-1.7");
					if( iLoc >= 0 && iLoc2 >= 0 ) {
						// is a fancy PDF but not sure it is XFA 
						byte pdfBytes[] = FileUtils.readBinaryFile(fFile);
						if( PDFUtils.isXFA(pdfBytes) ) {
							// is XFA but is it CO/KS
							Node nRoot = PDFUtils.getXFADataNode(pdfBytes);
							String xmlString = nodeToString( nRoot, true );
							int iLoc3 = xmlString.indexOf("eCVI");
							if( iLoc3 > 0 )
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
					String sAcrobatXml = toAcrobatXMLString();
					byte[] baXml = toStdXMLBytes( sAcrobatXml );
					model = new StdeCviXmlModel(baXml);
					String sInvalidID = model.checkAnimalIDTypes();
					if( sInvalidID != null ) {
						MessageDialog.showMessage(null, "Civet Warning", "Animal ID " + sInvalidID
								+ " in certificate " + model.getCertificateNumber() + " is not valid for its type.\nChanged to 'OtherOfficialID'");
						baXml = model.getXMLBytes();
					}
					if( !isValidCVI(baXml) ) {
						FileUtils.writeUTF8File(baXml, "FailedTransform" + fData.getName());
						throw new SourceFileException( "Failed to convert CO/KS Source\n"
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
	
	private boolean isValidCVI( byte[] baXml ) {
		boolean bRet = true;
		String sSchemaPath = CivetConfig.getSchemaFile();
		if( sSchemaPath != null && sSchemaPath.trim().length() > 0 ) {
			Validator v = new Validator(sSchemaPath);
			if( v != null ) {
				if( !v.isValidXMLBytes(baXml) ) {
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
		return true;
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
	
	
	/**
	 * Returns the XFA data as formatted by Acrobat's export XML with eCVI as the document element.
	 * Omits the xfa:dataset and xfa:data elements and puts definition of xfa namespace in 
	 * datagroup attributes that use it.
	 * @return XML String
	 */
	private String toAcrobatXMLString() {
		String sRet = null;
		String xmlString = null;
		Document doc = null; 
		Node xmlNode = PDFUtils.getXFADataNode(pdfBytes);
		if( xmlNode == null ) {
			logger.error("Could not extract CO/KS data node from XFA PDF");		
		}
		if( xmlString == null && xmlNode != null) {
			xmlString = nodeToString( xmlNode, true );
		}
		if( xmlString == null )
			return null;
		try {
			DocumentBuilder db = SafeDocBuilder.getSafeDocBuilder(); //DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			// Move namespace definition to each of the header nodes because we are losing the XFA document node later.
			String sStrip = xmlString.replaceAll(" xfa:dataNode=\"dataGroup\"", " xmlns:xfa=\"http://www.xfa.org/schema/xfa-data/1.0/\"\nxfa:dataNode=\"dataGroup\"");
			// Brute force remove any known problem characters.  Very hard to know how they show up here from Adobe.
			sStrip = sStrip.replaceAll("&amp;#10;", "?");
			sStrip = sStrip.replaceAll("&amp;#13;", "?");
			sStrip = sStrip.replaceAll("&amp;#xa;", "?");
			sStrip = sStrip.replaceAll("&amp;#xA;", "?");
			sStrip = sStrip.replaceAll("&amp;#xd;", "?");
			sStrip = sStrip.replaceAll("&amp;#xD;", "?");
			sStrip = sStrip.replaceAll("&amp;#9;", "?");
			sStrip = sStrip.replaceAll("&gt;", "?");
			sStrip = sStrip.replaceAll("&lt;", "?");
			System.out.println(sStrip);
			is.setCharacterStream(new StringReader(sStrip));
			doc = db.parse(is);
			doc.setXmlStandalone(true);
			NodeList nl = doc.getElementsByTagName("eCVI");
			if( nl.getLength() == 1 ) {
				Node nEcvi = nl.item(0);
				sRet = nodeToString(nEcvi, false);
			}
		} catch (SAXException e) {
			logger.error("Failed to parse XML\n" + xmlString, e);
		} catch (IOException e) {
			logger.error("Failed to read XML\n" + xmlString, e);
//		} catch (ParserConfigurationException e) {
//			logger.error("Failed to configure XML parser", e);
		}
		return sRet;
	}
	
	private static String nodeToString(Node node, boolean bOmitDeclaration) {
		byte[] baXml = nodeToBytes( node, bOmitDeclaration );
		return new String( baXml, StandardCharsets.UTF_8);
	}
	
	private static byte[] nodeToBytes(Node node, boolean bOmitDeclaration) {
		String sOmit = bOmitDeclaration ? "yes" : "no";
		ByteArrayOutputStream bast = new ByteArrayOutputStream();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, sOmit);
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.transform(new DOMSource(node), new StreamResult(bast));
		} catch (TransformerException te) {
			logger.error("nodeToString Transformer Exception", te);
		}
		return bast.toByteArray();
	}

	private static byte[] toStdXMLBytes( String sAcrobatXml ) {
		byte[] baRet = null;
		String sXSLT = CivetConfig.getCoKsXSLTFile();
		try {
			File fTransform = new File(sXSLT);
			InputStream isXLT = new FileInputStream(fTransform);
//			InputStream isXLT = getClass().getResourceAsStream("../res/CO_KS_eCVI_to_Standard2.xsl");
		    StringReader sourceReader = new StringReader( sAcrobatXml );
		    ByteArrayOutputStream baosDest = new ByteArrayOutputStream();
			TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer;
            transformer = tFactory.newTransformer(new StreamSource(isXLT));
				transformer.transform(new StreamSource(sourceReader),
						new StreamResult(baosDest));
				baRet = baosDest.toByteArray();
			} catch ( TransformerException e) {
				logger.error("Failed to transform XML with XSLT: " + sXSLT, e);
			} catch (FileNotFoundException e) {
				logger.error("Could not find transform file " + sXSLT, e);
			}
 		return baRet;
	}

	@Override
	public String getSystem() {
		return "CO/KS eCVI";
	}

}
