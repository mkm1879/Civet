package edu.clemson.lph.civet;
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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jpedal.PdfDecoder;

import edu.clemson.lph.civet.webservice.CivetWebServices;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.TwoLineQuestionDialog;

public class CivetConfig {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	// initialized in Civet.main via checkAllConfig
	private static Properties props;
	private static final int UNK = -1;
	private static final int LGPL = 0;
	private static final int XFA = 1;
	private static final long DEFAULT_MAX_ANIMALS = 100;
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
	

	/**
	 * Check to see if we are on LPH LAN.  Only used in local version.
	 * @return
	 */
	public static String[] listLocalNetAddresses() {
		String sAddresses = props.getProperty("localNetAddresses");
		if( sAddresses == null ) exitError("localNetAddresses");
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
			if( sVal == null ) 
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
			if( sVal == null )
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
			if( sVal == null )
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
	
	public static boolean hasBrokenLIDs() {
		if( bBrokenLIDs == null ) {
			String sVal = props.getProperty("brokenLIDs");
			// default to true;
			if( sVal == null || sVal.equalsIgnoreCase("true") || sVal.equalsIgnoreCase("yes")) {
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
			if( sVal == null || sVal.equalsIgnoreCase("true") || sVal.equalsIgnoreCase("yes")) {
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
		if( sRet == null ) exitError("defaultDirection");
		return sRet;
	}

	public static String getHomeStateAbbr() {
		String sRet = props.getProperty("homeStateAbbr");
		if( sRet == null ) exitError("homeStateAbbr");
		return sRet;
	}

	public static String getHomeState() {
		String sRet = props.getProperty("homeState");
		if( sRet == null ) exitError("homeState");
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
		if( sRet == null ) exitError("cviValidDays");
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
		if( sRet == null ) exitError("smtpHost");
		return sRet;
	}
	
	public static String getSmtpPort() {
		String sRet = props.getProperty("smtpPort");
		if( sRet == null ) exitError("smtpPort");
		return sRet;
	}

	public static String getSmtpSecurity() {
		String sRet = props.getProperty("smtpSecurity");
		if( sRet == null ) exitError("smtpSecurity");
		return sRet;
	}
	
	public static int getSmtpPortInt() {
		int iRet = -1;
		String sRet = props.getProperty("smtpPort");
		if( sRet == null ) exitError("smtpPorty");
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
		if( sRet == null ) exitError("smtpDomain");
		return sRet;
	}
	
	public static String getEmailCopyTo() {
		String sRet = props.getProperty("emailCopyTo");
		if( sRet == null ) exitError("emailCopyTo");
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
		return sRet;
	}
	
	/**
	 * Optional configuration setting to provide a Reply-to different from From
	 * @return String with Email From or NULL
	 */
	public static String getEmailReplyTo() {
		String sRet = null;
		sRet = props.getProperty("emailReplyTo");
		return sRet;
	}
	
	/**
	 * return the email address for testing to override real state vet CVI addresses
	 * This getter does not issue an error if not found because that is normal.
	 * @return
	 */
	public static String getEmailTestTo() {
		String sRet = props.getProperty("emailTestTo");
		return sRet;
	}
	
	/**
	 * Get the URL for USAHERDS web services
	 * @return
	 */
	public static String getHERDSWebServiceURL() {
		String sRet = props.getProperty("herdsWebServiceURL");
		if( sRet == null ) exitError("herdsWebServiceURL");
		return sRet;
	}
	
	/**
	 * Get the URL for USAHERDS web services
	 * @return
	 */
	public static String getHERDSWebServiceHost() {
		String sRet = props.getProperty("herdsWebServiceURL");
		if( sRet == null ) exitError("herdsWebServiceURL");
		try {
			URI uri = new URI(sRet);
			sRet = uri.getHost();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
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
					bValid = CivetWebServices.validUSAHERDSCredentials(sUser, sPass);
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
	
	public static String getInputDirPath() {
		String sRet = props.getProperty("InputDirPath");
		if( sRet == null ) exitError("InputDirPath");
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			logger.error( "InputDirPath " + sRet + " does not exist or is not a folder");
			System.exit(1);
		}
		return sRet;
	}
	
	public static String getToFileDirPath() {
		String sRet = props.getProperty("ToBeFiledDirPath");
		if( sRet == null ) exitError("ToBeFiledDirPath");
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			logger.error( "ToBeFiledDirPath " + sRet + " does not exist or is not a folder");
			System.exit(1);
		}
		return sRet;
	}
	

	public static String getEmailOutDirPath() {
		String sRet = props.getProperty("EmailOutDirPath");
		if( sRet == null ) exitError("EmailOutDirPath");
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			logger.error( "EmailOutDirPath " + sRet + " does not exist or is not a folder");
			System.exit(1);
		}
		return sRet;
	}
	
	public static String getEmailErrorsDirPath() {
		String sRet = props.getProperty("EmailErrorsDirPath");
		if( sRet == null ) exitError("EmailErrorsDirPath");
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			logger.error( "EmailErrorsDirPath " + sRet + " does not exist or is not a folder");
			System.exit(1);
		}
		return sRet;
	}
	
	public static String getOutputDirPath() {
		String sRet = props.getProperty("OutputDirPath");
		if( sRet == null ) exitError("OutputDirPath");
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			logger.error( "OutputDirPath " + sRet + " does not exist or is not a folder");
			System.exit(1);
		}
		return sRet;
	}
	
	public static String getBulkLoadDirPath() {
		String sRet = props.getProperty("bulkLoadDirPath");
		if( sRet == null ) exitError("bulkLoadDirPath");
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			logger.error( "bulkLoadDirPath " + sRet + " does not exist or is not a folder");
			System.exit(1);
		}
		return sRet;
	}

