package edu.clemson.lph.civet.emailonly;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.dialogs.ProgressDialog;
import edu.clemson.lph.utils.FileUtils;

public class EmailOnlySaveFileThread extends Thread {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private byte[] fileBytes;
	private String sFileName;
	private EmailOnlyDialog dlg;
	private ProgressDialog prog;

	public EmailOnlySaveFileThread( EmailOnlyDialog parent, byte[] fileBytes, String sFileName ) {
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
			String sSendPath = CivetConfig.getEmailOnlySendPath() + sFileName;
			FileUtils.writeBinaryFile(fileBytes, sSendPath);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex);
		}
	    finally {
	    	
	    }
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				dlg.saveComplete();
				prog.setVisible(false);
				prog.dispose();
			}
		});
	}

}
