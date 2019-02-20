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
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.SwingUtilities;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.webservice.CivetWebServices;
import edu.clemson.lph.civet.webservice.UsaHerdsWebServiceAuthentication;
import edu.clemson.lph.civet.webservice.WebServiceException;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.TwoLineQuestionDialog;
import edu.clemson.lph.mailman.MailMan;

public class CivetConfig {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
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
	private static Boolean bOpenAfterAdd = null;
	private static Boolean bCheckAccredStatus;
	

	public static Properties getProps() {
		return props;
	}

	/**
	 * Check to see if we are on LPH LAN.  Only used in local version.
	 * @return
	 */
	public static String[] listLocalNetAddresses() {
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
	
	public static boolean isStandAlone() {
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
	
	public static boolean isSmallScreen() {
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
	
	public static boolean isDefaultReceivedDate() {
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
	
	public static boolean isSaveCopies() {
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
	
	public static boolean isOpenAfterAdd() {
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
	
	public static boolean hasBrokenLIDs() {
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
	
	
	public static boolean hasStateIDChecksum() {
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

	public static void setStandAlone( boolean standAlone ) {
		bStandAlone = standAlone;
	}
	
	public static Level getLogLevel() {
		Level lRet = Level.ERROR;
		String sVal = props.getProperty("logLevel");
		if( sVal != null && sVal.equalsIgnoreCase("info") )
			lRet = Level.INFO;
		return lRet;
	}
	
	public static String getDefaultDirection() {
		String sRet = props.getProperty("defaultDirection");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("defaultDirection");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public static String getHomeStateAbbr() {
		String sRet = props.getProperty("homeStateAbbr");
		if( sRet == null || sRet.trim().length() == 0  ) exitError("homeStateAbbr");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public static String getHomeState() {
		String sRet = props.getProperty("homeState");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("homeState");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public static int getHomeStateKey() {
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
		
	public static int getCviValidDays() {
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

	
	public static String getSmtpHost() {
		String sRet = props.getProperty("smtpHost");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("smtpHost");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public static String getSmtpPort() {
		String sRet = props.getProperty("smtpPort");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("smtpPort");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public static String getSmtpSecurity() {
		String sRet = props.getProperty("smtpSecurity");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("smtpSecurity");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public static int getSmtpPortInt() {
		int iRet = -1;
		String sRet = props.getProperty("smtpPort");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("smtpPorty");
		try {
			iRet = Integer.parseInt(sRet);
		} catch( NumberFormatException nfe ) {
			logger.error( "Cannot read smtp Port " + sRet + " as an integer number");
			logger.error(nfe);
			System.exit(1);
		}
		return iRet;
	}
	
	public static String getSmtpDomain() {
		String sRet = props.getProperty("smtpDomain");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("smtpDomain");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public static String getEmailCopyTo() {
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
	public static String getEmailFrom() {
		String sRet = null;
		sRet = props.getProperty("emailFrom");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	/**
	 * Optional configuration setting to provide a Reply-to different from From
	 * @return String with Email From or NULL
	 */
	public static String getEmailReplyTo() {
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
	public static String getEmailTestTo() {
		String sRet = props.getProperty("emailTestTo");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	/**
	 * Get the URL for USAHERDS web services
	 * @return
	 */
	public static String getHERDSWebServiceURL() {
		String sRet = props.getProperty("herdsWebServiceURL");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("herdsWebServiceURL");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	/**
	 * Get the URL for USAHERDS web services
	 * @return
	 */
	public static String getHERDSWebServiceHost() {
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
	public static String getHERDSUserName() {
		return sHERDSUserName;
	}
	
	/**
	 * Get the Password USAHERDS web services
	 * @return
	 */
	public static String getHERDSPassword() {
		return sHERDSPassword;
	}
	
	public static void setHERDSUserName( String sUser ) {
		sHERDSUserName = sUser;
	}
	
	public static void setHERDSPassword( String sPass ) {
		sHERDSPassword = sPass;
	}
	
	public static boolean validateHerdsCredentials() {
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
			}
		} catch (Exception e) {
			logger.error("Error running main program in event thread", e);
		}
		return bRet;
		
	}

	public static void initWebServices() {
		String sUser = null;
		String sPass = null;
		try {
			if( CivetConfig.getHERDSUserName() == null || CivetConfig.getHERDSPassword() == null ) {
				boolean bValid = false;
				while( !bValid ) {
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
					try {
						CivetWebServices service = new CivetWebServices();
						bValid = service.validUSAHERDSCredentials(sUser, sPass);
						
					} catch( WebServiceException e ) {
						MessageDialog.showMessage(null, "Civet Login Error", "Error logging into USAHERDS\n"+ e.getMessage());
						bValid = false;
					}
				}
				setHERDSUserName( sUser );
				setHERDSPassword( sPass );
			}
		} catch (Exception e) {
			logger.error("Error running main program in event thread", e);
		}
		
	}
	
	public static void initDB() {
		
	}
	
	public static boolean initEmail(boolean bLogin) {
		boolean bRet = true;
		String sUserID = MailMan.getDefaultUserID();
		if( (MailMan.getDefaultUserID() == null || MailMan.getDefaultPassword() == null) && bLogin ) {
			TwoLineQuestionDialog ask = new TwoLineQuestionDialog( "Civet Email Login:",
					"Email UserID:", "Email Password:", true);
			ask.setPassword(true);
			ask.setVisible(true);
			if( ask.isExitOK() ) {
				sUserID = ask.getAnswerOne();
				MailMan.setDefaultUserID(sUserID);
				MailMan.setDefaultPassword(ask.getAnswerTwo());
				bRet = true;
			}
			else {
				bRet = false;
			}
		}
		MailMan.setDefaultHost(CivetConfig.getSmtpHost());
		MailMan.setDefaultPort(CivetConfig.getSmtpPortInt());
		String sSecurity = CivetConfig.getSmtpSecurity();
		MailMan.setSecurity(sSecurity);
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

	
	public static String getInputDirPath() {
		String sRet = props.getProperty("InputDirPath");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("InputDirPath");
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			logger.error( "InputDirPath " + sRet + " does not exist or is not a folder");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public static String getToFileDirPath() {
		String sRet = props.getProperty("ToBeFiledDirPath");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("ToBeFiledDirPath");
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			logger.error( "ToBeFiledDirPath " + sRet + " does not exist or is not a folder");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	

	public static String getEmailOutDirPath() {
		String sRet = props.getProperty("EmailOutDirPath");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("EmailOutDirPath");
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			logger.error( "EmailOutDirPath " + sRet + " does not exist or is not a folder");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public static String getEmailOnlySendPath() {
		String sRet = props.getProperty("EmailOnlySendPath");
		if( sRet == null || sRet.trim().length() == 0 ) {
			File fOut = new File( getOutputDirPath() );
			File fRoot = fOut.getParentFile();
			sRet = fRoot.getAbsolutePath() + "\\EmailOnlySend\\";
		}
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			logger.error( "EmailOnlySendPath " + sRet + " does not exist or is not a folder");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public static String getEmailOnlyEmailTemplate() {
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

	public static String getEmailOnlyPath() {
		String sRet = props.getProperty("EmailOnlyInputPath");
		if( sRet == null || sRet.trim().length() == 0 ) {
			File fOut = new File( getOutputDirPath() );
			File fRoot = fOut.getParentFile();
			sRet = fRoot.getAbsolutePath() + "\\EmailOnlyIn\\";
		}
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			logger.error( "EmailOnlyInputPath " + sRet + " does not exist or is not a folder");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public static String getEmailErrorsDirPath() {
		String sRet = props.getProperty("EmailErrorsDirPath");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("EmailErrorsDirPath");
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			logger.error( "EmailErrorsDirPath " + sRet + " does not exist or is not a folder");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public static String getOutputDirPath() {
		String sRet = props.getProperty("OutputDirPath");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("OutputDirPath");
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			logger.error( "OutputDirPath " + sRet + " does not exist or is not a folder");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public static String getBulkLoadDirPath() {
		String sRet = props.getProperty("bulkLoadDirPath");
		if( sRet != null ) {
			File f = new File( sRet );
			if( !f.exists() || !f.isDirectory() ) {
				logger.error( "bulkLoadDirPath " + sRet + " does not exist or is not a folder");
				sRet = "./";
			}
		}
			else sRet = "./";
		return sRet;
	}
	
	public static String getNineDashThreeLoadDirPath() {
		String sRet = props.getProperty("nineDashThreeLoadDirPath");
		if( sRet != null ) {
			File f = new File( sRet );
			if( !f.exists() || !f.isDirectory() ) {
				logger.error( "nineDashThreeLoadDirPath " + sRet + " does not exist or is not a folder");
				sRet = "./"; 
			}
		}
		else 
			sRet = "./"; 
		return sRet;
	}

	public static String getVspsDirPath() {
		String sRet = props.getProperty("vspsDirPath");
		if( sRet == null || sRet.trim().length() == 0 ) {
			logger.error( "vspsLoadDirPath not set.  Using install folder");
			return "./";
		}
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			logger.error( "vspsLoadDirPath " + sRet + " does not exist or is not a folder");
			sRet = "./";
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = "./"; 
		return sRet;
	}
	
	public static String getZipcodeTableFile() {
		String sRet = props.getProperty("zipcodeTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("zipcodeTableFile");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public static String getCountiesTableFile() {
		String sRet = props.getProperty("countiesTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("countiesTableFile");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public static String getCountyAliasesTableFile() {
		String sRet = props.getProperty("countyAliasesTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("countyAliasesTableFile");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public static String getVetTableFile() {
		String sRet = props.getProperty("vetTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("vetTableFile");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public static boolean isCheckAccredStatus() {
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

	public static String getStateVetTableFile() {
		String sRet = props.getProperty("stateVetTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("stateVetTableFile");
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "stateVetTableFile " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public static String getSppTableFile() {
		String sRet = props.getProperty("sppTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("sppTableFile");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public static String getErrorTypeTableFile() {
		String sRet = props.getProperty("errorTypeTableFile");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("errorTypeTableFile");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public static String getExportMailTemplate() {
		String sRet = props.getProperty("ExportEmailTemplate");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("ExportEmailTemplate");
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "ExportEmailTemplate " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public static String getImportErrorsEmailTemplate() {
		String sRet = props.getProperty("ImportErrorsEmailTemplate");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("ImportErrorsEmailTemplate");
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "ImportErrorsEmailTemplate " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public static String getImportErrorsLetterTemplate() {
		String sRet = props.getProperty("ImportErrorsLetterTemplate");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("ImportErrorsLetterTemplate");
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "ImportErrorsLetterTemplate " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}


	public static String getCoKsXSLTFile() {
		String sRet = props.getProperty("CoKsXSLTFile");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("xsltFile");
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "CoKsXSLTFile " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public static String getSchemaFile() {
		String sRet = props.getProperty("StdSchema");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("StdSchema");
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "StdSchema " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	
	public static int getRotation() {
		int iRet = -1;
		String sRet = props.getProperty("rotation");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("rotation");
		try {
			iRet = Integer.parseInt(sRet);
		} catch( NumberFormatException nfe ) {
			logger.error( "Cannot read rotation " + sRet + " as an integer number");
			logger.error(nfe);
			System.exit(1);
		}
		return iRet;
	}

	public static long getMaxAttachSize() {
		long iRet = -1;
		long iMulti = 1;
		String sRet = props.getProperty("maxAttachSize");
		if( sRet == null || sRet.trim().length() == 0 ) exitError("maxAttachSize");
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
	
	public static long getWSTimeout() {
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
	
	public static long getMaxAnimals() {
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
	public static String getDefaultPurpose() {
		String sRet = props.getProperty("defaultPurpose");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	/**
	 * get the string for the default species but allow it to not exist.
	 * @return
	 */
	public static String getDefaultSpecies() {
		String sRet = props.getProperty("defaultSpecies");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}
	
	public static boolean isJPedalXFA() {
		boolean bRet = false;
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
	
	public static boolean isAutoOpenPdf() {
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

	public static String getAcrobatPath() {
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
	public static String getDbServer() {
		String sRet = "LPHSQL"; 
		return sRet;
	}

	public static int getDbPort() {
		int iRet = 1433;
		return iRet;
	}


	public static String getDbDatabaseName() {
		String sRet = "USAHERDS"; 
		return sRet;
	}

	public static String getDbHerdsSchemaName() {
		String sRet = props.getProperty("dbHerdsSchemaName");
		if( sRet != null && sRet.trim().length() == 0 ) 
			sRet = null; 
		return sRet;
	}

	public static String getDbCivetSchemaName() {
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
	public static String getDBUserName() {
		if( sDBUserName == null || sDBPassword == null ) 
			initDB();
		return sDBUserName;
	}
	
	/**
	 * Get the Database Password 
	 * @return
	 */
	public static String getDBPassword() {
		if( sDBUserName == null || sDBPassword == null ) 
			initDB();
		return sDBPassword;
	}
	
	public static void setDBUserName( String sUser ) {
		sDBUserName = sUser;
	}
	
	public static void setDBPassword( String sPass ) {
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
			MessageDialog.messageWait(null, "Civet: Fatal Error in CivetConfig.txt", "Cannot read property " + sProp
						+ "\nDefault value assigned");
		ConfigDialog dialog = new ConfigDialog();
		dialog.setModal(true);;
		dialog.setVisible(true);
		int iFails = 1;
		while( iFails < 4 && !dialog.isComplete() ) {
			if( SwingUtilities.isEventDispatchThread() )
				MessageDialog.showMessage(null, "Civet: Fatal Error in CivetConfig.txt", "Still not complete");
			else
				MessageDialog.messageWait(null, "Civet: Fatal Error in CivetConfig.txt", "Still not complete");
			iFails++;
			dialog.setVisible(true);
		}
		if( iFails >= 4 ) {
			if( SwingUtilities.isEventDispatchThread() )
				MessageDialog.showMessage(null, "Civet: Fatal Error in CivetConfig.txt", "Still not complete");
			else
				MessageDialog.messageWait(null, "Civet: Fatal Error in CivetConfig.txt", "Still not complete");
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
			MessageDialog.messageWait(null, "Civet: Fatal Error in CivetConfig.txt", "Cannot read property " + sProp);
		logger.error("Cannot read property " + sProp);
		System.exit(1);
	}
	
	public static boolean writeConfigFile( String sFileName ) {
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
	
	public static void initConfig() {
		if( props == null ) {
			props = new Properties();
			FileInputStream fis = null;
			try {
				fis = new FileInputStream("CivetConfig.txt");
				props.load(fis);
			} catch (IOException e) {
				exitErrorImmediate("Cannot read configuration file CivetConfig.txt");
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
	
	public static void checkAllConfig() {
		String sErr = checkAllConfigImp();
		int iFails = 1;
		while( sErr != null ) {
			if( iFails >= 4 ) {
				if( SwingUtilities.isEventDispatchThread() )
					MessageDialog.showMessage(null, "Civet: Fatal Error in CivetConfig.txt", "Still not complete");
				else
					MessageDialog.messageWait(null, "Civet: Fatal Error in CivetConfig.txt", "Still not complete");
				logger.error("Cannot read property " + sErr + " after 5 tries");
				System.exit(1);
			}
			if( SwingUtilities.isEventDispatchThread() )
				MessageDialog.showMessage(null, "Civet: Fatal Error in CivetConfig.txt", "Cannot read property " + sErr
							+ "\nDefault value assigned");
			else
				MessageDialog.messageWait(null, "Civet: Fatal Error in CivetConfig.txt", "Cannot read property " + sErr
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
	public static String checkAllConfigImp() {
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
		sRet = props.getProperty("smtpHost");
		if( sRet == null || sRet.trim().length() == 0 ) return ("smtpHost");
		sRet = props.getProperty("smtpPort");
		if( sRet == null || sRet.trim().length() == 0 ) return ("smtpPort");
		sRet = props.getProperty("smtpSecurity");
		if( sRet == null || sRet.trim().length() == 0 ) return ("smtpIsTls");
		sRet = props.getProperty("smtpPort");
		if( sRet == null || sRet.trim().length() == 0 ) return ("smtpPorty");
		sRet = props.getProperty("smtpDomain");
		if( sRet == null || sRet.trim().length() == 0 ) return ("smtpDomain");
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
