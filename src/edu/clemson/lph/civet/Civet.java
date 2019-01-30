package edu.clemson.lph.civet;
/*
Copyright 2014-2018 Michael K Martin

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

Civet is a tool that we in the South Carolina State Veterinarian's office have found useful.  
We make it freely available to any other animal health authority in hopes that it will promote 
greater data interoperability, especially around animal disease traceability.  The only thing 
we ask in return is that you keep us informed of your use so that we can properly plan any 
enhancements and bug-fixes.  At least semi-annually please send your Java and Civet version 
numbers, the number of instances of Civet you are running in test and production, and any 
unusually details of your application to mmarti5@clemson.edu.
*/

import java.awt.EventQueue;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import edu.clemson.lph.civet.files.SourceFileException;
import edu.clemson.lph.civet.lookup.LookupFilesGenerator;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.civet.robot.COKSRobot;
import edu.clemson.lph.civet.webservice.UsaHerdsWebServiceAuthentication;
import edu.clemson.lph.civet.webservice.WebServiceException;
import edu.clemson.lph.dialogs.MessageDialog;
import edu.clemson.lph.dialogs.ProgressDialog;
import edu.clemson.lph.utils.StdErrLog;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Civet {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	static {
		// BasicConfigurator replaced with PropertyConfigurator.
	     PropertyConfigurator.configure("CivetConfig.txt");
	     logger.setLevel(Level.INFO);
	}
	private static ProgressDialog prog;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		// BasicConfigurator replaced with PropertyConfigurator.
		PropertyConfigurator.configure("CivetConfig.txt");
		// Fail now so config file and required files can be fixed before work is done.
		CivetConfig.checkAllConfig();
		logger.setLevel(CivetConfig.getLogLevel());
		CivetInbox.VERSION = readVersion();
		logger.info("Civet running build: " + CivetInbox.VERSION);
		if( args.length == 1  && !args[0].startsWith("-")) {
			String sFile = args[0];
			if( sFile != null && ( sFile.toLowerCase().endsWith(".cvi") || sFile.toLowerCase().endsWith(".pdf")) ) {
				previewFile( sFile );
			}
		}
		else if( args.length >= 1 && args[0].toLowerCase().equals("-robot") ) {
			logger.info("Starting in Robot Mode");
			if( !CivetConfig.isJPedalXFA() ) {
				logger.error("Robot mode requires JPedalXFA" );
				System.exit(1);
			}
			if( args.length >= 3 ) {
				CivetConfig.setHERDSUserName( args[1] );
				CivetConfig.setHERDSPassword( args[2] );
			}
			COKSRobot robbie;
			try {
				CivetConfig.checkRobotConfig();
				robbie = new COKSRobot();
				robbie.start();
				System.out.println("Running in Robot Mode\nMinimize but do not close this Command Window");
			} catch (IOException e) {
				logger.error("Failed to start robot", e);
				System.exit(1);
			}
		}
		else {
			if( args.length >= 2 ) {
				CivetConfig.setHERDSUserName( args[0] );
				CivetConfig.setHERDSPassword( args[1] );
			}
			StdErrLog.tieSystemErrToLog();
			CivetConfig.validateHerdCredentials();
			if( !CivetInbox.VERSION.contains("XFA") )
				System.setProperty("org.jpedal.jai","true");
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						String os = System.getProperty("os.name");
						if( os.toLowerCase().contains("mac os") ) {
							System.setProperty("apple.laf.useScreenMenuBar", "true");
							System.setProperty("com.apple.mrj.application.apple.menu.about.name",
									"Civet");
							System.setProperty("com.apple.mrj.application.growbox.intrudes",
									"false");
							UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
						}
						else {
							UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
						}
					} 
					catch (Exception e) {
						logger.error(e.getMessage());
					}
					if( CivetConfig.isStandAlone() ) {
						if( !LookupFilesGenerator.checkLookupFiles() ) {
							throw( new RuntimeException("Failed to load lookup tables"));
						}
						else {
							new CivetInbox();
						}
					}
					else {
						prog = new ProgressDialog(null, "Civet: Cache", "Updating lookup tables from USAHERDS database");
						prog.setAuto(true);
						prog.setVisible(true);
						(new Thread() {
							public void run() {
								try {
									CivetConfig.initWebServices();
									LookupFilesGenerator gen = new LookupFilesGenerator();
									gen.generateAllLookups();
									SwingUtilities.invokeLater(new Runnable() {
										public void run() {
											prog.setVisible(false);
											new CivetInbox();
										}
									});
								} catch (Exception e) {
									logger.error("Error running main program in event thread", e);
								}
							}
						}).start();
					}
				}
			});
	     }
	}
	
	private static String readVersion() {
		String sRet = null;
		try {
			InputStream iVersion = Civet.class.getResourceAsStream("res/Version.txt");
			Scanner s = new Scanner(iVersion).useDelimiter("\\A");
			sRet = s.hasNext() ? s.next() : "";
			s.close();
			iVersion.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
		return sRet;
	}

	private static boolean stillValid() {
		boolean bRet = false;
		java.util.Date dNow = new java.util.Date();
		SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd");
		try {
			java.util.Date dNewYear = df.parse("2016-01-01");
			if( dNow.before(dNewYear) )
				bRet = true;
			else {
				MessageDialog.showMessage(null, "Civet Error: Expired", "The JPedal XFA trial has expired.  Please use Civet.jar with a valid JPedal library file");
			}
		} catch (ParseException e) {
			logger.error(e);
		}
		
		
		return bRet;
	}
	
	private static void previewFile( String sFile ) {
		if( sFile == null || sFile.trim().length() == 0 ) return;
		try {
			File f = new File(sFile);
			ArrayList<File> files = new ArrayList<File>(); files.add(f);
			CivetEditDialog dlg;
			dlg = new CivetEditDialog( (Window)null, files );
			dlg.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			File fFile = new File(sFile);
			if( !fFile.exists() ) return;
			String sFileName = fFile.getName();
			String sInbox = CivetConfig.getInputDirPath();
			String sMoveTo = sInbox + sFileName;
			if( !(sMoveTo.equalsIgnoreCase(sFile)) ){
				File fMoveTo = new File( sMoveTo );
				if( !fFile.renameTo(fMoveTo) ) {
					MessageDialog.showMessage(dlg, "Civet: Preview Error", "Failed to move file\n" + sFile + "\n\t to Civet InBox\nOpening in Preview");
				}
				else {
					fFile = fMoveTo;
				}
			}
			File selectedFiles[] = {fFile};
			//		dlg.openFiles(selectedFiles, true);
			dlg.setVisible(true);
			MessageDialog.showMessage(dlg, "Civet: Preview", "File\n" + sFile + "\n\tmoved to Civet InBox\nOpening in Preview");
		} catch (SourceFileException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
	}
	
	/**
	 * Hide constructor.  This class is ONLY a static main for configuration and startup.
	 */
	private Civet() {
	}
}
