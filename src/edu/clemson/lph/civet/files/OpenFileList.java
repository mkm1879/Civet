/**
 * Copyright Dec 10, 2018 Michael K Martin
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
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.pdfgen.PDFViewer;

/**
 * This class represents the collection of files that have been selected to open in
 * the CivetDialog.  It handles the logic around navigating between files.
 */
public class OpenFileList {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private ArrayList<OpenFile> aOpenFiles = null;
	private ArrayList<OpenFile> aFilesComplete = null;
	private OpenFile oCurrent;
	private PDFViewer viewer;


	public OpenFileList(PDFViewer viewer) {
		this.viewer = viewer;
		aOpenFiles = new ArrayList<OpenFile>();
		aFilesComplete = new ArrayList<OpenFile>();		
	}


	/**
	 * @throws SourceFileException 
	 * @throws PdfException 
	 * 
	 */
	public OpenFileList( ArrayList<File> aFiles ) throws SourceFileException, PdfException {
		aOpenFiles = new ArrayList<OpenFile>();
		aFilesComplete = new ArrayList<OpenFile>();
		for( File f : aFiles ) {
			OpenFile openFile = new OpenFile(f, viewer);
			aOpenFiles.add(openFile);
		}
	}
	
	/**
	 * This method is called from OpenFilesThread for each file in the list.  
	 * During execution of that thread this entire object is not thread safe 
	 * and must be guarded.
	 * When chasing bugs, look here first 
	 * @param f File to open
	 * @throws SourceFileException 
	 * @throws PdfException 
	 * 
	 */
	public void openFile( File f ) throws SourceFileException, PdfException {
		OpenFile openFile = new OpenFile(f, viewer);
		aOpenFiles.add(openFile);
		oCurrent = aOpenFiles.get(0);	
	}
	
	/**
	 * Get the current OpenFile
	 * @return
	 */
	public OpenFile getCurrentFile() {
		return oCurrent;
	}
	
	public void replaceFile( OpenFile replaceFile, OpenFile withFile ) {
		int iIndex = aOpenFiles.indexOf(replaceFile);
		aOpenFiles.set(iIndex, withFile);
		aFilesComplete.remove(replaceFile);
		if( oCurrent == replaceFile ) {  // Yes I mean to test identity not equality.
			oCurrent = withFile;
		}
	}
	
	/**
	 * Convert the index of the currently open file to 1 based index
	 * @return current file 1-based index
	 */
	public Integer getCurrentFileNo() {
		Integer iCurrent = aOpenFiles.indexOf(oCurrent) + 1; // Count from 1 index from 0
		return iCurrent;
	}
	
	/**
	 * Number of files is already 1 based
	 * @return number of files
	 */
	public Integer getFileCount() {
		Integer iCount = aOpenFiles.size();
		return iCount;
	}
	
	/**
	 * Use to navigate to the next available file forward or back; marking current on complete
	 * See navigation section below for details and specific move functions.
	 * @param bIncompleteOnly boolean true to skip files marked complete
	 * @return Any not yet complete file
	 */
	public OpenFile nextFile(boolean bIncompleteOnly) {
		OpenFile oRet = null;
		oRet = nextFileForward(bIncompleteOnly);
		if( oRet == null )
			oRet = nextFileBack(bIncompleteOnly);
		aFilesComplete.add(oCurrent);
		oCurrent = oRet;
		return oRet;
	}
	
	/**
	 * We have to initialize somewhere.  This is used when the list is first created.
	 * @return
	 * @throws PdfException
	 */
	public OpenFile firstFile() throws PdfException {
		oCurrent = aOpenFiles.get(0);
		oCurrent.viewFile();
		return oCurrent;
	}
	
	public int moveCompleteFiles() {
		int iRet = 0;
    	// Destination for files 
    	File dirOut = new File(CivetConfig.getOutputDirPath());
    	iRet = 0;
    	String sDirIn = CivetConfig.getInputDirPath();
    	for( OpenFile fCurrent : aFilesComplete ) {
    		if( fCurrent.getSource().fSource.getAbsolutePath().startsWith(sDirIn) ) {
    			if( fCurrent.getSource().moveToDirectory(dirOut) )
    				iRet++;
    		}
    	}
    	return iRet;
	}
	
