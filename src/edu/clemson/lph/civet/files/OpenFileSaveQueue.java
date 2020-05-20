package edu.clemson.lph.civet.files;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetEditDialogController;
import edu.clemson.lph.civet.threads.SaveCVIModelThread;

/**
 * this is a queue of exactly one item
 */
public class OpenFileSaveQueue {
	private static final Logger logger = Logger.getLogger(Civet.class.getName());
	private String fileIn = null;
	private CivetEditDialogController controller;
	private int iThreads = 0;

	public OpenFileSaveQueue(CivetEditDialogController controller ) {
		this.controller = controller;
		iThreads = 0;
	}
//	
//	@Override protected void finalize() throws Throwable {
//	    flush();
//	    super.finalize();
//	}
	
	public void push( String openFile ) {
		String fileToSave = fileIn;
		fileIn = openFile;
		if( fileToSave != null ) {
			save(fileToSave);
		}
	}
	
	public String pop() {
		String fRet = fileIn;
		fileIn = null;
		return fRet;
	}
	
	public void flush() {
		String fileToSave = fileIn;
		fileIn = null;
		if( fileToSave != null ) {
			save( fileToSave );
		}
	}
	
	public boolean hasFileInQueue() {
		return (fileIn != null);
	}
	
	public synchronized void saveComplete(String sFilePath) {
		iThreads--;
		if( iThreads == 0 )
			controller.saveComplete(sFilePath);
		if( iThreads < 0 ) {
			logger.error("Lost thread count");
		}
	}

	private synchronized void save(String xmlToSave) {
		iThreads++;
		SaveCVIModelThread saveThread = new SaveCVIModelThread( this, xmlToSave);
		saveThread.start();
	}

}
