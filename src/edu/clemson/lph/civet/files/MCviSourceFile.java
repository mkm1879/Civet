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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.clemson.lph.civet.files.SourceFile.Types;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.xml.SafeDocBuilder;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.civet.xml.XMLDocHelper;
import edu.clemson.lph.pdfgen.PDFViewer;
import edu.clemson.lph.utils.FileUtils;
import edu.clemson.lph.utils.IDTypeGuesser;

/**
 * 
 */
public class MCviSourceFile extends SourceFile {
	
	public MCviSourceFile( File fFile, PDFViewer viewer ) throws SourceFileException {
		super(fFile, viewer);
		type = Types.mCVI;
		if( fSource == null || !fSource.exists() )
			logger.error("File " + sFilePath + " does not exist");
		sDataPath = getDataFilePath( sFilePath );
		fData = new File( sDataPath );
		try {
			pdfBytes = FileUtils.readBinaryFile(fSource);
			String sAcrobatXML = FileUtils.readTextFile(fData);
			String sStdXML = toStdXMLString( sAcrobatXML );
			model = new StdeCviXmlModel(sStdXML);
			model.setOrUpdatePDFAttachment(getPDFBytes(), fSource.getName());
		} catch (Exception e) {
			throw new SourceFileException(e);
		}
		
	}

	public static boolean isMCvi( File fFile ) {
		boolean bRet = false;
		String sData = MCviSourceFile.getDataFilePath(fFile.getAbsolutePath());
		if( sData != null ) {
			try { 
				File fData = new File( sData );
				if( fData != null && fData.exists() && fData.isFile() ) {
					String sXML = FileUtils.readTextFile(fData);
					int iV2 = sXML.indexOf("eCVI xmlns=\"\"");
					if( iV2 >= 0 )
						bRet = true;
				}
			} catch( Exception e ) {
				logger.error("Failed to read data file " + sData);
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
				if( fData != null && fData.exists() && fData.isFile() ) {
					String sAcrobatXml = FileUtils.readTextFile(fData);
					String sStdXml = toStdXMLString( sAcrobatXml );
					model = new StdeCviXmlModel(sStdXml);
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


	/**
	 * Note: AgView and MCvi use the same logic of PDF and Data file as will Civet going forward.
	 * I don't think it worth adding an intermediate class to avoid this duplication but that 
	 * might change.  This method is identical to AgView.
	 */	
	@Override
	public boolean moveToDirectory( File fDir ) {
		boolean bRet = super.moveToDirectory(fDir);
		File fNew = new File(fDir, fData.getName());
		if( fNew.exists() ) {
			logger.error(fNew.getName() + " already exists in " + fDir.getAbsolutePath() + "\n" +
						"Check that it really is a duplicate and manually delete.");
			String sOutPath = fNew.getAbsolutePath();
			sOutPath = FileUtils.incrementFileName(sOutPath);
			logger.error(fNew.getName() + " already exists in " + fDir.getAbsolutePath() + "\n" +
					"Saving as " + sOutPath);
			fNew = new File( sOutPath );
		}
		bRet = fData.renameTo(fNew);
		if (!bRet) {
			logger.error("Could not move " + fData.getAbsolutePath() + " to " + fNew.getAbsolutePath() );
		}
		return bRet;
	}

	
	/*
	 * For now this is identical to AgView but they could diverge
	 */
	private static String getDataFilePath( String sCVIPath ) {
		String sRet = null;
		int iLastDot = sCVIPath.lastIndexOf('.');
		sRet = sCVIPath.substring(0,iLastDot) + ".xml";  
		return sRet;
	}
	
	public String toStdXMLString( String sAcrobatXml ) {
		String sRet = null;
		String sXSLT = CivetConfig.getCoKsXSLTFile();
		try {
			InputStream isXLT = getClass().getResourceAsStream("../res/CO_KS_eCVI_to_Standard2.xsl");
		    StringReader sourceReader = new StringReader( sAcrobatXml );
		    ByteArrayOutputStream baosDest = new ByteArrayOutputStream();
			TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer;
            transformer = tFactory.newTransformer(new StreamSource(isXLT));
            transformer.transform(new StreamSource(sourceReader),
            		new StreamResult(baosDest));
            sRet = new String( baosDest.toByteArray(), "UTF-8" );
    		sRet = postProcessStdXML( sRet );
            sRet = postProcessv2(sRet);
		} catch ( TransformerException e) {
			logger.error("Failed to transform XML with XSLT: " + sXSLT, e);
		} catch (UnsupportedEncodingException e) {
			logger.error("Should not see this unsupported encoding", e);

		}
 		return sRet;
	}

	// Ugly stuff to deal with some transform problems.  Hide them here!
	// TODO modify to fit with new transform CO_KS_To_Version2.xslt
	private String postProcessStdXML( String sStdXml ) {
		Document doc = null;
		if( sStdXml != null ) {
			try {
				DocumentBuilder db = SafeDocBuilder.getSafeDocBuilder();  //DocumentBuilderFactory.newInstance().newDocumentBuilder();
				InputSource is = new InputSource();
				is.setCharacterStream(new StringReader(sStdXml));
				doc = db.parse(is);
				doc.setXmlStandalone(true);
			} catch (SAXException e) {
				logger.error("Failed to parse XML\n" + sStdXml, e);
				return null;
			} catch (IOException e) {
				logger.error("Failed to read XML\n" + sStdXml, e);
				return null;
			}
		}
		return postProcessStdXML( doc );
	}
	
	private String postProcessStdXML( Document doc ) {
		if( doc == null ) return null;
		XMLDocHelper helper = new XMLDocHelper( doc );
//		NodeList nlAnimalTags = helper.getNodeListByPath("//AnimalTag");
//		if( nlAnimalTags != null ) {
//			for( int i = 0; i < nlAnimalTags.getLength(); i++ ) {
//				Element eTag = (Element)nlAnimalTags.item(i);
//				String sTag = eTag.getAttribute("Number");
//				String sType = eTag.getAttribute("Type");
//				if( sType == null || sType.trim().length() == 0 || sType.equalsIgnoreCase("UN") ) {
//					sType = IDTypeGuesser.getTagType(sTag);
//					eTag.setAttribute("Type", sType);
//				}
//			}
//		}
		return helper.getXMLString();
	}

	@Override
	public boolean canSplit() {
		// Never split XFA PDF
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

}
