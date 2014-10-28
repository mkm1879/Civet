package edu.clemson.lph.db;
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
public interface DatabaseConnectionFactory {
	/**
	 * Because we can't know other implementation's DB details, we expect
	 * this class to be overridden by one that can send us a new DB Connection
	 * when makeDBConnection is called.  Login, errors, etc., should be handled 
	 * by the implemention of the factory.  If no connection can be made even after
	 * all this, return null.
	 * The connection is assumed to point to a USAHERDS database as a user with 
	 * the necessary write permissions to tables and history required to read accounts,
	 * vets, premises, etc. (read everything essentially) and write to the CVI related
	 * tables.  
	 * @return An open SQLServer database connection.  Will be closed on completion.
	 */
	public java.sql.Connection makeDBConnection();
	
	/**
	 * Provide the username of the login accessing the CVI database.  This
	 * will be used to populate createdByUserName and lastUpdatedByUserName
	 * entries needed for tracking history.
	 * @return String username, either USAHerds login or some other username used 
	 * to track this programs modifications to the DB.
	 */
	public String getUserName();
	public String getDatabaseServerName();
}
