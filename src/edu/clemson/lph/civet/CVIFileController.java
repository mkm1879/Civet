package edu.clemson.lph.civet;
/*
Copyright 2014 Michael K Martin

This file is part of Civet.

Civet is free software: you can redistribute it and/or modify
it under the terms of the Lesser GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Civet is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the Lesser GNU General Public License
along with Civet.  If not, see <http://www.gnu.org/licenses/>.
*/
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jpedal.PdfDecoder;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;

import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.xml.StdeCviXml;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.pdfgen.PDFOpener;
import edu.clemson.lph.pdfgen.PDFUtils;

/**
 * This class handles keeping track of Files and Pages open in the CivetEditDialog.
 * Big task because there are a number of invisible "modes" that must be tracked.
 * @author mmarti5
 *
 */
public class CVIFileController {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	
	CivetEditDialog dlg;
	/** Data defining PDF and Index state **/
	/**name of current PDF file*/
	String currentFilePath = null;
	String currentFileName = null;
	private File currentFile = null;
	private File currentFiles[] = null;
	/** File and Page Numbers are 1 based.  PageNo used as is in pdfDecoder.decodePage()
	 *  iFileNo is one more than the array index!!!  **/
	private int iFileNo = 1;
	private int iFiles = 0;
	private int iPageNo = 1;
	private int iPages = 0;
	ArrayList<Integer> aPagesInCurrent = new ArrayList<Integer>();
	private HashMap<String,ArrayList<Integer>> mPagesComplete = new HashMap<String,ArrayList<Integer>>();
	private HashMap<String,Integer> mFilePages = new HashMap<String,Integer>();
	private byte rawPdfBytes[];

	private File fLastSavedFile = null;
	private boolean bLastXFA = false;

	private StdeCviXml stdXml = null;

	public CVIFileController(CivetEditDialog parent) {
		dlg = parent;
	}
	
	public void setCurrentFiles( File files[], boolean bViewOnly ) {
		currentFiles = files;
		if( currentFiles == null ) {
			logger.error("setCurrentFiles called with null", new Exception("Civet Error"));
			return;
		}
		iFileNo = 1;
		iFiles = currentFiles.length;
		dlg.setFile(iFileNo);
		dlg.setFiles( iFiles );
		if( currentFiles != null && currentFiles.length > 0 ) {
			currentFile = currentFiles[0];
			currentFilePath = currentFile.getAbsolutePath();
			currentFileName = currentFile.getName();
			OpenFileThread thread = new OpenFileThread( dlg, currentFilePath );
			thread.setReadOnly(bViewOnly);
			thread.start();
		}
	}
	
	public void setCurrentPdfBytes( byte bytes[] ) { rawPdfBytes = bytes; }
	public byte[] getCurrentPdfBytes() { return rawPdfBytes; }
	
	public File getCurrentFile() { return currentFile; }
	
	public String getCurrentFilePath() { return currentFilePath; }
	public String getCurrentFileName() { return currentFileName; }
	
	public int getCurrentFileNo() {	return iFileNo;	}
	
	public int getNumberOfFiles() {	return currentFiles.length;	}
	
	public int getCurrentPageNo() {	return iPageNo;	}
	
	public void setNumberOfPages( int iPages ) { this.iPages = iPages; }
	public int getNumberOfPages() {	return iPages; }
	
	/**
	 * Get an array of complete files from the list of currentFiles
	 * @return File[]
	 */
	public File[] getCompleteFiles() {
		ArrayList<File> completeFiles = new ArrayList<File>();
		File aOut[] = new File[0];
		for( File fThis : currentFiles ) {
			if( isFileComplete( fThis ) ) {
				completeFiles.add(fThis);
			}
		}
		return (File[])completeFiles.toArray(aOut);
	}
	
