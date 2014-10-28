package edu.clemson.lph.civet.addons;
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
import java.sql.Connection;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetConfig;
import edu.clemson.lph.db.DatabaseConnectionFactory;
import edu.clemson.lph.dialogs.TwoLineQuestionDialog;

public class InitAddOns {
	public static final Logger logger = Logger.getLogger(Civet.class.getName());
	public static SCDatabaseConnectionFactory myFactory = null;

	public InitAddOns() {
		// TODO Auto-generated constructor stub
	}
	
	public static DatabaseConnectionFactory getFactory() {
		initAddOns();
		return myFactory;
	}
	
	public synchronized static void initAddOns() {
		if( myFactory == null ) {
			String sUserName = null;
			String sPassword = null;
			String sServerName = CivetConfig.getDbServer();
			try {
				// Do setup for your local database here!
				myFactory = new SCDatabaseConnectionFactory();
				myFactory.setServerName(sServerName);
				Connection conn = null;
				TwoLineQuestionDialog dlg = new TwoLineQuestionDialog( "Database Login", "UserID", "Password", true );
				dlg.setIntro("Database Login Settings");
				while( conn == null ) {
					dlg.setPassword(true);
					dlg.setVisible(true);
					if( !dlg.isExitOK() ) {
						System.exit(1);
					}
					sUserName = dlg.getAnswerOne();
					sPassword = dlg.getAnswerTwo();
					myFactory.setUserName(sUserName);
					myFactory.setPassword(sPassword);
					conn = myFactory.makeDBConnection();
				}
				conn.close();
			} catch (Exception e) {
				logger.error("Error running main program in event thread", e);
			}
		}
	}

}
