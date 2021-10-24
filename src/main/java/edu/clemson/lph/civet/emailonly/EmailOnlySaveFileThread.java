package edu.clemson.lph.civet.emailonly;

import java.io.File;

import javax.swing.SwingUtilities;

import edu.clemson.lph.logging.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.ProgressDialog;
import edu.clemson.lph.utils.FileUtils;

class EmailOnlySaveFileThread extends Thread {
      private static Logger logger = Logger.getLogger();
	private byte[] fileBytes;
	private String sFileName;
	private EmailOnlyDialog dlg;
	private ProgressDialog prog;

	EmailOnlySaveFileThread( EmailOnlyDialog parent, byte[] fileBytes, String sFileName ) {
		dlg = parent;
		this.fileBytes = fileBytes;
		this.sFileName = sFileName;
		prog = new ProgressDialog(dlg, "Civet", "Saving File");
		prog.setAuto(true);
		prog.setVisible(true);
	}
	
	@Override
	public void run() {
		
		try {
			String sSendPath = CivetConfig.getEmailOnlySendPath();
			if( sSendPath == null || sSendPath.trim().length() == 0 ) {
				MessageDialog.showMessage(dlg, "Civet Error", "Set EmailSendOnlyIn preference to use this feature");
			}
			else {
				File fSendPath = new File( CivetConfig.getEmailOnlySendPath() );
				if( fSendPath == null || !fSendPath.isDirectory() ) {
					MessageDialog.showMessage(dlg, "Civet Error", "EmailSendOnlyIn folder " + sSendPath + " does not exist");
				}
				else {
					File fSendFile = new File( fSendPath, sFileName );
					FileUtils.writeBinaryFile(fileBytes, fSendFile.getAbsolutePath() );
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex);
		}
	    finally {
	    	
	    }
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				prog.setVisible(false);
				prog.dispose();
			}
		});
	}

}
