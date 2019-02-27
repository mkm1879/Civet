package edu.clemson.lph.civet.files;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetEditDialogController;
import edu.clemson.lph.civet.threads.SaveCVIModelThread;

/**
 * this is a queue of exactly two items 
 */
public class OpenFileSaveQueue {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private volatile boolean bInSave = false;
	private OpenFile fileOut = null;
	private OpenFile fileIn = null;
	private CivetEditDialogController controller;
	private volatile int iThreads = 0;

	public OpenFileSaveQueue(CivetEditDialogController controller ) {
		this.controller = controller;
		iThreads = 0;
	}
	
	@Override protected void finalize() throws Throwable {
	    flush();
	    super.finalize();
	}
	
	public void push( OpenFile openFile ) {
		if( fileIn != null ) {
			fileOut = fileIn;
			iThreads++;
			save(fileOut);
			fileOut = null;
		}
		fileIn = openFile;
	}
	
	public OpenFile pop() {
		OpenFile fRet = fileIn;
		fileIn = null;
		return fRet;
	}
	
	public void flush() {
		if( fileIn != null ) {
			fileOut = fileIn;
			fileIn = null;
			iThreads++;
			save( fileOut );
			fileOut = null;
		}
	}
	
	public void saveComplete() {
		bInSave = false;
		iThreads--;
		if( iThreads == 0 )
			controller.saveComplete();
		if( iThreads < 0 ) {
			logger.error("Lost thread count");
		}
	}

	private void save(OpenFile fileOut) {
		bInSave = true;
		SaveCVIModelThread saveThread = new SaveCVIModelThread( this, fileOut);
		saveThread.start();
	}

}
