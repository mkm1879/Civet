package edu.clemson.lph.civet.prefs;
/*
Copyright 2014 Michael K Martin

This file is part of Civet.

Civet is free software: you can redistribute it and/or modify
it under the terms of the Lesser GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Civet is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the Lesser GNU General Public License
along with Civet.  If not, see <http://www.gnu.org/licenses/>.
*/
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.SwingUtilities;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.webservice.UsaHerdsWebServiceAuthentication;
import edu.clemson.lph.civet.webservice.WebServiceException;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.TwoLineQuestionDialog;
import edu.clemson.lph.mailman.MailMan;
import edu.clemson.lph.logging.Logger;

public class CivetConfig {
    private static Logger logger = Logger.getLogger();
	// initialized in Civet.main via checkAllConfig
	private static Properties props = null;
	private static final int UNK = -1;
	private static final int LGPL = 0;
	private static final int XFA = 1;
	private static final long DEFAULT_MAX_ANIMALS = 100;
	private static final long DEFAULT_WS_TIMEOUT = 2 * 60 * 1000;  // Two minutes
	private static int iJPedalType = UNK;
	private static String sHERDSUserName = null;
	private static String sHERDSPassword = null;
	private static Boolean bStandAlone = null;
	private static Boolean bDefaultReceivedDate = null;
	private static Boolean bBrokenLIDs = null;
	private static Boolean bSaveCopies = null;
	// Only used in local direct DB add-ons
	private static String sDBUserName = null;
	private static String sDBPassword = null;
	private static Boolean bSmall;
	private static boolean bStateIDChecksum;
	private static Boolean bAutoOpenPDF;
	private static Boolean bSaveSkipped = null;
	private static Boolean bIgnoreSkipped = null;
	private static Boolean bOpenAfterAdd = null;
	private static Boolean bCheckAccredStatus;
	private static Boolean bEmsVerbose = null;
	

	public synchronized static Properties getProps() {
		return props;
	}

	/**
	 * Check to see if we are on LPH LAN.  Only used in local version.
	 * @return
	 */
	public synchronized static String[] listLocalNetAddresses() {
		String sAddresses = props.getProperty("localNetAddresses");
		if( sAddresses == null || sAddresses.trim().length() == 0 ) {
			sAddresses = "130.127.169.203";
		}
		StringTokenizer tok = new StringTokenizer( sAddresses, ",");
		List<String>lAddresses = new ArrayList<String>();
		while( tok.hasMoreTokens() ) {
			lAddresses.add(tok.nextToken());
		}
		String[] aRet = new String[lAddresses.size()];
		for( int i = 0; i < lAddresses.size(); i++ )
			aRet[i] = lAddresses.get(i);
		return aRet;
	}
	
	public synchronized static boolean isStandAlone() {
		if( bStandAlone == null ) {
			String sVal = props.getProperty("standAlone");
			if( sVal == null ) exitError("standAlone");
			if( sVal.equalsIgnoreCase("true") || sVal.equalsIgnoreCase("yes")) {
				bStandAlone = true;
			}
			else {
				bStandAlone = false;
			}
		}
		return bStandAlone;
	}	
	
