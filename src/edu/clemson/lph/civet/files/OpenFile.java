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
import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;

/**
 * 
 */
public class OpenFile {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private SourceFile source = null;
	private StdeCviXmlModel model = null;
	private ArrayList<Integer> aPagesInCurrent = null;
	private ArrayList<Integer> aPagesDone = null;

	/**
	 * @throws SourceFileException 
	 * 
	 */
	public OpenFile( String sFilePath ) throws SourceFileException {
		File fFile = new File( sFilePath );
		if( fFile != null && fFile.exists() && fFile.isFile() ) {
			source = SourceFile.readSourceFile(fFile);
			model = source.getDataModel();
			aPagesInCurrent = new ArrayList<Integer>();
			aPagesInCurrent.add(source.getCurrentPage());
			aPagesDone = new ArrayList<Integer>();
		}
		else {
			throw new SourceFileException( "Attempt to read non-file " + sFilePath );
		}
	}
	/**
	 * @throws SourceFileException 
	 * 
	 */
	public OpenFile( File fFile ) throws SourceFileException {
		if( fFile != null && fFile.exists() && fFile.isFile() ) {
			source = SourceFile.readSourceFile(fFile);
			model = source.getDataModel();
			aPagesInCurrent = new ArrayList<Integer>();
			aPagesInCurrent.add(source.getCurrentPage());
			aPagesDone = new ArrayList<Integer>();
		}
		else if( fFile != null ) {
			throw new SourceFileException( "Attempt to read non-file " + fFile.getName() );
		}
		else  {
			throw new SourceFileException( "Attempt to read null file" );
		}
	}
	
	public SourceFile getSource() {
		return source;
	}
	
	public StdeCviXmlModel getModel() {
		return model;
	}
	
	public Integer getCurrentPage() {
		return source.getCurrentPage();
	}
	
	public Integer getPageCount() {
		return source.getPageCount();
	}
	
	/**
	 * Only looking at incomplete pages
	 * @return
	 */
	public boolean morePagesForward() {
		boolean bRet = false;
		if( source.isPageable() ) {
			for( Integer i = source.getCurrentPage() + 1; i < source.getPageCount(); i++ ) {
				if( !aPagesDone.contains(i) && !aPagesInCurrent.contains(i) ) {
					bRet = true;
					break;
				}
			}
		}
		return bRet;
	}
	
	/**
	 * Only looking at incomplete pages
	 * @return
	 */
	public boolean morePagesBack() {
		boolean bRet = false;
		if( source.isPageable() ) {
			for( Integer i = source.getCurrentPage() - 1; i > 0; i-- ) {
				if( !aPagesDone.contains(i) && !aPagesInCurrent.contains(i) ) {
					bRet = true;
					break;
				}
			}
		}
		return bRet;
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
			model = source.split();
//			Integer iPage = nextUnsavedPage();
//			source.setCurrentPage(iPage);
			aPagesInCurrent = new ArrayList<Integer>();
			aPagesInCurrent.add(getCurrentPage());
		}
		else {
			throw new SourceFileException("addPageToCurrent called on non-splittable source");
		}
		return modelRet;
	}
	
	public Integer nextUnsavedPage() {
		Integer iRet = null;
		iRet = nextPageForward();
		if( iRet == null ) 
			iRet = nextPageBack();
		return iRet;
	}
	
	public Integer nextPageForward() {
		Integer iRet = null;
		for( Integer i = source.getCurrentPage() + 1; i < source.getPageCount(); i++ ) {
			if( !aPagesDone.contains(i) && !aPagesInCurrent.contains(i) ) {
				iRet = i;
				break;
			}
		}
		return iRet;
	}
	
	public Integer nextPageBack() {
		Integer iRet = null;
		for( Integer i = source.getCurrentPage() - 1; i > 0; i-- ) {
			if( !aPagesDone.contains(i) && !aPagesInCurrent.contains(i) ) {
				iRet = i;
				break;
			}
		}
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
	public void addPageToCurrent( Integer iPage ) throws SourceFileException {
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

}