	/**
	 * Essentially answers the question whether every page in File fFile is 
	 * included in the array of pages marked complete in the mPagesComplete map.
	 * @param fFile
	 * @return
	 */
	public boolean isFileComplete( File fFile ) {
		boolean bRet = true;
		String sFilePath = fFile.getAbsolutePath();
		Integer iPagesInFile = mFilePages.get(sFilePath);
		// NOT an error.  iPagesInFile is set when file opened.  If this is null, it means the file
		// was never opened so it can never be complete.
		if( iPagesInFile == null  ) {
			return false;
		}
		ArrayList<Integer> aPagesComplete = mPagesComplete.get(sFilePath);
		if( aPagesComplete == null ) {
			return false;
		}
		for( int i = 1; i <= iPagesInFile; i++ ) {
			if( !aPagesComplete.contains(i) ) {
				bRet = false;
				break;
			}
		}
		return bRet;
	}

	/**
	 * Move to specified page in currentFile
	 * @param iPage Both page and file numbers are 1 based indexes they are converted to zero based
	 * as needed inside each method.
	 */
	public void setPage( int iPage ) {
		if (iPage > 0 && iPage <= iPages ) {
			iPageNo = iPage;
			try {
				dlg.getPdfDecoder().decodePage(iPageNo);
				dlg.updatePdfDisplay();
				dlg.setupForm(currentFileName, iPageNo, iPages, iFileNo, iFiles, isPageComplete(iPageNo));
			}
			catch (Exception e1) {
				logger.error(e1.getMessage() + "\nError moving forward one page");
			}
		}
	}

	/**
	 * Are there pages forward, complete or not
	 * @return true of false
	 */
	public boolean morePagesForward() {
		return ( iPageNo < iPages );
	}

	/**
	 * Are there pages backward, complete or not
	 * @return true of false
	 */
	public boolean morePagesBack() {
		return ( iPageNo > 1 );
	}
	
	/**
	 * Are there files forward, complete or not
	 * @return true of false
	 */
	public boolean moreFilesForward() {
		return currentFiles != null && iFiles > 0 && iFileNo < iFiles;
	}

	/**
	 * Are there files backward, complete or not
	 * @return true of false
	 */
	public boolean moreFilesBack() {
		return currentFiles != null && currentFiles.length > 0 && iFileNo > 1;
	}

	/**
	 * Move backward one page
	 */
	public void pageBack() {
		// First Try Paging Back
		if( iPageNo > 1 ) {
			iPageNo--;
			try {
				dlg.getPdfDecoder().decodePage(iPageNo);
				dlg.updatePdfDisplay();
			}
			catch (Exception e1) {
				logger.error(e1.getMessage() + "\nError moving back one page");
				iPageNo++;
				// Now what?  Another try?
			}
		}
		else if( iFileNo > 1 ) {
				iFileNo--;
				currentFile = currentFiles[iFileNo-1];
				currentFilePath = currentFile.getAbsolutePath();
				currentFileName = currentFile.getName();
				// Would have a race condition if we didn't perform this set BEFORE starting thread
				iPageNo = iPages;
				OpenFileThread thread = new OpenFileThread( dlg, currentFilePath );
				thread.start();	
		}
		dlg.setupForm(currentFileName, iPageNo, iPages, iFileNo, iFiles, isPageComplete(iPageNo));
	}

	/**
	 * Move forward one page
	 */
	public void pageForward() {
		if( iPageNo < iPages ) {
			iPageNo++;
			try {
				dlg.getPdfDecoder().decodePage(iPageNo);
				dlg.updatePdfDisplay();
				dlg.setupForm(currentFileName, iPageNo, iPages, iFileNo, iFiles, isPageComplete(iPageNo));
			}
			catch (Exception e1) {
				logger.error(e1.getMessage() + "\nError moving forward one page");
				iPageNo--;
				// Now what?  Another try?
			}
		}
		else if( iFileNo < currentFiles.length - 1 ) {
				iFileNo++;
				currentFile = currentFiles[iFileNo-1];
				currentFilePath = currentFile.getAbsolutePath();
				currentFileName = currentFile.getName();
				// Would have a race condition if we didn't perform this set BEFORE starting thread
				iPageNo = 1;
				OpenFileThread thread = new OpenFileThread( dlg, currentFile );
				thread.start();	
		}
	}
	
