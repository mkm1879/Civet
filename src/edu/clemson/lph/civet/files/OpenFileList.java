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

import edu.clemson.lph.civet.Civet;

/**
 * 
 */
public class OpenFileList {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private ArrayList<OpenFile> aOpenFiles = null;
	private ArrayList<OpenFile> aFilesComplete = null;
	private OpenFile oCurrent;

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
			if( !aOpenFiles.contains(oNext) ) {
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
			if( !aOpenFiles.contains(oNext) ) {
				bRet = true;
				break;
			}
		}
		return bRet;
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
			if( !aOpenFiles.contains(oNext) ) {
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
			if( !aOpenFiles.contains(oNext) ) {
				oRet = oNext;
				break;
			}
		}
		
		return oRet;
	}

}
