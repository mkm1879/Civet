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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.PrintStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import edu.clemson.lph.civet.lookup.LookupFilesGenerator;
import edu.clemson.lph.civet.prefs.CivetConfig;
import edu.clemson.lph.dialogs.ProgressDialog;
import edu.clemson.lph.utils.StdErrLog;

import edu.clemson.lph.logging.Logger;

public class Civet {
       private static Logger logger = Logger.getLogger();
	static {
	     logger.setLevel("Info");
	}
	
	private static ProgressDialog prog;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		CivetConfig.checkAllConfig();
		logger.setLevel(CivetConfig.getLogLevel());
//		logger.info("Civet running build: " + CivetConfig.getVersion());
		if( args.length >= 2 ) {
			CivetConfig.setHERDSUserName( args[0] );
			CivetConfig.setHERDSPassword( args[1] );
		}
		StdErrLog.tieSystemErrToLog();
	    try {
			System.setOut(new PrintStream(new FileOutputStream("stdout.log")));
		} catch (FileNotFoundException e1) {
			logger.error(e1);
		}
		CivetConfig.validateHerdsCredentials();
		logger.info("Civet running build: " + CivetConfig.getVersion());
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
	 * Hide constructor.  This class is ONLY a static main for configuration and startup.
	 */
	private Civet() {
	}
}
