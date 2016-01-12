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
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;

import org.apache.log4j.Logger;

import edu.clemson.lph.civet.Civet;
import edu.clemson.lph.civet.CivetConfig;
import edu.clemson.lph.db.DatabaseConnectionFactory;
import edu.clemson.lph.dialogs.*;


/**
 * Very simple example of a DatabaseConnectionFactory.  The idea is to put site specific
 * implementation details into these support classes for easy modification for each site.
 * This is in addition to the text configuration files because I can't anticipate every variant.
 * For example, an office that uses Windows authentication to their SQL Server would need to 
 * modify the connection URI to show that.
 *
 */
public class SCDatabaseConnectionFactory implements DatabaseConnectionFactory {
	private final static Logger logger = Logger.getLogger(Civet.class.getName());
	private String sServerName = CivetConfig.getDbServer();
	private String sServerPort = CivetConfig.getDbPortString();
	private int iServerPort = CivetConfig.getDbPort();
	private String sDatabaseName = CivetConfig.getDbDatabaseName();
	private String sUserName;
	private String sPassword;
	
	public SCDatabaseConnectionFactory() {
		checkNetwork();
		checkSQL();
	}
	
	/**
	 * TODO Enhance error handling
	 */
	public void setServerName( String sServer ) { this.sServerName = sServer; }
	public void setUserName( String sUserName ) { this.sUserName = sUserName; }
	public void setPassword( String sPassword ) { this.sPassword = sPassword; }
		
	  
	public synchronized DatabaseConnectionFactory getFactory() {
		SCDatabaseConnectionFactory factory = new SCDatabaseConnectionFactory();
		factory.setServerName(sServerName);
		factory.setUserName(sUserName);
		factory.setPassword(sPassword);
		return factory;
	}
	  
	@Override
	public Connection makeDBConnection() {
		Connection conn = null;
		try {
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		String sURI = "jdbc:sqlserver://" + sServerName + ":" + sServerPort +
				";user=" + sUserName + ";password=" + sPassword + ";databaseName=" + sDatabaseName;
		conn = java.sql.DriverManager.getConnection(sURI);
		conn.setAutoCommit(true);
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		} catch ( Exception e ) {
			logger.error(e);
		}
		return conn;
	}

	@Override
	public String getUserName() {
		return sUserName;
	}
	@Override
	public String getDatabaseServerName() {
		return sServerName;
	}

	  // Returns false failures
	  private void checkNetwork() {
	    boolean bGood = false;
	    String[] aPingAddresses = CivetConfig.listLocalNetAddresses();
	    for( int i = 0; i < aPingAddresses.length; i++ ) {
	      if (ping(new InetSocketAddress(aPingAddresses[i], 80))) {
	        bGood = true;
	        logger.info("Network Good at " + aPingAddresses[i]);
	        break;
	      }
	    }
	    if( !bGood ) {
	    	MessageDialog.showMessage(null, "DB Connection", "Not on Database Local Network");
	    }
	  }

	  // Returns false failures
	  private void checkSQL() {
	    boolean bGood = false;
	    String[] aPingAddresses = CivetConfig.listLocalNetAddresses();
	    for( int i = 0; i < aPingAddresses.length; i++ ) {
	      if (pingSQL(new InetSocketAddress(aPingAddresses[i], 80))) {
	        bGood = true;
	        logger.info("SQL Good at " + aPingAddresses[i]);
	        break;
	      }
	    }
	    if( !bGood ) {
	    	MessageDialog.showMessage(null, "DB Connection", "Cannot connect on port 1433");
	    }
	  }

	  private boolean ping( InetSocketAddress saAddress ) {
	    boolean bRet = false;
	    try {
	      Socket t = new Socket();
	      t.connect(saAddress, 80);
	      bRet = true;
	      t.close();
	    }
	    catch ( java.net.SocketTimeoutException ex ) {
	      bRet = false;
	    }
	    catch (IOException e) {
	      e.printStackTrace();
	    }
	    return bRet;
	  }


	  private boolean pingSQL( InetSocketAddress saAddress ) {
	    boolean bRet = false;
	    try {
	      Socket t = new Socket();
	      t.connect(saAddress, iServerPort);
	      bRet = true;
	      t.close();
	    }
	    catch ( java.net.SocketTimeoutException ex ) {
	      bRet = false;
	    }
	    catch (IOException e) {
	      e.printStackTrace();
	    }
	    return bRet;
	  }

}
