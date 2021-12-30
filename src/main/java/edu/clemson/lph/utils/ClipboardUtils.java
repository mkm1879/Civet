package edu.clemson.lph.utils;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.List;
import edu.clemson.lph.logging.Logger;


public class ClipboardUtils {
      private static Logger logger = Logger.getLogger();

	private ClipboardUtils() {}

	public static void main(String[] args) {
		List<String> lClip =  getClipStringList(); 
		for( String sLine : lClip )
			System.out.println( "Line " + sLine);
	}
	
	public static List<String> getClipStringList() {
		List<String> lRet = new ArrayList<String>();
		try {
			String sClip = getClipString();
			if( sClip != null ) {
				lRet = StringUtils.getStringLines(sClip);
			}
		} catch (Exception e) {
			logger.error("Error parsing clipboard string", e);
		}
		return lRet;
	}
	
	public static String getClipString() {
		String sRet = null;
		try {
			sRet = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (Exception e) {
			logger.error("Error parsing clipboard to string", e);
		} 
		return sRet;
	}

}