	public static String getVspsDirPath() {
		String sRet = props.getProperty("vspsDirPath");
		if( sRet == null ) {
			logger.error( "vspsLoadDirPath not set using install folder");
			return ".";
		}
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			logger.error( "vspsLoadDirPath " + sRet + " does not exist or is not a folder");
			sRet = ".";
		}
		return sRet;
	}

	public static String getRobotInputPath() {
		String sRet = props.getProperty("robotInputPath");
		if( sRet == null ) exitError("robotInputPath");
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			logger.error( "robotInputPath " + sRet + " does not exist or is not a folder");
			System.exit(1);
		}
		return sRet;
	}

	public static String getRobotCompleteOutPath() {
		String sRet = props.getProperty("robotCompleteOutPath");
		if( sRet == null ) exitError("robotCompleteOutPath");
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			logger.error( "robotCompleteOutPath " + sRet + " does not exist or is not a folder");
			System.exit(1);
		}
		return sRet;
	}

	public static String getRobotXMLOutPath() {
		String sRet = props.getProperty("robotXMLOutPath");
		if( sRet == null ) exitError("robotXMLOutPath");
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			logger.error( "robotXMLOutPath " + sRet + " does not exist or is not a folder");
			System.exit(1);
		}
		return sRet;
	}
	
	public static int getRobotWaitSeconds() {
		int iRet = -1;
		String sRet = props.getProperty("robotWaitSeconds");
		if( sRet == null ) exitError("robotWaitSeconds");
		try {
			iRet = Integer.parseInt(sRet);
		} catch( NumberFormatException nfe ) {
			logger.info( "Cannot read robot wait time " + sRet + " as an integer number");
			logger.error(nfe);
			System.exit(1);
		}
		return iRet;
	}

	public static String getVetTableFile() {
		String sRet = props.getProperty("vetTableFile");
		if( sRet == null ) exitError("vetTableFile");
		return sRet;
	}

	public static String getStateVetTableFile() {
		String sRet = props.getProperty("stateVetTableFile");
		if( sRet == null ) exitError("stateVetTableFile");
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "stateVetTableFile " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		return sRet;
	}

	public static String getSppTableFile() {
		String sRet = props.getProperty("sppTableFile");
		if( sRet == null ) exitError("sppTableFile");
		return sRet;
	}

	public static String getErrorTypeTableFile() {
		String sRet = props.getProperty("errorTypeTableFile");
		if( sRet == null ) exitError("errorTypeTableFile");
		return sRet;
	}

	public static String getRobotOutputFormat() {
		String sRet = props.getProperty("robotOutputFormat");
		if( sRet == null ) 
			exitError("robotOutputFormat");
		if( !sRet.equals("STD") && !sRet.equals("ADOBE") ) 
			exitError("Unknown robot output format: " + sRet );
		return sRet;
	}
	
	public static String getExportMailTemplate() {
		String sRet = props.getProperty("ExportEmailTemplate");
		if( sRet == null ) exitError("ExportEmailTemplate");
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "ExportEmailTemplate " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		return sRet;
	}
	
	public static String getImportErrorsEmailTemplate() {
		String sRet = props.getProperty("ImportErrorsEmailTemplate");
		if( sRet == null ) exitError("ImportErrorsEmailTemplate");
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "ImportErrorsEmailTemplate " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		return sRet;
	}

	public static String getImportErrorsLetterTemplate() {
		String sRet = props.getProperty("ImportErrorsLetterTemplate");
		if( sRet == null ) exitError("ImportErrorsLetterTemplate");
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "ImportErrorsLetterTemplate " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		return sRet;
	}


	public static String getCoKsXSLTFile() {
		String sRet = props.getProperty("CoKsXSLTFile");
		if( sRet == null ) exitError("xsltFile");
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "CoKsXSLTFile " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		return sRet;
	}

	public static String getSchemaFile() {
		String sRet = props.getProperty("StdSchema");
		if( sRet == null ) exitError("StdSchema");
		File f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			logger.error( "StdSchema " + sRet + " does not exist or is not a file");
			System.exit(1);
		}
		return sRet;
	}

	
	public static int getRotation() {
		int iRet = -1;
		String sRet = props.getProperty("rotation");
		if( sRet == null ) exitError("rotation");
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
		if( sRet == null ) exitError("maxAttachSize");
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
	
	public static long getMaxAnimals() {
		long iRet = -1;
		String sRet = props.getProperty("maxAnimals");
		if( sRet == null ) { 
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
	public static boolean isJPedalXFA() {
		boolean bRet = false;
		if( iJPedalType == UNK ) {
			// do something that requires XFA and set value;
			PdfDecoder decoder = new PdfDecoder();
			try {
				decoder.getFormRenderer();
				iJPedalType = XFA;
				bRet = true;
			} catch( java.lang.NoSuchMethodError e ) {
				iJPedalType = LGPL;
				bRet = false;
			}
		}
		else {
			bRet = (iJPedalType == XFA);
		}
		return bRet;
	}
	

	// These are only used by direct database "add ons" so don't start-up check but leave in.
	public static String getDbServer() {
		String sRet = props.getProperty("dbServer");
		if( sRet == null ) exitError("dbServer");
		return sRet;
	}

	public static int getDbPort() {
		int iRet = -1;
		String sRet = props.getProperty("dbPort");
		if( sRet == null ) exitError("dbPort");
		try {
			iRet = Integer.parseInt(sRet);
		} catch( NumberFormatException nfe ) {
			logger.error( "Cannot read dbPort " + sRet + " as an integer number");
			logger.error(nfe);
			System.exit(1);
		}
		return iRet;
	}

	public static String getDbPortString() {
		String sRet = props.getProperty("dbPort");
		if( sRet == null ) exitError("dbPort");
		return sRet;
	}

	public static String getDbDatabaseName() {
		String sRet = props.getProperty("dbDatabaseName");
		if( sRet == null ) exitError("dbDatabaseName");
		return sRet;
	}

	public static String getDbHerdsSchemaName() {
		String sRet = props.getProperty("dbHerdsSchemaName");
		if( sRet == null ) exitError("dbHerdsSchemaName");
		return sRet;
	}

	public static String getDbCivetSchemaName() {
		String sRet = props.getProperty("dbCivetSchemaName");
		if( sRet == null ) exitError("dbCivetSchemaName");
		return sRet;
	}

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
	
	/**
	 * Generic crash out routine.
	 */
	private static void exitError( String sProp ) {
		MessageDialog.messageWait(null, "Civet: Fatal Error in CivetConfig.txt", "Cannot read property " + sProp);
		logger.error("Cannot read property " + sProp);
	}
	
	/**
	 * Generic crash out routine.
	 */
	private static void exitErrorImmediate( String sProp ) {
		MessageDialog.showMessage(null, "Civet: Fatal Error in CivetConfig.txt", "Cannot read property " + sProp);
		logger.error("Cannot read property " + sProp);
		System.exit(1);
	}
	
	/**
	 * This method is designed to ensure that all necessary configuration is set.  
	 * Some may legitimately return null.  Remove them here and change error handling in 
	 * the individual get... methods.
	 * NOTE: Only test those that are required by the external release, i.e., outside of addons
	 */
	public static void checkAllConfig() {
		props = new Properties();
		try {
			props.load(new FileInputStream("CivetConfig.txt"));
		} catch (IOException e) {
			exitErrorImmediate("Cannot read configuration file CivetConfig.txt");
		}
		String sRet = props.getProperty("standAlone");
		if( sRet == null ) exitErrorImmediate("standAlone");
		sRet = props.getProperty("defaultDirection");
		if( sRet == null ) exitErrorImmediate("defaultDirection");
		sRet = props.getProperty("homeStateAbbr");
		if( sRet == null ) exitErrorImmediate("homeStateAbbr");
		sRet = props.getProperty("homeState");
		if( sRet == null ) exitErrorImmediate("homeState");
		sRet = props.getProperty("homeStateKey");
		if( sRet == null ) exitErrorImmediate("homeStateKey");
		sRet = props.getProperty("cviValidDays");
		if( sRet == null ) exitErrorImmediate("cviValidDays");
		sRet = props.getProperty("smtpHost");
		if( sRet == null ) exitErrorImmediate("smtpHost");
		sRet = props.getProperty("smtpPort");
		if( sRet == null ) exitErrorImmediate("smtpPort");
		sRet = props.getProperty("smtpSecurity");
		if( sRet == null ) exitErrorImmediate("smtpIsTls");
		sRet = props.getProperty("smtpPort");
		if( sRet == null ) exitErrorImmediate("smtpPorty");
		sRet = props.getProperty("smtpDomain");
		if( sRet == null ) exitErrorImmediate("smtpDomain");
		sRet = props.getProperty("herdsWebServiceURL");
		if( sRet == null ) exitErrorImmediate("herdsWebServiceURL");
		sRet = props.getProperty("InputDirPath");
		if( sRet == null ) exitErrorImmediate("InputDirPath");
		File f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			exitErrorImmediate( "InputDirPath\n" + sRet + " does not exist or is not a folder");			
		}
		sRet = props.getProperty("ToBeFiledDirPath");
		if( sRet == null ) exitErrorImmediate("ToBeFiledDirPath");
		f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			exitErrorImmediate( "ToBeFiledDirPath\n" + sRet + " does not exist or is not a folder");			
		}
		sRet = props.getProperty("EmailOutDirPath");
		if( sRet == null ) exitErrorImmediate("EmailOutDirPath");
		f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			exitErrorImmediate( "EmailOutDirPath\n" + sRet + " does not exist or is not a folder");			
		}
		sRet = props.getProperty("EmailErrorsDirPath");
		if( sRet == null ) exitErrorImmediate("EmailErrorsDirPath");
		f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			exitErrorImmediate( "EmailErrorsDirPath\n" + sRet + " does not exist or is not a folder");			
		}
		sRet = props.getProperty("OutputDirPath");
		if( sRet == null ) exitErrorImmediate("OutputDirPath");
		f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			exitErrorImmediate( "OutputDirPath\n" + sRet + " does not exist or is not a folder");			
		}
		sRet = props.getProperty("robotInputPath");
		if( sRet == null ) exitErrorImmediate("robotInputPath");
		f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			exitErrorImmediate( "robotInputPath\n" + sRet + " does not exist or is not a folder");			
		}
		sRet = props.getProperty("robotCompleteOutPath");
		if( sRet == null ) exitErrorImmediate("robotCompleteOutPath");
		f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			exitErrorImmediate( "robotCompleteOutPath\n" + sRet + " does not exist or is not a folder");			
		}
		sRet = props.getProperty("robotXMLOutPath");
		if( sRet == null ) exitErrorImmediate("robotXMLOutPath");
		f = new File( sRet );
		if( !f.exists() || !f.isDirectory() ) {
			exitErrorImmediate( "robotXMLOutPath\n" + sRet + " does not exist or is not a folder");			
		}
		sRet = props.getProperty("vetTableFile");
		if( sRet == null ) exitErrorImmediate("vetTableFile");
		sRet = props.getProperty("stateVetTableFile");
		if( sRet == null ) exitErrorImmediate("stateVetTableFile");
		f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			exitErrorImmediate( "stateVetTableFile\n" + sRet + " does not exist or is not a file");			
		}
		sRet = props.getProperty("sppTableFile");
		if( sRet == null ) exitErrorImmediate("sppTableFile");
		sRet = props.getProperty("purposeTableFile");
		if( sRet == null ) exitErrorImmediate("purposeTableFile");
		sRet = props.getProperty("errorTypeTableFile");
		if( sRet == null ) exitErrorImmediate("errorTypeTableFile");
		sRet = props.getProperty("robotOutputFormat");
		if( sRet == null ) exitErrorImmediate("robotOutputFormat");
		if( !sRet.equals("STD") && !sRet.equals("ADOBE") ) 
			exitErrorImmediate("Unknown robot output format:\n" + sRet );
		sRet = props.getProperty("CoKsXSLTFile");
		if( sRet == null ) exitErrorImmediate("xsltFile");
		f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			exitErrorImmediate( "CoKsXSLTFile\n" + sRet + " does not exist or is not a file");			
		}
		sRet = props.getProperty("StdSchema");
		if( sRet == null ) exitErrorImmediate("StdSchema");
		f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			exitErrorImmediate( "StdSchema\n" + sRet + " does not exist or is not a file");			
		}
		sRet = props.getProperty("ExportEmailTemplate");
		if( sRet == null ) exitErrorImmediate("ExportEmailTemplate");
		f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			exitErrorImmediate( "ExportEmailTemplate\n" + sRet + " does not exist or is not a file");			
		}
		sRet = props.getProperty("ImportErrorsEmailTemplate");
		if( sRet == null ) exitErrorImmediate("ImportErrorsEmailTemplate");
		f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			exitErrorImmediate( "ImportErrorsEmailTemplate\n" + sRet + " does not exist or is not a file");			
		}
		sRet = props.getProperty("ImportErrorsLetterTemplate");
		if( sRet == null ) exitErrorImmediate("ImportErrorsLetterTemplate");
		f = new File( sRet );
		if( !f.exists() || !f.isFile() ) {
			exitErrorImmediate( "ImportErrorsLetterTemplate\n" + sRet + " does not exist or is not a file");			
		}
		sRet = props.getProperty("rotation");
		if( sRet == null ) exitErrorImmediate("rotation");
		try {
			Integer.parseInt(sRet);
		} catch( NumberFormatException nfe ) {
			exitErrorImmediate( "Cannot read rotation\n" + sRet + " as an integer number");
		}
		sRet = props.getProperty("maxAttachSize");
		if( sRet == null ) exitErrorImmediate("maxAttachSize");
		if( !Character.isDigit(sRet.charAt(sRet.length()-1)) ) 
			sRet = sRet.substring(0,sRet.length()-1);
		try {
			Integer.parseInt(sRet);
		} catch( NumberFormatException nfe ) {
			exitErrorImmediate( "Cannot read maxAttachSize\n" + sRet + " as an integer number");			
		}
	}			
}
