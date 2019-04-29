package edu.clemson.lph.civet.threads;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.files.OpenFile;
import edu.clemson.lph.civet.files.OpenFileSaveQueue;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.xml.StdeCviXmlModel;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.utils.FileUtils;

public class SaveCVIModelThread extends Thread {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private OpenFileSaveQueue queue;
	private OpenFile fileToSave;
	StdeCviXmlModel modelToSave;
	
	private String sXmlFileName;


	public SaveCVIModelThread( OpenFileSaveQueue q, OpenFile fileToSave ) {
		this.queue = q;
		this.fileToSave = fileToSave;
		this.modelToSave = fileToSave.getModel();
	}
	
	@Override
	public void run() {
		try {
			setUpFileNamesAndContent();
			String sXml = modelToSave.getXMLString();
			saveXml( sXml );
			saveEmail( sXml );
		}
		catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex);
		}
	    finally {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					String sToFileDir = CivetConfig.getToFileDirPath();
					File toFile = new File( sToFileDir, sXmlFileName);
					queue.saveComplete(toFile.getAbsolutePath());
				}
			});
	    }
	}

	
	/**
	 * This is much simplified by new model.
	 */
	private void setUpFileNamesAndContent() {
		sXmlFileName = "CVI_" + modelToSave.getOriginStateCode() 
			+ "_To_" + modelToSave.getDestinationStateCode() + "_" 
			+ modelToSave.getCertificateNumber() + ".cvi";
		sXmlFileName = FileUtils.replaceInvalidFileNameChars(sXmlFileName);	
		String sOriginalFileName = fileToSave.getSource().getFileName();
		checkExistingFiles( sOriginalFileName, sXmlFileName );
		checkAttachment();
	}
	
	private void checkAttachment() {
		if( !modelToSave.hasPDFAttachment() ) {
			String sCertificateNbr = modelToSave.getCertificateNumber();
			MessageDialog.showMessage(null, "Civet Error: Empty Attachment", "PDF File Attachment Failed for CVI " + sCertificateNbr);
			logger.error("PDF File Attachment Failed for CVI " + sCertificateNbr);
		}
	}
	
	/**
	 * Look for existing .cvi files whose names will change due to state or cvi number edits
	 * Delete any existing.
	 * @param sExisting filename  (just name)
	 * @param sNew filename  (just name)
	 */
	private void checkExistingFiles(String sExisting, String sNew) {
		// Don't waste time if not editing an existing file
		if( sExisting != null && sExisting.toLowerCase().endsWith(".cvi") && !sExisting.equalsIgnoreCase(sNew) ) {
			// Appears we opened and changed an existing .cvi file need to delete existing before saving.
			String sEmailOutDir = CivetConfig.getEmailOutDirPath();
			String sEmailErrorsDir = CivetConfig.getEmailErrorsDirPath();
			String sToFileDir = CivetConfig.getToFileDirPath();
			File fEmailOut = new File ( sEmailOutDir + sExisting );
			File fEmailErrors = new File ( sEmailErrorsDir + sExisting );
			File fToFile = new File ( sToFileDir + sExisting );
			try { 
				if( fEmailOut.exists() && fEmailOut.isFile() )
					fEmailOut.delete();
			} catch( Exception e ) {
				logger.error("Could not delete file " + fEmailOut, e);
			}
			try { 
				if( fEmailErrors.exists() && fEmailErrors.isFile() )
					fEmailErrors.delete();
			} catch( Exception e ) {
				logger.error("Could not delete file " + fEmailErrors, e);
			}
			try { 
				if( fToFile.exists() && fToFile.isFile() )
					fToFile.delete();
			} catch( Exception e ) {
				logger.error("Could not delete file " + fToFile, e);
			}
		}
	}
	
	
	private void saveXml(String sStdXml) {
		String sDirPath = CivetConfig.getToFileDirPath();
		File fDir = new File(sDirPath);
		final File fileOut = new File(fDir, sXmlFileName);
		final String sFilePath = fileOut.getAbsolutePath();
		try {
			PrintWriter pw = new PrintWriter( new FileOutputStream( fileOut ) );
			pw.print(sStdXml);
			pw.flush();
			pw.close();
		} catch (final Exception e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					logger.error("Could not save " + sFilePath, e);
					MessageDialog.showMessage(null, "Civet Error: File Save", "Could not save file\n " + sFilePath );
				}
			});
			return;
		}
	}
	
	private void saveEmail(String sStdXml) {
		File fileOut = null;
		String sFilePath = null;
		if( modelToSave.isExport() ) {
			String sDirPath = CivetConfig.getEmailOutDirPath();
			File fDir = new File(sDirPath);
			fileOut = new File(fDir, sXmlFileName);
			sFilePath = fileOut.getAbsolutePath();
		}
		else if( modelToSave.hasErrors() ) {
			String sDirPath =  CivetConfig.getEmailErrorsDirPath();
			File fDir = new File(sDirPath);
			fileOut = new File(fDir, sXmlFileName);
			sFilePath = fileOut.getAbsolutePath();
		}
		if( sFilePath == null ) 
			return;
		try {
			PrintWriter pw = new PrintWriter( new FileOutputStream( fileOut ) );
			pw.print(sStdXml);
			pw.flush();
			pw.close();
		} catch (final Exception e) {
			final String sFilePathNotSaved = sFilePath;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					logger.error("Could not save " + sFilePathNotSaved, e);
					MessageDialog.showMessage(null, "Civet Error: File Save", "Could not save file\n " + sFilePathNotSaved );
				}
			});
			return;
		}
	}
	
}
