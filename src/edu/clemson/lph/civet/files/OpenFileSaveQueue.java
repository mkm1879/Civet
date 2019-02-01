package edu.clemson.lph.civet.files;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;

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
		}
	}
	
	public synchronized void saveComplete() {
		bInSave = false;
	}

	private synchronized void save(OpenFile fileOut2) {
		bInSave = true;
		
	}

}
