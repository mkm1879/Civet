/**
 * Copyright Dec 5, 2018 Michael K Martin
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
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.jpedal.exception.PdfException;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.pdfgen.PDFUtils;
import edu.clemson.lph.pdfgen.PDFViewer;

/**
 * 
 */
public class OpenFile {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private SourceFile source = null;
	private PDFViewer viewer = null;
	private StdeCviXmlModel xmlModel = null;
	private ArrayList<Integer> aPagesInCurrent = null;
	private ArrayList<Integer> aPagesDone = null;
	private boolean bXFA = false;

	/**
	 * @throws SourceFileException 
	 * @throws PdfException 
	 * 
	 */
	public OpenFile( String sFilePath, PDFViewer viewer ) throws SourceFileException {
		this.viewer = viewer;
		File fFile = new File( sFilePath );
		if( fFile != null && fFile.exists() && fFile.isFile() ) {
			// Factory method will populate with correct file type.
			// All variation in handling should be encapsulated in a SourceFile subclass.
			source = SourceFile.readSourceFile(fFile, viewer);
			// xmlModel may be split in case of multi-CVI PDF files 
			// so don't just call source.getDataModel() directly
			xmlModel = source.getDataModel();
			aPagesInCurrent = new ArrayList<Integer>();
			aPagesInCurrent.add(getCurrentPageNo());
			aPagesDone = new ArrayList<Integer>();
		}
		else {
			throw new SourceFileException( "Attempt to read non-file " + sFilePath );
		}
	}
	/**
	 * @throws SourceFileException 
	 * @throws PdfException 
	 * 
	 */
	public OpenFile( File fFile, PDFViewer viewer ) throws SourceFileException {
		this.viewer = viewer;
		if( fFile != null && fFile.exists() && fFile.isFile() ) {
			// Factory method will populate with correct file type.
			// All variation in handling should be encapsulated in a SourceFile subclass.
			source = SourceFile.readSourceFile(fFile, viewer);
			// xmlModel may be split in case of multi-CVI PDF files 
			// so don't just call source.getDataModel() directly
			xmlModel = source.getDataModel();
			aPagesInCurrent = new ArrayList<Integer>();
			aPagesInCurrent.add(getCurrentPageNo());
			aPagesDone = new ArrayList<Integer>();
		}
		else if( fFile != null ) {
			throw new SourceFileException( "Attempt to read non-file " + fFile.getName() );
		}
		else  {
			throw new SourceFileException( "Attempt to read null file" );
		}
	}
	
	public boolean isPageComplete( Integer iPage ) {
		boolean bRet = false;
		if( aPagesDone.contains(iPage) ) 
			bRet = true;
		return bRet;
	}
	
	public void viewFile() throws PdfException {
		source.viewFile();;
	}
	
	public SourceFile getSource() {
		return source;
	}
	
	public SourceFile.Types getType() {
		return source.type;
	}
	
	public PDFViewer getViewer() {
		return viewer;
	}
	
	public byte[] getPDFBytes() {
		return source.getPDFBytes();
	}

	public StdeCviXmlModel getModel() {
		return xmlModel;
	}
	
	public Integer getCurrentPageNo() {
		return source.getCurrentPageNo();
	}
	
	public Integer getPageCount() {
		return source.getPageCount();
	}
	
	public boolean isXFA() {
		return bXFA;
	}
	
	/**
	 * Preconditions:  source.iPage points to last page transcribed
	 * model has been updated with content of that page
	 * model pdf has had that page added
	 * aPagesInCurrent contains all pages that have been updated into model
	 * decoder displays the next page
	 * Postcondition:  model returned is ready to save to appropriate folder(s)
	 * source file now has new empty data model containing pdf content of the next page
	 * @return the model to save
	 * @throws SourceFileException
	 */
	public StdeCviXmlModel saveNext() throws SourceFileException {
		StdeCviXmlModel modelRet = null;
		if( source.canSplit() ) {
			aPagesDone.addAll(aPagesInCurrent);
			xmlModel = source.split();
//			Integer iPage = nextUnsavedPage();
//			source.setCurrentPage(iPage);
			aPagesInCurrent = new ArrayList<Integer>();
			aPagesInCurrent.add(getCurrentPageNo());
		}
		else {
			throw new SourceFileException("addPageToCurrent called on non-splittable source");
		}
		return modelRet;
	}
	
	public Integer nextUnsavedPage() {
		Integer iRet = null;
		iRet = nextPageForward(true);
		if( iRet == null ) 
			iRet = nextPageBack(true);
		return iRet;
	}
	
	/**
	 * Preconditions:  source.iPage points to last page transcribed
	 * model has been updated with content of that page
	 * aPagesInCurrent contains all pages that have been updated into model
	 * decoder displays the next page
	 * Postcondition:  Original model has all its original data content and the indicated
	 * source file pdf content appended to its pdf content.
	 * @param iPage
	 * @throws SourceFileException
	 */
	public void addPageToCurrent() throws SourceFileException {
		int iPage = getCurrentPageNo();
		if( source.canSplit() ) {
			source.addPageToCurrent(iPage);
			aPagesInCurrent.add(iPage);
		}
		else {
			throw new SourceFileException("addPageToCurrent called on non-splittable source");
		}
	}
	
	public ArrayList<Integer> getPagesInCurrent() {
		return aPagesInCurrent;
	}
	
