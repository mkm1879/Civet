package edu.clemson.lph.ems;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.PropertyConfigurator;

import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.webservice.CivetWebServices;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.utils.FileUtils;


public class EmsSubscriber {
	public static boolean bTesting = true;
	public static boolean bCompressOutput = true;
	public static boolean bDays = true;
	private CivetWebServices service = null;


	public static void main(String[] args) {
		PropertyConfigurator.configure("CivetConfig.txt");
		// Fail now so config file and required files can be fixed before work is done.
		CivetConfig.checkAllConfig();
		CivetConfig.validateHerdsCredentials();
		bTesting = true;
		EmsSubscriber me = new EmsSubscriber();
		me.getAlleCVIs();
	}
	
	public EmsSubscriber() {
		service = new CivetWebServices();
	}
	
	public void getAlleCVIs() {
		EMSGetter getter = new EMSGetter();
		int iLoop = 0;
		if( getter.getSummary() ) {
			int iMessagesLeft = getter.getSummaryInt("activeMessageCount");
			if( iMessagesLeft == 0 )
				System.out.println("No new messages");
			while( iMessagesLeft > 0 ) {
				System.out.println( "Message Count: " + iMessagesLeft );
				loopOnce();
				if( ++iLoop >= 1 && bTesting )  // Do just 1 at a time for now.
					break; 
				// Or check for remaining messages
				if( getter.getSummary() ) {
					iMessagesLeft = getter.getSummaryInt("activeMessageCount");
				}
				else {
					break;
				}
			}
		}
	}
	
		
//		//TODO Create loop of loopOnce until all are retrieved.
	
	private void loopOnce() {
		EMSGetter getter = new EMSGetter();
		EMSPutter putter = new EMSPutter();
		CivetWebServices cws = new CivetWebServices();
		try {
			boolean bGetRet = getter.getOne();
			boolean bPutRet = false;
			if( bGetRet ) {
				String sFileName = getter.getLastMsgFile();
				// Todo Get the CertificateNbr from the file and test using HerdsConnection as above
				System.out.println("Received: " + sFileName);
				String sCertificateNbr = getter.getLastCertificate();
				System.out.println("CertNbr = " + sCertificateNbr);
				byte[] baIn = FileUtils.readUTF8File(new File(sFileName));
				byte[] baClean = removePrefix(baIn);
				String sResp = service.sendCviXML(baClean);

				System.out.println("Send CVI XML returned: " + sResp);					
				bPutRet = putter.putOne(getter.getLastLock(), "CompleteSuccessfullyProcessed");
				MessageDialog.showMessage(null, "VSPS EMS", "Received certificate " + sCertificateNbr +
						"\nSaved as " + sFileName + " and uploaded.");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static String findPrefix( String sXml ) {
		String sRet = null;
		String sPattern = "xmlns:.*=\"http://www.usaha.org/xmlns/ecvi2\"";
		Pattern pattern = Pattern.compile(sPattern);
	      Matcher m = pattern.matcher(sXml);
	      if (m.find( )) {
	    	  sRet = m.group(0);
	    	  sRet = sRet.substring(6);
	    	  int iEnd = sRet.indexOf('=');
	    	  sRet = sRet.substring(0, iEnd);
	      }
		return sRet;
	}
	
	
	private static byte[] removePrefix(byte[] baIn) {
		byte[] aRet = null;
		try {
			String sXmlIn = new String( baIn, "UTF-8");
			String sPrefix = findPrefix( sXmlIn );
			if( sPrefix != null ) {
				String sReplace = sPrefix + ":";
				String sXml = sXmlIn.replaceAll(sReplace, "");
				sReplace = ":" + sPrefix;
				sXml = sXml.replaceAll(sReplace, "");
				aRet = sXml.getBytes("UTF-8");
			}
			else {
				aRet = baIn;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return aRet;
	}

}
