package edu.clemson.lph.civet.threads;
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
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import edu.clemson.lph.logging.Logger;
import org.jpedal.exception.PdfException;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetEditDialog;
import edu.clemson.lph.civet.CivetEditDialogController;
import edu.clemson.lph.civet.files.OpenFileList;
import edu.clemson.lph.civet.files.SourceFileException;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.ProgressDialog;

public class OpenFilesThread extends Thread {
      private static Logger logger = Logger.getLogger();
	private ProgressDialog prog;
	private CivetEditDialogController dlgController;
	private CivetEditDialog dlg;
	private ArrayList<File> filesToOpen = null;
	private OpenFileList openFiles = null;
	private File currentFile;
	
	/**
	 * Read a list of files and return list of OpenFile objects in call back.
	 * @param dlg CivetEditDialog that owns this thread
	 * @param fCurrentFile
	 */
	public OpenFilesThread(CivetEditDialogController dlgController, ArrayList<File> filesToOpen, OpenFileList openFiles) {
		this.dlgController = dlgController;
		this.dlg = dlgController.getDialog();
		this.filesToOpen = filesToOpen;
		this.openFiles = openFiles;
		if( this.filesToOpen != null && this.filesToOpen.size() > 0 ) {
			this.currentFile = this.filesToOpen.get(0);
			prog = new ProgressDialog(dlg.getInboxParent(), "Civet", "Opening CVI File " + this.currentFile.getName() );
			prog.setAuto(true);
			prog.setVisible(true);
		} 
		else {
			logger.error("OpenFilesThread called with empty file list");
		}
	}
	
	@Override
	public void run() {
		if( this.filesToOpen == null || this.filesToOpen.size() == 0 ) {
			logger.error("OpenFilesThread run with empty file list");
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					logger.error("OpenFilesThread run with empty file list");
					MessageDialog.showMessage(dlg.getInboxParent(), "Civet Error", "OpenFilesThread run with empty file list");
					dlgController.openFilesComplete();
					prog.setVisible(false);
					prog.dispose();
				}
			});
			return;
		}
		String sCurrentFile = null;
		try {
			for( File f : filesToOpen ) {
				sCurrentFile = f.getAbsolutePath();
				prog.setMessage("Opening CVI File " + f.getName());
				openFiles.openFile(f);
			}
		} catch(SourceFileException | PdfException e){
			logger.error("\nError reading or decoding file " + sCurrentFile, e );
		} finally {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					dlgController.openFilesComplete();
					prog.setVisible(false);
					prog.dispose();
				}
			});
		} 
	}

}