	public ArrayList<Integer> getPagesDone() {
		return aPagesDone;
	}
	
	public void setCurrentPagesDone() {
		aPagesDone.addAll(aPagesInCurrent);
	}

	
	/*
	 * Page navigation 
	 */
	
	/**
	 * !!!! Page numbers are based on 1 not 0
	 * @param iPage Integer page number to move to.
	 */
	public void gotoPageNo(Integer iPage) throws SourceFileException {
		if( iPage != null && iPage >= 1 && iPage <= getPageCount() ) {
			source.gotoPageNo(iPage);
		}
		else throw new SourceFileException( "Page # " + iPage + " out of bounds 0 - " + getPageCount() );
	}

	/**
	 * Look for additional pages forward in the current file
	 * !!!! Page numbers are ONE based
	 * Any reference to PageNo, PageNumber, FileNo or FileNumber must be ONE-based numbering!
	 * @param bIncompleteOnly boolean true to skip pages marked complete
	 * @return true if more pages beyond the current one
	 */
	public boolean morePagesForward(boolean bIncompleteOnly) {
		boolean bRet = false;
		for( Integer i = getCurrentPageNo() + 1; i <= getPageCount(); i++ ) {
			if( !bIncompleteOnly || !aPagesDone.contains(i) ) { //&& !aPagesInCurrent.contains(i) ) {
				bRet = true;
				break;
			}
		}
		return bRet;
	}
	
	/**
	 * Return the next page number forward in the current file
	 * Do NOT change state
	 * !!!! Page numbers are ONE based
	 * Any reference to PageNo, PageNumber, FileNo or FileNumber must be ONE-based numbering!
	 * @param bIncompleteOnly boolean true to skip pages marked complete
	 * @return Integer page number (one based) 
	 */
	public Integer nextPageForward(boolean bIncompleteOnly) {
		Integer iRet = null;
		for( Integer i = getCurrentPageNo() + 1; i <= viewer.getPageCount(); i++ ) {
			if( !bIncompleteOnly || !aPagesDone.contains(i) ) { //&& !aPagesInCurrent.contains(i) ) {
				iRet = i;
				break;
			}
		}
		return iRet;
	}
	
	/**
	 * Return the next page number forward in the current file
	 * while moving to that page.
	 * This and nextPageForward mirror nextFileForward and fileForward from OpenFileList.
	 * !!!! Page numbers are ONE based
	 * Any reference to PageNo, PageNumber, FileNo or FileNumber must be ONE-based numbering!
	 * @param bIncompleteOnly boolean true to skip pages marked complete
	 * @return Integer page number (one based) 
	 * @throws SourcFileException from attempt to move to page.
	 */
	public Integer pageForward(boolean bIncompleteOnly) throws SourceFileException {
		Integer iPage = nextPageForward(bIncompleteOnly);
		if( iPage != null )
			gotoPageNo(iPage);
		else {
			java.awt.Toolkit.getDefaultToolkit().beep();
			logger.error("Attempt to move past last page");
		}
		return iPage;
	}
	
	/**
	 * Look for additional pages backward in the current file
	 * !!!! Page numbers are ONE based
	 * Any reference to PageNo, PageNumber, FileNo or FileNumber must be ONE-based numbering!
	 * @param bIncompleteOnly boolean true to skip pages marked complete
	 * @return true if more pages before the current one
	 */
	public boolean morePagesBack(boolean bIncompleteOnly) {
		boolean bRet = false;
		for( Integer i = getCurrentPageNo() - 1; i >= 1; i-- ) {  // Subtract 1 to decrement
			if( !bIncompleteOnly || !aPagesDone.contains(i) ) { //&& !aPagesInCurrent.contains(i) ) {
				bRet = true;
				break;
			}
		}
		return bRet;
	}
	
	/**
	 * Return the next page number backward in the current file
	 * Do NOT change state
	 * !!!! Page numbers are ONE based
	 * Any reference to PageNo, PageNumber, FileNo or FileNumber must be ONE-based numbering!
	 * @param bIncompleteOnly boolean true to skip pages marked complete
	 * @return Integer page number (one based) 
	 */
	public Integer nextPageBack(boolean bIncompleteOnly) {
		Integer iRet = null;
		for( Integer i = getCurrentPageNo() - 1; i >= 1; i-- ) { // Subtract 1 to get index
			if( !bIncompleteOnly || !aPagesDone.contains(i) ) { //&& !aPagesInCurrent.contains(i) ) {
				iRet = i;  // Convert back to 1 based page number
				break;
			}
		}
		return iRet;
	}

	/**
	 * Return the next page number backward in the current file
	 * while moving to that page.
	 * This and nextPageForward mirror nextFileForward and fileForward from OpenFileList.
	 * !!!! Page numbers are ONE based
	 * Any reference to PageNo, PageNumber, FileNo or FileNumber must be ONE-based numbering!
	 * @param bIncompleteOnly boolean true to skip pages marked complete
	 * @return Integer page number (one based) 
	 * @throws SourcFileException from attempt to move to page.
	 */
	public void pageBackward(boolean bIncompleteOnly) throws SourceFileException {
		Integer iPage = nextPageBack(bIncompleteOnly);
		if( iPage != null )
			gotoPageNo(iPage);
		else {
			java.awt.Toolkit.getDefaultToolkit().beep();
			logger.error("Attempt to move past last page");
		}
	}
	

}
