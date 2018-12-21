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
import org.apache.log4j.PropertyConfigurator;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.utils.FileUtils;

/**
 * 
 */
public class OpenFileList {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private ArrayList<OpenFile> aOpenFiles = null;
	private ArrayList<OpenFile> aFilesComplete = null;
	private OpenFile oCurrent;

	/**
	 * Cheap little unit test
	 * @param args
	 */
	public static void main(String args[] ) {
		PropertyConfigurator.configure("CivetConfig.txt");
		CivetConfig.checkAllConfig();
		try {
			ArrayList<File> files = new ArrayList<File>();
		files.add(new File("Test/AgViewTest.pdf"));
		files.add(new File("Test/CivetTest.cvi"));
		files.add(new File("Test/CO_KS_Test1.pdf"));
		files.add(new File("Test/ImageTest1.gif"));
		files.add(new File("Test/ImageTest2.jpg"));
		files.add(new File("Test/ImageTest3.PNG"));
		files.add(new File("Test/mCVITest.pdf"));
		files.add(new File("Test/PDFTest1.pdf"));
		files.add(new File("Test/PDFTest3.pdf"));
		OpenFileList list = new OpenFileList(files);
		OpenFile thisFile = list.currentFile();
		while( thisFile != null ) {
			SourceFile source = thisFile.getSource();
			System.out.println(source.getType() + ", " + source.isPageable() + ": " + source.getPageCount() + ": " + source.canSplit());
			if( thisFile.getSource().canSplit() ) {
				while( thisFile.morePagesForward() ) {
					thisFile.saveNext();
//					could also add page to prev
				}
				thisFile.saveNext();
			}
			list.markFileComplete(thisFile);
			thisFile = list.nextFile();
		}
		for( OpenFile fileDone : list.aFilesComplete ) {
			System.out.print(fileDone.getSource().sFilePath);
			for( Integer i : fileDone.getPagesDone() )
				System.out.print( ", " + i );
			System.out.println();
		}
		} catch( SourceFileException e ) {
			logger.error("Bad Source File", e);
		}
		
	}

	/**
	 * @throws SourceFileException 
	 * 
	 */
	public OpenFileList( ArrayList<File> aFiles ) throws SourceFileException {
		aOpenFiles = new ArrayList<OpenFile>();
		aFilesComplete = new ArrayList<OpenFile>();
		for( File f : aFiles ) {
			OpenFile openFile = new OpenFile(f);
			aOpenFiles.add(openFile);
		}
		oCurrent = aOpenFiles.get(0);
	}
	
	public boolean moreFilesForward() {
		boolean bRet = false;
		Integer iCurrent = aOpenFiles.indexOf(oCurrent);
		for( int i = iCurrent + 1; i < aOpenFiles.size(); i++ ) {
			OpenFile oNext = aOpenFiles.get(i);
			if( !aFilesComplete.contains(oNext) ) {
				bRet = true;
				break;
			}
		}
		return bRet;
	}

	public boolean moreFilesBack() {
		boolean bRet = false;
		Integer iCurrent = aOpenFiles.indexOf(oCurrent);
		for( int i = iCurrent - 1; i >= 0; i-- ) {
			OpenFile oNext = aOpenFiles.get(i);
			if( !aFilesComplete.contains(oNext) ) {
				bRet = true;
				break;
			}
		}
		return bRet;
	}
	
	public OpenFile currentFile() {
		OpenFile oRet = null;
		oRet = oCurrent;
		if( oRet == null )
			oRet = nextFile();
		return oRet;
	}
	
	public OpenFile nextFile() {
		OpenFile oRet = null;
		oRet = nextFileForward();
		if( oRet == null )
			oRet = nextFileBack();
		return oRet;
	}
	
	public OpenFile nextFileForward() {
		OpenFile oRet = null;
		Integer iCurrent = aOpenFiles.indexOf(oCurrent);
		for( int i = iCurrent + 1; i < aOpenFiles.size(); i++ ) {
			OpenFile oNext = aOpenFiles.get(i);
			if( !aFilesComplete.contains(oNext) ) {
				oRet = oNext;
				break;
			}
		}
		
		return oRet;
	}
	
	public OpenFile nextFileBack() {
		OpenFile oRet = null;
		Integer iCurrent = aOpenFiles.indexOf(oCurrent);
		for( int i = iCurrent - 1; i >= 0; i-- ) {
			OpenFile oNext = aOpenFiles.get(i);
			if( !aFilesComplete.contains(oNext) ) {
				oRet = oNext;
				break;
			}
		}
		return oRet;
	}
	
	public void markFileComplete( OpenFile completeFile ) {
		aFilesComplete.add(completeFile);
	}

}