	public void markFileComplete( OpenFile completeFile ) {
		if( aFilesComplete.contains(completeFile) ) 
			logger.error("Repeat mark complete of file: " + completeFile.getSource().getFileName() );
		else
			aFilesComplete.add(completeFile);
	}

	
	
	/*
	 * File navigation.  File numbers are ONE based
	 */
	
	/**
	 * Look for the next file forward.
	 * Do not yet change state.
	 * @param bIncompleteOnly boolean true to skip files marked complete
	 * @return boolean true if more file(s) available forward 
	 */
	public boolean moreFilesForward(boolean bIncompleteOnly) {
		boolean bRet = false;
		Integer iCurrent = aOpenFiles.indexOf(oCurrent); // 0 indexed
		for( int i = iCurrent + 1; i < aOpenFiles.size(); i++ ) {
			OpenFile oNext = aOpenFiles.get(i); // 0 indexed
			if(  !bIncompleteOnly || !aFilesComplete.contains(oNext) ) {
				bRet = true;
				break;
			}
		}
		return bRet;
	}
	
	/**
	 * Return the next file forward.
	 * Do not yet change state.
	 * @param bIncompleteOnly boolean true to skip files marked complete
	 * @return OpenFile object 
	 */
	private OpenFile nextFileForward(boolean bIncompleteOnly) {
		OpenFile oRet = null;
		Integer iCurrent = aOpenFiles.indexOf(oCurrent);
		for( int i = iCurrent + 1; i <= aOpenFiles.size(); i++ ) { // 0 indexed
			OpenFile oNext = aOpenFiles.get(i); // 0 indexed
			if( !bIncompleteOnly || !aFilesComplete.contains(oNext) ) {
				oRet = oNext;
				break;
			}
		}
		if( oRet == null ) {
			System.err.println( iCurrent + " of " + aOpenFiles.size() + " files returned null in next");
		}
		return oRet;
	}

	/**
	 * Move oCurrent pointer to the next file back
	 * @throws PdfException 
	 * @param bIncompleteOnly boolean true to skip files marked complete
	 * @return OpenFile object 
	 */
	public OpenFile fileForward(boolean bIncompleteOnly) throws PdfException {
		oCurrent = nextFileForward(bIncompleteOnly);
		oCurrent.viewFile();
		return oCurrent;		
	}

	/**
	 * Look for the next file backward.
	 * Do not yet change state.
	 * @param bIncompleteOnly boolean true to skip files marked complete
	 * @return boolean true if more file(s) available forward 
	 */
	public boolean moreFilesBack(boolean bIncompleteOnly) {
		boolean bRet = false;
		Integer iCurrent = aOpenFiles.indexOf(oCurrent); // 0 indexed
		for( int i = iCurrent - 1; i >= 0; i-- ) {
			OpenFile oNext = aOpenFiles.get(i); // 0 indexed
			if(  !bIncompleteOnly || !aFilesComplete.contains(oNext) ) {
				bRet = true;
				break;
			}
		}
		return bRet;
	}
	
	/**
	 * Return the next file backward that is not yet completed.
	 * Do not yet change state
	 * @param bIncompleteOnly boolean true to skip files marked complete
	 * @return OpenFile object 
	 */
	private OpenFile nextFileBack(boolean bIncompleteOnly) {
		OpenFile oRet = null;
		Integer iCurrent = aOpenFiles.indexOf(oCurrent); // 0 indexed
		for( int i = iCurrent - 1; i >= 0; i-- ) {
			OpenFile oNext = aOpenFiles.get(i); // 0 indexed
			if( !bIncompleteOnly || !aFilesComplete.contains(oNext) ) {
				oRet = oNext;
				break;
			}
		}
		if( oRet == null ) {
			System.err.println( iCurrent + " of " + aOpenFiles.size() + " files returned null in back");
		}
		return oRet;
	}

	/**
	 * Move oCurrent pointer to the next file back
	 * @throws PdfException 
	 * @param bIncompleteOnly boolean true to skip files marked complete
	 * @return OpenFile object 
	 */
	public OpenFile fileBackward(boolean bIncompleteOnly) throws PdfException {
		oCurrent = nextFileBack(bIncompleteOnly);
		oCurrent.viewFile();
		return oCurrent;
	}

}
