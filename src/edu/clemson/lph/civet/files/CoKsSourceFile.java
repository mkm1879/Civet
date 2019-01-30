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

import javax.xml.parsers.DocumentBuilder;
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

import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.xml.SafeDocBuilder;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.pdfgen.PDFUtils;
import edu.clemson.lph.pdfgen.PDFViewer;
import edu.clemson.lph.utils.FileUtils;

/**
 * 
 */
public class CoKsSourceFile extends SourceFile {
	static {
		// BasicConfigurator replaced with PropertyConfigurator.
	     PropertyConfigurator.configure("CivetConfig.txt");
	     logger.setLevel(Level.INFO);
	}
	
	public CoKsSourceFile( File fFile, PDFViewer viewer ) throws SourceFileException {
		super(fFile, viewer);
		type = Types.CO_KS_PDF;
		if( fSource == null || !fSource.exists() ) {
			throw new SourceFileException("File " + sFilePath + " does not exist");
		}
		try {
			pdfBytes = FileUtils.readBinaryFile(fFile);
		} catch (Exception e) {
			throw new SourceFileException(e);
		}
		String sAcrobatXML = toAcrobatXMLString();
		String sStdXML = toStdXMLString( sAcrobatXML);
		
	FileUtils.writeTextFile(sStdXML, "Transform.xml");	
		model = new StdeCviXmlModel(sStdXML);
		model.setPDFAttachment(pdfBytes, fSource.getName());
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
		String sName = fFile.getName();
		if( sName.toLowerCase().endsWith(".pdf") ) {
			if( fFile != null && fFile.exists() && fFile.isFile() ) {
				String sTop;
				try {
					sTop = FileUtils.readTextFile( fFile, 4 );
					int iLoc = sTop.indexOf("<</Filter/FlateDecode");
					int iLoc2 = sTop.indexOf("PDF-1.7");
					if( iLoc >= 0 && iLoc2 >= 0 )
						bRet = true;
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
					String sStdXml = toStdXMLString( sAcrobatXml );
					model = new StdeCviXmlModel(sStdXml);
					byte pdfBytes[] = getPDFBytes();
					model.setPDFAttachment(pdfBytes, fSource.getName());
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
	
	private String nodeToString(Node node, boolean bOmitDeclaration) {
		String sOmit = bOmitDeclaration ? "yes" : "no";
		StringWriter sw = new StringWriter();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, sOmit);
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException te) {
			logger.error("nodeToString Transformer Exception", te);
		}
		return sw.toString();
	}

	private String toStdXMLString( String sAcrobatXml ) {
		String sRet = null;
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
	
}
