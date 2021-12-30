package edu.clemson.lph.civet.threads;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import edu.clemson.lph.logging.Logger;


import edu.clemson.lph.civet.CivetInbox;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.webservice.CivetWebServices;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.ProgressDialog;
import edu.clemson.lph.dialogs.ThreadCancelListener;
import edu.clemson.lph.ems.EMSGetter;
import edu.clemson.lph.ems.EMSPutter;
import edu.clemson.lph.utils.FileUtils;

public class EMSSubscribeThread extends Thread implements ThreadCancelListener {
      private static Logger logger = Logger.getLogger();
	private ProgressDialog prog;
	private String sProgTitle = "Civet: Retrieving VSPS CVIs";
	private String sProgPrompt = "Certificate #: ";
	private CivetInbox parent = null;
	private volatile boolean bCanceled = false;
	private CivetWebServices service = null;

	public EMSSubscribeThread(CivetInbox parent) {
		this.parent = parent;
		service = new CivetWebServices();
		prog = new ProgressDialog(parent, sProgTitle, sProgPrompt + "getting count" );
		prog.setCancelListener(this);
		prog.setAuto(true);
		prog.setVisible(true);
	}

	@Override
	public void cancelThread() {
		bCanceled = true;
		interrupt();
	}
	
	@Override
	public void run() {
		try {
			logger.info("Getting CVIs Now");
			EMSGetter getter = new EMSGetter();
			int iLoop = 0;
			if( getter.getSummary() ) {
				int iMessagesLeft = getter.getSummaryInt("activeMessageCount");
				if( iMessagesLeft == 0 )
					MessageDialog.messageLater(parent, "VSPS EMS", "No new CVIs found");
				else {
					while( iMessagesLeft > 0 ) {
						if( bCanceled )
							break;
						System.out.println( "Message Count: " + iMessagesLeft );
						if( loopOnce() )
							iLoop++;
						if( getter.getSummary() ) {
							iMessagesLeft = getter.getSummaryInt("activeMessageCount");
						}
						else {
							break;
						}
					}
					MessageDialog.messageLater(parent, "VSPS EMS", iLoop + " CVIs received  and uploaded.");
				}
			}
			else {
				MessageDialog.messageLater(parent, "VSPS EMS", "Failed to get new CVI count.");
				logger.error("Failed to get CVI count");
			}
		} catch(Exception e){
			logger.error("\nError getting VSPS CVIs ", e );
		}
		finally {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					prog.setVisible(false);
				}
			});		
		}
	}

	private boolean loopOnce() {
		boolean bRet = false;
		EMSGetter getter = new EMSGetter();
		EMSPutter putter = new EMSPutter();
		try {
			boolean bGetRet = getter.getOne();
			boolean bPutRet = false;
			if( bGetRet ) {
				String sFileName = getter.getLastMsgFile();
				String sCertificateNbr = getter.getLastCertificate();
				File fSource = new File(sFileName);
				byte[] baIn = FileUtils.readUTF8File(fSource);
				byte[] baClean = removePrefix(baIn);
				String sResp = service.sendCviXML(baClean);
		logger.info(sResp);
		System.out.println(sResp);
				if( "\"Success\"".equals(sResp) )
					bPutRet = putter.putOne(getter.getLastLock(), "CompleteSuccessfullyProcessed");
				if( bGetRet && bPutRet ) {
					bRet = true;
					moveToDirectory(fSource, new File( CivetConfig.getEmsOutDirPath() ) );
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							prog.setMessage(sProgPrompt + sCertificateNbr);
						}
					});		
				}
			}
		} catch (Exception e) {
			logger.error("Error in loopOnce", e);
			e.printStackTrace();
		}
		return bRet;
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
			logger.error(e);
			e.printStackTrace();
		}
		return aRet;
	}

	/**
	 * Move source file and if subclass includes one, the data file to the specified directory.
	 * @param fDir
	 * @return true if moved successfully.
	 */
	public boolean moveToDirectory( File fSource, File fDir ) {
		logger.info("Moving " + fSource.getAbsolutePath() + " to " + fDir.getAbsolutePath());
		boolean bRet = false;
		File fNew = new File(fDir, fSource.getName() );
		if( fNew.exists() ) {
			String sOutPath = fNew.getAbsolutePath();
			sOutPath = FileUtils.incrementFileName(sOutPath);
			logger.error(fNew.getName() + " already exists in " + fDir.getAbsolutePath() + "\n" +
					"Saving as " + sOutPath);
			fNew = new File( sOutPath );
		}
		bRet = fSource.renameTo(fNew);
		if (!bRet) {
			logger.error("Could not move " + fSource.getName() + " to " + fNew.getAbsolutePath() );
		}
		return bRet;
	}


}