	/**
	 * Move back one file
	 */
	public void fileBackward() {	
		if( iFileNo > 1 ) {
				iFileNo--;
				currentFile = currentFiles[iFileNo-1];
				currentFilePath = currentFile.getAbsolutePath();
				currentFileName = currentFile.getName();
				iPageNo = 1;
				OpenFileThread thread = new OpenFileThread( dlg, currentFile );
				thread.start();	
		}
	}
	
	/**
	 * Move forward one file
	 */
	public void fileForward() {
		if( iFileNo < iFiles ) {
			iFileNo++;
			currentFile = currentFiles[iFileNo - 1];
			currentFilePath = currentFile.getAbsolutePath();
			currentFileName = currentFile.getName();
			iPageNo = 1;
			OpenFileThread thread = new OpenFileThread( dlg, currentFilePath );
			thread.start();	
		}
	}

	/**
	 * Are there any pages ahead of this one either in this file or a later file
	 * that have not yet been marked complete?
	 * @return true or false
	 */
	public boolean moreIncompleteForward() {
		int iNextPage = iPageNo + 1;
		File fThis;
		if( currentFiles == null ) return false;
		for( int iFile = iFileNo; iFile <= iFiles; iFile++ ) {
			fThis = currentFiles[iFile - 1];
			ArrayList<Integer> aPagesComplete = mPagesComplete.get(fThis.getAbsolutePath());
			if( aPagesComplete == null )
				return true;  // This file has no pages complete
			else {
				Integer iPagesInFile = mFilePages.get(fThis.getAbsolutePath());
				if( iPagesInFile == null ) 
					return true;  // File has not been read so not complete.
				for( int iPage = iNextPage; iPage <= iPagesInFile; iPage++ ) {
					if( !aPagesComplete.contains(iPage) )
						return true;
				}
			}
			iNextPage = 1;
		}
		return false;
	}

