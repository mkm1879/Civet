package edu.clemson.lph.civet.files;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.threads.SaveCVIModelThread;

/**
 * this is a queue of exactly two items 
 */
public class OpenFileSaveQueue {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	private volatile boolean bInSave = false;
	private OpenFile fileOut = null;
	private OpenFile fileIn = null;

	public OpenFileSaveQueue() {
		// TODO Auto-generated constructor stub
	}
	
	@Override protected void finalize() throws Throwable {
	    flush();
	    super.finalize();
	}
	
	public void push( OpenFile openFile ) {
		if( fileIn != null ) {
			fileOut = fileIn;
			synchronized( this ) {
				while( bInSave ) {
					try {
						wait(100);
					} catch (InterruptedException e) {
						// Just ignore
					}
				}
			}
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
			save( fileOut );
			fileOut = null;
		}
	}
	
	public synchronized void saveComplete() {
		bInSave = false;
	}

	private synchronized void save(OpenFile fileOut) {
		bInSave = true;
		SaveCVIModelThread saveThread = new SaveCVIModelThread( this, fileOut);
		saveThread.start();
	}

}
