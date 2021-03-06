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
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.itextpdf.text.pdf.PdfReader;

import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.utils.FileUtils;
import edu.clemson.lph.utils.Validator;

/**
 * 
 */
public class AgViewSourceFile extends SourceFile {
	
	public AgViewSourceFile( File fFile ) throws SourceFileException {
		super(fFile);
		type = Types.AgView;
		if( fSource == null || !fSource.exists() )
			logger.error("File " + sFilePath + " does not exist");
		sDataPath  = AgViewSourceFile.getDataFilePath(sFilePath); 
		fData = new File(sDataPath);
		if( fData == null || !fData.exists() || !fData.isFile() ) {
				logger.error("Cannot find data file " + sDataPath);
		}
		try {
			pdfBytes = FileUtils.readBinaryFile(fSource);
			iTextPdfReader = new PdfReader(pdfBytes);
			byte[] xmlBytes = FileUtils.readUTF8File(fData);
			/*
			String sStdXML = FileUtils.readTextFile(fData);
			// Remove any whitespace before the first < in the XML.  Usually a BOM.
			sStdXML = sStdXML.trim().replaceFirst("^([\\W]+)<","<");
			if( sStdXML.indexOf("AddressBlock") > 0 )
				sStdXML = AddressBlock2Address(sStdXML);
				*/
			model = new StdeCviXmlModel(xmlBytes);
			String sStdXML = null;
			String sInvalidID = model.checkAnimalIDTypes();
			sStdXML = model.getXMLString();
			if( sInvalidID != null ) {
				MessageDialog.showMessage(null, "Civet Warning", "Animal ID " + sInvalidID
						+ " in certificate " + model.getCertificateNumber() + " is not valid for its type.\nChanged to 'OtherOfficialID'");
			}
			if( !isValidCVI(xmlBytes) ) {
				FileUtils.writeTextFile(sStdXML, "FailedTransform" + fData.getName());
				throw new SourceFileException( "Failed to convert AgView Source\n"
						+ fFile.getName() + "\nFix manually and try again");
			}
			model.setOrUpdatePDFAttachment(getPDFBytes(), fSource.getName());
		} catch (Exception e) {
			throw new SourceFileException(e);
		}
	}
	
	private boolean isValidCVI( byte[] xmlBytes ) {
		boolean bRet = true;
		String sSchemaPath = CivetConfig.getSchemaFile();
		if( sSchemaPath != null && sSchemaPath.trim().length() > 0 ) {
			Validator v = new Validator(sSchemaPath);
			if( v != null ) {
				if( !v.isValidXMLBytes(xmlBytes) ) {
					MessageDialog.showMessage(null, "Civet: Error", "Failed translation of AgView AddressBlock\n"
							+ v.getLastError() 
							+ "\nFix manually and try again" );
					logger.error("Civet: Error Failed translation of AgView AddressBlock\n");
					bRet = false;
				}
			}
		}
		return bRet;
	}

	/**
	 * Look for a corresponding XML data file with a version 2 standard namespace.
	 * @param sPath
	 * @return
	 */
	public static boolean isAgView( File fFile ) {
		boolean bRet = false;
		String sData = AgViewSourceFile.getDataFilePath(fFile.getAbsolutePath());
		if( sData != null ) {
			try { 
				File fData = new File( sData );
				if( fData != null && fData.exists() && fData.isFile() ) {
					String sXML = FileUtils.readTextFile(fData);
					int iV2 = sXML.indexOf("xmlns=\"http://www.usaha.org/xmlns/ecvi2\"");
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
	public StdeCviXmlModel getDataModel() {
		if( model == null ) {
			try { 
				if( fData != null && fData.exists() && fData.isFile() ) {
					byte[] xmlBytes = FileUtils.readUTF8File(fData);
					model = new StdeCviXmlModel(xmlBytes);
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
	public boolean isDataFile() {
		return true;
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
	
	/*
	 * For now this is identical to mCvi but they could diverge
	 */
	private static String getDataFilePath( String sCVIPath ) {
		String sRet = null;
		int iLastDot = sCVIPath.lastIndexOf('.');
		sRet = sCVIPath.substring(0,iLastDot) + ".xml";  
		return sRet;
	}
	
	/**
	 * Note: AgView and MCvi use the same logic of PDF and Data file as will Civet going forward.
	 * I don't think it worth adding an intermediate class to avoid this duplication but that 
	 * might change.  This method is identical to MCvi.
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

	@Override
	public boolean canSplit() {
		// Never split XFA PDF
		return false;
	}

	@Override
	public String getSystem() {
		return "AGV";
	}
	
	public String AddressBlock2Address( String sXmlIn) {
		String sRet = null;
		String sXSLT = "AddressBlock2Address.xsl";
		try {
		    FileReader xsltReader = new FileReader( sXSLT );
		    StringReader sourceReader = new StringReader( sXmlIn );
		    ByteArrayOutputStream baosDest = new ByteArrayOutputStream();
			TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer;
				transformer = tFactory.newTransformer(new StreamSource(xsltReader));
				transformer.transform(new StreamSource(sourceReader),
						new StreamResult(baosDest));
				sRet = new String( baosDest.toByteArray(), "UTF-8" );
			} catch ( TransformerException e) {
				logger.error("Failed to transform XML with XSLT: " + sXSLT, e);
			} catch (UnsupportedEncodingException e) {
				logger.error("Should not see this unsupported encoding", e);
			} catch (FileNotFoundException e) {
				logger.error("Could not find XSLT: " + sXSLT, e);
			}
 		return sRet;
	}

}