	/**
	 * Are there any pages behind of this one either in this file or a later file
	 * that have not yet been marked complete?
	 * @return true or false
	 */
	public boolean moreIncompleteBack() {
		if( currentFiles == null ) return false;
		File fThis;
		for( int iFile = iFileNo; iFile > 0; iFile-- ) {
			fThis = currentFiles[iFile - 1];
			ArrayList<Integer> aPagesComplete = mPagesComplete.get(fThis.getAbsolutePath());
			if( aPagesComplete == null )
				return true;  // This file has no pages complete
			else {
				Integer iPagesInFile = mFilePages.get(fThis.getAbsolutePath());
				if( iPagesInFile == null ) 
					return true;  // File has not been read so not complete. Should not happen backwards.
				for( int iPage = iPagesInFile; iPage > 0; iPage-- ) {
					if( !aPagesComplete.contains(iPage) )
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Move to the last page that is not marked complete either in this file or a previous one.
	 * This is only called if we had skipped a page somewhere and have hit the end so that
	 * we need to move back to find incomplete pages.
	 * Complexity here comes in needing to start at the END of previous files and move
	 * backward looking for incomplete pages.  Would the user even care?  
	 */
	public void moveToPreviousIncompletePage() {
		Integer iNextPage = iPageNo - 1;  // Start with PREVIOUS page
		// For every file from this one back...
		for( int iThisFile = iFileNo; iThisFile > 0; iThisFile-- ) {
			// Get information on the current file
			ArrayList<Integer> aPagesComplete = mPagesComplete.get(currentFiles[iThisFile-1].getAbsolutePath());
			Integer iPagesInFile = mFilePages.get(currentFiles[iThisFile-1].getAbsolutePath());
			if( iPagesInFile == null ) {
				logger.error(new Exception("Reached pageIncompleteBack (File with no page length recorded"));
				iPagesInFile = 1;
			}
			// Use the last page in the file unless we are on the current file
			if( iNextPage == null )
				iNextPage = iPagesInFile - 1;
			// For every page from the previous one back...
			for( int iThisPage = iNextPage; iThisPage >= 0; iThisPage-- ) {
				if( aPagesComplete == null || !aPagesComplete.contains(iThisPage) ) {
					// Found an unsaved page
					iPageNo = iThisPage;
					if( iFileNo != iThisFile ) {
						iFileNo = iThisFile;
						currentFile = currentFiles[iFileNo-1];
						currentFilePath = currentFile.getAbsolutePath();
						currentFileName = currentFile.getName();
						OpenFileThread thread = new OpenFileThread( dlg, currentFile );
						thread.start();	
						// Successfully tried to open file
						return;
					}
					else {
						try {
							dlg.getPdfDecoder().decodePage(iPageNo);
							dlg.updatePdfDisplay();
							dlg.setupForm(currentFileName, iPageNo, iPages, iFileNo, iFiles, isPageComplete(iPageNo));
							// Successfully read page
							return;
						}
						catch (Exception e1) {
							logger.error(e1.getMessage() + "\nError moving forward one pageIncomplete");
						}						
					}
				}
			}
			iNextPage = null; // reset so we use the last
		}
		logger.error(new Exception("Called pageIncompleteBack() but found none"));
	}

	/**
	 * Move to the next page that is not marked complete either in this file or a later one.
	 * This is the most used movement, called after each page is saved.
	 */
	void moveToNextIncompletePage() {
		Integer iNextPage = iPageNo + 1;  // Start with NEXT page
		// For every file from this one back...
		for( int iThisFile = iFileNo; iThisFile <= currentFiles.length; iThisFile++ ) {
			// Get information on the current file
			ArrayList<Integer> aPagesComplete = mPagesComplete.get(currentFiles[iThisFile-1].getAbsolutePath());
			Integer iPagesInFile = mFilePages.get(currentFiles[iThisFile-1].getAbsolutePath());
			if( iPagesInFile == null ) {
				// Not an error just the first time we've touched this file so won't need to look past first.
				// just don't trust this count for anything beyond picking the page.
				iPagesInFile = 1;
			}
			// Use the first page in the file unless we are on the current file
			if( iNextPage == null )
				iNextPage = 1;
			// For every page from the next one on ...
			for( int iThisPage = iNextPage; iThisPage <= iPagesInFile; iThisPage++ ) {
				if( aPagesComplete == null || !aPagesComplete.contains(iThisPage) ) {
					// Found an unsaved page
					iPageNo = iThisPage;
					if( iFileNo != iThisFile ) {
						iFileNo = iThisFile;
						currentFile = currentFiles[iFileNo-1];
						currentFilePath = currentFile.getAbsolutePath();
						currentFileName = currentFile.getName();
						OpenFileThread thread = new OpenFileThread( dlg, currentFile );
						thread.start();	
						// Successfully tried to open file
						return;
					}
					else {
						try {
							dlg.getPdfDecoder().decodePage(iPageNo);
							dlg.updatePdfDisplay();
							dlg.setupForm(currentFileName, iPageNo, iPages, iFileNo, iFiles, isPageComplete(iPageNo));
							// Successfully read page
							return;
						}
						catch (Exception e1) {
							logger.error(e1.getMessage() + "\nError moving forward one pageIncomplete");
						}						
					}
				}
			}
			iNextPage = null; // reset so we use the last
		}
		logger.error(new Exception("Called pageIncompleteForward() but found none"));
	}
	
	/**
	 * Used to setup appropriate buttons, etc., 
	 * This should be the logic that says whether we should even attempt to cut a file into pages
	 * or navigated page to page.
	 * This combined with !isXFADocument() tells if the file can be cut into multiple CVIs.
	 * @return true of false.
	 */
	public boolean isPageable() {
		return dlg.getPdfDecoder() != null && dlg.getPdfDecoder().isOpen() 
				&& currentFilePath.toLowerCase().endsWith(".pdf");
	}
	
	/**
	 * Used internally to check specific page agains mPagesComplete
	 * @param iPageNumber Both page and file numbers are 1 based indexes they are converted to zero based
	 * as needed inside each method.
	 * @return
	 */
	private boolean isPageComplete(Integer iPageNumber) {
		ArrayList<Integer> aPagesComplete = mPagesComplete.get(currentFilePath);
		if( aPagesComplete == null )
			return false;
		else
			return aPagesComplete.contains(iPageNumber);
	}
	
	/**
	 * Set the specified page complete.
	 * @param iPageNumber Both page and file numbers are 1 based indexes they are converted to zero based
	 * as needed inside each method.
	 */
	public void setPageComplete( Integer iPageNumber ) {
		ArrayList<Integer> aPagesComplete = mPagesComplete.get(currentFilePath);
		if( aPagesComplete == null ) {
			logger.error( "null aPagesComplete in setPageComplete for file (" + currentFilePath 
					+ ") Should have been set in setupFile", new Exception("Null aPagesComplete"));
			aPagesComplete = new ArrayList<Integer>();
			mPagesComplete.put(currentFilePath, aPagesComplete);
		}
		if( !aPagesComplete.contains( iPageNumber ) ) {
				aPagesComplete.add(iPageNumber);
		}
	}
	
	/**
	 * Add the current page to the current CVI
	 */
	public void addCurrentPage() {
		if( !aPagesInCurrent.contains(iPageNo) )
			aPagesInCurrent.add(iPageNo);  
	}
	
	/**
	 * Clear the current pages list
	 */
	public void clearCurrentPages() {
		aPagesInCurrent.clear();
	}

	/**
	 * @return if this is an XFA PDF and we can read it with JPedalXFA, return true, else false.
	 */
	public boolean isXFADocument() {
		boolean bRet = false;
		if( currentFileName.toLowerCase().endsWith(".pdf") ) {
			try {
				bRet = PDFUtils.isXFA(rawPdfBytes);
			} catch( Exception e ) {
				bRet = false;
			}
		}
		return bRet;
	}

	/**
	 * Get the appropriate byte array to include in standard XML.  This will be read from file for Images or XFAPDF files
	 * and extracted pages from other PDF files.
	 * @return byte array or null if not available (why not?)
	 */
	public byte[] getPDFBytesFromCurrent() {
		byte[] pdfBytes = null;
		if( isPageable() && !isXFADocument() ) {
			pdfBytes = extractPagesToNewPDF();
		}
		else {
			long len = currentFile.length();
			FileInputStream r = null;
			try {
				r = new FileInputStream( currentFile );
				pdfBytes = new byte[(int)len];
				int iRead = r.read(pdfBytes);
				if( iRead != len ) {
					throw new IOException( "Array length "+ iRead + " does not match file length " + len);
				}
			} catch (IOException e) {
				MessageDialog.showMessage( dlg, "Civet: Error",
						"Error Reading File " + currentFile.getAbsolutePath() )	;
			} finally {
				try {
					if( r != null) r.close();
				} catch (IOException e) {
					logger.error(e);
				}				
			}
		}
		return pdfBytes;
	}
	
	/**
	 * From the current pdfDecoder, extract the page(s) in aPagesInCurrent to a new pdfData buffer as output stream.
	 * @param aPages int[]
	 * @return byte[]
	 */
	public byte[] extractPagesToNewPDF() {
		ByteArrayOutputStream baOut = new ByteArrayOutputStream();
		try {
			byte[] pdfDataIn = rawPdfBytes;
			PdfReader reader = new PdfReader(pdfDataIn);
			com.itextpdf.text.Document document = new com.itextpdf.text.Document();
			PdfCopy writer = new PdfCopy(document, baOut);
			document.open();
			for( Integer iPage : aPagesInCurrent ) {
				PdfImportedPage pip = writer.getImportedPage(reader, iPage.intValue() );
				writer.addPage(pip);
			}
			document.close();
		} catch( IOException ioe ) {
			logger.info("IO error extracting pages to byte array", ioe);
			// This is a bug if the exception happens just returning the original pdf
			return rawPdfBytes;
		} catch( DocumentException de ) {
			logger.info(de.getMessage() + "\nDocument error extracting pages to byte array");
			// This is a bug if the exception happens just returning the original pdf
			return rawPdfBytes;
		}
		return baOut.toByteArray();
	}// End decode pages to new PDF

	/**
	 * Set all pages in the list of current pages as complete.
	 */
	public void setCurrentPagesComplete() {
		if( isXFADocument() ) {
			bLastXFA = true;
			Integer iPagesInFile = mFilePages.get(currentFilePath);
			if( iPagesInFile == null  ) {
				logger.error("Null iPagesInFile in setCurrentPagesComplete(" + currentFileName + ")", new Exception("Null iPages"));
				return;
			}
			for( int i = 1; i <= iPagesInFile; i++ )
				setPageComplete( i );
		}
		else {
			for( Integer iPage : aPagesInCurrent )
				setPageComplete( iPage );
		}
	}

	/**
	 * Callback from open file thread.
	 */
	public void setupFile() {
		PdfDecoder pdfDecoder = dlg.getPdfDecoder();
		try {
			pdfDecoder.decodePage(iPageNo);
			pdfDecoder.setPageParameters(dlg.getScale(),iPageNo,dlg.getRotation()); //values scaling (1=100%). page number
			int iPagesInFile = pdfDecoder.getPageCount();
			setNumberOfPages( iPagesInFile );
			// Pages in this file
			mFilePages.put(currentFilePath, iPagesInFile);
			// None of them complete unless reopened. But every open file will have a valid array object
			// in mPagesComplete keyed by its file path.
			ArrayList<Integer> aPagesComplete = mPagesComplete.get(currentFilePath);
			if( aPagesComplete == null )
				mPagesComplete.put(currentFilePath, new ArrayList<Integer>());
			aPagesInCurrent.clear();
			if( isXFADocument() ) {
				if( !CivetConfig.isJPedalXFA() && CivetConfig.isAutoOpenPdf() ) {
					PDFOpener opener = new PDFOpener(dlg);
					opener.openPDFContentInAcrobat(getCurrentPdfBytes());
				}
				dlg.setRotation(0);  // Always rotate 180 (actually 0) since we know they are right that way
				dlg.populateFromPDF();
			}
			else {
				dlg.setRotation( CivetConfig.getRotation() );
			}
			dlg.setupForm(currentFileName, iPageNo, iPagesInFile, iFileNo, iFiles, isPageComplete(iPageNo));
			dlg.setVisible(true);
			dlg.updatePdfDisplay();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
	}

	/**
	 * One of a series of methods that allow us to revisit the last file saved 
	 * These are used to add pages, edit last, etc.
	 * @param fLastSavedFile
	 */
	public void setLastSavedFile(File fLastSavedFile) {
		this.fLastSavedFile = fLastSavedFile;
	}
	/**
	 * One of a series of methods that allow us to revisit the last file saved 
	 * These are used to add pages, edit last, etc.
	 * @return File we last saved
	 */
	public File getLastSavedFile() {
		return fLastSavedFile ;
	}
	/**
	 * One of a series of methods that allow us to revisit the last file saved 
	 * These are used to add pages, edit last, etc.
	 * Don't offer to add pages or edit last if there is none!
	 * @return true or false
	 */
	public boolean hasLastSaved() {
		return (fLastSavedFile != null);
	}
	/**
	 * One of a series of methods that allow us to revisit the last file saved 
	 * These are used to add pages, edit last, etc.
	 * We need to reopen XFA documents differently than others.
	 * @return true or false
	 */
	public boolean isLastSavedXFA() {
		return bLastXFA;
	}
	/**
	 * Assign an existing StdeCviXml object to the current file.
	 * Used when opening an existing Civet "binary" file.
	 * This needs to be much more robust to ensure consistent state even with 
	 * null currentFile, currentFiles[], etc.
	 * Never called except by CivetEditDialog.openStdXml() which is never called.
	 * @param xStd
	 */
	public void setStdXml(StdeCviXml xStd) {
		this.stdXml = xStd;
	}
	/**
	 * Get the StdeCviXml object for the current file.
	 * This is a key method used by save to extract the original attachment for resaving.
	 * @return
	 */
	public StdeCviXml getStdXml() {
		return stdXml;
	}


}// End class CVIFileController
