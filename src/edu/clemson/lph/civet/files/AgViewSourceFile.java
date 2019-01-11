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

import edu.clemson.lph.civet.files.SourceFile.Types;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.pdfgen.PDFViewer;
import edu.clemson.lph.utils.FileUtils;

/**
 * 
 */
public class AgViewSourceFile extends SourceFile {
	
	public AgViewSourceFile( File fFile, PDFViewer viewer ) throws SourceFileException {
		super(fFile, viewer);
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
			String sStdXML = FileUtils.readTextFile(fData);
			model = new StdeCviXmlModel(sStdXML);
			model.setPDFAttachment(getPDFBytes(), fSource.getName());
		} catch (Exception e) {
			throw new SourceFileException(e);
		}
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
					String sStdXml = FileUtils.readTextFile(fData);
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
	
	@Override
	public boolean moveToDirectory( File fDir ) {
		boolean bRet = super.moveToDirectory(fDir);
		File fNew = new File(fDir, fData.getName());
		if( fNew.exists() ) {
			logger.error(fNew.getName() + " already exists in " + fDir.getAbsolutePath() + " .\n" +
						"Check that it really is a duplicate and manually delete.");
			String sOutPath = fNew.getAbsolutePath();
			sOutPath = FileUtils.incrementFileName(sOutPath);
			fNew = new File( sOutPath );
		}
		boolean success = fData.renameTo(fNew);
		if (!success) {
			logger.error("Could not move " + getFilePath() + " to " + fNew.getAbsolutePath() );
			bRet = false;
		}
		return bRet;
	}
	

	@Override
	public boolean canSplit() {
		// Never split XFA PDF
		return false;
	}
}
