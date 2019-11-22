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
import java.io.IOException;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.utils.FileUtils;

/**
 *  This class encapsulates actions taking place in one selected file.
 *  This is mainly movement through the pages of a multi-page PDF.
 *  The magic is in the polymorphism of source.  The factory creates the 
 *  appropriate subclass of SourceFile and we expose its methods to 
 *  perform the type-appropriate behaviors while hiding the type details (mostly).
 */
public class OpenFile {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private SourceFile source = null;
	private ArrayList<Integer> aPagesInCurrent = null;
	private ArrayList<Integer> aPagesDone = null;

	private OpenFile() {
	}

	/**
	 * Remove if we don't end up with a use
	 * @throws SourceFileException 
	 * 
	 */
	public OpenFile( String sFilePath ) throws SourceFileException {
		File fFile = new File( sFilePath );
		if( fFile != null && fFile.exists() && fFile.isFile() ) {
			try {
			// Factory method will populate with correct file type.
			// All variation in handling should be encapsulated in a SourceFile subclass.
			source = SourceFile.readSourceFile(fFile);
			// xmlModel may be split in case of multi-CVI PDF files 
			// so don't just call source.getDataModel() directly
			aPagesInCurrent = new ArrayList<Integer>();
			aPagesInCurrent.add(getCurrentPageNo());
			aPagesDone = new ArrayList<Integer>();
			} catch( Exception e ) {
				logger.error("Failed to read file " + sFilePath , e );
				throw new SourceFileException( "Failed to read file " + sFilePath );
			}
		} 
		else {
			throw new SourceFileException( "Attempt to read non-file " + sFilePath );
		}
	}
	/**
	 * Called from OpenFileList to create each file in the list.
	 * Constructor is executed inside OpenFilesThread so not safe.
	 * @throws SourceFileException 
	 * 
	 */
	public OpenFile( File fFile ) throws SourceFileException {
		if( fFile != null && fFile.exists() && fFile.isFile() ) {
			try {
			// Factory method will populate with correct file type.
			// All variation in handling should be encapsulated in a SourceFile subclass.
			source = SourceFile.readSourceFile(fFile);
			// xmlModel may be split in case of multi-CVI PDF files 
			// so don't just call source.getDataModel() directly
			aPagesInCurrent = new ArrayList<Integer>();
			aPagesInCurrent.add(getCurrentPageNo());
			aPagesDone = new ArrayList<Integer>();
		} catch( Exception e ) {
			logger.error("Failed to read file " + fFile.getName() , e );
			throw new SourceFileException( "Failed to read file " + fFile.getName() );
		}
		}
		else if( fFile != null ) {
			throw new SourceFileException( "Attempt to read non-file " + fFile.getName() );
		}
		else  {
			throw new SourceFileException( "Attempt to read null file" );
		}
	}
	
	/**
	 * This is a strangely overloaded method.  For all types other than PdfSource
	 * it simply returns the original since no split file thread issues.
	 * @return
	 */
	public OpenFile newOpenFileFromSource() {
		OpenFile fileOut = this;
		if( source.getType() == SourceFile.Types.PDF ) {
			fileOut = new OpenFile();
			fileOut.source = this.source.newSourceFromSameSourceFile();
			fileOut.aPagesInCurrent = new ArrayList<Integer>();
			fileOut.aPagesDone = new ArrayList<Integer>();
			fileOut.aPagesDone.addAll(this.aPagesDone);
		}
		return fileOut;
	}
	
	public boolean isCurrentPageComplete() {
		return isPageComplete(getCurrentPageNo());
	}
	
	public boolean isPageComplete( Integer iPage ) {
		boolean bRet = false;
		if( aPagesDone.contains(iPage) ) 
			bRet = true;
		return bRet;
	}
//	
//	public void viewFile() throws PdfException {
//		source.viewFile();;
//	}
	
	public SourceFile getSource() {
		return source;
	}
	
	public SourceFile.Types getType() {
		return source.type;
	}
	
	/**
	 * The one instance of PDFViewer is passed around by reference to 
	 * everything from the dialog to controller to here to the source.
	 * This is at risk of threading problems because it gets passed into
	 * the Open and Save threads.
	 * @return
	 */
//	public PDFViewer getViewer() {
//		return viewer;
//	}
	
	public byte[] getPDFBytes() {
		return source.getPDFBytes();
	}

	public StdeCviXmlModel getModel() {
		return source.getDataModel();
	}
	
	public Integer getCurrentPageNo() {
		return source.getCurrentPageNo();
	}
	
	public Integer getPageCount() {
		return source.getPageCount();
	}
	
	public boolean isXFA() {
		return source.isXFA();
	}
	
	public boolean isDataFile() {
		return source.isDataFile();
	}
	
	public java.util.Date getLastAccessDate() throws IOException {
		java.util.Date dRet = null;
		File fSource = getSource().fSource;
		dRet = FileUtils.getLastAccessedDate(fSource);
		return dRet;
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
	public void addPageToCurrent(int iPage) throws SourceFileException {
		if( source.canSplit() ) {
			source.addPageToCurrent(iPage);
			if( !aPagesInCurrent.contains(iPage) )
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
		aPagesInCurrent.add(getCurrentPageNo());
		for( Integer iPage : aPagesInCurrent ) {
			if( !aPagesDone.contains(iPage) )
				aPagesDone.add(iPage);
		}
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
	
	public boolean allPagesDone() {
		boolean bRet = true;
		if( getType() == SourceFile.Types.CO_KS_PDF || getType() == SourceFile.Types.AgView )
			bRet = true;  // Assume done whether opened or not
		else if( morePagesForward(true) || morePagesBack(true) )
			bRet = false;
		return bRet;
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
		for( Integer i = getCurrentPageNo() + 1; i <= source.getPageCount(); i++ ) {
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
