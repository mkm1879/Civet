package edu.clemson.lph.civet.files;

import edu.clemson.lph.logging.Logger;


import edu.clemson.lph.civet.CivetEditDialogController;
import edu.clemson.lph.civet.threads.SaveCVIModelThread;

/**
 * this is a queue of exactly one item
 */
public class OpenFileSaveQueue {
      private static Logger logger = Logger.getLogger();
	private byte[] xmlBytesIn = null;
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
	
	public void push( byte[] openFileBytes ) {
		byte[] bytesToSave = xmlBytesIn;
		xmlBytesIn = openFileBytes;
		if( bytesToSave != null ) {
			save(bytesToSave);
		}
	}
	
	public byte[] pop() {
		byte[] fRet = xmlBytesIn;
		xmlBytesIn = null;
		return fRet;
	}
	
	public void flush() {
		byte[] bytesToSave = xmlBytesIn;
		xmlBytesIn = null;
		if( bytesToSave != null ) {
			save( bytesToSave );
		}
	}
	
	public boolean hasFileInQueue() {
		return (xmlBytesIn != null);
	}
	
	public synchronized void saveComplete(String sFilePath) {
		iThreads--;
		if( iThreads == 0 )
			controller.saveComplete(sFilePath);
		if( iThreads < 0 ) {
			logger.error("Lost thread count");
		}
	}

	private synchronized void save(byte[] xmlToSave) {
		iThreads++;
		SaveCVIModelThread saveThread = new SaveCVIModelThread( this, xmlToSave);
		saveThread.start();
	}

}