	/**
	 * Read the version from a one line text file in Resources.
	 * 
	 *  I keep forgetting how this trick works.  See https://community.oracle.com/blogs/pat/2004/10/23/stupid-scanner-tricks
	 	Finally now with Java 1.5's Scanner I have a true one-liner: 
    	String text = new Scanner( source ).useDelimiter("\\A").next();
		One line, one class. The only tricky is to remember the regex \A, which matches the beginning of input. 
		This effectively tells Scanner to tokenize the entire stream, from beginning to (illogical) next beginning. 
		As a bonus, Scanner can work not only with an InputStream as the source, but also a File, Channel, or 
		anything that implements the new java.lang.Readable interface. For example, to read a file: 
    	String text = new Scanner( new File("poem.txt") ).useDelimiter("\\A").next();
	 * @return String with version number for display.
	 */
	public static String getVersion() {
		String sRet = null;
		try {
			InputStream iVersion = Civet.class.getResourceAsStream("res/Version.txt");
			try ( @SuppressWarnings("resource")
			Scanner s = new Scanner(iVersion).useDelimiter("\\A") ) {
				sRet = s.hasNext() ? s.next() : "";
				s.close();
				iVersion.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		} 
		return sRet;
	}

	
	public synchronized static boolean isSmallScreen() {
		if( bSmall == null ) {
			String sVal = props.getProperty("smallScreen");
			if( sVal == null || sVal.trim().length() == 0 ) 
				bSmall = false;
			else if( sVal.equalsIgnoreCase("true") || sVal.equalsIgnoreCase("yes")) {
				bSmall = true;
			}
			else {
				bSmall = false;
			}
		}
		return bSmall;
	}	
	
	public synchronized static boolean isDefaultReceivedDate() {
		if( bDefaultReceivedDate == null ) {
			String sVal = props.getProperty("defaultReceivedDate");
			if( sVal == null || sVal.trim().length() == 0 )
				bDefaultReceivedDate = false;
			else if( sVal.equalsIgnoreCase("true") || sVal.equalsIgnoreCase("yes")) {
				bDefaultReceivedDate = true;
			}
			else {
				bDefaultReceivedDate = false;
			}
		}
		return bDefaultReceivedDate;
	}	
	
	public synchronized static boolean isSaveCopies() {
		if( bSaveCopies  == null ) {
			String sVal = props.getProperty("saveCopies");
			if( sVal == null || sVal.trim().length() == 0 )
				bSaveCopies = true;
			else if( sVal.equalsIgnoreCase("true") || sVal.equalsIgnoreCase("yes")) {
				bSaveCopies = true;
			}
			else {
				bSaveCopies = false;
			}
		}
		return bSaveCopies;
	}	
	
	public synchronized static boolean isSaveSkippedPages() {
		if( bSaveSkipped == null ) {
			String sVal = props.getProperty("saveSkipped");
			if( sVal == null ) bSaveSkipped = false;
			else if( sVal.equalsIgnoreCase("true") || sVal.equalsIgnoreCase("yes")) {
				bSaveSkipped = true;
			}
			else {
				bSaveSkipped = false;
			}
		}
		return bSaveSkipped;
	}	
	
	public synchronized static boolean isIgnoreSkippedPages() {
		if( bIgnoreSkipped == null ) {
			String sVal = props.getProperty("ignoreSkipped");
			if( sVal == null ) bIgnoreSkipped = true;
			else if( sVal.equalsIgnoreCase("true") || sVal.equalsIgnoreCase("yes")) {
				bIgnoreSkipped = true;
			}
			else {
				bIgnoreSkipped = false;
			}
		}
		return bIgnoreSkipped;
	}	
	
	public synchronized static boolean isOpenAfterAdd() {
		if( bOpenAfterAdd  == null ) {
			String sVal = props.getProperty("openAfterAdd");
			if( sVal == null || sVal.trim().length() == 0 )
				bOpenAfterAdd = true;
			else if( sVal.equalsIgnoreCase("true") || sVal.equalsIgnoreCase("yes")) {
				bOpenAfterAdd = true;
			}
			else {
				bOpenAfterAdd = false;
			}
		}
		return bOpenAfterAdd;
	}	
	
	public synchronized static boolean hasBrokenLIDs() {
		if( bBrokenLIDs == null ) {
			String sVal = props.getProperty("brokenLIDs");
			// default to true;
			if( sVal == null || sVal.trim().length() == 0 || sVal.equalsIgnoreCase("true") || sVal.equalsIgnoreCase("yes")) {
				bBrokenLIDs = true;
			}
			else {
				bBrokenLIDs = false;
			}
		}
		return bBrokenLIDs;
	}	
	
	
	public synchronized static boolean hasStateIDChecksum() {
		if( bBrokenLIDs == null ) {
			String sVal = props.getProperty("stateIDChecksum");
			// default to true;
			if( sVal == null || sVal.trim().length() == 0 || sVal.equalsIgnoreCase("true") || sVal.equalsIgnoreCase("yes")) {
				bStateIDChecksum = true;
			}
			else {
				bStateIDChecksum = false;
			}
		}
		return bStateIDChecksum;
	}	

	public synchronized static void setStandAlone( boolean standAlone ) {
		bStandAlone = standAlone;
	}
	
	public synchronized static String getLogLevel() {
		String sVal = props.getProperty("logLevel");
		if( sVal != null && (sVal.equalsIgnoreCase("info") || sVal.equalsIgnoreCase("error")  || sVal.equalsIgnoreCase("warn") ) )
			return sVal;
		else
			return "Error";
	}
	
	public synchronized static String getDefaultDirection() {
		String sRet = props.getProperty("defaultDirection");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("defaultDirection");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public synchronized static String getHomeStateAbbr() {
		String sRet = props.getProperty("homeStateAbbr");
		if( sRet == null || sRet.trim().length() == 0  ) exitError("homeStateAbbr");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public synchronized static String getHomeState() {
		String sRet = props.getProperty("homeState");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("homeState");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public synchronized static int getHomeStateKey() {
		int iRet = -1;
		String sRet = props.getProperty("homeStateKey");
		if( sRet == null ) exitError("homeStateKey");
		try {
			iRet = Integer.parseInt(sRet);
		} catch( NumberFormatException nfe ) {
			logger.info( "Cannot read Home State Key " + sRet + " as an integer number");
			logger.error(nfe);
			System.exit(1);
		}
		return iRet;
	}
		
	public synchronized static int getCviValidDays() {
		int iRet = -1;
		String sRet = props.getProperty("cviValidDays");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("cviValidDays");
		try {
			iRet = Integer.parseInt(sRet);
		} catch( NumberFormatException nfe ) {
			logger.error( "Cannot read cviValidDays " + sRet + " as an integer number");
			logger.error(nfe);
			System.exit(1);
		}
		return iRet;
	}

	
	public synchronized static String getZohoKey() {
		String sRet = props.getProperty("zohoKey");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public synchronized static String getZohoHost() {
		return "smtp.zoho.com";
	}

	public synchronized static String getZohoPort() {
		return "587";
	}
	
	public synchronized static int getZohoPortInt() {
		return 587;
	}

	public synchronized static String getZohoSecurity() {
		return "STARTTLS";
	}

	public synchronized static String getZohoDomain() {
		return "@mminformatics.com";
	}

	public synchronized static String getZohoFrom() {
		return "civet@mminformatics.com";
	}

	public synchronized static String getZohoUser() {
		return "civet@mminformatics.com";
	}
	public synchronized static String getZohoPass() {
		return "$newOne" + getZohoKey();
	}
	
	public synchronized static String getSmtpHost() {
		String sRet = props.getProperty("smtpHost");
		if( sRet != null && sRet.trim().length() == 0 ) {
			sRet = null; // Do not provide default value so we can differentiate.
		}
		return sRet;
	}
	
	public synchronized static String getSmtpPort() {
		String sRet = props.getProperty("smtpPort");
		if( sRet == null || sRet.trim().length() == 0 ) 
			sRet = getZohoPort();
		if( sRet == null || sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public synchronized static String getSmtpSecurity() {
		String sRet = props.getProperty("smtpSecurity");
		if( sRet == null || sRet.trim().length() == 0 ) 
			sRet = getZohoSecurity();
		if( sRet == null || sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public synchronized static int getSmtpPortInt() {
		int iRet = -1;
		String sRet = props.getProperty("smtpPort");
		if( sRet == null || sRet.trim().length() == 0 ) 
			sRet = getZohoPort();
		try {
			iRet = Integer.parseInt(sRet);
		} catch( NumberFormatException nfe ) {
			logger.error( "Cannot read smtp Port " + sRet + " as an integer number");
			logger.error(nfe);
			System.exit(1);
		}
		return iRet;
	}
	
	public synchronized static String getSmtpDomain() {
		String sRet = props.getProperty("smtpDomain");
		if( sRet == null || sRet.trim().length() == 0 ) 
			sRet = getZohoDomain();
		if( sRet == null || sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public synchronized static String getEmailCopyTo() {
		String sRet = props.getProperty("emailCopyTo");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	/**
	 * Optional configuration setting overrides normal logic
	 * to generate the From line (login containing @ or login + domain)
	 * @return String with Email From or NULL
	 */
	public synchronized static String getEmailFrom() {
		String sRet = null;
		sRet = props.getProperty("emailFrom");
		if( sRet == null || sRet.trim().length() == 0 ) 
			sRet = getZohoFrom();
		if( sRet == null || sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	/**
	 * Optional configuration setting to provide a Reply-to different from From
	 * @return String with Email From or NULL
	 */
	public synchronized static String getEmailReplyTo() {
		String sRet = null;
		sRet = props.getProperty("emailReplyTo");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	/**
	 * return the email address for testing to override real state vet CVI addresses
	 * This getter does not issue an error if not found because that is normal.
	 * @return
	 */
	public synchronized static String getEmailTestTo() {
		String sRet = props.getProperty("emailTestTo");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	/**
	 * return the URL address for the VSPS CVI subscription
	 * This getter does not issue an error if not found because that is normal.
	 * @return
	 */
	public synchronized static String getEMSSubscriptionURL() {
		String sRet = props.getProperty("subscriptionURL");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	/**
	 * return the security token for EMS 
	 * This getter does not issue an error if not found because that is normal.
	 * @return
	 */
	public synchronized static String getEMSToken() {
		String sRet = props.getProperty("emsToken");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	
	public synchronized static boolean isEMSVerbose() {
		if( bEmsVerbose  == null ) {
			String sVal = props.getProperty("emsVerbose");
			if( sVal == null || sVal.trim().length() == 0 )
				bEmsVerbose = true;
			else if( sVal.equalsIgnoreCase("true") || sVal.equalsIgnoreCase("yes")) {
				bEmsVerbose = true;
			}
			else {
				bEmsVerbose = false;
			}
		}
		return bEmsVerbose;
	}	
	
	/**
	 * Get the URL for USAHERDS web services
	 * @return
	 */
	public synchronized static String getHERDSWebServiceURL() {
		String sRet = props.getProperty("herdsWebServiceURL");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("herdsWebServiceURL");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		if( sRet.endsWith("/") || sRet.endsWith("\\") )
			sRet = sRet.substring(0, sRet.length() - 1);
		return sRet;
	}
	
	/**
	 * Get the URL for USAHERDS web services
	 * @return
	 */
	public synchronized static String getHERDSWebServiceHost() {
		String sRet = props.getProperty("herdsWebServiceURL");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("herdsWebServiceURL");
		try {
			URI uri = new URI(sRet);
			sRet = uri.getHost();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	
	/**
	 * Get the UserName USAHERDS web services
	 * @return
	 */
	public synchronized static String getHERDSUserName() {
		return sHERDSUserName;
	}
	
	/**
	 * Get the Password USAHERDS web services
	 * @return
	 */
	public synchronized static String getHERDSPassword() {
		return sHERDSPassword;
	}
	
	public synchronized static void setHERDSUserName( String sUser ) {
		sHERDSUserName = sUser;
	}
	
	public synchronized static void setHERDSPassword( String sPass ) {
		sHERDSPassword = sPass;
	}
	
	public synchronized static boolean validateHerdsCredentials() {
		boolean bRet = false;
		String sUser = CivetConfig.getHERDSUserName();
		String sPass = CivetConfig.getHERDSPassword();
		try {
			boolean bValid = false;
			while( !bValid ) {
				if( sUser != null && sUser.trim().length() > 0 && sPass != null && sPass.trim().length() > 0 ) {
					try {
						String sToken = UsaHerdsWebServiceAuthentication.getToken(CivetConfig.getHERDSWebServiceURL(), sUser, sPass);
						if( sToken != null && sToken.trim().length() > 0 ) {
							setHERDSUserName( sUser );
							setHERDSPassword( sPass );
							bRet = true;
							break;
						}
						else {
							MessageDialog.showMessage(null, "Civet Login Error", "Error logging into USAHERDS");
						}
					} catch( WebServiceException e ) {
						MessageDialog.showMessage(null, "Civet Login Error", "Error logging into USAHERDS\n"+ e.getMessage());
						logger.error("Error logging into USAHERDS", e);
						bValid = false;
					}
				}
				TwoLineQuestionDialog dlg = new TwoLineQuestionDialog( "USAHERDS Login: " + getHERDSWebServiceHost(), "UserID", "Password", true );
				dlg.setIntro("USAHERDS Login Settings");
				dlg.setPassword(true);
				if( sUser != null )
					dlg.setAnswerOne(sUser);
				if( sPass != null )
					dlg.setAnswerTwo(sPass);
				dlg.setVisible(true);
				if( !dlg.isExitOK() ) {
					System.exit(1);
				}
				sUser = dlg.getAnswerOne();
				sPass = dlg.getAnswerTwo();
				dlg.dispose();
				CivetConfig.setHERDSUserName(sUser);
				CivetConfig.setHERDSPassword(sPass);
			}
		} catch (Exception e) {
			logger.error("Error running main program in event thread", e);
		}
		return bRet;
		
	}
	
	public synchronized static boolean initEmail(boolean bLogin) {
		boolean bRet = true;
		String sUserID = null;
		String sZohoKey = getZohoKey();
		String sSmtpHost = getSmtpHost();
		if( sSmtpHost == null && sZohoKey == null ) {
			logger.info("No email setup.  Either smtpHost or zohoKey is required to use email features.");
			return false;
		}
		if( sSmtpHost != null ) {
			MailMan.setDefaultHost(CivetConfig.getSmtpHost());
			MailMan.setDefaultPort(CivetConfig.getSmtpPortInt());
			MailMan.setSecurity(CivetConfig.getSmtpSecurity());
			sUserID = MailMan.getUserID();
			MailMan.setUserID(sUserID);
			if( !"NONE".equalsIgnoreCase(CivetConfig.getSmtpSecurity()) ) {
				if( (MailMan.getUserID() == null || MailMan.getPassword() == null) && bLogin ) {
					TwoLineQuestionDialog ask = new TwoLineQuestionDialog( "Civet Email Login:",
							"Email UserID:", "Email Password:", true);
					ask.setPassword(true);
					ask.setVisible(true);
					if( ask.isExitOK() ) {
						sUserID = ask.getAnswerOne();
						MailMan.setUserID(sUserID);
						MailMan.setPassword(ask.getAnswerTwo());
						bRet = true;
					}
					else {
						bRet = false;
					}
				}
			}
		}
		else if( sZohoKey != null ) {
			MailMan.setDefaultHost(CivetConfig.getZohoHost());
			sUserID = CivetConfig.getZohoUser();
			String sPassword = CivetConfig.getZohoPass();
			MailMan.setUserID(sUserID);
			MailMan.setPassword(sPassword);
			MailMan.setDefaultPort(CivetConfig.getZohoPortInt());
			MailMan.setSecurity(CivetConfig.getZohoSecurity());
		}
		String sFrom = CivetConfig.getEmailFrom();
		if( sFrom != null )
			MailMan.setDefaultFrom(sFrom);
		else if( !sUserID.contains("@")) {
			MailMan.setDefaultFrom(sUserID + CivetConfig.getSmtpDomain() );
		}
		else {
			MailMan.setDefaultFrom(sUserID);
		}
		String sReplyTo = CivetConfig.getEmailReplyTo();
		if( sReplyTo != null )
			MailMan.setDefaultReplyTo(sReplyTo);
		return bRet;
	}

	private static String getPath( String sProperty, String sDefault ) {
		return getPath( sProperty, sDefault, true);
	}
		
	private static String getPath( String sProperty, String sDefault, boolean bMandatory ) {
		String sRet = props.getProperty(sProperty);
		if( sRet == null || sRet.trim().length() == 0 ) {
			if( sDefault != null )
				sRet = sDefault;
			else
				exitError( sProperty );
		}
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			if( bMandatory ) {
				logger.error( "InputDirPath " + sRet + " does not exist or is not a folder");
				System.exit(1);
			}
			else
				return null;
		}
		sRet = f.getAbsolutePath();
		return sRet;
	}
	
	public synchronized static String getInputDirPath() {
		return getPath("InputDirPath", null);
	}
	
	public synchronized static String getToFileDirPath() {
		return getPath("ToBeFiledDirPath", null);
	}

	public synchronized static String getEmailOutDirPath() {
		return getPath("EmailOutDirPath", null);
	}

	public synchronized static String getEmsOutDirPath() {
		return getPath("emsOutputDirPath", ".\\EmsOutbox\\", false);
	}
	
	public synchronized static String getEmailOnlySendPath() {
		return getPath("EmailOnlySendPath", ".\\", false );
	}

	public synchronized static String getEmailOnlyEmailTemplate() {
		String sRet = props.getProperty("EmailOnlyMessage");
		if( sRet == null || sRet.trim().length() == 0 ) {
			sRet = "EmailOnlyMessage.txt";
		}
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "EmailOnlyMessage " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public synchronized static String getEmailOnlyPath() {
		return getPath("EmailOnlyInputPath", ".\\EmailOnlyIn\\", false);
	}
	
	public synchronized static String getEmailErrorsDirPath() {
		return getPath("EmailErrorsDirPath", null);
	}
	
	public synchronized static String getOutputDirPath() {
		return getPath("OutputDirPath", null);
	}
	
	public synchronized static String getBulkLoadDirPath() {
		return getPath("bulkLoadDirPath", ".\\Swine\\");
	}
	
	public synchronized static String getNineDashThreeLoadDirPath() {
		return getPath("nineDashThreeLoadDirPath", ".\\NineDashThree\\");
	}

	public synchronized static String getVspsDirPath() {
		return getPath("vspsDirPath", ".\\VSPSData\\");
	}
	
	public synchronized static String getZipcodeTableFile() {
		String sRet = props.getProperty("zipcodeTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("zipcodeTableFile");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public synchronized static String getCountiesTableFile() {
		String sRet = props.getProperty("countiesTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("countiesTableFile");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public synchronized static String getCountyAliasesTableFile() {
		String sRet = props.getProperty("countyAliasesTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("countyAliasesTableFile");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public synchronized static String getVetTableFile() {
		String sRet = props.getProperty("vetTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("vetTableFile");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public synchronized static boolean isCheckAccredStatus() {
		if( bCheckAccredStatus  == null ) {
			String sVal = props.getProperty("checkAccreditationStatus");
			if( sVal == null || sVal.trim().length() == 0 )
				bCheckAccredStatus = false;
			else if( sVal.equalsIgnoreCase("true") || sVal.equalsIgnoreCase("yes")) {
				bCheckAccredStatus = true;
			}
			else {
				bCheckAccredStatus = false;
			}
		}
		return bCheckAccredStatus;
	}	

	public synchronized static String getStateVetTableFile() {
		String sRet = props.getProperty("stateVetTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) { 
			exitError("stateVetTableFile");
			return null;
		}
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "stateVetTableFile " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public synchronized static String getSppTableFile() {
		String sRet = props.getProperty("sppTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) { 
			exitError("sppTableFile");
			return null;
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public synchronized static String getErrorTypeTableFile() {
		String sRet = props.getProperty("errorTypeTableFile");
		if( sRet == null || sRet.trim().length() == 0 )  { 
			exitError("errorTypeTableFile");
			return null;
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public synchronized static String getExportMailTemplate() {
		String sRet = props.getProperty("ExportEmailTemplate");
		if( sRet == null || sRet.trim().length() == 0 )  { 
			exitError("ExportEmailTemplate");
			return null;
		}
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "ExportEmailTemplate " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public synchronized static String getImportErrorsEmailTemplate() {
		String sRet = props.getProperty("ImportErrorsEmailTemplate");
		if( sRet == null || sRet.trim().length() == 0 )  { 
			exitError("ImportErrorsEmailTemplate");
			return null;
		}
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "ImportErrorsEmailTemplate " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public synchronized static String getImportErrorsLetterTemplate() {
		String sRet = props.getProperty("ImportErrorsLetterTemplate");
		if( sRet == null || sRet.trim().length() == 0 )  { 
			exitError("ImportErrorsLetterTemplate");
			return null;
		}
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "ImportErrorsLetterTemplate " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}


	public synchronized static String getCoKsXSLTFile() {
		String sRet = props.getProperty("CoKsXSLTFile");
		if( sRet == null || sRet.trim().length() == 0 )  { 
			exitError("xsltFile");
			return null;
		}
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "CoKsXSLTFile " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public synchronized static String getConveyanceXSLTFile() {
		String sRet = props.getProperty("ConveyanceXSLTFile");
		if( sRet == null || sRet.trim().length() == 0 )  { 
			exitError("xsltFile");
			return null;
		}
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "ConveyanceXSLTFile " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public synchronized static String getSchemaFile() {
		String sRet = props.getProperty("StdSchema");
		if( sRet == null || sRet.trim().length() == 0 )  { 
			exitError("StdSchema");
			return null;
		}
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "StdSchema " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	
	public synchronized static int getRotation() {
		int iRet = -1;
		String sRet = props.getProperty("rotation");
		if( sRet == null || sRet.trim().length() == 0 )  { 
			exitError("rotation");
		}
		try {
			iRet = Integer.parseInt(sRet);
		} catch( NumberFormatException nfe ) {
			logger.error( "Cannot read rotation " + sRet + " as an integer number");
			logger.error(nfe);
			System.exit(1);
		}
		return iRet;
	}

	public synchronized static long getMaxAttachSize() {
		long iRet = -1;
		long iMulti = 1;
		String sRet = props.getProperty("maxAttachSize");
		if( sRet == null || sRet.trim().length() == 0 )  { 
			exitError("maxAttachSize");
		}
		if( sRet.endsWith("K") ) iMulti = 1024;
		else if( sRet.endsWith("M")) iMulti = 1024*1024;
		if( !Character.isDigit(sRet.charAt(sRet.length()-1)) ) 
			sRet = sRet.substring(0,sRet.length()-1);
		try {
			iRet = Integer.parseInt(sRet);
			iRet *= iMulti;
		} catch( NumberFormatException nfe ) {
			logger.error( "Cannot read maxAttachSize " + sRet + " as an integer number");
			logger.error(nfe);
			System.exit(1);
		}
		return iRet;
	}
	
	public synchronized static long getWSTimeout() {
		long lTimeout = DEFAULT_WS_TIMEOUT; // Two minutes
		String sRet = props.getProperty("wsTimeout");
		if( sRet == null || sRet.trim().length() == 0 ) { 
			return DEFAULT_WS_TIMEOUT;
		}
		try {
			lTimeout = Long.parseLong(sRet);
		} catch( NumberFormatException nfe ) {
			logger.error( "Cannot read wsTimeout " + sRet + " as an long integer number");
			logger.error(nfe);
			return DEFAULT_WS_TIMEOUT;
		}
		
		return lTimeout;
	}
	
	public synchronized static long getMaxAnimals() {
		long iRet = -1;
		String sRet = props.getProperty("maxAnimals");
		if( sRet == null || sRet.trim().length() == 0 ) { 
			return DEFAULT_MAX_ANIMALS;
		}
		try {
			iRet = Integer.parseInt(sRet);
		} catch( NumberFormatException nfe ) {
			logger.error( "Cannot read maxAnimals " + sRet + " as an integer number");
			logger.error(nfe);
			return DEFAULT_MAX_ANIMALS;
		}
		return iRet;
	}
	
	/**
	 * get the string for the default purpose of movement but allow it to not exist.
	 * @return
	 */
	public synchronized static String getDefaultPurpose() {
		String sRet = props.getProperty("defaultPurpose");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	/**
	 * get the string for the default species but allow it to not exist.
	 * @return
	 */
	public synchronized static String getDefaultSpecies() {
		String sRet = props.getProperty("defaultSpecies");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public synchronized static boolean isJPedalXFA() {
		boolean bRet = false;
		String sVal = props.getProperty("supportXFA");
		if( sVal == null || sVal.equalsIgnoreCase("false") || sVal.equalsIgnoreCase("no")) {
			iJPedalType = LGPL;
		}
		if( iJPedalType == UNK ) {
			final String xfaClassPath="org/jpedal/objects/acroforms/AcroRendererXFA.class";
			ClassLoader loader = logger.getClass().getClassLoader();
			bRet = loader.getResource(xfaClassPath)!=null;
			if( bRet ) {
				iJPedalType = XFA;
			}
			else {
				iJPedalType = LGPL;
			}
		}
		else {
			bRet = ( iJPedalType == XFA );
		}
		return bRet;
	}
	
	public synchronized static boolean isAutoOpenPdf() {
		if( bAutoOpenPDF  == null ) {
			String sVal = props.getProperty("autoOpenPdf");
			if( sVal == null || sVal.trim().length() == 0 )
				bAutoOpenPDF = true;
			else if( sVal.equalsIgnoreCase("true") || sVal.equalsIgnoreCase("yes")) {
				bAutoOpenPDF = true;
			}
			else {
				bAutoOpenPDF = false;
			}
		}
		return bAutoOpenPDF;
	}

	public synchronized static String getAcrobatPath() {
		String sRet = props.getProperty("acrobatPath");
		if( sRet != null ) {
			File f = new File( sRet );
			if( !f.exists() || !f.isFile() ) {
				sRet = null;
			}
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	

	// These are only used by direct database "add ons" so don't start-up check but leave in.
	public synchronized static String getDbServer() {
		String sRet = "LPHStage"; 
		return sRet;
	}

	public synchronized static int getDbPort() {
		int iRet = 1433;
		return iRet;
	}


	public synchronized static String getDbDatabaseName() {
		String sRet = "USAHERDS"; 
		return sRet;
	}

	public synchronized static String getDbHerdsSchemaName() {
		String sRet = props.getProperty("dbHerdsSchemaName");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public synchronized static String getDbCivetSchemaName() {
		String sRet = props.getProperty("dbCivetSchemaName");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	

	// These are used to store db user credentials NOT as true config file data
	/**
	 * Get the Database UserName
	 * @return
	 */
	public synchronized static String getDBUserName() {
		return sDBUserName;
	}
	
	/**
	 * Get the Database Password 
	 * @return
	 */
	public synchronized static String getDBPassword() {
		return sDBPassword;
	}
	
	public synchronized static void setDBUserName( String sUser ) {
		sDBUserName = sUser;
	}
	
	public synchronized static void setDBPassword( String sPass ) {
		sDBPassword = sPass;
	}
	
	// Internal implementation details
	
	/**
	 * Generic crash out routine.
	 */
	private static void exitError( String sProp ) {
		if( SwingUtilities.isEventDispatchThread() )
			MessageDialog.showMessage(null, "Civet: Fatal Error in CivetConfig.txt", "Cannot read property " + sProp
						+ "\nDefault value assigned");
		else
			MessageDialog.showMessage(null, "Civet: Fatal Error in CivetConfig.txt", "Cannot read property " + sProp
						+ "\nDefault value assigned");
		ConfigDialog dialog = new ConfigDialog();
		dialog.setModal(true);;
		dialog.setVisible(true);
		int iFails = 1;
		while( iFails < 4 && !dialog.isComplete() ) {
			if( SwingUtilities.isEventDispatchThread() )
				MessageDialog.showMessage(null, "Civet: Fatal Error in CivetConfig.txt", "Still not complete");
			else
				MessageDialog.showMessage(null, "Civet: Fatal Error in CivetConfig.txt", "Still not complete");
			iFails++;
			dialog.setVisible(true);
		}
		if( iFails >= 4 ) {
			if( SwingUtilities.isEventDispatchThread() )
				MessageDialog.showMessage(null, "Civet: Fatal Error in CivetConfig.txt", "Still not complete");
			else
				MessageDialog.showMessage(null, "Civet: Fatal Error in CivetConfig.txt", "Still not complete");
			System.exit(1);
		}
		logger.error("Cannot read property " + sProp);
	}

	/**
	 * Generic crash out routine.
	 */
	private static void exitErrorImmediate( String sProp ) {
		if( SwingUtilities.isEventDispatchThread() )
			MessageDialog.showMessage(null, "Civet: Fatal Error in CivetConfig.txt", "Cannot read property " + sProp);
		else
			MessageDialog.showMessage(null, "Civet: Fatal Error in CivetConfig.txt", "Cannot read property " + sProp);
		logger.error("Cannot read property " + sProp);
		System.exit(1);
	}
	
	public synchronized static boolean writeConfigFile( String sFileName ) {
		boolean bRet = true;
		ConfigCsv config = new ConfigCsv();
		PrintWriter pw = null;
		try {
		pw = new PrintWriter( new FileOutputStream(sFileName) );
		while( config.nextTab() != null ) {
			pw.println("#" + config.getTab());
			while( config.nextRow() != null ) {
				String sKey = config.getName();
				String sValue = props.getProperty(sKey);
				if( sValue != null ) {
//					Replace \\ with \ and then \ with \\ to avoid quad quotes,etc.
					sValue = sValue.replace("\\\\", "\\");
					sValue = sValue.replace("\\", "\\\\");
					pw.println(sKey + "=" + sValue);
				}
				else {
					pw.println(sKey + "=");
				}
			}
			pw.println();
		}
		pw.flush();
		} catch( IOException ioe ) {
			bRet = false;
			logger.error(ioe);
		} finally {
			if( pw != null )
				pw.close();
		}
		return bRet;
	}
	
	public synchronized static void initConfig() {
		if( props == null ) {
			props = new Properties();
			FileInputStream fis = null;
			try {
				fis = new FileInputStream("CivetConfig.txt");
				props.load(fis);
			} catch (IOException e) {
				exitErrorImmediate("Cannot read configuration file CivetConfig.txt");
				return;
			} finally {
				if( fis != null )
					try {
						fis.close();
					} catch (IOException e) {
						logger.error(e);
					}
			}
		}
	}
	
	public synchronized static void checkAllConfig() {
		String sErr = checkAllConfigImp();
		int iFails = 1;
		while( sErr != null ) {
			if( iFails >= 4 ) {
				if( SwingUtilities.isEventDispatchThread() )
					MessageDialog.showMessage(null, "Civet: Fatal Error in CivetConfig.txt", "Still not complete");
				else
					MessageDialog.showMessage(null, "Civet: Fatal Error in CivetConfig.txt", "Still not complete");
				logger.error("Cannot read property " + sErr + " after 5 tries");
				System.exit(1);
			}
			if( SwingUtilities.isEventDispatchThread() )
				MessageDialog.showMessage(null, "Civet: Fatal Error in CivetConfig.txt", "Cannot read property " + sErr
							+ "\nDefault value assigned");
			else
				MessageDialog.showMessage(null, "Civet: Fatal Error in CivetConfig.txt", "Cannot read property " + sErr
							+ "\nDefault value assigned");
			ConfigDialog dialog = new ConfigDialog();
			dialog.setModal(true);;
			dialog.setVisible(true);
			sErr = checkAllConfigImp();
			iFails++;
		}
	}
		/**
		 * This method is designed to ensure that all necessary configuration is set.  
		 * Some may legitimately return null.  Remove them here and change error handling in 
		 * the individual get... methods.
		 * NOTE: Only test those that are required by the external release, i.e., outside of addons
		 * @return NULL if everything works or string with missing setting or file message
		 */
	public synchronized static String checkAllConfigImp() {
		initConfig();	
		String sRet = props.getProperty("standAlone");
		if( sRet == null || sRet.trim().length() == 0 ) return ("standAlone");
		sRet = props.getProperty("defaultDirection");
		if( sRet == null || sRet.trim().length() == 0 ) return ("defaultDirection");
		sRet = props.getProperty("homeStateAbbr");
		if( sRet == null || sRet.trim().length() == 0 ) return ("homeStateAbbr");
		sRet = props.getProperty("homeState");
		if( sRet == null || sRet.trim().length() == 0 ) return ("homeState");
		sRet = props.getProperty("homeStateKey");
		if( sRet == null || sRet.trim().length() == 0 ) return ("homeStateKey");
		sRet = props.getProperty("cviValidDays");
		if( sRet == null || sRet.trim().length() == 0 ) return ("cviValidDays");
		sRet = props.getProperty("herdsWebServiceURL");
		if( sRet == null || sRet.trim().length() == 0 ) return ("herdsWebServiceURL");
		sRet = props.getProperty("InputDirPath");
		if( sRet == null || sRet.trim().length() == 0 ) return ("InputDirPath");
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			return ( "InputDirPath\n" + sRet + " does not exist or is not a folder");			
		}
		sRet = props.getProperty("ToBeFiledDirPath");
		if( sRet == null || sRet.trim().length() == 0 ) return ("ToBeFiledDirPath");
		f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			return ( "ToBeFiledDirPath\n" + sRet + " does not exist or is not a folder");			
		}
		sRet = props.getProperty("EmailOutDirPath");
		if( sRet == null || sRet.trim().length() == 0 ) return ("EmailOutDirPath");
		f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			return ( "EmailOutDirPath\n" + sRet + " does not exist or is not a folder");			
		}
		sRet = props.getProperty("EmailErrorsDirPath");
		if( sRet == null || sRet.trim().length() == 0 ) return ("EmailErrorsDirPath");
		f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			return ( "EmailErrorsDirPath\n" + sRet + " does not exist or is not a folder");			
		}
		sRet = props.getProperty("OutputDirPath");
		if( sRet == null || sRet.trim().length() == 0 ) return ("OutputDirPath");
		f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			return ( "OutputDirPath\n" + sRet + " does not exist or is not a folder");			
		}
		sRet = props.getProperty("vetTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) return ("vetTableFile");
		sRet = props.getProperty("stateVetTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) return ("stateVetTableFile");
		f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			return ( "stateVetTableFile\n" + sRet + " does not exist or is not a file");			
		}
		
		sRet = props.getProperty("zipcodeTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) return ("zipcodeTableFile");
		f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			return ( "zipcodeTableFile\n" + sRet + " does not exist or is not a file");			
		}
		
		sRet = props.getProperty("countiesTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) return ("countiesTableFile");
		f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			return ( "countiesTableFile\n" + sRet + " does not exist or is not a file");			
		}
		
		sRet = props.getProperty("countyAliasesTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) return ("countyAliasesTableFile");
		f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			return ( "countyAliasesTableFile\n" + sRet + " does not exist or is not a file");			
		}
		
		sRet = props.getProperty("sppTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) return ("sppTableFile");
		sRet = props.getProperty("purposeTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) return ("purposeTableFile");
		sRet = props.getProperty("errorTypeTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) return ("errorTypeTableFile");
		sRet = props.getProperty("CoKsXSLTFile");
		if( sRet == null || sRet.trim().length() == 0 ) return ("xsltFile");
		f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			return ( "CoKsXSLTFile\n" + sRet + " does not exist or is not a file");			
		}
		sRet = props.getProperty("StdSchema");
		if( sRet == null || sRet.trim().length() == 0 ) return ("StdSchema");
		f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			return ( "StdSchema\n" + sRet + " does not exist or is not a file");			
		}
		sRet = props.getProperty("ExportEmailTemplate");
		if( sRet == null || sRet.trim().length() == 0 ) return ("ExportEmailTemplate");
		f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			return ( "ExportEmailTemplate\n" + sRet + " does not exist or is not a file");			
		}
		sRet = props.getProperty("ImportErrorsEmailTemplate");
		if( sRet == null || sRet.trim().length() == 0 ) return ("ImportErrorsEmailTemplate");
		f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			return ( "ImportErrorsEmailTemplate\n" + sRet + " does not exist or is not a file");			
		}
		sRet = props.getProperty("ImportErrorsLetterTemplate");
		if( sRet == null || sRet.trim().length() == 0 ) return ("ImportErrorsLetterTemplate");
		f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			return ( "ImportErrorsLetterTemplate\n" + sRet + " does not exist or is not a file");			
		}
		sRet = props.getProperty("rotation");
		if( sRet == null || sRet.trim().length() == 0 ) return ("rotation");
		try {
			Integer.parseInt(sRet);
		} catch( NumberFormatException nfe ) {
			return ( "Cannot read rotation\n" + sRet + " as an integer number");
		}
		sRet = props.getProperty("maxAttachSize");
		if( sRet == null || sRet.trim().length() == 0 ) return ("maxAttachSize");
		if( !Character.isDigit(sRet.charAt(sRet.length()-1)) ) 
			sRet = sRet.substring(0,sRet.length()-1);
		try {
			Integer.parseInt(sRet);
		} catch( NumberFormatException nfe ) {
			return ( "Cannot read maxAttachSize\n" + sRet + " as an integer number");			
		}
		return null;
	}		
}
