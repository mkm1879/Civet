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
import java.io.File;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.clemson.lph.dialogs.ProgressDialog;

public abstract class ProcessFilesThread extends Thread {
	protected static final Logger logger = Logger.getLogger(Civet.class.getName());
	protected List<File> allFiles = null;
	protected String sCurrentFilePath = "";
	protected ProgressDialog prog;
	protected String sProgTitle = "Civet: Processing File";
	protected String sProgPrompt = "File: ";
	protected String sOutPath;
	CivetInbox parent = null;

	public ProcessFilesThread( CivetInbox parent, List<File> files) {
		this.parent = parent;
		this.allFiles = files;
		sOutPath = CivetConfig.getOutputDirPath();
		prog = new ProgressDialog(parent, sProgTitle, sProgPrompt );
		prog.setAuto(true);
		prog.setVisible(true);
	}
	
	@Override
	public void run() {
		try {
			for( File fThis : allFiles ) {
				final String sName = fThis.getName();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						prog.setMessage(sProgPrompt + sName);
					}
				});		
				sCurrentFilePath = fThis.getAbsolutePath();
				processFile( fThis );
				File fOut = new File( sOutPath + sName );
				fThis.renameTo(fOut);
			}
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					doLater();
				}
			});		
		} catch(Exception e){
			logger.error("\nError processing file " + sCurrentFilePath, e );
		}
		finally {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					parent.refreshTables();
					prog.setVisible(false);
				}
			});		
		}
		doLater();
	}
	
	protected abstract void processFile(File fThis);
	
	protected abstract void doLater();

}
