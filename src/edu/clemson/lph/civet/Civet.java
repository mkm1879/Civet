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
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import edu.clemson.lph.civet.lookup.LookupFilesGenerator;
import edu.clemson.lph.civet.prefs.CivetConfig;
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
		if( args.length >= 2 ) {
			CivetConfig.setHERDSUserName( args[0] );
			CivetConfig.setHERDSPassword( args[1] );
		}
		StdErrLog.tieSystemErrToLog();
		CivetConfig.validateHerdsCredentials();
//		if( !CivetInbox.VERSION.contains("XFA") )
//			System.setProperty("org.jpedal.jai","true");
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
	private static String readVersion() {
		String sRet = null;
		try {
			InputStream iVersion = Civet.class.getResourceAsStream("res/Version.txt");
			try ( Scanner s = new Scanner(iVersion).useDelimiter("\\A") ) {
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
	
	/**
	 * Hide constructor.  This class is ONLY a static main for configuration and startup.
	 */
	private Civet() {
	}
}
